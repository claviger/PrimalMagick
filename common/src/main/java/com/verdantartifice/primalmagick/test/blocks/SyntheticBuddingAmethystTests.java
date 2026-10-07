package com.verdantartifice.primalmagick.test.blocks;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.blocks.minerals.BuddingGemClusterBlock;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import com.verdantartifice.primalmagick.test.TestRandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Tests for the growth and decay of synthetic budding amethyst blocks, driven by direct random ticks with a fixed
 * random source. BuddingGemSourceBlock.randomTick only grows when nextInt(GROWTH_CHANCE) returns 0, then picks the
 * direction Direction.values()[nextInt(6)], which is DOWN (index 0) whenever the fixed integer is 0. After a growth it
 * decays the source block whenever nextFloat() is below the 0.08 decay chance. The source sits at (1, 2, 1) with open
 * air below it, so that every growth targets (1, 1, 1).
 */
public class SyntheticBuddingAmethystTests extends AbstractBaseTest {
    private static final BlockPos SOURCE_POS = new BlockPos(1, 2, 1);
    private static final BlockPos BUD_POS = new BlockPos(1, 1, 1);

    /**
     * A random source that always grows downward and never decays the source.
     */
    private static RandomSource growWithoutDecay() {
        return TestRandomSource.builder().setInt(0).setFloat(0.99F).build();
    }

    /**
     * A random source that always grows downward and always decays the source.
     */
    private static RandomSource growWithDecay() {
        return TestRandomSource.builder().setInt(0).setFloat(0.0F).build();
    }

    private static void placeSource(GameTestHelper helper, Block source) {
        helper.setBlock(SOURCE_POS, source);
        helper.setBlock(BUD_POS, Blocks.AIR);
    }

    private static void tickSource(GameTestHelper helper, RandomSource random) {
        var state = helper.getBlockState(SOURCE_POS);
        state.randomTick(helper.getLevel(), helper.absolutePos(SOURCE_POS), random);
    }

    private static void assertBud(GameTestHelper helper, Block expected, String message) {
        var state = helper.getBlockState(BUD_POS);
        assertTrue(helper, state.is(expected), message + ": " + state);
        assertValueEqual(helper, Direction.DOWN, state.getValue(BuddingGemClusterBlock.FACING), "Bud facing");
    }

    public static void synthetic_budding_amethyst_grows_buds(GameTestHelper helper) {
        placeSource(helper, BlocksPM.FLAWED_BUDDING_AMETHYST_BLOCK.get());

        // The first growth places a small bud on the open face, and later growths on the same face upgrade it one stage at a time
        tickSource(helper, growWithoutDecay());
        assertBud(helper, BlocksPM.SMALL_SYNTHETIC_AMETHYST_BUD.get(), "First growth did not make a small bud");
        tickSource(helper, growWithoutDecay());
        assertBud(helper, BlocksPM.MEDIUM_SYNTHETIC_AMETHYST_BUD.get(), "Second growth did not make a medium bud");
        tickSource(helper, growWithoutDecay());
        assertBud(helper, BlocksPM.LARGE_SYNTHETIC_AMETHYST_BUD.get(), "Third growth did not make a large bud");
        tickSource(helper, growWithoutDecay());
        assertBud(helper, BlocksPM.SYNTHETIC_AMETHYST_CLUSTER.get(), "Fourth growth did not make a cluster");

        // A fully grown cluster stays as it is, and the source was never decayed with the roll that prevents it
        tickSource(helper, growWithoutDecay());
        assertBud(helper, BlocksPM.SYNTHETIC_AMETHYST_CLUSTER.get(), "Cluster changed after a further growth");
        assertTrue(helper, helper.getBlockState(SOURCE_POS).is(BlocksPM.FLAWED_BUDDING_AMETHYST_BLOCK.get()), "Source block changed without a decay roll: " + helper.getBlockState(SOURCE_POS));
        helper.succeed();
    }

    public static void synthetic_budding_amethyst_does_not_grow_without_a_growth_roll(GameTestHelper helper) {
        placeSource(helper, BlocksPM.FLAWED_BUDDING_AMETHYST_BLOCK.get());

        // A nonzero roll for the growth chance means that nothing grows
        tickSource(helper, TestRandomSource.builder().setInt(1).setFloat(0.0F).build());
        assertTrue(helper, helper.getBlockState(BUD_POS).isAir(), "Bud grew without a growth roll: " + helper.getBlockState(BUD_POS));
        assertTrue(helper, helper.getBlockState(SOURCE_POS).is(BlocksPM.FLAWED_BUDDING_AMETHYST_BLOCK.get()), "Source block changed without a growth roll: " + helper.getBlockState(SOURCE_POS));
        helper.succeed();
    }

    public static void synthetic_budding_amethyst_can_downgrade_when_a_bud_grows(GameTestHelper helper) {
        placeSource(helper, BlocksPM.FLAWED_BUDDING_AMETHYST_BLOCK.get());

        // With a decay roll under the chance, the flawed block drops one quality step as its first bud grows
        tickSource(helper, growWithDecay());
        assertBud(helper, BlocksPM.SMALL_SYNTHETIC_AMETHYST_BUD.get(), "Bud did not grow");
        assertTrue(helper, helper.getBlockState(SOURCE_POS).is(BlocksPM.CHIPPED_BUDDING_AMETHYST_BLOCK.get()), "Flawed block did not become chipped: " + helper.getBlockState(SOURCE_POS));

        // A chipped block drops to damaged in the same way
        tickSource(helper, growWithDecay());
        assertBud(helper, BlocksPM.MEDIUM_SYNTHETIC_AMETHYST_BUD.get(), "Bud did not grow again");
        assertTrue(helper, helper.getBlockState(SOURCE_POS).is(BlocksPM.DAMAGED_BUDDING_AMETHYST_BLOCK.get()), "Chipped block did not become damaged: " + helper.getBlockState(SOURCE_POS));
        helper.succeed();
    }

    public static void synthetic_budding_amethyst_does_not_downgrade_without_growth(GameTestHelper helper) {
        placeSource(helper, BlocksPM.FLAWED_BUDDING_AMETHYST_BLOCK.get());

        // Decay only happens as part of a growth, so a failed growth roll never downgrades the block
        tickSource(helper, TestRandomSource.builder().setInt(1).setFloat(0.0F).build());
        assertTrue(helper, helper.getBlockState(SOURCE_POS).is(BlocksPM.FLAWED_BUDDING_AMETHYST_BLOCK.get()), "Source block changed without a growth: " + helper.getBlockState(SOURCE_POS));
        helper.succeed();
    }

    public static void synthetic_budding_amethyst_eventually_reverts_to_amethyst_block(GameTestHelper helper) {
        // A damaged block, the lowest quality, reverts to a plain amethyst block on its next decay
        placeSource(helper, BlocksPM.DAMAGED_BUDDING_AMETHYST_BLOCK.get());
        tickSource(helper, growWithDecay());
        assertBud(helper, BlocksPM.SMALL_SYNTHETIC_AMETHYST_BUD.get(), "Bud did not grow");
        assertTrue(helper, helper.getBlockState(SOURCE_POS).is(Blocks.AMETHYST_BLOCK), "Damaged block did not revert to amethyst: " + helper.getBlockState(SOURCE_POS));

        // The plain amethyst block no longer buds
        tickSource(helper, growWithDecay());
        assertBud(helper, BlocksPM.SMALL_SYNTHETIC_AMETHYST_BUD.get(), "Plain amethyst changed the existing bud");
        helper.succeed();
    }

    public static void synthetic_budding_amethyst_decays_through_every_stage(GameTestHelper helper) {
        placeSource(helper, BlocksPM.FLAWED_BUDDING_AMETHYST_BLOCK.get());

        // Three decaying growths take a flawed block through chipped and damaged to plain amethyst
        tickSource(helper, growWithDecay());
        assertTrue(helper, helper.getBlockState(SOURCE_POS).is(BlocksPM.CHIPPED_BUDDING_AMETHYST_BLOCK.get()), "Flawed block did not become chipped: " + helper.getBlockState(SOURCE_POS));
        tickSource(helper, growWithDecay());
        assertTrue(helper, helper.getBlockState(SOURCE_POS).is(BlocksPM.DAMAGED_BUDDING_AMETHYST_BLOCK.get()), "Chipped block did not become damaged: " + helper.getBlockState(SOURCE_POS));
        tickSource(helper, growWithDecay());
        assertTrue(helper, helper.getBlockState(SOURCE_POS).is(Blocks.AMETHYST_BLOCK), "Damaged block did not become amethyst: " + helper.getBlockState(SOURCE_POS));
        helper.succeed();
    }
}
