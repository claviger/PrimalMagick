package com.verdantartifice.primalmagick.test.spells;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.effects.EffectsPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.misc.BlockBreaker;
import com.verdantartifice.primalmagick.common.misc.EntitySwapper;
import com.verdantartifice.primalmagick.common.spells.SpellManager;
import com.verdantartifice.primalmagick.common.spells.SpellPackage;
import com.verdantartifice.primalmagick.common.spells.SpellPropertiesPM;
import com.verdantartifice.primalmagick.common.spells.mods.BurstSpellMod;
import com.verdantartifice.primalmagick.common.spells.payloads.AbstractSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.BloodDamageSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.BreakSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.ConfiguredSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.ConjureAnimalSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.ConjureLavaSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.ConjureLightSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.ConjureStoneSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.ConjureWaterSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.ConsecrateSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.DrainSoulSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.EarthDamageSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.FlameDamageSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.FrostDamageSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.HealingSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.HolyDamageSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.LunarDamageSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.PolymorphWolfSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.ShearSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.SolarDamageSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.TeleportSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.VoidDamageSpellPayload;
import com.verdantartifice.primalmagick.common.spells.vehicles.TouchSpellVehicle;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableLong;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Tests of the effects of individual spell payloads. Most tests build a spell package and run the payload directly
 * against a target, since the vehicle that would normally deliver it is not what is under test.
 */
public class SpellPayloadTests extends AbstractBaseTest {
    private static final float PIG_MAX_HEALTH = 10.0F;
    private static final float ZOMBIE_MAX_HEALTH = 20.0F;
    private static final float SKELETON_MAX_HEALTH = 20.0F;

    // Helpers

    /**
     * Creates a stack to stand in for the item a spell is cast from. It must not be empty, because payloads that
     * enchant a copy of the source stack would otherwise modify the shared empty stack.
     */
    public static ItemStack scrollSource() {
        return new ItemStack(ItemsPM.SPELL_SCROLL_FILLED.get());
    }

    /**
     * Creates a mock player standing at the given position relative to the test, optionally added to the level.
     */
    public static ServerPlayer makeCaster(GameTestHelper helper, Vec3 relativePos, float yaw, float pitch, boolean joinLevel) {
        var player = makeMockServerPlayer(helper, joinLevel);
        Vec3 abs = helper.absoluteVec(relativePos);
        player.absSnapTo(abs.x, abs.y, abs.z, yaw, pitch);
        face(player, yaw, pitch);
        // Survival, so that block breaking drops items as it would for a real player
        player.setGameMode(GameType.SURVIVAL);
        return player;
    }

    /**
     * Turns the entity to face the given direction. The head rotation is what determines the view vector, and it is
     * not updated by snapping the entity's position and rotation.
     */
    public static void face(LivingEntity entity, float yaw, float pitch) {
        entity.setYRot(yaw);
        entity.setXRot(pitch);
        entity.setYHeadRot(yaw);
        entity.setYBodyRot(yaw);
    }

    protected static ServerPlayer makeCaster(GameTestHelper helper) {
        return makeCaster(helper, new Vec3(0.5D, 1.0D, 0.5D), 0.0F, 0.0F, false);
    }

    /**
     * Creates a Touch spell carrying the given payload, configured by the given consumer.
     */
    public static SpellPackage touchSpell(AbstractSpellPayload<?> payload, Consumer<ConfiguredSpellPayload.Builder> configurer) {
        var payloadBuilder = SpellPackageTests.spellWithVehicle(TouchSpellVehicle.INSTANCE).payload().type(payload);
        configurer.accept(payloadBuilder);
        return payloadBuilder.end().build();
    }

    protected static SpellPackage powerSpell(AbstractSpellPayload<?> payload, int power) {
        return touchSpell(payload, b -> b.with(SpellPropertiesPM.POWER.get(), power));
    }

    protected static SpellPackage powerDurationSpell(AbstractSpellPayload<?> payload, int power, int duration) {
        return touchSpell(payload, b -> b.with(SpellPropertiesPM.POWER.get(), power).with(SpellPropertiesPM.DURATION.get(), duration));
    }

    protected static SpellPackage durationSpell(AbstractSpellPayload<?> payload, int duration) {
        return touchSpell(payload, b -> b.with(SpellPropertiesPM.NON_ZERO_DURATION.get(), duration));
    }

    /**
     * Runs the payload of the given spell directly against the given hit result, as if cast by the given caster.
     */
    public static void execute(GameTestHelper helper, SpellPackage spell, HitResult hit, LivingEntity caster) {
        spell.payload().getComponent().execute(hit, null, spell, helper.getLevel(), caster, scrollSource(), null);
    }

    protected static void execute(GameTestHelper helper, SpellPackage spell, Entity target, LivingEntity caster) {
        execute(helper, spell, new EntityHitResult(target), caster);
    }

    /**
     * Spawns a mob with so much health that spell damage cannot kill it before its effects are checked.
     */
    protected static <T extends Mob> T spawnTanky(GameTestHelper helper, EntityType<T> type, Vec3 relativePos) {
        T mob = helper.spawnWithNoFreeWill(type, relativePos);
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100.0D);
        mob.setHealth(100.0F);
        return mob;
    }

    protected static BlockHitResult hitTop(GameTestHelper helper, BlockPos relativePos) {
        BlockPos abs = helper.absolutePos(relativePos);
        return new BlockHitResult(Vec3.atCenterOf(abs).add(0.0D, 0.5D, 0.0D), Direction.UP, abs, false);
    }

    protected static void assertEffect(GameTestHelper helper, LivingEntity target, Holder<MobEffect> effect, int expectedAmplifier, int expectedDuration, String label) {
        var instance = target.getEffect(effect);
        assertTrue(helper, instance != null, label + ": effect is missing");
        assertValueEqual(helper, expectedAmplifier, instance.getAmplifier(), label + ": effect amplifier");
        assertValueEqual(helper, expectedDuration, instance.getDuration(), label + ": effect duration");
    }

    protected static int countItems(GameTestHelper helper, Item item, BlockPos relativePos, double radius) {
        int total = 0;
        for (ItemEntity entity : helper.getEntities(EntityType.ITEM, relativePos, radius)) {
            if (entity.getItem().is(item)) {
                total += entity.getItem().getCount();
            }
        }
        return total;
    }

    protected static double horizontalSpeed(Entity entity) {
        Vec3 motion = entity.getDeltaMovement();
        return Math.hypot(motion.x, motion.z);
    }

    // Damage payload tests

    public static void earth_damage_knockback_scales_with_power(GameTestHelper helper) {
        var caster = makeCaster(helper);
        var weak = spawnTanky(helper, EntityType.PIG, new Vec3(3.5D, 1.0D, 0.5D));
        var strong = spawnTanky(helper, EntityType.PIG, new Vec3(3.5D, 1.0D, 2.5D));
        execute(helper, powerSpell(EarthDamageSpellPayload.INSTANCE, 1), weak, caster);
        execute(helper, powerSpell(EarthDamageSpellPayload.INSTANCE, 5), strong, caster);

        // The spell's knockback strength is 0.25 * (4 + 3 * power), which is 1.75 at power 1 and 4.75 at power 5. The
        // damage itself first knocks the target back by vanilla's 0.4, half of which carries into the spell's knockback,
        // along the same direction, so the speeds are 0.2 + 1.75 and 0.2 + 4.75.
        double weakSpeed = horizontalSpeed(weak);
        double strongSpeed = horizontalSpeed(strong);
        assertTrue(helper, Math.abs(weakSpeed - 1.95D) < 0.02D, "Power 1 knockback speed is not as expected: " + weakSpeed);
        assertTrue(helper, Math.abs(strongSpeed - 4.95D) < 0.02D, "Power 5 knockback speed is not as expected: " + strongSpeed);
        assertTrue(helper, strongSpeed > weakSpeed, "Higher power did not knock the target back further");
        helper.succeed();
    }

    public static void frost_damage_applies_slowness_scaled_by_power_and_duration(GameTestHelper helper) {
        var caster = makeCaster(helper);
        var low = spawnTanky(helper, EntityType.PIG, new Vec3(0.5D, 1.0D, 0.5D));
        var mid = spawnTanky(helper, EntityType.PIG, new Vec3(1.5D, 1.0D, 0.5D));
        var high = spawnTanky(helper, EntityType.PIG, new Vec3(2.5D, 1.0D, 0.5D));
        execute(helper, powerDurationSpell(FrostDamageSpellPayload.INSTANCE, 1, 1), low, caster);
        execute(helper, powerDurationSpell(FrostDamageSpellPayload.INSTANCE, 2, 2), mid, caster);
        execute(helper, powerDurationSpell(FrostDamageSpellPayload.INSTANCE, 5, 4), high, caster);

        // Amplifier is (int)((1 + power) / 3), which is 0, 1 and 2 here; duration is 2 seconds (40 ticks) per duration point
        assertEffect(helper, low, MobEffects.SLOWNESS, 0, 40, "Power 1, duration 1");
        assertEffect(helper, mid, MobEffects.SLOWNESS, 1, 80, "Power 2, duration 2");
        assertEffect(helper, high, MobEffects.SLOWNESS, 2, 160, "Power 5, duration 4");
        helper.succeed();
    }

    public static void solar_damage_applies_glowing_scaled_by_duration(GameTestHelper helper) {
        var caster = makeCaster(helper);
        var shortTarget = spawnTanky(helper, EntityType.PIG, new Vec3(0.5D, 1.0D, 0.5D));
        var longTarget = spawnTanky(helper, EntityType.PIG, new Vec3(1.5D, 1.0D, 0.5D));
        execute(helper, powerDurationSpell(SolarDamageSpellPayload.INSTANCE, 1, 1), shortTarget, caster);
        execute(helper, powerDurationSpell(SolarDamageSpellPayload.INSTANCE, 1, 4), longTarget, caster);

        // Glowing lasts 2 seconds (40 ticks) per duration point, at amplifier 0
        assertEffect(helper, shortTarget, MobEffects.GLOWING, 0, 40, "Duration 1");
        assertEffect(helper, longTarget, MobEffects.GLOWING, 0, 160, "Duration 4");
        helper.succeed();
    }

    public static void solar_damage_ignites_undead_targets_for_duration(GameTestHelper helper) {
        var caster = makeCaster(helper);
        var zombie = spawnTanky(helper, EntityType.ZOMBIE, new Vec3(0.5D, 1.0D, 0.5D));
        var longZombie = spawnTanky(helper, EntityType.ZOMBIE, new Vec3(1.5D, 1.0D, 0.5D));
        var pig = spawnTanky(helper, EntityType.PIG, new Vec3(2.5D, 1.0D, 0.5D));
        execute(helper, powerDurationSpell(SolarDamageSpellPayload.INSTANCE, 1, 1), zombie, caster);
        execute(helper, powerDurationSpell(SolarDamageSpellPayload.INSTANCE, 1, 3), longZombie, caster);
        execute(helper, powerDurationSpell(SolarDamageSpellPayload.INSTANCE, 1, 3), pig, caster);

        // Undead are ignited for 2 seconds per duration point, so 2 seconds (40 ticks) and 6 seconds (120 ticks)
        assertValueEqual(helper, 40, zombie.getRemainingFireTicks(), "Fire ticks of zombie hit by duration 1 spell");
        assertValueEqual(helper, 120, longZombie.getRemainingFireTicks(), "Fire ticks of zombie hit by duration 3 spell");
        assertValueEqual(helper, 0, pig.getRemainingFireTicks(), "Fire ticks of pig hit by solar spell");
        helper.succeed();
    }

    public static void lunar_damage_applies_weakness_scaled_by_power_and_duration(GameTestHelper helper) {
        var caster = makeCaster(helper);
        var low = spawnTanky(helper, EntityType.PIG, new Vec3(0.5D, 1.0D, 0.5D));
        var mid = spawnTanky(helper, EntityType.PIG, new Vec3(1.5D, 1.0D, 0.5D));
        var high = spawnTanky(helper, EntityType.PIG, new Vec3(2.5D, 1.0D, 0.5D));
        execute(helper, powerDurationSpell(LunarDamageSpellPayload.INSTANCE, 1, 1), low, caster);
        execute(helper, powerDurationSpell(LunarDamageSpellPayload.INSTANCE, 2, 2), mid, caster);
        execute(helper, powerDurationSpell(LunarDamageSpellPayload.INSTANCE, 5, 4), high, caster);

        // Amplifier is (int)((1 + power) / 3), which is 0, 1 and 2 here; duration is 2 seconds (40 ticks) per duration point
        assertEffect(helper, low, MobEffects.WEAKNESS, 0, 40, "Power 1, duration 1");
        assertEffect(helper, mid, MobEffects.WEAKNESS, 1, 80, "Power 2, duration 2");
        assertEffect(helper, high, MobEffects.WEAKNESS, 2, 160, "Power 5, duration 4");
        helper.succeed();
    }

    public static void blood_damage_ignores_armor(GameTestHelper helper) {
        var caster = makeCaster(helper);
        var bloodTarget = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new Vec3(0.5D, 1.0D, 0.5D));
        var earthTarget = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new Vec3(1.5D, 1.0D, 0.5D));
        bloodTarget.getAttribute(Attributes.ARMOR).setBaseValue(20.0D);
        earthTarget.getAttribute(Attributes.ARMOR).setBaseValue(20.0D);
        assertValueEqual(helper, ZOMBIE_MAX_HEALTH, bloodTarget.getHealth(), "Blood target starting health");
        assertValueEqual(helper, ZOMBIE_MAX_HEALTH, earthTarget.getHealth(), "Earth target starting health");

        // Blood damage is 3 + 2 * power = 5 at power 1, and ignores armor, so the full 5 is taken
        execute(helper, powerSpell(BloodDamageSpellPayload.INSTANCE, 1), bloodTarget, caster);
        assertTrue(helper, Math.abs(bloodTarget.getHealth() - 15.0F) < 0.01F, "Armored target did not take full blood damage; health is " + bloodTarget.getHealth());

        // Earth damage is 4 + 3 * power = 7 at power 1, which armor 20 reduces to 7 * (1 - (20 - 7 / 2) / 25) = 2.38
        execute(helper, powerSpell(EarthDamageSpellPayload.INSTANCE, 1), earthTarget, caster);
        assertTrue(helper, Math.abs(earthTarget.getHealth() - 17.62F) < 0.05F, "Armored target did not take armor-reduced earth damage; health is " + earthTarget.getHealth());
        helper.succeed();
    }

    public static void flame_damage_ignites_target_for_duration(GameTestHelper helper) {
        var caster = makeCaster(helper);
        var shortTarget = spawnTanky(helper, EntityType.PIG, new Vec3(0.5D, 1.0D, 0.5D));
        var longTarget = spawnTanky(helper, EntityType.PIG, new Vec3(1.5D, 1.0D, 0.5D));
        execute(helper, powerDurationSpell(FlameDamageSpellPayload.INSTANCE, 1, 1), shortTarget, caster);
        execute(helper, powerDurationSpell(FlameDamageSpellPayload.INSTANCE, 1, 3), longTarget, caster);

        // Targets burn for 2 seconds per duration point, so 2 seconds (40 ticks) and 6 seconds (120 ticks)
        assertValueEqual(helper, 40, shortTarget.getRemainingFireTicks(), "Fire ticks after duration 1 spell");
        assertValueEqual(helper, 120, longTarget.getRemainingFireTicks(), "Fire ticks after duration 3 spell");
        helper.succeed();
    }

    public static void void_damage_applies_wither(GameTestHelper helper) {
        var caster = makeCaster(helper);
        var low = spawnTanky(helper, EntityType.PIG, new Vec3(0.5D, 1.0D, 0.5D));
        var high = spawnTanky(helper, EntityType.PIG, new Vec3(1.5D, 1.0D, 0.5D));
        execute(helper, powerDurationSpell(VoidDamageSpellPayload.INSTANCE, 1, 1), low, caster);
        execute(helper, powerDurationSpell(VoidDamageSpellPayload.INSTANCE, 5, 3), high, caster);

        // Amplifier is (int)((1 + power) / 3), which is 0 and 2; duration is 2 seconds (40 ticks) per duration point
        assertEffect(helper, low, MobEffects.WITHER, 0, 40, "Power 1, duration 1");
        assertEffect(helper, high, MobEffects.WITHER, 2, 120, "Power 5, duration 3");
        helper.succeed();
    }

    public static void holy_damage_deals_double_damage_to_undead(GameTestHelper helper) {
        var caster = makeCaster(helper);
        var skeleton = helper.spawnWithNoFreeWill(EntityType.SKELETON, new Vec3(0.5D, 1.0D, 0.5D));
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(1.5D, 1.0D, 0.5D));
        assertValueEqual(helper, SKELETON_MAX_HEALTH, skeleton.getHealth(), "Skeleton starting health");
        assertValueEqual(helper, PIG_MAX_HEALTH, pig.getHealth(), "Pig starting health");

        // Holy damage is 4 + 3 * power = 7 at power 1, doubled to 14 against the undead
        var spell = powerSpell(HolyDamageSpellPayload.INSTANCE, 1);
        execute(helper, spell, skeleton, caster);
        execute(helper, spell, pig, caster);
        assertTrue(helper, Math.abs(skeleton.getHealth() - 6.0F) < 0.01F, "Skeleton did not take double damage; health is " + skeleton.getHealth());
        assertTrue(helper, Math.abs(pig.getHealth() - 3.0F) < 0.01F, "Pig did not take normal damage; health is " + pig.getHealth());
        helper.succeed();
    }

    // Healing tests

    public static void healing_restores_health_to_target(GameTestHelper helper) {
        var caster = makeCaster(helper);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(0.5D, 1.0D, 0.5D));
        pig.setHealth(3.0F);

        // Healing restores 2 * power health, which is 4 at power 2
        execute(helper, powerSpell(HealingSpellPayload.INSTANCE, 2), pig, caster);
        assertTrue(helper, Math.abs(pig.getHealth() - 7.0F) < 0.01F, "Pig was not healed by the expected amount; health is " + pig.getHealth());
        helper.succeed();
    }

    public static void healing_overheal_grants_absorption(GameTestHelper helper) {
        var caster = makeCaster(helper);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(0.5D, 1.0D, 0.5D));
        assertValueEqual(helper, PIG_MAX_HEALTH, pig.getHealth(), "Pig starting health");

        // Power 5 heals 10 on a full health pig, so the overhealing is 10, which is two levels of four, so Absorption II (amplifier 1)
        // for 200 ticks. Absorption II starts with 4 * (1 + 1) = 8 absorption.
        execute(helper, powerSpell(HealingSpellPayload.INSTANCE, 5), pig, caster);
        assertEffect(helper, pig, MobEffects.ABSORPTION, 1, 200, "Overhealed pig");
        assertTrue(helper, Math.abs(pig.getAbsorptionAmount() - 8.0F) < 0.01F, "Absorption amount is not as expected: " + pig.getAbsorptionAmount());
        helper.succeed();
    }

    public static void healing_small_overheal_grants_no_absorption(GameTestHelper helper) {
        var caster = makeCaster(helper);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(0.5D, 1.0D, 0.5D));

        // Power 1 heals 2 on a full health pig, so the overhealing is 2, which is less than the 4 needed for the first level
        execute(helper, powerSpell(HealingSpellPayload.INSTANCE, 1), pig, caster);
        assertFalse(helper, pig.hasEffect(MobEffects.ABSORPTION), "Pig with small overheal has absorption");
        helper.succeed();
    }

    // Drain soul tests

    public static void drain_soul_applies_debuff(GameTestHelper helper) {
        var caster = makeCaster(helper);
        var pig = spawnTanky(helper, EntityType.PIG, new Vec3(0.5D, 1.0D, 0.5D));
        assertFalse(helper, pig.hasEffect(EffectsPM.DRAIN_SOUL.getHolder()), "Pig has drain soul effect before spell");

        // The effect lasts 3 seconds (60 ticks) per duration point, so 120 ticks at duration 2
        execute(helper, durationSpell(DrainSoulSpellPayload.INSTANCE, 2), pig, caster);
        assertEffect(helper, pig, EffectsPM.DRAIN_SOUL.getHolder(), 0, 120, "Drain soul");
        helper.succeed();
    }

    public static void drain_soul_kill_drops_soul_gems_proportional_to_max_health(GameTestHelper helper) {
        var caster = makeCaster(helper);
        var zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new Vec3(0.5D, 1.0D, 0.5D));
        var bigZombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new Vec3(2.5D, 1.0D, 2.5D));
        bigZombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(30.0D);
        bigZombie.setHealth(30.0F);
        execute(helper, durationSpell(DrainSoulSpellPayload.INSTANCE, 1), zombie, caster);
        execute(helper, durationSpell(DrainSoulSpellPayload.INSTANCE, 1), bigZombie, caster);
        zombie.kill(helper.getLevel());
        bigZombie.kill(helper.getLevel());

        // A hostile creature drops max health / 20 soul gems: 20 / 20 = 1.0 gems and no slivers, or 30 / 20 = 1.5 gems, which is
        // 1 gem plus 5 slivers (each sliver is a tenth of a gem)
        assertValueEqual(helper, 1, countItems(helper, ItemsPM.SOUL_GEM.get(), new BlockPos(0, 1, 0), 1.0D), "Soul gems dropped by 20 health zombie");
        assertValueEqual(helper, 0, countItems(helper, ItemsPM.SOUL_GEM_SLIVER.get(), new BlockPos(0, 1, 0), 1.0D), "Soul gem slivers dropped by 20 health zombie");
        assertValueEqual(helper, 1, countItems(helper, ItemsPM.SOUL_GEM.get(), new BlockPos(2, 1, 2), 1.0D), "Soul gems dropped by 30 health zombie");
        assertValueEqual(helper, 5, countItems(helper, ItemsPM.SOUL_GEM_SLIVER.get(), new BlockPos(2, 1, 2), 1.0D), "Soul gem slivers dropped by 30 health zombie");
        helper.succeed();
    }

    public static void drain_soul_passive_mobs_drop_fewer_soul_gems(GameTestHelper helper) {
        var caster = makeCaster(helper);
        var cow = helper.spawnWithNoFreeWill(EntityType.COW, new Vec3(0.5D, 1.0D, 0.5D));
        var zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new Vec3(2.5D, 1.0D, 2.5D));
        execute(helper, durationSpell(DrainSoulSpellPayload.INSTANCE, 1), cow, caster);
        execute(helper, durationSpell(DrainSoulSpellPayload.INSTANCE, 1), zombie, caster);
        cow.kill(helper.getLevel());
        zombie.kill(helper.getLevel());

        // A friendly creature drops sqrt(max health) / 20 soul gems: sqrt(10) / 20 = 0.158 gems, which is no whole gems and
        // floor(1.58) = 1 sliver. The hostile zombie drops a whole gem, equivalent to 10 slivers.
        assertValueEqual(helper, 0, countItems(helper, ItemsPM.SOUL_GEM.get(), new BlockPos(0, 1, 0), 1.0D), "Soul gems dropped by cow");
        assertValueEqual(helper, 1, countItems(helper, ItemsPM.SOUL_GEM_SLIVER.get(), new BlockPos(0, 1, 0), 1.0D), "Soul gem slivers dropped by cow");
        assertValueEqual(helper, 1, countItems(helper, ItemsPM.SOUL_GEM.get(), new BlockPos(2, 1, 2), 1.0D), "Soul gems dropped by zombie");
        helper.succeed();
    }

    // Block payload tests

    public static void break_spell_speed_depends_on_power(GameTestHelper helper) {
        var caster = makeCaster(helper);
        BlockPos slowPos = new BlockPos(0, 1, 0);
        BlockPos fastPos = new BlockPos(2, 1, 2);
        helper.setBlock(slowPos, Blocks.STONE);
        helper.setBlock(fastPos, Blocks.STONE);
        execute(helper, touchSpell(BreakSpellPayload.INSTANCE, b -> b.with(SpellPropertiesPM.POWER.get(), 1)), hitTop(helper, slowPos), caster);
        execute(helper, touchSpell(BreakSpellPayload.INSTANCE, b -> b.with(SpellPropertiesPM.POWER.get(), 5)), hitTop(helper, fastPos), caster);

        // Stone has hardness 1.5, so a block breaker for it has durability sqrt(100 * 1.5) = 12.2 and each round of
        // breaking removes the spell's power. Power 5 needs 3 rounds and power 1 needs 13, and each round takes at least a
        // tick, so the strong spell finishes at least 10 ticks earlier than the weak one.
        MutableLong slowTick = new MutableLong(-1L);
        MutableLong fastTick = new MutableLong(-1L);
        helper.onEachTick(() -> {
            if (slowTick.longValue() < 0 && helper.getBlockState(slowPos).isAir()) {
                slowTick.setValue(helper.getTick());
            }
            if (fastTick.longValue() < 0 && helper.getBlockState(fastPos).isAir()) {
                fastTick.setValue(helper.getTick());
            }
        });
        helper.succeedWhen(() -> {
            assertTrue(helper, slowTick.longValue() >= 0 && fastTick.longValue() >= 0, "Not both blocks have been broken yet");
            assertTrue(helper, fastTick.longValue() + 10 <= slowTick.longValue(), "Power 5 break (tick " + fastTick + ") was not substantially faster than power 1 break (tick " + slowTick + ")");
        });
    }

    public static void break_spell_breaks_hard_blocks(GameTestHelper helper) {
        var caster = makeCaster(helper);
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.OBSIDIAN);
        execute(helper, touchSpell(BreakSpellPayload.INSTANCE, b -> b.with(SpellPropertiesPM.POWER.get(), 5)), hitTop(helper, pos), caster);

        // Obsidian has hardness 50, so it has durability sqrt(100 * 50) = 70.7 and takes 15 rounds at power 5; it still breaks
        helper.succeedWhen(() -> helper.assertBlockNotPresent(Blocks.OBSIDIAN, pos));
    }

    public static void break_spell_has_no_effect_on_entities(GameTestHelper helper) {
        var caster = makeCaster(helper);
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.STONE);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(1.5D, 2.0D, 1.5D));
        execute(helper, touchSpell(BreakSpellPayload.INSTANCE, b -> b.with(SpellPropertiesPM.POWER.get(), 5)), pig, caster);
        assertFalse(helper, BlockBreaker.hasBreakerQueued(helper.getLevel(), helper.absolutePos(pos)), "A block breaker was queued by hitting an entity");

        // A power 5 break would remove the stone within 8 ticks, so give it plenty of time
        helper.runAfterDelay(30L, () -> {
            helper.assertBlockPresent(Blocks.STONE, pos);
            assertTrue(helper, pig.isAlive(), "Pig is no longer alive");
            assertValueEqual(helper, PIG_MAX_HEALTH, pig.getHealth(), "Pig health after break spell");
            helper.succeed();
        });
    }

    public static void break_spell_respects_silk_touch(GameTestHelper helper) {
        var caster = makeCaster(helper);
        BlockPos plainPos = new BlockPos(0, 1, 0);
        BlockPos silkPos = new BlockPos(2, 1, 2);
        helper.setBlock(plainPos, Blocks.GRASS_BLOCK);
        helper.setBlock(silkPos, Blocks.GRASS_BLOCK);
        execute(helper, touchSpell(BreakSpellPayload.INSTANCE, b -> b.with(SpellPropertiesPM.POWER.get(), 5).with(SpellPropertiesPM.SILK_TOUCH.get(), 0)), hitTop(helper, plainPos), caster);
        execute(helper, touchSpell(BreakSpellPayload.INSTANCE, b -> b.with(SpellPropertiesPM.POWER.get(), 5).with(SpellPropertiesPM.SILK_TOUCH.get(), 1)), hitTop(helper, silkPos), caster);

        // Grass drops dirt when broken normally and itself when broken with silk touch
        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(Blocks.GRASS_BLOCK, plainPos);
            helper.assertBlockNotPresent(Blocks.GRASS_BLOCK, silkPos);
            assertValueEqual(helper, 1, countItems(helper, Items.DIRT, plainPos, 1.0D), "Dirt items dropped without silk touch");
            assertValueEqual(helper, 0, countItems(helper, Items.GRASS_BLOCK, plainPos, 1.0D), "Grass items dropped without silk touch");
            assertValueEqual(helper, 1, countItems(helper, Items.GRASS_BLOCK, silkPos, 1.0D), "Grass items dropped with silk touch");
            assertValueEqual(helper, 0, countItems(helper, Items.DIRT, silkPos, 1.0D), "Dirt items dropped with silk touch");
        });
    }

    public static void conjure_stone_places_stone_at_target(GameTestHelper helper) {
        var caster = makeCaster(helper);
        BlockPos floorPos = new BlockPos(1, 0, 1);
        helper.setBlock(floorPos, Blocks.OAK_PLANKS);
        execute(helper, touchSpell(ConjureStoneSpellPayload.INSTANCE, b -> {}), hitTop(helper, floorPos), caster);

        // The target block is solid, so the stone goes in the adjacent space on the side that was hit
        helper.assertBlockPresent(Blocks.STONE, floorPos.above());
        helper.assertBlockPresent(Blocks.OAK_PLANKS, floorPos);
        helper.succeed();
    }

    public static void conjure_water_places_water_source(GameTestHelper helper) {
        var caster = makeCaster(helper);
        BlockPos floorPos = new BlockPos(1, 0, 1);
        helper.setBlock(floorPos, Blocks.OAK_PLANKS);
        execute(helper, touchSpell(ConjureWaterSpellPayload.INSTANCE, b -> {}), hitTop(helper, floorPos), caster);

        BlockState placed = helper.getBlockState(floorPos.above());
        assertTrue(helper, placed.is(Blocks.WATER), "Block above target is not water: " + placed);
        assertTrue(helper, placed.getFluidState().isSource(), "Water is not a source block: " + placed.getFluidState());
        helper.succeed();
    }

    public static void conjure_lava_places_lava_source(GameTestHelper helper) {
        var caster = makeCaster(helper);
        BlockPos floorPos = new BlockPos(1, 0, 1);
        helper.setBlock(floorPos, Blocks.OAK_PLANKS);
        execute(helper, touchSpell(ConjureLavaSpellPayload.INSTANCE, b -> {}), hitTop(helper, floorPos), caster);

        BlockState placed = helper.getBlockState(floorPos.above());
        assertTrue(helper, placed.is(Blocks.LAVA), "Block above target is not lava: " + placed);
        assertTrue(helper, placed.getFluidState().isSource(), "Lava is not a source block: " + placed.getFluidState());
        helper.succeed();
    }

    public static void conjure_light_places_glow_field(GameTestHelper helper) {
        var caster = makeCaster(helper);
        BlockPos floorPos = new BlockPos(1, 0, 1);
        helper.setBlock(floorPos, Blocks.OAK_PLANKS);
        execute(helper, touchSpell(ConjureLightSpellPayload.INSTANCE, b -> {}), hitTop(helper, floorPos), caster);

        helper.assertBlockPresent(BlocksPM.GLOW_FIELD.get(), floorPos.above());
        helper.succeed();
    }

    public static void consecrate_places_two_consecration_fields(GameTestHelper helper) {
        var caster = makeCaster(helper);
        BlockPos floorPos = new BlockPos(1, 0, 1);
        helper.setBlock(floorPos, Blocks.OAK_PLANKS);
        execute(helper, touchSpell(ConsecrateSpellPayload.INSTANCE, b -> {}), hitTop(helper, floorPos), caster);

        // The payload places a column of 2 blocks, starting in the adjacent space on the side that was hit
        helper.assertBlockPresent(BlocksPM.CONSECRATION_FIELD.get(), floorPos.above());
        helper.assertBlockPresent(BlocksPM.CONSECRATION_FIELD.get(), floorPos.above(2));
        helper.succeed();
    }

    // Shear tests

    public static void shear_spell_breaks_leaves_and_drops_them(GameTestHelper helper) {
        var caster = makeCaster(helper);
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, Boolean.TRUE));
        execute(helper, touchSpell(ShearSpellPayload.INSTANCE, b -> {}), hitTop(helper, pos), caster);

        // Shears harvest leaves themselves rather than saplings or sticks
        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(Blocks.OAK_LEAVES, pos);
            assertValueEqual(helper, 1, countItems(helper, Items.OAK_LEAVES, pos, 1.5D), "Leaf items dropped by shear spell");
        });
    }

    public static void shear_spell_shears_sheep_for_wool(GameTestHelper helper) {
        var caster = makeCaster(helper);
        var sheep = helper.spawnWithNoFreeWill(EntityType.SHEEP, new Vec3(1.5D, 1.0D, 1.5D));
        sheep.setColor(DyeColor.WHITE);
        assertFalse(helper, sheep.isSheared(), "Sheep is sheared before spell");

        execute(helper, touchSpell(ShearSpellPayload.INSTANCE, b -> {}), sheep, caster);
        assertTrue(helper, sheep.isSheared(), "Sheep was not sheared by spell");
        int wool = countItems(helper, Items.WHITE_WOOL, new BlockPos(1, 1, 1), 2.0D);
        assertTrue(helper, wool >= 1 && wool <= 3, "Shearing a sheep should drop 1 to 3 wool, but dropped " + wool);
        helper.succeed();
    }

    public static void shear_spell_disarms_tripwire_safely(GameTestHelper helper) {
        var caster = makeCaster(helper);
        BlockPos hookAPos = new BlockPos(1, 1, 2);
        BlockPos stringPos = new BlockPos(2, 1, 2);
        BlockPos hookBPos = new BlockPos(3, 1, 2);
        helper.setBlock(new BlockPos(0, 1, 2), Blocks.STONE);
        helper.setBlock(new BlockPos(4, 1, 2), Blocks.STONE);
        helper.setBlock(hookAPos, Blocks.TRIPWIRE_HOOK.defaultBlockState().setValue(TripWireHookBlock.FACING, Direction.EAST));
        helper.setBlock(hookBPos, Blocks.TRIPWIRE_HOOK.defaultBlockState().setValue(TripWireHookBlock.FACING, Direction.WEST));
        helper.setBlock(stringPos, Blocks.TRIPWIRE);
        assertTrue(helper, helper.getBlockState(hookAPos).getValue(TripWireHookBlock.ATTACHED), "First hook is not attached to the string in setup");
        assertTrue(helper, helper.getBlockState(hookBPos).getValue(TripWireHookBlock.ATTACHED), "Second hook is not attached to the string in setup");

        execute(helper, touchSpell(ShearSpellPayload.INSTANCE, b -> {}), hitTop(helper, stringPos), caster);
        helper.assertBlockNotPresent(Blocks.TRIPWIRE, stringPos);

        // Neither hook may be powered at any point after the string is cut
        helper.onEachTick(() -> {
            assertFalse(helper, helper.getBlockState(hookAPos).getValue(TripWireHookBlock.POWERED), "First hook was powered by cutting the string");
            assertFalse(helper, helper.getBlockState(hookBPos).getValue(TripWireHookBlock.POWERED), "Second hook was powered by cutting the string");
        });
        helper.runAfterDelay(10L, helper::succeed);
    }

    // Polymorph tests

    public static void polymorph_turns_target_into_wolf(GameTestHelper helper) {
        var caster = makeCaster(helper);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(2.5D, 1.0D, 2.5D));
        execute(helper, durationSpell(PolymorphWolfSpellPayload.INSTANCE, 1), pig, caster);

        BlockPos pos = new BlockPos(2, 1, 2);
        helper.succeedWhen(() -> {
            assertValueEqual(helper, 1, helper.getEntities(EntityType.WOLF, pos, 2.0D).size(), "Wolves where the pig was");
            assertValueEqual(helper, 0, helper.getEntities(EntityType.PIG, pos, 2.0D).size(), "Pigs where the pig was");
        });
    }

    public static void polymorph_reverts_target_after_duration(GameTestHelper helper) {
        var caster = makeCaster(helper);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(2.5D, 1.0D, 2.5D));
        execute(helper, durationSpell(PolymorphWolfSpellPayload.INSTANCE, 1), pig, caster);

        // The polymorph lasts 6 seconds (120 ticks) per duration point; the target must first turn into a wolf, then back
        BlockPos pos = new BlockPos(2, 1, 2);
        MutableBoolean sawWolf = new MutableBoolean(false);
        helper.onEachTick(() -> {
            if (!helper.getEntities(EntityType.WOLF, pos, 2.0D).isEmpty()) {
                sawWolf.setTrue();
            }
        });
        helper.succeedWhen(() -> {
            assertTrue(helper, sawWolf.booleanValue(), "Target never turned into a wolf");
            assertValueEqual(helper, 0, helper.getEntities(EntityType.WOLF, pos, 2.0D).size(), "Wolves after the polymorph expired");
            assertValueEqual(helper, 1, helper.getEntities(EntityType.PIG, pos, 2.0D).size(), "Pigs after the polymorph expired");
        });
    }

    public static void polymorph_is_refused_for_wolves_players_and_bosses(GameTestHelper helper) {
        var caster = makeCaster(helper);
        var wolf = helper.spawnWithNoFreeWill(EntityType.WOLF, new Vec3(0.5D, 1.0D, 0.5D));
        var wither = helper.spawnWithNoFreeWill(EntityType.WITHER, new Vec3(2.5D, 1.0D, 2.5D));
        var dragon = EntityType.ENDER_DRAGON.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        var control = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(1.5D, 1.0D, 1.5D));
        var otherPlayer = makeMockServerPlayer(helper);

        // A pig can be polymorphed, so the refusals below are not just a blanket failure
        assertTrue(helper, SpellManager.canPolymorph(control), "Pig cannot be polymorphed");
        assertFalse(helper, SpellManager.canPolymorph(wolf), "Wolf can be polymorphed");
        assertFalse(helper, SpellManager.canPolymorph(otherPlayer), "Player can be polymorphed");
        assertFalse(helper, SpellManager.canPolymorph(wither), "Wither can be polymorphed");
        assertFalse(helper, SpellManager.canPolymorph(dragon), "Ender dragon can be polymorphed");

        // Casting the spell on a refused target must not queue a swap for it
        var spell = durationSpell(PolymorphWolfSpellPayload.INSTANCE, 1);
        execute(helper, spell, wolf, caster);
        execute(helper, spell, wither, caster);
        execute(helper, spell, otherPlayer, caster);
        execute(helper, spell, control, caster);
        for (Entity refused : List.of(wolf, wither, otherPlayer)) {
            var queue = EntitySwapper.getSwapperQueue(refused);
            assertTrue(helper, queue == null || queue.isEmpty(), "Swap queued for refused target " + refused.getType());
        }
        var controlQueue = EntitySwapper.getSwapperQueue(control);
        assertTrue(helper, controlQueue != null && !controlQueue.isEmpty(), "No swap queued for the control pig");
        helper.succeed();
    }

    // Conjure animal tests

    private static final Set<EntityType<?>> LAND_ANIMALS = Set.of(EntityType.ARMADILLO, EntityType.BAT, EntityType.CAT, EntityType.CHICKEN, EntityType.COW,
            EntityType.DONKEY, EntityType.FOX, EntityType.GOAT, EntityType.HORSE, EntityType.MOOSHROOM, EntityType.OCELOT, EntityType.PARROT, EntityType.PIG,
            EntityType.RABBIT, EntityType.SHEEP, EntityType.TURTLE);
    private static final Set<EntityType<?>> WATER_ANIMALS = Set.of(EntityType.AXOLOTL, EntityType.COD, EntityType.GLOW_SQUID, EntityType.PUFFERFISH,
            EntityType.SALMON, EntityType.SQUID, EntityType.TROPICAL_FISH, EntityType.TURTLE);
    private static final int CONJURE_ANIMAL_CASTS = 12;

    protected static List<Mob> mobsNear(GameTestHelper helper, BlockPos relativePos) {
        AABB area = new AABB(helper.absolutePos(relativePos)).inflate(2.0D);
        return helper.getLevel().getEntitiesOfClass(Mob.class, area);
    }

    public static void conjure_animal_summons_land_animals_above_water(GameTestHelper helper) {
        var caster = makeCaster(helper);
        BlockPos floorPos = new BlockPos(1, 0, 1);
        helper.setBlock(floorPos, Blocks.OAK_PLANKS);
        var spell = touchSpell(ConjureAnimalSpellPayload.INSTANCE, b -> {});
        for (int i = 0; i < CONJURE_ANIMAL_CASTS; i++) {
            execute(helper, spell, hitTop(helper, floorPos), caster);
        }

        // Every cast onto dry land summons exactly one animal from the land animal list
        var mobs = mobsNear(helper, floorPos.above());
        assertValueEqual(helper, CONJURE_ANIMAL_CASTS, mobs.size(), "Number of animals summoned");
        for (Mob mob : mobs) {
            assertTrue(helper, LAND_ANIMALS.contains(mob.getType()), "Summoned a non-land animal on dry land: " + mob.getType());
        }
        helper.succeed();
    }

    public static void conjure_animal_summons_water_animals_below_water(GameTestHelper helper) {
        var caster = makeCaster(helper);
        BlockPos floorPos = new BlockPos(1, 0, 1);
        helper.setBlock(floorPos, Blocks.OAK_PLANKS);
        helper.setBlock(floorPos.above(), Blocks.WATER);
        var spell = touchSpell(ConjureAnimalSpellPayload.INSTANCE, b -> {});
        for (int i = 0; i < CONJURE_ANIMAL_CASTS; i++) {
            execute(helper, spell, hitTop(helper, floorPos), caster);
        }

        // Every cast into water summons exactly one animal from the water animal list
        var mobs = mobsNear(helper, floorPos.above());
        assertValueEqual(helper, CONJURE_ANIMAL_CASTS, mobs.size(), "Number of animals summoned");
        for (Mob mob : mobs) {
            assertTrue(helper, WATER_ANIMALS.contains(mob.getType()), "Summoned a non-water animal in water: " + mob.getType());
        }
        helper.succeed();
    }

    public static void conjure_animal_fails_when_combined_with_burst(GameTestHelper helper) {
        var caster = makeCaster(helper);
        BlockPos floorPos = new BlockPos(1, 0, 1);
        helper.setBlock(floorPos, Blocks.OAK_PLANKS);
        var burstSpell = SpellPackageTests.spellWithVehicle(TouchSpellVehicle.INSTANCE)
                .payload().type(ConjureAnimalSpellPayload.INSTANCE).end()
                .primaryMod().type(BurstSpellMod.INSTANCE).with(SpellPropertiesPM.RADIUS.get(), 1).with(SpellPropertiesPM.BURST_POWER.get(), 0).end()
                .build();
        SpellManager.executeSpellPayload(burstSpell, hitTop(helper, floorPos), helper.getLevel(), caster, scrollSource(), true, null);
        assertValueEqual(helper, 0, mobsNear(helper, floorPos.above()).size(), "Animals summoned by a burst conjure animal spell");

        // The same payload without the burst mod does summon an animal
        SpellManager.executeSpellPayload(touchSpell(ConjureAnimalSpellPayload.INSTANCE, b -> {}), hitTop(helper, floorPos), helper.getLevel(), caster, scrollSource(), true, null);
        assertValueEqual(helper, 1, mobsNear(helper, floorPos.above()).size(), "Animals summoned by a plain conjure animal spell");
        helper.succeed();
    }

    // Consecration field tests

    private static BlockPos placeConsecrationField(GameTestHelper helper, ServerPlayer caster) {
        BlockPos floorPos = new BlockPos(1, 0, 1);
        helper.setBlock(floorPos, Blocks.OAK_PLANKS);
        execute(helper, touchSpell(ConsecrateSpellPayload.INSTANCE, b -> {}), hitTop(helper, floorPos), caster);
        helper.assertBlockPresent(BlocksPM.CONSECRATION_FIELD.get(), floorPos.above());
        return floorPos.above();
    }

    public static void consecration_field_cannot_be_entered_by_non_player_mobs(GameTestHelper helper) {
        var caster = makeCaster(helper, new Vec3(0.5D, 1.0D, 0.5D), 0.0F, 0.0F, false);
        BlockPos fieldPos = placeConsecrationField(helper, caster);
        BlockState fieldState = helper.getBlockState(fieldPos);
        BlockPos absFieldPos = helper.absolutePos(fieldPos);

        // The field has a collision box for living non-player entities only
        var zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new Vec3(0.5D, 1.0D, 1.5D));
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(0.5D, 1.0D, 2.5D));
        var item = new ItemEntity(helper.getLevel(), 0.5D, 1.0D, 0.5D, new ItemStack(Items.STICK));
        assertFalse(helper, fieldState.getCollisionShape(helper.getLevel(), absFieldPos, CollisionContext.of(zombie)).isEmpty(), "Field does not collide with a zombie");
        assertFalse(helper, fieldState.getCollisionShape(helper.getLevel(), absFieldPos, CollisionContext.of(pig)).isEmpty(), "Field does not collide with a pig");
        assertTrue(helper, fieldState.getCollisionShape(helper.getLevel(), absFieldPos, CollisionContext.of(caster)).isEmpty(), "Field collides with a player");
        assertTrue(helper, fieldState.getCollisionShape(helper.getLevel(), absFieldPos, CollisionContext.of(item)).isEmpty(), "Field collides with an item");

        // A zombie pushed a full block toward the field is stopped at its edge (x = 1.0, so the zombie's center stops at 0.7 given its 0.6 width)
        zombie.move(MoverType.SELF, new Vec3(1.0D, 0.0D, 0.0D));
        double zombieX = helper.relativeVec(zombie.position()).x;
        assertTrue(helper, zombieX < 0.71D, "Zombie walked into the consecration field; now at relative x " + zombieX);

        // A player moving the same way walks straight in, ending a full block further along
        var player = makeCaster(helper, new Vec3(0.5D, 1.0D, 1.5D), 0.0F, 0.0F, false);
        player.move(MoverType.SELF, new Vec3(1.0D, 0.0D, 0.0D));
        double playerX = helper.relativeVec(player.position()).x;
        assertTrue(helper, playerX > 1.4D, "Player was stopped by the consecration field; now at relative x " + playerX);
        helper.succeed();
    }

    public static void consecration_field_grants_regeneration_and_saturation_to_players(GameTestHelper helper) {
        var caster = makeCaster(helper, new Vec3(0.5D, 1.0D, 0.5D), 0.0F, 0.0F, false);
        BlockPos fieldPos = placeConsecrationField(helper, caster);
        BlockState fieldState = helper.getBlockState(fieldPos);
        BlockPos absFieldPos = helper.absolutePos(fieldPos);
        var zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new Vec3(1.5D, 1.0D, 1.5D));

        // The effects are given every fifth tick of the player's life, and a fresh player is at tick 0
        assertValueEqual(helper, 0, caster.tickCount, "Player tick count");
        assertFalse(helper, caster.hasEffect(MobEffects.REGENERATION), "Player has regeneration before entering the field");
        fieldState.entityInside(helper.getLevel(), absFieldPos, caster, InsideBlockEffectApplier.NOOP, true);
        fieldState.entityInside(helper.getLevel(), absFieldPos, zombie, InsideBlockEffectApplier.NOOP, true);

        // Both effects last 110 ticks at amplifier 0, and only players get them
        assertEffect(helper, caster, MobEffects.REGENERATION, 0, 110, "Player in consecration field");
        assertEffect(helper, caster, MobEffects.SATURATION, 0, 110, "Player in consecration field");
        assertFalse(helper, zombie.hasEffect(MobEffects.REGENERATION), "Zombie in the field has regeneration");
        helper.succeed();
    }

    // Teleport tests

    public static void teleport_moves_caster_to_target_point(GameTestHelper helper) {
        var caster = makeCaster(helper, new Vec3(0.5D, 1.0D, 0.5D), 0.0F, 0.0F, true);
        Vec3 destination = helper.absoluteVec(new Vec3(2.5D, 1.0D, 2.5D));
        var hit = new BlockHitResult(destination, Direction.UP, BlockPos.containing(destination), false);
        execute(helper, touchSpell(TeleportSpellPayload.INSTANCE, b -> {}), hit, caster);

        double distance = caster.position().distanceTo(destination);
        assertTrue(helper, distance < 0.01D, "Caster is " + distance + " blocks from the target point at " + caster.position());
        helper.succeed();
    }

    public static void teleport_fails_when_combined_with_burst(GameTestHelper helper) {
        var caster = makeCaster(helper, new Vec3(0.5D, 1.0D, 0.5D), 0.0F, 0.0F, true);
        Vec3 start = caster.position();
        Vec3 destination = helper.absoluteVec(new Vec3(2.5D, 1.0D, 2.5D));
        var hit = new BlockHitResult(destination, Direction.UP, BlockPos.containing(destination), false);
        var spell = SpellPackageTests.spellWithVehicle(TouchSpellVehicle.INSTANCE)
                .payload().type(TeleportSpellPayload.INSTANCE).end()
                .primaryMod().type(BurstSpellMod.INSTANCE).with(SpellPropertiesPM.RADIUS.get(), 1).with(SpellPropertiesPM.BURST_POWER.get(), 0).end()
                .build();
        SpellManager.executeSpellPayload(spell, hit, helper.getLevel(), caster, scrollSource(), true, null);

        assertValueEqual(helper, start, caster.position(), "Caster position after burst teleport");
        helper.succeed();
    }
}
