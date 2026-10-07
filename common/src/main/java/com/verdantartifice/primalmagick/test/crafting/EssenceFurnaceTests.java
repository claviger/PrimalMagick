package com.verdantartifice.primalmagick.test.crafting;

import com.verdantartifice.primalmagick.common.affinities.AffinityManager;
import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.menus.CalcinatorMenu;
import com.verdantartifice.primalmagick.common.research.ResearchEntries;
import com.verdantartifice.primalmagick.common.research.ResearchManager;
import com.verdantartifice.primalmagick.common.sources.SourceList;
import com.verdantartifice.primalmagick.common.tiles.crafting.AbstractCalcinatorTileEntity;
import com.verdantartifice.primalmagick.platform.Services;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.CompletableFuture;

/**
 * Tests for the essence furnace, the entry-level calcinator. It yields one essence dust for each source whose affinity
 * on the input item reaches the dust affinity of 5 (EssenceType.DUST), regardless of how far above that it is.
 */
public class EssenceFurnaceTests extends AbstractBaseTest {
    private static final BlockPos FURNACE_POS = new BlockPos(1, 1, 1);

    private static AbstractCalcinatorTileEntity placeFurnace(GameTestHelper helper, Player player) {
        helper.setBlock(FURNACE_POS, BlocksPM.ESSENCE_FURNACE.get());
        var furnace = helper.getBlockEntity(FURNACE_POS, AbstractCalcinatorTileEntity.class);
        furnace.setTileOwner(player);
        return furnace;
    }

    private static InteractionResult useWandOnFurnace(GameTestHelper helper, Player player, ItemStack wandStack) {
        BlockPos posAbs = helper.absolutePos(FURNACE_POS);
        var hit = new BlockHitResult(Vec3.atCenterOf(posAbs), Direction.UP, posAbs, false);
        var context = new UseOnContext(player, InteractionHand.MAIN_HAND, hit);
        return Services.ITEMS.onItemUseFirst(wandStack.getItem(), wandStack, context);
    }

    public static void essence_furnace_wand_transform_works_after_basic_alchemy(GameTestHelper helper) {
        // Create a player who has read the Basic Alchemy entry
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ResearchManager.forceGrantWithAllParents(player, ResearchEntries.BASIC_ALCHEMY);

        // Put a mundane wand in the main hand of that player and place a vanilla furnace
        ItemStack wandStack = new ItemStack(ItemsPM.MUNDANE_WAND.get());
        Item wandItem = wandStack.getItem();
        player.setItemInHand(InteractionHand.MAIN_HAND, wandStack);
        helper.setBlock(FURNACE_POS, Blocks.FURNACE);
        helper.assertBlockPresent(Blocks.FURNACE, FURNACE_POS);

        // Start transforming the furnace and keep channeling until it changes or the test times out
        assertTrue(helper, useWandOnFurnace(helper, player, wandStack).equals(InteractionResult.SUCCESS), "Failed to start using wand on furnace");
        int[] remainingTicks = {wandItem.getUseDuration(wandStack, player)};
        helper.onEachTick(() -> wandItem.onUseTick(helper.getLevel(), player, wandStack, --remainingTicks[0]));
        helper.succeedWhen(() -> helper.assertBlockPresent(BlocksPM.ESSENCE_FURNACE.get(), FURNACE_POS));
    }

    public static void essence_furnace_wand_transform_does_nothing_before_basic_alchemy(GameTestHelper helper) {
        // Create a player who has not read the Basic Alchemy entry
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        assertFalse(helper, ResearchManager.isResearchComplete(player, ResearchEntries.BASIC_ALCHEMY), "Fresh player already knows Basic Alchemy");

        ItemStack wandStack = new ItemStack(ItemsPM.MUNDANE_WAND.get());
        Item wandItem = wandStack.getItem();
        player.setItemInHand(InteractionHand.MAIN_HAND, wandStack);
        helper.setBlock(FURNACE_POS, Blocks.FURNACE);

        // The wand does not recognize the furnace as a transform target, and channeling for the full duration changes nothing
        assertTrue(helper, useWandOnFurnace(helper, player, wandStack).equals(InteractionResult.PASS), "Wand use on furnace was not passed over");
        int[] remainingTicks = {wandItem.getUseDuration(wandStack, player)};
        helper.onEachTick(() -> wandItem.onUseTick(helper.getLevel(), player, wandStack, --remainingTicks[0]));
        helper.runAfterDelay(60, () -> {
            helper.assertBlockPresent(Blocks.FURNACE, FURNACE_POS);
            helper.succeed();
        });
    }

    public static void essence_furnace_can_have_its_menu_opened(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var furnace = placeFurnace(helper, player);

        Services.PLAYER.openMenu(player, furnace, FURNACE_POS);
        assertInstanceOf(helper, player.containerMenu, CalcinatorMenu.class, "Menu not of expected type");
        helper.succeed();
    }

    public static void essence_furnace_generates_dust_for_each_source_of_affinity(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var furnace = placeFurnace(helper, player);

        // An item with earth 5, sea 10, sun 7, and sky 4 should yield one dust each of earth, sea, and sun. Sky is below
        // the dust affinity of 5, so it yields nothing. Outputs are added in the source order of Sources.getAllSorted.
        ItemStack input = new ItemStack(Items.AMETHYST_SHARD);
        AffinityManager.getInstance().setCachedItemResult(input, CompletableFuture.completedFuture(SourceList.builder().withEarth(5).withSea(10).withSky(4).withSun(7).build()));
        furnace.addItem(0, 0, input.copy());
        assertTrue(helper, furnace.canCalcinate(furnace.getItem(0, 0)), "Furnace cannot calcinate the test item");
        furnace.doCalcination();

        assertTrue(helper, furnace.getItem(0, 0).isEmpty(), "Input item stack not consumed");
        assertItemSlot(helper, furnace.getItem(2, 0), ItemsPM.ESSENCE_DUST_EARTH.get(), "first output slot");
        assertItemSlot(helper, furnace.getItem(2, 1), ItemsPM.ESSENCE_DUST_SEA.get(), "second output slot");
        assertItemSlot(helper, furnace.getItem(2, 2), ItemsPM.ESSENCE_DUST_SUN.get(), "third output slot");
        for (int index = 3; index < 9; index++) {
            assertTrue(helper, furnace.getItem(2, index).isEmpty(), "Output slot " + index + " is not empty: " + furnace.getItem(2, index));
        }
        helper.succeed();
    }

    public static void essence_furnace_output_is_one_dust_per_source_regardless_of_affinity(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var furnace = placeFurnace(helper, player);

        // An affinity of 500 would be several dust in a calcinator, but the furnace only ever gives one per source
        ItemStack input = new ItemStack(Items.DIAMOND);
        AffinityManager.getInstance().setCachedItemResult(input, CompletableFuture.completedFuture(SourceList.builder().withEarth(500).build()));
        furnace.addItem(0, 0, input.copy());
        furnace.doCalcination();

        assertTrue(helper, furnace.getItem(0, 0).isEmpty(), "Input item stack not consumed");
        assertItemSlot(helper, furnace.getItem(2, 0), ItemsPM.ESSENCE_DUST_EARTH.get(), "first output slot");
        for (int index = 1; index < 9; index++) {
            assertTrue(helper, furnace.getItem(2, index).isEmpty(), "Output slot " + index + " is not empty: " + furnace.getItem(2, index));
        }
        helper.succeed();
    }

    private static void assertItemSlot(GameTestHelper helper, ItemStack actual, Item expectedItem, String slotName) {
        assertTrue(helper, actual.is(expectedItem), "Item in " + slotName + " is not " + expectedItem + ": " + actual);
        assertValueEqual(helper, 1, actual.getCount(), "Count in " + slotName);
    }
}
