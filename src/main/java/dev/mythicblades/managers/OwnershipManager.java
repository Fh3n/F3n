package dev.mythicblades.managers;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.SwordType;
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
    private final Map<String, UUID> swordOwners = new HashMap<>();
    private final Map<UUID, Map<String, ItemStack>> playerSwords = new HashMap<>();
    private File dataFile;
    private FileConfiguration data;

    public OwnershipManager(MythicBladesPlugin plugin) {
        this.plugin = plugin;
        loadData();
    }

    public boolean isClaimed(SwordType type) { return swordOwners.containsKey(type.getId()); }
    public UUID getOwner(SwordType type) { return swordOwners.get(type.getId()); }

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

    public ItemStack getStoredSword(SwordType type) {
        UUID owner = swordOwners.get(type.getId());
        if (owner == null) return null;
        Map<String, ItemStack> swords = playerSwords.get(owner);
        return swords == null ? null : swords.get(type.getId());
    }

    public Map<String, UUID> getAllOwners() { return new HashMap<>(swordOwners); }

    public int countSwordsOwned(Player player) {
        Map<String, ItemStack> s = playerSwords.get(player.getUniqueId());
        return s == null ? 0 : s.size();
    }

    public boolean ownsAllSeven(Player player) {
        Map<String, ItemStack> swords = playerSwords.get(player.getUniqueId());
        if (swords == null) return false;
        int count = swords.size();
        if (swords.containsKey(SwordType.KAGURA_NO_TACHI.getId())) count += 1;
        return count >= 7;
    }

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
        for (Map.Entry<String, UUID> e : swordOwners.entrySet())
            data.set("owners." + e.getKey(), e.getValue().toString());
        try { data.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }
}
