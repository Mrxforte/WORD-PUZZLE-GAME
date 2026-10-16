package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GameColors

@Composable
fun WordPreviewBar(
  currentWord: String,
  isDragging: Boolean,
  statusMessage: String?,
  modifier: Modifier = Modifier,
) {
  val isDark = isSystemInDarkTheme()

  Box(
    modifier = modifier
      .height(44.dp)
      .padding(horizontal = 16.dp),
    contentAlignment = Alignment.Center
  ) {
    // Show either the currently dragged letters or the temporary status toast
    AnimatedVisibility(
      visible = isDragging && currentWord.isNotEmpty(),
      enter = fadeIn() + scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
      exit = fadeOut() + scaleOut()
    ) {
      Row(
        modifier = Modifier
          .shadow(4.dp, RoundedCornerShape(22.dp))
          .clip(RoundedCornerShape(22.dp))
          .background(
            if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White
          )
          .border(
            width = 1.5.dp,
            color = if (isDark) GameColors.ActiveStrandDark else GameColors.ActiveStrandLight,
            shape = RoundedCornerShape(22.dp)
          )
          .padding(horizontal = 20.dp, vertical = 8.dp)
          .testTag("word_preview_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        for (char in currentWord) {
          Text(
            text = char.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }

    // Status Message if not dragging
    AnimatedVisibility(
      visible = !isDragging && statusMessage != null,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      if (statusMessage != null) {
        val isSpangramMsg = statusMessage.contains("SPANGRAM", ignoreCase = true)
        val isErrorMsg = statusMessage.contains("Already", ignoreCase = true)

        val pillBg = when {
          isSpangramMsg -> if (isDark) GameColors.SpangramGoldBgDark else GameColors.SpangramGoldBgLight
          isErrorMsg -> GameColors.InvalidRedBg
          else -> if (isDark) GameColors.DiscoveredTealBgDark else GameColors.DiscoveredTealBgLight
        }

        val pillBorder = when {
          isSpangramMsg -> if (isDark) GameColors.SpangramGoldDark else GameColors.SpangramGoldLight
          isErrorMsg -> GameColors.InvalidRed
          else -> if (isDark) GameColors.DiscoveredTealDark else GameColors.DiscoveredTealLight
        }

        val pillTextColor = when {
          isSpangramMsg -> if (isDark) Color(0xFFFEF3C7) else Color(0xFF78350F)
          isErrorMsg -> GameColors.InvalidRed
          else -> if (isDark) Color(0xFFCCFBF1) else Color(0xFF0F766E)
        }

        Box(
          modifier = Modifier
            .shadow(3.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(pillBg)
            .border(1.dp, pillBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
          Text(
            text = statusMessage,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = pillTextColor
          )
        }
      }
    }
  }
}
