package com.verdantartifice.primalmagick.test.tiles;

import com.verdantartifice.primalmagick.common.capabilities.IItemHandlerPM;
import com.verdantartifice.primalmagick.common.tiles.base.AbstractTileSidedInventoryPM;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * Shared helpers for the mana tile tests, covering placement of sided-inventory tiles and checks on which items their
 * item handlers accept.
 */
public final class TileTestUtils {
    private TileTestUtils() {}

    /**
     * Places the given block, fetches its block entity as the given class, and returns the raw item handler for the
     * given face, failing the test if the face has no handler.
     */
    public static <T extends AbstractTileSidedInventoryPM> IItemHandlerPM placeTileAndGetHandler(GameTestHelper helper, BlockPos pos, Block block, Class<T> tileClass, Direction face) {
        helper.setBlock(pos, block);
        var tile = helper.getBlockEntity(pos, tileClass);
        var handler = tile.getRawItemHandler(face);
        helper.assertFalse(handler == null, Component.literal("No item handler found for face " + face));
        return handler;
    }

    public static void assertHandlerAccepts(GameTestHelper helper, IItemHandlerPM handler, ItemStack stack) {
        helper.assertTrue(handler.isItemValid(0, stack), Component.literal("Test stack " + stack + " unexpectedly invalid for item handler"));
    }

    public static void assertHandlerRejects(GameTestHelper helper, IItemHandlerPM handler, ItemStack stack) {
        helper.assertFalse(handler.isItemValid(0, stack), Component.literal("Test stack " + stack + " unexpectedly valid for item handler"));
    }
}
