package com.verdantartifice.primalmagick.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.verdantartifice.primalmagick.client.fx.FxDispatcher;
import com.verdantartifice.primalmagick.client.renderers.models.ModelLayersPM;
import com.verdantartifice.primalmagick.client.renderers.tile.model.ManaCubeModel;
import com.verdantartifice.primalmagick.client.renderers.tile.model.ManaRelayFrameModel;
import com.verdantartifice.primalmagick.client.renderers.tile.state.ManaRelayRenderState;
import com.verdantartifice.primalmagick.common.misc.DeviceTier;
import com.verdantartifice.primalmagick.common.tiles.mana.ManaRelayTileEntity;
import com.verdantartifice.primalmagick.common.util.ResourceUtils;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ManaRelayTER implements BlockEntityRenderer<ManaRelayTileEntity, ManaRelayRenderState> {
    public static final Identifier CORE_TEXTURE = ResourceUtils.loc("entity/mana_cube");
    private static final SpriteId CORE_SPRITE = Sheets.BLOCK_ENTITIES_MAPPER.apply(ResourceUtils.loc("mana_cube"));

    public static final Identifier BASIC_FRAME_TEXTURE = ResourceUtils.loc("entity/mana_relay/basic_frame");
    public static final Identifier ENCHANTED_FRAME_TEXTURE = ResourceUtils.loc("entity/mana_relay/enchanted_frame");
    public static final Identifier FORBIDDEN_FRAME_TEXTURE = ResourceUtils.loc("entity/mana_relay/forbidden_frame");
    public static final Identifier HEAVENLY_FRAME_TEXTURE = ResourceUtils.loc("entity/mana_relay/heavenly_frame");

    private static final SpriteId BASIC_FRAME_SPRITE = Sheets.BLOCK_ENTITIES_MAPPER.apply(ResourceUtils.loc("mana_relay/basic_frame"));
    private static final SpriteId ENCHANTED_FRAME_SPRITE = Sheets.BLOCK_ENTITIES_MAPPER.apply(ResourceUtils.loc("mana_relay/enchanted_frame"));
    private static final SpriteId FORBIDDEN_FRAME_SPRITE = Sheets.BLOCK_ENTITIES_MAPPER.apply(ResourceUtils.loc("mana_relay/forbidden_frame"));
    private static final SpriteId HEAVENLY_FRAME_SPRITE = Sheets.BLOCK_ENTITIES_MAPPER.apply(ResourceUtils.loc("mana_relay/heavenly_frame"));

    private final SpriteGetter sprites;
    protected final ManaRelayFrameModel frameModel;
    protected final ManaCubeModel manaCubeModel;

    public ManaRelayTER(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
        this.frameModel = new ManaRelayFrameModel(context.bakeLayer(ModelLayersPM.MANA_RELAY_FRAME));
        this.manaCubeModel = new ManaCubeModel(context.bakeLayer(ModelLayersPM.MANA_CUBE));
    }

    @Override
    public ManaRelayRenderState createRenderState() {
        return new ManaRelayRenderState();
    }

    @Override
    public void extractRenderState(ManaRelayTileEntity manaRelayTileEntity, ManaRelayRenderState state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(manaRelayTileEntity, state, partialTicks, cameraPosition, breakProgress);
        long time = manaRelayTileEntity.getLevel().getLevelData().getGameTime();
        state.tier = manaRelayTileEntity.getDeviceTier();
        state.bobDelta = 0.125D * Math.sin((time + (double)partialTicks) * (2D * Math.PI / (double)ManaRelayTileEntity.BOB_CYCLE_TIME_TICKS));
        state.rotation = 2 * (int)(time % 360);
        state.coreColor = manaRelayTileEntity.getCurrentColor(partialTicks);

        // Draw a particle stream rising from the core
        FxDispatcher.INSTANCE.spellcraftingGlow(manaRelayTileEntity.getBlockPos(), 0.5D + state.bobDelta, state.coreColor);
    }

    @Override
    public void submit(ManaRelayRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        final float baseScale = 0.5F;
        final float tilt = 45.0F;

        poseStack.pushPose();
        poseStack.translate(0D, state.bobDelta, 0D);

        // Render the relay's frame
        SpriteId frameSprite = getFrameSprite(state.tier);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.rotation));   // Spin the frame around its Y-axis
        poseStack.mulPose(Axis.ZP.rotationDegrees(tilt));   // Tilt the frame onto its diagonal
        poseStack.mulPose(Axis.XP.rotationDegrees(tilt));   // Tilt the frame onto its diagonal
        poseStack.scale(baseScale, baseScale, baseScale);
        submitNodeCollector.submitModelPart(this.frameModel.root(), poseStack, frameSprite.renderType(RenderTypes::entitySolid), state.lightCoords,
                OverlayTexture.NO_OVERLAY, this.sprites.get(frameSprite), false, false, -1, state.breakProgress, 0);
        poseStack.popPose();

        // Render the relay's core
        final float coreScale = 0.375F;
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));    // Spin the core around its Y-axis
        poseStack.mulPose(Axis.ZP.rotationDegrees(tilt));   // Tilt the core onto its diagonal
        poseStack.mulPose(Axis.XP.rotationDegrees(tilt));   // Tilt the core onto its diagonal
        poseStack.scale(baseScale, baseScale, baseScale);
        poseStack.scale(coreScale, coreScale, coreScale);
        submitNodeCollector.submitModelPart(this.manaCubeModel.root(), poseStack, CORE_SPRITE.renderType(RenderTypes::entitySolid), state.lightCoords,
                OverlayTexture.NO_OVERLAY, this.sprites.get(CORE_SPRITE), false, false, state.coreColor, state.breakProgress, 0);
        poseStack.popPose();

        poseStack.popPose();
    }

    protected static SpriteId getFrameSprite(DeviceTier tier) {
        return switch (tier) {
            case BASIC -> BASIC_FRAME_SPRITE;
            case ENCHANTED -> ENCHANTED_FRAME_SPRITE;
            case FORBIDDEN -> FORBIDDEN_FRAME_SPRITE;
            case HEAVENLY, CREATIVE -> HEAVENLY_FRAME_SPRITE;
        };
    }
}
