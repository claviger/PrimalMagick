package com.verdantartifice.primalmagick.test.blocks;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.spells.payloads.TeleportSpellPayload;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import com.verdantartifice.primalmagick.test.util.RecordingServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Tests for the enderward, which blocks teleports that arrive within 16 blocks of it. Uses the floor template.
 */
public class EnderwardTests extends AbstractBaseTest {
    private static final String BLOCK_KEY = "event.primalmagick.enderward.block";
    private static final BlockPos START_POS = new BlockPos(1, 1, 1);
    private static final BlockPos DEST_POS = new BlockPos(3, 1, 3);
    private static final int WAIT_TICKS = 40;

    /** Places an enderward at 4,1,1 on a supporting block, which is well clear of the 3,y,3 landing column. */
    private static void placeWard(GameTestHelper helper) {
        helper.setBlock(new BlockPos(3, 1, 1), Blocks.STONE);
        helper.setBlock(new BlockPos(4, 1, 1), BlocksPM.ENDERWARD.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.EAST));
        helper.assertBlockPresent(BlocksPM.ENDERWARD.get(), new BlockPos(4, 1, 1));
    }

    private static RecordingServerPlayer makePlayer(GameTestHelper helper) {
        var player = RecordingServerPlayer.create(helper, true);
        Vec3 start = helper.absoluteVec(Vec3.atBottomCenterOf(START_POS));
        player.snapTo(start.x, start.y, start.z, 0F, 0F);
        return player;
    }

    private static Vec3 destination(GameTestHelper helper) {
        return helper.absoluteVec(Vec3.atBottomCenterOf(DEST_POS));
    }

    private static void throwPearl(GameTestHelper helper, ServerPlayer player) {
        var pearl = new ThrownEnderpearl(helper.getLevel(), player, new ItemStack(Items.ENDER_PEARL));
        Vec3 dest = destination(helper);
        pearl.setPos(dest.x, dest.y + 2D, dest.z);
        pearl.setDeltaMovement(0D, -0.5D, 0D);
        helper.getLevel().addFreshEntity(pearl);
    }

    /** A pearl landing within the ward's radius does not teleport its owner, who is told why. */
    public static void enderward_blocks_ender_pearl(GameTestHelper helper) {
        placeWard(helper);
        var player = makePlayer(helper);
        Vec3 start = player.position();
        throwPearl(helper, player);
        helper.runAfterDelay(WAIT_TICKS, () -> {
            assertValueEqual(helper, start, player.position(), "Player position after a pearl landed inside an enderward");
            assertTrue(helper, player.hasOverlayMessage(BLOCK_KEY), "Player was not told that the enderward blocked the teleport");
            helper.succeed();
        });
    }

    /** Chorus fruit eaten within the ward's radius does not move the eater, who is told why. */
    public static void enderward_blocks_chorus_fruit(GameTestHelper helper) {
        placeWard(helper);
        var player = makePlayer(helper);
        Vec3 start = player.position();

        new ItemStack(Items.CHORUS_FRUIT).finishUsingItem(helper.getLevel(), player);

        assertValueEqual(helper, start, player.position(), "Player position after eating chorus fruit inside an enderward");
        assertTrue(helper, player.hasOverlayMessage(BLOCK_KEY), "Player was not told that the enderward blocked the teleport");
        helper.succeed();
    }

    private static void castTeleport(GameTestHelper helper, ServerPlayer player) {
        Vec3 dest = destination(helper);
        var hit = new BlockHitResult(dest, Direction.UP, helper.absolutePos(DEST_POS), false);
        TeleportSpellPayload.getInstance().execute(hit, null, null, helper.getLevel(), player, ItemStack.EMPTY, null);
    }

    /** A teleport spell aimed within the ward's radius leaves the caster in place. */
    public static void enderward_blocks_teleport_spell(GameTestHelper helper) {
        placeWard(helper);
        var player = makePlayer(helper);
        Vec3 start = player.position();

        castTeleport(helper, player);

        assertValueEqual(helper, start, player.position(), "Caster position after a teleport spell into an enderward");
        assertTrue(helper, player.hasOverlayMessage(BLOCK_KEY), "Caster was not told that the enderward blocked the teleport");
        helper.succeed();
    }

    /** Recall stones teleport directly rather than through the ender teleport event, so a ward at the respawn point does not stop them. */
    public static void enderward_does_not_block_recall_stone(GameTestHelper helper) {
        placeWard(helper);
        var player = makePlayer(helper);
        player.setGameMode(GameType.SURVIVAL);
        player.setRespawnPosition(new ServerPlayer.RespawnConfig(LevelData.RespawnData.of(helper.getLevel().dimension(), helper.absolutePos(DEST_POS), 0F, 0F), true), false);
        var stone = new ItemStack(ItemsPM.RECALL_STONE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stone);
        // Forced respawn puts the player at the block center, a tenth of a block up
        Vec3 expected = helper.absoluteVec(new Vec3(DEST_POS.getX() + 0.5D, DEST_POS.getY() + 0.1D, DEST_POS.getZ() + 0.5D));

        stone.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);

        assertTrue(helper, player.position().distanceTo(expected) < 0.01D, "Player not at the respawn point; at " + player.position());
        assertFalse(helper, player.hasOverlayMessage(BLOCK_KEY), "Enderward reported blocking a recall stone");
        helper.succeed();
    }
}
