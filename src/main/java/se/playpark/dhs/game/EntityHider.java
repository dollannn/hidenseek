package se.playpark.dhs.game;

import static com.comphenix.protocol.PacketType.Play.Server.*;

import java.util.Collections;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class EntityHider implements Listener {
    protected Table<Integer, Integer, Boolean> observerEntityMap = HashBasedTable.create();

    private static final PacketType[] ENTITY_PACKETS = {
            ENTITY_EQUIPMENT, ANIMATION,
            COLLECT, SPAWN_ENTITY, SPAWN_ENTITY_EXPERIENCE_ORB,
            ENTITY_VELOCITY, REL_ENTITY_MOVE, ENTITY_LOOK,
            ENTITY_TELEPORT, ENTITY_HEAD_ROTATION, ENTITY_STATUS, ATTACH_ENTITY, ENTITY_METADATA,
            ENTITY_EFFECT, REMOVE_ENTITY_EFFECT, BLOCK_BREAK_ANIMATION
    };

    public enum Policy {
        WHITELIST,
        BLACKLIST,
    }

    private ProtocolManager manager;

    private final Listener bukkitListener;
    private final PacketAdapter protocolListener;

    protected final Policy policy;

    public EntityHider(Plugin plugin, Policy policy) {
        Preconditions.checkNotNull(plugin, "plugin cannot be NULL.");

        // Save policy
        this.policy = policy;
        this.manager = ProtocolLibrary.getProtocolManager();

        // Register events and packet listener
        plugin.getServer().getPluginManager().registerEvents(
                bukkitListener = constructBukkit(), plugin);
        manager.addPacketListener(
                protocolListener = constructProtocol(plugin));
    }

    /**
     * Set the visibility status of a given entity for a particular observer.
     * 
     * @param observer - the observer player.
     * @param entityID - ID of the entity that will be hidden or made visible.
     * @param visible  - TRUE if the entity should be made visible, FALSE if not.
     * @return TRUE if the entity was visible before this method call, FALSE
     *         otherwise.
     */
    protected boolean setVisibility(Player observer, int entityID, boolean visible) {
        switch (policy) {
            case BLACKLIST:
                // Non-membership means they are visible
                return !setMembership(observer, entityID, !visible);
            case WHITELIST:
                return setMembership(observer, entityID, visible);
            default:
                throw new IllegalArgumentException("Unknown policy: " + policy);
        }
    }

    /**
     * Add or remove the given entity and observer entry from the table.
     * 
     * @param observer    - the player observer.
     * @param newEntityId - ID of the entity.
     * @param member      - TRUE if they should be present in the table, FALSE
     *                    otherwise.
     * @return TRUE if they already were present, FALSE otherwise.
     */
    protected boolean setMembership(Player observer, int newEntityId, boolean member) {
        int entityID;
        try {
            entityID = observer.getEntityId();
        } catch (Exception e) {
            return member;
        }
        if (member) {
            return observerEntityMap.put(newEntityId, entityID, true) != null;
        } else {
            return observerEntityMap.remove(newEntityId, entityID) != null;
        }
    }

    /**
     * Determine if the given entity and observer is present in the table.
     * 
     * @param observer    - the player observer.
     * @param newEntityID - ID of the entity.
     * @return TRUE if they are present, FALSE otherwise.
     */
    protected boolean getMembership(Player observer, int newEntityID) {
        int entityID;
        try {
            entityID = observer.getEntityId();
        } catch (Exception e) {
            return false;
        }
        return observerEntityMap.contains(entityID, newEntityID);
    }

    /**
     * Determine if a given entity is visible for a particular observer.
     * 
     * @param observer - the observer player.
     * @param entityID - ID of the entity that we are testing for visibility.
     * @return TRUE if the entity is visible, FALSE otherwise.
     */
    protected boolean isVisible(Player observer, int entityID) {
        // If we are using a whitelist, presence means visibility - if not, the opposite
        // is the case
        boolean presence = getMembership(observer, entityID);

        return (policy == Policy.WHITELIST) == presence;
    }

    /**
     * Remove the given entity from the underlying map.
     * 
     * @param entity - the entity to remove.
     */
    protected void removeEntity(Entity entity) {
        int entityID;
        try {
            entityID = entity.getEntityId();
        } catch (Exception e) {
            return;
        }

        for (Map<Integer, Boolean> maps : observerEntityMap.rowMap().values()) {
            maps.remove(entityID);
        }
    }

    /**
     * Invoked when a player logs out.
     * 
     * @param player - the player that jused logged out.
     */
    protected void removePlayer(Player player) {
        int entityID;
        try {
            entityID = player.getEntityId();
        } catch (Exception e) {
            return;
        }
        observerEntityMap.rowMap().remove(entityID);
    }

    /**
     * Construct the Bukkit event listener.
     * 
     * @return Our listener.
     */
    private Listener constructBukkit() {
        return new Listener() {
            @EventHandler
            public void onEntityDeath(EntityDeathEvent e) {
                removeEntity(e.getEntity());
            }

            @EventHandler
            public void onChunkUnload(ChunkUnloadEvent e) {
                for (Entity entity : e.getChunk().getEntities()) {
                    removeEntity(entity);
                }
            }

            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent e) {
                removePlayer(e.getPlayer());
            }
        };
    }

    /**
     * Construct the packet listener that will be used to intercept every
     * entity-related packet.
     * 
     * @param plugin - the parent plugin.
     * @return The packet listener.
     */
    private PacketAdapter constructProtocol(Plugin plugin) {
        return new PacketAdapter(plugin, ENTITY_PACKETS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                int entityID = event.getPacket().getIntegers().read(0);

                // See if this packet should be cancelled
                if (!isVisible(event.getPlayer(), entityID)) {
                    event.setCancelled(true);
                }
            }
        };
    }

    /**
     * Toggle the visibility status of an entity for a player.
     * <p>
     * If the entity is visible, it will be hidden. If it is hidden, it will become
     * visible.
     * 
     * @param observer - the player observer.
     * @param entity   - the entity to toggle.
     * @return TRUE if the entity was visible before, FALSE otherwise.
     */
    @SuppressWarnings("unused")
    public final boolean toggleEntity(Player observer, Entity entity) {
        int entityID;
        try {
            entityID = observer.getEntityId();
        } catch (Exception e) {
            return true;
        }
        if (isVisible(observer, entityID)) {
            return hideEntity(observer, entity);
        } else {
            return !showEntity(observer, entity);
        }
    }

    /**
     * Allow the observer to see an entity that was previously hidden.
     * 
     * @param observer - the observer.
     * @param entity   - the entity to show.
     * @return TRUE if the entity was hidden before, FALSE otherwise.
     */
    public final boolean showEntity(Player observer, Entity entity) {
        validate(observer, entity);
        int entityID;
        try {
            entityID = entity.getEntityId();
        } catch (Exception e) {
            return false;
        }
        boolean hiddenBefore = !setVisibility(observer, entityID, true);

        // Resend packets
        if (manager != null && hiddenBefore) {
            manager.updateEntity(entity, Collections.singletonList(observer));
        }
        return hiddenBefore;
    }

    /**
     * Prevent the observer from seeing a given entity.
     * 
     * @param observer - the player observer.
     * @param entity   - the entity to hide.
     * @return TRUE if the entity was previously visible, FALSE otherwise.
     */
    public final boolean hideEntity(Player observer, Entity entity) {
        validate(observer, entity);
        int entityID;
        try {
            entityID = entity.getEntityId();
        } catch (Exception e) {
            return true;
        }
        boolean visibleBefore = setVisibility(observer, entityID, false);

        if (visibleBefore) {
            PacketContainer destroyEntity = new PacketContainer(ENTITY_DESTROY);
            try {
                destroyEntity.getIntegerArrays().write(0, new int[] { entityID });
            } catch (Exception e) {
                return false;
            }
            // Make the entity disappear
            manager.sendServerPacket(observer, destroyEntity);
        }
        return visibleBefore;
    }

    /**
     * Determine if the given entity has been hidden from an observer.
     * <p>
     * Note that the entity may very well be occluded or out of range from the
     * perspective
     * of the observer. This method simply checks if an entity has been completely
     * hidden
     * for that observer.
     * 
     * @param observer - the observer.
     * @param entity   - the entity that may be hidden.
     * @return TRUE if the player may see the entity, FALSE if the entity has been
     *         hidden.
     */
    @SuppressWarnings("unused")
    public final boolean canSee(Player observer, Entity entity) {
        validate(observer, entity);
        int entityID;
        try {
            entityID = entity.getEntityId();
        } catch (Exception e) {
            return true;
        }
        return isVisible(observer, entityID);
    }

    private void validate(Player observer, Entity entity) {
        Preconditions.checkNotNull(observer, "observer cannot be NULL.");
        Preconditions.checkNotNull(entity, "entity cannot be NULL.");
    }

    /**
     * Retrieve the current visibility policy.
     * 
     * @return The current visibility policy.
     */
    @SuppressWarnings("unused")
    public Policy getPolicy() {
        return policy;
    }

    @SuppressWarnings("unused")
    public void close() {
        if (manager != null) {
            HandlerList.unregisterAll(bukkitListener);
            manager.removePacketListener(protocolListener);
            manager = null;
        }
    }
}
