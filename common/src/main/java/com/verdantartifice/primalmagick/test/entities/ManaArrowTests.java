package com.verdantartifice.primalmagick.test.entities;

import com.verdantartifice.primalmagick.common.effects.EffectsPM;
import com.verdantartifice.primalmagick.common.entities.projectiles.ManaArrowEntity;
import com.verdantartifice.primalmagick.common.sources.Source;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Tests for the secondary effects of mana-tinged arrows. Unless a test says otherwise, arrows are launched due north
 * (towards negative Z) at speed 1.5 from just south of a stationary mob. Mobs are placed on the floor of the
 * floor5x5x5 template at z = 3, so the arrow starts 1.4 blocks from the mob's center and reaches it on its first tick.
 */
public class ManaArrowTests extends AbstractBaseTest {
    private static final float SHOT_SPEED = 1.5F;
    private static final double ARROW_START_Z = 4.9D;
    private static final int MOB_Z = 3;

    private static ManaArrowEntity makeArrow(GameTestHelper helper, Source source, Vec3 relativePos) {
        Vec3 pos = helper.absoluteVec(relativePos);
        return new ManaArrowEntity(helper.getLevel(), pos.x, pos.y, pos.z, source, new ItemStack(Items.ARROW), null);
    }

    /**
     * Launches an arrow of the given source due north from the given X and Y coordinates at the given speed.
     */
    private static ManaArrowEntity fireArrowNorth(GameTestHelper helper, Source source, double x, double y, double speed) {
        ManaArrowEntity arrow = makeArrow(helper, source, new Vec3(x, y, ARROW_START_Z));
        arrow.shoot(0.0D, 0.0D, -1.0D, (float)speed, 0.0F);
        helper.getLevel().addFreshEntity(arrow);
        return arrow;
    }

    private static ManaArrowEntity fireAtMobAt(GameTestHelper helper, Source source, int mobX) {
        return fireArrowNorth(helper, source, mobX + 0.5D, 1.4D, SHOT_SPEED);
    }

    private static void effectOnHit(GameTestHelper helper, Source source, Holder<MobEffect> effect) {
        var cow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(2, 1, MOB_Z));
        assertFalse(helper, cow.hasEffect(effect), "Cow has effect before being shot");
        fireAtMobAt(helper, source, 2);
        helper.succeedWhen(() -> {
            assertTrue(helper, cow.getHealth() < cow.getMaxHealth(), "Cow was not hit by the arrow");
            assertTrue(helper, cow.hasEffect(effect), "Cow does not have the expected effect after being hit");
        });
    }

    // Earth arrows

    public static void earth_arrow_knocks_target_back_more(GameTestHelper helper) {
        // ManaArrowEntity.doKnockback uses a base force of 2.0 for earth arrows and 0.0 for all others, so the earth
        // arrow should push its target with a horizontal velocity of 2.0 * 0.6 = 1.2 while a moon arrow adds none
        var earthCow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(1, 1, MOB_Z));
        var plainCow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(3, 1, MOB_Z));
        fireAtMobAt(helper, Sources.EARTH, 1);
        fireAtMobAt(helper, Sources.MOON, 3);

        helper.succeedWhen(() -> {
            assertTrue(helper, earthCow.getHealth() < earthCow.getMaxHealth(), "Earth arrow target was not hit");
            assertTrue(helper, plainCow.getHealth() < plainCow.getMaxHealth(), "Plain arrow target was not hit");
            double earthPush = Math.abs(earthCow.getDeltaMovement().z);
            double plainPush = Math.abs(plainCow.getDeltaMovement().z);
            assertTrue(helper, earthPush > plainPush + 0.3D, "Earth arrow push " + earthPush + " is not clearly larger than plain arrow push " + plainPush);
        });
    }

    // Sea arrows

    public static void sea_arrow_inflicts_slowness(GameTestHelper helper) {
        effectOnHit(helper, Sources.SEA, MobEffects.SLOWNESS);
    }

    // Sky arrows

    public static void sky_arrow_ignores_gravity_in_air(GameTestHelper helper) {
        // ManaArrowEntity.setSource turns gravity off for sky arrows. A horizontal moon arrow drops under gravity
        // in the same time, which confirms that the test setup would show a fall.
        double startY = 3.5D;
        var skyArrow = fireArrowNorth(helper, Sources.SKY, 1.5D, startY, 0.5D);
        var moonArrow = fireArrowNorth(helper, Sources.MOON, 3.5D, startY, 0.5D);
        assertTrue(helper, skyArrow.isNoGravity(), "Sky arrow does not start without gravity");
        assertFalse(helper, moonArrow.isNoGravity(), "Moon arrow starts without gravity");

        double startAbsY = helper.absoluteVec(new Vec3(0.0D, startY, 0.0D)).y;
        helper.runAfterDelay(5, () -> {
            assertTrue(helper, Math.abs(skyArrow.getY() - startAbsY) < 1.0E-6D, "Sky arrow height changed in flight: " + (skyArrow.getY() - startAbsY));
            assertTrue(helper, skyArrow.isNoGravity(), "Sky arrow regained gravity in air");
            assertTrue(helper, moonArrow.getY() < startAbsY - 0.1D, "Moon arrow did not fall in flight: " + (moonArrow.getY() - startAbsY));
            helper.succeed();
        });
    }

    public static void sky_arrow_regains_gravity_in_water(GameTestHelper helper) {
        fillWater(helper);
        var arrow = makeArrow(helper, Sources.SKY, new Vec3(2.5D, 2.5D, 2.5D));
        assertTrue(helper, arrow.isNoGravity(), "Sky arrow does not start without gravity");
        helper.getLevel().addFreshEntity(arrow);

        // ManaArrowEntity.tick restores gravity to a sky arrow that is in water
        helper.runAfterDelay(3, () -> {
            assertTrue(helper, arrow.isInWater(), "Arrow is not in water");
            assertFalse(helper, arrow.isNoGravity(), "Sky arrow still has no gravity in water");
            helper.succeed();
        });
    }

    // Sun arrows

    public static void sun_arrow_makes_target_glow(GameTestHelper helper) {
        effectOnHit(helper, Sources.SUN, MobEffects.GLOWING);
    }

    public static void sun_arrow_ignites_undead(GameTestHelper helper) {
        undead_ignited_by_arrow(helper, Sources.SUN);
    }

    // Moon arrows

    public static void moon_arrow_inflicts_weakness(GameTestHelper helper) {
        effectOnHit(helper, Sources.MOON, MobEffects.WEAKNESS);
    }

    // Blood arrows

    public static void blood_arrow_inflicts_bleeding(GameTestHelper helper) {
        effectOnHit(helper, Sources.BLOOD, EffectsPM.BLEEDING.getHolder());
    }

    // Infernal arrows

    public static void infernal_arrow_is_on_fire_and_ignites_target(GameTestHelper helper) {
        var cow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(2, 1, MOB_Z));
        assertFalse(helper, cow.isOnFire(), "Cow is on fire before being shot");
        var arrow = fireAtMobAt(helper, Sources.INFERNAL, 2);
        assertTrue(helper, arrow.isOnFire(), "Infernal arrow is not on fire when fired");

        // A burning arrow sets any entity that it hits alight
        helper.succeedWhen(() -> {
            assertTrue(helper, cow.getHealth() < cow.getMaxHealth(), "Cow was not hit by the arrow");
            assertTrue(helper, cow.isOnFire(), "Cow is not on fire after being hit");
        });
    }

    // Void arrows

    public static void void_arrow_inflicts_wither(GameTestHelper helper) {
        effectOnHit(helper, Sources.VOID, MobEffects.WITHER);
    }

    // Hallowed arrows

    public static void hallowed_arrow_does_more_damage(GameTestHelper helper) {
        // Arrow damage is ceil(speed * base damage). The default base damage is 2.0 and hallowed arrows add 1.0, so at
        // speed 1.5 a plain arrow does ceil(3.0) = 3 damage and a hallowed arrow does ceil(4.5) = 5 damage.
        var plainCow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(1, 1, MOB_Z));
        var hallowedCow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(3, 1, MOB_Z));
        float startHealth = plainCow.getMaxHealth();
        assertValueEqual(helper, startHealth, hallowedCow.getMaxHealth(), "Cow max health");
        fireAtMobAt(helper, Sources.MOON, 1);
        fireAtMobAt(helper, Sources.HALLOWED, 3);

        helper.succeedWhen(() -> {
            assertTrue(helper, plainCow.getHealth() < startHealth, "Plain arrow target was not hit");
            assertTrue(helper, hallowedCow.getHealth() < startHealth, "Hallowed arrow target was not hit");
            assertValueEqual(helper, 3.0F, startHealth - plainCow.getHealth(), "Damage dealt by a plain arrow");
            assertValueEqual(helper, 5.0F, startHealth - hallowedCow.getHealth(), "Damage dealt by a hallowed arrow");
        });
    }

    public static void hallowed_arrow_ignites_undead(GameTestHelper helper) {
        undead_ignited_by_arrow(helper, Sources.HALLOWED);
    }

    /**
     * Shoots a zombie and a cow with the given arrow type and confirms that only the zombie is set alight. The zombie
     * wears a helmet so that it cannot catch fire from daylight on its own.
     */
    private static void undead_ignited_by_arrow(GameTestHelper helper, Source source) {
        Mob zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(1, 1, MOB_Z));
        zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        Mob cow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(3, 1, MOB_Z));
        assertFalse(helper, zombie.isOnFire(), "Zombie is on fire before being shot");
        assertFalse(helper, cow.isOnFire(), "Cow is on fire before being shot");
        fireArrowNorth(helper, source, 1.5D, 1.9D, SHOT_SPEED);
        fireAtMobAt(helper, source, 3);

        helper.succeedWhen(() -> {
            assertTrue(helper, zombie.getHealth() < zombie.getMaxHealth(), "Zombie was not hit by the arrow");
            assertTrue(helper, cow.getHealth() < cow.getMaxHealth(), "Cow was not hit by the arrow");
            assertTrue(helper, zombie.isOnFire(), "Zombie is not on fire after being hit");
            assertFalse(helper, cow.isOnFire(), "Cow is on fire after being hit");
        });
    }

    // Underwater flight

    public static void sea_arrow_keeps_speed_underwater(GameTestHelper helper) {
        arrow_speed_retention_underwater(helper, Sources.SEA);
    }

    public static void blood_arrow_keeps_speed_underwater(GameTestHelper helper) {
        arrow_speed_retention_underwater(helper, Sources.BLOOD);
    }

    /**
     * Fires an arrow of the given source and a moon arrow through water at the same speed and compares how much speed
     * each keeps. ManaArrowEntity.getWaterInertia gives sea and blood arrows 0.99 per tick, where other arrows keep the
     * default 0.6, so after three or four ticks from a speed of 0.5 the special arrow is still moving at about 0.48
     * while the moon arrow has slowed to 0.11 or less.
     */
    private static void arrow_speed_retention_underwater(GameTestHelper helper, Source source) {
        fillWater(helper);
        var special = fireArrowNorth(helper, source, 1.5D, 2.5D, 0.5D);
        var plain = fireArrowNorth(helper, Sources.MOON, 3.5D, 2.5D, 0.5D);
        helper.runAfterDelay(4, () -> {
            assertTrue(helper, special.isInWater(), "Special arrow is not in water");
            assertTrue(helper, plain.isInWater(), "Moon arrow is not in water");
            double specialSpeed = special.getDeltaMovement().horizontalDistance();
            double plainSpeed = plain.getDeltaMovement().horizontalDistance();
            assertTrue(helper, specialSpeed > 0.4D, "Special arrow speed in water is too low: " + specialSpeed);
            assertTrue(helper, plainSpeed < 0.2D, "Moon arrow speed in water is too high: " + plainSpeed);
            helper.succeed();
        });
    }

    private static void fillWater(GameTestHelper helper) {
        for (int x = 0; x < 5; x++) {
            for (int y = 1; y < 4; y++) {
                for (int z = 0; z < 5; z++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.WATER);
                }
            }
        }
    }
}
