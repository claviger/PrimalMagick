package com.verdantartifice.primalmagick.datagen.models;

import com.verdantartifice.primalmagick.platform.Services;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Objects;

public class TextureMappingsPM {
    public static TextureMapping empty(Block block) {
        return new TextureMapping();
    }

    public static TextureMapping manaFont(Block block) {
        return new TextureMapping()
                .put(TextureSlotsPM.BASE, TextureMapping.getBlockTexture(block));
    }

    public static TextureMapping columnWithEnd(Block block) {
        return TextureMapping.column(TextureMapping.getBlockTexture(block, "_side"), TextureMapping.getBlockTexture(block, "_end"));
    }

    public static TextureMapping calcinator(Block block) {
        // Calcinators have no bottom texture of their own and borrow the vanilla furnace's
        return TextureMapping.orientableCubeOnlyTop(block)
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(Blocks.FURNACE, "_top"));
    }

    public static TextureMapping pillar(Block block) {
        return new TextureMapping()
                .put(TextureSlotsPM.INNER, TextureMapping.getBlockTexture(block, "_inner"))
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block));
    }

    public static TextureMapping pillarBottom(Block block) {
        return new TextureMapping()
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(block, "_base"))
                .put(TextureSlotsPM.INNER, TextureMapping.getBlockTexture(block, "_inner"))
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_bottom"));
    }

    public static TextureMapping pillarTop(Block block) {
        return new TextureMapping()
                .put(TextureSlotsPM.INNER, TextureMapping.getBlockTexture(block, "_inner"))
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_top"))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_base"));
    }

    public static TextureMapping runescribingAltar(Block altarBlock, Block baseBlock) {
        return new TextureMapping()
                .put(TextureSlotsPM.ALTAR_BOTTOM, TextureMapping.getBlockTexture(baseBlock))
                .put(TextureSlotsPM.ALTAR_SIDE, TextureMapping.getBlockTexture(altarBlock, "_side"));
    }

    public static TextureMapping connected(Block block, ModelConnection modelConnection) {
        Identifier id = Services.BLOCKS_REGISTRY.getKey(block);
        return connected(Objects.requireNonNull(id), modelConnection);
    }

    public static TextureMapping connected(Identifier id, ModelConnection modelConnection) {
        TextureMapping retVal = new TextureMapping();
        modelConnection.textureConnections().forEach((slot, textureConnection) -> retVal.put(slot, getConnectedBlockTexture(id, textureConnection)));
        return retVal;
    }

    private static Material getConnectedBlockTexture(Block block, TextureConnection connection) {
        return TextureMapping.getBlockTexture(block, connection.suffix());
    }

    public static Material getConnectedBlockTexture(Identifier baseId, TextureConnection connection) {
        return new Material(baseId.withPath(path -> "block/" + path + connection.suffix()));
    }
}
