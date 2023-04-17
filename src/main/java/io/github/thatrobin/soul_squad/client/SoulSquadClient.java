package io.github.thatrobin.soul_squad.client;

import io.github.thatrobin.soul_squad.networking.HivemindPacketS2C;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.render.entity.PlayerEntityRenderer;

public class SoulSquadClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HivemindPacketS2C.register();
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if(entityRenderer instanceof PlayerEntityRenderer playerEntityRenderer) {
                registrationHelper.register(new PoltergeistFeatureRenderer<>(playerEntityRenderer, context.getModelLoader()));
                registrationHelper.register(new PoltergeistOuterFeatureRenderer<>(playerEntityRenderer, context.getModelLoader()));
            }
        });
    }
}
