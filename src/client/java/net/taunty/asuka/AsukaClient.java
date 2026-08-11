package net.taunty.asuka;

import net.fabricmc.api.ClientModInitializer;
import net.taunty.asuka.highlight.HighlightRenderPipeline;
import net.taunty.asuka.keybinding.ModKeybinds;

public class AsukaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Asuka.LOGGER.info("AsukaClient client initialized!");
        HighlightRenderPipeline.register();
        ModKeybinds.register();
    }
}