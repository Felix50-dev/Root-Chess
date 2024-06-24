package com.example.chess.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onPlayWithFriend: () -> Unit,
    onPlayWithComputer: () -> Unit
) {
    Column (
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Column (
            modifier = modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Button(
                onClick = { onPlayWithFriend() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA26232))
            ) {
                Text(text = "Play with a friend")
            }
            Button(
                onClick = { onPlayWithComputer() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA26232))
            ) {
                Text(text = "Play with computer")
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onPlayWithFriend = {},
        onPlayWithComputer = {}
    )
}