package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LevelData
import com.example.ui.theme.GameColors

@Composable
fun ThemeClueHeader(
  level: LevelData,
  foundCount: Int,
  totalWords: Int,
  hintsAvailable: Int,
  bonusCount: Int, // 0..3
  hasSpangramFound: Boolean,
  onBackClick: () -> Unit,
  onHintClick: () -> Unit,
  onPauseClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isDark = isSystemInDarkTheme()

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // Top control row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onBackClick,
        modifier = Modifier
          .size(42.dp)
          .testTag("back_button")
      ) {
        Icon(
          imageVector = Icons.Default.ArrowBack,
          contentDescription = "Back",
          tint = MaterialTheme.colorScheme.onSurface
        )
      }

      // Title & level number
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = level.title.uppercase(),
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.2.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "LEVEL ${level.id}",
          fontSize = 16.sp,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Hint button with bonus progress ring
        Box(contentAlignment = Alignment.Center) {
          // Circular progress of bonus words (0..3)
          val animatedProgress by animateFloatAsState(
            targetValue = (bonusCount / 3f).coerceIn(0f, 1f),
            label = "bonus_progress"
          )
          CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.size(42.dp),
            color = if (isDark) GameColors.SpangramGoldDark else GameColors.SpangramGoldLight,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
            strokeWidth = 2.5.dp,
            strokeCap = StrokeCap.Round
          )

          IconButton(
            onClick = onHintClick,
            modifier = Modifier
              .size(38.dp)
              .testTag("hint_button")
          ) {
            Icon(
              imageVector = Icons.Default.Lightbulb,
              contentDescription = "Use Hint ($hintsAvailable available)",
              tint = if (hintsAvailable > 0) {
                if (isDark) GameColors.SpangramGoldDark else GameColors.SpangramGoldLight
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
              },
              modifier = Modifier.size(20.dp)
            )
          }

          // Badge with hint count
          if (hintsAvailable > 0) {
            Box(
              modifier = Modifier
                .align(Alignment.TopEnd)
                .size(16.dp)
                .clip(CircleShape)
                .background(if (isDark) GameColors.SpangramGoldDark else GameColors.SpangramGoldLight),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = hintsAvailable.toString(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFF451A03) else Color.White
              )
            }
          }
        }

        // Pause button
        IconButton(
          onClick = onPauseClick,
          modifier = Modifier
            .size(42.dp)
            .testTag("pause_button")
        ) {
          Icon(
            imageVector = Icons.Default.Pause,
            contentDescription = "Pause Game",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }

    // Theme Clue Box
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(2.dp, RoundedCornerShape(16.dp))
        .clip(RoundedCornerShape(16.dp))
        .background(
          if (isDark) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
        .border(
          width = 1.dp,
          color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
          shape = RoundedCornerShape(16.dp)
        )
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "THEME:",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = if (isDark) GameColors.DiscoveredTealDark else GameColors.DiscoveredTealLight
            )
            Text(
              text = "\"${level.themeClue}\"",
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          if (hasSpangramFound) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                  if (isDark) GameColors.SpangramGoldBgDark else GameColors.SpangramGoldBgLight
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "SPANGRAM ★",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFFEF3C7) else Color(0xFF78350F)
              )
            }
          }
        }

        // Progress bar for found words
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val progressRatio = (foundCount.toFloat() / totalWords.coerceAtLeast(1)).coerceIn(0f, 1f)
          val animatedWordsProgress by animateFloatAsState(progressRatio, label = "theme_progress")

          LinearProgressIndicator(
            progress = { animatedWordsProgress },
            modifier = Modifier
              .weight(1f)
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = if (isDark) GameColors.DiscoveredTealDark else GameColors.DiscoveredTealLight,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
          )

          Text(
            text = "$foundCount of $totalWords words",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}
