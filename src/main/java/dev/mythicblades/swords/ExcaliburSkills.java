package dev.mythicblades.swords;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.utils.ParticleUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ExcaliburSkills {

    private static final Map<UUID, Boolean> twinLock = new HashMap<>();

    public static void applyLightPassive(LivingEntity target, Player attacker, MythicBladesPlugin plugin) {
        double bonus = plugin.getConfigManager().skill("excalibur", "passive", "bonus_damage", 4.0);
        int wkDur    = plugin.getConfigManager().skillInt("excalibur", "passive", "weakness_duration", 30);
        target.damage(bonus, attacker);
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, wkDur, 0, false, false, false));
        ParticleUtils.spawn(target.getWorld(), Particle.END_ROD,
            target.getLocation().add(0, 1, 0), 3, 0.2, 0.3, 0.2, 0.03);
    }

    public static void twinStrike(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), "twin_strike")) {
            player.sendMessage("§eTwin Strike: " + cd.getRemainingSeconds(player.getUniqueId(), "twin_strike") + "s");
            return;
        }
        if (twinLock.getOrDefault(player.getUniqueId(), false)) return;
        twinLock.put(player.getUniqueId(), true);

        cd.set(player.getUniqueId(), "twin_strike", plugin.getConfigManager().skillCooldownMs("excalibur", "twin_strike"));

        double dmg    = plugin.getConfigManager().skill("excalibur", "twin_strike", "damage", 14.0);
        int steps     = plugin.getConfigManager().skillInt("excalibur", "twin_strike", "arc_steps", 14);
        double stepSz = plugin.getConfigManager().skill("excalibur", "twin_strike", "step_size", 0.7);
        double hitbox = plugin.getConfigManager().skill("excalibur", "twin_strike", "hitbox", 1.3);

        World world = player.getWorld();
        Location loc = player.getLocation().add(0, 1, 0);
        Vector dir = player.getLocation().getDirection().normalize();

        world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1.4f);
        arcStrike(player, loc.clone(), dir.clone(), world, dmg, steps, stepSz, hitbox, plugin);

        new BukkitRunnable() {
            @Override public void run() {
                if (!player.isOnline()) { twinLock.put(player.getUniqueId(), false); return; }
                world.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1.8f);
                arcStrike(player, player.getLocation().add(0, 1, 0),
                    player.getLocation().getDirection().normalize(),
                    world, dmg * 1.2, steps, stepSz, hitbox, plugin);
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60,
                    plugin.getConfigManager().skillInt("excalibur", "twin_strike", "strength_amplifier", 1)));
                twinLock.put(player.getUniqueId(), false);
            }
        }.runTaskLater(plugin, 10L);
    }

    private static void arcStrike(Player player, Location start, Vector dir, World world,
                                   double dmg, int steps, double stepSz, double hitbox,
                                   MythicBladesPlugin plugin) {
        Set<UUID> hit = new HashSet<>();
        new BukkitRunnable() {
            int step = 0;
            Location cur = start.clone();
            @Override public void run() {
                if (step > steps) { cancel(); return; }
                cur.add(dir.clone().multiply(stepSz));
                if (step % 2 == 0) {
                    world.spawnParticle(Particle.SWEEP_ATTACK, cur, 1);
                    world.spawnParticle(Particle.END_ROD, cur, 1, 0.1, 0.1, 0.1, 0.02);
                }
                for (Entity e : world.getNearbyEntities(cur, hitbox, hitbox, hitbox)) {
                    if (!(e instanceof LivingEntity le) || e == player || le.isDead() || !hit.add(e.getUniqueId())) continue;
                    le.damage(dmg, player);
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                }
                step++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public static void holyPulse(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), "holy_pulse")) {
            player.sendMessage("§eHoly Pulse: " + cd.getRemainingSeconds(player.getUniqueId(), "holy_pulse") + "s");
            return;
        }
        cd.set(player.getUniqueId(), "holy_pulse", plugin.getConfigManager().skillCooldownMs("excalibur", "holy_pulse"));

        double dmg    = plugin.getConfigManager().skill("excalibur", "holy_pulse", "damage", 18.0);
        double radius = plugin.getConfigManager().skill("excalibur", "holy_pulse", "radius", 10.0);
        double kbMult = plugin.getConfigManager().skill("excalibur", "holy_pulse", "knockback", 0.8);
        double kbY    = plugin.getConfigManager().skill("excalibur", "holy_pulse", "knockback_y", 0.6);

        World world = player.getWorld();
        Location loc = player.getLocation().add(0, 1, 0);

        player.sendMessage("§e✦ Holy Pulse!");
        world.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, 1f, 1.8f);

        // Expanding ring — run every 2 ticks
        new BukkitRunnable() {
            double r = 0.5;
            @Override public void run() {
                if (r > radius + 2) { cancel(); return; }
                ParticleUtils.ring(world, Particle.END_ROD, loc, r, Math.max(12, (int)(r * 4)));
                r += 1.5;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        for (Entity e : world.getNearbyEntities(loc, radius, 5, radius)) {
            if (!(e instanceof LivingEntity le) || e == player || le.isDead()) continue;
            le.damage(dmg, player);
            le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));
            Vector knock = e.getLocation().toVector().subtract(loc.toVector())
                .normalize().multiply(kbMult).setY(kbY);
            le.setVelocity(knock);
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100,
            plugin.getConfigManager().skillInt("excalibur", "holy_pulse", "strength_amplifier", 1)));
    }

    public static void excaliburUlt(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), "excalibur_ult")) {
            player.sendMessage("§eExcalibur: " + cd.getRemainingSeconds(player.getUniqueId(), "excalibur_ult") + "s");
            return;
        }
        cd.set(player.getUniqueId(), "excalibur_ult", plugin.getConfigManager().skillCooldownMs("excalibur", "excalibur_ult"));

        double dmgPerTick = plugin.getConfigManager().skill("excalibur", "excalibur_ult", "damage_per_tick", 20.0);
        double radius     = plugin.getConfigManager().skill("excalibur", "excalibur_ult", "radius", 12.0);
        double height     = plugin.getConfigManager().skill("excalibur", "excalibur_ult", "height", 110.0);
        int duration      = plugin.getConfigManager().skillInt("excalibur", "excalibur_ult", "duration", 7) * 20;

        World world = player.getWorld();
        Location target = player.getTargetBlock(null, 50).getLocation().add(0.5, 0, 0.5);

        player.sendMessage("§e§l★ EXCALIBUR — HEAVEN DESCENDS");
        world.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 0.6f);
        player.setInvulnerable(true);

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (!p.equals(player))
                p.sendMessage(plugin.getConfigManager().getMessage("excalibur_ult_broadcast", "{player}", player.getName()));
        }

        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (!player.isOnline() || tick > duration) {
                    world.strikeLightningEffect(target);
                    world.spawnParticle(Particle.EXPLOSION, target, 1, 0, 0, 0, 0);
                    player.setInvulnerable(false);
                    cancel();
                    return;
                }

                // Falling light streaks — particles start high and drift down
                // Every 3 ticks spawn a fresh batch of falling streaks
                if (tick % 3 == 0) {
                    spawnFallingBeams(target, world, radius, height, tick);
                }

                // Pulsing ground ring — beat every 8 ticks for "alive" feel
                if (tick % 8 == 0) {
                    double pulseR = radius * (0.6 + 0.4 * Math.sin(tick * 0.15));
                    ParticleUtils.ring(world, Particle.END_ROD, target.clone().add(0, 0.2, 0),
                        pulseR, (int)(pulseR * 4));
                    ParticleUtils.ring(world, Particle.ENCHANT, target.clone().add(0, 0.1, 0),
                        pulseR * 0.6, (int)(pulseR * 2));
                }

                // Damage
                if (tick % 4 == 0) {
                    for (Entity e : world.getNearbyEntities(target, radius, height, radius)) {
                        if (!(e instanceof LivingEntity le) || e == player || le.isDead()) continue;
                        le.damage(dmgPerTick, player);
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 3));
                        if (tick % 12 == 0) le.setVelocity(new Vector(0, 1.4, 0));
                    }
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Spawns END_ROD particles at random X/Z within radius at full height,
     * offset slightly so they appear to be falling streaks each frame.
     * Cheap: just a handful of point-particles staggered vertically.
     */
    private static void spawnFallingBeams(Location center, World world, double radius, double height, int tick) {
        // 6 falling streaks per call — each a short vertical line of 4 points
        double angleOffset = tick * 0.18; // slow rotation so they don't look static
        int beamCount = 6;
        for (int i = 0; i < beamCount; i++) {
            double a = (Math.PI * 2 / beamCount) * i + angleOffset;
            double r = radius * (0.3 + 0.7 * ((i % 3) / 2.0 + 0.2));
            double x = Math.cos(a) * r;
            double z = Math.sin(a) * r;

            // Streak position falls over time using tick as offset
            double baseY = height - (tick % 60) * (height / 60.0);

            for (int seg = 0; seg < 4; seg++) {
                double y = baseY + seg * 4.0;
                if (y < 0 || y > height) continue;
                Location pt = center.clone().add(x, y, z);
                world.spawnParticle(Particle.END_ROD, pt, 1, 0.05, 0.2, 0.05, 0.02);
            }
        }

        // A few random inner sparkles for depth
        if (tick % 6 == 0) {
            for (int i = 0; i < 3; i++) {
                double a = Math.random() * Math.PI * 2;
                double r = Math.random() * radius * 0.8;
                double y = Math.random() * height;
                world.spawnParticle(Particle.END_ROD,
                    center.clone().add(Math.cos(a) * r, y, Math.sin(a) * r),
                    1, 0, 0, 0, 0.04);
            }
        }
    }
}
