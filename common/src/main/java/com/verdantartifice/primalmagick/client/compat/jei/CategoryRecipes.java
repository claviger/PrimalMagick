package com.verdantartifice.primalmagick.client.compat.jei;

import com.verdantartifice.primalmagick.client.recipes.ClientRecipeCache;
import com.verdantartifice.primalmagick.common.crafting.IArcaneRecipe;
import com.verdantartifice.primalmagick.common.crafting.IConcoctingRecipe;
import com.verdantartifice.primalmagick.common.crafting.IDissolutionRecipe;
import com.verdantartifice.primalmagick.common.crafting.IRitualRecipe;
import com.verdantartifice.primalmagick.common.crafting.IRunecarvingRecipe;
import com.verdantartifice.primalmagick.common.crafting.RecipeTypesPM;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

/**
 * Helper class to fetch which recipes belong to each recipe category.
 * 
 * @author Daedalus4096
 */
public class CategoryRecipes {
    private final ClientRecipeCache recipeCache;
    
    public CategoryRecipes() {
        this.recipeCache = ClientRecipeCache.getInstance();
    }
    
    public List<RecipeHolder<IArcaneRecipe>> getArcaneRecipes(IRecipeCategory<RecipeHolder<IArcaneRecipe>> category) {
        CategoryRecipeValidatorPM<IArcaneRecipe> validator = new CategoryRecipeValidatorPM<>(category, 9, true);
        return getValidHandledRecipes(this.recipeCache, RecipeTypesPM.ARCANE_CRAFTING.get(), validator);
    }
    
    public List<RecipeHolder<IConcoctingRecipe>> getConcoctingRecipes(IRecipeCategory<RecipeHolder<IConcoctingRecipe>> category) {
        CategoryRecipeValidatorPM<IConcoctingRecipe> validator = new CategoryRecipeValidatorPM<>(category, 9, true);
        return getValidHandledRecipes(this.recipeCache, RecipeTypesPM.CONCOCTING.get(), validator);
    }
    
    public List<RecipeHolder<IRunecarvingRecipe>> getRunecarvingRecipes(IRecipeCategory<RecipeHolder<IRunecarvingRecipe>> category) {
        CategoryRecipeValidatorPM<IRunecarvingRecipe> validator = new CategoryRecipeValidatorPM<>(category, 2, true);
        return getValidHandledRecipes(this.recipeCache, RecipeTypesPM.RUNECARVING.get(), validator);
    }
    
    public List<RecipeHolder<IDissolutionRecipe>> getDissolutionRecipes(IRecipeCategory<RecipeHolder<IDissolutionRecipe>> category) {
        CategoryRecipeValidatorPM<IDissolutionRecipe> validator = new CategoryRecipeValidatorPM<>(category, 1, true);
        return getValidHandledRecipes(this.recipeCache, RecipeTypesPM.DISSOLUTION.get(), validator);
    }
    
    public List<RecipeHolder<IRitualRecipe>> getRitualRecipes(IRecipeCategory<RecipeHolder<IRitualRecipe>> category) {
        CategoryRecipeValidatorPM<IRitualRecipe> validator = new CategoryRecipeValidatorPM<>(category, 100, true);  // TODO Fix max inputs for JEI rituals
        return getValidHandledRecipes(this.recipeCache, RecipeTypesPM.RITUAL.get(), validator);
    }
    
    private static <I extends RecipeInput, T extends Recipe<I>> List<RecipeHolder<T>> getValidHandledRecipes(ClientRecipeCache recipeCache, RecipeType<T> recipeType, CategoryRecipeValidatorPM<T> validator) {
        return recipeCache.byType(recipeType).stream().filter(r -> validator.isRecipeValid(r) && validator.isRecipeHandled(r)).toList();
    }
}
