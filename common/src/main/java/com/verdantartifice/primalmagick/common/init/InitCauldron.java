package com.verdantartifice.primalmagick.common.init;

import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.items.concoctions.BombCasingItem;
import com.verdantartifice.primalmagick.common.items.concoctions.ConcoctionItem;
import com.verdantartifice.primalmagick.common.items.concoctions.SkyglassFlaskItem;
import com.verdantartifice.primalmagick.common.items.entities.FlyingCarpetItem;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.apache.commons.lang3.function.TriConsumer;

/**
 * Point of registration for cauldron interactions.
 * 
 * @author Daedalus4096
 */
public class InitCauldron {
    private static final Identifier EMPTY = Identifier.withDefaultNamespace("empty");
    private static final Identifier WATER = Identifier.withDefaultNamespace("water");

    public static void initCauldronInteractions(TriConsumer<Identifier, Item, CauldronInteraction> registrar) {
        registrar.accept(EMPTY, ItemsPM.CONCOCTION.get(), ConcoctionItem.FILL_EMPTY_CAULDRON);
        registrar.accept(WATER, ItemsPM.FLYING_CARPET.get(), FlyingCarpetItem.DYED_CARPET);
        registrar.accept(WATER, ItemsPM.SKYGLASS_FLASK.get(), SkyglassFlaskItem.FILL_CONCOCTION);
        registrar.accept(WATER, ItemsPM.BOMB_CASING.get(), BombCasingItem.FILL_BOMB);
        registrar.accept(WATER, ItemsPM.CONCOCTION.get(), ConcoctionItem.FILL_WATER_CAULDRON);
    }
}
