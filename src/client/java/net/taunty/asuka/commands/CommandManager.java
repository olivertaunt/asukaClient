package net.taunty.asuka.commands;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.taunty.asuka.keybinding.ModKeybinds;


public class CommandManager {

    private static boolean openGuiNextTick = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openGuiNextTick) {
                openGuiNextTick = false;
                ModKeybinds.toggleClickGUI();
            }
        });
        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess) -> dispatcher.register(
                        ClientCommands.literal("asuka").executes(context -> {
                                    ModKeybinds.toggleClickGUI();
                                    return 1;
                                })));
    }
}

