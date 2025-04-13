package com.example.chess.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.chess.R

@Composable
fun BoardAnalysisScreen(
    chessGameViewModel: ChessGameViewModel,
) {

    val boardState by chessGameViewModel.boardState.collectAsState()
    val lastMove by chessGameViewModel.lastMove.collectAsState()
    val validMoves by chessGameViewModel.validMoves.collectAsState()
    val isInCheck by chessGameViewModel.isInCheck.collectAsState()
    val checkPosition by chessGameViewModel.checkPosition.collectAsState()
    val aiLastMove by chessGameViewModel.aiLastMove.collectAsState()

    val screenWidth = LocalConfiguration.current.screenWidthDp.toFloat()
    val squareSize = (screenWidth / 8).dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(0f)
    ) {
        Image(
            painter = painterResource(id = R.drawable.chesstable),
            contentDescription = "chess table",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Column {
                    // Row labels from a to h
                    ('8' downTo '1').forEach { char ->
                        Box(
                            modifier = Modifier
                                .height(squareSize)
                                .background(color = Color(0xFFA26232))
                        ) {
                            Text(
                                text = char.toString(),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Center),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Column {
                    boardState.board?.boxes?.forEachIndexed { row, rowData ->
                        Row {
                            rowData.forEachIndexed { column, spot ->
                                ChessSquare(
                                    isWhite = (row + column) % 2 == 0,
                                    spot = spot,
                                    isInCheck = isInCheck,
                                    checkPosition = checkPosition,
                                    validMoves = validMoves,
                                    onSelectSpot = { selectedSpot ->
                                        chessGameViewModel.onSelectSpot(selectedSpot)
                                    },
                                    onMovePieceTo = { position ->
                                        chessGameViewModel.movePieceTo(position)
                                    },
                                    squareSize = squareSize,
                                    lastMove = lastMove,
                                    aiLastMove = aiLastMove
                                )
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                // Row labels from a to h
                ('a'..'h').forEach { char ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(color = Color(0xFFA26232))
                    ) {
                        Text(
                            text = char.toString(),
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}