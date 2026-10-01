package com.verdantartifice.primalmagick.test.tiles;

import com.verdantartifice.primalmagick.common.components.DataComponentsPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.items.wands.IHasWandComponents;
import com.verdantartifice.primalmagick.common.wands.WandCap;
import com.verdantartifice.primalmagick.common.wands.WandCore;
import com.verdantartifice.primalmagick.common.wands.WandGem;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * Items that carry a mana storage component and can therefore be placed in the charge slot of the mana tiles (mana
 * batteries, auto chargers, and wand chargers). Each constant builds a fresh stack so that tests stay independent.
 */
public enum ChargeableItem {
    // Mundane wands get a fixed 2500 centimana storage from MundaneWandItem.getDefaultInstance
    MUNDANE_WAND(() -> ItemsPM.MUNDANE_WAND.get().getDefaultInstance(), true),

    // Modular casters only get a mana storage component once their components are set. They use a gold cap (siphon
    // amount 200) so that siphon tests can tell the cap's value apart from the iron fallback and the mundane wand (100).
    MODULAR_WAND(() -> IHasWandComponents.setWandComponents(ItemsPM.MODULAR_WAND.get().getDefaultInstance(), WandCore.HEARTWOOD, WandCap.GOLD, WandGem.APPRENTICE), true),
    MODULAR_STAFF(() -> IHasWandComponents.setWandComponents(ItemsPM.MODULAR_STAFF.get().getDefaultInstance(), WandCore.HEARTWOOD, WandCap.GOLD, WandGem.APPRENTICE), true),

    // Applying a warding module to wardable armor adds an earth-only mana storage component to the armor stack
    WARDED_PRIMALITE_CHEST(() -> ItemsPM.BASIC_WARDING_MODULE.get().applyWard(ItemsPM.PRIMALITE_CHEST.get().getDefaultInstance()), false);

    private final Supplier<ItemStack> stackSupplier;
    private final boolean caster;

    ChargeableItem(Supplier<ItemStack> stackSupplier, boolean caster) {
        this.stackSupplier = stackSupplier;
        this.caster = caster;
    }

    public ItemStack makeStack() {
        return this.stackSupplier.get();
    }

    /**
     * Returns whether this item is a wand or staff, as opposed to other mana-storing equipment such as warded armor.
     */
    public boolean isCaster() {
        return this.caster;
    }

    /**
     * Returns whether the given stack carries the mana storage component that makes it chargeable, so that tests can
     * confirm their precondition before asserting how a tile treats the stack.
     */
    public static boolean hasManaStorage(ItemStack stack) {
        return stack.has(DataComponentsPM.CAPABILITY_MANA_STORAGE.get());
    }
}
