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

public class KaguraSkills {

    // ── Passive ───────────────────────────────────────────────────────────────
    // Volatile duality — 35% wither+fire, 65% slowness; always bonus damage
    public static void applyKaguraPassive(LivingEntity target, Player attacker, MythicBladesPlugin plugin) {
        double chance  = plugin.getConfigManager().skill("kagura_no_tachi", "passive", "wither_chance", 0.35);
        int witDur     = plugin.getConfigManager().skillInt("kagura_no_tachi", "passive", "wither_duration", 40);
        int witAmp     = plugin.getConfigManager().skillInt("kagura_no_tachi", "passive", "wither_amplifier", 1);
        int fireTicks  = plugin.getConfigManager().skillInt("kagura_no_tachi", "passive", "fire_ticks_wither", 60);
        int slwDur     = plugin.getConfigManager().skillInt("kagura_no_tachi", "passive", "slowness_duration", 30);
        int slwAmp     = plugin.getConfigManager().skillInt("kagura_no_tachi", "passive", "slowness_amplifier", 1);
        double bonusDmg= plugin.getConfigManager().skill("kagura_no_tachi", "passive", "bonus_damage", 2.0);

        if (Math.random() < chance) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, witDur, witAmp));
            target.setFireTicks(fireTicks);
            // Duality hit effect — flame + holy
            ParticleUtils.spawn(target.getWorld(), Particle.FLAME,   target.getLocation().add(0, 1, 0), 3, 0.2, 0.2, 0.2, 0.03);
            ParticleUtils.spawn(target.getWorld(), Particle.END_ROD, target.getLocation().add(0, 1, 0), 2, 0.15, 0.15, 0.15, 0.02);
        } else {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slwDur, slwAmp));
        }
        target.damage(bonusDmg, attacker);
    }

    // ── Dual Resonance (RMB) ──────────────────────────────────────────────────
    // Twin diverging sweeps (fire left, holy right) converging on a forward drill point
    public static void dualResonance(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), "dual_resonance")) {
            player.sendMessage("§5Dual Resonance: " + cd.getRemainingSeconds(player.getUniqueId(), "dual_resonance") + "s");
            return;
        }
        cd.set(player.getUniqueId(), "dual_resonance", plugin.getConfigManager().skillCooldownMs("kagura_no_tachi", "dual_resonance"));

        double dmg     = plugin.getConfigManager().skill("kagura_no_tachi", "dual_resonance", "damage", 6.0);
        int steps      = plugin.getConfigManager().skillInt("kagura_no_tachi", "dual_resonance", "sweep_steps", 40);
        double stepSz  = plugin.getConfigManager().skill("kagura_no_tachi", "dual_resonance", "sweep_step_size", 0.8);
        double hbox    = plugin.getConfigManager().skill("kagura_no_tachi", "dual_resonance", "sweep_hitbox", 2.0);
        int drillSt    = plugin.getConfigManager().skillInt("kagura_no_tachi", "dual_resonance", "drill_steps", 20);
        double drillR  = plugin.getConfigManager().skill("kagura_no_tachi", "dual_resonance", "drill_radius", 2.0);

        World world = player.getWorld();
        Location start = player.getLocation().add(0, 1, 0);
        Vector dir = player.getLocation().getDirection().normalize();

        player.sendMessage("§5✦ Dual Resonance");
        world.playSound(start, Sound.BLOCK_BEACON_POWER_SELECT, 0.7f, 1.5f);

        // Fire sweep (left) — FLAME particles
        runSweep(world, start.clone(), dir, player, true, dmg, steps, stepSz, hbox, Particle.FLAME, plugin);
        // Holy sweep (right) — END_ROD particles
        runSweep(world, start.clone(), dir, player, false, dmg, steps, stepSz, hbox, Particle.END_ROD, plugin);

        // Drill convergence visual at center
        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (t > drillSt) { cancel(); return; }
                Vector spin = rotateY(dir, t * 30);
                Location p = start.clone().add(spin.multiply(drillR));
                world.spawnParticle(Particle.END_ROD, p, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.FLAME,   p, 1, 0, 0, 0, 0);
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static void runSweep(World world, Location start, Vector dir,
                                  Player player, boolean left, double dmg,
                                  int steps, double stepSz, double hbox,
                                  Particle particle, MythicBladesPlugin plugin) {
        Set<UUID> hit = new HashSet<>();
        new BukkitRunnable() {
            int step = 0;
            Location cur = start.clone();
            @Override public void run() {
                if (step > steps) { cancel(); return; }
                cur.add(dir.clone().multiply(stepSz));
                double angle = (left ? -step : step) * 2.0;
                Vector sweep = rotateY(dir, angle);
                Location p = cur.clone().add(sweep.multiply(0.5));
                if (step % 2 == 0) world.spawnParticle(particle, p, 1, 0.05, 0.1, 0.05, 0.02);
                for (Entity e : world.getNearbyEntities(cur, hbox, hbox, hbox)) {
                    if (!(e instanceof LivingEntity le) || e == player || !hit.add(e.getUniqueId())) continue;
                    le.damage(dmg, player);
                }
                step++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ── Tenchi Kaimei (Shift+RMB) ─────────────────────────────────────────────
    // Heaven and hell descend simultaneously — fire erupts below, holy crashes above
    // Only Singularity rival to Blade of Thaw's Absolute Zero
    public static void tenchiKaimei(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), "tenchi_kaimei")) {
            player.sendMessage("§5Tenchi Kaimei: " + cd.getRemainingSeconds(player.getUniqueId(), "tenchi_kaimei") + "s");
            return;
        }
        cd.set(player.getUniqueId(), "tenchi_kaimei", plugin.getConfigManager().skillCooldownMs("kagura_no_tachi", "tenchi_kaimei"));

        double dmg     = plugin.getConfigManager().skill("kagura_no_tachi", "tenchi_kaimei", "damage", 35.0);
        double radius  = plugin.getConfigManager().skill("kagura_no_tachi", "tenchi_kaimei", "radius", 18.0);
        double height  = plugin.getConfigManager().skill("kagura_no_tachi", "tenchi_kaimei", "height", 50.0);
        int windup     = plugin.getConfigManager().skillInt("kagura_no_tachi", "tenchi_kaimei", "windup_ticks", 35);
        double descent = plugin.getConfigManager().skill("kagura_no_tachi", "tenchi_kaimei", "descent_step", 1.4);
        boolean invuln = plugin.getConfigManager().skillBoolean("kagura_no_tachi", "tenchi_kaimei", "invuln", true);

        World world = player.getWorld();
        Location center = player.getLocation();

        player.sendMessage("§5§l☯ TENCHI KAIMEI — HEAVEN AND HELL");
        world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 1f, 0.8f);
        world.playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.5f);
        if (invuln) player.setInvulnerable(true);

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (!p.equals(player))
                p.sendMessage(plugin.getConfigManager().getMessage("kagura_ult_broadcast", "{player}", player.getName()));
        }

        // Simultaneous descent — fire from below, holy from above, meeting in middle
        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (t > windup) {
                    // Convergence burst
                    world.spawnParticle(Particle.EXPLOSION,     center, 3, 1.5, 0.5, 1.5, 0);
                    world.spawnParticle(Particle.FLAME,         center, 20, 2, 1, 2, 0.1);
                    world.spawnParticle(Particle.END_ROD,       center, 20, 2, 1, 2, 0.1);
                    world.spawnParticle(Particle.DRAGON_BREATH, center, 15, 3, 1, 3, 0.05);
                    world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.5f);
                    world.strikeLightningEffect(center);

                    for (Entity e : world.getNearbyEntities(center, radius, height, radius)) {
                        if (!(e instanceof LivingEntity le) || e == player) continue;
                        le.damage(dmg, player);
                        le.setFireTicks(100);
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 3));
                    }

                    if (invuln) player.setInvulnerable(false);
                    cancel();
                    return;
                }

                // Fire ascending from below
                double fireY = t * (height / windup) * 0.5;
                Location firePt = center.clone().add(0, fireY, 0);
                if (t % 2 == 0) {
                    world.spawnParticle(Particle.FLAME, firePt, 3, 0.5, 0.1, 0.5, 0.04);
                    world.spawnParticle(Particle.LAVA,  firePt, 1, 0.2, 0.1, 0.2, 0);
                }

                // Holy descending from above
                double holyY = height - t * (height / windup) * 0.5;
                Location holyPt = center.clone().add(0, holyY, 0);
                if (t % 2 == 1) {
                    world.spawnParticle(Particle.END_ROD, holyPt, 3, 0.3, 0.1, 0.3, 0.02);
                    world.spawnParticle(Particle.ENCHANT, holyPt, 2, 0.2, 0.1, 0.2, 0.05);
                }

                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static Vector rotateY(Vector v, double deg) {
        double r = Math.toRadians(deg);
        double cos = Math.cos(r), sin = Math.sin(r);
        return new Vector(v.getX() * cos + v.getZ() * sin, v.getY(), -v.getX() * sin + v.getZ() * cos);
    }
}
