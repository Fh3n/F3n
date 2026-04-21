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
import java.util.Map;
import java.util.UUID;

public class ExcaliburSkills {

    private static final String SKILL_TWIN  = "twin_strike";
    private static final String SKILL_PULSE = "holy_pulse";
    private static final String ULT         = "excalibur_ult";

    private static final Map<UUID, Boolean> twinLock = new HashMap<>();

    // ── PASSIVE ─────────────────────────────
    public static void applyLightPassive(LivingEntity target, Player attacker, MythicBladesPlugin plugin) {
        target.damage(4.0, attacker);
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 30, 0, false, false, false));
        ParticleUtils.spawnLightHitEffect(target.getLocation());
    }

    // ── TWIN STRIKE ─────────────────────────
    public static void twinStrike(Player player, MythicBladesPlugin plugin) {

        var cd = plugin.getCooldownManager();

        if (cd.isOnCooldown(player.getUniqueId(), SKILL_TWIN)) {
            player.sendMessage("§eTwin Strike: " +
                    cd.getRemainingSeconds(player.getUniqueId(), SKILL_TWIN) + "s");
            return;
        }

        if (twinLock.getOrDefault(player.getUniqueId(), false)) return;
        twinLock.put(player.getUniqueId(), true);

        cd.set(player.getUniqueId(), SKILL_TWIN,
                plugin.getConfigManager().getCooldownMs("excalibur", "twin_strike"));

        World world = player.getWorld();
        Location loc = player.getLocation().add(0, 1, 0);
        Vector dir = player.getLocation().getDirection().normalize();

        world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1.4f);
        arcStrike(player, loc, dir, world, 14.0);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                world.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1.8f);

                arcStrike(
                        player,
                        player.getLocation().add(0, 1, 0),
                        player.getLocation().getDirection().normalize(),
                        world,
                        16.0
                );

                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 1));
                twinLock.put(player.getUniqueId(), false);
            }
        }.runTaskLater(plugin, 10L);
    }

    // ── ARC STRIKE ─────────────────────────
    private static void arcStrike(Player player, Location start, Vector dir, World world, double dmg) {

        new BukkitRunnable() {

            int step = 0;
            Location cur = start.clone();

            @Override
            public void run() {

                if (step > 14) {
                    cancel();
                    return;
                }

                cur.add(dir.clone().multiply(0.7));

                world.spawnParticle(Particle.SWEEP_ATTACK, cur, 1);

                for (Entity e : world.getNearbyEntities(cur, 1.3, 1.3, 1.3)) {
                    if (e instanceof LivingEntity le && e != player) {
                        le.damage(dmg, player);
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                    }
                }

                step++;
            }
        }.runTaskTimer(MythicBladesPlugin.getPlugin(MythicBladesPlugin.class), 0L, 1L);
    }

    // ── HOLY PULSE ─────────────────────────
    public static void holyPulse(Player player, MythicBladesPlugin plugin) {

        var cd = plugin.getCooldownManager();

        if (cd.isOnCooldown(player.getUniqueId(), SKILL_PULSE)) {
            player.sendMessage("§eHoly Pulse: " +
                    cd.getRemainingSeconds(player.getUniqueId(), SKILL_PULSE) + "s");
            return;
        }

        cd.set(player.getUniqueId(), SKILL_PULSE,
                plugin.getConfigManager().getCooldownMs("excalibur", "holy_pulse"));

        World world = player.getWorld();
        Location loc = player.getLocation().add(0, 1, 0);

        player.sendMessage("§e✦ Holy Pulse!");
        world.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, 1f, 1.8f);

        new BukkitRunnable() {

            double r = 0.5;

            @Override
            public void run() {

                if (r > 10) {
                    cancel();
                    return;
                }

                ParticleUtils.spawnExcaliburUltRing(loc, r, world);
                r += 1.0;
            }
        }.runTaskTimer(plugin, 0L, 3L);

        for (Entity e : world.getNearbyEntities(loc, 10, 5, 10)) {
            if (e instanceof LivingEntity le && e != player) {

                le.damage(18.0, player);
                le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));

                Vector knock = e.getLocation().toVector()
                        .subtract(loc.toVector())
                        .normalize()
                        .multiply(0.8)
                        .setY(0.6);

                le.setVelocity(knock);
            }
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 1));
    }

    // ── EXCALIBUR ULT (TRUE HEAVEN DROP VERSION) ─────────────────────────
    public static void excaliburUlt(Player player, MythicBladesPlugin plugin) {

        var cd = plugin.getCooldownManager();

        if (cd.isOnCooldown(player.getUniqueId(), ULT)) {
            player.sendMessage("§eExcalibur: " +
                    cd.getRemainingSeconds(player.getUniqueId(), ULT) + "s");
            return;
        }

        cd.set(player.getUniqueId(), ULT,
                plugin.getConfigManager().getCooldownMs("excalibur", "excalibur_ult"));

        World world = player.getWorld();

        Location target = player.getTargetBlock(null, 50)
                .getLocation().add(0.5, 0, 0.5);

        double radius = 12.0;
        double height = 110.0; // 🔥 HEAVEN DROP HEIGHT
        int duration = 7 * 20;

        player.sendMessage("§e§l★ EXCALIBUR — HEAVEN DESCENDS");
        world.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 0.6f);

        player.setInvulnerable(true);

        new BukkitRunnable() {

            int tick = 0;

            @Override
            public void run() {

                if (!player.isOnline() || tick > duration) {

                    world.strikeLightningEffect(target);

                    world.spawnParticle(Particle.EXPLOSION, target, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.END_ROD, target.clone().add(0, 2, 0), 8, 0.5, 1, 0.5, 0.02);

                    player.setInvulnerable(false);
                    cancel();
                    return;
                }

                // massive sky pillar
                if (tick % 4 == 0) {
                    spawnVerticalPillar(target, world, 25.0, height);
                }

                // FULL AREA EXECUTION (NOT DIRECTIONAL)
                for (Entity e : world.getNearbyEntities(target, radius, height, radius)) {

                    if (!(e instanceof LivingEntity le) || e == player) continue;

                    le.damage(20.0, player);
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 3));

                    if (tick % 8 == 0) {
                        le.setVelocity(new Vector(0, 1.4, 0));
                    }
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static void spawnVerticalPillar(Location center, World world,
                                            double radius, double height) {

        for (double y = 0; y <= height; y += 5.0) {
            Location p = center.clone().add(0, y, 0);

            ParticleUtils.spawnExcaliburUltRing(
                    p,
                    radius * (1 - (y / height) * 0.5),
                    world
            );
        }
    }
}