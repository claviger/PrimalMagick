package com.verdantartifice.primalmagick.test.tiles;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.blocks.mana.ManaBatteryBlock;
import com.verdantartifice.primalmagick.common.capabilities.IItemHandlerPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.items.essence.EssenceItem;
import com.verdantartifice.primalmagick.common.items.essence.EssenceType;
import com.verdantartifice.primalmagick.common.menus.ManaBatteryMenu;
import com.verdantartifice.primalmagick.common.sources.SourceList;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.common.tiles.mana.AbstractManaFontTileEntity;
import com.verdantartifice.primalmagick.common.tiles.mana.ManaBatteryTileEntity;
import com.verdantartifice.primalmagick.platform.Services;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;

/**
 * Tests for mana batteries: menu access, which items each face's item handler accepts, and siphoning mana from
 * nearby fonts. Item handler rules don't depend on the battery's tier, so those tests use the mana nexus; siphon
 * tests cover every battery and hard-code each tier's transfer cap from ManaBatteryTileEntity.getBatteryTransferCap.
 */
public class ManaBatteryTests extends AbstractBaseTest {
    // Starting mana for fonts in siphon tests; it exceeds every battery's transfer cap so that the cap is exercised,
    // and fits within the ancient font's 10000 centimana capacity
    protected static final int START_FONT_MANA = 5000;

    // Ancient fonts are of the basic tier, which recharges 5 centimana per tick (AbstractManaFontTileEntity)
    protected static final int ANCIENT_FONT_RECHARGE_PER_TICK = 5;

    private static ManaBatteryBlock getHandlerTestBattery() {
        return BlocksPM.MANA_NEXUS.get();
    }

    private static IItemHandlerPM getItemHandlerForNewManaBattery(GameTestHelper helper, Direction direction) {
        return TileTestUtils.placeTileAndGetHandler(helper, BlockPos.ZERO, getHandlerTestBattery(), ManaBatteryTileEntity.class, direction);
    }

    /**
     * The tiles of a battery placed next to an ancient earth font holding START_FONT_MANA, for siphon tests.
     */
    private record BatteryAndFont(ManaBatteryTileEntity battery, AbstractManaFontTileEntity font) {}

    private static BatteryAndFont placeBatteryNextToFont(GameTestHelper helper, ManaBatteryBlock block) {
        // Place a mana battery block; both positions are within the test structure so that a retry replaces them
        var batteryPos = BlockPos.ZERO.south();
        helper.setBlock(batteryPos, block);
        var batteryTile = helper.getBlockEntity(batteryPos, ManaBatteryTileEntity.class);

        // Place a mana font block and give it a known amount of mana
        var fontPos = BlockPos.ZERO.east();
        helper.setBlock(fontPos, BlocksPM.ANCIENT_FONT_EARTH.get());
        var fontTile = helper.getBlockEntity(fontPos, AbstractManaFontTileEntity.class);
        fontTile.setMana(START_FONT_MANA);
        assertValueEqual(helper, START_FONT_MANA, fontTile.getMana(), "Before font mana");

        return new BatteryAndFont(batteryTile, fontTile);
    }

    public static void mana_battery_can_have_its_menu_opened(GameTestHelper helper) {
        var block = getHandlerTestBattery();

        // Create a test player
        var player = makeMockServerPlayer(helper);

        // Place a mana battery block and get its block entity
        var pos = BlockPos.ZERO;
        helper.setBlock(pos, block);
        var tile = helper.getBlockEntity(pos, ManaBatteryTileEntity.class);

        // Open the block entity menu
        Services.PLAYER.openMenu(player, tile, pos);
        assertInstanceOf(helper, player.containerMenu, ManaBatteryMenu.class, "Menu not of expected type");

        helper.succeed();
    }

    // Output (charge slot) handler tests

    public static void mana_battery_output_allows_chargeable_items(GameTestHelper helper, ChargeableItem item) {
        var stack = item.makeStack();
        assertTrue(helper, ChargeableItem.hasManaStorage(stack), "Test stack " + stack + " has no mana storage");
        TileTestUtils.assertHandlerAccepts(helper, getItemHandlerForNewManaBattery(helper, Direction.NORTH), stack);
        helper.succeed();
    }

    public static void mana_battery_output_allows_chargeable_items(GameTestHelper helper) {
        mana_battery_output_allows_chargeable_items(helper, ChargeableItem.MUNDANE_WAND);
    }

    public static void mana_battery_output_does_not_allow_unchargeable_items(GameTestHelper helper) {
        TileTestUtils.assertHandlerRejects(helper, getItemHandlerForNewManaBattery(helper, Direction.NORTH), Items.STICK.getDefaultInstance());
        helper.succeed();
    }

    public static void mana_battery_output_does_not_allow_essence(GameTestHelper helper) {
        // Essence can be broken down by the input slot, but carries no mana storage and so can't be charged
        TileTestUtils.assertHandlerRejects(helper, getItemHandlerForNewManaBattery(helper, Direction.NORTH), ItemsPM.ESSENCE_SHARD_EARTH.get().getDefaultInstance());
        helper.succeed();
    }

    // Input handler tests

    public static void mana_battery_input_allows_essence(GameTestHelper helper, EssenceType type) {
        TileTestUtils.assertHandlerAccepts(helper, getItemHandlerForNewManaBattery(helper, Direction.UP), EssenceItem.getEssence(type, Sources.EARTH));
        helper.succeed();
    }

    public static void mana_battery_input_allows_essence(GameTestHelper helper) {
        mana_battery_input_allows_essence(helper, EssenceType.SHARD);
    }

    public static void mana_battery_input_allows_wands(GameTestHelper helper, ChargeableItem wand) {
        assertTrue(helper, wand.isCaster(), wand + " is not a caster");
        TileTestUtils.assertHandlerAccepts(helper, getItemHandlerForNewManaBattery(helper, Direction.UP), wand.makeStack());
        helper.succeed();
    }

    public static void mana_battery_input_allows_wands(GameTestHelper helper) {
        mana_battery_input_allows_wands(helper, ChargeableItem.MUNDANE_WAND);
    }

    // Siphon tests

    /**
     * Confirms that the given battery has the expected transfer cap, and that on its first tick it siphons up to that
     * cap from a nearby ancient earth font.
     */
    public static void mana_battery_siphons_from_nearby_fonts(GameTestHelper helper, ManaBatteryBlock block, int expectedTransferCap) {
        var tiles = placeBatteryNextToFont(helper, block);
        var batteryTile = tiles.battery();
        var fontTile = tiles.font();

        // Confirm initial state
        assertValueEqual(helper, expectedTransferCap, batteryTile.getBatteryTransferCap(), "Battery transfer cap");
        assertTrue(helper, batteryTile.getAllMana().isEmpty(), "Before battery mana not empty: " + batteryTile.getAllMana());

        // Confirm that mana was siphoned from the font to the battery, after which the font recharged for one tick
        final int expectedFontMana = START_FONT_MANA - expectedTransferCap + ANCIENT_FONT_RECHARGE_PER_TICK;
        final SourceList expectedBatteryMana = SourceList.builder().with(Sources.EARTH, expectedTransferCap).build();
        helper.succeedOnTickWhen(1, () -> {
            assertValueEqual(helper, expectedFontMana, fontTile.getMana(), "After font mana");
            assertValueEqual(helper, expectedBatteryMana, batteryTile.getAllMana(), "After battery mana");
        });
    }

    public static void mana_battery_siphons_from_nearby_fonts(GameTestHelper helper) {
        // The mana nexus is of the forbidden tier, whose transfer cap is WandCap.HEXIUM's 800 centimana siphon amount
        mana_battery_siphons_from_nearby_fonts(helper, BlocksPM.MANA_NEXUS.get(), 800);
    }

    /**
     * Confirms that the creative mana singularity, which already holds infinite mana of every source, reports the
     * heavenly transfer cap but does not siphon from a nearby font.
     */
    public static void mana_battery_does_not_siphon_from_nearby_fonts_mana_singularity_creative(GameTestHelper helper) {
        var tiles = placeBatteryNextToFont(helper, BlocksPM.MANA_SINGULARITY_CREATIVE.get());
        var batteryTile = tiles.battery();
        var fontTile = tiles.font();

        // Confirm initial state; the creative tier shares WandCap.HALLOWSTEEL's 1600 centimana transfer cap with the
        // heavenly tier, and its infinite storage reports the maximum integer for every source
        assertValueEqual(helper, 1600, batteryTile.getBatteryTransferCap(), "Battery transfer cap");
        assertValueEqual(helper, Integer.MAX_VALUE, batteryTile.getMana(Sources.EARTH), "Before battery mana");

        // Confirm that the font only gained its recharge and the battery's storage is unchanged
        final int expectedFontMana = START_FONT_MANA + ANCIENT_FONT_RECHARGE_PER_TICK;
        helper.succeedOnTickWhen(1, () -> {
            assertValueEqual(helper, expectedFontMana, fontTile.getMana(), "After font mana");
            assertValueEqual(helper, Integer.MAX_VALUE, batteryTile.getMana(Sources.EARTH), "After battery mana");
        });
    }
}
