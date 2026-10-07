package com.verdantartifice.primalmagick.test.tiles;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.menus.DissolutionChamberMenu;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.common.tiles.devices.DissolutionChamberTileEntity;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Tests for the dissolution chamber: menu access, absorbing earth mana from a slotted wand, and dissolving metal ores
 * and raw metals into grit.
 */
public class DissolutionChamberTests extends AbstractBaseTest {
    // Dissolving takes 100 ticks (DissolutionChamberTileEntity.getProcessTimeTotal), and the ore and raw metal recipes
    // (iron/gold/copper_grit_from_dissolving_ore and _raw_metal) each cost 1000 centimana of earth mana and yield 3 grit
    private static final int PROCESS_TICKS = 100;
    private static final int MANA_COST = 1000;
    private static final int GRIT_YIELD = 3;

    private static void tick(GameTestHelper helper, BlockPos pos, DissolutionChamberTileEntity tile) {
        DissolutionChamberTileEntity.tick(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos), tile);
    }

    public static void dissolution_chamber_can_have_its_menu_opened(GameTestHelper helper) {
        MagitechTileTestUtils.assertMenuOpens(helper, BlocksPM.DISSOLUTION_CHAMBER.get(), DissolutionChamberTileEntity.class, DissolutionChamberMenu.class);
    }

    public static void dissolution_chamber_absorbs_earth_mana_from_wand(GameTestHelper helper) {
        MagitechTileTestUtils.assertAbsorbsWandMana(helper, BlocksPM.DISSOLUTION_CHAMBER.get(), DissolutionChamberTileEntity.class, DissolutionChamberTileEntity::tick, DissolutionChamberTileEntity.WAND_INV_INDEX, Sources.EARTH);
    }

    /**
     * Confirms that one of the given input, charged with exactly the recipe's mana cost, is dissolved into grit over
     * 100 ticks and not before.
     */
    public static void dissolution_chamber_dissolves(GameTestHelper helper, Item input, Item expectedGrit) {
        var pos = BlockPos.ZERO;
        helper.setBlock(pos, BlocksPM.DISSOLUTION_CHAMBER.get());
        var tile = helper.getBlockEntity(pos, DissolutionChamberTileEntity.class);
        tile.setMana(Sources.EARTH, MANA_COST);
        tile.addItem(DissolutionChamberTileEntity.INPUT_INV_INDEX, 0, new ItemStack(input));
        assertFalse(helper, tile.getItem(DissolutionChamberTileEntity.INPUT_INV_INDEX, 0).isEmpty(), "Input not accepted");

        // Nothing is produced until the process completes
        for (int i = 0; i < PROCESS_TICKS - 1; i++) {
            tick(helper, pos, tile);
        }
        assertTrue(helper, tile.getItem(DissolutionChamberTileEntity.OUTPUT_INV_INDEX, 0).isEmpty(), "Output produced before the process finished");

        // The final tick completes the process
        tick(helper, pos, tile);
        var output = tile.getItem(DissolutionChamberTileEntity.OUTPUT_INV_INDEX, 0);
        assertValueEqual(helper, expectedGrit, output.getItem(), "Output item");
        assertValueEqual(helper, GRIT_YIELD, output.getCount(), "Output count");
        assertTrue(helper, tile.getItem(DissolutionChamberTileEntity.INPUT_INV_INDEX, 0).isEmpty(), "Input not consumed");
        assertValueEqual(helper, 0, tile.getMana(Sources.EARTH), "Earth mana remaining");
        helper.succeed();
    }
}
