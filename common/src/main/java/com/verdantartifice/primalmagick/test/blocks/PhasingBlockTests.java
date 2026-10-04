package com.verdantartifice.primalmagick.test.blocks;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.blocks.trees.IPhasingBlock;
import com.verdantartifice.primalmagick.common.blockstates.properties.TimePhase;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;

/**
 * Tests that sunwood and moonwood blocks phase in and out with the time of day. Each test is registered with a
 * fixed-time environment, places a log in the opposite phase, and random ticks it the way the server would.
 */
public class PhasingBlockTests extends AbstractBaseTest {
    private static void assertPhase(GameTestHelper helper, Block log, Block leaves, TimePhase startPhase, TimePhase expected) {
        var pos = BlockPos.ZERO.above();
        var absPos = helper.absolutePos(pos);
        var level = helper.getLevel();

        // The leaves report the phase for the current level time
        var leavesBlock = assertInstanceOf(helper, leaves, IPhasingBlock.class, "Leaves block does not phase");
        assertValueEqual(helper, expected, leavesBlock.getCurrentPhase(level, absPos), "Leaves phase for the current time");

        // A log left over from the other half of the day catches up on its next random tick
        helper.setBlock(pos, log.defaultBlockState().setValue(IPhasingBlock.PHASE, startPhase));
        assertValueEqual(helper, startPhase, helper.getBlockState(pos).getValue(IPhasingBlock.PHASE), "Log phase before random tick");
        helper.getBlockState(pos).randomTick(level, absPos, level.getRandom());
        assertValueEqual(helper, expected, helper.getBlockState(pos).getValue(IPhasingBlock.PHASE), "Log phase after random tick");

        helper.succeed();
    }

    public static void sunwood_phase_is_full_during_day(GameTestHelper helper) {
        assertPhase(helper, BlocksPM.SUNWOOD_LOG.get(), BlocksPM.SUNWOOD_LEAVES.get(), TimePhase.FADED, TimePhase.FULL);
    }

    public static void sunwood_phase_is_faded_at_night(GameTestHelper helper) {
        assertPhase(helper, BlocksPM.SUNWOOD_LOG.get(), BlocksPM.SUNWOOD_LEAVES.get(), TimePhase.FULL, TimePhase.FADED);
    }

    public static void moonwood_phase_is_faded_during_day(GameTestHelper helper) {
        assertPhase(helper, BlocksPM.MOONWOOD_LOG.get(), BlocksPM.MOONWOOD_LEAVES.get(), TimePhase.FULL, TimePhase.FADED);
    }

    public static void moonwood_phase_is_full_at_night(GameTestHelper helper) {
        assertPhase(helper, BlocksPM.MOONWOOD_LOG.get(), BlocksPM.MOONWOOD_LEAVES.get(), TimePhase.FADED, TimePhase.FULL);
    }
}
