package com.verdantartifice.primalmagick.mixin;

import com.verdantartifice.primalmagick.client.gui.recipe_book.DissolutionChamberRecipeBookComponent;
import com.verdantartifice.primalmagick.client.gui.recipe_book.InfernalFurnaceRecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Mixin to give the recipe books of the mod's single-ingredient machines the furnace-style alternate recipe overlay.
 * Vanilla only selects it for furnace menus, and the crafting-style overlay cannot lay out a single ingredient.
 */
@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin {
    @ModifyArg(method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookPage;<init>(Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;Lnet/minecraft/client/gui/screens/recipebook/SlotSelectTime;Z)V"),
            index = 2)
    private boolean primalmagick$useFurnaceOverlay(boolean isFurnaceMenu) {
        Object self = this;
        return isFurnaceMenu || self instanceof DissolutionChamberRecipeBookComponent || self instanceof InfernalFurnaceRecipeBookComponent;
    }
}
