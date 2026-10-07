package com.verdantartifice.primalmagick.test.menus;

import com.verdantartifice.primalmagick.common.menus.RunicGrindstoneMenu;
import com.verdantartifice.primalmagick.common.runes.Rune;
import com.verdantartifice.primalmagick.common.runes.RuneManager;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.List;

/**
 * Tests for the runic grindstone menu, which works like a vanilla grindstone but also strips inscribed runes.
 */
public class RunicGrindstoneTests extends AbstractBaseTest {
    private static final int FIRST_INPUT_SLOT = 0;
    private static final int RESULT_SLOT = 2;

    /**
     * Grinding a runescribed, enchanted pickaxe leaves the same pickaxe with neither its enchantments nor its
     * runescribed status.
     */
    public static void runic_grindstone_removes_enchantments_and_runes(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var unbreaking = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING);
        var menu = new RunicGrindstoneMenu(0, player.getInventory());

        // Build the kind of item the runescribing altar makes: an enchantment with the runes that produced it
        var stack = new ItemStack(Items.IRON_PICKAXE);
        stack.enchant(unbreaking, 1);
        RuneManager.setRunes(stack, List.of(Rune.PROTECT, Rune.ITEM, Rune.EARTH));
        assertTrue(helper, RuneManager.hasRunes(stack), "Test pickaxe is not marked as runescribed");
        assertValueEqual(helper, 1, stack.getEnchantments().getLevel(unbreaking), "Test pickaxe Unbreaking level");

        menu.getSlot(FIRST_INPUT_SLOT).set(stack);

        var result = menu.getSlot(RESULT_SLOT).getItem();
        assertTrue(helper, result.is(Items.IRON_PICKAXE), "Grindstone result is not an iron pickaxe: " + result);
        assertTrue(helper, result.getEnchantments().isEmpty(), "Grindstone result still has enchantments: " + result.getEnchantments());
        assertFalse(helper, RuneManager.hasRunes(result), "Grindstone result is still runescribed");
        helper.succeed();
    }
}
