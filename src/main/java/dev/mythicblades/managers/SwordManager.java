package dev.mythicblades.managers;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.SwordType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class SwordManager {

    private final MythicBladesPlugin plugin;
    public static final String SWORD_ID_KEY = "mythicblades_sword_id";
    public static final String OWNER_KEY    = "mythicblades_owner";
    public static final String AWAKENED_KEY = "mythicblades_awakened";

    public SwordManager(MythicBladesPlugin plugin) {
        this.plugin = plugin;
    }

    public ItemStack createSword(SwordType type) {
        ItemStack item = new ItemStack(type.getMaterial());
        ItemMeta meta = item.getItemMeta();

        // Name
        Component name = Component.text(type.getDisplayName())
            .color(type.getColor())
            .decoration(TextDecoration.ITALIC, false)
            .decoration(TextDecoration.BOLD, false);
        meta.displayName(name);

        // Lore
        List<Component> loreComponents = new ArrayList<>();
        loreComponents.add(Component.text(type.getTier().getDisplay())
            .decoration(TextDecoration.ITALIC, false));
        loreComponents.add(Component.empty());
        for (String line : type.getLore()) {
            loreComponents.add(LegacyComponentSerializer.legacySection()
                .deserialize(line)
                .decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(loreComponents);

        // Enchants for visual effect
        meta.addEnchant(Enchantment.UNBREAKING, 10, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES,
                          ItemFlag.HIDE_UNBREAKABLE);
        meta.setUnbreakable(true);

        // NBT tag to identify this sword
        NamespacedKey key = new NamespacedKey(plugin, SWORD_ID_KEY);
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, type.getId());

        item.setItemMeta(meta);
        return item;
    }

    public SwordType getSwordType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, SWORD_ID_KEY);
        String id = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (id == null) return null;
        for (SwordType type : SwordType.values()) {
            if (type.getId().equals(id)) return type;
        }
        return null;
    }

    public boolean isMythicSword(ItemStack item) {
        return getSwordType(item) != null;
    }

    public boolean isAwakened(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, AWAKENED_KEY);
        return Boolean.TRUE.equals(meta.getPersistentDataContainer().get(key, PersistentDataType.BOOLEAN));
    }

    public void setAwakened(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, AWAKENED_KEY);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

        // Add awakened glow to lore
        List<Component> lore = meta.lore();
        if (lore != null) {
            lore.add(Component.empty());
            lore.add(LegacyComponentSerializer.legacySection()
                .deserialize("§e⚡ AWAKENED")
                .decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
    }

    public void setOwner(ItemStack item, String ownerUUID) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, OWNER_KEY);
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, ownerUUID);
        item.setItemMeta(meta);
    }

    public String getOwner(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, OWNER_KEY);
        return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }
}
