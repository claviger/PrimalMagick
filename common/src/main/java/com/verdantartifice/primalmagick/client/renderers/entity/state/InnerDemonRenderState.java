package com.verdantartifice.primalmagick.client.renderers.entity.state;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class InnerDemonRenderState extends AvatarRenderState {
    public final List<Vec3> crystalBeamOffsets = new ArrayList<>();
}
