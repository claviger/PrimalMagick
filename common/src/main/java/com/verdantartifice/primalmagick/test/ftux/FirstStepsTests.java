package com.verdantartifice.primalmagick.test.ftux;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.events.PlayerEvents;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.network.packets.data.SyncProgressPacket;
import com.verdantartifice.primalmagick.common.research.KnowledgeType;
import com.verdantartifice.primalmagick.common.research.ResearchEntries;
import com.verdantartifice.primalmagick.common.research.ResearchManager;
import com.verdantartifice.primalmagick.common.research.keys.ResearchEntryKey;
import com.verdantartifice.primalmagick.common.stats.StatsManager;
import com.verdantartifice.primalmagick.common.stats.StatsPM;
import com.verdantartifice.primalmagick.platform.Services;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import com.verdantartifice.primalmagick.test.RecordingServerPlayer;
import com.verdantartifice.primalmagick.common.crafting.WandTransforms;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 * Tests for the First Steps progression: gating the Arcane Workbench wand transform behind starting the entry,
 * advancing the entry through each of its stage requirements, and the shrine's prompt to siphon with a mundane wand.
 */
public class FirstStepsTests extends AbstractBaseTest {
    private static final ResearchEntryKey FIRST_STEPS_KEY = new ResearchEntryKey(ResearchEntries.FIRST_STEPS);

    private static UseOnContext startWandUse(GameTestHelper helper, Player player, ItemStack wandStack, BlockPos pos) {
        player.setItemInHand(InteractionHand.MAIN_HAND, wandStack);
        BlockPos posAbs = helper.absolutePos(pos);
        BlockHitResult blockHitResult = new BlockHitResult(Vec3.atCenterOf(posAbs), Direction.UP, posAbs, false);
        return new UseOnContext(player, InteractionHand.MAIN_HAND, blockHitResult);
    }

    public static void arcane_workbench_transform_requires_first_steps(GameTestHelper helper) {
        // A fresh player has not started First Steps
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        assertFalse(helper, ResearchManager.isResearchStarted(player, ResearchEntries.FIRST_STEPS), "Fresh player has already started First Steps");

        // Place a crafting table and start using a mundane wand on it
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.CRAFTING_TABLE);
        ItemStack wandStack = new ItemStack(ItemsPM.MUNDANE_WAND.get());
        Item wandItem = wandStack.getItem();
        UseOnContext useContext = startWandUse(helper, player, wandStack, pos);
        assertTrue(helper, Services.ITEMS.onItemUseFirst(wandItem, wandStack, useContext).equals(InteractionResult.PASS), "Wand began transforming a crafting table without First Steps");

        // Channel for longer than the transform takes, then confirm that nothing has changed
        MutableInt remainingTicks = new MutableInt(WandTransforms.CHANNEL_DURATION + 1);
        helper.onEachTick(() -> wandItem.onUseTick(helper.getLevel(), player, wandStack, remainingTicks.decrementAndGet()));
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(Blocks.CRAFTING_TABLE, pos);
            assertTrue(helper, remainingTicks.intValue() < 0, "Not enough channeling ticks have elapsed");
            helper.assertBlockNotPresent(BlocksPM.ARCANE_WORKBENCH.get(), pos);
        });
    }

    public static void arcane_workbench_transform_succeeds_after_first_steps(GameTestHelper helper) {
        // Start First Steps, which is all the transform's requirement (the entry's first stage) asks for
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        assertTrue(helper, ResearchManager.progressResearch(player, ResearchEntries.FIRST_STEPS), "Failed to start First Steps");
        assertFalse(helper, ResearchManager.isResearchComplete(player, ResearchEntries.FIRST_STEPS), "First Steps completed by starting it");

        // Place a crafting table and start using a mundane wand on it
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.CRAFTING_TABLE);
        ItemStack wandStack = new ItemStack(ItemsPM.MUNDANE_WAND.get());
        Item wandItem = wandStack.getItem();
        UseOnContext useContext = startWandUse(helper, player, wandStack, pos);
        assertTrue(helper, Services.ITEMS.onItemUseFirst(wandItem, wandStack, useContext).equals(InteractionResult.SUCCESS), "Failed to start using wand on crafting table");

        // Continue channeling the wand until the crafting table becomes an arcane workbench or the test times out
        MutableInt remainingTicks = new MutableInt(wandItem.getUseDuration(wandStack, player));
        helper.onEachTick(() -> wandItem.onUseTick(helper.getLevel(), player, wandStack, remainingTicks.decrementAndGet()));
        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(Blocks.CRAFTING_TABLE, pos);
            helper.assertBlockPresent(BlocksPM.ARCANE_WORKBENCH.get(), pos);
        });
    }

    private static int stage(Player player) {
        return Services.CAPABILITIES.knowledge(player).map(k -> k.getResearchStage(FIRST_STEPS_KEY)).orElse(-2);
    }

    /**
     * Advances the entry the way the progress packet does: the stage's requirements must be met for the check to pass,
     * and the entry only advances once it has passed.
     */
    private static void assertStageBlocksUntil(GameTestHelper helper, Player player, int expectedStage, String requirement, Runnable satisfy) {
        assertValueEqual(helper, expectedStage, stage(player), "First Steps stage before " + requirement);
        assertFalse(helper, SyncProgressPacket.checkAndConsumePrerequisites(player, FIRST_STEPS_KEY), "Stage " + expectedStage + " passed without " + requirement);
        satisfy.run();
        assertTrue(helper, SyncProgressPacket.checkAndConsumePrerequisites(player, FIRST_STEPS_KEY), "Stage " + expectedStage + " did not pass after " + requirement);
        assertTrue(helper, ResearchManager.progressResearch(player, FIRST_STEPS_KEY), "Failed to progress First Steps after " + requirement);
    }

    public static void first_steps_completes_when_stage_requirements_are_met(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);

        // Starting the entry puts the player at its first stage (index 0)
        assertTrue(helper, ResearchManager.progressResearch(player, FIRST_STEPS_KEY), "Failed to start First Steps");

        // Stage 0 asks for an Arcane Workbench to be crafted
        assertStageBlocksUntil(helper, player, 0, "crafting an arcane workbench",
                () -> PlayerEvents.registerItemCrafted(player, new ItemStack(ItemsPM.ARCANE_WORKBENCH.get())));

        // Stage 1 asks for 10 whole points of mana siphoned from a font
        assertStageBlocksUntil(helper, player, 1, "siphoning 10 mana",
                () -> StatsManager.incrementValue(player, StatsPM.MANA_SIPHONED, 10));

        // Stage 2 asks for one observation made, here by gaining the 16 points that make up one level
        assertStageBlocksUntil(helper, player, 2, "making an observation",
                () -> ResearchManager.addKnowledge(player, KnowledgeType.OBSERVATION, 16));

        // The final stage has no requirements, so the last progression completes the entry
        assertTrue(helper, ResearchManager.isResearchComplete(player, ResearchEntries.FIRST_STEPS), "First Steps not complete after meeting every stage's requirements");
        helper.succeed();
    }

    public static void shrine_prompts_mundane_wand_holder_to_siphon(GameTestHelper helper, Block font) {
        // Put a player carrying a mundane wand next to where the font will be
        var player = RecordingServerPlayer.create(helper, true);
        player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(BlockPos.ZERO)));
        player.getInventory().add(new ItemStack(ItemsPM.MUNDANE_WAND.get()));
        assertFalse(helper, ResearchManager.isResearchComplete(player, ResearchEntries.SIPHON_PROMPT), "Siphon prompt already shown to new player");

        // Place an ancient font within range of the player
        helper.setBlock(new BlockPos(1, 1, 1), font);

        // The font checks for nearby players every ten ticks, and should tell the wand carrier to use the wand on it
        helper.succeedWhen(() -> {
            assertTrue(helper, ResearchManager.isResearchComplete(player, ResearchEntries.SIPHON_PROMPT), "Siphon prompt research not complete");
            assertTrue(helper, player.hasMessage("event.primalmagick.siphon_prompt"), "Player was not sent the siphon prompt message");
            helper.getLevel().getServer().getPlayerList().remove(player);
        });
    }
}
