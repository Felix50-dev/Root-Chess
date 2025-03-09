package com.example.chess.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.chess.data.model.AILevel
import com.example.chess.data.model.GameMode
import com.example.chess.presentation.ChessGameScreen
import com.example.chess.presentation.ChessGameViewModel
import com.example.chess.presentation.HomeScreen
import com.example.chess.presentation.SelectColorScreen
import com.example.chess.presentation.SelectLevelScreen

object RootChessDestinations {
    const val HOME_ROUTE = "home"
    const val PLAY_WITH_FRIEND_ROUTE = "friend"
    const val PLAY_WITH_COMPUTER_ROUTE = "computer"
    const val LEVEL_ROUTE = "level"
    const val COLOR_ROUTE = "color"
}

@Composable
fun RootChessNavHost(
    navController: NavHostController,
    viewModel: ChessGameViewModel,
    modifier: Modifier = Modifier,
    activity: ComponentActivity,
) {
    NavHost(
        navController = navController,
        startDestination = RootChessDestinations.HOME_ROUTE,
        modifier = modifier
    ) {
        composable(
            route = RootChessDestinations.HOME_ROUTE
        ) {
            HomeScreen(
                onPlayWithFriend = {
                    viewModel.setGameMode(GameMode.FRIEND)
                    navController.navigate(RootChessDestinations.PLAY_WITH_FRIEND_ROUTE)
                },
                onPlayWithComputer = {
                    viewModel.setGameMode(GameMode.COMPUTER)
                    navController.navigate(RootChessDestinations.LEVEL_ROUTE)
                },
                onBackPressed = {
                    activity.finish()
                }
            )
        }
        composable(
            route = RootChessDestinations.PLAY_WITH_FRIEND_ROUTE
        ) {
            ChessGameScreen(
                chessGameViewModel = viewModel,
                onExitGame = { navController.navigate(RootChessDestinations.HOME_ROUTE) }
            )
        }

        composable(
            route = RootChessDestinations.LEVEL_ROUTE
        ) {
            SelectLevelScreen(
                onLevel1 = {
                    viewModel.setAILevel(AILevel.LEVEL1)
                    navController.navigate(RootChessDestinations.COLOR_ROUTE)
                }
            ) {
                viewModel.setAILevel(AILevel.LEVEL2)
                navController.navigate(RootChessDestinations.COLOR_ROUTE)
            }
        }
        composable(
            route = RootChessDestinations.COLOR_ROUTE
        ) {
            SelectColorScreen {
                viewModel.setColor(it)
                navController.navigate(RootChessDestinations.PLAY_WITH_COMPUTER_ROUTE)
            }
        }
        composable(
            route = RootChessDestinations.PLAY_WITH_COMPUTER_ROUTE
        ) {
            ChessGameScreen(
                chessGameViewModel = viewModel,
                onExitGame = { navController.navigate(RootChessDestinations.HOME_ROUTE) }
            )
        }
    }
}