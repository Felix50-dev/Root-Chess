package com.example.chess.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Color tokens ─────────────────────────────────────────────────────────────
private val ChessOak        = Color(0xFFA26232)
private val ChessOakLight   = Color(0xFFD4A96A)
private val AccentBlue      = Color(0xFF378ADD)
private val AccentPurple    = Color(0xFF7F77DD)
private val CategoryBrilliant = Color(0xFF1D9E75)
private val CategoryBest    = Color(0xFF378ADD)
private val CategoryGood    = Color(0xFF639922)
private val CategoryInaccuracy = Color(0xFFBA7517)
private val CategoryMistake = Color(0xFFD85A30)
private val CategoryBlunder = Color(0xFFE24B4A)
private val CategoryMiss    = Color(0xFFD4537E)

private data class MoveCategory(
    val label: String,
    val color: Color,
    val whiteCount: Int,
    val blackCount: Int,
)

// ── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun ChessAnalysisScreen(
    chessGameViewModel: ChessGameViewModel,
    onStartReview: () -> Unit,
) {
    val analysisResult by chessGameViewModel.analysisResult.collectAsState()
    val isAnalyzing    by chessGameViewModel.isAnalyzing.collectAsState()
    val analysisProgress by chessGameViewModel.analysisProgress.collectAsState()

    val isReady = analysisResult != null && !isAnalyzing

    // Build category rows from real data when available
    val categories = remember(analysisResult) {
        val w = analysisResult?.whiteCategoryCounts ?: emptyMap()
        val b = analysisResult?.blackCategoryCounts ?: emptyMap()
        listOf(
            MoveCategory("Brilliant",   CategoryBrilliant,  w["Brilliant"]   ?: 0, b["Brilliant"]   ?: 0),
            MoveCategory("Best",        CategoryBest,       w["Best"]        ?: 0, b["Best"]        ?: 0),
            MoveCategory("Great",       CategoryGood,       w["Great"]       ?: 0, b["Great"]       ?: 0),
            MoveCategory("Inaccuracy",  CategoryInaccuracy, w["Inaccurate"]  ?: 0, b["Inaccurate"]  ?: 0),
            MoveCategory("Mistake",     CategoryMistake,    w["Mistake"]     ?: 0, b["Mistake"]     ?: 0),
            MoveCategory("Blunder",     CategoryBlunder,    w["Blunder"]     ?: 0, b["Blunder"]     ?: 0),
            MoveCategory("Miss",        CategoryMiss,       w["Miss"]        ?: 0, b["Miss"]        ?: 0),
        )
    }

    Scaffold(
        topBar = { TopAppBar(text = "Game analysis") }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // ── Players row ──────────────────────────────────────────────
                PlayersCard()

                // ── Accuracy rings ───────────────────────────────────────────
                AccuracyRow(
                    isAnalyzing     = isAnalyzing,
                    whiteAccuracy   = analysisResult?.whiteAccuracy?.toFloat() ?: 0f,
                    blackAccuracy   = analysisResult?.blackAccuracy?.toFloat() ?: 0f,
                    isReady         = isReady,
                )

                // ── Move breakdown card ──────────────────────────────────────
                MoveBreakdownCard(
                    isAnalyzing = isAnalyzing,
                    categories  = categories,
                    isReady     = isReady,
                )

                Spacer(modifier = Modifier.weight(1f))

                // ── CTA button ───────────────────────────────────────────────
                AnalysisButton(
                    isAnalyzing  = isAnalyzing,
                    isReady      = isReady,
                    onClick      = onStartReview,
                    realProgress = analysisProgress,
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ── Players card ─────────────────────────────────────────────────────────────

@Composable
private fun PlayersCard() {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            PlayerChip(initials = "WP", name = "White", elo = "1240", avatarColor = AccentBlue)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text  = "vs",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            PlayerChip(
                initials     = "BP",
                name         = "Black",
                elo          = "1180",
                avatarColor  = AccentPurple,
                reverseOrder = true,
            )
        }
    }
}

@Composable
private fun PlayerChip(
    initials: String,
    name: String,
    elo: String,
    avatarColor: Color,
    reverseOrder: Boolean = false,
) {
    val items: @Composable () -> Unit = {
        Box(
            modifier        = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(avatarColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text  = initials,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = avatarColor,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = if (reverseOrder) Alignment.End else Alignment.Start) {
            Text(
                text  = name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text  = "$elo elo",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (reverseOrder) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text  = name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text  = "$elo elo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier         = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(avatarColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text  = initials,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = avatarColor,
                )
            }
        }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) { items() }
    }
}

// ── Accuracy row ─────────────────────────────────────────────────────────────

@Composable
private fun AccuracyRow(
    isAnalyzing: Boolean,
    whiteAccuracy: Float,
    blackAccuracy: Float,
    isReady: Boolean,
) {
    Row(
        modifier            = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AccuracyCard(
            modifier    = Modifier.weight(1f),
            label       = "White",
            accuracy    = whiteAccuracy,
            ringColor   = AccentBlue,
            isAnalyzing = isAnalyzing,
            isReady     = isReady,
        )
        AccuracyCard(
            modifier    = Modifier.weight(1f),
            label       = "Black",
            accuracy    = blackAccuracy,
            ringColor   = AccentPurple,
            isAnalyzing = isAnalyzing,
            isReady     = isReady,
        )
    }
}

@Composable
private fun AccuracyCard(
    modifier: Modifier,
    label: String,
    accuracy: Float,
    ringColor: Color,
    isAnalyzing: Boolean,
    isReady: Boolean,
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.spacedBy(12.dp),
        ) {
            if (isAnalyzing) {
                ShimmerBox(width = 80.dp, height = 80.dp, shape = CircleShape)
                ShimmerBox(width = 60.dp, height = 14.dp)
            } else {
                AccuracyRing(
                    accuracy  = if (isReady) accuracy else 0f,
                    color     = ringColor,
                    isReady   = isReady,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ringColor),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text  = label,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun AccuracyRing(
    accuracy: Float,
    color: Color,
    isReady: Boolean,
    ringSize: Dp = 80.dp,
) {
    val animatedSweep = remember { Animatable(0f) }
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(isReady, accuracy) {
        if (isReady) {
            launch {
                animatedSweep.animateTo(
                    targetValue   = accuracy / 100f * 360f,
                    animationSpec = tween(1200, easing = FastOutSlowInEasing),
                )
            }
            launch {
                animatedValue.animateTo(
                    targetValue   = accuracy,
                    animationSpec = tween(1200, easing = FastOutSlowInEasing),
                )
            }
        }
    }

    val trackColor = MaterialTheme.colorScheme.surface

    Box(
        modifier         = Modifier
            .size(ringSize)
            .drawBehind {
                val stroke     = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                val inset      = 4.dp.toPx()
                val arcSize    = Size(size.width - inset * 2, size.height - inset * 2)
                val topLeft    = Offset(inset, inset)

                // Track
                drawArc(
                    color      = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter  = false,
                    topLeft    = topLeft,
                    size       = arcSize,
                    style      = stroke,
                )
                // Progress
                drawArc(
                    color      = color,
                    startAngle = -90f,
                    sweepAngle = animatedSweep.value,
                    useCenter  = false,
                    topLeft    = topLeft,
                    size       = arcSize,
                    style      = stroke,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text  = "${animatedValue.value.toInt()}%",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text  = "accuracy",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Move breakdown card ───────────────────────────────────────────────────────

@Composable
private fun MoveBreakdownCard(
    isAnalyzing: Boolean,
    categories: List<MoveCategory>,
    isReady: Boolean,
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text  = "Move breakdown",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 0.08.sp,
                    fontWeight    = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isAnalyzing) {
                repeat(5) { i ->
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        height   = 16.dp,
                    )
                }
            } else {
                // Column headers
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text      = "White",
                        style     = MaterialTheme.typography.labelSmall,
                        color     = AccentBlue,
                        modifier  = Modifier.width(48.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                    Text(
                        text      = "Black",
                        style     = MaterialTheme.typography.labelSmall,
                        color     = AccentPurple,
                        modifier  = Modifier.width(48.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                categories.forEachIndexed { index, category ->
                    var visible by remember { mutableStateOf(false) }

                    LaunchedEffect(isReady) {
                        if (isReady) {
                            delay(index * 60L)
                            visible = true
                        } else {
                            visible = false
                        }
                    }

                    AnimatedVisibility(
                        visible = visible,
                        enter   = fadeIn(tween(200)) + slideInVertically(
                            animationSpec = tween(200),
                            initialOffsetY = { it / 2 },
                        ),
                    ) {
                        Column {
                            MoveCategoryRow(category = category)
                            if (index < categories.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoveCategoryRow(category: MoveCategory) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(category.color),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text     = category.label,
            style    = MaterialTheme.typography.bodyMedium,
            color    = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        CountChip(count = category.whiteCount, color = AccentBlue)
        Spacer(modifier = Modifier.width(12.dp))
        CountChip(count = category.blackCount, color = AccentPurple)
    }
}

@Composable
private fun CountChip(count: Int, color: Color) {
    Box(
        modifier         = Modifier
            .width(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (count > 0) color.copy(alpha = 0.12f) else Color.Transparent)
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text  = count.toString(),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = if (count > 0) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        )
    }
}

// ── CTA button ────────────────────────────────────────────────────────────────

@Composable
private fun AnalysisButton(
    isAnalyzing: Boolean,
    isReady: Boolean,
    realProgress: Float = 0f,
    onClick: () -> Unit,
) {
    val fillProgress = remember { Animatable(0f) }

    LaunchedEffect(isAnalyzing) {
        if (isAnalyzing) {
            fillProgress.animateTo(
                targetValue   = realProgress * 0.95f,  // leave 5% for completion snap
                animationSpec = tween(300)
            )
        }
    }

    LaunchedEffect(isReady) {
        if (isReady) {
            fillProgress.animateTo(
                targetValue   = 1f,
                animationSpec = tween(durationMillis = 300),
            )
        }
    }

    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ChessOak.copy(alpha = 0.25f))
            .then(
                if (isReady) Modifier.clickable { onClick() } else Modifier
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        // Fill overlay
        Box(
            modifier = Modifier
                .fillMaxWidth(fillProgress.value)
                .height(52.dp)
                .background(ChessOak),
        )

        // Label
        Row(
            modifier          = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(
                    modifier  = Modifier.size(16.dp),
                    color     = Color.White,
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text  = "Analyzing game…",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                )
            } else {
                Text(
                    text  = if (isReady) "Start review →" else "Waiting for analysis",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                )
            }
        }
    }
}

// ── Shimmer ───────────────────────────────────────────────────────────────────

@Composable
private fun ShimmerBox(
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(6.dp),
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue   = -300f,
        targetValue    = 300f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label          = "shimmer_x",
    )

    val shimmerBrush = Brush.linearGradient(
        colors     = listOf(
            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        ),
        start      = Offset(translateX, 0f),
        end        = Offset(translateX + 200f, 0f),
    )

    val sizeModifier = if (width != null) {
        modifier.size(width, height)
    } else {
        modifier.height(height)
    }

    Box(
        modifier = sizeModifier
            .clip(shape)
            .background(shimmerBrush),
    )
}
