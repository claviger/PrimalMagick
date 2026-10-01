package com.verdantartifice.primalmagick.test.spells;

import com.verdantartifice.primalmagick.common.sources.Source;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.common.spells.SpellPackage;
import com.verdantartifice.primalmagick.common.spells.SpellPropertiesPM;
import com.verdantartifice.primalmagick.common.spells.SpellProperty;
import com.verdantartifice.primalmagick.common.spells.mods.AmplifySpellMod;
import com.verdantartifice.primalmagick.common.spells.mods.BurstSpellMod;
import com.verdantartifice.primalmagick.common.spells.mods.EmptySpellMod;
import com.verdantartifice.primalmagick.common.spells.mods.ForkSpellMod;
import com.verdantartifice.primalmagick.common.spells.mods.MineSpellMod;
import com.verdantartifice.primalmagick.common.spells.mods.QuickenSpellMod;
import com.verdantartifice.primalmagick.common.spells.payloads.AbstractSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.BreakSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.EarthDamageSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.EmptySpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.FlameDamageSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.SpellPayloadType;
import com.verdantartifice.primalmagick.common.spells.vehicles.AbstractSpellVehicle;
import com.verdantartifice.primalmagick.common.spells.vehicles.BoltSpellVehicle;
import com.verdantartifice.primalmagick.common.spells.vehicles.ConfiguredSpellVehicle;
import com.verdantartifice.primalmagick.common.spells.vehicles.EmptySpellVehicle;
import com.verdantartifice.primalmagick.common.spells.vehicles.ProjectileSpellVehicle;
import com.verdantartifice.primalmagick.common.spells.vehicles.SelfSpellVehicle;
import com.verdantartifice.primalmagick.common.spells.vehicles.TouchSpellVehicle;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Optional;
import java.util.function.Consumer;

public class SpellPackageTests extends AbstractBaseTest {
    public static final String TEST_SPELL_NAME = "Test Spell";

    /**
     * Creates a named spell builder with the given vehicle, leaving the payload and mods to the caller.
     */
    protected static SpellPackage.Builder spellWithVehicle(AbstractSpellVehicle<?> vehicle) {
        return spellWithVehicle(vehicle, vehicleBuilder -> {});
    }

    /**
     * Creates a named spell builder with the given vehicle, letting the caller configure the vehicle's properties.
     */
    protected static SpellPackage.Builder spellWithVehicle(AbstractSpellVehicle<?> vehicle, Consumer<ConfiguredSpellVehicle.Builder> vehicleConfigurer) {
        var vehicleBuilder = SpellPackage.builder().name(TEST_SPELL_NAME).vehicle().type(vehicle);
        vehicleConfigurer.accept(vehicleBuilder);
        return vehicleBuilder.end();
    }

    /**
     * Creates a Touch spell carrying the given payload, with every payload property at its minimum valid value.
     */
    public static SpellPackage touchSpell(AbstractSpellPayload<?> payload) {
        var payloadBuilder = spellWithVehicle(TouchSpellVehicle.INSTANCE).payload().type(payload);
        for (SpellProperty property : payload.getProperties()) {
            payloadBuilder.with(property, property.min());
        }
        return payloadBuilder.end().build();
    }

    /**
     * Creates a spell builder with a Touch vehicle and an Earth damage payload at power 1. Touch adds nothing to
     * the base cost and multiplies it by 1, and the payload's base cost at power 1 is 1 mana, so a spell from this
     * builder with no mods costs exactly 100 centimana of Earth.
     */
    protected static SpellPackage.Builder baselineEarthSpell() {
        return spellWithVehicle(TouchSpellVehicle.INSTANCE)
                .payload().type(EarthDamageSpellPayload.INSTANCE).with(SpellPropertiesPM.POWER.get(), 1).end();
    }

    /**
     * Asserts that the spell's mana cost consists of exactly one source with exactly the given amount of centimana.
     */
    protected static void assertSingleSourceCost(GameTestHelper helper, SpellPackage spell, Source expectedSource, int expectedCentimana) {
        var cost = spell.getManaCost();
        assertValueEqual(helper, 1, cost.getSize(), "Number of sources in spell mana cost");
        assertTrue(helper, cost.isPresent(expectedSource), "Spell mana cost does not contain source " + expectedSource.getId() + "; actual cost is " + cost);
        assertValueEqual(helper, expectedCentimana, cost.getAmount(expectedSource), "Spell mana cost amount for " + expectedSource.getId());
    }

    /**
     * Confirms that a spell carrying the given payload is charged only in the expected source, at the payload's base cost.
     */
    public static void spell_mana_cost_isolated_to_payload_source(GameTestHelper helper, SpellPayloadType<?> payloadType, Source expectedSource) {
        var payloadInstance = payloadType.instanceSupplier().get();
        assertValueEqual(helper, expectedSource, payloadInstance.getSource(), "Payload source");

        // Configure every property of the payload at its minimum valid value
        var spell = touchSpell(payloadInstance);

        // Touch is a no-op vehicle, so the cost should be exactly the payload's base cost converted to centimana
        int baseManaCost = spell.payload().getBaseManaCost();
        assertTrue(helper, baseManaCost > 0, "Payload base mana cost is not positive: " + baseManaCost);
        assertSingleSourceCost(helper, spell, expectedSource, 100 * baseManaCost);

        helper.succeed();
    }

    public static void spell_mana_cost_earth_damage_power_1(GameTestHelper helper) {
        // EarthDamageSpellPayload base cost is (1 << (power - 1)) + ((1 << (power - 1)) >> 1) = 1 + 0 = 1 mana at power 1
        assertSingleSourceCost(helper, baselineEarthSpell().build(), Sources.EARTH, 100);
        helper.succeed();
    }

    public static void spell_mana_cost_earth_damage_power_5(GameTestHelper helper) {
        // EarthDamageSpellPayload base cost is (1 << 4) + ((1 << 4) >> 1) = 16 + 8 = 24 mana at power 5
        var spell = spellWithVehicle(TouchSpellVehicle.INSTANCE)
                .payload().type(EarthDamageSpellPayload.INSTANCE).with(SpellPropertiesPM.POWER.get(), 5).end()
                .build();
        assertSingleSourceCost(helper, spell, Sources.EARTH, 2400);
        helper.succeed();
    }

    public static void spell_mana_cost_flame_damage_power_3_duration_2(GameTestHelper helper) {
        // FlameDamageSpellPayload base cost is (1 << (power - 1)) + ((1 << (duration - 1)) >> 1) = 4 + 1 = 5 mana
        var spell = spellWithVehicle(TouchSpellVehicle.INSTANCE)
                .payload().type(FlameDamageSpellPayload.INSTANCE).with(SpellPropertiesPM.POWER.get(), 3).with(SpellPropertiesPM.DURATION.get(), 2).end()
                .build();
        assertSingleSourceCost(helper, spell, Sources.INFERNAL, 500);
        helper.succeed();
    }

    public static void spell_mana_cost_break_power_2_silk_touch(GameTestHelper helper) {
        // BreakSpellPayload base cost is power + (5 * silk_touch) = 2 + 5 = 7 mana
        var spell = spellWithVehicle(TouchSpellVehicle.INSTANCE)
                .payload().type(BreakSpellPayload.INSTANCE).with(SpellPropertiesPM.POWER.get(), 2).with(SpellPropertiesPM.SILK_TOUCH.get(), 1).end()
                .build();
        assertSingleSourceCost(helper, spell, Sources.EARTH, 700);
        helper.succeed();
    }

    public static void spell_mana_cost_vehicle_self(GameTestHelper helper) {
        // SelfSpellVehicle uses the defaults from AbstractSpellVehicle: base modifier 0, multiplier 1
        // Expected: (100 + 100 * 0) * 1 = 100
        var spell = spellWithVehicle(SelfSpellVehicle.INSTANCE)
                .payload().type(EarthDamageSpellPayload.INSTANCE).with(SpellPropertiesPM.POWER.get(), 1).end()
                .build();
        assertSingleSourceCost(helper, spell, Sources.EARTH, 100);
        helper.succeed();
    }

    public static void spell_mana_cost_vehicle_bolt(GameTestHelper helper) {
        // BoltSpellVehicle base modifier is its range property (3 here), multiplier is the default of 1
        // Expected: (100 + 100 * 3) * 1 = 400
        var spell = spellWithVehicle(BoltSpellVehicle.INSTANCE, v -> v.with(SpellPropertiesPM.RANGE.get(), 3))
                .payload().type(EarthDamageSpellPayload.INSTANCE).with(SpellPropertiesPM.POWER.get(), 1).end()
                .build();
        assertSingleSourceCost(helper, spell, Sources.EARTH, 400);
        helper.succeed();
    }

    public static void spell_mana_cost_vehicle_projectile(GameTestHelper helper) {
        // ProjectileSpellVehicle base modifier is 0, multiplier is 2
        // Expected: (100 + 100 * 0) * 2 = 200
        var spell = spellWithVehicle(ProjectileSpellVehicle.INSTANCE)
                .payload().type(EarthDamageSpellPayload.INSTANCE).with(SpellPropertiesPM.POWER.get(), 1).end()
                .build();
        assertSingleSourceCost(helper, spell, Sources.EARTH, 200);
        helper.succeed();
    }

    public static void spell_mana_cost_mod_amplify(GameTestHelper helper) {
        // AmplifySpellMod base modifier is 0, multiplier is 1 + amplify_power = 3
        // Expected: (100 + 0) * 3 = 300
        var spell = baselineEarthSpell().primaryMod().type(AmplifySpellMod.INSTANCE).with(SpellPropertiesPM.AMPLIFY_POWER.get(), 2).end().build();
        assertSingleSourceCost(helper, spell, Sources.EARTH, 300);
        helper.succeed();
    }

    public static void spell_mana_cost_mod_burst(GameTestHelper helper) {
        // BurstSpellMod base modifier is burst_power = 3, multiplier is 1 + radius^2 = 1 + 4 = 5
        // Expected: (100 + 100 * 3) * 5 = 2000
        var spell = baselineEarthSpell().primaryMod().type(BurstSpellMod.INSTANCE).with(SpellPropertiesPM.RADIUS.get(), 2).with(SpellPropertiesPM.BURST_POWER.get(), 3).end().build();
        assertSingleSourceCost(helper, spell, Sources.EARTH, 2000);
        helper.succeed();
    }

    public static void spell_mana_cost_mod_fork(GameTestHelper helper) {
        // ForkSpellMod base modifier is 0, multiplier is 1 + forks^2 + precision^2 = 1 + 4 + 1 = 6
        // Expected: (100 + 0) * 6 = 600
        var spell = baselineEarthSpell().primaryMod().type(ForkSpellMod.INSTANCE).with(SpellPropertiesPM.FORKS.get(), 2).with(SpellPropertiesPM.PRECISION.get(), 1).end().build();
        assertSingleSourceCost(helper, spell, Sources.EARTH, 600);
        helper.succeed();
    }

    public static void spell_mana_cost_mod_mine(GameTestHelper helper) {
        // MineSpellMod base modifier is its duration = 3, multiplier is 1
        // Expected: (100 + 100 * 3) * 1 = 400
        var spell = baselineEarthSpell().primaryMod().type(MineSpellMod.INSTANCE).with(SpellPropertiesPM.NON_ZERO_DURATION.get(), 3).end().build();
        assertSingleSourceCost(helper, spell, Sources.EARTH, 400);
        helper.succeed();
    }

    public static void spell_mana_cost_mod_quicken(GameTestHelper helper) {
        // QuickenSpellMod base modifier is 0, multiplier is 1 + haste = 3
        // Expected: (100 + 0) * 3 = 300
        var spell = baselineEarthSpell().primaryMod().type(QuickenSpellMod.INSTANCE).with(SpellPropertiesPM.HASTE.get(), 2).end().build();
        assertSingleSourceCost(helper, spell, Sources.EARTH, 300);
        helper.succeed();
    }

    public static void spell_mana_cost_mod_order_independent(GameTestHelper helper) {
        // Amplify (power 2) contributes x3; Burst (radius 2, power 3) contributes +300 and x5
        // Expected either way: (100 + 300) * 3 * 5 = 6000
        var ampFirst = baselineEarthSpell()
                .primaryMod().type(AmplifySpellMod.INSTANCE).with(SpellPropertiesPM.AMPLIFY_POWER.get(), 2).end()
                .secondaryMod().type(BurstSpellMod.INSTANCE).with(SpellPropertiesPM.RADIUS.get(), 2).with(SpellPropertiesPM.BURST_POWER.get(), 3).end()
                .build();
        var burstFirst = baselineEarthSpell()
                .primaryMod().type(BurstSpellMod.INSTANCE).with(SpellPropertiesPM.RADIUS.get(), 2).with(SpellPropertiesPM.BURST_POWER.get(), 3).end()
                .secondaryMod().type(AmplifySpellMod.INSTANCE).with(SpellPropertiesPM.AMPLIFY_POWER.get(), 2).end()
                .build();
        assertSingleSourceCost(helper, ampFirst, Sources.EARTH, 6000);
        assertSingleSourceCost(helper, burstFirst, Sources.EARTH, 6000);
        helper.succeed();
    }

    public static void spell_cooldown_base(GameTestHelper helper) {
        // SpellPackage.BASE_COOLDOWN_TICKS is 30
        assertValueEqual(helper, 30, baselineEarthSpell().build().getCooldownTicks(), "Base spell cooldown");
        helper.succeed();
    }

    public static void spell_cooldown_quicken(GameTestHelper helper, int haste, int expectedTicks) {
        // Cooldown is 30 - (5 * haste), clamped to [0, 30]. Haste 6 is the boundary (exactly 0) and haste 7 exercises
        // the clamp. Both exceed HASTE's max of 5 and rely on the builder not validating property values.
        var spell = baselineEarthSpell().primaryMod().type(QuickenSpellMod.INSTANCE).with(SpellPropertiesPM.HASTE.get(), haste).end().build();
        assertValueEqual(helper, expectedTicks, spell.getCooldownTicks(), "Spell cooldown with haste " + haste);
        helper.succeed();
    }

    public static void spell_package_nbt_roundtrip(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        var original = spellWithVehicle(BoltSpellVehicle.INSTANCE, v -> v.with(SpellPropertiesPM.RANGE.get(), 4))
                .payload().type(FlameDamageSpellPayload.INSTANCE).with(SpellPropertiesPM.POWER.get(), 3).with(SpellPropertiesPM.DURATION.get(), 2).end()
                .primaryMod().type(BurstSpellMod.INSTANCE).with(SpellPropertiesPM.RADIUS.get(), 2).with(SpellPropertiesPM.BURST_POWER.get(), 1).end()
                .secondaryMod().type(QuickenSpellMod.INSTANCE).with(SpellPropertiesPM.HASTE.get(), 3).end()
                .build();

        var tag = original.serializeNBT(registries);
        assertTrue(helper, tag != null, "Spell package failed to serialize");
        var deserialized = SpellPackage.deserializeNBT(tag, registries);
        assertTrue(helper, deserialized != null, "Spell package failed to deserialize");
        assertValueEqual(helper, original, deserialized, "Deserialized spell package");
        assertValueEqual(helper, original.getManaCost(), deserialized.getManaCost(), "Deserialized spell package mana cost");
        helper.succeed();
    }

    public static void spell_active_mod_count_0(GameTestHelper helper) {
        assertValueEqual(helper, 0, baselineEarthSpell().build().getActiveModCount(), "Active mod count");
        helper.succeed();
    }

    public static void spell_active_mod_count_1(GameTestHelper helper) {
        var spell = baselineEarthSpell().primaryMod().type(AmplifySpellMod.INSTANCE).with(SpellPropertiesPM.AMPLIFY_POWER.get(), 1).end().build();
        assertValueEqual(helper, 1, spell.getActiveModCount(), "Active mod count");
        helper.succeed();
    }

    public static void spell_active_mod_count_2(GameTestHelper helper) {
        var spell = baselineEarthSpell()
                .primaryMod().type(AmplifySpellMod.INSTANCE).with(SpellPropertiesPM.AMPLIFY_POWER.get(), 1).end()
                .secondaryMod().type(QuickenSpellMod.INSTANCE).with(SpellPropertiesPM.HASTE.get(), 1).end()
                .build();
        assertValueEqual(helper, 2, spell.getActiveModCount(), "Active mod count");
        helper.succeed();
    }

    public static void spell_active_mod_count_ignores_empty_mod(GameTestHelper helper) {
        // An empty mod occupies a slot but is not active
        var spell = baselineEarthSpell()
                .primaryMod().type(EmptySpellMod.INSTANCE).end()
                .secondaryMod().type(QuickenSpellMod.INSTANCE).with(SpellPropertiesPM.HASTE.get(), 1).end()
                .build();
        assertValueEqual(helper, 1, spell.getActiveModCount(), "Active mod count");
        helper.succeed();
    }

    public static void spell_is_valid_complete(GameTestHelper helper) {
        assertTrue(helper, baselineEarthSpell().build().isValid(), "Complete spell is not valid");
        helper.succeed();
    }

    public static void spell_is_invalid_empty_name(GameTestHelper helper) {
        var spell = baselineEarthSpell().name("").build();
        assertFalse(helper, spell.isValid(), "Spell with empty name is valid");
        helper.succeed();
    }

    public static void spell_is_invalid_empty_vehicle(GameTestHelper helper) {
        var spell = spellWithVehicle(EmptySpellVehicle.INSTANCE)
                .payload().type(EarthDamageSpellPayload.INSTANCE).with(SpellPropertiesPM.POWER.get(), 1).end()
                .build();
        assertFalse(helper, spell.isValid(), "Spell with empty vehicle is valid");
        helper.succeed();
    }

    public static void spell_is_invalid_empty_payload(GameTestHelper helper) {
        var spell = spellWithVehicle(TouchSpellVehicle.INSTANCE).payload().type(EmptySpellPayload.INSTANCE).end().build();
        assertFalse(helper, spell.isValid(), "Spell with empty payload is valid");
        helper.succeed();
    }

    // The null cases below cannot be produced by the builder, so they use the record constructor directly,
    // taking the other components from a known-valid spell so that only the nulled field differs.

    public static void spell_is_invalid_null_name(GameTestHelper helper) {
        var valid = baselineEarthSpell().build();
        assertTrue(helper, valid.isValid(), "Control spell is not valid");
        var spell = new SpellPackage(null, valid.vehicle(), valid.payload(), Optional.empty(), Optional.empty());
        assertFalse(helper, spell.isValid(), "Spell with null name is valid");
        helper.succeed();
    }

    public static void spell_is_invalid_null_vehicle(GameTestHelper helper) {
        var valid = baselineEarthSpell().build();
        assertTrue(helper, valid.isValid(), "Control spell is not valid");
        var spell = new SpellPackage(TEST_SPELL_NAME, null, valid.payload(), Optional.empty(), Optional.empty());
        assertFalse(helper, spell.isValid(), "Spell with null vehicle is valid");
        helper.succeed();
    }

    public static void spell_is_invalid_null_payload(GameTestHelper helper) {
        var valid = baselineEarthSpell().build();
        assertTrue(helper, valid.isValid(), "Control spell is not valid");
        var spell = new SpellPackage(TEST_SPELL_NAME, valid.vehicle(), null, Optional.empty(), Optional.empty());
        assertFalse(helper, spell.isValid(), "Spell with null payload is valid");
        helper.succeed();
    }
}
