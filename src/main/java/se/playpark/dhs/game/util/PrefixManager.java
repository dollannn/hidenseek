package se.playpark.dhs.game.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import se.playpark.dhs.Main;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Manages game state tracking for players during hide and seek games.
 * 
 * This class provides game state management with PlaceholderAPI integration
 * for flexible display of player roles without modifying permanent permissions.
 * 
 * Features:
 * - Game state tracking (seeker/hider/spectator)
 * - PlaceholderAPI integration for flexible display
 * - Optional display name prefix fallback
 * - Automatic cleanup on game end
 * - Thread-safe operations
 */
public class PrefixManager {

    // Game prefix constants
    private static final String SEEKER_PREFIX = "&c&lѕᴇᴇᴋᴇʀ &r";
    private static final String HIDER_PREFIX = "&9&lʜɪᴅᴇʀ &r";

    // Game state tracking for PlaceholderAPI
    private static final Map<UUID, GameRole> playerGameStates = new HashMap<>();

    /**
     * Enum representing player roles in the game
     */
    public enum GameRole {
        SEEKER("seeker", SEEKER_PREFIX, "&c&lSEEKER"),
        HIDER("hider", HIDER_PREFIX, "&9&lHIDER"),
        SPECTATOR("spectator", "&7&lѕᴘᴇᴄᴛᴀᴛᴏʀ &r", "&7&lSPECTATOR"),
        NONE("none", "", "");

        private final String identifier;
        private final String prefix;
        private final String displayName;

        GameRole(String identifier, String prefix, String displayName) {
            this.identifier = identifier;
            this.prefix = prefix;
            this.displayName = displayName;
        }

        public String getIdentifier() {
            return identifier;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Set a player as a seeker
     * 
     * @param player The player to set as seeker
     * @return CompletableFuture<Boolean> indicating success
     */
    public static CompletableFuture<Boolean> setSeekerPrefix(Player player) {
        setPlayerGameState(player, GameRole.SEEKER);
        return setDisplayNamePrefix(player, SEEKER_PREFIX);
    }

    /**
     * Set a player as a hider
     * 
     * @param player The player to set as hider
     * @return CompletableFuture<Boolean> indicating success
     */
    public static CompletableFuture<Boolean> setHiderPrefix(Player player) {
        setPlayerGameState(player, GameRole.HIDER);
        return setDisplayNamePrefix(player, HIDER_PREFIX);
    }

    /**
     * Set a player as a spectator
     * 
     * @param player The player to set as spectator
     * @return CompletableFuture<Boolean> indicating success
     */
    public static CompletableFuture<Boolean> setSpectatorRole(Player player) {
        setPlayerGameState(player, GameRole.SPECTATOR);
        return setDisplayNamePrefix(player, GameRole.SPECTATOR.getPrefix());
    }

    /**
     * Remove game role and reset display name
     * 
     * @param player The player to remove game role from
     * @return CompletableFuture<Boolean> indicating success
     */
    public static CompletableFuture<Boolean> removeGamePrefix(Player player) {
        setPlayerGameState(player, GameRole.NONE);
        return resetDisplayName(player);
    }

    // === GAME STATE MANAGEMENT FOR PLACEHOLDERAPI ===

    /**
     * Set a player's game state (for PlaceholderAPI integration)
     * 
     * @param player The player
     * @param role   The game role
     */
    public static void setPlayerGameState(Player player, GameRole role) {
        if (role == GameRole.NONE) {
            playerGameStates.remove(player.getUniqueId());
        } else {
            playerGameStates.put(player.getUniqueId(), role);
        }
    }

    /**
     * Get a player's current game state
     * 
     * @param player The player
     * @return The player's current game role
     */
    public static GameRole getPlayerGameState(Player player) {
        return playerGameStates.getOrDefault(player.getUniqueId(), GameRole.NONE);
    }

    /**
     * Get a player's current game state by UUID
     * 
     * @param playerId The player's UUID
     * @return The player's current game role
     */
    public static GameRole getPlayerGameState(UUID playerId) {
        return playerGameStates.getOrDefault(playerId, GameRole.NONE);
    }

    /**
     * Check if a player is in a game
     * 
     * @param player The player
     * @return true if the player has an active game role
     */
    public static boolean isPlayerInGame(Player player) {
        return getPlayerGameState(player) != GameRole.NONE;
    }

    /**
     * Check if a player is a seeker
     * 
     * @param player The player
     * @return true if the player is a seeker
     */
    public static boolean isSeeker(Player player) {
        return getPlayerGameState(player) == GameRole.SEEKER;
    }

    /**
     * Check if a player is a hider
     * 
     * @param player The player
     * @return true if the player is a hider
     */
    public static boolean isHider(Player player) {
        return getPlayerGameState(player) == GameRole.HIDER;
    }

    /**
     * Check if a player is a spectator
     * 
     * @param player The player
     * @return true if the player is a spectator
     */
    public static boolean isSpectator(Player player) {
        return getPlayerGameState(player) == GameRole.SPECTATOR;
    }

    /**
     * Get placeholder value for PlaceholderAPI integration
     * 
     * @param player     The player
     * @param identifier The placeholder identifier
     * @return The placeholder value or null if not found
     */
    public static String getPlaceholderValue(Player player, String identifier) {
        GameRole role = getPlayerGameState(player);

        switch (identifier.toLowerCase()) {
            case "role":
                return role.getIdentifier();
            case "prefix":
                return role.getPrefix();
            case "display":
            case "displayname":
                return role.getDisplayName();
            case "colored_prefix":
                return role.getPrefix().replace("&", "§");
            case "colored_display":
                return role.getDisplayName().replace("&", "§");
            case "in_game":
                return String.valueOf(role != GameRole.NONE);
            case "is_seeker":
                return String.valueOf(role == GameRole.SEEKER);
            case "is_hider":
                return String.valueOf(role == GameRole.HIDER);
            case "is_spectator":
                return String.valueOf(role == GameRole.SPECTATOR);
            default:
                return null;
        }
    }

    /**
     * Clear all game states (call when game ends)
     */
    public static void clearAllGameStates() {
        playerGameStates.clear();
        Main.getInstance().getLogger().info("Cleared all game states");
    }

    // === DISPLAY NAME MANAGEMENT (OPTIONAL FALLBACK) ===

    /**
     * Set display name prefix (optional fallback for visual display)
     * 
     * @param player The player
     * @param prefix The prefix to set
     * @return CompletableFuture<Boolean> indicating success
     */
    private static CompletableFuture<Boolean> setDisplayNamePrefix(Player player, String prefix) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                    String coloredPrefix = prefix.replace("&", "§");
                    String originalName = player.getName();
                    String newDisplayName = coloredPrefix + originalName;

                    player.setDisplayName(newDisplayName);
                    player.setPlayerListName(newDisplayName);
                });
                return true;
            } catch (Exception e) {
                Main.getInstance().getLogger()
                        .warning("Failed to set display name for " + player.getName() + ": " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Reset display name to original
     * 
     * @param player The player
     * @return CompletableFuture<Boolean> indicating success
     */
    private static CompletableFuture<Boolean> resetDisplayName(Player player) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                    String originalName = player.getName();
                    player.setDisplayName(originalName);
                    player.setPlayerListName(originalName);
                });
                return true;
            } catch (Exception e) {
                Main.getInstance().getLogger()
                        .warning("Failed to reset display name for " + player.getName() + ": " + e.getMessage());
                return false;
            }
        });
    }

    // === UTILITY METHODS ===

    /**
     * Clean up stored data when player disconnects
     * Call this from a PlayerQuitEvent listener
     */
    public static void onPlayerDisconnect(Player player) {
        playerGameStates.remove(player.getUniqueId());
    }

    /**
     * Clear all stored data (cleanup method)
     */
    public static void cleanup() {
        playerGameStates.clear();
        Main.getInstance().getLogger().info("PrefixManager cleanup completed");
    }

    // === LEGACY PLACEHOLDER METHODS ===
    // These are kept for backward compatibility but do nothing

    /**
     * @deprecated Use PlaceholderAPI placeholders instead
     */
    @Deprecated
    public static void hideNameTag(Player player) {
        // Placeholder method - use PlaceholderAPI for display control
    }

    /**
     * @deprecated Use PlaceholderAPI placeholders instead
     */
    @Deprecated
    public static void showNameTag(Player player) {
        // Placeholder method - use PlaceholderAPI for display control
    }

    /**
     * @deprecated Use PlaceholderAPI placeholders instead
     */
    @Deprecated
    public static void hideNameTagFrom(Player player, Player viewer) {
        // Placeholder method - use PlaceholderAPI for display control
    }

    /**
     * @deprecated Use PlaceholderAPI placeholders instead
     */
    @Deprecated
    public static void showNameTagTo(Player player, Player viewer) {
        // Placeholder method - use PlaceholderAPI for display control
    }

    /**
     * @deprecated Use PlaceholderAPI placeholders instead
     */
    @Deprecated
    public static boolean isNameTagHidden(Player player) {
        return false;
    }

    /**
     * @deprecated LuckPerms integration removed
     */
    @Deprecated
    public static boolean isLuckPermsEnabled() {
        return false;
    }

    /**
     * @deprecated LuckPerms integration removed
     */
    @Deprecated
    public static boolean isTabEnabled() {
        return false;
    }

    /**
     * @deprecated Not implemented
     */
    @Deprecated
    public static boolean isNameTagEnabled() {
        return false;
    }

    /**
     * @deprecated LuckPerms integration removed
     */
    @Deprecated
    public static void refreshPlayer(Player player) {
        // No longer needed
    }
}