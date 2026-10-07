package com.verdantartifice.primalmagick.test.entities;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.entities.EntityTypesPM;
import com.verdantartifice.primalmagick.common.entities.companions.CompanionManager;
import com.verdantartifice.primalmagick.common.entities.golems.PrimaliteGolemEntity;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.research.ResearchEntries;
import com.verdantartifice.primalmagick.common.research.ResearchManager;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

/**
 * Tests for primalite golems: forming one from blocks with a wand, commanding it to stay or follow, repairing it, and
 * the one-golem limit per owner.
 */
public class GolemTests extends AbstractBaseTest {
    private static final BlockPos GOLEM_POS = new BlockPos(1, 1, 1);

    public static void primalite_golem_forms_from_t_pattern_and_wand(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        ResearchManager.forceGrantWithAllParents(player, ResearchEntries.PRIMALITE_GOLEM);

        // Primalite blocks in a T (a row of three over a single block) topped by the controller above the middle of the row
        Block primalite = BlocksPM.PRIMALITE_BLOCK.get();
        helper.setBlock(new BlockPos(0, 1, 1), primalite);
        helper.setBlock(new BlockPos(1, 1, 1), primalite);
        helper.setBlock(new BlockPos(2, 1, 1), primalite);
        helper.setBlock(new BlockPos(1, 0, 1), primalite);
        BlockPos controllerPos = new BlockPos(1, 2, 1);
        helper.setBlock(controllerPos, BlocksPM.PRIMALITE_GOLEM_CONTROLLER.get());

        var controller = BlocksPM.PRIMALITE_GOLEM_CONTROLLER.get();
        var result = controller.onWandRightClick(new ItemStack(ItemsPM.MUNDANE_WAND.get()), helper.getLevel(), player, helper.absolutePos(controllerPos), Direction.UP);

        assertValueEqual(helper, InteractionResult.SUCCESS, result, "Wand use result on the controller");
        helper.assertEntityPresent(EntityTypesPM.PRIMALITE_GOLEM.get());
        helper.assertBlockNotPresent(primalite, new BlockPos(1, 1, 1));
        helper.assertBlockNotPresent(BlocksPM.PRIMALITE_GOLEM_CONTROLLER.get(), controllerPos);
        helper.succeed();
    }

    private static PrimaliteGolemEntity spawnOwnedGolem(GameTestHelper helper, ServerPlayer player) {
        var golem = helper.spawnWithNoFreeWill(EntityTypesPM.PRIMALITE_GOLEM.get(), GOLEM_POS);
        CompanionManager.addCompanion(player, golem);
        return golem;
    }

    public static void golem_stays_and_follows_when_right_clicked(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var golem = spawnOwnedGolem(helper, player);
        assertFalse(helper, golem.isCompanionStaying(), "New golem is staying");

        golem.interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);
        assertTrue(helper, golem.isCompanionStaying(), "Golem not staying after the first click");

        // Clicks in the same game tick are ignored, so wait a couple of ticks
        helper.runAfterDelay(2, () -> {
            golem.interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);
            assertFalse(helper, golem.isCompanionStaying(), "Golem still staying after the second click");
            helper.succeed();
        });
    }

    public static void golem_is_repaired_with_primalite_ingot(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        player.setGameMode(GameType.SURVIVAL);
        var golem = spawnOwnedGolem(helper, player);
        golem.setHealth(50.0F);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemsPM.PRIMALITE_INGOT.get(), 2));

        var result = golem.interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);

        // A primalite golem heals 25 points per ingot
        assertTrue(helper, result.consumesAction(), "Repair click was not consumed: " + result);
        assertValueEqual(helper, 75.0F, golem.getHealth(), "Golem health after repair");
        assertValueEqual(helper, 1, player.getItemInHand(InteractionHand.MAIN_HAND).getCount(), "Ingots remaining after repair");
        helper.succeed();
    }

    public static void activating_second_golem_destroys_first(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var first = spawnOwnedGolem(helper, player);
        var second = helper.spawnWithNoFreeWill(EntityTypesPM.PRIMALITE_GOLEM.get(), new BlockPos(2, 1, 1));

        CompanionManager.addCompanion(player, second);

        helper.succeedWhen(() -> {
            assertTrue(helper, first.isRemoved(), "First golem still present");
            assertTrue(helper, second.isAlive(), "Second golem not alive");
        });
    }
}
