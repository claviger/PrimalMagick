package com.verdantartifice.primalmagick.test.crafting;

import com.verdantartifice.primalmagick.common.crafting.ShapelessTagRecipe;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;
import java.util.Optional;

/**
 * Tests that the Earthshatter Hammer crushes each of the basic metal ores and raw metals into two grit. Each case
 * combines a hammer and the given item in a crafting grid and checks the recipe that matches and its output.
 */
public class EarthshatterHammerCrushingTests extends AbstractBaseTest {
    public static void hammer_crushes_ore(GameTestHelper helper, Item ore, Item expectedGrit, String expectedRecipeName) {
        crush(helper, ore, expectedGrit, expectedRecipeName);
        helper.succeed();
    }

    public static void hammer_crushes_raw_metal(GameTestHelper helper, Item rawMetal, Item expectedGrit, String expectedRecipeName) {
        crush(helper, rawMetal, expectedGrit, expectedRecipeName);
        helper.succeed();
    }

    private static void crush(GameTestHelper helper, Item input, Item expectedGrit, String expectedRecipeName) {
        ItemStack hammer = new ItemStack(ItemsPM.EARTHSHATTER_HAMMER.get());
        CraftingInput craftingInput = CraftingInput.of(2, 1, List.of(hammer, new ItemStack(input)));
        Optional<RecipeHolder<CraftingRecipe>> recipeOpt = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, craftingInput, helper.getLevel());
        assertTrue(helper, recipeOpt.isPresent(), "No crafting recipe found for hammer and " + input);
        RecipeHolder<CraftingRecipe> holder = recipeOpt.get();
        assertTrue(helper, holder.value() instanceof ShapelessTagRecipe, "Matched recipe is not a shapeless tag recipe: " + holder.id());
        assertValueEqual(helper, expectedRecipeName, holder.id().identifier().getPath(), "Matched recipe ID");

        // Every grit recipe yields two grit (resultAmount 2)
        ItemStack output = holder.value().assemble(craftingInput);
        assertTrue(helper, output.is(expectedGrit), "Recipe output is not " + expectedGrit + ": " + output);
        assertValueEqual(helper, 2, output.getCount(), "Recipe output count");

        // The hammer survives the craft and the crushed item is consumed
        var remaining = holder.value().getRemainingItems(craftingInput);
        assertTrue(helper, remaining.get(0).is(ItemsPM.EARTHSHATTER_HAMMER.get()), "Hammer slot remainder is not a hammer: " + remaining.get(0));
        assertTrue(helper, remaining.get(1).isEmpty(), "Crushed item slot remainder not empty: " + remaining.get(1));
    }
}
