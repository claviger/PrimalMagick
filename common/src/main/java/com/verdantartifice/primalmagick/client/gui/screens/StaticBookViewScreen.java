package com.verdantartifice.primalmagick.client.gui.screens;

import com.verdantartifice.primalmagick.client.books.ClientBookHelper;
import com.verdantartifice.primalmagick.client.gui.widgets.StaticBookPageButton;
import com.verdantartifice.primalmagick.common.books.BookType;
import com.verdantartifice.primalmagick.common.books.BookView;
import com.verdantartifice.primalmagick.common.books.LinguisticsManager;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.List;

/**
 * GUI screen for reading static books.
 * 
 * @author Daedalus4096
 */
public class StaticBookViewScreen extends Screen {
    protected static final Logger LOGGER = LogManager.getLogger();
    protected static final Style PAGE_TEXT_STYLE = Style.EMPTY.withoutShadow().withColor(Color.BLACK.getRGB());
    
    public static final int PAGE_INDICATOR_TEXT_Y_OFFSET = 16;
    public static final int PAGE_TEXT_X_OFFSET = 36;
    public static final int PAGE_TEXT_Y_OFFSET = 30;
    protected static final int TEXT_WIDTH = ClientBookHelper.TEXT_WIDTH;
    protected static final int TEXT_HEIGHT = ClientBookHelper.TEXT_HEIGHT;
    protected static final int LINE_HEIGHT = ClientBookHelper.LINE_HEIGHT;
    protected static final int IMAGE_WIDTH = 192;
    protected static final int IMAGE_HEIGHT = 192;
    protected static final int AUTO_TRANSLATE_DELAY_TICKS = 20;

    protected final boolean playTurnSound;
    protected final BookView requestedBookView;
    protected final BookType bookType;
    protected BookView actualBookView;
    protected boolean isAutoTranslating = false;
    protected int complexity;
    protected int ticksOpen = 0;
    private PageButton forwardButton;
    private PageButton backButton;
    private int currentPage;
    private int cachedPage = -1;
    private Component pageMsg = CommonComponents.EMPTY;

    public StaticBookViewScreen(BookView view, BookType bookType) {
        this(view, bookType, true);
    }
    
    private StaticBookViewScreen(BookView view, BookType bookType, boolean playTurnSound) {
        super(GameNarrator.NO_TITLE);
        this.playTurnSound = playTurnSound;
        this.requestedBookView = view;
        this.bookType = bookType;
    }
    
    /**
     * Moves the book to the specified page and returns true if it exists, {@code false} otherwise.
     */
    public boolean setPage(int newPage) {
        int clampedPage = Mth.clamp(newPage, 0, this.getNumPages() - 1);
        if (clampedPage != this.currentPage) {
            this.currentPage = clampedPage;
            this.updateButtonVisibility();
            this.cachedPage = -1;
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void init() {
        // Fetch language stats and determine actual comprehension of view
        int comp = LinguisticsManager.getComprehension(this.minecraft.player, this.requestedBookView.language());
        this.complexity = this.requestedBookView.language().value().complexity();
        this.isAutoTranslating = this.requestedBookView.language().value().autoTranslate();
        this.actualBookView = this.requestedBookView.withComprehension(Math.max(comp, this.requestedBookView.comprehension()));

        this.createMenuControls();
        this.createPageControlButtons();
    }

    @Override
    public void tick() {
        super.tick();
        this.ticksOpen++;
    }

    protected void createMenuControls() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).bounds(this.width / 2 - 100, IMAGE_HEIGHT + 4, IMAGE_WIDTH + 8, 20).build());
    }

    protected void createPageControlButtons() {
        int x = (this.width - IMAGE_WIDTH) / 2;
        this.forwardButton = this.addRenderableWidget(new StaticBookPageButton(x + 116, 159, true, (p_98297_) -> {
            this.pageForward();
        }, this.playTurnSound, this.bookType));
        this.backButton = this.addRenderableWidget(new StaticBookPageButton(x + 43, 159, false, (p_98287_) -> {
            this.pageBack();
        }, this.playTurnSound, this.bookType));
        this.updateButtonVisibility();
    }

    private int getNumPages() {
        return ClientBookHelper.getNumPages(this.actualBookView, this.font);
    }
    
    protected void pageBack() {
        if (this.currentPage > 0) {
            this.currentPage--;
        }
        this.updateButtonVisibility();
    }
    
    protected void pageForward() {
        if (this.currentPage < this.getNumPages() - 1) {
            this.currentPage++;
        }
        this.updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        this.forwardButton.visible = (this.currentPage < this.getNumPages() - 1);
        this.backButton.visible = (this.currentPage > 0);
    }

    @Override
    public boolean keyPressed(@NotNull KeyEvent pEvent) {
        if (super.keyPressed(pEvent)) {
            return true;
        } else {
            switch (pEvent.key()) {
            case GLFW.GLFW_KEY_PAGE_UP:
                this.backButton.onPress(pEvent);
                return true;
            case GLFW.GLFW_KEY_PAGE_DOWN:
                this.forwardButton.onPress(pEvent);
                return true;
            default:
                return false;
            }
        }
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        this.visitText(guiGraphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR));
    }

    protected void visitText(ActiveTextCollector collector) {
        int xPos = (this.width - IMAGE_WIDTH) / 2;
        int yPos = 2;

        if (this.cachedPage != this.currentPage) {
            // Set the page indicator text
            this.pageMsg = Component.translatable("book.pageIndicator", this.currentPage + 1, Math.max(this.getNumPages(), 1)).withStyle(PAGE_TEXT_STYLE);
        }
        
        this.cachedPage = this.currentPage;
        
        // Draw the page indicator text
        collector.accept(TextAlignment.RIGHT, xPos + IMAGE_WIDTH - 44, PAGE_INDICATOR_TEXT_Y_OFFSET + 2, this.pageMsg);

        // Draw the text lines for the current page
        BookView currentView = this.isAutoTranslating ? this.actualBookView.withComprehension(Mth.clamp(this.ticksOpen - AUTO_TRANSLATE_DELAY_TICKS, 0, this.complexity)) : this.actualBookView;
        List<FormattedCharSequence> page = ClientBookHelper.getTextPage(currentView, this.cachedPage, this.font);
        for (int index = 0; index < page.size(); index++) {
            int finalX = xPos + PAGE_TEXT_X_OFFSET;
            int finalY = yPos + PAGE_TEXT_Y_OFFSET + (index * LINE_HEIGHT);
            collector.accept(finalX, finalY, withPageStyle(page.get(index)));
        }
    }
    
    protected static FormattedCharSequence withPageStyle(FormattedCharSequence line) {
        // Fill in the page's base color and shadow for any character the book's own styling leaves unset
        return sink -> line.accept((position, style, codePoint) -> sink.accept(position, style.applyTo(PAGE_TEXT_STYLE), codePoint));
    }
    
    @Override
    public void extractBackground(@NotNull GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.extractBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        
        int xPos = (this.width - IMAGE_WIDTH) / 2;
        int yPos = 2;
        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, ClientBookHelper.getSprites(this.bookType).background(), xPos, yPos, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, 256, 256);
    }
}
