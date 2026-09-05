package com.example.game.model

enum class ItemCategory {
  MATERIAL,
  TOOL,
  WEAPON,
  CONSUMABLE,
  STRUCTURE,
  ARMOR
}

enum class ItemType(
  val displayName: String,
  val category: ItemCategory,
  val description: String,
  val maxStack: Int = 64,
  val toolType: ToolType = ToolType.NONE,
  val attackDamage: Int = 0,
  val armorValue: Int = 0,
  val hungerRestore: Int = 0,
  val thirstRestore: Int = 0,
  val healthRestore: Int = 0,
  val warmthRestore: Int = 0,
  val staminaRestore: Int = 0
) {
  // Raw materials
  WOOD("Wood Logs", ItemCategory.MATERIAL, "Sturdy timber harvested from felled trees.", maxStack = 99),
  STONE("Rough Stone", ItemCategory.MATERIAL, "Solid stone mined from boulders.", maxStack = 99),
  FLINT("Sharp Flint", ItemCategory.MATERIAL, "Used for making sparks and sharp stone edges.", maxStack = 64),
  FIBER("Plant Fiber", ItemCategory.MATERIAL, "Tough wild plant fibers for crafting ropes and cloth.", maxStack = 99),
  IRON_ORE("Iron Ore", ItemCategory.MATERIAL, "Heavy mineral ore rich in iron.", maxStack = 64),
  COAL("Coal Chunk", ItemCategory.MATERIAL, "Combustible fuel for smelting and torches.", maxStack = 64),
  GOLD_NUGGET("Gold Nugget", ItemCategory.MATERIAL, "Precious gleaming metal from deep veins.", maxStack = 64),
  RESIN("Pine Resin", ItemCategory.MATERIAL, "Sticky flammable tree sap.", maxStack = 64),
  LEAVES("Green Leaves", ItemCategory.MATERIAL, "Foliage used for bedding and kindling.", maxStack = 99),
  SAPLING("Tree Sapling", ItemCategory.MATERIAL, "Can be planted to grow new trees.", maxStack = 32),
  SEEDS("Wild Seeds", ItemCategory.MATERIAL, "Plant in tilled soil to grow crops.", maxStack = 64),
  RAW_MEAT("Raw Meat", ItemCategory.MATERIAL, "Freshly hunted game. Better cooked over fire.", maxStack = 32, hungerRestore = 10, healthRestore = -5),
  LEATHER("Animal Hide", ItemCategory.MATERIAL, "Tough pelts for crafting protective gear.", maxStack = 64),
  BONE("Beast Bone", ItemCategory.MATERIAL, "Sturdy skeletal material for tools and weapons.", maxStack = 64),

  // Consumables
  BERRIES("Wild Berries", ItemCategory.CONSUMABLE, "Sweet, juicy berries gathered from bushes.", maxStack = 64, hungerRestore = 15, thirstRestore = 12, healthRestore = 4),
  COCONUT("Fresh Coconut", ItemCategory.CONSUMABLE, "Rich in sweet milk and nourishing meat.", maxStack = 32, hungerRestore = 20, thirstRestore = 25, healthRestore = 5),
  CACTUS_FLESH("Cactus Flesh", ItemCategory.CONSUMABLE, "Water-rich desert pulp.", maxStack = 32, hungerRestore = 10, thirstRestore = 30),
  MUSHROOM("Forest Mushroom", ItemCategory.CONSUMABLE, "Edible woodland fungi.", maxStack = 64, hungerRestore = 14, healthRestore = 3),
  COOKED_MEAT("Roasted Meat", ItemCategory.CONSUMABLE, "Hearty meat cooked over a campfire.", maxStack = 32, hungerRestore = 50, healthRestore = 25, warmthRestore = 10),
  CLEAN_WATER("Clean Canteen", ItemCategory.CONSUMABLE, "Pure filtered drinking water.", maxStack = 10, thirstRestore = 60),
  HEALING_SALVE("Herbal Salve", ItemCategory.CONSUMABLE, "Soothing ointment that mends injuries.", maxStack = 16, healthRestore = 40),
  HERBAL_TEA("Warm Herbal Tea", ItemCategory.CONSUMABLE, "Invigorating brew that soothes chills.", maxStack = 16, thirstRestore = 35, warmthRestore = 30, healthRestore = 15, staminaRestore = 30),
  HERBS("Medicinal Herbs", ItemCategory.MATERIAL, "Wild herbs with curative properties.", maxStack = 64),

  // Tools & Weapons
  WOODEN_CLUB("Wooden Club", ItemCategory.WEAPON, "A hefty carved branch for basic self-defense.", maxStack = 1, toolType = ToolType.WEAPON, attackDamage = 6),
  STONE_AXE("Stone Axe", ItemCategory.TOOL, "Essential tool for rapidly chopping trees.", maxStack = 1, toolType = ToolType.AXE, attackDamage = 10),
  STONE_PICKAXE("Stone Pickaxe", ItemCategory.TOOL, "Sturdy pick for breaking rocks and mining veins.", maxStack = 1, toolType = ToolType.PICKAXE, attackDamage = 9),
  IRON_SWORD("Iron Broadsword", ItemCategory.WEAPON, "Forged blade dealing severe slashing damage.", maxStack = 1, toolType = ToolType.WEAPON, attackDamage = 22),
  IRON_AXE("Iron Axe", ItemCategory.TOOL, "Superior axe that cuts through trees in moments.", maxStack = 1, toolType = ToolType.AXE, attackDamage = 16),
  IRON_PICKAXE("Iron Pickaxe", ItemCategory.TOOL, "Durable pickaxe capable of mining rare minerals.", maxStack = 1, toolType = ToolType.PICKAXE, attackDamage = 15),
  BONE_SPEAR("Bone Spear", ItemCategory.WEAPON, "Long-reach piercing weapon made from monster bones.", maxStack = 1, toolType = ToolType.WEAPON, attackDamage = 14),
  HUNTER_BOW("Hunter Bow", ItemCategory.WEAPON, "Ranged bow crafted from flexible wood and fiber.", maxStack = 1, toolType = ToolType.WEAPON, attackDamage = 18),
  WOODEN_ARROW("Flint Arrows", ItemCategory.MATERIAL, "Ammunition for bows.", maxStack = 99),
  TORCH("Lit Torch", ItemCategory.TOOL, "Provides light in the dark and wards off cold.", maxStack = 16, warmthRestore = 15),

  // Armor & Gear
  LEATHER_TUNIC("Leather Tunic", ItemCategory.ARMOR, "Protective hide armor that softens monster blows.", maxStack = 1, armorValue = 18),
  FUR_CLOAK("Fur Cloak", ItemCategory.ARMOR, "Heavy insulated cloak that protects against extreme cold.", maxStack = 1, armorValue = 12, warmthRestore = 45),
  IRON_ARMOR("Iron Plate Mail", ItemCategory.ARMOR, "Heavy metal armor offering maximum physical protection.", maxStack = 1, armorValue = 40),

  // Placeable Structures
  CAMPFIRE("Campfire", ItemCategory.STRUCTURE, "Placeable heat and light source. Used to cook meat.", maxStack = 10),
  STORAGE_CHEST("Storage Chest", ItemCategory.STRUCTURE, "Placeable wooden chest with 16 storage slots.", maxStack = 5),
  WOODEN_WALL("Wooden Palisade", ItemCategory.STRUCTURE, "Defensive wooden wall to build shelters and keep monsters out.", maxStack = 30),
  WOODEN_DOOR("Wooden Gate", ItemCategory.STRUCTURE, "A walkable gateway for your shelter.", maxStack = 10),
  BEDROLL("Cozy Bedroll", ItemCategory.STRUCTURE, "Placeable bedding to sleep through the dangerous night.", maxStack = 5),
  FARM_PLOT("Garden Plot", ItemCategory.STRUCTURE, "Tilled soil plot to plant wild seeds for food.", maxStack = 20)
}

data class ItemStack(
  val item: ItemType,
  var count: Int
)

data class RecipeIngredient(
  val item: ItemType,
  val amount: Int
)

data class CraftingRecipe(
  val id: String,
  val result: ItemType,
  val resultCount: Int = 1,
  val category: ItemCategory,
  val ingredients: List<RecipeIngredient>,
  val description: String
)

object CraftingRecipes {
  val allRecipes = listOf(
    // Tools & Weapons
    CraftingRecipe(
      id = "stone_axe",
      result = ItemType.STONE_AXE,
      category = ItemCategory.TOOL,
      ingredients = listOf(
        RecipeIngredient(ItemType.WOOD, 3),
        RecipeIngredient(ItemType.STONE, 2),
        RecipeIngredient(ItemType.FIBER, 2)
      ),
      description = "Fast tree chopping tool."
    ),
    CraftingRecipe(
      id = "stone_pickaxe",
      result = ItemType.STONE_PICKAXE,
      category = ItemCategory.TOOL,
      ingredients = listOf(
        RecipeIngredient(ItemType.WOOD, 3),
        RecipeIngredient(ItemType.STONE, 3),
        RecipeIngredient(ItemType.FIBER, 2)
      ),
      description = "Efficient rock & ore mining pick."
    ),
    CraftingRecipe(
      id = "wooden_club",
      result = ItemType.WOODEN_CLUB,
      category = ItemCategory.WEAPON,
      ingredients = listOf(
        RecipeIngredient(ItemType.WOOD, 4)
      ),
      description = "Simple bludgeon weapon."
    ),
    CraftingRecipe(
      id = "bone_spear",
      result = ItemType.BONE_SPEAR,
      category = ItemCategory.WEAPON,
      ingredients = listOf(
        RecipeIngredient(ItemType.WOOD, 3),
        RecipeIngredient(ItemType.BONE, 2),
        RecipeIngredient(ItemType.FIBER, 3)
      ),
      description = "Piercing weapon with extended reach."
    ),
    CraftingRecipe(
      id = "hunter_bow",
      result = ItemType.HUNTER_BOW,
      category = ItemCategory.WEAPON,
      ingredients = listOf(
        RecipeIngredient(ItemType.WOOD, 5),
        RecipeIngredient(ItemType.FIBER, 5)
      ),
      description = "Ranged weapon for hunting at a distance."
    ),
    CraftingRecipe(
      id = "flint_arrows",
      result = ItemType.WOODEN_ARROW,
      resultCount = 8,
      category = ItemCategory.WEAPON,
      ingredients = listOf(
        RecipeIngredient(ItemType.WOOD, 2),
        RecipeIngredient(ItemType.FLINT, 2),
        RecipeIngredient(ItemType.LEAVES, 2)
      ),
      description = "Batch of 8 sharp hunting arrows."
    ),
    CraftingRecipe(
      id = "iron_sword",
      result = ItemType.IRON_SWORD,
      category = ItemCategory.WEAPON,
      ingredients = listOf(
        RecipeIngredient(ItemType.WOOD, 2),
        RecipeIngredient(ItemType.IRON_ORE, 5),
        RecipeIngredient(ItemType.LEATHER, 2)
      ),
      description = "Deadly steel blade."
    ),
    CraftingRecipe(
      id = "iron_axe",
      result = ItemType.IRON_AXE,
      category = ItemCategory.TOOL,
      ingredients = listOf(
        RecipeIngredient(ItemType.WOOD, 3),
        RecipeIngredient(ItemType.IRON_ORE, 4)
      ),
      description = "Chops trees in 1-2 strikes."
    ),
    CraftingRecipe(
      id = "iron_pickaxe",
      result = ItemType.IRON_PICKAXE,
      category = ItemCategory.TOOL,
      ingredients = listOf(
        RecipeIngredient(ItemType.WOOD, 3),
        RecipeIngredient(ItemType.IRON_ORE, 4)
      ),
      description = "Mines rocks and veins with ease."
    ),
    CraftingRecipe(
      id = "torch",
      result = ItemType.TORCH,
      resultCount = 2,
      category = ItemCategory.TOOL,
      ingredients = listOf(
        RecipeIngredient(ItemType.WOOD, 2),
        RecipeIngredient(ItemType.COAL, 1)
      ),
      description = "Lights up darkness and provides warmth."
    ),

    // Survival & Consumables
    CraftingRecipe(
      id = "cooked_meat",
      result = ItemType.COOKED_MEAT,
      category = ItemCategory.CONSUMABLE,
      ingredients = listOf(
        RecipeIngredient(ItemType.RAW_MEAT, 1),
        RecipeIngredient(ItemType.WOOD, 1)
      ),
      description = "Savory roasted meat, restores 50 hunger."
    ),
    CraftingRecipe(
      id = "healing_salve",
      result = ItemType.HEALING_SALVE,
      category = ItemCategory.CONSUMABLE,
      ingredients = listOf(
        RecipeIngredient(ItemType.HERBS, 2),
        RecipeIngredient(ItemType.RESIN, 1)
      ),
      description = "Heals 40 HP and eases suffering."
    ),
    CraftingRecipe(
      id = "herbal_tea",
      result = ItemType.HERBAL_TEA,
      category = ItemCategory.CONSUMABLE,
      ingredients = listOf(
        RecipeIngredient(ItemType.HERBS, 2),
        RecipeIngredient(ItemType.BERRIES, 2)
      ),
      description = "Warm soothing tea restoring warmth and thirst."
    ),
    CraftingRecipe(
      id = "clean_water",
      result = ItemType.CLEAN_WATER,
      category = ItemCategory.CONSUMABLE,
      ingredients = listOf(
        RecipeIngredient(ItemType.LEATHER, 1),
        RecipeIngredient(ItemType.FIBER, 2)
      ),
      description = "Craft a reusable canteen filled with clean water."
    ),

    // Structures & Shelters
    CraftingRecipe(
      id = "campfire",
      result = ItemType.CAMPFIRE,
      category = ItemCategory.STRUCTURE,
      ingredients = listOf(
        RecipeIngredient(ItemType.WOOD, 5),
        RecipeIngredient(ItemType.STONE, 4)
      ),
      description = "Placeable campfire for warmth, light, and safety."
    ),
    CraftingRecipe(
      id = "storage_chest",
      result = ItemType.STORAGE_CHEST,
      category = ItemCategory.STRUCTURE,
      ingredients = listOf(
        RecipeIngredient(ItemType.WOOD, 8),
        RecipeIngredient(ItemType.STONE, 2)
      ),
      description = "Store up to 16 item stacks securely."
    ),
    CraftingRecipe(
      id = "wooden_wall",
      result = ItemType.WOODEN_WALL,
      resultCount = 2,
      category = ItemCategory.STRUCTURE,
      ingredients = listOf(
        RecipeIngredient(ItemType.WOOD, 4)
      ),
      description = "Sturdy wooden palisade to wall off territory."
    ),
    CraftingRecipe(
      id = "wooden_door",
      result = ItemType.WOODEN_DOOR,
      category = ItemCategory.STRUCTURE,
      ingredients = listOf(
        RecipeIngredient(ItemType.WOOD, 6),
        RecipeIngredient(ItemType.FIBER, 2)
      ),
      description = "Passable door for player base."
    ),
    CraftingRecipe(
      id = "bedroll",
      result = ItemType.BEDROLL,
      category = ItemCategory.STRUCTURE,
      ingredients = listOf(
        RecipeIngredient(ItemType.LEAVES, 6),
        RecipeIngredient(ItemType.FIBER, 4),
        RecipeIngredient(ItemType.LEATHER, 2)
      ),
      description = "Sleep to pass the night and recover vitality."
    ),
    CraftingRecipe(
      id = "farm_plot",
      result = ItemType.FARM_PLOT,
      category = ItemCategory.STRUCTURE,
      ingredients = listOf(
        RecipeIngredient(ItemType.WOOD, 3),
        RecipeIngredient(ItemType.SEEDS, 2)
      ),
      description = "Grows recurring wild berries near your base."
    ),

    // Armor & Gear
    CraftingRecipe(
      id = "leather_tunic",
      result = ItemType.LEATHER_TUNIC,
      category = ItemCategory.ARMOR,
      ingredients = listOf(
        RecipeIngredient(ItemType.LEATHER, 5),
        RecipeIngredient(ItemType.FIBER, 4)
      ),
      description = "Light defense (+18 Armor)."
    ),
    CraftingRecipe(
      id = "fur_cloak",
      result = ItemType.FUR_CLOAK,
      category = ItemCategory.ARMOR,
      ingredients = listOf(
        RecipeIngredient(ItemType.LEATHER, 6),
        RecipeIngredient(ItemType.FIBER, 6)
      ),
      description = "Insulated protection against tundra and night chill."
    ),
    CraftingRecipe(
      id = "iron_armor",
      result = ItemType.IRON_ARMOR,
      category = ItemCategory.ARMOR,
      ingredients = listOf(
        RecipeIngredient(ItemType.IRON_ORE, 8),
        RecipeIngredient(ItemType.LEATHER, 4)
      ),
      description = "Heavy armor (+40 Armor)."
    )
  )
}
