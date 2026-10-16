package com.example.data.model

import androidx.compose.ui.geometry.Offset

/**
 * Represents a single theme word in the puzzle.
 * @param word The uppercase word string.
 * @param isSpecial True if this is the Spangram / Golden Theme Word.
 * @param path The ordered list of (row, col) coordinates forming the word.
 */
data class ThemeWord(
  val word: String,
  val isSpecial: Boolean = false,
  val path: List<Pair<Int, Int>>,
)

/**
 * Represents a complete puzzle level.
 */
data class LevelData(
  val id: Int,
  val title: String,
  val themeClue: String,
  val rows: Int,
  val cols: Int,
  val grid: List<String>, // List of rows, e.g. ["COFFEE", "TOASTB", ...]
  val themeWords: List<ThemeWord>,
  val bonusWords: Set<String> = emptySet(),
) {
  val totalLetters: Int get() = rows * cols
  val spangram: ThemeWord? get() = themeWords.firstOrNull { it.isSpecial }
}

/**
 * Saved progress for a level.
 */
data class LevelProgress(
  val levelId: Int,
  val completed: Boolean = false,
  val stars: Int = 0,
  val bestTimeSeconds: Int = 0,
  val foundWords: Set<String> = emptySet(),
)

/**
 * Global game statistics.
 */
data class GameStats(
  val gamesPlayed: Int = 0,
  val gamesWon: Int = 0,
  val currentStreak: Int = 0,
  val maxStreak: Int = 0,
  val totalWordsFound: Int = 0,
  val specialWordsFound: Int = 0,
  val hintsUsed: Int = 0,
  val bestTimeSeconds: Int = 0,
)

/**
 * Audio and app settings.
 */
data class GameSettings(
  val soundEnabled: Boolean = true,
  val musicEnabled: Boolean = true,
  val sfxVolume: Float = 0.8f,
  val musicVolume: Float = 0.4f,
  val hapticsEnabled: Boolean = true,
  val darkThemePreference: DarkThemeMode = DarkThemeMode.SYSTEM,
  val reducedMotion: Boolean = false,
)

enum class DarkThemeMode {
  LIGHT,
  DARK,
  SYSTEM,
}

/**
 * Active drag and selection state on the board.
 */
data class BoardSelectionState(
  val selectedCells: List<Pair<Int, Int>> = emptyList(),
  val currentWord: String = "",
  val isDragging: Boolean = false,
  val touchPoint: Offset? = null,
  val isInvalidFlash: Boolean = false,
  val lastAddedCell: Pair<Int, Int>? = null,
)

/**
 * High-level screens for navigation.
 */
sealed class Screen {
  object Home : Screen()
  data class Play(val levelId: Int, val isDaily: Boolean = false) : Screen()
  object LevelSelect : Screen()
  object Stats : Screen()
  object Settings : Screen()
  object HowToPlay : Screen()
}
