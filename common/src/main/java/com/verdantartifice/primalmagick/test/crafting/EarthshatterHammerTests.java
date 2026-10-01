package com.verdantartifice.primalmagick.test.crafting;

import com.verdantartifice.primalmagick.common.crafting.ShapelessTagRecipe;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;
import java.util.Optional;

/**
 * Tests for the crafting remainder of the Earthshatter Hammer. The hammer has no durability, so each hammer
 * craft should leave it in the grid unchanged. Crafting input layout: slot 0 = hammer, slot 1 = ore.
 */
public class EarthshatterHammerTests extends AbstractBaseTest {
    private static final int HAMMER_SLOT = 0;
    private static final int ORE_SLOT = 1;

    public static void earthshatter_hammer_recipe_returns_hammer(GameTestHelper helper) {
        ItemStack hammer = new ItemStack(ItemsPM.EARTHSHATTER_HAMMER.get());
        CraftingInput input = makeInput(hammer);

        // A hammer and iron ore should match the iron grit tag recipe, which yields two grit
        RecipeHolder<CraftingRecipe> holder = findRecipe(helper, input);
        assertTrue(helper, holder.value() instanceof ShapelessTagRecipe, "Matched recipe is not a shapeless tag recipe: " + holder.id());
        assertValueEqual(helper, "iron_grit_from_ore", holder.id().identifier().getPath(), "Matched recipe ID");
        ItemStack output = holder.value().assemble(input);
        assertTrue(helper, output.is(ItemsPM.IRON_GRIT.get()), "Recipe output is not iron grit: " + output);
        assertValueEqual(helper, 2, output.getCount(), "Recipe output count");

        // The hammer should come back unchanged as a single item, and the ore should be consumed
        NonNullList<ItemStack> remaining = holder.value().getRemainingItems(input);
        ItemStack hammerRemainder = remaining.get(HAMMER_SLOT);
        assertTrue(helper, ItemStack.isSameItemSameComponents(hammer, hammerRemainder), "Hammer slot remainder differs from input hammer: " + hammerRemainder);
        assertValueEqual(helper, 1, hammerRemainder.getCount(), "Hammer remainder count");
        assertTrue(helper, remaining.get(ORE_SLOT).isEmpty(), "Ore slot remainder not empty: " + remaining.get(ORE_SLOT));

        helper.succeed();
    }

    public static void earthshatter_hammer_has_no_durability(GameTestHelper helper) {
        // Upstream removed the hammer's durability, so it should carry no max damage and never be damageable
        ItemStack hammer = new ItemStack(ItemsPM.EARTHSHATTER_HAMMER.get());
        assertFalse(helper, hammer.has(DataComponents.MAX_DAMAGE), "Hammer has a max damage component");
        assertFalse(helper, hammer.isDamageableItem(), "Hammer is damageable");
        assertValueEqual(helper, 0, hammer.getMaxDamage(), "Hammer max damage");

        helper.succeed();
    }

    public static void earthshatter_hammer_remainder_keeps_components(GameTestHelper helper) {
        ItemStack hammer = new ItemStack(ItemsPM.EARTHSHATTER_HAMMER.get());
        hammer.set(DataComponents.CUSTOM_NAME, Component.literal("Rockbreaker"));
        CraftingInput input = makeInput(hammer);

        // The renamed hammer should come back with its name rather than as a default hammer
        NonNullList<ItemStack> remaining = findRecipe(helper, input).value().getRemainingItems(input);
        ItemStack remainder = remaining.get(HAMMER_SLOT);
        assertTrue(helper, remainder.is(ItemsPM.EARTHSHATTER_HAMMER.get()), "Hammer slot remainder is not a hammer: " + remainder);
        assertValueEqual(helper, Component.literal("Rockbreaker"), remainder.get(DataComponents.CUSTOM_NAME), "Remainder custom name");
        assertTrue(helper, ItemStack.isSameItemSameComponents(hammer, remainder), "Remainder components differ from input hammer: " + remainder);

        helper.succeed();
    }

    public static void earthshatter_hammer_remainder_count_is_one_for_stacked_input(GameTestHelper helper) {
        // Two hammers in one slot should still match the recipe
        ItemStack hammers = new ItemStack(ItemsPM.EARTHSHATTER_HAMMER.get(), 2);
        CraftingInput input = makeInput(hammers);
        NonNullList<ItemStack> remaining = findRecipe(helper, input).value().getRemainingItems(input);

        // The result slot merges the remainder back into the grid slot, so returning the full stack of 2 would
        // duplicate hammers; exactly one hammer must come back
        ItemStack remainder = remaining.get(HAMMER_SLOT);
        assertTrue(helper, remainder.is(ItemsPM.EARTHSHATTER_HAMMER.get()), "Hammer slot remainder is not a hammer: " + remainder);
        assertValueEqual(helper, 1, remainder.getCount(), "Hammer remainder count for stacked input");

        helper.succeed();
    }

    private static CraftingInput makeInput(ItemStack hammer) {
        return CraftingInput.of(2, 1, List.of(hammer, new ItemStack(Items.IRON_ORE)));
    }

    private static RecipeHolder<CraftingRecipe> findRecipe(GameTestHelper helper, CraftingInput input) {
        Optional<RecipeHolder<CraftingRecipe>> recipeOpt = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
        assertTrue(helper, recipeOpt.isPresent(), "No crafting recipe found for hammer and iron ore");
        return recipeOpt.get();
    }
}
