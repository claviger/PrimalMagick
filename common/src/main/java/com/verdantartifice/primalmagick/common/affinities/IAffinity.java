package com.verdantartifice.primalmagick.common.affinities;

import com.verdantartifice.primalmagick.common.sources.SourceList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Primary interface for a data-defined affinity entry.
 * 
 * @author Daedalus4096
 */
public interface IAffinity {
    @NotNull AffinityType<?> getType();
    @NotNull Identifier getTarget();
    @NotNull CompletableFuture<SourceList> getTotalAsync(@Nullable Collection<RecipeHolder<?>> recipes, @NotNull RegistryAccess registryAccess, @NotNull List<Identifier> history);
}
