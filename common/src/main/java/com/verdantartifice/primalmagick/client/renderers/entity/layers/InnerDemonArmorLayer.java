package com.verdantartifice.primalmagick.client.renderers.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.verdantartifice.primalmagick.client.renderers.entity.state.InnerDemonRenderState;
import com.verdantartifice.primalmagick.common.util.ResourceUtils;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * Layer renderer for the energy field surrounding an inner demon.
 * 
 * @author Daedalus4096
 */
public class InnerDemonArmorLayer extends RenderLayer<InnerDemonRenderState, PlayerModel> {
    protected static final Identifier TEXTURE = ResourceUtils.loc("textures/entity/inner_demon/inner_demon_armor.png");
    protected final PlayerModel model;

    public InnerDemonArmorLayer(RenderLayerParent<InnerDemonRenderState, PlayerModel> renderer, EntityModelSet modelSet, boolean slimModel) {
        super(renderer);
        this.model = new PlayerModel(modelSet.bakeLayer(slimModel ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slimModel);
    }

    @Override
    public void submit(@NotNull PoseStack poseStack, @NotNull SubmitNodeCollector collector, int lightCoords, @NotNull InnerDemonRenderState renderState, float yRot, float xRot) {
        float ticks = renderState.ageInTicks;
        collector.order(1).submitModel(this.model, renderState, poseStack, RenderTypes.energySwirl(TEXTURE, this.xOffset(ticks) % 1.0F, ticks * 0.01F % 1.0F), lightCoords, OverlayTexture.NO_OVERLAY, -8355712, null, renderState.outlineColor, null);
    }

    protected float xOffset(float p_225634_1_) {
        return p_225634_1_ * 0.01F;
    }
}
