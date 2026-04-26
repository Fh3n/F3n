package dev.mythicblades.swords;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.utils.ParticleUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
            ParticleUtils.spawn(target.getWorld(), Particle.FLAME,   target.getLocation().add(0, 1, 0), 2, 0.2, 0.2, 0.2, 0.03);
            ParticleUtils.spawn(target.getWorld(), Particle.END_ROD, target.getLocation().add(0, 1, 0), 1, 0.15, 0.15, 0.15, 0.02);
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

        double dmg    = plugin.getConfigManager().skill("kagura_no_tachi", "dual_resonance", "damage", 6.0);
        int steps     = plugin.getConfigManager().skillInt("kagura_no_tachi", "dual_resonance", "sweep_steps", 40);
        double stepSz = plugin.getConfigManager().skill("kagura_no_tachi", "dual_resonance", "sweep_step_size", 0.8);
        double hbox   = plugin.getConfigManager().skill("kagura_no_tachi", "dual_resonance", "sweep_hitbox", 2.0);
        int drillSt   = plugin.getConfigManager().skillInt("kagura_no_tachi", "dual_resonance", "drill_steps", 20);
        double drillR = plugin.getConfigManager().skill("kagura_no_tachi", "dual_resonance", "drill_radius", 2.0);

        World world = player.getWorld();
        Location start = player.getLocation().add(0, 1, 0);
        Vector dir = player.getLocation().getDirection().normalize();

        player.sendMessage("§5✦ Dual Resonance");
        world.playSound(start, Sound.BLOCK_BEACON_POWER_SELECT, 0.7f, 1.5f);

        runSweep(world, start.clone(), dir, player, true,  dmg, steps, stepSz, hbox, Particle.FLAME,   plugin);
        runSweep(world, start.clone(), dir, player, false, dmg, steps, stepSz, hbox, Particle.END_ROD, plugin);

        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (t > drillSt) { cancel(); return; }
                if (t % 2 == 0) {
                    Vector spin = rotateY(dir, t * 30);
                    Location p = start.clone().add(spin.multiply(drillR));
                    world.spawnParticle(Particle.END_ROD, p, 1, 0, 0, 0, 0);
                }
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
                if (step % 3 == 0) {
                    double angle = (left ? -step : step) * 2.0;
                    Vector sweep = rotateY(dir, angle);
                    Location p = cur.clone().add(sweep.multiply(0.5));
                    world.spawnParticle(particle, p, 1, 0.05, 0.1, 0.05, 0.02);
                }
                for (Entity e : world.getNearbyEntities(cur, hbox, hbox, hbox)) {
                    if (!(e instanceof LivingEntity le) || e == player || le.isDead() || !hit.add(e.getUniqueId())) continue;
                    le.damage(dmg, player);
                }
                step++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Tenchi Kaimei — optimized.
     * Instead of spawning 3 ArmorStands every tick (600+ over 10s), we maintain a
     * small pool of REUSED ArmorStands (12) that are teleported back to the top and
     * recycled once they hit the ground or a target. This keeps entity count capped
     * at ~12 at all times regardless of duration.
     */
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
        final double RADIUS     = 20.0;
        final double DROP_HEIGHT = 20.0;
        final int    POOL_SIZE  = 12;   // max concurrent falling swords
        final int    DURATION   = 200;  // ticks

        // Spawn the pool of reusable armor stands above the player
        List<ArmorStand> pool = new ArrayList<>();
        for (int i = 0; i < POOL_SIZE; i++) {
            ArmorStand stand = (ArmorStand) world.spawnEntity(
                center.clone().add(0, DROP_HEIGHT + 5, 0), EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setSmall(true);
            stand.setMarker(true);
            stand.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(Material.NETHERITE_SWORD));
            stand.setRightArmPose(new org.bukkit.util.EulerAngle(Math.toRadians(200), 0, 0));
            // Park it high out of sight initially
            pool.add(stand);
        }

        // Per-stand fall state: index → remaining fall ticks before reset
        int[] standLife = new int[POOL_SIZE];
        double[] fallSpeed = new double[POOL_SIZE];
        boolean[] active = new boolean[POOL_SIZE];
        double[] targetX = new double[POOL_SIZE];
        double[] targetZ = new double[POOL_SIZE];

        new BukkitRunnable() {
            int t = 0;

            @Override public void run() {
                if (!player.isOnline() || t >= DURATION) {
                    // Clean up all pool stands
                    for (ArmorStand s : pool) s.remove();
                    cancel();
                    return;
                }

                Location playerLoc = player.getLocation();

                // Activate an idle stand every 4 ticks (= ~3 per sec visible at any one time cycling)
                if (t % 4 == 0) {
                    for (int i = 0; i < POOL_SIZE; i++) {
                        if (!active[i]) {
                            double angle = rand.nextDouble() * Math.PI * 2;
                            double dist  = Math.sqrt(rand.nextDouble()) * RADIUS;
                            targetX[i] = playerLoc.getX() + Math.cos(angle) * dist;
                            targetZ[i] = playerLoc.getZ() + Math.sin(angle) * dist;
                            Location spawnLoc = new Location(world, targetX[i],
                                playerLoc.getY() + DROP_HEIGHT, targetZ[i],
                                playerLoc.getYaw(), playerLoc.getPitch());
                            pool.get(i).teleport(spawnLoc);
                            standLife[i] = 0;
                            fallSpeed[i] = 1.0 + rand.nextDouble() * 0.8;
                            active[i] = true;
                            break;
                        }
                    }
                }

                // Tick each active stand
                for (int i = 0; i < POOL_SIZE; i++) {
                    if (!active[i]) continue;
                    ArmorStand stand = pool.get(i);
                    standLife[i]++;

                    if (standLife[i] > 28) {
                        // Timed out, park and recycle
                        stand.teleport(playerLoc.clone().add(0, DROP_HEIGHT + 10, 0));
                        active[i] = false;
                        continue;
                    }

                    Location sl = stand.getLocation().subtract(0, fallSpeed[i], 0);
                    stand.teleport(sl);

                    if (standLife[i] % 3 == 0)
                        world.spawnParticle(Particle.CRIT, sl, 1, 0.05, 0.1, 0.05, 0.1);

                    boolean hit = false;
                    for (Entity e : world.getNearbyEntities(sl, 1.0, 1.5, 1.0)) {
                        if (!(e instanceof LivingEntity le) || e == player || le.isDead() || e instanceof ArmorStand || le.isDead()) continue;
                        le.damage(dmg, player);
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1, false, false, false));
                        world.spawnParticle(Particle.CRIT, sl, 6, 0.3, 0.3, 0.3, 0.2);
                        world.playSound(sl, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.7f, 0.9f + rand.nextFloat() * 0.4f);
                        hit = true;
                        break;
                    }

                    if (hit || sl.getBlock().getType().isSolid()) {
                        if (!hit) {
                            world.spawnParticle(Particle.END_ROD, sl, 3, 0.2, 0.1, 0.2, 0.05);
                        }
                        stand.teleport(playerLoc.clone().add(0, DROP_HEIGHT + 10, 0));
                        active[i] = false;
                    }
                }

                if (t % 10 == 0)
                    world.playSound(playerLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.3f, 0.8f + rand.nextFloat() * 0.4f);

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
