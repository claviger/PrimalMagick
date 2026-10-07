package com.verdantartifice.primalmagick.test.items;

import com.verdantartifice.primalmagick.common.attunements.AttunementManager;
import com.verdantartifice.primalmagick.common.attunements.AttunementType;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.research.ResearchEntries;
import com.verdantartifice.primalmagick.common.research.ResearchManager;
import com.verdantartifice.primalmagick.common.sources.Source;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Tests for ambrosia, which raises induced attunement to its own source by 2 and lowers every other source's by 1,
 * up to a cap of 10, 30, or 50 by tier.
 */
public class AmbrosiaTests extends AbstractBaseTest {
    private static ServerPlayer makeWizard(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        // Ambrosia only changes attunement for players who have started mod progression
        assertTrue(helper, ResearchManager.completeResearch(player, ResearchEntries.FIRST_STEPS), "Failed to grant prerequisite research");
        return player;
    }

    private static void eat(GameTestHelper helper, ServerPlayer player, Item ambrosia) {
        new ItemStack(ambrosia).finishUsingItem(helper.getLevel(), player);
    }

    private static int induced(ServerPlayer player, Source source) {
        return AttunementManager.getAttunement(player, source, AttunementType.INDUCED);
    }

    /** Eating ambrosia from no attunement grants two points of induced attunement to its source. */
    public static void ambrosia_grants_two_induced_attunement(GameTestHelper helper) {
        var player = makeWizard(helper);
        eat(helper, player, ItemsPM.BASIC_EARTH_AMBROSIA.get());
        assertValueEqual(helper, 2, induced(player, Sources.EARTH), "Induced earth attunement after one ambrosia");
        helper.succeed();
    }

    /** Eating ambrosia takes one point of induced attunement from every other source. */
    public static void ambrosia_deducts_one_point_from_other_sources(GameTestHelper helper) {
        var player = makeWizard(helper);
        Sources.getAll().forEach(s -> AttunementManager.setAttunement(player, s, AttunementType.INDUCED, 5));

        eat(helper, player, ItemsPM.BASIC_EARTH_AMBROSIA.get());

        assertValueEqual(helper, 7, induced(player, Sources.EARTH), "Induced earth attunement");
        Sources.getAll().stream().filter(s -> !s.equals(Sources.EARTH)).forEach(s ->
                assertValueEqual(helper, 4, induced(player, s), "Induced attunement for " + s.getId()));
        helper.succeed();
    }

    /**
     * One point under the cap, ambrosia only adds the one point that fits; at the cap it changes nothing, including
     * leaving other sources alone.
     */
    public static void ambrosia_stops_at_tier_cap(GameTestHelper helper, Item ambrosia, int cap) {
        var player = makeWizard(helper);
        AttunementManager.setAttunement(player, Sources.EARTH, AttunementType.INDUCED, cap - 1);
        AttunementManager.setAttunement(player, Sources.SEA, AttunementType.INDUCED, 5);

        eat(helper, player, ambrosia);
        assertValueEqual(helper, cap, induced(player, Sources.EARTH), "Induced earth attunement after the capping ambrosia");
        assertValueEqual(helper, 4, induced(player, Sources.SEA), "Induced sea attunement after the capping ambrosia");

        eat(helper, player, ambrosia);
        assertValueEqual(helper, cap, induced(player, Sources.EARTH), "Induced earth attunement after an ambrosia at the cap");
        assertValueEqual(helper, 4, induced(player, Sources.SEA), "Induced sea attunement after an ambrosia at the cap");
        helper.succeed();
    }
}
