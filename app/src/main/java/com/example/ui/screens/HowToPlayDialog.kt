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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GameColors

@Composable
fun HowToPlayScreen(
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
        .padding(bottom = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier.testTag("how_to_play_back_button")
      ) {
        Icon(
          imageVector = Icons.Default.ArrowBack,
          contentDescription = "Back",
          tint = MaterialTheme.colorScheme.onSurface
        )
      }
      Text(
        text = "HOW TO PLAY",
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    // Intro Card
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(2.dp, RoundedCornerShape(18.dp))
        .clip(RoundedCornerShape(18.dp))
        .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White)
        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
        .padding(18.dp)
    ) {
      Text(
        text = "Strands is a word search puzzle where every single letter on the board is used in exactly one theme word. Read the theme clue at the top to discover what words to find!",
        fontSize = 14.sp,
        lineHeight = 22.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    // Guide Step 1
    RuleCard(
      stepNumber = "1",
      title = "Drag to Connect Letters",
      description = "Touch and drag your finger across adjacent letters in any direction — horizontally, vertically, or diagonally. Drag backward to undo letters.",
      icon = Icons.Default.GridOn,
      accentColor = if (isDark) GameColors.ActiveStrandDark else GameColors.ActiveStrandLight,
      isDark = isDark
    )

    // Guide Step 2
    RuleCard(
      stepNumber = "2",
      title = "Find Theme Words",
      description = "All words relate to the puzzle's clue. When you discover a valid theme word, its tiles turn permanent teal with a star celebration!",
      icon = Icons.Default.AutoAwesome,
      accentColor = if (isDark) GameColors.DiscoveredTealDark else GameColors.DiscoveredTealLight,
      isDark = isDark
    )

    // Guide Step 3
    RuleCard(
      stepNumber = "3",
      title = "The Golden Spangram",
      description = "Every puzzle has one special overarching theme word called the Spangram. It turns golden yellow and spans from one border to an opposite border!",
      icon = Icons.Default.Star,
      accentColor = if (isDark) GameColors.SpangramGoldDark else GameColors.SpangramGoldLight,
      isDark = isDark
    )

    // Guide Step 4
    RuleCard(
      stepNumber = "4",
      title = "Bonus Words Earn Hints",
      description = "Find valid English words that aren't part of the theme to charge your Hint meter. Every 3 bonus words gives you +1 free hint to reveal letters!",
      icon = Icons.Default.Lightbulb,
      accentColor = Color(0xFFA855F7),
      isDark = isDark
    )

    Spacer(modifier = Modifier.height(12.dp))

    Button(
      onClick = onBack,
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("got_it_button"),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
      Text("Got It! Let's Play", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}

@Composable
private fun RuleCard(
  stepNumber: String,
  title: String,
  description: String,
  icon: ImageVector,
  accentColor: Color,
  isDark: Boolean,
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .shadow(1.dp, RoundedCornerShape(16.dp))
      .clip(RoundedCornerShape(16.dp))
      .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White)
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
      .padding(16.dp)
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(14.dp),
      verticalAlignment = Alignment.Top
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(accentColor.copy(alpha = 0.15f))
          .border(1.5.dp, accentColor, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(20.dp)
        )
      }

      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = "$stepNumber. $title",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = description,
          fontSize = 13.sp,
          lineHeight = 18.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
