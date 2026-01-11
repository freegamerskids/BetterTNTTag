package com.lolwm.bettertnttag.client.mixin;

import com.lolwm.bettertnttag.client.BettertnttagClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

    @Inject(method = "getPlayerName(Lnet/minecraft/client/network/PlayerListEntry;)Lnet/minecraft/text/Text;",
            at = @At("RETURN"), cancellable = true)
    private void modifyPlayerName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        Text originalText = cir.getReturnValue();
        if (originalText != null) {
            String playerName = originalText.getString();
            String modifiedName = BettertnttagClient.getInstance().getPlayerNameWithWins(playerName);
            if (!modifiedName.equals(playerName)) {
                cir.setReturnValue(Text.of(modifiedName));
            }
        }
    }
}
