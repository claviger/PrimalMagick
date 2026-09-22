package com.verdantartifice.primalmagick.datagen.models;

import com.google.common.collect.ImmutableMap;
import com.verdantartifice.primalmagick.common.blocks.trees.IPhasingBlock;
import com.verdantartifice.primalmagick.common.blockstates.properties.TimePhase;
import com.verdantartifice.primalmagick.platform.Services;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.data.BlockFamily;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class PhasingBlockFamilyProvider {
    private static final Map<BlockFamily.Variant, BiConsumer<PhasingBlockFamilyProvider, Block>> SHAPE_CONSUMERS = ImmutableMap.<BlockFamily.Variant, BiConsumer<PhasingBlockFamilyProvider, Block>>builder()
            .put(BlockFamily.Variant.SLAB, PhasingBlockFamilyProvider::slab)
            .put(BlockFamily.Variant.STAIRS, PhasingBlockFamilyProvider::stairs)
            .build();

    private final PhasingTextureMapping mapping;
    private final BlockModelGenerators blockModelGenerators;
    private final Map<TimePhase, MultiVariant> fullBlockVariants = new HashMap<>();

    public PhasingBlockFamilyProvider(PhasingTextureMapping mapping, BlockModelGenerators blockModelGenerators) {
        this.mapping = mapping;
        this.blockModelGenerators = blockModelGenerators;
    }

    public PhasingBlockFamilyProvider fullBlock(Block block, ModelTemplate modelTemplate) {
        Arrays.stream(TimePhase.values()).forEach(phase -> {
            Identifier modelId = this.createExtendedModel(block, modelTemplate, phase);
            this.fullBlockVariants.put(phase, BlockModelGenerators.plainVariant(modelId));
            if (phase == TimePhase.FULL) {
                this.blockModelGenerators.registerSimpleItemModel(block, modelId);
            }
        });
        this.blockModelGenerators.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(IPhasingBlock.PHASE).generate(this.fullBlockVariants::get)));
        return this;
    }

    public PhasingBlockFamilyProvider slab(Block block) {
        Map<TimePhase, MultiVariant> bottomVariants = new HashMap<>();
        Map<TimePhase, MultiVariant> topVariants = new HashMap<>();
        Arrays.stream(TimePhase.values()).forEach(phase -> {
            if (!this.fullBlockVariants.containsKey(phase)) {
                throw new IllegalStateException("Full block not generated for phase: " + phase);
            } else {
                Identifier bottomModelId = this.createExtendedModel(block, ModelTemplates.SLAB_BOTTOM, phase);
                Identifier topModelId = this.createExtendedModel(block, ModelTemplates.SLAB_TOP, phase);
                bottomVariants.put(phase, BlockModelGenerators.plainVariant(bottomModelId));
                topVariants.put(phase, BlockModelGenerators.plainVariant(topModelId));
                if (phase == TimePhase.FULL) {
                    this.blockModelGenerators.registerSimpleItemModel(block, bottomModelId);
                }
            }
        });
        this.blockModelGenerators.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(IPhasingBlock.PHASE, BlockStateProperties.SLAB_TYPE).generate((phase, type) -> switch (type) {
                    case BOTTOM -> bottomVariants.get(phase);
                    case TOP -> topVariants.get(phase);
                    case DOUBLE -> this.fullBlockVariants.get(phase);
                })));
        return this;
    }

    public PhasingBlockFamilyProvider stairs(Block block) {
        Map<TimePhase, MultiVariant> innerVariants = new HashMap<>();
        Map<TimePhase, MultiVariant> straightVariants = new HashMap<>();
        Map<TimePhase, MultiVariant> outerVariants = new HashMap<>();
        Arrays.stream(TimePhase.values()).forEach(phase -> {
            Identifier innerModelId = this.createExtendedModel(block, ModelTemplates.STAIRS_INNER, phase);
            Identifier straightModelId = this.createExtendedModel(block, ModelTemplates.STAIRS_STRAIGHT, phase);
            Identifier outerModelId = this.createExtendedModel(block, ModelTemplates.STAIRS_OUTER, phase);
            innerVariants.put(phase, BlockModelGenerators.plainVariant(innerModelId));
            straightVariants.put(phase, BlockModelGenerators.plainVariant(straightModelId));
            outerVariants.put(phase, BlockModelGenerators.plainVariant(outerModelId));
            if (phase == TimePhase.FULL) {
                this.blockModelGenerators.registerSimpleItemModel(block, straightModelId);
            }
        });
        this.blockModelGenerators.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(IPhasingBlock.PHASE, BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.STAIRS_SHAPE).generate((phase, facing, half, shape) -> {
                    MultiVariant variant = switch (shape) {
                        case STRAIGHT -> straightVariants.get(phase);
                        case INNER_LEFT, INNER_RIGHT -> innerVariants.get(phase);
                        case OUTER_LEFT, OUTER_RIGHT -> outerVariants.get(phase);
                    };
                    return variant.with(getStairsRotation(facing, half, shape));
                })));
        return this;
    }

    /**
     * Produces the same rotations as {@link BlockModelGenerators#createStairs} for the given block state values.
     */
    private static VariantMutator getStairsRotation(Direction facing, Half half, StairsShape shape) {
        boolean leftShape = shape == StairsShape.INNER_LEFT || shape == StairsShape.OUTER_LEFT;
        boolean rightShape = shape == StairsShape.INNER_RIGHT || shape == StairsShape.OUTER_RIGHT;
        int quarterTurns = switch (facing) {
            case EAST -> 0;
            case SOUTH -> 1;
            case WEST -> 2;
            case NORTH -> 3;
            default -> throw new IllegalArgumentException("Invalid stairs facing: " + facing);
        };
        if (half == Half.BOTTOM && leftShape) {
            quarterTurns += 3;
        } else if (half == Half.TOP && rightShape) {
            quarterTurns += 1;
        }
        VariantMutator yRotation = switch (quarterTurns % 4) {
            case 1 -> BlockModelGenerators.Y_ROT_90;
            case 2 -> BlockModelGenerators.Y_ROT_180;
            case 3 -> BlockModelGenerators.Y_ROT_270;
            default -> BlockModelGenerators.NOP;
        };
        if (half == Half.TOP) {
            return BlockModelGenerators.X_ROT_180.then(yRotation).then(BlockModelGenerators.UV_LOCK);
        } else if (quarterTurns % 4 == 0) {
            return BlockModelGenerators.NOP;
        } else {
            return yRotation.then(BlockModelGenerators.UV_LOCK);
        }
    }

    private Identifier createExtendedModel(Block block, ModelTemplate modelTemplate, TimePhase phase) {
        return Services.MODEL_TEMPLATES.extend(modelTemplate)
                .createWithSuffix(block, "_" + phase, this.mapping.resolve(phase), this.blockModelGenerators.modelOutput);
    }

    public PhasingBlockFamilyProvider generateFor(BlockFamily family) {
        family.getVariants().forEach((variant, block) -> {
            BiConsumer<PhasingBlockFamilyProvider, Block> consumer = SHAPE_CONSUMERS.get(variant);
            if (consumer != null) {
                consumer.accept(this, block);
            }
        });
        return this;
    }
}
