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

public class SenbonzakuraSkills {

    // ── Passive — Petal Bleed ─────────────────────────────────────────────────
    public static void applyPetalBleed(LivingEntity target, Player attacker, MythicBladesPlugin plugin) {
        double bleedDmg  = plugin.getConfigManager().skill("senbonzakura", "passive", "bleed_damage", 3.0);
        int ticks        = plugin.getConfigManager().skillInt("senbonzakura", "passive", "bleed_ticks", 8);
        int interval     = plugin.getConfigManager().skillInt("senbonzakura", "passive", "bleed_interval", 10);
        int startDelay   = plugin.getConfigManager().skillInt("senbonzakura", "passive", "bleed_start_delay", 5);

        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (!target.isValid() || target.isDead() || t >= ticks) { cancel(); return; }
                target.damage(bleedDmg, attacker);
                // Orbiting petal orbit effect
                Location l = target.getLocation().add(0, 1, 0);
                double angle = System.currentTimeMillis() / 400.0;
                for (int i = 0; i < 4; i++) {
                    double a = (Math.PI / 2) * i + angle;
                    ParticleUtils.spawn(l.getWorld(), Particle.CHERRY_LEAVES,
                        l.clone().add(Math.cos(a) * 0.7, 0, Math.sin(a) * 0.7), 1, 0.02, 0.08, 0.02, 0);
                }
                t++;
            }
        }.runTaskTimer(plugin, (long)startDelay, (long)interval);
    }

    // ── Scatter (RMB) ─────────────────────────────────────────────────────────
    // Charge -> detonate thousand-blade storm around player
    public static void scatter(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), "scatter")) {
            player.sendMessage("§dScatter: " + cd.getRemainingSeconds(player.getUniqueId(), "scatter") + "s");
            return;
        }
        cd.set(player.getUniqueId(), "scatter", plugin.getConfigManager().skillCooldownMs("senbonzakura", "scatter"));

        double dmg      = plugin.getConfigManager().skill("senbonzakura", "scatter", "damage", 12.0);
        double radius   = plugin.getConfigManager().skill("senbonzakura", "scatter", "radius", 10.0);
        double height   = plugin.getConfigManager().skill("senbonzakura", "scatter", "height", 5.0);
        int slwDur      = plugin.getConfigManager().skillInt("senbonzakura", "scatter", "slowness_duration", 80);
        int slwAmp      = plugin.getConfigManager().skillInt("senbonzakura", "scatter", "slowness_amplifier", 2);
        int chargeTks   = plugin.getConfigManager().skillInt("senbonzakura", "scatter", "charge_ticks", 20);

        World world = player.getWorld();
        Location center = player.getLocation().add(0, 1, 0);

        player.sendMessage("§d✦ Scatter — a thousand blades.");
        world.playSound(center, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 0.6f);

        // Charge: petals gather inward
        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (t >= chargeTks) {
                    cancel();
                    detonateScatter(player, center, world, dmg, radius, height, slwDur, slwAmp, plugin);
                    return;
                }
                world.spawnParticle(Particle.CHERRY_LEAVES, center, 8, 3, 1, 3, 0.15);
                world.spawnParticle(Particle.ENCHANT, center, 4, 2, 1, 2, 0.3);
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static void detonateScatter(Player player, Location center, World world,
                                         double dmg, double radius, double height,
                                         int slwDur, int slwAmp, MythicBladesPlugin plugin) {
        // Burst explosion of petals
        world.spawnParticle(Particle.CHERRY_LEAVES, center, 80, 5, 2, 5, 0.3);
        world.spawnParticle(Particle.ENCHANT,       center, 30, 4, 2, 4, 0.8);
        world.spawnParticle(Particle.END_ROD,       center, 20, 3, 1, 3, 0.15);
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.8f);

        for (Entity e : world.getNearbyEntities(center, radius, height, radius)) {
            if (!(e instanceof LivingEntity le) || e == player) continue;
            le.damage(dmg, player);
            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slwDur, slwAmp));
            le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, slwDur,
                plugin.getConfigManager().skillInt("senbonzakura", "scatter", "weakness_amplifier", 1)));
        }
    }

    // ── Petal Prison (F) ──────────────────────────────────────────────────────
    // Instant cage — heavy slowness, targets pinned briefly
    public static void petalPrison(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), "petal_prison")) {
            player.sendMessage("§dPetal Prison: " + cd.getRemainingSeconds(player.getUniqueId(), "petal_prison") + "s");
            return;
        }
        cd.set(player.getUniqueId(), "petal_prison", plugin.getConfigManager().skillCooldownMs("senbonzakura", "petal_prison"));

        double dmg    = plugin.getConfigManager().skill("senbonzakura", "petal_prison", "damage", 10.0);
        double radius = plugin.getConfigManager().skill("senbonzakura", "petal_prison", "radius", 8.0);
        double height = plugin.getConfigManager().skill("senbonzakura", "petal_prison", "height", 5.0);
        int slwDur    = plugin.getConfigManager().skillInt("senbonzakura", "petal_prison", "slowness_duration", 60);
        int slwAmp    = plugin.getConfigManager().skillInt("senbonzakura", "petal_prison", "slowness_amplifier", 3);

        World world = player.getWorld();
        Location center = player.getLocation().add(0, 1, 0);

        player.sendMessage("§d✦ Petal Prison");
        world.playSound(center, Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1f, 0.7f);

        // Rising petal cage columns
        for (int i = 0; i < 8; i++) {
            double a = Math.toRadians(45 * i);
            Location col = center.clone().add(Math.cos(a) * radius, 0, Math.sin(a) * radius);
            for (int y = 0; y < 10; y++) {
                final Location pt = col.clone().add(0, y * 0.4, 0);
                final long delay = y * 1L;
                new BukkitRunnable() {
                    @Override public void run() {
                        world.spawnParticle(Particle.CHERRY_LEAVES, pt, 2, 0.1, 0.05, 0.1, 0.01);
                    }
                }.runTaskLater(plugin, delay);
            }
        }

        for (Entity e : world.getNearbyEntities(center, radius, height, radius)) {
            if (!(e instanceof LivingEntity le) || e == player) continue;
            le.damage(dmg, player);
            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slwDur, slwAmp));
        }
    }

    // ── Kageyoshi (Shift+RMB) ─────────────────────────────────────────────────
    // Blossom domain — sustained rotating blade ring, continuous damage + pull
    public static void kageyoshi(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), "kageyoshi")) {
            player.sendMessage("§5Kageyoshi: " + cd.getRemainingSeconds(player.getUniqueId(), "kageyoshi") + "s");
            return;
        }
        cd.set(player.getUniqueId(), "kageyoshi", plugin.getConfigManager().skillCooldownMs("senbonzakura", "kageyoshi"));

        double dmg      = plugin.getConfigManager().skill("senbonzakura", "kageyoshi", "damage_per_pulse", 6.0);
        double radius   = plugin.getConfigManager().skill("senbonzakura", "kageyoshi", "radius", 12.0);
        double height   = plugin.getConfigManager().skill("senbonzakura", "kageyoshi", "height", 4.0);
        int duration    = plugin.getConfigManager().skillInt("senbonzakura", "kageyoshi", "duration", 10) * 20;
        int pulseInt    = plugin.getConfigManager().skillInt("senbonzakura", "kageyoshi", "pulse_interval", 8);
        double kbIn     = plugin.getConfigManager().skill("senbonzakura", "kageyoshi", "knockback_in", -0.4);
        double kbY      = plugin.getConfigManager().skill("senbonzakura", "kageyoshi", "knockback_y", 0.2);
        int bladeCount  = plugin.getConfigManager().skillInt("senbonzakura", "kageyoshi", "blade_count", 8);
        int slwDur      = plugin.getConfigManager().skillInt("senbonzakura", "kageyoshi", "slowness_duration", 40);
        int slwAmp      = plugin.getConfigManager().skillInt("senbonzakura", "kageyoshi", "slowness_amplifier", 2);

        World world = player.getWorld();
        Location center = player.getLocation();

        player.sendMessage("§5§l★ SENBONZAKURA KAGEYOSHI — BLOSSOM DOMAIN");
        world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 1f, 1.2f);

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (!p.equals(player))
                p.sendMessage(plugin.getConfigManager().getMessage("senbonzakura_ult_broadcast", "{player}", player.getName()));
        }

        // Initial bloom burst
        world.spawnParticle(Particle.CHERRY_LEAVES, center, 50, 3, 2, 3, 0.3);

        new BukkitRunnable() {
            int tick = 0;
            double angle = 0;
            @Override public void run() {
                if (!player.isOnline() || tick >= duration) { cancel(); return; }
                angle += 0.25;

                // Rotating blade ring visual
                for (int i = 0; i < bladeCount; i++) {
                    double a = angle + (i * Math.PI * 2 / bladeCount);
                    Location blade = center.clone().add(Math.cos(a) * radius, 1, Math.sin(a) * radius);
                    world.spawnParticle(Particle.CHERRY_LEAVES, blade, 1, 0, 0, 0, 0);
                    // Inner petal scatter
                    if (tick % 3 == 0)
                        world.spawnParticle(Particle.ENCHANT, blade, 1, 0.1, 0.2, 0.1, 0.05);
                }

                if (tick % pulseInt == 0) {
                    for (Entity e : world.getNearbyEntities(center, radius, height, radius)) {
                        if (!(e instanceof LivingEntity le) || e == player) continue;
                        le.damage(dmg, player);
                        // Pull inward slightly
                        Vector pull = center.toVector().subtract(e.getLocation().toVector())
                            .normalize().multiply(Math.abs(kbIn)).setY(kbY);
                        le.setVelocity(pull);
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slwDur, slwAmp));
                    }
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
