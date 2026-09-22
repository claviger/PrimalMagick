package com.verdantartifice.primalmagick.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.verdantartifice.primalmagick.client.renderers.tile.state.ManaFontRenderState;
import com.verdantartifice.primalmagick.common.blocks.mana.AbstractManaFontBlock;
import com.verdantartifice.primalmagick.common.tiles.mana.AbstractManaFontTileEntity;
import com.verdantartifice.primalmagick.common.util.ResourceUtils;
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
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

/**
 * Custom tile entity renderer for mana font blocks.
 * 
 * @author Daedalus4096
 * @see {@link com.verdantartifice.primalmagick.common.blocks.mana.AncientManaFontBlock}
 */
public class ManaFontTER implements BlockEntityRenderer<AbstractManaFontTileEntity, ManaFontRenderState> {
    public static final Identifier TEXTURE = ResourceUtils.loc("entity/mana_font_core");
    public static final SpriteId CORE_SPRITE = Sheets.BLOCK_ENTITIES_MAPPER.apply(ResourceUtils.loc("mana_font_core"));

    private final SpriteGetter sprites;

    public ManaFontTER(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
    }

    protected void addVertex(VertexConsumer renderer, PoseStack.Pose pose, float x, float y, float z, float r, float g, float b, float u, float v) {
        renderer.addVertex(pose.pose(), x, y, z)
                .setColor(r, g, b, 1.0F)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)  // The core always glows, regardless of ambient light
                .setNormal(pose, 1, 0, 0);
    }
    
    @Override
    public ManaFontRenderState createRenderState() {
        return new ManaFontRenderState();
    }

    @Override
    public void extractRenderState(AbstractManaFontTileEntity tileEntityIn, ManaFontRenderState state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tileEntityIn, state, partialTicks, cameraPosition, breakProgress);
        Block block = tileEntityIn.getBlockState().getBlock();
        state.source = (block instanceof AbstractManaFontBlock fontBlock) ? fontBlock.getSource() : null;
        state.rotation = (int)(tileEntityIn.getLevel().getLevelData().getGameTime() % 360);
        state.coreScale = (float)tileEntityIn.getMana() / Math.max(2000.0F, (float)tileEntityIn.getManaCapacity());    // Shrink the core as it holds less mana
    }

    @Override
    public void submit(ManaFontRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.source == null) {
            return;
        }

        // Color the tile entity core according to the block's source
        Color sourceColor = new Color(state.source.getColor());
        final float r = sourceColor.getRed() / 255.0F;
        final float g = sourceColor.getGreen() / 255.0F;
        final float b = sourceColor.getBlue() / 255.0F;
        final float ds = 0.1875F;
        final TextureAtlasSprite sprite = this.sprites.get(CORE_SPRITE);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));   // Spin the core around its Y-axis
        poseStack.mulPose(Axis.ZP.rotationDegrees(45.0F)); // Tilt the core onto its diagonal
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F)); // Tilt the core onto its diagonal
        poseStack.scale(state.coreScale, state.coreScale, state.coreScale);
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
