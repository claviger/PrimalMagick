package com.verdantartifice.primalmagick.test.blocks;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.blocks.rituals.SaltTrailBlock;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Tests for the salt power that ritual altars generate and salt trails carry. Uses the floor template, since salt
 * needs a solid block to sit on.
 */
public class SaltTrailTests extends AbstractBaseTest {
    /**
     * A ritual altar powers an adjacent salt trail at the full 15, and each further trail in the line carries one
     * less, the way redstone dust does.
     */
    public static void salt_power_drops_by_one_per_trail_block(GameTestHelper helper) {
        // Put the altar at the west edge of the floor and run a line of salt east of it along the floor
        var altarPos = new BlockPos(0, 1, 2);
        helper.setBlock(altarPos, BlocksPM.RITUAL_ALTAR.get());
        for (int x = 1; x <= 4; x++) {
            helper.setBlock(new BlockPos(x, 1, 2), BlocksPM.SALT_TRAIL.get());
        }

        // Altar power is 15 and the trail loses one point per block, so the line reads 15, 14, 13, 12
        helper.succeedWhen(() -> {
            for (int x = 1; x <= 4; x++) {
                var state = helper.getBlockState(new BlockPos(x, 1, 2));
                assertTrue(helper, state.is(BlocksPM.SALT_TRAIL.get()), "No salt trail at x=" + x);
                assertValueEqual(helper, 16 - x, state.getValue(SaltTrailBlock.POWER), "Salt power of the trail at x=" + x);
            }
        });
    }

    /**
     * Salt trails that are not connected to an altar carry no power.
     */
    public static void unconnected_salt_trail_has_no_power(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 2), BlocksPM.SALT_TRAIL.get());
        helper.setBlock(new BlockPos(2, 1, 2), BlocksPM.SALT_TRAIL.get());
        helper.succeedWhen(() -> {
            assertValueEqual(helper, 0, helper.getBlockState(new BlockPos(1, 1, 2)).getValue(SaltTrailBlock.POWER), "Salt power of the first lone trail");
            assertValueEqual(helper, 0, helper.getBlockState(new BlockPos(2, 1, 2)).getValue(SaltTrailBlock.POWER), "Salt power of the second lone trail");
        });
    }
}
