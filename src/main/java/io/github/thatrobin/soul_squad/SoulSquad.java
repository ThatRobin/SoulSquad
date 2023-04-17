package io.github.thatrobin.soul_squad;

import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.component.PlayerOriginComponent;
import io.github.apace100.origins.content.OrbOfOriginItem;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.registry.ModComponents;
import io.github.thatrobin.soul_squad.component.*;
import io.github.thatrobin.soul_squad.networking.HivemindPacketC2S;
import io.github.thatrobin.soul_squad.powers.HivemindPowerTypes;
import io.github.thatrobin.soul_squad.powers.PoltergeistPowerTypes;
import io.github.thatrobin.soul_squad.powers.factories.BlockConditions;
import io.github.thatrobin.soul_squad.powers.factories.EntityActions;
import io.github.thatrobin.soul_squad.powers.factories.EntityConditions;
import io.github.thatrobin.soul_squad.powers.factories.ItemConditions;
import io.github.thatrobin.soul_squad.screens.BodySelectionScreen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class SoulSquad implements ModInitializer, EntityComponentInitializer {

    public static final String MODID = "soul_squad";
    public static final Logger LOGGER = LogManager.getLogger(SoulSquad.class);
    public static final Item CUBE_OF_ORIGIN = new OrbOfOriginItem();

    public static KeyBinding SWAP_BLOCK = new KeyBinding("key.poltergeist.swap_block", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category." + MODID);
    public static KeyBinding CYCLE_BODIES = new KeyBinding("key.hivemind.cycle_bodies", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, "category." + MODID);
    public static KeyBinding OPEN_CYCLE_SCREEN = new KeyBinding("key.hivemind.open_cycle_screen", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, "category." + MODID);

    public static final TagKey<Block> UNPOSSESSABLE_BLOCKS = TagKey.of(Registries.BLOCK.getKey(), new Identifier(MODID, "unpossessable_blocks"));
    public static final TagKey<Block> MINEABLE_COMBO = TagKey.of(Registries.BLOCK.getKey(), new Identifier(MODID, "mineable_combo"));

    @Override
    public void onInitialize() {
        ApoliClient.registerPowerKeybinding("key.poltergeist.swap_block", SWAP_BLOCK);
        KeyBindingHelper.registerKeyBinding(SWAP_BLOCK);
        ApoliClient.registerPowerKeybinding("key.hivemind.cycle_bodies", CYCLE_BODIES);
        KeyBindingHelper.registerKeyBinding(CYCLE_BODIES);
        ApoliClient.registerPowerKeybinding("key.hivemind.open_cycle_screen", OPEN_CYCLE_SCREEN);
        KeyBindingHelper.registerKeyBinding(OPEN_CYCLE_SCREEN);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_CYCLE_SCREEN.wasPressed()) {
                if (client.player != null && ModComponents.ORIGIN.get(client.player).getOrigin(OriginLayers.getLayer(Origins.identifier("origin"))).getIdentifier().equals(SoulSquad.hivemind("hivemind"))) {
                    MinecraftClient.getInstance().setScreen(new BodySelectionScreen());
                }
            }
        });

        HivemindPacketC2S.register();
        BlockConditions.register();
        EntityActions.register();
        EntityConditions.register();
        ItemConditions.register();
        HivemindPowerTypes.register();
        PoltergeistPowerTypes.register();

        Registry.register(Registries.ITEM, new Identifier(MODID, "cube_of_origin"), CUBE_OF_ORIGIN);
    }

    public static Identifier hivemind(String id) {
        return new Identifier("hivemind", id);
    }

    public static Identifier poltergeist(String id) {
        return new Identifier("poltergeist", id);
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(LivingEntity.class, BlockDurabilityComponent.KEY)
                .impl(BlockDurabilityComponentImpl.class)
                .respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY)
                .end(BlockDurabilityComponentImpl::new);

        registry.beginRegistration(PlayerEntity.class, BodyOwnerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY)
                .end(BodyOwnerComponentImpl::new);

        registry.beginRegistration(PlayerEntity.class, BodyHolderComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY)
                .end(BodyHolderComponentImpl::new);
    }
}
