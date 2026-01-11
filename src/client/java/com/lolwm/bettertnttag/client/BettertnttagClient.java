package com.lolwm.bettertnttag.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.PlayerListEntry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import com.lolwm.bettertnttag.client.exception.TNTTagApiException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class BettertnttagClient implements ClientModInitializer {

    private static BettertnttagClient instance;

    public static final Logger LOGGER = LoggerFactory.getLogger("BetterTNTTag");

    private TNTTagApiClient apiClient;
    private PlayingState currentState = PlayingState.NOT_PLAYING;
    private RoundState roundState = RoundState.ONGOING;
    private int currentRound = 0;
    private long gameTimer = 0; // Timer in milliseconds
    private long lastUpdateTime = 0;

    private @Nullable String statusMessage = null;
    private ScheduledExecutorService statusMessageExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> statusMessageTimeout;

    // Player win data cache
    private final Map<String, Integer> playerWinsCache = new ConcurrentHashMap<>();
    private final Map<String, Long> playerCacheTimestamps = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = 30 * 60 * 1000; // 30 minutes
    private long lastPlayerCheck = 0;
    private static final long PLAYER_CHECK_INTERVAL_MS = 1000;

    // Zombie tracking
    private final Set<Integer> trackedZombies = new HashSet<>();
    private final Map<Integer, Boolean> zombieTNTStatus = new ConcurrentHashMap<>();
    private final Map<Integer, String> zombieNames = new ConcurrentHashMap<>();
    private long lastZombieCheck = 0;
    private static final long ZOMBIE_CHECK_INTERVAL_MS = 1000; // Check every second

    private static final Map<Integer, String> WIN_COLORS = Map.ofEntries(
        Map.entry(10000, "1"),  // dark blue
        Map.entry(5000, "0"),   // black
        Map.entry(2500, "c"),   // red
        Map.entry(1500, "6"),   // gold
        Map.entry(1000, "5"),   // dark purple
        Map.entry(500, "9"),    // blue
        Map.entry(250, "a"),    // green
        Map.entry(100, "2"),    // dark green
        Map.entry(50, "f"),     // white/light gray
        Map.entry(15, "7"),     // gray
        Map.entry(0, "8")       // dark gray
    );

    private static final Pattern POWERUP_REGEX = Pattern.compile("TELEPORT|REPULSOR|FROG LEGS|RANDOM|INVISIBILITY|SHIELD|SNOWBALL");

    @Override
    public void onInitializeClient() {
        instance = this;

        this.apiClient = new TNTTagApiClient();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            updateTimer();

            if (currentState != PlayingState.NOT_PLAYING && (lastPlayerCheck + PLAYER_CHECK_INTERVAL_MS) < System.currentTimeMillis()) {
                if (client.getNetworkHandler() != null) {
                    var playerList = client.getNetworkHandler().getPlayerList();
                    List<String> playerNames = playerList.stream()
                            .map(PlayerListEntry::getProfile)
                            .filter(Objects::nonNull)
                            .map(GameProfile::name)
                            .filter(name -> playerWinsCache.get(name) == null)
                            .distinct()
                            .toList();

                    if (!playerNames.isEmpty()) {
                        fetchAndCachePlayerWins(playerNames);
                    }
                    lastPlayerCheck = System.currentTimeMillis();
                }
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            resetToDefaults();
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            resetToDefaults();
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            shutdown();
        });
    }

    public TNTTagApiClient getApiClient() {
        return apiClient;
    }

    public static BettertnttagClient getInstance() {
        return instance;
    }

    private void resetToDefaults() {
        currentState = PlayingState.NOT_PLAYING;
        roundState = RoundState.ONGOING;
        gameTimer = 0;
        lastUpdateTime = 0;

        currentRound = 0;
        statusMessage = null;

        if (statusMessageTimeout != null && !statusMessageTimeout.isDone()) {
            statusMessageTimeout.cancel(false);
            statusMessageTimeout = null;
        }

        trackedZombies.clear();
        zombieTNTStatus.clear();
        zombieNames.clear();
        lastZombieCheck = 0;
    }

    public void shutdown() {
        statusMessageExecutor.shutdown();
        try {
            if (!statusMessageExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                statusMessageExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            statusMessageExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public Integer getCachedPlayerWins(String playerName) {
        Long timestamp = playerCacheTimestamps.get(playerName);
        if (timestamp == null || System.currentTimeMillis() - timestamp > CACHE_DURATION_MS) {
            // Cache expired or doesn't exist
            playerWinsCache.remove(playerName);
            playerCacheTimestamps.remove(playerName);
            return null;
        }
        return playerWinsCache.get(playerName);
    }

    public void cachePlayerWins(String playerName, int wins) {
        playerWinsCache.put(playerName, wins);
        playerCacheTimestamps.put(playerName, System.currentTimeMillis());
    }

    public void fetchAndCachePlayerWins(List<String> playerNames) {
        if (playerNames.isEmpty()) return;

        List<String> playersToFetch = playerNames.stream()
                .filter(name -> getCachedPlayerWins(name) == null)
                .toList();

        if (playersToFetch.isEmpty()) return;

        var minecraft = net.minecraft.client.MinecraftClient.getInstance();
        if (minecraft.getNetworkHandler() == null) return;

        var uuidToNameMap = new java.util.HashMap<String, String>();

        List<String> uuidsToFetch = playersToFetch.stream()
                .map(playerName -> {
                    var playerListEntry = minecraft.getNetworkHandler().getPlayerList().stream()
                            .filter(entry -> playerName.equals(entry.getProfile().name()))
                            .findFirst()
                            .orElse(null);

                    String uuid;
                    if (playerListEntry != null) {
                        try {
                            uuid = playerListEntry.getProfile().id().toString();
                        } catch (Exception e) {
                            uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes()).toString();
                            LOGGER.debug("Using generated UUID for player {}: {}", playerName, uuid);
                        }
                    } else {
                        uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes()).toString();
                        LOGGER.debug("Player {} not in player list, using generated UUID: {}", playerName, uuid);
                    }

                    uuidToNameMap.put(uuid, playerName);
                    return uuid;
                })
                .toList();

        if (uuidsToFetch.isEmpty()) return;

        try {
            apiClient.getMultipleUsers(uuidsToFetch).thenAccept(response -> {
                if (response.isSuccess() && response.getUsers() != null) {
                    for (var userWins : response.getUsers()) {
                        String playerName = uuidToNameMap.get(userWins.getUuid());
                        if (playerName != null) {
                            cachePlayerWins(playerName, userWins.getWins());
                        }
                    }
                } else {
                    LOGGER.warn("Failed to fetch player wins: {}", response.getError());
                }
            }).exceptionally(throwable -> {
                LOGGER.error("Error fetching player wins", throwable);
                return null;
            });
        } catch (TNTTagApiException e) {
            LOGGER.error("Failed to create API request for player wins", e);
        }
    }

    public String getPlayerNameWithWins(String playerName) {
        String realPlayerName = playerName.replaceAll("§.", "").replaceAll("\\[IT\\] ", "");
        Integer wins = getCachedPlayerWins(realPlayerName);
        if (wins != null) {
            String colorCode = getWinColorCode(wins);
            String playerColor = playerName.contains("[IT] ") ? "§c" : (playerName.contains("§7") ? "§7" : "");
            return playerColor + playerName + "§r §" + colorCode + "[" + wins + "]§r";
        }
        return playerName;
    }

    private String getWinColorCode(int wins) {
        return WIN_COLORS.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getKey(), e1.getKey()))
                .filter(entry -> wins >= entry.getKey())
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse("8");
    }

    public PlayingState getCurrentState() {
        return currentState;
    }

    public RoundState getRoundState() {
        return roundState;
    }

    public boolean isTrackedZombie(int entityId) {
        return trackedZombies.contains(entityId);
    }

    public boolean hasZombieTNT(int entityId) {
        return zombieTNTStatus.getOrDefault(entityId, false);
    }

    public String getZombieName(int entityId) {
        return zombieNames.get(entityId);
    }

    public long getGameTimer() {
        return gameTimer; // Convert to seconds
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public @Nullable String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String message) {
        if (statusMessageTimeout != null && !statusMessageTimeout.isDone()) {
            statusMessageTimeout.cancel(false);
        }

        this.statusMessage = message;

        if (message != null) {
            statusMessageTimeout = statusMessageExecutor.schedule(() -> {
                this.statusMessage = null;
                LOGGER.debug("Status message cleared due to timeout");
            }, 5, TimeUnit.SECONDS);
        }
    }

    public void setCurrentRound(int round) {
        this.currentRound = round;
    }

    public void setCurrentState(PlayingState state) {
        if (this.currentState != state) {
            this.currentState = state;
        }
    }

    public void setRoundState(RoundState state) {
        if (this.roundState != state) {
            this.roundState = state;
        }
    }

    public void setGameTimer(String timeString) {
        try {
            long newTimer = 0;

            if (timeString.contains(":")) {
                // Format: mm:ss
                String[] parts = timeString.split(":");
                if (parts.length == 2) {
                    int minutes = Integer.parseInt(parts[0]);
                    int seconds = Integer.parseInt(parts[1]);
                    newTimer = (minutes * 60 + seconds) * 1000L;
                }
            } else if (timeString.endsWith("s")) {
                // Format: XXs
                String secondsStr = timeString.substring(0, timeString.length() - 1);
                int seconds = Integer.parseInt(secondsStr);
                newTimer = seconds * 1000L;
            }

            if (Math.abs(this.gameTimer - newTimer) > 500) {
                this.gameTimer = newTimer;
                this.lastUpdateTime = System.currentTimeMillis();
            }
        } catch (NumberFormatException e) {
            LOGGER.warn("Failed to parse timer string: {}", timeString);
        }
    }

    private void updateTimer() {
        long currentTime = System.currentTimeMillis();

        if (currentState == PlayingState.WAITING || currentState == PlayingState.PLAYING || currentState == PlayingState.DEAD) {
            long deltaTime = currentTime - lastUpdateTime;

            if (deltaTime >= 100) { // Update every 100ms
                gameTimer = Math.max(0, gameTimer - deltaTime);

                if (gameTimer <= 0) {
                    setRoundState(RoundState.INTERMISSION);
                    gameTimer = 10500;
                }

                lastUpdateTime = currentTime;
            }
        }

        if (currentTime - lastZombieCheck >= ZOMBIE_CHECK_INTERVAL_MS) {
            checkForZombies();
            lastZombieCheck = currentTime;
        }
    }

    private void checkForZombies() {
        if (currentState == PlayingState.NOT_PLAYING) return;

        var client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        var entities = client.world.getEntities();

        for (var entity : entities) {
            if (entity.getType() != EntityType.ZOMBIE) continue;
            if (!(entity instanceof net.minecraft.entity.LivingEntity livingEntity)) continue;
            if (livingEntity.isBaby()) continue;

            int entityId = entity.getId();

            if (!trackedZombies.contains(entityId)) {
                boolean hasTNT = checkZombieHasTNT(entity);

                String playerName = extractZombiePlayerName(entity);
                if (POWERUP_REGEX.matcher(playerName).find()) continue;

                trackedZombies.add(entityId);
                zombieTNTStatus.put(entityId, hasTNT);
                zombieNames.put(entityId, playerName);

                if (!playerName.isEmpty()) {
                    String statusMessage = "§c" + playerName + "§r has turned into a zombie!";
                    setStatusMessage(statusMessage);
                }
            } else {
                boolean hasTNT = checkZombieHasTNT(entity);
                zombieTNTStatus.put(entityId, hasTNT);
            }
        }

        trackedZombies.removeIf(entityId -> {
            var entity = client.world.getEntityById(entityId);
            if (entity == null || !entity.isAlive()) {
                zombieTNTStatus.remove(entityId);
                zombieNames.remove(entityId);
                return true;
            }
            return false;
        });
    }

    private boolean checkZombieHasTNT(net.minecraft.entity.Entity entity) {
        if (!(entity instanceof net.minecraft.entity.LivingEntity livingEntity)) return false;

        var armorSlots = new net.minecraft.entity.EquipmentSlot[]{
                net.minecraft.entity.EquipmentSlot.HEAD,
                net.minecraft.entity.EquipmentSlot.CHEST,
                net.minecraft.entity.EquipmentSlot.LEGS,
                net.minecraft.entity.EquipmentSlot.FEET
        };

        for (var slot : armorSlots) {
            var itemStack = livingEntity.getEquippedStack(slot);
            if (itemStack != null && !itemStack.isEmpty() && itemStack.getItem() == net.minecraft.item.Items.TNT) return true;
        }

        var mainHand = livingEntity.getEquippedStack(net.minecraft.entity.EquipmentSlot.MAINHAND);
        if (mainHand != null && !mainHand.isEmpty() && mainHand.getItem() == net.minecraft.item.Items.TNT) return true;

        var offHand = livingEntity.getEquippedStack(net.minecraft.entity.EquipmentSlot.OFFHAND);
        if (offHand != null && !offHand.isEmpty() && offHand.getItem() == net.minecraft.item.Items.TNT) return true;

        return false;
    }

    private String extractZombiePlayerName(net.minecraft.entity.Entity entity) {
        var client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.world != null) {
            List<Entity> armorStands = new ArrayList<>();
            client.world.getEntities().forEach(armorStands::add);
            Optional<Entity> nearest = armorStands.stream()
                    .filter(e -> e.getType() == EntityType.ARMOR_STAND)
                    .min(Comparator.comparingDouble(e -> e.squaredDistanceTo(entity)));

            if (nearest.isPresent()) {
                return nearest.get().getName().getString();
            }
        }

        return "Unknown Player";
    }
}
