package dev.mythicblades.managers;

import dev.mythicblades.MythicBladesPlugin;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BuffManager {

    private final MythicBladesPlugin plugin;
    private final Map<UUID, BukkitTask> buffTasks = new HashMap<>();

    public BuffManager(MythicBladesPlugin plugin) { this.plugin = plugin; }

    public void applyCollectAllBuff(Player player) {
        removeBuff(player);
        player.sendMessage("§6§l[MythicBlades] ✦ ALL SEVEN BLADES UNITED ✦");
        player.sendMessage("§eThe world bends to your will.");

        BukkitTask task = new BukkitRunnable() {
            @Override public void run() {
                if (!player.isOnline()) { cancel(); return; }
                if (plugin.getOwnershipManager().ownsAllSeven(player)) {
                    applyBuffEffects(player);
                } else {
                    removeBuff(player); cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 100L);

        buffTasks.put(player.getUniqueId(), task);
        applyBuffEffects(player);

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (!p.equals(player))
                p.sendMessage("§6[MythicBlades] §f" + player.getName() + " §6now wields all seven mythic blades.");
        }
    }

    private void applyBuffEffects(Player player) {
        int dur = 200;
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,    dur, 2, true, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,       dur, 1, true, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE,  dur, 1, true, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,dur, 1, true, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST,  dur, 1, true, false, false));
    }

    public void removeBuff(Player player) {
        BukkitTask task = buffTasks.remove(player.getUniqueId());
        if (task != null) task.cancel();
        player.removePotionEffect(PotionEffectType.STRENGTH);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        player.removePotionEffect(PotionEffectType.REGENERATION);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
    }

    public boolean hasBuff(Player player) { return buffTasks.containsKey(player.getUniqueId()); }
}
