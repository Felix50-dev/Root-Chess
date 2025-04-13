package com.example.chess.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.chess.R

@Composable
fun ComputerModeScreen(
    modifier: Modifier = Modifier,
    onStartGame: (level: Int, isWhite: Boolean) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(text = "Play with Computer") },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            var selectedLevel by remember { mutableStateOf<Int?>(null) }
            val levels = listOf("Level 1" to 1, "Level 2" to 2)

            // Lottie Animation at the Top
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.computer_bot))
            val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = LottieConstants.IterateForever // Loop indefinitely
            )

            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .size(250.dp) // Adjust size as needed
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp) // Space below TopAppBar
            )

            // Level Selection
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Select Difficulty",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(levels) { (name, level) ->
                        DifficultyCard(
                            levelName = name,
                            isSelected = selectedLevel == level,
                            iconRes = if (level == 1) R.drawable.knight_dark else R.drawable.bishop_white,
                            onClick = { selectedLevel = level }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            AnimatedVisibility(
                visible = selectedLevel != null,
                enter = fadeIn(animationSpec = tween(1000, easing = FastOutSlowInEasing)) +
                        scaleIn(
                            animationSpec = tween(500, easing = FastOutSlowInEasing),
                            initialScale = 0.0f
                        ) ,
                exit = fadeOut(animationSpec = tween(1000, easing = FastOutSlowInEasing)) +
                        scaleOut(
                            animationSpec = tween(500, easing = FastOutSlowInEasing),
                            targetScale = 0.0f
                        )
            ) {
                ColorPickerPanel(
                    onColorSelected = { isWhite ->
                        selectedLevel?.let { level ->
                            onStartGame(level, isWhite)
                        }
                    },
                    onDismiss = { selectedLevel = null }
                )
            }
        }
    }
}

@Composable
fun DifficultyCard(
    levelName: String,
    isSelected: Boolean,
    iconRes: Int,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.05f else 1f,
        animationSpec = tween(durationMillis = 200)
    )
    val elevation by animateDpAsState(
        targetValue = if (isHovered) 12.dp else 6.dp,
        animationSpec = tween(durationMillis = 200)
    )

    Card(
        modifier = Modifier
            .width(250.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { isHovered = true },
                    onTap = { onClick(); isHovered = false }
                )
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFA26232).copy(0.9f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = levelName,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = Color.White
            )
            Image(
                painter = painterResource(iconRes),
                contentDescription = "$levelName Icon",
                modifier = Modifier
                    .size(24.dp)
                    .alpha(if (isHovered) 1f else 0.7f)
            )
        }
    }
}

@Composable
fun ColorPickerPanel(
    onColorSelected: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .width(350.dp)
                .height(350.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFA26232).copy(0.95f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Choose Your Color",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    ColorOption("White", R.drawable.king_white) { onColorSelected(true) }
                    Spacer(modifier = Modifier.weight(1f))
                    ColorOption("Black", R.drawable.king_dark) { onColorSelected(false) }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ColorOption(text: String, iconRes: Int, onClick: () -> Unit) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isHovered) 1.1f else 1f, animationSpec = tween(200))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { isHovered = true },
                    onTap = { onClick(); isHovered = false }
                )
            }
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = text,
            modifier = Modifier.size(100.dp)
        )
        Text(text, color = Color.Black, style = MaterialTheme.typography.bodyMedium)
    }
}