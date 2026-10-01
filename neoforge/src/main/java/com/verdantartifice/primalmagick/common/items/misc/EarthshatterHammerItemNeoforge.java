package com.verdantartifice.primalmagick.common.items.misc;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.Nullable;

/**
 * Item definition for an earthshatter hammer.  Can be crafted with ore to break it into grit for
 * ore doubling.  The hammer has no durability and is returned unchanged to the crafting grid.
 */
public class EarthshatterHammerItemNeoforge extends Item {
    public EarthshatterHammerItemNeoforge() {
        super(new Item.Properties().rarity(Rarity.UNCOMMON));
    }

    @Override
    public @Nullable ItemStackTemplate getCraftingRemainder(ItemInstance instance) {
        // Return the hammer as-is, keeping all of its components, rather than a fresh default stack
        return switch (instance) {
            case ItemStack stack -> ItemStackTemplate.fromNonEmptyStack(stack.copyWithCount(1));
            case ItemStackTemplate template -> template.withCount(1);
            default -> new ItemStackTemplate(this);
        };
    }
}
