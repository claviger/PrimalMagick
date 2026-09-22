package com.verdantartifice.primalmagick.mixin;

import com.verdantartifice.primalmagick.common.registries.RegistryIdContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to give item properties the registry key of the item being constructed, when the mod's registry service
 * has made one available, before the item constructor requires it.
 */
@Mixin(Item.Properties.class)
public abstract class ItemPropertiesMixin {
    @Shadow
    private ResourceKey<Item> id;

    @Shadow
    public abstract Item.Properties setId(ResourceKey<Item> id);

    @Inject(method = "itemIdOrThrow", at = @At("HEAD"))
    private void onItemIdOrThrow(CallbackInfoReturnable<ResourceKey<Item>> cir) {
        if (this.id == null) {
            ResourceKey<Item> key = RegistryIdContext.getCurrentItemKey();
            if (key != null) {
                this.setId(key);
            }
        }
    }
}
