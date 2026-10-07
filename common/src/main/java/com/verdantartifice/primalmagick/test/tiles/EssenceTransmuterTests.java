package com.verdantartifice.primalmagick.test.tiles;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.items.essence.EssenceItem;
import com.verdantartifice.primalmagick.common.items.essence.EssenceType;
import com.verdantartifice.primalmagick.common.menus.EssenceTransmuterMenu;
import com.verdantartifice.primalmagick.common.sources.Source;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.common.tiles.devices.EssenceTransmuterTileEntity;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Tests for the essence transmuter: menu access, absorbing moon mana from a slotted wand, and converting eight
 * essences of one source into one essence of a different source with the same grade.
 */
public class EssenceTransmuterTests extends AbstractBaseTest {
    // Eight essences are consumed per transmutation, it takes 100 ticks, and it costs 200 centimana of moon mana
    // (EssenceTransmuterTileEntity.ESSENCE_PER_TRANSMUTE, getProcessTimeTotal, and getManaCost)
    private static final int ESSENCES_PER_TRANSMUTE = 8;
    private static final int PROCESS_TICKS = 100;
    private static final int MANA_COST = 200;

    // The sources that can be transmuted into without any research: every source with a discover key is excluded
    // for a transmuter that has no owner (Sources: only blood, infernal, void, and hallowed have discover keys)
    private static final List<Source> UNRESTRICTED_SOURCES = List.of(Sources.EARTH, Sources.SEA, Sources.SKY, Sources.SUN, Sources.MOON);

    private static void tick(GameTestHelper helper, BlockPos pos, EssenceTransmuterTileEntity tile) {
        EssenceTransmuterTileEntity.tick(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos), tile);
    }

    private static EssenceTransmuterTileEntity placeLoadedTransmuter(GameTestHelper helper, BlockPos pos, ItemStack input) {
        helper.setBlock(pos, BlocksPM.ESSENCE_TRANSMUTER.get());
        var tile = helper.getBlockEntity(pos, EssenceTransmuterTileEntity.class);
        tile.setMana(Sources.MOON, MANA_COST * 2);
        tile.addItem(EssenceTransmuterTileEntity.INPUT_INV_INDEX, 0, input);
        assertValueEqual(helper, input.getCount(), tile.getItem(EssenceTransmuterTileEntity.INPUT_INV_INDEX, 0).getCount(), "Input count after loading");
        return tile;
    }

    /**
     * Counts all the output slots' contents and returns the first non-empty stack.
     */
    private static ItemStack firstOutput(EssenceTransmuterTileEntity tile) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = tile.getItem(EssenceTransmuterTileEntity.OUTPUT_INV_INDEX, slot);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static int outputCount(EssenceTransmuterTileEntity tile) {
        int total = 0;
        for (int slot = 0; slot < 9; slot++) {
            total += tile.getItem(EssenceTransmuterTileEntity.OUTPUT_INV_INDEX, slot).getCount();
        }
        return total;
    }

    public static void essence_transmuter_can_have_its_menu_opened(GameTestHelper helper) {
        MagitechTileTestUtils.assertMenuOpens(helper, BlocksPM.ESSENCE_TRANSMUTER.get(), EssenceTransmuterTileEntity.class, EssenceTransmuterMenu.class);
    }

    public static void essence_transmuter_absorbs_moon_mana_from_wand(GameTestHelper helper) {
        MagitechTileTestUtils.assertAbsorbsWandMana(helper, BlocksPM.ESSENCE_TRANSMUTER.get(), EssenceTransmuterTileEntity.class, EssenceTransmuterTileEntity::tick, EssenceTransmuterTileEntity.WAND_INV_INDEX, Sources.MOON);
    }

    public static void essence_transmuter_converts_eight_essence_to_a_different_source(GameTestHelper helper) {
        var pos = BlockPos.ZERO;
        var tile = placeLoadedTransmuter(helper, pos, EssenceItem.getEssence(EssenceType.DUST, Sources.EARTH, ESSENCES_PER_TRANSMUTE));

        // Nothing is produced until the process completes
        for (int i = 0; i < PROCESS_TICKS - 1; i++) {
            tick(helper, pos, tile);
        }
        assertValueEqual(helper, 0, outputCount(tile), "Output count before the process finished");

        // The final tick transmutes all eight essences into one
        tick(helper, pos, tile);
        assertValueEqual(helper, 1, outputCount(tile), "Output count after the process finished");
        assertTrue(helper, tile.getItem(EssenceTransmuterTileEntity.INPUT_INV_INDEX, 0).isEmpty(), "Input essences not consumed");
        assertValueEqual(helper, MANA_COST, tile.getMana(Sources.MOON), "Moon mana remaining");
        var output = assertInstanceOf(helper, firstOutput(tile).getItem(), EssenceItem.class, "Output is not an essence");
        assertFalse(helper, output.getSource().equals(Sources.EARTH), "Output source is the same as the input source");
        assertTrue(helper, UNRESTRICTED_SOURCES.contains(output.getSource()), "Output source " + output.getSource().getId() + " requires research the transmuter doesn't have");
        helper.succeed();
    }

    public static void essence_transmuter_does_not_convert_fewer_than_eight_essence(GameTestHelper helper) {
        var pos = BlockPos.ZERO;
        var tile = placeLoadedTransmuter(helper, pos, EssenceItem.getEssence(EssenceType.DUST, Sources.EARTH, ESSENCES_PER_TRANSMUTE - 1));
        for (int i = 0; i < PROCESS_TICKS * 2; i++) {
            tick(helper, pos, tile);
        }
        assertValueEqual(helper, 0, outputCount(tile), "Output count with too few essences");
        assertValueEqual(helper, ESSENCES_PER_TRANSMUTE - 1, tile.getItem(EssenceTransmuterTileEntity.INPUT_INV_INDEX, 0).getCount(), "Input count with too few essences");
        assertValueEqual(helper, MANA_COST * 2, tile.getMana(Sources.MOON), "Moon mana with too few essences");
        helper.succeed();
    }

    public static void essence_transmuter_preserves_essence_grade(GameTestHelper helper, EssenceType type) {
        var pos = BlockPos.ZERO;
        var tile = placeLoadedTransmuter(helper, pos, EssenceItem.getEssence(type, Sources.SEA, ESSENCES_PER_TRANSMUTE));
        for (int i = 0; i < PROCESS_TICKS; i++) {
            tick(helper, pos, tile);
        }
        assertValueEqual(helper, 1, outputCount(tile), "Output count for " + type.getSerializedName());
        var output = assertInstanceOf(helper, firstOutput(tile).getItem(), EssenceItem.class, "Output is not an essence");
        assertValueEqual(helper, type, output.getEssenceType(), "Output grade for input " + type.getSerializedName());
        assertFalse(helper, output.getSource().equals(Sources.SEA), "Output source is the same as the input source");
        helper.succeed();
    }
}
