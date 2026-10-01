package com.verdantartifice.primalmagick.test.runes;

import com.verdantartifice.primalmagick.common.enchantments.EnchantmentsPM;
import com.verdantartifice.primalmagick.common.research.ResearchEntries;
import com.verdantartifice.primalmagick.common.research.ResearchEntry;
import com.verdantartifice.primalmagick.common.research.ResearchManager;
import com.verdantartifice.primalmagick.common.research.keys.RuneEnchantmentKey;
import com.verdantartifice.primalmagick.common.research.keys.RuneEnchantmentPartialKey;
import com.verdantartifice.primalmagick.common.runes.NounRune;
import com.verdantartifice.primalmagick.common.runes.Rune;
import com.verdantartifice.primalmagick.common.runes.RuneManager;
import com.verdantartifice.primalmagick.common.runes.RuneType;
import com.verdantartifice.primalmagick.common.runes.SourceRune;
import com.verdantartifice.primalmagick.common.runes.VerbRune;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tests for RuneManager: resolving verb/noun/source rune combinations into enchantments, per-rune limits, storing
 * runes on item stacks, merging rune enchantments into existing ones, and rune definition and knowledge lookups.
 * Expected rune combinations mirror the datapack bootstrap in RuneEnchantmentDefinitions and are hard-coded in the
 * test registrations so that changes to that content are caught. Resolution tests filter incompatible
 * enchantments, matching the runescribing altar's preview, unless they are specifically testing that filter.
 */
public class RuneManagerTests extends AbstractBaseTest {
    /**
     * The research required by the Lucky Strike rune definition, which is one of the few definitions gated behind
     * research. All other definitions exercised here have no research requirement.
     */
    public static final List<ResourceKey<ResearchEntry>> LUCKY_STRIKE_RESEARCH = List.of(ResearchEntries.MASTER_RUNEWORKING,
            ResearchEntries.PRIMAL_PICKAXE, ResearchEntries.RUNE_SUMMON, ResearchEntries.RUNE_ITEM, ResearchEntries.RUNE_MOON);

    // Sharpness is Project + Item + Earth. Piercing, Breach, and Bludgeoning share that combination but apply only to
    // crossbows, maces, and staves respectively, so on a diamond sword the runes resolve to Sharpness alone.
    protected static final List<Rune> SHARPNESS_RUNES = List.of(Rune.PROJECT, Rune.ITEM, Rune.EARTH);

    // Lucky Strike is Summon + Item + Moon; no other definition shares that combination
    protected static final List<Rune> LUCKY_STRIKE_RUNES = List.of(Rune.SUMMON, Rune.ITEM, Rune.MOON);

    // Silk Touch is Project + Item + Sea. Aqua Affinity, Depth Strider, and Impaling share that combination but apply
    // only to helmets, boots, and tridents respectively, so on a diamond pickaxe the runes resolve to Silk Touch alone.
    protected static final List<Rune> SILK_TOUCH_RUNES = List.of(Rune.PROJECT, Rune.ITEM, Rune.SEA);

    // Protection and Fire Protection share the Protect and Self runes, so adding both of their source runes yields
    // two mutually exclusive enchantments for a chestplate
    protected static final List<Rune> COMPETING_PROTECTION_RUNES = List.of(Rune.PROTECT, Rune.SELF, Rune.EARTH, Rune.INFERNAL);

    // The rune types that make up an enchantment's rune combination
    protected static final List<RuneType> COMBO_RUNE_TYPES = List.of(RuneType.VERB, RuneType.NOUN, RuneType.SOURCE);

    // Number of copies used to show that an unlimited rune is not capped by checkLimits
    protected static final int MANY_RUNES = 16;

    protected static Holder<Enchantment> enchantment(GameTestHelper helper, ResourceKey<Enchantment> key) {
        return helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    protected static Map<Holder<Enchantment>, Integer> resolve(GameTestHelper helper, List<Rune> runes, ItemStack stack, Player player, boolean filterIncompatible) {
        return RuneManager.getRuneEnchantments(helper.getLevel().registryAccess(), runes, stack, player, filterIncompatible);
    }

    protected static void grantResearch(Player player, List<ResourceKey<ResearchEntry>> keys) {
        keys.forEach(key -> ResearchManager.forceGrantWithAllParents(player, key));
    }

    protected static void assertNoRunesKnown(GameTestHelper helper, Player player, Holder<Enchantment> enchant) {
        for (RuneType type : COMBO_RUNE_TYPES) {
            assertFalse(helper, RuneManager.isRuneKnown(player, enchant, type), "Fresh player knows the " + type.getSerializedName() + " rune");
        }
    }

    protected static List<Rune> concat(List<Rune> first, List<Rune> second) {
        List<Rune> retVal = new ArrayList<>(first);
        retVal.addAll(second);
        return retVal;
    }

    protected static ItemEnchantments itemEnchantments(Holder<Enchantment> enchant, int level) {
        var mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mutable.set(enchant, level);
        return mutable.toImmutable();
    }

    // Rune enchantment resolution tests

    public static void rune_enchantment_resolves(GameTestHelper helper, VerbRune verb, NounRune noun, SourceRune source, ResourceKey<Enchantment> expectedKey,
                                                 Item item, List<ResourceKey<ResearchEntry>> requiredResearch) {
        var player = makeMockServerPlayer(helper);
        grantResearch(player, requiredResearch);
        var result = resolve(helper, List.of(verb, noun, source), new ItemStack(item), player, true);
        assertValueEqual(helper, Map.of(enchantment(helper, expectedKey), 1), result, "Rune enchantments for " + expectedKey.identifier());
        helper.succeed();
    }

    public static void rune_enchantment_requires_research(GameTestHelper helper) {
        // No research is granted, so Lucky Strike's research requirement is unmet
        var player = makeMockServerPlayer(helper);
        var result = resolve(helper, LUCKY_STRIKE_RUNES, new ItemStack(Items.DIAMOND_PICKAXE), player, true);
        assertValueEqual(helper, Collections.emptyMap(), result, "Rune enchantments without required research");
        helper.succeed();
    }

    public static void rune_enchantment_requires_enchantable_stack(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        grantResearch(player, LUCKY_STRIKE_RESEARCH);

        // Control: with the research granted, the runes resolve on an item that supports the enchantment
        var control = resolve(helper, LUCKY_STRIKE_RUNES, new ItemStack(Items.DIAMOND_PICKAXE), player, true);
        assertValueEqual(helper, Map.of(enchantment(helper, EnchantmentsPM.LUCKY_STRIKE), 1), control, "Rune enchantments on a diamond pickaxe");

        var result = resolve(helper, LUCKY_STRIKE_RUNES, new ItemStack(Items.STICK), player, true);
        assertValueEqual(helper, Collections.emptyMap(), result, "Rune enchantments on a stick");
        helper.succeed();
    }

    public static void rune_enchantment_power_rune_gives_level(GameTestHelper helper, List<Rune> powerRunes, int expectedLevel) {
        // Sharpness has a max level of 5, so the level is never capped here
        var player = makeMockServerPlayer(helper);
        var result = resolve(helper, concat(SHARPNESS_RUNES, powerRunes), new ItemStack(Items.DIAMOND_SWORD), player, true);
        assertValueEqual(helper, Map.of(enchantment(helper, Enchantments.SHARPNESS), expectedLevel), result, "Rune enchantments with power runes " + powerRunes);
        helper.succeed();
    }

    public static void rune_enchantment_power_rune_capped_at_max_level(GameTestHelper helper) {
        // Two power-type runes would raise the level to 3, but Silk Touch has a max level of 1
        var player = makeMockServerPlayer(helper);
        var result = resolve(helper, concat(SILK_TOUCH_RUNES, List.of(Rune.POWER, Rune.GRACE)), new ItemStack(Items.DIAMOND_PICKAXE), player, true);
        assertValueEqual(helper, Map.of(enchantment(helper, Enchantments.SILK_TOUCH), 1), result, "Rune enchantments with power runes on a max level 1 enchantment");
        helper.succeed();
    }

    public static void rune_enchantment_competing_unfiltered(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var result = resolve(helper, COMPETING_PROTECTION_RUNES, new ItemStack(Items.DIAMOND_CHESTPLATE), player, false);
        var expected = Map.of(enchantment(helper, Enchantments.PROTECTION), 1, enchantment(helper, Enchantments.FIRE_PROTECTION), 1);
        assertValueEqual(helper, expected, result, "Unfiltered rune enchantments for competing protection runes");
        helper.succeed();
    }

    public static void rune_enchantment_competing_filtered(GameTestHelper helper) {
        // Protection and Fire Protection are mutually exclusive, so filtering keeps only one of them. The winner depends
        // on the resolution sort order (minimum enchanting cost, then hash code), so it is deliberately not checked.
        var player = makeMockServerPlayer(helper);
        var result = resolve(helper, COMPETING_PROTECTION_RUNES, new ItemStack(Items.DIAMOND_CHESTPLATE), player, true);
        assertValueEqual(helper, 1, result.size(), "Filtered rune enchantment count for competing protection runes, got " + result);
        var protections = Set.of(enchantment(helper, Enchantments.PROTECTION), enchantment(helper, Enchantments.FIRE_PROTECTION));
        assertTrue(helper, protections.containsAll(result.keySet()), "Filtered rune enchantment is not a competing protection enchantment: " + result);
        helper.succeed();
    }

    public static void rune_enchantment_empty_inputs(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var stack = new ItemStack(Items.DIAMOND_SWORD);

        // Control: the same inputs resolve when none of them are missing
        assertValueEqual(helper, Map.of(enchantment(helper, Enchantments.SHARPNESS), 1), resolve(helper, SHARPNESS_RUNES, stack, player, true), "Rune enchantments with valid inputs");

        assertValueEqual(helper, Collections.emptyMap(), resolve(helper, null, stack, player, true), "Rune enchantments with null runes");
        assertValueEqual(helper, Collections.emptyMap(), resolve(helper, List.of(), stack, player, true), "Rune enchantments with empty runes");
        assertValueEqual(helper, Collections.emptyMap(), resolve(helper, SHARPNESS_RUNES, null, player, true), "Rune enchantments with null stack");
        assertValueEqual(helper, Collections.emptyMap(), resolve(helper, SHARPNESS_RUNES, ItemStack.EMPTY, player, true), "Rune enchantments with empty stack");
        assertValueEqual(helper, Collections.emptyMap(), resolve(helper, SHARPNESS_RUNES, stack, null, true), "Rune enchantments with null player");
        helper.succeed();
    }

    // Rune limit tests

    public static void rune_limits(GameTestHelper helper, Rune rune, int expectedLimit) {
        assertTrue(helper, rune.hasLimit(), "Rune " + rune.getId() + " has no limit");
        assertValueEqual(helper, expectedLimit, rune.getLimit(), "Limit for rune " + rune.getId());

        List<Rune> atLimit = Collections.nCopies(expectedLimit, rune);
        List<Rune> overLimit = Collections.nCopies(expectedLimit + 1, rune);
        assertTrue(helper, RuneManager.checkLimits(atLimit), "Runes at the limit fail checkLimits");
        assertFalse(helper, RuneManager.checkLimits(overLimit), "Runes over the limit pass checkLimits");

        // Exceeding a limit invalidates the whole rune combination, not just the excess runes
        var player = makeMockServerPlayer(helper);
        var result = resolve(helper, concat(SHARPNESS_RUNES, overLimit), new ItemStack(Items.DIAMOND_SWORD), player, true);
        assertValueEqual(helper, Collections.emptyMap(), result, "Rune enchantments with runes over the limit");
        helper.succeed();
    }

    public static void rune_limits_unlimited(GameTestHelper helper, Rune rune) {
        assertFalse(helper, rune.hasLimit(), "Rune " + rune.getId() + " has a limit of " + rune.getLimit());
        assertTrue(helper, RuneManager.checkLimits(Collections.nCopies(MANY_RUNES, rune)), "Many copies of an unlimited rune fail checkLimits");
        helper.succeed();
    }

    // Rune stack storage tests

    public static void rune_stack_set_and_clear(GameTestHelper helper) {
        var stack = new ItemStack(Items.DIAMOND_SWORD);
        assertFalse(helper, RuneManager.hasRunes(stack), "Fresh stack has runes");
        assertValueEqual(helper, List.of(), RuneManager.getRunes(stack), "Runes on a fresh stack");

        // setRunes ignores an empty rune list
        RuneManager.setRunes(stack, List.of());
        assertFalse(helper, RuneManager.hasRunes(stack), "Stack has runes after setting an empty list");

        List<Rune> runes = List.of(Rune.PROJECT, Rune.ITEM, Rune.EARTH, Rune.POWER);

        // setRunes ignores an empty stack
        RuneManager.setRunes(ItemStack.EMPTY, runes);
        assertFalse(helper, RuneManager.hasRunes(ItemStack.EMPTY), "Empty stack has runes after setRunes");

        RuneManager.setRunes(stack, runes);
        assertTrue(helper, RuneManager.hasRunes(stack), "Stack has no runes after setRunes");
        assertValueEqual(helper, runes, RuneManager.getRunes(stack), "Runes after setRunes");

        RuneManager.clearRunes(stack);
        assertFalse(helper, RuneManager.hasRunes(stack), "Stack has runes after clearRunes");
        assertValueEqual(helper, List.of(), RuneManager.getRunes(stack), "Runes after clearRunes");
        helper.succeed();
    }

    // Enchantment merge tests

    public static void rune_merge_enchantments_takes_stronger(GameTestHelper helper) {
        var sharpness = enchantment(helper, Enchantments.SHARPNESS);

        var upgraded = RuneManager.mergeEnchantments(itemEnchantments(sharpness, 1), Map.of(sharpness, 3));
        assertValueEqual(helper, 3, upgraded.getLevel(sharpness), "Sharpness level when the addition is stronger");
        assertValueEqual(helper, 1, upgraded.size(), "Enchantment count when the addition is stronger");

        var kept = RuneManager.mergeEnchantments(itemEnchantments(sharpness, 3), Map.of(sharpness, 1));
        assertValueEqual(helper, 3, kept.getLevel(sharpness), "Sharpness level when the original is stronger");
        assertValueEqual(helper, 1, kept.size(), "Enchantment count when the original is stronger");
        helper.succeed();
    }

    /**
     * RuneManager.mergeEnchantments currently checks each addition's compatibility against the original map rather
     * than the accumulating result, so two mutually incompatible additions could both be added to a stack that has
     * neither. This test only covers an addition that conflicts with the original, and deliberately does not lock in
     * either behaviour for two mutually incompatible additions.
     */
    public static void rune_merge_enchantments_skips_incompatible_with_original(GameTestHelper helper) {
        // Sharpness and Smite are mutually exclusive damage enchantments
        var sharpness = enchantment(helper, Enchantments.SHARPNESS);
        var smite = enchantment(helper, Enchantments.SMITE);
        var merged = RuneManager.mergeEnchantments(itemEnchantments(sharpness, 2), Map.of(smite, 3));
        assertValueEqual(helper, 2, merged.getLevel(sharpness), "Sharpness level after merging incompatible Smite");
        assertValueEqual(helper, 0, merged.getLevel(smite), "Smite level after merging into Sharpness");
        assertValueEqual(helper, 1, merged.size(), "Enchantment count after merging incompatible Smite");
        helper.succeed();
    }

    public static void rune_merge_enchantments_adds_compatible(GameTestHelper helper) {
        var sharpness = enchantment(helper, Enchantments.SHARPNESS);
        var unbreaking = enchantment(helper, Enchantments.UNBREAKING);
        var merged = RuneManager.mergeEnchantments(itemEnchantments(sharpness, 2), Map.of(unbreaking, 3));
        assertValueEqual(helper, 2, merged.getLevel(sharpness), "Sharpness level after merging compatible Unbreaking");
        assertValueEqual(helper, 3, merged.getLevel(unbreaking), "Unbreaking level after merging into Sharpness");
        assertValueEqual(helper, 2, merged.size(), "Enchantment count after merging compatible Unbreaking");
        helper.succeed();
    }

    // Rune definition tests

    public static void rune_definition_lookup(GameTestHelper helper) {
        var registryAccess = helper.getLevel().registryAccess();
        var sharpness = enchantment(helper, Enchantments.SHARPNESS);
        assertTrue(helper, RuneManager.hasRuneDefinition(registryAccess, sharpness), "Sharpness has no rune definition");
        var definition = RuneManager.getRuneDefinition(registryAccess, sharpness);
        assertTrue(helper, definition.isPresent(), "Sharpness rune definition lookup is empty");
        assertValueEqual(helper, SHARPNESS_RUNES, definition.get().getRunes(), "Sharpness definition runes");

        // Curses are never produced by runes
        var binding = enchantment(helper, Enchantments.BINDING_CURSE);
        assertFalse(helper, RuneManager.hasRuneDefinition(registryAccess, binding), "Curse of Binding has a rune definition");
        assertTrue(helper, RuneManager.getRuneDefinition(registryAccess, binding).isEmpty(), "Curse of Binding rune definition lookup is present");
        helper.succeed();
    }

    // Rune knowledge tests

    /**
     * RuneManager.isRuneKnown reports a rune type as known for an enchantment if the player has completed either the
     * full RuneEnchantmentKey research for that enchantment or the RuneEnchantmentPartialKey for that specific rune
     * type. Completing a partial key reveals only its own rune type.
     */
    public static void rune_is_known(GameTestHelper helper, RuneType learnedType) {
        var player = makeMockServerPlayer(helper);
        var sharpness = enchantment(helper, Enchantments.SHARPNESS);
        assertNoRunesKnown(helper, player, sharpness);

        ResearchManager.completeResearch(player, new RuneEnchantmentPartialKey(sharpness, learnedType));
        for (RuneType type : COMBO_RUNE_TYPES) {
            assertValueEqual(helper, type == learnedType, RuneManager.isRuneKnown(player, sharpness, type),
                    "Knowledge of the " + type.getSerializedName() + " rune after learning the " + learnedType.getSerializedName() + " rune");
        }
        helper.succeed();
    }

    public static void rune_is_known_full_key(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var sharpness = enchantment(helper, Enchantments.SHARPNESS);
        assertNoRunesKnown(helper, player, sharpness);

        ResearchManager.completeResearch(player, new RuneEnchantmentKey(sharpness));
        for (RuneType type : COMBO_RUNE_TYPES) {
            assertTrue(helper, RuneManager.isRuneKnown(player, sharpness, type), "Player does not know the " + type.getSerializedName() + " rune after learning the full combination");
        }
        helper.succeed();
    }
}
