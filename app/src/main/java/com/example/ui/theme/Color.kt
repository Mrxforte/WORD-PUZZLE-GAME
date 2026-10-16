package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Light mode palette - Warm Ivory & Rich Teal
val LightBackground = Color(0xFFF9F7F2)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1ECE4)
val LightPrimary = Color(0xFF0D9488) // Rich Teal
val LightOnPrimary = Color.White
val LightSecondary = Color(0xFFD97706) // Golden Amber (Spangram)
val LightOnSecondary = Color.White
val LightTertiary = Color(0xFF0284C7) // Selection Strand Blue
val LightOutline = Color(0xFFE5DFD5)
val LightOnBackground = Color(0xFF1E293B)
val LightOnSurface = Color(0xFF1E293B)
val LightOnSurfaceVariant = Color(0xFF64748B)

// Dark mode palette - Deep Charcoal & Luminous Accents
val DarkBackground = Color(0xFF121417)
val DarkSurface = Color(0xFF1C1F26)
val DarkSurfaceVariant = Color(0xFF272C36)
val DarkPrimary = Color(0xFF2DD4BF) // Bright Teal
val DarkOnPrimary = Color(0xFF042F2E)
val DarkSecondary = Color(0xFFFBBF24) // Bright Gold (Spangram)
val DarkOnSecondary = Color(0xFF451A03)
val DarkTertiary = Color(0xFF38BDF8) // Bright Strand Blue
val DarkOutline = Color(0xFF333A48)
val DarkOnBackground = Color(0xFFF8FAFC)
val DarkOnSurface = Color(0xFFF8FAFC)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)

// Dedicated Game Colors
object GameColors {
  val DiscoveredTealLight = Color(0xFF0D9488)
  val DiscoveredTealBgLight = Color(0xFFCCFBF1)
  val DiscoveredTealDark = Color(0xFF2DD4BF)
  val DiscoveredTealBgDark = Color(0xFF134E4A)

  val SpangramGoldLight = Color(0xFFD97706)
  val SpangramGoldBgLight = Color(0xFFFEF3C7)
  val SpangramGoldDark = Color(0xFFFBBF24)
  val SpangramGoldBgDark = Color(0xFF78350F)

  val ActiveStrandLight = Color(0xFF0284C7)
  val ActiveStrandBgLight = Color(0xFFE0F2FE)
  val ActiveStrandDark = Color(0xFF38BDF8)
  val ActiveStrandBgDark = Color(0xFF1E3A8A)

  val InvalidRed = Color(0xFFEF4444)
  val InvalidRedBg = Color(0xFFFEE2E2)

  val StarGold = Color(0xFFFFD700)
  val StarOrange = Color(0xFFF59E0B)
  val SparkleWhite = Color(0xFFFFFFFF)
}
