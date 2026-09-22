package com.verdantartifice.primalmagick.datagen.models;

import com.verdantartifice.primalmagick.common.blocks.trees.IPhasingBlock;
import com.verdantartifice.primalmagick.common.blockstates.properties.TimePhase;
import com.verdantartifice.primalmagick.platform.Services;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PhasingWoodProvider {
    private final PhasingTextureMapping logMapping;
    private final BlockModelGenerators blockModelGenerators;

    public PhasingWoodProvider(PhasingTextureMapping logMapping, BlockModelGenerators blockModelGenerators) {
        this.logMapping = logMapping;
        this.blockModelGenerators = blockModelGenerators;
    }

    public PhasingWoodProvider wood(Block logBlock) {
        PhasingTextureMapping woodMapping = this.logMapping.copyAndUpdate(TextureSlot.SIDE, TextureSlot.END);
        Map<TimePhase, MultiVariant> variants = new HashMap<>();
        Arrays.stream(TimePhase.values()).forEach(phase -> {
            Identifier modelId = Services.MODEL_TEMPLATES.extend(ModelTemplates.CUBE_COLUMN)
                    .createWithSuffix(logBlock, "_" + phase, woodMapping.resolve(phase), this.blockModelGenerators.modelOutput);
            variants.put(phase, BlockModelGenerators.plainVariant(modelId));
            if (phase == TimePhase.FULL) {
                this.blockModelGenerators.registerSimpleItemModel(logBlock, modelId);
            }
        });
        this.blockModelGenerators.blockStateOutput.accept(MultiVariantGenerator.dispatch(logBlock)
                .with(PropertyDispatch.initial(IPhasingBlock.PHASE).generate(variants::get))
                .with(PropertyDispatch.modify(BlockStateProperties.AXIS)
                        .select(Direction.Axis.Y, BlockModelGenerators.NOP)
                        .select(Direction.Axis.Z, BlockModelGenerators.X_ROT_90)
                        .select(Direction.Axis.X, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90))));
        return this;
    }

    public PhasingWoodProvider logWithHorizontal(Block logBlock) {
        Map<TimePhase, MultiVariant> verticalVariants = new HashMap<>();
        Map<TimePhase, MultiVariant> horizontalVariants = new HashMap<>();
        Arrays.stream(TimePhase.values()).forEach(phase -> {
            Identifier verticalModelId = Services.MODEL_TEMPLATES.extend(ModelTemplates.CUBE_COLUMN)
                    .createWithSuffix(logBlock, "_" + phase, this.logMapping.resolve(phase), this.blockModelGenerators.modelOutput);
            Identifier horizontalModelId = Services.MODEL_TEMPLATES.extend(ModelTemplates.CUBE_COLUMN_HORIZONTAL)
                    .createWithSuffix(logBlock, "_" + phase, this.logMapping.resolve(phase), this.blockModelGenerators.modelOutput);
            verticalVariants.put(phase, BlockModelGenerators.plainVariant(verticalModelId));
            horizontalVariants.put(phase, BlockModelGenerators.plainVariant(horizontalModelId));
            if (phase == TimePhase.FULL) {
                this.blockModelGenerators.registerSimpleItemModel(logBlock, verticalModelId);
            }
        });
        this.blockModelGenerators.blockStateOutput.accept(MultiVariantGenerator.dispatch(logBlock)
                .with(PropertyDispatch.initial(IPhasingBlock.PHASE, BlockStateProperties.AXIS).generate((phase, axis) -> switch (axis) {
                    case Y -> verticalVariants.get(phase);
                    case Z -> horizontalVariants.get(phase).with(BlockModelGenerators.X_ROT_90);
                    case X -> horizontalVariants.get(phase).with(BlockModelGenerators.X_ROT_90).with(BlockModelGenerators.Y_ROT_90);
                })));
        return this;
    }
}
