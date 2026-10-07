package com.verdantartifice.primalmagick.test.tiles;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.items.essence.EssenceType;
import com.verdantartifice.primalmagick.common.menus.EssenceCaskMenu;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.common.tiles.devices.EssenceCaskTileEntity;
import com.verdantartifice.primalmagick.platform.Services;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;

/**
 * Tests for the enchanted essence cask: opening its menu and moving essence from the input slot into its storage.
 */
public class EssenceCaskTests extends AbstractBaseTest {
    private static final BlockPos CASK_POS = new BlockPos(1, 1, 1);

    public static void essence_cask_can_have_its_menu_opened(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        helper.setBlock(CASK_POS, BlocksPM.ESSENCE_CASK_ENCHANTED.get());
        var tile = helper.getBlockEntity(CASK_POS, EssenceCaskTileEntity.class);

        Services.PLAYER.openMenu(player, tile, CASK_POS);
        assertInstanceOf(helper, player.containerMenu, EssenceCaskMenu.class, "Menu not of expected type");
        helper.succeed();
    }

    public static void essence_cask_input_slot_transfers_essence_to_storage(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        helper.setBlock(CASK_POS, BlocksPM.ESSENCE_CASK_ENCHANTED.get());
        var tile = helper.getBlockEntity(CASK_POS, EssenceCaskTileEntity.class);
        assertValueEqual(helper, 0, tile.getTotalEssenceCount(), "Cask starting essence count");

        // Put some earth dust in the input slot by way of the cask menu
        Services.PLAYER.openMenu(player, tile, CASK_POS);
        var menu = assertInstanceOf(helper, player.containerMenu, EssenceCaskMenu.class, "Menu not of expected type");
        menu.slots.get(0).safeInsert(new ItemStack(ItemsPM.ESSENCE_DUST_EARTH.get(), 10));
        assertTrue(helper, menu.slots.get(0).getItem().is(ItemsPM.ESSENCE_DUST_EARTH.get()), "Dust not placed in the input slot");

        // The cask's own tick moves the input into storage, leaving the input slot empty
        helper.succeedWhen(() -> {
            assertValueEqual(helper, 10, tile.getEssenceCount(EssenceType.DUST, Sources.EARTH), "Stored earth dust");
            assertValueEqual(helper, 10, tile.getTotalEssenceCount(), "Total stored essence");
            assertFalse(helper, menu.slots.get(0).hasItem(), "Input slot not emptied");
        });
    }
}
