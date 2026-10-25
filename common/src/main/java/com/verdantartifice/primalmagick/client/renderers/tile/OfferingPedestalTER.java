package com.verdantartifice.primalmagick.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.verdantartifice.primalmagick.common.tiles.rituals.OfferingPedestalTileEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Custom tile entity renderer for offering pedestal blocks.
 * 
 * @author Daedalus4096
 * @see com.verdantartifice.primalmagick.common.blocks.rituals.OfferingPedestalBlock
 */
public class OfferingPedestalTER implements BlockEntityRenderer<OfferingPedestalTileEntity, OfferingPedestalTER.RenderState> {
    private final ItemModelResolver itemModelResolver;

    public OfferingPedestalTER(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    @NotNull
    public OfferingPedestalTER.RenderState createRenderState() {
        return new OfferingPedestalTER.RenderState();
    }

    @Override
    public void extractRenderState(@NotNull OfferingPedestalTileEntity blockEntity, @NotNull OfferingPedestalTER.RenderState state,
                                   float partialTicks, @NotNull Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        // The GUI display transform centers the item on the origin, so it spins in place about the pedestal's axis
        this.itemModelResolver.updateForTopItem(state.item, blockEntity.getSyncedStack().copy(), ItemDisplayContext.GUI, blockEntity.getLevel(), null, 0);
        state.stackRotation = (blockEntity.getLevel().getLevelData().getGameTime() % 360) + partialTicks;
    }

    @Override
    public void submit(@NotNull OfferingPedestalTER.RenderState renderState, @NotNull PoseStack poseStack,
                       @NotNull SubmitNodeCollector submitNodeCollector, @NotNull CameraRenderState cameraRenderState) {
        if (!renderState.item.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 1.5D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(renderState.stackRotation));  // Spin the stack around its Y-axis
            poseStack.scale(0.75F, 0.75F, 0.75F);
            renderState.item.submit(poseStack, submitNodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    public static class RenderState extends BlockEntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public float stackRotation;
    }
}
