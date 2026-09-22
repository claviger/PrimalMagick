package com.verdantartifice.primalmagick.datagen.models;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.stream.Stream;

public class ModelProviderPMNeoforge extends AbstractModelProviderPM {
    // The parent class is in common code, which means the two-parameter constructor added by NF isn't
    // usable, and its modId field is final and so can't be set in this class's constructor. So, ignore
    // it and save our own instead, overriding all the methods that would look for it to use this field
    // instead.
    private final String modIdentifier;
    private static final java.util.Set<String> LOCAL_SKIP = java.util.Set.of("stripped_sunwood_log", "sunwood_log", "stripped_sunwood_wood", "sunwood_wood", "sunwood_leaves", "sunwood_planks", "sunwood_slab", "sunwood_stairs", "stripped_moonwood_log", "moonwood_log", "stripped_moonwood_wood", "moonwood_wood", "moonwood_leaves", "moonwood_planks", "moonwood_slab", "moonwood_stairs", "skyglass_pane", "salt_trail", "stained_skyglass_pane_black", "stained_skyglass_pane_blue", "stained_skyglass_pane_brown", "stained_skyglass_pane_cyan", "stained_skyglass_pane_gray", "stained_skyglass_pane_green", "stained_skyglass_pane_light_blue", "stained_skyglass_pane_light_gray", "stained_skyglass_pane_lime", "stained_skyglass_pane_magenta", "stained_skyglass_pane_orange", "stained_skyglass_pane_pink", "stained_skyglass_pane_purple", "stained_skyglass_pane_red", "stained_skyglass_pane_white", "stained_skyglass_pane_yellow");

    public ModelProviderPMNeoforge(PackOutput output, String modId) {
        super(output);
        this.modIdentifier = modId;
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        this.executeBlockModelGenerators(blockModels);
        this.executeItemModelGenerators(itemModels);
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return BuiltInRegistries.BLOCK.listElements().filter(holder -> holder.getKey().identifier().getNamespace().equals(this.modIdentifier) && !LOCAL_SKIP.contains(holder.getKey().identifier().getPath()));
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return BuiltInRegistries.ITEM.listElements().filter(holder -> holder.getKey().identifier().getNamespace().equals(this.modIdentifier) && !LOCAL_SKIP.contains(holder.getKey().identifier().getPath()));
    }

    @Override
    public String getName() {
        return "Model Definitions - " + this.modIdentifier;
    }

    @Override
    protected void registerClientItem(ItemModelGenerators itemModels, Identifier id, ItemModel.Unbaked model) {
        itemModels.itemModelOutput.register(id, new ClientItem(model, ClientItem.Properties.DEFAULT));
    }

    @Override
    public Identifier modLocation(String modelPath) {
        return Identifier.fromNamespaceAndPath(this.modIdentifier, modelPath);
    }
}
