package com.verdantartifice.primalmagick.test.crafting;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.crafting.WandInscriptionRecipe;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.menus.WandInscriptionTableMenu;
import com.verdantartifice.primalmagick.common.spells.SpellPackage;
import com.verdantartifice.primalmagick.common.spells.payloads.EarthDamageSpellPayload;
import com.verdantartifice.primalmagick.common.spells.payloads.FlameDamageSpellPayload;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;
import java.util.Optional;

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

    public static void wand_inscription_clears_spells_without_scroll(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var menu = openTable(helper, player);
        SpellPackage spell = makeSpell();

        // An apprentice spelltome holding one spell, with no scroll present, should offer a cleared copy of itself
        ItemStack filledTome = new ItemStack(ItemsPM.SPELLTOME_APPRENTICE.get());
        assertTrue(helper, ItemsPM.SPELLTOME_APPRENTICE.get().addSpell(filledTome, spell), "Could not pre-inscribe spelltome");
        assertTrue(helper, ItemsPM.SPELLTOME_APPRENTICE.get().setActiveSpellIndex(filledTome, 0), "Could not select pre-inscribed spell");
        menu.getSlot(CASTER_SLOT).set(filledTome);

        // Clearing removes every spell and the selection, so the list is empty and the index is NO_SPELL_SELECTED (-1)
        ItemStack preview = menu.getSlot(RESULT_SLOT).getItem();
        assertTrue(helper, preview.is(ItemsPM.SPELLTOME_APPRENTICE.get()), "Result is not an apprentice spelltome: " + preview);
        assertSpells(helper, preview, List.of(), ISpellContainer.NO_SPELL_SELECTED);

        // Taking the cleared tome should consume the original from the caster slot
        ItemStack output = menu.quickMoveStack(player, RESULT_SLOT);
        assertTrue(helper, output.is(ItemsPM.SPELLTOME_APPRENTICE.get()), "Taken output is not an apprentice spelltome: " + output);
        assertSpells(helper, output, List.of(), ISpellContainer.NO_SPELL_SELECTED);
        assertTrue(helper, menu.getSlot(CASTER_SLOT).getItem().isEmpty(), "Caster slot not empty after taking result");
        assertTrue(helper, menu.getSlot(RESULT_SLOT).getItem().isEmpty(), "Result slot not empty after taking result");

        helper.succeed();
    }

    public static void wand_inscription_empty_caster_without_scroll_gives_nothing(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var menu = openTable(helper, player);

        // An empty spelltome has nothing to clear, so with no scroll there is no output
        menu.getSlot(CASTER_SLOT).set(new ItemStack(ItemsPM.SPELLTOME_APPRENTICE.get()));
        assertTrue(helper, menu.getSlot(RESULT_SLOT).getItem().isEmpty(), "Empty spelltome produced an output: " + menu.getSlot(RESULT_SLOT).getItem());

        // Likewise for an empty modular wand
        menu.getSlot(CASTER_SLOT).set(TestUtils.makeModularCaster(ItemsPM.MODULAR_WAND.get(), WandCore.HEARTWOOD, WandCap.IRON, WandGem.APPRENTICE));
        assertTrue(helper, menu.getSlot(RESULT_SLOT).getItem().isEmpty(), "Empty modular wand produced an output: " + menu.getSlot(RESULT_SLOT).getItem());

        helper.succeed();
    }

    public static void wand_inscription_appends_spell_to_partially_filled_spelltome(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var menu = openTable(helper, player);
        SpellPackage spellA = makeSpell();
        SpellPackage spellB = SpellPackageTests.touchSpell(FlameDamageSpellPayload.INSTANCE);

        // An adept spelltome (ENCHANTED tier) holds two spells, so one already inscribed leaves room for a second
        ItemStack tome = new ItemStack(ItemsPM.SPELLTOME_ADEPT.get());
        assertTrue(helper, ItemsPM.SPELLTOME_ADEPT.get().addSpell(tome, spellA), "Could not pre-inscribe spelltome");
        assertTrue(helper, ItemsPM.SPELLTOME_ADEPT.get().setActiveSpellIndex(tome, 0), "Could not select pre-inscribed spell");
        menu.getSlot(CASTER_SLOT).set(tome);
        menu.getSlot(SCROLL_SLOT).set(makeScroll(spellB));

        // The new spell is appended after A and selected, so the active index is that of the second entry, 1
        ItemStack preview = menu.getSlot(RESULT_SLOT).getItem();
        assertTrue(helper, preview.is(ItemsPM.SPELLTOME_ADEPT.get()), "Result is not an adept spelltome: " + preview);
        assertSpells(helper, preview, List.of(spellA, spellB), 1);

        ItemStack output = menu.quickMoveStack(player, RESULT_SLOT);
        assertSpells(helper, output, List.of(spellA, spellB), 1);
        assertTrue(helper, menu.getSlot(CASTER_SLOT).getItem().isEmpty(), "Caster slot not empty after taking result");
        assertTrue(helper, menu.getSlot(SCROLL_SLOT).getItem().isEmpty(), "Scroll slot not empty after taking result");

        helper.succeed();
    }

    public static void wand_inscription_all_recipe_keys_resolve(GameTestHelper helper) {
        // One key each for the modular wand and staff, plus one per spelltome tier (apprentice, adept, wizard, archmage)
        assertValueEqual(helper, 6, WandInscriptionRecipe.ALL_KEYS.size(), "Number of wand inscription recipe keys");
        for (ResourceKey<Recipe<?>> key : WandInscriptionRecipe.ALL_KEYS) {
            Optional<RecipeHolder<?>> holder = helper.getLevel().recipeAccess().byKey(key);
            assertTrue(helper, holder.isPresent(), "No recipe loaded for key " + key);
            assertTrue(helper, holder.get().value() instanceof WandInscriptionRecipe, "Recipe for key " + key + " is not a wand inscription recipe");
        }

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
        // The container started empty, so after inscription it holds exactly the scroll's spell; the newly added
        // spell is selected, and it is the only one, so the active index is 0
        assertSpells(helper, stack, List.of(spell), 0);
    }

    private static void assertSpells(GameTestHelper helper, ItemStack stack, List<SpellPackage> expectedSpells, int expectedActiveIndex) {
        var container = assertInstanceOf(helper, stack.getItem(), ISpellContainer.class, "Item is not a spell container");
        assertValueEqual(helper, expectedSpells, container.getSpells(stack), "Inscribed spell list");
        assertValueEqual(helper, expectedActiveIndex, container.getActiveSpellIndex(stack), "Active spell index");
    }
}
