package com.verdantartifice.primalmagick.mixin;

import com.verdantartifice.primalmagick.common.crafting.display.DissolutionRecipeDisplay;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Mixin to let the recipe book's single-ingredient alternate recipe overlay show the ingredient of dissolution
 * recipes.  The vanilla overlay only knows furnace displays, so dissolution displays are presented to it as one.
 */
@Mixin(targets = "net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent$OverlaySmeltingRecipeButton")
public abstract class OverlaySmeltingRecipeButtonMixin {
    @ModifyVariable(method = "calculateIngredientsPositions", at = @At("HEAD"), argsOnly = true)
    private static RecipeDisplay primalmagick$adaptRecipeDisplay(RecipeDisplay recipe) {
        if (recipe instanceof DissolutionRecipeDisplay dissolution) {
            return new FurnaceRecipeDisplay(dissolution.ingredient(), SlotDisplay.Empty.INSTANCE, dissolution.result(), dissolution.craftingStation(), 0, 0F);
        } else {
            return recipe;
        }
    }
}
