package io.github.thatrobin.hivemind.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import io.github.thatrobin.hivemind.Hivemind;
import io.github.thatrobin.hivemind.entity.HivemindBodyEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow public ServerPlayerEntity player;

    @WrapWithCondition(
            method = "onDisconnected",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V")
    )
    private boolean shouldAnnounce(PlayerManager instance, Text reason, boolean overlay) {
        return !(player instanceof HivemindBodyEntity);
    }
}
