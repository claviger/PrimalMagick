package com.verdantartifice.primalmagick.test.spells;

import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.spells.SpellPropertiesPM;
import com.verdantartifice.primalmagick.common.spells.payloads.EarthDamageSpellPayload;
import com.verdantartifice.primalmagick.common.spells.vehicles.TouchSpellVehicle;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

/**
 * Tests of casting spells from spell scrolls.
 */
public class SpellScrollTests extends AbstractBaseTest {
    private static ItemStack makeScroll(int count) {
        var spell = SpellPackageTests.spellWithVehicle(TouchSpellVehicle.INSTANCE)
                .payload().type(EarthDamageSpellPayload.INSTANCE).with(SpellPropertiesPM.POWER.get(), 1).end()
                .build();
        var stack = new ItemStack(ItemsPM.SPELL_SCROLL_FILLED.get(), count);
        ItemsPM.SPELL_SCROLL_FILLED.get().setSpell(stack, spell);
        return stack;
    }

    private static void useScroll(GameTestHelper helper, GameType gameType, int startCount, int expectedCount) {
        var player = makeMockServerPlayer(helper, true);
        player.setGameMode(gameType);
        player.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());
        player.setItemInHand(InteractionHand.MAIN_HAND, makeScroll(startCount));

        var result = player.getItemInHand(InteractionHand.MAIN_HAND).use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        assertTrue(helper, result.consumesAction(), "Scroll use was not a success: " + result);
        assertValueEqual(helper, expectedCount, player.getItemInHand(InteractionHand.MAIN_HAND).getCount(), "Scrolls left after use in " + gameType);
    }

    public static void spell_scroll_is_consumed_when_used_in_survival(GameTestHelper helper) {
        useScroll(helper, GameType.SURVIVAL, 2, 1);
        helper.succeed();
    }

    public static void spell_scroll_is_not_consumed_when_used_in_creative(GameTestHelper helper) {
        useScroll(helper, GameType.CREATIVE, 2, 2);
        helper.succeed();
    }
}
