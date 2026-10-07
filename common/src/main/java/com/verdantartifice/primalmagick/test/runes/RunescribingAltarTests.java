package com.verdantartifice.primalmagick.test.runes;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.items.misc.RuneItem;
import com.verdantartifice.primalmagick.common.menus.AbstractRunescribingAltarMenu;
import com.verdantartifice.primalmagick.common.runes.Rune;
import com.verdantartifice.primalmagick.common.runes.RuneManager;
import com.verdantartifice.primalmagick.common.tiles.crafting.RunescribingAltarTileEntity;
import com.verdantartifice.primalmagick.platform.Services;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Tests for the runescribing altar menus: how many runes each altar tier accepts, and which items they will inscribe.
 */
public class RunescribingAltarTests extends AbstractBaseTest {
    // The menu's slots are the result slot, the input slot, the rune slots, and then the 27 backpack and 9 hotbar slots
    private static final int NON_RUNE_SLOT_COUNT = 2 + 36;
    private static final int RESULT_SLOT = 0;
    private static final int INPUT_SLOT = 1;
    private static final int FIRST_RUNE_SLOT = 2;

    private static AbstractRunescribingAltarMenu openAltar(GameTestHelper helper, ServerPlayer player, Block altarBlock) {
        var pos = BlockPos.ZERO.above();
        helper.setBlock(pos, altarBlock);
        var tile = helper.getBlockEntity(pos, RunescribingAltarTileEntity.class);
        Services.PLAYER.openMenu(player, tile, helper.absolutePos(pos));
        return assertInstanceOf(helper, player.containerMenu, AbstractRunescribingAltarMenu.class, "Menu not of expected type");
    }

    private static void insertRunes(AbstractRunescribingAltarMenu menu, List<Rune> runes) {
        for (int i = 0; i < runes.size(); i++) {
            menu.getSlot(FIRST_RUNE_SLOT + i).set(RuneItem.getRune(runes.get(i)));
        }
    }

    /**
     * Each altar tier's menu has its own rune capacity, set by the tier menu classes: 3 for basic, 5 for enchanted,
     * 7 for forbidden, and 9 for heavenly.
     */
    public static void altar_rune_capacity_by_tier(GameTestHelper helper, Block altarBlock, int expectedRuneSlots) {
        var player = makeMockServerPlayer(helper);
        var menu = openAltar(helper, player, altarBlock);

        assertValueEqual(helper, NON_RUNE_SLOT_COUNT + expectedRuneSlots, menu.slots.size(), "Total slot count of the altar menu");

        // Every rune slot accepts runes and rejects ordinary items
        var rune = new ItemStack(ItemsPM.RUNE_EARTH.get());
        var notRune = new ItemStack(Items.DIRT);
        for (int i = 0; i < expectedRuneSlots; i++) {
            var slot = menu.getSlot(FIRST_RUNE_SLOT + i);
            assertTrue(helper, slot.mayPlace(rune), "Rune slot " + i + " rejected a rune");
            assertFalse(helper, slot.mayPlace(notRune), "Rune slot " + i + " accepted a non-rune item");
        }

        // The slot right after the last rune slot is the first backpack slot, which takes anything
        assertTrue(helper, menu.getSlot(FIRST_RUNE_SLOT + expectedRuneSlots).mayPlace(notRune), "Slot after the rune slots did not accept an ordinary item");
        helper.succeed();
    }

    /**
     * The Protect, Item, and Earth runes are the combination for Unbreaking in the rune enchantment definitions, and
     * Unbreaking applies to a tool with durability at level 1 when there is only the base power level.
     */
    public static void protect_item_earth_runes_give_unbreaking_on_pickaxe(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var menu = openAltar(helper, player, BlocksPM.RUNESCRIBING_ALTAR_BASIC.get());
        var unbreaking = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING);

        menu.getSlot(INPUT_SLOT).set(new ItemStack(Items.IRON_PICKAXE));
        insertRunes(menu, List.of(Rune.PROTECT, Rune.ITEM, Rune.EARTH));

        var result = menu.getSlot(RESULT_SLOT).getItem();
        assertTrue(helper, result.is(Items.IRON_PICKAXE), "Altar result is not an iron pickaxe: " + result);
        assertValueEqual(helper, 1, result.getEnchantments().size(), "Result enchantment count");
        assertValueEqual(helper, 1, result.getEnchantments().getLevel(unbreaking), "Result Unbreaking level");
        assertTrue(helper, RuneManager.hasRunes(result), "Result is not marked as runescribed");
        helper.succeed();
    }

    /**
     * An item that has already been runescribed is given no result by the altar, even with a rune combination that
     * would otherwise enchant it. The same combination on a plain pickaxe is covered by the Unbreaking test above.
     */
    public static void runescribed_item_cannot_be_runescribed_again(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var menu = openAltar(helper, player, BlocksPM.RUNESCRIBING_ALTAR_BASIC.get());

        var scribed = new ItemStack(Items.IRON_PICKAXE);
        RuneManager.setRunes(scribed, List.of(Rune.PROTECT, Rune.ITEM, Rune.EARTH));
        assertTrue(helper, RuneManager.hasRunes(scribed), "Test pickaxe is not marked as runescribed");

        menu.getSlot(INPUT_SLOT).set(scribed);
        insertRunes(menu, List.of(Rune.PROTECT, Rune.ITEM, Rune.EARTH));

        var result = menu.getSlot(RESULT_SLOT).getItem();
        assertTrue(helper, result.isEmpty(), "Altar offered a result for an already runescribed item: " + result);
        helper.succeed();
    }
}
