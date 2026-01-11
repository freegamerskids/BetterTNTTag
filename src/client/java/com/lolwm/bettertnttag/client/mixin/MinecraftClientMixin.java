package com.lolwm.bettertnttag.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.lolwm.bettertnttag.client.BettertnttagClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @ModifyReturnValue(method = "hasOutline", at = @At("RETURN"))
    private boolean hasOutline(boolean original, Entity entity) {
        BettertnttagClient client = BettertnttagClient.getInstance();

        return client.isTrackedZombie(entity.getId()) || original;
    }
}
