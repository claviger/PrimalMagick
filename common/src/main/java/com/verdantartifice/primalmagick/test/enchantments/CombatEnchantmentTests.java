package com.verdantartifice.primalmagick.test.enchantments;

import com.verdantartifice.primalmagick.common.effects.EffectsPM;
import com.verdantartifice.primalmagick.common.enchantments.EnchantmentsPM;
import com.verdantartifice.primalmagick.common.events.CombatEvents;
import com.verdantartifice.primalmagick.common.events.EntityEvents;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Tests for the Soulpiercing, Rending, and Bulwark enchantments.
 */
public class CombatEnchantmentTests extends AbstractBaseTest {
    private static ItemStack enchanted(GameTestHelper helper, ItemStack stack, ResourceKey<Enchantment> key, int level) {
        stack.enchant(helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key), level);
        return stack;
    }

    private static int countSlivers(GameTestHelper helper, net.minecraft.world.entity.Entity near) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, near.getBoundingBox().inflate(3D), e -> e.getItem().is(ItemsPM.SOUL_GEM_SLIVER.get()))
                .stream().mapToInt(e -> e.getItem().getCount()).sum();
    }

    /**
     * An arrow shot by someone holding a Soulpiercing bow makes the target drop one soul gem sliver per enchantment
     * level the first time, and nothing on later hits because the target is then soulpierced.
     */
    public static void soulpiercing_drops_slivers_only_once(GameTestHelper helper, int level) {
        var player = makeMockServerPlayer(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, enchanted(helper, new ItemStack(Items.BOW), EnchantmentsPM.SOULPIERCING, level));
        var target = helper.spawnWithNoFreeWill(EntityType.COW, BlockPos.ZERO.above());
        var arrow = new Arrow(EntityType.ARROW, helper.getLevel());
        arrow.setOwner(player);

        CombatEvents.onArrowImpact(arrow, new EntityHitResult(target));
        assertValueEqual(helper, level, countSlivers(helper, target), "Soul gem slivers after the first hit");
        assertTrue(helper, target.hasEffect(EffectsPM.SOULPIERCED.getHolder()), "Target is not soulpierced after the first hit");

        CombatEvents.onArrowImpact(arrow, new EntityHitResult(target));
        assertValueEqual(helper, level, countSlivers(helper, target), "Soul gem slivers after the second hit");
        helper.succeed();
    }

    /**
     * A level 2 Rending weapon bleeds its target on a hit, and a second hit raises the amplifier by one. The target is
     * an iron golem so it survives, and its hurt timer is cleared between hits so the second one lands.
     */
    public static void rending_hits_stack_bleeding(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        player.setGameMode(GameType.SURVIVAL);
        player.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());
        player.setItemInHand(InteractionHand.MAIN_HAND, enchanted(helper, new ItemStack(Items.IRON_SWORD), EnchantmentsPM.RENDING, 2));
        var target = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, BlockPos.ZERO.above());
        var bleeding = EffectsPM.BLEEDING.getHolder();
        assertFalse(helper, target.hasEffect(bleeding), "Target was bleeding before being hit");

        player.attack(target);
        assertTrue(helper, target.hasEffect(bleeding), "Target is not bleeding after the first hit");
        assertValueEqual(helper, 0, target.getEffect(bleeding).getAmplifier(), "Bleeding amplifier after the first hit");

        target.invulnerableTime = 0;
        player.attack(target);
        assertValueEqual(helper, 1, target.getEffect(bleeding).getAmplifier(), "Bleeding amplifier after the second hit");
        helper.succeed();
    }

    private static int shieldDuration(ItemStack shield, net.minecraft.world.entity.LivingEntity user) {
        return shield.getUseDuration(user);
    }

    /**
     * While a Bulwark level 2 shield is raised, every fifth tick of use gives Resistance, and each application raises
     * the amplifier by one up to the level minus one.
     */
    public static void bulwark_stacks_resistance_while_blocking(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        var shield = enchanted(helper, new ItemStack(Items.SHIELD), EnchantmentsPM.BULWARK, 2);
        int max = shieldDuration(shield, player);

        // Nothing happens before the first full five ticks
        EntityEvents.onLivingEntityUseItemTick(player, shield, max - 3);
        assertFalse(helper, player.hasEffect(MobEffects.RESISTANCE), "Resistance granted before five ticks of blocking");

        EntityEvents.onLivingEntityUseItemTick(player, shield, max - 5);
        assertValueEqual(helper, 0, player.getEffect(MobEffects.RESISTANCE).getAmplifier(), "Resistance amplifier after 5 ticks");
        EntityEvents.onLivingEntityUseItemTick(player, shield, max - 10);
        assertValueEqual(helper, 1, player.getEffect(MobEffects.RESISTANCE).getAmplifier(), "Resistance amplifier after 10 ticks");
        EntityEvents.onLivingEntityUseItemTick(player, shield, max - 15);
        assertValueEqual(helper, 1, player.getEffect(MobEffects.RESISTANCE).getAmplifier(), "Resistance amplifier after 15 ticks, capped by the level");
        helper.succeed();
    }

    /**
     * Bulwark gives Resistance for only 10 ticks at a time, so once the shield stops being used and refreshing it,
     * the stacks lapse on their own.
     */
    public static void bulwark_resistance_lapses_after_blocking_stops(GameTestHelper helper) {
        var wearer = helper.spawnWithNoFreeWill(EntityType.PIG, BlockPos.ZERO.above());
        var shield = enchanted(helper, new ItemStack(Items.SHIELD), EnchantmentsPM.BULWARK, 2);
        int max = shieldDuration(shield, wearer);

        EntityEvents.onLivingEntityUseItemTick(wearer, shield, max - 5);
        assertTrue(helper, wearer.hasEffect(MobEffects.RESISTANCE), "Resistance not granted while blocking");
        assertValueEqual(helper, 10, wearer.getEffect(MobEffects.RESISTANCE).getDuration(), "Resistance duration while blocking");

        helper.succeedWhen(() -> assertFalse(helper, wearer.hasEffect(MobEffects.RESISTANCE), "Resistance still present after blocking stopped"));
    }
}
