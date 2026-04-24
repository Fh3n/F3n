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

public class EaSkills {

    public static void applyVoidPassive(LivingEntity target, Player attacker, MythicBladesPlugin plugin) {
        double bonus = plugin.getConfigManager().skill("ea", "passive", "bonus_damage", 3.0);
        int witDur   = plugin.getConfigManager().skillInt("ea", "passive", "wither_duration", 40);
        target.damage(bonus, attacker);
        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, witDur, 0, false, false, false));
        if (Math.random() < plugin.getConfigManager().skill("ea", "passive", "particle_chance", 0.25)) {
            ParticleUtils.spawn(target.getWorld(), Particle.PORTAL,
                target.getLocation().add(0, 1, 0), 3, 0.2, 0.3, 0.2, 0.2);
        }
    }

    public static void swordBarrage(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), "sword_barrage")) {
            player.sendMessage("§cSword Barrage: " + cd.getRemainingSeconds(player.getUniqueId(), "sword_barrage") + "s");
            return;
        }
        cd.set(player.getUniqueId(), "sword_barrage", plugin.getConfigManager().skillCooldownMs("ea", "sword_barrage"));

        double dmg   = plugin.getConfigManager().skill("ea", "sword_barrage", "damage", 22.0);
        int count    = plugin.getConfigManager().skillInt("ea", "sword_barrage", "count", 6);
        int delay    = plugin.getConfigManager().skillInt("ea", "sword_barrage", "delay_per_sword", 4);
        double speed = plugin.getConfigManager().skill("ea", "sword_barrage", "step_size", 0.9);
        double hbox  = plugin.getConfigManager().skill("ea", "sword_barrage", "hitbox", 1.2);

        World world = player.getWorld();
        Location origin = player.getLocation().add(0, 1, 0);
        Vector dir = player.getLocation().getDirection().normalize();

        world.playSound(origin, Sound.ENTITY_WITHER_SHOOT, 0.6f, 0.8f);

        for (int i = 0; i < count; i++) {
            double spreadY = (Math.random() - 0.5) * 0.3;
            double spreadH = (Math.random() - 0.5) * 0.3;
            Vector bladeDir = dir.clone().add(new Vector(spreadH, spreadY, spreadH)).normalize();

            new BukkitRunnable() {
                @Override public void run() {
                    if (!player.isOnline()) return;
                    launchBlade(player, origin.clone(), bladeDir, world, dmg, speed, hbox, plugin);
                }
            }.runTaskLater(plugin, (long)(i * delay));
        }
    }

    private static void launchBlade(Player player, Location start, Vector dir,
                                     World world, double dmg, double speed, double hbox,
                                     MythicBladesPlugin plugin) {
        new BukkitRunnable() {
            int step = 0;
            Location cur = start.clone();
            @Override public void run() {
                if (step > 40) { cancel(); return; }
                cur.add(dir.clone().multiply(speed));
                if (step % 2 == 0) {
                    world.spawnParticle(Particle.CRIT, cur, 1);
                    world.spawnParticle(Particle.PORTAL, cur, 2, 0.05, 0.05, 0.05, 0.1);
                }
                for (Entity e : world.getNearbyEntities(cur, hbox, hbox, hbox)) {
                    if (!(e instanceof LivingEntity le) || e == player) continue;
                    le.damage(dmg, player);
                    world.spawnParticle(Particle.PORTAL, cur, 8, 0.2, 0.2, 0.2, 0.3);
                    cancel();
                    return;
                }
                step++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public static void voidSlash(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), "void_slash")) {
            player.sendMessage("§cVoid Slash: " + cd.getRemainingSeconds(player.getUniqueId(), "void_slash") + "s");
            return;
        }
        cd.set(player.getUniqueId(), "void_slash", plugin.getConfigManager().skillCooldownMs("ea", "void_slash"));

        double dmg    = plugin.getConfigManager().skill("ea", "void_slash", "damage", 30.0);
        double range  = plugin.getConfigManager().skill("ea", "void_slash", "range", 35.0);
        double width  = plugin.getConfigManager().skill("ea", "void_slash", "width", 8.0);
        double liftY  = plugin.getConfigManager().skill("ea", "void_slash", "lift_velocity", 0.4);
        int chargeTk  = plugin.getConfigManager().skillInt("ea", "void_slash", "charge_ticks", 15);

        World world = player.getWorld();
        Location start = player.getLocation().add(0, 1, 0);
        Vector dir = player.getLocation().getDirection().normalize();

        world.playSound(start, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 0.5f);
        player.sendMessage("§c✦ Void Slash!");

        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (t > chargeTk) {
                    cancel();
                    fireRupture(player, start, dir, world, dmg, range, width, liftY, plugin);
                    return;
                }
                Location fx = start.clone().add(0, t * 0.3, 0);
                if (t % 2 == 0) world.spawnParticle(Particle.PORTAL, fx, 2, 0.1, 0.1, 0.1, 0.1);
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static void fireRupture(Player player, Location start, Vector dir, World world,
                                     double dmg, double range, double width, double liftY,
                                     MythicBladesPlugin plugin) {
        Vector right = dir.clone().crossProduct(new Vector(0, 1, 0)).normalize();
        Set<UUID> hit = new HashSet<>();

        world.playSound(start, Sound.ENTITY_WITHER_SHOOT, 1f, 0.3f);

        new BukkitRunnable() {
            int step = 0;
            Location cur = start.clone();
            @Override public void run() {
                if (step > range) { cancel(); return; }
                cur.add(dir.clone().multiply(0.7));

                for (double w = -width / 2; w <= width / 2; w += 2.0) {
                    for (double h = -1; h <= 3; h += 2) {
                        Location p = cur.clone().add(right.clone().multiply(w)).add(0, h, 0);
                        if (step % 2 == 0) world.spawnParticle(Particle.PORTAL, p, 1, 0, 0, 0, 0.05);
                    }
                }
                if (step % 3 == 0)
                    world.spawnParticle(Particle.DRAGON_BREATH, cur, 2, 0.2, 0.5, 0.2, 0.02);

                for (Entity e : world.getNearbyEntities(cur, width / 2, 3, width / 2)) {
                    if (!(e instanceof LivingEntity le) || e == player || !hit.add(e.getUniqueId())) continue;
                    le.damage(dmg, player);
                    le.setVelocity(new Vector(0, liftY, 0));
                }
                step++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public static void enumaElish(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), "enuma_elish")) {
            player.sendMessage("§4Enuma Elish: " + cd.getRemainingSeconds(player.getUniqueId(), "enuma_elish") + "s");
            return;
        }
        cd.set(player.getUniqueId(), "enuma_elish", plugin.getConfigManager().skillCooldownMs("ea", "enuma_elish"));

        double beamDmg    = plugin.getConfigManager().skill("ea", "enuma_elish", "beam_damage", 30.0);
        double beamHbox   = plugin.getConfigManager().skill("ea", "enuma_elish", "beam_hitbox", 3.0);
        double skyHeight  = plugin.getConfigManager().skill("ea", "enuma_elish", "sky_height", 35.0);
        double descSpeed  = plugin.getConfigManager().skill("ea", "enuma_elish", "descend_speed", 1.2);
        int    descSteps  = plugin.getConfigManager().skillInt("ea", "enuma_elish", "descend_steps", 60);

        World world = player.getWorld();
        Location center = player.getLocation().add(0, 1, 0);

        player.sendMessage("§4§l★ ENUMA ELISH — VOID DESCENDS");
        player.setInvulnerable(true);
        world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 0.8f, 0.4f);

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (!p.equals(player))
                p.sendMessage(plugin.getConfigManager().getMessage("ea_ult_broadcast", "{player}", player.getName()));
        }

        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (t > 30) {
                    cancel();
                    descendBeam(player, center, world, beamDmg, beamHbox, skyHeight, descSpeed, descSteps, plugin);
                    return;
                }
                Location sky = center.clone().add(0, skyHeight - t * 0.5, 0);
                if (t % 2 == 0) {
                    world.spawnParticle(Particle.PORTAL, sky, 4, 1, 0.2, 1, 0.2);
                    world.spawnParticle(Particle.DRAGON_BREATH, sky, 2, 0.5, 0.1, 0.5, 0.05);
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static void descendBeam(Player player, Location center, World world,
                                     double dmg, double hbox, double skyHeight,
                                     double speed, int steps, MythicBladesPlugin plugin) {
        world.playSound(center, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1f, 0.5f);

        new BukkitRunnable() {
            int step = 0;
            Location cur = center.clone().add(0, skyHeight, 0);
            @Override public void run() {
                if (step > steps) {
                    world.spawnParticle(Particle.EXPLOSION, center, 3, 1, 0.5, 1, 0);
                    world.spawnParticle(Particle.DRAGON_BREATH, center, 30, 3, 1, 3, 0.1);
                    world.strikeLightningEffect(center);
                    player.setInvulnerable(false);
                    cancel();
                    return;
                }
                cur.subtract(0, speed, 0);
                if (step % 2 == 0) {
                    world.spawnParticle(Particle.DRAGON_BREATH, cur, 3, 0.3, 0.1, 0.3, 0.03);
                    world.spawnParticle(Particle.PORTAL, cur, 5, 0.5, 0.2, 0.5, 0.3);
                }
                for (Entity e : world.getNearbyEntities(cur, hbox, hbox, hbox)) {
                    if (!(e instanceof LivingEntity le) || e == player) continue;
                    le.damage(dmg, player);
                }
                step++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
