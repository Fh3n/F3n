package dev.mythicblades;

import dev.mythicblades.managers.OwnershipManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MythicBladesCommand implements CommandExecutor, TabCompleter {

    private final MythicBladesPlugin plugin;

    public MythicBladesCommand(MythicBladesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mythicblades.admin")) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "give" -> {
                // /mb give <player> <swordId>
                if (args.length < 3) { sender.sendMessage("§cUsage: /mb give <player> <swordId>"); return true; }
                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) { sender.sendMessage("§cPlayer not found."); return true; }

                SwordType type = getSwordType(args[2]);
                if (type == null) { sender.sendMessage("§cUnknown sword: " + args[2]); return true; }

                if (plugin.getOwnershipManager().isClaimed(type)) {
                    UUID owner = plugin.getOwnershipManager().getOwner(type);
                    sender.sendMessage("§c" + type.getDisplayName() + " is already owned by " +
                        plugin.getServer().getOfflinePlayer(owner).getName());
                    return true;
                }

                ItemStack sword = plugin.getSwordManager().createSword(type);
                plugin.getOwnershipManager().claim(type, target, sword);
                target.getInventory().addItem(sword);
                sender.sendMessage("§aGave " + type.getDisplayName() + " to " + target.getName());
                target.sendMessage(Component.text("§6[MythicBlades] §fYou have been granted " + type.getDisplayName(), NamedTextColor.GOLD));
            }

            case "fuse" -> {
                // /mb fuse (must be run by the player themselves)
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can fuse swords.");
                    return true;
                }
                plugin.getFusionManager().attemptFusion(player);
            }

            case "reset" -> {
                // /mb reset <swordId> — unclaim a sword
                if (args.length < 2) { sender.sendMessage("§cUsage: /mb reset <swordId>"); return true; }
                SwordType type = getSwordType(args[1]);
                if (type == null) { sender.sendMessage("§cUnknown sword: " + args[1]); return true; }
                plugin.getOwnershipManager().unclaim(type);
                sender.sendMessage("§a" + type.getDisplayName() + " has been unclaimed and can be forged again.");
            }

            case "resetall" -> {
                for (SwordType type : SwordType.values()) {
                    plugin.getOwnershipManager().unclaim(type);
                }
                sender.sendMessage("§aAll swords have been unclaimed.");
            }

            case "awakened" -> {
                // /mb awakened <player> <swordId> — force-awaken a sword
                if (args.length < 3) { sender.sendMessage("§cUsage: /mb awakened <player> <swordId>"); return true; }
                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) { sender.sendMessage("§cPlayer not found."); return true; }
                SwordType type = getSwordType(args[2]);
                if (type == null) { sender.sendMessage("§cUnknown sword: " + args[2]); return true; }
                plugin.getAwakeningManager().awaken(target, type);
                sender.sendMessage("§aAwakened " + type.getDisplayName() + " for " + target.getName());
            }

            case "reload" -> {
                plugin.getConfigManager().reload();
                sender.sendMessage("§a[MythicBlades] Config reloaded. Damage, cooldowns, particles and messages updated.");
            }

            case "status" -> {
                sender.sendMessage("§6=== MythicBlades Status ===");
                for (SwordType type : SwordType.values()) {
                    if (plugin.getOwnershipManager().isClaimed(type)) {
                        UUID owner = plugin.getOwnershipManager().getOwner(type);
                        String name = plugin.getServer().getOfflinePlayer(owner).getName();
                        sender.sendMessage("§7" + type.getDisplayName() + " §8→ §f" + name);
                    } else {
                        sender.sendMessage("§7" + type.getDisplayName() + " §8→ §aUnclaimed");
                    }
                }
            }

            case "debug" -> {
                if (!(sender instanceof Player player)) return true;
                ItemStack item = player.getInventory().getItemInMainHand();
                SwordType type = plugin.getSwordManager().getSwordType(item);
                if (type == null) { sender.sendMessage("§cNot holding a mythic sword."); return true; }
                boolean awakened = plugin.getSwordManager().isAwakened(item);
                int kills = plugin.getAwakeningManager().getKillProgress(player.getUniqueId(), type);
                sender.sendMessage("§6Sword: §f" + type.getDisplayName());
                sender.sendMessage("§6Tier: §f" + type.getTier().getDisplay());
                sender.sendMessage("§6Awakened: §f" + awakened);
                sender.sendMessage("§6Kill Progress: §f" + kills + "/500");
                sender.sendMessage("§6Owned by you: §f" + plugin.getOwnershipManager().isOwnedBy(type, player));
            }

            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== MythicBlades Commands ===");
        sender.sendMessage("§7/mb give <player> <swordId> §8— Give a sword");
        sender.sendMessage("§7/mb fuse §8— Fuse Enma + Ame no Habakiri (run as player)");
        sender.sendMessage("§7/mb reset <swordId> §8— Unclaim a sword");
        sender.sendMessage("§7/mb resetall §8— Unclaim all swords");
        sender.sendMessage("§7/mb awakened <player> <swordId> §8— Force-awaken a sword");
        sender.sendMessage("§7/mb status §8— View all sword ownership");
        sender.sendMessage("§7/mb debug §8— Debug held sword");
        sender.sendMessage("§7/mb reload §8— Reload config.yml (no restart needed)");
        sender.sendMessage("§8Sword IDs: blade_of_thaw, excalibur, ea, murasame, enma, ame_no_habakiri, nichirin, senbonzakura, kagura_no_tachi");
    }

    private SwordType getSwordType(String id) {
        for (SwordType type : SwordType.values()) {
            if (type.getId().equalsIgnoreCase(id)) return type;
        }
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("give", "fuse", "reset", "resetall", "awakened", "status", "debug", "reload");
        if (args.length == 3 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("awakened") || args[0].equalsIgnoreCase("reset"))) {
            List<String> ids = new ArrayList<>();
            for (SwordType t : SwordType.values()) ids.add(t.getId());
            return ids;
        }
        return List.of();
    }
}
