package com.example.chess.presentation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.chess.R
import com.example.chess.data.model.Bishop
import com.example.chess.data.model.ChessPiece
import com.example.chess.data.model.GameStatus
import com.example.chess.data.model.King
import com.example.chess.data.model.Knight
import com.example.chess.data.model.Player
import com.example.chess.data.model.Position
import com.example.chess.data.model.Queen
import com.example.chess.data.model.Rook
import com.example.chess.data.model.SANMovePair
import com.example.chess.data.model.Spot
import kotlin.math.abs

private const val TAG = "BoardScreen"

@Composable
fun ChessGameScreen(
    chessGameViewModel: ChessGameViewModel,
    onExitGame: () -> Unit
) {
    val boardState by chessGameViewModel.boardState.collectAsState()
    Log.d("ChessBoard", "Observed lastMovedFrom: ${boardState.lastMovedFrom}")
    val validMoves by chessGameViewModel.validMoves.collectAsState()
    val isInCheck by chessGameViewModel.isInCheck.collectAsState()
    val checkPosition by chessGameViewModel.checkPosition.collectAsState()
    val piecesKilled by chessGameViewModel.piecesKilled.collectAsState()
    val movesMade by chessGameViewModel.movesMade.collectAsState()
    val movesMadeSAN by chessGameViewModel.movesMadeSAN.collectAsState()
    val lastMove by chessGameViewModel.lastMove.collectAsState()
    val aiLastMove by chessGameViewModel.aiLastMove.collectAsState()
    val showPawnPromotion by chessGameViewModel.showPawnPromotion.collectAsState()
    val promotionPiece by chessGameViewModel.promotionPiece.collectAsState()
    Log.d(TAG, "ChessGameScreen: pieces killed are: $piecesKilled")

    var checkMateDialog by remember {
        mutableStateOf(false)
    }
    var staleMateDialog by remember {
        mutableStateOf(false)
    }

    var showExitDialog by remember {
        mutableStateOf(false)
    }

    BackHandler {
        showExitDialog = true
    }

    AnimatedVisibility(
        visible = showExitDialog,
        enter = slideInHorizontally() + fadeIn()
    ) {
        ExitDialog(
            onConfirm = {
                chessGameViewModel.quitGame()
                onExitGame()
            },
            onDismiss = {
                showExitDialog = false
            }
        )
    }

    boardState.board?.let {
        boardState.currentPlayer?.let {
            ChessGrid(
                currentPlayer = boardState.currentPlayer!!,
                boardState = boardState,
                isInCheck = isInCheck,
                lastMove = lastMove,
                validMoves = validMoves,
                onSelectSpot = chessGameViewModel::onSelectSpot,
                onMovePieceTo = { position ->
                    chessGameViewModel.movePieceTo(position)
                },
                onSetPromotionPiece = chessGameViewModel::setPromotedPiece,
                onUndoMove = { chessGameViewModel.undoMove() },
                onRedoMove = { chessGameViewModel.redoMove() },
                onStartNewGame = { chessGameViewModel.startNewGame() },
                onQuitGame = {
                    chessGameViewModel.quitGame()
                },
                onExitGame = {
                    onExitGame()
                },
                checkPosition = checkPosition,
                piecesKilled = piecesKilled,
                movesMade = movesMade,
                movesMadeSan = movesMadeSAN,
                showPromotionDialog = showPawnPromotion,
                aiLastMove = aiLastMove
            )
        }
        Log.d(TAG, "ChessGameScreen last moved piece: ${boardState.lastMovedFrom}")
        if (boardState.gameStatus == GameStatus.WHITE_WIN) {
            checkMateDialog = true
        }
        if (boardState.gameStatus == GameStatus.BLACK_WIN) {
            checkMateDialog = true
        }
        AnimatedVisibility(visible = checkMateDialog) {
            CheckmateDialog(
                text = "CheckMate",
                onNewGameSelected = {
                    chessGameViewModel.startNewGame()
                    checkMateDialog = false
                }) {
                checkMateDialog = false
                boardState.gameStatus = GameStatus.ACTIVE
            }
        }
        if (boardState.gameStatus == GameStatus.STALEMATE) {
            if (boardState.gameStatus == GameStatus.STALEMATE) {
                staleMateDialog = true
            }
            AnimatedVisibility(visible = staleMateDialog) {
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
        Image(
            painter = painterResource(id = piece.vectorAsset),
            contentDescription = "chess piece"
        )
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

@Composable
fun StartGameDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Start New game")
        },
        text = {
            Text(text = "Are you sure you want to Start a New Game")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Yes")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = "No")
            }
        }
    )
}

@Composable
fun ChessGrid(
    currentPlayer: Player,
    modifier: Modifier = Modifier,
    boardState: BoardState,
    isInCheck: Boolean,
    lastMove: Move?,
    validMoves: List<Position>,
    onSelectSpot: (Spot) -> Unit,
    onMovePieceTo: (Position) -> Unit, // Add this parameter
    onSetPromotionPiece: (ChessPiece) -> Unit,
    onUndoMove: () -> Unit,
    onRedoMove: () -> Unit,
    onStartNewGame: () -> Unit,
    onQuitGame: () -> Unit,
    onExitGame: () -> Unit,
    checkPosition: Position,
    piecesKilled: MutableList<ChessPiece>,
    movesMade: MutableList<String>,
    movesMadeSan: MutableList<SANMovePair>,
    showPromotionDialog: Boolean,
    aiLastMove: Move?
) {
    val blackPiecesKilled = piecesKilled.filter {
        it.color == com.example.chess.data.model.Color.BLACK
    }
    val whitePiecesKilled = piecesKilled.filter {
        it.color == com.example.chess.data.model.Color.WHITE
    }

    // Black's material advantage
    val whiteAdvantage = calculateMaterialAdvantage(piecesKilled)
    val blackAdvantage = -whiteAdvantage // Black’s perspective

    var showStartGameDialog by remember {
        mutableStateOf(false)
    }
    var showExitDialog by remember {
        mutableStateOf(false)
    }

    val screenWidth = LocalConfiguration.current.screenWidthDp.toFloat()
    val squareSize = (screenWidth / 8).dp

    if (showStartGameDialog) {
        StartGameDialog(
            onConfirm = {
                onStartNewGame()
                showStartGameDialog = false
            }) {
            showStartGameDialog = false
        }
    }

    if (showExitDialog) {
        ExitDialog(
            onConfirm = {
                onQuitGame()
                onExitGame()
            },
            onDismiss = {
                showExitDialog = false
            }
        )
    }

    if (showPromotionDialog ) {
        PawnPromotionDialog(
            onPromotionSelected = {
                onSetPromotionPiece(it)
            },
            color = if (currentPlayer.isWhite) com.example.chess.data.model.Color.BLACK else com.example.chess.data.model.Color.WHITE
        ) {

        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(0f)
    ) {
        Image(
            painter = painterResource(id = R.drawable.chesstable),
            contentDescription = "chess table",
            modifier = modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(top = 32.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp),
                    color = Color(0xFFA26232),
                    tonalElevation = 8.dp
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_restart_alt_24),
                        contentDescription = "restart button",
                        modifier = Modifier
                            .clickable {
                                showStartGameDialog = true
                            }
                    )
                }
                Spacer(modifier = Modifier.weight(0.3f))
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp),
                    color = Color(0xFFA26232),
                    tonalElevation = 8.dp
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_fullscreen_exit_24),
                        contentDescription = "exit button",
                        modifier = Modifier
                            .clickable {
                                showExitDialog = true
                            }
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Panel(
                lostPieces = whitePiecesKilled.toMutableList(),
                moves = movesMadeSan,
                modifier = Modifier.fillMaxWidth(),
                advantage = blackAdvantage
            )
        }

        Column(
            modifier = modifier
                .fillMaxSize(),
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
                                        onSelectSpot(selectedSpot)
                                    },
                                    onMovePieceTo = { position ->
                                        onMovePieceTo(position)
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
                            modifier = modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(bottom = 32.dp),
        ) {
            Panel(
                lostPieces = blackPiecesKilled.toMutableList(),
                moves = movesMadeSan,
                modifier = Modifier.fillMaxWidth(),
                advantage = whiteAdvantage
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp),
                    color = Color(0xFFA26232),
                    tonalElevation = 8.dp
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_undo_24),
                        contentDescription = "undo button",
                        modifier = Modifier
                            .clickable {
                                onUndoMove()
                            }
                    )
                }
                Spacer(modifier = Modifier.weight(0.3f))
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp),
                    color = Color(0xFFA26232),
                    tonalElevation = 8.dp
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_redo_24),
                        contentDescription = "redo button",
                        modifier = Modifier
                            .clickable {
                                onRedoMove()
                            }
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ChessSquare(
    isWhite: Boolean,
    spot: Spot,
    isInCheck: Boolean,
    checkPosition: Position,
    validMoves: List<Position>,
    onSelectSpot: (Spot) -> Unit,
    onMovePieceTo: (Position) -> Unit,
    squareSize: Dp,
    lastMove: Move?,
    aiLastMove: Move?
) {
    val currentPosition = spot.position
    val piece = spot.chessPiece

    //val offset = remember { Animatable(Offset(0f, 0f), Offset.VectorConverter) }
    val isMoveOrigin = lastMove != null && currentPosition == lastMove.from
    val isMoveDestination = lastMove != null && currentPosition == lastMove.to

//    LaunchedEffect(lastMove?.moveId) {
//        if (isMoveOrigin) {
//            val fromCol = lastMove!!.from.column - currentPosition.column
//            val fromRow = lastMove.from.row - currentPosition.row
//            val startOffset = Offset(
//                x = fromCol * squareSize.value,
//                y = fromRow * squareSize.value
//            )
//            val targetOffset = Offset(
//                x = (lastMove.to.column - currentPosition.column) * squareSize.value,
//                y = (lastMove.to.row - currentPosition.row) * squareSize.value
//            )
//
//            Log.d(
//                "ChessSquare",
//                "Animating origin $currentPosition from ${lastMove.from} to ${lastMove.to}, startOffset: $startOffset, targetOffset: $targetOffset"
//            )
//
//            offset.snapTo(startOffset)
//            offset.animateTo(
//                targetValue = targetOffset,
//                animationSpec = tween(
//                    durationMillis = 600,
//                    easing = LinearEasing
//                )
//            )
//        } else if (piece != null && !isMoveDestination) {
//            offset.snapTo(Offset(0f, 0f))
//        }
//    }

    val backgroundColor = when {
        isWhite -> Color.White
        else -> Color(0xFFA26232) // Brown color
    }

    Box(
        modifier = Modifier
            .size(squareSize)
            .background(backgroundColor)
            .clickable {
                if (validMoves.contains(spot.position)) {
                    onMovePieceTo(spot.position)
                } else {
                    onSelectSpot(spot)
                }
            }
            .zIndex(0f) // Grid square at base level
            .then(
                if (currentPosition == lastMove?.from || currentPosition == lastMove?.to || currentPosition == aiLastMove?.from || currentPosition == aiLastMove?.to) {
                    Modifier.background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Green.copy(alpha = 0.3f))
                        )
                    )
                } else Modifier
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 3.dp,
                    color = when {
                        piece is King && isInCheck && spot.position == checkPosition -> Color.Red
                        else -> Color.Transparent
                    }
                )
                .zIndex(1f) // Static elements slightly above grid
        ) {
            piece?.let { chessPiece ->
                AnimatedChessPiece(
                    chessPiece = chessPiece,
                    squareSize = squareSize,
                    //offset = if (isMoveOrigin) offset.value else Offset(0f, 0f),
                    //modifier = Modifier.zIndex(if (isMoveOrigin) 20f else 1f) // Animating piece on top
                )
            }
            if (validMoves.contains(spot.position)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.Green)
                        .align(Alignment.Center)
                        .zIndex(2f) // Valid move indicator at static level
                )
            }
        }
    }
}

@Composable
fun AnimatedChessPiece(
    chessPiece: ChessPiece,
    squareSize: Dp,
    //offset: Offset,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(squareSize)
        //.offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
    ) {
        Image(
            painter = painterResource(chessPiece.vectorAsset),
            contentDescription = null,
            modifier = Modifier.matchParentSize()
        )
    }
}

@Composable
fun ExitDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Exit game")
        },
        text = {
            Text(text = "Are you sure you want to exit the game?")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Yes")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = "No")
            }
        }
    )
}

@Composable
fun Panel(
    lostPieces: MutableList<ChessPiece?>,
    moves: List<SANMovePair>,
    modifier: Modifier = Modifier,
    advantage: Int
) {
    Surface(
        modifier = modifier
            .height(60.dp)
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFA26232),
        tonalElevation = 2.dp,
        contentColor = Color.Black
    ) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (moves.isEmpty()) {
                        item {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_fullscreen_exit_24),
                                contentDescription = "No moves yet",
                                tint = Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        itemsIndexed(moves) { index, movePair ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                movePair.whitePieceDrawable?.let { id ->
                                    Image(
                                        painter = painterResource(id = id),
                                        contentDescription = "White piece",
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(
                                                Color.White.copy(alpha = 0.1f),
                                                shape = CircleShape
                                            )
                                            .padding(1.dp)
                                    )
                                    Text(
                                        text = movePair.whiteDestination,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            color = Color.Black
                                        )
                                    )
                                } ?: Text( // Pawns or castling
                                    text = movePair.whiteDestination,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        color = Color.Black
                                    )
                                )
                                // Black move
                                movePair.blackPieceDrawable?.let { id ->
                                    Image(
                                        painter = painterResource(id = id),
                                        contentDescription = "Black piece",
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(
                                                Color.Black.copy(alpha = 0.1f),
                                                shape = CircleShape
                                            )
                                            .padding(1.dp)
                                            .alpha(if (index == moves.lastIndex) 1f else 0.7f)
                                    )
                                    Text(
                                        text = movePair.blackDestination,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            color = if (index == moves.lastIndex) Color(0xFF4CDA24) else Color.Black,
                                            fontWeight = if (index == moves.lastIndex) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                } ?: movePair.blackDestination.takeIf { it.isNotEmpty() }
                                    ?.let { destination ->
                                        Text(
                                            text = destination,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 12.sp,
                                                color = if (index == moves.lastIndex) Color(
                                                    0xFF4CDA24
                                                ) else Color.Black,
                                                fontWeight = if (index == moves.lastIndex) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                    }
                            }
                        }
                    }
                }
            }
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .height(24.dp)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    lostPieces.forEach { piece ->
                        if (piece != null) {
                            Image(
                                painter = painterResource(id = piece.vectorAsset),
                                contentDescription = "${piece.color} $piece",
                                modifier = Modifier
                                    .size(24.dp) //
                                    .background(
                                        if (piece.color == com.example.chess.data.model.Color.WHITE) Color.White.copy(
                                            alpha = 0.1f
                                        )
                                        else Color.Black.copy(alpha = 0.1f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f)) // Push to the right
                    Text(
                        text = when {
                            advantage > 0 -> "+$advantage"
                            advantage < 0 -> "$advantage"
                            else -> "+0"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 14.sp,
                            color = when {
                                advantage > 0 -> Color(0xFF4CDA24) // Green for White advantage
                                advantage < 0 -> Color.Red // Red for Black advantage
                                else -> Color.Black.copy(alpha = 0.7f) // Neutral
                            },
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }

fun calculateMaterialAdvantage(lostPieces: List<ChessPiece?>): Int {
    val pieceValues = mapOf(
        R.drawable.pawn_white to 1, R.drawable.pawn_dark to 1,
        R.drawable.knight_white to 3, R.drawable.knight_dark to 3,
        R.drawable.bishop_white to 3, R.drawable.bishop_dark to 3,
        R.drawable.rook_white to 5, R.drawable.rook_dark to 5,
        R.drawable.queen_white to 9, R.drawable.queen_dark to 9,
        R.drawable.king_white to 0, R.drawable.king_dark to 0 // Kings don’t count
    )

    var whiteAdvantage = 0 // Positive means White is ahead, negative means Black is ahead

    lostPieces.filterNotNull().forEach { piece ->
        val value = pieceValues[piece.vectorAsset] ?: 0
        when (piece.color) {
            com.example.chess.data.model.Color.WHITE -> whiteAdvantage -= value // White loses material
            com.example.chess.data.model.Color.BLACK -> whiteAdvantage += value // Black loses material
        }
    }

    return whiteAdvantage
}
