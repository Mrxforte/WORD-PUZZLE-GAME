package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BoardSelectionState
import com.example.data.model.LevelData
import com.example.data.model.ThemeWord
import com.example.ui.theme.GameColors
import kotlin.math.roundToInt

@Composable
fun LetterGrid(
  level: LevelData,
  selectionState: BoardSelectionState,
  foundThemeWords: Set<String>,
  hintedWord: ThemeWord?,
  onCellTouchStart: (Int, Int, Offset) -> Unit,
  onCellTouchMove: (Int, Int, Offset) -> Unit,
  onDragPointUpdate: (Offset) -> Unit,
  onCellTouchEnd: (Map<Pair<Int, Int>, Offset>) -> Unit,
  modifier: Modifier = Modifier,
) {
  val isDark = isSystemInDarkTheme()
  val density = LocalDensity.current

  // Map of cell coordinate to center Offset in local coordinates
  val cellCenters = remember { mutableStateMapOf<Pair<Int, Int>, Offset>() }

  // Shake animation for invalid word
  val shakeOffset = remember { Animatable(0f) }
  LaunchedEffect(selectionState.isInvalidFlash) {
    if (selectionState.isInvalidFlash) {
      shakeOffset.animateTo(
        targetValue = 1f,
        animationSpec = spring(
          dampingRatio = Spring.DampingRatioHighBouncy,
          stiffness = Spring.StiffnessMedium
        )
      )
      shakeOffset.snapTo(0f)
    }
  }

  // Pre-calculate found word mapping: cell -> ThemeWord
  val cellToFoundWord = remember(foundThemeWords, level) {
    val map = mutableMapOf<Pair<Int, Int>, ThemeWord>()
    for (tw in level.themeWords) {
      if (foundThemeWords.contains(tw.word)) {
        for (coord in tw.path) {
          map[coord] = tw
        }
      }
    }
    map
  }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 12.dp, vertical = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    // Dynamic cell sizing: fit inside available box constraints
    val availableWidth = constraints.maxWidth.toFloat()
    val availableHeight = constraints.maxHeight.toFloat()

    val numRows = level.rows
    val numCols = level.cols

    // Cell spacing
    val spacingPx = with(density) { 6.dp.toPx() }
    val totalSpacingX = spacingPx * (numCols + 1)
    val totalSpacingY = spacingPx * (numRows + 1)

    val maxCellWidth = (availableWidth - totalSpacingX) / numCols
    val maxCellHeight = (availableHeight - totalSpacingY) / numRows
    val cellSizePx = minOf(maxCellWidth, maxCellHeight).coerceAtLeast(40f)

    val totalGridWidthPx = numCols * cellSizePx + totalSpacingX
    val totalGridHeightPx = numRows * cellSizePx + totalSpacingY

    val totalGridWidthDp = with(density) { totalGridWidthPx.toDp() }
    val totalGridHeightDp = with(density) { totalGridHeightPx.toDp() }
    val cellSizeDp = with(density) { cellSizePx.toDp() }
    val spacingDp = with(density) { spacingPx.toDp() }

    // Hit-testing helper
    fun findCellAt(touchOffset: Offset): Pair<Int, Int>? {
      for (r in 0 until numRows) {
        for (c in 0 until numCols) {
          val center = cellCenters[r to c] ?: continue
          val radius = cellSizePx * 0.58f
          val dx = touchOffset.x - center.x
          val dy = touchOffset.y - center.y
          if (dx * dx + dy * dy <= radius * radius) {
            return r to c
          }
        }
      }
      return null
    }

    Box(
      modifier = Modifier
        .size(totalGridWidthDp, totalGridHeightDp)
        .offset {
          val shakeX = if (shakeOffset.value > 0f) {
            (kotlin.math.sin(shakeOffset.value * 25f) * 16f).roundToInt()
          } else 0
          IntOffset(shakeX, 0)
        }
        .pointerInput(level.id) {
          detectDragGestures(
            onDragStart = { offset ->
              val cell = findCellAt(offset)
              if (cell != null) {
                onCellTouchStart(cell.first, cell.second, offset)
              }
            },
            onDrag = { change, _ ->
              change.consume()
              val offset = change.position
              onDragPointUpdate(offset)
              val cell = findCellAt(offset)
              if (cell != null) {
                onCellTouchMove(cell.first, cell.second, offset)
              }
            },
            onDragEnd = {
              onCellTouchEnd(cellCenters)
            },
            onDragCancel = {
              onCellTouchEnd(cellCenters)
            }
          )
        }
    ) {
      // 1. Canvas layer for permanent discovered word strands and active connecting line
      Canvas(modifier = Modifier.fillMaxSize()) {
        val strandStrokeWidth = cellSizePx * 0.32f

        // Draw permanent discovered word strands
        for (tw in level.themeWords) {
          if (foundThemeWords.contains(tw.word) && tw.path.size >= 2) {
            val strandColor = if (tw.isSpecial) {
              if (isDark) GameColors.SpangramGoldDark else GameColors.SpangramGoldLight
            } else {
              if (isDark) GameColors.DiscoveredTealDark else GameColors.DiscoveredTealLight
            }

            val path = Path()
            val firstPt = cellCenters[tw.path.first()]
            if (firstPt != null) {
              path.moveTo(firstPt.x, firstPt.y)
              for (i in 1 until tw.path.size) {
                val pt = cellCenters[tw.path[i]] ?: continue
                path.lineTo(pt.x, pt.y)
              }
              // Soft outer glow for Spangram
              if (tw.isSpecial) {
                drawPath(
                  path = path,
                  color = strandColor.copy(alpha = 0.3f),
                  style = Stroke(
                    width = strandStrokeWidth * 1.55f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                  )
                )
              }
              drawPath(
                path = path,
                color = strandColor,
                style = Stroke(
                  width = strandStrokeWidth,
                  cap = StrokeCap.Round,
                  join = StrokeJoin.Round
                )
              )
            }
          }
        }

        // Draw active selection strand
        val selectedCells = selectionState.selectedCells
        if (selectedCells.isNotEmpty()) {
          val activeColor = if (selectionState.isInvalidFlash) {
            GameColors.InvalidRed
          } else {
            if (isDark) GameColors.ActiveStrandDark else GameColors.ActiveStrandLight
          }

          val path = Path()
          val firstPt = cellCenters[selectedCells.first()]
          if (firstPt != null) {
            path.moveTo(firstPt.x, firstPt.y)
            for (i in 1 until selectedCells.size) {
              val pt = cellCenters[selectedCells[i]] ?: continue
              path.lineTo(pt.x, pt.y)
            }

            // Draw line to current touch point if dragging
            if (selectionState.isDragging && selectionState.touchPoint != null) {
              path.lineTo(selectionState.touchPoint.x, selectionState.touchPoint.y)
            }

            // Outer soft glow
            drawPath(
              path = path,
              color = activeColor.copy(alpha = 0.28f),
              style = Stroke(
                width = strandStrokeWidth * 1.5f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
              )
            )

            // Main strand line
            drawPath(
              path = path,
              color = activeColor,
              style = Stroke(
                width = strandStrokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
              )
            )
          }
        }
      }

      // 2. Individual Letter Cells Grid
      for (r in 0 until numRows) {
        for (c in 0 until numCols) {
          val letter = level.grid[r][c]
          val coord = r to c

          val isDiscovered = cellToFoundWord.containsKey(coord)
          val foundWord = cellToFoundWord[coord]
          val isSelected = selectionState.selectedCells.contains(coord)
          val isHinted = hintedWord != null && hintedWord.path.contains(coord) && !isDiscovered

          // Precompute top-left & center
          val leftPx = spacingPx + c * (cellSizePx + spacingPx)
          val topPx = spacingPx + r * (cellSizePx + spacingPx)
          val leftDp = with(density) { leftPx.toDp() }
          val topDp = with(density) { topPx.toDp() }
          val center = Offset(leftPx + cellSizePx / 2f, topPx + cellSizePx / 2f)
          cellCenters[coord] = center

          // Cell component
          LetterCell(
            letter = letter,
            coord = coord,
            isSelected = isSelected,
            isDiscovered = isDiscovered,
            isSpecialDiscovered = foundWord?.isSpecial == true,
            isHinted = isHinted,
            isInvalid = selectionState.isInvalidFlash && isSelected,
            isDark = isDark,
            cellSizeDp = cellSizeDp,
            modifier = Modifier
              .offset(leftDp, topDp)
              .testTag("cell_${r}_${c}")
          )
        }
      }
    }
  }
}

@Composable
fun LetterCell(
  letter: Char,
  coord: Pair<Int, Int>,
  isSelected: Boolean,
  isDiscovered: Boolean,
  isSpecialDiscovered: Boolean,
  isHinted: Boolean,
  isInvalid: Boolean,
  isDark: Boolean,
  cellSizeDp: androidx.compose.ui.unit.Dp,
  modifier: Modifier = Modifier,
) {
  // Tile scale animation: 1.0 -> 1.08 on selection
  val targetScale = when {
    isInvalid -> 0.95f
    isSelected -> 1.09f
    else -> 1.0f
  }
  val scale by animateFloatAsState(
    targetValue = targetScale,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMedium
    ),
    label = "cell_scale"
  )

  // Background and border colors
  val targetBgColor = when {
    isInvalid -> GameColors.InvalidRedBg
    isSpecialDiscovered -> if (isDark) GameColors.SpangramGoldBgDark else GameColors.SpangramGoldBgLight
    isDiscovered -> if (isDark) GameColors.DiscoveredTealBgDark else GameColors.DiscoveredTealBgLight
    isSelected -> if (isDark) GameColors.ActiveStrandBgDark else GameColors.ActiveStrandBgLight
    else -> if (isDark) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
  }

  val targetBorderColor = when {
    isInvalid -> GameColors.InvalidRed
    isSpecialDiscovered -> if (isDark) GameColors.SpangramGoldDark else GameColors.SpangramGoldLight
    isDiscovered -> if (isDark) GameColors.DiscoveredTealDark else GameColors.DiscoveredTealLight
    isSelected -> if (isDark) GameColors.ActiveStrandDark else GameColors.ActiveStrandLight
    isHinted -> if (isDark) GameColors.SpangramGoldDark else GameColors.SpangramGoldLight
    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
  }

  val targetTextColor = when {
    isInvalid -> GameColors.InvalidRed
    isSpecialDiscovered -> if (isDark) Color(0xFFFEF3C7) else Color(0xFF78350F)
    isDiscovered -> if (isDark) Color(0xFFCCFBF1) else Color(0xFF0F766E)
    isSelected -> if (isDark) Color.White else GameColors.ActiveStrandLight
    else -> MaterialTheme.colorScheme.onSurface
  }

  val bgColor by animateColorAsState(targetBgColor, label = "bg_color")
  val borderColor by animateColorAsState(targetBorderColor, label = "border_color")
  val textColor by animateColorAsState(targetTextColor, label = "text_color")

  val cornerRadius = 14.dp
  val borderWidth = if (isSelected || isSpecialDiscovered || isDiscovered || isHinted) 2.dp else 1.dp

  Box(
    modifier = modifier
      .size(cellSizeDp)
      .scale(scale)
      .shadow(
        elevation = if (isSelected) 6.dp else if (isDiscovered) 2.dp else 1.dp,
        shape = RoundedCornerShape(cornerRadius),
        spotColor = if (isSpecialDiscovered) GameColors.StarGold.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.15f)
      )
      .clip(RoundedCornerShape(cornerRadius))
      .background(bgColor)
      .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius)),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = letter.toString(),
      fontSize = (cellSizeDp.value * 0.46f).sp,
      fontWeight = if (isSelected || isDiscovered || isSpecialDiscovered) FontWeight.ExtraBold else FontWeight.Bold,
      color = textColor
    )
  }
}
