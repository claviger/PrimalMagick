package com.verdantartifice.primalmagick.test.tiles;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.components.DataComponentsPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.items.wands.IHasWandComponents;
import com.verdantartifice.primalmagick.common.menus.WandGlamourTableMenu;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Tests for the wand glamour table: opening its menu by right-clicking the block, and clearing the glamours from a
 * wand that is slotted with no other components. Menu slots are 0 = result, 1 = wand or staff, 2 = core, 3 = cap, 4 = gem.
 */
public class WandGlamourTableTests extends AbstractBaseTest {
    private static final int RESULT_SLOT = 0;
    private static final int CASTER_SLOT = 1;
    private static final int CORE_SLOT = 2;
    private static final int CAP_SLOT = 3;
    private static final int GEM_SLOT = 4;

    private static WandGlamourTableMenu openTableMenu(GameTestHelper helper, ServerPlayer player) {
        BlockPos pos = BlockPos.ZERO.above();
        helper.setBlock(pos, BlocksPM.WAND_GLAMOUR_TABLE.get());
        BlockPos absPos = helper.absolutePos(pos);
        var hit = new BlockHitResult(absPos.getCenter(), Direction.UP, absPos, false);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        var result = player.gameMode.useItemOn(player, helper.getLevel(), ItemStack.EMPTY, InteractionHand.MAIN_HAND, hit);
        assertTrue(helper, result.consumesAction(), "Empty-hand use on the table was not consumed: " + result);
        return assertInstanceOf(helper, player.containerMenu, WandGlamourTableMenu.class, "Menu not of expected type");
    }

    public static void wand_glamour_table_can_have_its_menu_opened(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        openTableMenu(helper, player);
        helper.succeed();
    }

    public static void wand_glamour_table_applies_glamours_from_components(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var menu = openTableMenu(helper, player);

        // Glamour a plain heartwood/iron/apprentice wand with obsidian, gold, and adept components
        ItemStack wandStack = TestUtils.makeModularCaster(ItemsPM.MODULAR_WAND.get(), WandCore.HEARTWOOD, WandCap.IRON, WandGem.APPRENTICE);
        menu.slots.get(CASTER_SLOT).safeInsert(wandStack);
        menu.slots.get(CORE_SLOT).safeInsert(new ItemStack(ItemsPM.OBSIDIAN_WAND_CORE_ITEM.get()));
        menu.slots.get(CAP_SLOT).safeInsert(new ItemStack(ItemsPM.GOLD_WAND_CAP_ITEM.get()));
        menu.slots.get(GEM_SLOT).safeInsert(new ItemStack(ItemsPM.ADEPT_WAND_GEM_ITEM.get()));

        ItemStack result = menu.slots.get(RESULT_SLOT).getItem();
        var caster = assertInstanceOf(helper, result.getItem(), IHasWandComponents.class, "Result is not a modular wand: " + result);
        assertTrue(helper, caster.getWandCoreAppearance(result) == WandCore.OBSIDIAN, "Core appearance not as expected: " + caster.getWandCoreAppearance(result));
        assertTrue(helper, caster.getWandCapAppearance(result) == WandCap.GOLD, "Cap appearance not as expected: " + caster.getWandCapAppearance(result));
        assertTrue(helper, caster.getWandGemAppearance(result) == WandGem.ADEPT, "Gem appearance not as expected: " + caster.getWandGemAppearance(result));

        // The wand's real components are unchanged
        assertTrue(helper, caster.getWandCore(result) == WandCore.HEARTWOOD, "Real core changed: " + caster.getWandCore(result));
        assertTrue(helper, caster.getWandCap(result) == WandCap.IRON, "Real cap changed: " + caster.getWandCap(result));
        assertTrue(helper, caster.getWandGem(result) == WandGem.APPRENTICE, "Real gem changed: " + caster.getWandGem(result));
        helper.succeed();
    }

    public static void wand_glamour_table_removes_glamours_when_slotted_alone(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var menu = openTableMenu(helper, player);

        // Start with a wand that has been glamoured to look like a different core, cap, and gem
        ItemStack wandStack = TestUtils.makeModularCaster(ItemsPM.MODULAR_WAND.get(), WandCore.HEARTWOOD, WandCap.IRON, WandGem.APPRENTICE);
        var caster = assertInstanceOf(helper, wandStack.getItem(), IHasWandComponents.class, "Wand has no wand components");
        caster.setWandCoreAppearance(wandStack, WandCore.OBSIDIAN);
        caster.setWandCapAppearance(wandStack, WandCap.GOLD);
        caster.setWandGemAppearance(wandStack, WandGem.ADEPT);
        assertTrue(helper, wandStack.has(DataComponentsPM.WAND_CORE_APPEARANCE.get()), "Core glamour not set on the test wand");
        assertTrue(helper, wandStack.has(DataComponentsPM.WAND_CAP_APPEARANCE.get()), "Cap glamour not set on the test wand");
        assertTrue(helper, wandStack.has(DataComponentsPM.WAND_GEM_APPEARANCE.get()), "Gem glamour not set on the test wand");

        // Slot the wand without any other components
        menu.slots.get(CASTER_SLOT).safeInsert(wandStack);
        ItemStack result = menu.slots.get(RESULT_SLOT).getItem();
        assertTrue(helper, result.is(ItemsPM.MODULAR_WAND.get()), "No wand result shown for a wand slotted alone: " + result);

        // All three glamours should be gone, so that the wand shows its real components
        assertTrue(helper, result.get(DataComponentsPM.WAND_CAP_APPEARANCE.get()) == null, "Cap glamour not removed");
        assertTrue(helper, result.get(DataComponentsPM.WAND_GEM_APPEARANCE.get()) == null, "Gem glamour not removed");
        assertTrue(helper, result.get(DataComponentsPM.WAND_CORE_APPEARANCE.get()) == null, "Core glamour not removed: " + result.get(DataComponentsPM.WAND_CORE_APPEARANCE.get()));
        helper.succeed();
    }

    public static void wand_glamour_table_removes_staff_glamours_when_slotted_alone(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var menu = openTableMenu(helper, player);

        // Start with a staff that has been glamoured to look like a different core, cap, and gem
        ItemStack staffStack = TestUtils.makeModularCaster(ItemsPM.MODULAR_STAFF.get(), WandCore.HEARTWOOD, WandCap.IRON, WandGem.APPRENTICE);
        var caster = assertInstanceOf(helper, staffStack.getItem(), IHasWandComponents.class, "Staff has no wand components");
        caster.setWandCoreAppearance(staffStack, WandCore.OBSIDIAN);
        caster.setWandCapAppearance(staffStack, WandCap.GOLD);
        caster.setWandGemAppearance(staffStack, WandGem.ADEPT);
        assertTrue(helper, staffStack.has(DataComponentsPM.WAND_CORE_APPEARANCE.get()), "Core glamour not set on the test staff");
        assertTrue(helper, staffStack.has(DataComponentsPM.WAND_CAP_APPEARANCE.get()), "Cap glamour not set on the test staff");
        assertTrue(helper, staffStack.has(DataComponentsPM.WAND_GEM_APPEARANCE.get()), "Gem glamour not set on the test staff");

        // Slot the staff without any other components; the staff recipe should match and produce a staff
        menu.slots.get(CASTER_SLOT).safeInsert(staffStack);
        ItemStack result = menu.slots.get(RESULT_SLOT).getItem();
        assertTrue(helper, result.is(ItemsPM.MODULAR_STAFF.get()), "No staff result shown for a staff slotted alone: " + result);

        // All three glamours should be gone, so that the staff shows its real components
        assertTrue(helper, result.get(DataComponentsPM.WAND_CAP_APPEARANCE.get()) == null, "Cap glamour not removed");
        assertTrue(helper, result.get(DataComponentsPM.WAND_GEM_APPEARANCE.get()) == null, "Gem glamour not removed");
        assertTrue(helper, result.get(DataComponentsPM.WAND_CORE_APPEARANCE.get()) == null, "Core glamour not removed: " + result.get(DataComponentsPM.WAND_CORE_APPEARANCE.get()));

        // The staff's real components are unchanged
        assertTrue(helper, caster.getWandCore(result) == WandCore.HEARTWOOD, "Real core changed: " + caster.getWandCore(result));
        assertTrue(helper, caster.getWandCap(result) == WandCap.IRON, "Real cap changed: " + caster.getWandCap(result));
        assertTrue(helper, caster.getWandGem(result) == WandGem.APPRENTICE, "Real gem changed: " + caster.getWandGem(result));
        helper.succeed();
    }

    public static void wand_glamour_table_replaces_glamours_when_slotted_with_only_a_cap(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var menu = openTableMenu(helper, player);

        // Start with a wand glamoured as obsidian/gold/adept, then slot it with only a hexium cap
        ItemStack wandStack = TestUtils.makeModularCaster(ItemsPM.MODULAR_WAND.get(), WandCore.HEARTWOOD, WandCap.IRON, WandGem.APPRENTICE);
        var caster = assertInstanceOf(helper, wandStack.getItem(), IHasWandComponents.class, "Wand has no wand components");
        caster.setWandCoreAppearance(wandStack, WandCore.OBSIDIAN);
        caster.setWandCapAppearance(wandStack, WandCap.GOLD);
        caster.setWandGemAppearance(wandStack, WandGem.ADEPT);
        menu.slots.get(CASTER_SLOT).safeInsert(wandStack);
        menu.slots.get(CAP_SLOT).safeInsert(new ItemStack(ItemsPM.HEXIUM_WAND_CAP_ITEM.get()));

        // The slotted cap replaces the gold cap glamour, and the empty core and gem slots clear those glamours
        ItemStack result = menu.slots.get(RESULT_SLOT).getItem();
        assertTrue(helper, result.is(ItemsPM.MODULAR_WAND.get()), "No wand result shown for a wand slotted with a cap: " + result);
        assertTrue(helper, caster.getWandCapAppearance(result) == WandCap.HEXIUM, "Cap appearance not as expected: " + caster.getWandCapAppearance(result));
        assertTrue(helper, result.get(DataComponentsPM.WAND_GEM_APPEARANCE.get()) == null, "Gem glamour not removed");
        assertTrue(helper, result.get(DataComponentsPM.WAND_CORE_APPEARANCE.get()) == null, "Core glamour not removed: " + result.get(DataComponentsPM.WAND_CORE_APPEARANCE.get()));
        helper.succeed();
    }
}
