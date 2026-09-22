package com.verdantartifice.primalmagick.client.renderers.tile.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class WindGeneratorRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public float rotation;
    public int coreColor;
}
