package dev.mythicblades.swords;

import dev.mythicblades.MythicBladesPlugin;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class NichirinSkills {

    // ── Passive ───────────────────────────────────────────────────────────────
    // Fire + regen-null (Wither removes regen effectively)
    public static void applyBurnPassive(LivingEntity target, Player attacker, MythicBladesPlugin plugin) {
        int fireTicks = plugin.getConfigManager().skillInt("nichirin", "passive", "fire_ticks", 80);
        double bonus  = plugin.getConfigManager().skill("nichirin", "passive", "bonus_damage", 2.0);
        target.setFireTicks(fireTicks);
        target.damage(bonus, attacker);
        // Remove any regen the target has
        target.removePotionEffect(PotionEffectType.REGENERATION);
    }

    public static void spawnFireTrail(Player player, MythicBladesPlugin plugin) {
        double chance = plugin.getConfigManager().skill("nichirin", "fire_trail", "spawn_chance", 0.35);
        if (Math.random() < chance)
            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 1, 0, 0, 0, 0);
    }

    // ── Flame Hashira (RMB) ───────────────────────────────────────────────────
    // Teleport-dash forward, igniting everything in the path
    public static void flameHashira(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), "flame_hashira")) {
            player.sendMessage("§cFlame Hashira: " + cd.getRemainingSeconds(player.getUniqueId(), "flame_hashira") + "s");
            return;
        }
        cd.set(player.getUniqueId(), "flame_hashira", plugin.getConfigManager().skillCooldownMs("nichirin", "flame_hashira"));

        double dmgPerTick = plugin.getConfigManager().skill("nichirin", "flame_hashira", "damage_per_tick", 4.0);
        double dashMult   = plugin.getConfigManager().skill("nichirin", "flame_hashira", "dash_multiplier", 1.8);
        int duration      = plugin.getConfigManager().skillInt("nichirin", "flame_hashira", "duration_ticks", 12);
        double hbox       = plugin.getConfigManager().skill("nichirin", "flame_hashira", "hitbox", 1.5);
        int fireTicks     = plugin.getConfigManager().skillInt("nichirin", "flame_hashira", "fire_ticks", 80);

        Vector dir = player.getLocation().getDirection().normalize();
        World world = player.getWorld();

        player.sendMessage("§c⚡ Flame Hashira!");
        world.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.8f, 1.2f);
        player.setVelocity(dir.multiply(dashMult));

        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (!player.isOnline() || t++ > duration) { cancel(); return; }
                if (t % 3 == 0) world.spawnParticle(Particle.FLAME, player.getLocation(), 2, 0.2, 0.2, 0.2, 0);
                for (Entity e : world.getNearbyEntities(player.getLocation(), hbox, hbox, hbox)) {
                    if (!(e instanceof LivingEntity le) || e == player) continue;
                    le.damage(dmgPerTick, player);
                    le.setFireTicks(fireTicks);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ── Flame Sweep (F) ───────────────────────────────────────────────────────
    // Progressive arc swing — not instant circle, feels like an actual sweep
    public static void flameSweep(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), "flame_sweep")) {
            player.sendMessage("§cFlame Sweep: " + cd.getRemainingSeconds(player.getUniqueId(), "flame_sweep") + "s");
            return;
        }
        cd.set(player.getUniqueId(), "flame_sweep", plugin.getConfigManager().skillCooldownMs("nichirin", "flame_sweep"));

        double dmg      = plugin.getConfigManager().skill("nichirin", "flame_sweep", "damage", 6.0);
        int arcSteps    = plugin.getConfigManager().skillInt("nichirin", "flame_sweep", "arc_steps", 7);
        int arcStart    = plugin.getConfigManager().skillInt("nichirin", "flame_sweep", "arc_start_deg", -70);
        int arcStepDeg  = plugin.getConfigManager().skillInt("nichirin", "flame_sweep", "arc_step_deg", 25);
        double arcRad   = plugin.getConfigManager().skill("nichirin", "flame_sweep", "arc_radius", 5.0);
        double hbox     = plugin.getConfigManager().skill("nichirin", "flame_sweep", "hitbox", 2.0);
        int fireTicks   = plugin.getConfigManager().skillInt("nichirin", "flame_sweep", "fire_ticks", 100);

        World world = player.getWorld();
        Location origin = player.getLocation().add(0, 1, 0);
        Vector dir = player.getLocation().getDirection().normalize();

        player.sendMessage("§c✦ Flame Sweep!");
        world.playSound(origin, Sound.ENTITY_BLAZE_SHOOT, 0.6f, 0.8f);

        new BukkitRunnable() {
            int step = 0;
            @Override public void run() {
                if (step > arcSteps) { cancel(); return; }
                double angle = Math.toRadians(arcStart + step * arcStepDeg);
                Vector arc = rotateY(dir, angle).multiply(arcRad);
                Location p = origin.clone().add(arc);
                world.spawnParticle(Particle.FLAME, p, 2, 0.1, 0.1, 0.1, 0.02);
                world.spawnParticle(Particle.LAVA,  p, 1, 0, 0, 0, 0);
                for (Entity e : world.getNearbyEntities(p, hbox, hbox, hbox)) {
                    if (!(e instanceof LivingEntity le) || e == player) continue;
                    le.damage(dmg, player);
                    le.setFireTicks(fireTicks);
                }
                step++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    // ── Hinokami Kagura (Shift+RMB) ───────────────────────────────────────────
    // Invuln windup -> X-slash sequence -> solar finisher burst
    public static void hinokamiKagura(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), "hinokami_kagura")) {
            player.sendMessage("§6Hinokami Kagura: " + cd.getRemainingSeconds(player.getUniqueId(), "hinokami_kagura") + "s");
            return;
        }
        cd.set(player.getUniqueId(), "hinokami_kagura", plugin.getConfigManager().skillCooldownMs("nichirin", "hinokami_kagura"));

        double diagDmg      = plugin.getConfigManager().skill("nichirin", "hinokami_kagura", "diagonal_damage", 10.0);
        double diagScale    = plugin.getConfigManager().skill("nichirin", "hinokami_kagura", "diagonal_damage_scale", 0.6);
        double reinforce    = plugin.getConfigManager().skill("nichirin", "hinokami_kagura", "reinforce_multiplier", 1.2);
        double finisherDmg  = plugin.getConfigManager().skill("nichirin", "hinokami_kagura", "finisher_damage", 18.0);
        double finisherR    = plugin.getConfigManager().skill("nichirin", "hinokami_kagura", "finisher_radius", 6.0);
        int finisherFire    = plugin.getConfigManager().skillInt("nichirin", "hinokami_kagura", "finisher_fire_ticks", 120);
        int phaseInterval   = plugin.getConfigManager().skillInt("nichirin", "hinokami_kagura", "phase_interval", 6);
        int windup          = plugin.getConfigManager().skillInt("nichirin", "hinokami_kagura", "invuln_windup", 15);

        World world = player.getWorld();
        Location origin = player.getLocation().add(0, 1, 0);

        player.sendMessage("§6§l★ HINOKAMI KAGURA");
        world.playSound(origin, Sound.ENTITY_BLAZE_SHOOT, 0.6f, 0.6f);
        player.setInvulnerable(true);

        // Windup breathing aura
        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (t++ > windup) {
                    cancel();
                    runXSequence(player, world, origin, diagDmg, diagScale, reinforce,
                        finisherDmg, finisherR, finisherFire, phaseInterval, plugin);
                    return;
                }
                if (t % 4 == 0)
                    world.spawnParticle(Particle.FLAME, origin, 1, 0.2, 0.2, 0.2, 0);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static void runXSequence(Player player, World world, Location origin,
                                      double diagDmg, double diagScale, double reinforce,
                                      double finisherDmg, double finisherR, int finisherFire,
                                      int phaseInterval, MythicBladesPlugin plugin) {
        Vector dir = player.getLocation().getDirection().normalize();

        new BukkitRunnable() {
            int phase = 0;
            @Override public void run() {
                if (!player.isOnline() || phase > 3) {
                    player.setInvulnerable(false);
                    cancel();
                    return;
                }
                switch (phase) {
                    case 0 -> drawDiagonal(world, origin, dir, -1, diagDmg * diagScale, player);
                    case 1 -> drawDiagonal(world, origin, dir,  1, diagDmg * diagScale, player);
                    case 2 -> {
                        drawDiagonal(world, origin, dir, -1, diagDmg * diagScale * reinforce, player);
                        drawDiagonal(world, origin, dir,  1, diagDmg * diagScale * reinforce, player);
                    }
                    case 3 -> {
                        world.spawnParticle(Particle.FLAME,    origin, 12, 0.6, 0.6, 0.6, 0);
                        world.spawnParticle(Particle.EXPLOSION, origin, 1, 0.2, 0.2, 0.2, 0);
                        world.playSound(origin, Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.2f);
                        for (Entity e : world.getNearbyEntities(origin, finisherR, finisherR, finisherR)) {
                            if (!(e instanceof LivingEntity le) || e == player) continue;
                            le.damage(finisherDmg, player);
                            le.setFireTicks(finisherFire);
                        }
                    }
                }
                phase++;
            }
        }.runTaskTimer(plugin, 0L, (long)phaseInterval);
    }

    private static void drawDiagonal(World world, Location origin, Vector dir, int side, double dmg, Player player) {
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(i * 18 * side);
            Vector offset = rotateY(dir, angle).multiply(i * 1.2);
            Location p = origin.clone().add(offset);
            if (i % 2 == 0) world.spawnParticle(Particle.FLAME, p, 1);
            for (Entity e : world.getNearbyEntities(p, 2, 2, 2)) {
                if (!(e instanceof LivingEntity le) || e == player) continue;
                le.damage(dmg, player);
            }
        }
    }

    private static Vector rotateY(Vector v, double rad) {
        double cos = Math.cos(rad), sin = Math.sin(rad);
        return new Vector(v.getX() * cos - v.getZ() * sin, v.getY(), v.getX() * sin + v.getZ() * cos);
    }
}
