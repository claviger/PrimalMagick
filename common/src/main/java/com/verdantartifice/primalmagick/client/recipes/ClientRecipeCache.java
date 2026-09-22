package com.verdantartifice.primalmagick.client.recipes;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

/**
 * Client-side cache of the recipes sent by the server. The client no longer receives the server's
 * full recipe list, so the server pushes the recipe types that the mod's client-side code needs and
 * they are held here for the lifetime of the connection.
 */
public class ClientRecipeCache {
    private static final ClientRecipeCache INSTANCE = new ClientRecipeCache();

    private RecipeMap recipes = RecipeMap.EMPTY;

    private ClientRecipeCache() {}

    public static ClientRecipeCache getInstance() {
        return INSTANCE;
    }

    /**
     * Replaces the cached recipes with those most recently received from the server.
     *
     * @param recipes the recipes received from the server
     */
    public void setRecipes(@NotNull RecipeMap recipes) {
        this.recipes = recipes;
    }

    /**
     * Discards all cached recipes, such as when the player leaves the level.
     */
    public void clear() {
        this.recipes = RecipeMap.EMPTY;
    }

    @NotNull
    public Collection<RecipeHolder<?>> getRecipes() {
        return this.recipes.values();
    }

    @NotNull
    public Optional<RecipeHolder<?>> byKey(@NotNull ResourceKey<Recipe<?>> recipeKey) {
        return Optional.ofNullable(this.recipes.byKey(recipeKey));
    }

    @NotNull
    public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> byType(@NotNull RecipeType<T> recipeType) {
        return this.recipes.byType(recipeType);
    }
}
