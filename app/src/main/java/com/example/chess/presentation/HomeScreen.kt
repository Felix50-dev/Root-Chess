package com.example.chess.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onPlayWithFriend: () -> Unit,
    onPlayWithComputer: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(text = "Root Chess")
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(text: String) {
    androidx.compose.material3.TopAppBar(
        title = {
            Text(
                text = text,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
                },
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onPlayWithFriend = {},
        onPlayWithComputer = {}
    )
}