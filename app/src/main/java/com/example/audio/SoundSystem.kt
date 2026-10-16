package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.model.GameSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Procedural low-latency Audio and Haptic engine.
 * Synthesizes crystal-clear, harmonic game sounds in memory with zero external asset dependencies.
 */
class SoundSystem(private val context: Context) {

  private val sampleRate = 44100
  private val scope = CoroutineScope(Dispatchers.Default)

  private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
    vibratorManager?.defaultVibrator
  } else {
    @Suppress("DEPRECATION")
    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
  }

  @Volatile
  var settings: GameSettings = GameSettings()

  private var ambientMusicJob: Job? = null
  private val soundCache = ConcurrentHashMap<String, ShortArray>()

  // Pentatonic scale notes for dynamic drag extension
  private val pentatonicNotes = doubleArrayOf(
    523.25, // C5
    587.33, // D5
    659.25, // E5
    783.99, // G5
    880.00, // A5
    1046.50, // C6
    1174.66, // D6
    1318.51, // E6
    1567.98  // G6
  )

  init {
    // Pre-synthesize core sounds for instantaneous playback
    scope.launch {
      warmupSounds()
    }
  }

  private fun warmupSounds() {
    for (i in pentatonicNotes.indices) {
      soundCache["tile_$i"] = generatePluck(pentatonicNotes[i], 0.12, 0.7)
    }
    soundCache["invalid"] = generateInvalidTone()
    soundCache["word_match"] = generateArpeggio(doubleArrayOf(523.25, 659.25, 783.99, 1046.50), 0.08, 0.45)
    soundCache["bonus_word"] = generateArpeggio(doubleArrayOf(659.25, 880.00, 1174.66), 0.07, 0.35)
    soundCache["special_word"] = generateGoldenFlourish()
    soundCache["hint"] = generateArpeggio(doubleArrayOf(783.99, 987.77, 1174.66, 1567.98), 0.09, 0.5)
    soundCache["progress_tick"] = generateWoodblock(900.0, 0.04)
    soundCache["victory"] = generateVictoryFanfare()
    soundCache["button_tap"] = generateClick(1200.0, 0.02)
    soundCache["level_unlock"] = generateArpeggio(doubleArrayOf(587.33, 783.99, 1046.50, 1318.51), 0.09, 0.45)
  }

  fun updateSettings(newSettings: GameSettings) {
    this.settings = newSettings
    if (newSettings.musicEnabled) {
      startAmbientMusic()
    } else {
      stopAmbientMusic()
    }
  }

  // --- Sound Trigger Events ---

  fun playTileSelect(stepIndex: Int) {
    if (!settings.soundEnabled) return
    val noteIdx = (stepIndex.coerceAtLeast(0)) % pentatonicNotes.size
    val key = "tile_$noteIdx"
    val samples = soundCache[key] ?: generatePluck(pentatonicNotes[noteIdx], 0.12, 0.7)
    playPcm(samples, settings.sfxVolume * 0.75f)
    performHaptic(HapticType.LIGHT)
  }

  fun playInvalid() {
    if (!settings.soundEnabled) return
    val samples = soundCache["invalid"] ?: generateInvalidTone()
    playPcm(samples, settings.sfxVolume * 0.85f)
    performHaptic(HapticType.ERROR)
  }

  fun playWordMatch() {
    if (!settings.soundEnabled) return
    val samples = soundCache["word_match"] ?: generateArpeggio(doubleArrayOf(523.25, 659.25, 783.99, 1046.50), 0.08, 0.45)
    playPcm(samples, settings.sfxVolume * 0.95f)
    performHaptic(HapticType.SUCCESS)
  }

  fun playBonusWord() {
    if (!settings.soundEnabled) return
    val samples = soundCache["bonus_word"] ?: generateArpeggio(doubleArrayOf(659.25, 880.00, 1174.66), 0.07, 0.35)
    playPcm(samples, settings.sfxVolume * 0.85f)
    performHaptic(HapticType.MEDIUM)
  }

  fun playSpecialWordReveal() {
    if (!settings.soundEnabled) return
    val samples = soundCache["special_word"] ?: generateGoldenFlourish()
    playPcm(samples, settings.sfxVolume * 1.0f)
    performHaptic(HapticType.HEAVY)
  }

  fun playHint() {
    if (!settings.soundEnabled) return
    val samples = soundCache["hint"] ?: generateArpeggio(doubleArrayOf(783.99, 987.77, 1174.66, 1567.98), 0.09, 0.5)
    playPcm(samples, settings.sfxVolume * 0.85f)
    performHaptic(HapticType.LIGHT)
  }

  fun playProgressTick() {
    if (!settings.soundEnabled) return
    val samples = soundCache["progress_tick"] ?: generateWoodblock(900.0, 0.04)
    playPcm(samples, settings.sfxVolume * 0.6f)
  }

  fun playVictory() {
    if (!settings.soundEnabled) return
    val samples = soundCache["victory"] ?: generateVictoryFanfare()
    playPcm(samples, settings.sfxVolume * 1.0f)
    performHaptic(HapticType.VICTORY)
  }

  fun playButtonTap() {
    if (!settings.soundEnabled) return
    val samples = soundCache["button_tap"] ?: generateClick(1200.0, 0.02)
    playPcm(samples, settings.sfxVolume * 0.5f)
    performHaptic(HapticType.LIGHT)
  }

  fun playLevelUnlock() {
    if (!settings.soundEnabled) return
    val samples = soundCache["level_unlock"] ?: generateArpeggio(doubleArrayOf(587.33, 783.99, 1046.50, 1318.51), 0.09, 0.45)
    playPcm(samples, settings.sfxVolume * 0.85f)
    performHaptic(HapticType.SUCCESS)
  }

  // --- Background Ambient Music Engine ---

  fun startAmbientMusic() {
    if (!settings.musicEnabled || ambientMusicJob?.isActive == true) return
    ambientMusicJob = scope.launch {
      val chords = listOf(
        doubleArrayOf(261.63, 329.63, 392.00, 493.88), // Cmaj7
        doubleArrayOf(220.00, 261.63, 329.63, 392.00), // Am7
        doubleArrayOf(174.61, 261.63, 329.63, 392.00), // Fmaj7
        doubleArrayOf(196.00, 246.94, 293.66, 392.00)  // G6
      )
      var chordIdx = 0
      while (isActive && settings.musicEnabled) {
        val chord = chords[chordIdx % chords.size]
        chordIdx++
        for (freq in chord) {
          if (!isActive || !settings.musicEnabled) break
          val notePcm = generateAmbientPad(freq, 1.8, 0.4)
          playPcm(notePcm, settings.musicVolume * 0.35f)
          delay(450)
        }
        delay(1200)
      }
    }
  }

  fun stopAmbientMusic() {
    ambientMusicJob?.cancel()
    ambientMusicJob = null
  }

  fun pauseAll() {
    stopAmbientMusic()
  }

  fun resumeAll() {
    if (settings.musicEnabled) {
      startAmbientMusic()
    }
  }

  // --- Audio Synthesis Algorithms ---

  private fun playPcm(samples: ShortArray, volume: Float) {
    scope.launch {
      try {
        val scaledVolume = volume.coerceIn(0f, 1f)
        if (scaledVolume <= 0.01f) return@launch

        val track = AudioTrack.Builder()
          .setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_GAME)
              .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
              .build()
          )
          .setAudioFormat(
            AudioFormat.Builder()
              .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
              .setSampleRate(sampleRate)
              .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
              .build()
          )
          .setBufferSizeInBytes(samples.size * 2)
          .setTransferMode(AudioTrack.MODE_STATIC)
          .build()

        track.setVolume(scaledVolume)
        track.write(samples, 0, samples.size)
        track.play()

        // Clean up track after playback finishes
        val durationMs = (samples.size * 1000L / sampleRate) + 50L
        delay(durationMs)
        track.stop()
        track.release()
      } catch (_: Exception) {
        // Audio device unavailable or interrupted
      }
    }
  }

  private fun generatePluck(freq: Double, durationSec: Double, decaySpeed: Double): ShortArray {
    val totalSamples = (sampleRate * durationSec).toInt()
    val buffer = ShortArray(totalSamples)
    for (i in 0 until totalSamples) {
      val t = i.toDouble() / sampleRate
      val env = exp(-t * (12.0 * decaySpeed))
      // Fundamental + 2nd overtone for rich celesta timbre
      val wave = sin(2 * PI * freq * t) * 0.7 + sin(2 * PI * (freq * 2.0) * t) * 0.25 + sin(2 * PI * (freq * 3.0) * t) * 0.05
      val sample = (wave * env * 32767.0).toInt().coerceIn(-32768, 32767)
      buffer[i] = sample.toShort()
    }
    return buffer
  }

  private fun generateClick(freq: Double, durationSec: Double): ShortArray {
    val totalSamples = (sampleRate * durationSec).toInt()
    val buffer = ShortArray(totalSamples)
    for (i in 0 until totalSamples) {
      val t = i.toDouble() / sampleRate
      val env = exp(-t * 90.0)
      val wave = sin(2 * PI * freq * t)
      val sample = (wave * env * 28000.0).toInt().coerceIn(-32768, 32767)
      buffer[i] = sample.toShort()
    }
    return buffer
  }

  private fun generateWoodblock(freq: Double, durationSec: Double): ShortArray {
    val totalSamples = (sampleRate * durationSec).toInt()
    val buffer = ShortArray(totalSamples)
    for (i in 0 until totalSamples) {
      val t = i.toDouble() / sampleRate
      val env = exp(-t * 60.0)
      val wave = sin(2 * PI * freq * t) * 0.8 + sin(2 * PI * (freq * 2.3) * t) * 0.2
      val sample = (wave * env * 30000.0).toInt().coerceIn(-32768, 32767)
      buffer[i] = sample.toShort()
    }
    return buffer
  }

  private fun generateInvalidTone(): ShortArray {
    val durationSec = 0.28
    val totalSamples = (sampleRate * durationSec).toInt()
    val buffer = ShortArray(totalSamples)
    for (i in 0 until totalSamples) {
      val t = i.toDouble() / sampleRate
      // Double tap pulse
      val pulse = if (t < 0.12) exp(-t * 22.0) else if (t in 0.14..0.26) exp(-(t - 0.14) * 22.0) else 0.0
      val freq = if (t < 0.12) 220.0 else 180.0
      val wave = sin(2 * PI * freq * t) * 0.7 + sin(2 * PI * (freq * 0.5) * t) * 0.3
      val sample = (wave * pulse * 28000.0).toInt().coerceIn(-32768, 32767)
      buffer[i] = sample.toShort()
    }
    return buffer
  }

  private fun generateArpeggio(freqs: DoubleArray, noteDurationSec: Double, decay: Double): ShortArray {
    val totalDuration = noteDurationSec * freqs.size + 0.3
    val totalSamples = (sampleRate * totalDuration).toInt()
    val buffer = ShortArray(totalSamples)
    for (noteIdx in freqs.indices) {
      val startSample = (noteIdx * noteDurationSec * sampleRate).toInt()
      val freq = freqs[noteIdx]
      for (i in 0 until (sampleRate * 0.45).toInt()) {
        val targetIdx = startSample + i
        if (targetIdx >= totalSamples) break
        val t = i.toDouble() / sampleRate
        val env = exp(-t * (9.0 * decay))
        val wave = sin(2 * PI * freq * t) * 0.75 + sin(2 * PI * (freq * 2.0) * t) * 0.2
        val currentVal = buffer[targetIdx].toInt()
        val newVal = (currentVal + (wave * env * 22000.0).toInt()).coerceIn(-32768, 32767)
        buffer[targetIdx] = newVal.toShort()
      }
    }
    return buffer
  }

  private fun generateGoldenFlourish(): ShortArray {
    // 6-note grand harp flourish with shimmering resonance
    val freqs = doubleArrayOf(523.25, 783.99, 1046.50, 1318.51, 1567.98, 2093.00)
    val noteStepSec = 0.08
    val totalDuration = 0.95
    val totalSamples = (sampleRate * totalDuration).toInt()
    val buffer = ShortArray(totalSamples)
    for (idx in freqs.indices) {
      val startSample = (idx * noteStepSec * sampleRate).toInt()
      val freq = freqs[idx]
      for (i in 0 until (sampleRate * 0.6).toInt()) {
        val targetIdx = startSample + i
        if (targetIdx >= totalSamples) break
        val t = i.toDouble() / sampleRate
        val env = exp(-t * 5.0)
        // Add harmonic shimmer with soft chorus
        val wave = sin(2 * PI * freq * t) * 0.65 +
            sin(2 * PI * (freq * 2.0) * t) * 0.25 +
            sin(2 * PI * (freq * 1.004) * t) * 0.1
        val currentVal = buffer[targetIdx].toInt()
        val newVal = (currentVal + (wave * env * 20000.0).toInt()).coerceIn(-32768, 32767)
        buffer[targetIdx] = newVal.toShort()
      }
    }
    return buffer
  }

  private fun generateVictoryFanfare(): ShortArray {
    // Triumphant uplifting melody: C5 -> E5 -> G5 -> C6 (held) -> E6
    val notes = listOf(
      523.25 to 0.12,
      659.25 to 0.12,
      783.99 to 0.14,
      1046.50 to 0.28,
      1318.51 to 0.55
    )
    val totalDuration = 1.35
    val totalSamples = (sampleRate * totalDuration).toInt()
    val buffer = ShortArray(totalSamples)
    var currentTimeSec = 0.0
    for ((freq, dur) in notes) {
      val startSample = (currentTimeSec * sampleRate).toInt()
      for (i in 0 until (sampleRate * (dur + 0.35)).toInt()) {
        val targetIdx = startSample + i
        if (targetIdx >= totalSamples) break
        val t = i.toDouble() / sampleRate
        val env = exp(-t * (4.2 / dur))
        val wave = sin(2 * PI * freq * t) * 0.7 + sin(2 * PI * (freq * 2.0) * t) * 0.25 + sin(2 * PI * (freq * 3.0) * t) * 0.05
        val currentVal = buffer[targetIdx].toInt()
        val newVal = (currentVal + (wave * env * 22000.0).toInt()).coerceIn(-32768, 32767)
        buffer[targetIdx] = newVal.toShort()
      }
      currentTimeSec += dur
    }
    return buffer
  }

  private fun generateAmbientPad(freq: Double, durationSec: Double, amp: Double): ShortArray {
    val totalSamples = (sampleRate * durationSec).toInt()
    val buffer = ShortArray(totalSamples)
    for (i in 0 until totalSamples) {
      val t = i.toDouble() / sampleRate
      // Gentle attack and slow decay
      val attack = (t / 0.35).coerceAtMost(1.0)
      val decay = exp(-t * 1.5)
      val env = attack * decay * amp
      val wave = sin(2 * PI * freq * t) * 0.8 + sin(2 * PI * (freq * 0.5) * t) * 0.2
      val sample = (wave * env * 25000.0).toInt().coerceIn(-32768, 32767)
      buffer[i] = sample.toShort()
    }
    return buffer
  }

  // --- Haptics ---

  enum class HapticType {
    LIGHT,
    MEDIUM,
    HEAVY,
    SUCCESS,
    ERROR,
    VICTORY,
  }

  private fun performHaptic(type: HapticType) {
    if (!settings.hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        when (type) {
          HapticType.LIGHT -> {
            vibrator.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE))
          }
          HapticType.MEDIUM -> {
            vibrator.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
          }
          HapticType.HEAVY -> {
            vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
          }
          HapticType.SUCCESS -> {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 20, 40, 30), intArrayOf(0, 180, 0, 255), -1))
          }
          HapticType.ERROR -> {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 35, 45, 35), intArrayOf(0, 220, 0, 200), -1))
          }
          HapticType.VICTORY -> {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 30, 40, 30, 60), intArrayOf(0, 160, 0, 200, 0, 255), -1))
          }
        }
      } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(25)
      }
    } catch (_: Exception) {
      // Ignore vibration failures on devices with missing or restricted permissions
    }
  }
}
