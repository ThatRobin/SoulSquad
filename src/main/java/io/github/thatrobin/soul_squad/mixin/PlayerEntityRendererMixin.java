package io.github.thatrobin.hivemind.mixin;

import com.mojang.authlib.GameProfile;
import io.github.thatrobin.hivemind.Hivemind;
import io.github.thatrobin.hivemind.component.BodyOwnerComponent;
import io.github.thatrobin.hivemind.component.BodyOwnerComponentImpl;
import io.github.thatrobin.hivemind.entity.HivemindBodyEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameModeSelectionScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.UserCache;
import net.minecraft.util.Uuids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    public PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
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
