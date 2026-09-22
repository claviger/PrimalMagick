package com.verdantartifice.primalmagick.client.renderers.tile.state;

import com.verdantartifice.primalmagick.common.sources.Source;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import org.jetbrains.annotations.Nullable;

public class ManaFontRenderState extends BlockEntityRenderState {
    public @Nullable Source source;
    public int rotation;
    public float coreScale;
}
