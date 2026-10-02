package com.verdantartifice.primalmagick.test.blocks;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.blocks.devices.SanguineCrucibleBlock;
import com.verdantartifice.primalmagick.common.blocks.rituals.BloodletterBlock;
import com.verdantartifice.primalmagick.common.blocks.rituals.IncenseBrazierBlock;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.tiles.devices.SanguineCrucibleTileEntity;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Tests for empty-hand interactions with ritual props and the sanguine crucible. Clicks go through the server's full
 * block interaction dispatch, since each block's empty-hand handler is only reached if its item handler defers to it.
 */
public class RitualPropTests extends AbstractBaseTest {
    private static BlockHitResult hitTop(GameTestHelper helper, BlockPos pos) {
        var absPos = helper.absolutePos(pos);
        return new BlockHitResult(absPos.getCenter(), Direction.UP, absPos, false);
    }

    public static void incense_brazier_extinguished_by_empty_hand(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        helper.setBlock(pos, BlocksPM.INCENSE_BRAZIER.get().defaultBlockState().setValue(IncenseBrazierBlock.LIT, true));
        assertValueEqual(helper, true, helper.getBlockState(pos).getValue(IncenseBrazierBlock.LIT), "Brazier lit state after placement");

        // Right-click the lit brazier with an empty hand
        var player = makeMockServerPlayer(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        var result = player.gameMode.useItemOn(player, helper.getLevel(), ItemStack.EMPTY, InteractionHand.MAIN_HAND, hitTop(helper, pos));

        assertTrue(helper, result.consumesAction(), "Empty-hand use on a lit brazier was not consumed: " + result);
        assertValueEqual(helper, false, helper.getBlockState(pos).getValue(IncenseBrazierBlock.LIT), "Brazier lit state after empty-hand use");
        helper.succeed();
    }

    public static void sanguine_crucible_core_removed_by_sneaking_empty_hand(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        helper.setBlock(pos, BlocksPM.SANGUINE_CRUCIBLE.get().defaultBlockState().setValue(SanguineCrucibleBlock.LIT, true));
        var tile = helper.getBlockEntity(pos, SanguineCrucibleTileEntity.class);
        tile.addItem(new ItemStack(ItemsPM.SANGUINE_CORE_ALLAY.get()));
        assertTrue(helper, tile.hasCore(), "Crucible has no core after setup");

        // Sneak and right-click the crucible with an empty hand
        var player = makeMockServerPlayer(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setShiftKeyDown(true);
        var result = player.gameMode.useItemOn(player, helper.getLevel(), ItemStack.EMPTY, InteractionHand.MAIN_HAND, hitTop(helper, pos));

        // The core pops out as an item entity on the clicked face rather than going to the player, and the crucible goes dark
        assertTrue(helper, result.consumesAction(), "Sneaking empty-hand use on a cored crucible was not consumed: " + result);
        assertFalse(helper, tile.hasCore(), "Crucible still has a core after sneaking empty-hand use");
        assertValueEqual(helper, false, helper.getBlockState(pos).getValue(SanguineCrucibleBlock.LIT), "Crucible lit state after core removal");
        assertTrue(helper, player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty(), "Player hand not empty after core removal");
        helper.assertItemEntityCountIs(ItemsPM.SANGUINE_CORE_ALLAY.get(), pos.above(), 1.0D, 1);
        helper.succeed();
    }

    public static void bloodletter_filled_by_empty_hand(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        helper.setBlock(pos, BlocksPM.BLOODLETTER.get());
        assertValueEqual(helper, false, helper.getBlockState(pos).getValue(BloodletterBlock.FILLED), "Bloodletter filled state after placement");

        // Use a survival player, since the test server's default creative mode is invulnerable. Mock players also start
        // in the client-load grace period, which makes them invulnerable, so end it by sending the packet a client sends
        // once it has loaded.
        var player = makeMockServerPlayer(helper);
        player.setGameMode(GameType.SURVIVAL);
        player.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        assertValueEqual(helper, 20.0F, player.getHealth(), "Player health before use");

        // Right-click the empty bloodletter with an empty hand
        var result = player.gameMode.useItemOn(player, helper.getLevel(), ItemStack.EMPTY, InteractionHand.MAIN_HAND, hitTop(helper, pos));

        // The bloodletter cuts the player for 2 points of bleeding damage and fills
        assertTrue(helper, result.consumesAction(), "Empty-hand use on an empty bloodletter was not consumed: " + result);
        assertValueEqual(helper, true, helper.getBlockState(pos).getValue(BloodletterBlock.FILLED), "Bloodletter filled state after empty-hand use");
        assertValueEqual(helper, 18.0F, player.getHealth(), "Player health after use");
        helper.succeed();
    }
}
