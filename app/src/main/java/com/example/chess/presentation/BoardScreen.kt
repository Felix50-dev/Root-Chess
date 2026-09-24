package com.example.chess.presentation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chess.R
import com.example.chess.data.model.Bishop
import com.example.chess.data.model.ChessPiece
import com.example.chess.data.model.GameStatus
import com.example.chess.data.model.Knight
import com.example.chess.data.model.Player
import com.example.chess.data.model.Position
import com.example.chess.data.model.Queen
import com.example.chess.data.model.Rook
import com.example.chess.data.model.SANMovePair
import com.example.chess.data.model.Spot
import com.example.chess.presentation.model.SquareUiState
import com.example.chess.presentation.model.VisualPiece
import com.example.chess.presentation.model.buildSquareUiStates
import com.example.chess.ui.theme.ChessTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private const val TAG = "BoardScreen"

@Composable
fun ChessGameScreen(
    chessGameViewModel: ChessGameViewModel,
    onExitGame: () -> Unit,
    onAnalyzeGame: () -> Unit,
) {
    val boardState by chessGameViewModel.boardState.collectAsStateWithLifecycle()
    Log.d("ChessBoard", "Observed lastMovedFrom: ${boardState.lastMovedFrom}")
    val validMoves by chessGameViewModel.validMoves.collectAsStateWithLifecycle()
    val isInCheck by chessGameViewModel.isInCheck.collectAsStateWithLifecycle()
    val checkPosition by chessGameViewModel.checkPosition.collectAsStateWithLifecycle()
    val piecesKilled by chessGameViewModel.piecesKilled.collectAsStateWithLifecycle()
    val movesMadeSAN by chessGameViewModel.movesMadeSAN.collectAsStateWithLifecycle()
    val lastMove by chessGameViewModel.lastMove.collectAsStateWithLifecycle()
    val aiLastMove by chessGameViewModel.aiLastMove.collectAsStateWithLifecycle()
    val showPawnPromotion by chessGameViewModel.showPawnPromotion.collectAsStateWithLifecycle()
    val visualPieces by chessGameViewModel.visualPieces.collectAsStateWithLifecycle()
    Log.d(TAG, "ChessGameScreen: pieces killed are: $piecesKilled")

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        chessGameViewModel.initializeSounds(context)
    }

    var checkMateDialog by remember {
        mutableStateOf(false)
    }
    var staleMateDialog by remember {
        mutableStateOf(false)
    }

    var showExitDialog by remember {
        mutableStateOf(false)
    }
    var showStartGameDialog by remember {
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

    if (showStartGameDialog) {
        StartGameDialog(
            onConfirm = {
                chessGameViewModel.startNewGame()
                showStartGameDialog = false
            }) {
            showStartGameDialog = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            GameTopBar(
                turnText = when (boardState.currentPlayer?.isWhite) {
                    true -> "White to move"
                    false -> "Black to move"
                    null -> ""
                },
                onExit = { showExitDialog = true },
                onRestart = { showStartGameDialog = true },
            )
        }
    ) { innerPadding ->
        boardState.currentPlayer?.let { currentPlayer ->
            ChessGrid(
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
                currentPlayer = currentPlayer,
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
                onAnalzeGame = {
                    onAnalyzeGame()
                },
                checkPosition = checkPosition,
                piecesKilled = piecesKilled,
                movesMadeSan = movesMadeSAN,
                showPromotionDialog = showPawnPromotion,
                aiLastMove = aiLastMove,
                visualPieces = visualPieces
            )
        }
    }

    Log.d(TAG, "ChessGameScreen last moved piece: ${boardState.lastMovedFrom}")
    LaunchedEffect(boardState.gameStatus) {
        when (boardState.gameStatus) {
            GameStatus.WHITE_WIN,
            GameStatus.BLACK_WIN -> {
                delay(300) // wait for piece animation
                delay(500) // dramatic pause 😎
                checkMateDialog = true
            }

            GameStatus.STALEMATE -> {
                delay(300)
                delay(500)
                staleMateDialog = true
            }

            else -> Unit
        }
    }
    AnimatedVisibility(visible = checkMateDialog) {
        CheckmateDialog(
            text = when (boardState.gameStatus) {
                GameStatus.WHITE_WIN -> "Checkmate — White wins"
                GameStatus.BLACK_WIN -> "Checkmate — Black wins"
                else -> "CheckMate"
            },
            onNewGameSelected = {
                chessGameViewModel.startNewGame()
                checkMateDialog = false
            }) {
            checkMateDialog = false
            chessGameViewModel.resetGameStatus()
        }
    }
    if (boardState.gameStatus == GameStatus.STALEMATE) {
        AnimatedVisibility(visible = staleMateDialog) {
            CheckmateDialog(
                text = "StaleMate",
                onNewGameSelected = {
                    chessGameViewModel.startNewGame()
                    staleMateDialog = false
                }) {
                staleMateDialog = false
                chessGameViewModel.resetGameStatus()
            }
        }
    }
}

// Minimal game top bar: exit on the left, live turn indicator as the title, restart on
// the right. Deliberately NOT the shared HomeScreen TopAppBar helper (that one has no
// actions slot and a heavier centered style). Fully qualified TopAppBar because this
// package declares its own composable with the same name.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameTopBar(
    turnText: String,
    onExit: () -> Unit,
    onRestart: () -> Unit,
) {
    androidx.compose.material3.TopAppBar(
        title = { Text(text = turnText, style = MaterialTheme.typography.titleMedium) },
        navigationIcon = {
            IconButton(onClick = onExit) {
                Icon(Icons.Filled.Close, contentDescription = "Exit game")
            }
        },
        actions = {
            IconButton(onClick = onRestart) {
                Icon(Icons.Filled.RestartAlt, contentDescription = "Restart game")
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PawnPromotionDialog(
    onPromotionSelected: (ChessPiece) -> Unit,
    color: com.example.chess.data.model.Color,
    onDismiss: () -> Unit,
) {
    var selectedOption by remember { mutableStateOf<ChessPiece?>(null) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
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
                            color = color,
                            position = Position(0, 0),
                            isKilled = false,
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
                            color = color,
                            position = Position(0, 0),
                            isKilled = false,
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
                            color = color,
                            position = Position(0, 0),
                            isKilled = false,
                            vectorAsset = if (color == com.example.chess.data.model.Color.WHITE) R.drawable.bishop_white else R.drawable.bishop_dark
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
                            color = color,
                            position = Position(0, 0),
                            isKilled = false,
                            vectorAsset = if (color == com.example.chess.data.model.Color.WHITE) R.drawable.knight_white else R.drawable.knight_dark
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
    BasicAlertDialog(onDismissRequest = { /* Handle dialog dismissal */ }) {
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
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(text = "New Game")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { onViewBoardSelected() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(text = "Show Board")
                    }
                }
            }
        }
    }
}

fun positionToOffset(
    row: Int,
    col: Int,
    squareSizePx: Int,
    boardOffset: Offset
): IntOffset {
    return IntOffset(
        x = (boardOffset.x + col * squareSizePx).roundToInt(),
        y = (boardOffset.y + row * squareSizePx).roundToInt()
    )
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
    onAnalzeGame: () -> Unit,
    checkPosition: Position,
    piecesKilled: List<ChessPiece>,
    movesMadeSan: MutableList<SANMovePair>,
    showPromotionDialog: Boolean,
    aiLastMove: Move?,
    visualPieces: ImmutableList<VisualPiece>
) {

    val blackPiecesKilled = remember (piecesKilled) {
        piecesKilled.filter {
            it.color == com.example.chess.data.model.Color.BLACK
        }
    }
    val whitePiecesKilled = remember {
        piecesKilled.filter {
            it.color == com.example.chess.data.model.Color.WHITE
        }
    }

    // Black's material advantage
    val whiteAdvantage = calculateMaterialAdvantage(piecesKilled)
    val blackAdvantage = -whiteAdvantage // Black’s perspective

    if (showPromotionDialog) {
        PawnPromotionDialog(
            onPromotionSelected = {
                onSetPromotionPiece(it)
            },
            color = if (currentPlayer.isWhite) com.example.chess.data.model.Color.BLACK else com.example.chess.data.model.Color.WHITE
        ) {

        }
    }


    // Flat themed background: the Scaffold provides colorScheme.background; the old
    // full-bleed wood JPG (ContentScale.FillBounds) distorted per aspect ratio.
    // `modifier` MUST live on this root — it carries the Scaffold innerPadding.
    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val scope = this
        val isLandscape = scope.maxWidth > scope.maxHeight

        // maxHeight already excludes the status bar, top bar, and nav bar (this Box
        // sits inside the padded Scaffold content), so only real content heights and
        // spacing are reserved here — no insets fudge factors.
        val labelSize = 20.dp
        val topPanelHeight = 36.dp
        val bottomPanelHeight = 60.dp
        val controlsHeight = 48.dp
        val sectionSpacing = 32.dp
        val totalReservedHeight =
            topPanelHeight + bottomPanelHeight + controlsHeight + sectionSpacing + labelSize

        val boardSize = if (isLandscape) {
            minOf(scope.maxWidth * 0.6f, scope.maxHeight - labelSize - 16.dp)
        } else {
            minOf(scope.maxWidth - labelSize, scope.maxHeight - totalReservedHeight)
        }
        val squareSize = boardSize / 8

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Section — slim captured-pieces strip (moves live in the bottom panel)
            Panel(
                lostPieces = whitePiecesKilled,
                moves = movesMadeSan,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                advantage = blackAdvantage,
                showMoves = false
            )

            // Board Section - Takes up the remaining space but maintains 1:1 aspect ratio
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.wrapContentSize()
                ) {
                    Column {
                        // Row labels 8..1
                        ('8' downTo '1').forEach { char ->
                            Box(
                                modifier = Modifier
                                    .height(squareSize)
                                    .width(labelSize)
                            ) {
                                Text(
                                    text = char.toString(),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.Center),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    val squareStates = remember(boardState.board, validMoves, lastMove, aiLastMove, isInCheck, checkPosition) {
                        buildSquareUiStates(boardState.board, validMoves, lastMove, aiLastMove, isInCheck, checkPosition)
                    }

                    Column {
                        Box(modifier = Modifier.size(boardSize)) {
                            Column {
                                squareStates.chunked(8).forEach { rowStates ->
                                    Row {
                                        rowStates.forEach { square ->
                                            key(square.position.row, square.position.column) {
                                                ChessSquare(
                                                    square = square,
                                                    onSelectSpot = { pos ->
                                                        onSelectSpot(boardState.board.getBox(pos.row, pos.column))
                                                    },
                                                    onMovePieceTo = { position -> onMovePieceTo(position) },
                                                    squareSize = squareSize,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            PiecesLayer(
                                pieces = visualPieces,
                                squareSize = squareSize
                            )
                        }
                        
                        Row {
                            ('a'..'h').forEach { char ->
                                Box(
                                    modifier = Modifier
                                        .size(width = squareSize, height = labelSize)
                                ) {
                                    Text(
                                        text = char.toString(),
                                        modifier = Modifier.align(Alignment.Center),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Section — moves+captured panel, then one 48dp controls row
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Panel(
                    lostPieces = blackPiecesKilled,
                    moves = movesMadeSan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    advantage = whiteAdvantage,
                    showMoves = true
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onUndoMove, modifier = Modifier.size(48.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo move",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    AnalyzeGameButton(
                        text = "Analyze Game",
                        icon = Icons.Outlined.Insights,
                        onClick = { onAnalzeGame() }
                    )
                    IconButton(onClick = onRedoMove, modifier = Modifier.size(48.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo move",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChessSquare(
    square: SquareUiState,
    onSelectSpot: (Position) -> Unit,
    onMovePieceTo: (Position) -> Unit,
    squareSize: Dp,
) {
    val backgroundColor = if (square.isWhite) ChessTheme.colors.boardLight else ChessTheme.colors.boardDark

    Box(
        modifier = Modifier
            .size(squareSize)
            .background(backgroundColor)
            .clickable {
                if (square.isValidMove) {
                    onMovePieceTo(square.position)
                } else {
                    onSelectSpot(square.position)
                }
            }
            .then(
                if (square.isLastMoveHighlight) {
                    Modifier.background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, ChessTheme.colors.boardLastMove)
                        )
                    )
                } else Modifier
            )
    ) {
        // Check highlight — red border around king's square
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    3.dp,
                    if (square.isCheckedSquare) ChessTheme.colors.boardCheck else Color.Transparent
                )
        )

        // Empty square valid move — small dot in center
        if (square.isValidMove && !square.isCapturableTarget) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color  = ChessTheme.colors.boardLegalDot,
                        shape  = CircleShape,
                    )
                    .align(Alignment.Center)
                    .zIndex(20f)   // above pieces layer
            )
        }
        if (square.isCapturableTarget) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(20f)   // above pieces layer
                    .border(
                        width  = 3.dp,
                        color  = ChessTheme.colors.boardSelected,
                        shape  = CircleShape,
                    )
                    .padding(2.dp)  // slight inset so ring is visible
            )
        }
    }
}

@Composable
fun AnimatedChessPiece(piece: VisualPiece, squareSize: Dp) {
    val density = LocalDensity.current

    val target by remember(piece.position, squareSize) {
        derivedStateOf {
            with(density) {
                Offset(
                    piece.position.column * squareSize.toPx(),
                    piece.position.row * squareSize.toPx()
                )
            }
        }
    }

    val startOffset by remember(piece.previousPosition, squareSize) {
        derivedStateOf {
            with(density) {
                val prev = piece.previousPosition ?: piece.position
                Offset(
                    prev.column * squareSize.toPx(),
                    prev.row * squareSize.toPx()
                )
            }
        }
    }

    val animOffset = remember(piece.id) {
        Animatable(startOffset, Offset.VectorConverter)
    }

    LaunchedEffect(target) {
        animOffset.animateTo(
            target,
            tween(
                durationMillis = 450,
                easing = FastOutSlowInEasing
            )
        )
    }

    Image(
        painter = painterResource(piece.asset),
        contentDescription = null,
        modifier = Modifier
            .size(squareSize)
            .zIndex(10f)
            .graphicsLayer {
                translationX = animOffset.value.x
                translationY = animOffset.value.y
            }
    )
}

@Composable
fun PiecesLayer(
    pieces: ImmutableList<VisualPiece>,
    squareSize: Dp
) {
    Box(Modifier.fillMaxSize()) {
        pieces.forEach { piece ->
            key(piece.id) {
                AnimatedChessPiece(piece, squareSize)
            }
        }
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
fun AnalyzeGameButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun Panel(
    lostPieces: List<ChessPiece?>,
    moves: List<SANMovePair>,
    modifier: Modifier = Modifier,
    advantage: Int,
    // The same move list used to render in BOTH panels (pure duplication); now only the
    // bottom panel shows it, and the top panel is a slimmer captured-pieces strip.
    showMoves: Boolean = true,
) {
    Surface(
        modifier = modifier
            .height(if (showMoves) 60.dp else 36.dp)
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (showMoves) Row {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (moves.isEmpty()) {
                        item {
                            Text(
                                text = "No moves yet",
                                style = MaterialTheme.typography.labelSmall,
                                color = LocalContentColor.current.copy(alpha = 0.6f)
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
                                                MaterialTheme.colorScheme.surface,
                                                shape = CircleShape
                                            )
                                            .padding(1.dp)
                                    )
                                    Text(
                                        text = movePair.whiteDestination,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            color = LocalContentColor.current
                                        )
                                    )
                                } ?: Text( // Pawns or castling
                                    text = movePair.whiteDestination,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        color = LocalContentColor.current
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
                                                MaterialTheme.colorScheme.surface,
                                                shape = CircleShape
                                            )
                                            .padding(1.dp)
                                            .alpha(if (index == moves.lastIndex) 1f else 0.7f)
                                    )
                                    Text(
                                        text = movePair.blackDestination,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            color = if (index == moves.lastIndex) ChessTheme.colors.evalAdvantage else LocalContentColor.current,
                                            fontWeight = if (index == moves.lastIndex) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                } ?: movePair.blackDestination.takeIf { it.isNotEmpty() }
                                    ?.let { destination ->
                                        Text(
                                            text = destination,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 12.sp,
                                                color = if (index == moves.lastIndex) ChessTheme.colors.evalAdvantage
                                                else LocalContentColor.current,
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
                                    MaterialTheme.colorScheme.surface,
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
                            advantage > 0 -> ChessTheme.colors.evalAdvantage
                            advantage < 0 -> MaterialTheme.colorScheme.error
                            else -> LocalContentColor.current.copy(alpha = 0.7f) // Neutral
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
