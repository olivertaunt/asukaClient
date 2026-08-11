package net.taunty.skyblockesp;

import net.fabricmc.api.ClientModInitializer;
import net.taunty.skyblockesp.highlight.HighlightRenderPipeline;

public class SkyblockESPClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SkyblockESP.LOGGER.info("SkyblockESP client initialized!");
        HighlightRenderPipeline.register();
    }
}