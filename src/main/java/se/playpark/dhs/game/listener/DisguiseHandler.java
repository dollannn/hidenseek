package se.playpark.dhs.game.listener;

import static com.comphenix.protocol.PacketType.Play.Client.*;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import se.playpark.dhs.Main;
import se.playpark.dhs.game.util.Disguise;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DisguiseHandler implements Listener {

    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    public DisguiseHandler() {
        protocolManager.addPacketListener(createProtocol());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final Disguise disguise = Main.getInstance().getDisguiser().getDisguise(player);
        if (disguise == null)
            return;
        if (event.getFrom().distance(event.getTo()) > .1) {
            disguise.setSolidify(false);
        }
        disguise.startSolidifying();
    }

    private PacketAdapter createProtocol() {
        return new PacketAdapter(Main.getInstance(), USE_ENTITY) {

            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                // only left click attacks
                EnumWrappers.EntityUseAction action = packet.getEntityUseActions().getValues().stream().findFirst()
                        .orElse(null);
                if (action == null)
                    return;
                // noinspection ComparatorResultComparison
                if (action.compareTo(EnumWrappers.EntityUseAction.INTERACT) == 2) {
                    return;
                }

                Player player = event.getPlayer();
                int id = packet.getIntegers().read(0);
                Disguise disguise = Main.getInstance().getDisguiser().getByEntityID(id);
                if (disguise == null)
                    disguise = Main.getInstance().getDisguiser().getByHitBoxID(id);
                if (disguise == null)
                    return;

                if (disguise.getPlayer().getGameMode() == GameMode.CREATIVE)
                    return;
                event.setCancelled(true);
                handleAttack(disguise, player);
            }
        };
    }

    private final List<Player> debounce = new ArrayList<>();

    private void handleAttack(Disguise disguise, Player seeker) {

        if (disguise.getPlayer() == seeker)
            return;

        double amount;
        if (Main.getInstance().supports(9)) {
            amount = Objects.requireNonNull(seeker.getAttribute(Attribute.ATTACK_DAMAGE)).getValue();
        } else {
            return; // 1.8 is not supported in Blockhunt yet!!!
        }

        disguise.setSolidify(false);
        if (debounce.contains(disguise.getPlayer()))
            return;

        debounce.add(disguise.getPlayer());

        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
            // Create entity damage event with the correct parameters using DamageSource API
            DamageSource damageSource = DamageSource.builder(DamageType.PLAYER_ATTACK)
                    .withCausingEntity(seeker)
                    .build();
            EntityDamageEvent event = new EntityDamageEvent(disguise.getPlayer(), EntityDamageEvent.DamageCause.ENTITY_ATTACK, damageSource, amount);
            Main.getInstance().getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                disguise.getPlayer().damage(amount, seeker);
                disguise.getPlayer().setVelocity(seeker.getLocation().getDirection().setY(.2).multiply(1));
            }

        }, 0);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> debounce.remove(disguise.getPlayer()),
                10);
    }

}
