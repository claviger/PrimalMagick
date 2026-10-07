package com.verdantartifice.primalmagick.test.tiles;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.items.wands.IHasWandComponents;
import com.verdantartifice.primalmagick.common.menus.WandAssemblyTableMenu;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.common.wands.IWand;
import com.verdantartifice.primalmagick.common.wands.WandCap;
import com.verdantartifice.primalmagick.common.wands.WandCore;
import com.verdantartifice.primalmagick.common.wands.WandGem;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Tests for the wand assembly table: opening its menu by right-clicking the block, and assembling modular wands and
 * staves from a core, a gem, and two matching caps. Menu slots are 0 = result, 1 = core, 2 = gem, 3 and 4 = caps.
 */
public class WandAssemblyTableTests extends AbstractBaseTest {
    private static final int RESULT_SLOT = 0;
    private static final int CORE_SLOT = 1;
    private static final int GEM_SLOT = 2;
    private static final int CAP_SLOT_1 = 3;
    private static final int CAP_SLOT_2 = 4;

    private static WandAssemblyTableMenu openTableMenu(GameTestHelper helper, ServerPlayer player) {
        BlockPos pos = BlockPos.ZERO.above();
        helper.setBlock(pos, BlocksPM.WAND_ASSEMBLY_TABLE.get());
        BlockPos absPos = helper.absolutePos(pos);
        var hit = new BlockHitResult(absPos.getCenter(), Direction.UP, absPos, false);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        var result = player.gameMode.useItemOn(player, helper.getLevel(), ItemStack.EMPTY, InteractionHand.MAIN_HAND, hit);
        assertTrue(helper, result.consumesAction(), "Empty-hand use on the table was not consumed: " + result);
        return assertInstanceOf(helper, player.containerMenu, WandAssemblyTableMenu.class, "Menu not of expected type");
    }

    public static void wand_assembly_table_can_have_its_menu_opened(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        openTableMenu(helper, player);
        helper.succeed();
    }

    private static void assembleCaster(GameTestHelper helper, Item coreItem, Item capItem, Item gemItem, Item expectedCaster,
                                       WandCore expectedCore, WandCap expectedCap, WandGem expectedGem) {
        var player = makeMockServerPlayer(helper);
        var menu = openTableMenu(helper, player);
        assertTrue(helper, menu.slots.get(RESULT_SLOT).getItem().isEmpty(), "Result present with an empty table");

        // The result is shown only once all four components are present
        menu.slots.get(CORE_SLOT).safeInsert(new ItemStack(coreItem));
        menu.slots.get(GEM_SLOT).safeInsert(new ItemStack(gemItem));
        menu.slots.get(CAP_SLOT_1).safeInsert(new ItemStack(capItem));
        assertTrue(helper, menu.slots.get(RESULT_SLOT).getItem().isEmpty(), "Result present with only one cap");
        menu.slots.get(CAP_SLOT_2).safeInsert(new ItemStack(capItem));

        ItemStack result = menu.slots.get(RESULT_SLOT).getItem();
        assertTrue(helper, result.is(expectedCaster), "Result is not the expected caster item: " + result);
        var caster = assertInstanceOf(helper, result.getItem(), IHasWandComponents.class, "Result item has no wand components");
        assertTrue(helper, caster.getWandCore(result) == expectedCore, "Result core is not as expected: " + caster.getWandCore(result));
        assertTrue(helper, caster.getWandCap(result) == expectedCap, "Result cap is not as expected: " + caster.getWandCap(result));
        assertTrue(helper, caster.getWandGem(result) == expectedGem, "Result gem is not as expected: " + caster.getWandGem(result));

        // The wand's mana capacity comes from its gem
        IWand wand = assertInstanceOf(helper, result.getItem(), IWand.class, "Result item is not a wand");
        assertValueEqual(helper, expectedGem.getCapacity(), wand.getMaxMana(result, Sources.EARTH), "Result earth mana capacity");

        // Taking the result consumes the components
        var taken = menu.quickMoveStack(player, RESULT_SLOT);
        assertTrue(helper, taken.is(expectedCaster), "Taken item is not the expected caster item: " + taken);
        for (int slot : new int[] {CORE_SLOT, GEM_SLOT, CAP_SLOT_1, CAP_SLOT_2}) {
            assertFalse(helper, menu.slots.get(slot).hasItem(), "Component slot " + slot + " not consumed");
        }
        helper.succeed();
    }

    public static void wand_assembly_table_assembles_modular_wand(GameTestHelper helper) {
        // The wand recipe takes wand cores. The gem sets the capacity: WandGem.ADEPT holds 25000 centimana.
        assembleCaster(helper, ItemsPM.OBSIDIAN_WAND_CORE_ITEM.get(), ItemsPM.GOLD_WAND_CAP_ITEM.get(), ItemsPM.ADEPT_WAND_GEM_ITEM.get(),
                ItemsPM.MODULAR_WAND.get(), WandCore.OBSIDIAN, WandCap.GOLD, WandGem.ADEPT);
    }

    public static void wand_assembly_table_assembles_modular_staff(GameTestHelper helper) {
        // The staff recipe takes staff cores. The gem sets the capacity: WandGem.WIZARD holds 75000 centimana.
        assembleCaster(helper, ItemsPM.BAMBOO_STAFF_CORE_ITEM.get(), ItemsPM.IRON_WAND_CAP_ITEM.get(), ItemsPM.WIZARD_WAND_GEM_ITEM.get(),
                ItemsPM.MODULAR_STAFF.get(), WandCore.BAMBOO, WandCap.IRON, WandGem.WIZARD);
    }

    public static void wand_assembly_table_rejects_mismatched_caps(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var menu = openTableMenu(helper, player);

        // The recipe requires two identical caps
        menu.slots.get(CORE_SLOT).safeInsert(new ItemStack(ItemsPM.HEARTWOOD_WAND_CORE_ITEM.get()));
        menu.slots.get(GEM_SLOT).safeInsert(new ItemStack(ItemsPM.APPRENTICE_WAND_GEM_ITEM.get()));
        menu.slots.get(CAP_SLOT_1).safeInsert(new ItemStack(ItemsPM.IRON_WAND_CAP_ITEM.get()));
        menu.slots.get(CAP_SLOT_2).safeInsert(new ItemStack(ItemsPM.GOLD_WAND_CAP_ITEM.get()));
        assertTrue(helper, menu.slots.get(RESULT_SLOT).getItem().isEmpty(), "Result present with mismatched caps: " + menu.slots.get(RESULT_SLOT).getItem());
        helper.succeed();
    }
}
