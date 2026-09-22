package com.verdantartifice.primalmagick.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.verdantartifice.primalmagick.common.entities.misc.SinCrystalEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.crystal.EndCrystalModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class SinCrystalRenderer extends EntityRenderer<SinCrystalEntity, EndCrystalRenderState> {
    protected static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/end_crystal/end_crystal.png");
    
    protected final EndCrystalModel model;

    public SinCrystalRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.model = new EndCrystalModel(context.bakeLayer(ModelLayers.END_CRYSTAL));
    }

    @Override
    public boolean shouldRender(SinCrystalEntity livingEntityIn, Frustum camera, double camX, double camY, double camZ) {
        return super.shouldRender(livingEntityIn, camera, camX, camY, camZ) || livingEntityIn.getBeamTarget() != null;
    }

    @Override
    @NotNull
    public EndCrystalRenderState createRenderState() {
        return new EndCrystalRenderState();
    }

    @Override
    public void extractRenderState(@NotNull SinCrystalEntity entityIn, @NotNull EndCrystalRenderState renderState, float partialTicks) {
        super.extractRenderState(entityIn, renderState, partialTicks);
        renderState.ageInTicks = (float)entityIn.innerRotation + partialTicks;
        renderState.showsBottom = false;
        renderState.beamOffset = entityIn.getBeamTarget().map(pos -> Vec3.atCenterOf(pos).subtract(entityIn.getPosition(partialTicks))).orElse(null);
    }

    @Override
    public void submit(@NotNull EndCrystalRenderState renderState, @NotNull PoseStack matrixStackIn, @NotNull SubmitNodeCollector collector, @NotNull CameraRenderState cameraRenderState) {
        matrixStackIn.pushPose();
        matrixStackIn.scale(2.0F, 2.0F, 2.0F);
        matrixStackIn.translate(0.0F, -0.5F, 0.0F);
        collector.submitModel(this.model, renderState, matrixStackIn, TEXTURE, renderState.lightCoords, OverlayTexture.NO_OVERLAY, renderState.outlineColor, null);
        matrixStackIn.popPose();
        Vec3 beamOffset = renderState.beamOffset;
        if (beamOffset != null) {
            float deltaY = EndCrystalRenderer.getY(renderState.ageInTicks);
            float dx = (float)beamOffset.x;
            float dy = (float)beamOffset.y;
            float dz = (float)beamOffset.z;
            matrixStackIn.translate(beamOffset);
            EnderDragonRenderer.submitCrystalBeams(-dx, -dy + deltaY, -dz, renderState.ageInTicks, matrixStackIn, collector, renderState.lightCoords);
        }

        super.submit(renderState, matrixStackIn, collector, cameraRenderState);
    }
}
