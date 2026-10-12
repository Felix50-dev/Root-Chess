package com.example.chess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.chess.navigation.RootChessNavHost
import com.example.chess.presentation.ChessGameViewModel
import com.example.chess.ui.theme.ChessTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must run before super.onCreate() so it can intercept the window before the
        // system finishes creating it (androidx Core SplashScreen's documented ordering).
        // This gives well-behaved OEM launchers the branded dark/tan splash immediately;
        // some OEM ROMs (verified on this project's own test device) create the platform
        // splash window but don't honor its custom background/icon attributes, only ever
        // showing a plain placeholder — the in-app SplashContent below is the reliable
        // fallback that doesn't depend on OEM splash-screen support at all.
        val splashScreen = installSplashScreen()
        var keepSplashOnScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

        // No-arg default is SystemBarStyle.auto: bar icons follow uiMode (light icons in
        // dark theme), which the old forced-.light() styles broke.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            var showSplash by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                delay(500)
                showSplash = false
                keepSplashOnScreen = false
            }
            ChessTheme {
                if (showSplash) {
                    SplashContent()
                } else {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val viewModel: ChessGameViewModel = hiltViewModel()
                        RootChessNavHost(
                            navController = rememberNavController(),
                            viewModel = viewModel,
                            activity = this
                        )
                    }
                }
            }
        }
    }
}

// In-app fallback for the cold-start splash, shown for a fixed beat regardless of whether
// the OEM honors the platform SplashScreen theme (see the comment in onCreate above).
// Theme-aware like the native splash: cream/brown in light mode, near-black/tan in dark.
@Composable
private fun SplashContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_app_mark),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(96.dp)
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ChessTheme {
        Greeting("Android")
    }
}