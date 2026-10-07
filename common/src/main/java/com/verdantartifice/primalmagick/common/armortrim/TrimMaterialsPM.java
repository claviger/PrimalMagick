package com.verdantartifice.primalmagick.common.armortrim;

import com.verdantartifice.primalmagick.common.sources.Source;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.common.util.ResourceUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.equipment.trim.MaterialAssetGroup;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry of mod armor trim materials, backed by datapack JSON.
 * 
 * @author Daedalus4096
 */
public class TrimMaterialsPM {
    protected static final Map<ResourceKey<TrimMaterial>, Source> SOURCE_MAPPING = new HashMap<>();
    protected static final Map<ResourceKey<TrimMaterial>, MaterialAssetGroup> ASSET_MAPPING = new LinkedHashMap<>();
    
    public static final ResourceKey<TrimMaterial> RUNE_EARTH = registryKey("rune_earth", Sources.EARTH, MaterialAssetGroup.EMERALD);           // Use emerald trim palette
    public static final ResourceKey<TrimMaterial> RUNE_SEA = registryKey("rune_sea", Sources.SEA, MaterialAssetGroup.LAPIS);                   // Use lapis trim palette
    public static final ResourceKey<TrimMaterial> RUNE_SKY = registryKey("rune_sky", Sources.SKY, MaterialAssetGroup.DIAMOND);                 // Use diamond trim palette
    public static final ResourceKey<TrimMaterial> RUNE_SUN = registryKey("rune_sun", Sources.SUN, MaterialAssetGroup.GOLD);                    // Use gold trim palette
    public static final ResourceKey<TrimMaterial> RUNE_MOON = registryKey("rune_moon", Sources.MOON, MaterialAssetGroup.IRON);                 // Use iron trim palette
    public static final ResourceKey<TrimMaterial> RUNE_BLOOD = registryKey("rune_blood", Sources.BLOOD, MaterialAssetGroup.REDSTONE);          // Use redstone trim palette
    public static final ResourceKey<TrimMaterial> RUNE_INFERNAL = registryKey("rune_infernal", Sources.INFERNAL, MaterialAssetGroup.COPPER);   // Use copper trim palette
    public static final ResourceKey<TrimMaterial> RUNE_VOID = registryKey("rune_void", Sources.VOID, MaterialAssetGroup.AMETHYST);             // Use amethyst trim palette
    public static final ResourceKey<TrimMaterial> RUNE_HALLOWED = registryKey("rune_hallowed", Sources.HALLOWED, MaterialAssetGroup.QUARTZ);   // Use quartz trim palette
    
    private static ResourceKey<TrimMaterial> registryKey(String name, Source source, MaterialAssetGroup assets) {
        ResourceKey<TrimMaterial> key = ResourceKey.create(Registries.TRIM_MATERIAL, ResourceUtils.loc(name));
        if (SOURCE_MAPPING.containsKey(key)) {
            throw new IllegalStateException("Source mapping already set for trim material " + name);
        }
        SOURCE_MAPPING.put(key, source);
        ASSET_MAPPING.put(key, assets);
        return key;
    }

    private static void register(BootstrapContext<TrimMaterial> pContext, ResourceKey<TrimMaterial> pKey) {
        Component component = Component.translatable(Util.makeDescriptionId("trim_material", pKey.identifier())).withStyle(getStyle(getSource(pKey)));
        pContext.register(pKey, new TrimMaterial(ASSET_MAPPING.get(pKey), component));
    }
    
    private static Style getStyle(Source source) {
        return Style.EMPTY.withColor(source.getColor());
    }
    
    public static Source getSource(ResourceKey<TrimMaterial> key) {
        if (!SOURCE_MAPPING.containsKey(key)) {
            throw new IllegalArgumentException("No source mapping found for trim material " + key.toString());
        } else {
            return SOURCE_MAPPING.get(key);
        }
    }
    
    /**
     * Returns the texture assets used by each mod trim material, in registration order.
     */
    public static Map<ResourceKey<TrimMaterial>, MaterialAssetGroup> getAssetMapping() {
        return Collections.unmodifiableMap(ASSET_MAPPING);
    }
    
    public static void bootstrap(BootstrapContext<TrimMaterial> context) {
        register(context, RUNE_EARTH);
        register(context, RUNE_SEA);
        register(context, RUNE_SKY);
        register(context, RUNE_SUN);
        register(context, RUNE_MOON);
        register(context, RUNE_BLOOD);
        register(context, RUNE_INFERNAL);
        register(context, RUNE_VOID);
        register(context, RUNE_HALLOWED);
    }
}
