package com.verdantartifice.primalmagick.platform;

import com.verdantartifice.primalmagick.platform.services.IPlatformService;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

public class PlatformServiceNeoforge implements IPlatformService {
    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        FMLLoader loader = FMLLoader.getCurrentOrNull();
        return loader != null && !loader.isProduction();
    }

    @Override
    public boolean isClientDist() {
        return FMLEnvironment.getDist().isClient();
    }

    @Override
    public @Nullable RegistryAccess getRegistryAccess() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.registryAccess();
        } else if (this.isClientDist()) {
            return ClientRegistryAccess.get();
        } else {
            return null;
        }
    }

    /**
     * Client-only registry access lookup, kept in its own class so that it is never loaded on a dedicated server.
     */
    private static class ClientRegistryAccess {
        static @Nullable RegistryAccess get() {
            Minecraft mc = Minecraft.getInstance();
            return mc.level == null ? null : mc.level.registryAccess();
        }
    }
}
