package com.verdantartifice.primalmagick.test.blocks;

import com.verdantartifice.primalmagick.common.blockstates.properties.SaltSide;
import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.blocks.rituals.EntropySinkBlock;
import com.verdantartifice.primalmagick.common.blocks.rituals.SaltTrailBlock;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.research.ResearchEntries;
import com.verdantartifice.primalmagick.common.research.ResearchManager;
import com.verdantartifice.primalmagick.common.rituals.IRitualPropBlock;
import com.verdantartifice.primalmagick.common.rituals.ISaltPowered;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.common.tiles.rituals.OfferingPedestalTileEntity;
import com.verdantartifice.primalmagick.common.tiles.rituals.RitualAltarTileEntity;
import com.verdantartifice.primalmagick.common.wands.IWand;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Tests for the entropy sink as a universal ritual prop. Each test runs a manafruit ritual (three offerings and a
 * candle prop) on an altar with a sink beside it, ticking the altar directly until the ritual asks for the sink. The
 * altar's stability is pinned high while the ritual runs so that random mishaps cannot disturb it. Stability bonuses
 * are hard-coded from the essence types' affinities: 5 for dust, 20 for shards, 50 for crystals and 100 for clusters.
 */
public class EntropySinkTests extends AbstractBaseTest {
    private static final int MAX_ALTAR_TICKS = 5000;

    private static final BlockPos ALTAR = new BlockPos(2, 1, 2);
    private static final BlockPos SINK = new BlockPos(3, 1, 3);
    private static final BlockPos CANDLE = new BlockPos(2, 1, 1);
    private static final BlockPos TRAIL = new BlockPos(3, 1, 2);

    private record Scene(ServerPlayer player, RitualAltarTileEntity altar, BlockPos altarPos, BlockPos sinkPos, BlockPos candlePos) {}

    private static BlockHitResult hitTop(BlockPos absPos) {
        return new BlockHitResult(absPos.getCenter(), Direction.UP, absPos, false);
    }

    private static void placePedestal(GameTestHelper helper, BlockPos pos, ItemStack offering) {
        helper.setBlock(pos, BlocksPM.OFFERING_PEDESTAL.get());
        helper.getBlockEntity(pos, OfferingPedestalTileEntity.class).addItem(offering);
    }

    private static boolean isSaltPowered(GameTestHelper helper, BlockPos pos) {
        return helper.getBlockState(pos).getBlock() instanceof ISaltPowered powered && powered.isBlockSaltPowered(helper.getLevel(), helper.absolutePos(pos));
    }

    private static boolean isOpen(GameTestHelper helper, BlockPos absPos) {
        var state = helper.getLevel().getBlockState(absPos);
        return state.getBlock() instanceof IRitualPropBlock prop && prop.isPropOpen(state, helper.getLevel(), absPos);
    }

    /**
     * Builds the altar, pedestals, candle, salt and sink, and starts the ritual.
     */
    private static Scene startRitual(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        player.setGameMode(GameType.SURVIVAL);
        ResearchManager.forceGrantWithAllParents(player, ResearchEntries.MANAFRUIT);

        // A stone floor to hold the salt, then the altar with its props and pedestals around it
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(ALTAR, BlocksPM.RITUAL_ALTAR.get());
        placePedestal(helper, new BlockPos(1, 1, 2), new ItemStack(Items.APPLE));
        placePedestal(helper, new BlockPos(2, 1, 3), new ItemStack(Items.HONEY_BOTTLE));
        placePedestal(helper, new BlockPos(3, 1, 1), new ItemStack(ItemsPM.MANA_SALTS.get()));
        helper.setBlock(CANDLE, BlocksPM.RITUAL_CANDLE_WHITE.get());
        helper.setBlock(SINK, BlocksPM.ENTROPY_SINK.get());
        helper.setBlock(TRAIL, BlocksPM.SALT_TRAIL.get().defaultBlockState()
                .setValue(SaltTrailBlock.NORTH, SaltSide.SIDE).setValue(SaltTrailBlock.SOUTH, SaltSide.SIDE).setValue(SaltTrailBlock.WEST, SaltSide.SIDE));

        // A full wand to pay the ritual's mana cost
        ItemStack wandStack = ItemsPM.MUNDANE_WAND.get().getDefaultInstance();
        IWand wand = (IWand)wandStack.getItem();
        Sources.getAll().forEach(s -> wand.addMana(wandStack, s, wand.getMaxMana(wandStack, s)));

        // Ritual mana is drawn from the wand in the player's main hand
        player.setItemInHand(InteractionHand.MAIN_HAND, wandStack);
        var altar = helper.getBlockEntity(ALTAR, RitualAltarTileEntity.class);
        var altarPos = helper.absolutePos(ALTAR);
        altar.onWandRightClick(wandStack, helper.getLevel(), player, altarPos, Direction.UP);
        assertTrue(helper, altar.isActive(), "Ritual did not start; salt powered: pedestals "
                + isSaltPowered(helper, new BlockPos(1, 1, 2)) + "/" + isSaltPowered(helper, new BlockPos(2, 1, 3)) + "/" + isSaltPowered(helper, new BlockPos(3, 1, 1))
                + ", candle " + isSaltPowered(helper, CANDLE) + ", sink " + isSaltPowered(helper, SINK) + ", trail " + helper.getBlockState(TRAIL));
        return new Scene(player, altar, altarPos, helper.absolutePos(SINK), helper.absolutePos(CANDLE));
    }

    /**
     * Ticks the ritual, lighting the candle when asked, until the sink is asked for.
     */
    private static void runUntilSinkOpen(GameTestHelper helper, Scene scene) {
        var level = helper.getLevel();
        for (int tick = 0; tick < MAX_ALTAR_TICKS && !isOpen(helper, scene.sinkPos()); tick++) {
            RitualAltarTileEntity.tick(level, scene.altarPos(), level.getBlockState(scene.altarPos()), scene.altar());
            scene.altar().setStability(25.0F);
            if (isOpen(helper, scene.candlePos())) {
                var flint = new ItemStack(Items.FLINT_AND_STEEL);
                scene.player().setItemInHand(InteractionHand.MAIN_HAND, flint);
                scene.player().gameMode.useItemOn(scene.player(), level, flint, InteractionHand.MAIN_HAND, hitTop(scene.candlePos()));
            }
        }
        assertTrue(helper, isOpen(helper, scene.sinkPos()), "Entropy sink was never asked for by the ritual");
    }

    public static void entropy_sink_is_selected_as_ritual_prop(GameTestHelper helper) {
        var scene = startRitual(helper);
        runUntilSinkOpen(helper, scene);
        helper.succeed();
    }

    public static void entropy_sink_accepts_essence(GameTestHelper helper, Item essence) {
        var scene = startRitual(helper);
        runUntilSinkOpen(helper, scene);
        var level = helper.getLevel();
        var stack = new ItemStack(essence, 2);
        scene.player().setItemInHand(InteractionHand.MAIN_HAND, stack);

        var result = scene.player().gameMode.useItemOn(scene.player(), level, stack, InteractionHand.MAIN_HAND, hitTop(scene.sinkPos()));

        assertTrue(helper, result.consumesAction(), "Essence use on the sink was not consumed: " + result);
        assertValueEqual(helper, 1, scene.player().getItemInHand(InteractionHand.MAIN_HAND).getCount(), "Essence remaining after use");
        assertValueEqual(helper, true, level.getBlockState(scene.sinkPos()).getValue(EntropySinkBlock.LIT), "Sink lit state after use");
        assertFalse(helper, isOpen(helper, scene.sinkPos()), "Sink still waiting for essence after use");
        helper.succeed();
    }

    public static void entropy_sink_grants_stability_by_essence_grade(GameTestHelper helper, Item essence, int expectedBonus) {
        var scene = startRitual(helper);
        runUntilSinkOpen(helper, scene);
        var stack = new ItemStack(essence);
        scene.player().setItemInHand(InteractionHand.MAIN_HAND, stack);

        // Start from the minimum stability so that no grade's bonus is cut off by the maximum
        scene.altar().setStability(-100.0F);
        assertValueEqual(helper, -100.0F, scene.altar().getStability(), "Stability before the sink is used");
        scene.player().gameMode.useItemOn(scene.player(), helper.getLevel(), stack, InteractionHand.MAIN_HAND, hitTop(scene.sinkPos()));

        assertValueEqual(helper, -100.0F + expectedBonus, scene.altar().getStability(), "Stability after the sink is used");
        helper.succeed();
    }
}
