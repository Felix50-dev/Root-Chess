package com.example.chess.presentation

import android.content.res.Configuration
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chess.R
import com.example.chess.data.model.BotCatalog
import com.example.chess.data.model.BotProfile
import com.example.chess.ui.theme.ChessTheme
import kotlin.random.Random

// UI-side only: Random resolves to a coin flip before the (isWhite) callback, so the
// ViewModel/navigation contract is unchanged.
private enum class ColorChoice { WHITE, RANDOM, BLACK }

// What ComputerModeScreen (binary "Level 1"/"Level 2" picker) used to be — the bot is
// now fixed by which roadmap node was tapped, so only color selection remains here.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotDetailScreen(
    bot: BotProfile,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onStartGame: (isWhite: Boolean) -> Unit,
) {
    var colorChoice by remember { mutableStateOf(ColorChoice.WHITE) }

    Scaffold(
        topBar = { TopAppBar(text = "Challenger", onBack = onBack) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = bot.name.first().toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = bot.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    text = "RATING ${bot.elo}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "“${bot.flavorText}”",
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Text(
                text = "Play as",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ColorChip(
                    label = "White",
                    selected = colorChoice == ColorChoice.WHITE,
                    onClick = { colorChoice = ColorChoice.WHITE },
                    leading = {
                        Image(
                            painter = painterResource(R.drawable.king_white),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                ColorChip(
                    label = "Random",
                    selected = colorChoice == ColorChoice.RANDOM,
                    onClick = { colorChoice = ColorChoice.RANDOM },
                    leading = {
                        Icon(
                            imageVector = Icons.Filled.Shuffle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                ColorChip(
                    label = "Black",
                    selected = colorChoice == ColorChoice.BLACK,
                    onClick = { colorChoice = ColorChoice.BLACK },
                    leading = {
                        Image(
                            painter = painterResource(R.drawable.king_dark),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val isWhite = when (colorChoice) {
                        ColorChoice.WHITE -> true
                        ColorChoice.BLACK -> false
                        ColorChoice.RANDOM -> Random.nextBoolean()
                    }
                    onStartGame(isWhite)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Challenge",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ColorChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    leading: @Composable () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = leading,
    )
}

@Preview
@Composable
private fun BotDetailScreenPreview() {
    ChessTheme {
        BotDetailScreen(bot = BotCatalog.bots[3], onStartGame = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BotDetailScreenDarkPreview() {
    ChessTheme(darkTheme = true) {
        BotDetailScreen(bot = BotCatalog.bots[3], onStartGame = {})
    }
}
