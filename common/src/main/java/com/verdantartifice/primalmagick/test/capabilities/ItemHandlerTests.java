package com.verdantartifice.primalmagick.test.capabilities;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.capabilities.IItemHandlerPM;
import com.verdantartifice.primalmagick.platform.Services;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class ItemHandlerTests extends AbstractBaseTest {
    public static void block_entity_can_retrieve_item_handler_with_null_direction(GameTestHelper helper, Block block) {
        // Place a copy of the block into the test world
        var pos = BlockPos.ZERO;
        helper.setBlock(pos, block);
        var tile = helper.getBlockEntity(pos, BlockEntity.class);

        // Confirm that the item handler for the block entity can be fetched with a null direction without crashing
        assertTrue(helper, Services.ITEM_HANDLERS.touch(tile, null), "Failed to get item handler");
        helper.succeed();
    }

    /**
     * Confirms that each operation in a simulated transaction is checked against the handler contents as changed by the
     * operations before it, rather than against the original contents, and that the simulation changes nothing. Covers
     * both a block entity's own handler and the view of a handler exposed through the item capability.
     */
    public static void item_handler_simulated_transaction_sees_cumulative_state(GameTestHelper helper) {
        // A one-slot handler can hold only one sword, as swords don't stack
        IItemHandlerPM handler = Services.ITEM_HANDLERS.create(1, null);
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        assertFalse(helper, handler.transact(true, List.of(
                new IItemHandlerPM.HandlerOperation(IItemHandlerPM.OperationType.INSERT, sword.copy()),
                new IItemHandlerPM.HandlerOperation(IItemHandlerPM.OperationType.INSERT, sword.copy()))),
                "Simulated insertion of two swords into one slot reported success");
        assertFalse(helper, handler.transactSlots(true, List.of(
                new IItemHandlerPM.SlotOperation(IItemHandlerPM.OperationType.INSERT, 0, sword.copy()),
                new IItemHandlerPM.SlotOperation(IItemHandlerPM.OperationType.INSERT, 0, sword.copy()))),
                "Simulated slot insertion of two swords into one slot reported success");
        assertTrue(helper, handler.getStackInSlot(0).isEmpty(), "Simulated transactions changed the handler: " + handler.getStackInSlot(0));
        assertTrue(helper, handler.transact(true, List.of(new IItemHandlerPM.HandlerOperation(IItemHandlerPM.OperationType.INSERT, sword.copy()))),
                "Simulated insertion of one sword into an empty slot reported failure");

        // An offering pedestal's slot holds only one item, and its item capability enforces that
        var pos = BlockPos.ZERO;
        helper.setBlock(pos, BlocksPM.OFFERING_PEDESTAL.get());
        var capability = Services.CAPABILITIES.itemHandler(helper.getLevel(), helper.absolutePos(pos), Direction.UP);
        assertTrue(helper, capability.isPresent(), "No item handler capability found for the offering pedestal");
        assertFalse(helper, capability.get().transact(true, List.of(
                new IItemHandlerPM.HandlerOperation(IItemHandlerPM.OperationType.INSERT, new ItemStack(Items.APPLE)),
                new IItemHandlerPM.HandlerOperation(IItemHandlerPM.OperationType.INSERT, new ItemStack(Items.APPLE)))),
                "Simulated insertion of two apples into the offering pedestal reported success");
        assertTrue(helper, capability.get().getStackInSlot(0).isEmpty(), "Simulated transaction changed the offering pedestal: " + capability.get().getStackInSlot(0));
        helper.succeed();
    }
}
