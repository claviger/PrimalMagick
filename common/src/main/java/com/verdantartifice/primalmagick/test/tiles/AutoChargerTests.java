package com.verdantartifice.primalmagick.test.tiles;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.capabilities.IItemHandlerPM;
import com.verdantartifice.primalmagick.common.capabilities.ManaStorage;
import com.verdantartifice.primalmagick.common.components.DataComponentsPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.common.tiles.mana.AbstractManaFontTileEntity;
import com.verdantartifice.primalmagick.common.tiles.mana.AutoChargerTileEntity;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Tests for the auto charger: which items its item handler accepts, inserting and removing items by hand, and
 * siphoning mana from a nearby font into the charging item. Handler acceptance is checked for every kind of chargeable
 * item in ChargeableItem.
 */
public class AutoChargerTests extends AbstractBaseTest {
    private static ItemStack getChargeableTestStack() {
        return ChargeableItem.MUNDANE_WAND.makeStack();
    }

    private static ItemStack getUnchargeableTestStack() {
        return Items.STICK.getDefaultInstance();
    }

    private static IItemHandlerPM getItemHandlerForNewAutoCharger(GameTestHelper helper, BlockPos pos, Direction face) {
        return TileTestUtils.placeTileAndGetHandler(helper, pos, BlocksPM.AUTO_CHARGER.get(), AutoChargerTileEntity.class, face);
    }

    public static void auto_charger_output_allows_chargeable_items(GameTestHelper helper, ChargeableItem item) {
        var stack = item.makeStack();
        assertTrue(helper, ChargeableItem.hasManaStorage(stack), "Test stack " + stack + " has no mana storage");
        TileTestUtils.assertHandlerAccepts(helper, getItemHandlerForNewAutoCharger(helper, BlockPos.ZERO, Direction.NORTH), stack);
        helper.succeed();
    }

    public static void auto_charger_output_allows_chargeable_items(GameTestHelper helper) {
        auto_charger_output_allows_chargeable_items(helper, ChargeableItem.MUNDANE_WAND);
    }

    public static void auto_charger_output_does_not_allow_unchargeable_items(GameTestHelper helper) {
        TileTestUtils.assertHandlerRejects(helper, getItemHandlerForNewAutoCharger(helper, BlockPos.ZERO, Direction.NORTH), getUnchargeableTestStack());
        helper.succeed();
    }

    public static void auto_charger_output_does_not_allow_essence(GameTestHelper helper) {
        // Essence carries no mana storage, so it can't be charged
        TileTestUtils.assertHandlerRejects(helper, getItemHandlerForNewAutoCharger(helper, BlockPos.ZERO, Direction.NORTH), ItemsPM.ESSENCE_SHARD_EARTH.get().getDefaultInstance());
        helper.succeed();
    }

    public static void auto_charger_can_have_chargeable_items_inserted(GameTestHelper helper) {
        var stack = getChargeableTestStack();

        // Track a copy of the test stack for later
        var before = stack.copy();

        // Create a test player with a chargeable item in hand
        var player = makeMockServerPlayer(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        // Place an auto charger and get its item handler
        var chargerPos = BlockPos.ZERO;
        var handler = getItemHandlerForNewAutoCharger(helper, chargerPos, Direction.UP);
        assertTrue(helper, handler.getStackInSlot(0).isEmpty(), "Charger has an item before use");

        // Use the player's main hand item on the charger
        var chargerState = helper.getBlockState(chargerPos);
        var hitResult = new BlockHitResult(helper.absolutePos(chargerPos).getCenter(), Direction.UP, helper.absolutePos(chargerPos), true);
        var useResult = chargerState.useItemOn(stack, helper.getLevel(), player, InteractionHand.MAIN_HAND, hitResult);

        // Confirm success
        assertTrue(helper, useResult.consumesAction(), "Use action failed");
        assertFalse(helper, handler.getStackInSlot(0).isEmpty(), "Charger has no item after use");
        assertValueEqual(helper, before.getItem(), handler.getStackInSlot(0).getItem(), "Charge slot item");

        helper.succeed();
    }

    public static void auto_charger_cannot_have_unchargeable_items_inserted(GameTestHelper helper) {
        var stack = getUnchargeableTestStack();

        // Create a test player with an unchargeable item in hand
        var player = makeMockServerPlayer(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        // Place an auto charger and get its item handler
        var chargerPos = BlockPos.ZERO.south();
        var handler = getItemHandlerForNewAutoCharger(helper, chargerPos, Direction.UP);
        assertTrue(helper, handler.getStackInSlot(0).isEmpty(), "Charger has an item before use");

        // Use the player's main hand item on the charger
        var chargerState = helper.getBlockState(chargerPos);
        var hitResult = new BlockHitResult(helper.absolutePos(chargerPos).getCenter(), Direction.UP, helper.absolutePos(chargerPos), true);
        var useResult = chargerState.useItemOn(stack, helper.getLevel(), player, InteractionHand.MAIN_HAND, hitResult);

        // Confirm that the use was rejected and the charger is still empty
        assertFalse(helper, useResult.consumesAction(), "Use action unexpectedly succeeded");
        assertTrue(helper, handler.getStackInSlot(0).isEmpty(), "Charger has item after use");

        helper.succeed();
    }

    public static void auto_charger_can_have_chargeable_items_removed(GameTestHelper helper) {
        var stack = getChargeableTestStack();

        // Track a copy of the test stack for later
        var before = stack.copy();

        // Create a test player
        var player = makeMockServerPlayer(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

        // Place an auto charger, get its item handler, and insert the test stack
        var chargerPos = BlockPos.ZERO;
        var handler = getItemHandlerForNewAutoCharger(helper, chargerPos, Direction.UP);
        handler.insertItem(0, stack, false);
        assertFalse(helper, handler.getStackInSlot(0).isEmpty(), "Failed to set item in charger");

        // Use the player's empty main hand on the charger
        var chargerState = helper.getBlockState(chargerPos);
        var hitResult = new BlockHitResult(helper.absolutePos(chargerPos).getCenter(), Direction.UP, helper.absolutePos(chargerPos), true);
        var useResult = chargerState.useItemOn(ItemStack.EMPTY, helper.getLevel(), player, InteractionHand.MAIN_HAND, hitResult);

        // Confirm success
        assertTrue(helper, useResult.consumesAction(), "Use action failed");
        assertTrue(helper, handler.getStackInSlot(0).isEmpty(), "Charger has item after use");
        assertValueEqual(helper, before.getItem(), player.getItemInHand(InteractionHand.MAIN_HAND).getItem(), "Hand item");

        helper.succeed();
    }

    public static void auto_charger_siphons_into_chargeable_items(GameTestHelper helper) {
        var stack = getChargeableTestStack();

        // Place an auto charger block
        var chargerPos = BlockPos.ZERO.south();
        helper.setBlock(chargerPos, BlocksPM.AUTO_CHARGER.get());
        var chargerTile = helper.getBlockEntity(chargerPos, AutoChargerTileEntity.class);

        // Place an earth font block, half full so that its one-tick recharge can't be clipped by its 1000 centimana
        // capacity regardless of whether it ticks before or after the charger
        var fontPos = BlockPos.ZERO.east();
        helper.setBlock(fontPos, BlocksPM.ARTIFICIAL_FONT_EARTH.get());
        var fontTile = helper.getBlockEntity(fontPos, AbstractManaFontTileEntity.class);
        final int startFontMana = 500;
        fontTile.setMana(startFontMana);

        // Place the chargeable item stack into the auto charger
        var handler = chargerTile.getRawItemHandler(Direction.NORTH);
        assertFalse(helper, handler == null, "No item handler found");
        handler.insertItem(0, stack, false);

        // Confirm initial state
        var beforeStack = handler.getStackInSlot(0);
        assertFalse(helper, beforeStack.isEmpty(), "Stack not successfully inserted into charger");
        assertTrue(helper, beforeStack.has(DataComponentsPM.CAPABILITY_MANA_STORAGE.get()), "Before stack has no mana storage");
        assertValueEqual(helper, 0, beforeStack.getOrDefault(DataComponentsPM.CAPABILITY_MANA_STORAGE.get(), ManaStorage.EMPTY).getManaStored(Sources.EARTH), "Before stack not initially empty");
        assertValueEqual(helper, startFontMana, fontTile.getMana(), "Before font mana not as expected");

        // Confirm that mana was successfully siphoned; the mundane wand siphons 100 centimana, and the artificial font
        // recharges 1 centimana per tick, leaving 500 - 100 + 1 = 401
        final int expectedSiphonAmount = 100;
        final int expectedFontMana = 401;
        helper.succeedOnTickWhen(1, () -> {
            var afterStack = handler.getStackInSlot(0);
            assertFalse(helper, afterStack.isEmpty(), "After stack empty");
            assertTrue(helper, afterStack.has(DataComponentsPM.CAPABILITY_MANA_STORAGE.get()), "After stack has no mana storage");
            int afterStackMana = afterStack.getOrDefault(DataComponentsPM.CAPABILITY_MANA_STORAGE.get(), ManaStorage.EMPTY).getManaStored(Sources.EARTH);
            assertValueEqual(helper, expectedSiphonAmount, afterStackMana, "After stack mana total not as expected");
            assertValueEqual(helper, expectedFontMana, fontTile.getMana(), "After font mana not as expected");
        });
    }
}
