package com.verdantartifice.primalmagick.common.capabilities;

import com.verdantartifice.primalmagick.common.util.AbstractContainerWrapperPMNeoforge;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;

/**
 * View of a mod item handler that is exposed to other blocks through the item capability. Insertions through it are
 * held to the handler's item validity and slot limit functions, which the underlying stacks handler doesn't apply on
 * its own, so that automation can't fill output slots or overfill limited slots. The owning block entity keeps using
 * the unrestricted handler for its own inserts.
 * <p>
 * This view is meant for external automation and not to back menu slots, as its index modifier bypasses the filter.
 */
public class FilteredItemHandlerPMNeoforge extends DelegatingResourceHandler<ItemResource> implements IItemHandlerNeoforge {
    protected final ItemStackHandlerPMNeoforge handler;
    protected final IItemHandlerNeoforge view;

    public FilteredItemHandlerPMNeoforge(ItemStackHandlerPMNeoforge handler) {
        super(handler);
        this.handler = handler;
        this.view = new AbstractContainerWrapperPMNeoforge(handler.asContainer(), this) {};
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        if (resource.isEmpty()) {
            return true;
        }
        return this.handler.isItemValid(this.convertIndex(index), resource.toStack(1));
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        int slot = this.convertIndex(index);
        if (!this.isValid(slot, resource)) {
            return 0;
        }
        return this.capacityFor(slot, resource);
    }

    /**
     * Returns the capacity of the given slot for the given resource, capped by the handler's slot limit function,
     * without checking whether the resource is valid for the slot.
     */
    private long capacityFor(int slot, ItemResource resource) {
        long capacity = this.handler.getCapacityAsLong(slot, resource);
        return this.handler.limitFuncOverride.map(f -> Math.min(capacity, f.apply(slot))).orElse(capacity);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int slot = this.convertIndex(index);
        if (!this.isValid(slot, resource)) {
            return 0;
        }
        long room = this.capacityFor(slot, resource) - this.handler.getAmountAsLong(slot);
        if (room <= 0) {
            return 0;
        }
        return this.handler.insert(slot, resource, (int)Math.min(amount, room), transaction);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int inserted = 0;
        for (int index = 0; index < this.size() && inserted < amount; index++) {
            inserted += this.insert(index, resource, amount - inserted, transaction);
        }
        return inserted;
    }

    @Override
    public ResourceHandler<ItemResource> getResourceHandler() {
        return this;
    }

    @Override
    public IndexModifier<ItemResource> getIndexModifier() {
        return this.handler.getIndexModifier();
    }

    @Override
    public int getSlots() {
        return this.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.view.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return this.view.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        return this.view.insertItem(stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return this.view.extractItem(slot, amount, simulate);
    }

    @Override
    public ItemStack extractItem(ItemStack stack, boolean simulate) {
        return this.view.extractItem(stack, simulate);
    }

    @Override
    public boolean transactSlots(boolean simulate, List<SlotOperation> slotOperations) {
        return this.view.transactSlots(simulate, slotOperations);
    }

    @Override
    public boolean transact(boolean simulate, List<HandlerOperation> handlerOperations) {
        return this.view.transact(simulate, handlerOperations);
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.handler.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return this.handler.isItemValid(slot, stack);
    }

    @Override
    public Container asContainer() {
        return this.handler.asContainer();
    }
}
