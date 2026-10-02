package com.verdantartifice.primalmagick.test.capabilities;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.capabilities.IItemHandlerPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.tiles.mana.WandChargerTileEntity;
import com.verdantartifice.primalmagick.common.tiles.rituals.OfferingPedestalTileEntity;
import com.verdantartifice.primalmagick.common.tiles.rituals.RitualAltarTileEntity;
import com.verdantartifice.primalmagick.platform.Services;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/**
 * Tests for the item handlers that mod block entities expose to other blocks through the item capability, which must
 * apply each handler's item validity and slot limit rules to insertions the way the 1.21 handlers did.
 */
public class ItemHandlerCapabilityTests extends AbstractBaseTest {
    /**
     * The ritual altar's only inventory, index 0, holds the ritual output in its single slot.
     */
    private static final int ALTAR_OUTPUT_INV_INDEX = 0;

    private static IItemHandlerPM placeAndGetCapability(GameTestHelper helper, BlockPos pos, Block block, Direction face) {
        helper.setBlock(pos, block);
        var handler = Services.CAPABILITIES.itemHandler(helper.getLevel(), helper.absolutePos(pos), face);
        assertTrue(helper, handler.isPresent(), "No item handler capability found for face " + face);
        return handler.get();
    }

    /**
     * Confirms that the ritual altar's output slot rejects items inserted through the capability, as automation could
     * otherwise fill the slot, while the altar's own replacement of the slot contents still works.
     */
    public static void item_handler_capability_rejects_invalid_item_for_altar_output(GameTestHelper helper) {
        var pos = BlockPos.ZERO;
        var handler = placeAndGetCapability(helper, pos, BlocksPM.RITUAL_ALTAR.get(), Direction.UP);
        var tile = helper.getBlockEntity(pos, RitualAltarTileEntity.class);

        ItemStack leftover = handler.insertItem(0, new ItemStack(Items.APPLE, 3), false);
        assertValueEqual(helper, 3, leftover.getCount(), "Apples left over after a slot insert into the altar output");
        leftover = handler.insertItem(new ItemStack(Items.APPLE, 3), false);
        assertValueEqual(helper, 3, leftover.getCount(), "Apples left over after a handler insert into the altar output");
        assertTrue(helper, tile.getItem().isEmpty(), "Altar output slot not empty after inserting through the capability: " + tile.getItem());

        ItemStack oldStack = tile.replaceItem(ALTAR_OUTPUT_INV_INDEX, 0, new ItemStack(Items.APPLE, 3));
        assertTrue(helper, oldStack.isEmpty(), "Replaced stack from an empty slot was not empty: " + oldStack);
        assertValueEqual(helper, Items.APPLE, tile.getItem().getItem(), "Output slot item after replace");
        assertValueEqual(helper, 3, tile.getItem().getCount(), "Output slot count after replace");
        helper.succeed();
    }

    /**
     * Confirms that the offering pedestal's one-item slot limit holds for items inserted through the capability.
     */
    public static void item_handler_capability_enforces_slot_limit_on_offering_pedestal(GameTestHelper helper) {
        var pos = BlockPos.ZERO;
        var handler = placeAndGetCapability(helper, pos, BlocksPM.OFFERING_PEDESTAL.get(), Direction.UP);
        var tile = helper.getBlockEntity(pos, OfferingPedestalTileEntity.class);

        ItemStack leftover = handler.insertItem(0, new ItemStack(Items.APPLE, 4), false);
        assertValueEqual(helper, Items.APPLE, leftover.getItem(), "Leftover item after inserting into the pedestal");
        assertValueEqual(helper, 3, leftover.getCount(), "Leftover count after inserting into the pedestal");
        assertValueEqual(helper, Items.APPLE, tile.getItem().getItem(), "Pedestal slot item after insert");
        assertValueEqual(helper, 1, tile.getItem().getCount(), "Pedestal slot count after insert");

        // A second insert must not push the slot past its limit
        leftover = handler.insertItem(new ItemStack(Items.APPLE, 2), false);
        assertValueEqual(helper, 2, leftover.getCount(), "Leftover count after inserting into a full pedestal");
        assertValueEqual(helper, 1, tile.getItem().getCount(), "Pedestal slot count after inserting into a full pedestal");
        helper.succeed();
    }

    /**
     * Confirms that the capability still accepts valid items, here an essence inserted into a wand charger's input.
     */
    public static void item_handler_capability_accepts_valid_item(GameTestHelper helper) {
        var pos = BlockPos.ZERO;
        var handler = placeAndGetCapability(helper, pos, BlocksPM.WAND_CHARGER.get(), Direction.UP);
        var tile = helper.getBlockEntity(pos, WandChargerTileEntity.class);

        ItemStack leftover = handler.insertItem(0, new ItemStack(ItemsPM.ESSENCE_DUST_EARTH.get(), 2), false);
        assertTrue(helper, leftover.isEmpty(), "Essence not fully accepted by the wand charger: " + leftover);
        ItemStack inputStack = tile.getItem(WandChargerTileEntity.INPUT_INV_INDEX, 0);
        assertValueEqual(helper, ItemsPM.ESSENCE_DUST_EARTH.get(), inputStack.getItem(), "Wand charger input item");
        assertValueEqual(helper, 2, inputStack.getCount(), "Wand charger input count");

        // An item that isn't an essence is still turned away
        leftover = handler.insertItem(0, new ItemStack(Items.STICK), false);
        assertValueEqual(helper, 1, leftover.getCount(), "Sticks left over after inserting into the wand charger input");
        helper.succeed();
    }
}
