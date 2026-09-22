package com.verdantartifice.primalmagick.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.verdantartifice.primalmagick.client.fx.FxDispatcher;
import com.verdantartifice.primalmagick.client.renderers.tile.state.RitualAltarRenderState;
import com.verdantartifice.primalmagick.common.tiles.rituals.RitualAltarTileEntity;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

/**
 * Custom tile entity renderer for ritual altar tile entities.
 * 
 * @author Daedalus4096
 * @see {@link com.verdantartifice.primalmagick.common.tiles.rituals.RitualAltarTileEntity}
 */
public class RitualAltarTER implements BlockEntityRenderer<RitualAltarTileEntity, RitualAltarRenderState> {
    private final ItemModelResolver itemModelResolver;
    private final SpriteGetter sprites;

    public RitualAltarTER(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
        this.sprites = context.sprites();
    }

    protected void addVertex(VertexConsumer renderer, PoseStack.Pose pose, float x, float y, float z, float r, float g, float b, float a, float u, float v) {
        renderer.addVertex(pose.pose(), x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, 1, 0, 0);
    }

    protected void renderCube(VertexConsumer builder, PoseStack.Pose pose, float ds, float r, float g, float b, float a, TextureAtlasSprite sprite) {
        // Draw the south face of the cube
        this.addVertex(builder, pose, -ds, ds, ds, r, g, b, a, sprite.getU0(), sprite.getV1());
        this.addVertex(builder, pose, -ds, -ds, ds, r, g, b, a, sprite.getU0(), sprite.getV0());
        this.addVertex(builder, pose, ds, -ds, ds, r, g, b, a, sprite.getU1(), sprite.getV0());
        this.addVertex(builder, pose, ds, ds, ds, r, g, b, a, sprite.getU1(), sprite.getV1());

        // Draw the north face of the cube
        this.addVertex(builder, pose, -ds, ds, -ds, r, g, b, a, sprite.getU0(), sprite.getV1());
        this.addVertex(builder, pose, ds, ds, -ds, r, g, b, a, sprite.getU1(), sprite.getV1());
        this.addVertex(builder, pose, ds, -ds, -ds, r, g, b, a, sprite.getU1(), sprite.getV0());
        this.addVertex(builder, pose, -ds, -ds, -ds, r, g, b, a, sprite.getU0(), sprite.getV0());

        // Draw the east face of the cube
        this.addVertex(builder, pose, ds, ds, -ds, r, g, b, a, sprite.getU0(), sprite.getV1());
        this.addVertex(builder, pose, ds, ds, ds, r, g, b, a, sprite.getU1(), sprite.getV1());
        this.addVertex(builder, pose, ds, -ds, ds, r, g, b, a, sprite.getU1(), sprite.getV0());
        this.addVertex(builder, pose, ds, -ds, -ds, r, g, b, a, sprite.getU0(), sprite.getV0());

        // Draw the west face of the cube
        this.addVertex(builder, pose, -ds, -ds, ds, r, g, b, a, sprite.getU1(), sprite.getV0());
        this.addVertex(builder, pose, -ds, ds, ds, r, g, b, a, sprite.getU1(), sprite.getV1());
        this.addVertex(builder, pose, -ds, ds, -ds, r, g, b, a, sprite.getU0(), sprite.getV1());
        this.addVertex(builder, pose, -ds, -ds, -ds, r, g, b, a, sprite.getU0(), sprite.getV0());

        // Draw the top face of the cube
        this.addVertex(builder, pose, ds, ds, -ds, r, g, b, a, sprite.getU1(), sprite.getV0());
        this.addVertex(builder, pose, -ds, ds, -ds, r, g, b, a, sprite.getU0(), sprite.getV0());
        this.addVertex(builder, pose, -ds, ds, ds, r, g, b, a, sprite.getU0(), sprite.getV1());
        this.addVertex(builder, pose, ds, ds, ds, r, g, b, a, sprite.getU1(), sprite.getV1());

        // Draw the bottom face of the cube
        this.addVertex(builder, pose, ds, -ds, -ds, r, g, b, a, sprite.getU1(), sprite.getV0());
        this.addVertex(builder, pose, ds, -ds, ds, r, g, b, a, sprite.getU1(), sprite.getV1());
        this.addVertex(builder, pose, -ds, -ds, ds, r, g, b, a, sprite.getU0(), sprite.getV1());
        this.addVertex(builder, pose, -ds, -ds, -ds, r, g, b, a, sprite.getU0(), sprite.getV0());
    }
    
    @Override
    public RitualAltarRenderState createRenderState() {
        return new RitualAltarRenderState();
    }

    @Override
    public void extractRenderState(RitualAltarTileEntity tileEntityIn, RitualAltarRenderState state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tileEntityIn, state, partialTicks, cameraPosition, breakProgress);

        // Extract the held item stack above the altar
        ItemStack stack = tileEntityIn.getSyncedStack().copy();
        state.rotation = (int)(tileEntityIn.getLevel().getLevelData().getGameTime() % 360);
        this.itemModelResolver.updateForTopItem(state.item, stack, ItemDisplayContext.GUI, tileEntityIn.getLevel(), null, 0);

        // Extract the ritual orb above the altar if active
        state.active = tileEntityIn.isActive();
        if (state.active) {
            Color color = tileEntityIn.getOrbColor();
            state.orbColor = color.getRGB();
            state.ticks = (float)tileEntityIn.getActiveCount() + partialTicks;

            FxDispatcher.INSTANCE.ritualGlow(tileEntityIn.getBlockPos(), color.getRGB());
        }
    }

    @Override
    public void submit(RitualAltarRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        // Render the held item stack above the altar
        if (!state.item.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 1.5D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));   // Spin the stack around its Y-axis
            poseStack.scale(0.75F, 0.75F, 0.75F);
            state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        // Render the ritual orb above the altar if active
        if (state.active) {
            Color color = new Color(state.orbColor);
            final float r = color.getRed() / 255.0F;
            final float g = color.getGreen() / 255.0F;
            final float b = color.getBlue() / 255.0F;
            final float ds = 0.1875F;
            final TextureAtlasSprite sprite = this.sprites.get(ManaFontTER.CORE_SPRITE);

            poseStack.pushPose();
            poseStack.translate(0.5D, 2.5D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(Mth.sin(state.ticks * 0.1F) * 180.0F)); // Spin the orb like a shulker bullet
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.cos(state.ticks * 0.1F) * 180.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(state.ticks * 0.15F) * 360.0F));

            // FIXME Revert to translucent once Fabulous graphics bug is fixed
            submitNodeCollector.submitCustomGeometry(poseStack, Sheets.cutoutBlockSheet(),
                    (pose, consumer) -> this.renderCube(consumer, pose, ds, r, g, b, 1.0F, sprite));

            // FIXME Uncomment once Fabulous graphics bug is fixed
//            poseStack.scale(1.5F, 1.5F, 1.5F);
//            submitNodeCollector.submitCustomGeometry(poseStack, Sheets.cutoutBlockSheet(),
//                    (pose, consumer) -> this.renderCube(consumer, pose, ds, r, g, b, 0.5F, sprite));

            poseStack.popPose();
        }
    }
}
