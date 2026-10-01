package com.verdantartifice.primalmagick.test.items;

import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.items.essence.EssenceItem;
import com.verdantartifice.primalmagick.common.items.essence.EssenceType;
import com.verdantartifice.primalmagick.common.research.ResearchEntries;
import com.verdantartifice.primalmagick.common.research.ResearchEntry;
import com.verdantartifice.primalmagick.common.research.ResearchManager;
import com.verdantartifice.primalmagick.common.sources.Source;
import com.verdantartifice.primalmagick.common.sources.Sources;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Tests for essence items and essence types: looking up essence items and stacks by type and source, the fixed
 * properties of each essence type (affinity, mana equivalent, upgrade and downgrade chains, upgrade media, and
 * synthesis research), and the research that gates discovery of essence types and sources. Expected values are
 * hard-coded from the EssenceType and Sources definitions so that changes to that content are caught.
 */
public class EssenceTests extends AbstractBaseTest {
    // There are four essence types (dust, shard, crystal, cluster) and nine sources (five primal, three forbidden,
    // one hallowed), with exactly one essence item for each combination
    protected static final int ESSENCE_TYPE_COUNT = 4;
    protected static final int SOURCE_COUNT = 9;
    protected static final int ESSENCE_ITEM_COUNT = ESSENCE_TYPE_COUNT * SOURCE_COUNT;

    // Stack size used to confirm that getEssence honors an explicit count
    protected static final int STACK_COUNT = 5;

    // Sources that have no discovery research and are therefore known to every player from the start
    protected static final List<Source> PRIMAL_SOURCES = List.of(Sources.EARTH, Sources.SEA, Sources.SKY, Sources.SUN, Sources.MOON);

    // Lookup tests

    /**
     * Confirms that every source has an essence item of the given type that reports that type and source, and that
     * the spot-checked combinations resolve to the expected registered items.
     */
    public static void essence_item_lookup(GameTestHelper helper, EssenceType type, Map<Source, Item> spotChecks) {
        for (Source source : Sources.getAll()) {
            String label = type.getSerializedName() + " " + source.getId();
            Item item = EssenceItem.getEssenceItem(type, source);
            EssenceItem essence = assertInstanceOf(helper, item, EssenceItem.class, "Item for " + label + " is not an essence: " + item);
            assertValueEqual(helper, type, essence.getEssenceType(), "Essence type of " + label);
            assertValueEqual(helper, source, essence.getSource(), "Source of " + label);
        }
        spotChecks.forEach((source, expectedItem) -> assertValueEqual(helper, expectedItem, EssenceItem.getEssenceItem(type, source), "Registered item for " + type.getSerializedName() + " " + source.getId()));
        helper.succeed();
    }

    public static void essence_stack_lookup(GameTestHelper helper) {
        ItemStack single = EssenceItem.getEssence(EssenceType.SHARD, Sources.SKY);
        assertValueEqual(helper, ItemsPM.ESSENCE_SHARD_SKY.get(), single.getItem(), "Item of default-count stack");
        assertValueEqual(helper, 1, single.getCount(), "Count of default-count stack");

        ItemStack multiple = EssenceItem.getEssence(EssenceType.CRYSTAL, Sources.VOID, STACK_COUNT);
        assertValueEqual(helper, ItemsPM.ESSENCE_CRYSTAL_VOID.get(), multiple.getItem(), "Item of explicit-count stack");
        assertValueEqual(helper, STACK_COUNT, multiple.getCount(), "Count of explicit-count stack");

        // A missing type or source finds no item in the lookup table, so an empty stack is returned rather than null
        ItemStack noType = EssenceItem.getEssence(null, Sources.SKY);
        assertTrue(helper, noType.isEmpty(), "Stack for null type is not empty: " + noType);
        ItemStack noSource = EssenceItem.getEssence(EssenceType.SHARD, null);
        assertTrue(helper, noSource.isEmpty(), "Stack for null source is not empty: " + noSource);
        ItemStack noTypeWithCount = EssenceItem.getEssence(null, Sources.SKY, STACK_COUNT);
        assertTrue(helper, noTypeWithCount.isEmpty(), "Explicit-count stack for null type is not empty: " + noTypeWithCount);
        helper.succeed();
    }

    public static void essence_all_essences_count(GameTestHelper helper) {
        assertValueEqual(helper, SOURCE_COUNT, Sources.getAll().size(), "Number of sources");
        assertValueEqual(helper, ESSENCE_TYPE_COUNT, EssenceType.values().length, "Number of essence types");

        Collection<Item> essences = EssenceItem.getAllEssences();
        assertValueEqual(helper, ESSENCE_ITEM_COUNT, essences.size(), "Number of essence items");
        assertValueEqual(helper, ESSENCE_ITEM_COUNT, Set.copyOf(essences).size(), "Number of distinct essence items");
        for (Item item : essences) {
            assertInstanceOf(helper, item, EssenceItem.class, "Non-essence item in essence collection: " + item);
        }
        helper.succeed();
    }

    // Essence type property tests

    public static void essence_type_mana_equivalent(GameTestHelper helper) {
        // Each step up in quality is worth ten times the centimana of the step below
        assertValueEqual(helper, 100, EssenceType.DUST.getManaEquivalent(), "Dust mana equivalent");
        assertValueEqual(helper, 1000, EssenceType.SHARD.getManaEquivalent(), "Shard mana equivalent");
        assertValueEqual(helper, 10000, EssenceType.CRYSTAL.getManaEquivalent(), "Crystal mana equivalent");
        assertValueEqual(helper, 100000, EssenceType.CLUSTER.getManaEquivalent(), "Cluster mana equivalent");
        helper.succeed();
    }

    public static void essence_type_affinity(GameTestHelper helper) {
        assertValueEqual(helper, 5, EssenceType.DUST.getAffinity(), "Dust affinity");
        assertValueEqual(helper, 20, EssenceType.SHARD.getAffinity(), "Shard affinity");
        assertValueEqual(helper, 50, EssenceType.CRYSTAL.getAffinity(), "Crystal affinity");
        assertValueEqual(helper, 100, EssenceType.CLUSTER.getAffinity(), "Cluster affinity");
        helper.succeed();
    }

    public static void essence_type_upgrade_chain(GameTestHelper helper) {
        assertValueEqual(helper, Optional.of(EssenceType.SHARD), EssenceType.DUST.getUpgrade(), "Dust upgrade");
        assertValueEqual(helper, Optional.of(EssenceType.CRYSTAL), EssenceType.SHARD.getUpgrade(), "Shard upgrade");
        assertValueEqual(helper, Optional.of(EssenceType.CLUSTER), EssenceType.CRYSTAL.getUpgrade(), "Crystal upgrade");
        assertValueEqual(helper, Optional.empty(), EssenceType.CLUSTER.getUpgrade(), "Cluster upgrade");

        assertValueEqual(helper, Optional.of(EssenceType.CRYSTAL), EssenceType.CLUSTER.getDowngrade(), "Cluster downgrade");
        assertValueEqual(helper, Optional.of(EssenceType.SHARD), EssenceType.CRYSTAL.getDowngrade(), "Crystal downgrade");
        assertValueEqual(helper, Optional.of(EssenceType.DUST), EssenceType.SHARD.getDowngrade(), "Shard downgrade");
        assertValueEqual(helper, Optional.empty(), EssenceType.DUST.getDowngrade(), "Dust downgrade");
        helper.succeed();
    }

    /**
     * The upgrade medium is the quartz needed to synthesize an essence *into* the given type, so the lowest type has
     * none and the media scale up from nugget to gem to block.
     */
    public static void essence_type_upgrade_medium(GameTestHelper helper) {
        assertValueEqual(helper, Optional.empty(), EssenceType.DUST.getUpgradeMedium(), "Dust upgrade medium");
        assertValueEqual(helper, Optional.of(ItemsPM.QUARTZ_NUGGET.get()), EssenceType.SHARD.getUpgradeMedium(), "Shard upgrade medium");
        assertValueEqual(helper, Optional.of(Items.QUARTZ), EssenceType.CRYSTAL.getUpgradeMedium(), "Crystal upgrade medium");
        assertValueEqual(helper, Optional.of(Items.QUARTZ_BLOCK), EssenceType.CLUSTER.getUpgradeMedium(), "Cluster upgrade medium");
        helper.succeed();
    }

    /**
     * The upgrade research for a type is the synthesis research for the type it upgrades to, and the downgrade
     * research for a type is the desynthesis research for that type itself.
     */
    public static void essence_type_upgrade_research(GameTestHelper helper) {
        assertValueEqual(helper, Optional.of(ResearchEntries.SHARD_SYNTHESIS), EssenceType.DUST.getUpgradeResearchEntry(), "Dust upgrade research");
        assertValueEqual(helper, Optional.of(ResearchEntries.CRYSTAL_SYNTHESIS), EssenceType.SHARD.getUpgradeResearchEntry(), "Shard upgrade research");
        assertValueEqual(helper, Optional.of(ResearchEntries.CLUSTER_SYNTHESIS), EssenceType.CRYSTAL.getUpgradeResearchEntry(), "Crystal upgrade research");
        assertValueEqual(helper, Optional.empty(), EssenceType.CLUSTER.getUpgradeResearchEntry(), "Cluster upgrade research");

        assertValueEqual(helper, Optional.of(ResearchEntries.CLUSTER_DESYNTHESIS), EssenceType.CLUSTER.getDowngradeResearchEntry(), "Cluster downgrade research");
        assertValueEqual(helper, Optional.of(ResearchEntries.CRYSTAL_DESYNTHESIS), EssenceType.CRYSTAL.getDowngradeResearchEntry(), "Crystal downgrade research");
        assertValueEqual(helper, Optional.of(ResearchEntries.SHARD_DESYNTHESIS), EssenceType.SHARD.getDowngradeResearchEntry(), "Shard downgrade research");
        assertValueEqual(helper, Optional.empty(), EssenceType.DUST.getDowngradeResearchEntry(), "Dust downgrade research");
        helper.succeed();
    }

    // Discovery tests

    /**
     * Confirms that the given essence type is unknown to a fresh player and becomes known once the gating research is
     * granted. Dust has no gating research and is discovered by every player from the start, so an empty research
     * key means the type should be discovered immediately. In either case, the next type up must remain undiscovered,
     * since granting a type's research (with its parents) must not unlock higher qualities.
     */
    public static void essence_type_discovery(GameTestHelper helper, EssenceType type, Optional<ResourceKey<ResearchEntry>> gatingResearch) {
        var player = makeMockServerPlayer(helper);
        String label = "Essence type " + type.getSerializedName();
        assertFalse(helper, type.isDiscovered(null), label + " is discovered by a null player");
        if (gatingResearch.isEmpty()) {
            assertTrue(helper, type.isDiscovered(player), label + " has no gating research but is not discovered by a fresh player");
        } else {
            assertFalse(helper, type.isDiscovered(player), label + " is discovered by a fresh player");
            ResearchManager.forceGrantWithAllParents(player, gatingResearch.get());
            assertTrue(helper, type.isDiscovered(player), label + " is not discovered after granting " + gatingResearch.get());
        }
        type.getUpgrade().ifPresent(next -> assertFalse(helper, next.isDiscovered(player), "Next essence type " + next.getSerializedName() + " is discovered alongside " + type.getSerializedName()));
        helper.succeed();
    }

    public static void source_discovery(GameTestHelper helper, Source source, ResourceKey<ResearchEntry> discoverResearch) {
        var player = makeMockServerPlayer(helper);
        String label = "Source " + source.getId();
        // A gated source's research requirement is never met by a null player
        assertFalse(helper, source.isDiscovered(null), label + " is discovered by a null player");
        assertFalse(helper, source.isDiscovered(player), label + " is discovered by a fresh player");
        ResearchManager.forceGrantWithAllParents(player, discoverResearch);
        assertTrue(helper, source.isDiscovered(player), label + " is not discovered after granting " + discoverResearch);
        helper.succeed();
    }

    public static void source_discovery_primal_sources_always_discovered(GameTestHelper helper) {
        var player = makeMockServerPlayer(helper);
        for (Source source : PRIMAL_SOURCES) {
            String label = "Primal source " + source.getId();
            // With no discovery requirement there is nothing to check, so even a null player has discovered the source
            assertTrue(helper, source.isDiscovered(null), label + " is not discovered by a null player");
            assertTrue(helper, source.isDiscovered(player), label + " is not discovered by a fresh player");
        }
        helper.succeed();
    }
}
