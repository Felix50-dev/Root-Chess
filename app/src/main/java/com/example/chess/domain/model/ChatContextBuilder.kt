package com.example.chess.domain.model

import com.example.chess.data.model.AnalysisResult

// Pure prompt-text assembly — no DI, no I/O — same shape as MoveExplanationRequest.
// Builds the system-instruction prefix sent alongside every chat turn.
object ChatContextBuilder {

    fun build(
        moveNotation: List<String>,
        analysisResult: AnalysisResult?,
        playerElo: Int = 1200,
    ): String {
        if (moveNotation.isEmpty()) {
            return """
                You are a friendly, encouraging chess coach chatting with a $playerElo rated
                player inside a chess app. No game has been played yet in this session, so
                answer general chess questions (rules, openings, strategy, terminology)
                without referencing a specific game.

                Rules:
                - Keep answers concise (max 4-5 sentences unless asked for more detail).
                - Use plain language calibrated to a $playerElo rated player.
                - Never say "the engine prefers".
            """.trimIndent()
        }

        val movesText = moveNotation
            .mapIndexed { i, m -> "${(i / 2) + 1}${if (i % 2 == 0) "." else "..."} $m" }
            .joinToString(" ")

        val summary = analysisResult?.gameSummary
        val analysisBlock = if (analysisResult != null && summary != null) {
            """
                This game has been analyzed:
                - White accuracy: ${analysisResult.whiteAccuracy}%, Black accuracy: ${analysisResult.blackAccuracy}%
                - Opening: ${summary.openingName ?: "unidentified"}
                - Narrative: ${summary.gameNarrative}
                - Key weakness to work on: ${summary.weaknessHint}
                - Biggest mistake: ${summary.biggestMistakeMove}
            """.trimIndent()
        } else {
            "This game has not been analyzed yet — no per-move insight or accuracy data is available."
        }

        return """
            You are a friendly, encouraging chess coach chatting with a $playerElo rated player
            inside a chess app.

            Most recent/current game so far: $movesText

            $analysisBlock

            Rules:
            - Answer using the game context above when the player's question relates to it.
            - Keep answers concise (max 4-5 sentences unless asked for more detail).
            - Complexity matches a $playerElo rated player.
            - Never say "the engine prefers".
        """.trimIndent()
    }
}
