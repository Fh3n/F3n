package dev.mythicblades.swords;

import dev.mythicblades.MythicBladesPlugin;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EaSkills {

    private static final String SKILL_BARRAGE = "sword_barrage";
    private static final String SKILL_SLASH   = "void_slash";
    private static final String ULT_ENUMA     = "enuma_elish";

    // ───────────────── PASSIVE ─────────────────
    public static void applyVoidPassive(LivingEntity target, Player attacker) {
        target.damage(3.0, attacker);
        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40, 0, false, false, false));

        if (Math.random() < 0.25) {
            target.getWorld().spawnParticle(Particle.SMOKE, target.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
        }
    }

    // ───────────────── SWORD BARRAGE ─────────────────
    public static void swordBarrage(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), SKILL_BARRAGE)) return;

        cd.set(player.getUniqueId(), SKILL_BARRAGE,
                plugin.getConfigManager().getCooldownMs("ea", "sword_barrage"));

        World world = player.getWorld();
        Location origin = player.getLocation().add(0, 1, 0);
        Vector dir = player.getLocation().getDirection().normalize();

        world.playSound(origin, Sound.ENTITY_WITHER_SHOOT, 0.6f, 0.8f);

        for (int i = 0; i < 6; i++) {
            int delay = i * 4;
            new BukkitRunnable() {
                @Override
                public void run() {
                    launchSword(player, origin.clone(), dir, world, plugin);
                }
            }.runTaskLater(plugin, delay);
        }
    }

    private static void launchSword(Player player, Location start, Vector dir, World world, MythicBladesPlugin plugin) {
        double dmg = plugin.getConfigManager().getDamage("ea", "sword_barrage");

        new BukkitRunnable() {
            int step = 0;
            Location cur = start.clone();

            @Override
            public void run() {
                if (step > 40) { cancel(); return; }

                cur.add(dir.clone().multiply(0.9));

                if (step % 3 == 0) {
                    world.spawnParticle(Particle.CRIT, cur, 1);
                }

                for (Entity e : world.getNearbyEntities(cur, 1.2, 1.2, 1.2)) {
                    if (e == player || !(e instanceof LivingEntity le)) continue;

                    le.damage(dmg, player);
                    world.spawnParticle(Particle.PORTAL, cur, 2);
                    cancel();
                    return;
                }
                step++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ───────────────── VOID SLASH ─────────────────
    public static void voidSlash(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), SKILL_SLASH)) return;

        cd.set(player.getUniqueId(), SKILL_SLASH,
                plugin.getConfigManager().getCooldownMs("ea", "void_slash"));

        World world = player.getWorld();
        Location start = player.getLocation().add(0, 1, 0);
        Vector dir = player.getLocation().getDirection().normalize();

        double range = 35;
        double width = 8;
        double dmg = plugin.getConfigManager().getDamage("ea", "void_slash");

        world.playSound(start, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 0.5f);

        new BukkitRunnable() {
            int t = 0;
            @Override
            public void run() {
                if (t > 15) {
                    cancel();
                    startRupture(player, start, dir, world, plugin, dmg, range, width);
                    return;
                }
                Location fx = start.clone().add(0, t * 0.4, 0);
                if (t % 2 == 0) world.spawnParticle(Particle.PORTAL, fx, 2);
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static void startRupture(Player player, Location start, Vector dir, World world, MythicBladesPlugin plugin,
                                     double dmg, double range, double width) {
        Vector right = dir.clone().crossProduct(new Vector(0, 1, 0)).normalize();
        Set<UUID> hit = new HashSet<>();

        new BukkitRunnable() {
            int step = 0;
            Location cur = start.clone();

            @Override
            public void run() {
                if (step > range) { cancel(); return; }

                cur.add(dir.clone().multiply(0.7));

                for (double w = -width; w <= width; w += 2.5) {
                    for (double h = -1; h <= 3; h += 2) {
                        Location p = cur.clone().add(right.clone().multiply(w)).add(0, h, 0);
                        if (step % 3 == 0) world.spawnParticle(Particle.SMOKE, p, 0);
                    }
                }

                for (Entity e : world.getNearbyEntities(cur, width, 3, width)) {
                    if (e == player || !(e instanceof LivingEntity le)) continue;
                    if (hit.add(e.getUniqueId())) {
                        le.damage(dmg, player);
                        le.setVelocity(new Vector(0, 0.4, 0));
                    }
                }
                step++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ───────────────── ENUMA ELISH ─────────────────
    public static void enumaElish(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), ULT_ENUMA)) return;

        cd.set(player.getUniqueId(), ULT_ENUMA,
                plugin.getConfigManager().getCooldownMs("ea", "enuma_elish"));

        World world = player.getWorld();
        Location center = player.getLocation().add(0, 1, 0);
        double dmg = plugin.getConfigManager().getDouble("swords.ea.enuma_elish.beam_damage", 30.0);

        player.setInvulnerable(true);
        world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 0.8f, 0.4f);

        new BukkitRunnable() {
            int t = 0;
            @Override
            public void run() {
                if (t > 30) {
                    cancel();
                    descendBeam(player, center, world, plugin, dmg);
                    return;
                }
                Location sky = center.clone().add(0, 25 + t, 0);
                if (t % 3 == 0) world.spawnParticle(Particle.PORTAL, sky, 3);
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static void descendBeam(Player player, Location center, World world, MythicBladesPlugin plugin, double dmg) {
        Vector down = new Vector(0, -1, 0);

        new BukkitRunnable() {
            int step = 0;
            Location cur = center.clone().add(0, 35, 0);

            @Override
            public void run() {
                if (step > 60) {
                    player.setInvulnerable(false);
                    cancel();
                    return;
                }

                cur.add(down.clone().multiply(1.2));

                if (step % 2 == 0) world.spawnParticle(Particle.DRAGON_BREATH, cur, 2);

                for (Entity e : world.getNearbyEntities(cur, 3, 3, 3)) {
                    if (e == player || !(e instanceof LivingEntity le)) continue;
                    le.damage(dmg, player);
                }
                step++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}