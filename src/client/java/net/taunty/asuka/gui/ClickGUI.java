package net.taunty.asuka.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.taunty.asuka.keybinding.ModKeybinds;
import net.taunty.asuka.player.GhostBlock;

import java.awt.*;

public class ClickGUI extends Screen {

    private static final int BACKGROUND_COLOUR = 0xE0101018;
    private static final int ACCENT_COLOUR = 0xFF7A5AC7;
    private static final int BORDER_COLOUR = 0xFF454052;

    private static final int PANEL_WIDTH = 440;
    private static final int PANEL_HEIGHT = 240;
    private static final int HEADER_HEIGHT = 24;
    private static final int SIDEBAR_WIDTH = 85;
    private static final int MODULE_LIST_WIDTH = 165;


    private enum Category {
        VISUAL, PLAYER, WORLD
    }

    private Category selectedCategory = Category.VISUAL;


    public ClickGUI(Component title) {
        super(title);
    }

    @Override
    protected void init() {
    }

    private record ButtonBounds(int x, int y, int width, int height) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }

    private ButtonBounds getModuleButton(int row) {
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;
        int buttonX = panelX + SIDEBAR_WIDTH + 10;
        int buttonY = panelY + HEADER_HEIGHT + 10 + row * 25;
        return new ButtonBounds(buttonX, buttonY, MODULE_LIST_WIDTH - 20, 20);
    }

    private ButtonBounds getCategoryButton(int row) {
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;
        int category = panelY + HEADER_HEIGHT + 8 + row * 26;
        return new ButtonBounds(panelX + 1, category, SIDEBAR_WIDTH - 1, 26);
    }

    private void drawModules(GuiGraphicsExtractor graphics, Category selectedCategory, int mouseX, int mouseY) {
        ButtonBounds boundsNE = getModuleButton(0);
        ButtonBounds boundsGB = getModuleButton(0);

        boolean nodeEspEnabled = ModKeybinds.isEnabledNodeESP();
        boolean ghostBlock = GhostBlock.get().isEnabled();

        if (selectedCategory == Category.VISUAL) {
            drawModuleButton(graphics, boundsNE, "Node ESP", nodeEspEnabled, mouseX, mouseY);
        } else if (selectedCategory == Category.PLAYER) {
            //
        } else if (selectedCategory == Category.WORLD) {
            drawModuleButton(graphics, boundsGB, "Ghost Block", ghostBlock, mouseX, mouseY);
        }
    }

    /**
     * Checks whether mouse clicks inside box and toggles module
     * @param event;
     * @param doubleClick;
     */
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick){
        double mouseX = event.x();
        double mouseY = event.y();

        ButtonBounds boundsNE = getModuleButton(1);
        ButtonBounds boundsGB = getModuleButton(0);
        for (Category category : Category.values()) {
            ButtonBounds categoryBounds = getCategoryButton(category.ordinal());
            if (categoryBounds.contains(mouseX, mouseY)) {
                selectedCategory = category;
                return true;
            }
        }
        if (boundsNE.contains(mouseX, mouseY) && selectedCategory == Category.VISUAL) {
            ModKeybinds.toggleNodeESP();
            return true;
        }
        if (boundsGB.contains(mouseX, mouseY) && selectedCategory == Category.WORLD) {
            GhostBlock.get().toggle();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void drawGuiComponents(GuiGraphicsExtractor graphics, int panelX, int panelY) {
        int sidebarEnd = panelX + SIDEBAR_WIDTH;
        int modulesEnd = sidebarEnd + MODULE_LIST_WIDTH;

        // window
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, BACKGROUND_COLOUR);
        // header
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + HEADER_HEIGHT, 0xFF181820);
        // sidebar
        graphics.fill(panelX, panelY + HEADER_HEIGHT, panelX + SIDEBAR_WIDTH, panelY + PANEL_HEIGHT, 0xFF14141C);
        // moduleList
        graphics.fill(panelX + SIDEBAR_WIDTH, panelY + HEADER_HEIGHT, panelX + SIDEBAR_WIDTH + MODULE_LIST_WIDTH, panelY + PANEL_HEIGHT, 0xFF181820);
        // border
        graphics.outline(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, BORDER_COLOUR);
        graphics.fill(sidebarEnd, panelY + HEADER_HEIGHT, sidebarEnd + 1, panelY + PANEL_HEIGHT, BORDER_COLOUR);
        graphics.fill(modulesEnd, panelY + HEADER_HEIGHT, modulesEnd + 1, panelY + PANEL_HEIGHT, BORDER_COLOUR);
        graphics.text(this.font, "Asuka", panelX + 10, panelY + 10, ACCENT_COLOUR, false);
    }

    private void drawModuleButton(GuiGraphicsExtractor graphics, ButtonBounds bounds, String label, boolean enabled, int mouseX, int mouseY) {
        boolean hovered = bounds.contains(mouseX, mouseY);
        int colour = enabled || hovered ? ACCENT_COLOUR : 0xFF303040;
        graphics.fill(bounds.x(), bounds.y(), bounds.x() + bounds.width(), bounds.y() + bounds.height(), colour);
        graphics.text(this.font, label, bounds.x() + 8, bounds.y() + 6, 0xFFFFFFFF, false);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta){
        int panelY = (height - PANEL_HEIGHT) / 2;
        int panelX = (width - PANEL_WIDTH) / 2;


        drawGuiComponents(graphics, panelX, panelY);

        int categoryY = panelY + HEADER_HEIGHT + 8;
        int categoryH = 26;

        for (Category category : Category.values()) {
            ButtonBounds categoryBounds = getCategoryButton(selectedCategory.ordinal());
            if (category == selectedCategory) {
                graphics.fill(categoryBounds.x(), categoryBounds.y(), categoryBounds.width() + categoryBounds.x(), categoryBounds.height() + categoryBounds.y(), ACCENT_COLOUR);
            }
            graphics.text(this.font, category.name(), panelX + 10, categoryY + 9, 0xFFFFFFFF, false);
            categoryY += categoryH;
        }
        drawModules(graphics, selectedCategory, mouseX, mouseY);
    }
}
