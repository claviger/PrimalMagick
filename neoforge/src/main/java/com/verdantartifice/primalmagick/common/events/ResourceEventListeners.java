package com.verdantartifice.primalmagick.common.events;

import com.verdantartifice.primalmagick.Constants;
import com.verdantartifice.primalmagick.common.affinities.AffinityManager;
import com.verdantartifice.primalmagick.common.books.grids.GridDefinitionLoader;
import com.verdantartifice.primalmagick.common.crafting.RecipeTypesPM;
import com.verdantartifice.primalmagick.common.util.ResourceUtils;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class ResourceEventListeners {
    @SubscribeEvent
    public static void onResourceReload(AddServerReloadListenersEvent event) {
        event.addListener(ResourceUtils.loc("affinities"), AffinityManager.getOrCreateInstance());
        event.addListener(ResourceUtils.loc("linguistics_grids"), GridDefinitionLoader.getOrCreateInstance());
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        // Ask that the recipes needed by the mod's client-side code be sent to the joining or reloading players
        event.sendRecipes(RecipeTypesPM.ARCANE_CRAFTING.get(), RecipeTypesPM.CONCOCTING.get(), RecipeTypesPM.DISSOLUTION.get(),
                RecipeTypesPM.RITUAL.get(), RecipeTypesPM.RUNECARVING.get(), RecipeType.BLASTING, RecipeType.CAMPFIRE_COOKING,
                RecipeType.CRAFTING, RecipeType.SMELTING, RecipeType.SMITHING, RecipeType.SMOKING, RecipeType.STONECUTTING);
    }
}
