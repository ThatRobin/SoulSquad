package io.github.thatrobin.soul_squad.client;

import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.registry.ModComponents;
import io.github.thatrobin.soul_squad.SoulSquad;
import io.github.thatrobin.soul_squad.networking.HivemindPacketS2C;
import io.github.thatrobin.soul_squad.screens.BodySelectionScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class SoulSquadClient implements ClientModInitializer {

    public static KeyBinding SWAP_BLOCK = new KeyBinding("key.poltergeist.swap_block", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category." + SoulSquad.MODID);
    public static KeyBinding CYCLE_BODIES = new KeyBinding("key.hivemind.cycle_bodies", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, "category." + SoulSquad.MODID);
    public static KeyBinding OPEN_CYCLE_SCREEN = new KeyBinding("key.hivemind.open_cycle_screen", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, "category." + SoulSquad.MODID);


    @Override
    public void onInitializeClient() {
        HivemindPacketS2C.register();

        ApoliClient.registerPowerKeybinding("key.poltergeist.swap_block", SWAP_BLOCK);
        KeyBindingHelper.registerKeyBinding(SWAP_BLOCK);
        ApoliClient.registerPowerKeybinding("key.hivemind.cycle_bodies", CYCLE_BODIES);
        KeyBindingHelper.registerKeyBinding(CYCLE_BODIES);
        ApoliClient.registerPowerKeybinding("key.hivemind.open_cycle_screen", OPEN_CYCLE_SCREEN);
        KeyBindingHelper.registerKeyBinding(OPEN_CYCLE_SCREEN);

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if(entityRenderer instanceof PlayerEntityRenderer playerEntityRenderer) {
                registrationHelper.register(new PoltergeistFeatureRenderer<>(playerEntityRenderer, context.getModelLoader()));
                registrationHelper.register(new PoltergeistOuterFeatureRenderer<>(playerEntityRenderer, context.getModelLoader()));
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_CYCLE_SCREEN.wasPressed()) {
                if (client.player != null && ModComponents.ORIGIN.get(client.player).getOrigin(OriginLayers.getLayer(Origins.identifier("origin"))).getIdentifier().equals(new Identifier(SoulSquad.MODID,"hivemind"))) {
                    MinecraftClient.getInstance().setScreen(new BodySelectionScreen());
                }
            }
        });
    }
}
