package com.verdantartifice.primalmagick.mixin;

import com.verdantartifice.primalmagick.common.registries.RegistryIdContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to give block properties the registry key of the block being constructed, when the mod's registry service
 * has made one available, before the block constructor requires it.
 */
@Mixin(BlockBehaviour.Properties.class)
public abstract class BlockBehaviourPropertiesMixin {
    @Shadow
    private ResourceKey<Block> id;

    @Shadow
    public abstract BlockBehaviour.Properties setId(ResourceKey<Block> id);

    @Inject(method = "effectiveDrops", at = @At("HEAD"))
    private void onEffectiveDrops(CallbackInfoReturnable<?> cir) {
        this.applyCurrentKey();
    }

    @Inject(method = "effectiveDescriptionId", at = @At("HEAD"))
    private void onEffectiveDescriptionId(CallbackInfoReturnable<?> cir) {
        this.applyCurrentKey();
    }

    private void applyCurrentKey() {
        if (this.id == null) {
            ResourceKey<Block> key = RegistryIdContext.getCurrentBlockKey();
            if (key != null) {
                this.setId(key);
            }
        }
    }
}
