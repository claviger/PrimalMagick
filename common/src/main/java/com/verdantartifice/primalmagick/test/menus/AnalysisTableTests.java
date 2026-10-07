package com.verdantartifice.primalmagick.test.menus;

import com.verdantartifice.primalmagick.common.affinities.AffinityManager;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.menus.AnalysisTableMenu;
import com.verdantartifice.primalmagick.common.research.KnowledgeType;
import com.verdantartifice.primalmagick.common.research.ResearchEntries;
import com.verdantartifice.primalmagick.common.research.ResearchManager;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.common.sources.SourceList;
import com.verdantartifice.primalmagick.platform.Services;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import com.verdantartifice.primalmagick.test.RecordingServerPlayer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

/**
 * Tests for the analysis table menu's scan action. Scanned items are given pre-cached affinities matching their real
 * values, so the synchronous scanned check agrees with the affinities the scan itself calculates.
 */
public class AnalysisTableTests extends AbstractBaseTest {
    private static final int INPUT_SLOT = 0;
    private static final int LAST_SCANNED_SLOT = 1;

    // Diamond has 20 earth affinity (set in AffinityProvider), worth 0.5 observation points per point of affinity:
    // ceil(sqrt(20 * 0.5)) = 4 observation points. Cache that same value so the synchronous scanned check agrees.
    private static final Item SCANNED_ITEM = Items.DIAMOND;
    private static final int SCANNED_ITEM_OBSERVATION_POINTS = 4;

    private static void cacheScannedItemAffinities() {
        AffinityManager.getInstance().setCachedItemResult(new ItemStack(SCANNED_ITEM), CompletableFuture.completedFuture(SourceList.builder().withEarth(20).build()));
    }

    public static void analysis_table_consumes_scanned_item(GameTestHelper helper) {
        var player = RecordingServerPlayer.create(helper, false);
        cacheScannedItemAffinities();
        var menu = new AnalysisTableMenu(1, player.getInventory(), ContainerLevelAccess.NULL);
        menu.getSlot(INPUT_SLOT).set(new ItemStack(SCANNED_ITEM));
        assertFalse(helper, ResearchManager.isScanned(new ItemStack(SCANNED_ITEM), player), "Item already scanned by a new player");

        // Press the table's scan button
        menu.doScan();

        // The item leaves the input slot, is shown in the last-scanned slot, and is marked as scanned
        assertTrue(helper, menu.getSlot(INPUT_SLOT).getItem().isEmpty(), "Input slot not emptied by the scan");
        assertTrue(helper, menu.getSlot(LAST_SCANNED_SLOT).getItem().is(SCANNED_ITEM), "Scanned item not moved to the last-scanned slot");
        helper.succeedWhen(() -> assertTrue(helper, ResearchManager.isScanned(new ItemStack(SCANNED_ITEM), player), "Item not marked as scanned after the scan"));
    }

    public static void analysis_table_scan_grants_observation_progress(GameTestHelper helper) {
        var player = RecordingServerPlayer.create(helper, false);
        cacheScannedItemAffinities();
        var knowledge = Services.CAPABILITIES.knowledge(player).orElse(null);
        assertTrue(helper, knowledge != null, "Player has no knowledge capability");
        assertValueEqual(helper, 0, knowledge.getKnowledgeRaw(KnowledgeType.OBSERVATION), "Starting observation points");
        var menu = new AnalysisTableMenu(1, player.getInventory(), ContainerLevelAccess.NULL);
        menu.getSlot(INPUT_SLOT).set(new ItemStack(SCANNED_ITEM));

        menu.doScan();

        helper.succeedWhen(() -> assertValueEqual(helper, SCANNED_ITEM_OBSERVATION_POINTS, knowledge.getKnowledgeRaw(KnowledgeType.OBSERVATION), "Observation points after the scan"));
    }

    public static void analysis_table_refuses_hallowed_orb(GameTestHelper helper) {
        var player = RecordingServerPlayer.create(helper, false);
        var menu = new AnalysisTableMenu(1, player.getInventory(), ContainerLevelAccess.NULL);
        menu.getSlot(INPUT_SLOT).set(new ItemStack(ItemsPM.HALLOWED_ORB.get()));

        menu.doScan();

        // The player is told why, the orb stays where it was, and nothing about it is learned
        assertTrue(helper, player.hasMessage("event.primalmagick.analysis_table.forbidden"), "Player was not told the orb can't be analyzed");
        assertTrue(helper, menu.getSlot(INPUT_SLOT).getItem().is(ItemsPM.HALLOWED_ORB.get()), "Orb was removed from the input slot");
        assertTrue(helper, menu.getSlot(LAST_SCANNED_SLOT).getItem().isEmpty(), "Orb was moved to the last-scanned slot");
        assertFalse(helper, Sources.HALLOWED.isDiscovered(player), "Hallowed source discovered by analyzing the orb");
        assertFalse(helper, ResearchManager.isResearchComplete(player, ResearchEntries.DISCOVER_HALLOWED), "Discover Hallowed research complete after analyzing the orb");
        helper.succeed();
    }
}
