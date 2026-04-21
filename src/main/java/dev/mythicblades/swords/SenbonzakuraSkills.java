package dev.mythicblades.swords;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.utils.ParticleUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class SenbonzakuraSkills {

    private static final String SKILL_SCATTER = "scatter";
    private static final String SKILL_PRISON  = "petal_prison";
    private static final String ULT_KAGEYOSHI = "kageyoshi";

    // ── Passive ─────────────────────────────────────────
    public static void applyPetalBleed(LivingEntity target, Player attacker, MythicBladesPlugin plugin) {

        double bleedDmg = plugin.getConfigManager()
                .getDouble("swords.senbonzakura.petal_bleed_damage_per_tick", 3.0);

        int ticks = plugin.getConfigManager()
                .getInt("swords.senbonzakura.petal_bleed_duration_ticks", 8);

        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (!target.isValid() || target.isDead() || t >= ticks) {
                    cancel();
                    return;
                }

                target.damage(bleedDmg, attacker);
                ParticleUtils.spawnPetalBleedOrbit(target, plugin);
                t++;
            }
        }.runTaskTimer(plugin, 5L, 10L);
    }

    // ── Scatter ─────────────────────────────────────────
    public static void scatter(Player player, MythicBladesPlugin plugin) {

        var cd = plugin.getCooldownManager();

        if (cd.isOnCooldown(player.getUniqueId(), SKILL_SCATTER)) {
            player.sendMessage("§dScatter: " +
                    cd.getRemainingSeconds(player.getUniqueId(), SKILL_SCATTER) + "s");
            return;
        }

        cd.set(player.getUniqueId(), SKILL_SCATTER,
                plugin.getConfigManager().getCooldownMs("senbonzakura", "scatter"));

        Location center = player.getLocation().add(0, 1, 0);
        World world = player.getWorld();

        player.sendMessage("§d✦ Scatter — a thousand blades.");
        world.playSound(center, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 0.6f);

        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= 20) {
                    cancel();
                    detonate(player, center, world, plugin);
                    return;
                }

                world.spawnParticle(Particle.CHERRY_LEAVES, center, 10, 3, 1, 3, 0.15);
                world.spawnParticle(Particle.ENCHANT, center, 6, 2, 1, 2, 0.3);

                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static void detonate(Player player, Location center, World world,
                                 MythicBladesPlugin plugin) {

        double dmg = plugin.getConfigManager()
                .getDouble("swords.senbonzakura.scatter.damage", 12.0);

        double radius = plugin.getConfigManager()
                .getDouble("swords.senbonzakura.scatter.radius", 10.0);

        ParticleUtils.spawnScatterStorm(center, plugin);

        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.8f);

        for (Entity e : world.getNearbyEntities(center, radius, 5, radius)) {
            if (!(e instanceof LivingEntity le) || e == player) continue;

            le.damage(dmg, player);
            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2));
            le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1));
        }
    }

    // ── Petal Prison ────────────────────────────────────
    public static void petalPrison(Player player, MythicBladesPlugin plugin) {

        var cd = plugin.getCooldownManager();

        if (cd.isOnCooldown(player.getUniqueId(), SKILL_PRISON)) {
            player.sendMessage("§dPetal Prison: " +
                    cd.getRemainingSeconds(player.getUniqueId(), SKILL_PRISON) + "s");
            return;
        }

        cd.set(player.getUniqueId(), SKILL_PRISON,
                plugin.getConfigManager().getCooldownMs("senbonzakura", "petal_prison"));

        Location center = player.getLocation().add(0, 1, 0);
        World world = player.getWorld();

        double dmg = plugin.getConfigManager()
                .getDouble("swords.senbonzakura.petal_prison.damage", 10.0);

        double radius = plugin.getConfigManager()
                .getDouble("swords.senbonzakura.petal_prison.radius", 8.0);

        player.sendMessage("§d✦ Petal Prison");

        world.playSound(center, Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1f, 0.7f);

        for (Entity e : world.getNearbyEntities(center, radius, 5, radius)) {
            if (!(e instanceof LivingEntity le) || e == player) continue;

            le.damage(dmg, player);
            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3));
        }
    }

    // ── ULT: KAGEYOSHI (FIXED + REAL DOMAIN SYSTEM) ─────────────────────────
    public static void kageyoshi(Player player, MythicBladesPlugin plugin) {

        var cd = plugin.getCooldownManager();

        if (cd.isOnCooldown(player.getUniqueId(), ULT_KAGEYOSHI)) {
            player.sendMessage("§5Kageyoshi: " +
                    cd.getRemainingSeconds(player.getUniqueId(), ULT_KAGEYOSHI) + "s");
            return;
        }

        cd.set(player.getUniqueId(), ULT_KAGEYOSHI,
                plugin.getConfigManager().getCooldownMs("senbonzakura", "kageyoshi"));

        Location center = player.getLocation();
        World world = player.getWorld();

        double radius = plugin.getConfigManager()
                .getDouble("swords.senbonzakura.kageyoshi.radius", 12.0);

        double dmg = plugin.getConfigManager()
                .getDouble("swords.senbonzakura.kageyoshi.damage", 6.0);

        int duration = plugin.getConfigManager()
                .getInt("swords.senbonzakura.kageyoshi.duration_seconds", 10) * 20;

        player.sendMessage("§5§l★ SENBONZAKURA KAGEYOSHI — BLOSSOM DOMAIN");
        world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 1f, 1.2f);

        // bloom start
        world.spawnParticle(Particle.CHERRY_LEAVES, center, 50, 3, 2, 3, 0.3);

        new BukkitRunnable() {

            int tick = 0;
            double angle = 0;

            @Override
            public void run() {

                if (!player.isOnline() || tick >= duration) {
                    cancel();
                    return;
                }

                angle += 0.25;

                // rotating blade ring (visual domain)
                for (int i = 0; i < 8; i++) {
                    double a = angle + (i * Math.PI / 4);
                    double x = Math.cos(a) * radius;
                    double z = Math.sin(a) * radius;

                    Location blade = center.clone().add(x, 1, z);

                    world.spawnParticle(Particle.CHERRY_LEAVES, blade, 1, 0, 0, 0, 0);
                }

                // DAMAGE SYSTEM (THIS WAS MISSING BEFORE)
                if (tick % 8 == 0) {
                    for (Entity e : world.getNearbyEntities(center, radius, 4, radius)) {
                        if (!(e instanceof LivingEntity le) || e == player) continue;

                        le.damage(dmg, player);
                        le.setVelocity(le.getLocation()
                                .toVector()
                                .subtract(center.toVector())
                                .normalize()
                                .multiply(-0.4)
                                .setY(0.2));

                        le.addPotionEffect(new PotionEffect(
                                PotionEffectType.SLOWNESS, 40, 2));
                    }
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}