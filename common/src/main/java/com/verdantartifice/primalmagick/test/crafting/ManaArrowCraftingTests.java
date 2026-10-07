package com.verdantartifice.primalmagick.test.crafting;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.menus.ArcaneWorkbenchMenu;
import com.verdantartifice.primalmagick.common.research.ResearchDisciplines;
import com.verdantartifice.primalmagick.common.research.ResearchEntries;
import com.verdantartifice.primalmagick.common.research.ResearchManager;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.common.stats.ExpertiseManager;
import com.verdantartifice.primalmagick.common.wands.IWand;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

/**
 * Tests for the expertise granted by crafting mana-tinged arrows in the arcane workbench. Mana arrows are a basic-tier
 * recipe group, so each batch grants 1 base expertise (ResearchTier.BASIC default expertise), and the first batch of
 * any arrow type grants a further 4 bonus expertise (ResearchTier.BASIC default bonus expertise). All arrow recipes
 * share the expertise group primalmagick:mana_arrow, so after one arrow type has been crafted no other arrow type
 * earns the bonus.
 */
public class ManaArrowCraftingTests extends AbstractBaseTest {
    private static final int BASE_EXPERTISE = 1;
    private static final int BONUS_EXPERTISE = 4;
    private static final int ARROWS_PER_BATCH = 4;

    private static ArcaneWorkbenchMenu openWorkbench(GameTestHelper helper, ServerPlayer player) {
        BlockPos tablePos = new BlockPos(1, 1, 1);
        helper.setBlock(tablePos, BlocksPM.ARCANE_WORKBENCH.get());
        var menuProvider = new MenuProvider() {
            @Override
            public @NotNull AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
                return new ArcaneWorkbenchMenu(windowId, inv, ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(tablePos)));
            }

            @Override
            public @NotNull Component getDisplayName() {
                return Component.literal("Arcane Workbench");
            }
        };
        player.openMenu(menuProvider);
        return assertInstanceOf(helper, player.containerMenu, ArcaneWorkbenchMenu.class, "Menu not of expected type");
    }

    private static void insertFullWand(GameTestHelper helper, ArcaneWorkbenchMenu menu) {
        ItemStack wandStack = ItemsPM.MUNDANE_WAND.get().getDefaultInstance();
        IWand wand = assertInstanceOf(helper, wandStack.getItem(), IWand.class, "Wand not of expected type");
        Sources.getAll().forEach(s -> wand.addMana(wandStack, s, wand.getMaxMana(wandStack, s)));
        menu.getSlots().get(10).safeInsert(wandStack);
    }

    /**
     * Fills the arrow recipe's plus-shaped pattern (arrows at the four edge midpoints, dust in the center) and takes the result.
     */
    private static void craftBatch(GameTestHelper helper, ServerPlayer player, ArcaneWorkbenchMenu menu, Item dust, Item expectedArrow) {
        for (int slot : new int[] {2, 4, 6, 8}) {
            menu.getSlots().get(slot).safeInsert(new ItemStack(Items.ARROW));
        }
        menu.getSlots().get(5).safeInsert(new ItemStack(dust));
        var output = menu.quickMoveStack(player, 0);
        assertTrue(helper, output.is(expectedArrow), "Output item not of expected type: " + output);
        assertValueEqual(helper, ARROWS_PER_BATCH, output.getCount(), "Output arrow count");
        assertFalse(helper, menu.getSlots().get(5).hasItem(), "Dust material stack not consumed");
    }

    private static int getExpertise(ServerPlayer player) {
        return ExpertiseManager.getValue(player, ResearchDisciplines.MANAWEAVING).orElse(-1);
    }

    private static ServerPlayer makePlayer(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        ResearchManager.forceGrantWithAllParents(player, ResearchEntries.MANA_ARROWS);
        assertValueEqual(helper, 0, getExpertise(player), "Starting expertise");
        return player;
    }

    public static void mana_arrow_first_craft_grants_base_and_bonus_expertise(GameTestHelper helper) {
        var player = makePlayer(helper);
        var menu = openWorkbench(helper, player);
        insertFullWand(helper, menu);

        craftBatch(helper, player, menu, ItemsPM.ESSENCE_DUST_EARTH.get(), ItemsPM.MANA_ARROW_EARTH.get());
        assertValueEqual(helper, BASE_EXPERTISE + BONUS_EXPERTISE, getExpertise(player), "Expertise after the first batch");
        helper.succeed();
    }

    public static void mana_arrow_subsequent_crafts_grant_only_base_expertise(GameTestHelper helper) {
        var player = makePlayer(helper);
        var menu = openWorkbench(helper, player);
        insertFullWand(helper, menu);

        // The first batch takes the bonus
        craftBatch(helper, player, menu, ItemsPM.ESSENCE_DUST_EARTH.get(), ItemsPM.MANA_ARROW_EARTH.get());
        int afterFirst = getExpertise(player);
        assertValueEqual(helper, BASE_EXPERTISE + BONUS_EXPERTISE, afterFirst, "Expertise after the first batch");

        // A repeat batch of the same arrow grants only the base amount
        craftBatch(helper, player, menu, ItemsPM.ESSENCE_DUST_EARTH.get(), ItemsPM.MANA_ARROW_EARTH.get());
        int afterSecond = getExpertise(player);
        assertValueEqual(helper, afterFirst + BASE_EXPERTISE, afterSecond, "Expertise after a repeat batch of the same arrow");

        // A first batch of a different arrow type is in the same expertise group, so it also grants only the base amount
        craftBatch(helper, player, menu, ItemsPM.ESSENCE_DUST_SEA.get(), ItemsPM.MANA_ARROW_SEA.get());
        assertValueEqual(helper, afterSecond + BASE_EXPERTISE, getExpertise(player), "Expertise after a first batch of a different arrow");
        helper.succeed();
    }
}
