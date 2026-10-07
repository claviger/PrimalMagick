package com.verdantartifice.primalmagick.test.research;

import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.research.ResearchEntries;
import com.verdantartifice.primalmagick.common.research.ResearchManager;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import com.verdantartifice.primalmagick.test.RecordingServerPlayer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

/**
 * Tests for discovering the forbidden and heavenly sources through items: eating Bloody Flesh, reading Blood-Scrawled
 * Ravings, and scanning a Hallowed Orb.
 */
public class SourceDiscoveryTests extends AbstractBaseTest {
    public static void eating_bloody_flesh_unlocks_blood_source(GameTestHelper helper) {
        var player = RecordingServerPlayer.create(helper, false);
        assertFalse(helper, Sources.BLOOD.isDiscovered(player), "Blood source discovered by a new player");
        var flesh = new ItemStack(ItemsPM.BLOODY_FLESH.get());

        // A player who hasn't started First Steps gets no benefit from the meal
        flesh.getItem().finishUsingItem(flesh.copy(), helper.getLevel(), player);
        assertFalse(helper, Sources.BLOOD.isDiscovered(player), "Blood source discovered before starting First Steps");
        assertFalse(helper, player.hasMessage("event.primalmagick.discover_source.blood"), "Discovery message sent before starting First Steps");

        // Once they've started, eating the flesh discovers the source and tells them so
        assertTrue(helper, ResearchManager.progressResearch(player, ResearchEntries.FIRST_STEPS), "Failed to start First Steps");
        flesh.getItem().finishUsingItem(flesh.copy(), helper.getLevel(), player);
        assertTrue(helper, Sources.BLOOD.isDiscovered(player), "Blood source not discovered after eating bloody flesh");
        assertTrue(helper, ResearchManager.isResearchComplete(player, ResearchEntries.DISCOVER_BLOOD), "Discover Blood research not complete after eating bloody flesh");
        assertTrue(helper, player.hasMessage("event.primalmagick.discover_source.blood"), "Discovery message not sent after eating bloody flesh");
        helper.succeed();
    }

    public static void reading_blood_notes_unlocks_blood_source(GameTestHelper helper) {
        var player = RecordingServerPlayer.create(helper, false);
        player.setGameMode(GameType.SURVIVAL);
        assertFalse(helper, Sources.BLOOD.isDiscovered(player), "Blood source discovered by a new player");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemsPM.BLOOD_NOTES.get(), 2));

        // A player who hasn't started First Steps can't make sense of the notes, and keeps them
        var stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        assertFalse(helper, Sources.BLOOD.isDiscovered(player), "Blood source discovered before starting First Steps");
        assertValueEqual(helper, 2, player.getItemInHand(InteractionHand.MAIN_HAND).getCount(), "Blood notes count after a failed reading");

        // Once they've started, reading discovers the source and uses up one set of notes
        assertTrue(helper, ResearchManager.progressResearch(player, ResearchEntries.FIRST_STEPS), "Failed to start First Steps");
        stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        assertTrue(helper, Sources.BLOOD.isDiscovered(player), "Blood source not discovered after reading blood notes");
        assertTrue(helper, player.hasMessage("event.primalmagick.discover_source.blood.alternate"), "Discovery message not sent after reading blood notes");
        assertValueEqual(helper, 1, player.getItemInHand(InteractionHand.MAIN_HAND).getCount(), "Blood notes count after reading");
        helper.succeed();
    }

    public static void scanning_hallowed_orb_unlocks_hallowed_source(GameTestHelper helper) {
        var player = RecordingServerPlayer.create(helper, false);
        assertFalse(helper, Sources.HALLOWED.isDiscovered(player), "Hallowed source discovered by a new player");

        // Mark the orb as scanned on the server, as the arcanometer's scan packet does
        assertTrue(helper, ResearchManager.setScanned(new ItemStack(ItemsPM.HALLOWED_ORB.get()), player), "Failed to mark the hallowed orb as scanned");

        assertTrue(helper, Sources.HALLOWED.isDiscovered(player), "Hallowed source not discovered after scanning the hallowed orb");
        assertTrue(helper, ResearchManager.isResearchComplete(player, ResearchEntries.DISCOVER_HALLOWED), "Discover Hallowed research not complete after scanning the hallowed orb");
        assertTrue(helper, player.hasMessage("event.primalmagick.discover_source.hallowed"), "Discovery message not sent after scanning the hallowed orb");
        helper.succeed();
    }
}
