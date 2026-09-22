package com.verdantartifice.primalmagick.client.renderers.entity.state;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.ItemStack;

public class PixieHouseRenderState extends LivingEntityRenderState {
    public float wiggle;
    public ItemStack housedPixie = ItemStack.EMPTY;
    public boolean pixieDeployed;
}
