package dev.mythicblades.managers;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.SwordType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OwnershipManager {

    private final MythicBladesPlugin plugin;
    private final Map<String, UUID> swordOwners = new HashMap<>(); // swordId -> ownerUUID
    private final Map<UUID, Map<String, ItemStack>> playerSwords = new HashMap<>(); // playerUUID -> swordId -> item
    private File dataFile;
    private FileConfiguration data;

    public OwnershipManager(MythicBladesPlugin plugin) {
        this.plugin = plugin;
        loadData();
    }

    public boolean isClaimed(SwordType type) {
        return swordOwners.containsKey(type.getId());
    }

    public UUID getOwner(SwordType type) {
        return swordOwners.get(type.getId());
    }

    public boolean isOwnedBy(SwordType type, Player player) {
        UUID owner = swordOwners.get(type.getId());
        return owner != null && owner.equals(player.getUniqueId());
    }

    public void claim(SwordType type, Player player, ItemStack item) {
        swordOwners.put(type.getId(), player.getUniqueId());
        playerSwords.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                    .put(type.getId(), item.clone());
        plugin.getSwordManager().setOwner(item, player.getUniqueId().toString());
        saveData();
    }

    public void unclaim(SwordType type) {
        UUID owner = swordOwners.remove(type.getId());
        if (owner != null) {
            Map<String, ItemStack> swords = playerSwords.get(owner);
            if (swords != null) swords.remove(type.getId());
        }
        saveData();
    }

    // Called on player death — returns sword to their inventory on respawn
    public ItemStack getStoredSword(SwordType type) {
        UUID owner = swordOwners.get(type.getId());
        if (owner == null) return null;
        Map<String, ItemStack> swords = playerSwords.get(owner);
        if (swords == null) return null;
        return swords.get(type.getId());
    }

    public Map<String, UUID> getAllOwners() {
        return new HashMap<>(swordOwners);
    }

    public int countSwordsOwned(Player player) {
        Map<String, ItemStack> swords = playerSwords.get(player.getUniqueId());
        return swords == null ? 0 : swords.size();
    }

    public boolean ownsAllSeven(Player player) {
        // Kagura no Tachi counts as 2 (replaces Enma + Ame no Habakiri)
        Map<String, ItemStack> swords = playerSwords.get(player.getUniqueId());
        if (swords == null) return false;
        int count = swords.size();
        // If they have Kagura, it replaced Enma + Habakiri so count as 7
        if (swords.containsKey(SwordType.KAGURA_NO_TACHI.getId())) {
            count += 1; // Kagura counts as 2 entries effectively
        }
        return count >= 7;
    }

    // Persistence
    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "ownership.yml");
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        if (data.contains("owners")) {
            for (String swordId : data.getConfigurationSection("owners").getKeys(false)) {
                String uuidStr = data.getString("owners." + swordId);
                if (uuidStr != null) {
                    try { swordOwners.put(swordId, UUID.fromString(uuidStr)); }
                    catch (IllegalArgumentException ignored) {}
                }
            }
        }
    }

    public void saveData() {
        for (Map.Entry<String, UUID> entry : swordOwners.entrySet()) {
            data.set("owners." + entry.getKey(), entry.getValue().toString());
        }
        try { data.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }
}
