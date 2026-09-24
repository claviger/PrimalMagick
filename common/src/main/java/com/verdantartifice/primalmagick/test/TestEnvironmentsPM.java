package com.verdantartifice.primalmagick.test;

import com.verdantartifice.primalmagick.common.util.ResourceUtils;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.gamerules.GameRuleMap;
import net.minecraft.world.level.gamerules.GameRules;

public class TestEnvironmentsPM {
    public static final ResourceKey<TestEnvironmentDefinition<?>> DEFAULT = ResourceKey.create(Registries.TEST_ENVIRONMENT, Identifier.withDefaultNamespace("default"));

    public static final ResourceKey<TestEnvironmentDefinition<?>> DAYTIME_ENV = ResourceKey.create(Registries.TEST_ENVIRONMENT, ResourceUtils.loc("daytime"));
    public static final ResourceKey<TestEnvironmentDefinition<?>> NIGHTTIME_ENV = ResourceKey.create(Registries.TEST_ENVIRONMENT, ResourceUtils.loc("nighttime"));

    public static void bootstrap(BootstrapContext<TestEnvironmentDefinition<?>> context) {
        HolderGetter<WorldClock> clocks = context.lookup(Registries.WORLD_CLOCK);
        GameRuleMap frozenTime = new GameRuleMap.Builder().set(GameRules.ADVANCE_TIME, false).build();
        context.register(DAYTIME_ENV, new TestEnvironmentDefinition.AllOf(new TestEnvironmentDefinition.ClockTime(clocks.getOrThrow(WorldClocks.OVERWORLD), 6000), new TestEnvironmentDefinition.SetGameRules(frozenTime)));
        context.register(NIGHTTIME_ENV, new TestEnvironmentDefinition.AllOf(new TestEnvironmentDefinition.ClockTime(clocks.getOrThrow(WorldClocks.OVERWORLD), 18000), new TestEnvironmentDefinition.SetGameRules(frozenTime)));
    }
}
