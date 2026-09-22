package com.verdantartifice.primalmagick.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.verdantartifice.primalmagick.client.renderers.entity.model.SpellProjectileModel;
import com.verdantartifice.primalmagick.client.renderers.entity.state.SpellProjectileRenderState;
import com.verdantartifice.primalmagick.client.renderers.models.ModelLayersPM;
import com.verdantartifice.primalmagick.common.entities.projectiles.SinCrashEntity;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.common.util.ResourceUtils;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer for a sin crash projectile.  Looks just like a void spell projectile.
 * 
 * @author Daedalus4096
 */
public class SinCrashRenderer extends EntityRenderer<SinCrashEntity, SpellProjectileRenderState> {
    protected static final Identifier TEXTURE = ResourceUtils.loc("textures/entity/spell_projectile.png");
    protected static final RenderType TRANSLUCENT_TYPE = RenderTypes.entityTranslucent(TEXTURE);

    protected final SpellProjectileModel model;

    public SinCrashRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SpellProjectileModel(context.bakeLayer(ModelLayersPM.SPELL_PROJECTILE));
    }

    @Override
    protected int getBlockLightLevel(SinCrashEntity entityIn, BlockPos blockPos) {
        return 15;
    }
    
    @Override
    @NotNull
    public SpellProjectileRenderState createRenderState() {
        return new SpellProjectileRenderState();
    }

    @Override
    public void extractRenderState(@NotNull SinCrashEntity entity, @NotNull SpellProjectileRenderState renderState, float partialTicks) {
        super.extractRenderState(entity, renderState, partialTicks);
        renderState.yRot = entity.getYRot(partialTicks);
        renderState.xRot = entity.getXRot(partialTicks);
        renderState.color = Sources.VOID.getColor();
    }

    @Override
    public void submit(@NotNull SpellProjectileRenderState renderState, @NotNull PoseStack matrixStack, @NotNull SubmitNodeCollector collector, @NotNull CameraRenderState cameraRenderState) {
        float ticks = renderState.ageInTicks;
        int coreColor = ARGB.color(ARGB.as8BitChannel(1.0F), renderState.color);
        int glowColor = ARGB.color(ARGB.as8BitChannel(0.5F), renderState.color);
        matrixStack.pushPose();
        matrixStack.translate(0.0D, 0.15D, 0.0D);
        matrixStack.mulPose(Axis.YP.rotationDegrees(Mth.sin(ticks * 0.1F) * 180.0F)); // Spin the projectile like a shulker bullet
        matrixStack.mulPose(Axis.XP.rotationDegrees(Mth.cos(ticks * 0.1F) * 180.0F));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(ticks * 0.15F) * 360.0F));
        matrixStack.scale(-0.5F, -0.5F, 0.5F);
        // Render the core of the projectile
        collector.order(0).submitModel(this.model, renderState, matrixStack, this.model.renderType(TEXTURE), renderState.lightCoords, OverlayTexture.NO_OVERLAY, coreColor, null, renderState.outlineColor, null);
        matrixStack.scale(1.5F, 1.5F, 1.5F);
        // Render the transparent glow of the projectile
        collector.order(1).submitModel(this.model, renderState, matrixStack, TRANSLUCENT_TYPE, renderState.lightCoords, OverlayTexture.NO_OVERLAY, glowColor, null, renderState.outlineColor, null);
        matrixStack.popPose();
        super.submit(renderState, matrixStack, collector, cameraRenderState);
    }
}
