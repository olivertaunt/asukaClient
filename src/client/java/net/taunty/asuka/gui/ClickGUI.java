package net.taunty.asuka.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.taunty.asuka.highlight.HandheldAnimation;
import net.taunty.asuka.keybinding.ModKeybinds;
import net.taunty.asuka.player.GhostBlock;
import net.taunty.asuka.player.freecam.Freecam;

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
    private static final int SETTINGS_WIDTH = 188;

    private String expandedModule = null;
    private boolean draggingSlider = false;


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

    private ButtonBounds getModuleButton(int row, int column) {
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;

        int buttonX = panelX + SIDEBAR_WIDTH + 10 + column * MODULE_LIST_WIDTH;
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
        ButtonBounds boundsNE = getModuleButton(0, 0);
        ButtonBounds boundsHA = getModuleButton(1, 0);
        ButtonBounds boundsGB = getModuleButton(0, 0);
        ButtonBounds boundsFC = getModuleButton(0, 0);

        boolean nodeesp = ModKeybinds.isEnabledNodeESP();
        boolean ghostBlock = GhostBlock.get().isEnabled();
        boolean freecam = Freecam.isEnabled();
        boolean swingAnimation = HandheldAnimation.isEnabled();

        if (selectedCategory == Category.VISUAL) {
            drawModuleButton(graphics, boundsNE, "Block ESP", nodeesp, mouseX, mouseY);
            drawModuleButton(graphics, boundsHA, "Swing Animation", swingAnimation, mouseX, mouseY);
        } else if (selectedCategory == Category.PLAYER) {
            drawModuleButton(graphics, boundsFC, "Freecam", freecam, mouseX, mouseY);
        } else if (selectedCategory == Category.WORLD) {
            drawModuleButton(graphics, boundsGB, "Ghost Block", ghostBlock, mouseX, mouseY);
        }
    }

    private void updateSlider(double mouseX) {
        int panelX = (width - PANEL_WIDTH) / 2;
        int settingsX = panelX + SIDEBAR_WIDTH + MODULE_LIST_WIDTH + 1;
        int sliderX = settingsX + 10;

        float progress = (float) (mouseX - sliderX) / SLIDER_WIDTH;
        progress = Math.clamp(progress, 0.0f, 1.0f);

        animationSpeed = MIN_SPEED + progress * (MAX_SPEED - MIN_SPEED);
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

        ButtonBounds boundsNE = getModuleButton(0, 0);
        ButtonBounds boundsHA = getModuleButton(1, 0);
        ButtonBounds boundsGB = getModuleButton(0, 0);
        ButtonBounds boundsFC = getModuleButton(0, 0);
        for (Category category : Category.values()) {
            ButtonBounds categoryBounds = getCategoryButton(category.ordinal());
            if (categoryBounds.contains(mouseX, mouseY)) {
                selectedCategory = category;
                return true;
            }
        }

        if (event.button() == 0 && expandedModule != null && expandedModule.equals("Swing Animation")) {
            int panelX = (width - PANEL_WIDTH) / 2;
            int panelY = (height - PANEL_HEIGHT) / 2;
            int settingsX = panelX + SIDEBAR_WIDTH + MODULE_LIST_WIDTH + 1;
            int sliderX = settingsX + 10;
            int sliderY = panelY + HEADER_HEIGHT + 50;

            if (mouseX >= sliderX && mouseX <= sliderX + SLIDER_WIDTH && mouseY >= sliderY - 4 && mouseY <= sliderY + 8) {
                draggingSlider = true;
                updateSlider(mouseX);
                return true;
            }
        }

        if (event.button() == 0) {
            if (boundsNE.contains(mouseX, mouseY) && selectedCategory == Category.VISUAL) {
                ModKeybinds.toggleNodeESP();
                return true;
            }
            if (boundsHA.contains(mouseX, mouseY) && selectedCategory == Category.VISUAL) {
                HandheldAnimation.toggle();
                return true;
            }
            if (boundsGB.contains(mouseX, mouseY) && selectedCategory == Category.WORLD) {
                GhostBlock.get().toggle();
                return true;
            }
            if (boundsFC.contains(mouseX, mouseY) && selectedCategory == Category.PLAYER) {
                Freecam.toggle();
                return true;
            }
        } else if (event.button() == 1){
            if (boundsNE.contains(mouseX, mouseY) && selectedCategory == Category.VISUAL) {
                expandedModule = "Block ESP".equals(expandedModule) ? null : "Block ESP";
                return true;
            }
            if (boundsHA.contains(mouseX, mouseY) && selectedCategory == Category.VISUAL) {
                expandedModule = "Swing Animation".equals(expandedModule) ? null : "Swing Animation";
                return true;
            }
            if (boundsGB.contains(mouseX, mouseY) && selectedCategory == Category.WORLD) {
                GhostBlock.get().toggle();
                return true;
            }
            if (boundsFC.contains(mouseX, mouseY) && selectedCategory == Category.PLAYER) {
                Freecam.toggle();
                return true;
            }
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
        graphics.fill(panelX, panelY + HEADER_HEIGHT, panelX + PANEL_WIDTH, panelY + HEADER_HEIGHT + 1, BORDER_COLOUR);
        graphics.text(this.font, "Asuka", panelX + 10, panelY + 10, ACCENT_COLOUR, false);
    }

    private void drawModuleButton(GuiGraphicsExtractor graphics, ButtonBounds bounds, String label, boolean enabled, int mouseX, int mouseY) {
        boolean hovered = bounds.contains(mouseX, mouseY);
        int colour = enabled || hovered ? ACCENT_COLOUR : 0xFF303040;
        graphics.fill(bounds.x(), bounds.y(), bounds.x() + bounds.width(), bounds.y() + bounds.height(), colour);
        graphics.text(this.font, label, bounds.x() + 8, bounds.y() + 6, 0xFFFFFFFF, false);
    }

    private void drawExpandedBackground(GuiGraphicsExtractor graphics) {
        if (expandedModule == null) {
            return;
        }

        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;

        int settingsX = panelX + SIDEBAR_WIDTH + MODULE_LIST_WIDTH + 1;
        int settingsY = panelY + HEADER_HEIGHT + 1;
        int settingsHeight = PANEL_HEIGHT - HEADER_HEIGHT - 2;
        graphics.fill(settingsX, settingsY, settingsX + SETTINGS_WIDTH, settingsY + settingsHeight, 0xFF14141C);

        if (expandedModule.equals("Swing Animation")) {
            graphics.text(this.font, "Animation Speed", settingsX + 10, settingsY + 25, 0xFFFFFFFF, false);
            drawSlider(graphics, settingsX + 10, settingsY + 50, 140, HandheldAnimation.getSwingDuration(), 0.1f, 2.0f);
        }
    }

    private void drawSlider(GuiGraphicsExtractor graphics, int x, int y, int sliderWidth, float value, float min, float max) {
        float progress = (value - min) / (max - min);
        graphics.fill(x, y, x + sliderWidth, y + 4, 0xFF303040);
        graphics.fill(x, y, x + (int)(progress * sliderWidth), y + 4, ACCENT_COLOUR);

        int knobX = x + (int)(progress * sliderWidth);
        graphics.fill(knobX - 3, y - 3, knobX + 4, y + 7, 0xFFFFFFFF);
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
        if (expandedModule != null) {
            drawExpandedBackground(graphics);
        }
    }
}
