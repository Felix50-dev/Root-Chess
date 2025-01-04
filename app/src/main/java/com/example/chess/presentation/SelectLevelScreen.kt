package com.example.chess.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SelectLevelScreen(
    modifier: Modifier = Modifier,
    onLevel1: () -> Unit,
    onLevel2: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(text = "Select Level")
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
                    onClick = {
                        onLevel1()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA26232))
                ) {
                    Text(text = "Level 1")
                }
                Button(
                    onClick = {
                        onLevel2()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA26232))
                ) {
                    Text(text = "Level 2")
                }
            }
        }
    }
}

@Preview
@Composable
fun SelectLevelScreenPreview() {
    HomeScreen(
        onPlayWithFriend = {},
        onPlayWithComputer = {}
    )
}