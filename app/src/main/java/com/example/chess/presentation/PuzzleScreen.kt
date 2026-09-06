package com.example.chess.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.chess.data.local.PuzzleEntity
import com.example.chess.data.model.Position
import com.example.chess.presentation.model.SquareUiState
import com.example.chess.presentation.model.buildSquareUiStates
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleListScreen(
    onOpenPuzzle: (Long) -> Unit,
    onBack: () -> Unit,
    puzzleViewModel: PuzzleViewModel = hiltViewModel(),
) {
    val puzzles by puzzleViewModel.puzzles.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Puzzles from your games") },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .padding(12.dp)
                            .clickable { onBack() }
                    )
                }
            )
        }
    ) { padding ->
        if (puzzles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    "No puzzles yet — play a game and review it to generate puzzles " +
                        "from your blunders and mistakes.",
                    modifier = Modifier.padding(32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(puzzles, key = { it.id }) { puzzle ->
                    PuzzleListRow(puzzle = puzzle) {
                        onOpenPuzzle(puzzle.id)
                    }
                }
            }
        }
    }
}

@Composable
private fun PuzzleListRow(puzzle: PuzzleEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(puzzle.category, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text(
                    DateFormat.getDateInstance().format(Date(puzzle.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (puzzle.solved) {
                Text("Solved", color = Color(0xFF2E7D32))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleScreen(
    puzzleId: Long,
    onBack: () -> Unit,
    puzzleViewModel: PuzzleViewModel = hiltViewModel(),
) {
    // PuzzleListScreen and PuzzleScreen are separate nav destinations, so each gets its
    // own PuzzleViewModel instance by default — load by id here rather than relying on
    // a loadPuzzle() call made from the list screen's (different) instance.
    LaunchedEffect(puzzleId) {
        puzzleViewModel.loadPuzzleById(puzzleId)
    }

    val boardState by puzzleViewModel.boardState.collectAsStateWithLifecycle()
    val validMoves by puzzleViewModel.validMoves.collectAsStateWithLifecycle()
    val feedback by puzzleViewModel.feedback.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solve the puzzle") },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .padding(12.dp)
                            .clickable { onBack() }
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (feedback) {
                PuzzleFeedback.CORRECT -> FeedbackBanner("Correct!", Color(0xFF2E7D32))
                PuzzleFeedback.INCORRECT -> FeedbackBanner("Not quite — try again.", Color(0xFFC62828))
                PuzzleFeedback.NONE -> Spacer(modifier = Modifier.height(0.dp))
            }

            val screenWidth = LocalConfiguration.current.screenWidthDp.toFloat()
            val squareSize = (screenWidth / 8).dp

            val squareStates = remember(boardState.board, validMoves) {
                buildSquareUiStates(
                    board = boardState.board,
                    validMoves = validMoves,
                    lastMove = null,
                    aiLastMove = null,
                    isInCheck = false,
                    checkPosition = Position(0, 0),
                )
            }

            Column(modifier = Modifier.padding(top = 16.dp)) {
                squareStates.chunked(8).forEach { rowStates ->
                    Row {
                        rowStates.forEach { square ->
                            PuzzleSquare(
                                square = square,
                                squareSize = squareSize,
                                onSelectSpot = { position ->
                                    puzzleViewModel.onSelectSpot(boardState.board.getBox(position.row, position.column))
                                },
                                onMovePieceTo = { position -> puzzleViewModel.onMovePieceTo(position) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedbackBanner(text: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(color.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = color, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    }
}

// A minimal, puzzle-specific square renderer — deliberately not the full ChessGrid/
// ChessSquare used for live play, which bundles game-specific chrome (undo/redo,
// analyze, exit dialogs) that doesn't belong in a puzzle-solving screen. Pieces render
// statically per square (no slide animation) — reusing the live-play VisualPiece/
// PiecesLayer animation system wasn't worth the added coupling for this v1.
@Composable
private fun PuzzleSquare(
    square: SquareUiState,
    squareSize: androidx.compose.ui.unit.Dp,
    onSelectSpot: (Position) -> Unit,
    onMovePieceTo: (Position) -> Unit,
) {
    val backgroundColor = if (square.isWhite) Color.White else Color(0xFFA26232)

    Box(
        modifier = Modifier
            .size(squareSize)
            .background(backgroundColor)
            .clickable {
                if (square.isValidMove) onMovePieceTo(square.position) else onSelectSpot(square.position)
            }
            .then(
                if (square.isLastMoveHighlight) {
                    Modifier.background(Color.Green.copy(alpha = 0.3f))
                } else Modifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        square.pieceAsset?.let { asset ->
            Image(
                painter = painterResource(id = asset),
                contentDescription = null,
                modifier = Modifier.size(squareSize * 0.85f),
            )
        }
        if (square.isValidMove && !square.isCapturableTarget) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color = Color(0x8800C853), shape = CircleShape)
            )
        }
        if (square.isCapturableTarget) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(width = 3.dp, color = Color(0xFFFFB300), shape = CircleShape)
                    .padding(2.dp)
            )
        }
    }
}
