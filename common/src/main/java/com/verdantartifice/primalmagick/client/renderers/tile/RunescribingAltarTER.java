package com.verdantartifice.primalmagick.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.tiles.crafting.RunescribingAltarTileEntity;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Custom tile entity renderer for runescribing altar blocks.
 * 
 * @author Daedalus4096
 * @see com.verdantartifice.primalmagick.common.blocks.crafting.RunescribingAltarBlock
 */
public class RunescribingAltarTER implements BlockEntityRenderer<RunescribingAltarTileEntity, RunescribingAltarTER.RenderState> {
    protected ItemStack runeStack = null;
    private final ItemModelResolver itemModelResolver;
    
    public RunescribingAltarTER(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    @NotNull
    public RunescribingAltarTER.RenderState createRenderState() {
        return new RunescribingAltarTER.RenderState();
    }

    @Override
    public void extractRenderState(@NotNull RunescribingAltarTileEntity blockEntity, @NotNull RunescribingAltarTER.RenderState state, float partialTicks, @NotNull Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.stackRotation = blockEntity.getLevel() == null ? 0 : (blockEntity.getLevel().getLevelData().getGameTime() % 360) + partialTicks;
        // The GUI display transform centers the rune on the origin, so it spins in place about the altar's axis
        this.itemModelResolver.updateForTopItem(state.item, this.getRuneStack(), ItemDisplayContext.GUI, blockEntity.getLevel(), null, 0);
    }

    @Override
    public void submit(@NotNull RunescribingAltarTER.RenderState renderState, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector, @NotNull CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 1.1D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(renderState.stackRotation));   // Spin the stack around its Y-axis
        poseStack.scale(0.75F, 0.75F, 0.75F);
        renderState.item.submit(poseStack, submitNodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    protected ItemStack getRuneStack() {
        if (this.runeStack == null) {
            this.runeStack = new ItemStack(ItemsPM.RUNE_UNATTUNED.get());
        }
        return this.runeStack;
    }

    public static class RenderState extends BlockEntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public float stackRotation;
    }
}
