package com.verdantartifice.primalmagick.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.verdantartifice.primalmagick.client.renderers.tile.state.RitualBellRenderState;
import com.verdantartifice.primalmagick.common.tiles.rituals.RitualBellTileEntity;
import com.verdantartifice.primalmagick.common.util.ResourceUtils;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.bell.BellModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Custom tile entity renderer for ritual bell blocks.
 * 
 * @author Daedalus4096
 * @see {@link com.verdantartifice.primalmagick.common.blocks.rituals.RitualBellBlock}
 */
public class RitualBellTER implements BlockEntityRenderer<RitualBellTileEntity, RitualBellRenderState> {
    public static final Identifier TEXTURE = ResourceUtils.loc("entity/ritual_bell_body");
    public static final SpriteId BODY_SPRITE = Sheets.BLOCK_ENTITIES_MAPPER.apply(ResourceUtils.loc("ritual_bell_body"));

    private final SpriteGetter sprites;
    protected final BellModel model;

    public RitualBellTER(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
        this.model = new BellModel(context.bakeLayer(ModelLayers.BELL));
    }

    @Override
    public RitualBellRenderState createRenderState() {
        return new RitualBellRenderState();
    }

    @Override
    public void extractRenderState(RitualBellTileEntity tileEntityIn, RitualBellRenderState state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tileEntityIn, state, partialTicks, cameraPosition, breakProgress);
        state.ticks = (float)tileEntityIn.getRingingTicks() + partialTicks;
        state.shakeDirection = tileEntityIn.isRinging() ? tileEntityIn.getRingDirection() : null;
    }

    @Override
    public void submit(RitualBellRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        BellModel.State modelState = new BellModel.State(state.ticks, state.shakeDirection);
        submitNodeCollector.submitModel(this.model, modelState, poseStack, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, BODY_SPRITE, this.sprites, 0,
                state.breakProgress);
    }
}
