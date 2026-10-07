package com.verdantartifice.primalmagick.test.tiles;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.menus.InfernalFurnaceMenu;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.common.tiles.devices.InfernalFurnaceTileEntity;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Tests for the infernal furnace's normal operation: menu access, absorbing infernal mana from a slotted wand,
 * smelting at the cost of mana, and the speedup from supercharging with ignyx.
 */
public class InfernalFurnaceOperationTests extends AbstractBaseTest {
    // Smelting raw iron takes 200 ticks in a vanilla furnace, and the infernal furnace halves that (getTotalCookTime), so
    // it takes 100 ticks. The mana cost is 100 centimana per ten ticks of that cook time (getManaNeeded), so 1000 centimana.
    private static final int SMELT_TICKS = 100;
    private static final int SMELT_MANA_COST = 1000;
    private static final int START_MANA = 5000;

    // Supercharging makes the furnace progress five times as fast (SUPERCHARGE_MULTIPLIER), so 100 ticks of work takes 20
    private static final int SUPERCHARGED_SMELT_TICKS = 20;

    private static final int MAX_TICKS = 300;

    /**
     * Loads a furnace with mana and raw iron, plus ignyx if requested, ticks it until it produces output, and returns
     * how many ticks that took. Fails the test if the furnace is still working after MAX_TICKS.
     */
    private static int smeltRawIron(GameTestHelper helper, boolean ignyx) {
        var pos = BlockPos.ZERO;
        helper.setBlock(pos, BlocksPM.INFERNAL_FURNACE.get());
        var furnace = helper.getBlockEntity(pos, InfernalFurnaceTileEntity.class);
        furnace.setMana(Sources.INFERNAL, START_MANA);
        furnace.addItem(InfernalFurnaceTileEntity.INPUT_INV_INDEX, 0, new ItemStack(Items.RAW_IRON));
        assertFalse(helper, furnace.getItem(InfernalFurnaceTileEntity.INPUT_INV_INDEX, 0).isEmpty(), "Raw iron not accepted");
        if (ignyx) {
            furnace.addItem(InfernalFurnaceTileEntity.FUEL_INV_INDEX, 0, new ItemStack(ItemsPM.IGNYX.get()));
            assertTrue(helper, furnace.getItem(InfernalFurnaceTileEntity.FUEL_INV_INDEX, 0).is(ItemsPM.IGNYX.get()), "Ignyx not in the fuel slot");
        }

        int ticks = 0;
        while (ticks < MAX_TICKS && furnace.getItem(InfernalFurnaceTileEntity.OUTPUT_INV_INDEX, 0).isEmpty()) {
            InfernalFurnaceTileEntity.tick(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos), furnace);
            ticks++;
        }

        // Whichever way it was powered, the same item is made at the same mana cost
        var output = furnace.getItem(InfernalFurnaceTileEntity.OUTPUT_INV_INDEX, 0);
        assertValueEqual(helper, Items.IRON_INGOT, output.getItem(), "Smelted item");
        assertValueEqual(helper, 1, output.getCount(), "Smelted count");
        assertTrue(helper, furnace.getItem(InfernalFurnaceTileEntity.INPUT_INV_INDEX, 0).isEmpty(), "Raw iron not consumed");
        assertValueEqual(helper, START_MANA - SMELT_MANA_COST, furnace.getMana(Sources.INFERNAL), "Infernal mana remaining");
        if (ignyx) {
            assertTrue(helper, furnace.getItem(InfernalFurnaceTileEntity.FUEL_INV_INDEX, 0).isEmpty(), "Ignyx not consumed");
        }
        return ticks;
    }

    public static void infernal_furnace_can_have_its_menu_opened(GameTestHelper helper) {
        MagitechTileTestUtils.assertMenuOpens(helper, BlocksPM.INFERNAL_FURNACE.get(), InfernalFurnaceTileEntity.class, InfernalFurnaceMenu.class);
    }

    public static void infernal_furnace_absorbs_infernal_mana_from_wand(GameTestHelper helper) {
        MagitechTileTestUtils.assertAbsorbsWandMana(helper, BlocksPM.INFERNAL_FURNACE.get(), InfernalFurnaceTileEntity.class, InfernalFurnaceTileEntity::tick, InfernalFurnaceTileEntity.WAND_INV_INDEX, Sources.INFERNAL);
    }

    public static void infernal_furnace_smelts_items_at_the_cost_of_mana(GameTestHelper helper) {
        assertValueEqual(helper, SMELT_TICKS, smeltRawIron(helper, false), "Ticks to smelt without ignyx");
        helper.succeed();
    }

    public static void infernal_furnace_does_not_smelt_without_mana(GameTestHelper helper) {
        var pos = BlockPos.ZERO;
        helper.setBlock(pos, BlocksPM.INFERNAL_FURNACE.get());
        var furnace = helper.getBlockEntity(pos, InfernalFurnaceTileEntity.class);
        furnace.addItem(InfernalFurnaceTileEntity.INPUT_INV_INDEX, 0, new ItemStack(Items.RAW_IRON));
        for (int i = 0; i < SMELT_TICKS * 2; i++) {
            InfernalFurnaceTileEntity.tick(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos), furnace);
        }
        assertTrue(helper, furnace.getItem(InfernalFurnaceTileEntity.OUTPUT_INV_INDEX, 0).isEmpty(), "Smelted without mana");
        assertValueEqual(helper, 1, furnace.getItem(InfernalFurnaceTileEntity.INPUT_INV_INDEX, 0).getCount(), "Raw iron count without mana");
        helper.succeed();
    }

    public static void infernal_furnace_runs_faster_when_supercharged_with_ignyx(GameTestHelper helper) {
        assertValueEqual(helper, SUPERCHARGED_SMELT_TICKS, smeltRawIron(helper, true), "Ticks to smelt with ignyx");
        helper.succeed();
    }
}
