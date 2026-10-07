package com.verdantartifice.primalmagick.test.items;

import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import com.verdantartifice.primalmagick.test.util.RecordingServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;

/**
 * Tests for the recall stone, which sends its user back to their respawn point.
 */
public class RecallStoneTests extends AbstractBaseTest {
    private static final String CANNOT_CROSS_DIMENSIONS_KEY = "event.primalmagick.recall_stone.cannot_cross_dimensions";

    private static final BlockPos START_POS = new BlockPos(1, 1, 1);
    private static final BlockPos RESPAWN_POS = new BlockPos(3, 1, 3);

    /**
     * Creates a survival player standing at the start position, holding two recall stones, and with a forced respawn
     * point at the given relative position in the given dimension.
     */
    private static RecordingServerPlayer makePlayer(GameTestHelper helper, ResourceKey<Level> respawnDimension, BlockPos respawnPos) {
        var player = RecordingServerPlayer.create(helper, true);
        player.setGameMode(GameType.SURVIVAL);
        Vec3 start = helper.absoluteVec(Vec3.atBottomCenterOf(START_POS));
        player.snapTo(start.x, start.y, start.z, 0F, 0F);
        player.setRespawnPosition(new ServerPlayer.RespawnConfig(LevelData.RespawnData.of(respawnDimension, helper.absolutePos(respawnPos), 0F, 0F), true), false);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemsPM.RECALL_STONE.get(), 2));
        return player;
    }

    private static void useStone(GameTestHelper helper, ServerPlayer player) {
        var stone = player.getItemInHand(InteractionHand.MAIN_HAND);
        stone.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
    }

    /**
     * Using a recall stone moves the player to their respawn point and uses up one stone. A forced respawn point in an
     * open space puts the player at the center of the block, a tenth of a block up, as in vanilla's respawn logic.
     */
    public static void recall_stone_teleports_player_to_respawn_point(GameTestHelper helper) {
        var player = makePlayer(helper, helper.getLevel().dimension(), RESPAWN_POS);
        Vec3 start = player.position();
        Vec3 expected = helper.absoluteVec(new Vec3(RESPAWN_POS.getX() + 0.5D, RESPAWN_POS.getY() + 0.1D, RESPAWN_POS.getZ() + 0.5D));

        useStone(helper, player);

        assertTrue(helper, player.position().distanceTo(expected) < 0.01D, "Player not at the respawn point; expected " + expected + " from " + start + " but found " + player.position());
        assertValueEqual(helper, 1, player.getItemInHand(InteractionHand.MAIN_HAND).getCount(), "Recall stones left in hand");
        helper.succeed();
    }

    /**
     * A recall stone does nothing but show an error message when the player's respawn point is in another dimension,
     * and the stone is not used up.
     */
    public static void recall_stone_fails_when_respawn_is_in_another_dimension(GameTestHelper helper) {
        var player = makePlayer(helper, Level.NETHER, RESPAWN_POS);
        Vec3 start = player.position();

        useStone(helper, player);

        assertValueEqual(helper, start, player.position(), "Player position after using a recall stone with a respawn point in another dimension");
        assertTrue(helper, player.hasOverlayMessage(CANNOT_CROSS_DIMENSIONS_KEY), "Player was not told that recall stones cannot cross dimensions");
        assertValueEqual(helper, 2, player.getItemInHand(InteractionHand.MAIN_HAND).getCount(), "Recall stones left in hand");
        helper.succeed();
    }
}
