package com.verdantartifice.primalmagick.client.renderers;

import com.verdantartifice.primalmagick.common.util.ResourceUtils;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SpriteMapper;
import net.minecraft.client.resources.model.sprite.SpriteId;

public class SheetsPM {
    // Tiered shield bases live in vanilla's shield_patterns atlas, alongside the banner pattern sprites drawn on top of
    // them. That atlas's directory source stitches textures/entity/shield/ from every namespace, so our textures are
    // picked up without an atlas definition of our own. A SpriteId must name a registered atlas texture; a mod-namespaced
    // "textures/atlas/shield_patterns.png" is not one and makes AtlasManager.get throw on first render.
    public static final SpriteMapper SHIELD_MAPPER = Sheets.SHIELD_MAPPER;

    public static final SpriteId PRIMALITE_SHIELD_BASE = SHIELD_MAPPER.apply(ResourceUtils.loc("primalite_shield_base"));
    public static final SpriteId PRIMALITE_SHIELD_BASE_NO_PATTERN = SHIELD_MAPPER.apply(ResourceUtils.loc("primalite_shield_base_nopattern"));
    public static final SpriteId HEXIUM_SHIELD_BASE = SHIELD_MAPPER.apply(ResourceUtils.loc("hexium_shield_base"));
    public static final SpriteId HEXIUM_SHIELD_BASE_NO_PATTERN = SHIELD_MAPPER.apply(ResourceUtils.loc("hexium_shield_base_nopattern"));
    public static final SpriteId HALLOWSTEEL_SHIELD_BASE = SHIELD_MAPPER.apply(ResourceUtils.loc("hallowsteel_shield_base"));
    public static final SpriteId HALLOWSTEEL_SHIELD_BASE_NO_PATTERN = SHIELD_MAPPER.apply(ResourceUtils.loc("hallowsteel_shield_base_nopattern"));
}
