package com.verdantartifice.primalmagick.test.items;

import com.mojang.authlib.GameProfile;
import com.verdantartifice.primalmagick.common.components.DataComponentsPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DowsingRodTests extends AbstractBaseTest {
    private static final String RECORD_KEY = "event.primalmagick.dowsing_rod.position.record";
    private static final String CLEAR_KEY = "event.primalmagick.dowsing_rod.position.clear";

    private record SentMessage(Component message) {
        boolean hasKey(String key) {
            return this.message.getContents() instanceof TranslatableContents contents && contents.getKey().equals(key);
        }
    }

    /**
     * A mock server player that records every system message sent to it.
     */
    private static class RecordingPlayer extends ServerPlayer {
        final List<SentMessage> messages = new ArrayList<>();

        RecordingPlayer(ServerLevel level, CommonListenerCookie cookie) {
            super(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation());
        }

        @Override
        public boolean isSpectator() {
            return false;
        }

        @Override
        public boolean isCreative() {
            return true;
        }

        @Override
        public void sendSystemMessage(Component message, boolean overlay) {
            this.messages.add(new SentMessage(message));
            super.sendSystemMessage(message, overlay);
        }

        boolean hasMessage(String key) {
            return this.messages.stream().anyMatch(m -> m.hasKey(key));
        }
    }

    private static RecordingPlayer makeRecordingPlayer(GameTestHelper helper) {
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-mock-player"), false);
        RecordingPlayer player = new RecordingPlayer(helper.getLevel(), cookie);
        attachMockConnection(helper, player, cookie, false);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemsPM.DOWSING_ROD.get()));
        return player;
    }

    private static void placePlayerAbove(GameTestHelper helper, ServerPlayer player, BlockPos relativePos, float xRot) {
        Vec3 feet = helper.absoluteVec(Vec3.atBottomCenterOf(relativePos.above()));
        player.snapTo(feet.x, feet.y, feet.z, 0F, xRot);
    }

    public static void dowsing_rod_block_click_does_not_clear_positions(GameTestHelper helper) {
        // Stand a sneaking player on a plain block, looking straight down at it with a dowsing rod in hand
        BlockPos targetPos = BlockPos.ZERO;
        helper.setBlock(targetPos, Blocks.STONE);
        RecordingPlayer player = makeRecordingPlayer(helper);
        placePlayerAbove(helper, player, targetPos, 90F);
        player.setShiftKeyDown(true);
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        BlockPos absTargetPos = helper.absolutePos(targetPos);

        // Use the rod on the block, then follow up with the item use call that the client sends when useOn passes
        var context = new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(absTargetPos.getCenter(), Direction.UP, absTargetPos, false));
        stack.getItem().useOn(context);
        assertValueEqual(helper, absTargetPos, stack.get(DataComponentsPM.DOWSING_PRIMARY_POSITION.get()), "Dowsed position not recorded");
        stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);

        helper.succeedIf(() -> {
            assertValueEqual(helper, absTargetPos, stack.get(DataComponentsPM.DOWSING_PRIMARY_POSITION.get()), "Dowsed position was cleared by the follow-up use call");
            assertTrue(helper, player.hasMessage(RECORD_KEY), "Dowsing rod did not report the recorded position");
            assertFalse(helper, player.hasMessage(CLEAR_KEY), "Dowsing rod reported clearing positions after a block click");
        });
    }

    public static void dowsing_rod_clears_positions_when_used_on_air(GameTestHelper helper) {
        // Give a sneaking player a dowsing rod with two recorded positions, looking straight up into empty air. The
        // test structure is capped with barriers, so shorten the player's reach to keep the ceiling out of range.
        RecordingPlayer player = makeRecordingPlayer(helper);
        placePlayerAbove(helper, player, BlockPos.ZERO.below(), -90F);
        player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).setBaseValue(1D);
        player.setShiftKeyDown(true);
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        stack.set(DataComponentsPM.DOWSING_PRIMARY_POSITION.get(), helper.absolutePos(BlockPos.ZERO));
        stack.set(DataComponentsPM.DOWSING_SECONDARY_POSITION.get(), helper.absolutePos(BlockPos.ZERO.east()));
        Vec3 eye = player.getEyePosition();
        HitResult hit = helper.getLevel().clip(new ClipContext(eye, eye.add(0D, player.blockInteractionRange(), 0D), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        assertValueEqual(helper, HitResult.Type.MISS, hit.getType(), "Test player is not looking at empty air");

        // Use the rod on empty air
        stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);

        helper.succeedIf(() -> {
            assertFalse(helper, stack.has(DataComponentsPM.DOWSING_PRIMARY_POSITION.get()), "Primary dowsing position not cleared");
            assertFalse(helper, stack.has(DataComponentsPM.DOWSING_SECONDARY_POSITION.get()), "Secondary dowsing position not cleared");
            assertTrue(helper, player.hasMessage(CLEAR_KEY), "Dowsing rod did not report clearing positions");
        });
    }
}
