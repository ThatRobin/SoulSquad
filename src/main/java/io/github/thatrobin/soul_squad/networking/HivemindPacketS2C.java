package io.github.thatrobin.soul_squad.networking;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.thatrobin.soul_squad.powers.BodyManagementPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;

import java.util.Optional;
import java.util.UUID;

public class HivemindPacketS2C {

    @Environment(EnvType.CLIENT)
    public static void register() {
        ClientPlayConnectionEvents.INIT.register(((clientPlayNetworkHandler, minecraftClient) -> {
            ClientPlayNetworking.registerReceiver(HivemindPackets.UPDATE_SLOT, HivemindPacketS2C::updateInventory);
            ClientPlayNetworking.registerGlobalReceiver(HivemindPackets.UPDATE_UUIDS, HivemindPacketS2C::updateUUIDS);
        }));
    }

    private static void updateUUIDS(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        int bodyIndex = packetByteBuf.readInt();
        UUID uuid = packetByteBuf.readUuid();
        if (minecraftClient.player != null) {
            PowerHolderComponent component = PowerHolderComponent.KEY.get(minecraftClient.player);
            Optional<BodyManagementPower> powerOptional = component.getPowers(BodyManagementPower.class).stream().findFirst();
            powerOptional.ifPresent((power) -> {
                if (bodyIndex == 1) {
                    power.entityOneUUID = uuid;
                } else if (bodyIndex == 2) {
                    power.entityTwoUUID = uuid;
                }
            });
        }
    }

    private static void updateInventory(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        int slot = packetByteBuf.readInt();
        ClientPlayerEntity clientPlayerEntity = minecraftClient.player;
        if (clientPlayerEntity != null) {
            clientPlayerEntity.inventory.selectedSlot = slot;
        }
    }

}
