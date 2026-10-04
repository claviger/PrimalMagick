package com.verdantartifice.primalmagick.test.worldgen;

import com.verdantartifice.primalmagick.Constants;
import com.verdantartifice.primalmagick.common.util.ResourceUtils;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tests for the mod's world generation data.
 */
public class WorldgenTests extends AbstractBaseTest {
    private static final BlockPos GROUND_POS = new BlockPos(3, 0, 3);
    private static final BlockPos TREE_POS = GROUND_POS.above();

    /**
     * Every block named in a mod structure template's palette must still be registered once the template has been
     * run through the vanilla structure data fixers; an unknown name silently loads as air.
     */
    public static void structure_templates_reference_known_blocks(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        Map<Identifier, Resource> resources = server.getResourceManager().listResources("structure",
                loc -> loc.getNamespace().equals(Constants.MOD_ID) && loc.getPath().endsWith(".nbt"));
        assertFalse(helper, resources.isEmpty(), "No mod structure templates found");
        List<String> unknown = new ArrayList<>();
        resources.forEach((loc, resource) -> {
            CompoundTag tag;
            try (InputStream input = resource.open()) {
                tag = NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap());
            } catch (IOException e) {
                unknown.add(loc + " (unreadable: " + e.getMessage() + ")");
                return;
            }
            tag = DataFixTypes.STRUCTURE.updateToCurrentVersion(server.getFixerUpper(), tag, NbtUtils.getDataVersion(tag, 500));
            List<ListTag> palettes = new ArrayList<>();
            tag.getList("palette").ifPresent(palettes::add);
            tag.getList("palettes").ifPresent(list -> list.stream().forEach(t -> t.asList().ifPresent(palettes::add)));
            for (ListTag palette : palettes) {
                palette.compoundStream().forEach(entry -> {
                    String name = entry.getStringOr("Name", "");
                    if (!BuiltInRegistries.BLOCK.containsKey(Identifier.parse(name))) {
                        unknown.add(loc + " -> " + name);
                    }
                });
            }
        });
        assertTrue(helper, unknown.isEmpty(), "Structure templates reference unregistered blocks: " + unknown);
        helper.succeed();
    }

    /**
     * The bottom layer of a structure template must contain the expected number of the given block, so that its
     * floor, and the water pools on top of it, are placed intact.
     */
    public static void template_has_floor(GameTestHelper helper, String templateName, Block floorBlock, int expectedCount) {
        StructureTemplate template = helper.getLevel().getStructureManager().getOrCreate(ResourceUtils.loc(templateName));
        long floorCount = template.filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(), floorBlock, false).stream()
                .filter(info -> info.pos().getY() == 0)
                .count();
        assertValueEqual(helper, (long)expectedCount, floorCount, "Number of " + BuiltInRegistries.BLOCK.getKey(floorBlock) + " blocks in the bottom layer of " + templateName);
        helper.succeed();
    }

    /**
     * A sapling planted on a grass block must be able to stay there, and bonemealing it must grow its tree.
     */
    public static void sapling_grows_tree(GameTestHelper helper, Block sapling, Block log) {
        helper.setBlock(GROUND_POS, Blocks.GRASS_BLOCK);
        helper.setBlock(TREE_POS, sapling);
        BlockState saplingState = helper.getBlockState(TREE_POS);
        assertTrue(helper, saplingState.canSurvive(helper.getLevel(), helper.absolutePos(TREE_POS)), BuiltInRegistries.BLOCK.getKey(sapling) + " cannot survive on a grass block");

        // Each bonemeal advances the sapling one stage; the second one grows the tree
        RandomSource random = RandomSource.create(0L);
        for (int attempt = 0; attempt < 10 && helper.getBlockState(TREE_POS).is(sapling); attempt++) {
            BlockState state = helper.getBlockState(TREE_POS);
            ((SaplingBlock)sapling).performBonemeal(helper.getLevel(), random, helper.absolutePos(TREE_POS), state);
        }
        assertTrue(helper, helper.getBlockState(TREE_POS).is(log), "Expected " + BuiltInRegistries.BLOCK.getKey(log) + " at the sapling position but found " + helper.getBlockState(TREE_POS));
        assertTrue(helper, helper.getBlockState(TREE_POS.above(4)).is(log), "Expected a trunk at least five blocks tall but found " + helper.getBlockState(TREE_POS.above(4)));
        helper.succeed();
    }

    /**
     * The sapling-checked placed feature used by world generation must place its tree on a grass block.
     */
    public static void placed_feature_places_tree(GameTestHelper helper, ResourceKey<PlacedFeature> featureKey, Block log) {
        helper.setBlock(GROUND_POS, Blocks.GRASS_BLOCK);
        var level = helper.getLevel();
        PlacedFeature feature = level.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE).getValueOrThrow(featureKey);
        boolean placed = feature.place(level, level.getChunkSource().getGenerator(), RandomSource.create(0L), helper.absolutePos(TREE_POS));
        assertTrue(helper, placed, "Placed feature " + featureKey.identifier() + " did not place on a grass block");
        assertTrue(helper, helper.getBlockState(TREE_POS).is(log), "Expected " + BuiltInRegistries.BLOCK.getKey(log) + " at the feature origin but found " + helper.getBlockState(TREE_POS));
        helper.succeed();
    }
}
