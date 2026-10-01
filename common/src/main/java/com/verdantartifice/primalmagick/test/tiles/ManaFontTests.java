package com.verdantartifice.primalmagick.test.tiles;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.blocks.mana.AbstractManaFontBlock;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.common.tiles.mana.AbstractManaFontTileEntity;
import com.verdantartifice.primalmagick.common.wands.IWand;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/**
 * Tests for mana fonts: siphoning mana from every font into a wand, siphoning with each kind of caster, and
 * recharging fonts of each tier. Expected values are hard-coded from the font tier definitions in
 * AbstractManaFontBlock.getManaCapacity and AbstractManaFontTileEntity.getManaRechargedPerTick, and from the siphon
 * amounts of the casters used, so that changes to those numbers are caught.
 */
public class ManaFontTests extends AbstractBaseTest {
    // MundaneWandItem.getSiphonAmount is a fixed 100 centimana; the modular casters in ChargeableItem use WandCap.GOLD,
    // whose siphon amount is 200 centimana
    protected static final int MUNDANE_WAND_SIPHON_AMOUNT = 100;
    protected static final int GOLD_CAP_SIPHON_AMOUNT = 200;

    protected static String describeFont(AbstractManaFontBlock block) {
        return block.getTierDescriptor() + " " + block.getSource().getId() + " font";
    }

    protected static AbstractManaFontTileEntity placeFont(GameTestHelper helper, AbstractManaFontBlock block) {
        var fontPos = BlockPos.ZERO;
        helper.setBlock(fontPos, block);
        helper.assertBlockState(fontPos, state -> state.is(block), state -> Component.literal("Font not placed correctly"));
        return helper.getBlockEntity(fontPos, AbstractManaFontTileEntity.class);
    }

    // Siphon tests

    /**
     * Confirms that a mundane wand siphons its fixed siphon amount of the font's source from the given font, and no
     * mana of any other source.
     */
    public static void mana_font_siphoned_by_wand(GameTestHelper helper, AbstractManaFontBlock block) {
        assertFontSiphonedBy(helper, block, ChargeableItem.MUNDANE_WAND.makeStack(), MUNDANE_WAND_SIPHON_AMOUNT);
        helper.succeed();
    }

    public static void mana_font_siphoned_by_wand(GameTestHelper helper) {
        mana_font_siphoned_by_wand(helper, BlocksPM.ANCIENT_FONT_EARTH.get());
    }

    /**
     * Confirms that a modular caster with a gold cap siphons the cap's siphon amount from an ancient earth font.
     */
    public static void mana_font_siphoned_by(GameTestHelper helper, ChargeableItem caster) {
        assertTrue(helper, caster.isCaster(), caster + " is not a caster");
        assertFontSiphonedBy(helper, BlocksPM.ANCIENT_FONT_EARTH.get(), caster.makeStack(), GOLD_CAP_SIPHON_AMOUNT);
        helper.succeed();
    }

    protected static void assertFontSiphonedBy(GameTestHelper helper, AbstractManaFontBlock block, ItemStack wandStack, int expectedSiphon) {
        String fontId = describeFont(block);

        // Create a test player in the level and put a wand in their hand
        var player = makeMockServerPlayer(helper, true);
        try {
            var wand = assertInstanceOf(helper, wandStack.getItem(), IWand.class, "Wand stack not a wand as expected");
            player.setItemInHand(InteractionHand.MAIN_HAND, wandStack);

            // Place the font block in the world and fill it
            var fontTile = placeFont(helper, block);
            final int fontCapacity = fontTile.getManaCapacity();
            fontTile.setMana(fontCapacity);

            // Confirm the initial wand and font state
            Sources.getAll().forEach(s -> assertValueEqual(helper, 0, wand.getMana(wandStack, s), "Initial wand mana for " + s.getId()));
            assertValueEqual(helper, fontCapacity, fontTile.getMana(), "Initial mana of " + fontId);

            // Siphon a bit of mana from the font
            fontTile.doSiphon(wandStack, helper.getLevel(), player, player.getEyePosition());

            // Confirm that the correct amount of mana was siphoned from the font to the wand
            assertValueEqual(helper, expectedSiphon, wand.getSiphonAmount(wandStack), "Wand siphon amount");
            Sources.getAll().forEach(s -> {
                var expectedMana = s.equals(block.getSource()) ? expectedSiphon : 0;
                assertValueEqual(helper, expectedMana, wand.getMana(wandStack, s), "Final wand mana for " + s.getId() + " after siphoning " + fontId);
            });
            assertValueEqual(helper, fontCapacity - expectedSiphon, fontTile.getMana(), "Final mana of " + fontId);
        } finally {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
    }

    // Recharge tests

    /**
     * Confirms that a font of the given block's tier has the expected capacity, recharges the expected amount of mana
     * in a single tick, and does not recharge past its capacity.
     */
    public static void mana_font_recharges(GameTestHelper helper, AbstractManaFontBlock block, int expectedRechargePerTick, int expectedCapacity) {
        String fontId = describeFont(block);

        // Place the font block in the world and ensure it's empty
        var fontTile = placeFont(helper, block);
        fontTile.setMana(0);
        assertValueEqual(helper, 0, fontTile.getMana(), "Initial mana of " + fontId);

        // Confirm the font's tier-derived values
        assertValueEqual(helper, expectedCapacity, fontTile.getManaCapacity(), "Capacity of " + fontId);
        assertValueEqual(helper, expectedRechargePerTick, fontTile.getManaRechargedPerTick(), "Recharge per tick of " + fontId);

        // Trigger a recharge tick for the font and confirm that it recharged one tick's worth of mana
        fontTile.doRecharge();
        assertValueEqual(helper, expectedRechargePerTick, fontTile.getMana(), "Mana of " + fontId + " after one recharge tick");

        // Fill the font and confirm that a recharge tick leaves it at capacity
        fontTile.setMana(expectedCapacity);
        fontTile.doRecharge();
        assertValueEqual(helper, expectedCapacity, fontTile.getMana(), "Mana of " + fontId + " after recharging when full");

        helper.succeed();
    }

    public static void mana_font_recharges(GameTestHelper helper) {
        // Ancient fonts are of the basic tier, which holds 10000 centimana and recharges 5 per tick
        mana_font_recharges(helper, BlocksPM.ANCIENT_FONT_EARTH.get(), 5, 10000);
    }
}
