package com.verdantartifice.primalmagick.test.attunements;

import com.verdantartifice.primalmagick.common.attunements.AttunementAttributeModifiers;
import com.verdantartifice.primalmagick.common.attunements.AttunementManager;
import com.verdantartifice.primalmagick.common.attunements.AttunementThreshold;
import com.verdantartifice.primalmagick.common.attunements.AttunementType;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/**
 * Tests for attunement shackles, which toggle suppression of the lesser and greater attunement bonuses of one source
 * each time they are used.
 */
public class AttunementShacklesTests extends AbstractBaseTest {
    private static boolean hasEarthModifier(net.minecraft.server.level.ServerPlayer player) {
        return player.getAttributes().hasModifier(Attributes.ATTACK_SPEED, AttunementAttributeModifiers.EARTH_LESSER_ID);
    }

    public static void attunement_shackles_suppress_attunement_bonuses(GameTestHelper helper) {
        // Create a test player with lesser earth attunement, which grants a haste attribute modifier
        var player = makeMockServerPlayer(helper);
        AttunementManager.setAttunement(player, Sources.EARTH, AttunementType.PERMANENT, AttunementThreshold.LESSER.getValue());
        assertFalse(helper, AttunementManager.isSuppressed(player, Sources.EARTH), "Earth attunement suppressed before using shackles");
        assertTrue(helper, hasEarthModifier(player), "Player does not have the earth attunement modifier before using shackles");
        assertTrue(helper, AttunementManager.meetsThreshold(player, Sources.EARTH, AttunementThreshold.LESSER), "Player does not meet the lesser earth threshold before using shackles");

        // Use earth shackles
        useShackles(helper, player);

        // The earth bonus is suppressed and its modifier removed, but the attunement value itself is untouched
        assertTrue(helper, AttunementManager.isSuppressed(player, Sources.EARTH), "Earth attunement not suppressed after using shackles");
        assertFalse(helper, hasEarthModifier(player), "Player still has the earth attunement modifier after using shackles");
        assertFalse(helper, AttunementManager.meetsThreshold(player, Sources.EARTH, AttunementThreshold.LESSER), "Player still meets the lesser earth threshold after using shackles");
        assertValueEqual(helper, AttunementThreshold.LESSER.getValue(), AttunementManager.getTotalAttunement(player, Sources.EARTH), "Total earth attunement after using shackles");

        // Only the shackled source is affected
        assertFalse(helper, AttunementManager.isSuppressed(player, Sources.SEA), "Sea attunement suppressed by earth shackles");
        helper.succeed();
    }

    public static void attunement_shackles_second_use_restores_attunement_bonuses(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        AttunementManager.setAttunement(player, Sources.EARTH, AttunementType.PERMANENT, AttunementThreshold.LESSER.getValue());

        // Use the shackles twice, confirming the state after each use
        useShackles(helper, player);
        assertTrue(helper, AttunementManager.isSuppressed(player, Sources.EARTH), "Earth attunement not suppressed after the first use");
        assertFalse(helper, hasEarthModifier(player), "Player still has the earth attunement modifier after the first use");

        useShackles(helper, player);
        assertFalse(helper, AttunementManager.isSuppressed(player, Sources.EARTH), "Earth attunement still suppressed after the second use");
        assertTrue(helper, hasEarthModifier(player), "Player does not have the earth attunement modifier after the second use");
        assertTrue(helper, AttunementManager.meetsThreshold(player, Sources.EARTH, AttunementThreshold.LESSER), "Player does not meet the lesser earth threshold after the second use");
        helper.succeed();
    }

    private static void useShackles(GameTestHelper helper, net.minecraft.server.level.ServerPlayer player) {
        ItemStack stack = new ItemStack(ItemsPM.ATTUNEMENT_SHACKLES_EARTH.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
    }
}
