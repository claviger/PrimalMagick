package com.verdantartifice.primalmagick.test.crafting;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.menus.WandInscriptionTableMenu;
import com.verdantartifice.primalmagick.common.spells.SpellPackage;
import com.verdantartifice.primalmagick.common.spells.payloads.EarthDamageSpellPayload;
import com.verdantartifice.primalmagick.common.wands.ISpellContainer;
import com.verdantartifice.primalmagick.common.wands.WandCap;
import com.verdantartifice.primalmagick.common.wands.WandCore;
import com.verdantartifice.primalmagick.common.wands.WandGem;
import com.verdantartifice.primalmagick.platform.Services;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import com.verdantartifice.primalmagick.test.TestUtils;
import com.verdantartifice.primalmagick.test.spells.SpellPackageTests;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Tests for inscribing spell scrolls onto spell containers (wands, staves, and spelltomes) via the wand
 * inscription table menu. Menu slot layout: 0 = result, 1 = caster, 2 = scroll, 3-38 = player inventory.
 */
public class WandInscriptionTests extends AbstractBaseTest {
    private static final int RESULT_SLOT = 0;
    private static final int CASTER_SLOT = 1;
    private static final int SCROLL_SLOT = 2;

    public static void wand_inscription_inscribes_scroll_into_wand(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var menu = openTable(helper, player);
        SpellPackage spell = makeSpell();

        // A heartwood core offers one spell slot, so an empty wand has room for the scroll's spell
        menu.getSlot(CASTER_SLOT).set(TestUtils.makeModularCaster(ItemsPM.MODULAR_WAND.get(), WandCore.HEARTWOOD, WandCap.IRON, WandGem.APPRENTICE));
        menu.getSlot(SCROLL_SLOT).set(makeScroll(spell));

        // The previewed result should be the wand with the scroll's spell inscribed and selected
        ItemStack preview = menu.getSlot(RESULT_SLOT).getItem();
        assertTrue(helper, preview.is(ItemsPM.MODULAR_WAND.get()), "Result is not a modular wand: " + preview);
        assertInscribedWith(helper, preview, spell);

        // Taking the result should consume both inputs
        ItemStack output = menu.quickMoveStack(player, RESULT_SLOT);
        assertTrue(helper, output.is(ItemsPM.MODULAR_WAND.get()), "Taken output is not a modular wand: " + output);
        assertInscribedWith(helper, output, spell);
        assertTrue(helper, menu.getSlot(CASTER_SLOT).getItem().isEmpty(), "Caster slot not empty after taking result");
        assertTrue(helper, menu.getSlot(SCROLL_SLOT).getItem().isEmpty(), "Scroll slot not empty after taking result");

        helper.succeed();
    }

    public static void wand_inscription_inscribes_scroll_into_spelltome(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var menu = openTable(helper, player);
        SpellPackage spell = makeSpell();

        // An apprentice spelltome (BASIC tier) holds one spell, so an empty tome has room for the scroll's spell
        menu.getSlot(CASTER_SLOT).set(new ItemStack(ItemsPM.SPELLTOME_APPRENTICE.get()));
        menu.getSlot(SCROLL_SLOT).set(makeScroll(spell));

        // The previewed result should be the tome with the scroll's spell inscribed and selected
        ItemStack preview = menu.getSlot(RESULT_SLOT).getItem();
        assertTrue(helper, preview.is(ItemsPM.SPELLTOME_APPRENTICE.get()), "Result is not an apprentice spelltome: " + preview);
        assertInscribedWith(helper, preview, spell);

        // Taking the result should consume both the empty tome and the scroll
        ItemStack output = menu.quickMoveStack(player, RESULT_SLOT);
        assertTrue(helper, output.is(ItemsPM.SPELLTOME_APPRENTICE.get()), "Taken output is not an apprentice spelltome: " + output);
        assertInscribedWith(helper, output, spell);
        assertTrue(helper, menu.getSlot(CASTER_SLOT).getItem().isEmpty(), "Caster slot not empty after taking result");
        assertTrue(helper, menu.getSlot(SCROLL_SLOT).getItem().isEmpty(), "Scroll slot not empty after taking result");

        helper.succeed();
    }

    public static void wand_inscription_rejects_full_container(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var menu = openTable(helper, player);
        SpellPackage spell = makeSpell();

        // An apprentice spelltome holds one spell; with one already inscribed, a second would need two slots
        ItemStack fullTome = new ItemStack(ItemsPM.SPELLTOME_APPRENTICE.get());
        assertTrue(helper, ItemsPM.SPELLTOME_APPRENTICE.get().addSpell(fullTome, spell), "Could not pre-inscribe spelltome");
        menu.getSlot(CASTER_SLOT).set(fullTome);
        menu.getSlot(SCROLL_SLOT).set(makeScroll(spell));
        assertTrue(helper, menu.getSlot(RESULT_SLOT).getItem().isEmpty(), "Full spelltome produced an output: " + menu.getSlot(RESULT_SLOT).getItem());

        // Clear the table before trying the next container
        menu.getSlot(CASTER_SLOT).set(ItemStack.EMPTY);
        menu.getSlot(SCROLL_SLOT).set(ItemStack.EMPTY);

        // A heartwood core offers one spell slot and no bonus slot, so a wand already holding one spell is full
        ItemStack fullWand = TestUtils.makeModularCaster(ItemsPM.MODULAR_WAND.get(), WandCore.HEARTWOOD, WandCap.IRON, WandGem.APPRENTICE);
        assertTrue(helper, ItemsPM.MODULAR_WAND.get().addSpell(fullWand, spell), "Could not pre-inscribe wand");
        menu.getSlot(CASTER_SLOT).set(fullWand);
        menu.getSlot(SCROLL_SLOT).set(makeScroll(spell));
        assertTrue(helper, menu.getSlot(RESULT_SLOT).getItem().isEmpty(), "Full wand produced an output: " + menu.getSlot(RESULT_SLOT).getItem());

        helper.succeed();
    }

    private static WandInscriptionTableMenu openTable(GameTestHelper helper, ServerPlayer player) {
        BlockPos tablePos = new BlockPos(1, 1, 1);
        helper.setBlock(tablePos, BlocksPM.WAND_INSCRIPTION_TABLE.get());
        helper.assertBlockPresent(BlocksPM.WAND_INSCRIPTION_TABLE.get(), tablePos);
        BlockPos absPos = helper.absolutePos(tablePos);
        Services.PLAYER.openMenu(player, new SimpleMenuProvider(
                (windowId, inv, p) -> new WandInscriptionTableMenu(windowId, inv, ContainerLevelAccess.create(helper.getLevel(), absPos)),
                Component.empty()), absPos);
        return assertInstanceOf(helper, player.containerMenu, WandInscriptionTableMenu.class, "Menu not of expected type");
    }

    private static SpellPackage makeSpell() {
        return SpellPackageTests.touchSpell(EarthDamageSpellPayload.INSTANCE);
    }

    private static ItemStack makeScroll(SpellPackage spell) {
        ItemStack scroll = new ItemStack(ItemsPM.SPELL_SCROLL_FILLED.get());
        ItemsPM.SPELL_SCROLL_FILLED.get().setSpell(scroll, spell);
        return scroll;
    }

    private static void assertInscribedWith(GameTestHelper helper, ItemStack stack, SpellPackage spell) {
        var container = assertInstanceOf(helper, stack.getItem(), ISpellContainer.class, "Item is not a spell container");
        // The container started empty, so after inscription it holds exactly the scroll's spell
        assertValueEqual(helper, List.of(spell), container.getSpells(stack), "Inscribed spell list");
        // The newly added spell is selected, and it is the only one, so the active index is 0
        assertValueEqual(helper, 0, container.getActiveSpellIndex(stack), "Active spell index");
    }
}
