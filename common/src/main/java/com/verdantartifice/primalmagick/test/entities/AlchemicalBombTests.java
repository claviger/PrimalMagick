package com.verdantartifice.primalmagick.test.entities;

import com.verdantartifice.primalmagick.common.concoctions.ConcoctionUtils;
import com.verdantartifice.primalmagick.common.concoctions.FuseType;
import com.verdantartifice.primalmagick.common.entities.EntityTypesPM;
import com.verdantartifice.primalmagick.common.entities.projectiles.AlchemicalBombEntity;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for alchemical bombs: tooltips, fuse cycling, throwing, and the detonation behaviour of the thrown bomb
 * entity. Bombs carry a swiftness potion, which gives speed level one (amplifier 0) for 3600 ticks (Potions.SWIFTNESS),
 * so the duration a mob receives shows how strongly the bomb's effect reached it.
 * <p>
 * Fuse lengths come from FuseType: short 20 ticks, medium 60, long 100, and impact has no timer. A bomb in range of
 * a detonation gets its potion duration scaled by 1 - distance / 4, except for a mob the bomb struck directly, which
 * gets the full duration, and mobs 4 or more blocks away get nothing (AlchemicalBombEntity.applyPotionEffects).
 */
public class AlchemicalBombTests extends AbstractBaseTest {
    private static final int FULL_DURATION = 3600;

    // Bombs start with six charges (ConcoctionType.BOMB)
    private static final int START_CHARGES = 6;

    private static ItemStack makeBomb(FuseType fuse) {
        return ConcoctionUtils.newBomb(Potions.SWIFTNESS, fuse).create();
    }

    private static Player makeSurvivalPlayer(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        assertFalse(helper, player.getAbilities().instabuild, "Survival test player has infinite materials");
        return player;
    }

    /**
     * Adds a bomb entity to the level at the given test-relative position.
     */
    private static AlchemicalBombEntity spawnBomb(GameTestHelper helper, FuseType fuse, Vec3 relPos, Vec3 velocity, boolean noGravity) {
        var bomb = new AlchemicalBombEntity(EntityTypesPM.ALCHEMICAL_BOMB.get(), helper.getLevel());
        bomb.setItem(makeBomb(fuse));
        bomb.setPos(helper.absoluteVec(relPos));
        bomb.setDeltaMovement(velocity);
        bomb.setNoGravity(noGravity);
        helper.getLevel().addFreshEntity(bomb);
        return bomb;
    }

    /**
     * Spawns a pig that stays where it is, so that its distance from a detonation is fixed.
     */
    private static Mob spawnStillPig(GameTestHelper helper, Vec3 relPos) {
        Mob pig = helper.spawnWithNoFreeWill(EntityType.PIG, relPos);
        pig.setNoGravity(true);
        pig.setInvulnerable(true);
        return pig;
    }

    private static MobEffectInstance getSpeed(Mob pig) {
        return pig.getEffect(MobEffects.SPEED);
    }

    /**
     * Confirms that a bomb entity is in the level next to the player. The mock player starts far from the test structure,
     * so the tests move it inside first and the helper's bounds-limited entity checks aren't used.
     */
    private static void assertThrownBombPresent(GameTestHelper helper, Player player) {
        var bombs = helper.getLevel().getEntitiesOfClass(AlchemicalBombEntity.class, player.getBoundingBox().inflate(5.0D));
        assertValueEqual(helper, 1, bombs.size(), "Number of bomb entities near the player");
    }

    // Tooltip tests

    public static void bomb_tooltip_shows_charges_and_fuse(GameTestHelper helper, FuseType fuse, int charges, String expectedFuseKey) {
        ItemStack stack = ConcoctionUtils.setCurrentDoses(makeBomb(fuse), charges);
        assertValueEqual(helper, charges, ConcoctionUtils.getCurrentDoses(stack), "Charges of test bomb");

        List<Component> lines = new ArrayList<>();
        stack.getItem().appendHoverText(stack, Item.TooltipContext.EMPTY, TooltipDisplay.DEFAULT, lines::add, TooltipFlag.NORMAL);
        assertValueEqual(helper, 2, lines.size(), "Tooltip line count");

        var chargesLine = assertInstanceOf(helper, lines.get(0).getContents(), TranslatableContents.class, "Charges line is not translatable");
        assertValueEqual(helper, "concoctions.primalmagick.charges_remaining", chargesLine.getKey(), "Charges line translation key");
        assertValueEqual(helper, 1, chargesLine.getArgs().length, "Charges line argument count");
        assertValueEqual(helper, (Object)charges, chargesLine.getArgs()[0], "Charges line argument");

        var fuseLine = assertInstanceOf(helper, lines.get(1).getContents(), TranslatableContents.class, "Fuse line is not translatable");
        assertValueEqual(helper, "concoctions.primalmagick.fuse_length", fuseLine.getKey(), "Fuse line translation key");
        assertValueEqual(helper, 1, fuseLine.getArgs().length, "Fuse line argument count");
        var fuseArg = assertInstanceOf(helper, fuseLine.getArgs()[0], Component.class, "Fuse line argument is not a component");
        var fuseArgContents = assertInstanceOf(helper, fuseArg.getContents(), TranslatableContents.class, "Fuse name is not translatable");
        assertValueEqual(helper, expectedFuseKey, fuseArgContents.getKey(), "Fuse name translation key");
        helper.succeed();
    }

    // Fuse setting tests

    public static void bomb_fuse_advances_when_used_while_sneaking(GameTestHelper helper, FuseType start, FuseType expectedNext) {
        Player player = makeSurvivalPlayer(helper);
        player.setShiftKeyDown(true);
        ItemStack stack = makeBomb(start);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);

        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        assertValueEqual(helper, expectedNext, ConcoctionUtils.getFuseType(held), "Fuse after sneak-use from " + start.getSerializedName());
        assertValueEqual(helper, START_CHARGES, ConcoctionUtils.getCurrentDoses(held), "Charges after setting the fuse");
        helper.assertEntityNotPresent(EntityTypesPM.ALCHEMICAL_BOMB.get());
        helper.succeed();
    }

    // Throwing tests

    public static void bomb_charges_decrease_when_thrown(GameTestHelper helper) {
        Player player = makeSurvivalPlayer(helper);
        player.setPos(helper.absoluteVec(new Vec3(1.5, 1.0, 1.5)));
        ItemStack stack = makeBomb(FuseType.MEDIUM);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);

        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        assertFalse(helper, held.isEmpty(), "Bomb used up after one throw");
        assertValueEqual(helper, START_CHARGES - 1, ConcoctionUtils.getCurrentDoses(held), "Charges after one throw");
        assertThrownBombPresent(helper, player);
        helper.succeed();
    }

    public static void bomb_is_used_up_when_last_charge_thrown(GameTestHelper helper) {
        Player player = makeSurvivalPlayer(helper);
        player.setPos(helper.absoluteVec(new Vec3(1.5, 1.0, 1.5)));
        ItemStack stack = ConcoctionUtils.setCurrentDoses(makeBomb(FuseType.MEDIUM), 1);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);

        assertTrue(helper, player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty(), "Bomb not used up after its last charge was thrown");
        assertThrownBombPresent(helper, player);
        helper.succeed();
    }

    // Detonation tests

    public static void impact_bomb_explodes_on_hitting_a_block(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.STONE);
        Mob pig = spawnStillPig(helper, new Vec3(2.5, 1.0, 1.5));
        var bomb = spawnBomb(helper, FuseType.IMPACT, new Vec3(1.5, 2.5, 1.5), Vec3.ZERO, false);

        // The bomb falls onto the stone block and goes off, which only happens when it lands: it has no timer. The pig is
        // one block from the landing point, so it gets a scaled dose of the potion.
        helper.succeedWhen(() -> {
            assertTrue(helper, bomb.isRemoved(), "Impact bomb has not exploded");
            var effect = getSpeed(pig);
            assertFalse(helper, effect == null, "Mob has no speed effect after the bomb exploded");
            assertTrue(helper, effect.getDuration() > 20 && effect.getDuration() < FULL_DURATION, "Unexpected speed duration " + effect.getDuration());
        });
    }

    public static void timed_bomb_explodes_when_its_fuse_runs_out(GameTestHelper helper, FuseType fuse, int expectedTicks) {
        assertValueEqual(helper, expectedTicks, fuse.getFuseLength(), "Fuse length of " + fuse.getSerializedName());
        Mob pig = spawnStillPig(helper, new Vec3(2.5, 2.5, 1.5));

        // A bomb with no gravity or velocity can only go off by timing out
        var bomb = spawnBomb(helper, fuse, new Vec3(1.5, 2.5, 1.5), Vec3.ZERO, true);

        helper.succeedWhen(() -> {
            assertTrue(helper, bomb.isRemoved(), "Bomb has not exploded");
            assertValueEqual(helper, expectedTicks, bomb.tickCount, "Age of the bomb when it exploded");
            assertFalse(helper, getSpeed(pig) == null, "Mob has no speed effect after the bomb exploded; mob at " + pig.position() + " alive " + pig.isAlive() + " removed " + pig.getRemovalReason() + ", bomb at " + bomb.position() + " expected mob at " + helper.absoluteVec(new Vec3(2.5, 2.5, 1.5)));
        });
    }

    public static void timed_bomb_bounces_off_a_block(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.STONE);

        // Drop a medium fuse bomb onto the stone block, whose top face is at y = 1
        var bomb = spawnBomb(helper, FuseType.MEDIUM, new Vec3(1.5, 2.5, 1.5), Vec3.ZERO, false);

        // A falling bomb always has downward velocity, so upward velocity means it has bounced rather than stopped or burst
        helper.succeedWhen(() -> {
            assertFalse(helper, bomb.isRemoved(), "Timed bomb exploded on hitting the block");
            assertTrue(helper, bomb.getDeltaMovement().y > 0.0D, "Timed bomb has not bounced, vertical velocity is " + bomb.getDeltaMovement().y);
            assertTrue(helper, bomb.getY() >= helper.absoluteVec(new Vec3(0.0, 1.0, 0.0)).y, "Bomb is below the top of the block");
        });
    }

    public static void timed_bomb_explodes_on_hitting_an_entity(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.STONE);
        Mob pig = spawnStillPig(helper, new Vec3(1.5, 1.0, 1.5));

        // A medium fuse bomb takes 60 ticks to time out, and falls onto the pig long before then
        var bomb = spawnBomb(helper, FuseType.MEDIUM, new Vec3(1.5, 2.5, 1.5), new Vec3(0.0, -0.5, 0.0), false);

        // The pig was struck directly, so it gets the full duration rather than one scaled by its distance
        helper.succeedWhen(() -> {
            assertTrue(helper, bomb.isRemoved(), "Timed bomb has not exploded");
            assertTrue(helper, bomb.tickCount < FuseType.MEDIUM.getFuseLength(), "Timed bomb exploded by timing out, at age " + bomb.tickCount + "; mob at " + pig.position() + " alive " + pig.isAlive() + ", bomb at " + bomb.position() + " expected mob at " + helper.absoluteVec(new Vec3(1.5, 1.0, 1.5)));
            var effect = getSpeed(pig);
            assertFalse(helper, effect == null, "Mob has no speed effect after being struck");
            assertTrue(helper, effect.getDuration() >= FULL_DURATION - 10, "Struck pig did not get the full duration: " + effect.getDuration());
        });
    }

    public static void bomb_effect_reaches_all_entities_near_the_detonation(GameTestHelper helper) {
        // Two pigs within four blocks of the bomb, one on either side, and one six blocks away
        Mob nearPig1 = spawnStillPig(helper, new Vec3(2.5, 2.5, 1.5));
        Mob nearPig2 = spawnStillPig(helper, new Vec3(0.5, 2.5, 1.5));
        Mob farPig = spawnStillPig(helper, new Vec3(1.5, 2.5, 7.5));
        var bomb = spawnBomb(helper, FuseType.SHORT, new Vec3(1.5, 2.5, 1.5), Vec3.ZERO, true);

        helper.succeedWhen(() -> {
            assertTrue(helper, bomb.isRemoved(), "Bomb has not exploded");
            for (Mob pig : List.of(nearPig1, nearPig2)) {
                // Each is one block from the bomb, so it gets 1 - 1/4 of the full duration, and it may have ticked once
                var effect = getSpeed(pig);
                assertFalse(helper, effect == null, "Near pig has no speed effect");
                assertEffectDurationInRange(helper, effect, 2700 - 5, 2700);
                assertValueEqual(helper, 0, effect.getAmplifier(), "Speed amplifier");
            }
            assertTrue(helper, getSpeed(farPig) == null, "Far pig has a speed effect");
        });
    }

    private static void assertEffectDurationInRange(GameTestHelper helper, MobEffectInstance effect, int min, int max) {
        assertTrue(helper, effect.getDuration() >= min && effect.getDuration() <= max, "Effect duration " + effect.getDuration() + " not in " + min + " to " + max);
    }
}
