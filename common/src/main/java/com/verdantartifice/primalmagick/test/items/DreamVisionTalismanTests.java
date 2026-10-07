package com.verdantartifice.primalmagick.test.items;

import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.items.misc.DreamVisionTalismanItem;
import com.verdantartifice.primalmagick.common.research.KnowledgeType;
import com.verdantartifice.primalmagick.platform.Services;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import com.verdantartifice.primalmagick.test.TestUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

/**
 * Tests for the dream vision talisman, which stores picked-up experience (up to 64) and turns a full charge into an
 * observation when its holder wakes.
 */
public class DreamVisionTalismanTests extends AbstractBaseTest {
    private static ItemStack giveTalisman(Player player) {
        var stack = new ItemStack(ItemsPM.DREAM_VISION_TALISMAN.get());
        player.getInventory().setItem(0, stack);
        return stack;
    }

    private static DreamVisionTalismanItem talismanItem(GameTestHelper helper, ItemStack stack) {
        return assertInstanceOf(helper, stack.getItem(), DreamVisionTalismanItem.class, "Item is not a talisman");
    }

    private static ExperienceOrb makeOrb(GameTestHelper helper, int value) {
        return new ExperienceOrb(helper.getLevel(), helper.absoluteVec(Vec3.atBottomCenterOf(BlockPos.ZERO.above())), Vec3.ZERO, value);
    }

    /** Touching an experience orb stores its value in an active talisman instead of giving the player the experience. */
    public static void talisman_absorbs_experience_from_orbs(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var stack = giveTalisman(player);
        var orb = makeOrb(helper, 10);

        orb.playerTouch(player);

        assertValueEqual(helper, 10, talismanItem(helper, stack).getStoredExp(stack), "Experience stored in the talisman");
        assertValueEqual(helper, 0, player.totalExperience, "Experience the player received");
        helper.succeed();
    }

    /** A talisman that has been switched off leaves the orb alone, so the player gets the experience. */
    public static void disabled_talisman_does_not_absorb_experience(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var stack = giveTalisman(player);
        var talisman = talismanItem(helper, stack);
        talisman.setActive(stack, false);
        var orb = makeOrb(helper, 10);

        orb.playerTouch(player);

        assertValueEqual(helper, 0, talisman.getStoredExp(stack), "Experience stored in a disabled talisman");
        assertValueEqual(helper, 10, player.totalExperience, "Experience the player received");
        helper.succeed();
    }

    /**
     * Waking after sleeping with a talisman that holds its full 64 experience grants one observation, which takes 16
     * knowledge points, empties the talisman, and costs it one durability.
     */
    public static void full_talisman_grants_observation_on_waking(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stack = giveTalisman(player);
        var talisman = talismanItem(helper, stack);
        assertValueEqual(helper, 0, talisman.addStoredExp(stack, 64), "Experience that did not fit in the talisman");
        assertTrue(helper, talisman.isReadyToDrain(stack), "Full talisman is not ready to drain");
        var knowledge = Services.CAPABILITIES.knowledge(player).orElseThrow();
        assertValueEqual(helper, 0, knowledge.getKnowledgeRaw(KnowledgeType.OBSERVATION), "Starting observation points");

        // Place a bed and sleep in it
        BlockPos bedPos = new BlockPos(2, 2, 2);
        TestUtils.placeBed(helper, bedPos);
        player.startSleepInBed(helper.absolutePos(bedPos));
        player.stopSleeping();

        helper.succeedWhen(() -> {
            assertValueEqual(helper, 16, knowledge.getKnowledgeRaw(KnowledgeType.OBSERVATION), "Observation points after waking");
            assertValueEqual(helper, 0, talisman.getStoredExp(stack), "Experience left in the talisman after waking");
            assertValueEqual(helper, 1, stack.getDamageValue(), "Talisman damage after waking");
        });
    }

    /** A talisman that is not yet full grants nothing when its holder wakes. */
    public static void partial_talisman_grants_nothing_on_waking(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stack = giveTalisman(player);
        var talisman = talismanItem(helper, stack);
        talisman.addStoredExp(stack, 63);
        var knowledge = Services.CAPABILITIES.knowledge(player).orElseThrow();

        BlockPos bedPos = new BlockPos(2, 2, 2);
        TestUtils.placeBed(helper, bedPos);
        player.startSleepInBed(helper.absolutePos(bedPos));
        player.stopSleeping();

        helper.runAfterDelay(10, () -> {
            assertValueEqual(helper, 0, knowledge.getKnowledgeRaw(KnowledgeType.OBSERVATION), "Observation points after waking with a partial talisman");
            assertValueEqual(helper, 63, talisman.getStoredExp(stack), "Experience left in the partial talisman");
            helper.succeed();
        });
    }
}
