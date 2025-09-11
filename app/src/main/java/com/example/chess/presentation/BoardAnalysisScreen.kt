package com.example.chess.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.chess.R
import com.example.chess.data.model.Feedback
import com.example.chess.ui.theme.ChessTheme

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
    val feedback by chessGameViewModel.feedback.collectAsState()
    val isPlaying by chessGameViewModel.isPlaying.collectAsState()

    val screenWidth = LocalConfiguration.current.screenWidthDp.toFloat()
    val squareSize = (screenWidth / 8).dp

    var showStartReviewButton by remember {
        mutableStateOf(true)
    }

    var showMoveFeedback by remember {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = { TopAppBar(text = "Game Analysis") },
        modifier = Modifier.fillMaxSize().background(Color.Transparent),
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .zIndex(0f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.chesstable),
                contentDescription = "chess table",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            Column (
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MoveControls(
                    isPlaying = isPlaying,
                    onPrevious = {
                        chessGameViewModel.previousMove()
                    },
                    onPlayPause = {
                        chessGameViewModel.onPlayPause()
                    },
                    onNext = {
                        chessGameViewModel.nextMove(isReviewMode = true)
                    },
                    totalMoves = 23,
                    currentMoveIndex = 3
                )
            }

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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
            ) {
                if (showStartReviewButton) {
                    AnalyzeGameButton(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 16.dp),
                        text = "Start Review",
                        iconRes = R.drawable.baseline_fullscreen_exit_24,
                        onClick = {
                            chessGameViewModel.startAutoReview()
                            showStartReviewButton = false
                            showMoveFeedback = true
                        }
                    )
                }
                if (showMoveFeedback) {
                    feedback?.let {
                        MoveFeedback(
                            feedback = it
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoveFeedback(feedback: Feedback) {
    val (icon, color) = when (feedback.type) {
        "Best move" -> Pair(Icons.Default.Check, Color.Green)
        "Good move" -> Pair(Icons.Default.Info, Color.Blue)
        "Inaccurate" -> Pair(Icons.Default.Warning, Color.Yellow)
        "Mistake" -> Pair(Icons.Default.Warning, Color(0xFFFFA500)) // Orange
        "Blunder" -> Pair(Icons.Default.Error, Color.Red)
        "Brilliant move" -> Pair(Icons.Default.Star, Color.Yellow)
        else -> Pair(null, Color.Black)
    }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Your move: ${feedback.move}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.semantics { contentDescription = "${feedback.type} feedback" }
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = "${feedback.type} icon",
                        tint = color
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = feedback.type,
                    style = MaterialTheme.typography.titleSmall,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = feedback.explanation,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Move controls composable
@Composable
fun MoveControls(
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    totalMoves: Int,
    currentMoveIndex: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = Color(0xFFA26232),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomIconButton(
                onClick = onPrevious,
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous Move",
                enabled = currentMoveIndex > 0
            )
            CustomIconButton(
                onClick = onPlayPause,
                icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play"
            )
            CustomIconButton(
                onClick = onNext,
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next Move",
                enabled = currentMoveIndex < totalMoves
            )
        }
    }
}

// Custom icon button composable
@Composable
fun CustomIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = if (enabled) Color(0xFF333333) else Color(0xFF666666),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            enabled = enabled
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White
            )
        }
    }
}


@Preview(showBackground = true, name = "Move Feedback Preview")
@Composable
fun MoveFeedbackPreview() {
    ChessTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Brilliant move
            MoveFeedback(
                Feedback(
                    move = "e4",
                    type = "Brilliant move",
                    explanation = "This opens up the center and prepares for development."
                )
            )
            // Mistake
            MoveFeedback(
                Feedback(
                    move = "f3",
                    type = "Mistake",
                    explanation = "This weakens the king. Better was d4.",
                    bestMove = "d4"
                )
            )
            // Blunder
            MoveFeedback(
                Feedback(
                    move = "Qh5",
                    type = "Blunder",
                    explanation = "This loses the queen to Nf6.",
                    bestMove = "Nf3"
                )
            )
            // Good move
            MoveFeedback(
                Feedback(
                    move = "Nf3",
                    type = "Good move",
                    explanation = "Develops a knight and controls the center."
                )
            )
        }
    }
}