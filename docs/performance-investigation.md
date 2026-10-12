# Chess App Performance Investigation

Date: 2026-10-12
Investigator: Claude Code, using the installed `android-profiler` skill
(Perfetto recording/analysis workflows) plus manual read-only code review.

## Evidence legend

- **[MEASURED]** — backed by a Perfetto trace queried with `trace_processor` (concrete timestamps/durations), or by an `adb shell am start -W` measurement.
- **[CODE-REVIEW]** — inferred by reading source; plausible and specific, but not exercised under a profiler in this investigation. Treat as a hypothesis, not a confirmed bottleneck.

---

## 1. Environment

| | |
|---|---|
| Device | MIRAY / TPM4G_E9863 (manufacturer string `NB55`), physical device connected via wireless ADB (adb-tls) |
| Android version | 12 (API 31), security patch 2022-11-05 |
| ABI | arm64-v8a |
| RAM | ~2.9 GB total (`MemTotal` from `/proc/meminfo`) |
| Storage | ~8.7 GB partition, **91% full, ~765 MB free** at time of testing |
| Build under test | `app-debug` (debug build, unminified, single-ABI `arm64-v8a` — the full 4-ABI debug build is 298 MB and would not fit in the device's free storage; the arm64-only rebuild is 100 MB) |
| App package | `dev.rootchess.app` (Gradle `namespace` is `com.example.chess`; `MainActivity` component is `dev.rootchess.app/com.example.chess.MainActivity`) |
| Profiling tool | Perfetto `trace_processor` (v57.2) + `record_android_trace`, both from the `android-profiler` skill's bundled references; SQL queries run via a local `trace_processor server http` session |

**Important device caveat:** this device is the user's personal daily-driver phone (WhatsApp, a POS app, etc. were observed live on it during testing), not a dedicated clean test rig. It is also low on free storage and low on free RAM (`MemAvailable` ~1 GB at the time of inspection) relative to its 2.9 GB total. Absolute timings below are specific to this device's current state (storage pressure, background apps, thermal state at test time) and are not necessarily representative of a flagship device or a factory-fresh device — but the *relative* breakdown (what fraction of startup is dex loading vs. app code vs. Stockfish) is informative regardless of device.

Because this is a shared personal device, the investigation was limited to one clean, repeatable scenario (cold start) captured via a scripted, non-interactive `adb shell am start -W` + Perfetto recording. A second interactive scenario (in-app navigation) was intentionally **not** captured via blind on-device UI taps once it became clear the screen could contain personal content; the gameplay/post-game-analysis findings below are code-review-only for that reason.

---

## 2. Scenario tested: cold start

**Why this scenario:** cold start is the first, universal experience of every user and the scenario explicitly supported by the installed profiler skill's `android.startup` tooling. Code reading also surfaced several concrete startup-path hypotheses (a hardcoded splash delay, a possibly-blocking engine-process spawn) worth checking against real data.

**Method:**
1. `adb shell am force-stop dev.rootchess.app` (clear any warm state).
2. Perfetto recording started (`record_android_trace`, custom config: `sched`, process fork/exec, `gfx`/`view`/`wm`/`am`/`dalvik`/`binder_driver` atrace categories, `android.surfaceflinger.frametimeline`, `linux.process_stats`), with the script waiting for ftrace to actually be enabled on-device before proceeding — see "Limitations" for why this mattered.
3. `adb shell am start -W -n dev.rootchess.app/com.example.chess.MainActivity` (blocks until first-frame-displayed and reports `TotalTime`).
4. Recording captured for 30s total, covering the whole launch plus ~25s of idle home-screen time afterward.

**Headline measurement [MEASURED]:**

```
LaunchState: COLD
TotalTime: 4750   (a repeat run measured 4811ms — consistent, ~4.75–4.8s)
WaitTime:  4759
```

A cold start of **~4.8 seconds** on this device, confirmed by two independent runs and corroborated by the trace's own `launching: dev.rootchess.app` async slice, which measured `dur = 4,705,862,881 ns` (4706 ms) — matching the `am start -W` figure to within measurement noise.

### 2.1 Where the 4.7 seconds goes — [MEASURED], from `trace_processor` queries against the app's main thread (`tid` resolved via `thread.name` + `thread_track` join; all timestamps are relative to `dispatchingStartProcess:dev.rootchess.app`)

| Phase (relative to process start) | Duration | Evidence |
|---|---|---|
| Process fork → `bindApplication` begins | 0 – ~0.27s | `dispatchingStartProcess`, `Start proc:`, `WMS#createSurface … Splash Screen` slices |
| `bindApplication` (ActivityThread binding: `Application.onCreate`, content-provider attach, static init) | **1588.7 ms total**, starting ~0.27s in | `slice.name = 'bindApplication'`, single slice on main thread |
| ART dex loading (first-run, no AOT/baseline profile) — `OpenDexFilesFromOat`, `Extract dex file`, `Verify dex file` across the APK's dex files (multidex; the app ships 20+ `classesN.dex` files) | **Extract: 632 ms, Verify: 752 ms** (sum of all `Extract dex file*` / `Verify dex file*` slices inside the cold-start window) — nested inside/overlapping `bindApplication` | `group by s.name` aggregation, bounded to the launch window |
| First Choreographer frame (splash screen's first composition) | **797 ms** single frame, ~2.10s–2.90s | `Choreographer#doFrame 816656`, dur=797,393,614 ns |
| Gap (≈590 ms) between first and second big frame — consistent with, but not a direct measurement of, the app's hardcoded `delay(500)` splash dismissal (`MainActivity.kt`, `LaunchedEffect(Unit) { delay(500); showSplash = false }`) | ~0.59s | Inferred from frame boundaries; `delay()` itself emits no trace slice |
| **Second Choreographer frame — the frame that actually shows the real UI and ends the launch** | **1219.9 ms**, ~3.48s–4.70s | `Choreographer#doFrame 816703`, dur=1,219,924,614 ns |
| → within that one frame: Hilt constructs 5 `@HiltViewModel`s for the first time, each needing first-touch ART class verification of generated Dagger factories | `VerifyClass …HiltViewModelKt`, `…HiltViewModelFactory`, `…ChessGameViewModel_HiltModules…`, `…ChatViewModel_HiltModules…`, `…PaywallViewModel_HiltModules…`, `…PuzzleViewModel_HiltModules…`, `…RoadmapViewModel_HiltModules…` — individually 15–580 µs each but dozens of them, first-touch only | main-thread slice dump, rel. ts 3.50–3.64s |
| → within the same frame: Room database first opened (`PuzzleDatabase`) | `VerifyClass androidx.room.Room`, `…RoomDatabase$Builder` (2.69 ms alone), `…JournalMode`, `…MigrationContainer`, SQLite open-helper factory classes — all first-touch | rel. ts 3.52–3.56s |
| → within the same frame: **Stockfish engine subprocess spawned** | A new OS process named `.../lib/arm64/lib_chess.so` (pid distinct from the app's pid) first appears in the trace at **rel. ts ≈ 3.64s** — i.e. inside this same 1.22s frame, immediately after/alongside the Hilt/Room class-verification burst | `thread`/`process` tables, `name glob '*chess*'` excluding the app's own `rootchess` thread |
| → within the same frame: initial board/screen measure pass | **526 ms** across 4 `measure`/`AndroidOwner:onMeasure` slices | aggregated `measure` + `AndroidOwner:onMeasure` slices in-window |
| `activityStart` / `performCreate:com.example.chess.MainActivity` (framework overhead) | 224 ms / 150 ms | named slices |

**Systemic confound check [MEASURED]:** main-thread `thread_state` breakdown for the whole 4.7s window: **83% (`3908/4706 ms`) `Running`** (actually executing on a CPU core), 14% `S` (sleeping — benign), 1.7% `D` (disk I/O wait), <1% waiting runnable-but-not-scheduled. This rules out CPU scheduling contention or I/O stalls as the dominant cause — the main thread had the CPU whenever it needed it and simply had ~3.9 seconds of real work queued up. CPU-frequency counters were not available in this trace (kernel didn't expose the counter path Perfetto reads), so thermal throttling as a *contributing* factor cannot be fully ruled out, but the high `Running` percentage argues against it being the primary explanation.

### 2.2 Root cause, in plain language [MEASURED + narrative synthesis]

The app is not slow because of one bug; it's slow because of **lazy initialization colliding with ART cold-start**. Nothing above the `MainActivity`/splash layer runs any app code until the Compose tree for the real (post-splash) screen is first composed — which is also the very first moment Hilt, Room, the 5 ViewModels, and the Stockfish engine all get touched for the first time. Because this debug build has no baseline profile and no AOT compilation, every one of those classes has to be verified/interpreted by ART from scratch, synchronously, inside a single Choreographer frame — which is why one single frame callback takes **1.22 seconds**. The earlier **797ms** frame and **~1.4s** of raw dex extract+verify time show this isn't confined to that one frame either — ART class-loading cost is paid throughout the whole cold start, it just concentrates catastrophically at the one moment everything is instantiated at once.

### 2.3 Confirmed vs. refuted hypotheses from code review

- **[MEASURED] CONFIRMED (contributing, not sole cause):** `ChessGameViewModel`'s `init { viewModelScope.launch { game.stockfish.startStockfish(...) } }` (`ChessGameViewModel.kt:283-288`) does run on the main thread — `viewModelScope` defaults to `Dispatchers.Main.immediate`, and `StockfishEngine.startStockfish()` calls `Runtime.getRuntime().exec(...)` synchronously (`StockfishEngine.kt:66`). The trace confirms the `lib_chess.so` process is created during the same main-thread frame as ViewModel construction. The *fork+exec itself* is not separately broken out in this trace (it's not an atrace-instrumented slice), so its individual cost versus the surrounding class-verification cost can't be isolated from this data — but it is, at minimum, additional synchronous main-thread work stacked on top of everything else happening in that frame.
- **[CODE-REVIEW, now de-prioritized]** The hardcoded `delay(500)` splash dismissal (`MainActivity.kt:57`) is real and does add a fixed 500ms to every cold start — but it is dwarfed by the ~3.9s of `Running` time elsewhere. Fixing it alone would improve cold start by ~10%, not fix the real problem.

---

## 3. Scenario NOT captured live: gameplay / post-game analysis — [CODE-REVIEW ONLY]

As noted in §1, a second live scenario was not captured on this device for privacy reasons (it's the user's personal phone). The following are **code-review inferences only** — plausible, specific, and worth profiling later on a dedicated test device, but **not confirmed by any trace**.

- **`GamePlay.analyzeMoveScores()`** (`GamePlay.kt:426-477`), triggered by tapping "Analyze Game": the per-move loop (`movesPlayed.indices.map { analyzeSingleMove(...) }`, replaying the board from move 0 for every ply via `replayBoardUpTo`, plus `TacticsRecognizer` pattern matching) runs as a plain synchronous function. Its caller, `ChessGameViewModel.analyzeGame()` (`ChessGameViewModel.kt:492-526`), launches it via a bare `viewModelScope.launch { }` with **no `withContext(Dispatchers.Default)`** — meaning this CPU-bound work, which scales with game length, executes on the main thread. Plausible symptom: a UI freeze proportional to move count when analysis finishes.
- Same call path: `OpeningBookRepository.loadBook()` (`OpeningBookRepository.kt:24-36`) lazily parses a JSON asset (`context.assets.open(...).readText()` + manual `JSONArray` parsing) the first time `findOpening()` is called inside `analyzeMoveScores()` — also on whatever thread called it, i.e. likely the main thread for the same reason above.
- **`MoveExplainer`'s debug-only capture-to-disk** (`MoveExplainer.kt:125-131`): a synchronous `File.appendText()` call with no dispatcher switch in its call chain, firing once per AI move-explanation and once per chat message in debug builds. Low severity (sub-ms disk append) but real, and worth removing/backgrounding regardless.
- `BoardScreen.kt:538`: `val whitePiecesKilled = remember { piecesKilled.filter { ... } }` is missing its `remember` key (contrast with `blackPiecesKilled` two lines below it, which correctly uses `remember(piecesKilled)`). This is a **correctness bug** (White's captured-pieces strip would never update after the first capture), not strictly a performance bug, but flagged here since it was found during this pass.
- `BoardScreen.kt:1071-1092`, `calculateMaterialAdvantage()`: allocates a fresh 12-entry `Map` and iterates `piecesKilled` on every `ChessGrid` recomposition (i.e. on every move), unmemoized. Likely negligible in absolute terms (small list, small map) but a trivially avoidable allocation in a hot path.
- `AnimatedChessPiece` (`BoardScreen.kt:796-848`) wraps `derivedStateOf` inside `remember(piece.position, squareSize)` — this is the classic no-benefit `derivedStateOf` pattern (the "state" that changes is the `remember` key itself, which already forces recreation; `derivedStateOf` adds no extra skipping here). Minor overhead, not a correctness issue.

---

## 4. Prioritized recommendations

| Priority | Bottleneck | Evidence | Recommended change | Expected benefit | Risk |
|---|---|---|---|---|---|
| **P0** | Cold start ≈ 4.8s, dominated by first-touch ART class verification (Hilt factories, Room, ViewModel graph) concentrated into one 1.22s frame | [MEASURED] §2.1–2.2 | Ship a **Baseline Profile** (AGP 8.2+ Baseline Profile Generator + Macrobenchmark, per the `generating-baseline-profiles` skill) covering cold startup through first-frame. This lets ART AOT-compile/pre-verify the hot classes (Hilt factories, Room, Compose runtime, the 5 ViewModels) instead of interpreting them cold. | Cited industry gains are ~30% faster startup; given 83% of this cold start is raw `Running` CPU time on uncompiled code, a baseline profile directly targets the actual bottleneck. | Low — purely additive, generated artifact; must be regenerated when the ViewModel/DI graph changes meaningfully, or it under-covers the new code paths. |
| **P1** | All 5 `@HiltViewModel`s, Room, and the Stockfish engine are first-touched simultaneously because `hiltViewModel()` is only called once splash dismisses | [MEASURED] §2.1 (Hilt/Room/Stockfish all first-appear in the same frame) | Spread initialization: construct `ChessGameViewModel` (and therefore trigger Hilt/Room setup) *earlier* — e.g. during the splash's own composition/`LaunchedEffect`, instead of gating it behind `showSplash`. This doesn't reduce total work, but moves it out of the one frame that currently has to do everything at once, letting the splash's own frames absorb some of the cost instead of blocking first-content-frame. | Converts one 1.22s janky frame into the splash period already appearing to the user as a loading screen — perceived responsiveness improves even if wall-clock total doesn't drop much (though a Baseline Profile would also reduce it). | Low-medium — requires restructuring where `hiltViewModel()` is called relative to the splash state machine; must verify the splash doesn't dismiss before the ViewModel is ready. |
| **P2** | `StockfishEngine.startStockfish()` calls `Runtime.getRuntime().exec()` synchronously inside `viewModelScope.launch { }` (defaults to `Dispatchers.Main.immediate`) | [MEASURED, partially] §2.3 — process spawn confirmed co-located with main-thread work in the same frame; exact exec() cost not isolated in this trace | Wrap the call in `withContext(Dispatchers.IO) { game.stockfish.startStockfish(...) }` in `ChessGameViewModel.kt:284-287`. | Removes one more synchronous main-thread operation from the already-overloaded startup frame; `Process.exec` + pipe setup is a syscall-heavy operation that has no reason to run on the main thread. | Very low — mechanical dispatcher change; `sendCommand("uci")` right after should stay sequenced after start completes, which `withContext` preserves. |
| **P3** | Hardcoded `delay(500)` before splash dismissal (`MainActivity.kt:57`) | [CODE-REVIEW] confirmed present in source; code-reading, not trace-measured duration (it's a known constant) | Replace the fixed delay with a real readiness signal (e.g. dismiss once `ChessGameViewModel` and Stockfish are ready, or at minimum reduce the constant) | Up to 500ms off every cold start, independent of device | Very low |
| **P4** (unverified — code review only) | `analyzeGame()` runs CPU-bound analysis (`analyzeMoveScores`, `TacticsRecognizer`, board replay) on the main thread with no dispatcher override | [CODE-REVIEW] §3 | Wrap `game.analyzeMoveScores(...)` in `ChessGameViewModel.analyzeGame()` with `withContext(Dispatchers.Default) { ... }` | Removes a plausible main-thread freeze on "Analyze Game" tap, scaling with move count | Low — must keep the `onProgress` callback's `_analysisProgress.value = ...` write properly dispatched back to Main (StateFlow writes are thread-safe, but confirm UI reads remain correct) |
| **P5** (unverified — code review only, also a correctness bug) | `BoardScreen.kt:538` `remember` missing its key | [CODE-REVIEW] §3 | Add the missing key: `remember(piecesKilled) { piecesKilled.filter { ... } }` | Fixes a real staleness bug (White's captured-pieces strip never updates); also makes recomposition behavior correct/predictable | Very low |

**Verification plan for P0–P3:** re-run the exact same scripted cold-start capture (`record_android_trace` + `am start -W`) after each change, compare `TotalTime` and the `bindApplication`/Choreographer-frame breakdown above. This was not done in this investigation — see Limitations.

---

## 5. Limitations and follow-up

- **No before/after comparison was performed.** This investigation establishes a baseline only; no code changes were made (per the read-only investigation constraint), so §4's "expected benefit" figures are inferred from the measured breakdown plus general Baseline Profile literature, not verified on this app.
- **Only one live scenario (cold start) was profiled.** Gameplay, post-game analysis, puzzle-list scrolling, and chat were all skipped for live profiling because the test device turned out to be the user's personal phone, and further blind UI automation risked touching personal data (a WhatsApp conversation was visible in a screenshot taken to plan navigation; it was deleted immediately off-device and not used for anything). A dedicated/clean test device or emulator would be needed to safely automate those scenarios.
- **First recording attempt had a methodology flaw, corrected in the second:** the first cold-start trace used a 15-second recording window started several tool-calls before the actual `am start`, and multi-turn conversational latency ate into that window — the capture was truncated after only ~1.26s of the app's actual startup activity (though it still produced one genuine finding: ~920ms of dex extract/verify in that partial window, consistent with the fuller second capture's ~1.4s figure). The second attempt used a single non-interactive shell script to tightly sequence "start recording → wait for ftrace armed → launch app → wait 30s" with no conversational round-trips in between, and is the capture all of §2's analysis is based on. Anyone reproducing this should do the same (tight scripting, not interactively).
- **CPU frequency / thermal counters were unavailable** in this trace (the `cpu_frequency` counter path wasn't populated), so thermal throttling cannot be fully ruled out as a contributing factor, only deprioritized based on the high `Running`-state percentage.
- **This device is storage- and RAM-constrained** (765 MB free storage, ~1 GB available RAM out of 2.9 GB total) and is an older/lower-tier chipset (Android 12, 2022 security patch). Absolute timings will very likely be better on a modern flagship, but the *relative* finding — that first-touch ART class verification dominates cold start on a debug, non-AOT build — generalizes to any device without a baseline profile, just with different absolute numbers.
- **Follow-up suggested:** (1) implement and measure a Baseline Profile (P0) with before/after Macrobenchmark numbers; (2) get access to a dedicated test device/emulator to safely profile the gameplay and post-game-analysis scenarios live; (3) re-run this same cold-start capture on a `release`-configured, minified build, since R8/dex optimization and resource shrinking may materially change the dex-count and class-verification picture from what was measured here on the unminified debug build.
