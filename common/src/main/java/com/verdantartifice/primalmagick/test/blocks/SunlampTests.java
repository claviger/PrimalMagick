package com.verdantartifice.primalmagick.test.blocks;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.blocks.devices.SunlampBlock;
import com.verdantartifice.primalmagick.common.blocks.misc.GlowFieldBlock;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Tests for the cleanup of glow fields when a sunlamp is broken. SunlampBlock.affectNeighborsAfterRemoval removes every
 * glow field of the lamp's own type in a cube extending 15 blocks in each direction from the lamp (RADIUS = 15).
 */
public class SunlampTests extends AbstractBaseTest {
    private static final BlockPos LAMP_POS = new BlockPos(1, 2, 1);

    private static void assertGlowFieldsRemovedWhenBroken(GameTestHelper helper, SunlampBlock lamp, GlowFieldBlock ownField, GlowFieldBlock otherField) {
        // Mount the lamp on top of a stone block
        helper.setBlock(LAMP_POS.below(), Blocks.STONE);
        helper.setBlock(LAMP_POS, lamp.defaultBlockState());
        helper.assertBlockPresent(lamp, LAMP_POS);

        // Scatter the lamp's own glow fields at several offsets within the radius, including one above the lamp
        BlockPos[] inside = {
                LAMP_POS.offset(2, 0, 1),
                LAMP_POS.offset(0, 5, 0)
        };
        for (BlockPos pos : inside) {
            setBlockAbsolute(helper, pos, ownField);
            assertTrue(helper, helper.getLevel().getBlockState(helper.absolutePos(pos)).is(ownField), "Glow field not placed at " + pos + " (absolute " + helper.absolutePos(pos) + ", state " + helper.getLevel().getBlockState(helper.absolutePos(pos)) + ", max y " + helper.getLevel().getMaxY() + ")");
        }

        // One block beyond the radius, and a different kind of glow field inside it, must survive
        BlockPos outside = LAMP_POS.offset(16, 0, 0);
        BlockPos otherInside = LAMP_POS.offset(1, 0, 2);
        boolean outsidePlaced = helper.getLevel().setBlock(helper.absolutePos(outside), ownField.defaultBlockState(), Block.UPDATE_ALL);
        setBlockAbsolute(helper, otherInside, otherField);

        // Break the lamp
        helper.getLevel().destroyBlock(helper.absolutePos(LAMP_POS), false);
        helper.assertBlockNotPresent(lamp, LAMP_POS);

        for (BlockPos pos : inside) {
            assertFalse(helper, helper.getLevel().getBlockState(helper.absolutePos(pos)).is(ownField), "Glow field within the radius survived at " + pos);
        }
        // The block beyond the radius is only placeable if its chunk is loaded
        if (outsidePlaced) {
            assertTrue(helper, helper.getLevel().getBlockState(helper.absolutePos(outside)).is(ownField), "Glow field beyond the radius was removed");
        }
        assertTrue(helper, helper.getLevel().getBlockState(helper.absolutePos(otherInside)).is(otherField), "Glow field of a different type was removed");
        helper.succeed();
    }

    private static void setBlockAbsolute(GameTestHelper helper, BlockPos relativePos, Block block) {
        helper.getLevel().setBlock(helper.absolutePos(relativePos), block.defaultBlockState(), Block.UPDATE_ALL);
    }

    public static void sunlamp_removes_glow_fields_within_radius_when_broken(GameTestHelper helper) {
        assertGlowFieldsRemovedWhenBroken(helper, BlocksPM.SUNLAMP.get(), BlocksPM.GLOW_FIELD.get(), BlocksPM.SOUL_GLOW_FIELD.get());
    }

    public static void spirit_lantern_removes_soul_glow_fields_within_radius_when_broken(GameTestHelper helper) {
        assertGlowFieldsRemovedWhenBroken(helper, BlocksPM.SPIRIT_LANTERN.get(), BlocksPM.SOUL_GLOW_FIELD.get(), BlocksPM.GLOW_FIELD.get());
    }
}
