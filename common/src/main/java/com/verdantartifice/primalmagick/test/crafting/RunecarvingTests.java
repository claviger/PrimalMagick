package com.verdantartifice.primalmagick.test.crafting;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.menus.RunecarvingTableMenu;
import com.verdantartifice.primalmagick.common.research.ResearchDisciplines;
import com.verdantartifice.primalmagick.common.research.ResearchEntries;
import com.verdantartifice.primalmagick.common.research.ResearchManager;
import com.verdantartifice.primalmagick.common.stats.ExpertiseManager;
import com.verdantartifice.primalmagick.common.tiles.crafting.RunecarvingTableTileEntity;
import com.verdantartifice.primalmagick.platform.Services;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RunecarvingTests extends AbstractBaseTest {
    public static void craft_works(GameTestHelper helper) {
        // Create a test player with the research needed for basic runecarving
        var player = makeMockServerPlayer(helper);
        ResearchManager.forceGrantWithAllParents(player, ResearchEntries.BASIC_RUNEWORKING);
        assertTrue(helper, ExpertiseManager.getValue(player, ResearchDisciplines.RUNEWORKING).orElse(-1) == 0, "Expected starting expertise is not zero for test player");
        
        // Place a runecarving table
        BlockPos tablePos = new BlockPos(1, 1, 1);
        helper.setBlock(tablePos, BlocksPM.RUNECARVING_TABLE.get());
        helper.assertBlockPresent(BlocksPM.RUNECARVING_TABLE.get(), tablePos);
        
        // Populate the runecarving table with materials
        var tile = helper.getBlockEntity(tablePos, RunecarvingTableTileEntity.class);
        tile.addItem(0, 0, new ItemStack(Items.STONE_SLAB));
        assertTrue(helper, tile.getItem(0, 0).is(Items.STONE_SLAB), "Stone slab material not properly set");
        tile.addItem(0, 1, new ItemStack(Items.LAPIS_LAZULI));
        assertTrue(helper, tile.getItem(0, 1).is(Items.LAPIS_LAZULI), "Lapis lazuli material not properly set");
        
        // Open the block entity menu and select the first (and only) recipe
        Services.PLAYER.openMenu(player, tile, tablePos);
        var menu = assertInstanceOf(helper, player.containerMenu, RunecarvingTableMenu.class, "Menu not of expected type");
        assertTrue(helper, menu.getRecipeListSize() == 1, "Recipe list not as expected in runecarving menu");
        assertTrue(helper, menu.clickMenuButton(player, 0), "Recipe selection failed");
        
        // Take the result that should be there and confirm it's the right type of rune
        var output = menu.quickMoveStack(player, 2);
        assertTrue(helper, output.is(ItemsPM.RUNE_UNATTUNED.get()), "Output item not of expected type");
        
        // Confirm that crafting materials were consumed
        assertTrue(helper, tile.getItem(0, 0).isEmpty(), "Stone slab material stack not empty");
        assertTrue(helper, tile.getItem(0, 1).isEmpty(), "Lapis lazuli material stack not empty");
        
        // Confirm that expertise was granted to the player
        assertTrue(helper, ExpertiseManager.getValue(player, ResearchDisciplines.RUNEWORKING).orElse(-1) == 5, "Final expertise is not as expected for test player");
        
        helper.succeed();
    }

    public static void table_holds_materials_between_uses(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        ResearchManager.forceGrantWithAllParents(player, ResearchEntries.BASIC_RUNEWORKING);

        // Stock a runecarving table with enough materials for two runes
        BlockPos tablePos = new BlockPos(1, 1, 1);
        helper.setBlock(tablePos, BlocksPM.RUNECARVING_TABLE.get());
        var tile = helper.getBlockEntity(tablePos, RunecarvingTableTileEntity.class);
        tile.addItem(0, 0, new ItemStack(Items.STONE_SLAB, 2));
        tile.addItem(0, 1, new ItemStack(Items.LAPIS_LAZULI, 2));

        // Craft one rune, which consumes one slab and one lapis
        Services.PLAYER.openMenu(player, tile, helper.absolutePos(tablePos));
        var menu = assertInstanceOf(helper, player.containerMenu, RunecarvingTableMenu.class, "Menu not of expected type");
        assertTrue(helper, menu.clickMenuButton(player, 0), "Recipe selection failed");
        assertTrue(helper, menu.quickMoveStack(player, 2).is(ItemsPM.RUNE_UNATTUNED.get()), "First output item not of expected type");

        // Close the menu; the leftover materials stay in the table rather than going back to the player
        player.closeContainer();
        assertValueEqual(helper, 1, tile.getItem(0, 0).getCount(), "Stone slabs left in table after closing the menu");
        assertValueEqual(helper, 1, tile.getItem(0, 1).getCount(), "Lapis lazuli left in table after closing the menu");

        // Reopen the menu and confirm that the stored materials are offered again and craft a second rune
        Services.PLAYER.openMenu(player, tile, helper.absolutePos(tablePos));
        var reopened = assertInstanceOf(helper, player.containerMenu, RunecarvingTableMenu.class, "Reopened menu not of expected type");
        assertTrue(helper, reopened.hasItemsInInputSlot(), "Reopened menu does not show the stored materials");
        assertValueEqual(helper, 1, reopened.getRecipeListSize(), "Recipe list size in reopened menu");
        assertTrue(helper, reopened.clickMenuButton(player, 0), "Recipe selection failed on reopened menu");
        assertTrue(helper, reopened.quickMoveStack(player, 2).is(ItemsPM.RUNE_UNATTUNED.get()), "Second output item not of expected type");
        assertTrue(helper, tile.getItem(0, 0).isEmpty(), "Stone slab stack not empty after second craft");
        assertTrue(helper, tile.getItem(0, 1).isEmpty(), "Lapis lazuli stack not empty after second craft");
        helper.succeed();
    }
}
