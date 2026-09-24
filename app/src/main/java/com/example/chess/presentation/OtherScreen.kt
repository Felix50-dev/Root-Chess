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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.chess.R
import com.example.chess.ui.theme.ChessTheme
import kotlin.random.Random

// UI-side only: Random resolves to a coin flip before the existing (level, isWhite)
// callback, so the ViewModel/navigation contract is unchanged.
private enum class ColorChoice { WHITE, RANDOM, BLACK }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComputerModeScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onStartGame: (level: Int, isWhite: Boolean) -> Unit,
) {
    var selectedLevel by remember { mutableIntStateOf(1) }
    var colorChoice by remember { mutableStateOf(ColorChoice.WHITE) }

    Scaffold(
        topBar = { TopAppBar(text = "Play with Computer", onBack = onBack) },
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

            // Theme-invariant cream disc: the robot animation is black line art, which
            // vanishes on the dark theme without a light backing.
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(ChessTheme.colors.boardLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(R.raw.computer_bot)
                )
                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations = LottieConstants.IterateForever
                )
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(150.dp)
                )
            }

            Text(
                text = "Difficulty",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp)
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf(1 to "Level 1", 2 to "Level 2").forEachIndexed { index, (level, label) ->
                    SegmentedButton(
                        selected = selectedLevel == level,
                        onClick = { selectedLevel = level },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 2),
                    ) {
                        Text(label)
                    }
                }
            }

            Text(
                text = "Play as",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp)
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
                    onStartGame(selectedLevel, isWhite)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Start Game",
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
fun ComputerModeScreenPreview() {
    ChessTheme {
        ComputerModeScreen(onStartGame = { _, _ -> })
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ComputerModeScreenDarkPreview() {
    ChessTheme(darkTheme = true) {
        ComputerModeScreen(onStartGame = { _, _ -> })
    }
}
