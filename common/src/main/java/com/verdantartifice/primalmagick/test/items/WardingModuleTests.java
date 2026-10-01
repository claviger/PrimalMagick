package com.verdantartifice.primalmagick.test.items;

import com.verdantartifice.primalmagick.common.capabilities.ManaStorage;
import com.verdantartifice.primalmagick.common.components.DataComponentsPM;
import com.verdantartifice.primalmagick.common.events.PlayerEvents;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.platform.Services;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import com.verdantartifice.primalmagick.test.tiles.ChargeableItem;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Tests for warded armor: regenerating a player's ward from the earth mana stored in their warded equipment. Mana
 * amounts are hard-coded from WardingModuleItem, whose regeneration cost is 500 centimana per point of ward.
 */
public class WardingModuleTests extends AbstractBaseTest {
    /**
     * Confirms that ward regeneration replaces the armor stack's mana storage rather than mutating it in place, so that
     * a copy of the armor taken beforehand keeps its original mana.
     */
    public static void ward_regeneration_does_not_mutate_stack_copies(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);

        // Equip a warded chestplate holding 1000 centimana of earth mana, enough for two points of ward
        var armor = ChargeableItem.WARDED_PRIMALITE_CHEST.makeStack();
        armor.update(DataComponentsPM.CAPABILITY_MANA_STORAGE.get(), ManaStorage.EMPTY, storage -> storage.copyWith(Sources.EARTH, 1000));
        assertValueEqual(helper, 1000, armor.getOrDefault(DataComponentsPM.CAPABILITY_MANA_STORAGE.get(), ManaStorage.EMPTY).getManaStored(Sources.EARTH), "Starting armor mana");
        player.setItemSlot(EquipmentSlot.CHEST, armor);
        var before = player.getItemBySlot(EquipmentSlot.CHEST).copy();

        // Give the player an empty ward with room to regenerate; regeneration is never paused for a fresh player
        var ward = Services.CAPABILITIES.ward(player).orElse(null);
        assertFalse(helper, ward == null, "Player has no ward capability");
        ward.setMaxWard(10);
        assertTrue(helper, ward.isRegenerating(), "Ward is not regenerating");

        PlayerEvents.handleWardRegeneration(player);

        // One point of ward costs 500 centimana, so the equipped armor drops from 1000 to 500 while the copy stays at 1000
        var equipped = player.getItemBySlot(EquipmentSlot.CHEST);
        assertValueEqual(helper, 1.0F, ward.getCurrentWard(), "Ward after regeneration");
        assertValueEqual(helper, 500, equipped.getOrDefault(DataComponentsPM.CAPABILITY_MANA_STORAGE.get(), ManaStorage.EMPTY).getManaStored(Sources.EARTH), "Equipped armor mana");
        assertValueEqual(helper, 1000, before.getOrDefault(DataComponentsPM.CAPABILITY_MANA_STORAGE.get(), ManaStorage.EMPTY).getManaStored(Sources.EARTH), "Pre-regeneration copy mana");
        assertFalse(helper, ItemStack.isSameItemSameComponents(before, equipped), "Equipped armor still matches pre-regeneration copy");
        helper.succeed();
    }
}
