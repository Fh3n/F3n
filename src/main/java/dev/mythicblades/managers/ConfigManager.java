package dev.mythicblades.managers;

import dev.mythicblades.MythicBladesPlugin;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final MythicBladesPlugin plugin;
    private FileConfiguration cfg;

    public ConfigManager(MythicBladesPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        cfg = plugin.getConfig();
    }

    // ── Awakening ─────────────────────────────────────────────────────────────
    public int getKillsRequired() {
        return cfg.getInt("awakening.kills_required", 500);
    }

    // ── Collect all buff ──────────────────────────────────────────────────────
    public int getCollectBuffLevel(String effect) {
        return cfg.getInt("collect_all_buff." + effect + "_level", 1);
    }

    // ── Damage & cooldowns ────────────────────────────────────────────────────
    public double getDamage(String sword, String skill) {
        return cfg.getDouble("swords." + sword + "." + skill + ".damage", 20.0);
    }

    public double getDouble(String path, double def) {
        return cfg.getDouble(path, def);
    }

    public int getInt(String path, int def) {
        return cfg.getInt(path, def);
    }

    public long getCooldownMs(String sword, String skill) {
        int seconds = cfg.getInt("swords." + sword + "." + skill + ".cooldown_seconds", 10);
        return seconds * 1000L;
    }

    public long getCooldownMs(String path) {
        int seconds = cfg.getInt(path, 10);
        return seconds * 1000L;
    }

    // ── Particles ─────────────────────────────────────────────────────────────
    public Particle getParticle(String sword, String effect) {
        String name = cfg.getString("particles." + sword + "." + effect, "FLAME");
        if (name == null || name.equalsIgnoreCase("NONE")) return null;
        try {
            return Particle.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle '" + name + "' at particles." + sword + "." + effect + " — using FLAME");
            return Particle.FLAME;
        }
    }

    // ── Messages ──────────────────────────────────────────────────────────────
    public String getMessage(String key) {
        String msg = cfg.getString("messages." + key, "&7[MythicBlades] " + key);
        return colorize(msg);
    }

    public String getMessage(String key, String... replacements) {
        String msg = getMessage(key);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }
        return msg;
    }

    private String colorize(String s) {
        return s.replace("&0", "§0").replace("&1", "§1").replace("&2", "§2")
                .replace("&3", "§3").replace("&4", "§4").replace("&5", "§5")
                .replace("&6", "§6").replace("&7", "§7").replace("&8", "§8")
                .replace("&9", "§9").replace("&a", "§a").replace("&b", "§b")
                .replace("&c", "§c").replace("&d", "§d").replace("&e", "§e")
                .replace("&f", "§f").replace("&l", "§l").replace("&o", "§o")
                .replace("&n", "§n").replace("&m", "§m").replace("&k", "§k")
                .replace("&r", "§r");
    }
}
