package com.verdantartifice.primalmagick.client.events;

import com.verdantartifice.primalmagick.client.recipes.ClientRecipeCache;
import net.minecraft.world.item.crafting.RecipeMap;

/**
 * Respond to client-side recipe sync events.
 */
public class ClientRecipeEvents {
    public static void onRecipesReceived(RecipeMap recipeMap) {
        // Replace the client's cached recipes with those just received from the server
        ClientRecipeCache.getInstance().setRecipes(recipeMap);
    }

    public static void onLoggingOut() {
        // Discard the cached recipes, as they belong to the connection being closed
        ClientRecipeCache.getInstance().clear();
    }
}
