package com.example.chess.presentation

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.chess.data.model.BotCatalog
import com.example.chess.data.model.RoadmapWorld
import com.example.chess.domain.model.NodeState
import com.example.chess.domain.model.RoadmapNodeUiState
import com.example.chess.ui.theme.ChessTheme

private val REGULAR_NODE_SIZE = 56.dp
private val BOSS_NODE_SIZE = 72.dp
private val CONNECTOR_HEIGHT = 36.dp
private val ROAD_STROKE_WIDTH = 10.dp
// Fixed so a node's on-screen center is always exactly `fraction * rowWidth` — the same
// fraction drawRoad() uses — regardless of whether its name/rating text is wider than its
// circle. Generous enough for "The Grandmaster" to stay on one line.
private val NODE_SLOT_WIDTH = 140.dp
private const val LEFT_FRACTION = 0.22f
private const val RIGHT_FRACTION = 0.78f

private sealed interface RoadmapListItem {
    data class Header(val world: RoadmapWorld) : RoadmapListItem
    data class Node(val node: RoadmapNodeUiState, val index: Int, val isLast: Boolean) : RoadmapListItem
}

// Flattens the flat catalog-order list into header+node rows, one header per world the
// first time it's seen — relies on BotCatalog.bots already being grouped by world.
private fun buildListItems(nodes: List<RoadmapNodeUiState>): List<RoadmapListItem> {
    val items = mutableListOf<RoadmapListItem>()
    var lastWorld: RoadmapWorld? = null
    nodes.forEachIndexed { index, node ->
        if (node.bot.world != lastWorld) {
            items += RoadmapListItem.Header(node.bot.world)
            lastWorld = node.bot.world
        }
        items += RoadmapListItem.Node(node, index, isLast = index == nodes.lastIndex)
    }
    return items
}

@Composable
fun RoadmapScreen(
    state: List<RoadmapNodeUiState>,
    rating: Int,
    onBotSelected: (String) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler { onBack() }

    Scaffold(
        topBar = { TopAppBar(text = "Your Journey", onBack = onBack) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            RatingHeader(rating = rating)
            val items = buildListItems(state)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            ) {
                items(items) { item ->
                    when (item) {
                        is RoadmapListItem.Header -> WorldHeaderRow(item.world)
                        is RoadmapListItem.Node -> RoadmapNodeRow(
                            node = item.node,
                            index = item.index,
                            isLast = item.isLast,
                            onClick = { onBotSelected(item.node.bot.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingHeader(rating: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "YOUR RATING",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = rating.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun WorldHeaderRow(world: RoadmapWorld) {
    Column(modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = world.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Text(
                    text = world.eloRangeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun RoadmapNodeRow(
    node: RoadmapNodeUiState,
    index: Int,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    val fraction = if (index % 2 == 0) LEFT_FRACTION else RIGHT_FRACTION
    val nextFraction = if ((index + 1) % 2 == 0) LEFT_FRACTION else RIGHT_FRACTION
    val roadColor = ChessTheme.colors.boardDark

    // BoxWithConstraints (not Alignment.CenterStart/CenterEnd) so the node's circle lands
    // at the exact same `fraction * rowWidth` the road below is drawn to — CenterStart/
    // CenterEnd place a child flush against the row's edge, not at a fractional position,
    // which is what caused the road to visibly miss the nodes.
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        NodeBubble(
            node = node,
            onClick = onClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = maxWidth * fraction - NODE_SLOT_WIDTH / 2),
        )
    }

    if (!isLast) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(CONNECTOR_HEIGHT)
        ) {
            drawRoad(fraction, nextFraction, roadColor)
        }
    }
}

// A solid winding "road" between consecutive nodes — King/Candy-Crush style — in the
// chessboard's own wood-brown tone, instead of a thin dashed connector. Both endpoints
// are known from index parity alone, no cross-item position measurement needed, so this
// stays Lazy-recycling-friendly.
private fun DrawScope.drawRoad(startFraction: Float, endFraction: Float, color: Color) {
    val start = Offset(size.width * startFraction, 0f)
    val end = Offset(size.width * endFraction, size.height)
    val control = Offset((start.x + end.x) / 2f, size.height / 2f)
    val path = Path().apply {
        moveTo(start.x, start.y)
        quadraticTo(control.x, control.y, end.x, end.y)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = ROAD_STROKE_WIDTH.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
    )
}

@Composable
private fun NodeBubble(node: RoadmapNodeUiState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bot = node.bot
    val size: Dp = if (bot.isBoss) BOSS_NODE_SIZE else REGULAR_NODE_SIZE
    // Even though it's locked like any other future bot, the final Grandmaster node
    // hints at itself (name + tertiary-tinted ring) so the distant goal stays visible
    // from the very start of the journey.
    val isGrandmasterGoal = node.state is NodeState.Locked && bot.world == RoadmapWorld.GRANDMASTER

    Column(
        modifier = modifier.width(NODE_SLOT_WIDTH),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                enabled = node.state !is NodeState.Locked,
                shape = CircleShape,
                tonalElevation = 1.dp,
                shadowElevation = 3.dp,
                color = when (node.state) {
                    is NodeState.Locked -> MaterialTheme.colorScheme.surfaceVariant
                    is NodeState.Current -> MaterialTheme.colorScheme.primaryContainer
                    is NodeState.Completed -> MaterialTheme.colorScheme.tertiaryContainer
                },
                border = when {
                    node.state is NodeState.Current -> BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
                    // Locked takes precedence over isBoss — an ordinary locked boss (e.g.
                    // Oli, Soren) stays uniformly muted; only the Grandmaster goal hint
                    // gets a tertiary ring while still locked.
                    node.state is NodeState.Locked -> if (isGrandmasterGoal)
                        BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary)
                    else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    bot.isBoss -> BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary)
                    else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                },
                onClick = onClick,
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    when (node.state) {
                        is NodeState.Locked -> Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Locked",
                            tint = if (isGrandmasterGoal) MaterialTheme.colorScheme.tertiary
                                   else MaterialTheme.colorScheme.outline,
                        )
                        is NodeState.Current -> Text(
                            text = bot.name.first().toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        is NodeState.Completed -> Text(
                            text = bot.name.first().toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                }
            }
            if (node.state is NodeState.Completed) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.BottomEnd),
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        when (val state = node.state) {
            is NodeState.Current, is NodeState.Completed -> {
                Text(
                    text = bot.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = bot.elo.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state is NodeState.Completed) {
                    StarRow(stars = state.stars)
                }
            }
            is NodeState.Locked -> if (isGrandmasterGoal) {
                Text(
                    text = bot.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = bot.world.eloRangeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@Composable
private fun StarRow(stars: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        repeat(3) { i ->
            Icon(
                imageVector = if (i < stars) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

private fun previewState(): List<RoadmapNodeUiState> = BotCatalog.bots.mapIndexed { index, bot ->
    val state = when {
        index < 3 -> NodeState.Completed(stars = (index % 3) + 1)
        index == 3 -> NodeState.Current
        else -> NodeState.Locked
    }
    RoadmapNodeUiState(bot, state)
}

@Preview
@Composable
private fun RoadmapScreenPreview() {
    ChessTheme {
        RoadmapScreen(state = previewState(), rating = 1240, onBotSelected = {}, onBack = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RoadmapScreenDarkPreview() {
    ChessTheme(darkTheme = true) {
        RoadmapScreen(state = previewState(), rating = 1240, onBotSelected = {}, onBack = {})
    }
}
