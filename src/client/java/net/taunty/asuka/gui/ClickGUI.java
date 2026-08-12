package net.taunty.asuka.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.taunty.asuka.keybinding.ModKeybinds;

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
    private ButtonBounds getButtonBounds() {
        int panelX = width / 2 - 100;
        int panelY = height / 2 - 60;
        int btnX = panelX + 10, btnY = panelY + 40, btnW = 180, btnH = 20;
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

        ButtonBounds bounds = getButtonBounds();
        if (mouseX >= bounds.x() && mouseX <= bounds.x() + bounds.width() && mouseY >= bounds.y() && mouseY <= bounds.y() + bounds.height()) {
            ModKeybinds.toggleNodeESP();

            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta){
        ButtonBounds bounds = getButtonBounds();
        boolean NodeESP = ModKeybinds.isEnabledNodeESP();

        int panelX = width / 2 - 150;
        int panelY = height / 2 - 90;
        int panelW = 300;
        int panelH = 180;

        // background :p
        graphics.fill(panelX, panelY, panelX + panelW, panelY+panelH, BACKGROUND_COLOUR);

        // border
        graphics.fill(panelX, panelY, panelX + panelW, panelY + 2, ACCENT_COLOUR);

        graphics.text(this.font, "Asuka", panelX + 10, panelY + 10, ACCENT_COLOUR, false);

        boolean hovered = mouseX >= bounds.x() && mouseX <= bounds.x() + bounds.width() && mouseY >= bounds.y() && mouseY <= bounds.y() + bounds.height();

        graphics.fill(bounds.x(), bounds.y(), bounds.x() + bounds.width(), bounds.y() + bounds.height(), hovered ? ACCENT_COLOUR : 0xFF303040);
        graphics.fill(bounds.x(), bounds.y(), bounds.x() + bounds.width(), bounds.y() + bounds.height(), NodeESP ? ACCENT_COLOUR : 0xFF303040);

        graphics.text(this.font, "Toggle Node ESP", bounds.x() + 8, bounds.y() + 6, 0xFFFFFFFF, false);
    }
}
