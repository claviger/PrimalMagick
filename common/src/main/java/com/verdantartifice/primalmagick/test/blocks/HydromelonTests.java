package com.verdantartifice.primalmagick.test.blocks;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * Tests for hydromelon planting, growth, and stripping. Uses the floor template and the daytime environment, since
 * stems need light to grow. Farmland replaces the floor at 2,0,2.
 */
public class HydromelonTests extends AbstractBaseTest {
    private static final BlockPos FARMLAND_POS = new BlockPos(2, 0, 2);
    private static final BlockPos STEM_POS = FARMLAND_POS.above();

    private static void placeFarmland(GameTestHelper helper) {
        helper.setBlock(FARMLAND_POS, Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE, 7));
    }

    /** Hydromelon seeds placed on the top of farmland become a hydromelon stem. */
    public static void hydromelon_seeds_plant_on_farmland(GameTestHelper helper) {
        placeFarmland(helper);
        var player = makeMockServerPlayer(helper);
        var seeds = new ItemStack(ItemsPM.HYDROMELON_SEEDS.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, seeds);
        var abs = helper.absolutePos(FARMLAND_POS);

        var result = player.gameMode.useItemOn(player, helper.getLevel(), seeds, InteractionHand.MAIN_HAND, new BlockHitResult(abs.getCenter(), Direction.UP, abs, false));

        assertTrue(helper, result.consumesAction(), "Seed use on farmland was not consumed: " + result);
        helper.assertBlockPresent(BlocksPM.HYDROMELON_STEM.get(), STEM_POS);
        helper.succeed();
    }

    /**
     * A fully grown stem eventually fruits. The four ground blocks beside the stem are made dirt so that any direction
     * the stem picks is a valid place for the fruit, and a light block ensures the stem has the light it needs.
     */
    public static void hydromelon_stem_grows_hydromelon(GameTestHelper helper) {
        placeFarmland(helper);
        List<BlockPos> neighbors = List.of(STEM_POS.east(), STEM_POS.west(), STEM_POS.south(), STEM_POS.north());
        neighbors.forEach(p -> helper.setBlock(p.below(), Blocks.DIRT));
        helper.setBlock(STEM_POS.above(2), Blocks.LIGHT);
        helper.setBlock(STEM_POS, BlocksPM.HYDROMELON_STEM.get().defaultBlockState().setValue(StemBlock.AGE, 7));

        helper.succeedWhen(() -> {
            for (int i = 0; i < 300; i++) {
                helper.randomTick(STEM_POS);
            }
            assertTrue(helper, neighbors.stream().anyMatch(p -> helper.getBlockState(p).is(BlocksPM.HYDROMELON.get())), "No hydromelon grew next to the stem");
        });
    }

    /** Using an axe on a hydromelon replaces it with a water source block. */
    public static void axe_turns_hydromelon_into_water(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        helper.setBlock(pos, BlocksPM.HYDROMELON.get());
        var player = makeMockServerPlayer(helper);
        var axe = new ItemStack(Items.IRON_AXE);
        player.setItemInHand(InteractionHand.MAIN_HAND, axe);
        var abs = helper.absolutePos(pos);

        var result = player.gameMode.useItemOn(player, helper.getLevel(), axe, InteractionHand.MAIN_HAND, new BlockHitResult(abs.getCenter(), Direction.UP, abs, false));

        assertTrue(helper, result.consumesAction(), "Axe use on a hydromelon was not consumed: " + result);
        helper.assertBlockPresent(Blocks.WATER, pos);
        assertTrue(helper, helper.getLevel().getFluidState(abs).isSource(), "Water left by the hydromelon is not a source block");
        helper.succeed();
    }
}
