package com.example.chess.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.chess.R


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onPlayWithFriend: () -> Unit,
    onPlayWithComputer: () -> Unit,
    onBackPressed: () -> Unit
) {
    BackHandler { onBackPressed() }

    Scaffold(
        topBar = { TopAppBar(text = "Root Chess") },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.computer_knight))
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

            // Button Cards
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedCardButton(
                    text = "Play with a Friend",
                    iconRes = R.drawable.knight_white,
                    onClick = onPlayWithFriend,
                    containerColor = Color(0xFFA26232)
                )
                AnimatedCardButton(
                    text = "Play with Computer",
                    iconRes = R.drawable.rook_dark,
                    onClick = onPlayWithComputer,
                    containerColor = Color(0xFFA26232)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(text: String) {
    androidx.compose.material3.TopAppBar(
        title = {
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth()
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RectangleShape)
    )
}

@Composable
fun AnimatedCardButton(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    containerColor: Color
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
        colors = CardDefaults.cardColors(containerColor = containerColor.copy(alpha = 0.9f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = Color.White
            )
            Image(
                painter = painterResource(iconRes),
                contentDescription = "$text Icon",
                modifier = Modifier
                    .size(24.dp)
                    .alpha(if (isHovered) 1f else 0.7f)
            )
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onPlayWithFriend = {},
        onPlayWithComputer = {},
        onBackPressed = {}
    )
}