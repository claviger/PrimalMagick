package com.verdantartifice.primalmagick.test.linguistics;

import com.verdantartifice.primalmagick.common.blocks.BlocksPM;
import com.verdantartifice.primalmagick.common.books.BookLanguagesPM;
import com.verdantartifice.primalmagick.common.books.BooksPM;
import com.verdantartifice.primalmagick.common.books.LinguisticsManager;
import com.verdantartifice.primalmagick.common.books.ScribeTableMode;
import com.verdantartifice.primalmagick.common.items.ItemsPM;
import com.verdantartifice.primalmagick.common.items.books.StaticBookItem;
import com.verdantartifice.primalmagick.common.menus.AbstractScribeTableMenu;
import com.verdantartifice.primalmagick.common.menus.ScribeGainComprehensionMenu;
import com.verdantartifice.primalmagick.common.menus.ScribeStudyVocabularyMenu;
import com.verdantartifice.primalmagick.common.menus.ScribeTranscribeWorksMenu;
import com.verdantartifice.primalmagick.common.network.packets.scribe_table.ChangeScribeTableModePacket;
import com.verdantartifice.primalmagick.common.network.packets.scribe_table.UnlockGridNodeActionPacket;
import com.verdantartifice.primalmagick.common.research.KnowledgeType;
import com.verdantartifice.primalmagick.common.tiles.devices.ScribeTableTileEntity;
import com.verdantartifice.primalmagick.common.util.ResourceUtils;
import com.verdantartifice.primalmagick.platform.Services;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import com.verdantartifice.primalmagick.test.RecordingServerPlayer;
import commonnetwork.networking.data.PacketContext;
import commonnetwork.networking.data.Side;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import org.joml.Vector2i;

import com.verdantartifice.primalmagick.common.books.BookLanguage;

/**
 * Tests for the scribe table: its remembered mode and retained inventory, and the study vocabulary, gain
 * comprehension and transcribe works modes. Costs are hard-coded from ScribeStudyVocabularyMenu (25, 100 and 300 XP
 * points per slot, each also paying for the unstudied slots before it, with no bookshelves nearby) and node rewards
 * from the earth linguistics grid (start node at 3,7 grants 1 earth comprehension; the node at 4,7 grants one level of
 * observation knowledge; each costs 1 vocabulary).
 */
public class ScribeTableTests extends AbstractBaseTest {
    private static final BlockPos TABLE_POS = BlockPos.ZERO.above();
    private static final Identifier EARTH_GRID = ResourceUtils.loc("earth");
    private static final Vector2i START_NODE = new Vector2i(3, 7);
    private static final Vector2i OBSERVATION_NODE = new Vector2i(4, 7);

    private static ScribeTableTileEntity placeTable(GameTestHelper helper) {
        helper.setBlock(TABLE_POS, BlocksPM.SCRIBE_TABLE.get());
        return helper.getBlockEntity(TABLE_POS, ScribeTableTileEntity.class);
    }

    private static ItemStack makeBook(GameTestHelper helper, ResourceKey<BookLanguage> language) {
        return StaticBookItem.builder(ItemsPM.STATIC_BOOK::get, helper.getLevel().registryAccess()).book(BooksPM.FIVE_CULTURES_EARTH_PART_1).language(language).build();
    }

    private static Holder<BookLanguage> earth(GameTestHelper helper) {
        return BookLanguagesPM.getLanguageOrThrow(BookLanguagesPM.EARTH, helper.getLevel().registryAccess());
    }

    private static RecordingServerPlayer makeSurvivalPlayer(GameTestHelper helper) {
        var player = RecordingServerPlayer.create(helper, false);
        player.setGameMode(GameType.SURVIVAL);
        return player;
    }

    private static <T extends AbstractScribeTableMenu> T open(GameTestHelper helper, RecordingServerPlayer player, ScribeTableTileEntity tile, Class<T> menuClass) {
        player.openMenu(tile);
        return assertInstanceOf(helper, player.containerMenu, menuClass, "Opened menu not of expected type, was " + player.containerMenu);
    }

    private static void changeMode(RecordingServerPlayer player, ScribeTableMode mode) {
        ChangeScribeTableModePacket.onMessage(new PacketContext<>(player, new ChangeScribeTableModePacket(player.containerMenu.containerId, mode), Side.SERVER));
    }

    public static void scribe_table_remembers_last_mode(GameTestHelper helper) {
        var player = RecordingServerPlayer.create(helper, false);
        var tile = placeTable(helper);

        // A new player's table opens to vocabulary study
        open(helper, player, tile, ScribeStudyVocabularyMenu.class);

        // Switch to each other mode, then close and reopen the table to see that it opens to the same one
        changeMode(player, ScribeTableMode.TRANSCRIBE_WORKS);
        assertInstanceOf(helper, player.containerMenu, ScribeTranscribeWorksMenu.class, "Menu after switching to transcribe works");
        player.closeContainer();
        open(helper, player, tile, ScribeTranscribeWorksMenu.class);

        changeMode(player, ScribeTableMode.GAIN_COMPREHENSION);
        assertInstanceOf(helper, player.containerMenu, ScribeGainComprehensionMenu.class, "Menu after switching to gain comprehension");
        player.closeContainer();
        open(helper, player, tile, ScribeGainComprehensionMenu.class);
        assertValueEqual(helper, ScribeTableMode.GAIN_COMPREHENSION, LinguisticsManager.getScribeTableMode(player), "Stored table mode");
        helper.succeed();
    }

    public static void scribe_table_retains_inventory_between_openings_and_modes(GameTestHelper helper) {
        var player = RecordingServerPlayer.create(helper, false);
        var tile = placeTable(helper);
        assertTrue(helper, tile.addItem(0, 0, makeBook(helper, BookLanguagesPM.EARTH)).isEmpty(), "Ancient book not accepted by the table");
        assertTrue(helper, tile.addItem(0, 1, new ItemStack(Items.WRITABLE_BOOK)).isEmpty(), "Book and quill not accepted by the table");

        var study = open(helper, player, tile, ScribeStudyVocabularyMenu.class);
        assertTrue(helper, study.getSlot(0).getItem().is(ItemsPM.STATIC_BOOK.get()), "Book missing from study mode");

        changeMode(player, ScribeTableMode.TRANSCRIBE_WORKS);
        var transcribe = assertInstanceOf(helper, player.containerMenu, ScribeTranscribeWorksMenu.class, "Menu after switching modes");
        assertTrue(helper, transcribe.getSlot(1).getItem().is(ItemsPM.STATIC_BOOK.get()), "Book missing from transcribe mode");
        assertTrue(helper, transcribe.getSlot(2).getItem().is(Items.WRITABLE_BOOK), "Book and quill missing from transcribe mode");

        player.closeContainer();
        var reopened = open(helper, player, tile, ScribeTranscribeWorksMenu.class);
        assertTrue(helper, reopened.getSlot(1).getItem().is(ItemsPM.STATIC_BOOK.get()), "Book missing after reopening");
        assertTrue(helper, reopened.getSlot(2).getItem().is(Items.WRITABLE_BOOK), "Book and quill missing after reopening");
        helper.succeed();
    }

    public static void study_vocabulary_requires_ancient_book(GameTestHelper helper) {
        var player = makeSurvivalPlayer(helper);
        player.giveExperiencePoints(1000);
        var tile = placeTable(helper);
        var menu = open(helper, player, tile, ScribeStudyVocabularyMenu.class);

        // Nothing can be studied without a book
        assertFalse(helper, menu.checkStudyClick(player, 0), "Study allowed with an empty slot");

        // The slot rejects anything that isn't an ancient-language book
        assertFalse(helper, menu.getSlot(0).mayPlace(new ItemStack(Items.BOOK)), "Slot accepted a vanilla book");
        assertFalse(helper, menu.getSlot(0).mayPlace(makeBook(helper, BookLanguagesPM.DEFAULT)), "Slot accepted a book in the default language");
        assertTrue(helper, menu.getSlot(0).mayPlace(makeBook(helper, BookLanguagesPM.EARTH)), "Slot rejected an ancient book");

        menu.getSlot(0).safeInsert(makeBook(helper, BookLanguagesPM.EARTH));
        assertTrue(helper, menu.checkStudyClick(player, 0), "Study not allowed with an ancient book and enough XP");
        helper.succeed();
    }

    public static void study_vocabulary_charges_listed_cost(GameTestHelper helper) {
        var player = makeSurvivalPlayer(helper);
        player.giveExperiencePoints(500);
        var tile = placeTable(helper);
        var menu = open(helper, player, tile, ScribeStudyVocabularyMenu.class);
        menu.getSlot(0).safeInsert(makeBook(helper, BookLanguagesPM.EARTH));

        // The first slot costs 25 XP points
        assertValueEqual(helper, 25, menu.costs[0], "Listed cost of the first slot");
        int xpBefore = player.totalExperience;
        menu.doStudyClick(player, 0);
        assertValueEqual(helper, xpBefore - 25, player.totalExperience, "Player XP after studying");
        helper.succeed();
    }

    public static void study_vocabulary_grants_vocabulary_for_book_language(GameTestHelper helper) {
        var player = makeSurvivalPlayer(helper);
        player.giveExperiencePoints(500);
        var tile = placeTable(helper);
        var menu = open(helper, player, tile, ScribeStudyVocabularyMenu.class);
        menu.getSlot(0).safeInsert(makeBook(helper, BookLanguagesPM.EARTH));
        assertValueEqual(helper, 0, LinguisticsManager.getVocabulary(player, earth(helper)), "Starting earth vocabulary");

        menu.doStudyClick(player, 0);

        assertValueEqual(helper, 1, LinguisticsManager.getVocabulary(player, earth(helper)), "Earth vocabulary after studying");
        assertValueEqual(helper, 0, LinguisticsManager.getVocabulary(player, BookLanguagesPM.getLanguageOrThrow(BookLanguagesPM.SEA, helper.getLevel().registryAccess())), "Sea vocabulary after studying an earth book");
        helper.succeed();
    }

    public static void book_can_be_studied_at_most_three_times(GameTestHelper helper) {
        var player = makeSurvivalPlayer(helper);
        player.giveExperiencePoints(5000);
        var tile = placeTable(helper);
        var menu = open(helper, player, tile, ScribeStudyVocabularyMenu.class);
        menu.getSlot(0).safeInsert(makeBook(helper, BookLanguagesPM.EARTH));

        // Study the three slots in turn, each of which gives one point of vocabulary
        for (int slot = 0; slot < 3; slot++) {
            assertTrue(helper, menu.checkStudyClick(player, slot), "Study of slot " + slot + " not allowed");
            menu.doStudyClick(player, slot);
            assertValueEqual(helper, slot + 1, LinguisticsManager.getVocabulary(player, earth(helper)), "Earth vocabulary after studying slot " + slot);
        }

        // Every slot is now spent
        for (int slot = 0; slot < 3; slot++) {
            assertFalse(helper, menu.checkStudyClick(player, slot), "Study of slot " + slot + " allowed after three studies");
            menu.doStudyClick(player, slot);
        }
        assertValueEqual(helper, 3, LinguisticsManager.getVocabulary(player, earth(helper)), "Earth vocabulary after refused fourth study");
        helper.succeed();
    }

    private static void unlock(RecordingServerPlayer player, Vector2i node) {
        UnlockGridNodeActionPacket.onMessage(new PacketContext<>(player, new UnlockGridNodeActionPacket(EARTH_GRID, node), Side.SERVER));
    }

    private static boolean isUnlocked(RecordingServerPlayer player, Vector2i node) {
        return Services.CAPABILITIES.linguistics(player).map(l -> l.getUnlockedNodes(EARTH_GRID).contains(node)).orElse(false);
    }

    public static void claiming_node_requires_vocabulary_in_grid_language(GameTestHelper helper) {
        var player = makeSurvivalPlayer(helper);
        var sea = BookLanguagesPM.getLanguageOrThrow(BookLanguagesPM.SEA, helper.getLevel().registryAccess());

        // No vocabulary at all, or vocabulary in the wrong language, is not enough to claim an earth node
        unlock(player, START_NODE);
        assertFalse(helper, isUnlocked(player, START_NODE), "Node claimed with no vocabulary");
        LinguisticsManager.setVocabulary(player, sea, 5);
        unlock(player, START_NODE);
        assertFalse(helper, isUnlocked(player, START_NODE), "Node claimed with only sea vocabulary");

        // One point of earth vocabulary pays for the node
        LinguisticsManager.setVocabulary(player, earth(helper), 1);
        unlock(player, START_NODE);
        assertTrue(helper, isUnlocked(player, START_NODE), "Node not claimed with earth vocabulary");
        assertValueEqual(helper, 0, LinguisticsManager.getVocabulary(player, earth(helper)), "Earth vocabulary after claiming");
        helper.succeed();
    }

    public static void claiming_node_grants_listed_reward(GameTestHelper helper) {
        var player = makeSurvivalPlayer(helper);
        LinguisticsManager.setVocabulary(player, earth(helper), 2);
        var knowledge = Services.CAPABILITIES.knowledge(player).orElseThrow();

        // The start node grants one point of earth comprehension
        assertValueEqual(helper, 0, LinguisticsManager.getComprehension(player, earth(helper)), "Starting earth comprehension");
        unlock(player, START_NODE);
        assertValueEqual(helper, 1, LinguisticsManager.getComprehension(player, earth(helper)), "Earth comprehension after claiming the start node");

        // The adjacent node grants one level of observation knowledge, which is 16 points
        assertValueEqual(helper, 0, knowledge.getKnowledgeRaw(KnowledgeType.OBSERVATION), "Starting observation points");
        unlock(player, OBSERVATION_NODE);
        assertValueEqual(helper, 16, knowledge.getKnowledgeRaw(KnowledgeType.OBSERVATION), "Observation points after claiming the observation node");
        helper.succeed();
    }

    public static void transcribe_works_requires_ancient_book_and_writable_book(GameTestHelper helper) {
        var player = RecordingServerPlayer.create(helper, false);
        var tile = placeTable(helper);
        LinguisticsManager.setScribeTableMode(player, ScribeTableMode.TRANSCRIBE_WORKS);
        var menu = open(helper, player, tile, ScribeTranscribeWorksMenu.class);

        // The slots reject the wrong items
        assertFalse(helper, menu.getSlot(1).mayPlace(new ItemStack(Items.BOOK)), "Original slot accepted a vanilla book");
        assertFalse(helper, menu.getSlot(2).mayPlace(makeBook(helper, BookLanguagesPM.EARTH)), "Blank slot accepted an ancient book");
        assertTrue(helper, menu.getSlot(2).mayPlace(new ItemStack(Items.WRITABLE_BOOK)), "Blank slot rejected a book and quill");

        // The ancient book alone produces nothing
        tile.addItem(0, 0, makeBook(helper, BookLanguagesPM.EARTH));
        menu.doTranscribe();
        assertTrue(helper, tile.getItem(1, 0).isEmpty(), "Transcription produced a book without a book and quill");

        // With both, a copy one generation removed is produced and the book and quill is used up
        tile.addItem(0, 1, new ItemStack(Items.WRITABLE_BOOK));
        menu.doTranscribe();
        assertTrue(helper, tile.getItem(1, 0).is(ItemsPM.STATIC_BOOK.get()), "Transcription did not produce a book");
        assertValueEqual(helper, 1, StaticBookItem.getGeneration(tile.getItem(1, 0)), "Generation of the transcribed book");
        assertTrue(helper, tile.getItem(0, 1).isEmpty(), "Book and quill not used up by transcription");
        assertTrue(helper, tile.getItem(0, 0).is(ItemsPM.STATIC_BOOK.get()), "Original book consumed by transcription");

        // A lone book and quill with no book to copy produces nothing
        var emptyTile = tile;
        emptyTile.removeItem(0, 0, 1);
        emptyTile.addItem(0, 1, new ItemStack(Items.WRITABLE_BOOK));
        emptyTile.removeItem(1, 0, 1);
        menu.doTranscribe();
        assertTrue(helper, tile.getItem(1, 0).isEmpty(), "Transcription produced a book without a book to copy");
        helper.succeed();
    }

    public static void transcribed_works_keep_comprehension_after_reset(GameTestHelper helper) {
        var player = RecordingServerPlayer.create(helper, false);
        var tile = placeTable(helper);
        LinguisticsManager.setScribeTableMode(player, ScribeTableMode.TRANSCRIBE_WORKS);
        var menu = open(helper, player, tile, ScribeTranscribeWorksMenu.class);
        LinguisticsManager.setComprehension(player, earth(helper), 10);
        tile.addItem(0, 0, makeBook(helper, BookLanguagesPM.EARTH));
        tile.addItem(0, 1, new ItemStack(Items.WRITABLE_BOOK));

        menu.doTranscribe();
        var result = tile.getItem(1, 0);
        assertValueEqual(helper, 10, StaticBookItem.getTranslatedComprehension(result).orElse(-1), "Comprehension recorded on the transcribed book");

        // Reset the player's linguistics data, as /pm linguistics @s reset does; the book keeps its comprehension
        Services.CAPABILITIES.linguistics(player).orElseThrow().clear();
        assertValueEqual(helper, 0, LinguisticsManager.getComprehension(player, earth(helper)), "Player comprehension after reset");
        assertValueEqual(helper, 10, StaticBookItem.getTranslatedComprehension(tile.getItem(1, 0)).orElse(-1), "Comprehension on the transcribed book after reset");
        helper.succeed();
    }
}
