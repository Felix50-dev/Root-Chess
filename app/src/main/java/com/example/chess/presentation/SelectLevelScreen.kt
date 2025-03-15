package com.example.chess.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.chess.R

@Composable
fun SelectLevelScreen(
    modifier: Modifier = Modifier,
    onLevel1: () -> Unit,
    onLevel2: () -> Unit,
) {

    Scaffold(
        topBar = { TopAppBar(text = "Select Level") },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedCardButton(
                    text = "Level 1",
                    iconRes = R.drawable.pawn_white,
                    onClick = onLevel1,
                    containerColor = Color(0xFFA26232)
                )
                AnimatedCardButton(
                    text = "Level 2",
                    iconRes = R.drawable.knight_dark,
                    onClick = onLevel2,
                    containerColor = Color(0xFFA26232)
                )
            }
        }
    }
}