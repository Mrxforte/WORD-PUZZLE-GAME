package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.LevelData
import com.example.ui.theme.GameColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VictoryDialog(
  level: LevelData,
  elapsedSeconds: Int,
  stars: Int,
  victoryStep: Int,
  onNextLevel: () -> Unit,
  onReplay: () -> Unit,
  onLevelSelect: () -> Unit,
) {
  val isDark = isSystemInDarkTheme()

  // Format time as MM:SS
  val minutes = elapsedSeconds / 60
  val seconds = elapsedSeconds % 60
  val timeFormatted = String.format("%02d:%02d", minutes, seconds)

  val scaleAnim by animateFloatAsState(
    targetValue = if (victoryStep >= 3) 1f else 0.82f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMedium
    ),
    label = "victory_scale"
  )

  Dialog(
    onDismissRequest = {},
    properties = DialogProperties(
      dismissOnBackPress = false,
      dismissOnClickOutside = false,
      usePlatformDefaultWidth = false
    )
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      AnimatedVisibility(
        visible = victoryStep >= 3,
        enter = fadeIn() + scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy))
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .scale(scaleAnim)
            .shadow(16.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(
              if (isDark) MaterialTheme.colorScheme.surface else Color.White
            )
            .border(
              width = 1.5.dp,
              color = if (isDark) GameColors.SpangramGoldDark.copy(alpha = 0.5f) else GameColors.SpangramGoldLight.copy(alpha = 0.35f),
              shape = RoundedCornerShape(28.dp)
            )
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            // Trophy emblem with glow circle
            Box(
              modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                  if (isDark) GameColors.SpangramGoldBgDark else GameColors.SpangramGoldBgLight
                )
                .border(
                  2.dp,
                  if (isDark) GameColors.SpangramGoldDark else GameColors.SpangramGoldLight,
                  CircleShape
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Victory Trophy",
                tint = if (isDark) GameColors.SpangramGoldDark else GameColors.SpangramGoldLight,
                modifier = Modifier.size(42.dp)
              )
            }

            // Headings
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "PUZZLE SOLVED!",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Level ${level.id} • ${level.title}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            // Stars display
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              for (i in 1..3) {
                Icon(
                  imageVector = Icons.Default.Star,
                  contentDescription = "Star $i",
                  tint = if (i <= stars) GameColors.StarGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                  modifier = Modifier.size(32.dp)
                )
              }
            }

            // Stats row (Time taken)
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(
                  if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF1F5F9)
                )
                .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
              Text(
                text = "Time: $timeFormatted",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            // Discovered Theme Words summary chips
            Text(
              text = "Theme Words Found:",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.5.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
              horizontalArrangement = Arrangement.Center,
              verticalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              for (tw in level.themeWords) {
                val chipBg = if (tw.isSpecial) {
                  if (isDark) GameColors.SpangramGoldBgDark else GameColors.SpangramGoldBgLight
                } else {
                  if (isDark) GameColors.DiscoveredTealBgDark else GameColors.DiscoveredTealBgLight
                }
                val chipText = if (tw.isSpecial) {
                  if (isDark) Color(0xFFFEF3C7) else Color(0xFF78350F)
                } else {
                  if (isDark) Color(0xFFCCFBF1) else Color(0xFF0F766E)
                }

                Box(
                  modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(chipBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = if (tw.isSpecial) "★ ${tw.word}" else tw.word,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = chipText
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons
            Button(
              onClick = onNextLevel,
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("victory_continue_button"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isDark) GameColors.DiscoveredTealDark else GameColors.DiscoveredTealLight,
                contentColor = Color.White
              )
            ) {
              Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("Next Level", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Icon(
                  imageVector = Icons.Default.ArrowForward,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OutlinedButton(
                onClick = onReplay,
                modifier = Modifier
                  .weight(1f)
                  .height(44.dp)
                  .testTag("victory_replay_button"),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(6.dp))
                Text("Replay", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
              }

              OutlinedButton(
                onClick = onLevelSelect,
                modifier = Modifier
                  .weight(1f)
                  .height(44.dp)
                  .testTag("victory_levels_button"),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(6.dp))
                Text("Levels", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }
      }
    }
  }
}
