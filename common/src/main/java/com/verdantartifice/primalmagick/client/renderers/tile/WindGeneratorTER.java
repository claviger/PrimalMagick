package com.verdantartifice.primalmagick.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.verdantartifice.primalmagick.client.renderers.tile.state.WindGeneratorRenderState;
import com.verdantartifice.primalmagick.common.blocks.devices.AbstractWindGeneratorBlock;
import com.verdantartifice.primalmagick.common.tiles.devices.WindGeneratorTileEntity;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

/**
 * Custom tile entity renderer for zephyr engine and void turbine blocks.
 * 
 * @author Daedalus4096
 */
public class WindGeneratorTER implements BlockEntityRenderer<WindGeneratorTileEntity, WindGeneratorRenderState> {
    private final SpriteGetter sprites;

    public WindGeneratorTER(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
    }

    protected void addVertex(VertexConsumer renderer, PoseStack.Pose pose, float x, float y, float z, float r, float g, float b, float u, float v) {
        renderer.addVertex(pose.pose(), x, y, z)
                .setColor(r, g, b, 1.0F)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, 1, 0, 0);
    }
    
    @Override
    public WindGeneratorRenderState createRenderState() {
        return new WindGeneratorRenderState();
    }

    @Override
    public void extractRenderState(WindGeneratorTileEntity tileEntityIn, WindGeneratorRenderState state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tileEntityIn, state, partialTicks, cameraPosition, breakProgress);
        BlockState blockState = tileEntityIn.getBlockState();
        boolean powered = blockState.getValue(AbstractWindGeneratorBlock.POWERED);
        long time = tileEntityIn.getLevel().getLevelData().getGameTime();
        state.facing = blockState.getValue(AbstractWindGeneratorBlock.FACING);
        state.rotation = ((int)(time % 360) + partialTicks) * (powered ? 30.0F : 1.0F);    // Spin the core faster if the block is powered

        // Color the tile entity core according to the block's source
        state.coreColor = (blockState.getBlock() instanceof AbstractWindGeneratorBlock windBlock) ? windBlock.getCoreColor() : Color.WHITE.getRGB();
    }

    @Override
    public void submit(WindGeneratorRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        Color sourceColor = new Color(state.coreColor);
        final float r = sourceColor.getRed() / 255.0F;
        final float g = sourceColor.getGreen() / 255.0F;
        final float b = sourceColor.getBlue() / 255.0F;
        final float ds = 0.1875F;
        final float scale = 0.9F;
        final TextureAtlasSprite sprite = this.sprites.get(ManaFontTER.CORE_SPRITE);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        if (!state.facing.getAxis().equals(Direction.Axis.Y)) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            if (state.facing.getAxis().equals(Direction.Axis.X)) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            }
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));   // Spin the core around its Y-axis
        poseStack.mulPose(Axis.ZP.rotationDegrees(45.0F)); // Tilt the core onto its diagonal
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F)); // Tilt the core onto its diagonal
        poseStack.scale(scale, scale, scale);

        // TODO Abstract into a model instead of plotting individual vertices
        submitNodeCollector.submitCustomGeometry(poseStack, Sheets.cutoutBlockSheet(), (pose, consumer) -> {
            // Draw the south face of the core
            this.addVertex(consumer, pose, -ds, ds, ds, r, g, b, sprite.getU0(), sprite.getV1());
            this.addVertex(consumer, pose, -ds, -ds, ds, r, g, b, sprite.getU0(), sprite.getV0());
            this.addVertex(consumer, pose, ds, -ds, ds, r, g, b, sprite.getU1(), sprite.getV0());
            this.addVertex(consumer, pose, ds, ds, ds, r, g, b, sprite.getU1(), sprite.getV1());

            // Draw the north face of the core
            this.addVertex(consumer, pose, -ds, ds, -ds, r, g, b, sprite.getU0(), sprite.getV1());
            this.addVertex(consumer, pose, ds, ds, -ds, r, g, b, sprite.getU1(), sprite.getV1());
            this.addVertex(consumer, pose, ds, -ds, -ds, r, g, b, sprite.getU1(), sprite.getV0());
            this.addVertex(consumer, pose, -ds, -ds, -ds, r, g, b, sprite.getU0(), sprite.getV0());

            // Draw the east face of the core
            this.addVertex(consumer, pose, ds, ds, -ds, r, g, b, sprite.getU0(), sprite.getV1());
            this.addVertex(consumer, pose, ds, ds, ds, r, g, b, sprite.getU1(), sprite.getV1());
            this.addVertex(consumer, pose, ds, -ds, ds, r, g, b, sprite.getU1(), sprite.getV0());
            this.addVertex(consumer, pose, ds, -ds, -ds, r, g, b, sprite.getU0(), sprite.getV0());

            // Draw the west face of the core
            this.addVertex(consumer, pose, -ds, -ds, ds, r, g, b, sprite.getU1(), sprite.getV0());
            this.addVertex(consumer, pose, -ds, ds, ds, r, g, b, sprite.getU1(), sprite.getV1());
            this.addVertex(consumer, pose, -ds, ds, -ds, r, g, b, sprite.getU0(), sprite.getV1());
            this.addVertex(consumer, pose, -ds, -ds, -ds, r, g, b, sprite.getU0(), sprite.getV0());

            // Draw the top face of the core
            this.addVertex(consumer, pose, ds, ds, -ds, r, g, b, sprite.getU1(), sprite.getV0());
            this.addVertex(consumer, pose, -ds, ds, -ds, r, g, b, sprite.getU0(), sprite.getV0());
            this.addVertex(consumer, pose, -ds, ds, ds, r, g, b, sprite.getU0(), sprite.getV1());
            this.addVertex(consumer, pose, ds, ds, ds, r, g, b, sprite.getU1(), sprite.getV1());

            // Draw the bottom face of the core
            this.addVertex(consumer, pose, ds, -ds, -ds, r, g, b, sprite.getU1(), sprite.getV0());
            this.addVertex(consumer, pose, ds, -ds, ds, r, g, b, sprite.getU1(), sprite.getV1());
            this.addVertex(consumer, pose, -ds, -ds, ds, r, g, b, sprite.getU0(), sprite.getV1());
            this.addVertex(consumer, pose, -ds, -ds, -ds, r, g, b, sprite.getU0(), sprite.getV0());
        });
        poseStack.popPose();
    }
}
