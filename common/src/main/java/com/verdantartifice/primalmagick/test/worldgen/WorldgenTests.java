package com.verdantartifice.primalmagick.test.worldgen;

import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Tests for the mod's world generation data.
 */
public class WorldgenTests extends AbstractBaseTest {
    private static final BlockPos GROUND_POS = new BlockPos(3, 0, 3);
    private static final BlockPos TREE_POS = GROUND_POS.above();

    /**
     * A sapling planted on a grass block must be able to stay there, and bonemealing it must grow its tree.
     */
    public static void sapling_grows_tree(GameTestHelper helper, Block sapling, Block log) {
        helper.setBlock(GROUND_POS, Blocks.GRASS_BLOCK);
        helper.setBlock(TREE_POS, sapling);
        BlockState saplingState = helper.getBlockState(TREE_POS);
        assertTrue(helper, saplingState.canSurvive(helper.getLevel(), helper.absolutePos(TREE_POS)), BuiltInRegistries.BLOCK.getKey(sapling) + " cannot survive on a grass block");

        // Each bonemeal advances the sapling one stage; the second one grows the tree
        RandomSource random = RandomSource.create(0L);
        for (int attempt = 0; attempt < 10 && helper.getBlockState(TREE_POS).is(sapling); attempt++) {
            BlockState state = helper.getBlockState(TREE_POS);
            ((SaplingBlock)sapling).performBonemeal(helper.getLevel(), random, helper.absolutePos(TREE_POS), state);
        }
        assertTrue(helper, helper.getBlockState(TREE_POS).is(log), "Expected " + BuiltInRegistries.BLOCK.getKey(log) + " at the sapling position but found " + helper.getBlockState(TREE_POS));
        assertTrue(helper, helper.getBlockState(TREE_POS.above(4)).is(log), "Expected a trunk at least five blocks tall but found " + helper.getBlockState(TREE_POS.above(4)));
        helper.succeed();
    }

    /**
     * The sapling-checked placed feature used by world generation must place its tree on a grass block.
     */
    public static void placed_feature_places_tree(GameTestHelper helper, ResourceKey<PlacedFeature> featureKey, Block log) {
        helper.setBlock(GROUND_POS, Blocks.GRASS_BLOCK);
        var level = helper.getLevel();
        PlacedFeature feature = level.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE).getValueOrThrow(featureKey);
        boolean placed = feature.place(level, level.getChunkSource().getGenerator(), RandomSource.create(0L), helper.absolutePos(TREE_POS));
        assertTrue(helper, placed, "Placed feature " + featureKey.identifier() + " did not place on a grass block");
        assertTrue(helper, helper.getBlockState(TREE_POS).is(log), "Expected " + BuiltInRegistries.BLOCK.getKey(log) + " at the feature origin but found " + helper.getBlockState(TREE_POS));
        helper.succeed();
    }
}
