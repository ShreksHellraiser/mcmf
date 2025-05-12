package com.github.shrekshellraiser.computer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
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
        poseStack.translate(0.5, 0.8, 0.5);
        assert blockEntity.getLevel() != null;
        long l = blockEntity.getLevel().getGameTime();
        poseStack.mulPose(Quaternion.fromXYZ(0,  l / 20.0f, 0));
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.GROUND, i, j,
                poseStack, multiBufferSource, 0);
    }
}
