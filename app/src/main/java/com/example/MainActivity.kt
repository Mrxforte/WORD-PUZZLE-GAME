package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.DarkThemeMode
import com.example.data.model.Screen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HowToPlayScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val viewModel: GameViewModel = viewModel()
      val uiState by viewModel.uiState.collectAsState()

      // Determine active dark theme based on user settings
      val isDark = when (uiState.settings.darkThemePreference) {
        DarkThemeMode.DARK -> true
        DarkThemeMode.LIGHT -> false
        DarkThemeMode.SYSTEM -> isSystemInDarkTheme()
      }

      // Manage lifecycle pausing of audio
      val lifecycleOwner = LocalLifecycleOwner.current
      DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
          when (event) {
            Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
              viewModel.soundSystem.pauseAll()
            }
            Lifecycle.Event.ON_RESUME -> {
              viewModel.soundSystem.resumeAll()
            }
            else -> {}
          }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
          lifecycleOwner.lifecycle.removeObserver(observer)
        }
      }

      MyApplicationTheme(darkTheme = isDark) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          when (val screen = uiState.currentScreen) {
            is Screen.Home -> {
              HomeScreen(
                stats = uiState.stats,
                dailyDateString = uiState.dailyDateString,
                onPlayLevel = { levelId -> viewModel.navigateTo(Screen.Play(levelId)) },
                onPlayDaily = { viewModel.navigateTo(Screen.Play(levelId = 1, isDaily = true)) },
                onNavigate = { target -> viewModel.navigateTo(target) }
              )
            }
            is Screen.Play -> {
              GameScreen(viewModel = viewModel)
            }
            is Screen.LevelSelect -> {
              LevelSelectScreen(
                repository = viewModel.repository,
                onSelectLevel = { levelId -> viewModel.navigateTo(Screen.Play(levelId)) },
                onBack = { viewModel.navigateTo(Screen.Home) }
              )
            }
            is Screen.Stats -> {
              StatsScreen(
                stats = uiState.stats,
                onBack = { viewModel.navigateTo(Screen.Home) }
              )
            }
            is Screen.Settings -> {
              SettingsScreen(
                settings = uiState.settings,
                onUpdateSettings = { newSettings -> viewModel.updateSettings(newSettings) },
                onResetProgress = { viewModel.resetAllProgress() },
                onBack = { viewModel.navigateTo(Screen.Home) }
              )
            }
            is Screen.HowToPlay -> {
              HowToPlayScreen(
                onBack = { viewModel.navigateTo(Screen.Home) }
              )
            }
          }
        }
      }
    }
  }
}
