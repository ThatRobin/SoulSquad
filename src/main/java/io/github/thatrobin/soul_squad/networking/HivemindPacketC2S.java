package io.github.thatrobin.soul_squad.networking;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.thatrobin.soul_squad.powers.BodyManagementPower;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class HivemindPacketC2S {

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(HivemindPackets.SWAP_BODIES, HivemindPacketC2S::swapBodies);
    }

    private static void swapBodies(MinecraftServer server, ServerPlayerEntity serverPlayerEntity, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        int bodyIndex = packetByteBuf.readInt();
        Optional<BodyManagementPower> powerOptional = PowerHolderComponent.getPowers(serverPlayerEntity, BodyManagementPower.class).stream().findFirst();
        powerOptional.ifPresent((power) -> {
            power.swapPlayer(bodyIndex);
        });
    }
}
