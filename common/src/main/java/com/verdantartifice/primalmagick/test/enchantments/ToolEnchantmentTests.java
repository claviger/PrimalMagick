package com.verdantartifice.primalmagick.test.enchantments;

import com.verdantartifice.primalmagick.common.enchantments.EnchantmentsPM;
import com.verdantartifice.primalmagick.common.events.PlayerEvents;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Tests for the area and growth tool enchantments: Disintegration, Verdant, and Reverberation. Uses the floor template.
 */
public class ToolEnchantmentTests extends AbstractBaseTest {
    private static ItemStack enchanted(GameTestHelper helper, ItemStack stack, ResourceKey<Enchantment> key, int level) {
        stack.enchant(helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key), level);
        return stack;
    }

    private static ServerPlayer makeSurvivalPlayer(GameTestHelper helper, ItemStack tool) {
        var player = makeMockServerPlayer(helper);
        player.setGameMode(GameType.SURVIVAL);
        player.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());
        player.setItemInHand(InteractionHand.MAIN_HAND, tool);
        return player;
    }

    /**
     * Breaking a log with a level 1 Disintegration axe also breaks the connected logs of the same type, up to 9 more.
     * A different kind of log touching the chain is left alone.
     */
    public static void disintegration_breaks_connected_blocks_of_same_type(GameTestHelper helper) {
        var player = makeSurvivalPlayer(helper, enchanted(helper, new ItemStack(Items.IRON_AXE), EnchantmentsPM.DISINTEGRATION, 1));
        for (int x = 0; x <= 3; x++) {
            helper.setBlock(new BlockPos(x, 1, 2), Blocks.OAK_LOG);
        }
        helper.setBlock(new BlockPos(2, 1, 3), Blocks.BIRCH_LOG);
        helper.setBlock(new BlockPos(4, 1, 2), Blocks.STONE);

        assertTrue(helper, player.gameMode.destroyBlock(helper.absolutePos(new BlockPos(0, 1, 2))), "Initial log was not broken");

        helper.succeedWhen(() -> {
            for (int x = 0; x <= 3; x++) {
                helper.assertBlockPresent(Blocks.AIR, new BlockPos(x, 1, 2));
            }
            helper.assertBlockPresent(Blocks.BIRCH_LOG, new BlockPos(2, 1, 3));
            helper.assertBlockPresent(Blocks.STONE, new BlockPos(4, 1, 2));
        });
    }

    /**
     * A level 1 Verdant hoe used on a young wheat crop grows it like bone meal would, which adds 2 to 5 growth stages,
     * and costs durability. The enchantment's own cost at level 1 is 8, and the hoe's tilling step adds one more.
     */
    public static void verdant_hoe_grows_crop_and_costs_durability(GameTestHelper helper) {
        var hoe = enchanted(helper, new ItemStack(Items.IRON_HOE), EnchantmentsPM.VERDANT, 1);
        var player = makeSurvivalPlayer(helper, hoe);
        var farmlandPos = new BlockPos(2, 0, 2);
        var cropPos = farmlandPos.above();
        helper.setBlock(farmlandPos, Blocks.FARMLAND);
        helper.setBlock(cropPos, Blocks.WHEAT);
        var abs = helper.absolutePos(cropPos);

        hoe.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(abs.getCenter(), Direction.UP, abs, false)));

        int age = helper.getBlockState(cropPos).getValue(CropBlock.AGE);
        assertTrue(helper, age >= 2, "Wheat did not grow by at least two stages; age " + age);
        assertTrue(helper, hoe.getDamageValue() >= 8, "Hoe damage after one Verdant use is below the enchantment cost of 8: " + hoe.getDamageValue());
        helper.succeed();
    }

    /**
     * Breaking a block in the middle of a flat 3x3 patch of dirt, struck from the top, with a level 1 Reverberation
     * shovel breaks the eight surrounding blocks too.
     */
    public static void reverberation_breaks_three_by_three_area(GameTestHelper helper) {
        var player = makeSurvivalPlayer(helper, enchanted(helper, new ItemStack(Items.IRON_SHOVEL), EnchantmentsPM.REVERBERATION, 1));
        for (int x = 1; x <= 3; x++) {
            for (int z = 1; z <= 3; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.DIRT);
            }
        }
        var center = new BlockPos(2, 1, 2);
        var absCenter = helper.absolutePos(center);

        // The area is oriented by the face of the last left-click, so record a click on the top face
        PlayerEvents.onPlayerInteractLeftClickBlock(player, InteractionHand.MAIN_HAND, absCenter, Direction.UP);
        assertTrue(helper, player.gameMode.destroyBlock(absCenter), "Center block was not broken");

        helper.succeedWhen(() -> {
            for (int x = 1; x <= 3; x++) {
                for (int z = 1; z <= 3; z++) {
                    helper.assertBlockPresent(Blocks.AIR, new BlockPos(x, 1, z));
                }
            }
        });
    }
}
