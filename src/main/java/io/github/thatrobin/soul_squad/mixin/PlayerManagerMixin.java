package io.github.thatrobin.hivemind.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.thatrobin.hivemind.entity.HivemindBodyEntity;
import io.github.thatrobin.hivemind.networking.HivemindPackets;
import io.github.thatrobin.hivemind.powers.BodyManagementPower;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @WrapWithCondition(
            method = "onPlayerConnect",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V")
    )
    private boolean shouldAnnounce(PlayerManager instance, Text reason, boolean overlay, @Local(ordinal = 0) ServerPlayerEntity player) {
        return !(player instanceof HivemindBodyEntity);
    }
}
