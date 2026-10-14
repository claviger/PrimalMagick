package com.verdantartifice.primalmagick.test.entities;

import com.verdantartifice.primalmagick.common.entities.EntityTypesPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;

/**
 * Tests for the pixie house entity.
 */
public class PixieHouseTests extends AbstractBaseTest {
    /** A placed pixie house is not removed by the despawn check when the nearest player is far away, and keeps its housed pixie. */
    public static void pixie_house_does_not_despawn_far_from_players(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos housePos = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockPos controlPos = helper.absolutePos(new BlockPos(3, 1, 3));

        // Create the entities the way their items do, since the test helper's spawn methods make mobs persistent
        var house = EntityTypesPM.PIXIE_HOUSE.get().create(level, null, housePos, EntitySpawnReason.SPAWN_ITEM_USE, true, true);
        assertTrue(helper, house != null, "Pixie house was not created");
        house.setHousedPixie(new ItemStack(ItemsPM.BASIC_EARTH_PIXIE.get()));
        level.addFreshEntity(house);
        var control = EntityType.ZOMBIE.create(level, EntitySpawnReason.TRIGGERED);
        assertTrue(helper, control != null, "Control mob was not created");
        control.snapTo(Vec3.atBottomCenterOf(controlPos));
        level.addFreshEntity(control);
        assertFalse(helper, control.isPersistenceRequired(), "Control mob requires persistence");

        // Put a player more than 128 blocks away, where vanilla removes mobs that allow it
        var player = makeMockServerPlayer(helper, true);
        player.setPos(house.getX() + 200D, house.getY(), house.getZ());

        // Players left behind by other tests share this level, so move them out of range while the check runs
        var others = new HashMap<Player, Vec3>();
        for (Player other : level.players()) {
            if (other != player) {
                others.put(other, other.position());
                other.setPos(house.getX() + 100000D, house.getY(), house.getZ());
            }
        }
        try {
            assertTrue(helper, level.getNearestPlayer(house, -1D) == player, "Far player is not the nearest player to the pixie house");

            house.checkDespawn();
            control.checkDespawn();

            assertTrue(helper, control.isRemoved(), "Control mob was not removed, so the despawn check was not exercised");
            assertFalse(helper, house.isRemoved(), "Pixie house was removed by the despawn check");
            assertFalse(helper, house.getHousedPixie().isEmpty(), "Pixie house lost its housed pixie");
        } finally {
            // Clean up even on failure, since a leftover far-away player would skew other tests sharing this level
            others.forEach((other, pos) -> other.setPos(pos));
            player.discard();
            house.discard();
            control.discard();
        }

        helper.succeed();
    }
}
