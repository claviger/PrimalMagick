package com.verdantartifice.primalmagick.test.spells;

import com.verdantartifice.primalmagick.common.entities.EntityTypesPM;
import com.verdantartifice.primalmagick.common.spells.SpellManager;
import com.verdantartifice.primalmagick.common.spells.SpellPackage;
import com.verdantartifice.primalmagick.common.spells.SpellPropertiesPM;
import com.verdantartifice.primalmagick.common.spells.mods.BurstSpellMod;
import com.verdantartifice.primalmagick.common.spells.mods.ForkSpellMod;
import com.verdantartifice.primalmagick.common.spells.mods.SpellModsPM;
import com.verdantartifice.primalmagick.common.spells.payloads.BloodDamageSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.EarthDamageSpellPayload;
import com.verdantartifice.primalmagick.common.spells.vehicles.BoltSpellVehicle;
import com.verdantartifice.primalmagick.common.spells.vehicles.ProjectileSpellVehicle;
import com.verdantartifice.primalmagick.common.spells.vehicles.TouchSpellVehicle;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import com.verdantartifice.primalmagick.test.TestRandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Tests of the spell vehicles that deliver payloads (bolt, projectile, and burst/fork mods that alter delivery).
 */
public class SpellVehicleTests extends AbstractBaseTest {
    private static final float PIG_MAX_HEALTH = 10.0F;

    // Earth damage at power 1 deals 4 + 3 * 1 = 7, which leaves a 10 health pig with 3
    private static final float PIG_HEALTH_AFTER_EARTH_HIT = 3.0F;

    private static SpellPackage boltSpell(int range) {
        return SpellPackageTests.spellWithVehicle(BoltSpellVehicle.INSTANCE, v -> v.with(SpellPropertiesPM.RANGE.get(), range))
                .payload().type(EarthDamageSpellPayload.INSTANCE).with(SpellPropertiesPM.POWER.get(), 1).end()
                .build();
    }

    private static void castSpell(GameTestHelper helper, SpellPackage spell, LivingEntity caster) {
        spell.cast(helper.getLevel(), caster, SpellPayloadTests.scrollSource());
    }

    /**
     * Removes the barrier blocks that surround the test structure from a line of blocks, so that a bolt can reach beyond the
     * structure's edge. Anything other than a barrier is left alone.
     */
    private static void clearBarriers(GameTestHelper helper, BlockPos start, Direction direction, int length) {
        for (int i = 0; i < length; i++) {
            BlockPos pos = start.relative(direction, i);
            if (helper.getBlockState(pos).is(Blocks.BARRIER)) {
                helper.setBlock(pos, Blocks.AIR);
            }
        }
    }

    // Projectile tests

    public static void projectile_spell_is_subject_to_gravity(GameTestHelper helper) {
        // The caster stands in the air 4 blocks above the floor of the room and shoots horizontally to the south
        var caster = SpellPayloadTests.makeCaster(helper, new Vec3(3.5D, 4.0D, 0.5D), 0.0F, 0.0F, true);
        var spell = SpellPackageTests.spellWithVehicle(ProjectileSpellVehicle.INSTANCE)
                .payload().type(EarthDamageSpellPayload.INSTANCE).with(SpellPropertiesPM.POWER.get(), 1).end()
                .build();
        castSpell(helper, spell, caster);

        BlockPos casterPos = new BlockPos(3, 4, 0);
        var projectiles = helper.getEntities(EntityTypesPM.SPELL_PROJECTILE.get(), casterPos, 3.0D);
        assertValueEqual(helper, 1, projectiles.size(), "Number of projectiles after cast");
        var projectile = projectiles.getFirst();
        double startY = projectile.getY();
        assertValueEqual(helper, 0.0D, projectile.getDeltaMovement().y, "Projectile initial vertical speed");

        // Arrow-like projectiles lose 0.05 of vertical speed per tick, so after 4 ticks they have dropped about 0.3 blocks
        // (0 + 0.05 + 0.10 + 0.15) and are moving downward
        helper.runAfterDelay(4L, () -> {
            assertTrue(helper, projectile.isAlive(), "Projectile hit something before the gravity check");
            assertTrue(helper, projectile.getY() < startY - 0.2D, "Projectile has not dropped; started at " + startY + ", now at " + projectile.getY());
            assertTrue(helper, projectile.getDeltaMovement().y < -0.1D, "Projectile is not moving downward: " + projectile.getDeltaMovement());
            helper.succeed();
        });
    }

    // Bolt tests

    public static void bolt_spell_hits_only_targets_within_range(GameTestHelper helper) {
        var caster = SpellPayloadTests.makeCaster(helper, new Vec3(0.5D, 1.0D, 0.5D), 0.0F, 0.0F, true);
        // The caster's eyes are 1.62 blocks up, so pigs spanning 1.0 to 1.9 blocks up are on the line of sight
        clearBarriers(helper, new BlockPos(0, 2, 1), Direction.SOUTH, 14);
        clearBarriers(helper, new BlockPos(1, 2, 0), Direction.EAST, 14);
        var nearPig = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(0.5D, 2.0D, 7.5D));
        var farPig = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(9.5D, 2.0D, 0.5D));

        // A bolt with range 1 reaches 6 + 2 * 1 = 8 blocks. The near pig is 7 blocks to the south, in range. The far pig is
        // 9 blocks to the east, so even its near face at 8.55 blocks is out of range.
        castSpell(helper, boltSpell(1), caster);
        assertValueEqual(helper, PIG_HEALTH_AFTER_EARTH_HIT, nearPig.getHealth(), "Health of pig within range");

        SpellPayloadTests.face(caster, -90.0F, 0.0F);
        castSpell(helper, boltSpell(1), caster);
        assertValueEqual(helper, PIG_MAX_HEALTH, farPig.getHealth(), "Health of pig out of range");

        // A bolt with range 3 reaches 6 + 2 * 3 = 12 blocks, which now includes the far pig
        castSpell(helper, boltSpell(3), caster);
        assertValueEqual(helper, PIG_HEALTH_AFTER_EARTH_HIT, farPig.getHealth(), "Health of pig after range increase");
        helper.succeed();
    }

    public static void bolt_spell_is_instant_and_unaffected_by_gravity(GameTestHelper helper) {
        var caster = SpellPayloadTests.makeCaster(helper, new Vec3(0.5D, 1.0D, 0.5D), 0.0F, 0.0F, true);

        // The line of sight is at 1.62 blocks up. Place a pig whose top is just 0.02 above that, at the very edge of the 8 block
        // range of a range 1 bolt (the pig's near face is at 6.55 blocks). A projectile would have dropped well below that
        // height by then, but a bolt travels in a straight line.
        clearBarriers(helper, new BlockPos(0, 2, 1), Direction.SOUTH, 14);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(0.5D, 1.0D + 1.62D - 0.88D, 7.0D));
        castSpell(helper, boltSpell(1), caster);

        // No ticks have passed since the cast, but the pig has already been hit
        assertValueEqual(helper, PIG_HEALTH_AFTER_EARTH_HIT, pig.getHealth(), "Health of pig immediately after bolt cast");
        assertValueEqual(helper, 0, helper.getEntities(EntityTypesPM.SPELL_PROJECTILE.get(), new BlockPos(0, 1, 0), 20.0D).size(), "Projectiles in flight after bolt cast");
        helper.succeed();
    }

    // Burst tests

    private static SpellPackage burstSpell(int radius) {
        return SpellPackageTests.spellWithVehicle(TouchSpellVehicle.INSTANCE)
                .payload().type(BloodDamageSpellPayload.INSTANCE).with(SpellPropertiesPM.POWER.get(), 1).end()
                .primaryMod().type(BurstSpellMod.INSTANCE).with(SpellPropertiesPM.RADIUS.get(), radius).with(SpellPropertiesPM.BURST_POWER.get(), 0).end()
                .build();
    }

    private static void burst_hits_only_within_radius(GameTestHelper helper, int radius) {
        // The caster stays well away from the impact point so that it is not caught in its own burst
        var caster = SpellPayloadTests.makeCaster(helper, new Vec3(2.5D, 1.0D, 12.5D), 0.0F, 0.0F, false);
        Vec3 impact = helper.absoluteVec(new Vec3(2.5D, 1.0D, 2.5D));
        var hit = new BlockHitResult(impact, Direction.UP, BlockPos.containing(impact), false);

        // Targets are affected when their position is within the radius (in blocks) of the impact point
        var inside = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(2.5D + radius - 0.5D, 1.0D, 2.5D));
        var outside = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(2.5D + radius + 0.5D, 1.0D, 2.5D));
        var center = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(2.5D, 1.0D, 2.5D));
        SpellManager.executeSpellPayload(burstSpell(radius), hit, helper.getLevel(), caster, SpellPayloadTests.scrollSource(), true, null);

        // Blood damage is 3 + 2 * 1 = 5 at power 1, which leaves a 10 health pig with 5
        assertValueEqual(helper, 5.0F, inside.getHealth(), "Health of pig inside the burst radius");
        assertValueEqual(helper, 5.0F, center.getHealth(), "Health of pig at the burst center");
        assertValueEqual(helper, PIG_MAX_HEALTH, outside.getHealth(), "Health of pig outside the burst radius");
    }

    public static void burst_spell_affects_targets_within_radius_2(GameTestHelper helper) {
        burst_hits_only_within_radius(helper, 2);
        helper.succeed();
    }

    public static void burst_spell_affects_targets_within_radius_4(GameTestHelper helper) {
        burst_hits_only_within_radius(helper, 4);
        helper.succeed();
    }

    // Fork tests

    public static void fork_spell_creates_one_vehicle_per_fork(GameTestHelper helper, int forks) {
        var caster = SpellPayloadTests.makeCaster(helper, new Vec3(3.5D, 4.0D, 0.5D), 0.0F, 0.0F, true);
        var spell = SpellPackageTests.spellWithVehicle(ProjectileSpellVehicle.INSTANCE)
                .payload().type(EarthDamageSpellPayload.INSTANCE).with(SpellPropertiesPM.POWER.get(), 1).end()
                .primaryMod().type(ForkSpellMod.INSTANCE).with(SpellPropertiesPM.FORKS.get(), forks).with(SpellPropertiesPM.PRECISION.get(), 0).end()
                .build();
        castSpell(helper, spell, caster);

        // Each fork launches its own projectile
        var projectiles = helper.getEntities(EntityTypesPM.SPELL_PROJECTILE.get(), new BlockPos(3, 4, 0), 3.0D);
        assertValueEqual(helper, forks, projectiles.size(), "Number of projectiles after cast with " + forks + " forks");
        helper.succeed();
    }

    /**
     * Confirms that fork direction vectors lie within the maximum spread angle of the original direction.
     *
     * @param maxDegrees the spread angle expected for the given precision, which is 10 + 15 * (5 - precision)
     */
    public static void fork_vehicles_are_spread_within_precision_angle(GameTestHelper helper, int precision, double maxDegrees) {
        var spell = SpellPackageTests.spellWithVehicle(ProjectileSpellVehicle.INSTANCE)
                .payload().type(EarthDamageSpellPayload.INSTANCE).with(SpellPropertiesPM.POWER.get(), 1).end()
                .primaryMod().type(ForkSpellMod.INSTANCE).with(SpellPropertiesPM.FORKS.get(), 5).with(SpellPropertiesPM.PRECISION.get(), precision).end()
                .build();
        var forkMod = spell.getMod(SpellModsPM.FORK.get()).orElseThrow().getComponent();
        Vec3 direction = new Vec3(0.0D, 0.0D, 1.0D);

        // With the random source pinned to its largest offset, every fork should be exactly at the maximum spread angle
        var maxRandom = TestRandomSource.builder().setDouble(1.0D).setGaussian(1.0D).build();
        List<Vec3> maxVectors = forkMod.getDirectionUnitVectors(direction, maxRandom, spell, ItemStack.EMPTY);
        assertValueEqual(helper, 5, maxVectors.size(), "Number of fork vectors");
        for (Vec3 vector : maxVectors) {
            double angle = angleDegrees(direction, vector);
            assertTrue(helper, Math.abs(angle - maxDegrees) < 0.01D, "Fork at maximum offset is at " + angle + " degrees, expected " + maxDegrees);
        }

        // With the random source pinned to no offset, every fork should be exactly on the original direction
        var minRandom = TestRandomSource.builder().setDouble(0.0D).setGaussian(1.0D).build();
        for (Vec3 vector : forkMod.getDirectionUnitVectors(direction, minRandom, spell, ItemStack.EMPTY)) {
            double angle = angleDegrees(direction, vector);
            assertTrue(helper, angle < 0.01D, "Fork at zero offset is at " + angle + " degrees");
        }

        // With real randomness, no fork may exceed the maximum spread angle
        for (int trial = 0; trial < 20; trial++) {
            for (Vec3 vector : forkMod.getDirectionUnitVectors(direction, helper.getLevel().getRandom(), spell, ItemStack.EMPTY)) {
                double angle = angleDegrees(direction, vector);
                assertTrue(helper, angle <= maxDegrees + 0.01D, "Random fork is at " + angle + " degrees, more than the maximum " + maxDegrees);
            }
        }
        helper.succeed();
    }

    private static double angleDegrees(Vec3 a, Vec3 b) {
        return Math.toDegrees(Math.acos(Math.min(1.0D, a.normalize().dot(b.normalize()))));
    }
}
