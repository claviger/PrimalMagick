package com.verdantartifice.primalmagick.test.blocks;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Tests for blood roses, which hurt creatures standing in them like cacti but leave dropped items alone. Uses the
 * floor template.
 */
public class BloodRoseTests extends AbstractBaseTest {
    private static final BlockPos ROSE_POS = new BlockPos(2, 1, 2);

    private static void placeRose(GameTestHelper helper) {
        // Tall flowers only survive on dirt-like blocks, not on the stone floor
        helper.setBlock(ROSE_POS.below(), Blocks.GRASS_BLOCK);
        var lower = BlocksPM.BLOOD_ROSE.get().defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER);
        helper.setBlock(ROSE_POS, lower);
        helper.setBlock(ROSE_POS.above(), lower.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
    }

    /** A pig left standing in a blood rose bush takes damage. */
    public static void blood_rose_damages_entities_inside(GameTestHelper helper) {
        placeRose(helper);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, ROSE_POS);
        helper.succeedWhen(() -> assertTrue(helper, pig.getHealth() < pig.getMaxHealth(), "Pig inside a blood rose was not damaged: pig at " + pig.position() + " box " + pig.getBoundingBox()
                + " in block " + helper.getLevel().getBlockState(pig.blockPosition()) + " ticks " + pig.tickCount + " alive " + pig.isAlive()));
    }

    /** An apple dropped inside a blood rose bush is still there after the bush has had time to act on it. */
    public static void blood_rose_does_not_destroy_items(GameTestHelper helper) {
        placeRose(helper);
        helper.spawnItem(Items.APPLE, ROSE_POS);
        helper.runAfterDelay(40, () -> {
            helper.assertItemEntityPresent(Items.APPLE, ROSE_POS, 2.0D);
            helper.succeed();
        });
    }
}
