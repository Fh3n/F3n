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
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class KaguraSkills {

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
            ParticleUtils.spawn(target.getWorld(), Particle.FLAME,   target.getLocation().add(0, 1, 0), 3, 0.2, 0.2, 0.2, 0.03);
            ParticleUtils.spawn(target.getWorld(), Particle.END_ROD, target.getLocation().add(0, 1, 0), 2, 0.15, 0.15, 0.15, 0.02);
        } else {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slwDur, slwAmp));
        }
        target.damage(bonusDmg, attacker);
    }

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

        runSweep(world, start.clone(), dir, player, true, dmg, steps, stepSz, hbox, Particle.FLAME, plugin);
        runSweep(world, start.clone(), dir, player, false, dmg, steps, stepSz, hbox, Particle.END_ROD, plugin);

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

    public static void tenchiKaimei(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), "tenchi_kaimei")) {
            player.sendMessage("§5Tenchi Kaimei: " + cd.getRemainingSeconds(player.getUniqueId(), "tenchi_kaimei") + "s");
            return;
        }
        cd.set(player.getUniqueId(), "tenchi_kaimei", plugin.getConfigManager().skillCooldownMs("kagura_no_tachi", "tenchi_kaimei"));

        double dmg = plugin.getConfigManager().skill("kagura_no_tachi", "tenchi_kaimei", "damage", 35.0);

        World world = player.getWorld();
        Location center = player.getLocation().clone();

        player.sendMessage("§5§l☯ TENCHI KAIMEI");
        world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 1f, 0.8f);
        world.playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.5f);

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (!p.equals(player))
                p.sendMessage(plugin.getConfigManager().getMessage("kagura_ult_broadcast", "{player}", player.getName()));
        }

        Random rand = new Random();
        final double RADIUS = 20.0;
        final int DURATION_TICKS = 200; // 10 seconds
        // How many swords fall per tick — ramps up over duration
        final int SWORDS_PER_TICK = 3;
        final double DROP_HEIGHT = 20.0;

        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (!player.isOnline() || t >= DURATION_TICKS) { cancel(); return; }

                // Snap center to player each tick so it follows them
                Location cur = player.getLocation();

                for (int s = 0; s < SWORDS_PER_TICK; s++) {
                    double angle = rand.nextDouble() * Math.PI * 2;
                    double dist  = Math.sqrt(rand.nextDouble()) * RADIUS;
                    double ox = Math.cos(angle) * dist;
                    double oz = Math.sin(angle) * dist;

                    Location spawnLoc = cur.clone().add(ox, DROP_HEIGHT, oz);

                    // ArmorStand holding a netherite sword, tilted to point downward
                    ArmorStand stand = (ArmorStand) world.spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
                    stand.setVisible(false);
                    stand.setGravity(false);
                    stand.setSmall(true);
                    stand.setMarker(true);
                    stand.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(Material.NETHERITE_SWORD));
                    // Tilt arm so sword points straight down
                    stand.setRightArmPose(new org.bukkit.util.EulerAngle(Math.toRadians(200), 0, 0));

                    final double fallSpeed = 1.2 + rand.nextDouble() * 0.8;

                    new BukkitRunnable() {
                        int life = 0;
                        @Override public void run() {
                            if (life > 30) { stand.remove(); cancel(); return; }
                            Location sl = stand.getLocation().subtract(0, fallSpeed, 0);
                            stand.teleport(sl);

                            world.spawnParticle(Particle.CRIT,    sl, 2, 0.05, 0.1, 0.05, 0.1);
                            world.spawnParticle(Particle.END_ROD, sl, 1, 0,    0.05, 0,    0.02);

                            // Damage anything within 1 block
                            for (Entity e : world.getNearbyEntities(sl, 1.0, 1.5, 1.0)) {
                                if (!(e instanceof LivingEntity le) || e == player || e instanceof ArmorStand) continue;
                                le.damage(dmg, player);
                                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1, false, false, false));
                                world.spawnParticle(Particle.CRIT, sl, 10, 0.3, 0.3, 0.3, 0.2);
                                world.playSound(sl, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.7f, 0.9f + (float)rand.nextDouble() * 0.4f);
                                stand.remove();
                                cancel();
                                return;
                            }

                            // Remove if it hits the ground
                            if (sl.getBlock().getType().isSolid()) { stand.remove(); cancel(); }
                            life++;
                        }
                    }.runTaskTimer(plugin, 0L, 1L);
                }

                // Impact sound occasionally
                if (t % 5 == 0) world.playSound(cur, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.3f, 0.8f + (float)rand.nextDouble() * 0.4f);

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
