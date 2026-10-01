package com.verdantartifice.primalmagick.test.menus;

import com.verdantartifice.primalmagick.common.menus.slots.RunescribingResultSlot;
import com.verdantartifice.primalmagick.common.research.ResearchEntries;
import com.verdantartifice.primalmagick.common.research.ResearchManager;
import com.verdantartifice.primalmagick.common.research.keys.RuneEnchantmentKey;
import com.verdantartifice.primalmagick.common.runes.Rune;
import com.verdantartifice.primalmagick.common.runes.RuneManager;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.List;
import java.util.Map;

/**
 * Tests for the runescribing altar's result slot: taking a runescribed item grants rune enchantment research only for
 * the enchantments the runes actually applied to it. Output stacks are built the same way the altar menu builds them,
 * resolving the runes with incompatible enchantments filtered and merging the result into the base item's existing
 * enchantments. Rune combinations mirror the datapack bootstrap in RuneEnchantmentDefinitions, and none of the
 * definitions used here have a research requirement.
 */
public class RunescribingResultSlotTests extends AbstractBaseTest {
    // The altar menu's input container is a 4x3 crafting grid, holding the base item and up to eleven runes
    protected static final int INPUT_SLOT_COUNT = 12;

    // Protection is Protect + Self + Earth and Fire Protection is Protect + Self + Infernal; the two are mutually
    // exclusive, and no other definition shares those combinations
    protected static final List<Rune> COMPETING_PROTECTION_RUNES = List.of(Rune.PROTECT, Rune.SELF, Rune.EARTH, Rune.INFERNAL);

    // Sharpness is Project + Item + Earth and Unbreaking is Protect + Item + Earth. Piercing, Breach, and Bludgeoning
    // share Sharpness's combination but apply only to crossbows, maces, and staves, so on a diamond sword these runes
    // yield exactly Sharpness and Unbreaking, which are compatible.
    protected static final List<Rune> SHARPNESS_AND_UNBREAKING_RUNES = List.of(Rune.PROJECT, Rune.PROTECT, Rune.ITEM, Rune.EARTH);

    // Sharpness alone, as above
    protected static final List<Rune> SHARPNESS_RUNES = List.of(Rune.PROJECT, Rune.ITEM, Rune.EARTH);

    protected static Holder<Enchantment> enchantment(GameTestHelper helper, ResourceKey<Enchantment> key) {
        return helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    protected static RunescribingResultSlot makeSlot(ServerPlayer player) {
        return new RunescribingResultSlot(player, new SimpleContainer(INPUT_SLOT_COUNT), new SimpleContainer(1), 0, 0, 0);
    }

    /**
     * Builds the stack the altar menu would offer for the given base item and runes: the filtered rune enchantments
     * merged into the base item's own, plus the runes themselves.
     */
    protected static ItemStack makeOutputStack(GameTestHelper helper, ServerPlayer player, ItemStack baseStack, List<Rune> runes) {
        Map<Holder<Enchantment>, Integer> inputEnch = RuneManager.getRuneEnchantments(helper.getLevel().registryAccess(), runes, baseStack, player, true);
        assertFalse(helper, inputEnch.isEmpty(), "Runes " + runes + " resolve to no enchantments on " + baseStack);
        ItemEnchantments finalEnch = RuneManager.mergeEnchantments(baseStack.getEnchantments(), inputEnch);
        ItemStack stack = baseStack.copy();
        EnchantmentHelper.setEnchantments(stack, finalEnch);
        RuneManager.setRunes(stack, runes);
        return stack;
    }

    protected static void assertRuneResearch(GameTestHelper helper, ServerPlayer player, Holder<Enchantment> enchant, boolean expected) {
        assertValueEqual(helper, expected, ResearchManager.isResearchComplete(player, new RuneEnchantmentKey(enchant)),
                "Rune enchantment research for " + enchant.getRegisteredName());
    }

    /**
     * The runes resolve to both Protection and Fire Protection, but only Fire Protection survives the incompatibility
     * filter and is applied, so only Fire Protection's research is granted.
     */
    public static void runescribing_credit_only_applied_enchantments(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var protection = enchantment(helper, Enchantments.PROTECTION);
        var fireProtection = enchantment(helper, Enchantments.FIRE_PROTECTION);

        // Fire Protection wins the filter on minimum cost at level 1: vanilla gives it 10, against Protection's 1
        var stack = makeOutputStack(helper, player, new ItemStack(Items.DIAMOND_CHESTPLATE), COMPETING_PROTECTION_RUNES);
        assertValueEqual(helper, 1, stack.getEnchantments().size(), "Output enchantment count");
        assertValueEqual(helper, 1, stack.getEnchantments().getLevel(fireProtection), "Output Fire Protection level");

        makeSlot(player).onTake(player, stack);

        assertRuneResearch(helper, player, fireProtection, true);
        assertRuneResearch(helper, player, protection, false);
        assertTrue(helper, ResearchManager.isResearchComplete(player, ResearchEntries.UNLOCK_RUNE_ENCHANTMENTS), "Rune enchantment unlock research not granted");
        helper.succeed();
    }

    /**
     * When every enchantment the runes produce is applied, research is granted for all of them.
     */
    public static void runescribing_credit_all_applied_enchantments(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var sharpness = enchantment(helper, Enchantments.SHARPNESS);
        var unbreaking = enchantment(helper, Enchantments.UNBREAKING);

        var stack = makeOutputStack(helper, player, new ItemStack(Items.DIAMOND_SWORD), SHARPNESS_AND_UNBREAKING_RUNES);
        assertValueEqual(helper, 2, stack.getEnchantments().size(), "Output enchantment count");
        assertValueEqual(helper, 1, stack.getEnchantments().getLevel(sharpness), "Output Sharpness level");
        assertValueEqual(helper, 1, stack.getEnchantments().getLevel(unbreaking), "Output Unbreaking level");

        makeSlot(player).onTake(player, stack);

        assertRuneResearch(helper, player, sharpness, true);
        assertRuneResearch(helper, player, unbreaking, true);
        assertTrue(helper, ResearchManager.isResearchComplete(player, ResearchEntries.UNLOCK_RUNE_ENCHANTMENTS), "Rune enchantment unlock research not granted");
        helper.succeed();
    }

    /**
     * The base sword already has Smite, so merging drops the Sharpness the runes produce and the output keeps Smite
     * alone while still carrying the runes. No rune enchantment was applied, so nothing is credited; Smite is on the
     * stack but didn't come from the runes, so it isn't credited either.
     */
    public static void runescribing_credit_skips_enchantment_blocked_by_existing(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var sharpness = enchantment(helper, Enchantments.SHARPNESS);
        var smite = enchantment(helper, Enchantments.SMITE);

        var baseStack = new ItemStack(Items.DIAMOND_SWORD);
        baseStack.enchant(smite, 1);
        var stack = makeOutputStack(helper, player, baseStack, SHARPNESS_RUNES);
        assertValueEqual(helper, 1, stack.getEnchantments().size(), "Output enchantment count");
        assertValueEqual(helper, 1, stack.getEnchantments().getLevel(smite), "Output Smite level");
        assertValueEqual(helper, SHARPNESS_RUNES, RuneManager.getRunes(stack), "Output runes");

        makeSlot(player).onTake(player, stack);

        assertRuneResearch(helper, player, sharpness, false);
        assertRuneResearch(helper, player, smite, false);
        assertFalse(helper, ResearchManager.isResearchComplete(player, ResearchEntries.UNLOCK_RUNE_ENCHANTMENTS), "Rune enchantment unlock research granted");
        helper.succeed();
    }

    /**
     * The base chestplate already has Protection. The runes resolve to Protection and Fire Protection, the filter keeps
     * Fire Protection, and the merge then drops it as incompatible with the existing Protection, so the output keeps
     * the original Protection alone. Protection is on the stack and the runes would give it, but they didn't apply it,
     * so nothing is credited.
     */
    public static void runescribing_credit_skips_preexisting_enchantment_matching_runes(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var protection = enchantment(helper, Enchantments.PROTECTION);
        var fireProtection = enchantment(helper, Enchantments.FIRE_PROTECTION);

        var baseStack = new ItemStack(Items.DIAMOND_CHESTPLATE);
        baseStack.enchant(protection, 1);
        var stack = makeOutputStack(helper, player, baseStack, COMPETING_PROTECTION_RUNES);
        assertValueEqual(helper, 1, stack.getEnchantments().size(), "Output enchantment count");
        assertValueEqual(helper, 1, stack.getEnchantments().getLevel(protection), "Output Protection level");
        assertValueEqual(helper, COMPETING_PROTECTION_RUNES, RuneManager.getRunes(stack), "Output runes");

        makeSlot(player).onTake(player, stack);

        assertRuneResearch(helper, player, protection, false);
        assertRuneResearch(helper, player, fireProtection, false);
        assertFalse(helper, ResearchManager.isResearchComplete(player, ResearchEntries.UNLOCK_RUNE_ENCHANTMENTS), "Rune enchantment unlock research granted");
        helper.succeed();
    }
}
