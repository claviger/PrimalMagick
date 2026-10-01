package com.verdantartifice.primalmagick.test.tiles;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.capabilities.IItemHandlerPM;
import com.verdantartifice.primalmagick.common.capabilities.ManaStorage;
import com.verdantartifice.primalmagick.common.components.DataComponentsPM;
import com.verdantartifice.primalmagick.common.items.essence.EssenceItem;
import com.verdantartifice.primalmagick.common.items.essence.EssenceType;
import com.verdantartifice.primalmagick.common.menus.WandChargerMenu;
import com.verdantartifice.primalmagick.common.sources.Source;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.common.tiles.mana.WandChargerTileEntity;
import com.verdantartifice.primalmagick.platform.Services;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;

/**
 * Tests for the wand charger: menu access, which items each face's item handler accepts, and charging a mundane wand
 * from essence. Expected charge amounts are hard-coded from the mana equivalents in EssenceType, capped at the mundane
 * wand's fixed 2500 centimana capacity (MundaneWandItem.MAX_MANA).
 */
public class WandChargerTests extends AbstractBaseTest {
    private static IItemHandlerPM getItemHandlerForNewWandCharger(GameTestHelper helper, Direction direction) {
        return TileTestUtils.placeTileAndGetHandler(helper, BlockPos.ZERO, BlocksPM.WAND_CHARGER.get(), WandChargerTileEntity.class, direction);
    }

    private static ItemStack getChargeSlotStack(WandChargerTileEntity tile) {
        return tile.getItem(WandChargerTileEntity.CHARGE_INV_INDEX, 0);
    }

    public static void wand_charger_can_have_its_menu_opened(GameTestHelper helper) {
        // Create a test player
        var player = makeMockServerPlayer(helper);

        // Place a wand charger block and get its block entity
        var pos = BlockPos.ZERO;
        helper.setBlock(pos, BlocksPM.WAND_CHARGER.get());
        var tile = helper.getBlockEntity(pos, WandChargerTileEntity.class);

        // Open the block entity menu
        Services.PLAYER.openMenu(player, tile, pos);
        assertInstanceOf(helper, player.containerMenu, WandChargerMenu.class, "Menu not of expected type");

        helper.succeed();
    }

    // Output (charge slot) handler tests

    public static void wand_charger_output_allows_chargeable_items(GameTestHelper helper, ChargeableItem item) {
        var stack = item.makeStack();
        assertTrue(helper, ChargeableItem.hasManaStorage(stack), "Test stack " + stack + " has no mana storage");
        TileTestUtils.assertHandlerAccepts(helper, getItemHandlerForNewWandCharger(helper, Direction.NORTH), stack);
        helper.succeed();
    }

    public static void wand_charger_output_allows_chargeable_items(GameTestHelper helper) {
        wand_charger_output_allows_chargeable_items(helper, ChargeableItem.MUNDANE_WAND);
    }

    public static void wand_charger_output_does_not_allow_unchargeable_items(GameTestHelper helper) {
        // Confirm that the output item handler will not accept the test item
        TileTestUtils.assertHandlerRejects(helper, getItemHandlerForNewWandCharger(helper, Direction.NORTH), TileTestUtils.getUnchargeableTestStack());
        helper.succeed();
    }

    public static void wand_charger_output_does_not_allow_essence(GameTestHelper helper) {
        // Essence is accepted by the input slot, but carries no mana storage and so can't be charged
        TileTestUtils.assertHandlerRejects(helper, getItemHandlerForNewWandCharger(helper, Direction.NORTH), EssenceItem.getEssence(EssenceType.SHARD, Sources.EARTH));
        helper.succeed();
    }

    // Input handler tests

    public static void wand_charger_input_allows_essence(GameTestHelper helper, EssenceType type) {
        TileTestUtils.assertHandlerAccepts(helper, getItemHandlerForNewWandCharger(helper, Direction.UP), EssenceItem.getEssence(type, Sources.EARTH));
        helper.succeed();
    }

    public static void wand_charger_input_allows_essence(GameTestHelper helper) {
        wand_charger_input_allows_essence(helper, EssenceType.SHARD);
    }

    public static void wand_charger_input_does_not_allow_non_essence(GameTestHelper helper) {
        // Confirm that the input item handler will not accept the test item
        TileTestUtils.assertHandlerRejects(helper, getItemHandlerForNewWandCharger(helper, Direction.UP), TileTestUtils.getUnchargeableTestStack());
        helper.succeed();
    }

    // Charging tests

    public static void wand_charger_can_charge_with_right_items(GameTestHelper helper) {
        var stack = TileTestUtils.getChargeableTestStack();

        // Place a wand charger block and get its block entity
        var pos = BlockPos.ZERO;
        helper.setBlock(pos, BlocksPM.WAND_CHARGER.get());
        var tile = helper.getBlockEntity(pos, WandChargerTileEntity.class);

        // Fill the block entity with essence and a chargeable item
        tile.addItem(WandChargerTileEntity.INPUT_INV_INDEX, 0, EssenceItem.getEssence(EssenceType.DUST, Sources.EARTH));
        tile.addItem(WandChargerTileEntity.CHARGE_INV_INDEX, 0, stack);

        // Confirm that the charger can charge with the inputs provided
        assertTrue(helper, tile.canCharge(), "Unable to charge");

        helper.succeed();
    }

    /**
     * Confirms that charging an empty mundane wand with one earth essence of the given type consumes the essence and
     * adds the expected amount of earth mana, and no mana of any other source.
     * <p>
     * These tests pin the current behaviour of WandChargerTileEntity.doCharge, where a whole essence is consumed even
     * when the mana added is capped by the wand's capacity, so the excess is lost. The crystal and cluster cases in
     * TestFunctionsPM rely on this by expecting the input to be emptied while the wand only reaches 2500 centimana; if
     * doCharge is changed to refund or keep essence when the charge is capped, update those cases too.
     */
    public static void wand_charger_do_charge_with_right_items(GameTestHelper helper, EssenceType type, int expectedCharge) {
        var source = Sources.EARTH;

        // Place a wand charger block and get its block entity
        var pos = BlockPos.ZERO;
        helper.setBlock(pos, BlocksPM.WAND_CHARGER.get());
        var tile = helper.getBlockEntity(pos, WandChargerTileEntity.class);

        // Fill the block entity with essence and a chargeable item
        tile.addItem(WandChargerTileEntity.INPUT_INV_INDEX, 0, EssenceItem.getEssence(type, source));
        tile.addItem(WandChargerTileEntity.CHARGE_INV_INDEX, 0, TileTestUtils.getChargeableTestStack());

        // Confirm that the charged stack starts with empty mana storage
        var startStack = getChargeSlotStack(tile);
        assertTrue(helper, startStack.has(DataComponentsPM.CAPABILITY_MANA_STORAGE.get()), "Stack has no starting mana storage");
        assertValueEqual(helper, 0, startStack.getOrDefault(DataComponentsPM.CAPABILITY_MANA_STORAGE.get(), ManaStorage.EMPTY).getManaStored(source), "Initial mana load");

        // Attempt the charge
        tile.doCharge();

        // Confirm that the essence was consumed and the stack was charged by the expected amount of the essence's source
        assertTrue(helper, tile.getItem(WandChargerTileEntity.INPUT_INV_INDEX, 0).isEmpty(), "Input stack not empty after charging with " + type.getSerializedName());
        var finalStack = getChargeSlotStack(tile);
        assertFalse(helper, finalStack.isEmpty(), "Charge stack empty");
        assertTrue(helper, finalStack.has(DataComponentsPM.CAPABILITY_MANA_STORAGE.get()), "Stack has no ending mana storage");
        var finalStorage = finalStack.getOrDefault(DataComponentsPM.CAPABILITY_MANA_STORAGE.get(), ManaStorage.EMPTY);
        for (Source s : Sources.getAllSorted()) {
            var expectedMana = s.equals(source) ? expectedCharge : 0;
            assertValueEqual(helper, expectedMana, finalStorage.getManaStored(s), "Final " + s.getId() + " mana load after charging with " + type.getSerializedName());
        }

        helper.succeed();
    }

    public static void wand_charger_do_charge_with_right_items(GameTestHelper helper) {
        // Dust is worth 100 centimana, well under the mundane wand's 2500 centimana cap
        wand_charger_do_charge_with_right_items(helper, EssenceType.DUST, 100);
    }

    /**
     * Confirms that charging replaces the charged stack's mana storage rather than mutating it in place, so that a copy
     * of the stack taken before charging keeps its original mana and is no longer component-equal to the charged stack.
     */
    public static void wand_charger_charge_does_not_mutate_stack_copies(GameTestHelper helper) {
        var source = Sources.EARTH;
        var stack = TileTestUtils.getChargeableTestStack();

        // Take a copy of the chargeable stack before it goes into the charger
        var before = stack.copy();

        // Place a wand charger block and fill it with essence and the chargeable item
        var pos = BlockPos.ZERO;
        helper.setBlock(pos, BlocksPM.WAND_CHARGER.get());
        var tile = helper.getBlockEntity(pos, WandChargerTileEntity.class);
        tile.addItem(WandChargerTileEntity.INPUT_INV_INDEX, 0, EssenceItem.getEssence(EssenceType.DUST, source));
        tile.addItem(WandChargerTileEntity.CHARGE_INV_INDEX, 0, stack);

        // Give the slotted stack a stale timestamp, as left by charging before the timestamp was retired
        getChargeSlotStack(tile).set(DataComponentsPM.LAST_UPDATED.get(), 1L);

        // Attempt the charge
        tile.doCharge();

        // Dust is worth 100 centimana, well under the mundane wand's 2500 centimana cap, so the charged stack goes from 0
        // to 100 while the copy stays at 0
        var charged = getChargeSlotStack(tile);
        assertValueEqual(helper, 100, charged.getOrDefault(DataComponentsPM.CAPABILITY_MANA_STORAGE.get(), ManaStorage.EMPTY).getManaStored(source), "Charged stack mana");
        assertValueEqual(helper, 0, before.getOrDefault(DataComponentsPM.CAPABILITY_MANA_STORAGE.get(), ManaStorage.EMPTY).getManaStored(source), "Pre-charge copy mana");
        assertFalse(helper, ItemStack.isSameItemSameComponents(before, charged), "Charged stack still matches pre-charge copy");
        assertFalse(helper, charged.has(DataComponentsPM.LAST_UPDATED.get()), "Charged stack has a last updated component");

        helper.succeed();
    }
}
