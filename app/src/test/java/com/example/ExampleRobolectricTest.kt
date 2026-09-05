package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.engine.GameEngine
import com.example.game.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Wildcraft", appName)
  }

  @Test
  fun `world generator is deterministic for given seed`() {
    val seed = 42891L
    val gen1 = WorldGen(seed)
    val gen2 = WorldGen(seed)

    for (x in -10..10) {
      for (y in -10..10) {
        val b1 = gen1.getBiomeAt(x, y)
        val b2 = gen2.getBiomeAt(x, y)
        assertEquals("Biomes must match at ($x, $y)", b1, b2)
      }
    }
  }

  @Test
  fun `game engine initializes with starter kit and survival state`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val engine = GameEngine(context)

    assertTrue("Engine should have starter inventory", engine.inventory.isNotEmpty())
    assertEquals(100f, engine.survival.health, 0.1f)
    assertTrue("Hunger should be high", engine.survival.hunger >= 80f)
    assertTrue("Thirst should be high", engine.survival.thirst >= 80f)
    assertNotNull(engine.player)
  }

  @Test
  fun `crafting recipes work as expected`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val engine = GameEngine(context)

    // Clear inventory and provide exact materials for Stone Axe (Wood x3, Stone x2, Fiber x2)
    engine.inventory.clear()
    engine.addItemToInventory(ItemType.WOOD, 3)
    engine.addItemToInventory(ItemType.STONE, 2)
    engine.addItemToInventory(ItemType.FIBER, 2)

    val axeRecipe = CraftingRecipes.allRecipes.first { it.id == "stone_axe" }
    val crafted = engine.craftRecipe(axeRecipe)

    assertTrue("Should successfully craft Stone Axe", crafted)
    val axeInInventory = engine.inventory.firstOrNull { it.item == ItemType.STONE_AXE }
    assertNotNull(axeInInventory)
    assertEquals(1, axeInInventory?.count)
  }
}
