package com.verdantartifice.primalmagick.mixin;

import com.verdantartifice.primalmagick.common.crafting.display.ConcoctingRecipeDisplay;
import com.verdantartifice.primalmagick.common.crafting.display.ShapedArcaneCraftingRecipeDisplay;
import com.verdantartifice.primalmagick.common.crafting.display.ShapelessArcaneCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Mixin to let the recipe book's alternate recipe overlay lay out the ingredients of the mod's grid-based recipe
 * displays.  The vanilla overlay only knows the vanilla shaped and shapeless crafting displays, and draws an empty
 * grid for anything else, so mod displays are presented to it as their vanilla equivalents.
 */
@Mixin(targets = "net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent$OverlayCraftingRecipeButton")
public abstract class OverlayCraftingRecipeButtonMixin {
    @ModifyVariable(method = "calculateIngredientsPositions", at = @At("HEAD"), argsOnly = true)
    private static RecipeDisplay primalmagick$adaptRecipeDisplay(RecipeDisplay recipe) {
        if (recipe instanceof ShapedArcaneCraftingRecipeDisplay shaped) {
            return new ShapedCraftingRecipeDisplay(shaped.width(), shaped.height(), shaped.ingredients(), shaped.result(), shaped.craftingStation());
        } else if (recipe instanceof ShapelessArcaneCraftingRecipeDisplay shapeless) {
            return new ShapelessCraftingRecipeDisplay(shapeless.ingredients(), shapeless.result(), shapeless.craftingStation());
        } else if (recipe instanceof ConcoctingRecipeDisplay concocting) {
            return new ShapelessCraftingRecipeDisplay(concocting.ingredients(), concocting.result(), concocting.craftingStation());
        } else {
            return recipe;
        }
    }
}
