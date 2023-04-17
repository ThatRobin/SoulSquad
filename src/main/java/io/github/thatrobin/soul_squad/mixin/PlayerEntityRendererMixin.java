package io.github.thatrobin.soul_squad.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.thatrobin.soul_squad.component.BodyOwnerComponent;
import io.github.thatrobin.soul_squad.powers.BlockPossession;
import io.github.thatrobin.soul_squad.util.BuiltinModelItemRendererExtention;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    @Shadow
    protected abstract void setModelPose(AbstractClientPlayerEntity player);

    public PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Redirect(
            method = "render*",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
    )
    private void redirectRender(LivingEntityRenderer renderer, LivingEntity entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        if(PowerHolderComponent.hasPower(entity, BlockPossession.class)) {
            BlockPossession power = PowerHolderComponent.getPowers(entity, BlockPossession.class).get(0);
            BlockState blockState = power.getPossessedBlock();

            if (blockState != null) {
                matrixStack.translate(-0.5D, 0.0D, -0.5D);
                this.shadowRadius = 0.0f;

                BlockRenderType blockRenderType = blockState.getRenderType();

                if(blockRenderType.equals(BlockRenderType.MODEL)) {
                    MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(blockState, matrixStack, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV);
                } else {
                    BuiltinModelItemRendererExtention.render(entity, entity.getBlockPos(), blockState, matrixStack, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV);
                }
            } else {
                this.shadowRadius = 0.5f;
                super.render((AbstractClientPlayerEntity) entity, f, g, matrixStack, vertexConsumerProvider, i);
            }
        } else {
            super.render((AbstractClientPlayerEntity) entity, f, g, matrixStack, vertexConsumerProvider, i);
        }
    }

    @Inject(method = "renderArm", at = @At("HEAD"), cancellable = true)
    private void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        if (PowerHolderComponent.hasPower(player, BlockPossession.class)) {
            BlockPossession power = PowerHolderComponent.getPowers(player, BlockPossession.class).get(0);

            BlockState blockState = power.getPossessedBlock();

            if (blockState != null) {

                PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel = this.getModel();

                this.setModelPose(player);
                playerEntityModel.handSwingProgress = 0.0F;
                playerEntityModel.sneaking = false;
                playerEntityModel.leaningPitch = 0.0F;
                playerEntityModel.setAngles(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
                arm.pitch = 0.0F;

                Sprite sprite = MinecraftClient.getInstance().getBlockRenderManager().getModel(blockState).getParticleSprite();

                Identifier textureId = new Identifier(sprite.getContents().getId().getNamespace(), "textures/"+sprite.getContents().getId().getPath()+".png");

                arm.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(textureId)), light, OverlayTexture.DEFAULT_UV);
                ci.cancel();
            } else {
                PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel = this.getModel();

                this.setModelPose(player);
                playerEntityModel.handSwingProgress = 0.0F;
                playerEntityModel.sneaking = false;
                playerEntityModel.leaningPitch = 0.0F;
                playerEntityModel.setAngles(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
                arm.pitch = 0.0F;

                Sprite sprite = MinecraftClient.getInstance().getBlockRenderManager().getModel(Blocks.QUARTZ_BLOCK.getDefaultState()).getParticleSprite();

                Identifier textureId = new Identifier(sprite.getContents().getId().getNamespace(), "textures/"+sprite.getContents().getId().getPath()+".png");

                arm.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(textureId)), light, OverlayTexture.DEFAULT_UV);
                ci.cancel();
            }
        }
    }

    @Inject(method = "getTexture(Lnet/minecraft/client/network/AbstractClientPlayerEntity;)Lnet/minecraft/util/Identifier;", at = @At("RETURN"), cancellable = true)
    public void getTexture(AbstractClientPlayerEntity abstractClientPlayerEntity, CallbackInfoReturnable<Identifier> cir) {
        Optional<BodyOwnerComponent> optional = BodyOwnerComponent.KEY.maybeGet(abstractClientPlayerEntity);
        if(optional.isPresent()) {
            BodyOwnerComponent component = optional.get();
            if (component.getOwner() != null) {
                if (component.getEntityOwner() != null) {
                    cir.setReturnValue(MinecraftClient.getInstance().getSkinProvider().loadSkin(component.getEntityOwner().getGameProfile()));
                }
            }
        }
    }

    @Inject(method = "renderLabelIfPresent*", at = @At("HEAD"), cancellable = true)
    protected void renderLabelIfPresent(AbstractClientPlayerEntity abstractClientPlayerEntity, Text text, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        Optional<BodyOwnerComponent> optional = BodyOwnerComponent.KEY.maybeGet(abstractClientPlayerEntity);
        if(optional.isPresent()) {
            BodyOwnerComponent component = optional.get();
            if (component.getOwner() != null) {
                if (component.getEntityOwner() != null) {
                    matrixStack.push();
                    super.renderLabelIfPresent(abstractClientPlayerEntity, component.getEntityOwner().getDisplayName(), matrixStack, vertexConsumerProvider, i);
                    matrixStack.pop();
                    ci.cancel();
                }
            }
        }
    }
}
