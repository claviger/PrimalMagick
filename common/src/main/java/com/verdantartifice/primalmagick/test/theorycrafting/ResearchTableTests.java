package com.verdantartifice.primalmagick.test.theorycrafting;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.menus.ResearchTableMenu;
import com.verdantartifice.primalmagick.common.network.packets.theorycrafting.CompleteProjectPacket;
import com.verdantartifice.primalmagick.common.registries.RegistryKeysPM;
import com.verdantartifice.primalmagick.common.research.KnowledgeType;
import com.verdantartifice.primalmagick.common.stats.StatsManager;
import com.verdantartifice.primalmagick.common.stats.StatsPM;
import com.verdantartifice.primalmagick.common.theorycrafting.MaterialInstance;
import com.verdantartifice.primalmagick.common.theorycrafting.Project;
import com.verdantartifice.primalmagick.common.theorycrafting.ProjectTemplate;
import com.verdantartifice.primalmagick.common.theorycrafting.ProjectTemplates;
import com.verdantartifice.primalmagick.common.theorycrafting.materials.ItemProjectMaterial;
import com.verdantartifice.primalmagick.common.tiles.devices.ResearchTableTileEntity;
import com.verdantartifice.primalmagick.platform.Services;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import com.verdantartifice.primalmagick.test.RecordingServerPlayer;
import com.verdantartifice.primalmagick.test.TestRandomSource;
import commonnetwork.networking.data.PacketContext;
import commonnetwork.networking.data.Side;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Tests for the research table and the theorycrafting projects it runs: the menu's writing-materials check, how
 * generated projects scale with the player's completed projects, and what the complete-project action consumes and
 * grants. The project completion action is driven through its packet handler, with the player's random source fixed
 * so that the success roll is deterministic.
 */
public class ResearchTableTests extends AbstractBaseTest {
    private static final BlockPos TABLE_POS = BlockPos.ZERO.above();
    private static final int PENCIL_SLOT = 0;
    private static final int PAPER_SLOT = 1;
    private static final double TOLERANCE = 1.0E-9D;

    private static ResearchTableTileEntity placeTable(GameTestHelper helper) {
        helper.setBlock(TABLE_POS, BlocksPM.RESEARCH_TABLE.get());
        helper.assertBlockPresent(BlocksPM.RESEARCH_TABLE.get(), TABLE_POS);
        return helper.getBlockEntity(TABLE_POS, ResearchTableTileEntity.class);
    }

    private static ResearchTableMenu openTable(GameTestHelper helper, RecordingServerPlayer player, ResearchTableTileEntity tile) {
        assertTrue(helper, player.openMenu(tile).isPresent(), "Failed to open the research table");
        return assertInstanceOf(helper, player.containerMenu, ResearchTableMenu.class, "Menu not of expected type");
    }

    private static RecordingServerPlayer makeSurvivalPlayer(GameTestHelper helper) {
        var player = RecordingServerPlayer.create(helper, false);
        player.setGameMode(GameType.SURVIVAL);
        player.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());
        assertFalse(helper, player.hasInfiniteMaterials(), "Survival player has infinite materials");
        return player;
    }

    private static ProjectTemplate getTemplate(GameTestHelper helper, net.minecraft.resources.ResourceKey<ProjectTemplate> key) {
        return helper.getLevel().registryAccess().lookupOrThrow(RegistryKeysPM.PROJECT_TEMPLATES).getOrThrow(key).value();
    }

    /**
     * Fixes every random value the player's own random source will produce. The success roll is its double: a project
     * succeeds when that double is below the project's success chance.
     */
    private static void fixPlayerRandom(RecordingServerPlayer player, double roll) {
        player.setForcedRandom(TestRandomSource.builder().setDouble(roll).setGaussian(0.0D).setFloat(0.5F).setInt(0).setLong(0L).setBoolean(false).build());
    }

    /**
     * Gives the player a project with a single optional material, a stick that they carry, selected or not. Its theory
     * reward is (int)(32 points per level * (0.5 base multiplier + 0.25 for the selected stick)) = 24 points.
     */
    private static Project giveStickProject(GameTestHelper helper, RecordingServerPlayer player, boolean selectStick) {
        var stick = new MaterialInstance(ItemProjectMaterial.builder(Items.STICK).bonusReward(0.25D).build());
        stick.setSelected(selectStick);
        var project = new Project(ProjectTemplates.MUNDANE_TINKERING, List.of(stick), List.of(), 0.5D, 0.5D, Optional.empty());
        Services.CAPABILITIES.knowledge(player).ifPresent(knowledge -> knowledge.setActiveResearchProject(project));
        player.getInventory().add(new ItemStack(Items.STICK));
        return project;
    }

    private static void completeProject(RecordingServerPlayer player, ResearchTableMenu menu) {
        CompleteProjectPacket.onMessage(new PacketContext<>(player, new CompleteProjectPacket(menu.containerId), Side.SERVER));
    }

    public static void research_table_requires_paper_and_ink(GameTestHelper helper) {
        var player = RecordingServerPlayer.create(helper, false);
        var tile = placeTable(helper);
        var menu = openTable(helper, player, tile);
        assertFalse(helper, menu.isWritingReady(), "Empty research table is ready for writing");

        // Ink alone isn't enough
        menu.getSlot(PENCIL_SLOT).safeInsert(new ItemStack(ItemsPM.ENCHANTED_INK_AND_QUILL.get()));
        assertTrue(helper, menu.getSlot(PENCIL_SLOT).hasItem(), "Ink and quill not accepted by the writing implement slot");
        assertFalse(helper, menu.isWritingReady(), "Research table with only ink is ready for writing");

        // Paper alone isn't enough either
        menu.getSlot(PENCIL_SLOT).set(ItemStack.EMPTY);
        assertFalse(helper, menu.getSlot(PENCIL_SLOT).hasItem(), "Ink and quill still in the writing implement slot");
        menu.getSlot(PAPER_SLOT).safeInsert(new ItemStack(Items.PAPER));
        assertTrue(helper, menu.getSlot(PAPER_SLOT).hasItem(), "Paper not accepted by the paper slot");
        assertFalse(helper, menu.isWritingReady(), "Research table with only paper is ready for writing");

        // Both together are
        menu.getSlot(PENCIL_SLOT).safeInsert(new ItemStack(ItemsPM.ENCHANTED_INK_AND_QUILL.get()));
        assertTrue(helper, menu.isWritingReady(), "Research table with paper and ink is not ready for writing");
        helper.succeed();
    }

    public static void research_projects_gain_materials_as_projects_are_completed(GameTestHelper helper) {
        var player = makeSurvivalPlayer(helper);
        var template = getTemplate(helper, ProjectTemplates.MUNDANE_TINKERING);

        // A template with no overrides needs 1 material, plus one for every 5 projects completed, to a maximum of 4
        int[][] completedToMaterials = { {0, 1}, {4, 1}, {5, 2}, {10, 3}, {15, 4}, {40, 4} };
        for (int[] pair : completedToMaterials) {
            StatsManager.setValue(player, StatsPM.RESEARCH_PROJECTS_COMPLETED, pair[0]);
            var project = template.initialize(player, Set.of());
            assertTrue(helper, project != null, "Failed to initialize a project with " + pair[0] + " projects completed");
            assertValueEqual(helper, pair[1], project.activeMaterials().size(), "Material count with " + pair[0] + " projects completed");
        }
        helper.succeed();
    }

    public static void research_projects_lose_base_success_chance_as_projects_are_completed(GameTestHelper helper) {
        var player = makeSurvivalPlayer(helper);
        var template = getTemplate(helper, ProjectTemplates.MUNDANE_TINKERING);

        // The base chance starts at 50% and drops by 10 points for every 3 projects completed, to a minimum of zero
        double[][] completedToChance = { {0, 0.5D}, {2, 0.5D}, {3, 0.4D}, {6, 0.3D}, {12, 0.1D}, {15, 0.0D}, {45, 0.0D} };
        for (double[] pair : completedToChance) {
            StatsManager.setValue(player, StatsPM.RESEARCH_PROJECTS_COMPLETED, (int)pair[0]);
            var project = template.initialize(player, Set.of());
            assertTrue(helper, project != null, "Failed to initialize a project with " + (int)pair[0] + " projects completed");
            assertTrue(helper, Math.abs(project.baseSuccessChance() - pair[1]) < TOLERANCE, "Base success chance with " + (int)pair[0] + " projects completed: expected " + pair[1] + " but was " + project.baseSuccessChance());
            assertTrue(helper, Math.abs(project.getSuccessChance() - pair[1]) < TOLERANCE, "Success chance with no materials selected and " + (int)pair[0] + " projects completed: expected " + pair[1] + " but was " + project.getSuccessChance());
        }
        helper.succeed();
    }

    private static ResearchTableMenu prepareCompletion(GameTestHelper helper, RecordingServerPlayer player, int paperCount) {
        var tile = placeTable(helper);
        tile.addItem(0, PENCIL_SLOT, new ItemStack(ItemsPM.ENCHANTED_INK_AND_QUILL.get()));
        tile.addItem(0, PAPER_SLOT, new ItemStack(Items.PAPER, paperCount));
        var menu = openTable(helper, player, tile);
        assertTrue(helper, menu.isWritingReady(), "Research table not ready for writing after loading it");
        assertValueEqual(helper, 0, tile.getItem(0, PENCIL_SLOT).getDamageValue(), "Ink and quill damage before the project");
        return menu;
    }

    public static void research_table_consumes_paper_and_ink_on_success(GameTestHelper helper) {
        var player = makeSurvivalPlayer(helper);
        var menu = prepareCompletion(helper, player, 3);
        var tile = helper.getBlockEntity(TABLE_POS, ResearchTableTileEntity.class);
        giveStickProject(helper, player, true);

        // A selected, satisfied material gives a 100% success chance, so any roll below 1.0 succeeds
        fixPlayerRandom(player, 0.0D);
        completeProject(player, menu);
        player.setForcedRandom(null);

        assertValueEqual(helper, 1, StatsManager.getValue(player, StatsPM.RESEARCH_PROJECTS_COMPLETED), "Projects completed after the project");
        assertValueEqual(helper, 2, tile.getItem(0, PAPER_SLOT).getCount(), "Paper remaining after a successful project");
        assertValueEqual(helper, 1, tile.getItem(0, PENCIL_SLOT).getDamageValue(), "Ink and quill damage after a successful project");
        helper.succeed();
    }

    public static void research_table_consumes_paper_and_ink_on_failure(GameTestHelper helper) {
        var player = makeSurvivalPlayer(helper);
        var menu = prepareCompletion(helper, player, 3);
        var tile = helper.getBlockEntity(TABLE_POS, ResearchTableTileEntity.class);
        giveStickProject(helper, player, false);

        // With the stick unselected the chance is the base 50%, so a roll of 0.99 fails
        fixPlayerRandom(player, 0.99D);
        completeProject(player, menu);
        player.setForcedRandom(null);

        assertValueEqual(helper, 0, StatsManager.getValue(player, StatsPM.RESEARCH_PROJECTS_COMPLETED), "Projects completed after a failed project");
        assertValueEqual(helper, 0, Services.CAPABILITIES.knowledge(player).map(k -> k.getKnowledgeRaw(KnowledgeType.THEORY)).orElse(-1), "Theory points after a failed project");
        assertValueEqual(helper, 2, tile.getItem(0, PAPER_SLOT).getCount(), "Paper remaining after a failed project");
        assertValueEqual(helper, 1, tile.getItem(0, PENCIL_SLOT).getDamageValue(), "Ink and quill damage after a failed project");
        helper.succeed();
    }

    public static void research_project_success_grants_listed_theory_progress(GameTestHelper helper) {
        var player = makeSurvivalPlayer(helper);
        var menu = prepareCompletion(helper, player, 1);
        var project = giveStickProject(helper, player, true);

        // The listed reward is 24 points: 32 points per level * (0.5 base multiplier + 0.25 bonus for the selected stick)
        assertValueEqual(helper, 24, project.getTheoryPointReward(), "Listed theory point reward");
        fixPlayerRandom(player, 0.0D);
        completeProject(player, menu);
        player.setForcedRandom(null);

        assertValueEqual(helper, 24, Services.CAPABILITIES.knowledge(player).map(k -> k.getKnowledgeRaw(KnowledgeType.THEORY)).orElse(-1), "Theory points after a successful project");
        helper.succeed();
    }
}
