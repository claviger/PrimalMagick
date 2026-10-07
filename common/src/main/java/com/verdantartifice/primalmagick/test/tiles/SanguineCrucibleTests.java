package com.verdantartifice.primalmagick.test.tiles;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.blocks.devices.SanguineCrucibleBlock;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.tiles.devices.SanguineCrucibleTileEntity;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Tests for the sanguine crucible: slotting a sanguine core with a right-click and summoning the core's creature once
 * it has been fed enough soul gems. The armadillo core is used for summoning, since it needs 2 souls per spawn
 * (SanguineCoreItem soulsPerSpawn) and armadillos are not spawned anywhere else in the test suite. A crucible fills 1
 * fluid per tick and needs 200 (FLUID_DRAIN), then charges for 100 ticks (CHARGE_MAX) before it spawns, so a summon
 * takes about 300 ticks from placement.
 */
public class SanguineCrucibleTests extends AbstractBaseTest {
    private static final BlockPos CRUCIBLE_POS = new BlockPos(2, 1, 2);

    public static void sanguine_crucible_can_be_slotted_with_core(GameTestHelper helper) {
        helper.setBlock(CRUCIBLE_POS, BlocksPM.SANGUINE_CRUCIBLE.get());
        var tile = helper.getBlockEntity(CRUCIBLE_POS, SanguineCrucibleTileEntity.class);
        assertFalse(helper, tile.hasCore(), "Crucible has a core before one is used on it");
        assertValueEqual(helper, false, helper.getBlockState(CRUCIBLE_POS).getValue(SanguineCrucibleBlock.LIT), "Crucible lit state before slotting");

        // Use a survival player so that the core stack is consumed by the use
        var player = makeMockServerPlayer(helper);
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemsPM.SANGUINE_CORE_ARMADILLO.get(), 2));
        BlockPos absPos = helper.absolutePos(CRUCIBLE_POS);
        var hit = new BlockHitResult(absPos.getCenter(), Direction.UP, absPos, false);
        var held = player.getItemInHand(InteractionHand.MAIN_HAND);
        var result = player.gameMode.useItemOn(player, helper.getLevel(), held, InteractionHand.MAIN_HAND, hit);

        // One core moves into the crucible, which lights up, and the rest stay in the hand
        assertTrue(helper, result.consumesAction(), "Core use on the crucible was not consumed: " + result);
        assertTrue(helper, tile.hasCore(), "Crucible has no core after use");
        assertTrue(helper, tile.getItem().is(ItemsPM.SANGUINE_CORE_ARMADILLO.get()), "Slotted item is not the armadillo core: " + tile.getItem());
        assertValueEqual(helper, 1, tile.getItem().getCount(), "Slotted core count");
        assertValueEqual(helper, true, helper.getBlockState(CRUCIBLE_POS).getValue(SanguineCrucibleBlock.LIT), "Crucible lit state after slotting");
        assertValueEqual(helper, 1, player.getItemInHand(InteractionHand.MAIN_HAND).getCount(), "Core count left in hand");
        helper.succeed();
    }

    public static void sanguine_crucible_spawns_creature_with_core_and_souls(GameTestHelper helper) {
        helper.setBlock(CRUCIBLE_POS, BlocksPM.SANGUINE_CRUCIBLE.get());
        var tile = helper.getBlockEntity(CRUCIBLE_POS, SanguineCrucibleTileEntity.class);
        tile.addItem(new ItemStack(ItemsPM.SANGUINE_CORE_ARMADILLO.get()));
        assertTrue(helper, tile.hasCore(), "Crucible has no core after setup");
        assertValueEqual(helper, 0, tile.getSouls(), "Crucible souls before feeding");

        // Drop two soul gems into the crucible, which absorbs them as souls
        Vec3 dropPos = helper.absoluteVec(new Vec3(CRUCIBLE_POS.getX() + 0.5D, CRUCIBLE_POS.getY() + 0.6D, CRUCIBLE_POS.getZ() + 0.5D));
        helper.getLevel().addFreshEntity(new ItemEntity(helper.getLevel(), dropPos.x, dropPos.y, dropPos.z, new ItemStack(ItemsPM.SOUL_GEM.get(), 2)));

        // After the fluid and charge timers run out, the crucible spends the souls and one use of the core to summon an armadillo nearby
        AABB searchBox = new AABB(helper.absolutePos(CRUCIBLE_POS)).inflate(8.0D);
        helper.succeedWhen(() -> {
            assertFalse(helper, helper.getLevel().getEntitiesOfClass(Armadillo.class, searchBox).isEmpty(), "No armadillo has been summoned");
            assertValueEqual(helper, 0, tile.getSouls(), "Crucible souls after summoning");
            assertTrue(helper, tile.hasCore(), "Core used up by a single summon");
            assertValueEqual(helper, 1, tile.getItem().getDamageValue(), "Core damage after one summon");
        });
    }
}
