package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.Screen
import com.example.particles.FallingStarCanvas
import com.example.ui.components.LetterGrid
import com.example.ui.components.PauseDialog
import com.example.ui.components.ThemeClueHeader
import com.example.ui.components.VictoryDialog
import com.example.ui.components.WordPreviewBar
import com.example.viewmodel.GameViewModel

@Composable
fun GameScreen(
  viewModel: GameViewModel,
  modifier: Modifier = Modifier,
) {
  val uiState by viewModel.uiState.collectAsState()

  // Handle hardware / gesture back button
  BackHandler {
    if (uiState.isVictory) {
      viewModel.navigateTo(Screen.LevelSelect)
    } else if (!uiState.isPaused) {
      viewModel.togglePause()
    } else {
      viewModel.navigateTo(Screen.Home)
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .statusBarsPadding()
      .navigationBarsPadding()
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Theme Clue & Controls Header
      ThemeClueHeader(
        level = uiState.level,
        foundCount = uiState.foundThemeWords.size,
        totalWords = uiState.level.themeWords.size,
        hintsAvailable = uiState.hintsAvailable,
        bonusCount = uiState.bonusWordCount,
        hasSpangramFound = uiState.level.spangram?.let { uiState.foundThemeWords.contains(it.word) } ?: false,
        onBackClick = { viewModel.togglePause() },
        onHintClick = { viewModel.useHint() },
        onPauseClick = { viewModel.togglePause() }
      )

      // 2. Dragged Word Preview Bar
      WordPreviewBar(
        currentWord = uiState.selectionState.currentWord,
        isDragging = uiState.selectionState.isDragging,
        statusMessage = uiState.statusMessage,
        modifier = Modifier.fillMaxWidth()
      )

      // 3. Pixel-perfect Letter Grid
      LetterGrid(
        level = uiState.level,
        selectionState = uiState.selectionState,
        foundThemeWords = uiState.foundThemeWords,
        hintedWord = uiState.hintedWord,
        onCellTouchStart = { r, c, pos -> viewModel.onCellTouchStart(r, c, pos) },
        onCellTouchMove = { r, c, pos -> viewModel.onCellTouchMove(r, c, pos) },
        onDragPointUpdate = { pos -> viewModel.onDragPointUpdate(pos) },
        onCellTouchEnd = { centers -> viewModel.onCellTouchEnd(centers) },
        modifier = Modifier.weight(1f)
      )

      Spacer(modifier = Modifier.height(12.dp))
    }

    // 4. Particle Celebration Overlay
    FallingStarCanvas(
      controller = viewModel.particleController,
      modifier = Modifier.fillMaxSize()
    )

    // 5. Victory Dialog
    if (uiState.isVictory && uiState.victoryStep >= 3) {
      VictoryDialog(
        level = uiState.level,
        elapsedSeconds = uiState.elapsedSeconds,
        stars = uiState.starsEarned,
        victoryStep = uiState.victoryStep,
        onNextLevel = { viewModel.nextLevel() },
        onReplay = { viewModel.restartLevel() },
        onLevelSelect = { viewModel.navigateTo(Screen.LevelSelect) }
      )
    }

    // 6. Pause Dialog
    if (uiState.isPaused) {
      PauseDialog(
        levelTitle = "Level ${uiState.level.id}: ${uiState.level.title}",
        settings = uiState.settings,
        onResume = { viewModel.togglePause() },
        onRestart = {
          viewModel.togglePause()
          viewModel.restartLevel()
        },
        onExit = {
          viewModel.togglePause()
          viewModel.navigateTo(Screen.Home)
        },
        onToggleSound = {
          viewModel.updateSettings(uiState.settings.copy(soundEnabled = !uiState.settings.soundEnabled))
        },
        onToggleMusic = {
          viewModel.updateSettings(uiState.settings.copy(musicEnabled = !uiState.settings.musicEnabled))
        }
      )
    }
  }
}
