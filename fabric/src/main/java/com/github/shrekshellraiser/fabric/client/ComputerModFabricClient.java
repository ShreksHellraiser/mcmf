package com.github.shrekshellraiser.fabric.client;

import com.github.shrekshellraiser.ModBlockEntities;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.ClientModInitializer;

public final class ComputerModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        BlockEntityRendererRegistry.register(
            ModBlockEntities.COMPUTER_BLOCK_ENTITY.get(),
            ComputerBlockEntityRenderer::new
        );
    }
}
