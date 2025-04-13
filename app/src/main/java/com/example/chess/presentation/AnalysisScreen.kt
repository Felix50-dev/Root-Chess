package com.example.chess.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.chess.R
import java.util.Locale

@Composable
fun ChessAnalysisScreen(
    chessGameViewModel: ChessGameViewModel,
    onStartReview: () -> Unit
) {
    val analysisResult by chessGameViewModel.analysisResult.collectAsState()

    Scaffold(
        topBar = { TopAppBar(text = "Chess Analysis") }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Players Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlayerInfo(
                        name = "whitePlayer",
                        avatarRes = R.drawable.ic_player, // Replace with your drawable
                        isWhite = true
                    )
                    PlayerInfo(
                        name = "blackPlayer",
                        avatarRes = R.drawable.ic_player, // Replace with your drawable
                        isWhite = false
                    )
                }

                // Analysis Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Labels Column
                        Column {
                            AnalysisLabel("Accuracy")
                            AnalysisLabel("Brilliant")
                            AnalysisLabel("Great")
                            AnalysisLabel("Best")
                            AnalysisLabel("Mistake")
                            AnalysisLabel("Miss")
                        }

                        // White Player Values
                        AnalysisColumn(
                            accuracy = (analysisResult?.whiteAccuracy?.let {
                                String.format(
                                    Locale.US,
                                    it.toString()
                                )
                            } ?: "66.2"),
                            counts = analysisResult?.whiteCategoryCounts ?: mapOf(
                                "Brilliant" to 1,
                                "Great" to 1,
                                "Best" to 7,
                                "Mistake" to 0,
                                "Blunder" to 1
                            )
                        )

                        // Black Player Values
                        AnalysisColumn(
                            accuracy = analysisResult?.blackAccuracy?.let {
                                String.format(
                                    Locale.US,
                                    it.toString()
                                )
                            } ?: "55.8",
                            counts = analysisResult?.blackCategoryCounts ?: mapOf(
                                "Brilliant" to 0,
                                "Great" to 0,
                                "Best" to 3,
                                "Mistake" to 1,
                                "Blunder" to 3
                            )
                        )
                    }
                }

                // Start Review Button
                Button(
                    onClick = { onStartReview() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFA26232),
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "START REVIEW",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f)) // Push content up, leaving space at bottom
            }
        }
    }
}

@Composable
fun PlayerInfo(name: String, avatarRes: Int, isWhite: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isWhite) Arrangement.Start else Arrangement.End
    ) {
        if (isWhite) {
            Image(
                painter = painterResource(id = avatarRes),
                contentDescription = "$name Avatar",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (!isWhite) {
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(id = avatarRes),
                contentDescription = "$name Avatar",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

@Composable
fun AnalysisLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun AnalysisColumn(accuracy: String, counts: Map<String, Int>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = accuracy,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        AnalysisRow(
            count = counts["Brilliant"] ?: 0,
            iconRes = R.drawable.brilliant_move,
            tint = MaterialTheme.colorScheme.secondary
        )
        AnalysisRow(
            count = counts["Great"] ?: 0,
            iconRes = R.drawable.best_move,
            tint = MaterialTheme.colorScheme.secondary
        )
        AnalysisRow(
            count = counts["Best"] ?: 0,
            iconRes = R.drawable.best_move,
            tint = MaterialTheme.colorScheme.secondary
        )
        AnalysisRow(
            count = counts["Mistake"] ?: 0,
            iconRes = R.drawable.mistake_move,
            tint = Color(0xFFFFA500)
        )
        AnalysisRow(
            count = counts["Blunder"] ?: 0, // Map "Miss" to "Blunder"
            iconRes = R.drawable.blunder_move,
            tint = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun AnalysisRow(count: Int, iconRes: Int, tint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}