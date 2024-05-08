package com.example.chess.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.chess.data.model.Bishop
import com.example.chess.data.model.ChessPiece
import com.example.chess.data.model.Knight
import com.example.chess.data.model.Position
import com.example.chess.data.model.Queen
import com.example.chess.data.model.Rook
import com.example.chess.data.model.Spot
import com.example.chess.R

@Composable
fun ChessGameScreen(chessGameViewModel: ChessGameViewModel) {
    val boardState by chessGameViewModel.boardState.collectAsState()
    val selectedStartSpot by chessGameViewModel.selectedStartSpot.collectAsState()
    val validMoves by chessGameViewModel.validMoves.collectAsState()

    boardState.board?.let { board ->
        ChessGrid(
            boardState = board.boxes,
            selectedStartSpot = selectedStartSpot,
            validMoves = validMoves,
            onSelectSpot = chessGameViewModel::onSelectSpot,
            onMovePieceTo = chessGameViewModel::movePieceTo
        )
    }
}

@Composable
fun ChessSquare(
    isWhite: Boolean,
    spot: Spot,
    isSelected: Boolean,
    validMoves: List<Position>,
    onSelectSpot: (Spot) -> Unit,
    onMovePieceTo: (Position) -> Unit,
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .background(color = if (isWhite) Color.White else Color(0xFFA26232))
            .clickable {
                if (validMoves.contains(spot.position)) {
                    onMovePieceTo(spot.position)
                } else {
                    onSelectSpot(spot)
                }
            }
    )
    {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(2.dp)
                .background(
                    color = if (isSelected) Color(0xFF43A047) else Color.Transparent,
                    shape = RectangleShape
                )
        ) {
            spot.chessPiece?.let { chessPiece ->
                Image(
                    painter = painterResource(chessPiece.vectorAsset),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize()
                )
            }
            // Display dots on valid spots
            validMoves.forEach { validMove ->
                if (validMove == spot.position) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.Green)
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }

}


@Composable
fun ChessGrid(
    boardState: Array<Array<Spot>>,
    selectedStartSpot: Spot?,
    validMoves: List<Position>,
    onSelectSpot: (Spot) -> Unit,
    onMovePieceTo: (Position) -> Unit, // Add this parameter
) {

    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column (
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    modifier = Modifier.fillMaxHeight(),
                    painter = painterResource(id = R.drawable.chesstable),
                    contentDescription = "chess table"
                )
            }
            Column (
                verticalArrangement = Arrangement.Center
            ) {
                boardState.forEachIndexed { row, rowData ->
                    Row {
                        rowData.forEachIndexed { column, spot ->
                            val isSelected = spot == selectedStartSpot
                            ChessSquare(
                                isWhite = (row + column) % 2 == 0,
                                spot = spot,
                                isSelected = isSelected,
                                validMoves = validMoves,
                                onSelectSpot = { selectedSpot ->
                                    onSelectSpot(selectedSpot)
                                },
                                onMovePieceTo = { position ->
                                    onMovePieceTo(position)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PawnPromotionDialog(
    onPromotionSelected: (ChessPiece) -> Unit,
    color: com.example.chess.data.model.Color
) {
    var selectedOption by remember { mutableStateOf<ChessPiece?>(null) }

    AlertDialog(
        onDismissRequest = { /*TODO*/ },

        ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PromotionOption(
                piece = Queen(color, Position(0, 0), false, R.drawable.queen_dark),
                onSelected = { selectedOption = it },
                selected = selectedOption is Queen
            )
            PromotionOption(
                piece = Rook(color, Position(0, 0), false, R.drawable.rook_dark),
                onSelected = { selectedOption = it },
                selected = selectedOption is Rook
            )
            PromotionOption(
                piece = Bishop(color, Position(0, 0), false, R.drawable.bishop_dark),
                onSelected = { selectedOption = it },
                selected = selectedOption is Bishop
            )
            PromotionOption(
                piece = Knight(color, Position(0, 0), false, R.drawable.knight_dark),
                onSelected = { selectedOption = it },
                selected = selectedOption is Knight
            )
        }
    }
}

@Composable
fun PromotionOption(piece: ChessPiece, onSelected: (ChessPiece) -> Unit, selected: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = { onSelected(piece) },
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(text = piece.javaClass.simpleName)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckmateDialog(
    onNewGameSelected: () -> Unit,
    onViewBoardSelected: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Handle dialog dismissal */ },
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Checkmate has occurred. What would you like to do?",
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}