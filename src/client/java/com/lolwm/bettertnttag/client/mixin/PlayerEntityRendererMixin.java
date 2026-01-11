package com.lolwm.bettertnttag.client.mixin;

import com.lolwm.bettertnttag.client.BettertnttagClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    @ModifyVariable(method = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
                    at = @At("HEAD"), argsOnly = true)
    private PlayerEntityRenderState modifyPlayerName(PlayerEntityRenderState value) {
        if (value.displayName != null) {
            String playerName = value.displayName.getString();
            String modifiedName = BettertnttagClient.getInstance().getPlayerNameWithWins(playerName);

            if (!modifiedName.equals(playerName)) {
                value.displayName = net.minecraft.text.Text.of(modifiedName);
            }
        }
        return value;
    }
}
