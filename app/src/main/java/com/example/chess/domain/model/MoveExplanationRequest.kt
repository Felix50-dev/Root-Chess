package com.example.chess.domain.model

import com.example.chess.data.model.AnalysisResult

data class MoveExplanationRequest(
    val type: String,               // "Best", "Blunder", "Mistake", "Brilliant" etc.
    val movePlayed: String,         // SAN notation e.g. "Nf6"
    val bestMove: String?,          // Stockfish best move in SAN
    val chessPiece: String?,        // piece involved in capture opportunity
    val evalDelta: Float,           // centipawn loss from player's perspective
    val tactic: List<String>?,      // from TacticsRecognizer e.g. ["Fork", "Pin"]
    val mateIn: Int?,               // if MissedMate, how many moves
    val matingPiece: String?,       // piece that delivers mate
    val pgn: String,                // game so far in PGN format
    val playerElo: Int,             // for explanation depth calibration
    val moveNumber: Int,            // move number in the game
    val playerColor: String         // "White" or "Black"
) {
    companion object {
        // Convenience builder that pulls directly from your AnalysisResult
        fun fromAnalysisResult(
            index: Int,
            result: AnalysisResult,
            pgn: String,
            playerElo: Int
        ): MoveExplanationRequest {
            val missedMate = result.missedMates.getOrNull(index)
            val captureOpportunity = result.captureOpportunity.getOrNull(index)

            return MoveExplanationRequest(
                type = result.moveCategories.getOrNull(index) ?: "Best",
                movePlayed = result.bestMoves.getOrNull(index) ?: "",
                bestMove = result.bestMoves.getOrNull(index),
                chessPiece = captureOpportunity?.target?.toString(),
                evalDelta = 0f, //result.losses.getOrNull(index)?.toFloat() ?: 0f,
                tactic = result.tactics.getOrNull(index),
                mateIn = missedMate?.mateInBefore,
                matingPiece = missedMate?.moveObject?.pieceMoved?.toString(),
                pgn = pgn,
                playerElo = playerElo,
                moveNumber = index + 1,
                playerColor = if (index % 2 == 0) "White" else "Black"
            )
        }
    }
}
