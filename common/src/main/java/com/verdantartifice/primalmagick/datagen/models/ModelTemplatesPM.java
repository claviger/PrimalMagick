package com.verdantartifice.primalmagick.datagen.models;

import com.verdantartifice.primalmagick.common.util.ResourceUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public class ModelTemplatesPM {
    public static final ModelTemplate MANA_ORB = createItem("template_mana_orb", TextureSlot.PARTICLE);
    public static final ModelTemplate SPELLTOME = createItem("template_spelltome", TextureSlot.PARTICLE);
    public static final ModelTemplate TRIDENT_IN_HAND = createVanillaItem("trident_in_hand", "_in_hand", TextureSlot.PARTICLE);
    public static final ModelTemplate TRIDENT_THROWING = createVanillaItem("trident_throwing", "_throwing", TextureSlot.PARTICLE);
    public static final ModelTemplate SHIELD = createVanillaItem("shield", "");
    public static final ModelTemplate SHIELD_BLOCKING = createVanillaItem("shield_blocking", "_blocking");

    public static final ModelTemplate EMPTY = createBlock("empty");
    public static final ModelTemplate MANA_FONT = createBlock("mana_font", TextureSlotsPM.BASE);
    public static final ModelTemplate PILLAR = createBlock("pillar", TextureSlotsPM.INNER, TextureSlot.SIDE);
    public static final ModelTemplate PILLAR_BOTTOM = createBlock("pillar_bottom", "_bottom", TextureSlot.BOTTOM, TextureSlotsPM.INNER, TextureSlot.SIDE);
    public static final ModelTemplate PILLAR_TOP = createBlock("pillar_top", "_top", TextureSlotsPM.INNER, TextureSlot.SIDE, TextureSlot.TOP);
    public static final ModelTemplate RUNESCRIBING_ALTAR = createBlock("runescribing_altar", TextureSlotsPM.ALTAR_BOTTOM, TextureSlotsPM.ALTAR_SIDE);
    public static final ModelTemplate CARVED_BOOKSHELF = createVanillaBlock("chiseled_bookshelf", "", TextureSlot.TOP, TextureSlot.SIDE);
    public static final ModelTemplate CARVED_BOOKSHELF_INVENTORY = createVanillaBlock("chiseled_bookshelf_inventory", "_inventory", TextureSlot.TOP, TextureSlot.SIDE, TextureSlot.FRONT);

    public static ModelTemplate createBlock(String name, TextureSlot... requiredSlots) {
        return new ModelTemplate(Optional.of(ResourceUtils.loc(name).withPrefix("block/")), Optional.empty(), requiredSlots);
    }

    public static ModelTemplate createBlock(String name, String suffix, TextureSlot... requiredSlots) {
        return new ModelTemplate(Optional.of(ResourceUtils.loc(name).withPrefix("block/")), Optional.of(suffix), requiredSlots);
    }

    public static ModelTemplate createItem(String name, TextureSlot... requiredSlots) {
        return new ModelTemplate(Optional.of(ResourceUtils.loc(name).withPrefix("item/")), Optional.empty(), requiredSlots);
    }

    public static ModelTemplate createVanillaBlock(String name, String suffix, TextureSlot... requiredSlots) {
        return new ModelTemplate(Optional.of(Identifier.withDefaultNamespace(name).withPrefix("block/")), suffix.isEmpty() ? Optional.empty() : Optional.of(suffix), requiredSlots);
    }

    public static ModelTemplate createVanillaItem(String name, String suffix, TextureSlot... requiredSlots) {
        return new ModelTemplate(Optional.of(Identifier.withDefaultNamespace(name).withPrefix("item/")), suffix.isEmpty() ? Optional.empty() : Optional.of(suffix), requiredSlots);
    }
}
