package com.example.chess.data.repository

import com.example.chess.data.remote.GroqApi
import com.example.chess.domain.model.MoveExplanationRequest
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoveExplainer @Inject constructor(
    private val groqApi: GroqApi
) {
    // Singleton-scoped and explain() can be invoked concurrently (e.g. rapidly stepping
    // through review moves each launches its own coroutine) — must be thread-safe.
    private val explanationCache = ConcurrentHashMap<String, String>()

    suspend fun explain(request: MoveExplanationRequest): String {

        // Keyed on the full fact list (not just its length) so two different games that
        // happen to reach the same move/category with the same facts don't collide.
        val cacheKey = "${request.facts.joinToString()}_${request.movePlayed}_${request.type}"
        explanationCache[cacheKey]?.let { return it }

        val prompt = buildPrompt(request)
        val explanation = groqApi.complete(prompt)
        explanationCache[cacheKey] = explanation
        return explanation
    }

    private fun buildPrompt(r: MoveExplanationRequest): String {
        val tacticContext = if (!r.tactic.isNullOrEmpty())
            "Tactic detected: ${r.tactic.joinToString()}"
        else ""

        val mateContext = if (r.mateIn != null)
            "There was a mate in ${r.mateIn} available with ${r.matingPiece} to ${r.bestMove}"
        else ""

        // Translate eval delta into human language
        val evalDescription = when {
            r.evalDelta < -3.0f -> "a major blunder losing significant material"
            r.evalDelta < -1.0f -> "a serious mistake"
            r.evalDelta < -0.4f -> "a slight inaccuracy"
            r.evalDelta > 1.0f  -> "an excellent move gaining advantage"
            else -> "a reasonable move"
        }

        val factsBlock = r.facts.joinToString("\n") { "- $it" }

        return """
            You are a chess coach explaining a single move to a ${r.playerElo} rated player.

            Facts (verified — the only source of truth for this position; nothing else about
            piece locations or board geometry is true unless stated here):
            $factsBlock

            Move number: ${r.moveNumber}
            Player color: ${r.playerColor}
            Move category: ${r.type} ($evalDescription)
            Best move was: ${r.bestMove ?: "the move played"}
            Eval change: ${r.evalDelta} pawns
            $tacticContext
            $mateContext

            Rules:
            - You may ONLY reference squares, pieces, and moves listed in the Facts above — never invent, guess, or assume a piece location, square, or variation that isn't explicitly given
            - If a detail isn't in the Facts, omit it rather than guess
            - Max 2 sentences
            - Explain WHY not just what happened
            - Name specific pieces and squares, using only the Facts above
            - Complexity matches ${r.playerElo} elo: ${eloGuidance(r.playerElo)}
            - If a tactic was present, name it and the pieces involved
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
