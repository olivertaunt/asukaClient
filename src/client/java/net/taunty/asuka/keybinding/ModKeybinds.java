package net.taunty.asuka.keybinding;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.taunty.asuka.gui.ClickGUI;
import net.taunty.asuka.player.GhostBlock;
import net.taunty.asuka.player.freecam.Freecam;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds{
    private static KeyMapping nodeEspKey;
    private static KeyMapping clickGuiKey;
    private static KeyMapping ghostBlockToggle;
    private static KeyMapping ghostBlockKey;
    private static KeyMapping freecamKey;
    private static boolean nodeESPToggle = false;
    private static boolean clickGUIToggle = false;
    private static final KeyMapping.Category RENDER = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("asuka", "render"));
    private static final KeyMapping.Category PLAYER = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("asuka", "player"));

    public static void register() {
        nodeEspKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.asuka.node_esp",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                RENDER
        ));

        clickGuiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.asuka.click_gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                RENDER
        ));

        ghostBlockToggle = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.asuka.ghost_toggle:",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                PLAYER
        ));

        ghostBlockKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.asuka.ghost_block:",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                PLAYER
        ));

        freecamKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.asuka.freecam:",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                PLAYER
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client->{
            while(nodeEspKey.consumeClick()){
                toggleNodeESP();
                client.player.sendSystemMessage(Component.literal("Node ESP " + (nodeESPToggle ? "Enabled" : "Disabled")));
            }
            while(clickGuiKey.consumeClick()){
                toggleClickGUI();
            }
            while(ghostBlockToggle.consumeClick()){
                GhostBlock.get().toggle();
                client.player.sendSystemMessage(Component.literal("Ghost Block " + (GhostBlock.get().isEnabled() ? "Enabled" : "Disabled")));
            }
            while (freecamKey.consumeClick()) {
                Freecam.toggle();
            }
            if (GhostBlock.get().isEnabled()) {
                while (ghostBlockKey.consumeClick()) {
                    GhostBlock.get().ghostTargetedBlock();
                }
            }
        });
    }

    public static void toggleClickGUI(){
        Minecraft client = Minecraft.getInstance();
        if (client.gui.screen() instanceof ClickGUI) {
            client.setScreenAndShow(null);
            clickGUIToggle = false;
        } else {
            client.setScreenAndShow(new ClickGUI(Component.literal("asuka client")));
            clickGUIToggle = true;
        }
        clickGUIToggle = !clickGUIToggle;
    }

    public static boolean isEnabledNodeESP(){
        return nodeESPToggle;
    }
    public static void toggleNodeESP(){
        nodeESPToggle = !nodeESPToggle;
    }


}