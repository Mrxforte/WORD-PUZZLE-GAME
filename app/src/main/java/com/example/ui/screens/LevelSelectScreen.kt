package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LevelsRepository
import com.example.data.model.LevelData
import com.example.data.model.LevelProgress
import com.example.ui.theme.GameColors

@Composable
fun LevelSelectScreen(
  repository: LevelsRepository,
  onSelectLevel: (Int) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isDark = isSystemInDarkTheme()
  val allLevels = repository.allLevels

  // Read progress for all levels
  val progressList = remember(allLevels) {
    allLevels.map { level ->
      repository.getLevelProgress(level.id)
    }
  }

  val totalStars = progressList.sumOf { it.stars }
  val maxPossibleStars = allLevels.size * 3

  BackHandler { onBack() }

  Column(
    modifier = modifier
      .fillMaxSize()
      .statusBarsPadding()
      .padding(horizontal = 20.dp, vertical = 12.dp)
  ) {
    // Header Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier.testTag("level_select_back_button")
        ) {
          Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }
        Text(
          text = "LEVELS",
          fontSize = 20.sp,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      // Total Stars badge
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(16.dp))
          .background(if (isDark) GameColors.SpangramGoldBgDark else GameColors.SpangramGoldBgLight)
          .border(
            1.dp,
            if (isDark) GameColors.SpangramGoldDark else GameColors.SpangramGoldLight,
            RoundedCornerShape(16.dp)
          )
          .padding(horizontal = 12.dp, vertical = 6.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = if (isDark) GameColors.SpangramGoldDark else GameColors.SpangramGoldLight,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = "$totalStars / $maxPossibleStars",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color(0xFFFEF3C7) else Color(0xFF78350F)
          )
        }
      }
    }

    // List of Level Cards
    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(14.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(allLevels) { level ->
        val progress = progressList.getOrNull(level.id - 1) ?: LevelProgress(level.id)
        val isUnlocked = repository.isLevelUnlocked(level.id)
        val isCurrent = isUnlocked && !progress.completed

        LevelCard(
          level = level,
          progress = progress,
          isUnlocked = isUnlocked,
          isCurrent = isCurrent,
          isDark = isDark,
          onClick = {
            if (isUnlocked) {
              onSelectLevel(level.id)
            }
          }
        )
      }
      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Composable
private fun LevelCard(
  level: LevelData,
  progress: LevelProgress,
  isUnlocked: Boolean,
  isCurrent: Boolean,
  isDark: Boolean,
  onClick: () -> Unit,
) {
  // Gentle pulse animation for current active level
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = if (isCurrent) 1.02f else 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  val cardBg = when {
    !isUnlocked -> if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color(0xFFF1F5F9)
    isCurrent -> if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White
    else -> if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White
  }

  val borderColor = when {
    !isUnlocked -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    isCurrent -> if (isDark) GameColors.DiscoveredTealDark else GameColors.DiscoveredTealLight
    progress.completed -> if (isDark) GameColors.SpangramGoldDark.copy(alpha = 0.6f) else GameColors.SpangramGoldLight.copy(alpha = 0.6f)
    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .scale(pulseScale)
      .shadow(if (isCurrent) 4.dp else if (isUnlocked) 2.dp else 0.dp, RoundedCornerShape(18.dp))
      .clip(RoundedCornerShape(18.dp))
      .background(cardBg)
      .border(if (isCurrent) 2.dp else 1.dp, borderColor, RoundedCornerShape(18.dp))
      .clickable(enabled = isUnlocked, onClick = onClick)
      .padding(16.dp)
      .testTag("level_card_${level.id}")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Level number badge or lock icon
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(
              when {
                !isUnlocked -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                progress.completed -> if (isDark) GameColors.SpangramGoldBgDark else GameColors.SpangramGoldBgLight
                isCurrent -> if (isDark) GameColors.DiscoveredTealBgDark else GameColors.DiscoveredTealBgLight
                else -> MaterialTheme.colorScheme.surface
              }
            ),
          contentAlignment = Alignment.Center
        ) {
          if (!isUnlocked) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "Locked",
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.size(20.dp)
            )
          } else if (progress.completed) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = "Completed",
              tint = if (isDark) GameColors.SpangramGoldDark else GameColors.SpangramGoldLight,
              modifier = Modifier.size(24.dp)
            )
          } else {
            Text(
              text = level.id.toString(),
              fontSize = 18.sp,
              fontWeight = FontWeight.ExtraBold,
              color = if (isDark) GameColors.DiscoveredTealDark else GameColors.DiscoveredTealLight
            )
          }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Level ${level.id}: ${level.title}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
          )
          Text(
            text = "\"${level.themeClue}\"",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Right side: Stars or Play Icon
      if (progress.completed) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
          for (i in 1..3) {
            Icon(
              imageVector = Icons.Default.Star,
              contentDescription = null,
              tint = if (i <= progress.stars) GameColors.StarGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      } else if (isUnlocked) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (isDark) GameColors.DiscoveredTealDark else GameColors.DiscoveredTealLight),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}
