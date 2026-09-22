package com.verdantartifice.primalmagick.client.renderers.entity.layers;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.verdantartifice.primalmagick.client.fx.FxDispatcher;
import com.verdantartifice.primalmagick.client.renderers.entity.BasicPixieRenderer;
import com.verdantartifice.primalmagick.client.renderers.entity.GrandPixieRenderer;
import com.verdantartifice.primalmagick.client.renderers.entity.MajesticPixieRenderer;
import com.verdantartifice.primalmagick.client.renderers.entity.model.PixieHouseModel;
import com.verdantartifice.primalmagick.client.renderers.entity.model.PixieModel;
import com.verdantartifice.primalmagick.client.renderers.entity.state.PixieHouseRenderState;
import com.verdantartifice.primalmagick.client.renderers.models.ModelLayersPM;
import com.verdantartifice.primalmagick.common.entities.pixies.PixieRank;
import com.verdantartifice.primalmagick.common.items.misc.DrainedPixieItem;
import com.verdantartifice.primalmagick.common.items.misc.IPixieItem;
import com.verdantartifice.primalmagick.common.sources.Source;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PixieHouseOccupantLayer extends RenderLayer<PixieHouseRenderState, PixieHouseModel> {
    private static final Map<PixieRank, Identifier> TEXTURES = ImmutableMap.of(
            PixieRank.BASIC, BasicPixieRenderer.TEXTURE,
            PixieRank.GRAND, GrandPixieRenderer.TEXTURE,
            PixieRank.MAJESTIC, MajesticPixieRenderer.TEXTURE);

    private final PixieModel basePixieModel;
    private final PixieModel royalPixieModel;
    private final PixieModel baseDrainedPixieModel;
    private final PixieModel royalDrainedPixieModel;

    public PixieHouseOccupantLayer(RenderLayerParent<PixieHouseRenderState, PixieHouseModel> pRenderer, EntityModelSet pModelSet) {
        super(pRenderer);
        this.basePixieModel = new PixieModel(pModelSet.bakeLayer(ModelLayersPM.PIXIE_BASIC));
        this.royalPixieModel = new PixieModel(pModelSet.bakeLayer(ModelLayersPM.PIXIE_ROYAL));
        this.baseDrainedPixieModel = new PixieModel(pModelSet.bakeLayer(ModelLayersPM.PIXIE_BASIC));
        this.royalDrainedPixieModel = new PixieModel(pModelSet.bakeLayer(ModelLayersPM.PIXIE_ROYAL));
    }

    @Override
    public void submit(@NotNull PoseStack pPoseStack, @NotNull SubmitNodeCollector pCollector, int pPackedLight, @NotNull PixieHouseRenderState pRenderState, float pNetHeadYaw, float pHeadPitch) {
        ItemStack pixieStack = pRenderState.housedPixie;
        if (pixieStack.getItem() instanceof IPixieItem pixieItem && !pRenderState.pixieDeployed) {
            // Render pixie house occupant if present and not deployed
            PixieRank rank = pixieItem.getPixieRank();
            PixieModel model = rank == PixieRank.MAJESTIC ? this.royalPixieModel : this.basePixieModel;
            double yBob = -0.125D * Mth.sin(pRenderState.ageInTicks / 6F);
            pPoseStack.pushPose();
            pPoseStack.translate(0D, -0.25D + yBob, 0D);
            pPoseStack.scale(0.25F, 0.25F, 0.25F);
            pCollector.order(0).submitModel(model, null, pPoseStack, model.renderType(TEXTURES.get(rank)), pPackedLight, OverlayTexture.NO_OVERLAY, pRenderState.outlineColor, null);
            pPoseStack.popPose();

            // Render falling pixie dust
            RandomSource random = Minecraft.getInstance().level.getRandom();
            Source source = pixieItem.getPixieSource();
            double px = pRenderState.x + (random.nextGaussian() * 0.125D);
            double py = pRenderState.y + 1.5D - yBob;
            double pz = pRenderState.z + (random.nextGaussian() * 0.125D);
            FxDispatcher.INSTANCE.pixieDust(px, py, pz, source.getColor());
        } else if (pixieStack.getItem() instanceof DrainedPixieItem drainedPixieItem) {
            // Render pixie house occupant convalescing if drained
            PixieRank rank = drainedPixieItem.getPixieRank();
            PixieModel model = rank == PixieRank.MAJESTIC ? this.royalDrainedPixieModel : this.baseDrainedPixieModel;
            pPoseStack.pushPose();
            pPoseStack.translate(0D, 0.27D, -0.25D);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            pPoseStack.scale(0.25F, 0.25F, 0.25F);
            pCollector.order(0).submitModelPart(model.root(), pPoseStack, model.renderType(TEXTURES.get(rank)), pPackedLight, OverlayTexture.NO_OVERLAY, null, false, false, -1, null, pRenderState.outlineColor);
            pPoseStack.popPose();
        }
    }
}
