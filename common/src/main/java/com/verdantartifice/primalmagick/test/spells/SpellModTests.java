package com.verdantartifice.primalmagick.test.spells;

import com.verdantartifice.primalmagick.common.entities.EntityTypesPM;
import com.verdantartifice.primalmagick.common.spells.SpellManager;
import com.verdantartifice.primalmagick.common.spells.SpellPackage;
import com.verdantartifice.primalmagick.common.spells.SpellPropertiesPM;
import com.verdantartifice.primalmagick.common.spells.mods.AmplifySpellMod;
import com.verdantartifice.primalmagick.common.spells.mods.MineSpellMod;
import com.verdantartifice.primalmagick.common.spells.mods.SpellModsPM;
import com.verdantartifice.primalmagick.common.spells.payloads.BloodDamageSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.FlameDamageSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.FrostDamageSpellPayload;
import com.verdantartifice.primalmagick.common.spells.vehicles.TouchSpellVehicle;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Tests of spell mods that modify how a spell's payload is applied (amplify and mine; fork and burst are covered with
 * the vehicles).
 */
public class SpellModTests extends AbstractBaseTest {
    // Amplify tests

    public static void amplify_mod_increases_power_and_duration(GameTestHelper helper) {
        var caster = SpellPayloadTests.makeCaster(helper);
        var registries = helper.getLevel().registryAccess();
        var spell = SpellPackageTests.spellWithVehicle(TouchSpellVehicle.INSTANCE)
                .payload().type(FlameDamageSpellPayload.INSTANCE).with(SpellPropertiesPM.POWER.get(), 1).with(SpellPropertiesPM.DURATION.get(), 1).end()
                .primaryMod().type(AmplifySpellMod.INSTANCE).with(SpellPropertiesPM.AMPLIFY_POWER.get(), 2).end()
                .build();

        // Amplify power 2 adds 2 to both of the payload's power 1 and duration 1
        var payload = spell.payload().getComponent();
        assertValueEqual(helper, 3, payload.getModdedPropertyValue(SpellPropertiesPM.POWER.get(), spell, ItemStack.EMPTY, null, registries), "Amplified power");
        assertValueEqual(helper, 3, payload.getModdedPropertyValue(SpellPropertiesPM.DURATION.get(), spell, ItemStack.EMPTY, null, registries), "Amplified duration");

        // The effects of the spell follow: damage of 4 + 3 * 3 = 13 and burning for 2 seconds per duration point, so 6 seconds (120 ticks)
        var pig = SpellPayloadTests.spawnTanky(helper, EntityType.PIG, new Vec3(0.5D, 1.0D, 0.5D));
        SpellPayloadTests.execute(helper, spell, pig, caster);
        assertTrue(helper, Math.abs(pig.getHealth() - 87.0F) < 0.01F, "Amplified spell did not deal the expected damage; health is " + pig.getHealth());
        assertValueEqual(helper, 120, pig.getRemainingFireTicks(), "Fire ticks after amplified spell");
        helper.succeed();
    }

    public static void amplify_mod_does_not_add_duration_to_zero_duration(GameTestHelper helper) {
        var caster = SpellPayloadTests.makeCaster(helper);
        var registries = helper.getLevel().registryAccess();
        var spell = SpellPackageTests.spellWithVehicle(TouchSpellVehicle.INSTANCE)
                .payload().type(FrostDamageSpellPayload.INSTANCE).with(SpellPropertiesPM.POWER.get(), 1).with(SpellPropertiesPM.DURATION.get(), 0).end()
                .primaryMod().type(AmplifySpellMod.INSTANCE).with(SpellPropertiesPM.AMPLIFY_POWER.get(), 3).end()
                .build();

        // Only positive values are amplified, so a duration of 0 stays 0 while power 1 becomes 4
        var payload = spell.payload().getComponent();
        assertValueEqual(helper, 4, payload.getModdedPropertyValue(SpellPropertiesPM.POWER.get(), spell, ItemStack.EMPTY, null, registries), "Amplified power");
        assertValueEqual(helper, 0, payload.getModdedPropertyValue(SpellPropertiesPM.DURATION.get(), spell, ItemStack.EMPTY, null, registries), "Amplified zero duration");

        var pig = SpellPayloadTests.spawnTanky(helper, EntityType.PIG, new Vec3(0.5D, 1.0D, 0.5D));
        SpellPayloadTests.execute(helper, spell, pig, caster);
        assertFalse(helper, pig.hasEffect(MobEffects.SLOWNESS), "Zero duration frost spell applied slowness after amplification");
        helper.succeed();
    }

    public static void amplify_mod_does_not_amplify_itself_or_other_amplify_mods(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        var spell = SpellPackageTests.spellWithVehicle(TouchSpellVehicle.INSTANCE)
                .payload().type(BloodDamageSpellPayload.INSTANCE).with(SpellPropertiesPM.POWER.get(), 1).end()
                .primaryMod().type(AmplifySpellMod.INSTANCE).with(SpellPropertiesPM.AMPLIFY_POWER.get(), 2).end()
                .secondaryMod().type(AmplifySpellMod.INSTANCE).with(SpellPropertiesPM.AMPLIFY_POWER.get(), 3).end()
                .build();

        // When a spell has two Amplify mods, only the stronger one (3) applies; it neither stacks with the weaker one nor amplifies itself
        var ampMod = spell.getMod(SpellModsPM.AMPLIFY.get()).orElseThrow();
        assertValueEqual(helper, 3, ampMod.getPropertyValue(SpellPropertiesPM.AMPLIFY_POWER.get()), "Strongest amplify mod power");
        assertValueEqual(helper, 3, ampMod.getComponent().getModdedPropertyValue(SpellPropertiesPM.AMPLIFY_POWER.get(), spell, ItemStack.EMPTY, null, registries), "Amplify power after amplification");

        // The payload's power of 1 is raised by exactly 3 rather than by 2 + 3 or by an amplified 3 + 3
        assertValueEqual(helper, 4, spell.payload().getComponent().getModdedPropertyValue(SpellPropertiesPM.POWER.get(), spell, ItemStack.EMPTY, null, registries), "Amplified payload power");
        helper.succeed();
    }

    // Mine tests

    public static void mine_mod_creates_mine_that_triggers_once_on_first_creature(GameTestHelper helper) {
        var caster = SpellPayloadTests.makeCaster(helper, new Vec3(0.5D, 1.0D, 4.5D), 0.0F, 0.0F, true);
        Vec3 minePoint = helper.absoluteVec(new Vec3(2.5D, 1.0D, 2.5D));
        var spell = SpellPackageTests.spellWithVehicle(TouchSpellVehicle.INSTANCE)
                .payload().type(BloodDamageSpellPayload.INSTANCE).with(SpellPropertiesPM.POWER.get(), 1).end()
                .primaryMod().type(MineSpellMod.INSTANCE).with(SpellPropertiesPM.NON_ZERO_DURATION.get(), 1).end()
                .build();
        var triggerPig = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(2.5D, 1.0D, 2.5D));
        var latePig = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(0.5D, 1.0D, 0.5D));

        // Casting the spell at the floor creates a mine rather than damaging anything
        var hit = new BlockHitResult(minePoint, Direction.UP, BlockPos.containing(minePoint), false);
        SpellManager.executeSpellPayload(spell, hit, helper.getLevel(), caster, SpellPayloadTests.scrollSource(), true, null);
        BlockPos minePos = new BlockPos(2, 1, 2);
        assertValueEqual(helper, 1, helper.getEntities(EntityTypesPM.SPELL_MINE.get(), minePos, 1.0D).size(), "Number of mines after cast");
        assertValueEqual(helper, 10.0F, triggerPig.getHealth(), "Health of pig on the mine right after cast");

        // A mine takes 60 ticks to arm, so a creature standing on it at 30 ticks is not affected
        helper.runAfterDelay(30L, () -> {
            assertValueEqual(helper, 1, helper.getEntities(EntityTypesPM.SPELL_MINE.get(), minePos, 1.0D).size(), "Number of mines before arming");
            assertValueEqual(helper, 10.0F, triggerPig.getHealth(), "Health of pig on the mine before it armed");
        });

        // By 80 ticks the armed mine has triggered on the pig (blood damage 3 + 2 * 1 = 5) and removed itself
        helper.runAfterDelay(80L, () -> {
            assertValueEqual(helper, 0, helper.getEntities(EntityTypesPM.SPELL_MINE.get(), minePos, 1.0D).size(), "Number of mines after triggering");
            assertValueEqual(helper, 5.0F, triggerPig.getHealth(), "Health of pig on the mine after it triggered");

            // A creature that arrives afterwards finds nothing, since the mine was used up
            latePig.setPos(minePoint);
            helper.runAfterDelay(20L, () -> {
                assertValueEqual(helper, 10.0F, latePig.getHealth(), "Health of pig that arrived after the mine triggered");
                helper.succeed();
            });
        });
    }
}
