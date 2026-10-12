# Stockfish MultiPV Integration Plan for Chess App

## Purpose

The chess app already has:

- Custom chess/game logic
- Stockfish integration
- Tactic detection
- Opening detection
- LLM-powered move explanations

The goal of adding **Stockfish MultiPV** is to move the app from simply identifying the best move toward a richer chess-learning and coaching system.

The key idea:

> MultiPV should not just expose more engine moves. It should give the app enough information to understand the quality of a player's decision, identify critical moments, compare alternatives, and provide better context to the LLM.

---

# 1. What MultiPV Adds

Normal Stockfish analysis might return:

```text
Best move: Nf3
Evaluation: +0.8
```

With `MultiPV = 3`, Stockfish returns multiple candidate moves:

| Rank | Move | Evaluation | Meaning |
|---|---|---:|---|
| 1 | Nf3 | +0.8 | Best |
| 2 | d4 | +0.6 | Strong alternative |
| 3 | Bc4 | +0.2 | Playable, but less accurate |

The important part is not simply showing three moves.

The app should compare the candidates and determine how meaningful the differences are.

For example:

```text
Nf3   +0.8
d4    +0.7
Bc4   +0.6
```

means something very different from:

```text
Nf3   +0.9
d4    +0.2
Bc4   -0.4
```

The first position has several good choices.

The second position has a much narrower set of good moves.

---

# 2. Improve Game Review

Instead of giving the LLM only:

```text
Position:
...

Best move: Nf3
Evaluation: +0.8
Played move: d4
```

provide structured analysis:

```text
Position:
...

Played move:
d4

Engine candidates:
1. Nf3  +0.8
2. d4   +0.6
3. Bc4  +0.2

Evaluation after played move:
+0.6

Tactical opportunities:
None

Opening:
Italian Game

Position characteristics:
- White has development advantage
- Black king is uncastled
- Center is contested
```

The LLM can then explain:

> You played d4, which is still a strong move. Nf3 was slightly more precise because it develops a piece while keeping pressure on the center. Your move doesn't create a major problem, but it gives Black a little more flexibility.

This is more useful than:

> Nf3 is the best move.

---

# 3. Detect the Quality of a Decision

MultiPV can help distinguish between:

- Best move
- Strong alternative
- Reasonable alternative
- Inaccuracy
- Mistake
- Critical mistake
- Tactical opportunity
- Forced/near-forced position

## Example A: Many Good Moves

```text
Nf3   +0.8
d4    +0.7
Bc4   +0.6
```

Possible classification:

```text
MANY_GOOD_MOVES
```

The player should not be heavily penalized for choosing the second-best move.

## Example B: Critical Decision

```text
Nf3   +0.9
d4    +0.2
Bc4   -0.4
```

Possible classification:

```text
CRITICAL_DECISION
```

Possible explanation:

> There were several candidate moves, but Nf3 was significantly more precise. Your move allowed Black to equalize.

The exact classification thresholds should be configurable and tested rather than arbitrarily hardcoded.

---

# 4. Compare the Played Move Against the Candidate Set

A useful analysis model is:

```text
bestEvaluation
secondBestEvaluation
playedMoveEvaluation
evaluationGapFromBest
candidateSpread
```

For example:

```text
bestEvaluation = +1.2
secondBestEvaluation = +1.1
playedMoveEvaluation = +0.9
```

This likely represents a relatively small loss in move quality.

Compare with:

```text
bestEvaluation = +1.4
secondBestEvaluation = +1.2
playedMoveEvaluation = -0.8
```

This represents a much more significant turning point.

Possible logic:

```text
IF played move is significantly worse than best
AND the evaluation gap is significant
    -> CRITICAL_MISTAKE

ELSE IF played move is close to the top candidates
    -> REASONABLE_ALTERNATIVE

ELSE
    -> INACCURACY
```

Do not rely exclusively on raw centipawn loss. The app should combine evaluation difference with contextual information such as tactics, forced lines, material changes, king safety, and position type.

---

# 5. "What Could I Have Played?"

This could become one of the main learning features.

Example:

Player played:

```text
Qxd4?
```

MultiPV:

```text
1. Nf3  +1.1
2. Be2  +0.9
3. O-O  +0.8
4. Qxd4 -0.7
```

The app could present:

## Decision Point

### Nf3

Develops the knight and prepares castling.

### Be2

Keeps the position flexible.

### O-O

Gets the king safe immediately.

### Your move: Qxd4?

This allowed Black to gain a tempo with `...Nc6`.

The important distinction is that the app explains **why the alternatives were useful**, rather than simply saying that the player's move was wrong.

---

# 6. MultiPV + Existing Tactic Detection

The existing tactic detector can make MultiPV much more powerful.

Suppose Stockfish produces:

```text
PV1:
Qxh7+ Kxh7 Nxf7+

PV2:
Nxf7+ ...

PV3:
Bxh7+ ...
```

The custom logic can inspect the candidate lines and identify:

```text
PV1:
- check
- sacrifice
- forcing sequence
- tactical continuation
```

The app can then classify the candidate:

```text
BRILLIANT_TACTICAL_OPPORTUNITY
```

and present something like:

> 🔥 Tactical opportunity
>
> Sacrificing the queen looks dangerous, but it starts a forcing tactical sequence.

The exact labels should be determined by the app's existing classification system. Avoid assuming that every engine-best sacrifice should automatically be called "brilliant."

---

# 7. MultiPV + LLM Architecture

Do not send raw Stockfish output directly to the LLM.

Instead, use an intermediate chess-analysis layer.

Recommended architecture:

```text
                    Chess Game
                        |
             +----------+----------+
             |                     |
             v                     v
        Game State            Player Move
             |                     |
             +----------+----------+
                        |
                        v
                   Stockfish
                    MultiPV
                        |
          +-------------+-------------+
          |             |             |
          v             v             v
      Best Move    Alternatives   Evaluations
          |             |             |
          +-------------+-------------+
                        |
                        v
                Chess Analyzer
                        |
        +---------------+----------------+
        |               |                |
        v               v                v
     Tactics         Opening        Move Quality
        |               |                |
        +---------------+----------------+
                        |
                        v
                Critical Moments
                        |
                        v
                       LLM
                        |
                        v
              Human Explanation
```

The LLM should receive a structured representation of the analysis rather than having to infer chess facts from raw engine output.

---

# 8. Suggested Analysis Data Model

Consider introducing a structured object similar to:

```kotlin
data class MultiPvAnalysis(
    val position: ChessPosition,
    val playedMove: Move?,
    val candidates: List<EngineCandidate>,
    val bestMove: Move?,
    val evaluationBefore: EngineEvaluation,
    val evaluationAfter: EngineEvaluation,
    val evaluationLoss: Double,
    val candidateSpread: Double,
    val tacticalInformation: TacticalAnalysis?,
    val openingInformation: OpeningAnalysis?,
    val positionCharacteristics: PositionCharacteristics?,
    val moveClassification: MoveClassification
)
```

Candidate:

```kotlin
data class EngineCandidate(
    val rank: Int,
    val move: Move,
    val evaluation: EngineEvaluation,
    val principalVariation: List<Move>,
    val depth: Int,
    val mateIn: Int?,
)
```

The exact model should fit the existing architecture rather than introducing duplicate representations of `Move`, `Board`, or engine evaluations.

---

# 9. MultiPV Engine API

If the current Stockfish wrapper only supports one principal variation, extend it to support multiple PVs.

Conceptually:

```text
analyze(
    position,
    depth,
    multiPv = 3
)
```

or:

```kotlin
data class EngineAnalysisRequest(
    val position: ChessPosition,
    val depth: Int,
    val multiPv: Int = 3,
    val timeLimitMs: Long? = null
)
```

Return:

```kotlin
data class EngineAnalysisResult(
    val candidates: List<EngineCandidate>,
    val depth: Int,
    val nodes: Long?,
    val timeMs: Long?
)
```

The exact API depends on the existing Stockfish integration.

Do not rewrite the engine integration unnecessarily if the current implementation can be extended cleanly.

---

# 10. Start With MultiPV = 3

A reasonable initial configuration is:

```text
MultiPV = 3
```

This is enough to understand the main candidate set without generating excessive engine work.

Do not immediately default to:

```text
MultiPV = 10
```

Higher MultiPV increases computation and can introduce less useful alternatives.

A future adaptive approach could be:

```text
Normal position
    -> MultiPV 3

Interesting/critical position
    -> MultiPV 5

Tactical position
    -> MultiPV 3 + deeper search
```

The initial implementation should remain simple.

---

# 11. Dynamic Analysis

The app can eventually decide how much analysis to spend on a position.

Example:

```text
Normal move
    |
    v
MultiPV 3
    |
    +-- Small candidate differences
            |
            v
       No extra work

    +-- Large candidate differences
            |
            v
       Critical position
            |
            v
       Deeper analysis
```

Potential triggers for deeper analysis:

- Large evaluation spread
- Tactical opportunity detected
- Material sacrifice
- Check sequence
- Forced response
- King attack
- Large evaluation swing
- Candidate move substantially better than played move

---

# 12. "Why Not My Move?"

A particularly useful UI interaction:

```text
User taps:
"Why was my move worse?"
```

The app compares:

```text
Played:
Qxd4

Best:
Nf3

Evaluation:
Nf3      +1.2
Qxd4     -0.6

Difference:
1.8
```

The analysis layer can then identify why:

```text
Recommended move: Nf3

Benefits:
- Develops a piece
- Maintains central pressure
- Preserves king safety
- Prevents ...e5

Played move: Qxd4

Problems:
- Queen becomes exposed
- Allows ...Nc6 with tempo
- Delays development
- Gives Black a development advantage
```

The LLM receives these structured facts and turns them into a natural explanation.

---

# 13. Candidate-Move Puzzles

MultiPV can also power automatic puzzle generation.

Instead of relying only on traditional tactic detection, look for positions where:

```text
bestMove >> secondBestMove
```

or:

```text
bestMove >> playedMove
```

and the position contains an interesting decision.

For example:

```text
Position:
...

Top candidate:
Nxf7!

Second candidate:
Bxf7

Evaluation:
Nxf7   +2.4
Bxf7   +0.6

Tactical information:
- sacrifice
- check
- king exposure
- forcing continuation
```

This can become a puzzle:

> **You reached this position in your own game. Can you find the move you missed?**

This fits particularly well with an adventure/progression-based chess-learning system.

---

# 14. Player-Level Analysis

After analyzing multiple games, aggregate MultiPV information.

Possible metrics:

```text
Best move chosen:       68%
Top-3 move chosen:      84%
Top-5 move chosen:      91%

Missed tactical opportunities: 7
Missed defensive moves: 4
Opening inaccuracies: 11

Average evaluation loss:
...

Critical decisions:
...

Reasonable alternatives chosen:
...
```

These metrics can help identify patterns in a player's decision-making.

For example, the app might discover that a player frequently:

- Finds tactical opportunities
- Chooses strong attacking moves
- Struggles in quiet positions
- Gives away advantages during conversion
- Misses defensive resources

The LLM can then turn those observations into a personalized training recommendation.

Avoid presenting these statistics as definitive claims about a player's ability. They should be framed as patterns observed in the analyzed games and should account for sample size.

---

# 15. Longitudinal Coaching

The ultimate direction could be:

```text
Game 1
    |
    v
Game Analysis
    |
    v
Player Patterns
    |
    v
Training Recommendations
    |
    v
Adventure / Puzzle
    |
    v
Game 2
    |
    v
Updated Player Patterns
```

Instead of:

> "You blundered on move 24."

the app can eventually say:

> "Across your recent games, you've frequently found attacking ideas but have missed quieter defensive moves. Here are three positions from your games that train that exact skill."

This transforms the system from a **game analyzer** into a **personal chess-learning system**.

---

# 16. Recommended Separation of Responsibilities

The architecture should separate the following concerns:

## Stockfish

Responsible for:

- Position evaluation
- Candidate moves
- Principal variations
- Search depth
- Mate scores
- Engine-specific information

## Chess Analysis Layer

Responsible for:

- Comparing candidate moves
- Evaluation differences
- Move-quality classification
- Critical-moment detection
- Tactical interpretation
- Position characteristics
- Opening interpretation
- Puzzle extraction

## LLM

Responsible for:

- Natural-language explanation
- Teaching concepts
- Explaining candidate differences
- Explaining why the player's move was inferior
- Generating contextual coaching
- Turning structured analysis into human-readable feedback

The LLM should **not** be treated as the source of truth for engine evaluations or legal chess moves.

---

# 17. Suggested Initial Implementation Plan

### Phase 1: Engine

1. Extend the Stockfish wrapper to support MultiPV.
2. Start with `MultiPV = 3`.
3. Return all candidate moves and their PVs.
4. Preserve the existing single-PV API where practical.

### Phase 2: Domain Model

Create structured models for:

```text
EngineCandidate
MultiPvAnalysis
Evaluation
MoveClassification
CriticalMoment
```

Reuse existing `Move`, `Board`, and position models.

### Phase 3: Analysis

Implement:

```text
candidate ranking
evaluation spread
played-vs-best comparison
reasonable-alternative detection
critical-decision detection
```

### Phase 4: Existing Features

Integrate MultiPV with:

```text
tactic detection
opening detection
game review
move classifications
```

### Phase 5: LLM

Create a structured prompt/input model that contains:

```text
position
played move
candidate moves
evaluations
evaluation difference
PV lines
tactical information
opening information
position characteristics
move classification
```

Do not send unnecessary engine noise.

### Phase 6: UI

Add features such as:

```text
Top Choices
Why this move?
Why not my move?
Alternative moves
Critical Decision
Missed Opportunity
```

### Phase 7: Training

Use the same analysis infrastructure to generate:

```text
missed-move puzzles
critical-position exercises
player-pattern reports
personalized training
```

---

# 18. Important Implementation Considerations

## Do not over-engineer the first version

Start with:

```text
Stockfish
    -> MultiPV 3
    -> structured candidates
    -> compare played move
    -> existing tactic/opening logic
    -> LLM
```

Do not immediately build an elaborate coaching engine.

## Preserve the existing chess engine architecture

The current app already has custom chess logic and Stockfish integration.

The MultiPV implementation should extend those components instead of creating parallel chess representations.

## Avoid duplicate domain models

If the app already has:

```text
Move
Board
Player
GameState
Position
```

reuse them.

Only create new models where the current architecture does not already have an appropriate abstraction.

## Keep engine work off the UI thread

Stockfish analysis should remain asynchronous.

MultiPV may require more engine computation, so cancellation and lifecycle handling are important.

For example:

```text
User plays move
    |
    v
Start analysis
    |
    v
User makes another move
    |
    v
Cancel obsolete analysis
    |
    v
Analyze current position
```

Avoid allowing stale engine results to overwrite the current game state.

---

# 19. Potential Final Architecture

A possible high-level architecture:

```text
                 ┌─────────────────────┐
                 │      Game UI        │
                 └──────────┬──────────┘
                            │
                            v
                 ┌─────────────────────┐
                 │    Game ViewModel   │
                 └──────────┬──────────┘
                            │
                            v
                 ┌─────────────────────┐
                 │   Game / Use Case   │
                 └──────────┬──────────┘
                            │
                ┌───────────┴───────────┐
                v                       v
       ┌─────────────────┐     ┌─────────────────┐
       │ Chess Analysis  │     │   Game Logic    │
       │     Engine      │     │                 │
       └────────┬────────┘     └─────────────────┘
                │
       ┌────────┴─────────┐
       v                  v
┌──────────────┐   ┌───────────────┐
│  Stockfish   │   │ Custom Chess  │
│   MultiPV    │   │   Analysis    │
└──────────────┘   └───────────────┘
       │                  │
       └────────┬─────────┘
                v
       ┌──────────────────┐
       │ Structured Game  │
       │    Analysis      │
       └────────┬─────────┘
                │
                v
       ┌──────────────────┐
       │       LLM        │
       └────────┬─────────┘
                │
                v
       ┌──────────────────┐
       │ Coaching / UI    │
       └──────────────────┘
```

---

# 20. Core Goal

The goal is **not**:

> "Show the user three Stockfish moves."

The goal is:

> **Use multiple engine candidates to understand the decision the player faced, compare it with the decision they made, identify why the alternatives matter, and turn that information into useful chess training.**

MultiPV gives the application a much richer picture of a position.

Combined with the existing:

- Custom chess logic
- Stockfish
- Tactic detection
- Opening detection
- LLM explanations
- Adventure/progression system

it can become the foundation for a much more intelligent chess-learning experience.
