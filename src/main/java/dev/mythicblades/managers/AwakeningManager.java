package dev.mythicblades.managers;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.SwordType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AwakeningManager {

    private final MythicBladesPlugin plugin;
    private final Map<String, Integer> killProgress = new HashMap<>();
    private final Map<String, Boolean> awakenedMap  = new HashMap<>();
    private File dataFile;
    private FileConfiguration data;

    public AwakeningManager(MythicBladesPlugin plugin) {
        this.plugin = plugin;
        loadData();
    }

    private String key(UUID player, SwordType type) { return player + ":" + type.getId(); }

    public boolean isAwakened(UUID player, SwordType type) {
        return Boolean.TRUE.equals(awakenedMap.get(key(player, type)));
    }

    public int getKillProgress(UUID player, SwordType type) {
        return killProgress.getOrDefault(key(player, type), 0);
    }

    public void addKill(Player player, SwordType type) {
        if (isAwakened(player.getUniqueId(), type)) return;
        String k = key(player.getUniqueId(), type);
        int kills = killProgress.getOrDefault(k, 0) + 1;
        killProgress.put(k, kills);
        int required = plugin.getConfigManager().getKillsRequired();
        if (kills % 50 == 0) {
            player.sendMessage(Component.text("[MythicBlades] " + type.getDisplayName() +
                " awakening: " + kills + "/" + required + " kills", NamedTextColor.GOLD));
        }
        if (kills >= required) awaken(player, type);
    }

    public void awaken(Player player, SwordType type) {
        awakenedMap.put(key(player.getUniqueId(), type), true);
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            var item = player.getInventory().getItem(i);
            if (item != null && plugin.getSwordManager().getSwordType(item) == type) {
                plugin.getSwordManager().setAwakened(item);
                player.getInventory().setItem(i, item);
                break;
            }
        }
        player.sendMessage(Component.text("⚡ " + type.getDisplayName() + " has been AWAKENED!", NamedTextColor.YELLOW));
        if (type == SwordType.ENMA || type == SwordType.AME_NO_HABAKIRI) {
            if (isAwakened(player.getUniqueId(), SwordType.ENMA) &&
                isAwakened(player.getUniqueId(), SwordType.AME_NO_HABAKIRI)) {
                player.sendMessage(Component.text(
                    "§5✦ Both Enma and Ame no Habakiri are Awakened. Run /mb fuse.", NamedTextColor.LIGHT_PURPLE));
            }
        }
        saveData();
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "awakening.yml");
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        if (data.contains("kills"))
            for (String k : data.getConfigurationSection("kills").getKeys(false))
                killProgress.put(k, data.getInt("kills." + k));
        if (data.contains("awakened"))
            for (String k : data.getConfigurationSection("awakened").getKeys(false))
                awakenedMap.put(k, data.getBoolean("awakened." + k));
    }

    public void saveData() {
        killProgress.forEach((k, v) -> data.set("kills." + k, v));
        awakenedMap.forEach((k, v) -> data.set("awakened." + k, v));
        try { data.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }
}
