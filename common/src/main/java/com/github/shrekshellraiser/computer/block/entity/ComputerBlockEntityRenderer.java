package com.github.shrekshellraiser.computer.block.entity;

import com.github.shrekshellraiser.computer.block.ComputerBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class ComputerBlockEntityRenderer implements BlockEntityRenderer<ComputerBlockEntity> {
    BlockEntityRendererProvider.Context context;

    public ComputerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(ComputerBlockEntity blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        var facing = blockEntity.getBlockState().getValue(ComputerBlock.FACING);
        poseStack.translate(blockEntity.getRenderX(), 0.4, blockEntity.getRenderZ());
        assert blockEntity.getLevel() != null;

        ItemStack stack = blockEntity.getItem(1);
        poseStack.mulPose(Quaternion.fromXYZ(0, (float) (facing.get2DDataValue() * Math.PI / 2.0f), 0));
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.GROUND, i, j,
                poseStack, multiBufferSource, 0);
    }
}
