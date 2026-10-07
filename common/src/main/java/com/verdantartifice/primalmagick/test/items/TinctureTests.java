package com.verdantartifice.primalmagick.test.items;

import com.verdantartifice.primalmagick.common.concoctions.ConcoctionType;
import com.verdantartifice.primalmagick.common.concoctions.ConcoctionUtils;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for drinkable tinctures: the doses shown in their tooltips, the potion effect they grant, and how drinking
 * uses up doses.
 */
public class TinctureTests extends AbstractBaseTest {
    // Leaping gives jump boost level one (amplifier 0) for 3600 ticks (Potions.LEAPING)
    private static final int LEAPING_DURATION = 3600;

    private static ItemStack makeTincture(int doses) {
        ItemStack stack = ConcoctionUtils.newConcoction(Potions.LEAPING, ConcoctionType.TINCTURE).create();
        return ConcoctionUtils.setCurrentDoses(stack, doses);
    }

    private static Player makeSurvivalPlayer(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        assertFalse(helper, player.getAbilities().instabuild, "Survival test player has infinite materials");
        return player;
    }

    private static ItemStack drink(GameTestHelper helper, Player player, ItemStack stack) {
        return stack.getItem().finishUsingItem(stack, helper.getLevel(), player);
    }

    public static void tincture_tooltip_shows_remaining_doses(GameTestHelper helper, int doses) {
        ItemStack stack = makeTincture(doses);
        assertValueEqual(helper, doses, ConcoctionUtils.getCurrentDoses(stack), "Doses of test tincture");

        List<Component> lines = new ArrayList<>();
        stack.getItem().appendHoverText(stack, Item.TooltipContext.EMPTY, TooltipDisplay.DEFAULT, lines::add, TooltipFlag.NORMAL);

        assertValueEqual(helper, 1, lines.size(), "Tooltip line count");
        var contents = assertInstanceOf(helper, lines.getFirst().getContents(), TranslatableContents.class, "Tooltip line is not translatable");
        assertValueEqual(helper, "concoctions.primalmagick.doses_remaining", contents.getKey(), "Tooltip translation key");
        assertValueEqual(helper, 1, contents.getArgs().length, "Tooltip argument count");
        assertValueEqual(helper, (Object)doses, contents.getArgs()[0], "Tooltip doses argument");
        helper.succeed();
    }

    public static void tincture_grants_its_potion_effect_when_drunk(GameTestHelper helper) {
        Player player = makeSurvivalPlayer(helper);
        assertFalse(helper, player.hasEffect(MobEffects.JUMP_BOOST), "Player already has jump boost");

        drink(helper, player, makeTincture(3));

        var effect = player.getEffect(MobEffects.JUMP_BOOST);
        assertFalse(helper, effect == null, "Player has no jump boost after drinking a leaping tincture");
        assertValueEqual(helper, 0, effect.getAmplifier(), "Jump boost amplifier");
        assertValueEqual(helper, LEAPING_DURATION, effect.getDuration(), "Jump boost duration");
        helper.succeed();
    }

    public static void tincture_loses_a_dose_when_drunk(GameTestHelper helper) {
        Player player = makeSurvivalPlayer(helper);
        ItemStack stack = makeTincture(3);

        ItemStack result = drink(helper, player, stack);

        assertTrue(helper, result.is(ItemsPM.CONCOCTION.get()), "Result of drinking a three dose tincture is not a concoction");
        assertValueEqual(helper, 2, ConcoctionUtils.getCurrentDoses(result), "Doses after one drink");
        assertValueEqual(helper, ConcoctionType.TINCTURE, ConcoctionUtils.getConcoctionType(result), "Concoction type after one drink");
        helper.succeed();
    }

    public static void tincture_returns_skyglass_flask_when_last_dose_drunk(GameTestHelper helper) {
        Player player = makeSurvivalPlayer(helper);
        ItemStack stack = makeTincture(1);

        ItemStack result = drink(helper, player, stack);

        assertTrue(helper, result.is(ItemsPM.SKYGLASS_FLASK.get()), "Result of drinking the last dose is not a skyglass flask: " + result);
        assertValueEqual(helper, 1, result.getCount(), "Flask count");
        helper.succeed();
    }
}
