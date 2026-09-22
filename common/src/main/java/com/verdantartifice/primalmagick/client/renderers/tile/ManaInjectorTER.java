package com.verdantartifice.primalmagick.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.verdantartifice.primalmagick.client.fx.FxDispatcher;
import com.verdantartifice.primalmagick.client.renderers.models.ModelLayersPM;
import com.verdantartifice.primalmagick.client.renderers.tile.model.ManaCubeModel;
import com.verdantartifice.primalmagick.client.renderers.tile.model.ManaInjectorFrameRingBottomMiddleModel;
import com.verdantartifice.primalmagick.client.renderers.tile.model.ManaInjectorFrameRingBottomModel;
import com.verdantartifice.primalmagick.client.renderers.tile.model.ManaInjectorFrameRingTopMiddleModel;
import com.verdantartifice.primalmagick.client.renderers.tile.model.ManaInjectorFrameRingTopModel;
import com.verdantartifice.primalmagick.client.renderers.tile.state.ManaInjectorRenderState;
import com.verdantartifice.primalmagick.common.misc.DeviceTier;
import com.verdantartifice.primalmagick.common.tiles.mana.ManaInjectorTileEntity;
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
import org.joml.Math;

public class ManaInjectorTER implements BlockEntityRenderer<ManaInjectorTileEntity, ManaInjectorRenderState> {
    public static final Identifier CORE_TEXTURE = ResourceUtils.loc("entity/mana_cube");
    private static final SpriteId CORE_SPRITE = Sheets.BLOCK_ENTITIES_MAPPER.apply(ResourceUtils.loc("mana_cube"));

    public static final Identifier BASIC_FRAME_TEXTURE = ResourceUtils.loc("entity/mana_injector/basic_frame_top");
    public static final Identifier ENCHANTED_FRAME_TEXTURE = ResourceUtils.loc("entity/mana_injector/enchanted_frame_top");
    public static final Identifier FORBIDDEN_FRAME_TEXTURE = ResourceUtils.loc("entity/mana_injector/forbidden_frame_top");
    public static final Identifier HEAVENLY_FRAME_TEXTURE = ResourceUtils.loc("entity/mana_injector/heavenly_frame_top");
    public static final Identifier BOTTOM_FRAME_TEXTURE = ResourceUtils.loc("entity/mana_injector/frame_bottom");

    private static final SpriteId BASIC_FRAME_SPRITE = Sheets.BLOCK_ENTITIES_MAPPER.apply(ResourceUtils.loc("mana_injector/basic_frame_top"));
    private static final SpriteId ENCHANTED_FRAME_SPRITE = Sheets.BLOCK_ENTITIES_MAPPER.apply(ResourceUtils.loc("mana_injector/enchanted_frame_top"));
    private static final SpriteId FORBIDDEN_FRAME_SPRITE = Sheets.BLOCK_ENTITIES_MAPPER.apply(ResourceUtils.loc("mana_injector/forbidden_frame_top"));
    private static final SpriteId HEAVENLY_FRAME_SPRITE = Sheets.BLOCK_ENTITIES_MAPPER.apply(ResourceUtils.loc("mana_injector/heavenly_frame_top"));
    private static final SpriteId BOTTOM_FRAME_SPRITE = Sheets.BLOCK_ENTITIES_MAPPER.apply(ResourceUtils.loc("mana_injector/frame_bottom"));

    private final SpriteGetter sprites;
    protected final ManaInjectorFrameRingTopModel ringTopModel;
    protected final ManaInjectorFrameRingTopMiddleModel ringTopMiddleModel;
    protected final ManaInjectorFrameRingBottomMiddleModel ringBottomMiddleModel;
    protected final ManaInjectorFrameRingBottomModel ringBottomModel;
    protected final ManaCubeModel manaCubeModel;

    public ManaInjectorTER(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
        this.ringTopModel = new ManaInjectorFrameRingTopModel(context.bakeLayer(ModelLayersPM.MANA_INJECTOR_FRAME_TOP));
        this.ringTopMiddleModel = new ManaInjectorFrameRingTopMiddleModel(context.bakeLayer(ModelLayersPM.MANA_INJECTOR_FRAME_TOP_MIDDLE));
        this.ringBottomMiddleModel = new ManaInjectorFrameRingBottomMiddleModel(context.bakeLayer(ModelLayersPM.MANA_INJECTOR_FRAME_BOTTOM_MIDDLE));
        this.ringBottomModel = new ManaInjectorFrameRingBottomModel(context.bakeLayer(ModelLayersPM.MANA_INJECTOR_FRAME_BOTTOM));
        this.manaCubeModel = new ManaCubeModel(context.bakeLayer(ModelLayersPM.MANA_CUBE));
    }

    protected static final int DIP_DURATION = 8;
    protected static final int CYCLE_DURATION = 40;

    @Override
    public ManaInjectorRenderState createRenderState() {
        return new ManaInjectorRenderState();
    }

    @Override
    public void extractRenderState(ManaInjectorTileEntity manaInjectorTileEntity, ManaInjectorRenderState state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(manaInjectorTileEntity, state, partialTicks, cameraPosition, breakProgress);
        long time = manaInjectorTileEntity.getLevel().getLevelData().getGameTime();
        state.tier = manaInjectorTileEntity.getDeviceTier();
        state.cycleTime = (time % CYCLE_DURATION) + (double)partialTicks;
        state.rotation = 2 * (int)(time % 360);
        state.coreColor = manaInjectorTileEntity.getCurrentColor(partialTicks);

        // Draw a particle stream rising from the core
        FxDispatcher.INSTANCE.spellcraftingGlow(manaInjectorTileEntity.getBlockPos(), 0.75D, state.coreColor);
    }

    @Override
    public void submit(ManaInjectorRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        final float tilt = 45.0F;

        // Render the injector's frame rings
        SpriteId topFrameSprite = getTopFrameSprite(state.tier);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0D, 0.5D);

        poseStack.pushPose();
        poseStack.translate(0D, 1.875D + this.getDipAmount(state.cycleTime, 0), 0D);
        submitNodeCollector.submitModelPart(this.ringTopModel.root(), poseStack, topFrameSprite.renderType(RenderTypes::entitySolid), state.lightCoords,
                OverlayTexture.NO_OVERLAY, this.sprites.get(topFrameSprite), false, false, -1, state.breakProgress, 0);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0D, 1.375D + this.getDipAmount(state.cycleTime, 4), 0D);
        submitNodeCollector.submitModelPart(this.ringTopMiddleModel.root(), poseStack, BOTTOM_FRAME_SPRITE.renderType(RenderTypes::entitySolid),
                state.lightCoords, OverlayTexture.NO_OVERLAY, this.sprites.get(BOTTOM_FRAME_SPRITE), false, false, -1, state.breakProgress, 0);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0D, 0.875D + this.getDipAmount(state.cycleTime, 8), 0D);
        submitNodeCollector.submitModelPart(this.ringBottomMiddleModel.root(), poseStack, BOTTOM_FRAME_SPRITE.renderType(RenderTypes::entitySolid),
                state.lightCoords, OverlayTexture.NO_OVERLAY, this.sprites.get(BOTTOM_FRAME_SPRITE), false, false, -1, state.breakProgress, 0);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0D, 0.375D + this.getDipAmount(state.cycleTime, 12), 0D);
        submitNodeCollector.submitModelPart(this.ringBottomModel.root(), poseStack, BOTTOM_FRAME_SPRITE.renderType(RenderTypes::entitySolid),
                state.lightCoords, OverlayTexture.NO_OVERLAY, this.sprites.get(BOTTOM_FRAME_SPRITE), false, false, -1, state.breakProgress, 0);
        poseStack.popPose();

        poseStack.popPose();

        // Render the relay's core
        final float coreScale = 0.1875F;
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.75D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));    // Spin the core around its Y-axis
        poseStack.mulPose(Axis.ZP.rotationDegrees(tilt));   // Tilt the core onto its diagonal
        poseStack.mulPose(Axis.XP.rotationDegrees(tilt));   // Tilt the core onto its diagonal
        poseStack.scale(coreScale, coreScale, coreScale);
        submitNodeCollector.submitModelPart(this.manaCubeModel.root(), poseStack, CORE_SPRITE.renderType(RenderTypes::entitySolid), state.lightCoords,
                OverlayTexture.NO_OVERLAY, this.sprites.get(CORE_SPRITE), false, false, state.coreColor, state.breakProgress, 0);
        poseStack.popPose();
    }

    protected double getDipAmount(double cycleTime, int dipStartTime) {
        if (cycleTime >= dipStartTime && cycleTime <= (dipStartTime + DIP_DURATION)) {
            return -0.125D * Math.sin((cycleTime - dipStartTime) * (Math.PI / (double)DIP_DURATION));
        } else {
            return 0D;
        }
    }

    protected static SpriteId getTopFrameSprite(DeviceTier tier) {
        return switch (tier) {
            case BASIC -> BASIC_FRAME_SPRITE;
            case ENCHANTED -> ENCHANTED_FRAME_SPRITE;
            case FORBIDDEN -> FORBIDDEN_FRAME_SPRITE;
            case HEAVENLY, CREATIVE -> HEAVENLY_FRAME_SPRITE;
        };
    }
}
