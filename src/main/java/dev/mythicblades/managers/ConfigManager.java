package dev.mythicblades.managers;

import dev.mythicblades.MythicBladesPlugin;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigManager {

    private final MythicBladesPlugin plugin;
    private FileConfiguration cfg;
    private FileConfiguration skills;

    public ConfigManager(MythicBladesPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        cfg = plugin.getConfig();
        skills = loadSkills();
    }

    private FileConfiguration loadSkills() {
        File file = new File(plugin.getDataFolder(), "skills.yml");
        if (!file.exists()) plugin.saveResource("skills.yml", false);
        YamlConfiguration loaded = YamlConfiguration.loadConfiguration(file);
        InputStream is = plugin.getResource("skills.yml");
        if (is != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(is, StandardCharsets.UTF_8));
            loaded.setDefaults(defaults);
        }
        return loaded;
    }

    public int getKillsRequired() { return cfg.getInt("awakening.kills_required", 500); }
    public int getCollectBuffLevel(String effect) { return cfg.getInt("collect_all_buff." + effect + "_level", 1); }

    public double skill(String sword, String skill, String key, double def) {
        return skills.getDouble(sword + "." + skill + "." + key, def);
    }
    public int skillInt(String sword, String skill, String key, int def) {
        return skills.getInt(sword + "." + skill + "." + key, def);
    }
    public boolean skillBoolean(String sword, String skill, String key, boolean def) {
        String p = sword + "." + skill + "." + key;
        return skills.contains(p) ? skills.getBoolean(p, def) : def;
    }
    public long skillCooldownMs(String sword, String skill) {
        return skills.getInt(sword + "." + skill + ".cooldown", 10) * 1000L;
    }
    public double swordVal(String sword, String key, double def) {
        return skills.getDouble(sword + "." + key, def);
    }
    public int swordInt(String sword, String key, int def) {
        return skills.getInt(sword + "." + key, def);
    }

    public Particle skillParticle(String sword, String key) {
        return parseParticle(skills.getString(sword + ".particles." + key, "FLAME"));
    }

    public double getDamage(String sword, String skill) {
        double v = skills.getDouble(sword + "." + skill + ".damage", -1);
        return v >= 0 ? v : cfg.getDouble("swords." + sword + "." + skill + ".damage", 20.0);
    }
    public double getDouble(String path, double def) {
        return skills.contains(path) ? skills.getDouble(path, def) : cfg.getDouble(path, def);
    }
    public int getInt(String path, int def) {
        return skills.contains(path) ? skills.getInt(path, def) : cfg.getInt(path, def);
    }
    public long getCooldownMs(String sword, String skill) {
        String sp = sword + "." + skill + ".cooldown";
        if (skills.contains(sp)) return skills.getInt(sp, 10) * 1000L;
        return cfg.getInt("swords." + sword + "." + skill + ".cooldown_seconds", 10) * 1000L;
    }

    public String getMessage(String key) {
        return colorize(cfg.getString("messages." + key, "&7[MythicBlades] " + key));
    }
    public String getMessage(String key, String... replacements) {
        String msg = getMessage(key);
        for (int i = 0; i < replacements.length - 1; i += 2) msg = msg.replace(replacements[i], replacements[i + 1]);
        return msg;
    }

    private Particle parseParticle(String name) {
        if (name == null || name.equalsIgnoreCase("NONE")) return null;
        try { return Particle.valueOf(name.toUpperCase()); }
        catch (IllegalArgumentException e) { return Particle.FLAME; }
    }

    private String colorize(String s) {
        return s.replace("&0","§0").replace("&1","§1").replace("&2","§2").replace("&3","§3")
                .replace("&4","§4").replace("&5","§5").replace("&6","§6").replace("&7","§7")
                .replace("&8","§8").replace("&9","§9").replace("&a","§a").replace("&b","§b")
                .replace("&c","§c").replace("&d","§d").replace("&e","§e").replace("&f","§f")
                .replace("&l","§l").replace("&o","§o").replace("&n","§n").replace("&m","§m")
                .replace("&k","§k").replace("&r","§r");
    }
}
