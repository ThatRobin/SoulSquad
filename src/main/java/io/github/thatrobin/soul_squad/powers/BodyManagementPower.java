package io.github.thatrobin.soul_squad.powers;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.registry.ModComponents;
import io.github.thatrobin.soul_squad.SoulSquad;
import io.github.thatrobin.soul_squad.component.BodyHolderComponent;
import io.github.thatrobin.soul_squad.component.BodyOwnerComponent;
import io.github.thatrobin.soul_squad.entity.HivemindBodyEntity;
import io.github.thatrobin.soul_squad.mixin.PowerHolderComponentImplAccessorMixin;
import io.github.thatrobin.soul_squad.networking.HivemindPackets;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeleportCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BodyManagementPower extends Power {

    public HivemindBodyEntity entityOne;
    public UUID entityOneUUID;
    public HivemindBodyEntity entityTwo;
    public UUID entityTwoUUID;
    public int currentBodyIndex = 0;
    public int bodyOneIndex = 1;
    public int bodyTwoIndex = 2;

    public BodyManagementPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }

    public void createBody() {
        if(!entity.world.isClient) {
            if (entity instanceof ServerPlayerEntity player) {
                if(entityOne == null) {
                    String name = entity.getDisplayName().getString() + "-1";
                    entityOne = HivemindBodyEntity.createFake(name, Objects.requireNonNull(entity.getServer()), entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch(), entity.world.getRegistryKey(), false);
                    BodyHolderComponent bodyHolderComponent = BodyHolderComponent.KEY.get(entity);
                    bodyHolderComponent.setBody(this.bodyOneIndex, entityOne.getUuid());
                    bodyHolderComponent.sync();
                    BodyOwnerComponent component = BodyOwnerComponent.KEY.get(entityOne);
                    component.setOwner(player.getUuid());
                    this.entityOneUUID = entityOne.getUuid();
                    PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                    buf.writeInt(1);
                    buf.writeUuid(this.entityOneUUID);
                    ServerPlayNetworking.send(player, HivemindPackets.UPDATE_UUIDS, buf);
                    component.sync();
                    OriginComponent originComponent = ModComponents.ORIGIN.get(player);
                    OriginComponent originOneComponent = ModComponents.ORIGIN.get(entityOne);
                    for (OriginLayer layer : OriginLayers.getLayers()) {
                        originOneComponent.setOrigin(layer, originComponent.getOrigin(layer));
                    }
                    originOneComponent.sync();
                } else if(entityTwo == null) {
                    String name = entity.getDisplayName().getString() + "-2";
                    entityTwo = HivemindBodyEntity.createFake(name, Objects.requireNonNull(entity.getServer()), entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch(), entity.world.getRegistryKey(), false);
                    BodyHolderComponent bodyHolderComponent = BodyHolderComponent.KEY.get(entity);
                    bodyHolderComponent.setBody(this.bodyTwoIndex, entityTwo.getUuid());
                    bodyHolderComponent.sync();
                    BodyOwnerComponent component = BodyOwnerComponent.KEY.get(entityTwo);
                    component.setOwner(player.getUuid());
                    this.entityTwoUUID = entityTwo.getUuid();
                    PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                    buf.writeInt(2);
                    buf.writeUuid(this.entityTwoUUID);
                    ServerPlayNetworking.send(player, HivemindPackets.UPDATE_UUIDS, buf);
                    component.sync();
                    OriginComponent originComponent = ModComponents.ORIGIN.get(player);
                    OriginComponent originOneComponent = ModComponents.ORIGIN.get(entityTwo);
                    for (OriginLayer layer : OriginLayers.getLayers()) {
                        originOneComponent.setOrigin(layer, originComponent.getOrigin(layer));
                    }
                    originOneComponent.sync();
                }
            }
            PowerHolderComponent.sync(entity);
        }
        swapPlayer(this.currentBodyIndex);
    }

    public void removeBody(HivemindBodyEntity entity) {
        if (entity.equals(entityOne)) entityOne = null;
        if (entity.equals(entityTwo)) entityTwo = null;
    }

    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(SoulSquad.hivemind("body_manager"),
                new SerializableData(),
                data ->
                        BodyManagementPower::new)
                .allowCondition();
    }

    public void swapPlayer(int bodyIndex) {
        BodyHolderComponent bodyHolderComponent = BodyHolderComponent.KEY.get(entity);
        if(bodyHolderComponent.hasBody(bodyIndex)) {
            PlayerEntity pBody = bodyHolderComponent.getBody(bodyIndex);
            if (entity instanceof PlayerEntity player) {
                bodyHolderComponent.setBody(bodyIndex, player.getUuid());
                bodyHolderComponent.setIcon(bodyIndex, player.getMainHandStack());
                bodyHolderComponent.setBody(this.currentBodyIndex, pBody.getUuid());
                bodyHolderComponent.setIcon(this.currentBodyIndex, pBody.getMainHandStack());
            }

            if (pBody instanceof ServerPlayerEntity body) {
                performSwap(body);
            }
            if (this.currentBodyIndex != bodyIndex) {
                int temp;
                if (bodyIndex == this.bodyOneIndex) {
                    temp = this.bodyOneIndex;
                    this.bodyOneIndex = this.currentBodyIndex;
                    this.currentBodyIndex = temp;
                } else if (bodyIndex == this.bodyTwoIndex) {
                    temp = this.bodyTwoIndex;
                    this.bodyTwoIndex = this.currentBodyIndex;
                    this.currentBodyIndex = temp;
                }
            }
            PowerHolderComponent.sync(entity);
        }
        bodyHolderComponent.sync();
    }

    void performSwap(ServerPlayerEntity body) {
        if(body != null) {
            if (entity instanceof ServerPlayerEntity player) {
                if (body.equals(player)) return;
                PowerHolderComponent playerComponent = PowerHolderComponent.KEY.get(player);
                PowerHolderComponent bodyComponent = PowerHolderComponent.KEY.get(body);
                Vec3d pos = player.getPos();
                float yaw = player.getYaw();
                float pitch = player.getPitch();
                float health = player.getHealth();
                float exhaustion = player.getHungerManager().getExhaustion();
                int foodLevel = player.getHungerManager().getFoodLevel();
                float saturationLevel = player.getHungerManager().getSaturationLevel();
                ServerWorld world = player.getWorld();
                int slot = player.inventory.selectedSlot;
                int slot2 = body.inventory.selectedSlot;

                ConcurrentHashMap<PowerType<?>, List<Identifier>> playerPowerSources = new ConcurrentHashMap<>(((PowerHolderComponentImplAccessorMixin)playerComponent).getPowerSources());
                ConcurrentHashMap<PowerType<?>, Power> playerPowers = new ConcurrentHashMap<>(((PowerHolderComponentImplAccessorMixin)playerComponent).getPowers());

                Map<Integer, ItemStack> main = new HashMap<>();
                Map<Integer, ItemStack> main2 = new HashMap<>();
                Map<Integer, ItemStack> armor = new HashMap<>();
                Map<Integer, ItemStack> armor2 = new HashMap<>();
                Map<Integer, ItemStack> offHand = new HashMap<>();
                Map<Integer, ItemStack> offHand2 = new HashMap<>();

                for (int i = 0; i < body.inventory.main.size(); ++i) {
                    if (body.inventory.main.get(i).isEmpty()) continue;
                    main.put(i, body.inventory.main.get(i));
                }
                for (int i = 0; i < body.inventory.armor.size(); ++i) {
                    if (body.inventory.armor.get(i).isEmpty()) continue;
                    armor.put(i + 100, body.inventory.armor.get(i));
                }
                for (int i = 0; i < body.inventory.offHand.size(); ++i) {
                    if (body.inventory.offHand.get(i).isEmpty()) continue;
                    offHand.put(i + 150, body.inventory.offHand.get(i));
                }

                for (int i = 0; i < player.inventory.main.size(); ++i) {
                    if (player.inventory.main.get(i).isEmpty()) continue;
                    main2.put(i, player.inventory.main.get(i));
                }
                for (int i = 0; i < player.inventory.armor.size(); ++i) {
                    if (player.inventory.armor.get(i).isEmpty()) continue;
                    armor2.put(i + 100, player.inventory.armor.get(i));
                }
                for (int i = 0; i < player.inventory.offHand.size(); ++i) {
                    if (player.inventory.offHand.get(i).isEmpty()) continue;
                    offHand2.put(i + 150, player.inventory.offHand.get(i));
                }


                player.inventory.main.clear();
                player.inventory.armor.clear();
                player.inventory.offHand.clear();

                body.inventory.main.clear();
                body.inventory.armor.clear();
                body.inventory.offHand.clear();

                main.forEach(((integer, itemStack) -> {
                    player.inventory.main.set(integer, itemStack);
                }));
                armor.forEach(((integer, itemStack) -> {
                    player.inventory.armor.set(integer - 100, itemStack);
                }));
                offHand.forEach(((integer, itemStack) -> {
                    player.inventory.offHand.set(integer - 150, itemStack);
                }));

                main2.forEach(((integer, itemStack) -> {
                    body.inventory.main.set(integer, itemStack);
                }));
                armor2.forEach(((integer, itemStack) -> {
                    body.inventory.armor.set(integer - 100, itemStack);
                }));
                offHand2.forEach(((integer, itemStack) -> {
                    body.inventory.offHand.set(integer - 150, itemStack);
                }));


                player.inventory.selectedSlot = body.inventory.selectedSlot;
                body.inventory.selectedSlot = slot;
                player.playerScreenHandler.sendContentUpdates();
                body.playerScreenHandler.sendContentUpdates();


                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                buf.writeInt(slot);
                ServerPlayNetworking.send(body, HivemindPackets.UPDATE_SLOT, buf);

                PacketByteBuf buf1 = new PacketByteBuf(Unpooled.buffer());
                buf1.writeInt(slot2);
                ServerPlayNetworking.send(player, HivemindPackets.UPDATE_SLOT, buf1);

                player.setHealth(body.getHealth());
                player.getHungerManager().setExhaustion(body.getHungerManager().getExhaustion());
                player.getHungerManager().setFoodLevel(body.getHungerManager().getFoodLevel());
                player.getHungerManager().setSaturationLevel(body.getHungerManager().getSaturationLevel());
                body.setHealth(health);
                body.getHungerManager().setExhaustion(exhaustion);
                body.getHungerManager().setFoodLevel(foodLevel);
                body.getHungerManager().setSaturationLevel(saturationLevel);


                ((PowerHolderComponentImplAccessorMixin)playerComponent).getPowers().forEach(((powerType, power) -> {
                    if(!(power instanceof BodyManagementPower)) {
                        ((PowerHolderComponentImplAccessorMixin)playerComponent).getPowers().remove(powerType);
                    }
                }));
                ((PowerHolderComponentImplAccessorMixin)playerComponent).getPowerSources().forEach(((powerType, identifiers) -> {
                    if(!(powerType.getIdentifier().equals(SoulSquad.hivemind("body_management")))) {
                        ((PowerHolderComponentImplAccessorMixin)playerComponent).getPowerSources().remove(powerType);
                    }
                }));

                ((PowerHolderComponentImplAccessorMixin)bodyComponent).getPowers().forEach((powerType, power) -> {
                    if(!(power instanceof BodyManagementPower)) {
                        ((PowerHolderComponentImplAccessorMixin) playerComponent).getPowers().put(powerType, power);
                    }
                });
                ((PowerHolderComponentImplAccessorMixin)bodyComponent).getPowerSources().forEach(((powerType, identifiers) -> {
                    if(!(powerType.get(body) instanceof BodyManagementPower)) {
                        ((PowerHolderComponentImplAccessorMixin)playerComponent).getPowerSources().put(powerType, identifiers);
                    }
                }));

                ((PowerHolderComponentImplAccessorMixin)bodyComponent).getPowers().forEach(((powerType, power) -> {
                    if(!(power instanceof BodyManagementPower)) {
                        ((PowerHolderComponentImplAccessorMixin)bodyComponent).getPowers().remove(powerType);
                    }
                }));
                ((PowerHolderComponentImplAccessorMixin)bodyComponent).getPowerSources().forEach(((powerType, identifiers) -> {
                    if(!(powerType.getIdentifier().equals(SoulSquad.hivemind("body_management")))) {
                        ((PowerHolderComponentImplAccessorMixin)bodyComponent).getPowerSources().remove(powerType);
                    }
                }));

                playerPowers.forEach((powerType, power) -> {
                    if(!(power instanceof BodyManagementPower)) {
                        ((PowerHolderComponentImplAccessorMixin) bodyComponent).getPowers().put(powerType, power);
                    }
                });
                playerPowerSources.forEach(((powerType, identifiers) -> {
                    if(!(powerType.get(body) instanceof BodyManagementPower)) {
                        ((PowerHolderComponentImplAccessorMixin)bodyComponent).getPowerSources().put(powerType, identifiers);
                    }
                }));
                playerComponent.sync();
                bodyComponent.sync();

                ServerCommandSource source = player.getCommandSource();
                ServerCommandSource source2 = body.getCommandSource();
                try {
                    TeleportCommand.teleport(source2, player, body.getWorld(), body.getPos().x, body.getPos().y, body.getPos().z, EnumSet.noneOf(PlayerPositionLookS2CPacket.Flag.class), body.getYaw(), body.getPitch(), null);
                    TeleportCommand.teleport(source, body, world, pos.x, pos.y, pos.z, EnumSet.noneOf(PlayerPositionLookS2CPacket.Flag.class), yaw, pitch, null);
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public NbtElement toTag() {
        NbtCompound nbtCompound = new NbtCompound();
        NbtList list = new NbtList();

        if(entityOne != null) {
            NbtCompound compound = new NbtCompound();
            compound.putString("Name", entityOne.getDisplayName().getString());
            compound.put("Position", NbtHelper.fromBlockPos(entityOne.getBlockPos()));
            compound.putUuid("UUID", this.entityOneUUID);
            list.add(compound);
        }
        if(entityTwo != null) {
            NbtCompound compound2 = new NbtCompound();
            compound2.putString("Name", entityTwo.getDisplayName().getString());
            compound2.put("Position", NbtHelper.fromBlockPos(entityTwo.getBlockPos()));
            compound2.putUuid("UUID", this.entityTwoUUID);
            list.add(compound2);
        }

        nbtCompound.put("Bodies", list);
        nbtCompound.putInt("CurrentBodyIndex", this.currentBodyIndex);
        nbtCompound.putInt("BodyOneIndex", this.bodyOneIndex);
        nbtCompound.putInt("BodyTwoIndex", this.bodyTwoIndex);
        return nbtCompound;
    }

    @Override
    public void fromTag(NbtElement tag) {
        NbtCompound compound = (NbtCompound) tag;
        if (compound.contains("BodyOneIndex")) {
            this.bodyOneIndex = compound.getInt("BodyOneIndex");
        }
        if (compound.contains("BodyTwoIndex")) {
            this.bodyTwoIndex = compound.getInt("BodyTwoIndex");
        }
        if (compound.contains("CurrentBodyIndex")) {
            this.currentBodyIndex = compound.getInt("CurrentBodyIndex");
        }
        if (!(entity instanceof HivemindBodyEntity)) {
            for (NbtElement bodies : compound.getList("Bodies", NbtElement.COMPOUND_TYPE)) {
                NbtCompound bodyCompound = (NbtCompound) bodies;
                String name = bodyCompound.getString("Name");
                BlockPos blockPos = NbtHelper.toBlockPos(bodyCompound.getCompound("Position"));
                if (name.endsWith("1") && entityOne == null) {
                    entityOneUUID = bodyCompound.getUuid("UUID");
                    if (entity instanceof ServerPlayerEntity) {
                        entityOne = HivemindBodyEntity.createFake(name, Objects.requireNonNull(entity.getServer()), blockPos.getX(), blockPos.getY(), blockPos.getZ(), entity.getYaw(), entity.getPitch(), entity.world.getRegistryKey(), false);
                        BodyOwnerComponent component = BodyOwnerComponent.KEY.get(entityOne);
                        component.setOwner(entity.getUuid());
                        component.sync();
                    }
                } else if (name.endsWith("2") && entityTwo == null) {
                    entityTwoUUID = bodyCompound.getUuid("UUID");
                    if (entity instanceof ServerPlayerEntity) {
                        entityTwo = HivemindBodyEntity.createFake(name, Objects.requireNonNull(entity.getServer()), blockPos.getX(), blockPos.getY(), blockPos.getZ(), entity.getYaw(), entity.getPitch(), entity.world.getRegistryKey(), false);
                        BodyOwnerComponent component = BodyOwnerComponent.KEY.get(entityTwo);
                        component.setOwner(entity.getUuid());
                        component.sync();
                    }
                }
            }
            BodyHolderComponent bodyHolderComponent = BodyHolderComponent.KEY.get(entity);
            bodyHolderComponent.setBody(this.currentBodyIndex, entity.getUuid());
            bodyHolderComponent.setBody(this.bodyOneIndex, entityOneUUID);
            bodyHolderComponent.setBody(this.bodyTwoIndex, entityTwoUUID);
            bodyHolderComponent.sync();
        }
    }
}
