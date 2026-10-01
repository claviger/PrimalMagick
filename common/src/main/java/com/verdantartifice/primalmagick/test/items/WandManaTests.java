package com.verdantartifice.primalmagick.test.items;

import com.verdantartifice.primalmagick.common.components.DataComponentsPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.sources.Source;
import com.verdantartifice.primalmagick.common.sources.SourceList;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.common.wands.IWand;
import com.verdantartifice.primalmagick.common.wands.WandCap;
import com.verdantartifice.primalmagick.common.wands.WandCore;
import com.verdantartifice.primalmagick.common.wands.WandGem;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import com.verdantartifice.primalmagick.test.TestUtils;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class WandManaTests extends AbstractBaseTest {
    /**
     * Source used by the tests that are not parameterized by source.
     */
    protected static final Source DEFAULT_SOURCE = Sources.EARTH;

    /**
     * The kinds of wand that the mana tests can be run against, along with the hard-coded mana behaviour expected of each.
     */
    public enum WandType {
        // Heartwood core, iron cap, apprentice gem. WandGem.APPRENTICE holds 7500 centimana, and the iron cap's 10%
        // cost modifier makes a 100 centimana charge cost floor(100 / 1.10) = 90. Heartwood has no aligned sources,
        // so the cost is the same for every source.
        MODULAR_WAND(() -> TestUtils.makeModularCaster(ItemsPM.MODULAR_WAND.get(), WandCore.HEARTWOOD, WandCap.IRON, WandGem.APPRENTICE), 7500, 90),

        // Same components as the modular wand; staves take their capacity and cost modifier from the same gem and cap
        MODULAR_STAFF(() -> TestUtils.makeModularCaster(ItemsPM.MODULAR_STAFF.get(), WandCore.HEARTWOOD, WandCap.IRON, WandGem.APPRENTICE), 7500, 90),

        // Mundane wands have no gem or cap. MundaneWandItem.MAX_MANA is a fixed 2500 centimana and the base cost
        // modifier is 0, so a 100 centimana charge costs exactly 100.
        MUNDANE_WAND(() -> ItemsPM.MUNDANE_WAND.get().getDefaultInstance(), 2500, 100);

        private final Supplier<ItemStack> stackSupplier;
        private final int maxCentimana;
        private final int costOf100Centimana;

        WandType(Supplier<ItemStack> stackSupplier, int maxCentimana, int costOf100Centimana) {
            this.stackSupplier = stackSupplier;
            this.maxCentimana = maxCentimana;
            this.costOf100Centimana = costOf100Centimana;
        }

        public ItemStack makeStack() {
            return this.stackSupplier.get();
        }

        public int getMaxCentimana() {
            return this.maxCentimana;
        }

        public int getCostOf100Centimana() {
            return this.costOf100Centimana;
        }
    }

    public static void wand_can_get_and_add_mana(GameTestHelper helper, Source source) {
        wand_can_get_and_add_mana(helper, source, WandType.MODULAR_WAND);
    }

    public static void wand_can_get_and_add_mana(GameTestHelper helper, Source source, WandType wandType) {
        var wandStack = wandType.makeStack();

        // Confirm that the wand was created successfully
        IWand wand = assertInstanceOf(helper, wandStack.getItem(), IWand.class, "Wand stack is not a wand as expected");

        // Confirm that the wand reports the capacity expected for its type
        assertValueEqual(helper, wandType.getMaxCentimana(), wand.getMaxMana(wandStack, source), "Wand max mana for " + source.getId());

        // Confirm that the wand is empty at first
        assertValueEqual(helper, 0, wand.getMana(wandStack, source), "Starting wand mana for " + source.getId());

        // Add a point of centimana to the wand
        assertValueEqual(helper, 0, wand.addMana(wandStack, source, 1), "Overflow when adding centimana to wand for " + source.getId());

        // Confirm that the wand has mana in it
        assertValueEqual(helper, 1, wand.getMana(wandStack, source), "Wand mana total for " + source.getId());

        helper.succeed();
    }

    public static void wand_can_get_and_add_real_mana(GameTestHelper helper) {
        var wandStack = WandType.MODULAR_WAND.makeStack();

        // Confirm that the wand was created successfully
        IWand wand = assertInstanceOf(helper, wandStack.getItem(), IWand.class, "Wand stack is not a wand as expected");

        // Confirm that the wand is empty at first
        assertValueEqual(helper, 0, wand.getMana(wandStack, DEFAULT_SOURCE), "Starting wand mana");

        // Add a point of real mana to the wand
        assertValueEqual(helper, 0, wand.addMana(wandStack, DEFAULT_SOURCE, 100), "Overflow when adding real mana to wand");

        // Confirm that the wand has mana in it
        assertValueEqual(helper, 100, wand.getMana(wandStack, DEFAULT_SOURCE), "Wand mana total");

        helper.succeed();
    }

    /**
     * Confirms that adding mana to a wand replaces its mana storage rather than mutating it in place, so that a copy of
     * the wand taken beforehand keeps its original mana and is no longer component-equal to the wand.
     */
    public static void wand_mana_change_does_not_mutate_stack_copies(GameTestHelper helper) {
        var wandStack = WandType.MODULAR_WAND.makeStack();

        // Confirm that the wand was created successfully
        IWand wand = assertInstanceOf(helper, wandStack.getItem(), IWand.class, "Wand stack is not a wand as expected");

        // Give the wand some mana, then take a copy of it
        assertValueEqual(helper, 0, wand.addMana(wandStack, DEFAULT_SOURCE, 100), "Overflow when adding first mana to wand");
        var before = wandStack.copy();

        // Add more mana to the original wand; it goes from 100 to 200 centimana while the copy stays at 100
        assertValueEqual(helper, 0, wand.addMana(wandStack, DEFAULT_SOURCE, 100), "Overflow when adding second mana to wand");
        assertValueEqual(helper, 200, wand.getMana(wandStack, DEFAULT_SOURCE), "Wand mana total");
        assertValueEqual(helper, 100, wand.getMana(before, DEFAULT_SOURCE), "Copy mana total");
        assertFalse(helper, ItemStack.isSameItemSameComponents(before, wandStack), "Wand still matches earlier copy");
        assertFalse(helper, wandStack.has(DataComponentsPM.LAST_UPDATED.get()), "Wand has a last updated component");

        helper.succeed();
    }

    public static void wand_cannot_add_too_much_mana(GameTestHelper helper, Source source) {
        var wandType = WandType.MODULAR_WAND;
        var wandStack = wandType.makeStack();

        // Confirm that the wand was created successfully
        IWand wand = assertInstanceOf(helper, wandStack.getItem(), IWand.class, "Wand stack is not a wand as expected");

        int maxCentimana = wandType.getMaxCentimana();
        int attemptedRealMana = 100000;
        int attemptedCentimana = 100 * attemptedRealMana;
        int expectedOverflow = attemptedCentimana - maxCentimana;
        assertValueEqual(helper, maxCentimana, wand.getMaxMana(wandStack, source), "Wand max mana for " + source.getId());
        int actualOverflow = wand.addMana(wandStack, source, attemptedCentimana);

        // Confirm that the wand is full and the overfill is as expected
        assertValueEqual(helper, maxCentimana, wand.getMana(wandStack, source), "Wand mana total for " + source.getId());
        assertValueEqual(helper, expectedOverflow, actualOverflow, "Wand overfill for " + source.getId());

        helper.succeed();
    }

    public static void wand_can_get_all_mana(GameTestHelper helper) {
        var wandStack = WandType.MODULAR_WAND.makeStack();

        // Confirm that the wand was created successfully
        IWand wand = assertInstanceOf(helper, wandStack.getItem(), IWand.class, "Wand stack is not a wand as expected");

        // Add a point of real mana to the wand for each source *except* the test source
        Sources.stream().filter(s -> !s.equals(DEFAULT_SOURCE)).forEach(s -> wand.addMana(wandStack, s, 100));

        // Create a source list of centimana to be expected; all sources *except* the test source
        var sourceListBuilder = SourceList.builder();
        Sources.stream().filter(s -> !s.equals(DEFAULT_SOURCE)).forEach(s -> sourceListBuilder.with(s, 100));
        var sourceList = sourceListBuilder.build();

        // Confirm that the wand has the expected amount of mana in it
        assertValueEqual(helper, sourceList, wand.getAllMana(wandStack), "Wand mana totals");

        helper.succeed();
    }

    public static void wand_can_consume_mana(GameTestHelper helper, Source source) {
        wand_can_consume_mana(helper, source, WandType.MODULAR_WAND);
    }

    public static void wand_can_consume_mana(GameTestHelper helper, Source source, WandType wandType) {
        var player = makeMockServerPlayer(helper);
        var wandStack = wandType.makeStack();

        // Confirm that the wand was created successfully
        IWand wand = assertInstanceOf(helper, wandStack.getItem(), IWand.class, "Wand stack is not a wand as expected");

        final int startingRealMana = 10;
        final int startingCentimana = 100 * startingRealMana;
        final int consumedCentimana = 100;
        final int expectedCost = wandType.getCostOf100Centimana();
        final int expectedCentimana = startingCentimana - expectedCost;

        // Confirm that the wand applies the cost modifier expected for its type
        assertValueEqual(helper, expectedCost, wand.getModifiedCost(wandStack, player, source, consumedCentimana, helper.getLevel().registryAccess()), "Modified cost for " + source.getId());

        // Add some real mana to the wand
        assertValueEqual(helper, 0, wand.addMana(wandStack, source, startingCentimana), "Overflow when adding real mana to wand for " + source.getId());

        // Confirm that a few points of centimana can be consumed
        assertTrue(helper, wand.consumeMana(wandStack, player, source, consumedCentimana, helper.getLevel().registryAccess()), "Failed to consume mana from wand for " + source.getId());

        // Confirm that the mana was deducted correctly
        assertValueEqual(helper, expectedCentimana, wand.getMana(wandStack, source), "Mana total for " + source.getId());

        helper.succeed();
    }

    public static void wand_cannot_consume_more_mana_than_it_has(GameTestHelper helper, Source source) {
        var player = makeMockServerPlayer(helper);
        var wandStack = WandType.MODULAR_WAND.makeStack();

        // Confirm that the wand was created successfully
        IWand wand = assertInstanceOf(helper, wandStack.getItem(), IWand.class, "Wand stack is not a wand as expected");

        // Add a point of real mana to the wand
        assertValueEqual(helper, 0, wand.addMana(wandStack, source, 100), "Overflow when adding real mana to wand for " + source.getId());

        // Confirm that attempting to consume more mana than the wand has fails; even with the iron cap's discount,
        // 200 centimana costs floor(200 / 1.10) = 181, which is more than the 100 held
        assertFalse(helper, wand.consumeMana(wandStack, player, source, 200, helper.getLevel().registryAccess()), "Consumption of 200 centimana of " + source.getId() + " succeeded with only 100 held");

        // Confirm that the wand still has the mana it started with
        assertValueEqual(helper, 100, wand.getMana(wandStack, source), "Wand mana total for " + source.getId());

        helper.succeed();
    }

    public static void wand_can_consume_multiple_types_of_mana(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var wandStack = WandType.MODULAR_WAND.makeStack();

        // Confirm that the wand was created successfully
        IWand wand = assertInstanceOf(helper, wandStack.getItem(), IWand.class, "Wand stack is not a wand as expected");

        final int startingRealMana = 10;
        final int startingCentimana = 100 * startingRealMana;
        final int consumedCentimana = 100;
        final int finalCost = wand.getModifiedCost(wandStack, player, DEFAULT_SOURCE, consumedCentimana, helper.getLevel().registryAccess());
        final int expectedCentimana = startingCentimana - finalCost;

        // Add some real mana to the wand for each source
        Sources.getAll().forEach(s -> {
            assertValueEqual(helper, 0, wand.addMana(wandStack, s, startingCentimana), "Overflow when adding real mana to wand for " + s.getId());
        });

        // Create a source list of centimana to be deducted; all sources *except* the test source
        var sourceListBuilder = SourceList.builder();
        Sources.stream().filter(s -> !s.equals(DEFAULT_SOURCE)).forEach(s -> sourceListBuilder.with(s, consumedCentimana));
        var sourceList = sourceListBuilder.build();

        // Confirm that the centimana can be consumed
        assertTrue(helper, wand.consumeMana(wandStack, player, sourceList, helper.getLevel().registryAccess()), "Failed to consume mana from wand");

        // Confirm that the mana was deducted correctly for each source
        Sources.getAll().forEach(s -> {
            var expected = s.equals(DEFAULT_SOURCE) ? startingCentimana : expectedCentimana;
            assertValueEqual(helper, expected, wand.getMana(wandStack, s), "Mana total for " + s.getId());
        });

        helper.succeed();
    }

    public static void wand_cannot_consume_more_mana_than_it_has_with_multiple_types(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var wandStack = WandType.MODULAR_WAND.makeStack();

        // Confirm that the wand was created successfully
        IWand wand = assertInstanceOf(helper, wandStack.getItem(), IWand.class, "Wand stack is not a wand as expected");

        // Add a point of real mana to the wand for every source
        Sources.getAll().forEach(s -> wand.addMana(wandStack, s, 100));

        // Create a source list of centimana to be deducted; all sources *except* for the test source
        var sourceListBuilder = SourceList.builder();
        Sources.stream().filter(s -> !s.equals(DEFAULT_SOURCE)).forEach(s -> sourceListBuilder.with(s, 500));
        var sourceList = sourceListBuilder.build();

        // Confirm that attempting to deduct more mana than the wand has fails
        assertFalse(helper, wand.consumeMana(wandStack, player, sourceList, helper.getLevel().registryAccess()), "Mana consumption succeeded when it shouldn't have");

        // Confirm that the wand's mana is still in its original state for every source
        Sources.getAll().forEach(s -> assertValueEqual(helper, 100, wand.getMana(wandStack, s), "Mana total for " + s.getId()));

        helper.succeed();
    }

    public static void wand_can_remove_mana_raw(GameTestHelper helper) {
        var wandStack = WandType.MODULAR_WAND.makeStack();

        // Confirm that the wand was created successfully
        IWand wand = assertInstanceOf(helper, wandStack.getItem(), IWand.class, "Wand stack is not a wand as expected");

        final int startingRealMana = 1;
        final int startingCentimana = 100 * startingRealMana;
        final int removedCentimana = 10;
        final int expectedCentimana = startingCentimana - removedCentimana;

        // Add some real mana to the wand for the test source
        assertValueEqual(helper, 0, wand.addMana(wandStack, DEFAULT_SOURCE, startingCentimana), "Overflow when adding mana to wand");

        // Confirm that a few points of centimana can be consumed
        assertTrue(helper, wand.removeManaRaw(wandStack, DEFAULT_SOURCE, removedCentimana), "Failed to remove mana from wand");

        // Confirm that the mana was deducted correctly
        assertValueEqual(helper, expectedCentimana, wand.getMana(wandStack, DEFAULT_SOURCE), "Wand mana total");

        helper.succeed();
    }

    public static void wand_cannot_remove_more_raw_mana_than_it_has(GameTestHelper helper) {
        var wandStack = WandType.MODULAR_WAND.makeStack();

        // Confirm that the wand was created successfully
        IWand wand = assertInstanceOf(helper, wandStack.getItem(), IWand.class, "Wand stack is not a wand as expected");

        // Add some real mana to the wand for the test source
        assertValueEqual(helper, 0, wand.addMana(wandStack, DEFAULT_SOURCE, 100), "Overflow when adding mana to wand");

        // Confirm that attempting to remove more than that fails
        assertFalse(helper, wand.removeManaRaw(wandStack, DEFAULT_SOURCE, 200), "Mana removal succeeded when it shouldn't have");

        // Confirm that the wand's mana is still in its starting state
        assertValueEqual(helper, 100, wand.getMana(wandStack, DEFAULT_SOURCE), "Wand mana total");

        helper.succeed();
    }

    public static void wand_contains_mana(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var wandStack = WandType.MODULAR_WAND.makeStack();

        // Confirm that the wand was created successfully
        IWand wand = assertInstanceOf(helper, wandStack.getItem(), IWand.class, "Wand stack is not a wand as expected");

        final int startingRealMana = 10;
        final int startingCentimana = 100 * startingRealMana;
        final double costModifier = 1 + (wand.getTotalCostModifier(wandStack, player, DEFAULT_SOURCE, helper.getLevel().registryAccess()) / 100D);
        final int exactCentimana = (int)(startingCentimana * costModifier);
        final int lessCentimana = exactCentimana - 10;
        final int greaterCentimana = exactCentimana + 10;

        // Add some real mana to the wand for the test source
        assertValueEqual(helper, 0, wand.addMana(wandStack, DEFAULT_SOURCE, startingCentimana), "Overflow when adding real mana to wand");

        // Confirm that the wand recognizes it contains centimana up to the threshold of what it was given
        assertValueEqual(helper, startingCentimana, wand.getMana(wandStack, DEFAULT_SOURCE), "Mana total for source " + DEFAULT_SOURCE.getId());
        assertTrue(helper, wand.containsMana(wandStack, player, DEFAULT_SOURCE, lessCentimana, helper.getLevel().registryAccess()), "Contains returned false for less than held");
        assertTrue(helper, wand.containsMana(wandStack, player, DEFAULT_SOURCE, exactCentimana, helper.getLevel().registryAccess()), "Contains returned false for exact held");
        assertFalse(helper, wand.containsMana(wandStack, player, DEFAULT_SOURCE, greaterCentimana, helper.getLevel().registryAccess()), "Contains returned true for greater than held");

        helper.succeed();
    }

    public static void wand_contains_mana_list(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var wandStack = WandType.MODULAR_WAND.makeStack();

        // Confirm that the wand was created successfully
        IWand wand = assertInstanceOf(helper, wandStack.getItem(), IWand.class, "Wand stack is not a wand as expected");

        final int startingRealMana = 10;
        final int startingCentimana = startingRealMana * 100;
        final double costModifier = 1 + (wand.getTotalCostModifier(wandStack, player, DEFAULT_SOURCE, helper.getLevel().registryAccess()) / 100D);
        final int modifiedCentimana = (int)(startingCentimana * costModifier);

        // Add some real mana to the wand for all sources except the test source
        Sources.stream().filter(s -> !s.equals(DEFAULT_SOURCE)).forEach(s -> wand.addMana(wandStack, s, startingCentimana));

        // Confirm that the wand contains centimana for a list containing all source except the test source
        var greenBuilder = SourceList.builder();
        Sources.stream().filter(s -> !s.equals(DEFAULT_SOURCE)).forEach(s -> greenBuilder.with(s, modifiedCentimana));
        var greenList = greenBuilder.build();
        assertTrue(helper, wand.containsMana(wandStack, player, greenList, helper.getLevel().registryAccess()), "Contains returned false for green list");

        // Confirm that the wand does not contain centimana for all sources
        var redBuilder = SourceList.builder();
        Sources.getAll().forEach(s -> redBuilder.with(s, modifiedCentimana));
        var redList = redBuilder.build();
        assertFalse(helper, wand.containsMana(wandStack, player, redList, helper.getLevel().registryAccess()), "Contains returned true for red list");

        helper.succeed();
    }

    public static void wand_contains_mana_raw(GameTestHelper helper) {
        var wandStack = WandType.MODULAR_WAND.makeStack();

        // Confirm that the wand was created successfully
        IWand wand = assertInstanceOf(helper, wandStack.getItem(), IWand.class, "Wand stack is not a wand as expected");

        final int startingRealMana = 10;
        final int exactCentimana = 100 * startingRealMana;
        final int lessCentimana = exactCentimana - 1;
        final int greaterCentimana = exactCentimana + 1;

        // Add some real mana to the wand for the test source
        assertValueEqual(helper, 0, wand.addMana(wandStack, DEFAULT_SOURCE, exactCentimana), "Overflow when adding mana to wand");

        // Confirm that the wand recognizes it contains centimana up to the threshold of what it was given
        assertTrue(helper, wand.containsManaRaw(wandStack, DEFAULT_SOURCE, lessCentimana), "Contains returned false for less than held");
        assertTrue(helper, wand.containsManaRaw(wandStack, DEFAULT_SOURCE, exactCentimana), "Contains returned false for exact held");
        assertFalse(helper, wand.containsManaRaw(wandStack, DEFAULT_SOURCE, greaterCentimana), "Contains returned true for greater than held");

        helper.succeed();
    }
}
