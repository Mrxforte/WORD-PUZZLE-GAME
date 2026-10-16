package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameStats
import com.example.ui.theme.GameColors

@Composable
fun StatsScreen(
  stats: GameStats,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isDark = isSystemInDarkTheme()
  BackHandler { onBack() }

  Column(
    modifier = modifier
      .fillMaxSize()
      .statusBarsPadding()
      .padding(horizontal = 20.dp, vertical = 12.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier.testTag("stats_back_button")
      ) {
        Icon(
          imageVector = Icons.Default.ArrowBack,
          contentDescription = "Back",
          tint = MaterialTheme.colorScheme.onSurface
        )
      }
      Text(
        text = "STATISTICS",
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    // High Level Highlights Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      HighlightCard(
        title = "Puzzles Won",
        value = "${stats.gamesWon} / ${stats.gamesPlayed}",
        icon = Icons.Default.EmojiEvents,
        tint = if (isDark) GameColors.SpangramGoldDark else GameColors.SpangramGoldLight,
        modifier = Modifier.weight(1f)
      )
      HighlightCard(
        title = "Win Rate",
        value = if (stats.gamesPlayed > 0) "${(stats.gamesWon * 100 / stats.gamesPlayed)}%" else "100%",
        icon = Icons.Default.Bolt,
        tint = if (isDark) GameColors.DiscoveredTealDark else GameColors.DiscoveredTealLight,
        modifier = Modifier.weight(1f)
      )
    }

    // Detailed Stats Grid
    Text(
      text = "PERFORMANCE METRICS",
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(top = 8.dp)
    )

    StatRowItem(
      label = "Current Streak",
      value = "${stats.currentStreak} Days",
      icon = Icons.Default.Bolt,
      isDark = isDark
    )
    StatRowItem(
      label = "Max Streak",
      value = "${stats.maxStreak} Days",
      icon = Icons.Default.EmojiEvents,
      isDark = isDark
    )
    StatRowItem(
      label = "Total Words Discovered",
      value = stats.totalWordsFound.toString(),
      icon = Icons.Default.Spellcheck,
      isDark = isDark
    )
    StatRowItem(
      label = "Spangrams Unlocked",
      value = stats.specialWordsFound.toString(),
      icon = Icons.Default.Star,
      isDark = isDark
    )
    StatRowItem(
      label = "Hints Used",
      value = stats.hintsUsed.toString(),
      icon = Icons.Default.Lightbulb,
      isDark = isDark
    )
    StatRowItem(
      label = "Best Solve Time",
      value = if (stats.bestTimeSeconds > 0) "${stats.bestTimeSeconds / 60}m ${stats.bestTimeSeconds % 60}s" else "--",
      icon = Icons.Default.Timer,
      isDark = isDark
    )

    Spacer(modifier = Modifier.height(24.dp))
  }
}

@Composable
private fun HighlightCard(
  title: String,
  value: String,
  icon: ImageVector,
  tint: Color,
  modifier: Modifier = Modifier,
) {
  val isDark = isSystemInDarkTheme()
  Box(
    modifier = modifier
      .shadow(2.dp, RoundedCornerShape(16.dp))
      .clip(RoundedCornerShape(16.dp))
      .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White)
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
      .padding(16.dp)
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
      Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
    }
  }
}

@Composable
private fun StatRowItem(
  label: String,
  value: String,
  icon: ImageVector,
  isDark: Boolean,
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .shadow(1.dp, RoundedCornerShape(14.dp))
      .clip(RoundedCornerShape(14.dp))
      .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White)
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
      .padding(horizontal = 16.dp, vertical = 14.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp)
        )
        Text(
          text = label,
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
      Text(
        text = value,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}
