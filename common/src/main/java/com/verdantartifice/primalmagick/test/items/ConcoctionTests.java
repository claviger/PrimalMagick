package com.verdantartifice.primalmagick.test.items;

import com.verdantartifice.primalmagick.common.components.DataComponentsPM;
import com.verdantartifice.primalmagick.common.concoctions.ConcoctionType;
import com.verdantartifice.primalmagick.common.concoctions.ConcoctionUtils;
import com.verdantartifice.primalmagick.common.concoctions.FuseType;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

import java.util.List;

/**
 * Tests for ConcoctionUtils: building concoction and bomb stacks, reading and writing their concoction type, dose
 * count, and fuse type components, identifying bombs, and classifying potions as beneficial. Maximum dose counts and
 * defaults are hard-coded from ConcoctionType and ConcoctionUtils so that changes to them are caught.
 */
public class ConcoctionTests extends AbstractBaseTest {
    // A tincture holds at most three doses, which leaves room to test values below, at, and above the maximum
    protected static final ConcoctionType DOSE_TEST_TYPE = ConcoctionType.TINCTURE;
    protected static final int DOSE_TEST_MAX = 3;

    // Stack data that is absent falls back to a plain water concoction with a medium fuse
    protected static final ConcoctionType DEFAULT_CONCOCTION_TYPE = ConcoctionType.WATER;
    protected static final FuseType DEFAULT_FUSE_TYPE = FuseType.MEDIUM;

    // Hard-coded from ConcoctionType.BOMB so that a change to the bomb's dose count is caught
    protected static final int BOMB_MAX_DOSES = 6;

    protected static void assertHasPotion(GameTestHelper helper, Holder<Potion> expected, ItemStack stack) {
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        assertTrue(helper, contents.is(expected), "Stack does not contain potion " + expected.getRegisteredName() + ": " + contents);
    }

    // Concoction type and dose tests

    /**
     * The BOMB case only pins that type's dose count; gameplay never puts the BOMB type on the concoction item, but the
     * helpers treat it like any other type. Since getConcoctionType falls back to WATER, the presence of the type
     * component is checked directly so that the WATER case is meaningful.
     */
    public static void concoction_type_round_trip(GameTestHelper helper, ConcoctionType type, int expectedMaxDoses) {
        // A new concoction is created with the given type and a full set of doses
        ItemStackTemplate template = ConcoctionUtils.newConcoction(Potions.HEALING, type);
        assertValueEqual(helper, type, ConcoctionUtils.getConcoctionType(template), "Concoction type of template");
        ItemStack created = template.create();
        assertValueEqual(helper, ItemsPM.CONCOCTION.get(), created.getItem(), "Item of created concoction");
        assertTrue(helper, created.has(DataComponentsPM.CONCOCTION_TYPE.get()), "Created stack has no concoction type component");
        assertValueEqual(helper, type, ConcoctionUtils.getConcoctionType(created), "Concoction type of created stack");
        assertValueEqual(helper, expectedMaxDoses, ConcoctionUtils.getCurrentDoses(created), "Doses of created stack");
        assertHasPotion(helper, Potions.HEALING, created);

        // Setting the type on a bare stack also fills it to that type's maximum doses
        ItemStack stack = new ItemStack(ItemsPM.CONCOCTION.get());
        assertFalse(helper, stack.has(DataComponentsPM.CONCOCTION_TYPE.get()), "Bare stack already has a concoction type component");
        ConcoctionUtils.setConcoctionType(stack, type);
        assertTrue(helper, stack.has(DataComponentsPM.CONCOCTION_TYPE.get()), "Stack has no concoction type component after setting");
        assertValueEqual(helper, type, ConcoctionUtils.getConcoctionType(stack), "Concoction type after setting");
        assertValueEqual(helper, expectedMaxDoses, ConcoctionUtils.getCurrentDoses(stack), "Doses after setting type");

        // A null type is ignored and leaves the stack unchanged
        ConcoctionUtils.setConcoctionType(stack, null);
        assertValueEqual(helper, type, ConcoctionUtils.getConcoctionType(stack), "Concoction type after setting null");
        assertValueEqual(helper, expectedMaxDoses, ConcoctionUtils.getCurrentDoses(stack), "Doses after setting null type");
        helper.succeed();
    }

    /**
     * setCurrentDoses caps the stored value at the stack's maximum doses, as determined by its concoction type, so
     * values at or below the maximum round trip unchanged and values above it are stored as the maximum.
     */
    public static void concoction_doses_round_trip(GameTestHelper helper) {
        var stack = ConcoctionUtils.newConcoction(Potions.HEALING, DOSE_TEST_TYPE).create();
        for (int doses : List.of(0, 1, 2, DOSE_TEST_MAX)) {
            ConcoctionUtils.setCurrentDoses(stack, doses);
            assertValueEqual(helper, doses, ConcoctionUtils.getCurrentDoses(stack), "Doses after setting " + doses);
        }
        for (int doses : List.of(DOSE_TEST_MAX + 1, 100)) {
            ConcoctionUtils.setCurrentDoses(stack, doses);
            assertValueEqual(helper, DOSE_TEST_MAX, ConcoctionUtils.getCurrentDoses(stack), "Doses after setting " + doses);
        }

        // A stack with no concoction type is treated as water, which holds a single dose
        var untyped = new ItemStack(ItemsPM.CONCOCTION.get());
        ConcoctionUtils.setCurrentDoses(untyped, DOSE_TEST_MAX);
        assertValueEqual(helper, 1, ConcoctionUtils.getCurrentDoses(untyped), "Doses of untyped stack after overfilling");
        helper.succeed();
    }

    // Bomb tests

    public static void concoction_bomb_fuse_round_trip(GameTestHelper helper, FuseType fuse) {
        // A new bomb carries the given fuse along with the bomb concoction type and its full set of doses
        ItemStack bomb = ConcoctionUtils.newBomb(Potions.HEALING, fuse).create();
        assertValueEqual(helper, ItemsPM.ALCHEMICAL_BOMB.get(), bomb.getItem(), "Item of created bomb");
        assertValueEqual(helper, fuse, ConcoctionUtils.getFuseType(bomb), "Fuse type of created bomb");
        assertValueEqual(helper, ConcoctionType.BOMB, ConcoctionUtils.getConcoctionType(bomb), "Concoction type of created bomb");
        assertValueEqual(helper, BOMB_MAX_DOSES, ConcoctionUtils.getCurrentDoses(bomb), "Doses of created bomb");
        assertHasPotion(helper, Potions.HEALING, bomb);

        // Setting the fuse on a stack that already has a different one replaces it
        FuseType otherFuse = fuse.getNext();
        ItemStack stack = ConcoctionUtils.newBomb(Potions.HEALING, otherFuse).create();
        assertValueEqual(helper, otherFuse, ConcoctionUtils.getFuseType(stack), "Fuse type before setting");
        ConcoctionUtils.setFuseType(stack, fuse);
        assertValueEqual(helper, fuse, ConcoctionUtils.getFuseType(stack), "Fuse type after setting");

        // A null fuse is ignored and leaves the stack unchanged
        ConcoctionUtils.setFuseType(stack, null);
        assertValueEqual(helper, fuse, ConcoctionUtils.getFuseType(stack), "Fuse type after setting null");
        helper.succeed();
    }

    public static void concoction_bomb_default_fuse(GameTestHelper helper) {
        // A bomb created without an explicit fuse is given a medium fuse. Since getFuseType also falls back to MEDIUM
        // when no fuse is stored, the presence of the fuse component is checked directly.
        ItemStack bomb = ConcoctionUtils.newBomb(Potions.HEALING).create();
        assertValueEqual(helper, ItemsPM.ALCHEMICAL_BOMB.get(), bomb.getItem(), "Item of created bomb");
        assertTrue(helper, bomb.has(DataComponentsPM.FUSE_TYPE.get()), "Created bomb has no fuse type component");
        assertValueEqual(helper, DEFAULT_FUSE_TYPE, ConcoctionUtils.getFuseType(bomb), "Default fuse type of created bomb");
        helper.succeed();
    }

    public static void concoction_is_bomb(GameTestHelper helper) {
        assertTrue(helper, ConcoctionUtils.isBomb(ConcoctionUtils.newBomb(Potions.HEALING).create()), "Alchemical bomb is not a bomb");
        assertFalse(helper, ConcoctionUtils.isBomb(ConcoctionUtils.newConcoction(Potions.HEALING, ConcoctionType.TINCTURE).create()), "Concoction is a bomb");
        assertFalse(helper, ConcoctionUtils.isBomb(new ItemStack(ItemsPM.SKYGLASS_FLASK.get())), "Skyglass flask is a bomb");
        assertFalse(helper, ConcoctionUtils.isBomb(new ItemStack(ItemsPM.BOMB_CASING.get())), "Empty bomb casing is a bomb");
        assertFalse(helper, ConcoctionUtils.isBomb(new ItemStack(Items.STICK)), "Stick is a bomb");
        assertFalse(helper, ConcoctionUtils.isBomb(ItemStack.EMPTY), "Empty stack is a bomb");
        helper.succeed();
    }

    /**
     * hasBeneficialEffect returns true if *any* of the potion's effects is beneficial, regardless of any harmful
     * effects alongside it.
     */
    public static void concoction_has_beneficial_effect(GameTestHelper helper) {
        // Healing grants only instant health, which is beneficial
        assertTrue(helper, ConcoctionUtils.hasBeneficialEffect(Potions.HEALING.value()), "Healing has no beneficial effect");

        // Poison inflicts only poison, which is harmful
        assertFalse(helper, ConcoctionUtils.hasBeneficialEffect(Potions.POISON.value()), "Poison has a beneficial effect");

        // Water has no effects at all
        assertFalse(helper, ConcoctionUtils.hasBeneficialEffect(Potions.WATER.value()), "Water has a beneficial effect");

        // The Turtle Master potion combines harmful slowness with beneficial resistance, so the resistance is enough
        assertTrue(helper, ConcoctionUtils.hasBeneficialEffect(Potions.TURTLE_MASTER.value()), "Turtle Master has no beneficial effect");
        helper.succeed();
    }

    public static void concoction_defaults_on_non_concoction(GameTestHelper helper) {
        // Stacks without concoction data report water, a medium fuse, and no doses
        for (ItemStack stack : List.of(new ItemStack(Items.STICK), ItemStack.EMPTY)) {
            assertValueEqual(helper, DEFAULT_CONCOCTION_TYPE, ConcoctionUtils.getConcoctionType(stack), "Concoction type of " + stack);
            assertValueEqual(helper, DEFAULT_FUSE_TYPE, ConcoctionUtils.getFuseType(stack), "Fuse type of " + stack);
            assertValueEqual(helper, 0, ConcoctionUtils.getCurrentDoses(stack), "Doses of " + stack);
        }
        helper.succeed();
    }
}
