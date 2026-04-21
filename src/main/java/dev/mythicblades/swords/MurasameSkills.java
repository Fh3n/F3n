package dev.mythicblades.swords;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.utils.ParticleUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MurasameSkills {

    private static final String SKILL_POISON = "lethal_poison";
    private static final String ULT_BERSERK  = "berserk_mode";

    // ── CURSE SYSTEM ─────────────────────────────
    private static final Map<UUID, Integer> curseStacks = new HashMap<>();
    private static final Set<UUID> marked = new HashSet<>();

    // ─────────────────────────────────────────────
    // PASSIVE — CLEAN + CONTROLLED
    // ─────────────────────────────────────────────
    public static void applyMurasameCurse(LivingEntity target, Player attacker) {

        UUID id = target.getUniqueId();

        marked.add(id);
        curseStacks.put(id, Math.min(curseStacks.getOrDefault(id, 0) + 1, 5));

        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 1));
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1));

        ParticleUtils.spawnPoisonStrike(target.getLocation(), null);

        if (target instanceof Player p) {
            p.sendMessage("§4☠ You have been marked...");
        }
    }

    // ─────────────────────────────────────────────
    // HUD SYSTEM (ACTIONBAR)
    // ─────────────────────────────────────────────
    public static void updateCurseHud(Player player) {

        int stacks = curseStacks.getOrDefault(player.getUniqueId(), 0);
        if (stacks <= 0) return;

        String bar = switch (stacks) {
            case 1 -> "§7☠ Curse I §8✕";
            case 2 -> "§c☠ Curse II §8✕✕";
            case 3 -> "§4☠ Curse III §8✕✕✕";
            case 4 -> "§5☠ Curse IV §8✕✕✕✕";
            default -> "§5☠☠ CURSE MAXIMUM ☠☠";
        };

        player.sendActionBar(Component.text(bar));
    }

    public static void startHudTask(MythicBladesPlugin plugin) {

        new BukkitRunnable() {
            @Override
            public void run() {

                for (Player p : Bukkit.getOnlinePlayers()) {
                    updateCurseHud(p);
                }

            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    // ─────────────────────────────────────────────
    // LETHAL POISON (CLEANER AOE BURST)
    // ─────────────────────────────────────────────
    public static void lethalPoisonActive(Player player, MythicBladesPlugin plugin) {

        var cd = plugin.getCooldownManager();

        if (cd.isOnCooldown(player.getUniqueId(), SKILL_POISON)) {
            player.sendMessage("§4Curse Mark: " +
                    cd.getRemainingSeconds(player.getUniqueId(), SKILL_POISON) + "s");
            return;
        }

        cd.set(player.getUniqueId(), SKILL_POISON,
                plugin.getConfigManager().getCooldownMs("murasame", "lethal_poison"));

        World world = player.getWorld();
        Location loc = player.getLocation().add(0, 1, 0);

        double radius = 6.0;

        player.sendMessage("§4☠ CURSE MARK — Death begins.");

        world.playSound(loc, Sound.ENTITY_WITHER_AMBIENT, 1f, 0.6f);
        world.spawnParticle(Particle.SMOKE, loc, 15, 0.5, 0.5, 0.5, 0.02);

        for (Entity e : world.getNearbyEntities(loc, radius, radius, radius)) {

            if (!(e instanceof LivingEntity le) || e == player) continue;

            UUID id = le.getUniqueId();

            marked.add(id);
            curseStacks.put(id, 3);

            le.damage(10.0, player);
            le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 120, 1));
            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));

            ParticleUtils.spawnPoisonStrike(le.getLocation(), plugin);

            if (le instanceof Player p) {
                p.sendMessage("§4☠ YOU ARE MARKED.");
            }
        }
    }

    // ─────────────────────────────────────────────
    // BERSERK MODE (CURSE OVERFLOW CLEANED)
    // ─────────────────────────────────────────────
    public static void berserkMode(Player player, MythicBladesPlugin plugin) {

        var cd = plugin.getCooldownManager();

        if (cd.isOnCooldown(player.getUniqueId(), ULT_BERSERK)) {
            player.sendMessage("§4Berserk: " +
                    cd.getRemainingSeconds(player.getUniqueId(), ULT_BERSERK) + "s");
            return;
        }

        cd.set(player.getUniqueId(), ULT_BERSERK,
                plugin.getConfigManager().getCooldownMs("murasame", "berserk_mode"));

        int dur = plugin.getConfigManager()
                .getInt("swords.murasame.berserk_mode.duration_seconds", 15) * 20;

        World world = player.getWorld();

        player.sendMessage("§4§l★ CURSE OVERFLOW — AWAKENING");
        world.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 0.5f);

        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, dur, 3));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, dur, 2));

        new BukkitRunnable() {

            int tick = 0;

            @Override
            public void run() {

                if (!player.isOnline() || tick >= dur) {
                    cancel();
                    aftermath(player);
                    return;
                }

                Location loc = player.getLocation().add(0, 1, 0);

                world.spawnParticle(Particle.SMOKE, loc, 8, 0.4, 0.4, 0.4, 0.01);

                if (tick % 15 == 0) {

                    for (Entity e : world.getNearbyEntities(player.getLocation(), 4, 3, 4)) {

                        if (!(e instanceof LivingEntity le) || e == player) continue;

                        UUID id = le.getUniqueId();

                        curseStacks.put(id, curseStacks.getOrDefault(id, 0) + 1);

                        le.damage(5.0 + curseStacks.get(id), player);
                        le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1));

                        ParticleUtils.spawnPoisonStrike(le.getLocation(), plugin);
                    }
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ─────────────────────────────────────────────
    // AFTERMATH (WEAKENED STATE FEELING)
    // ─────────────────────────────────────────────
    private static void aftermath(Player player) {

        if (!player.isOnline()) return;

        World world = player.getWorld();

        player.sendMessage("§4☠ The blade demands repayment.");

        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 1));

        world.spawnParticle(Particle.SMOKE, player.getLocation(), 20, 1, 1, 1, 0.02);
        world.playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.7f, 0.4f);
    }

    // ─────────────────────────────────────────────
    // CLEANUP HELPERS
    // ─────────────────────────────────────────────
    public static void clearCurse(UUID id) {
        curseStacks.remove(id);
        marked.remove(id);
    }
}