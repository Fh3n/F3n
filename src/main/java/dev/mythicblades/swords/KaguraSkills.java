package dev.mythicblades.swords;

import dev.mythicblades.MythicBladesPlugin;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class KaguraSkills {

    private static final String SKILL_RESONANCE = "dual_resonance";
    private static final String SKILL_ARC       = "kagura_arc";
    private static final String ULT_TENCHI      = "tenchi_kaimei";

    // ─────────────────────────────────────────────
    // PASSIVE (LOW VISUALS)
    // ─────────────────────────────────────────────
    public static void applyKaguraPassive(LivingEntity target, Player attacker, MythicBladesPlugin plugin) {

        if (Math.random() < 0.35) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40, 1));
            target.setFireTicks(60);
        } else {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 1));
        }

        target.damage(2.0, attacker);
    }

    // ─────────────────────────────────────────────
    // DUAL RESONANCE (X SWEEP + DRILL CORE)
    // ─────────────────────────────────────────────
    public static void dualResonance(Player player, MythicBladesPlugin plugin) {

        var cd = plugin.getCooldownManager();

        if (cd.isOnCooldown(player.getUniqueId(), SKILL_RESONANCE)) {
            player.sendMessage("§5Dual Resonance: " +
                    cd.getRemainingSeconds(player.getUniqueId(), SKILL_RESONANCE) + "s");
            return;
        }

        cd.set(player.getUniqueId(), SKILL_RESONANCE, 12000);

        World world = player.getWorld();
        Location start = player.getLocation().add(0, 1, 0);
        Vector dir = player.getLocation().getDirection().normalize();

        player.sendMessage("§5✦ Dual Resonance");

        runSweep(world, start, dir, player, true);
        runSweep(world, start, dir, player, false);

        // DRILL VISUAL (LOW PARTICLES)
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t > 20) { cancel(); return; }

                Vector spin = rotateY(dir, t * 30);
                Location p = start.clone().add(spin.multiply(2));

                world.spawnParticle(Particle.END_ROD, p, 1);

                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static void runSweep(World world, Location start, Vector dir,
                                 Player player, boolean left) {

        Set<UUID> hit = new HashSet<>();

        new BukkitRunnable() {

            int step = 0;
            Location cur = start.clone();

            @Override
            public void run() {

                if (step > 40) { cancel(); return; }

                cur.add(dir.clone().multiply(0.8));

                double angle = (left ? -step : step) * 2;
                Vector sweep = rotateY(dir, angle);

                Location p = cur.clone().add(sweep.multiply(0.5));

                if (step % 2 == 0) {
                    world.spawnParticle(Particle.FLAME, p, 1);
                    world.spawnParticle(Particle.CHERRY_LEAVES, p, 1);
                }

                for (Entity e : world.getNearbyEntities(cur, 2, 2, 2)) {
                    if (!(e instanceof LivingEntity le) || e == player) continue;
                    if (!hit.add(e.getUniqueId())) continue;

                    le.damage(6.0, player);
                }

                step++;
            }
        }.runTaskTimer(MythicBladesPlugin.getPlugin(MythicBladesPlugin.class), 0L, 1L);
    }

    // ─────────────────────────────────────────────
    // ARC STRIKE (EXTENDED SWEEP)
    // ─────────────────────────────────────────────
    public static void kaguraArcStrike(Player player, MythicBladesPlugin plugin) {

        var cd = plugin.getCooldownManager();

        if (cd.isOnCooldown(player.getUniqueId(), SKILL_ARC)) {
            player.sendMessage("§5Arc Strike: " +
                    cd.getRemainingSeconds(player.getUniqueId(), SKILL_ARC) + "s");
            return;
        }

        cd.set(player.getUniqueId(), SKILL_ARC, 8000);

        World world = player.getWorld();
        Location start = player.getLocation().add(0, 1, 0);
        Vector dir = player.getLocation().getDirection().normalize();

        player.sendMessage("§5✦ Arc Strike");

        Set<UUID> hit = new HashSet<>();

        new BukkitRunnable() {

            int t = 0;

            @Override
            public void run() {

                if (t > 30) { cancel(); return; }

                Vector arc = rotateY(dir, t * 3);
                Location p = start.clone().add(dir.clone().multiply(t * 0.7)).add(arc);

                if (t % 2 == 0) {
                    world.spawnParticle(Particle.FLAME, p, 1);
                }

                for (Entity e : world.getNearbyEntities(p, 2, 2, 2)) {
                    if (!(e instanceof LivingEntity le) || e == player) continue;
                    if (!hit.add(e.getUniqueId())) continue;

                    le.damage(8.0, player);
                }

                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ─────────────────────────────────────────────
    // ULTIMATE — TENCHI KAIMEI (SILENT DESCENT)
    // ─────────────────────────────────────────────
    public static void tenchiKaimei(Player player, MythicBladesPlugin plugin) {

        var cd = plugin.getCooldownManager();

        if (cd.isOnCooldown(player.getUniqueId(), ULT_TENCHI)) {
            player.sendMessage("§5Tenchi Kaimei: " +
                    cd.getRemainingSeconds(player.getUniqueId(), ULT_TENCHI) + "s");
            return;
        }

        cd.set(player.getUniqueId(), ULT_TENCHI, 20000);

        World world = player.getWorld();
        Location center = player.getLocation();

        player.sendMessage("§5§l☯ TENCHI KAIMEI");
        player.setInvulnerable(true);

        double radius = 18;
        double height = 50;

        // SKY DESCENT (LOW PARTICLES, NO EXPLOSION SPAM)
        new BukkitRunnable() {

            int t = 0;

            @Override
            public void run() {

                if (t > 35) {

                    // CLEAN FINAL BURST
                    world.spawnParticle(Particle.FLAME, center, 10);
                    world.spawnParticle(Particle.CHERRY_LEAVES, center, 10);

                    for (Entity e : world.getNearbyEntities(center, radius, height, radius)) {
                        if (e instanceof LivingEntity le && e != player) {
                            le.damage(35.0, player);
                        }
                    }

                    player.setInvulnerable(false);
                    cancel();
                    return;
                }

                double y = height - (t * 1.4);
                Location p = center.clone().add(0, y, 0);

                if (t % 2 == 0) {
                    world.spawnParticle(Particle.FLAME, p, 2);
                } else {
                    world.spawnParticle(Particle.CHERRY_LEAVES, p, 2);
                }

                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ─────────────────────────────────────────────
    // UTIL
    // ─────────────────────────────────────────────
    private static Vector rotateY(Vector v, double deg) {
        double r = Math.toRadians(deg);
        double cos = Math.cos(r), sin = Math.sin(r);

        return new Vector(
                v.getX() * cos + v.getZ() * sin,
                v.getY(),
                -v.getX() * sin + v.getZ() * cos
        );
    }
}