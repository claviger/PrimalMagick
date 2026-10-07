package com.verdantartifice.primalmagick.test.tiles;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.menus.HoneyExtractorMenu;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.common.tiles.devices.HoneyExtractorTileEntity;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Tests for the honey extractor: menu access, absorbing sky mana from a slotted wand, and turning honeycomb and
 * glass bottles into honey bottles and beeswax.
 */
public class HoneyExtractorTests extends AbstractBaseTest {
    // Spin time total is 100 ticks and mana cost is 200 centimana (HoneyExtractorTileEntity.getSpinTimeTotal and getManaCost)
    private static final int SPIN_TICKS = 100;
    private static final int MANA_COST = 200;

    private static void tick(GameTestHelper helper, BlockPos pos, HoneyExtractorTileEntity tile) {
        HoneyExtractorTileEntity.tick(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos), tile);
    }

    public static void honey_extractor_can_have_its_menu_opened(GameTestHelper helper) {
        MagitechTileTestUtils.assertMenuOpens(helper, BlocksPM.HONEY_EXTRACTOR.get(), HoneyExtractorTileEntity.class, HoneyExtractorMenu.class);
    }

    public static void honey_extractor_absorbs_sky_mana_from_wand(GameTestHelper helper) {
        MagitechTileTestUtils.assertAbsorbsWandMana(helper, BlocksPM.HONEY_EXTRACTOR.get(), HoneyExtractorTileEntity.class, HoneyExtractorTileEntity::tick, HoneyExtractorTileEntity.WAND_INV_INDEX, Sources.SKY);
    }

    public static void honey_extractor_makes_honey_bottle_and_beeswax(GameTestHelper helper) {
        var pos = BlockPos.ZERO;
        helper.setBlock(pos, BlocksPM.HONEY_EXTRACTOR.get());
        var tile = helper.getBlockEntity(pos, HoneyExtractorTileEntity.class);

        // Load the extractor with twice the mana a spin costs, one honeycomb, and one glass bottle
        tile.setMana(Sources.SKY, MANA_COST * 2);
        tile.addItem(HoneyExtractorTileEntity.INPUT_INV_INDEX, 0, new ItemStack(Items.HONEYCOMB));
        tile.addItem(HoneyExtractorTileEntity.INPUT_INV_INDEX, 1, new ItemStack(Items.GLASS_BOTTLE));
        assertFalse(helper, tile.getItem(HoneyExtractorTileEntity.INPUT_INV_INDEX, 0).isEmpty(), "Honeycomb not accepted");
        assertFalse(helper, tile.getItem(HoneyExtractorTileEntity.INPUT_INV_INDEX, 1).isEmpty(), "Glass bottle not accepted");

        // Nothing is produced until the spin completes
        for (int i = 0; i < SPIN_TICKS - 1; i++) {
            tick(helper, pos, tile);
        }
        assertTrue(helper, tile.getItem(HoneyExtractorTileEntity.OUTPUT_INV_INDEX, 0).isEmpty(), "Honey produced before the spin finished");

        // The final tick completes the spin
        tick(helper, pos, tile);
        var honey = tile.getItem(HoneyExtractorTileEntity.OUTPUT_INV_INDEX, 0);
        var wax = tile.getItem(HoneyExtractorTileEntity.OUTPUT_INV_INDEX, 1);
        assertValueEqual(helper, Items.HONEY_BOTTLE, honey.getItem(), "Honey output item");
        assertValueEqual(helper, 1, honey.getCount(), "Honey output count");
        assertValueEqual(helper, ItemsPM.BEESWAX.get(), wax.getItem(), "Beeswax output item");
        assertValueEqual(helper, 1, wax.getCount(), "Beeswax output count");
        assertTrue(helper, tile.getItem(HoneyExtractorTileEntity.INPUT_INV_INDEX, 0).isEmpty(), "Honeycomb not consumed");
        assertTrue(helper, tile.getItem(HoneyExtractorTileEntity.INPUT_INV_INDEX, 1).isEmpty(), "Glass bottle not consumed");
        assertValueEqual(helper, MANA_COST, tile.getMana(Sources.SKY), "Sky mana remaining");
        helper.succeed();
    }
}
