package io.github.thatrobin.hivemind.mixin;

import com.mojang.authlib.GameProfile;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.thatrobin.hivemind.Hivemind;
import io.github.thatrobin.hivemind.component.BodyOwnerComponent;
import io.github.thatrobin.hivemind.entity.HivemindBodyEntity;
import io.github.thatrobin.hivemind.powers.BodyManagementPower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        BodyOwnerComponent component = BodyOwnerComponent.KEY.get(player);
        Entity entity = player.world.getEntityLookup().get(component.getOwner());
        if (entity instanceof LivingEntity livingEntity) {
            PowerHolderComponent powerHolderComponent = PowerHolderComponent.KEY.get(livingEntity);
            for (BodyManagementPower power : powerHolderComponent.getPowers(BodyManagementPower.class)) {
                power.removeBody((HivemindBodyEntity) player);
            }
            powerHolderComponent.sync();
        }
    }

}
