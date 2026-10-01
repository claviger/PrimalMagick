package com.verdantartifice.primalmagick.test.loot;

import com.google.common.collect.ImmutableMap;
import com.verdantartifice.primalmagick.common.enchantments.EnchantmentsPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.loot.LootModifiers;
import com.verdantartifice.primalmagick.common.tags.BlockExtensionTags;
import com.verdantartifice.primalmagick.common.tags.CommonTags;
import com.verdantartifice.primalmagick.common.tags.EntityTypeTagsPM;
import com.verdantartifice.primalmagick.common.tags.ItemExtensionTags;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Tests for the platform-independent loot modifier logic in LootModifiers: adding and replacing items, the
 * tag-gated mob drops (bloody flesh, blood notes, relic fragments, and Guillotine heads), the enchantment-driven
 * bonuses (Lucky Strike nuggets, Bounty farming and fishing rolls), and the Essence Thief enchantment level gate.
 * The random chance conditions that guard most modifiers live in the datapack JSON rather than in these methods, so
 * each test calls a modifier method directly with a hand-built loot context. Where a method takes its own chance or
 * count range, tests pin it (chance 1.0 or 0.0, single-value ranges) so the outcome is deterministic.
 */
public class LootModifierTests extends AbstractBaseTest {
    // Mirrors the nugget map of the lucky_strike loot modifier in LootModifierProviderNeoforge
    protected static final Map<TagKey<Block>, TagKey<Item>> NUGGET_MAP = ImmutableMap.<TagKey<Block>, TagKey<Item>>builder()
            .put(CommonTags.Blocks.ORES_IRON, CommonTags.Items.NUGGETS_IRON)
            .put(CommonTags.Blocks.ORES_GOLD, CommonTags.Items.NUGGETS_GOLD)
            .put(CommonTags.Blocks.ORES_QUARTZ, ItemExtensionTags.NUGGETS_QUARTZ)
            .put(CommonTags.Blocks.ORES_COPPER, ItemExtensionTags.NUGGETS_COPPER)
            .put(BlockExtensionTags.ORES_TIN, ItemExtensionTags.NUGGETS_TIN)
            .put(BlockExtensionTags.ORES_LEAD, ItemExtensionTags.NUGGETS_LEAD)
            .put(BlockExtensionTags.ORES_SILVER, ItemExtensionTags.NUGGETS_SILVER)
            .put(BlockExtensionTags.ORES_URANIUM, ItemExtensionTags.NUGGETS_URANIUM)
            .build();

    // Lucky Strike makes one chance roll per level, so at chance 1.0 a level 3 tool yields exactly 3 nuggets
    protected static final int LUCKY_STRIKE_LEVEL = 3;

    // Bounty makes one bonus roll of the source loot table per level
    protected static final int BOUNTY_LEVEL = 2;

    // Modifiers test their chance as nextFloat() < chance, and nextFloat() is in [0, 1), so a chance of 1.0 always
    // passes and a chance of 0.0 never does
    protected static final float GUARANTEED_CHANCE = 1.0F;
    protected static final float IMPOSSIBLE_CHANCE = 0.0F;

    // The relic_fragments_high modifier drops between 3 and 5 mystical relic fragments
    protected static final int RELIC_FRAGMENTS_HIGH_MIN = 3;
    protected static final int RELIC_FRAGMENTS_HIGH_MAX = 5;

    // Number of independent relic fragment drops sampled to confirm the count range bounds
    protected static final int RELIC_FRAGMENT_SAMPLES = 200;

    // Number of independent Guillotine drops sampled to confirm that the drop chance scales with enchantment level
    protected static final int GUILLOTINE_SCALING_SAMPLES = 30;

    // The most items a single roll of the vanilla fishing table can produce is the junk pool's stack of 10 ink sacs;
    // every other entry yields a single item
    protected static final int MAX_FISHING_ROLL_ITEMS = 10;

    /**
     * Returns a fresh copy of the pre-existing loot that each modifier is applied to, so that tests can confirm the
     * modifier leaves earlier entries in place.
     */
    protected static ObjectArrayList<ItemStack> existingLoot() {
        return ObjectArrayList.of(new ItemStack(Items.DIAMOND, 2));
    }

    /**
     * Returns the pre-existing loot followed by the given stacks, i.e. the expected result of a modifier that keeps
     * the existing loot and appends the given stacks.
     */
    protected static List<ItemStack> expectedLoot(ItemStack... added) {
        List<ItemStack> retVal = new ArrayList<>(existingLoot());
        retVal.addAll(List.of(added));
        return retVal;
    }

    /**
     * Renders a loot list as "count item_id" strings so that lists can be compared and reported readably. Empty
     * stacks are skipped, since the loot pipeline never drops them.
     */
    protected static List<String> describe(List<ItemStack> loot) {
        return loot.stream().filter(stack -> !stack.isEmpty()).map(stack -> stack.getCount() + " " + BuiltInRegistries.ITEM.getKey(stack.getItem())).toList();
    }

    protected static void assertLoot(GameTestHelper helper, List<ItemStack> expected, List<ItemStack> actual, String failureMessage) {
        assertValueEqual(helper, describe(expected), describe(actual), failureMessage);
    }

    protected static String entityName(EntityType<?> type) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
    }

    protected static ItemStack enchantedStack(GameTestHelper helper, Item item, ResourceKey<Enchantment> enchantKey, int level) {
        ItemStack stack = new ItemStack(item);
        stack.enchant(helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantKey), level);
        return stack;
    }

    protected static Vec3 testOrigin(GameTestHelper helper) {
        return Vec3.atCenterOf(helper.absolutePos(BlockPos.ZERO));
    }

    /**
     * Assembles a loot context for a mob killed by the given player, as used for entity loot tables.
     */
    protected static LootContext entityKillContext(GameTestHelper helper, Mob target, ServerPlayer killer) {
        LootParams params = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.THIS_ENTITY, target)
                .withParameter(LootContextParams.ORIGIN, target.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, killer.damageSources().playerAttack(killer))
                .withParameter(LootContextParams.ATTACKING_ENTITY, killer)
                .create(LootContextParamSets.ENTITY);
        return new LootContext.Builder(params).create(Optional.empty());
    }

    /**
     * Assembles a loot context for a mob of the given type killed by a fresh player wielding the given weapon.
     */
    protected static LootContext entityKillContext(GameTestHelper helper, EntityType<? extends Mob> targetType, ItemStack weapon) {
        ServerPlayer player = makeMockServerPlayer(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, weapon);
        Mob target = helper.spawnWithNoFreeWill(targetType, BlockPos.ZERO);
        return entityKillContext(helper, target, player);
    }

    /**
     * Assembles a loot context for a block broken with the given tool, as used for block loot tables.
     */
    protected static LootContext blockContext(GameTestHelper helper, BlockState state, ItemStack tool) {
        LootParams params = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withParameter(LootContextParams.ORIGIN, testOrigin(helper))
                .withParameter(LootContextParams.TOOL, tool)
                .create(LootContextParamSets.BLOCK);
        return new LootContext.Builder(params).create(Optional.empty());
    }

    /**
     * Assembles a loot context for a catch reeled in with the given fishing rod.
     */
    protected static LootContext fishingContext(GameTestHelper helper, ItemStack rod) {
        LootParams params = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, testOrigin(helper))
                .withParameter(LootContextParams.TOOL, rod)
                .create(LootContextParamSets.FISHING);
        return new LootContext.Builder(params).create(Optional.empty());
    }

    /**
     * Assembles a loot context with only an origin, as used for chest and archaeology loot. The add and replace item
     * modifiers read nothing from the context but its random source.
     */
    protected static LootContext originContext(GameTestHelper helper) {
        LootParams params = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, testOrigin(helper))
                .create(LootContextParamSets.CHEST);
        return new LootContext.Builder(params).create(Optional.empty());
    }

    protected static int totalCount(List<ItemStack> loot) {
        return loot.stream().mapToInt(ItemStack::getCount).sum();
    }

    // Add item tests

    public static void loot_add_item_adds_fixed_rolls(GameTestHelper helper) {
        // Each roll appends its own single-item stack rather than one stack of the rolled count
        var fragment = ItemsPM.LORE_TABLET_FRAGMENT.get();
        var actual = LootModifiers.addItem(existingLoot(), originContext(helper), fragment, UniformInt.of(3, 3));
        assertLoot(helper, expectedLoot(new ItemStack(fragment), new ItemStack(fragment), new ItemStack(fragment)), actual, "Loot after adding three rolls");
        helper.succeed();
    }

    public static void loot_add_item_zero_rolls_adds_nothing(GameTestHelper helper) {
        var actual = LootModifiers.addItem(existingLoot(), originContext(helper), ItemsPM.LORE_TABLET_FRAGMENT.get(), UniformInt.of(0, 0));
        assertLoot(helper, expectedLoot(), actual, "Loot after adding zero rolls");
        helper.succeed();
    }

    // Replace item tests

    public static void loot_replace_item_replaces_all_entries(GameTestHelper helper) {
        // Every existing entry is discarded, regardless of item or count, in favor of a single replacement item
        var initial = ObjectArrayList.of(new ItemStack(Items.DIAMOND, 2), new ItemStack(Items.EMERALD, 5));
        var actual = LootModifiers.replaceItem(initial, originContext(helper), ItemsPM.LORE_TABLET_FRAGMENT.get());
        assertLoot(helper, List.of(new ItemStack(ItemsPM.LORE_TABLET_FRAGMENT.get())), actual, "Loot after replacement");
        helper.succeed();
    }

    // Bloody flesh tests

    public static void loot_bloody_flesh_drops_for_tagged_entity(GameTestHelper helper, EntityType<? extends Mob> targetType) {
        var context = entityKillContext(helper, targetType, new ItemStack(Items.DIAMOND_SWORD));
        var actual = LootModifiers.bloodyFlesh(existingLoot(), context, EntityTypeTagsPM.DROPS_BLOODY_FLESH);
        assertLoot(helper, expectedLoot(new ItemStack(ItemsPM.BLOODY_FLESH.get())), actual, "Loot for " + entityName(targetType));
        helper.succeed();
    }

    public static void loot_bloody_flesh_skips_untagged_entity(GameTestHelper helper) {
        var context = entityKillContext(helper, EntityType.COW, new ItemStack(Items.DIAMOND_SWORD));
        var actual = LootModifiers.bloodyFlesh(existingLoot(), context, EntityTypeTagsPM.DROPS_BLOODY_FLESH);
        assertLoot(helper, expectedLoot(), actual, "Loot for untagged cow");
        helper.succeed();
    }

    // Blood notes tests

    public static void loot_blood_notes_drops_for_tagged_entity(GameTestHelper helper, EntityType<? extends Mob> targetType, TagKey<EntityType<?>> targetTag) {
        var context = entityKillContext(helper, targetType, new ItemStack(Items.DIAMOND_SWORD));
        var actual = LootModifiers.bloodNotes(existingLoot(), context, targetTag);
        assertLoot(helper, expectedLoot(new ItemStack(ItemsPM.BLOOD_NOTES.get())), actual, "Loot for " + entityName(targetType) + " with tag " + targetTag.location());
        helper.succeed();
    }

    public static void loot_blood_notes_skips_untagged_entity(GameTestHelper helper) {
        var context = entityKillContext(helper, EntityType.COW, new ItemStack(Items.DIAMOND_SWORD));
        var actual = LootModifiers.bloodNotes(existingLoot(), context, EntityTypeTagsPM.DROPS_BLOOD_NOTES_HIGH);
        assertLoot(helper, expectedLoot(), actual, "Loot for untagged cow");
        helper.succeed();
    }

    // Bonus nugget (Lucky Strike) tests

    public static void loot_bonus_nugget(GameTestHelper helper, Block ore, Item nugget, float chance, int expectedNuggets) {
        var tool = enchantedStack(helper, Items.DIAMOND_PICKAXE, EnchantmentsPM.LUCKY_STRIKE, LUCKY_STRIKE_LEVEL);
        var actual = LootModifiers.bonusNugget(existingLoot(), blockContext(helper, ore.defaultBlockState(), tool), chance, NUGGET_MAP);
        var expected = expectedNuggets > 0 ? expectedLoot(new ItemStack(nugget, expectedNuggets)) : expectedLoot();
        // When every chance roll fails, the modifier still appends a zero-count (and therefore empty) nugget stack,
        // which describe() skips because it is never dropped
        assertLoot(helper, expected, actual, "Loot for " + BuiltInRegistries.BLOCK.getKey(ore) + " at chance " + chance);
        helper.succeed();
    }

    public static void loot_bonus_nugget_requires_lucky_strike(GameTestHelper helper) {
        var actual = LootModifiers.bonusNugget(existingLoot(), blockContext(helper, Blocks.IRON_ORE.defaultBlockState(), new ItemStack(Items.DIAMOND_PICKAXE)), GUARANTEED_CHANCE, NUGGET_MAP);
        assertLoot(helper, expectedLoot(), actual, "Loot for iron ore mined without Lucky Strike");
        helper.succeed();
    }

    public static void loot_bonus_nugget_skips_untagged_block(GameTestHelper helper) {
        var tool = enchantedStack(helper, Items.DIAMOND_PICKAXE, EnchantmentsPM.LUCKY_STRIKE, LUCKY_STRIKE_LEVEL);
        var actual = LootModifiers.bonusNugget(existingLoot(), blockContext(helper, Blocks.STONE.defaultBlockState(), tool), GUARANTEED_CHANCE, NUGGET_MAP);
        assertLoot(helper, expectedLoot(), actual, "Loot for stone");
        helper.succeed();
    }

    // Bounty farming tests

    public static void loot_bounty_farming(GameTestHelper helper, float chance, int expectedSeeds) {
        // Immature wheat has a deterministic loot table of exactly one wheat seeds item, so each bonus roll of that
        // table adds one seed, which is merged into the existing seeds stack
        var tool = enchantedStack(helper, Items.DIAMOND_HOE, EnchantmentsPM.BOUNTY, BOUNTY_LEVEL);
        var context = blockContext(helper, Blocks.WHEAT.defaultBlockState(), tool);
        var actual = LootModifiers.bountyFarming(ObjectArrayList.of(new ItemStack(Items.WHEAT_SEEDS)), context, chance);
        assertLoot(helper, List.of(new ItemStack(Items.WHEAT_SEEDS, expectedSeeds)), actual, "Loot for immature wheat at chance " + chance);
        helper.succeed();
    }

    // Bounty fishing tests

    public static void loot_bounty_fishing_chance_1(GameTestHelper helper) {
        // Each bonus roll of the vanilla fishing table draws its result from the context's random source, so only
        // bounds on the added item count can be asserted. This only checks that the modifier rolls the FISHING table;
        // bountyInner's per-level rolls and stack merging are pinned exactly by the bounty farming tests. Merging only
        // combines stacks, so the total count is the original count plus whatever the bonus rolls produced. The upper
        // bound of 20 relies on the vanilla junk pool's stack of 10 ink sacs being the largest single roll.
        var rod = enchantedStack(helper, Items.FISHING_ROD, EnchantmentsPM.BOUNTY, BOUNTY_LEVEL);
        var actual = LootModifiers.bountyFishing(ObjectArrayList.of(new ItemStack(Items.COD)), fishingContext(helper, rod), GUARANTEED_CHANCE);
        assertTrue(helper, !actual.isEmpty() && actual.getFirst().is(Items.COD), "Original catch not kept first in loot: " + describe(actual));
        int added = totalCount(actual) - 1;
        assertTrue(helper, added >= BOUNTY_LEVEL && added <= BOUNTY_LEVEL * MAX_FISHING_ROLL_ITEMS, "Bonus fishing item count out of range: " + added + " in " + describe(actual));
        helper.succeed();
    }

    public static void loot_bounty_fishing_chance_0(GameTestHelper helper) {
        var rod = enchantedStack(helper, Items.FISHING_ROD, EnchantmentsPM.BOUNTY, BOUNTY_LEVEL);
        var actual = LootModifiers.bountyFishing(ObjectArrayList.of(new ItemStack(Items.COD)), fishingContext(helper, rod), IMPOSSIBLE_CHANCE);
        assertLoot(helper, List.of(new ItemStack(Items.COD)), actual, "Loot for fishing at chance 0");
        helper.succeed();
    }

    // Guillotine tests

    public static void loot_guillotine_drops_head_for_tagged_entity(GameTestHelper helper, EntityType<? extends Mob> targetType, TagKey<EntityType<?>> targetTag, Item head) {
        // The real Guillotine modifiers use a chance of 0.1 per level; a chance of 1.0 at level 1 guarantees the head
        var weapon = enchantedStack(helper, Items.DIAMOND_SWORD, EnchantmentsPM.GUILLOTINE, 1);
        var actual = LootModifiers.guillotine(existingLoot(), entityKillContext(helper, targetType, weapon), targetTag, head, GUARANTEED_CHANCE);
        assertLoot(helper, expectedLoot(new ItemStack(head)), actual, "Loot for " + entityName(targetType));
        helper.succeed();
    }

    public static void loot_guillotine_drops_head_scales_with_level(GameTestHelper helper) {
        // The drop chance is the configured chance multiplied by the enchantment level, so 0.5 at level 2 is 1.0 and
        // the head must drop on every sample. If the level multiplier were lost, each sample would drop the head only
        // half the time, so a regression would pass all samples with probability 0.5^30, or about 3 * 0.5^30 across
        // the test's three attempts.
        var killer = makeMockServerPlayer(helper);
        killer.setItemInHand(InteractionHand.MAIN_HAND, enchantedStack(helper, Items.DIAMOND_SWORD, EnchantmentsPM.GUILLOTINE, 2));
        var target = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, BlockPos.ZERO);
        for (int sample = 0; sample < GUILLOTINE_SCALING_SAMPLES; sample++) {
            var actual = LootModifiers.guillotine(existingLoot(), entityKillContext(helper, target, killer), EntityTypeTagsPM.GUILLOTINE_ZOMBIE_HEAD, Items.ZOMBIE_HEAD, 0.5F);
            assertLoot(helper, expectedLoot(new ItemStack(Items.ZOMBIE_HEAD)), actual, "Loot for zombie killed with Guillotine 2 at chance 0.5, sample " + sample);
        }
        helper.succeed();
    }

    public static void loot_guillotine_skips_untagged_entity(GameTestHelper helper) {
        var weapon = enchantedStack(helper, Items.DIAMOND_SWORD, EnchantmentsPM.GUILLOTINE, 1);
        var actual = LootModifiers.guillotine(existingLoot(), entityKillContext(helper, EntityType.COW, weapon), EntityTypeTagsPM.GUILLOTINE_ZOMBIE_HEAD, Items.ZOMBIE_HEAD, GUARANTEED_CHANCE);
        assertLoot(helper, expectedLoot(), actual, "Loot for untagged cow");
        helper.succeed();
    }

    public static void loot_guillotine_requires_enchantment(GameTestHelper helper) {
        var actual = LootModifiers.guillotine(existingLoot(), entityKillContext(helper, EntityType.ZOMBIE, new ItemStack(Items.DIAMOND_SWORD)),
                EntityTypeTagsPM.GUILLOTINE_ZOMBIE_HEAD, Items.ZOMBIE_HEAD, GUARANTEED_CHANCE);
        assertLoot(helper, expectedLoot(), actual, "Loot for zombie killed without Guillotine");
        helper.succeed();
    }

    public static void loot_guillotine_skips_existing_head(GameTestHelper helper) {
        // A head that is already in the loot (e.g. from a charged creeper explosion) is not duplicated
        var weapon = enchantedStack(helper, Items.DIAMOND_SWORD, EnchantmentsPM.GUILLOTINE, 1);
        var initial = ObjectArrayList.of(new ItemStack(Items.ZOMBIE_HEAD));
        var actual = LootModifiers.guillotine(initial, entityKillContext(helper, EntityType.ZOMBIE, weapon), EntityTypeTagsPM.GUILLOTINE_ZOMBIE_HEAD, Items.ZOMBIE_HEAD, GUARANTEED_CHANCE);
        assertLoot(helper, List.of(new ItemStack(Items.ZOMBIE_HEAD)), actual, "Loot for zombie that already dropped its head");
        helper.succeed();
    }

    // Relic fragment tests

    public static void loot_relic_fragments_drops_for_tagged_entity(GameTestHelper helper, EntityType<? extends Mob> targetType, TagKey<EntityType<?>> targetTag, int count) {
        // A single-value count range makes the drawn count deterministic
        var context = entityKillContext(helper, targetType, new ItemStack(Items.DIAMOND_SWORD));
        var actual = LootModifiers.relicFragments(existingLoot(), context, targetTag, count, count);
        assertLoot(helper, expectedLoot(new ItemStack(ItemsPM.MYSTICAL_RELIC_FRAGMENT.get(), count)), actual, "Loot for " + entityName(targetType) + " with tag " + targetTag.location());
        helper.succeed();
    }

    public static void loot_relic_fragments_skips_untagged_entity(GameTestHelper helper) {
        var context = entityKillContext(helper, EntityType.COW, new ItemStack(Items.DIAMOND_SWORD));
        var actual = LootModifiers.relicFragments(existingLoot(), context, EntityTypeTagsPM.DROPS_RELIC_FRAGMENTS_LOW, 1, 1);
        assertLoot(helper, expectedLoot(), actual, "Loot for untagged cow");
        helper.succeed();
    }

    public static void loot_relic_fragments_count_within_range(GameTestHelper helper) {
        // The fragment count is drawn from the context's random source, so sample many independent drops, checking
        // that every count is within the range and that both ends of the range are reached. Each end is missed by
        // every sample with probability (2/3)^200, so the chance of a false failure is roughly 2 * (2/3)^200 (~1e-35).
        var killer = makeMockServerPlayer(helper);
        killer.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
        var target = helper.spawnWithNoFreeWill(EntityType.EVOKER, BlockPos.ZERO);
        Set<Integer> observedCounts = new HashSet<>();
        for (int sample = 0; sample < RELIC_FRAGMENT_SAMPLES; sample++) {
            var actual = LootModifiers.relicFragments(ObjectArrayList.of(), entityKillContext(helper, target, killer), EntityTypeTagsPM.DROPS_RELIC_FRAGMENTS_HIGH,
                    RELIC_FRAGMENTS_HIGH_MIN, RELIC_FRAGMENTS_HIGH_MAX);
            assertValueEqual(helper, 1, actual.size(), "Relic fragment stack count");
            var stack = actual.getFirst();
            assertTrue(helper, stack.is(ItemsPM.MYSTICAL_RELIC_FRAGMENT.get()), "Dropped item is not a mystical relic fragment: " + describe(actual));
            assertTrue(helper, stack.getCount() >= RELIC_FRAGMENTS_HIGH_MIN && stack.getCount() <= RELIC_FRAGMENTS_HIGH_MAX, "Relic fragment count out of range: " + stack.getCount());
            observedCounts.add(stack.getCount());
        }
        assertTrue(helper, observedCounts.contains(RELIC_FRAGMENTS_HIGH_MIN), "Minimum relic fragment count never observed: " + observedCounts);
        assertTrue(helper, observedCounts.contains(RELIC_FRAGMENTS_HIGH_MAX), "Maximum relic fragment count never observed: " + observedCounts);
        helper.succeed();
    }

    // Essence Thief tests

    public static void loot_essence_thief_requires_enchantment(GameTestHelper helper) {
        // Cows have blood affinity, so the only thing preventing an essence drop is the missing enchantment. The
        // positive control is RitualEnchantmentTests.enchantment_essence_thief, which gets essence from a cow.
        var context = entityKillContext(helper, EntityType.COW, new ItemStack(Items.DIAMOND_SWORD));
        var actual = LootModifiers.essenceThief(existingLoot(), context);
        assertLoot(helper, expectedLoot(), actual, "Loot for cow killed without Essence Thief");
        helper.succeed();
    }
}
