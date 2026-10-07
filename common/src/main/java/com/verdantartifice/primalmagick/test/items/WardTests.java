package com.verdantartifice.primalmagick.test.items;

import com.verdantartifice.primalmagick.common.capabilities.ManaStorage;
import com.verdantartifice.primalmagick.common.components.DataComponentsPM;
import com.verdantartifice.primalmagick.common.events.PlayerEvents;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.platform.Services;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import com.verdantartifice.primalmagick.test.tiles.ChargeableItem;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;

import java.util.List;

/**
 * Tests for warding modules and ward: which armor a module can be applied to, ward absorbing damage before health,
 * and the pause before ward regenerates after a hit.
 */
public class WardTests extends AbstractBaseTest {
    private static CraftingInput applicationInput(Item armor) {
        return CraftingInput.of(2, 1, List.of(new ItemStack(ItemsPM.BASIC_WARDING_MODULE.get()), new ItemStack(armor)));
    }

    public static void warding_module_applies_to_primal_metal_armor(GameTestHelper helper, Item armor) {
        var input = applicationInput(armor);
        var recipe = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
        assertTrue(helper, recipe.isPresent(), "No warding recipe found for " + armor);
        var result = recipe.get().value().assemble(input);
        assertTrue(helper, result.is(armor), "Warding result is not the armor piece");
        // The basic module is the enchanted tier, which gives ward level 1
        assertValueEqual(helper, 1, result.getOrDefault(DataComponentsPM.WARD_LEVEL.get(), 0), "Ward level of the result");
        helper.succeed();
    }

    public static void warding_module_rejects_other_armor(GameTestHelper helper, Item armor) {
        var recipe = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, applicationInput(armor), helper.getLevel());
        assertFalse(helper, recipe.isPresent(), "Warding recipe found for " + armor);
        helper.succeed();
    }

    private static ServerPlayer makeWardedSurvivalPlayer(GameTestHelper helper, float currentWard, float maxWard) {
        var player = makeMockServerPlayer(helper);
        player.setGameMode(GameType.SURVIVAL);
        player.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());
        var ward = Services.CAPABILITIES.ward(player).orElseThrow();
        ward.setMaxWard(maxWard);
        ward.setCurrentWard(currentWard);
        return player;
    }

    public static void ward_absorbs_damage_before_health(GameTestHelper helper) {
        var player = makeWardedSurvivalPlayer(helper, 5.0F, 10.0F);
        var ward = Services.CAPABILITIES.ward(player).orElseThrow();
        assertValueEqual(helper, 20.0F, player.getHealth(), "Starting health");

        // Ward soaks up a hit smaller than itself entirely
        player.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 3.0F);
        assertValueEqual(helper, 2.0F, ward.getCurrentWard(), "Ward after a 3 point hit");
        assertValueEqual(helper, 20.0F, player.getHealth(), "Health after a hit absorbed by ward");

        // The excess of a bigger hit goes through to health once the ward is gone
        player.invulnerableTime = 0;
        player.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 5.0F);
        assertValueEqual(helper, 0.0F, ward.getCurrentWard(), "Ward after a 5 point hit");
        assertValueEqual(helper, 17.0F, player.getHealth(), "Health after a hit that broke the ward");
        helper.succeed();
    }

    public static void ward_regeneration_pauses_after_damage(GameTestHelper helper) {
        var player = makeWardedSurvivalPlayer(helper, 5.0F, 10.0F);
        var ward = Services.CAPABILITIES.ward(player).orElseThrow();
        var armor = ChargeableItem.WARDED_PRIMALITE_CHEST.makeStack();
        armor.update(DataComponentsPM.CAPABILITY_MANA_STORAGE.get(), ManaStorage.EMPTY, storage -> storage.copyWith(Sources.EARTH, 5000));
        player.setItemSlot(EquipmentSlot.CHEST, armor);
        assertTrue(helper, ward.isRegenerating(), "Ward not regenerating before being hit");

        player.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 1.0F);
        assertValueEqual(helper, 4.0F, ward.getCurrentWard(), "Ward after the hit");
        assertFalse(helper, ward.isRegenerating(), "Ward regenerating immediately after a hit");
        PlayerEvents.handleWardRegeneration(player);
        assertValueEqual(helper, 4.0F, ward.getCurrentWard(), "Ward after a regeneration pass during the pause");

        // The pause lasts ten seconds of real time, which the test server's fast ticking would never wait out, so age the
        // pause by saving the ward, moving its pause timestamp back eleven seconds and loading it again
        var registries = helper.getLevel().registryAccess();
        var tag = assertInstanceOf(helper, ward.serializeNBT(registries), CompoundTag.class, "Serialized ward not a compound tag");
        tag.putLong("lastPaused", tag.getLongOr("lastPaused", 0L) - 11000L);
        tag.putLong("syncTimestamp", tag.getLongOr("syncTimestamp", 0L) + 1L);
        ward.deserializeNBT(registries, tag);
        assertValueEqual(helper, 4.0F, ward.getCurrentWard(), "Ward after reloading the aged pause");
        assertTrue(helper, ward.isRegenerating(), "Ward not regenerating after the pause elapsed");
        PlayerEvents.handleWardRegeneration(player);
        assertValueEqual(helper, 5.0F, ward.getCurrentWard(), "Ward after a regeneration pass once resumed");
        helper.succeed();
    }
}
