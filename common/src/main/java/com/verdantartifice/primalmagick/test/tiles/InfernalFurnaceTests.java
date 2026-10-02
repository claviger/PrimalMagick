package com.verdantartifice.primalmagick.test.tiles;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.common.tiles.devices.InfernalFurnaceTileEntity;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Tests for the infernal furnace block entity.
 */
public class InfernalFurnaceTests extends AbstractBaseTest {
    /**
     * Confirms that starting a supercharge with a lava bucket leaves just the empty bucket in the fuel slot, as the 1.21
     * furnace did, rather than also handing the burnt lava bucket back by dropping it into the world. The fuel slot only
     * accepts ignyx from players and automation, so the bucket is placed through the furnace's own inventory.
     */
    public static void infernal_furnace_lava_bucket_fuel_leaves_empty_bucket(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, BlocksPM.INFERNAL_FURNACE.get());
        var furnace = helper.getBlockEntity(pos, InfernalFurnaceTileEntity.class);

        // Charge the furnace and load it with something to smelt and a lava bucket as supercharge fuel
        furnace.setMana(Sources.INFERNAL, 10000);
        furnace.addItem(InfernalFurnaceTileEntity.INPUT_INV_INDEX, 0, new ItemStack(Items.RAW_IRON));
        furnace.addItem(InfernalFurnaceTileEntity.FUEL_INV_INDEX, 0, new ItemStack(Items.LAVA_BUCKET));
        assertTrue(helper, furnace.getItem(InfernalFurnaceTileEntity.FUEL_INV_INDEX, 0).is(Items.LAVA_BUCKET), "Input fuel not set correctly");

        // Run one tick of the furnace, which starts the supercharge and burns the fuel
        InfernalFurnaceTileEntity.tick(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos), furnace);

        // Confirm that only an empty bucket remains and that nothing was dropped
        ItemStack fuelStack = furnace.getItem(InfernalFurnaceTileEntity.FUEL_INV_INDEX, 0);
        assertValueEqual(helper, Items.BUCKET, fuelStack.getItem(), "Fuel slot item after the supercharge started");
        assertValueEqual(helper, 1, fuelStack.getCount(), "Fuel slot count after the supercharge started");
        helper.assertEntityNotPresent(EntityType.ITEM);
        helper.succeed();
    }
}
