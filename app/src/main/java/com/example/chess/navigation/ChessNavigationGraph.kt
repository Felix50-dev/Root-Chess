package com.example.chess.navigation

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.chess.data.model.BotCatalog
import com.example.chess.data.model.GameMode
import com.example.chess.presentation.BoardAnalysisScreen
import com.example.chess.payments.presentation.PaywallScreen
import com.example.chess.presentation.BotDetailScreen
import com.example.chess.presentation.ChatScreen
import com.example.chess.presentation.ChatViewModel
import com.example.chess.presentation.ChessAnalysisScreen
import com.example.chess.presentation.ChessGameScreen
import com.example.chess.presentation.ChessGameViewModel
import com.example.chess.presentation.HomeScreen
import com.example.chess.presentation.PuzzleListScreen
import com.example.chess.presentation.PuzzleScreen
import com.example.chess.presentation.RoadmapScreen
import com.example.chess.presentation.RoadmapViewModel

object RootChessDestinations {
    const val HOME_ROUTE = "home"
    const val PLAY_WITH_FRIEND_ROUTE = "friend"
    const val PLAY_WITH_COMPUTER_ROUTE = "computer"
    const val ROADMAP_ROUTE = "roadmap"
    const val BOT_DETAIL_ROUTE = "bot_detail/{botId}"
    const val ANALYSIS_ROUTE = "analysis"
    const val REVIEW_GAME_ROUTE = "review"
    const val PUZZLE_LIST_ROUTE = "puzzles"
    const val PUZZLE_SOLVE_ROUTE = "puzzle/{puzzleId}"
    const val CHAT_ROUTE = "chat"
    const val PAYWALL_ROUTE = "paywall"
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
                    navController.navigate(RootChessDestinations.ROADMAP_ROUTE)
                },
                onOpenPuzzles = {
                    navController.navigate(RootChessDestinations.PUZZLE_LIST_ROUTE)
                },
                onOpenChat = {
                    navController.navigate(RootChessDestinations.CHAT_ROUTE)
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
                onExitGame = { navController.navigate(RootChessDestinations.HOME_ROUTE) },
                onAnalyzeGame = {
                    navController.navigate(RootChessDestinations.ANALYSIS_ROUTE)
                    viewModel.analyzeGame()
                }
            )
        }

        composable(
            route = RootChessDestinations.ROADMAP_ROUTE
        ) {
            val roadmapViewModel = hiltViewModel<RoadmapViewModel>()
            val roadmapState by roadmapViewModel.roadmapState.collectAsStateWithLifecycle()
            val rating by roadmapViewModel.playerRating.collectAsStateWithLifecycle()
            RoadmapScreen(
                state = roadmapState,
                rating = rating,
                onBotSelected = { botId ->
                    navController.navigate("bot_detail/$botId")
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = RootChessDestinations.BOT_DETAIL_ROUTE,
            arguments = listOf(navArgument("botId") { type = NavType.StringType })
        ) { backStackEntry ->
            val botId = backStackEntry.arguments?.getString("botId") ?: return@composable
            val bot = BotCatalog.bots.firstOrNull { it.id == botId } ?: return@composable
            BotDetailScreen(
                bot = bot,
                onBack = { navController.popBackStack() },
            ) { isWhite ->
                viewModel.setBot(bot)
                viewModel.setColor(isWhite)
                navController.navigate(RootChessDestinations.PLAY_WITH_COMPUTER_ROUTE)
            }
        }

        composable(
            route = RootChessDestinations.PLAY_WITH_COMPUTER_ROUTE
        ) {
            ChessGameScreen(
                chessGameViewModel = viewModel,
                onExitGame = { navController.navigate(RootChessDestinations.HOME_ROUTE) },
                onAnalyzeGame = {
                    navController.navigate(RootChessDestinations.ANALYSIS_ROUTE)
                    viewModel.analyzeGame()
                }
            )
        }
        composable (
            route = RootChessDestinations.ANALYSIS_ROUTE
        ) {
            ChessAnalysisScreen(
                chessGameViewModel = viewModel,
                onStartReview = {
                    navController.navigate(RootChessDestinations.REVIEW_GAME_ROUTE)
                    viewModel.initReviewMode()
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable (
            route = RootChessDestinations.REVIEW_GAME_ROUTE
        ) {
            BoardAnalysisScreen(
                chessGameViewModel = viewModel,
                onBack = { navController.popBackStack() },
                onGoPremium = { navController.navigate(RootChessDestinations.PAYWALL_ROUTE) },
            )
        }
        composable(
            route = RootChessDestinations.PUZZLE_LIST_ROUTE
        ) {
            PuzzleListScreen(
                onOpenPuzzle = { puzzleId ->
                    navController.navigate("puzzle/$puzzleId")
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = RootChessDestinations.PUZZLE_SOLVE_ROUTE,
            arguments = listOf(navArgument("puzzleId") { type = NavType.LongType })
        ) { backStackEntry ->
            val puzzleId = backStackEntry.arguments?.getLong("puzzleId") ?: return@composable
            PuzzleScreen(
                puzzleId = puzzleId,
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = RootChessDestinations.CHAT_ROUTE
        ) {
            // Premium-only: the whole route is gated so no chat UI ever renders for
            // free users, and it flips into the chat the moment a purchase completes
            // (the inline paywall's onPurchased needs no navigation for that reason).
            val chatViewModel = hiltViewModel<ChatViewModel>()
            val isPremium by chatViewModel.isPremium.collectAsStateWithLifecycle()
            if (isPremium) {
                ChatScreen(
                    onBack = { navController.popBackStack() },
                    chessGameViewModel = viewModel,
                    chatViewModel = chatViewModel,
                )
            } else {
                PaywallScreen(
                    onBack = { navController.popBackStack() },
                    onPurchased = { /* route recomposes into the chat by itself */ },
                    accent = MaterialTheme.colorScheme.primary,
                    background = MaterialTheme.colorScheme.background,
                )
            }
        }
        composable(
            route = RootChessDestinations.PAYWALL_ROUTE
        ) {
            PaywallScreen(
                onBack = { navController.popBackStack() },
                onPurchased = { navController.popBackStack() },
                accent = MaterialTheme.colorScheme.primary,
                background = MaterialTheme.colorScheme.background,
            )
        }
    }
}