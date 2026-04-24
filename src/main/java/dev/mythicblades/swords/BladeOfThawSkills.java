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

    // ── Passive ───────────────────────────────────────────────────────────────
    public static void applyFrostPassive(LivingEntity target, Player owner, MythicBladesPlugin plugin) {
        int amp = plugin.getConfigManager().skillInt("blade_of_thaw", "passive", "slow_amplifier", 1);
        int dur = plugin.getConfigManager().skillInt("blade_of_thaw", "passive", "slow_duration", 40);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, dur, amp));
        if (areSentinelsActive(owner.getUniqueId())) {
            double dmg = plugin.getConfigManager().skill("blade_of_thaw", "sentinels", "counter_damage", 15.0);
            target.damage(dmg, owner);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3));
            ParticleUtils.spawn(target.getWorld(), Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0),
                15, 0.4, 0.4, 0.4, 0.08);
        }
    }

    // ── Sentinels ─────────────────────────────────────────────────────────────
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

    // ── Glacial Monolith (RMB) ────────────────────────────────────────────────
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

        new BukkitRunnable() {
            int y = 0;
            @Override public void run() {
                if (y > 12) { cancel(); return; }
                Location pt = base.clone().add(0, y * 0.5, 0);
                world.spawnParticle(Particle.BLOCK, pt, 8, 0.3, 0.1, 0.3, Material.ICE.createBlockData());
                world.spawnParticle(Particle.SNOWFLAKE, pt, 4, 0.3, 0.1, 0.3, 0.05);
                y++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        new BukkitRunnable() {
            double r = 1;
            @Override public void run() {
                if (r > radius + 2) { cancel(); return; }
                ParticleUtils.ring(world, Particle.SNOWFLAKE, base.clone().add(0, 0.1, 0), r, 24);
                for (Entity e : world.getNearbyEntities(base, r, 3, r)) {
                    if (!(e instanceof LivingEntity le) || e == player) continue;
                    double dist = e.getLocation().distance(base);
                    if (dist >= r - 1 && dist <= r + 0.5) {
                        le.damage(dmg, player);
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 4));
                        le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1));
                        le.setVelocity(le.getLocation().toVector()
                            .subtract(base.toVector()).normalize().multiply(0.6).setY(0.4));
                    }
                }
                r += 1.2;
            }
        }.runTaskTimer(plugin, 3L, 2L);

        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80,
            plugin.getConfigManager().skillInt("blade_of_thaw", "glacial_monolith", "resistance_amplifier", 3)));
    }

    // ── Absolute Zero — arm ───────────────────────────────────────────────────
    public static void armResurrection(UUID uuid) {
        resurrectionReady.put(uuid, true);
    }

    public static boolean isInResurrection(UUID uuid) {
        return inResurrection.contains(uuid);
    }

    public static boolean isResurrectionReady(UUID uuid) {
        return resurrectionReady.getOrDefault(uuid, false);
    }

    // ── Absolute Zero — trigger ───────────────────────────────────────────────
    // Phase 1 : 6-branch snowflake geometry erupts from death point, jagged randomized spikes
    // Phase 2 : Player rockets to 25 blocks, a physical ice sphere forms around them, gravity overridden
    // Phase 3 : Suspension ends instantly, high-velocity drop, invulnerable projectile state,
    //           spikes sink top-down while player falls
    // Phase 4 : Landing strips invuln, FallingBlock ice shards fly outward (aesthetic, removed instantly),
    //           heavy shockwave damages + launches all nearby
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
            world.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 0.5f);
            if (!p.equals(player))
                p.sendMessage(plugin.getConfigManager().getMessage("absolute_zero_broadcast", "{player}", player.getName()));
        }
        player.sendMessage("§b§l❄ ABSOLUTE ZERO — RISING.");

        // Spike heights per arm — randomized once at start for natural look
        // 6 branches, each branch has 4 radial distances with a height value
        // branchSpikeHeights[branch][radialIndex] = height
        int[][] branchSpikeHeights = new int[6][4];
        Random rand = new Random();
        int[] baseHeights = {5, 8, 11, 14};
        for (int b = 0; b < 6; b++) {
            for (int r = 0; r < 4; r++) {
                // +/- 2 blocks variance for jagged look
                branchSpikeHeights[b][r] = baseHeights[r] + rand.nextInt(5) - 2;
            }
        }

        // Radial distances of spike tips on each branch
        double[] branchRadii = {6.0, 11.0, 17.0, 24.0};
        // Ice block columns placed for real
        List<Block> placedIce = Collections.synchronizedList(new ArrayList<>());
        // Ice sphere blocks placed around player at peak
        List<Block> sphereBlocks = Collections.synchronizedList(new ArrayList<>());

        // ── Phase 1: snowflake eruption ───────────────────────────────────────
        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (t > 55) { cancel(); return; }

                for (int b = 0; b < 6; b++) {
                    double branchAngle = Math.toRadians(60.0 * b);

                    for (int ri = 0; ri < branchRadii.length; ri++) {
                        int startTick = ri * 10;
                        if (t < startTick) continue;

                        int localT  = t - startTick;
                        double br   = branchRadii[ri];
                        int maxH    = branchSpikeHeights[b][ri];
                        double currentH = Math.min(maxH, localT * 0.85);

                        double tx = Math.cos(branchAngle) * br;
                        double tz = Math.sin(branchAngle) * br;

                        // Main spike — wide at base, tapers to tip, with 3 parallel columns
                        for (double h = 0; h <= currentH; h += 0.4) {
                            double progress = h / (maxH + 1);
                            // Width narrows from 0.6 at base to 0 at tip
                            double baseWidth = 0.6 * (1.0 - progress);

                            // Center column
                            Location center = deathLoc.clone().add(tx, h, tz);
                            world.spawnParticle(Particle.BLOCK, center, 1,
                                baseWidth * 0.25, 0.05, baseWidth * 0.25,
                                Material.ICE.createBlockData());

                            // Flanking columns — offset perpendicular to branch
                            double perpX = Math.cos(branchAngle + Math.PI / 2) * baseWidth;
                            double perpZ = Math.sin(branchAngle + Math.PI / 2) * baseWidth;
                            if (h % 0.8 < 0.4) {
                                world.spawnParticle(Particle.BLOCK,
                                    deathLoc.clone().add(tx + perpX, h + rand.nextDouble() * 0.3, tz + perpZ),
                                    1, 0.05, 0.08, 0.05, Material.ICE.createBlockData());
                                world.spawnParticle(Particle.BLOCK,
                                    deathLoc.clone().add(tx - perpX, h + rand.nextDouble() * 0.3, tz - perpZ),
                                    1, 0.05, 0.08, 0.05, Material.ICE.createBlockData());
                            }
                            // Snowflake mist along full height
                            if (h % 1.0 < 0.4)
                                world.spawnParticle(Particle.SNOWFLAKE, center, 1, baseWidth * 0.3, 0, baseWidth * 0.3, 0.01);
                        }

                        // Sub-spikes: 2 side spurs off each main spike at ~50-70% height, angled outward
                        for (int side : new int[]{-1, 1}) {
                            double subAngle = branchAngle + side * Math.toRadians(25);
                            double subBr    = br * 0.55 + rand.nextDouble() * br * 0.1;
                            double subMaxH  = currentH * (0.5 + rand.nextDouble() * 0.2);
                            double sx = Math.cos(subAngle) * subBr;
                            double sz = Math.sin(subAngle) * subBr;
                            for (double h = 0; h <= subMaxH; h += 0.5) {
                                double taper = 0.3 * (1.0 - h / (subMaxH + 1));
                                world.spawnParticle(Particle.BLOCK,
                                    deathLoc.clone().add(sx, h, sz),
                                    1, taper * 0.15, 0.04, taper * 0.15,
                                    Material.ICE.createBlockData());
                                if (h % 1.2 < 0.5)
                                    world.spawnParticle(Particle.SNOWFLAKE,
                                        deathLoc.clone().add(sx, h, sz), 1, 0, 0, 0, 0.01);
                            }
                            // Tiny tertiary spur off the sub-spike
                            double tertAngle = subAngle + side * Math.toRadians(15);
                            double tertBr    = subBr * 0.5;
                            double tertMaxH  = subMaxH * 0.55;
                            double tertX = Math.cos(tertAngle) * tertBr;
                            double tertZ = Math.sin(tertAngle) * tertBr;
                            for (double h = 0; h <= tertMaxH; h += 0.6) {
                                world.spawnParticle(Particle.SNOWFLAKE,
                                    deathLoc.clone().add(tertX, h + rand.nextDouble() * 0.2, tertZ),
                                    1, 0.05, 0.05, 0.05, 0.01);
                            }
                        }

                        // Place real ice blocks during growth
                        if (localT < 18 && localT % 3 == 0) {
                            for (int h = 1; h <= (int) currentH; h++) {
                                Block blk = deathLoc.clone().add(tx, h, tz).getBlock();
                                if (blk.getType() == Material.AIR) {
                                    blk.setType(Material.ICE);
                                    placedIce.add(blk);
                                }
                                // Side blocks for width
                                double perpX = Math.cos(branchAngle + Math.PI / 2);
                                double perpZ = Math.sin(branchAngle + Math.PI / 2);
                                if (h < (int)(currentH * 0.6)) {
                                    Block side1 = deathLoc.clone().add(tx + perpX, h, tz + perpZ).getBlock();
                                    Block side2 = deathLoc.clone().add(tx - perpX, h, tz - perpZ).getBlock();
                                    if (side1.getType() == Material.AIR) { side1.setType(Material.ICE); placedIce.add(side1); }
                                    if (side2.getType() == Material.AIR) { side2.setType(Material.ICE); placedIce.add(side2); }
                                }
                            }
                        }
                    }

                    // Ground frost crawling along branch lines
                    if (t < 35) {
                        double groundReach = t * 0.8;
                        double gx = Math.cos(branchAngle) * groundReach;
                        double gz = Math.sin(branchAngle) * groundReach;
                        world.spawnParticle(Particle.SNOWFLAKE,
                            deathLoc.clone().add(gx, 0.1, gz), 2, 0.1, 0, 0.1, 0.01);
                        // Frost between branches too
                        double betweenAngle = branchAngle + Math.toRadians(30);
                        world.spawnParticle(Particle.SNOWFLAKE,
                            deathLoc.clone().add(Math.cos(betweenAngle) * groundReach * 0.6, 0.1,
                                Math.sin(betweenAngle) * groundReach * 0.6),
                            1, 0.1, 0, 0.1, 0.01);
                    }
                }

                // Damage pulse every 8 ticks
                if (t % 8 == 0) {
                    double dmgR = Math.min(24, 5 + t * 0.45);
                    for (Entity e : world.getNearbyEntities(deathLoc, dmgR, 14, dmgR)) {
                        if (!(e instanceof LivingEntity le) || e == player) continue;
                        le.damage(14.0, player);
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 3));
                        le.setVelocity(le.getLocation().toVector()
                            .subtract(deathLoc.toVector()).normalize().multiply(0.6).setY(0.4));
                    }
                }

                if (t % 6 == 0)
                    world.playSound(deathLoc, Sound.BLOCK_GLASS_BREAK, 1f, 0.3f + t * 0.012f);

                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // ── Phase 2: aggressive launch to 25 blocks, ice sphere, gravity override ──
        final double TARGET_HEIGHT = 25.0;
        final int HOLD_START = 28; // tick at which we start holding
        final int HOLD_END   = 95; // tick at which suspension ends → Phase 3

        new BukkitRunnable() {
            int t = 0;
            boolean sphereBuilt = false;

            @Override public void run() {
                if (!player.isOnline()) { cancel(); return; }

                double currentY = player.getLocation().getY() - deathLoc.getY();

                // Aggressive launch for first ~28 ticks
                if (t < HOLD_START) {
                    double liftForce = 0.9 - t * 0.018;
                    player.setVelocity(new Vector(0, Math.max(0.15, liftForce), 0));
                }

                // Hard gravity override during suspension
                if (t >= HOLD_START && t < HOLD_END) {
                    Vector vel = player.getVelocity();
                    player.setVelocity(new Vector(vel.getX() * 0.1, 0, vel.getZ() * 0.1));

                    // Build ice sphere once at peak
                    if (!sphereBuilt) {
                        sphereBuilt = true;
                        buildIceSphere(player.getLocation(), world, sphereBlocks);
                        world.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.5f, 0.4f);
                        world.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 0.8f);
                    }

                    // Keep sphere updated as player may drift slightly
                    if (t % 5 == 0 && !sphereBlocks.isEmpty()) {
                        // just keep particles around the sphere
                        Location pl = player.getLocation().add(0, 1, 0);
                        world.spawnParticle(Particle.SNOWFLAKE, pl, 4, 2.0, 2.0, 2.0, 0.02);
                        world.spawnParticle(Particle.END_ROD, pl, 2, 1.5, 1.5, 1.5, 0.01);
                    }
                }

                // Phase 3 trigger
                if (t >= HOLD_END) {
                    cancel();
                    removeIceSphere(sphereBlocks, world);
                    beginDescent(player, deathLoc, world, placedIce, branchSpikeHeights, branchRadii, plugin);
                    return;
                }

                // Heal during suspension
                if (t >= HOLD_START) {
                    double maxHp  = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    double curHp  = player.getHealth();
                    if (curHp < maxHp) player.setHealth(Math.min(curHp + 0.4, maxHp));
                }

                if (t == 50)
                    world.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.8f);

                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // Build a small physical sphere of ICE blocks around the given centre
    private static void buildIceSphere(Location centre, World world, List<Block> sphereBlocks) {
        double radius = 2.5;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    if (dx * dx + dy * dy + dz * dz <= radius * radius) {
                        Block b = centre.clone().add(dx, dy, dz).getBlock();
                        if (b.getType() == Material.AIR) {
                            b.setType(Material.ICE);
                            sphereBlocks.add(b);
                        }
                    }
                }
            }
        }
    }

    private static void removeIceSphere(List<Block> sphereBlocks, World world) {
        for (Block b : sphereBlocks) {
            if (b.getType() == Material.ICE) b.setType(Material.AIR);
        }
        sphereBlocks.clear();
    }

    // ── Phase 3: instant drop, spike retraction top-down ─────────────────────
    private static void beginDescent(Player player, Location deathLoc, World world,
                                      List<Block> placedIce, int[][] branchSpikeHeights,
                                      double[] branchRadii, MythicBladesPlugin plugin) {
        world.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1.5f);
        world.playSound(deathLoc, Sound.BLOCK_GLASS_BREAK, 2f, 0.25f);
        player.sendMessage("§b❄§f Absolute Zero — §bDescending.");

        // Instant high-velocity drop
        player.setVelocity(new Vector(0, -3.5, 0));

        // Spike retraction: top-down — find max height per spike location, shrink from top
        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (t > 50) {
                    // Clean up all remaining ice
                    for (Block b : placedIce)
                        if (b.getType() == Material.ICE) b.setType(Material.AIR);
                    placedIce.clear();
                    cancel();
                    return;
                }

                for (int b = 0; b < 6; b++) {
                    double branchAngle = Math.toRadians(60.0 * b);
                    for (int ri = 0; ri < branchRadii.length; ri++) {
                        int maxH = branchSpikeHeights[b][ri];
                        // Current visible height shrinks from top
                        double retractH = Math.max(0, maxH - t * 0.6);
                        double br = branchRadii[ri];
                        double tx = Math.cos(branchAngle) * br;
                        double tz = Math.sin(branchAngle) * br;

                        for (double h = 0; h <= retractH; h += 0.5) {
                            world.spawnParticle(Particle.SNOWFLAKE,
                                deathLoc.clone().add(tx, h, tz), 1, 0.05, 0.05, 0.05, 0.02);
                        }

                        // Remove ice blocks above current retract height
                        if (t % 3 == 0) {
                            for (int h = maxH; h > (int) retractH; h--) {
                                Block blk = deathLoc.clone().add(tx, h, tz).getBlock();
                                if (blk.getType() == Material.ICE) blk.setType(Material.AIR);
                            }
                        }
                    }
                }

                if (t % 5 == 0)
                    world.playSound(deathLoc, Sound.BLOCK_GLASS_BREAK, 0.6f, 1.0f + t * 0.025f);

                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // Wait for landing
        waitForLanding(player, world, plugin);
    }

    // ── Wait for landing ──────────────────────────────────────────────────────
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
                    // Keep invulnerable through the landing tick — cleared inside impactShockwave
                    cancel();
                    impactShockwave(player, world, plugin);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ── Phase 4: kinetic impact ───────────────────────────────────────────────
    @SuppressWarnings("deprecation")
    private static void impactShockwave(Player player, World world, MythicBladesPlugin plugin) {
        Location impact = player.getLocation();
        player.setInvulnerable(false);
        inResurrection.remove(player.getUniqueId());

        world.playSound(impact, Sound.ENTITY_GENERIC_EXPLODE, 2f, 0.4f);
        world.playSound(impact, Sound.BLOCK_GLASS_BREAK, 2f, 0.3f);
        world.playSound(impact, Sound.BLOCK_BEACON_POWER_SELECT, 2f, 1.4f);
        world.playSound(impact, Sound.ENTITY_WITHER_SPAWN, 1f, 1.8f);

        world.spawnParticle(Particle.EXPLOSION,  impact, 4, 1.0, 0.3, 1.0, 0);
        world.spawnParticle(Particle.SNOWFLAKE,  impact, 80, 4.0, 1.0, 4.0, 0.35);
        world.spawnParticle(Particle.END_ROD,    impact, 40, 3.0, 0.5, 3.0, 0.25);
        world.spawnParticle(Particle.BLOCK,      impact, 50, 3.0, 0.8, 3.0,
            Material.ICE.createBlockData());

        player.sendMessage("§b§l❄ ABSOLUTE ZERO — IMPACT.");

        // Aesthetic FallingBlock ice shards — purely visual, removed on next tick
        spawnAestheticShards(impact, world, plugin);

        // Announce to nearby players
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (p.getLocation().distance(impact) < 60)
                world.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.4f);
        }

        // Heavy expanding shockwave
        new BukkitRunnable() {
            double r = 1.0;
            final Set<UUID> hit = new HashSet<>();
            @Override public void run() {
                if (r > 30) { cancel(); return; }

                ParticleUtils.ring(world, Particle.SNOWFLAKE,  impact.clone().add(0, 0.15, 0), r,      36);
                ParticleUtils.ring(world, Particle.SNOWFLAKE,  impact.clone().add(0, 0.5,  0), r * 0.85, 28);
                if ((int)(r) % 3 == 0)
                    ParticleUtils.ring(world, Particle.END_ROD, impact.clone().add(0, 0.8, 0), r * 0.7, 18);

                // Vertical shards at wave front
                for (int i = 0; i < 8; i++) {
                    double a = (Math.PI * 2 / 8) * i + r * 0.1;
                    for (double h = 0; h <= 2.5; h += 0.5) {
                        world.spawnParticle(Particle.SNOWFLAKE,
                            impact.clone().add(Math.cos(a) * r, h, Math.sin(a) * r),
                            1, 0.05, 0.05, 0.05, 0.02);
                    }
                }

                for (Entity e : world.getNearbyEntities(impact, r + 1.5, 5, r + 1.5)) {
                    if (!(e instanceof LivingEntity le) || e == player || !hit.add(e.getUniqueId())) continue;
                    double dist = e.getLocation().distance(impact);
                    if (dist > r + 1.8) continue;
                    le.damage(45.0, player);
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 4));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 160, 2));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));
                    // High knockback outward and upward
                    Vector kb = le.getLocation().toVector().subtract(impact.toVector())
                        .normalize().multiply(2.4).setY(1.4);
                    le.setVelocity(kb);
                }

                r += 0.75;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    // Spawn FallingBlock ice entities outward — they are killed on the next tick so they never land
    private static void spawnAestheticShards(Location impact, World world, MythicBladesPlugin plugin) {
        List<FallingBlock> shards = new ArrayList<>();
        Random rand = new Random();
        int count = 20;
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2 / count) * i;
            double dx = Math.cos(angle) * 0.3 + (rand.nextDouble() - 0.5) * 0.4;
            double dz = Math.sin(angle) * 0.3 + (rand.nextDouble() - 0.5) * 0.4;
            double dy = 0.6 + rand.nextDouble() * 0.8;
            FallingBlock fb = world.spawnFallingBlock(impact.clone().add(0, 0.5, 0),
                Material.ICE.createBlockData());
            fb.setVelocity(new Vector(dx * 1.6, dy, dz * 1.6));
            fb.setDropItem(false);
            shards.add(fb);
        }
        // Remove all shards 2 ticks later — purely aesthetic, never touch the ground
        new BukkitRunnable() {
            @Override public void run() {
                for (FallingBlock fb : shards) {
                    if (!fb.isDead()) fb.remove();
                }
            }
        }.runTaskLater(plugin, 2L);
    }
}
