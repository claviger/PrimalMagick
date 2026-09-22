package com.verdantartifice.primalmagick.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.verdantartifice.primalmagick.client.renderers.tile.state.SpinningItemRenderState;
import com.verdantartifice.primalmagick.common.tiles.rituals.OfferingPedestalTileEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Custom tile entity renderer for offering pedestal blocks.
 * 
 * @author Daedalus4096
 * @see {@link com.verdantartifice.primalmagick.common.blocks.rituals.OfferingPedestalBlock}
 */
public class OfferingPedestalTER implements BlockEntityRenderer<OfferingPedestalTileEntity, SpinningItemRenderState> {
    private final ItemModelResolver itemModelResolver;

    public OfferingPedestalTER(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public SpinningItemRenderState createRenderState() {
        return new SpinningItemRenderState();
    }

    @Override
    public void extractRenderState(OfferingPedestalTileEntity tileEntityIn, SpinningItemRenderState state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tileEntityIn, state, partialTicks, cameraPosition, breakProgress);
        ItemStack stack = tileEntityIn.getSyncedStack().copy();
        state.rotation = (int)(tileEntityIn.getLevel().getLevelData().getGameTime() % 360);
        this.itemModelResolver.updateForTopItem(state.item, stack, ItemDisplayContext.GUI, tileEntityIn.getLevel(), null, 0);
    }

    @Override
    public void submit(SpinningItemRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.item.isEmpty()) {
            // Render the held item stack above the pedestal
            poseStack.pushPose();
            poseStack.translate(0.5D, 1.5D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));   // Spin the stack around its Y-axis
            poseStack.scale(0.75F, 0.75F, 0.75F);
            state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}
