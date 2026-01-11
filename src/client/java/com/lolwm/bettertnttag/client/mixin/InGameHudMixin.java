package com.lolwm.bettertnttag.client.mixin;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;

import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lolwm.bettertnttag.client.BettertnttagClient;
import net.minecraft.client.MinecraftClient;
import com.lolwm.bettertnttag.client.PlayingState;
import com.lolwm.bettertnttag.client.RoundState;


@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "renderStatusBars(Lnet/minecraft/client/gui/DrawContext;)V", at = @At("HEAD"), cancellable = true)
    private void renderStatusBars(DrawContext drawCtx, CallbackInfo ci) {
        BettertnttagClient client = BettertnttagClient.getInstance();
        PlayingState currentState = client.getCurrentState();

        if (currentState != PlayingState.NOT_PLAYING) {
            ci.cancel();

            renderTimerBar(drawCtx);
        }
    }

    @Unique
    private int getTotalSeconds(RoundState roundState) {
        int totalSeconds;
        if (roundState == RoundState.INTERMISSION) {
            totalSeconds = 11_000;
        } else {
            totalSeconds = Math.max(30, (60 - (BettertnttagClient.getInstance().getCurrentRound() * 5))) * 1000;
        }
        return totalSeconds;
    }

    @Unique
    private void renderTimerBar(DrawContext drawCtx) {
        BettertnttagClient client = BettertnttagClient.getInstance();
        RoundState roundState = client.getRoundState();

        long remainingSeconds = client.getGameTimer();
        int totalSeconds = getTotalSeconds(roundState);

        float progress = Math.max(0.0f, Math.min(1.0f, (float) remainingSeconds / totalSeconds));

        int screenWidth = drawCtx.getScaledWindowWidth();
        int screenHeight = drawCtx.getScaledWindowHeight();

        int barWidth = 180;
        int barHeight = 8;
        int barX = screenWidth / 2 - barWidth / 2;
        int barY = screenHeight - 35;

        drawCtx.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF000000);

        // progress bar
        int progressWidth = (int) (barWidth * progress);
        int progressColor = getTimerBarColor(roundState, remainingSeconds);
        int progressLeft = barX + (barWidth - progressWidth) / 2;
        int progressRight = progressLeft + progressWidth;
        drawCtx.fill(progressLeft, barY, progressRight, barY + barHeight, progressColor);

        // border
        drawCtx.fill(barX - 1, barY - 1, barX + barWidth + 1, barY, 0xFFFFFFFF); // outline top
        drawCtx.fill(barX - 1, barY + barHeight, barX + barWidth + 1, barY + barHeight + 1, 0xFFFFFFFF); // outline bottom
        drawCtx.fill(barX - 1, barY, barX, barY + barHeight, 0xFFFFFFFF); // outline left
        drawCtx.fill(barX + barWidth, barY, barX + barWidth + 1, barY + barHeight, 0xFFFFFFFF); // outline right
    }

    @Unique
    private int getTimerBarColor(RoundState roundState, long gameTimer) {
        if (roundState == RoundState.INTERMISSION) {
            return gameTimer > 5_000 ? 0xFFFFFF00 : 0xFFFFA500;
        } else {
            if (gameTimer > 15_000) {
                return 0xFF00FF00; // green
            } else if (gameTimer > 5_000) {
                return 0xFFFFA500; // orange
            } else {
                return 0xFFFF0000; // red
            }
        }
    }

    @Unique
    private String getOverlayMessageColor(RoundState roundState, long gameTimer) {
        if (roundState == RoundState.INTERMISSION) {
            return gameTimer > 5_000 ? "§f" : "§6";
        } else {
            if (gameTimer > 15_000) {
                return "§a"; // green
            } else if (gameTimer > 5_000) {
                return "§6"; // orange
            } else {
                return "§c"; // red
            }
        }
    }

    @Inject(method = "getCurrentBarType()Lnet/minecraft/client/gui/hud/InGameHud$BarType;", at = @At("HEAD"), cancellable = true)
    private void getCurrentBarType(CallbackInfoReturnable<InGameHud.BarType> cir) {
        if (BettertnttagClient.getInstance().getCurrentState() != PlayingState.NOT_PLAYING) {
            cir.setReturnValue(InGameHud.BarType.EMPTY);
        }
    }

    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At("HEAD"), cancellable = true)
    private void renderScoreboardSidebar(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
        assert MinecraftClient.getInstance().world != null;
        Scoreboard scoreboard = MinecraftClient.getInstance().world.getScoreboard();
        if (objective != null) {
            String objectiveName = objective.getName();
            if (objective.getDisplayName().getString().contains("TNT TAG")) {
                StringBuilder scoreboardContent = new StringBuilder();

                scoreboardContent.append(objective.getDisplayName().getString()).append("\n");

                for (var entry : scoreboard.getScoreboardEntries(objective)) {
                    Team team = scoreboard.getScoreHolderTeam(entry.owner());
                    Text text = entry.name();
                    Text text2 = Team.decorateName(team, text);
                    scoreboardContent.append(text2.getString().replaceAll("§.", "")).append("\n");
                }

                String content = scoreboardContent.toString();
                
                if (objectiveName.contains("PreScoreboard")){
                    BettertnttagClient.getInstance().setCurrentState(PlayingState.WAITING);
                    extractStartingTimer(content);
                }

                if (objectiveName.contains("TNT")) {
                    if (content.contains("Round #")) {
                        int startIndex = scoreboardContent.indexOf("Round #");
                        if (startIndex != -1) {
                            String afterStartingIn = scoreboardContent.substring(startIndex + "Round #".length()).trim();
                            String[] lines = afterStartingIn.split("\n");
                            if (lines.length > 0) {
                                String roundLine = lines[0].trim();
                                BettertnttagClient.getInstance().setCurrentRound(Integer.parseInt(roundLine));
                            }
                        }
                    }

                    if (content.contains("Explosion in 0s")) {
                        BettertnttagClient.getInstance().setRoundState(RoundState.INTERMISSION);
                    } else {
                        BettertnttagClient.getInstance().setRoundState(RoundState.ONGOING);
                    }

                    if (content.contains("Goal:")) {
                        BettertnttagClient.getInstance().setCurrentState(PlayingState.PLAYING);
                        extractExplosionTimer(content);
                    } else {
                        BettertnttagClient.getInstance().setCurrentState(PlayingState.DEAD);
                        extractExplosionTimer(content);
                    }
                }
            } else {
                BettertnttagClient.getInstance().setCurrentState(PlayingState.NOT_PLAYING);
            }
        } else {
            BettertnttagClient.getInstance().setCurrentState(PlayingState.NOT_PLAYING);
        }
    }

    @Inject(method = "renderOverlayMessage(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At("HEAD"))
    private void renderOverlayMessage(DrawContext context, RenderTickCounter renderTickCounter, CallbackInfo ci) {
        BettertnttagClient client = BettertnttagClient.getInstance();
        PlayingState currentState = client.getCurrentState();
        RoundState roundState = client.getRoundState();
        InGameHud hud = (InGameHud) (Object) this;
        if (currentState != PlayingState.NOT_PLAYING) {
            hud.setOverlayMessage(Text.of((currentState == PlayingState.WAITING ? "Starting in " : (roundState == RoundState.ONGOING ? "Explosion in " : "Next round in "))
            + getOverlayMessageColor(roundState, client.getGameTimer())
            + String.format("%.1f", client.getGameTimer() / 1000.0f) + "s§r"), false);
        }
    }

    @Inject(method = "renderOverlayMessage(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithBackground(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIII)V"))
    private void renderStatusMessage(DrawContext context, RenderTickCounter renderTickCounter, CallbackInfo ci) {
        BettertnttagClient client = BettertnttagClient.getInstance();
        PlayingState currentState = client.getCurrentState();
        InGameHud hud = (InGameHud) (Object) this;
        if (currentState != PlayingState.NOT_PLAYING && client.getStatusMessage() != null) {
            TextRenderer textRenderer = hud.getTextRenderer();
            int width = textRenderer.getWidth(client.getStatusMessage());
            context.drawTextWithBackground(textRenderer, Text.of(client.getStatusMessage()), -width / 2, 6, width, ColorHelper.whiteWithAlpha(255));
        }
    }

    @ModifyVariable(method = "renderHeldItemTooltip(Lnet/minecraft/client/gui/DrawContext;)V", ordinal = 2, index = 3, name = "k", at = @At("LOAD"))
    private int modifyYTooltipPosition(int y) {
        if (BettertnttagClient.getInstance().getCurrentState() != PlayingState.NOT_PLAYING) {
            return y + 10;
        }
        return y;
    }

    @Unique
    private String lastStartingTimerContent = "";
    @Unique
    private void extractStartingTimer(String scoreboardContent) {
        int startIndex = scoreboardContent.indexOf("Starting in");
        if (startIndex != -1) {
            String afterStartingIn = scoreboardContent.substring(startIndex + "Starting in".length()).trim();
            String[] lines = afterStartingIn.split(" ");
            if (lines.length > 0) {
                String timerLine = lines[0].trim();
                if (timerLine.equals(lastStartingTimerContent)) return;
                lastStartingTimerContent = timerLine;
                BettertnttagClient.getInstance().setGameTimer(timerLine);
            }
        }
    }

    @Unique
    private String lastExplosionTimerContent = "";
    @Unique
    private void extractExplosionTimer(String scoreboardContent) {
        int explosionIndex = scoreboardContent.indexOf("Explosion in");
        if (explosionIndex != -1) {
            String afterExplosionIn = scoreboardContent.substring(explosionIndex + "Explosion in".length()).trim();
            String[] lines = afterExplosionIn.split("\n");
            if (lines.length > 0) {
                String timerLine = lines[0].trim();
                if (timerLine.equals(lastExplosionTimerContent)) return;
                lastExplosionTimerContent = timerLine;
                if (timerLine.contains("0s") && BettertnttagClient.getInstance().getRoundState() == RoundState.INTERMISSION) return;
                BettertnttagClient.getInstance().setGameTimer(timerLine);
            }
        }
    }
}