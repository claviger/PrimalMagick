package com.verdantartifice.primalmagick.test.tiles;

import com.verdantartifice.primalmagick.common.capabilities.IItemHandlerPM;
import com.verdantartifice.primalmagick.common.tiles.base.AbstractTileSidedInventoryPM;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/**
 * Shared helpers for the mana tile tests, covering test item stacks, placement of sided-inventory tiles, and checks on
 * which items their item handlers accept.
 */
public final class TileTestUtils extends AbstractBaseTest {
    private TileTestUtils() {}

    /**
     * Returns a fresh stack of an item that carries mana storage and so can be charged by the mana tiles.
     */
    public static ItemStack getChargeableTestStack() {
        return ChargeableItem.MUNDANE_WAND.makeStack();
    }

    /**
     * Returns a fresh stack of an item that carries no mana storage and so can't be charged by the mana tiles.
     */
    public static ItemStack getUnchargeableTestStack() {
        return Items.STICK.getDefaultInstance();
    }

    /**
     * Places the given block, fetches its block entity as the given class, and returns the raw item handler for the
     * given face, failing the test if the face has no handler.
     */
    public static <T extends AbstractTileSidedInventoryPM> IItemHandlerPM placeTileAndGetHandler(GameTestHelper helper, BlockPos pos, Block block, Class<T> tileClass, Direction face) {
        helper.setBlock(pos, block);
        var tile = helper.getBlockEntity(pos, tileClass);
        var handler = tile.getRawItemHandler(face);
        assertFalse(helper, handler == null, "No item handler found for face " + face);
        return handler;
    }

    public static void assertHandlerAccepts(GameTestHelper helper, IItemHandlerPM handler, ItemStack stack) {
        assertTrue(helper, handler.isItemValid(0, stack), "Test stack " + stack + " unexpectedly invalid for item handler");
    }

    public static void assertHandlerRejects(GameTestHelper helper, IItemHandlerPM handler, ItemStack stack) {
        assertFalse(helper, handler.isItemValid(0, stack), "Test stack " + stack + " unexpectedly valid for item handler");
    }
}
