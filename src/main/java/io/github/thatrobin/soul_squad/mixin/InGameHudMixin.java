package io.github.thatrobin.poltergeist.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.thatrobin.poltergeist.Poltergeist;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.thatrobin.poltergeist.component.BlockDurabilityComponent;
import io.github.thatrobin.poltergeist.powers.BlockPossession;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {

    @Shadow @Final private MinecraftClient client;

    @Shadow private int scaledHeight;

    @Shadow protected abstract PlayerEntity getCameraPlayer();

    @Shadow private int scaledWidth;

    @Inject(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHealthBar(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/entity/player/PlayerEntity;IIIIFIIIZ)V"),cancellable = true)
    public void preventHealthBarRender(MatrixStack matrices, CallbackInfo ci) {
        PlayerEntity player = this.getCameraPlayer();
        if(PowerHolderComponent.hasPower(player, BlockPossession.class)) {
            BlockPossession blockPossession = PowerHolderComponent.getPowers(player, BlockPossession.class).get(0);
            if(blockPossession.getPossessedBlock() != null) {
                BlockDurabilityComponent blockDurabilityComponent = BlockDurabilityComponent.KEY.get(player);
                if(blockDurabilityComponent.hasBlock(blockPossession.getPossessedBlock().getBlock())) {
                    float durability = blockDurabilityComponent.getDurability(blockPossession.getPossessedBlock().getBlock());
                    this.renderDurabilityBar(durability, matrices);
                    ci.cancel();
                }
            }
        }
    }

    public void renderDurabilityBar(float durability, MatrixStack matrices) {
        RenderSystem.setShaderTexture(0, new Identifier("textures/gui/bars.png"));

        int j = 182;
        int l = this.scaledHeight - 32 - 4;
        int x = this.scaledWidth / 2 - 91;

        this.drawTexture(matrices, x, l, 0, 0 * 5 * 2, j, 5);
        int i;
        if ((i = (int) ((durability / 1000f) * 183.0f)) > 0) {
            this.drawTexture(matrices, x, l, 0, 0 * 5 * 2 + 5, i, 5);
        }
    }
}
