package com.example.chess.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.R
import com.example.chess.data.model.Bishop
import com.example.chess.data.model.ChessPiece
import com.example.chess.data.model.GameStatus
import com.example.chess.data.model.Knight
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Position
import com.example.chess.data.model.Queen
import com.example.chess.data.model.Rook
import com.example.chess.data.model.Spot


@Composable
fun ChessGameScreen(chessGameViewModel: ChessGameViewModel) {
    val boardState by chessGameViewModel.boardState.collectAsState()
    val selectedStartSpot by chessGameViewModel.selectedStartSpot.collectAsState()
    val validMoves by chessGameViewModel.validMoves.collectAsState()
    val promotionPiece by chessGameViewModel.promotionPiece.collectAsState()

    var checkMateDialog by remember {
        mutableStateOf(false)
    }
    var staleMateDialog by remember {
        mutableStateOf(false)
    }

    boardState.board?.let { board ->
        ChessGrid(
            boardState = board.boxes,
            selectedStartSpot = selectedStartSpot,
            validMoves = validMoves,
            onSelectSpot = chessGameViewModel::onSelectSpot,
            onMovePieceTo = {
                chessGameViewModel.movePieceTo(it)
            },
            onSetPromotionPiece = chessGameViewModel::setPromotedPiece,
            promotionPiece = promotionPiece
        )
        if (boardState.gameStatus == GameStatus.WHITE_WIN) {
            checkMateDialog = true
        }
        if (checkMateDialog) {
            CheckmateDialog(
                text = "CheckMate",
                onNewGameSelected = {
                    chessGameViewModel.startNewGame()
                    checkMateDialog = false
                }) {
                checkMateDialog = false
            }
        }
        if (boardState.gameStatus == GameStatus.STALEMATE) {
            staleMateDialog = true
        }
        if (staleMateDialog) {
            CheckmateDialog(
                text = "StaleMate",
                onNewGameSelected = {
                    chessGameViewModel.startNewGame()
                    staleMateDialog = false
                }) {
                staleMateDialog = false
                boardState.gameStatus = GameStatus.ACTIVE
            }
        }
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
    squareSize: Dp
) {
    Box(
        modifier = Modifier
            .size(squareSize)
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
    modifier: Modifier = Modifier,
    boardState: Array<Array<Spot>>,
    selectedStartSpot: Spot?,
    validMoves: List<Position>,
    onSelectSpot: (Spot) -> Unit,
    onMovePieceTo: (Position) -> Unit, // Add this parameter
    onSetPromotionPiece: (ChessPiece) -> Unit,
    promotionPiece: ChessPiece?
) {

    var isPawnPromotion by remember {
        mutableStateOf(false)
    }
    var color: com.example.chess.data.model.Color = com.example.chess.data.model.Color.BLACK

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val squareSize = (screenWidth / 8)


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.chesstable),
            contentDescription = "chess table",
            modifier = modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = modifier.align(Alignment.CenterHorizontally)
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
                                modifier = modifier.align(Alignment.Center),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Column {
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
                                    },
                                    squareSize = squareSize
                                )

                                if (spot.position.row == 0 && spot.chessPiece?.color == com.example.chess.data.model.Color.WHITE && spot.chessPiece is Pawn) {
                                    color = (spot.chessPiece as Pawn).color
                                    isPawnPromotion = true
                                    if (promotionPiece != null) {
                                        spot.chessPiece = promotionPiece
                                        isPawnPromotion = false
                                    }
                                }
                                if (spot.position.row == 7 && spot.chessPiece?.color == com.example.chess.data.model.Color.BLACK && spot.chessPiece is Pawn) {
                                    color = (spot.chessPiece as Pawn).color
                                    isPawnPromotion = true
                                    if (promotionPiece != null) {
                                        spot.chessPiece = promotionPiece
                                        isPawnPromotion = false
                                    }
                                }
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
                            .width(squareSize)
                            .background(color = Color(0xFFA26232))
                    ) {
                        Text(
                            text = char.toString(),
                            modifier = modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
        if (isPawnPromotion) {
            PawnPromotionDialog(
                onPromotionSelected = {
                    onSetPromotionPiece(it)
                },
                color = color,
                onDismiss = { isPawnPromotion = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PawnPromotionDialog(
    onPromotionSelected: (ChessPiece) -> Unit,
    color: com.example.chess.data.model.Color,
    onDismiss: () -> Unit,
) {
    var selectedOption by remember { mutableStateOf<ChessPiece?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Pawn Promotion",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    PromotionOption(
                        piece = Queen(
                            color,
                            Position(0, 0),
                            false,
                            vectorAsset = if (color == com.example.chess.data.model.Color.WHITE) R.drawable.queen_white else R.drawable.queen_dark
                        ),
                        onSelected = {
                            selectedOption = it
                            onPromotionSelected(selectedOption!!)
                        },
                        selected = selectedOption is Queen,
                        text = "Queen"
                    )
                    PromotionOption(
                        piece = Rook(
                            color,
                            Position(0, 0),
                            false,
                            vectorAsset = if (color == com.example.chess.data.model.Color.WHITE) R.drawable.rook_white else R.drawable.rook_dark
                        ),
                        onSelected = {
                            selectedOption = it
                            onPromotionSelected(selectedOption!!)
                        },
                        selected = selectedOption is Rook,
                        text = "Rook"
                    )
                    PromotionOption(
                        piece = Bishop(
                            color,
                            Position(0, 0),
                            false,
                            if (color == com.example.chess.data.model.Color.WHITE) R.drawable.bishop_white else R.drawable.bishop_dark
                        ),
                        onSelected = {
                            selectedOption = it
                            onPromotionSelected(selectedOption!!)
                        },
                        selected = selectedOption is Bishop,
                        text = "Bishop"
                    )
                    PromotionOption(
                        piece = Knight(
                            color,
                            Position(0, 0),
                            false,
                            if (color == com.example.chess.data.model.Color.WHITE) R.drawable.knight_white else R.drawable.knight_dark
                        ),
                        onSelected = {
                            selectedOption = it
                            onPromotionSelected(selectedOption!!)
                        },
                        selected = selectedOption is Knight,
                        text = "Knight"
                    )
                }
            }
            }
    }
}

@Composable
fun PromotionOption(
    piece: ChessPiece,
    onSelected: (ChessPiece) -> Unit,
    selected: Boolean,
    text: String
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = { onSelected(piece) },
            modifier = Modifier.padding(end = 8.dp)
        )
        Image(painter = painterResource(id = piece.vectorAsset), contentDescription = "chess piece")
        Text(
            text = "$text ",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckmateDialog(
    text: String,
    onNewGameSelected: () -> Unit,
    onViewBoardSelected: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { /* Handle dialog dismissal */ },

        ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "$text ",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "What would you like to do?",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Row {
                    Button(
                        onClick = { onNewGameSelected() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA26232))
                    ) {
                        Text(text = "New Game")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { onViewBoardSelected() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA26232))
                    ) {
                        Text(text = "Show Board")
                    }
                }
            }
        }
    }
}