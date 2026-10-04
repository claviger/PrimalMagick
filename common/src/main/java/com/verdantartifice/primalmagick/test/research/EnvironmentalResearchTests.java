package com.verdantartifice.primalmagick.test.research;

import com.verdantartifice.primalmagick.common.events.PlayerEvents;
import com.verdantartifice.primalmagick.common.research.ResearchEntries;
import com.verdantartifice.primalmagick.common.research.ResearchEntry;
import com.verdantartifice.primalmagick.common.research.ResearchManager;
import com.verdantartifice.primalmagick.common.research.keys.ResearchEntryKey;
import com.verdantartifice.primalmagick.platform.Services;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiConsumer;

/**
 * Tests for the environmental research triggers checked as the player moves around the world. Each test is
 * registered with a fixed-time environment and passes the relevant biome to the trigger check directly.
 */
public class EnvironmentalResearchTests extends AbstractBaseTest {
    private static boolean checkEnvironment(GameTestHelper helper, ResourceKey<ResearchEntry> sourceEntry, ResourceKey<ResearchEntry> envEntry,
                                            ResourceKey<Biome> biome, BiConsumer<ServerPlayer, Holder<Biome>> check) {
        var player = makeMockServerPlayer(helper);
        player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(BlockPos.ZERO.above())));

        // Put the player on the first stage of the source research, which asks them to witness the environment
        Services.CAPABILITIES.knowledge(player).ifPresent(knowledge -> knowledge.addResearch(new ResearchEntryKey(sourceEntry)));
        assertFalse(helper, ResearchManager.isResearchStarted(player, envEntry), "Environmental research started before check");

        check.accept(player, helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(biome));
        return ResearchManager.isResearchStarted(player, envEntry);
    }

    private static boolean checkSunlightInDesert(GameTestHelper helper) {
        return checkEnvironment(helper, ResearchEntries.SOURCE_SUN, ResearchEntries.ENV_SUN, Biomes.DESERT, PlayerEvents::checkSunResearch);
    }

    private static boolean checkMoonlightInForest(GameTestHelper helper) {
        return checkEnvironment(helper, ResearchEntries.SOURCE_MOON, ResearchEntries.ENV_MOON, Biomes.FOREST, PlayerEvents::checkMoonResearch);
    }

    public static void sunlight_scan_requires_day(GameTestHelper helper) {
        assertValueEqual(helper, false, checkSunlightInDesert(helper), "Sunlight research granted in a desert at night");
        helper.succeed();
    }

    public static void sunlight_scan_succeeds_during_day(GameTestHelper helper) {
        assertValueEqual(helper, true, checkSunlightInDesert(helper), "Sunlight research granted in a desert during the day");
        helper.succeed();
    }

    public static void moonlight_scan_requires_night(GameTestHelper helper) {
        assertValueEqual(helper, false, checkMoonlightInForest(helper), "Moonlight research granted in a forest during the day");
        helper.succeed();
    }

    public static void moonlight_scan_succeeds_at_night(GameTestHelper helper) {
        assertValueEqual(helper, true, checkMoonlightInForest(helper), "Moonlight research granted in a forest at night");
        helper.succeed();
    }
}
