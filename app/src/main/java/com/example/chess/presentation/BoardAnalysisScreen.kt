package com.example.chess.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chess.data.model.Feedback
import com.example.chess.domain.model.GameSummary
import com.example.chess.domain.model.MoveInsight
import com.example.chess.presentation.model.buildSquareUiStates

// ── Design tokens ─────────────────────────────────────────────────────────────

private val ColorOak         = Color(0xFFA26232)
private val ColorWhiteSquare = Color(0xFFF0D9B5)
private val ColorBlunder     = Color(0xFFE24B4A)

@Composable
fun BoardAnalysisScreen(
    chessGameViewModel: ChessGameViewModel,
    onBack: () -> Unit,
) {
    val boardState         by chessGameViewModel.boardState.collectAsStateWithLifecycle()
    val lastMove           by chessGameViewModel.lastMove.collectAsStateWithLifecycle()
    val validMoves         by chessGameViewModel.validMoves.collectAsStateWithLifecycle()
    val isInCheck          by chessGameViewModel.isInCheck.collectAsStateWithLifecycle()
    val checkPosition      by chessGameViewModel.checkPosition.collectAsStateWithLifecycle()
    val aiLastMove         by chessGameViewModel.aiLastMove.collectAsStateWithLifecycle()
    val feedback           by chessGameViewModel.feedback.collectAsStateWithLifecycle()
    val isPlaying          by chessGameViewModel.isPlaying.collectAsStateWithLifecycle()
    val playbackSpeed      by chessGameViewModel.playbackSpeed.collectAsStateWithLifecycle()
    val isTransitioning    by chessGameViewModel.isTransitioning.collectAsStateWithLifecycle()
    val visualPieces       by chessGameViewModel.visualPieces.collectAsStateWithLifecycle()
    val explanationLoading by chessGameViewModel.explanationLoading.collectAsStateWithLifecycle()
    val aiExplanationDisabled by chessGameViewModel.aiExplanationDisabled.collectAsStateWithLifecycle()
    val analysisResult     by chessGameViewModel.analysisResult.collectAsStateWithLifecycle()
    val currentMoveIndex   by chessGameViewModel.currentMoveIndex.collectAsStateWithLifecycle()
    val currentInsight     by chessGameViewModel.currentInsight.collectAsStateWithLifecycle()
    val gameSummary        by chessGameViewModel.gameSummary.collectAsStateWithLifecycle()
    val bestMoveArrow      by chessGameViewModel.bestMoveArrow.collectAsStateWithLifecycle()
    val playerMoveArrow    by chessGameViewModel.playerMoveArrow.collectAsStateWithLifecycle()

    val screenWidth = LocalConfiguration.current.screenWidthDp.toFloat()
    // 28dp eval bar + 4dp gap + 8dp padding each side = 48dp reserved
    val boardWidth  = screenWidth - 48f
    val squareSize  = (boardWidth / 8).dp

    val currentEval = analysisResult?.evaluations
        ?.getOrNull((currentMoveIndex - 1).coerceAtLeast(0)) ?: 0

    val totalMoves = chessGameViewModel.totalMovesCount()
    val isAtEnd    = totalMoves > 0 && currentMoveIndex >= totalMoves

    Scaffold(
        topBar         = { TopAppBar(text = "Game review", onBack = onBack) },
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {

            // ── Move controls ─────────────────────────────────────────────────
            ReviewControls(
                isPlaying        = isPlaying,
                isTransitioning  = isTransitioning,
                currentMoveIndex = currentMoveIndex,
                totalMoves       = totalMoves,
                onPrevious       = { chessGameViewModel.launchPreviousMove() },
                onPlayPause      = { chessGameViewModel.onPlayPause() },
                onNext           = { chessGameViewModel.launchNextMove() },
                currentCategory  = feedback?.type,
                playbackSpeed    = playbackSpeed,
                onCycleSpeed     = { chessGameViewModel.cyclePlaybackSpeed() },
                modifier         = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )

            // ── Eval bar + board ──────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Eval bar
                EvalBar(
                    evalCp   = currentEval,
                    modifier = Modifier
                        .width(28.dp)
                        .height(squareSize * 8)
                        .clip(RoundedCornerShape(6.dp)),
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Board stack: squares → arrows → pieces
                val squareStates = remember(boardState.board, validMoves, lastMove, aiLastMove, isInCheck, checkPosition) {
                    buildSquareUiStates(boardState.board, validMoves, lastMove, aiLastMove, isInCheck, checkPosition)
                }

                Box(modifier = Modifier.size(squareSize * 8)) {

                    // Layer 1 — squares
                    Column {
                        squareStates.chunked(8).forEach { rowStates ->
                            Row {
                                rowStates.forEach { square ->
                                    key(square.position.row, square.position.column) {
                                        ChessSquare(
                                            square        = square,
                                            onSelectSpot  = { pos ->
                                                boardState.board?.getBox(pos.row, pos.column)
                                                    ?.let { spot -> chessGameViewModel.onSelectSpot(spot) }
                                            },
                                            onMovePieceTo = { chessGameViewModel.movePieceTo(it) },
                                            squareSize    = squareSize,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Layer 2 — arrows (above squares, below pieces)
                    ArrowLayer(
                        bestMove    = bestMoveArrow,
                        playerMove  = playerMoveArrow,
                        squareSize  = squareSize,
                        modifier    = Modifier
                            .size(squareSize * 8)
                            .zIndex(5f),
                    )

                    // Layer 3 — animated pieces (always on top)
                    PiecesLayer(
                        pieces     = visualPieces,
                        squareSize = squareSize,
                    )
                }
            }

            // ── Eval graph ────────────────────────────────────────────────────
            analysisResult?.let { result ->
                EvalGraphCard(
                    evaluations    = result.evaluations,
                    moveCategories = result.moveCategories,
                    currentIndex   = currentMoveIndex,
                    modifier       = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }

            // ── Move feedback card ────────────────────────────────────────────
            AnimatedVisibility(
                visible = feedback != null,
                enter   = fadeIn(tween(200)) + slideInVertically(
                    animationSpec  = tween(250),
                    initialOffsetY = { it / 3 },
                ),
                exit    = fadeOut(tween(150)),
            ) {
                feedback?.let {
                    MoveFeedbackCard(
                        feedback     = it,
                        insight      = currentInsight,
                        isLoading    = explanationLoading,
                        aiUnavailable = aiExplanationDisabled,
                        modifier     = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }

            // ── Game summary (shown at end of review) ─────────────────────────
            AnimatedVisibility(
                visible = isAtEnd && gameSummary != null,
                enter   = fadeIn(tween(300)) + slideInVertically(
                    animationSpec  = tween(350),
                    initialOffsetY = { it / 2 },
                ),
                exit    = fadeOut(tween(150)),
            ) {
                gameSummary?.let {
                    GameSummaryCard(
                        summary  = it,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Arrow layer ───────────────────────────────────────────────────────────────

@Composable
fun ArrowLayer(
    bestMove: String?,
    playerMove: String?,
    squareSize: Dp,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    Canvas(modifier = modifier.fillMaxSize()) {
        val sq = with(density) { squareSize.toPx() }

        // Draw player's move first so best move renders on top
        playerMove?.let { move ->
            if (move.length >= 4) {
                val from = move.substring(0, 2).toSquareCenter(sq)
                val to   = move.substring(2, 4).toSquareCenter(sq)
                if (from != null && to != null && from != to) {
                    drawArrow(
                        start       = from,
                        end         = to,
                        color       = Color(0xAAFFFFFF),
                        strokeWidth = sq * 0.10f,
                        headSize    = sq * 0.28f,
                    )
                }
            }
        }

        // Best move — teal, slightly larger, drawn on top
        bestMove?.let { move ->
            if (move.length >= 4) {
                val from = move.substring(0, 2).toSquareCenter(sq)
                val to   = move.substring(2, 4).toSquareCenter(sq)
                if (from != null && to != null && from != to) {
                    drawArrow(
                        start       = from,
                        end         = to,
                        color       = Color(0xCC1D9E75),
                        strokeWidth = sq * 0.12f,
                        headSize    = sq * 0.32f,
                    )
                }
            }
        }
    }
}

// ── Arrow drawing ─────────────────────────────────────────────────────────────

private fun DrawScope.drawArrow(
    start: Offset,
    end: Offset,
    color: Color,
    strokeWidth: Float,
    headSize: Float,
) {
    val dx     = end.x - start.x
    val dy     = end.y - start.y
    val length = kotlin.math.sqrt(dx * dx + dy * dy)
    if (length == 0f) return

    val unitX = dx / length
    val unitY = dy / length

    // Shaft — stops short of arrowhead base
    val lineEnd = Offset(
        x = end.x - unitX * headSize * 0.8f,
        y = end.y - unitY * headSize * 0.8f,
    )
    drawLine(
        color       = color,
        start       = start,
        end         = lineEnd,
        strokeWidth = strokeWidth,
        cap         = StrokeCap.Round,
    )

    // Arrowhead — filled triangle
    val perpX    = -unitY
    val perpY    =  unitX
    val headBase = Offset(
        x = end.x - unitX * headSize,
        y = end.y - unitY * headSize,
    )
    val arrowPath = Path().apply {
        moveTo(end.x, end.y)
        lineTo(
            headBase.x + perpX * headSize * 0.5f,
            headBase.y + perpY * headSize * 0.5f,
        )
        lineTo(
            headBase.x - perpX * headSize * 0.5f,
            headBase.y - perpY * headSize * 0.5f,
        )
        close()
    }
    drawPath(path = arrowPath, color = color)
}

// ── Square coordinate helper ──────────────────────────────────────────────────

/**
 * Converts a 2-char UCI square string ("e4", "g1" etc.) to the
 * centre pixel of that square within the board canvas.
 */
private fun String.toSquareCenter(squareSizePx: Float): Offset? {
    if (length < 2) return null
    val file = this[0]   // 'a'..'h'
    val rank = this[1]   // '1'..'8'
    if (file !in 'a'..'h' || rank !in '1'..'8') return null

    val col = file - 'a'          // 0 = left,  7 = right
    val row = 8 - (rank - '0')    // 0 = top,   7 = bottom  (rank 8 → row 0)

    return Offset(
        x = col * squareSizePx + squareSizePx / 2f,
        y = row * squareSizePx + squareSizePx / 2f,
    )
}

// ── Review controls ───────────────────────────────────────────────────────────

@Composable
private fun ReviewControls(
    isPlaying: Boolean,
    isTransitioning: Boolean,
    currentMoveIndex: Int,
    totalMoves: Int,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    currentCategory: String?,
    playbackSpeed: Float,
    onCycleSpeed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = currentCategory?.let { categoryColor(it) } ?: ColorOak

    Surface(
        modifier       = modifier,
        shape          = RoundedCornerShape(14.dp),
        color          = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.width(52.dp)) {
                Text(
                    text  = if (totalMoves > 0) "$currentMoveIndex / $totalMoves" else "— / —",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text     = "${formatSpeed(playbackSpeed)}x",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.clickable { onCycleSpeed() },
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                ControlButton(
                    onClick = onPrevious,
                    enabled = currentMoveIndex > 0 && !isPlaying && !isTransitioning,
                    accent  = accent,
                ) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous",
                        modifier           = Modifier.size(18.dp),
                    )
                }

                Box(
                    modifier         = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(accent)
                        .clickable { onPlayPause() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector        = if (isPlaying) Icons.Default.Pause
                        else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint               = Color.White,
                        modifier           = Modifier.size(22.dp),
                    )
                }

                ControlButton(
                    onClick = onNext,
                    enabled = currentMoveIndex < totalMoves && !isPlaying && !isTransitioning,
                    accent  = accent,
                ) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        modifier           = Modifier.size(18.dp),
                    )
                }
            }

            currentCategory?.let { cat ->
                Box(
                    modifier         = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accent.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text  = "${categoryIcon(cat)} $cat",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                    )
                }
            } ?: Spacer(modifier = Modifier.width(52.dp))
        }
    }
}

private fun formatSpeed(speed: Float): String =
    if (speed == speed.toInt().toFloat()) speed.toInt().toString() else speed.toString()

@Composable
private fun ControlButton(
    onClick: () -> Unit,
    enabled: Boolean,
    accent: Color,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (enabled) accent.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
            )
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides
                    if (enabled) accent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        ) {
            content()
        }
    }
}

// ── Eval bar ──────────────────────────────────────────────────────────────────

@Composable
fun EvalBar(
    evalCp: Int,
    modifier: Modifier = Modifier,
) {
    val clampedEval   = evalCp.coerceIn(-800, 800)
    val whiteFraction = (clampedEval + 800f) / 1600f
    // Not destructured with `by` — reading .value happens inside drawWithContent{}
    // below (draw phase only). Resizing a Box's real layout height every animation
    // frame (via weight() or Modifier.layout{}) forces a full relayout+redraw
    // regardless of which phase reads the animated value — a size change can't be
    // handled as a cheap GPU-layer transform. Keeping both boxes full-size and
    // clipping what's drawn avoids the relayout entirely: just a clip + redraw.
    val animatedFraction = animateFloatAsState(
        targetValue   = whiteFraction,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label         = "eval_bar",
    )

    val evalLabel = when {
        evalCp >  900 -> "+M"
        evalCp < -900 -> "-M"
        evalCp >  0   -> "+${"%.1f".format(evalCp / 100f)}"
        evalCp <  0   -> "${"%.1f".format(evalCp / 100f)}"
        else          -> "0.0"
    }

    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
        // Gray fill — clipped to the top (1 - fraction) portion, draw phase only.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    val f = (1f - animatedFraction.value).coerceAtLeast(0.02f)
                    clipRect(bottom = size.height * f) { this@drawWithContent.drawContent() }
                }
                .background(Color(0xFF2A2A2A))
        )

        // White fill — clipped to the bottom fraction portion, draw phase only.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    val f = animatedFraction.value.coerceAtLeast(0.02f)
                    clipRect(top = size.height * (1f - f)) { this@drawWithContent.drawContent() }
                }
                .background(ColorWhiteSquare)
        )

        // Labels pinned to the fixed top/bottom of the bar — independent of the
        // animated fraction, so they never trigger layout on their own.
        if (evalCp < -50) {
            Text(
                text      = evalLabel,
                fontSize  = 8.sp,
                color     = Color.White.copy(alpha = 0.6f),
                modifier  = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 3.dp),
                textAlign = TextAlign.Center,
            )
        }
        if (evalCp >= -50) {
            Text(
                text      = evalLabel,
                fontSize  = 8.sp,
                color     = Color(0xFF1A1A1A).copy(alpha = 0.6f),
                modifier  = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 3.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Eval graph card ───────────────────────────────────────────────────────────

@Composable
private fun EvalGraphCard(
    evaluations: List<Int>,
    moveCategories: List<String>,
    currentIndex: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    text  = "Evaluation",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (evaluations.isNotEmpty()) {
                    val lastEval = evaluations.getOrNull(currentIndex - 1) ?: 0
                    val label    = when {
                        lastEval > 0 -> "+${"%.1f".format(lastEval / 100f)}"
                        else         -> "${"%.1f".format(lastEval / 100f)}"
                    }
                    Text(
                        text  = label,
                        style = MaterialTheme.typography.labelSmall
                            .copy(fontWeight = FontWeight.SemiBold),
                        color = if (lastEval >= 0) ColorWhiteSquare
                        else Color(0xFF888888),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            EvalGraph(
                evaluations    = evaluations,
                moveCategories = moveCategories,
                currentIndex   = currentIndex,
                modifier       = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
            )
        }
    }
}

// ── Eval graph canvas ─────────────────────────────────────────────────────────

// The curve, fills, and blunder dots depend only on the (fixed, post-analysis) evaluation
// history and canvas size — never on which move is currently selected.
private class EvalGraphStatics(
    val path: Path,
    val whiteFill: Path,
    val blackFill: Path,
    val blunderDots: List<Offset>,
    val xStep: Float,
    val mid: Float,
    val height: Float,
)

@Composable
fun EvalGraph(
    evaluations: List<Int>,
    moveCategories: List<String>,
    currentIndex: Int,
    modifier: Modifier = Modifier,
) {
    val clampedEvals = remember(evaluations) { evaluations.map { it.coerceIn(-800, 800) } }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    // Rebuilding these Paths (O(n) over the whole game) from scratch on every single
    // Next/Previous/auto-review step was wasted main-thread work competing with the
    // piece-slide animation on every move, growing with game length — remembering them
    // means a step only has to redraw the cheap current-move marker line below.
    val statics = remember(clampedEvals, canvasSize) {
        if (clampedEvals.size < 2 || canvasSize.width == 0) return@remember null

        val w     = canvasSize.width.toFloat()
        val h     = canvasSize.height.toFloat()
        val mid   = h / 2f
        val xStep = w / (clampedEvals.size - 1).toFloat()

        val path = Path().apply {
            clampedEvals.forEachIndexed { i, eval ->
                val x = i * xStep
                val y = mid - (eval / 800f) * mid
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
        }

        val whiteFill = Path().apply {
            clampedEvals.forEachIndexed { i, eval ->
                val x = i * xStep
                val y = (mid - (eval / 800f) * mid).coerceAtMost(mid)
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            lineTo((clampedEvals.size - 1) * xStep, mid)
            lineTo(0f, mid)
            close()
        }

        val blackFill = Path().apply {
            moveTo(0f, mid)
            clampedEvals.forEachIndexed { i, eval ->
                val x = i * xStep
                val y = (mid - (eval / 800f) * mid).coerceAtLeast(mid)
                lineTo(x, y)
            }
            lineTo((clampedEvals.size - 1) * xStep, mid)
            close()
        }

        val blunderDots = moveCategories.mapIndexedNotNull { i, cat ->
            if ((cat == "Blunder" || cat == "MissedMate") && i < clampedEvals.size) {
                Offset(i * xStep, mid - (clampedEvals[i] / 800f) * mid)
            } else null
        }

        EvalGraphStatics(path, whiteFill, blackFill, blunderDots, xStep, mid, h)
    }

    Box(modifier = modifier.onSizeChanged { canvasSize = it }) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val s = statics ?: return@Canvas

            drawLine(
                color       = Color(0xFF444444),
                start       = Offset(0f, s.mid),
                end         = Offset(size.width, s.mid),
                strokeWidth = 1.dp.toPx(),
                pathEffect  = PathEffect.dashPathEffect(floatArrayOf(5f, 4f)),
            )

            drawPath(
                s.whiteFill,
                Brush.verticalGradient(
                    colors = listOf(ColorWhiteSquare.copy(alpha = 0.25f), Color.Transparent),
                    startY = 0f,
                    endY   = s.mid,
                ),
            )

            drawPath(
                s.blackFill,
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0xFF444444).copy(alpha = 0.3f)),
                    startY = s.mid,
                    endY   = s.height,
                ),
            )

            drawPath(
                path  = s.path,
                color = ColorWhiteSquare.copy(alpha = 0.8f),
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round),
            )

            s.blunderDots.forEach { center ->
                drawCircle(
                    color  = ColorBlunder,
                    radius = 3.5.dp.toPx(),
                    center = center,
                )
            }

            if (currentIndex > 0 && currentIndex <= clampedEvals.size) {
                val curX = (currentIndex - 1) * s.xStep
                drawLine(
                    color       = ColorOak.copy(alpha = 0.7f),
                    start       = Offset(curX, 0f),
                    end         = Offset(curX, s.height),
                    strokeWidth = 1.5.dp.toPx(),
                )
            }
        }
    }
}

// ── Move feedback card ────────────────────────────────────────────────────────

@Composable
fun MoveFeedbackCard(
    feedback: Feedback,
    insight: MoveInsight?,
    isLoading: Boolean,
    aiUnavailable: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val accent = categoryColor(feedback.type)
    val icon   = categoryIcon(feedback.type)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .drawBehind {
                drawRect(
                    color   = accent,
                    topLeft = Offset(0f, 0f),
                    size    = Size(3.dp.toPx(), size.height),
                )
            }
            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier         = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text       = icon,
                            color      = accent,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text       = feedback.type,
                            color      = accent,
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text  = feedback.move,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }

                feedback.bestMove?.let { best ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(accent.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text  = "Best: $best",
                            color = accent,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }

            insight?.let { ins ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    if (ins.playerMoveRank > 1) {
                        MetaChip(
                            text  = when (ins.playerMoveRank) {
                                2    -> "2nd best"
                                3    -> "3rd best"
                                else -> "Not top 3"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            bg    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                        )
                    }
                    MetaChip(
                        text  = ins.gamePhase.name
                            .lowercase()
                            .replaceFirstChar { it.uppercase() },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        bg    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                    )
                    if (ins.isCriticalMoment) {
                        MetaChip(
                            text  = "🔥 Key moment",
                            color = Color(0xFFFFB74D),
                            bg    = Color(0xFFFFB74D).copy(alpha = 0.10f),
                        )
                    }
                    if (ins.evalLost > 40) {
                        MetaChip(
                            text  = "-${"%.1f".format(ins.evalLost / 100f)}♟",
                            color = accent.copy(alpha = 0.8f),
                            bg    = accent.copy(alpha = 0.08f),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = accent.copy(alpha = 0.10f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            if (isLoading) {
                ShimmerExplanation()
            } else {
                Text(
                    text       = feedback.explanation,
                    color      = MaterialTheme.colorScheme.onSurface,
                    style      = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp,
                )
                if (aiUnavailable) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text  = "Standard analysis — AI commentary unavailable",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            insight?.bestContinuation
                ?.takeIf { it.isNotEmpty() && !isLoading }
                ?.let { line ->
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(
                        color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f),
                        thickness = 0.5.dp,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BestContinuation(moves = line, accent = accent)
                }
        }
    }
}

// ── Game summary card ─────────────────────────────────────────────────────────

@Composable
fun GameSummaryCard(
    summary: GameSummary,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text  = "Game summary",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (summary.openingName != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text  = if (summary.openingEco != null) {
                        "${summary.openingName} (${summary.openingEco})"
                    } else {
                        summary.openingName
                    },
                    style      = MaterialTheme.typography.bodySmall,
                    color      = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text  = summary.gameNarrative,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.06f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ColorOak.copy(alpha = 0.10f))
                    .padding(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(text = "💡", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text       = "Coach tip",
                        color      = ColorOak,
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text  = summary.weaknessHint,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

// ── Shared sub-components ─────────────────────────────────────────────────────

@Composable
private fun MetaChip(text: String, color: Color, bg: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text  = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun BestContinuation(moves: List<String>, accent: Color) {
    Column {
        Text(
            text  = "Best continuation",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            moves.forEachIndexed { index, move ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (index == 0) accent.copy(alpha = 0.18f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text       = move.toReadableNotation(),
                        color      = if (index == 0) accent
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = if (index == 0) FontWeight.SemiBold
                        else FontWeight.Normal,
                    )
                }
                if (index < moves.lastIndex) {
                    Text(
                        text  = "→",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun ShimmerExplanation() {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    // Not destructured with `by` — reading .value happens inside drawBehind{} below
    // (draw phase only), so this infinite 1200ms animation — active for up to 4s on
    // every single move step while the AI explanation loads — no longer recomposes
    // this composable (and rebuilds a Brush) on every one of its frames.
    val translateX = transition.animateFloat(
        initialValue  = -300f,
        targetValue   = 300f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_translate",
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(11.dp)
                .clip(RoundedCornerShape(4.dp))
                .drawBehind {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = shimmerColors,
                            start  = Offset(translateX.value, 0f),
                            end    = Offset(translateX.value + 200f, 0f),
                        )
                    )
                },
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .height(11.dp)
                .clip(RoundedCornerShape(4.dp))
                .drawBehind {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = shimmerColors,
                            start  = Offset(translateX.value, 0f),
                            end    = Offset(translateX.value + 200f, 0f),
                        )
                    )
                },
        )
    }
}

// ── Notation helper ───────────────────────────────────────────────────────────

private fun String.toReadableNotation(): String {
    if (length < 4) return this
    return when {
        this == "e1g1" || this == "e8g8" -> "O-O"
        this == "e1c1" || this == "e8c8" -> "O-O-O"
        else -> takeLast(2)
    }
}
