package com.verdantartifice.primalmagick.platform.registries;

import com.verdantartifice.primalmagick.Constants;
import com.verdantartifice.primalmagick.platform.services.registries.IEntityDataSerializerRegistryService;
import net.minecraft.core.Registry;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Neoforge implementation of the entity data serializer registry service.
 */
public class EntityDataSerializerRegistryServiceNeoforge extends AbstractRegistryServiceNeoforge<EntityDataSerializer<?>> implements IEntityDataSerializerRegistryService {
    private static final DeferredRegister<EntityDataSerializer<?>> SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, Constants.MOD_ID);

    @Override
    protected Supplier<DeferredRegister<EntityDataSerializer<?>>> getDeferredRegisterSupplier() {
        return () -> SERIALIZERS;
    }

    @Override
    protected Registry<EntityDataSerializer<?>> getRegistry() {
        return NeoForgeRegistries.ENTITY_DATA_SERIALIZERS;
    }
}
