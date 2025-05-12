package com.github.shrekshellraiser;

import com.github.shrekshellraiser.computer.block.entity.ComputerBlockEntityRenderer;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;

public class ModBlockEntityRenderers {
    public static void register() {
        BlockEntityRendererRegistry.register(
                ModBlockEntities.COMPUTER_BLOCK_ENTITY.get(),
                ComputerBlockEntityRenderer::new
        );
    }
}
