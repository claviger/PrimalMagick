package com.verdantartifice.primalmagick.common.util;

import com.verdantartifice.primalmagick.common.capabilities.IItemHandlerNeoforge;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
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
public abstract class AbstractContainerWrapperPMNeoforge implements IItemHandlerNeoforge {
    protected final Container container;
    protected final ResourceHandler<ItemResource> handler;

    protected AbstractContainerWrapperPMNeoforge(Container container, ResourceHandler<ItemResource> handler) {
        this.container = container;
        this.handler = handler;
    }

    @Override
    public ResourceHandler<ItemResource> getResourceHandler() {
        return this.handler;
    }

    @Override
    public IndexModifier<ItemResource> getIndexModifier() {
        return (index, resource, amount) -> this.container.setItem(index, resource.toStack(amount));
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
            if (resource.isEmpty() || amount <= 0) {
                // The transfer API rejects empty resources; extracting from an empty slot yields nothing
                return ItemStack.EMPTY;
            }
            retVal = resource.toStack(this.handler.extract(slot, resource, amount, tx));
            if (!simulate) {
                tx.commit();
            }
        }
        return retVal;
    }

    @Override
    public ItemStack extractItem(ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            // The transfer API rejects empty resources; extracting nothing yields nothing
            return ItemStack.EMPTY;
        }
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
        if (slotOperation.stack().isEmpty()) {
            // The transfer API rejects empty resources, so treat moving an empty stack as a successful no-op. This lets
            // a replacement extract from an empty slot or insert nothing, as setting a slot's contents could before.
            return ItemStack.EMPTY;
        }
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
        if (handlerOperation.stack().isEmpty()) {
            // The transfer API rejects empty resources, so treat moving an empty stack as a successful no-op
            return ItemStack.EMPTY;
        }
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
