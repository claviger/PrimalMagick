package com.verdantartifice.primalmagick.test.tiles;

import com.verdantartifice.primalmagick.common.sources.Source;
import com.verdantartifice.primalmagick.common.tiles.base.AbstractTileSidedInventoryPM;
import com.verdantartifice.primalmagick.common.tiles.base.IManaContainingBlockEntity;
import com.verdantartifice.primalmagick.common.wands.IWand;
import com.verdantartifice.primalmagick.platform.Services;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shared checks for the magitech devices (honey extractor, essence transmuter, concocter, dissolution chamber, and
 * infernal furnace), which all open a menu and top up their internal mana storage from a wand in the same way.
 */
public final class MagitechTileTestUtils extends AbstractBaseTest {
    // Mana loaded into the test wand; more than the 100 centimana a device can pull from a wand in one tick
    public static final int WAND_START_MANA = 500;

    // Each device pulls at most 100 centimana from its wand per tick (Mth.clamp(centimanaMissing, 0, 100) in each tile's tick)
    public static final int WAND_TRANSFER_PER_TICK = 100;

    private MagitechTileTestUtils() {}

    /**
     * A device's static tick method, as a functional interface.
     */
    @FunctionalInterface
    public interface Ticker<T extends BlockEntity> {
        void tick(Level level, BlockPos pos, BlockState state, T tile);
    }

    public static <T extends BlockEntity & MenuProvider> void assertMenuOpens(GameTestHelper helper, Block block, Class<T> tileClass, Class<? extends AbstractContainerMenu> menuClass) {
        var player = makeMockServerPlayer(helper);
        var pos = BlockPos.ZERO;
        helper.setBlock(pos, block);
        var tile = helper.getBlockEntity(pos, tileClass);
        Services.PLAYER.openMenu(player, tile, pos);
        assertInstanceOf(helper, player.containerMenu, menuClass, "Menu not of expected type");
        helper.succeed();
    }

    public static <T extends AbstractTileSidedInventoryPM & IManaContainingBlockEntity> void assertAbsorbsWandMana(GameTestHelper helper, Block block, Class<T> tileClass, Ticker<T> ticker, int wandInvIndex, Source source) {
        var pos = BlockPos.ZERO;
        helper.setBlock(pos, block);
        var tile = helper.getBlockEntity(pos, tileClass);

        // Slot a mundane wand holding mana of the device's source
        ItemStack wandStack = ChargeableItem.MUNDANE_WAND.makeStack();
        IWand wand = assertInstanceOf(helper, wandStack.getItem(), IWand.class, "Test stack is not a wand");
        assertValueEqual(helper, 0, wand.addMana(wandStack, source, WAND_START_MANA), "Overflow when charging the test wand");
        tile.addItem(wandInvIndex, 0, wandStack);
        assertFalse(helper, tile.getItem(wandInvIndex, 0).isEmpty(), "Wand not accepted by the wand slot");
        assertValueEqual(helper, 0, tile.getMana(source), "Device " + source.getId() + " mana before ticking");

        // One tick moves one transfer's worth of mana from the wand into the device
        ticker.tick(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos), tile);
        assertValueEqual(helper, WAND_TRANSFER_PER_TICK, tile.getMana(source), "Device " + source.getId() + " mana after one tick");
        assertValueEqual(helper, WAND_START_MANA - WAND_TRANSFER_PER_TICK, wand.getMana(tile.getItem(wandInvIndex, 0), source), "Wand " + source.getId() + " mana after one tick");
        helper.succeed();
    }
}
