package com.verdantartifice.primalmagick.common.registries;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * Holds the registry key of the block or item currently being constructed by a platform registry service, so that
 * its properties can be given that key before the constructor requires it. Block and item properties must carry
 * their registry key, but the mod's registration pattern constructs the properties inside a supplier which has no
 * access to the key; the registry services set the key here around the supplier call and the properties mixins
 * read it back.
 */
public class RegistryIdContext {
    private static final ThreadLocal<ResourceKey<Block>> CURRENT_BLOCK_KEY = new ThreadLocal<>();
    private static final ThreadLocal<ResourceKey<Item>> CURRENT_ITEM_KEY = new ThreadLocal<>();

    public static ResourceKey<Block> getCurrentBlockKey() {
        return CURRENT_BLOCK_KEY.get();
    }

    public static ResourceKey<Item> getCurrentItemKey() {
        return CURRENT_ITEM_KEY.get();
    }

    public static <T> T withBlockKey(ResourceKey<Block> key, Supplier<T> supplier) {
        CURRENT_BLOCK_KEY.set(key);
        try {
            return supplier.get();
        } finally {
            CURRENT_BLOCK_KEY.remove();
        }
    }

    public static <T> T withItemKey(ResourceKey<Item> key, Supplier<T> supplier) {
        CURRENT_ITEM_KEY.set(key);
        try {
            return supplier.get();
        } finally {
            CURRENT_ITEM_KEY.remove();
        }
    }
}
