package com.verdantartifice.primalmagick.client.renderers.tile.state;

import com.verdantartifice.primalmagick.common.misc.DeviceTier;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class ManaRelayRenderState extends BlockEntityRenderState {
    public DeviceTier tier = DeviceTier.BASIC;
    public double bobDelta;
    public int rotation;
    public int coreColor;
}
