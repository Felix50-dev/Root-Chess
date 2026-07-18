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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.chess.data.model.Feedback
import com.example.chess.domain.model.GameSummary
import com.example.chess.domain.model.MoveInsight

// ── Design tokens ─────────────────────────────────────────────────────────────

private val ColorBg          = Color(0xFF0D0D0D)
private val ColorSurface     = Color(0xFF1A1A1A)
private val ColorOak         = Color(0xFFA26232)
private val ColorWhiteSquare = Color(0xFFF0D9B5)
private val ColorBlunder     = Color(0xFFE24B4A)

private fun categoryColor(type: String) = when (type) {
    "Brilliant"                -> Color(0xFF1D9E75)
    "Best"                     -> Color(0xFF378ADD)
    "Great"                    -> Color(0xFF639922)
    "Inaccuracy", "Inaccurate" -> Color(0xFFBA7517)
    "Mistake"                  -> Color(0xFFD85A30)
    "Blunder"                  -> Color(0xFFE24B4A)
    "Miss"                     -> Color(0xFFD4537E)
    "MissedMate"               -> Color(0xFF7F77DD)
    else                       -> Color(0xFF888888)
}

private fun categoryIcon(type: String) = when (type) {
    "Brilliant"                -> "✦"
    "Best"                     -> "✓"
    "Great"                    -> "↑"
    "Inaccuracy", "Inaccurate" -> "?!"
    "Mistake"                  -> "?"
    "Blunder"                  -> "??"
    "Miss"                     -> "!"
    "MissedMate"               -> "M"
    else                       -> "·"
}

@Composable
fun BoardAnalysisScreen(
    chessGameViewModel: ChessGameViewModel,
) {
    val boardState         by chessGameViewModel.boardState.collectAsState()
    val lastMove           by chessGameViewModel.lastMove.collectAsState()
    val validMoves         by chessGameViewModel.validMoves.collectAsState()
    val isInCheck          by chessGameViewModel.isInCheck.collectAsState()
    val checkPosition      by chessGameViewModel.checkPosition.collectAsState()
    val aiLastMove         by chessGameViewModel.aiLastMove.collectAsState()
    val feedback           by chessGameViewModel.feedback.collectAsState()
    val isPlaying          by chessGameViewModel.isPlaying.collectAsState()
    val visualPieces       by chessGameViewModel.visualPieces.collectAsState()
    val explanationLoading by chessGameViewModel.explanationLoading.collectAsState()
    val analysisResult     by chessGameViewModel.analysisResult.collectAsState()
    val currentMoveIndex   by chessGameViewModel.currentMoveIndex.collectAsState()
    val currentInsight     by chessGameViewModel.currentInsight.collectAsState()
    val gameSummary        by chessGameViewModel.gameSummary.collectAsState()
    val bestMoveArrow      by chessGameViewModel.bestMoveArrow.collectAsState()
    val playerMoveArrow    by chessGameViewModel.playerMoveArrow.collectAsState()

    val screenWidth = LocalConfiguration.current.screenWidthDp.toFloat()
    // 28dp eval bar + 4dp gap + 8dp padding each side = 48dp reserved
    val boardWidth  = screenWidth - 48f
    val squareSize  = (boardWidth / 8).dp

    val currentEval = analysisResult?.evaluations
        ?.getOrNull((currentMoveIndex - 1).coerceAtLeast(0)) ?: 0

    val totalMoves = chessGameViewModel.totalMovesCount()
    val isAtEnd    = totalMoves > 0 && currentMoveIndex >= totalMoves

    Scaffold(
        topBar         = { TopAppBar(text = "Game review") },
        containerColor = ColorBg,
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ColorBg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {

            // ── Move controls ─────────────────────────────────────────────────
            ReviewControls(
                isPlaying        = isPlaying,
                currentMoveIndex = currentMoveIndex,
                totalMoves       = totalMoves,
                onPrevious       = { chessGameViewModel.launchPreviousMove() },
                onPlayPause      = { chessGameViewModel.onPlayPause() },
                onNext           = { chessGameViewModel.launchNextMove() },
                currentCategory  = feedback?.type,
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
                Box(modifier = Modifier.size(squareSize * 8)) {

                    // Layer 1 — squares
                    Column {
                        boardState.board?.boxes?.forEachIndexed { row, rowData ->
                            Row {
                                rowData.forEachIndexed { column, spot ->
                                    ChessSquare(
                                        isWhite       = (row + column) % 2 == 0,
                                        spot          = spot,
                                        isInCheck     = isInCheck,
                                        checkPosition = checkPosition,
                                        validMoves    = validMoves,
                                        onSelectSpot  = { chessGameViewModel.onSelectSpot(it) },
                                        onMovePieceTo = { chessGameViewModel.movePieceTo(it) },
                                        squareSize    = squareSize,
                                        lastMove      = lastMove,
                                        aiLastMove    = aiLastMove,
                                    )
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
                        feedback  = it,
                        insight   = currentInsight,
                        isLoading = explanationLoading,
                        modifier  = Modifier
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
    currentMoveIndex: Int,
    totalMoves: Int,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    currentCategory: String?,
    modifier: Modifier = Modifier,
) {
    val accent = currentCategory?.let { categoryColor(it) } ?: ColorOak

    Surface(
        modifier       = modifier,
        shape          = RoundedCornerShape(14.dp),
        color          = ColorSurface,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                text     = if (totalMoves > 0) "$currentMoveIndex / $totalMoves" else "— / —",
                style    = MaterialTheme.typography.labelMedium,
                color    = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.width(52.dp),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                ControlButton(
                    onClick = onPrevious,
                    enabled = currentMoveIndex > 0 && !isPlaying,
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
                    enabled = currentMoveIndex < totalMoves,
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
                else Color.White.copy(alpha = 0.04f)
            )
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides
                    if (enabled) accent else Color.White.copy(alpha = 0.2f)
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
    val clampedEval      = evalCp.coerceIn(-800, 800)
    val whiteFraction    = (clampedEval + 800f) / 1600f
    val animatedFraction by animateFloatAsState(
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

    Column(
        modifier            = modifier.background(ColorSurface),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Box(
            modifier         = Modifier
                .fillMaxWidth()
                .weight((1f - animatedFraction).coerceAtLeast(0.02f))
                .background(Color(0xFF2A2A2A)),
            contentAlignment = Alignment.TopCenter,
        ) {
            if (evalCp < -50) {
                Text(
                    text      = evalLabel,
                    fontSize  = 8.sp,
                    color     = Color.White.copy(alpha = 0.6f),
                    modifier  = Modifier.padding(top = 3.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }

        Box(
            modifier         = Modifier
                .fillMaxWidth()
                .weight(animatedFraction.coerceAtLeast(0.02f))
                .background(ColorWhiteSquare),
            contentAlignment = Alignment.BottomCenter,
        ) {
            if (evalCp >= -50) {
                Text(
                    text      = evalLabel,
                    fontSize  = 8.sp,
                    color     = Color(0xFF1A1A1A).copy(alpha = 0.6f),
                    modifier  = Modifier.padding(bottom = 3.dp),
                    textAlign = TextAlign.Center,
                )
            }
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
        colors    = CardDefaults.cardColors(containerColor = ColorSurface),
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
                    color = Color.White.copy(alpha = 0.4f),
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

@Composable
fun EvalGraph(
    evaluations: List<Int>,
    moveCategories: List<String>,
    currentIndex: Int,
    modifier: Modifier = Modifier,
) {
    val clampedEvals = evaluations.map { it.coerceIn(-800, 800) }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (clampedEvals.size < 2) return@Canvas

            val w     = size.width
            val h     = size.height
            val mid   = h / 2f
            val xStep = w / (clampedEvals.size - 1).toFloat()

            drawLine(
                color       = Color(0xFF444444),
                start       = Offset(0f, mid),
                end         = Offset(w, mid),
                strokeWidth = 1.dp.toPx(),
                pathEffect  = PathEffect.dashPathEffect(floatArrayOf(5f, 4f)),
            )

            val path = Path()
            clampedEvals.forEachIndexed { i, eval ->
                val x = i * xStep
                val y = mid - (eval / 800f) * mid
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
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
            drawPath(
                whiteFill,
                Brush.verticalGradient(
                    colors = listOf(ColorWhiteSquare.copy(alpha = 0.25f), Color.Transparent),
                    startY = 0f,
                    endY   = mid,
                ),
            )

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
            drawPath(
                blackFill,
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0xFF444444).copy(alpha = 0.3f)),
                    startY = mid,
                    endY   = h,
                ),
            )

            drawPath(
                path  = path,
                color = ColorWhiteSquare.copy(alpha = 0.8f),
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round),
            )

            moveCategories.forEachIndexed { i, cat ->
                if ((cat == "Blunder" || cat == "MissedMate") && i < clampedEvals.size) {
                    val x = i * xStep
                    val y = mid - (clampedEvals[i] / 800f) * mid
                    drawCircle(
                        color  = ColorBlunder,
                        radius = 3.5.dp.toPx(),
                        center = Offset(x, y),
                    )
                }
            }

            if (currentIndex > 0 && currentIndex <= clampedEvals.size) {
                val curX = (currentIndex - 1) * xStep
                drawLine(
                    color       = ColorOak.copy(alpha = 0.7f),
                    start       = Offset(curX, 0f),
                    end         = Offset(curX, h),
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
    modifier: Modifier = Modifier,
) {
    val accent = categoryColor(feedback.type)
    val icon   = categoryIcon(feedback.type)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ColorSurface)
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
                            color = Color.White.copy(alpha = 0.35f),
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
                            color = Color.White.copy(alpha = 0.35f),
                            bg    = Color.White.copy(alpha = 0.06f),
                        )
                    }
                    MetaChip(
                        text  = ins.gamePhase.name
                            .lowercase()
                            .replaceFirstChar { it.uppercase() },
                        color = Color.White.copy(alpha = 0.25f),
                        bg    = Color.White.copy(alpha = 0.04f),
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
                    color      = Color.White.copy(alpha = 0.78f),
                    style      = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp,
                )
            }

            insight?.bestContinuation
                ?.takeIf { it.isNotEmpty() && !isLoading }
                ?.let { line ->
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(
                        color     = Color.White.copy(alpha = 0.05f),
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
        colors    = CardDefaults.cardColors(containerColor = ColorSurface),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text  = "Game summary",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text  = summary.gameNarrative,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
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
                        color = Color.White.copy(alpha = 0.75f),
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
            color = Color.White.copy(alpha = 0.25f),
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
                            else Color.White.copy(alpha = 0.05f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text       = move.toReadableNotation(),
                        color      = if (index == 0) accent
                        else Color.White.copy(alpha = 0.4f),
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = if (index == 0) FontWeight.SemiBold
                        else FontWeight.Normal,
                    )
                }
                if (index < moves.lastIndex) {
                    Text(
                        text  = "→",
                        color = Color.White.copy(alpha = 0.15f),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun ShimmerExplanation() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue  = -300f,
        targetValue   = 300f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_translate",
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.04f),
            Color.White.copy(alpha = 0.10f),
            Color.White.copy(alpha = 0.04f),
        ),
        start  = Offset(translateX, 0f),
        end    = Offset(translateX + 200f, 0f),
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(11.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .height(11.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush),
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
