package com.example.particles

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import com.example.ui.theme.GameColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class ParticleShape {
  STAR,
  SPARKLE,
  CONFETTI,
  CIRCLE,
}

data class Particle(
  var x: Float,
  var y: Float,
  var vx: Float,
  var vy: Float,
  var gravity: Float = 600f,
  var drag: Float = 0.98f,
  var rotation: Float = 0f,
  var vRot: Float = 0f,
  var size: Float,
  var color: Color,
  var alpha: Float = 1f,
  var maxLifeMs: Long,
  var ageMs: Long = 0L,
  var shape: ParticleShape = ParticleShape.STAR,
) {
  val isAlive: Boolean get() = ageMs < maxLifeMs

  fun update(deltaSec: Float) {
    ageMs += (deltaSec * 1000).toLong()
    vx *= drag
    vy += gravity * deltaSec
    x += vx * deltaSec
    y += vy * deltaSec
    rotation += vRot * deltaSec

    val lifeRatio = (ageMs.toFloat() / maxLifeMs).coerceIn(0f, 1f)
    // Fade out during last 30% of life
    alpha = if (lifeRatio > 0.7f) {
      (1f - (lifeRatio - 0.7f) / 0.3f).coerceIn(0f, 1f)
    } else {
      (lifeRatio / 0.15f).coerceIn(0f, 1f) // Quick fade-in
    }
  }
}

/**
 * Controller to trigger particle effects anywhere in the game.
 */
class ParticleController {
  val particles = mutableStateListOf<Particle>()

  fun triggerSmallMatch(origin: Offset, color: Color = GameColors.StarGold) {
    val count = Random.nextInt(8, 13)
    val list = ArrayList<Particle>(count)
    for (i in 0 until count) {
      val angle = Random.nextDouble(-PI * 0.9, -PI * 0.1).toFloat()
      val speed = Random.nextFloat() * 320f + 160f
      list.add(
        Particle(
          x = origin.x + Random.nextFloat() * 20f - 10f,
          y = origin.y + Random.nextFloat() * 20f - 10f,
          vx = cos(angle) * speed,
          vy = sin(angle) * speed,
          gravity = 750f,
          drag = 0.96f,
          rotation = Random.nextFloat() * 360f,
          vRot = Random.nextFloat() * 300f - 150f,
          size = Random.nextFloat() * 8f + 8f,
          color = if (Random.nextBoolean()) color else GameColors.SparkleWhite,
          maxLifeMs = Random.nextLong(600, 950),
          shape = if (Random.nextBoolean()) ParticleShape.STAR else ParticleShape.SPARKLE
        )
      )
    }
    particles.addAll(list)
  }

  fun triggerSpecialWord(points: List<Offset>) {
    val list = ArrayList<Particle>()
    for (pt in points) {
      val count = Random.nextInt(3, 6)
      for (i in 0 until count) {
        val angle = Random.nextFloat() * 2 * PI.toFloat()
        val speed = Random.nextFloat() * 380f + 120f
        list.add(
          Particle(
            x = pt.x + Random.nextFloat() * 24f - 12f,
            y = pt.y + Random.nextFloat() * 24f - 12f,
            vx = cos(angle) * speed,
            vy = sin(angle) * speed - 150f, // Upward bias
            gravity = 650f,
            drag = 0.97f,
            rotation = Random.nextFloat() * 360f,
            vRot = Random.nextFloat() * 400f - 200f,
            size = Random.nextFloat() * 12f + 10f,
            color = when (Random.nextInt(4)) {
              0 -> GameColors.StarGold
              1 -> GameColors.StarOrange
              2 -> GameColors.SparkleWhite
              else -> Color(0xFFFFE082)
            },
            maxLifeMs = Random.nextLong(900, 1400),
            shape = if (Random.nextFloat() < 0.65f) ParticleShape.STAR else ParticleShape.SPARKLE
          )
        )
      }
    }
    particles.addAll(list)
  }

  fun triggerLevelComplete(canvasSize: Size) {
    val count = Random.nextInt(45, 65)
    val list = ArrayList<Particle>(count)
    val width = canvasSize.width.coerceAtLeast(400f)
    for (i in 0 until count) {
      val x = Random.nextFloat() * width
      val y = -Random.nextFloat() * 150f // Spawn above top
      val vx = Random.nextFloat() * 160f - 80f
      val vy = Random.nextFloat() * 260f + 140f
      val colors = listOf(
        GameColors.StarGold,
        GameColors.StarOrange,
        GameColors.DiscoveredTealLight,
        Color(0xFF38BDF8),
        Color(0xFFA855F7),
        GameColors.SparkleWhite
      )
      val shape = when (Random.nextInt(3)) {
        0 -> ParticleShape.STAR
        1 -> ParticleShape.CONFETTI
        else -> ParticleShape.SPARKLE
      }
      list.add(
        Particle(
          x = x,
          y = y,
          vx = vx,
          vy = vy,
          gravity = 220f,
          drag = 0.99f,
          rotation = Random.nextFloat() * 360f,
          vRot = Random.nextFloat() * 240f - 120f,
          size = Random.nextFloat() * 14f + 10f,
          color = colors[Random.nextInt(colors.size)],
          maxLifeMs = Random.nextLong(2200, 3200),
          shape = shape
        )
      )
    }
    particles.addAll(list)
  }

  fun clear() {
    particles.clear()
  }
}

/**
 * High performance Canvas overlay that animates particles.
 */
@Composable
fun FallingStarCanvas(
  controller: ParticleController,
  modifier: Modifier = Modifier,
) {
  // Pre-created star paths for efficient rendering
  val starPath = remember { createStarPath(1f) }
  val sparklePath = remember { createSparklePath(1f) }

  // Game loop updates particles on every frame
  LaunchedEffect(controller) {
    var lastNano = 0L
    while (true) {
      withFrameNanos { frameTimeNanos ->
        if (lastNano != 0L && controller.particles.isNotEmpty()) {
          val deltaSec = ((frameTimeNanos - lastNano) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
          var i = 0
          while (i < controller.particles.size) {
            val p = controller.particles[i]
            p.update(deltaSec)
            if (!p.isAlive) {
              controller.particles.removeAt(i)
            } else {
              i++
            }
          }
        }
        lastNano = frameTimeNanos
      }
    }
  }

  Canvas(modifier = modifier.fillMaxSize()) {
    for (p in controller.particles) {
      if (p.alpha <= 0.01f) continue

      val drawColor = p.color.copy(alpha = (p.color.alpha * p.alpha).coerceIn(0f, 1f))

      when (p.shape) {
        ParticleShape.STAR -> {
          rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
            scale(scale = p.size, pivot = Offset(p.x, p.y)) {
              drawPath(
                path = starPath,
                color = drawColor,
                topLeft = Offset(p.x, p.y)
              )
            }
          }
        }
        ParticleShape.SPARKLE -> {
          rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
            scale(scale = p.size, pivot = Offset(p.x, p.y)) {
              drawPath(
                path = sparklePath,
                color = drawColor,
                topLeft = Offset(p.x, p.y)
              )
            }
          }
        }
        ParticleShape.CONFETTI -> {
          rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
            drawRect(
              color = drawColor,
              topLeft = Offset(p.x - p.size / 2, p.y - p.size / 4),
              size = Size(p.size, p.size * 0.6f)
            )
          }
        }
        ParticleShape.CIRCLE -> {
          drawCircle(
            color = drawColor,
            radius = p.size / 2,
            center = Offset(p.x, p.y)
          )
        }
      }
    }
  }
}

// DrawScope helpers for paths
private fun DrawScope.drawPath(path: Path, color: Color, topLeft: Offset) {
  val translatedPath = Path().apply {
    addPath(path, topLeft)
  }
  drawPath(translatedPath, color)
}

private fun createStarPath(radius: Float): Path {
  val path = Path()
  val points = 5
  val outerRadius = radius
  val innerRadius = radius * 0.45f
  val angleStep = PI / points

  var angle = -PI / 2
  path.moveTo((cos(angle) * outerRadius).toFloat(), (sin(angle) * outerRadius).toFloat())

  for (i in 1 until points * 2) {
    angle += angleStep
    val r = if (i % 2 == 1) innerRadius else outerRadius
    path.lineTo((cos(angle) * r).toFloat(), (sin(angle) * r).toFloat())
  }
  path.close()
  return path
}

private fun createSparklePath(radius: Float): Path {
  val path = Path()
  val outerRadius = radius
  val innerRadius = radius * 0.2f
  val points = 4
  val angleStep = PI / points

  var angle = -PI / 2
  path.moveTo((cos(angle) * outerRadius).toFloat(), (sin(angle) * outerRadius).toFloat())

  for (i in 1 until points * 2) {
    angle += angleStep
    val r = if (i % 2 == 1) innerRadius else outerRadius
    path.lineTo((cos(angle) * r).toFloat(), (sin(angle) * r).toFloat())
  }
  path.close()
  return path
}
