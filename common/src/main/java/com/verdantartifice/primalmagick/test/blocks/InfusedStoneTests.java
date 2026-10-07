package com.verdantartifice.primalmagick.test.blocks;

import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;

/**
 * Tests for the drops of infused stone, which yields the essence dust of its source rather than itself.
 */
public class InfusedStoneTests extends AbstractBaseTest {
    public static void infused_stone_drops_essence_dust(GameTestHelper helper, Block stone, Item expectedDust) {
        var pos = BlockPos.ZERO.above();
        helper.setBlock(pos, stone);
        helper.assertBlockPresent(stone, pos);

        // Mine the block with a pickaxe as a survival player, the way an ordinary miner would
        var player = makeMockServerPlayer(helper);
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
        assertFalse(helper, player.hasInfiniteMaterials(), "Mining player has infinite materials");
        assertTrue(helper, player.gameMode.destroyBlock(helper.absolutePos(pos)), "Failed to break the infused stone");

        // The stone is gone and a single essence dust of its source dropped in its place, not the stone itself
        helper.assertBlockNotPresent(stone, pos);
        helper.assertItemEntityCountIs(expectedDust, pos, 1.0D, 1);
        helper.assertItemEntityNotPresent(stone.asItem(), pos, 2.0D);
        helper.succeed();
    }
}
