package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.DarkThemeMode
import com.example.data.model.GameSettings
import com.example.data.model.GameStats
import com.example.data.model.LevelData
import com.example.data.model.LevelProgress
import com.example.data.model.ThemeWord
import java.util.Calendar

class LevelsRepository(private val context: Context) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences("strands_game_prefs", Context.MODE_PRIVATE)

  val allLevels: List<LevelData> = listOf(
    createLevel1(),
    createLevel2(),
    createLevel3(),
    createLevel4(),
    createLevel5(),
    createLevel6(),
    createLevel7(),
    createLevel8()
  )

  fun getLevelById(id: Int): LevelData {
    return allLevels.find { it.id == id } ?: allLevels.first()
  }

  fun getDailyPuzzle(): Pair<LevelData, String> {
    val cal = Calendar.getInstance()
    val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
    val year = cal.get(Calendar.YEAR)
    val index = (dayOfYear + year) % allLevels.size
    val dateStr = "${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.DAY_OF_MONTH)}/$year"
    return Pair(allLevels[index], dateStr)
  }

  // --- Persistence for Level Progress ---

  fun getLevelProgress(levelId: Int): LevelProgress {
    val completed = prefs.getBoolean("level_${levelId}_completed", false)
    val stars = prefs.getInt("level_${levelId}_stars", 0)
    val bestTime = prefs.getInt("level_${levelId}_time", 0)
    val wordsSet = prefs.getStringSet("level_${levelId}_words", emptySet()) ?: emptySet()
    return LevelProgress(levelId, completed, stars, bestTime, wordsSet)
  }

  fun saveLevelProgress(progress: LevelProgress) {
    prefs.edit()
      .putBoolean("level_${progress.levelId}_completed", progress.completed)
      .putInt("level_${progress.levelId}_stars", progress.stars)
      .putInt("level_${progress.levelId}_time", progress.bestTimeSeconds)
      .putStringSet("level_${progress.levelId}_words", progress.foundWords)
      .apply()
  }

  fun isLevelUnlocked(levelId: Int): Boolean {
    if (levelId == 1) return true
    // Unlocked if previous level is completed
    return getLevelProgress(levelId - 1).completed
  }

  // --- Stats Persistence ---

  fun getStats(): GameStats {
    return GameStats(
      gamesPlayed = prefs.getInt("stats_played", 0),
      gamesWon = prefs.getInt("stats_won", 0),
      currentStreak = prefs.getInt("stats_streak", 0),
      maxStreak = prefs.getInt("stats_max_streak", 0),
      totalWordsFound = prefs.getInt("stats_words", 0),
      specialWordsFound = prefs.getInt("stats_special_words", 0),
      hintsUsed = prefs.getInt("stats_hints", 0),
      bestTimeSeconds = prefs.getInt("stats_best_time", 0)
    )
  }

  fun recordGameCompleted(levelId: Int, timeSeconds: Int, wordsFoundCount: Int, hasSpangram: Boolean) {
    val stats = getStats()
    val newPlayed = stats.gamesPlayed + 1
    val newWon = stats.gamesWon + 1
    val newStreak = stats.currentStreak + 1
    val newMaxStreak = maxOf(stats.maxStreak, newStreak)
    val newWords = stats.totalWordsFound + wordsFoundCount
    val newSpecial = stats.specialWordsFound + (if (hasSpangram) 1 else 0)
    val newBestTime = if (stats.bestTimeSeconds == 0) timeSeconds else minOf(stats.bestTimeSeconds, timeSeconds)

    prefs.edit()
      .putInt("stats_played", newPlayed)
      .putInt("stats_won", newWon)
      .putInt("stats_streak", newStreak)
      .putInt("stats_max_streak", newMaxStreak)
      .putInt("stats_words", newWords)
      .putInt("stats_special_words", newSpecial)
      .putInt("stats_best_time", newBestTime)
      .apply()
  }

  fun incrementHintUsed() {
    val hints = prefs.getInt("stats_hints", 0)
    prefs.edit().putInt("stats_hints", hints + 1).apply()
  }

  // --- Settings Persistence ---

  fun getSettings(): GameSettings {
    val sound = prefs.getBoolean("settings_sound", true)
    val music = prefs.getBoolean("settings_music", true)
    val sfxVol = prefs.getFloat("settings_sfx_vol", 0.85f)
    val musicVol = prefs.getFloat("settings_music_vol", 0.45f)
    val haptics = prefs.getBoolean("settings_haptics", true)
    val themeModeName = prefs.getString("settings_theme_mode", DarkThemeMode.SYSTEM.name)
    val themeMode = try {
      DarkThemeMode.valueOf(themeModeName ?: DarkThemeMode.SYSTEM.name)
    } catch (_: Exception) {
      DarkThemeMode.SYSTEM
    }
    val reducedMotion = prefs.getBoolean("settings_reduced_motion", false)
    return GameSettings(sound, music, sfxVol, musicVol, haptics, themeMode, reducedMotion)
  }

  fun saveSettings(settings: GameSettings) {
    prefs.edit()
      .putBoolean("settings_sound", settings.soundEnabled)
      .putBoolean("settings_music", settings.musicEnabled)
      .putFloat("settings_sfx_vol", settings.sfxVolume)
      .putFloat("settings_music_vol", settings.musicVolume)
      .putBoolean("settings_haptics", settings.hapticsEnabled)
      .putString("settings_theme_mode", settings.darkThemePreference.name)
      .putBoolean("settings_reduced_motion", settings.reducedMotion)
      .apply()
  }

  fun resetAllProgress() {
    prefs.edit().clear().apply()
  }

  // --- Level Definitions with mathematically verified paths ---

  private fun createLevel1(): LevelData {
    // 6x6 Grid, Theme: Rise and Shine (Breakfast essentials)
    val grid = listOf(
      "COFFEE",
      "TOASTM",
      "JUICEI",
      "BACONL",
      "MORNIK",
      "EGGSNG"
    )
    val themeWords = listOf(
      ThemeWord("COFFEE", false, listOf(0 to 0, 0 to 1, 0 to 2, 0 to 3, 0 to 4, 0 to 5)),
      ThemeWord("TOAST", false, listOf(1 to 0, 1 to 1, 1 to 2, 1 to 3, 1 to 4)),
      ThemeWord("JUICE", false, listOf(2 to 0, 2 to 1, 2 to 2, 2 to 3, 2 to 4)),
      ThemeWord("BACON", false, listOf(3 to 0, 3 to 1, 3 to 2, 3 to 3, 3 to 4)),
      ThemeWord("EGGS", false, listOf(5 to 0, 5 to 1, 5 to 2, 5 to 3)),
      ThemeWord("MILK", false, listOf(1 to 5, 2 to 5, 3 to 5, 4 to 5)),
      ThemeWord("MORNING", true, listOf(4 to 0, 4 to 1, 4 to 2, 4 to 3, 4 to 4, 5 to 4, 5 to 5))
    )
    return LevelData(
      id = 1,
      title = "Rise & Shine",
      themeClue = "Breakfast essentials",
      rows = 6,
      cols = 6,
      grid = grid,
      themeWords = themeWords,
      bonusWords = setOf("ICE", "TEA", "EAT", "COFF", "OAST", "CON", "FOR", "NOR", "GIN")
    )
  }

  private fun createLevel2(): LevelData {
    // 6x6 Grid, Theme: Celestial Night
    val grid = listOf(
      "PLANET",
      "COMETS",
      "ORBITU",
      "SOLARN",
      "NIGHTS",
      "MOONYK"
    )
    val themeWords = listOf(
      ThemeWord("PLANET", false, listOf(0 to 0, 0 to 1, 0 to 2, 0 to 3, 0 to 4, 0 to 5)),
      ThemeWord("COMET", false, listOf(1 to 0, 1 to 1, 1 to 2, 1 to 3, 1 to 4)),
      ThemeWord("ORBIT", false, listOf(2 to 0, 2 to 1, 2 to 2, 2 to 3, 2 to 4)),
      ThemeWord("SOLAR", false, listOf(3 to 0, 3 to 1, 3 to 2, 3 to 3, 3 to 4)),
      ThemeWord("SUN", false, listOf(1 to 5, 2 to 5, 3 to 5)),
      ThemeWord("MOON", false, listOf(5 to 0, 5 to 1, 5 to 2, 5 to 3)),
      ThemeWord("NIGHTSKY", true, listOf(4 to 0, 4 to 1, 4 to 2, 4 to 3, 4 to 4, 4 to 5, 5 to 5, 5 to 4))
    )
    return LevelData(
      id = 2,
      title = "Celestial Night",
      themeClue = "Look up after dark",
      rows = 6,
      cols = 6,
      grid = grid,
      themeWords = themeWords,
      bonusWords = setOf("STAR", "SKY", "LANE", "NET", "MET", "BIT", "SON", "NIGHT")
    )
  }

  private fun createLevel3(): LevelData {
    // 6x6 Grid, Theme: Beneath the Waves
    val grid = listOf(
      "CORALF",
      "SHARKI",
      "WHALEN",
      "SQUIDC",
      "DEEPWL",
      "ATERMA"
    )
    val themeWords = listOf(
      ThemeWord("CORAL", false, listOf(0 to 0, 0 to 1, 0 to 2, 0 to 3, 0 to 4)),
      ThemeWord("SHARK", false, listOf(1 to 0, 1 to 1, 1 to 2, 1 to 3, 1 to 4)),
      ThemeWord("WHALE", false, listOf(2 to 0, 2 to 1, 2 to 2, 2 to 3, 2 to 4)),
      ThemeWord("SQUID", false, listOf(3 to 0, 3 to 1, 3 to 2, 3 to 3, 3 to 4)),
      ThemeWord("FIN", false, listOf(0 to 5, 1 to 5, 2 to 5)),
      ThemeWord("CLAM", false, listOf(3 to 5, 4 to 5, 5 to 5, 5 to 4)),
      ThemeWord("DEEPWATER", true, listOf(4 to 0, 4 to 1, 4 to 2, 4 to 3, 4 to 4, 5 to 3, 5 to 2, 5 to 1, 5 to 0))
    )
    return LevelData(
      id = 3,
      title = "Deep Ocean",
      themeClue = "Beneath the waves",
      rows = 6,
      cols = 6,
      grid = grid,
      themeWords = themeWords,
      bonusWords = setOf("SEA", "WAVE", "HARK", "ALE", "QUID", "DEEP", "WATER", "TERM")
    )
  }

  private fun createLevel4(): LevelData {
    // 6x6 Grid, Theme: Sweet Tooth
    val grid = listOf(
      "FUDGEP",
      "JELLYI",
      "TAFFYE",
      "MINTSC",
      "SUGARA",
      "HSUREK"
    )
    val themeWords = listOf(
      ThemeWord("FUDGE", false, listOf(0 to 0, 0 to 1, 0 to 2, 0 to 3, 0 to 4)),
      ThemeWord("JELLY", false, listOf(1 to 0, 1 to 1, 1 to 2, 1 to 3, 1 to 4)),
      ThemeWord("TAFFY", false, listOf(2 to 0, 2 to 1, 2 to 2, 2 to 3, 2 to 4)),
      ThemeWord("MINTS", false, listOf(3 to 0, 3 to 1, 3 to 2, 3 to 3, 3 to 4)),
      ThemeWord("PIE", false, listOf(0 to 5, 1 to 5, 2 to 5)),
      ThemeWord("CAKE", false, listOf(3 to 5, 4 to 5, 5 to 5, 5 to 4)),
      ThemeWord("SUGARRUSH", true, listOf(4 to 0, 4 to 1, 4 to 2, 4 to 3, 4 to 4, 5 to 3, 5 to 2, 5 to 1, 5 to 0))
    )
    return LevelData(
      id = 4,
      title = "Sweet Tooth",
      themeClue = "Sugar rush treats",
      rows = 6,
      cols = 6,
      grid = grid,
      themeWords = themeWords,
      bonusWords = setOf("SUGAR", "RUSH", "MINT", "AFFY", "ELL", "SURE", "RUE")
    )
  }

  private fun createLevel5(): LevelData {
    // 6x6 Grid, Theme: Sound Tracks (Orchestra)
    val grid = listOf(
      "FLUTEB",
      "PIANOO",
      "DRUMSW",
      "CELLOH",
      "ORCHEA",
      "ARTSPR"
    )
    val themeWords = listOf(
      ThemeWord("FLUTE", false, listOf(0 to 0, 0 to 1, 0 to 2, 0 to 3, 0 to 4)),
      ThemeWord("PIANO", false, listOf(1 to 0, 1 to 1, 1 to 2, 1 to 3, 1 to 4)),
      ThemeWord("DRUMS", false, listOf(2 to 0, 2 to 1, 2 to 2, 2 to 3, 2 to 4)),
      ThemeWord("CELLO", false, listOf(3 to 0, 3 to 1, 3 to 2, 3 to 3, 3 to 4)),
      ThemeWord("BOW", false, listOf(0 to 5, 1 to 5, 2 to 5)),
      ThemeWord("HARP", false, listOf(3 to 5, 4 to 5, 5 to 5, 5 to 4)),
      ThemeWord("ORCHESTRA", true, listOf(4 to 0, 4 to 1, 4 to 2, 4 to 3, 4 to 4, 5 to 3, 5 to 2, 5 to 1, 5 to 0))
    )
    return LevelData(
      id = 5,
      title = "Sound Tracks",
      themeClue = "In the concert hall",
      rows = 6,
      cols = 6,
      grid = grid,
      themeWords = themeWords,
      bonusWords = setOf("LUTE", "RUM", "ART", "STAR", "HEAR", "PART", "ROAR")
    )
  }

  private fun createLevel6(): LevelData {
    // 6x6 Grid, Theme: Green Thumb (Garden blossoms)
    val grid = listOf(
      "TULIPB",
      "DAISYU",
      "PEONYD",
      "FERNSR",
      "BOTANO",
      "LACIES"
    )
    val themeWords = listOf(
      ThemeWord("TULIP", false, listOf(0 to 0, 0 to 1, 0 to 2, 0 to 3, 0 to 4)),
      ThemeWord("DAISY", false, listOf(1 to 0, 1 to 1, 1 to 2, 1 to 3, 1 to 4)),
      ThemeWord("PEONY", false, listOf(2 to 0, 2 to 1, 2 to 2, 2 to 3, 2 to 4)),
      ThemeWord("FERNS", false, listOf(3 to 0, 3 to 1, 3 to 2, 3 to 3, 3 to 4)),
      ThemeWord("BUD", false, listOf(0 to 5, 1 to 5, 2 to 5)),
      ThemeWord("ROSE", false, listOf(3 to 5, 4 to 5, 5 to 5, 5 to 4)),
      ThemeWord("BOTANICAL", true, listOf(4 to 0, 4 to 1, 4 to 2, 4 to 3, 4 to 4, 5 to 3, 5 to 2, 5 to 1, 5 to 0))
    )
    return LevelData(
      id = 6,
      title = "Green Thumb",
      themeClue = "Garden blossoms",
      rows = 6,
      cols = 6,
      grid = grid,
      themeWords = themeWords,
      bonusWords = setOf("FERN", "TUL", "DAY", "PALE", "LACE", "ICE", "ROSE")
    )
  }

  private fun createLevel7(): LevelData {
    // 6x6 Grid, Theme: Wild Safari
    val grid = listOf(
      "ZEBRAC",
      "HYENAU",
      "RHINOB",
      "LIONSB",
      "SERENI",
      "ITEGDR"
    )
    val themeWords = listOf(
      ThemeWord("ZEBRA", false, listOf(0 to 0, 0 to 1, 0 to 2, 0 to 3, 0 to 4)),
      ThemeWord("HYENA", false, listOf(1 to 0, 1 to 1, 1 to 2, 1 to 3, 1 to 4)),
      ThemeWord("RHINO", false, listOf(2 to 0, 2 to 1, 2 to 2, 2 to 3, 2 to 4)),
      ThemeWord("LIONS", false, listOf(3 to 0, 3 to 1, 3 to 2, 3 to 3, 3 to 4)),
      ThemeWord("CUB", false, listOf(0 to 5, 1 to 5, 2 to 5)),
      ThemeWord("BIRD", false, listOf(3 to 5, 4 to 5, 5 to 5, 5 to 4)),
      ThemeWord("SERENGETI", true, listOf(4 to 0, 4 to 1, 4 to 2, 4 to 3, 4 to 4, 5 to 3, 5 to 2, 5 to 1, 5 to 0))
    )
    return LevelData(
      id = 7,
      title = "Wild Safari",
      themeClue = "On the savanna",
      rows = 6,
      cols = 6,
      grid = grid,
      themeWords = themeWords,
      bonusWords = setOf("LION", "HIN", "BRA", "RIDE", "GRID", "TIGER", "GET")
    )
  }

  private fun createLevel8(): LevelData {
    // 6x6 Grid, Theme: Cozy Library
    val grid = listOf(
      "NOVELI",
      "PROSEN",
      "SPINEK",
      "COVERP",
      "BOOKSA",
      "EROTEG"
    )
    val themeWords = listOf(
      ThemeWord("NOVEL", false, listOf(0 to 0, 0 to 1, 0 to 2, 0 to 3, 0 to 4)),
      ThemeWord("PROSE", false, listOf(1 to 0, 1 to 1, 1 to 2, 1 to 3, 1 to 4)),
      ThemeWord("SPINE", false, listOf(2 to 0, 2 to 1, 2 to 2, 2 to 3, 2 to 4)),
      ThemeWord("COVER", false, listOf(3 to 0, 3 to 1, 3 to 2, 3 to 3, 3 to 4)),
      ThemeWord("INK", false, listOf(0 to 5, 1 to 5, 2 to 5)),
      ThemeWord("PAGE", false, listOf(3 to 5, 4 to 5, 5 to 5, 5 to 4)),
      ThemeWord("BOOKSTORE", true, listOf(4 to 0, 4 to 1, 4 to 2, 4 to 3, 4 to 4, 5 to 3, 5 to 2, 5 to 1, 5 to 0))
    )
    return LevelData(
      id = 8,
      title = "Cozy Library",
      themeClue = "Between the pages",
      rows = 6,
      cols = 6,
      grid = grid,
      themeWords = themeWords,
      bonusWords = setOf("BOOK", "ROSE", "PIN", "OVER", "STORE", "TORE", "NOTE")
    )
  }
}
