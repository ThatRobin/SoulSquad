package io.github.thatrobin.hivemind.networking;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.thatrobin.hivemind.Hivemind;
import io.github.thatrobin.hivemind.powers.BodyManagementPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.HotbarStorage;
import net.minecraft.client.option.HotbarStorageEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Map;
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
