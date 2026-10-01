package com.verdantartifice.primalmagick.test.items;

import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.items.wands.IHasWandComponents;
import com.verdantartifice.primalmagick.common.items.wands.ModularWandItem;
import com.verdantartifice.primalmagick.common.sources.Source;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.common.spells.SpellPackage;
import com.verdantartifice.primalmagick.common.spells.SpellProperty;
import com.verdantartifice.primalmagick.common.spells.payloads.AbstractSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.FlameDamageSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.HolyDamageSpellPayload;
import com.verdantartifice.primalmagick.common.spells.vehicles.TouchSpellVehicle;
import com.verdantartifice.primalmagick.common.wands.IWand;
import com.verdantartifice.primalmagick.common.wands.WandCap;
import com.verdantartifice.primalmagick.common.wands.WandCore;
import com.verdantartifice.primalmagick.common.wands.WandGem;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

/**
 * Tests that each wand component confers its expected properties on modular wands and staves. Unless a test varies
 * it, every wand is built from a baseline of WandCore.HEARTWOOD (no aligned sources, no bonus slot), WandCap.IRON
 * (10% cost modifier), and WandGem.APPRENTICE (7500 centimana). Tests that apply to both wands and staves take the
 * caster item as a parameter.
 */
public class WandComponentTests extends AbstractBaseTest {
    protected static final String TEST_SPELL_NAME = "Test Spell";

    // Upper bound on spells added while probing a wand's capacity, so that a broken canAddSpell can't loop forever
    protected static final int MAX_SPELL_PROBE = 32;

    protected static ItemStack makeStack(Item item, WandCore core, WandCap cap, WandGem gem) {
        return IHasWandComponents.setWandComponents(item.getDefaultInstance(), core, cap, gem);
    }

    protected static ItemStack makeWand(WandCore core, WandCap cap, WandGem gem) {
        return makeStack(ItemsPM.MODULAR_WAND.get(), core, cap, gem);
    }

    /**
     * Creates a Touch spell carrying the given payload, with every payload property at its minimum value. Spell slot
     * rules only look at the payload's source, so the vehicle and property values are irrelevant to these tests.
     */
    protected static SpellPackage touchSpell(AbstractSpellPayload<?> payload) {
        var payloadBuilder = SpellPackage.builder().name(TEST_SPELL_NAME).vehicle().type(TouchSpellVehicle.INSTANCE).end().payload().type(payload);
        for (SpellProperty property : payload.getProperties()) {
            payloadBuilder.with(property, property.min());
        }
        return payloadBuilder.end().build();
    }

    /**
     * Adds copies of the given spell to the wand until it stops accepting them, returning the number accepted.
     */
    protected static int fillWithSpell(GameTestHelper helper, IWand wand, ItemStack stack, SpellPackage spell) {
        int accepted = 0;
        while (accepted < MAX_SPELL_PROBE && wand.canAddSpell(stack, spell)) {
            assertTrue(helper, wand.addSpell(stack, spell), "addSpell failed after canAddSpell returned true");
            accepted++;
        }
        return accepted;
    }

    // Gem tests

    protected static void assertGemSetsMaxMana(GameTestHelper helper, ItemStack stack, int expectedCentimana) {
        IWand wand = assertInstanceOf(helper, stack.getItem(), IWand.class, "Stack is not a wand as expected");
        for (Source source : Sources.getAllSorted()) {
            assertValueEqual(helper, expectedCentimana, wand.getMaxMana(stack, source), "Max mana for " + source.getId());

            // Confirm the capacity is enforced, not just reported: one centimana over capacity overflows
            assertValueEqual(helper, 1, wand.addMana(stack, source, expectedCentimana + 1), "Overflow when overfilling " + source.getId());
            assertValueEqual(helper, expectedCentimana, wand.getMana(stack, source), "Mana after overfilling " + source.getId());
        }
    }

    public static void gem_sets_max_mana(GameTestHelper helper, Item caster, WandGem gem, int expectedCentimana) {
        assertGemSetsMaxMana(helper, makeStack(caster, WandCore.HEARTWOOD, WandCap.IRON, gem), expectedCentimana);
        helper.succeed();
    }

    public static void wand_gem_creative_has_infinite_mana(GameTestHelper helper) {
        // The creative gem has capacity -1 (IManaContainer.INFINITE_MANA). ModularWandItem.getMaxMana passes that
        // through, and the IManaContainer defaults treat it as a sentinel: getMana always reports -1, additions are
        // ignored with no overflow, and any amount is contained and consumed without being deducted.
        final int infinite = -1;
        final int hugeAmount = 1_000_000;
        final int consumedCentimana = 100;
        var player = makeMockServerPlayer(helper);
        var registries = helper.getLevel().registryAccess();
        var stack = makeWand(WandCore.HEARTWOOD, WandCap.IRON, WandGem.CREATIVE);
        IWand wand = assertInstanceOf(helper, stack.getItem(), IWand.class, "Stack is not a wand as expected");

        for (Source source : Sources.getAllSorted()) {
            assertValueEqual(helper, infinite, wand.getMaxMana(stack, source), "Max mana for " + source.getId());
            assertValueEqual(helper, infinite, wand.getMana(stack, source), "Starting mana for " + source.getId());

            // Adding mana is a no-op that reports no overflow
            assertValueEqual(helper, 0, wand.addMana(stack, source, hugeAmount), "Overflow when adding to " + source.getId());
            assertValueEqual(helper, infinite, wand.getMana(stack, source), "Mana after adding to " + source.getId());

            // Any amount is contained, with or without cost modifiers
            assertTrue(helper, wand.containsManaRaw(stack, source, hugeAmount), "Creative wand does not contain raw mana for " + source.getId());
            assertTrue(helper, wand.containsMana(stack, player, source, hugeAmount, registries), "Creative wand does not contain mana for " + source.getId());

            // Removing and deducting succeed without changing the reported amount
            assertTrue(helper, wand.removeManaRaw(stack, source, hugeAmount), "Raw removal failed for " + source.getId());
            assertValueEqual(helper, 0, wand.deductMana(stack, source, hugeAmount), "Leftover when deducting from " + source.getId());
            assertValueEqual(helper, infinite, wand.getMana(stack, source), "Mana after removal from " + source.getId());
        }

        // getAllMana reports the sentinel for every source
        var allMana = wand.getAllMana(stack);
        for (Source source : Sources.getAllSorted()) {
            assertValueEqual(helper, infinite, allMana.getAmount(source), "getAllMana amount for " + source.getId());
        }

        // Consumption succeeds and leaves the wand infinite. Kept last to avoid side effects on the mock player, since
        // consuming still records mana spent and grants temporary attunement.
        for (Source source : Sources.getAllSorted()) {
            assertTrue(helper, wand.consumeMana(stack, player, source, consumedCentimana, registries), "Consumption failed for " + source.getId());
            assertValueEqual(helper, infinite, wand.getMana(stack, source), "Mana after consuming " + source.getId());
        }

        helper.succeed();
    }

    // Cap tests

    protected static void assertCapSetsBaseCostModifier(GameTestHelper helper, ItemStack stack, int expectedModifier) {
        var player = makeMockServerPlayer(helper);
        IWand wand = assertInstanceOf(helper, stack.getItem(), IWand.class, "Stack is not a wand as expected");
        assertValueEqual(helper, expectedModifier, wand.getBaseCostModifier(stack), "Base cost modifier");

        // A fresh player has no gear, attunement, or effects, and the heartwood core has no aligned sources, so the
        // cap is the only contributor to the total modifier for every source
        for (Source source : Sources.getAllSorted()) {
            assertValueEqual(helper, expectedModifier, wand.getTotalCostModifier(stack, player, source, helper.getLevel().registryAccess()), "Total cost modifier for " + source.getId());
        }
    }

    public static void cap_sets_base_cost_modifier(GameTestHelper helper, Item caster, WandCap cap, int expectedModifier) {
        assertCapSetsBaseCostModifier(helper, makeStack(caster, WandCore.HEARTWOOD, cap, WandGem.APPRENTICE), expectedModifier);
        helper.succeed();
    }

    protected static void assertCapSetsSiphonAmount(GameTestHelper helper, ItemStack stack, int expectedSiphon) {
        IWand wand = assertInstanceOf(helper, stack.getItem(), IWand.class, "Stack is not a wand as expected");
        assertValueEqual(helper, expectedSiphon, wand.getSiphonAmount(stack), "Siphon amount");
    }

    public static void cap_sets_siphon_amount(GameTestHelper helper, Item caster, WandCap cap, int expectedSiphon) {
        assertCapSetsSiphonAmount(helper, makeStack(caster, WandCore.HEARTWOOD, cap, WandGem.APPRENTICE), expectedSiphon);
        helper.succeed();
    }

    // Core tests

    protected static void assertCoreSpellSlots(GameTestHelper helper, ItemStack stack, int expectedSlots) {
        IWand wand = assertInstanceOf(helper, stack.getItem(), IWand.class, "Stack is not a wand as expected");

        // Fill with Hallowed spells. No core has a Hallowed bonus slot, so this measures only the base slots; Earth
        // spells, for example, would also fill the obsidian core's bonus slot.
        int accepted = fillWithSpell(helper, wand, stack, touchSpell(HolyDamageSpellPayload.INSTANCE));
        assertValueEqual(helper, expectedSlots, accepted, "Number of spells accepted");
        assertValueEqual(helper, expectedSlots, wand.getSpellCount(stack), "Number of spells inscribed");
    }

    /**
     * Note that ModularStaffItem.getCoreSpellSlotCount doubles the core's base slots, so the expected count for a
     * staff is twice that of a wand with the same core.
     */
    public static void core_spell_slots(GameTestHelper helper, Item caster, WandCore core, int expectedSlots) {
        assertCoreSpellSlots(helper, makeStack(caster, core, WandCap.IRON, WandGem.APPRENTICE), expectedSlots);
        helper.succeed();
    }

    /**
     * ModularWandItem.canAddSpell counts the payload sources of the existing spells plus the new one. If that count
     * is at most the core's base slots, the spell is accepted; if it exceeds base slots + 1, it is rejected; if it is
     * exactly base slots + 1, it is accepted only when the core has a bonus slot and *any* of the counted spells
     * (existing or new) matches the bonus slot's source.
     */
    public static void wand_core_bonus_slot_accepts_matching_spell(GameTestHelper helper, WandCore core, AbstractSpellPayload<?> bonusPayload, int baseSlots) {
        assertValueEqual(helper, core.getBonusSlot(), bonusPayload.getSource(), "Bonus payload source");

        // Filler (Hallowed) and other (Infernal) spells must not match the bonus source of any core under test
        var fillerSpell = touchSpell(HolyDamageSpellPayload.INSTANCE);
        var otherSpell = touchSpell(FlameDamageSpellPayload.INSTANCE);
        var bonusSpell = touchSpell(bonusPayload);
        assertFalse(helper, core.getBonusSlot().equals(fillerSpell.payload().getComponent().getSource()), "Filler spell matches bonus slot");
        assertFalse(helper, core.getBonusSlot().equals(otherSpell.payload().getComponent().getSource()), "Other spell matches bonus slot");

        var stack = makeWand(core, WandCap.IRON, WandGem.APPRENTICE);
        IWand wand = assertInstanceOf(helper, stack.getItem(), IWand.class, "Stack is not a wand as expected");

        // Fill the base slots with non-matching spells
        for (int index = 0; index < baseSlots; index++) {
            assertTrue(helper, wand.addSpell(stack, fillerSpell), "Filler spell " + index + " was rejected");
        }

        // With the base slots full, only a spell matching the bonus source fits
        assertFalse(helper, wand.canAddSpell(stack, otherSpell), "Non-matching spell accepted into bonus slot");
        assertTrue(helper, wand.addSpell(stack, bonusSpell), "Matching spell rejected from bonus slot");
        assertValueEqual(helper, baseSlots + 1, wand.getSpellCount(stack), "Number of spells inscribed");

        // Once the bonus slot is used, nothing else fits, not even another matching spell
        assertFalse(helper, wand.canAddSpell(stack, bonusSpell), "Matching spell accepted beyond bonus slot");
        assertFalse(helper, wand.canAddSpell(stack, otherSpell), "Non-matching spell accepted beyond bonus slot");

        // Because the check considers all inscribed spells, a matching spell already sitting in a base slot lets a
        // non-matching spell take the final slot
        var preloadedStack = makeWand(core, WandCap.IRON, WandGem.APPRENTICE);
        assertTrue(helper, wand.addSpell(preloadedStack, bonusSpell), "Matching spell rejected from empty wand");
        for (int index = 1; index < baseSlots; index++) {
            assertTrue(helper, wand.addSpell(preloadedStack, fillerSpell), "Filler spell " + index + " was rejected from preloaded wand");
        }
        assertTrue(helper, wand.canAddSpell(preloadedStack, otherSpell), "Non-matching spell rejected despite a matching spell already inscribed");

        helper.succeed();
    }

    public static void wand_core_aligned_sources(GameTestHelper helper, WandCore core, Set<Source> expectedSources) {
        var player = makeMockServerPlayer(helper);
        var stack = makeWand(core, WandCap.IRON, WandGem.APPRENTICE);
        var wandItem = assertInstanceOf(helper, stack.getItem(), ModularWandItem.class, "Stack is not a modular wand as expected");
        var stackCore = wandItem.getWandCore(stack);
        assertTrue(helper, stackCore != null, "Wand has no core");
        // Alignment order has no effect, so compare as sets
        var actualSources = stackCore.getAlignedSources();
        assertValueEqual(helper, expectedSources.size(), actualSources.size(), "Number of aligned sources");
        assertValueEqual(helper, expectedSources, Set.copyOf(actualSources), "Aligned sources");

        // Alignment grants a 5% discount on top of the iron cap's 10%, for aligned sources only
        for (Source source : Sources.getAllSorted()) {
            int expectedModifier = expectedSources.contains(source) ? 15 : 10;
            assertValueEqual(helper, expectedModifier, wandItem.getTotalCostModifier(stack, player, source, helper.getLevel().registryAccess()), "Total cost modifier for " + source.getId());
        }

        helper.succeed();
    }
}
