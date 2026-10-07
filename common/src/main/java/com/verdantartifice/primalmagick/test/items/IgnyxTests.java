package com.verdantartifice.primalmagick.test.items;

import com.verdantartifice.primalmagick.common.entities.projectiles.IgnyxEntity;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.tiles.crafting.AbstractCalcinatorTileEntity;
import com.verdantartifice.primalmagick.platform.Services;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Tests for ignyx, the alchemical super-coal: its burn time as fuel, and its explosion on impact whether thrown or fired
 * from a dispenser. A dirt block (blast resistance 0.5) next to the impact point stands in for the explosion, since
 * a power 1.5 TNT-style explosion at that distance destroys it.
 */
public class IgnyxTests extends AbstractBaseTest {
    public static void ignyx_is_long_lasting_furnace_fuel(GameTestHelper helper) {
        // IgnyxItem.BURN_TICKS is 12800 ticks, which is eight times the 1600 ticks of burn time of a piece of coal
        ItemStack ignyx = new ItemStack(ItemsPM.IGNYX.get());
        assertValueEqual(helper, 12800, Services.EVENTS.getBurnTime(ignyx, null, helper.getLevel().fuelValues()), "Ignyx burn time");
        assertValueEqual(helper, 1600, Services.EVENTS.getBurnTime(new ItemStack(net.minecraft.world.item.Items.COAL), null, helper.getLevel().fuelValues()), "Coal burn time");
        assertTrue(helper, AbstractCalcinatorTileEntity.isFuel(ignyx, helper.getLevel().fuelValues()), "Ignyx is not accepted as fuel");
        helper.succeed();
    }

    public static void ignyx_explodes_on_impact_when_thrown(GameTestHelper helper) {
        // Place a dirt block with a thrown ignyx heading straight for its south face
        BlockPos dirtPos = new BlockPos(1, 1, 1);
        helper.setBlock(dirtPos, Blocks.DIRT);
        Vec3 start = helper.absoluteVec(new Vec3(1.5D, 1.5D, 2.7D));
        IgnyxEntity ignyx = new IgnyxEntity(helper.getLevel(), start.x, start.y, start.z);
        ignyx.shoot(0.0D, 0.0D, -1.0D, 1.0F, 0.0F);
        helper.getLevel().addFreshEntity(ignyx);

        // On impact the projectile explodes, which discards it and destroys the dirt block
        helper.succeedWhen(() -> {
            assertTrue(helper, ignyx.isRemoved(), "Ignyx projectile was not discarded on impact");
            helper.assertBlockNotPresent(Blocks.DIRT, dirtPos);
        });
    }

    public static void ignyx_explodes_on_impact_when_fired_from_dispenser(GameTestHelper helper) {
        // Place an east-facing dispenser with a dirt block two blocks in front of it
        BlockPos dispenserPos = new BlockPos(0, 1, 1);
        BlockPos dirtPos = new BlockPos(2, 1, 1);
        helper.setBlock(dirtPos, Blocks.DIRT);
        helper.setBlock(dispenserPos, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.EAST));

        // Attach a button to the south side of the dispenser
        BlockPos buttonPos = dispenserPos.south();
        helper.setBlock(buttonPos, Blocks.OAK_BUTTON.defaultBlockState().setValue(ButtonBlock.FACING, Direction.NORTH));

        // Load the dispenser with ignyx and press the button
        var dispenserEntity = helper.getBlockEntity(dispenserPos, DispenserBlockEntity.class);
        dispenserEntity.insertItem(new ItemStack(ItemsPM.IGNYX.get()));
        helper.pressButton(buttonPos);

        // The dispenser fires the ignyx as a projectile rather than dropping it, and the projectile explodes on the dirt
        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(Blocks.DIRT, dirtPos);
            helper.assertItemEntityNotPresent(ItemsPM.IGNYX.get(), dispenserPos.east(), 3.0D);
            assertFalse(helper, dispenserEntity.getItem(0).is(ItemsPM.IGNYX.get()), "Ignyx still in the dispenser");
        });
    }
}
