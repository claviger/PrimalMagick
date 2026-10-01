package com.verdantartifice.primalmagick.test;

import com.verdantartifice.primalmagick.common.items.wands.IHasWandComponents;
import com.verdantartifice.primalmagick.common.util.ResourceUtils;
import com.verdantartifice.primalmagick.common.wands.WandCap;
import com.verdantartifice.primalmagick.common.wands.WandCore;
import com.verdantartifice.primalmagick.common.wands.WandGem;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

public class TestUtils {
    public static final Identifier DEFAULT_TEMPLATE = ResourceUtils.loc("test/empty3x3x3");

    /**
     * Creates a fresh stack of the given modular caster item (wand or staff) assembled from the given components.
     */
    public static ItemStack makeModularCaster(Item item, WandCore core, WandCap cap, WandGem gem) {
        return IHasWandComponents.setWandComponents(item.getDefaultInstance(), core, cap, gem);
    }

    public static void placeBed(GameTestHelper helper, BlockPos bedPos) {
        helper.setBlock(bedPos, Blocks.BLUE_BED);
        BlockState footState = helper.getBlockState(bedPos);
        BlockPos headPos = bedPos.relative(footState.getValue(BedBlock.FACING));
        helper.setBlock(headPos, footState.setValue(BedBlock.PART, BedPart.HEAD));
    }
}
