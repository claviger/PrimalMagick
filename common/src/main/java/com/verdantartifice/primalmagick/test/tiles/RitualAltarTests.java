package com.verdantartifice.primalmagick.test.tiles;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.tiles.rituals.RitualAltarTileEntity;
import com.verdantartifice.primalmagick.common.util.ResourceUtils;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.storage.TagValueInput;

import java.util.Optional;

/**
 * Tests for the ritual altar's block entity data and output slot handling.
 */
public class RitualAltarTests extends AbstractBaseTest {
    /**
     * Confirms that the altar's active recipe ID is saved as the plain recipe identifier string, which is also what the
     * client decodes from the block entity update packet, and that it loads back to the same key. Also confirms that a
     * value saved in the ResourceKey#toString() form written by earlier builds of the 26.1 port is still recovered.
     */
    public static void ritual_altar_active_recipe_round_trips(GameTestHelper helper) {
        var pos = BlockPos.ZERO;
        helper.setBlock(pos, BlocksPM.RITUAL_ALTAR.get());
        helper.assertBlockState(pos, state -> state.is(BlocksPM.RITUAL_ALTAR.get()), state -> Component.literal("Ritual altar not placed correctly"));
        var tile = helper.getBlockEntity(pos, RitualAltarTileEntity.class);
        var registries = helper.getLevel().registryAccess();

        ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(Registries.RECIPE, ResourceUtils.loc("manafruit"));
        tile.setActiveRecipeId(recipeKey);

        // Save the altar and confirm the stored value is the plain identifier string
        CompoundTag tag = tile.saveWithoutMetadata(registries);
        Optional<String> saved = tag.getString("ActiveRecipeId");
        assertValueEqual(helper, Optional.of("primalmagick:manafruit"), saved, "Saved active recipe ID");
        assertFalse(helper, saved.get().contains("ResourceKey["), "Saved active recipe ID is a ResourceKey string: " + saved.get());

        // Load the saved data back into the altar and confirm the key round-trips without decode problems
        tile.setActiveRecipeId(null);
        var reporter = new ProblemReporter.Collector();
        tile.loadWithComponents(TagValueInput.create(reporter, registries, tag));
        assertValueEqual(helper, recipeKey, tile.getActiveRecipeId(), "Loaded active recipe ID");
        assertTrue(helper, reporter.isEmpty(), "Problems reported while reloading the saved altar data: " + reporter.getReport());

        // Data saved by earlier port builds stored ResourceKey#toString(); confirm the identifier is recovered from it
        tag.putString("ActiveRecipeId", "ResourceKey[minecraft:recipe / primalmagick:manafruit]");
        tile.setActiveRecipeId(null);
        tile.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag));
        assertValueEqual(helper, recipeKey, tile.getActiveRecipeId(), "Active recipe ID loaded from legacy format");

        // An unparseable value loads as no active recipe
        tag.putString("ActiveRecipeId", "not a key!");
        tile.setActiveRecipeId(recipeKey);
        tile.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag));
        assertTrue(helper, tile.getActiveRecipeId() == null, "Active recipe ID loaded from an unparseable value was not null: " + tile.getActiveRecipeId());

        // A missing value loads as no active recipe
        tag.remove("ActiveRecipeId");
        tile.setActiveRecipeId(recipeKey);
        tile.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag));
        assertTrue(helper, tile.getActiveRecipeId() == null, "Active recipe ID loaded from data without one was not null: " + tile.getActiveRecipeId());

        helper.succeed();
    }

    /**
     * The altar's only inventory, index 0, holds the ritual output in its single slot.
     */
    private static final int OUTPUT_INV_INDEX = 0;

    private static RitualAltarTileEntity placeAltar(GameTestHelper helper) {
        var pos = BlockPos.ZERO;
        helper.setBlock(pos, BlocksPM.RITUAL_ALTAR.get());
        helper.assertBlockState(pos, state -> state.is(BlocksPM.RITUAL_ALTAR.get()), state -> Component.literal("Ritual altar not placed correctly"));
        return helper.getBlockEntity(pos, RitualAltarTileEntity.class);
    }

    /**
     * Confirms that replacing the contents of an empty output slot places the new stack and reports an empty old stack,
     * rather than trying to extract an empty resource from the slot.
     */
    public static void ritual_altar_replace_item_on_empty_slot(GameTestHelper helper) {
        var tile = placeAltar(helper);
        assertTrue(helper, tile.getItem().isEmpty(), "Altar output slot not empty after placement: " + tile.getItem());

        ItemStack oldStack = tile.replaceItem(OUTPUT_INV_INDEX, 0, new ItemStack(Items.APPLE, 3));

        assertTrue(helper, oldStack.isEmpty(), "Replaced stack from an empty slot was not empty: " + oldStack);
        assertValueEqual(helper, Items.APPLE, tile.getItem().getItem(), "Output slot item after replace");
        assertValueEqual(helper, 3, tile.getItem().getCount(), "Output slot count after replace");
        helper.succeed();
    }

    /**
     * Confirms that replacing the contents of an occupied output slot with an empty stack empties the slot and returns
     * the stack that was there, rather than trying to insert an empty resource.
     */
    public static void ritual_altar_replace_item_with_empty_stack(GameTestHelper helper) {
        var tile = placeAltar(helper);
        ItemStack leftover = tile.addItem(OUTPUT_INV_INDEX, 0, new ItemStack(Items.APPLE, 2));
        assertTrue(helper, leftover.isEmpty(), "Apples were not all added to the output slot: " + leftover);
        assertValueEqual(helper, Items.APPLE, tile.getItem().getItem(), "Output slot item before replace");
        assertValueEqual(helper, 2, tile.getItem().getCount(), "Output slot count before replace");

        ItemStack oldStack = tile.replaceItem(OUTPUT_INV_INDEX, 0, ItemStack.EMPTY);

        assertValueEqual(helper, Items.APPLE, oldStack.getItem(), "Replaced stack item");
        assertValueEqual(helper, 2, oldStack.getCount(), "Replaced stack count");
        assertTrue(helper, tile.getItem().isEmpty(), "Output slot not empty after replacing with an empty stack: " + tile.getItem());
        helper.succeed();
    }

    /**
     * Confirms that finishing a manafruit ritual on an altar with an empty output slot places the recipe result, a
     * single manafruit, in the slot and resets the altar, as the 1.21 altar did with setItem.
     */
    public static void ritual_altar_finish_craft_with_empty_slot_does_not_throw(GameTestHelper helper) {
        var tile = placeAltar(helper);
        assertTrue(helper, tile.getItem().isEmpty(), "Altar output slot not empty after placement: " + tile.getItem());
        tile.setActiveRecipeId(ResourceKey.create(Registries.RECIPE, ResourceUtils.loc("manafruit")));

        tile.finishCraftForTesting();

        // The manafruit ritual recipe's result is one manafruit
        assertValueEqual(helper, ItemsPM.MANAFRUIT.get(), tile.getItem().getItem(), "Output slot item after finishing the ritual");
        assertValueEqual(helper, 1, tile.getItem().getCount(), "Output slot count after finishing the ritual");
        assertTrue(helper, tile.getActiveRecipeId() == null, "Active recipe ID not cleared after finishing the ritual: " + tile.getActiveRecipeId());
        helper.succeed();
    }
}
