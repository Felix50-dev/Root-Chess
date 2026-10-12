package com.example.chess.data.repository

import android.content.Context
import com.example.chess.BuildConfig
import com.example.chess.data.remote.GroqApi
import com.example.chess.domain.model.MoveExplanationRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

// Thrown by explain()'s post-generation checks — deliberately NOT a GroqApiException, so it
// can never match `e is GroqApiException && e.statusCode == 429` in ChessGameViewModel/
// ChatViewModel's catch blocks and can never trip their disable-cooldown flags. A rejected
// response is a content problem, not a rate-limit/network one — the caller's existing
// deterministic-insight fallback already handles it correctly either way.
class MoveExplanationValidationException(message: String) : IllegalStateException(message)

// Catches a narrow, deterministic slice of hallucination: content the prompt explicitly
// forbids but doesn't enforce, and references to squares that don't appear anywhere in the
// Facts this response was supposed to be grounded in. This is NOT a semantic fact-checker —
// it would not catch a response that only uses grounded squares/pieces but mischaracterizes
// what a Fact actually says (e.g. calling a capture "an even trade" when the Fact states
// "White can win material" — a real example found via manual audit). Reliably catching that
// class needs something closer to an LLM-as-judge second pass; out of scope here.
private val BANNED_PHRASES = listOf(
    Regex("(?i)the engine prefers"),
)

// Coarse heuristics tuned against the ai_capture.log corpus gathered during the hallucination
// audit — the prompt bans numeric evals/centipawns outright, so any digit shaped like a pawn
// count or a raw eval ("+2.3", "1.5 pawns", "centipawns") is a leak, not a legitimate mention.
private val NUMERIC_EVAL_LEAKS = listOf(
    Regex("""[-+]?\d+[.,]\d+\s*pawns?"""),
    Regex("(?i)centipawn"),
    Regex("""[-+]\d+\.\d+"""),
)

private val SQUARE_TOKEN = Regex("""\b[a-h][1-8]\b""")

@Singleton
class MoveExplainer @Inject constructor(
    private val groqApi: GroqApi,
    @ApplicationContext private val context: Context,
) {
    // Singleton-scoped and explain() can be invoked concurrently (e.g. rapidly stepping
    // through review moves each launches its own coroutine) — must be thread-safe.
    private val explanationCache = ConcurrentHashMap<String, String>()

    suspend fun explain(request: MoveExplanationRequest): String {
        // The full prompt is the cache key: it embeds every request field (facts, moves,
        // type, eval, mate, color, move number, elo), so distinct requests cannot collide.
        val prompt = buildPrompt(request)
        explanationCache[prompt]?.let {
            capture("=== CACHE HIT ===\n--- RESPONSE ---\n$it")
            return it
        }

        val explanation = try {
            groqApi.complete(prompt)
        } catch (e: Exception) {
            capture("=== MOVE ===\n$prompt\n--- ERROR ---\n${e.javaClass.simpleName}: ${e.message}")
            throw e
        }
        capture("=== MOVE ===\n$prompt\n--- RESPONSE ---\n$explanation")
        // A reasoning model can burn its whole token budget and return "" with HTTP 200 —
        // treat that as a failure (the caller falls back to the deterministic insight) and
        // never cache it, so the next visit to this move retries.
        check(explanation.isNotBlank()) { "Groq returned a blank explanation" }
        // Same not-cached-on-failure contract as the blank check above — rethrown before
        // reaching the cache-store line, so the next visit to this move retries the network
        // call rather than serving a rejected response forever.
        try {
            validate(explanation, request)
        } catch (e: MoveExplanationValidationException) {
            capture("=== MOVE ===\n$prompt\n--- VALIDATION FAILED ---\n${e.message}")
            throw e
        }
        explanationCache[prompt] = explanation
        return explanation
    }

    private fun validate(response: String, request: MoveExplanationRequest) {
        BANNED_PHRASES.forEach { pattern ->
            if (pattern.containsMatchIn(response)) {
                throw MoveExplanationValidationException(
                    "Response matches banned phrase ${pattern.pattern}: $response"
                )
            }
        }
        NUMERIC_EVAL_LEAKS.forEach { pattern ->
            if (pattern.containsMatchIn(response)) {
                throw MoveExplanationValidationException(
                    "Response leaks a numeric evaluation (${pattern.pattern}): $response"
                )
            }
        }
        val grounded = groundedSquares(request)
        val mentioned = SQUARE_TOKEN.findAll(response).map { it.value }.toSet()
        val ungrounded = mentioned - grounded
        if (ungrounded.isNotEmpty()) {
            throw MoveExplanationValidationException(
                "Response references square(s) $ungrounded not present in the Facts: $response"
            )
        }
    }

    // Every square mentioned anywhere the response is allowed to draw from: the Facts
    // themselves, any detected tactic's (already fully-grounded, see DetectedTactic) prose
    // description, and the two UCI move strings — covers 5-char promotion moves (e.g.
    // "e7e8q") the same as plain 4-char ones, since the regex just finds both squares inside.
    private fun groundedSquares(request: MoveExplanationRequest): Set<String> {
        val text = buildString {
            request.facts.forEach { appendLine(it) }
            request.tactic?.forEach { appendLine(it.description) }
            appendLine(request.movePlayed)
            request.bestMove?.let { appendLine(it) }
        }
        return SQUARE_TOKEN.findAll(text).map { it.value }.toSet()
    }

    // TEMP-CAPTURE: file-based diagnostic for the explanation-regression investigation
    // (device ROM suppresses logcat) — remove once the analysis round is done. Must never
    // affect behavior: debug-only and every failure swallowed.
    private fun capture(entry: String) {
        if (!BuildConfig.DEBUG) return
        runCatching {
            context.filesDir.resolve("ai_capture.log")
                .appendText("[${System.currentTimeMillis()}] $entry\n\n")
        }
    }

    private fun buildPrompt(r: MoveExplanationRequest): String {
        val mateContext = if (r.mateIn != null)
            "There was a mate in ${r.mateIn} available with ${r.matingPiece} to ${r.bestMove}"
        else ""

        // Mutually exclusive with mateContext by construction (a move can't both miss the
        // mover's mate and newly allow one against them — allowedMateIn requires no prior
        // forced mate either way).
        val allowedMateContext = if (r.allowedMateIn != null)
            "This move allows the opponent a forced checkmate in ${r.allowedMateIn} — " +
                "the stronger alternative would have prevented that forced mate."
        else ""

        // Translate eval delta into human language
        val evalDescription = when {
            // calculateLoss ignores mate scores, so a thrown-away forced mate can arrive
            // with evalDelta ≈ 0 — never let that read as "reasonable" when the mate
            // context says otherwise.
            r.type == "MissedMate" || r.mateIn != null ->
                "a serious mistake — it threw away a forced checkmate" +
                    (r.mateIn?.let { " in $it" } ?: "")
            r.evalDelta < -3.0f -> "a major blunder losing significant material"
            r.evalDelta < -1.0f -> "a serious mistake"
            r.evalDelta < -0.5f -> "a slight inaccuracy" // aligned with categorizeByLoss's 50cp "Great" ceiling
            r.evalDelta > 1.0f  -> "an excellent move gaining advantage"
            else -> "a reasonable move"
        }

        // Tactic geometry rendered as Facts — the rules below only allow the model to
        // reference pieces/squares stated in the Facts, so the tactic must live there
        // (a bare "Tactic detected: Pin" outside the Facts forced the model to invent
        // which pieces were pinned).
        val tacticFacts = r.tactic.orEmpty().map { "Tactic: ${it.description}" }
        val factsBlock = (r.facts + tacticFacts).joinToString("\n") { "- $it" }
        val betterAlternative = if (r.bestMove != null && r.bestMove != r.movePlayed) {
            // Benefit size in words only — the raw eval number is banned from the prompt.
            val benefit = when {
                r.mateIn != null || r.allowedMateIn != null -> "" // the mate context states the stakes
                -r.evalDelta > 3f -> " The alternative would have won material or given a winning position."
                -r.evalDelta >= 1f -> " The alternative was clearly better."
                else -> " The alternative was only slightly better."
            }
            "A stronger alternative for ${r.playerColor} was: ${r.bestMove} — described in the \"Stronger alternative\" Fact (NOT played — mention it only as what would have been better).$benefit"
        } else {
            "The move played was the best available"
        }

        return """
            You are a chess coach explaining a single move to a ${r.playerElo} rated player.

            Facts (verified — the only source of truth for this position; nothing else about
            piece locations or board geometry is true unless stated here):
            $factsBlock

            Move number: ${r.moveNumber}
            Player color: ${r.playerColor}
            Move to explain: ${r.movePlayed} — the move described in the first Fact
            (Moves are given in coordinate notation: from-square then to-square, e.g. g1f3)
            Move category: ${r.type} ($evalDescription)
            $betterAlternative
            $mateContext
            $allowedMateContext

            Rules:
            - Explain the move that was PLAYED (${r.movePlayed}, the first Fact) — never explain the alternative move as if it were played
            - You may ONLY reference squares, pieces, and moves listed in the Facts above — never invent, guess, or assume a piece location, square, or variation that isn't explicitly given
            - Do not claim any attack, pin, fork, defense, check, or threat unless it is stated in the Facts
            - Do not claim any piece defends, supports, protects, or is connected to another piece unless that relationship is stated in the Facts
            - Do not claim connected rooks, open or half-open files, batteries, outposts, control of a square, or any other positional relationship — never work out square-to-square geometry yourself
            - A side never attacks, pressures, or wins material from its own pieces — check piece colors against the Facts before describing any interaction
            - Only describe material as won, lost, or hanging if the Facts state it; a piece that can be captured but is defended is a trade, not a loss
            - Never mention numeric evaluations, centipawns, or fractions of a pawn — describe move quality in plain words only
            - An evaluation or advantage change is NOT material: never say material was won or lost unless the Material balance fact or a capture in the Facts shows it
            - If the Facts say the move delivers checkmate, lead with that — it outweighs all other commentary
            - Describe what the stronger alternative would have achieved ONLY from the "Stronger alternative" Fact — never invent its follow-up moves or consequences
            - If a detail isn't in the Facts, omit it rather than guess
            - Max 2 sentences
            - Explain WHY not just what happened
            - Name specific pieces and squares, using only the Facts above
            - Complexity matches ${r.playerElo} elo: ${eloGuidance(r.playerElo)}
            - If a Tactic fact is present, name the tactic and describe it using ONLY the pieces and squares stated in that Tactic fact — never re-derive or extend its geometry
            - "Opponent threat" facts describe what the opponent COULD have done, not moves that were played — never narrate them as having happened
            - Never say "the engine prefers"
        """.trimIndent()
    }

    private fun eloGuidance(elo: Int) = when {
        elo < 800  -> "very simple language, avoid chess terms"
        elo < 1200 -> "basic chess terms, short sentences"
        elo < 1600 -> "standard chess terminology is fine"
        elo < 2000 -> "intermediate concepts, positional ideas"
        else       -> "advanced concepts, deep positional reasoning"
    }
}
