package com.github.shrekshellraiser.fabric.client;

import com.github.shrekshellraiser.ModBlockEntities;
import com.github.shrekshellraiser.computer.block.entity.ComputerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ComputerBlockEntityRenderer implements BlockEntityRenderer<ComputerBlockEntity> {
    BlockEntityRendererProvider.Context context;
    private static final ItemStack stack = new ItemStack(Items.BEETROOT);

    public ComputerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(ComputerBlockEntity blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {

    }
}
