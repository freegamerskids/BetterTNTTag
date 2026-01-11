package com.lolwm.bettertnttag.client.mixin;

import com.lolwm.bettertnttag.client.BettertnttagClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class ZombieGlowMixin {

    @Inject(method = "getTeamColorValue()I", at = @At("RETURN"), cancellable = true)
    private void modifyZombieGlowColor(CallbackInfoReturnable<Integer> cir) {
        Entity entity = (Entity) (Object) this;

        if (entity.getType() == net.minecraft.entity.EntityType.ZOMBIE) {
            BettertnttagClient client = BettertnttagClient.getInstance();
            int entityId = entity.getId();

            if (client.isTrackedZombie(entityId)) {
                boolean hasTNT = client.hasZombieTNT(entityId);
                int color = hasTNT ? 0xFFFF0000 : 0xFFFFFFFF;
                cir.setReturnValue(color);
            }
        }
    }
}
