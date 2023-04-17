package io.github.thatrobin.soul_squad.util;

import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Comparator;

public class BuiltinModelItemRendererExtention {

    public static void render(LivingEntity entity, BlockPos blockPos, BlockState blockState, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockEntity blockEntity;
        Block block = blockState.getBlock();
        if (block instanceof AbstractSkullBlock) {
            blockEntity = new SkullBlockEntity(blockPos, blockState);
        } else if (block instanceof AbstractBannerBlock) {
            blockEntity = new BannerBlockEntity(blockPos, blockState, ((AbstractBannerBlock) block).getColor());
        } else if (block instanceof BedBlock) {
            blockEntity = new BedBlockEntity(blockPos, blockState, ((BedBlock) block).getColor());
        } else if (blockState.isOf(Blocks.CONDUIT)) {
            blockEntity = new ConduitBlockEntity(blockPos, blockState);
        } else if (blockState.isOf(Blocks.CHEST)) {
            blockEntity = new ChestBlockEntity(blockPos, blockState);
        } else if (blockState.isOf(Blocks.ENDER_CHEST)) {
            blockEntity = new EnderChestBlockEntity(blockPos, blockState);
        } else if (blockState.isOf(Blocks.TRAPPED_CHEST)) {
            blockEntity = new TrappedChestBlockEntity(blockPos, blockState);
        } else if (block instanceof ShulkerBoxBlock) {
            DyeColor dyeColor2 = ShulkerBoxBlock.getColor(block);
            ShulkerBoxBlockEntity[] RENDER_SHULKER_BOX_DYED = Arrays.stream(DyeColor.values()).sorted(Comparator.comparingInt(DyeColor::getId)).map(dyeColor -> new ShulkerBoxBlockEntity(dyeColor, blockPos, blockState)).toArray(ShulkerBoxBlockEntity[]::new);
            blockEntity = dyeColor2 == null ? new ShulkerBoxBlockEntity(blockPos, blockState) : RENDER_SHULKER_BOX_DYED[dyeColor2.getId()];
        } else {
            return;
        }
        blockEntity.setWorld(entity.world);
        MinecraftClient.getInstance().getBlockRenderManager().builtinModelItemRenderer.blockEntityRenderDispatcher.renderEntity(blockEntity, matrices, vertexConsumers, light, overlay);

    }
}
