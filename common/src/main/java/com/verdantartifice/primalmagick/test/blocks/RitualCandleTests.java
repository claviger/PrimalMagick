package com.verdantartifice.primalmagick.test.blocks;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.blocks.rituals.RitualCandleBlock;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Tests for lighting and snuffing ritual candles by hand. Clicks go through the server's full block interaction
 * dispatch, since the block's empty-hand handler is only reached if its item handler defers to it.
 */
public class RitualCandleTests extends AbstractBaseTest {
    private static BlockHitResult hitTop(GameTestHelper helper, BlockPos pos) {
        var absPos = helper.absolutePos(pos);
        return new BlockHitResult(absPos.getCenter(), Direction.UP, absPos, false);
    }

    private static void placeCandle(GameTestHelper helper, BlockPos pos, boolean lit) {
        helper.setBlock(pos, BlocksPM.RITUAL_CANDLE_WHITE.get().defaultBlockState().setValue(RitualCandleBlock.LIT, lit));
        assertValueEqual(helper, lit, helper.getBlockState(pos).getValue(RitualCandleBlock.LIT), "Candle lit state after placement");
    }

    public static void ritual_candle_extinguished_by_empty_hand(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        placeCandle(helper, pos, true);

        // Right-click the lit candle with an empty hand
        var player = makeMockServerPlayer(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        var result = player.gameMode.useItemOn(player, helper.getLevel(), ItemStack.EMPTY, InteractionHand.MAIN_HAND, hitTop(helper, pos));

        assertTrue(helper, result.consumesAction(), "Empty-hand use on a lit candle was not consumed: " + result);
        assertValueEqual(helper, false, helper.getBlockState(pos).getValue(RitualCandleBlock.LIT), "Candle lit state after empty-hand use");
        helper.succeed();
    }

    public static void ritual_candle_lit_by_flint_and_steel(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        placeCandle(helper, pos, false);

        // Use flint and steel on the unlit candle as a survival player, since creative players don't wear out tools
        var player = makeMockServerPlayer(helper);
        player.setGameMode(GameType.SURVIVAL);
        var stack = new ItemStack(Items.FLINT_AND_STEEL);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var result = player.gameMode.useItemOn(player, helper.getLevel(), stack, InteractionHand.MAIN_HAND, hitTop(helper, pos));

        assertTrue(helper, result.consumesAction(), "Flint and steel use on an unlit candle was not consumed: " + result);
        assertValueEqual(helper, true, helper.getBlockState(pos).getValue(RitualCandleBlock.LIT), "Candle lit state after flint and steel use");
        assertValueEqual(helper, 1, player.getItemInHand(InteractionHand.MAIN_HAND).getDamageValue(), "Flint and steel damage after use");
        helper.succeed();
    }

    public static void ritual_candle_empty_hand_on_unlit_does_nothing(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        placeCandle(helper, pos, false);

        // Right-click the unlit candle with an empty hand
        var player = makeMockServerPlayer(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        var result = player.gameMode.useItemOn(player, helper.getLevel(), ItemStack.EMPTY, InteractionHand.MAIN_HAND, hitTop(helper, pos));

        assertFalse(helper, result.consumesAction(), "Empty-hand use on an unlit candle was consumed: " + result);
        assertValueEqual(helper, false, helper.getBlockState(pos).getValue(RitualCandleBlock.LIT), "Candle lit state after empty-hand use");
        helper.succeed();
    }
}
