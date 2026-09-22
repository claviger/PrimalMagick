package com.verdantartifice.primalmagick.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.verdantartifice.primalmagick.client.renderers.tile.state.RitualLecternRenderState;
import com.verdantartifice.primalmagick.common.blocks.rituals.RitualLecternBlock;
import com.verdantartifice.primalmagick.common.tiles.rituals.RitualLecternTileEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Custom tile entity renderer for ritual lectern blocks.
 * 
 * @author Daedalus4096
 * @see {@link com.verdantartifice.primalmagick.common.blocks.rituals.RitualLecternBlock}
 */
public class RitualLecternTER implements BlockEntityRenderer<RitualLecternTileEntity, RitualLecternRenderState> {
    private static final BookModel.State BOOK_STATE = BookModel.State.forAnimation(0.0F, 0.1F, 0.9F, 1.2F);

    private final SpriteGetter sprites;
    protected final BookModel bookModel;

    public RitualLecternTER(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public RitualLecternRenderState createRenderState() {
        return new RitualLecternRenderState();
    }

    @Override
    public void extractRenderState(RitualLecternTileEntity tileEntityIn, RitualLecternRenderState state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tileEntityIn, state, partialTicks, cameraPosition, breakProgress);
        BlockState blockState = tileEntityIn.getBlockState();
        state.hasBook = blockState.getValue(RitualLecternBlock.HAS_BOOK);
        state.yRot = blockState.getValue(RitualLecternBlock.FACING).getClockWise().toYRot();
    }

    @Override
    public void submit(RitualLecternRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.hasBook) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 1.0625D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(-state.yRot));
            poseStack.mulPose(Axis.ZP.rotationDegrees(67.5F));
            poseStack.translate(0.0D, -0.125D, 0.0D);
            submitNodeCollector.submitModel(this.bookModel, BOOK_STATE, poseStack, state.lightCoords, OverlayTexture.NO_OVERLAY, -1,
                    EnchantTableRenderer.BOOK_TEXTURE, this.sprites, 0, state.breakProgress);
            poseStack.popPose();
        }
    }
}
