package com.verdantartifice.primalmagick.test.tiles;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.components.DataComponentsPM;
import com.verdantartifice.primalmagick.common.concoctions.ConcoctionType;
import com.verdantartifice.primalmagick.common.concoctions.ConcoctionUtils;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.items.essence.EssenceItem;
import com.verdantartifice.primalmagick.common.items.essence.EssenceType;
import com.verdantartifice.primalmagick.common.menus.ConcocterMenu;
import com.verdantartifice.primalmagick.common.research.ResearchEntries;
import com.verdantartifice.primalmagick.common.research.ResearchManager;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.common.tiles.crafting.ConcocterTileEntity;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameType;

/**
 * Tests for the concocter: menu access, absorbing infernal mana from a slotted wand, and brewing a tincture.
 */
public class ConcocterTests extends AbstractBaseTest {
    // The concocter's inventory indices are protected constants of ConcocterTileEntity: input 0, wand 1, output 2
    private static final int INPUT_INV_INDEX = 0;
    private static final int WAND_INV_INDEX = 1;
    private static final int OUTPUT_INV_INDEX = 2;

    // Cook time is 100 ticks (ConcocterTileEntity.getCookTimeTotal), and the leaping tincture recipe costs 200 centimana
    // of infernal mana (leaping_tincture.json) and makes a three dose tincture
    private static final int COOK_TICKS = 100;
    private static final int TINCTURE_MANA_COST = 200;
    private static final int TINCTURE_DOSES = 3;

    public static void concocter_can_have_its_menu_opened(GameTestHelper helper) {
        MagitechTileTestUtils.assertMenuOpens(helper, BlocksPM.CONCOCTER.get(), ConcocterTileEntity.class, ConcocterMenu.class);
    }

    public static void concocter_absorbs_infernal_mana_from_wand(GameTestHelper helper) {
        MagitechTileTestUtils.assertAbsorbsWandMana(helper, BlocksPM.CONCOCTER.get(), ConcocterTileEntity.class, ConcocterTileEntity::tick, WAND_INV_INDEX, Sources.INFERNAL);
    }

    public static void concocter_creates_three_dose_tinctures(GameTestHelper helper) {
        // Create an owner who has the research needed for tincture recipes
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ResearchManager.forceGrantWithAllParents(player, ResearchEntries.CONCOCTING_TINCTURES);
        assertTrue(helper, ResearchManager.isResearchComplete(player, ResearchEntries.CONCOCTING_TINCTURES), "Failed to grant tincture research");

        var pos = BlockPos.ZERO;
        helper.setBlock(pos, BlocksPM.CONCOCTER.get());
        var tile = helper.getBlockEntity(pos, ConcocterTileEntity.class);
        tile.setTileOwner(player);

        // Load the ingredients of the leaping tincture recipe: a water concoction, sky dust, nether wart, and a rabbit's foot
        tile.setMana(Sources.INFERNAL, TINCTURE_MANA_COST * 2);
        tile.addItem(INPUT_INV_INDEX, 0, ItemsPM.CONCOCTION.get().getDefaultInstance());
        tile.addItem(INPUT_INV_INDEX, 1, EssenceItem.getEssence(EssenceType.DUST, Sources.SKY));
        tile.addItem(INPUT_INV_INDEX, 2, new ItemStack(Items.NETHER_WART));
        tile.addItem(INPUT_INV_INDEX, 3, new ItemStack(Items.RABBIT_FOOT));
        for (int slot = 0; slot < 4; slot++) {
            assertFalse(helper, tile.getItem(INPUT_INV_INDEX, slot).isEmpty(), "Ingredient " + slot + " not accepted");
        }

        // Nothing is produced until the cook completes
        for (int i = 0; i < COOK_TICKS - 1; i++) {
            ConcocterTileEntity.tick(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos), tile);
        }
        assertTrue(helper, tile.getItem(OUTPUT_INV_INDEX, 0).isEmpty(), "Output produced before the cook finished");

        // The final tick completes the cook
        ConcocterTileEntity.tick(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos), tile);
        var output = tile.getItem(OUTPUT_INV_INDEX, 0);
        assertValueEqual(helper, ItemsPM.CONCOCTION.get(), output.getItem(), "Output item");
        assertValueEqual(helper, ConcoctionType.TINCTURE, ConcoctionUtils.getConcoctionType(output), "Output concoction type");
        assertValueEqual(helper, TINCTURE_DOSES, ConcoctionUtils.getCurrentDoses(output), "Output doses");
        assertTrue(helper, output.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.LEAPING), "Output potion is not leaping");
        assertTrue(helper, output.has(DataComponentsPM.CONCOCTION_DOSES.get()), "Output has no doses component");
        for (int slot = 0; slot < 4; slot++) {
            assertTrue(helper, tile.getItem(INPUT_INV_INDEX, slot).isEmpty(), "Ingredient " + slot + " not consumed");
        }
        assertValueEqual(helper, TINCTURE_MANA_COST, tile.getMana(Sources.INFERNAL), "Infernal mana remaining");
        helper.succeed();
    }
}
