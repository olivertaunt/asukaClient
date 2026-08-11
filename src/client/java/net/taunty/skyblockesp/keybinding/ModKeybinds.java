package net.taunty.skyblockesp.keybinding;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.taunty.skyblockesp.gui.ClickGUI;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds{
    private static KeyMapping nodeEspKey;
    private static KeyMapping clickGuiKey;
    private static boolean nodeESPToggle = false;
    private static boolean clickGUIToggle = false;
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("skyblockesp", "render"));

    public static void register() {
        nodeEspKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.skyblockesp.toggle_nodeesp",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                CATEGORY
        ));

        clickGuiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.skyblockesp.click_gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client->{
            while(nodeEspKey.consumeClick()){
                toggleNodeESP();
                client.player.sendSystemMessage(Component.literal("Node ESP " + (nodeESPToggle ? "Enabled" : "Disabled")));
            }
            while(clickGuiKey.consumeClick()){
                toggleClickGUI();
                client.player.sendSystemMessage(Component.literal("Click GUI " + (clickGUIToggle ? "Enabled" : "Disabled")));
                client.setScreenAndShow(new ClickGUI(Component.literal("taunty client")));
            }
        });
    }

    public static boolean isEnabledClickGUI(){
        return clickGUIToggle;
    }

    public static void toggleClickGUI(){
        clickGUIToggle = !clickGUIToggle;
    }

    public static boolean isEnabledNodeESP(){
        return nodeESPToggle;
    }

    public static void toggleNodeESP(){
        nodeESPToggle = !nodeESPToggle;
    }
}