package dev.mythicblades.listeners;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.SwordType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.UUID;

public class SwordCraftListener implements Listener {

    private final MythicBladesPlugin plugin;

    public SwordCraftListener(MythicBladesPlugin plugin) {
        this.plugin = plugin;
        registerRecipes();
    }

    private void registerRecipes() {
        // ── EPIC ──────────────────────────────────────────────────────────────

        // Crimson Nichirin — Diamond sword core, nether star, blaze, fire
        registerRecipe(SwordType.NICHIRIN, "nichirin",
            "NWN",
            "NBN",
            "NEN",
            'N', Material.NETHER_STAR,
            'W', Material.WITHER_ROSE,
            'B', Material.DIAMOND_SWORD,
            'E', Material.BLAZE_ROD
        );

        // Senbonzakura — Diamond sword, pink petals, amethyst, gold
        registerRecipe(SwordType.SENBONZAKURA, "senbonzakura",
            "PGP",
            "ADA",
            "PGP",
            'P', Material.PINK_PETALS,
            'G', Material.GOLD_INGOT,
            'A', Material.AMETHYST_SHARD,
            'D', Material.DIAMOND_SWORD
        );

        // ── LEGENDARY ─────────────────────────────────────────────────────────

        // Murasame — Netherite sword, dragon breath, nether star, poison
        registerRecipe(SwordType.MURASAME, "murasame",
            "SDS",
            "DND",
            "SDS",
            'S', Material.DRAGON_BREATH,
            'D', Material.NETHERITE_INGOT,
            'N', Material.NETHERITE_SWORD
        );

        // Enma — Netherite sword, wither skulls, blaze, magma
        registerRecipe(SwordType.ENMA, "enma",
            "WBW",
            "MNM",
            "WBW",
            'W', Material.WITHER_SKELETON_SKULL,
            'B', Material.BLAZE_ROD,
            'M', Material.MAGMA_BLOCK,
            'N', Material.NETHERITE_SWORD
        );

        // Ame no Habakiri — Netherite sword, end rod, nether star, echo shard
        registerRecipe(SwordType.AME_NO_HABAKIRI, "ame_no_habakiri",
            "ESE",
            "SNS",
            "ESE",
            'E', Material.END_ROD,
            'S', Material.ECHO_SHARD,
            'N', Material.NETHERITE_SWORD
        );

        // ── MYTHIC ────────────────────────────────────────────────────────────

        // Excalibur — Netherite sword, nether stars, end crystal, beacon
        registerRecipe(SwordType.EXCALIBUR, "excalibur",
            "NCN",
            "NNN",
            "NBN",
            'N', Material.NETHER_STAR,
            'C', Material.END_CRYSTAL,
            'B', Material.BEACON
        );

        // Ea — Netherite sword, ancient debris, nether star, end crystal
        registerRecipe(SwordType.EA, "ea",
            "ANA",
            "NSN",
            "ANA",
            'A', Material.ANCIENT_DEBRIS,
            'N', Material.NETHER_STAR,
            'S', Material.NETHERITE_SWORD
        );

        // ── SINGULARITY — Blade of Thaw is NOT craftable — it is discovered ──
        // No recipe registered intentionally. It must be given by the admin
        // or found via a custom MythicMobs drop.
    }

    private void registerRecipe(SwordType type, String keyName,
                                 String row1, String row2, String row3,
                                 Object... mappings) {
        ItemStack result = plugin.getSwordManager().createSword(type);
        NamespacedKey key = new NamespacedKey(plugin, keyName);

        // Remove existing recipe if reloading
        Bukkit.removeRecipe(key);

        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(row1, row2, row3);
        for (int i = 0; i < mappings.length; i += 2) {
            recipe.setIngredient((char) mappings[i], (Material) mappings[i + 1]);
        }
        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        ItemStack result = event.getRecipe().getResult();
        if (!plugin.getSwordManager().isMythicSword(result)) return;

        SwordType type = plugin.getSwordManager().getSwordType(result);
        if (type == null) return;

        if (!(event.getWhoClicked() instanceof org.bukkit.entity.Player player)) return;

        // Check if sword is already claimed
        if (plugin.getOwnershipManager().isClaimed(type)) {
            UUID ownerUUID = plugin.getOwnershipManager().getOwner(type);
            String ownerName = plugin.getServer().getOfflinePlayer(ownerUUID).getName();
            event.setCancelled(true);
            player.sendMessage(Component.text(
                "§8[§6MythicBlades§8] §c" + type.getDisplayName() +
                " §7already walks this world, bound to §f" + ownerName + "§7.",
                NamedTextColor.GRAY));
            return;
        }

        // Claim the sword for this player
        plugin.getOwnershipManager().claim(type, player, result);

        // Broadcast
        for (org.bukkit.entity.Player p : plugin.getServer().getOnlinePlayers()) {
            p.sendMessage(Component.text(
                "§8[§6MythicBlades§8] §f" + player.getName() +
                " §7has forged §f" + type.getDisplayName() +
                " §8[" + type.getTier().getDisplay() + "§8]",
                NamedTextColor.GRAY));
        }

        player.sendMessage(Component.text(
            "§6You now carry " + type.getDisplayName() + ". §7Survive, and it may yet awaken.",
            NamedTextColor.GOLD));

        // Check collect all
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getOwnershipManager().ownsAllSeven(player)) {
                plugin.getBuffManager().applyCollectAllBuff(player);
            }
        }, 5L);
    }
}
