package com.verdantartifice.primalmagick.test.spells;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.items.wands.AbstractWandItem;
import com.verdantartifice.primalmagick.common.menus.SpellcraftingAltarMenu;
import com.verdantartifice.primalmagick.common.menus.WandInscriptionTableMenu;
import com.verdantartifice.primalmagick.common.research.ResearchEntries;
import com.verdantartifice.primalmagick.common.research.ResearchManager;
import com.verdantartifice.primalmagick.common.sources.Source;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.common.spells.SpellComponent;
import com.verdantartifice.primalmagick.common.spells.SpellManager;
import com.verdantartifice.primalmagick.common.spells.SpellPropertiesPM;
import com.verdantartifice.primalmagick.common.spells.mods.SpellModsPM;
import com.verdantartifice.primalmagick.common.spells.payloads.EarthDamageSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.SpellPayloadsPM;
import com.verdantartifice.primalmagick.common.spells.vehicles.SpellVehiclesPM;
import com.verdantartifice.primalmagick.common.spells.vehicles.TouchSpellVehicle;
import com.verdantartifice.primalmagick.common.wands.WandCap;
import com.verdantartifice.primalmagick.common.wands.WandCore;
import com.verdantartifice.primalmagick.common.wands.WandGem;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import com.verdantartifice.primalmagick.test.TestUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Tests of the spellcrafting altar and wand inscription table menus. Spellcrafting altar menu slot layout: 0 = result,
 * 1 = wand, 2 = blank scroll, 3-38 = player inventory.
 */
public class SpellcraftingAltarTests extends AbstractBaseTest {
    private static final int RESULT_SLOT = 0;
    private static final int WAND_SLOT = 1;
    private static final int SCROLL_SLOT = 2;

    private static BlockHitResult hitTop(GameTestHelper helper, BlockPos pos) {
        var absPos = helper.absolutePos(pos);
        return new BlockHitResult(absPos.getCenter(), Direction.UP, absPos, false);
    }

    private static SpellcraftingAltarMenu openAltar(GameTestHelper helper, ServerPlayer player, BlockPos pos) {
        helper.setBlock(pos, BlocksPM.SPELLCRAFTING_ALTAR.get());
        var result = player.gameMode.useItemOn(player, helper.getLevel(), ItemStack.EMPTY, InteractionHand.MAIN_HAND, hitTop(helper, pos));
        assertTrue(helper, result.consumesAction(), "Using the altar was not consumed: " + result);
        return assertInstanceOf(helper, player.containerMenu, SpellcraftingAltarMenu.class, "Menu not of expected type: " + player.containerMenu);
    }

    public static void spellcrafting_altar_can_have_its_menu_opened(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        assertFalse(helper, player.containerMenu instanceof SpellcraftingAltarMenu, "Player already has the altar menu open");
        openAltar(helper, player, BlockPos.ZERO.above());
        helper.succeed();
    }

    public static void wand_inscription_table_can_have_its_menu_opened(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var pos = BlockPos.ZERO.above();
        helper.setBlock(pos, BlocksPM.WAND_INSCRIPTION_TABLE.get());
        var result = player.gameMode.useItemOn(player, helper.getLevel(), ItemStack.EMPTY, InteractionHand.MAIN_HAND, hitTop(helper, pos));
        assertTrue(helper, result.consumesAction(), "Using the inscription table was not consumed: " + result);
        assertInstanceOf(helper, player.containerMenu, WandInscriptionTableMenu.class, "Menu not of expected type: " + player.containerMenu);
        helper.succeed();
    }

    public static void spellcrafting_altar_options_limited_to_unlocked_research(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);

        // With no research, no vehicle, payload or mod beyond the empty placeholders is offered
        assertValueEqual(helper, 1, SpellManager.getVehicleTypes(player).size(), "Vehicle types offered with no research");
        assertValueEqual(helper, 1, SpellManager.getPayloadTypes(player).size(), "Payload types offered with no research");
        assertValueEqual(helper, 1, SpellManager.getModTypes(player).size(), "Mod types offered with no research");

        // Basic Sorcery unlocks the touch and self vehicles and the earth damage payload, but no mods
        grant(helper, player, ResearchEntries.BASIC_SORCERY, "Failed to complete Basic Sorcery");
        var vehicles = SpellManager.getVehicleTypes(player);
        var payloads = SpellManager.getPayloadTypes(player);
        assertValueEqual(helper, 3, vehicles.size(), "Vehicle types offered after Basic Sorcery");
        assertTrue(helper, vehicles.contains(SpellVehiclesPM.TOUCH.get()), "Touch vehicle not offered after Basic Sorcery");
        assertTrue(helper, vehicles.contains(SpellVehiclesPM.SELF.get()), "Self vehicle not offered after Basic Sorcery");
        assertFalse(helper, vehicles.contains(SpellVehiclesPM.BOLT.get()), "Bolt vehicle offered before its research");
        assertFalse(helper, vehicles.contains(SpellVehiclesPM.PROJECTILE.get()), "Projectile vehicle offered before its research");
        assertValueEqual(helper, 2, payloads.size(), "Payload types offered after Basic Sorcery");
        assertTrue(helper, payloads.contains(SpellPayloadsPM.EARTH_DAMAGE.get()), "Earth damage payload not offered after Basic Sorcery");
        assertFalse(helper, payloads.contains(SpellPayloadsPM.FROST_DAMAGE.get()), "Frost damage payload offered before its research");
        assertValueEqual(helper, 1, SpellManager.getModTypes(player).size(), "Mod types offered after Basic Sorcery");

        // The altar menu will not select past the last unlocked payload
        var menu = openAltar(helper, player, BlockPos.ZERO.above());
        menu.setSpellPayloadTypeIndex(50);
        assertTrue(helper, menu.getSpellPackage().payload().getComponent() instanceof EarthDamageSpellPayload, "Menu payload selection went beyond the unlocked payloads: " + menu.getSpellPackage().payload().getComponent());

        // Unlocking further research makes the corresponding option appear
        grant(helper, player, ResearchEntries.SPELL_PAYLOAD_FROST, "Failed to complete frost payload research");
        grant(helper, player, ResearchEntries.SPELL_VEHICLE_BOLT, "Failed to complete bolt vehicle research");
        grant(helper, player, ResearchEntries.SPELL_MOD_AMPLIFY, "Failed to complete amplify mod research");
        assertTrue(helper, SpellManager.getPayloadTypes(player).contains(SpellPayloadsPM.FROST_DAMAGE.get()), "Frost damage payload not offered after its research");
        assertTrue(helper, SpellManager.getVehicleTypes(player).contains(SpellVehiclesPM.BOLT.get()), "Bolt vehicle not offered after its research");
        assertTrue(helper, SpellManager.getModTypes(player).contains(SpellModsPM.AMPLIFY.get()), "Amplify mod not offered after its research");
        helper.succeed();
    }

    public static void spellcrafting_altar_creates_scroll_as_configured(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        grant(helper, player, ResearchEntries.BASIC_SORCERY, "Failed to complete Basic Sorcery");
        var menu = openAltar(helper, player, BlockPos.ZERO.above());

        // Put a full wand and a blank scroll into the altar
        ItemStack wandStack = TestUtils.makeModularCaster(ItemsPM.MODULAR_WAND.get(), WandCore.HEARTWOOD, WandCap.IRON, WandGem.APPRENTICE);
        var wand = assertInstanceOf(helper, wandStack.getItem(), AbstractWandItem.class, "Wand stack is not a wand as expected");
        Sources.getAll().forEach(s -> wand.addMana(wandStack, s, wand.getMaxMana(wandStack, s)));
        menu.getSlot(WAND_SLOT).set(wandStack);
        menu.getSlot(SCROLL_SLOT).set(new ItemStack(ItemsPM.SPELL_SCROLL_BLANK.get()));
        assertTrue(helper, menu.getSlot(RESULT_SLOT).getItem().isEmpty(), "Result slot has an output before the spell is configured");

        // Configure the spell as a Touch vehicle with an Earth Damage payload at power 2, with a custom name
        menu.setSpellVehicleTypeIndex(SpellManager.getVehicleTypes(player).indexOf(SpellVehiclesPM.TOUCH.get()));
        menu.setSpellPayloadTypeIndex(SpellManager.getPayloadTypes(player).indexOf(SpellPayloadsPM.EARTH_DAMAGE.get()));
        menu.setSpellName("Test Name");
        menu.setSpellPropertyValue(SpellComponent.PAYLOAD, SpellPropertiesPM.POWER.get(), 2);

        // The previewed result is a scroll holding exactly the configured spell
        ItemStack preview = menu.getSlot(RESULT_SLOT).getItem();
        assertTrue(helper, preview.is(ItemsPM.SPELL_SCROLL_FILLED.get()), "Preview is not a filled spell scroll: " + preview);
        var scrollItem = ItemsPM.SPELL_SCROLL_FILLED.get();
        var spell = scrollItem.getSpell(preview);
        assertTrue(helper, spell != null, "Preview scroll holds no spell");
        assertValueEqual(helper, "Test Name", spell.name(), "Spell name");
        assertValueEqual(helper, TouchSpellVehicle.INSTANCE, spell.vehicle().getComponent(), "Spell vehicle");
        assertValueEqual(helper, EarthDamageSpellPayload.INSTANCE, spell.payload().getComponent(), "Spell payload");
        assertValueEqual(helper, 2, spell.payload().getPropertyValue(SpellPropertiesPM.POWER.get()), "Spell payload power");
        assertValueEqual(helper, 0, spell.getActiveModCount(), "Active mod count");

        // Taking the result consumes the blank scroll, charges the wand, and puts the finished scroll in the player's inventory.
        // Earth damage at power 2 costs (1 << 1) + ((1 << 1) >> 1) = 3 mana of Earth before any wand discount.
        final int startMana = wand.getMana(wandStack, Sources.EARTH);
        menu.quickMoveStack(player, RESULT_SLOT);
        // The altar's wand slot holds its own copy of the wand, which is the one that is charged
        ItemStack slotWand = menu.getWand();
        assertTrue(helper, menu.getSlot(SCROLL_SLOT).getItem().isEmpty(), "Blank scroll not consumed");
        ItemStack given = findInInventory(player, ItemsPM.SPELL_SCROLL_FILLED.get());
        assertTrue(helper, !given.isEmpty(), "Player did not receive the scroll");
        assertValueEqual(helper, spell, scrollItem.getSpell(given), "Spell on the scroll the player received");
        int spent = startMana - wand.getMana(slotWand, Sources.EARTH);
        assertTrue(helper, spent > 0 && spent <= 300, "Wand Earth mana spent is not as expected for a 300 centimana spell: " + spent + " (start " + startMana + ", now " + wand.getMana(slotWand, Sources.EARTH) + ", cost " + menu.getManaCosts() + ")");
        for (Source other : Sources.getAll()) {
            if (!other.equals(Sources.EARTH)) {
                assertValueEqual(helper, wand.getMaxMana(wandStack, other), wand.getMana(slotWand, other), "Wand mana of unrelated source " + other.getId());
            }
        }
        helper.succeed();
    }

    private static void grant(GameTestHelper helper, ServerPlayer player, net.minecraft.resources.ResourceKey<com.verdantartifice.primalmagick.common.research.ResearchEntry> key, String failureMessage) {
        ResearchManager.forceGrantWithAllParents(player, key);
        assertTrue(helper, ResearchManager.isResearchComplete(player, key), failureMessage);
    }

    private static ItemStack findInInventory(ServerPlayer player, Item item) {
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(item)) {
                return inventory.getItem(i);
            }
        }
        return ItemStack.EMPTY;
    }
}
