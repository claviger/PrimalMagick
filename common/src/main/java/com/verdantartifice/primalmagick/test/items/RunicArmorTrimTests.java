package com.verdantartifice.primalmagick.test.items;

import com.verdantartifice.primalmagick.common.armortrim.TrimMaterialsPM;
import com.verdantartifice.primalmagick.common.armortrim.TrimPatternsPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.items.armor.RobeArmorItem;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

import java.util.Map;

/**
 * Tests for the runic armor trim: which armor the smithing recipe accepts, and how a trimmed robe changes its mana
 * discounts. The chest robe piece's base discount is 2, as set on its item properties.
 */
public class RunicArmorTrimTests extends AbstractBaseTest {
    private static final int BASE_CHEST_DISCOUNT = 2;

    /** Builds the trim the smithing recipe is meant to produce, so the discount tests do not depend on the recipe. */
    private static ItemStack manuallyTrimmedRobe(GameTestHelper helper, ResourceKey<TrimMaterial> material) {
        var stack = new ItemStack(ItemsPM.IMBUED_WOOL_CHEST.get());
        var registries = helper.getLevel().registryAccess();
        stack.set(DataComponents.TRIM, new ArmorTrim(registries.lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(material), registries.lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(TrimPatternsPM.RUNIC)));
        return stack;
    }

    private static ItemStack trimmedRobe(GameTestHelper helper, Item runeItem) {
        var input = new SmithingRecipeInput(new ItemStack(ItemsPM.RUNIC_ARMOR_TRIM_SMITHING_TEMPLATE.get()), new ItemStack(ItemsPM.IMBUED_WOOL_CHEST.get()), new ItemStack(runeItem));
        var recipe = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.SMITHING, input, helper.getLevel());
        assertTrue(helper, recipe.isPresent(), "Runic trim recipe not found for a robe and " + runeItem);
        return recipe.get().value().assemble(input);
    }

    /** The smithing recipe trims a Primal Magick robe with the runic pattern, in the material of the source rune used. */
    public static void runic_trim_recipe_applies_to_robe(GameTestHelper helper) {
        Map<Item, ResourceKey<TrimMaterial>> runeMaterials = Map.of(
                ItemsPM.RUNE_EARTH.get(), TrimMaterialsPM.RUNE_EARTH,
                ItemsPM.RUNE_SEA.get(), TrimMaterialsPM.RUNE_SEA,
                ItemsPM.RUNE_SKY.get(), TrimMaterialsPM.RUNE_SKY,
                ItemsPM.RUNE_SUN.get(), TrimMaterialsPM.RUNE_SUN,
                ItemsPM.RUNE_MOON.get(), TrimMaterialsPM.RUNE_MOON,
                ItemsPM.RUNE_BLOOD.get(), TrimMaterialsPM.RUNE_BLOOD,
                ItemsPM.RUNE_INFERNAL.get(), TrimMaterialsPM.RUNE_INFERNAL,
                ItemsPM.RUNE_VOID.get(), TrimMaterialsPM.RUNE_VOID,
                ItemsPM.RUNE_HALLOWED.get(), TrimMaterialsPM.RUNE_HALLOWED);
        runeMaterials.forEach((runeItem, material) -> {
            var result = trimmedRobe(helper, runeItem);
            assertTrue(helper, result.is(ItemsPM.IMBUED_WOOL_CHEST.get()), "Trim result for " + runeItem + " is not the robe: " + result);
            var trim = result.get(DataComponents.TRIM);
            assertTrue(helper, trim != null, "Trim result for " + runeItem + " has no armor trim");
            assertTrue(helper, trim.pattern().is(TrimPatternsPM.RUNIC), "Armor trim pattern for " + runeItem + " is not runic");
            assertTrue(helper, trim.material().is(material), "Armor trim material for " + runeItem + " is not " + material.identifier());
        });
        helper.succeed();
    }

    /** The recipe's base is the runic trimmable tag, which holds only Primal Magick robes, so other armor is rejected. */
    public static void runic_trim_recipe_rejects_other_armor(GameTestHelper helper) {
        for (Item armor : new Item[] { Items.LEATHER_CHESTPLATE, Items.IRON_CHESTPLATE, Items.DIAMOND_CHESTPLATE }) {
            var input = new SmithingRecipeInput(new ItemStack(ItemsPM.RUNIC_ARMOR_TRIM_SMITHING_TEMPLATE.get()), new ItemStack(armor), new ItemStack(ItemsPM.RUNE_EARTH.get()));
            var recipe = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.SMITHING, input, helper.getLevel());
            assertTrue(helper, recipe.isEmpty(), "Runic trim recipe accepted " + armor);
        }
        helper.succeed();
    }

    /** Without a trim every source gets the robe's base discount. */
    public static void untrimmed_robe_gives_base_discount_for_every_source(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var stack = new ItemStack(ItemsPM.IMBUED_WOOL_CHEST.get());
        var robe = assertInstanceOf(helper, stack.getItem(), RobeArmorItem.class, "Item is not a robe");
        Sources.streamSorted().forEach(s -> assertValueEqual(helper, BASE_CHEST_DISCOUNT, robe.getManaDiscount(stack, player, s), "Untrimmed discount for " + s.getId()));
        helper.succeed();
    }

    /** A robe trimmed with the earth rune gets double its base discount for earth mana. */
    public static void runic_trim_doubles_discount_for_trim_source(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var stack = manuallyTrimmedRobe(helper, TrimMaterialsPM.RUNE_EARTH);
        var robe = assertInstanceOf(helper, stack.getItem(), RobeArmorItem.class, "Item is not a robe");
        assertValueEqual(helper, 2 * BASE_CHEST_DISCOUNT, robe.getManaDiscount(stack, player, Sources.EARTH), "Earth discount on an earth-trimmed robe");
        helper.succeed();
    }

    /** A trimmed robe gives no discount at all for any source other than its trim's. */
    public static void runic_trim_removes_discounts_for_other_sources(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var stack = manuallyTrimmedRobe(helper, TrimMaterialsPM.RUNE_EARTH);
        var robe = assertInstanceOf(helper, stack.getItem(), RobeArmorItem.class, "Item is not a robe");
        Sources.streamSorted().filter(s -> !s.equals(Sources.EARTH)).forEach(s ->
                assertValueEqual(helper, 0, robe.getManaDiscount(stack, player, s), "Discount for " + s.getId() + " on an earth-trimmed robe"));
        helper.succeed();
    }
}
