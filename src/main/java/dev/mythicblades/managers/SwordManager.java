package dev.mythicblades.managers;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.SwordType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

    public SwordManager(MythicBladesPlugin plugin) { this.plugin = plugin; }

    public ItemStack createSword(SwordType type) {
        ItemStack item = new ItemStack(type.getMaterial());
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(type.getDisplayName())
            .color(type.getColor())
            .decoration(TextDecoration.ITALIC, false)
            .decoration(TextDecoration.BOLD, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(type.getTier().getDisplay()).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        for (String line : type.getLore()) {
            lore.add(LegacyComponentSerializer.legacySection().deserialize(line)
                .decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);

        meta.addEnchant(Enchantment.UNBREAKING, 10, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        meta.setUnbreakable(true);

        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, SWORD_ID_KEY),
            PersistentDataType.STRING, type.getId());

        item.setItemMeta(meta);
        return item;
    }

    public SwordType getSwordType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String id = item.getItemMeta().getPersistentDataContainer()
            .get(new NamespacedKey(plugin, SWORD_ID_KEY), PersistentDataType.STRING);
        if (id == null) return null;
        for (SwordType t : SwordType.values()) if (t.getId().equals(id)) return t;
        return null;
    }

    public boolean isMythicSword(ItemStack item) { return getSwordType(item) != null; }

    public boolean isAwakened(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return Boolean.TRUE.equals(item.getItemMeta().getPersistentDataContainer()
            .get(new NamespacedKey(plugin, AWAKENED_KEY), PersistentDataType.BOOLEAN));
    }

    public void setAwakened(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, AWAKENED_KEY),
            PersistentDataType.BOOLEAN, true);
        List<Component> lore = meta.lore();
        if (lore != null) {
            lore.add(Component.empty());
            lore.add(LegacyComponentSerializer.legacySection().deserialize("§e⚡ AWAKENED")
                .decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
    }

    public void setOwner(ItemStack item, String ownerUUID) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, OWNER_KEY),
            PersistentDataType.STRING, ownerUUID);
        item.setItemMeta(meta);
    }

    public String getOwner(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer()
            .get(new NamespacedKey(plugin, OWNER_KEY), PersistentDataType.STRING);
    }
}
