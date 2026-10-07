package com.verdantartifice.primalmagick.common.entities;

import com.verdantartifice.primalmagick.common.registries.IRegistryItem;
import com.verdantartifice.primalmagick.platform.Services;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Deferred registry for mod entity data serializers.
 */
public class EntityDataSerializersPM {
    public static void init() {
        // Pass the service initialization through this class so it gets class loaded and fields registered
        Services.ENTITY_DATA_SERIALIZERS_REGISTRY.init();
    }

    public static final IRegistryItem<EntityDataSerializer<?>, EntityDataSerializer<Optional<EntityReference<Entity>>>> OPTIONAL_ENTITY_REFERENCE = register("optional_entity_reference", () -> EntityDataSerializer.forValueType(EntityReference.<Entity>streamCodec().apply(ByteBufCodecs::optional)));

    private static <T> IRegistryItem<EntityDataSerializer<?>, EntityDataSerializer<T>> register(String name, Supplier<EntityDataSerializer<T>> supplier) {
        return Services.ENTITY_DATA_SERIALIZERS_REGISTRY.register(name, supplier);
    }
}
