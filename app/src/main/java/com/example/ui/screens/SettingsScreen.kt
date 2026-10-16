package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DarkThemeMode
import com.example.data.model.GameSettings

@Composable
fun SettingsScreen(
  settings: GameSettings,
  onUpdateSettings: (GameSettings) -> Unit,
  onResetProgress: () -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isDark = isSystemInDarkTheme()
  var showResetDialog by remember { mutableStateOf(false) }

  BackHandler { onBack() }

  Column(
    modifier = modifier
      .fillMaxSize()
      .statusBarsPadding()
      .padding(horizontal = 20.dp, vertical = 12.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier.testTag("settings_back_button")
      ) {
        Icon(
          imageVector = Icons.Default.ArrowBack,
          contentDescription = "Back",
          tint = MaterialTheme.colorScheme.onSurface
        )
      }
      Text(
        text = "SETTINGS",
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    // Appearance Mode
    Text(
      text = "APPEARANCE",
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      ThemeModeButton(
        label = "Light",
        icon = Icons.Default.LightMode,
        isSelected = settings.darkThemePreference == DarkThemeMode.LIGHT,
        onClick = { onUpdateSettings(settings.copy(darkThemePreference = DarkThemeMode.LIGHT)) },
        modifier = Modifier.weight(1f)
      )
      ThemeModeButton(
        label = "Dark",
        icon = Icons.Default.DarkMode,
        isSelected = settings.darkThemePreference == DarkThemeMode.DARK,
        onClick = { onUpdateSettings(settings.copy(darkThemePreference = DarkThemeMode.DARK)) },
        modifier = Modifier.weight(1f)
      )
      ThemeModeButton(
        label = "System",
        icon = Icons.Default.SettingsBrightness,
        isSelected = settings.darkThemePreference == DarkThemeMode.SYSTEM,
        onClick = { onUpdateSettings(settings.copy(darkThemePreference = DarkThemeMode.SYSTEM)) },
        modifier = Modifier.weight(1f)
      )
    }

    // Audio & Haptics
    Text(
      text = "AUDIO & HAPTICS",
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(top = 8.dp)
    )

    // Sound FX Row
    SettingsToggleCard(
      title = "Sound Effects",
      subtitle = "Tile selection, match flourishes, chimes",
      icon = Icons.Default.VolumeUp,
      checked = settings.soundEnabled,
      onCheckedChange = { onUpdateSettings(settings.copy(soundEnabled = it)) }
    )

    if (settings.soundEnabled) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("SFX Volume", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("${(settings.sfxVolume * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
          value = settings.sfxVolume,
          onValueChange = { onUpdateSettings(settings.copy(sfxVolume = it)) },
          valueRange = 0f..1f,
          colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary
          )
        )
      }
    }

    // Ambient Music Row
    SettingsToggleCard(
      title = "Ambient Music",
      subtitle = "Peaceful, relaxing background marimba notes",
      icon = Icons.Default.MusicNote,
      checked = settings.musicEnabled,
      onCheckedChange = { onUpdateSettings(settings.copy(musicEnabled = it)) }
    )

    if (settings.musicEnabled) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Music Volume", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("${(settings.musicVolume * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
          value = settings.musicVolume,
          onValueChange = { onUpdateSettings(settings.copy(musicVolume = it)) },
          valueRange = 0f..1f,
          colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.secondary,
            activeTrackColor = MaterialTheme.colorScheme.secondary
          )
        )
      }
    }

    // Haptics Row
    SettingsToggleCard(
      title = "Haptic Feedback",
      subtitle = "Tactile response on selection and completion",
      icon = Icons.Default.Vibration,
      checked = settings.hapticsEnabled,
      onCheckedChange = { onUpdateSettings(settings.copy(hapticsEnabled = it)) }
    )

    // Reset Progress
    Text(
      text = "DATA MANAGEMENT",
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(top = 8.dp)
    )

    OutlinedButton(
      onClick = { showResetDialog = true },
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("reset_progress_button"),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.error
      )
    ) {
      Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.size(8.dp))
      Text("Reset All Puzzle Progress", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }

    Spacer(modifier = Modifier.height(24.dp))
  }

  // Reset Confirmation Dialog
  if (showResetDialog) {
    AlertDialog(
      onDismissRequest = { showResetDialog = false },
      title = { Text("Reset Progress?", fontWeight = FontWeight.Bold) },
      text = { Text("This will clear your completed levels, stars, and play statistics. This action cannot be undone.") },
      confirmButton = {
        Button(
          onClick = {
            onResetProgress()
            showResetDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Reset", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun ThemeModeButton(
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isDark = isSystemInDarkTheme()
  Box(
    modifier = modifier
      .shadow(if (isSelected) 3.dp else 1.dp, RoundedCornerShape(14.dp))
      .clip(RoundedCornerShape(14.dp))
      .background(
        if (isSelected) {
          MaterialTheme.colorScheme.primary
        } else if (isDark) {
          MaterialTheme.colorScheme.surfaceVariant
        } else {
          Color.White
        }
      )
      .border(
        width = 1.dp,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        shape = RoundedCornerShape(14.dp)
      )
      .clickable(onClick = onClick)
      .padding(vertical = 12.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(20.dp)
      )
      Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

@Composable
private fun SettingsToggleCard(
  title: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  val isDark = isSystemInDarkTheme()
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .shadow(1.dp, RoundedCornerShape(14.dp))
      .clip(RoundedCornerShape(14.dp))
      .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White)
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
      .padding(horizontal = 16.dp, vertical = 12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(22.dp)
        )
        Column {
          Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
          Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
      Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
          checkedThumbColor = Color.White,
          checkedTrackColor = MaterialTheme.colorScheme.primary
        )
      )
    }
  }
}
