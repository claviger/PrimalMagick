package com.verdantartifice.primalmagick.test.entities;

import com.verdantartifice.primalmagick.common.entities.EntityTypesPM;
import com.verdantartifice.primalmagick.common.entities.misc.InnerDemonEntity;
import com.verdantartifice.primalmagick.common.entities.misc.SinCrystalEntity;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.Vec3;

/**
 * Tests for the inner demon boss and its sin crystals. These run in the large template so that a crystal can sit
 * inside the demon's 16 block healing range but outside the 3 block radius of the damage cloud the crystal emits.
 */
public class InnerDemonTests extends AbstractBaseTest {
    private static final BlockPos DEMON_POS = new BlockPos(1, 1, 1);
    private static final float START_HEALTH = 100.0F;

    private static InnerDemonEntity spawnWoundedDemon(GameTestHelper helper) {
        var demon = helper.spawnWithNoFreeWill(EntityTypesPM.INNER_DEMON.get(), DEMON_POS);
        demon.setHealth(START_HEALTH);
        return demon;
    }

    private static SinCrystalEntity spawnCrystal(GameTestHelper helper) {
        Vec3 pos = helper.absoluteVec(new Vec3(5.5D, 2.0D, 5.5D));
        // Creating a crystal initializes its class, which fails with an Error rather than an Exception if its entity data
        // serializer can't be registered; report that as a test failure instead of letting it take down the server
        try {
            var crystal = new SinCrystalEntity(helper.getLevel(), pos.x, pos.y, pos.z);
            helper.getLevel().addFreshEntity(crystal);
            return crystal;
        } catch (LinkageError e) {
            assertTrue(helper, false, "Sin crystal could not be created: " + e + " / cause: " + e.getCause());
            return null;
        }
    }

    public static void sin_crystals_heal_inner_demon_in_range(GameTestHelper helper) {
        var demon = spawnWoundedDemon(helper);
        assertValueEqual(helper, START_HEALTH, demon.getHealth(), "Demon health after wounding");
        spawnCrystal(helper);

        // The demon heals one point per crystal in range every ten ticks
        helper.succeedWhen(() -> assertTrue(helper, demon.getHealth() > START_HEALTH, "Demon health did not rise: " + demon.getHealth()));
    }

    public static void sin_crystals_explode_when_damaged(GameTestHelper helper) {
        var demon = spawnWoundedDemon(helper);
        var crystal = spawnCrystal(helper);
        assertTrue(helper, crystal.isAlive(), "Crystal not alive after spawning");

        crystal.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 1.0F);

        // The crystal detonates, and demons it was healing take a 10 point backlash (a little less after their 4 armor)
        assertTrue(helper, crystal.isRemoved(), "Crystal not removed after being damaged");
        assertTrue(helper, demon.getHealth() <= START_HEALTH - 9.0F, "Demon took no backlash damage: " + demon.getHealth());
        helper.succeed();
    }

    public static void inner_demon_drops_hallowed_orb_when_killed(GameTestHelper helper) {
        var demon = helper.spawnWithNoFreeWill(EntityTypesPM.INNER_DEMON.get(), DEMON_POS);
        helper.assertItemEntityNotPresent(ItemsPM.HALLOWED_ORB.get(), DEMON_POS, 3.0D);

        demon.kill(helper.getLevel());

        helper.succeedWhen(() -> helper.assertItemEntityPresent(ItemsPM.HALLOWED_ORB.get(), DEMON_POS, 3.0D));
    }
}
