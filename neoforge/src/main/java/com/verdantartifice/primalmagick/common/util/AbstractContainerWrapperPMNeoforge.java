package com.verdantartifice.primalmagick.common.util;

import com.verdantartifice.primalmagick.common.capabilities.IItemHandlerPM;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;

/**
 * Base implementation of the mod's item handler capability interface which delegates to a Neoforge
 * resource handler over a vanilla container.
 */
public abstract class AbstractContainerWrapperPMNeoforge implements IItemHandlerPM {
    protected final Container container;
    protected final ResourceHandler<ItemResource> handler;

    protected AbstractContainerWrapperPMNeoforge(Container container, ResourceHandler<ItemResource> handler) {
        this.container = container;
        this.handler = handler;
    }

    @Override
    public int getSlots() {
        return this.handler.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return ItemUtil.getStack(this.handler, slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return ItemUtil.insertItemReturnRemaining(this.handler, slot, stack, simulate, null);
    }

    @Override
    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        return ItemUtil.insertItemReturnRemaining(this.handler, stack, simulate, null);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack retVal;
        try (Transaction tx = Transaction.openRoot()) {
            ItemResource resource = this.handler.getResource(slot);
            retVal = resource.toStack(this.handler.extract(slot, resource, amount, tx));
            if (!simulate) {
                tx.commit();
            }
        }
        return retVal;
    }

    @Override
    public ItemStack extractItem(ItemStack stack, boolean simulate) {
        ItemStack retVal;
        try (Transaction tx = Transaction.openRoot()) {
            ItemResource resource = ItemResource.of(stack);
            retVal = resource.toStack(this.handler.extract(resource, stack.count(), tx));
            if (!simulate) {
                tx.commit();
            }
        }
        return retVal;
    }

    @Override
    public boolean transactSlots(boolean simulate, List<SlotOperation> slotOperations) {
        boolean success;
        try (Transaction tx = Transaction.openRoot()) {
            success = slotOperations.stream().allMatch(op -> ItemStack.matches(op.stack(), this.performSlotTransactionOperation(simulate, op, tx)));
            if (success && !simulate) {
                tx.commit();
            }
        }
        return success;
    }

    protected ItemStack performSlotTransactionOperation(boolean simulate, SlotOperation slotOperation, TransactionContext parent) {
        try (Transaction childTx = Transaction.open(parent)) {
            ItemResource opResource = ItemResource.of(slotOperation.stack());
            ItemStack retVal = switch (slotOperation.type()) {
                case EXTRACT -> this.handler.getResource(slotOperation.slot()).toStack(this.handler.extract(slotOperation.slot(), opResource, slotOperation.stack().count(), childTx));
                case INSERT -> opResource.toStack(this.handler.insert(slotOperation.slot(), opResource, slotOperation.stack().count(), childTx));
            };
            if (!simulate) {
                childTx.commit();
            }
            return retVal;
        }
    }

    @Override
    public boolean transact(boolean simulate, List<HandlerOperation> handlerOperations) {
        boolean success;
        try (Transaction tx = Transaction.openRoot()) {
            success = handlerOperations.stream().allMatch(op -> ItemStack.matches(op.stack(), this.performHandlerTransactionOperations(simulate, op, tx)));
            if (success && !simulate) {
                tx.commit();
            }
        }
        return success;
    }

    protected ItemStack performHandlerTransactionOperations(boolean simulate, HandlerOperation handlerOperation, TransactionContext parent) {
        try (Transaction childTx = Transaction.open(parent)) {
            ItemResource opResource = ItemResource.of(handlerOperation.stack());
            ItemStack retVal = switch (handlerOperation.type()) {
                case EXTRACT -> opResource.toStack(this.handler.extract(opResource, handlerOperation.stack().count(), childTx));
                case INSERT -> opResource.toStack(this.handler.insert(opResource, handlerOperation.stack().count(), childTx));
            };
            if (!simulate) {
                childTx.commit();
            }
            return retVal;
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.handler.getCapacityAsInt(slot, this.handler.getResource(slot));
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return this.handler.isValid(slot, ItemResource.of(stack));
    }

    @Override
    public Container asContainer() {
        return this.container;
    }
}
