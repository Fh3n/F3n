package dev.mythicblades.swords;

import dev.mythicblades.MythicBladesPlugin;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class NichirinSkills {

    private static final String SKILL_HASHIRA = "flame_hashira";
    private static final String SKILL_SWEEP   = "flame_sweep";
    private static final String ULT_HINOKAMI  = "hinokami_kagura";

    // ─────────────────────────────────────────
    // PASSIVE (UNCHANGED BUT LIGHTER)
    // ─────────────────────────────────────────
    public static void applyBurnPassive(LivingEntity target, Player attacker) {
        target.setFireTicks(80);
        target.damage(2.0, attacker);
    }

    // ─────────────────────────────────────────
    // FIRE TRAIL (REDUCED HARD)
    // ─────────────────────────────────────────
    public static void spawnFireTrail(Player player, MythicBladesPlugin plugin) {
        Location loc = player.getLocation();

        if (Math.random() < 0.35) {
            player.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0);
        }
    }

    // ─────────────────────────────────────────
    // HASHIRA (UNCHANGED FEEL, LESS SPAM)
    // ─────────────────────────────────────────
    public static void flameHashira(Player player, MythicBladesPlugin plugin) {

        Vector dir = player.getLocation().getDirection().normalize();
        World world = player.getWorld();

        player.sendMessage("§c⚡ Flame Hashira!");
        player.setVelocity(dir.multiply(1.8));

        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {

                if (!player.isOnline() || t++ > 12) {
                    cancel();
                    return;
                }

                if (t % 3 == 0) {
                    world.spawnParticle(Particle.FLAME, player.getLocation(), 1);
                }

                for (Entity e : world.getNearbyEntities(player.getLocation(), 1.5, 1.5, 1.5)) {
                    if (e instanceof LivingEntity le && e != player) {
                        le.damage(4.0, player);
                        le.setFireTicks(80);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ─────────────────────────────────────────
    // FLAME SWEEP (NOW TRUE ARC SWING)
    // ─────────────────────────────────────────
    public static void flameSweep(Player player, MythicBladesPlugin plugin) {

        World world = player.getWorld();
        Location origin = player.getLocation().add(0, 1, 0);
        Vector dir = player.getLocation().getDirection().normalize();

        player.sendMessage("§c✦ Flame Sweep!");

        world.playSound(origin, Sound.ENTITY_BLAZE_SHOOT, 0.6f, 0.8f);

        // ── progressive arc (NOT full instant circle)
        new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {

                if (step > 6) {
                    cancel();
                    return;
                }

                double angle = Math.toRadians(-70 + (step * 25));

                Vector arc = rotate(dir, angle).multiply(5);
                Location p = origin.clone().add(arc);

                world.spawnParticle(Particle.FLAME, p, 1);

                for (Entity e : world.getNearbyEntities(p, 2, 2, 2)) {
                    if (e instanceof LivingEntity le && e != player) {
                        le.damage(6.0, player);
                        le.setFireTicks(100);
                    }
                }

                step++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    // ─────────────────────────────────────────
    // 🔥 HINOKAMI KAGURA (REBUILT CORE SYSTEM)
    // ─────────────────────────────────────────
    public static void hinokamiKagura(Player player, MythicBladesPlugin plugin) {

        World world = player.getWorld();
        Location origin = player.getLocation().add(0, 1, 0);

        player.sendMessage("§6§l★ HINOKAMI KAGURA");
        world.playSound(origin, Sound.ENTITY_BLAZE_SHOOT, 0.6f, 0.6f);

        player.setInvulnerable(true);

        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {

                if (t++ > 15) {
                    cancel();
                    startXSequence(player, plugin, origin);
                    return;
                }

                // LIGHT BREATHING AURA ONLY (NO SPAM)
                if (t % 4 == 0) {
                    world.spawnParticle(Particle.FLAME, origin, 1, 0.2, 0.2, 0.2, 0);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ─────────────────────────────────────────
    // 🔥 X-SEQUENCE SYSTEM (KEY REWRITE)
    // ─────────────────────────────────────────
    private static void startXSequence(Player player, MythicBladesPlugin plugin, Location origin) {

        World world = player.getWorld();
        Vector dir = player.getLocation().getDirection().normalize();

        double dmg = 10.0;

        new BukkitRunnable() {

            int phase = 0;

            @Override
            public void run() {

                if (!player.isOnline() || phase > 3) {
                    player.setInvulnerable(false);
                    cancel();
                    return;
                }

                switch (phase) {

                    // ── 1st slash: \ diagonal (wide arc)
                    case 0 -> drawDiagonal(world, origin, dir, -1, dmg, player);

                    // ── 2nd slash: / diagonal (counter arc)
                    case 1 -> drawDiagonal(world, origin, dir, 1, dmg, player);

                    // ── 3rd: reinforce X (lighter overlap)
                    case 2 -> {
                        drawDiagonal(world, origin, dir, -1, dmg * 1.2, player);
                        drawDiagonal(world, origin, dir, 1, dmg * 1.2, player);
                    }

                    // ── finisher: burst point
                    case 3 -> {
                        world.spawnParticle(Particle.FLAME, origin, 10, 0.5, 0.5, 0.5, 0);
                        world.playSound(origin, Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.2f);

                        for (Entity e : world.getNearbyEntities(origin, 6, 4, 6)) {
                            if (e instanceof LivingEntity le && e != player) {
                                le.damage(18.0, player);
                                le.setFireTicks(120);
                            }
                        }
                    }
                }

                phase++;
            }
        }.runTaskTimer(plugin, 0L, 6L);
    }

    // ─────────────────────────────────────────
    // DIAGONAL ARC (LOW PARTICLE, HIGH READABILITY)
    // ─────────────────────────────────────────
    private static void drawDiagonal(World world, Location origin,
                                     Vector dir, int side, double dmg, Player player) {

        for (int i = 0; i < 6; i++) {

            double angle = Math.toRadians((i * 18) * side);
            Vector offset = rotate(dir, angle).multiply(i * 1.2);

            Location p = origin.clone().add(offset);

            if (i % 2 == 0) {
                world.spawnParticle(Particle.FLAME, p, 1);
            }

            for (Entity e : world.getNearbyEntities(p, 2, 2, 2)) {
                if (e instanceof LivingEntity le && e != player) {
                    le.damage(dmg * 0.6, player);
                }
            }
        }
    }

    // ─────────────────────────────────────────
    private static Vector rotate(Vector v, double rad) {
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        return new Vector(
                v.getX() * cos - v.getZ() * sin,
                v.getY(),
                v.getX() * sin + v.getZ() * cos
        );
    }
}