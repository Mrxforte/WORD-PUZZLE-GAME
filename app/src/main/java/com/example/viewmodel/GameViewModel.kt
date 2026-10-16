package com.example.viewmodel

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundSystem
import com.example.data.LevelsRepository
import com.example.data.model.BoardSelectionState
import com.example.data.model.GameSettings
import com.example.data.model.GameStats
import com.example.data.model.LevelData
import com.example.data.model.LevelProgress
import com.example.data.model.Screen
import com.example.data.model.ThemeWord
import com.example.particles.ParticleController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

data class GameUiState(
  val currentScreen: Screen = Screen.Home,
  val level: LevelData,
  val foundThemeWords: Set<String> = emptySet(),
  val foundBonusWords: Set<String> = emptySet(),
  val bonusWordCount: Int = 0, // 0..3 towards next hint
  val hintsAvailable: Int = 1,
  val hintedWord: ThemeWord? = null,
  val selectionState: BoardSelectionState = BoardSelectionState(),
  val isPaused: Boolean = false,
  val isVictory: Boolean = false,
  val victoryStep: Int = 0,
  val elapsedSeconds: Int = 0,
  val starsEarned: Int = 3,
  val stats: GameStats = GameStats(),
  val settings: GameSettings = GameSettings(),
  val isDaily: Boolean = false,
  val dailyDateString: String = "",
  val statusMessage: String? = null,
  val lastFoundWord: ThemeWord? = null,
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

  val repository = LevelsRepository(application)
  val soundSystem = SoundSystem(application)
  val particleController = ParticleController()

  private val _uiState = MutableStateFlow(
    GameUiState(
      level = repository.getLevelById(1),
      stats = repository.getStats(),
      settings = repository.getSettings()
    )
  )
  val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

  private var timerJob: Job? = null

  init {
    soundSystem.updateSettings(repository.getSettings())
    val (dailyLevel, dateStr) = repository.getDailyPuzzle()
    _uiState.update {
      it.copy(dailyDateString = dateStr)
    }
  }

  fun navigateTo(screen: Screen) {
    soundSystem.playButtonTap()
    when (screen) {
      is Screen.Play -> {
        startLevel(screen.levelId, screen.isDaily)
      }
      is Screen.Home -> {
        stopTimer()
        _uiState.update { it.copy(currentScreen = screen, isPaused = false) }
      }
      else -> {
        _uiState.update {
          it.copy(
            currentScreen = screen,
            stats = repository.getStats(),
            settings = repository.getSettings()
          )
        }
      }
    }
  }

  fun startLevel(levelId: Int, isDaily: Boolean = false) {
    stopTimer()
    particleController.clear()

    val levelData = if (isDaily) {
      repository.getDailyPuzzle().first
    } else {
      repository.getLevelById(levelId)
    }

    _uiState.update {
      it.copy(
        currentScreen = Screen.Play(levelId, isDaily),
        level = levelData,
        foundThemeWords = emptySet(),
        foundBonusWords = emptySet(),
        bonusWordCount = 0,
        hintsAvailable = 1,
        hintedWord = null,
        selectionState = BoardSelectionState(),
        isPaused = false,
        isVictory = false,
        victoryStep = 0,
        elapsedSeconds = 0,
        starsEarned = 3,
        isDaily = isDaily,
        statusMessage = null,
        lastFoundWord = null
      )
    }

    startTimer()
  }

  private fun startTimer() {
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      while (isActive) {
        delay(1000)
        if (!_uiState.value.isPaused && !_uiState.value.isVictory) {
          _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
        }
      }
    }
  }

  private fun stopTimer() {
    timerJob?.cancel()
    timerJob = null
  }

  // --- Grid Touch Dragging Interaction ---

  fun onCellTouchStart(row: Int, col: Int, touchPos: Offset) {
    if (_uiState.value.isVictory || _uiState.value.isPaused) return

    // If cell already belongs to a found theme word, ignore selection start
    if (isCellAlreadyDiscovered(row, col)) return

    val cell = row to col
    val letter = _uiState.value.level.grid[row][col].toString()

    soundSystem.playTileSelect(0)

    _uiState.update {
      it.copy(
        selectionState = BoardSelectionState(
          selectedCells = listOf(cell),
          currentWord = letter,
          isDragging = true,
          touchPoint = touchPos,
          lastAddedCell = cell
        )
      )
    }
  }

  fun onCellTouchMove(row: Int, col: Int, touchPos: Offset) {
    val state = _uiState.value.selectionState
    if (!state.isDragging || _uiState.value.isVictory || _uiState.value.isPaused) return

    val currentList = state.selectedCells
    if (currentList.isEmpty()) return

    val currentLast = currentList.last()
    val targetCell = row to col

    // If moving to the cell we're already on, just update touch position
    if (targetCell == currentLast) {
      _uiState.update {
        it.copy(selectionState = state.copy(touchPoint = touchPos))
      }
      return
    }

    // BACKTRACK SUPPORT: If player drags back onto the second-to-last cell, pop the last cell!
    if (currentList.size >= 2 && targetCell == currentList[currentList.size - 2]) {
      val poppedList = currentList.dropLast(1)
      val newWord = poppedList.map { (r, c) -> _uiState.value.level.grid[r][c] }.joinToString("")
      soundSystem.playTileSelect(poppedList.size - 1)
      _uiState.update {
        it.copy(
          selectionState = state.copy(
            selectedCells = poppedList,
            currentWord = newWord,
            touchPoint = touchPos,
            lastAddedCell = poppedList.last()
          )
        )
      }
      return
    }

    // Must be adjacent (8-way neighbor: dx <= 1, dy <= 1)
    val dr = abs(row - currentLast.first)
    val dc = abs(col - currentLast.second)
    if (dr > 1 || dc > 1) {
      // Not adjacent, just update touch position
      _uiState.update {
        it.copy(selectionState = state.copy(touchPoint = touchPos))
      }
      return
    }

    // Check if cell is already in the current selection
    if (currentList.contains(targetCell)) {
      _uiState.update {
        it.copy(selectionState = state.copy(touchPoint = touchPos))
      }
      return
    }

    // Check if cell is already part of a found theme word
    if (isCellAlreadyDiscovered(row, col)) {
      _uiState.update {
        it.copy(selectionState = state.copy(touchPoint = touchPos))
      }
      return
    }

    // Valid extension!
    val updatedList = currentList + targetCell
    val newLetter = _uiState.value.level.grid[row][col]
    val newWord = state.currentWord + newLetter

    soundSystem.playTileSelect(updatedList.size - 1)

    _uiState.update {
      it.copy(
        selectionState = state.copy(
          selectedCells = updatedList,
          currentWord = newWord,
          touchPoint = touchPos,
          lastAddedCell = targetCell
        )
      )
    }
  }

  fun onDragPointUpdate(touchPos: Offset) {
    val state = _uiState.value.selectionState
    if (state.isDragging) {
      _uiState.update {
        it.copy(selectionState = state.copy(touchPoint = touchPos))
      }
    }
  }

  fun onCellTouchEnd(cellCenterPositions: Map<Pair<Int, Int>, Offset>) {
    val state = _uiState.value.selectionState
    if (!state.isDragging) return

    val word = state.currentWord.uppercase()
    val level = _uiState.value.level

    if (word.length < 2) {
      clearSelection()
      return
    }

    // Check if word is already found
    if (_uiState.value.foundThemeWords.contains(word)) {
      flashStatusMessage("Already found: $word")
      soundSystem.playInvalid()
      clearSelection()
      return
    }

    // 1. Check if it matches a Theme Word
    val matchedThemeWord = level.themeWords.find {
      it.word == word && matchesPath(state.selectedCells, it.path)
    }

    if (matchedThemeWord != null) {
      handleThemeWordMatch(matchedThemeWord, cellCenterPositions)
      return
    }

    // 2. Check if it's a valid Bonus Word
    if (level.bonusWords.contains(word) && !_uiState.value.foundBonusWords.contains(word)) {
      handleBonusWordMatch(word)
      clearSelection()
      return
    }

    // 3. Invalid word
    handleInvalidWord()
  }

  private fun matchesPath(selected: List<Pair<Int, Int>>, required: List<Pair<Int, Int>>): Boolean {
    // Either forward or backward path is valid
    if (selected == required) return true
    if (selected == required.reversed()) return true
    return false
  }

  private fun handleThemeWordMatch(
    themeWord: ThemeWord,
    cellCenterPositions: Map<Pair<Int, Int>, Offset>
  ) {
    val newFound = _uiState.value.foundThemeWords + themeWord.word
    val isAllFound = newFound.size >= _uiState.value.level.themeWords.size

    val wordPoints = themeWord.path.mapNotNull { cellCenterPositions[it] }

    if (themeWord.isSpecial) {
      // Golden Spangram celebration!
      soundSystem.playSpecialWordReveal()
      particleController.triggerSpecialWord(wordPoints)
      flashStatusMessage("SPANGRAM FOUND: ${themeWord.word}!")
    } else {
      // Normal theme word celebration!
      soundSystem.playWordMatch()
      val midPoint = wordPoints.getOrNull(wordPoints.size / 2) ?: Offset(300f, 300f)
      particleController.triggerSmallMatch(midPoint)
      flashStatusMessage("Theme word found: ${themeWord.word}!")
    }

    _uiState.update {
      it.copy(
        foundThemeWords = newFound,
        lastFoundWord = themeWord,
        hintedWord = if (it.hintedWord?.word == themeWord.word) null else it.hintedWord,
        selectionState = BoardSelectionState()
      )
    }

    if (isAllFound) {
      triggerVictorySequence()
    }
  }

  private fun handleBonusWordMatch(word: String) {
    soundSystem.playBonusWord()
    val newBonusWords = _uiState.value.foundBonusWords + word
    val newCount = _uiState.value.bonusWordCount + 1

    if (newCount >= 3) {
      // Earned a hint!
      flashStatusMessage("Bonus word: $word! +1 Hint Earned!")
      soundSystem.playProgressTick()
      _uiState.update {
        it.copy(
          foundBonusWords = newBonusWords,
          bonusWordCount = 0,
          hintsAvailable = it.hintsAvailable + 1
        )
      }
    } else {
      flashStatusMessage("Bonus word: $word ($newCount/3 for a hint)")
      _uiState.update {
        it.copy(
          foundBonusWords = newBonusWords,
          bonusWordCount = newCount
        )
      }
    }
  }

  private fun handleInvalidWord() {
    soundSystem.playInvalid()
    viewModelScope.launch {
      _uiState.update {
        it.copy(selectionState = it.selectionState.copy(isInvalidFlash = true, isDragging = false))
      }
      delay(320)
      _uiState.update {
        it.copy(selectionState = BoardSelectionState())
      }
    }
  }

  private fun clearSelection() {
    _uiState.update {
      it.copy(selectionState = BoardSelectionState())
    }
  }

  private fun flashStatusMessage(msg: String) {
    viewModelScope.launch {
      _uiState.update { it.copy(statusMessage = msg) }
      delay(2400)
      if (_uiState.value.statusMessage == msg) {
        _uiState.update { it.copy(statusMessage = null) }
      }
    }
  }

  private fun isCellAlreadyDiscovered(row: Int, col: Int): Boolean {
    val level = _uiState.value.level
    val foundSet = _uiState.value.foundThemeWords
    for (tw in level.themeWords) {
      if (foundSet.contains(tw.word) && tw.path.contains(row to col)) {
        return true
      }
    }
    return false
  }

  // --- Hint System ---

  fun useHint() {
    val state = _uiState.value
    if (state.hintsAvailable <= 0 || state.isVictory) return

    // Find first unrevealed theme word
    val unfound = state.level.themeWords.filter { !state.foundThemeWords.contains(it.word) }
    if (unfound.isEmpty()) return

    soundSystem.playHint()
    repository.incrementHintUsed()

    val targetWord = unfound.first()
    _uiState.update {
      it.copy(
        hintsAvailable = it.hintsAvailable - 1,
        hintedWord = targetWord
      )
    }
    flashStatusMessage("Hint: Look for '${targetWord.word.take(2)}...' (${targetWord.word.length} letters)")
  }

  // --- Victory Cinematic Flow ---

  private fun triggerVictorySequence() {
    stopTimer()
    val elapsed = _uiState.value.elapsedSeconds
    val levelId = _uiState.value.level.id
    val hasSpangram = _uiState.value.level.spangram != null

    // Determine stars: 3 stars if under 3 min, 2 stars if under 5 min, 1 star otherwise
    val stars = when {
      elapsed <= 180 -> 3
      elapsed <= 300 -> 2
      else -> 1
    }

    // Save progress & stats
    repository.saveLevelProgress(
      LevelProgress(
        levelId = levelId,
        completed = true,
        stars = stars,
        bestTimeSeconds = elapsed,
        foundWords = _uiState.value.foundThemeWords
      )
    )
    repository.recordGameCompleted(levelId, elapsed, _uiState.value.foundThemeWords.size, hasSpangram)

    viewModelScope.launch {
      // Step 1: Final word highlight (0 - 250ms)
      _uiState.update { it.copy(isVictory = true, victoryStep = 1, starsEarned = stars) }
      delay(250)

      // Step 2: Board celebration (250 - 650ms)
      _uiState.update { it.copy(victoryStep = 2) }
      delay(400)

      // Step 3: Victory reveal (500 - 1000ms)
      soundSystem.playVictory()
      _uiState.update { it.copy(victoryStep = 3) }
      delay(400)

      // Step 4: Falling stars celebration
      particleController.triggerLevelComplete(Size(1080f, 1920f))
      _uiState.update { it.copy(victoryStep = 4) }
    }
  }

  // --- Pause & Game Controls ---

  fun togglePause() {
    soundSystem.playButtonTap()
    val newPause = !_uiState.value.isPaused
    _uiState.update { it.copy(isPaused = newPause) }
    if (newPause) {
      soundSystem.pauseAll()
    } else {
      soundSystem.resumeAll()
    }
  }

  fun restartLevel() {
    soundSystem.playButtonTap()
    startLevel(_uiState.value.level.id, _uiState.value.isDaily)
  }

  fun nextLevel() {
    soundSystem.playButtonTap()
    val nextId = _uiState.value.level.id + 1
    if (nextId <= repository.allLevels.size) {
      soundSystem.playLevelUnlock()
      startLevel(nextId, false)
    } else {
      // Completed all levels! Go home
      navigateTo(Screen.LevelSelect)
    }
  }

  // --- Settings Update ---

  fun updateSettings(newSettings: GameSettings) {
    repository.saveSettings(newSettings)
    soundSystem.updateSettings(newSettings)
    _uiState.update { it.copy(settings = newSettings) }
  }

  fun resetAllProgress() {
    repository.resetAllProgress()
    _uiState.update {
      it.copy(
        stats = repository.getStats(),
        settings = repository.getSettings()
      )
    }
    soundSystem.updateSettings(_uiState.value.settings)
  }
}
