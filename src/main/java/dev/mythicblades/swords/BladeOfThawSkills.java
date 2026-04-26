package dev.mythicblades.swords;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.utils.ParticleUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class BladeOfThawSkills {

    private static final Map<UUID, Boolean> resurrectionReady = new HashMap<>();
    private static final Map<UUID, Boolean> sentinelsActive   = new HashMap<>();
    private static final Set<UUID>          inResurrection    = new HashSet<>();

    public static void applyFrostPassive(LivingEntity target, Player owner, MythicBladesPlugin plugin) {
        int amp = plugin.getConfigManager().skillInt("blade_of_thaw", "passive", "slow_amplifier", 1);
        int dur = plugin.getConfigManager().skillInt("blade_of_thaw", "passive", "slow_duration", 40);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, dur, amp));
        if (areSentinelsActive(owner.getUniqueId())) {
            double dmg = plugin.getConfigManager().skill("blade_of_thaw", "sentinels", "counter_damage", 15.0);
            target.damage(dmg, owner);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3));
            ParticleUtils.spawn(target.getWorld(), Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0),
                6, 0.4, 0.4, 0.4, 0.08);
        }
    }

    public static void toggleSentinels(Player player, MythicBladesPlugin plugin) {
        boolean next = !sentinelsActive.getOrDefault(player.getUniqueId(), false);
        sentinelsActive.put(player.getUniqueId(), next);
        player.sendMessage(next ? "§b❄ Sentinels activated." : "§7Sentinels deactivated.");
        if (next) player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.7f, 1.8f);
    }

    public static void tickSentinels(Player player, MythicBladesPlugin plugin) {
        if (!areSentinelsActive(player.getUniqueId())) return;
        int count = plugin.getConfigManager().skillInt("blade_of_thaw", "sentinels", "count", 6);
        ParticleUtils.sentinelOrbit(player, count);
    }

    public static boolean areSentinelsActive(UUID uuid) {
        return sentinelsActive.getOrDefault(uuid, false);
    }

    public static void glacialMonolith(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), "glacial_monolith")) {
            player.sendMessage("§bGlacial Monolith: " +
                cd.getRemainingSeconds(player.getUniqueId(), "glacial_monolith") + "s");
            return;
        }
        cd.set(player.getUniqueId(), "glacial_monolith",
            plugin.getConfigManager().skillCooldownMs("blade_of_thaw", "glacial_monolith"));

        World world = player.getWorld();
        Location base = player.getLocation();
        double radius = plugin.getConfigManager().skill("blade_of_thaw", "glacial_monolith", "radius", 8.0);
        double dmg    = plugin.getConfigManager().skill("blade_of_thaw", "glacial_monolith", "damage", 45.0);

        player.sendMessage("§b❄ Glacial Monolith!");
        world.playSound(base, Sound.BLOCK_GLASS_BREAK, 1f, 0.5f);
        world.playSound(base, Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.6f);

        // Spike effect — run every 2 ticks (was every 1)
        new BukkitRunnable() {
            int y = 0;
            @Override public void run() {
                if (y > 10) { cancel(); return; }
                Location pt = base.clone().add(0, y * 0.6, 0);
                world.spawnParticle(Particle.BLOCK, pt, 4, 0.3, 0.1, 0.3, Material.ICE.createBlockData());
                world.spawnParticle(Particle.SNOWFLAKE, pt, 2, 0.3, 0.1, 0.3, 0.05);
                y++;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        // Shockwave ring — expanded step size to reduce calls
        new BukkitRunnable() {
            double r = 1;
            @Override public void run() {
                if (r > radius + 2) { cancel(); return; }
                ParticleUtils.ring(world, Particle.SNOWFLAKE, base.clone().add(0, 0.1, 0), r, 16);
                for (Entity e : world.getNearbyEntities(base, r, 3, r)) {
                    if (!(e instanceof LivingEntity le) || e == player || le.isDead()) continue;
                    double dist = e.getLocation().distance(base);
                    if (dist >= r - 1 && dist <= r + 0.5) {
                        le.damage(dmg, player);
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 4));
                        le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1));
                        le.setVelocity(le.getLocation().toVector()
                            .subtract(base.toVector()).normalize().multiply(0.6).setY(0.4));
                    }
                }
                r += 1.5;
            }
        }.runTaskTimer(plugin, 3L, 3L);

        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80,
            plugin.getConfigManager().skillInt("blade_of_thaw", "glacial_monolith", "resistance_amplifier", 3)));
    }

    public static void armResurrection(UUID uuid) {
        resurrectionReady.put(uuid, true);
    }

    public static boolean isInResurrection(UUID uuid) {
        return inResurrection.contains(uuid);
    }

    public static boolean isResurrectionReady(UUID uuid) {
        return resurrectionReady.getOrDefault(uuid, false);
    }

    /**
     * Absolute Zero — heavily trimmed version.
     * Core phases kept intact; particle counts and density drastically reduced.
     * Ice blocks still placed and retracted. Geometry simplified to 6 spikes (no sub-spikes).
     */
    public static void triggerResurrection(Player player, MythicBladesPlugin plugin) {
        if (!isResurrectionReady(player.getUniqueId())) return;
        resurrectionReady.put(player.getUniqueId(), false);
        inResurrection.add(player.getUniqueId());

        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.setInvulnerable(true);

        World world = player.getWorld();
        Location deathLoc = player.getLocation().clone();

        world.playSound(deathLoc, Sound.ENTITY_WITHER_SPAWN, 2f, 0.4f);
        world.playSound(deathLoc, Sound.BLOCK_BEACON_POWER_SELECT, 2f, 0.5f);
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (!p.equals(player))
                p.sendMessage(plugin.getConfigManager().getMessage("absolute_zero_broadcast", "{player}", player.getName()));
        }
        player.sendMessage("§b§l❄ ABSOLUTE ZERO — RISING.");

        Random rand = new Random();
        int[] spikeHeights = new int[6];
        for (int b = 0; b < 6; b++) spikeHeights[b] = 8 + rand.nextInt(5);
        double[] radii = {6.0, 14.0, 22.0};

        List<Block> placedIce   = Collections.synchronizedList(new ArrayList<>());
        List<Block> sphereBlocks = Collections.synchronizedList(new ArrayList<>());

        // Phase 1: spike eruption — tick every 2 ticks
        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (t > 40) { cancel(); return; }

                for (int b = 0; b < 6; b++) {
                    double ang = Math.toRadians(60.0 * b);
                    for (int ri = 0; ri < radii.length; ri++) {
                        int startTick = ri * 8;
                        if (t < startTick) continue;
                        int localT = t - startTick;
                        double br  = radii[ri];
                        int maxH   = spikeHeights[b];
                        double curH = Math.min(maxH, localT * 1.2);
                        double tx = Math.cos(ang) * br;
                        double tz = Math.sin(ang) * br;

                        // Sparse spine particles — every 1.0 block height
                        for (double h = 0; h <= curH; h += 1.0) {
                            Location pt = deathLoc.clone().add(tx, h, tz);
                            world.spawnParticle(Particle.SNOWFLAKE, pt, 1, 0.1, 0.05, 0.1, 0.01);
                            if (h % 2 < 0.5)
                                world.spawnParticle(Particle.BLOCK, pt, 1, 0.1, 0.05, 0.1,
                                    Material.ICE.createBlockData());
                        }

                        // Place real ice blocks once during growth
                        if (localT < 14 && localT % 4 == 0) {
                            for (int h = 1; h <= (int) curH; h++) {
                                Block blk = deathLoc.clone().add(tx, h, tz).getBlock();
                                if (blk.getType() == Material.AIR) {
                                    blk.setType(Material.ICE);
                                    placedIce.add(blk);
                                }
                            }
                        }
                    }

                    // Ground frost ring
                    if (t < 30 && t % 3 == 0) {
                        double reach = t * 0.7;
                        world.spawnParticle(Particle.SNOWFLAKE,
                            deathLoc.clone().add(Math.cos(ang) * reach, 0.1, Math.sin(ang) * reach),
                            1, 0.1, 0, 0.1, 0.01);
                    }
                }

                // Damage pulse every 10 ticks
                if (t % 10 == 0) {
                    double dmgR = Math.min(22, 4 + t * 0.55);
                    for (Entity e : world.getNearbyEntities(deathLoc, dmgR, 12, dmgR)) {
                        if (!(e instanceof LivingEntity le) || e == player || le.isDead()) continue;
                        le.damage(14.0, player);
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 3));
                        le.setVelocity(le.getLocation().toVector()
                            .subtract(deathLoc.toVector()).normalize().multiply(0.6).setY(0.4));
                    }
                }

                if (t % 8 == 0)
                    world.playSound(deathLoc, Sound.BLOCK_GLASS_BREAK, 1f, 0.3f + t * 0.015f);

                t++;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        // Phase 2: launch + suspend
        final int HOLD_START = 28;
        final int HOLD_END   = 95;

        new BukkitRunnable() {
            int t = 0;
            boolean sphereBuilt = false;
            @Override public void run() {
                if (!player.isOnline()) { cancel(); return; }
                if (t < HOLD_START) {
                    double liftForce = 0.9 - t * 0.018;
                    player.setVelocity(new Vector(0, Math.max(0.15, liftForce), 0));
                }
                if (t >= HOLD_START && t < HOLD_END) {
                    Vector vel = player.getVelocity();
                    player.setVelocity(new Vector(vel.getX() * 0.1, 0, vel.getZ() * 0.1));
                    if (!sphereBuilt) {
                        sphereBuilt = true;
                        buildIceSphere(player.getLocation(), world, sphereBlocks);
                        world.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.5f, 0.4f);
                    }
                    // Sparse sphere particles — every 8 ticks only
                    if (t % 8 == 0) {
                        world.spawnParticle(Particle.SNOWFLAKE, player.getLocation().add(0, 1, 0),
                            3, 2.0, 2.0, 2.0, 0.02);
                    }
                }
                if (t >= HOLD_END) {
                    cancel();
                    removeIceSphere(sphereBlocks, world);
                    beginDescent(player, deathLoc, world, placedIce, spikeHeights, radii, plugin);
                    return;
                }
                if (t >= HOLD_START) {
                    double maxHp = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    double curHp = player.getHealth();
                    if (curHp < maxHp) player.setHealth(Math.min(curHp + 0.4, maxHp));
                }
                if (t == 50) world.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.8f);
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static void buildIceSphere(Location centre, World world, List<Block> sphereBlocks) {
        double radius = 2.5;
        for (int dx = -3; dx <= 3; dx++)
            for (int dy = -3; dy <= 3; dy++)
                for (int dz = -3; dz <= 3; dz++)
                    if (dx*dx + dy*dy + dz*dz <= radius * radius) {
                        Block b = centre.clone().add(dx, dy, dz).getBlock();
                        if (b.getType() == Material.AIR) { b.setType(Material.ICE); sphereBlocks.add(b); }
                    }
    }

    private static void removeIceSphere(List<Block> sphereBlocks, World world) {
        for (Block b : sphereBlocks) if (b.getType() == Material.ICE) b.setType(Material.AIR);
        sphereBlocks.clear();
    }

    private static void beginDescent(Player player, Location deathLoc, World world,
                                      List<Block> placedIce, int[] spikeHeights,
                                      double[] radii, MythicBladesPlugin plugin) {
        world.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1.5f);
        player.sendMessage("§b❄§f Absolute Zero — §bDescending.");
        player.setVelocity(new Vector(0, -3.5, 0));

        // Spike retraction — run every 2 ticks
        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (t > 40) {
                    for (Block b : placedIce) if (b.getType() == Material.ICE) b.setType(Material.AIR);
                    placedIce.clear();
                    cancel();
                    return;
                }
                for (int b = 0; b < 6; b++) {
                    double ang = Math.toRadians(60.0 * b);
                    for (int ri = 0; ri < radii.length; ri++) {
                        int maxH = spikeHeights[b];
                        double retractH = Math.max(0, maxH - t * 0.7);
                        double br = radii[ri];
                        double tx = Math.cos(ang) * br;
                        double tz = Math.sin(ang) * br;
                        for (double h = 0; h <= retractH; h += 1.0)
                            world.spawnParticle(Particle.SNOWFLAKE, deathLoc.clone().add(tx, h, tz),
                                1, 0.05, 0.05, 0.05, 0.01);
                        if (t % 4 == 0) {
                            for (int h = maxH; h > (int) retractH; h--) {
                                Block blk = deathLoc.clone().add(tx, h, tz).getBlock();
                                if (blk.getType() == Material.ICE) blk.setType(Material.AIR);
                            }
                        }
                    }
                }
                if (t % 6 == 0)
                    world.playSound(deathLoc, Sound.BLOCK_GLASS_BREAK, 0.6f, 1.0f + t * 0.025f);
                t++;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        waitForLanding(player, world, plugin);
    }

    private static void waitForLanding(Player player, World world, MythicBladesPlugin plugin) {
        new BukkitRunnable() {
            int wait = 0;
            @Override public void run() {
                if (!player.isOnline()) {
                    player.setInvulnerable(false);
                    inResurrection.remove(player.getUniqueId());
                    cancel();
                    return;
                }
                wait++;
                if (wait < 4) return;
                if (player.isOnGround() || wait > 100) {
                    cancel();
                    impactShockwave(player, world, plugin);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static void impactShockwave(Player player, World world, MythicBladesPlugin plugin) {
        Location impact = player.getLocation();
        player.setInvulnerable(false);
        inResurrection.remove(player.getUniqueId());

        world.playSound(impact, Sound.ENTITY_GENERIC_EXPLODE, 2f, 0.4f);
        world.playSound(impact, Sound.BLOCK_GLASS_BREAK, 2f, 0.3f);
        world.playSound(impact, Sound.BLOCK_BEACON_POWER_SELECT, 2f, 1.4f);

        world.spawnParticle(Particle.EXPLOSION,  impact, 2, 1.0, 0.3, 1.0, 0);
        world.spawnParticle(Particle.SNOWFLAKE,  impact, 30, 3.0, 0.8, 3.0, 0.3);
        world.spawnParticle(Particle.END_ROD,    impact, 15, 2.5, 0.5, 2.5, 0.2);
        world.spawnParticle(Particle.BLOCK,      impact, 20, 2.5, 0.6, 2.5,
            Material.ICE.createBlockData());

        player.sendMessage("§b§l❄ ABSOLUTE ZERO — IMPACT.");

        // Shockwave ring
        new BukkitRunnable() {
            double r = 1.0;
            final Set<UUID> hit = new HashSet<>();
            @Override public void run() {
                if (r > 28) { cancel(); return; }
                ParticleUtils.ring(world, Particle.SNOWFLAKE, impact.clone().add(0, 0.15, 0), r, 24);
                if ((int)(r) % 4 == 0)
                    ParticleUtils.ring(world, Particle.END_ROD, impact.clone().add(0, 0.8, 0), r * 0.7, 12);

                for (Entity e : world.getNearbyEntities(impact, r + 1.5, 5, r + 1.5)) {
                    if (!(e instanceof LivingEntity le) || e == player || le.isDead() || !hit.add(e.getUniqueId())) continue;
                    double dist = e.getLocation().distance(impact);
                    if (dist > r + 1.8) continue;
                    le.damage(45.0, player);
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 4));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 160, 2));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));
                    Vector kb = le.getLocation().toVector().subtract(impact.toVector())
                        .normalize().multiply(2.4).setY(1.4);
                    le.setVelocity(kb);
                }
                r += 1.0;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
