package com.verdantartifice.primalmagick.test.items;

import com.mojang.authlib.GameProfile;
import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.components.DataComponentsPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.tiles.rituals.OfferingPedestalTileEntity;
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
import net.minecraft.world.item.Items;
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

    // Altar scans run once every 20 ticks, so wait out a full period before dowsing anything that depends on a scan
    private static final int SCAN_WAIT_TICKS = 30;
    private static final String STABILITY_KEY_PREFIX = "event.primalmagick.dowsing_rod.altar_stability.";
    private static final String SALT_ACTIVE_KEY = "event.primalmagick.dowsing_rod.salt_connection.active";
    private static final String SALT_INACTIVE_KEY = "event.primalmagick.dowsing_rod.salt_connection.inactive";
    private static final String SYMMETRY_FOUND_KEY = "event.primalmagick.dowsing_rod.symmetry.found";
    private static final String SYMMETRY_NOT_FOUND_KEY = "event.primalmagick.dowsing_rod.symmetry.not_found";

    private static final BlockPos ALTAR_POS = new BlockPos(2, 1, 2);

    private static void dowse(GameTestHelper helper, RecordingPlayer player, BlockPos relativePos) {
        BlockPos absPos = helper.absolutePos(relativePos);
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        stack.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(absPos.getCenter(), Direction.UP, absPos, false)));
    }

    private static void placeBrazier(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, BlocksPM.INCENSE_BRAZIER.get());
    }

    /**
     * An altar with nothing around it has a stability change of zero per tick, which is below the rod's 0.025
     * threshold for a good or poor reading, so the rod reports neutral.
     */
    public static void dowsing_rod_on_altar_reports_neutral_stability(GameTestHelper helper) {
        helper.setBlock(ALTAR_POS, BlocksPM.RITUAL_ALTAR.get());
        RecordingPlayer player = makeRecordingPlayer(helper);

        dowse(helper, player, ALTAR_POS);

        assertTrue(helper, player.hasMessage(STABILITY_KEY_PREFIX + "neutral"), "Dowsing rod did not report neutral stability for a bare altar");
        helper.succeed();
    }

    /**
     * Four salted incense braziers around the altar form two symmetric pairs. Each brazier adds a stability bonus of
     * 0.01, diminished by a factor of 0.75 for each earlier brazier, which totals 0.01 + 0.0075 + 0.005625 + 0.004219 =
     * 0.0273. That is above the 0.025 threshold for a good reading and below 0.15 for a very good one.
     */
    public static void dowsing_rod_on_altar_reports_good_stability(GameTestHelper helper) {
        helper.setBlock(ALTAR_POS, BlocksPM.RITUAL_ALTAR.get());
        placeBrazier(helper, ALTAR_POS.east());
        placeBrazier(helper, ALTAR_POS.west());
        placeBrazier(helper, ALTAR_POS.south());
        placeBrazier(helper, ALTAR_POS.north());
        RecordingPlayer player = makeRecordingPlayer(helper);

        helper.runAfterDelay(SCAN_WAIT_TICKS, () -> {
            dowse(helper, player, ALTAR_POS);
            assertTrue(helper, player.hasMessage(STABILITY_KEY_PREFIX + "good"), "Dowsing rod did not report good stability for a symmetric altar");
            helper.succeed();
        });
    }

    /**
     * Three salted incense braziers with no symmetric partners cost 0.01 of stability each, for a total of -0.03. That
     * is at or below the -0.025 threshold for a poor reading and above -0.15 for a very poor one. One brazier is next
     * to the altar and two are at the ends of salt trails running east and south from it. The mirror position of the
     * west brazier holds salt and the other two mirror positions are empty, so none of them is matched.
     */
    public static void dowsing_rod_on_altar_reports_poor_stability(GameTestHelper helper) {
        helper.setBlock(ALTAR_POS, BlocksPM.RITUAL_ALTAR.get());
        helper.setBlock(ALTAR_POS.east(), BlocksPM.SALT_TRAIL.get());
        placeBrazier(helper, ALTAR_POS.east(2));
        helper.setBlock(ALTAR_POS.south(), BlocksPM.SALT_TRAIL.get());
        placeBrazier(helper, ALTAR_POS.south(2));
        placeBrazier(helper, ALTAR_POS.west());
        RecordingPlayer player = makeRecordingPlayer(helper);

        helper.runAfterDelay(SCAN_WAIT_TICKS, () -> {
            dowse(helper, player, ALTAR_POS);
            assertTrue(helper, player.hasMessage(STABILITY_KEY_PREFIX + "poor"), "Dowsing rod did not report poor stability for an asymmetric altar");
            helper.succeed();
        });
    }

    /**
     * A brazier directly beside the altar receives salt power from it, and a brazier on the opposite side matches it
     * in the mirrored position, so the rod reports both an active salt connection and symmetry.
     */
    public static void dowsing_rod_on_prop_reports_salt_and_symmetry(GameTestHelper helper) {
        helper.setBlock(ALTAR_POS, BlocksPM.RITUAL_ALTAR.get());
        placeBrazier(helper, ALTAR_POS.east());
        placeBrazier(helper, ALTAR_POS.west());
        RecordingPlayer player = makeRecordingPlayer(helper);

        helper.runAfterDelay(SCAN_WAIT_TICKS, () -> {
            dowse(helper, player, ALTAR_POS.east());
            assertTrue(helper, player.hasMessage(SALT_ACTIVE_KEY), "Dowsing rod did not report an active salt connection");
            assertFalse(helper, player.hasMessage(SALT_INACTIVE_KEY), "Dowsing rod reported an inactive salt connection");
            assertTrue(helper, player.hasMessage(SYMMETRY_FOUND_KEY), "Dowsing rod did not report symmetry");
            assertFalse(helper, player.hasMessage(SYMMETRY_NOT_FOUND_KEY), "Dowsing rod reported missing symmetry");
            helper.succeed();
        });
    }

    /**
     * A salted brazier with nothing in its mirrored position is reported as asymmetric.
     */
    public static void dowsing_rod_on_prop_reports_missing_symmetry(GameTestHelper helper) {
        helper.setBlock(ALTAR_POS, BlocksPM.RITUAL_ALTAR.get());
        placeBrazier(helper, ALTAR_POS.east());
        RecordingPlayer player = makeRecordingPlayer(helper);

        helper.runAfterDelay(SCAN_WAIT_TICKS, () -> {
            dowse(helper, player, ALTAR_POS.east());
            assertTrue(helper, player.hasMessage(SALT_ACTIVE_KEY), "Dowsing rod did not report an active salt connection");
            assertTrue(helper, player.hasMessage(SYMMETRY_NOT_FOUND_KEY), "Dowsing rod did not report missing symmetry");
            assertFalse(helper, player.hasMessage(SYMMETRY_FOUND_KEY), "Dowsing rod reported symmetry with an empty mirror position");
            helper.succeed();
        });
    }

    /**
     * A brazier that no salt reaches is reported as unconnected, and with no altar having claimed it there is no
     * mirror position to check either.
     */
    public static void dowsing_rod_on_unconnected_prop_reports_inactive_salt(GameTestHelper helper) {
        var brazierPos = new BlockPos(1, 1, 1);
        placeBrazier(helper, brazierPos);
        RecordingPlayer player = makeRecordingPlayer(helper);

        dowse(helper, player, brazierPos);

        assertTrue(helper, player.hasMessage(SALT_INACTIVE_KEY), "Dowsing rod did not report an inactive salt connection");
        assertFalse(helper, player.hasMessage(SALT_ACTIVE_KEY), "Dowsing rod reported an active salt connection");
        assertTrue(helper, player.hasMessage(SYMMETRY_NOT_FOUND_KEY), "Dowsing rod did not report missing symmetry");
        helper.succeed();
    }

    /**
     * Offering pedestals are checked the same way as props: a salted pedestal mirrored by an empty one on the other
     * side of the altar reports salt and symmetry, and the report changes once one of the pair holds an item.
     */
    public static void dowsing_rod_on_pedestal_reports_salt_and_symmetry(GameTestHelper helper) {
        helper.setBlock(ALTAR_POS, BlocksPM.RITUAL_ALTAR.get());
        helper.setBlock(ALTAR_POS.east(), BlocksPM.OFFERING_PEDESTAL.get());
        helper.setBlock(ALTAR_POS.west(), BlocksPM.OFFERING_PEDESTAL.get());
        RecordingPlayer player = makeRecordingPlayer(helper);

        helper.runAfterDelay(SCAN_WAIT_TICKS, () -> {
            // Two empty pedestals mirror each other
            dowse(helper, player, ALTAR_POS.east());
            assertTrue(helper, player.hasMessage(SALT_ACTIVE_KEY), "Dowsing rod did not report an active salt connection for the pedestal");
            assertTrue(helper, player.hasMessage(SYMMETRY_FOUND_KEY), "Dowsing rod did not report symmetry for matching empty pedestals");
            assertFalse(helper, player.hasMessage(SYMMETRY_NOT_FOUND_KEY), "Dowsing rod reported missing symmetry for matching empty pedestals");

            // A full pedestal opposite an empty one is a mismatch
            player.messages.clear();
            helper.getBlockEntity(ALTAR_POS.west(), OfferingPedestalTileEntity.class).addItem(new ItemStack(Items.APPLE));
            dowse(helper, player, ALTAR_POS.east());
            assertTrue(helper, player.hasMessage(SYMMETRY_NOT_FOUND_KEY), "Dowsing rod did not report missing symmetry for mismatched pedestals");
            assertFalse(helper, player.hasMessage(SYMMETRY_FOUND_KEY), "Dowsing rod reported symmetry for mismatched pedestals");
            helper.succeed();
        });
    }
}
