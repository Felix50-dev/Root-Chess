package com.example.chess.data.repository

import com.example.chess.data.remote.GroqApi
import com.example.chess.domain.model.GameSummaryRequest
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

data class GeneratedGameSummary(val narrative: String, val weaknessHint: String)

@Singleton
class GameSummaryGenerator @Inject constructor(
    private val groqApi: GroqApi
) {
    // Singleton-scoped, same concurrency concern as MoveExplainer's cache.
    private val cache = ConcurrentHashMap<String, GeneratedGameSummary>()

    suspend fun generate(request: GameSummaryRequest): GeneratedGameSummary {
        val cacheKey = "${request.pgn}_${request.criticalMoveIndex}"
        cache[cacheKey]?.let { return it }

        val prompt = buildPrompt(request)
        val raw = groqApi.completeChat(
            systemInstruction = SYSTEM_INSTRUCTION,
            turns = listOf("user" to prompt),
            maxOutputTokens = 650,
            temperature = 0.6,
        )
        val parsed = parse(raw)
            ?: throw IllegalStateException("Unparseable Groq game-summary response: $raw")

        cache[cacheKey] = parsed
        return parsed
    }

    private fun buildPrompt(r: GameSummaryRequest): String {
        val whiteCats = r.whiteCategoryCounts.entries.joinToString { "${it.key}: ${it.value}" }
        val blackCats = r.blackCategoryCounts.entries.joinToString { "${it.key}: ${it.value}" }
        val tacticsLine = if (r.distinctTactics.isNotEmpty()) {
            "Tactics that appeared in the game: ${r.distinctTactics.joinToString()}"
        } else ""
        val openingLine = if (r.openingName != null) {
            "Opening played: ${r.openingName}${r.openingEco?.let { " ($it)" } ?: ""}"
        } else ""
        val streakLine = if (r.longestStreakLength >= 3) {
            "${r.longestStreakPlayer}'s longest accurate-move streak: ${r.longestStreakLength} moves in a row"
        } else ""
        val continuationLine = if (r.criticalMoveBestContinuation.isNotEmpty()) {
            "The better continuation was: ${r.criticalMoveBestContinuation.joinToString(" ")}"
        } else ""

        return """
            Game so far (PGN): ${r.pgn}
            Total moves: ${r.totalMoves}
            White accuracy: ${"%.1f".format(r.whiteAccuracy)}%
            Black accuracy: ${"%.1f".format(r.blackAccuracy)}%
            White move categories: $whiteCats
            Black move categories: $blackCats
            $openingLine
            $tacticsLine
            $streakLine
            Missed forced mates: ${r.missedMateCount}

            The single most critical moment was move ${r.criticalMoveNumber} (${r.criticalMovePlayerColor}),
            playing ${r.criticalMoveSan} during the ${r.criticalMoveGamePhase.lowercase()},
            losing ${r.criticalMoveEvalLost / 100f} pawns of evaluation.
            $continuationLine
            Deterministic analysis of that move: ${r.criticalMoveDetail}

            You are a warm, encouraging chess coach writing a post-game summary for a
            ${r.playerElo}-rated player. Respond in EXACTLY this format, nothing else:

            NARRATIVE: <2-3 sentence story of how the game unfolded, referencing the
            opening, the critical moment, and the overall accuracy>
            TIP: <one specific, actionable coaching tip based on the weakest pattern in
            this game>

            Rules:
            - Do not use the words "engine" or "Stockfish"
            - Name specific pieces/squares/moves where relevant
            - Complexity matches ${r.playerElo} elo
            - Keep NARRATIVE to 2-3 sentences and TIP to 1 sentence
        """.trimIndent()
    }

    private fun parse(raw: String): GeneratedGameSummary? {
        val narrative = Regex(
            "(?i)NARRATIVE:\\s*(.+?)(?=\\n\\s*TIP:|$)", RegexOption.DOT_MATCHES_ALL
        ).find(raw)?.groupValues?.get(1)?.trim()
        val tip = Regex(
            "(?i)TIP:\\s*(.+)", RegexOption.DOT_MATCHES_ALL
        ).find(raw)?.groupValues?.get(1)?.trim()

        if (narrative.isNullOrBlank() || tip.isNullOrBlank()) return null
        return GeneratedGameSummary(narrative, tip)
    }

    private companion object {
        const val SYSTEM_INSTRUCTION =
            "You are a supportive chess coach who writes concise, specific post-game feedback."
    }
}
