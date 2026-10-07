package com.verdantartifice.primalmagick.test.entities;

import com.verdantartifice.primalmagick.common.entities.companions.CompanionManager;
import com.verdantartifice.primalmagick.common.entities.pixies.companions.AbstractPixieEntity;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Tests for the pixie companions: their drops, being plucked by their owner, and their attack effects.
 */
public class PixieTests extends AbstractBaseTest {
    private static final Vec3 PIXIE_POS = new Vec3(1.5D, 1D, 2.5D);

    /** Killing a pixie drops the drained pixie item for its rank and source, as set in its loot table. */
    public static void pixie_drops_drained_pixie_when_killed(GameTestHelper helper, EntityType<AbstractPixieEntity> type, Item drainedItem) {
        var pixie = helper.spawnWithNoFreeWill(type, PIXIE_POS);

        pixie.kill(helper.getLevel());

        helper.succeedWhen(() -> helper.assertItemEntityPresent(drainedItem, new BlockPos(1, 1, 2), 3.0D));
    }

    /** The owner takes a pixie out of the air with an empty hand, receiving its item while the entity goes away. */
    public static void owner_plucks_pixie_with_empty_hand(GameTestHelper helper, EntityType<AbstractPixieEntity> type, Item pixieItem) {
        var player = makeMockServerPlayer(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        var pixie = helper.spawnWithNoFreeWill(type, PIXIE_POS);
        CompanionManager.addCompanion(player, pixie);
        assertTrue(helper, pixie.isCompanionOwner(player), "Player is not the pixie's owner");

        var result = player.interactOn(pixie, InteractionHand.MAIN_HAND, pixie.position());

        assertTrue(helper, result.consumesAction(), "Interaction with the pixie was not consumed: " + result);
        assertTrue(helper, player.getItemInHand(InteractionHand.MAIN_HAND).is(pixieItem), "Player is not holding the pixie item: " + player.getItemInHand(InteractionHand.MAIN_HAND));
        assertTrue(helper, pixie.isRemoved(), "Pixie entity was not removed");
        helper.succeed();
    }

    /**
     * Has the pixie shoot a target two and a half blocks east of it. The pixie's ranged attack casts a bolt spell along
     * its line of sight, so it is turned to face east first. The target's health is raised so the hit cannot kill it.
     */
    private static Mob shootTarget(GameTestHelper helper, EntityType<AbstractPixieEntity> type) {
        var pixie = helper.spawnWithNoFreeWill(type, PIXIE_POS);
        var pos = pixie.position();
        pixie.snapTo(pos.x, pos.y, pos.z, -90F, 0F);
        // Bolt spells are aimed along the head's direction, which is separate from the body rotation
        pixie.setYHeadRot(-90F);
        pixie.setYBodyRot(-90F);
        var target = helper.spawnWithNoFreeWill(EntityType.COW, new Vec3(PIXIE_POS.x + 2.5D, PIXIE_POS.y, PIXIE_POS.z));
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100D);
        target.setHealth(100F);
        var shooter = assertInstanceOf(helper, pixie, RangedAttackMob.class, "Pixie has no ranged attack");
        shooter.performRangedAttack(target, 1F);
        return target;
    }

    private static String diagnostics(GameTestHelper helper, LivingEntity target) {
        var pixies = helper.getLevel().getEntitiesOfClass(AbstractPixieEntity.class, target.getBoundingBox().inflate(6D));
        var pixie = pixies.isEmpty() ? null : pixies.getFirst();
        return "target at " + target.position() + " health " + target.getHealth() + "; pixie " + (pixie == null ? "missing" : pixie.position() + " eye " + pixie.getEyePosition() + " yRot " + pixie.getYRot() + " look " + pixie.getViewVector(1F));
    }

    /** An earth pixie's attack knocks its target back, away from the pixie and so to the east. */
    public static void earth_pixie_attack_knocks_target_back(GameTestHelper helper, EntityType<AbstractPixieEntity> type) {
        var target = shootTarget(helper, type);
        assertTrue(helper, target.getHealth() < 100F, "Target was not damaged: " + diagnostics(helper, target));
        assertTrue(helper, target.getDeltaMovement().x > 0D, "Target was not knocked east; motion " + target.getDeltaMovement());
        helper.succeed();
    }

    /** A pixie's attack leaves its target with the given status effect. */
    public static void pixie_attack_inflicts_effect(GameTestHelper helper, EntityType<AbstractPixieEntity> type, Holder<MobEffect> effect) {
        LivingEntity target = shootTarget(helper, type);
        assertTrue(helper, target.getHealth() < 100F, "Target was not damaged: " + diagnostics(helper, target));
        assertTrue(helper, target.hasEffect(effect), "Target does not have the expected effect");
        helper.succeed();
    }
}
