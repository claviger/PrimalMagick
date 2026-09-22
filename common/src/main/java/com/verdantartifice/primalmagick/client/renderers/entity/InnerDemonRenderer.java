package com.verdantartifice.primalmagick.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.verdantartifice.primalmagick.client.renderers.entity.layers.InnerDemonArmorLayer;
import com.verdantartifice.primalmagick.client.renderers.entity.state.InnerDemonRenderState;
import com.verdantartifice.primalmagick.common.entities.misc.InnerDemonEntity;
import com.verdantartifice.primalmagick.common.entities.misc.SinCrystalEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Entity renderer for an inner demon.
 * 
 * @author Daedalus4096
 */
public class InnerDemonRenderer extends MobRenderer<InnerDemonEntity, InnerDemonRenderState, PlayerModel> {
    protected static final float SCALE = 2.0F;
    
    protected final EntityRendererProvider.Context context;
    protected InnerDemonArmorLayer armorLayer;
    protected boolean modelFinalized = false;
    
    public InnerDemonRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F * SCALE);
        this.context = context;
        this.armorLayer = new InnerDemonArmorLayer(this, context.getModelSet(), false);
        this.addLayer(this.armorLayer);
    }

    @Override
    @NotNull
    public Identifier getTextureLocation(@NotNull InnerDemonRenderState renderState) {
        return renderState.skin.body().texturePath();
    }

    @Override
    protected void scale(@NotNull InnerDemonRenderState renderState, @NotNull PoseStack matrixStackIn) {
        if (!this.modelFinalized) {
            // Can't get the player's skin type at renderer registration time, so monkey-patch it after we're already going
            boolean slimModel = renderState.skin.model().equals(PlayerModelType.SLIM);

            this.model = new PlayerModel(this.context.bakeLayer(slimModel ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slimModel);

            this.layers.remove(this.armorLayer);
            this.armorLayer = new InnerDemonArmorLayer(this, this.context.getModelSet(), slimModel);
            this.addLayer(this.armorLayer);
            
            this.modelFinalized = true;
        }
        matrixStackIn.scale(SCALE, SCALE, SCALE);
    }

    @Override
    @NotNull
    public InnerDemonRenderState createRenderState() {
        return new InnerDemonRenderState();
    }

    @Override
    public void extractRenderState(@NotNull InnerDemonEntity entityIn, @NotNull InnerDemonRenderState renderState, float partialTicks) {
        super.extractRenderState(entityIn, renderState, partialTicks);
        HumanoidMobRenderer.extractHumanoidRenderState(entityIn, renderState, partialTicks, this.itemModelResolver);

        // Use the viewing player's skin
        renderState.skin = Minecraft.getInstance().player.getSkin();

        // Gather the beam endpoint for each in-range sin crystal
        renderState.crystalBeamOffsets.clear();
        for (SinCrystalEntity crystal : entityIn.getCrystalsInRange()) {
            float f6 = (float)(crystal.getX() - Mth.lerp((double)partialTicks, entityIn.xo, entityIn.getX()));
            float f8 = (float)(crystal.getY() - Mth.lerp((double)partialTicks, entityIn.yo, entityIn.getY()));
            float f9 = (float)(crystal.getZ() - Mth.lerp((double)partialTicks, entityIn.zo, entityIn.getZ()));
            renderState.crystalBeamOffsets.add(new Vec3(f6, f8 + EndCrystalRenderer.getY((float)crystal.innerRotation + partialTicks), f9));
        }
    }

    @Override
    public void submit(@NotNull InnerDemonRenderState renderState, @NotNull PoseStack matrixStackIn, @NotNull SubmitNodeCollector collector, @NotNull CameraRenderState cameraRenderState) {
        super.submit(renderState, matrixStackIn, collector, cameraRenderState);

        // Render beams for each in-range sin crystal
        for (Vec3 offset : renderState.crystalBeamOffsets) {
            matrixStackIn.pushPose();
            EnderDragonRenderer.submitCrystalBeams((float)offset.x, (float)offset.y, (float)offset.z, renderState.ageInTicks, matrixStackIn, collector, renderState.lightCoords);
            matrixStackIn.popPose();
        }
    }
}
