package com.verdantartifice.primalmagick.common.capabilities;

import com.verdantartifice.primalmagick.common.util.AbstractContainerWrapperPMNeoforge;
import net.minecraft.world.SimpleContainer;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;

/**
 * Item handler capability which holds no slots and rejects all operations.
 */
public class EmptyItemHandlerPMNeoforge extends AbstractContainerWrapperPMNeoforge {
    public static final EmptyItemHandlerPMNeoforge INSTANCE = new EmptyItemHandlerPMNeoforge();

    private EmptyItemHandlerPMNeoforge() {
        super(new SimpleContainer(0), EmptyResourceHandler.instance());
    }
}
