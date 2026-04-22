package dev.mythicblades.swords;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.utils.ParticleUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EnmaSkills {

    // ── Passive ───────────────────────────────────────────────────────────────
    // Curse echo — delayed damage pulses after each hit
    public static void applyEnmaPassive(LivingEntity target, Player attacker, MythicBladesPlugin plugin) {
        int witDur  = plugin.getConfigManager().skillInt("enma", "passive", "wither_duration", 60);
        int witAmp  = plugin.getConfigManager().skillInt("enma", "passive", "wither_amplifier", 1);
        int echoCt  = plugin.getConfigManager().skillInt("enma", "passive", "curse_echo_ticks", 3);
        double echoD = plugin.getConfigManager().skill("enma", "passive", "curse_echo_damage", 1.0);
        int echoDelay = plugin.getConfigManager().skillInt("enma", "passive", "curse_echo_delay", 10);
        double chance = plugin.getConfigManager().skill("enma", "passive", "particle_chance", 0.4);

        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, witDur, witAmp, false, true, true));

        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (t++ >= echoCt || target.isDead()) { cancel(); return; }
                target.damage(echoD, attacker);
            }
        }.runTaskTimer(MythicBladesPlugin.getInstance(), (long)echoDelay, (long)echoDelay);

        if (Math.random() < chance) {
            ParticleUtils.spawn(target.getWorld(), Particle.FLAME,
                target.getLocation().add(0, 1, 0), 6, 0.2, 0.3, 0.2, 0.04);
        }
    }

    // ── Drain Info (F) ────────────────────────────────────────────────────────
    public static void drainInfo(Player player, MythicBladesPlugin plugin) {
        player.sendMessage("§6Enma drains life force with every strike.");
        player.sendMessage("§7Hold without Ame no Habakiri: §cWither II passive.");
        player.sendMessage("§7Hold both: §aRegen II + Strength III.");
    }

    // ── Hakai Slash (Shift+RMB) ───────────────────────────────────────────────
    // Long-range chaos cleave — wide, fire + curse on all hit, fracture afterimage
    public static void hakaiSlash(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), "hakai_slash")) {
            player.sendMessage("§cHakai Slash: " + cd.getRemainingSeconds(player.getUniqueId(), "hakai_slash") + "s");
            return;
        }
        cd.set(player.getUniqueId(), "hakai_slash", plugin.getConfigManager().skillCooldownMs("enma", "hakai_slash"));

        double dmg     = plugin.getConfigManager().skill("enma", "hakai_slash", "damage", 45.0);
        double range   = plugin.getConfigManager().skill("enma", "hakai_slash", "range", 45.0);
        double width   = plugin.getConfigManager().skill("enma", "hakai_slash", "width", 5.0);
        double stepSz  = plugin.getConfigManager().skill("enma", "hakai_slash", "step_size", 0.8);
        int fireTicks  = plugin.getConfigManager().skillInt("enma", "hakai_slash", "fire_ticks", 120);
        int witDur     = plugin.getConfigManager().skillInt("enma", "hakai_slash", "wither_duration", 60);
        int wkDur      = plugin.getConfigManager().skillInt("enma", "hakai_slash", "weakness_duration", 80);

        Vector dir  = player.getLocation().getDirection().normalize();
        Vector perp = dir.clone().crossProduct(new Vector(0, 1, 0)).normalize();
        if (perp.lengthSquared() < 0.001) perp = new Vector(1, 0, 0);

        Location start = player.getLocation().add(0, 1, 0);
        World world = player.getWorld();
        int steps = (int)(range / stepSz);

        player.sendMessage("§c§l★ HAKAI SLASH");
        world.playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1f, 0.4f);

        Set<UUID> hit = new HashSet<>();
        final Vector perpF = perp;

        new BukkitRunnable() {
            int step = 0;
            Location cur = start.clone();

            @Override public void run() {
                if (step >= steps) {
                    // Fracture afterimage at endpoint
                    new BukkitRunnable() {
                        double r = 1;
                        @Override public void run() {
                            if (r > 6) { cancel(); return; }
                            world.spawnParticle(Particle.SMOKE, cur, (int)(8 / r), r, r, r, 0.01);
                            r += 1.2;
                        }
                    }.runTaskTimer(MythicBladesPlugin.getInstance(), 0L, 5L);
                    cancel();
                    return;
                }

                cur.add(dir.clone().multiply(stepSz));

                if (step % 2 == 0) {
                    // Main slash particles
                    world.spawnParticle(Particle.FLAME, cur, 4, 0.1, 0.3, 0.1, 0.04);
                    // Edge echo slashes
                    Location left  = cur.clone().add(perpF.clone().multiply(-width * 0.55));
                    Location right = cur.clone().add(perpF.clone().multiply(width * 0.55));
                    world.spawnParticle(Particle.SMOKE, left,  1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.SMOKE, right, 1, 0, 0, 0, 0);
                }

                if (step % 2 == 0) {
                    for (Entity e : world.getNearbyEntities(cur, width / 2, 2.5, width / 2)) {
                        if (!(e instanceof LivingEntity le) || e == player || !hit.add(e.getUniqueId())) continue;
                        le.damage(dmg, player);
                        le.setFireTicks(fireTicks);
                        le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, witDur, 1));
                        le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, wkDur, 2));
                    }
                }

                if (step % 6 == 0) world.playSound(cur, Sound.ENTITY_BLAZE_HURT, 0.1f, 0.6f);
                step++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
