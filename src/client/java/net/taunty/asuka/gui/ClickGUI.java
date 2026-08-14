package net.taunty.asuka.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.taunty.asuka.keybinding.ModKeybinds;
import net.taunty.asuka.player.GhostBlock;

public class ClickGUI extends Screen {

    private static final int BACKGROUND_COLOUR = 0xE0101018;
    private static final int ACCENT_COLOUR = 0xFF7A5AC7;


    public ClickGUI(Component title) {
        super(title);
    }

    @Override
    protected void init() {
    }

    private record ButtonBounds(int x, int y, int width, int height) {}

    /**
     * it's what the method says.
     * @return bounds object
     */
    private ButtonBounds getNodeESPButton() {
        int panelX = width / 2 - 145;
        int panelY = height / 2 - 60;
        int btnX = panelX + 10, btnY = panelY + 40, btnW = 75, btnH = 20;
        return new ButtonBounds(btnX, btnY, btnW, btnH);
    }

    private ButtonBounds getGhostBlockButton() {
        int panelX = width / 2 - 145;
        int panelY = height / 2 - 85;
        int btnX = panelX + 10, btnY = panelY + 40, btnW = 75, btnH = 20;
        return new ButtonBounds(btnX, btnY, btnW, btnH);
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

        ButtonBounds boundsNE = getNodeESPButton();
        ButtonBounds boundsGB = getGhostBlockButton();
        if (mouseX >= boundsNE.x() && mouseX <= boundsNE.x() + boundsNE.width() && mouseY >= boundsNE.y() && mouseY <= boundsNE.y() + boundsNE.height()) {
            ModKeybinds.toggleNodeESP();
            return true;
        }
        if (mouseX >= boundsGB.x() && mouseX <= boundsGB.x() + boundsGB.width() && mouseY >= boundsGB.y() && mouseY <= boundsGB.y() + boundsGB.height()) {
            GhostBlock.get().toggle();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta){
        ButtonBounds boundsNE = getNodeESPButton();
        ButtonBounds boundsGB = getGhostBlockButton();
        boolean NodeESP = ModKeybinds.isEnabledNodeESP();
        boolean ghostBlock = GhostBlock.get().isEnabled();

        int panelX = width / 2 - 150;
        int panelY = height / 2 - 90;
        int panelW = 300;
        int panelH = 180;

        // background :p
        graphics.fill(panelX, panelY, panelX + panelW, panelY+panelH, BACKGROUND_COLOUR);

        // border
        graphics.fill(panelX, panelY, panelX + panelW, panelY + 2, ACCENT_COLOUR);

        graphics.text(this.font, "Asuka", panelX + 10, panelY + 10, ACCENT_COLOUR, false);

        boolean hoveredNE = mouseX >= boundsNE.x() && mouseX <= boundsNE.x() + boundsNE.width() && mouseY >= boundsNE.y() && mouseY <= boundsNE.y() + boundsNE.height();
        boolean hoveredGB = mouseX >= boundsGB.x() && mouseX <= boundsGB.x() + boundsGB.width() && mouseY >= boundsGB.y() && mouseY <= boundsGB.y() + boundsGB.height();

        graphics.fill(boundsNE.x(), boundsNE.y(), boundsNE.x() + boundsNE.width(), boundsNE.y() + boundsNE.height(), hoveredNE ? ACCENT_COLOUR : 0xFF303040);
        graphics.fill(boundsNE.x(), boundsNE.y(), boundsNE.x() + boundsNE.width(), boundsNE.y() + boundsNE.height(), NodeESP ? ACCENT_COLOUR : 0xFF303040);
        graphics.fill(boundsGB.x(), boundsGB.y(), boundsGB.x() + boundsGB.width(), boundsGB.y() + boundsGB.height(), hoveredGB ? ACCENT_COLOUR : 0xFF303040);
        graphics.fill(boundsGB.x(), boundsGB.y(), boundsGB.x() + boundsGB.width(), boundsGB.y() + boundsGB.height(), ghostBlock ? ACCENT_COLOUR : 0xFF303040);

        graphics.text(this.font, "Node ESP", boundsNE.x() + 8, boundsNE.y() + 6, 0xFFFFFFFF, false);
        graphics.text(this.font, "Ghost Block", boundsGB.x() + 8, boundsGB.y() + 6, 0xFFFFFFFF, false);
    }
}
