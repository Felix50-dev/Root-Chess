package com.example.chess.presentation.model

import androidx.compose.runtime.Immutable
import com.example.chess.data.model.Board
import com.example.chess.data.model.King
import com.example.chess.data.model.Position
import com.example.chess.presentation.Move

@Immutable
data class SquareUiState(
    val position: Position,
    val isWhite: Boolean,
    val pieceAsset: Int?,
    val isKing: Boolean,
    val isValidMove: Boolean,
    val isCapturableTarget: Boolean,
    val isLastMoveHighlight: Boolean,
    val isCheckedSquare: Boolean,
)

// Built from value fields only — never from the raw ChessPiece reference or its id,
// since Board.clone() mints fresh piece instances (new identity) on every move.
fun buildSquareUiStates(
    board: Board,
    validMoves: List<Position>,
    lastMove: Move?,
    aiLastMove: Move?,
    isInCheck: Boolean,
    checkPosition: Position,
): List<SquareUiState> {
    val boxes = board.boxes
    val validSet = validMoves.toHashSet()
    return boxes.flatMapIndexed { row, rowData ->
        rowData.mapIndexed { column, spot ->
            val pos = spot.position
            val piece = spot.chessPiece
            val isValid = pos in validSet
            SquareUiState(
                position = pos,
                isWhite = (row + column) % 2 == 0,
                pieceAsset = piece?.vectorAsset,
                isKing = piece is King,
                isValidMove = isValid,
                isCapturableTarget = isValid && piece != null,
                isLastMoveHighlight = pos == lastMove?.from || pos == lastMove?.to ||
                    pos == aiLastMove?.from || pos == aiLastMove?.to,
                isCheckedSquare = isInCheck && piece is King && pos == checkPosition,
            )
        }
    }
}
