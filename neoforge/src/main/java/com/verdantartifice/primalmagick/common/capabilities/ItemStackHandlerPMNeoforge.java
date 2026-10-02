package com.verdantartifice.primalmagick.common.capabilities;

import com.verdantartifice.primalmagick.common.tiles.base.AbstractTilePM;
import com.verdantartifice.primalmagick.common.util.RecipeContainerWrapper;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Util;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Extension of the default IItemHandler implementation which updates its owning tile's client
 * side when the contents of the capability change.
 * 
 * @author Daedalus4096
 */
public class ItemStackHandlerPMNeoforge extends ItemStacksResourceHandler implements IItemHandlerNeoforge {
    protected final AbstractTilePM tile;
    protected final Optional<Function<Integer, Integer>> limitFuncOverride;
    protected final Optional<BiPredicate<Integer, ItemStack>> validityFuncOverride;
    protected final Optional<BiConsumer<Integer, ItemStack>> contentsChangedFuncOverride;
    protected FilteredItemHandlerPMNeoforge capabilityHandler;

    public ItemStackHandlerPMNeoforge(int size, AbstractTilePM tile) {
        super(size);
        this.tile = tile;
        this.limitFuncOverride = Optional.empty();
        this.validityFuncOverride = Optional.empty();
        this.contentsChangedFuncOverride = Optional.empty();
    }

    public ItemStackHandlerPMNeoforge(NonNullList<ItemStack> stacks, AbstractTilePM tile) {
        super(stacks);
        // Share the given list rather than a copy, so that changes made through the handler are seen by its owner
        this.stacks = stacks;
        this.tile = tile;
        this.limitFuncOverride = Optional.empty();
        this.validityFuncOverride = Optional.empty();
        this.contentsChangedFuncOverride = Optional.empty();
    }

    public ItemStackHandlerPMNeoforge(ResourceHandler<ItemResource> original, @Nullable AbstractTilePM tile) {
        super(Util.make(NonNullList.createWithCapacity(original.size()), newList -> {
            for (int i = 0; i < original.size(); i++) {
                newList.add(ItemUtil.getStack(original, i));
            }
        }));
        this.tile = tile;
        this.limitFuncOverride = Optional.empty();
        this.validityFuncOverride = Optional.empty();
        this.contentsChangedFuncOverride = Optional.empty();
    }

    protected ItemStackHandlerPMNeoforge(NonNullList<ItemStack> stacks, AbstractTilePM tile, Optional<Function<Integer, Integer>> limit,
                                         Optional<BiPredicate<Integer, ItemStack>> validity, Optional<BiConsumer<Integer, ItemStack>> contentsChanged) {
        super(stacks);
        // Share the given list rather than a copy, so that changes made through the handler are seen by its owner
        this.stacks = stacks;
        this.tile = tile;
        this.limitFuncOverride = limit;
        this.validityFuncOverride = validity;
        this.contentsChangedFuncOverride = contentsChanged;
    }

    @Override
    public ResourceHandler<ItemResource> getResourceHandler() {
        return this;
    }

    /**
     * Returns the view of this handler to expose through the item capability, which holds external insertions to this
     * handler's item validity and slot limit functions.
     *
     * @return the filtered view of this handler
     */
    public FilteredItemHandlerPMNeoforge getCapabilityHandler() {
        if (this.capabilityHandler == null) {
            this.capabilityHandler = new FilteredItemHandlerPMNeoforge(this);
        }
        return this.capabilityHandler;
    }

    @Override
    public IndexModifier<ItemResource> getIndexModifier() {
        return this::set;
    }

    @Override
    public int getSlots() {
        return this.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return ItemUtil.getStack(this, slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return ItemUtil.insertItemReturnRemaining(this, slot, stack, simulate, null);
    }

    @Override
    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        return ItemUtil.insertItemReturnRemaining(this, stack, simulate, null);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack retVal;
        try (Transaction tx = Transaction.openRoot()) {
            ItemResource resource = this.getResource(slot);
            if (resource.isEmpty() || amount <= 0) {
                // The transfer API rejects empty resources; extracting from an empty slot yields nothing
                return ItemStack.EMPTY;
            }
            retVal = resource.toStack(this.extract(slot, resource, amount, tx));
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
            retVal = resource.toStack(this.extract(resource, stack.count(), tx));
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
                case EXTRACT -> this.getResource(slotOperation.slot()).toStack(this.extract(slotOperation.slot(), opResource, slotOperation.stack().count(), childTx));
                case INSERT -> opResource.toStack(this.insert(slotOperation.slot(), opResource, slotOperation.stack().count(), childTx));
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
                case EXTRACT -> opResource.toStack(this.extract(opResource, handlerOperation.stack().count(), childTx));
                case INSERT -> opResource.toStack(this.insert(opResource, handlerOperation.stack().count(), childTx));
            };
            if (!simulate) {
                childTx.commit();
            }
            return retVal;
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.limitFuncOverride.map(f -> f.apply(slot)).orElseGet(() -> super.getCapacity(slot, this.getResource(slot)));
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return this.validityFuncOverride.map(f -> f.test(slot, stack)).orElseGet(() -> super.isValid(slot, ItemResource.of(stack)));
    }

    @Override
    public Container asContainer() {
        return new RecipeContainerWrapper(this);
    }

    @Override
    protected void onContentsChanged(int slot, @NotNull ItemStack previousContents) {
        super.onContentsChanged(slot, previousContents);
        if (this.tile != null) {
            this.tile.syncTile(true);
            this.tile.setChanged();
        }
        this.contentsChangedFuncOverride.ifPresent(c -> c.accept(slot, previousContents));
    }

    public static Builder builder(NonNullList<ItemStack> stacks, AbstractTilePM tile) {
        return new Builder(stacks, tile);
    }

    public static class Builder implements IItemHandlerPM.Builder {
        private final NonNullList<ItemStack> stacks;
        private final AbstractTilePM tile;
        private Optional<Function<Integer, Integer>> limitFuncOverride = Optional.empty();
        private Optional<BiPredicate<Integer, ItemStack>> validityFuncOverride = Optional.empty();
        private Optional<BiConsumer<Integer, ItemStack>> contentsChangedFuncOverride = Optional.empty();

        public Builder(NonNullList<ItemStack> stacks, AbstractTilePM tile) {
            this.stacks = stacks;
            this.tile = tile;
        }

        @Override
        public IItemHandlerPM.Builder slotLimitFunction(Function<Integer, Integer> limitFunction) {
            this.limitFuncOverride = Optional.of(limitFunction);
            return this;
        }

        @Override
        public IItemHandlerPM.Builder itemValidFunction(BiPredicate<Integer, ItemStack> itemValidFunction) {
            this.validityFuncOverride = Optional.of(itemValidFunction);
            return this;
        }

        @Override
        public IItemHandlerPM.Builder contentsChangedFunction(BiConsumer<Integer, ItemStack> contentsChangedFunction) {
            this.contentsChangedFuncOverride = Optional.of(contentsChangedFunction);
            return this;
        }

        @Override
        public IItemHandlerPM build() {
            return new ItemStackHandlerPMNeoforge(this.stacks, this.tile, this.limitFuncOverride,
                    this.validityFuncOverride, this.contentsChangedFuncOverride);
        }
    }
}
