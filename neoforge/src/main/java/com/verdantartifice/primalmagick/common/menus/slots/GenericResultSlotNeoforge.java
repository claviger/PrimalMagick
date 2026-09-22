package com.verdantartifice.primalmagick.common.menus.slots;

import com.verdantartifice.primalmagick.common.capabilities.IItemHandlerNeoforge;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

/**
 * Custom GUI slot for generic device outputs.
 * 
 * @author Daedalus4096
 */
public class GenericResultSlotNeoforge extends ResourceHandlerSlot {
    protected final Player player;
    protected int removeCount = 0;

    public GenericResultSlotNeoforge(Player player, IItemHandlerNeoforge inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn.getResourceHandler(), inventoryIn.getIndexModifier(), index, xPosition, yPosition);
        this.player = player;
    }
    
    @Override
    public boolean mayPlace(ItemStack stack) {
        // Don't allow anything to be dropped into the slot
        return false;
    }
    
    @Override
    public void onTake(@NotNull Player thePlayer, @NotNull ItemStack stack) {
        if (this.removeCount == 0) {
            // Quick crafting counts the taken items as it goes; a direct pickup counts them here
            this.removeCount = stack.getCount();
        }
        this.checkTakeAchievements(stack);
        super.onTake(thePlayer, stack);
    }
    
    @Override
    public void onQuickCraft(ItemStack oldStackIn, ItemStack newStackIn) {
        // Restore functionality occluded by ResourceHandlerSlot
        int delta = newStackIn.getCount() - oldStackIn.getCount();
        if (delta > 0) {
            this.onQuickCraft(newStackIn, delta);
        }
    }

    @Override
    protected void onQuickCraft(@NotNull ItemStack stack, int amount) {
        this.removeCount += amount;
        this.checkTakeAchievements(stack);
    }
    
    @Override
    protected void checkTakeAchievements(ItemStack stack) {
        stack.onCraftedBy(this.player, this.removeCount);
        this.removeCount = 0;
    }
}
