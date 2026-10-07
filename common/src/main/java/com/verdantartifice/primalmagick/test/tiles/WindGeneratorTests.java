package com.verdantartifice.primalmagick.test.tiles;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.blocks.devices.AbstractWindGeneratorBlock;
import com.verdantartifice.primalmagick.common.tiles.devices.WindGeneratorTileEntity;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.phys.Vec3;

/**
 * Tests for the zephyr engine (pushes entities away) and the void turbine (pulls entities in). Both reach out along
 * their facing for as many blocks as the redstone power they receive, and nudge each item or living entity in that
 * range by 0.1 * power / 15 blocks per tick (WindGeneratorTileEntity.tick). The tests face the device up and power it
 * with a daylight detector set to the desired signal strength, then run one tick of the tile. Facing up keeps the whole
 * range clear of neighbouring test structures.
 */
public class WindGeneratorTests extends AbstractBaseTest {
    private static ItemEntity spawnItem(GameTestHelper helper, Vec3 relPos) {
        var item = new ItemEntity(helper.getLevel(), 0.0D, 0.0D, 0.0D, new ItemStack(Items.STICK), 0.0D, 0.0D, 0.0D);
        item.setPos(helper.absoluteVec(relPos));
        item.setDeltaMovement(Vec3.ZERO);
        item.setNoGravity(true);
        helper.getLevel().addFreshEntity(item);
        return item;
    }

    /**
     * Powers a device facing up with the given signal strength, puts one item in the last block of its range and one
     * just beyond it, runs a tick, and checks which one was moved and by how much. The expected change in y velocity of
     * the item in range is given, with the sign set by whether the device pushes or pulls.
     */
    private static void assertWindMovesEntitiesInRange(GameTestHelper helper, Block block, int power, double expectedDeltaY) {
        // Place the power source first so the device sees a signal as soon as it is placed
        var pos = new BlockPos(1, 0, 1);
        helper.setBlock(pos.south(), Blocks.DAYLIGHT_DETECTOR.defaultBlockState().setValue(DaylightDetectorBlock.POWER, power));
        helper.setBlock(pos, block.defaultBlockState().setValue(AbstractWindGeneratorBlock.FACING, Direction.UP).setValue(AbstractWindGeneratorBlock.POWERED, true));
        assertValueEqual(helper, power, helper.getLevel().getBestNeighborSignal(helper.absolutePos(pos)), "Signal strength received by the device");

        // The range is the block of the device plus as many blocks up as the signal strength, so the last block in range
        // is at y = power and the first block out of range is at y = power + 1
        var inRange = spawnItem(helper, new Vec3(1.5D, power + 0.5D, 1.5D));
        var outOfRange = spawnItem(helper, new Vec3(1.5D, power + 1.5D, 1.5D));
        var tile = helper.getBlockEntity(pos, WindGeneratorTileEntity.class);

        WindGeneratorTileEntity.tick(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos), tile);

        assertTrue(helper, Math.abs(inRange.getDeltaMovement().y - expectedDeltaY) < 1.0E-9D, "Velocity of item in range was " + inRange.getDeltaMovement().y + ", expected " + expectedDeltaY);
        assertValueEqual(helper, 0.0D, inRange.getDeltaMovement().x, "Sideways velocity of item in range");
        assertValueEqual(helper, 0.0D, inRange.getDeltaMovement().z, "Depth velocity of item in range");
        assertValueEqual(helper, 0.0D, outOfRange.getDeltaMovement().y, "Velocity of item out of range");
        helper.succeed();
    }

    public static void zephyr_engine_pushes_entities_within_its_power_range(GameTestHelper helper, int power) {
        // Pushing up is positive y
        assertWindMovesEntitiesInRange(helper, BlocksPM.ZEPHYR_ENGINE.get(), power, 0.1D * power / 15.0D);
    }

    public static void void_turbine_pulls_entities_within_its_power_range(GameTestHelper helper, int power) {
        // Pulling toward a device that faces up is down, which is negative y
        assertWindMovesEntitiesInRange(helper, BlocksPM.VOID_TURBINE.get(), power, -0.1D * power / 15.0D);
    }
}
