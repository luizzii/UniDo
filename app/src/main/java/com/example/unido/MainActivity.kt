package com.example.unido

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.unido.ui.navigation.UniDoNavHost
import com.example.unido.ui.theme.UniDoTheme
import com.example.unido.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TaskViewModel by viewModels {
        TaskViewModel.factory((application as UniDoApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep the splash visible until the tasks are loaded, and for at least MIN_SPLASH_MS.
        val startedAt = SystemClock.elapsedRealtime()
        splashScreen.setKeepOnScreenCondition {
            !viewModel.isReady.value || SystemClock.elapsedRealtime() - startedAt < MIN_SPLASH_MS
        }

        enableEdgeToEdge()

        setContent {
            UniDoTheme {
                UniDoNavHost(viewModel = viewModel)
            }
        }
    }

    private companion object {
        const val MIN_SPLASH_MS = 1_000L
    }
}
