package com.verdantartifice.primalmagick.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.verdantartifice.primalmagick.client.fx.FxDispatcher;
import com.verdantartifice.primalmagick.client.renderers.tile.state.SanguineCrucibleRenderState;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.common.tiles.devices.SanguineCrucibleTileEntity;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

/**
 * Custom tile entity renderer for sanguine crucible blocks.
 * 
 * @author Daedalus4096
 * @see {@link com.verdantartifice.primalmagick.common.blocks.devices.SanguineCrucibleBlock}
 */
public class SanguineCrucibleTER implements BlockEntityRenderer<SanguineCrucibleTileEntity, SanguineCrucibleRenderState> {
    protected static final SpriteId WATER_SPRITE = Sheets.BLOCKS_MAPPER.defaultNamespaceApply("water_still");
    protected static final Color COLOR = new Color(Sources.BLOOD.getColor()).brighter().brighter();
    protected static final float R = COLOR.getRed() / 255.0F;
    protected static final float G = COLOR.getGreen() / 255.0F;
    protected static final float B = COLOR.getBlue() / 255.0F;

    private final SpriteGetter sprites;

    public SanguineCrucibleTER(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
    }

    @Override
    public SanguineCrucibleRenderState createRenderState() {
        return new SanguineCrucibleRenderState();
    }

    @Override
    public void extractRenderState(SanguineCrucibleTileEntity tileEntityIn, SanguineCrucibleRenderState state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tileEntityIn, state, partialTicks, cameraPosition, breakProgress);
        state.fluidHeight = tileEntityIn.getFluidHeight();

        Level world = tileEntityIn.getLevel();
        RandomSource rand = world.getRandom();
        BlockPos pos = tileEntityIn.getBlockPos();
        if (tileEntityIn.showBubble(rand)) {
            double x = (double)pos.getX() + 0.2D + (rand.nextDouble() * 0.6D);
            double y = (double)pos.getY() + (double)state.fluidHeight;
            double z = (double)pos.getZ() + 0.2D + (rand.nextDouble() * 0.6D);
            FxDispatcher.INSTANCE.crucibleBubble(x, y, z, R, G, B);
        }
        if (rand.nextDouble() < tileEntityIn.getSmokeChance()) {
            double x = (double)pos.getX() + 0.1D + (rand.nextDouble() * 0.8D);
            double y = (double)pos.getY() + 1.0D;
            double z = (double)pos.getZ() + 0.1D + (rand.nextDouble() * 0.8D);
            world.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.1D, 0.0D);
        }
    }

    @Override
    public void submit(SanguineCrucibleRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        final TextureAtlasSprite sprite = this.sprites.get(WATER_SPRITE);

        poseStack.pushPose();
        poseStack.translate(0.0D, state.fluidHeight, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        submitNodeCollector.submitCustomGeometry(poseStack, Sheets.cutoutBlockSheet(), (pose, consumer) -> {
            consumer.addVertex(pose.pose(), 0.0F, 1.0F, 0.0F).setColor(R, G, B, 1.0F).setUv(sprite.getU0(), sprite.getV1())
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_SKY).setNormal(pose, 1, 0, 0);
            consumer.addVertex(pose.pose(), 1.0F, 1.0F, 0.0F).setColor(R, G, B, 1.0F).setUv(sprite.getU1(), sprite.getV1())
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_SKY).setNormal(pose, 1, 0, 0);
            consumer.addVertex(pose.pose(), 1.0F, 0.0F, 0.0F).setColor(R, G, B, 1.0F).setUv(sprite.getU1(), sprite.getV0())
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_SKY).setNormal(pose, 1, 0, 0);
            consumer.addVertex(pose.pose(), 0.0F, 0.0F, 0.0F).setColor(R, G, B, 1.0F).setUv(sprite.getU0(), sprite.getV0())
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_SKY).setNormal(pose, 1, 0, 0);
        });
        poseStack.popPose();
    }
}
