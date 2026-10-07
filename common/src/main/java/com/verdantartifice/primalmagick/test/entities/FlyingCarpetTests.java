package com.verdantartifice.primalmagick.test.entities;

import com.verdantartifice.primalmagick.common.entities.EntityTypesPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;

/**
 * Tests for picking up flying carpets.
 */
public class FlyingCarpetTests extends AbstractBaseTest {
    /** Interacting with a carpet while sneaking turns it back into its item and removes the entity. */
    public static void sneaking_interaction_picks_up_flying_carpet(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        var carpet = helper.spawn(EntityTypesPM.FLYING_CARPET.get(), pos);
        var player = makeMockServerPlayer(helper);
        player.setShiftKeyDown(true);

        var result = player.interactOn(carpet, InteractionHand.MAIN_HAND, carpet.position());

        assertTrue(helper, result.consumesAction(), "Sneaking interaction with the carpet was not consumed: " + result);
        assertTrue(helper, carpet.isRemoved(), "Carpet entity was not removed");
        assertTrue(helper, player.getVehicle() == null, "Player is riding the carpet instead of picking it up");
        helper.assertItemEntityPresent(ItemsPM.FLYING_CARPET.get(), pos, 2.0D);
        helper.succeed();
    }
}
