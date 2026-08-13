package net.taunty.asuka;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.taunty.asuka.discord.DiscordPresence;
import net.taunty.asuka.highlight.HighlightRenderPipeline;
import net.taunty.asuka.keybinding.ModKeybinds;

public class AsukaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Asuka.LOGGER.info("AsukaClient client initialized!");
        HighlightRenderPipeline.register();
        ModKeybinds.register();
        DiscordPresence.init();
        ClientLifecycleEvents.CLIENT_STOPPING.register(client->DiscordPresence.shutdown());
    }
}