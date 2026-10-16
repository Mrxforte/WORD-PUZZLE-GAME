package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameStats
import com.example.data.model.Screen
import com.example.ui.theme.GameColors

@Composable
fun HomeScreen(
  stats: GameStats,
  dailyDateString: String,
  onPlayLevel: (Int) -> Unit,
  onPlayDaily: () -> Unit,
  onNavigate: (Screen) -> Unit,
  modifier: Modifier = Modifier,
) {
  val isDark = isSystemInDarkTheme()

  Column(
    modifier = modifier
      .fillMaxSize()
      .statusBarsPadding()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top Bar with settings and how to play icons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Box(
          modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(if (isDark) GameColors.SpangramGoldDark else GameColors.SpangramGoldLight)
        )
        Text(
          text = "STRANDS",
          fontSize = 12.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 2.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        IconButton(
          onClick = { onNavigate(Screen.HowToPlay) },
          modifier = Modifier.testTag("nav_how_to_play")
        ) {
          Icon(
            imageVector = Icons.Default.HelpOutline,
            contentDescription = "How to play",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }
        IconButton(
          onClick = { onNavigate(Screen.Stats) },
          modifier = Modifier.testTag("nav_stats")
        ) {
          Icon(
            imageVector = Icons.Default.BarChart,
            contentDescription = "Statistics",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }
        IconButton(
          onClick = { onNavigate(Screen.Settings) },
          modifier = Modifier.testTag("nav_settings")
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }

    // Hero Title Card
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(4.dp, RoundedCornerShape(24.dp))
        .clip(RoundedCornerShape(24.dp))
        .background(
          Brush.verticalGradient(
            colors = if (isDark) {
              listOf(Color(0xFF1E293B), Color(0xFF0F172A))
            } else {
              listOf(Color(0xFFFFFFFF), Color(0xFFF8FAFC))
            }
          )
        )
        .border(
          width = 1.dp,
          color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
          shape = RoundedCornerShape(24.dp)
        )
        .padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Star badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
              if (isDark) GameColors.SpangramGoldBgDark else GameColors.SpangramGoldBgLight
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
              text = "PREMIUM WORD PUZZLE",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = if (isDark) Color(0xFFFEF3C7) else Color(0xFF78350F)
            )
          }
        }

        Text(
          text = "Strands",
          fontSize = 38.sp,
          fontWeight = FontWeight.Black,
          color = MaterialTheme.colorScheme.onSurface,
          letterSpacing = (-0.5).sp
        )

        Text(
          text = "Uncover theme words and weave the golden spangram in a sea of letters.",
          fontSize = 14.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Normal,
          modifier = Modifier.padding(horizontal = 8.dp),
          lineHeight = 20.sp
        )
      }
    }

    // Daily Puzzle Card
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(3.dp, RoundedCornerShape(20.dp))
        .clip(RoundedCornerShape(20.dp))
        .background(
          if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White
        )
        .border(
          width = 1.5.dp,
          color = if (isDark) GameColors.DiscoveredTealDark.copy(alpha = 0.45f) else GameColors.DiscoveredTealLight.copy(alpha = 0.35f),
          shape = RoundedCornerShape(20.dp)
        )
        .padding(20.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                  if (isDark) GameColors.DiscoveredTealBgDark else GameColors.DiscoveredTealBgLight
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = if (isDark) GameColors.DiscoveredTealDark else GameColors.DiscoveredTealLight,
                modifier = Modifier.size(22.dp)
              )
            }

            Column {
              Text(
                text = "DAILY PUZZLE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = if (isDark) GameColors.DiscoveredTealDark else GameColors.DiscoveredTealLight
              )
              Text(
                text = dailyDateString,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }

          if (stats.currentStreak > 0) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                  if (isDark) GameColors.SpangramGoldBgDark else GameColors.SpangramGoldBgLight
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = "🔥 ${stats.currentStreak}d Streak",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFFEF3C7) else Color(0xFF78350F)
              )
            }
          }
        }

        Button(
          onClick = onPlayDaily,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("play_daily_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isDark) GameColors.DiscoveredTealDark else GameColors.DiscoveredTealLight,
            contentColor = Color.White
          )
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
            Text("Play Today's Puzzle", fontSize = 15.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Campaign / Level Selection Card
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(3.dp, RoundedCornerShape(20.dp))
        .clip(RoundedCornerShape(20.dp))
        .background(
          if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White
        )
        .border(
          width = 1.dp,
          color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
          shape = RoundedCornerShape(20.dp)
        )
        .padding(20.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                  if (isDark) GameColors.SpangramGoldBgDark else GameColors.SpangramGoldBgLight
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.ViewModule,
                contentDescription = null,
                tint = if (isDark) GameColors.SpangramGoldDark else GameColors.SpangramGoldLight,
                modifier = Modifier.size(22.dp)
              )
            }

            Column {
              Text(
                text = "CAMPAIGN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = if (isDark) GameColors.SpangramGoldDark else GameColors.SpangramGoldLight
              )
              Text(
                text = "Level Journey (1-8)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }

          Text(
            text = "${stats.gamesWon}/8 Solved",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Button(
          onClick = { onNavigate(Screen.LevelSelect) },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("play_campaign_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isDark) GameColors.SpangramGoldDark else GameColors.SpangramGoldLight,
            contentColor = if (isDark) Color(0xFF451A03) else Color.White
          )
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
            Text("Select Level", fontSize = 15.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Mini Stats Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      QuickStatPill(
        label = "Words Found",
        value = stats.totalWordsFound.toString(),
        icon = Icons.Default.Star,
        modifier = Modifier.weight(1f)
      )
      QuickStatPill(
        label = "Win Rate",
        value = if (stats.gamesPlayed > 0) "${(stats.gamesWon * 100 / stats.gamesPlayed)}%" else "100%",
        icon = Icons.Default.BarChart,
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
private fun QuickStatPill(
  label: String,
  value: String,
  icon: ImageVector,
  modifier: Modifier = Modifier,
) {
  val isDark = isSystemInDarkTheme()
  Box(
    modifier = modifier
      .shadow(1.dp, RoundedCornerShape(14.dp))
      .clip(RoundedCornerShape(14.dp))
      .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White)
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
      .padding(14.dp)
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = value,
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}
