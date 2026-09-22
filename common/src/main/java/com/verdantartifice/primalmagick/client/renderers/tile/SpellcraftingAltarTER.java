package com.verdantartifice.primalmagick.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.verdantartifice.primalmagick.client.fx.FxDispatcher;
import com.verdantartifice.primalmagick.client.renderers.models.ModelLayersPM;
import com.verdantartifice.primalmagick.client.renderers.tile.model.SpellcraftingAltarRingModel;
import com.verdantartifice.primalmagick.client.renderers.tile.state.SpellcraftingAltarRenderState;
import com.verdantartifice.primalmagick.common.blocks.crafting.SpellcraftingAltarBlock;
import com.verdantartifice.primalmagick.common.tiles.crafting.SpellcraftingAltarTileEntity;
import com.verdantartifice.primalmagick.common.util.ResourceUtils;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

/**
 * Custom tile entity renderer for spellcrafting altar blocks.
 * 
 * @author Daedalus4096
 */
public class SpellcraftingAltarTER implements BlockEntityRenderer<SpellcraftingAltarTileEntity, SpellcraftingAltarRenderState> {
    public static final Identifier RING_TEXTURE = ResourceUtils.loc("entity/spellcrafting_altar/spellcrafting_altar_ring");
    public static final SpriteId RING_SPRITE = Sheets.BLOCK_ENTITIES_MAPPER.apply(ResourceUtils.loc("spellcrafting_altar/spellcrafting_altar_ring"));

    private final SpriteGetter sprites;
    protected final SpellcraftingAltarRingModel ringModel;
    
    public SpellcraftingAltarTER(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
        this.ringModel = new SpellcraftingAltarRingModel(context.bakeLayer(ModelLayersPM.SPELLCRAFTING_ALTAR_RING));
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
    public SpellcraftingAltarRenderState createRenderState() {
        return new SpellcraftingAltarRenderState();
    }

    @Override
    public void extractRenderState(SpellcraftingAltarTileEntity tileEntityIn, SpellcraftingAltarRenderState state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tileEntityIn, state, partialTicks, cameraPosition, breakProgress);
        BlockState blockState = tileEntityIn.getBlockState();
        long time = tileEntityIn.getLevel().getLevelData().getGameTime();
        state.bobDelta = 0.125D * Math.sin((time + (double)partialTicks) * (2D * Math.PI / (double)SpellcraftingAltarTileEntity.BOB_CYCLE_TIME_TICKS));
        state.facingAngle = blockState.getValue(SpellcraftingAltarBlock.FACING).getClockWise().toYRot();
        state.ringRotation = tileEntityIn.getCurrentRotation(partialTicks);

        // Color the tile entity core according to the block's source
        Color sourceColor = tileEntityIn.getCurrentColor(partialTicks);
        state.coreColor = sourceColor.getRGB();
        state.rotation = (int)(time % 360);

        // Draw a particle stream rising from the core
        FxDispatcher.INSTANCE.spellcraftingGlow(tileEntityIn.getBlockPos(), 1.125D, sourceColor.getRed() / 255.0F, sourceColor.getGreen() / 255.0F,
                sourceColor.getBlue() / 255.0F);
    }

    @Override
    public void submit(SpellcraftingAltarRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        // Render the altar's ring
        poseStack.pushPose();
        poseStack.translate(0.5D, 0D, 0.5D);
        poseStack.translate(0D, 2.5D, 0D);    // Model position correction
        poseStack.translate(0D, state.bobDelta, 0D);    // Bob the ring up and down
        poseStack.mulPose(Axis.ZP.rotationDegrees(180F));
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.facingAngle));
        poseStack.mulPose(Axis.YP.rotationDegrees(90F));  // Model rotation correction
        poseStack.mulPose(Axis.YP.rotationDegrees(state.ringRotation));    // Spin the ring according to tile control
        submitNodeCollector.submitModelPart(this.ringModel.root(), poseStack, RING_SPRITE.renderType(RenderTypes::entitySolid), state.lightCoords,
                OverlayTexture.NO_OVERLAY, this.sprites.get(RING_SPRITE), false, false, -1, state.breakProgress, 0);
        poseStack.popPose();

        Color sourceColor = new Color(state.coreColor);
        final float r = sourceColor.getRed() / 255.0F;
        final float g = sourceColor.getGreen() / 255.0F;
        final float b = sourceColor.getBlue() / 255.0F;
        final float ds = 0.1875F;
        final float scale = 0.5F;
        final TextureAtlasSprite sprite = this.sprites.get(ManaFontTER.CORE_SPRITE);

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.125D, 0.5D);
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
