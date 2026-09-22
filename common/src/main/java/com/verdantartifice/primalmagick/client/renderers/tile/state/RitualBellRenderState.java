package com.verdantartifice.primalmagick.client.renderers.tile.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public class RitualBellRenderState extends BlockEntityRenderState {
    public @Nullable Direction shakeDirection;
    public float ticks;
}
