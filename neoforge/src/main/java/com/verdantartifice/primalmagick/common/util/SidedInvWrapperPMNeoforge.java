package com.verdantartifice.primalmagick.common.util;

import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import org.jetbrains.annotations.Nullable;

public class SidedInvWrapperPMNeoforge extends AbstractContainerWrapperPMNeoforge {
    public SidedInvWrapperPMNeoforge(WorldlyContainer inv, @Nullable Direction side) {
        super(inv, new WorldlyContainerWrapper(inv, side));
    }
}
