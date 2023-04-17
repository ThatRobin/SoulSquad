package io.github.thatrobin.hivemind.entity;

import com.mojang.authlib.GameProfile;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.thatrobin.hivemind.component.BodyOwnerComponent;
import io.github.thatrobin.hivemind.powers.BodyManagementPower;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.UserCache;
import net.minecraft.util.Uuids;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class HivemindBodyEntity extends ServerPlayerEntity {

    public Runnable fixStartingPosition = () -> {};
    public boolean isAShadow;


    public static HivemindBodyEntity createFake(String username, MinecraftServer server, double d0, double d1, double d2, double yaw, double pitch, RegistryKey<World> dimensionId, boolean flying) {
        ServerWorld worldIn = server.getWorld(dimensionId);
        UserCache.setUseRemote(false);
        GameProfile gameprofile;
        try {
            gameprofile = server.getUserCache().findByName(username).orElse(null);
        } finally {
            UserCache.setUseRemote(server.isDedicated() && server.isOnlineMode());
        }
        if (gameprofile == null) {
            gameprofile = new GameProfile(Uuids.getOfflinePlayerUuid(username), username);
        }
        if (gameprofile.getProperties().containsKey("textures")) {
            AtomicReference<GameProfile> result = new AtomicReference<>();
            SkullBlockEntity.loadProperties(gameprofile, result::set);
            gameprofile = result.get();
        }
        HivemindBodyEntity instance = new HivemindBodyEntity(server, worldIn, gameprofile, false);
        instance.fixStartingPosition = () -> instance.refreshPositionAndAngles(d0, d1, d2, (float) yaw, (float) pitch);
        server.getPlayerManager().onPlayerConnect(new FakeClientConnection(NetworkSide.SERVERBOUND), instance);
        instance.teleport(worldIn, d0, d1, d2, (float)yaw, (float)pitch);
        instance.setHealth(20.0F);
        instance.unsetRemoved();
        instance.stepHeight = 0.6F;
        instance.interactionManager.changeGameMode(GameMode.SURVIVAL);
        server.getPlayerManager().sendToDimension(new EntitySetHeadYawS2CPacket(instance, (byte) (instance.headYaw * 256 / 360)), dimensionId);
        server.getPlayerManager().sendToDimension(new EntityPositionS2CPacket(instance), dimensionId);
        instance.dataTracker.set(PLAYER_MODEL_PARTS, (byte) 0x7f);
        instance.getAbilities().flying = flying;
        return instance;
    }

    private HivemindBodyEntity(MinecraftServer server, ServerWorld worldIn, GameProfile profile, boolean shadow) {
        super(server, worldIn, profile);
        isAShadow = shadow;
    }

    @Override
    public void onEquipStack(final EquipmentSlot slot, final ItemStack previous, final ItemStack stack) {
        if (!isUsingItem()) super.onEquipStack(slot, previous, stack);
    }

    @Override
    public void kill() {
        kill(Text.literal("Killed"));
    }

    public void kill(Text reason) {
        shakeOff();
        this.server.send(new ServerTask(this.server.getTicks(), () -> {
            this.networkHandler.onDisconnected(reason);
        }));
    }

    @Override
    public void tick() {
        if (Objects.requireNonNull(this.getServer()).getTicks() % 10 == 0) {
            this.networkHandler.syncWithPlayerPosition();
            this.getWorld().getChunkManager().updatePosition(this);
            onTeleportationDone();
        }
        try {
            super.tick();
            this.playerTick();
        }
        catch (NullPointerException ignored) {
        }
    }

    private void shakeOff() {
        if (getVehicle() instanceof PlayerEntity) stopRiding();
        for (Entity passenger : getPassengersDeep()) {
            if (passenger instanceof PlayerEntity) passenger.stopRiding();
        }
    }

    @Override
    public void onDeath(DamageSource cause) {
        shakeOff();
        BodyOwnerComponent component = BodyOwnerComponent.KEY.get(this);
        Entity entity = this.world.getEntityLookup().get(component.getOwner());
        if (entity instanceof LivingEntity livingEntity) {
            PowerHolderComponent powerHolderComponent = PowerHolderComponent.KEY.get(livingEntity);
            for (BodyManagementPower power : powerHolderComponent.getPowers(BodyManagementPower.class)) {
                power.removeBody(this);
            }
            powerHolderComponent.sync();
        }
        super.onDeath(cause);
        setHealth(20);
        this.hungerManager = new HungerManager();
        kill(this.getDamageTracker().getDeathMessage());
    }

    @Override
    public String getIp() {
        return "127.0.0.1";
    }

}
