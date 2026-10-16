package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.LevelsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var context: Context
  private lateinit var repository: LevelsRepository

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    repository = LevelsRepository(context)
  }

  @Test
  fun `read app_name from context`() {
    val appName = context.getString(R.string.app_name)
    assertEquals("Strands: Word Search", appName)
  }

  @Test
  fun `verify all levels exist and have valid theme words and grids`() {
    val levels = repository.allLevels
    assertEquals(8, levels.size)

    for (level in levels) {
      assertNotNull("Level ${level.id} title should not be null", level.title)
      assertTrue("Level ${level.id} should have rows > 0", level.rows > 0)
      assertTrue("Level ${level.id} should have cols > 0", level.cols > 0)
      assertEquals("Grid rows should match level.rows", level.rows, level.grid.size)

      // Spangram must exist
      val spangram = level.spangram
      assertNotNull("Level ${level.id} must have a special theme word (Spangram)", spangram)
      assertTrue("Spangram must be marked isSpecial", spangram!!.isSpecial)

      // Verify every word's path matches the grid letters and is contiguous
      for (tw in level.themeWords) {
        assertEquals("Path size must match word length for ${tw.word}", tw.word.length, tw.path.size)

        for (i in tw.path.indices) {
          val (r, c) = tw.path[i]
          assertTrue("Row $r out of bounds in level ${level.id}", r in 0 until level.rows)
          assertTrue("Col $c out of bounds in level ${level.id}", c in 0 until level.cols)
          val gridChar = level.grid[r][c]
          assertEquals("Letter at ($r,$c) in level ${level.id} must match word letter", tw.word[i], gridChar)

          // Check contiguous adjacency with previous letter
          if (i > 0) {
            val (prevR, prevC) = tw.path[i - 1]
            val dr = abs(r - prevR)
            val dc = abs(c - prevC)
            assertTrue("Path step for ${tw.word} must be an adjacent 8-way neighbor", dr <= 1 && dc <= 1)
          }
        }
      }
    }
  }

  @Test
  fun `verify level progress persistence`() {
    val initial = repository.getLevelProgress(1)
    repository.saveLevelProgress(initial.copy(completed = true, stars = 3, bestTimeSeconds = 65))
    val updated = repository.getLevelProgress(1)
    assertTrue("Level 1 should be marked completed", updated.completed)
    assertEquals(3, updated.stars)
    assertEquals(65, updated.bestTimeSeconds)
  }
}
