package com.verdantartifice.primalmagick.common.capabilities;

import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * Neoforge view of a mod item handler, exposing what a menu slot needs to read and write it directly.
 */
public interface IItemHandlerNeoforge extends IItemHandlerPM {
    ResourceHandler<ItemResource> getResourceHandler();

    IndexModifier<ItemResource> getIndexModifier();
}
