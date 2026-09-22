package com.verdantartifice.primalmagick.common.util;

import net.minecraft.world.Container;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;

public class InvWrapperPMNeoforge extends AbstractContainerWrapperPMNeoforge {
    public InvWrapperPMNeoforge(Container container) {
        super(container, VanillaContainerWrapper.of(container));
    }
}
