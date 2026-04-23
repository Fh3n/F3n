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
    private static final Map<UUID, Long>    meltdownEnd       = new HashMap<>();

    // ── Passive ──────────────────────────────────────────────────────────────
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

    public static boolean isResurrectionReady(UUID uuid) {
        return resurrectionReady.getOrDefault(uuid, false);
    }

    // ── Absolute Zero — trigger ───────────────────────────────────────────────
    // Phase 1 (0-60t)  : 30-block radius ice spikes erupt from the ground, staggered outward, damage enemies
    // Phase 2 (0-100t) : player lifted 15 blocks into spinning ice cocoon, healed, invulnerable
    // Phase 3 (100t)   : cocoon starts falling — spikes retract simultaneously, ice blocks removed
    // Phase 4 (landing): massive shockwave from impact point, damages + launches all in 30-block radius
    public static void triggerResurrection(Player player, MythicBladesPlugin plugin) {
        if (!isResurrectionReady(player.getUniqueId())) return;
        resurrectionReady.put(player.getUniqueId(), false);

        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.setInvulnerable(true);

        World world = player.getWorld();
        Location deathLoc = player.getLocation().clone();

        // Global announce — everyone sees this
        world.playSound(deathLoc, Sound.ENTITY_WITHER_SPAWN, 2f, 0.4f);
        world.playSound(deathLoc, Sound.BLOCK_BEACON_POWER_SELECT, 2f, 0.5f);
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            world.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 0.5f);
            if (!p.equals(player))
                p.sendMessage(plugin.getConfigManager().getMessage("absolute_zero_broadcast", "{player}", player.getName()));
        }
        player.sendMessage("§b§l❄ ABSOLUTE ZERO — RISING.");

        List<Block> placedIce = Collections.synchronizedList(new ArrayList<>());

        // ── Phase 1: spikes erupt in staggered rings out to 30 blocks ─────────
        // Each ring erupts 3 ticks after the previous to create a ripple effect
        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (t > 60) { cancel(); return; }

                // 6 rings staggered: innermost first, outermost last
                // ring 0 = r6, ring 1 = r10, ring 2 = r14, ring 3 = r18, ring 4 = r24, ring 5 = r30
                int[] ringRadii   = {6, 10, 14, 18, 24, 30};
                int[] ringDelays  = {0,  4,  8, 13, 19, 26}; // tick each ring starts erupting
                int[] ringSpikeH  = {6,  7,  8,  9, 10, 12}; // max spike height per ring

                for (int ri = 0; ri < ringRadii.length; ri++) {
                    int ringStart = ringDelays[ri];
                    if (t < ringStart) continue;

                    int localT  = t - ringStart;
                    double br   = ringRadii[ri];
                    int maxH    = ringSpikeH[ri];
                    int spikeCount = 6 + ri * 3; // more spikes on outer rings

                    // Spike height grows over 20 ticks then holds
                    double currentH = Math.min(maxH, localT * 0.7);

                    for (int i = 0; i < spikeCount; i++) {
                        double a = (Math.PI * 2 / spikeCount) * i;
                        // Alternate spikes are taller for a jagged crown look
                        double thisH = (i % 2 == 0) ? currentH : currentH * 0.65;

                        for (double h = 0; h <= thisH; h += 0.45) {
                            // Taper the spike width as it rises
                            double taperR = 0.5 * (1.0 - h / (maxH + 1));
                            Location pt = deathLoc.clone().add(
                                Math.cos(a) * (br + taperR), h, Math.sin(a) * (br + taperR));
                            world.spawnParticle(Particle.BLOCK, pt, 1, taperR * 0.3, 0.05, taperR * 0.3,
                                Material.ICE.createBlockData());
                            if (h % 1.0 < 0.5)
                                world.spawnParticle(Particle.SNOWFLAKE, pt, 1, 0, 0, 0, 0.02);
                        }

                        // Place real ice blocks during growth phase
                        if (localT < 20 && localT % 3 == 0) {
                            for (int h = 1; h <= (int) currentH; h++) {
                                Block b = deathLoc.clone()
                                    .add(Math.cos(a) * br, h, Math.sin(a) * br).getBlock();
                                if (b.getType() == Material.AIR) {
                                    b.setType(Material.ICE);
                                    placedIce.add(b);
                                }
                            }
                        }
                    }
                }

                // Ground frost shockwave visual (expands early)
                if (t < 30) {
                    double groundR = t * 1.0;
                    for (int i = 0; i < 32; i++) {
                        double a = (Math.PI * 2 / 32) * i + t * 0.05;
                        world.spawnParticle(Particle.SNOWFLAKE,
                            deathLoc.clone().add(Math.cos(a) * groundR, 0.1, Math.sin(a) * groundR),
                            1, 0, 0, 0, 0.01);
                    }
                }

                // Damage enemies — checked every 8 ticks, radius grows with eruption
                if (t % 8 == 0) {
                    double damageR = Math.min(30, 6 + t * 0.5);
                    for (Entity e : world.getNearbyEntities(deathLoc, damageR, 12, damageR)) {
                        if (!(e instanceof LivingEntity le) || e == player) continue;
                        le.damage(14.0, player);
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 3));
                        le.setVelocity(le.getLocation().toVector()
                            .subtract(deathLoc.toVector()).normalize().multiply(0.6).setY(0.4));
                    }
                }

                if (t % 6 == 0)
                    world.playSound(deathLoc, Sound.BLOCK_GLASS_BREAK, 1f, 0.3f + t * 0.012f);
                if (t % 15 == 0)
                    world.playSound(deathLoc, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.4f);

                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // ── Phase 2: lift 15 blocks + spinning cocoon + heal ──────────────────
        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (!player.isOnline()) { cancel(); return; }

                // Lift forcefully for first 35 ticks to reach ~15 blocks
                if (t < 35) {
                    double liftForce = 0.5 - t * 0.008; // strong launch, tapering off
                    player.setVelocity(new Vector(0, Math.max(0.1, liftForce), 0));
                }
                // Hold roughly in place from tick 35-100
                if (t >= 35 && t < 100) {
                    Vector vel = player.getVelocity();
                    // dampen vertical drift gently
                    player.setVelocity(new Vector(vel.getX() * 0.3, vel.getY() * 0.15, vel.getZ() * 0.3));
                }

                Location pl = player.getLocation().add(0, 1, 0);

                // Outer rotating arms (4 arms, longer)
                for (int arm = 0; arm < 4; arm++) {
                    double armAngle = t * 0.22 + arm * (Math.PI / 2);
                    for (double d = 0.5; d <= 2.8; d += 0.4) {
                        Location ap = pl.clone().add(Math.cos(armAngle) * d, 0, Math.sin(armAngle) * d);
                        world.spawnParticle(Particle.SNOWFLAKE, ap, 1, 0, 0, 0, 0);
                    }
                }

                // Inner cocoon rings — 6 rings wrapping the player
                for (int ring = 0; ring < 6; ring++) {
                    double ry    = ring * 0.5 - 1.25;
                    double rr    = 1.5 - Math.abs(ry) * 0.22;
                    double aOff  = t * 0.20 + ring * (Math.PI / 6);
                    int pts = 12;
                    for (int i = 0; i < pts; i++) {
                        double a = (Math.PI * 2 / pts) * i + aOff;
                        Location cp = pl.clone().add(Math.cos(a) * rr, ry, Math.sin(a) * rr);
                        world.spawnParticle(Particle.SNOWFLAKE, cp, 1, 0, 0, 0, 0);
                        if (t % 3 == ring % 3)
                            world.spawnParticle(Particle.END_ROD, cp, 1, 0, 0, 0, 0);
                    }
                }

                // Inner glow pulse
                if (t % 5 == 0)
                    world.spawnParticle(Particle.END_ROD, pl, 3, 0.2, 0.35, 0.2, 0.02);

                // Heal
                double maxHp  = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                double current = player.getHealth();
                if (current < maxHp) player.setHealth(Math.min(current + 0.5, maxHp));

                if (t % 10 == 0)
                    world.playSound(pl, Sound.BLOCK_GLASS_BREAK, 0.35f, 1.9f);
                if (t == 50)
                    world.playSound(pl, Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.8f);

                // At tick 100 — begin drop, signal spikes to retract
                if (t >= 100) {
                    cancel();
                    retractAndDrop(player, deathLoc, world, placedIce, plugin);
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ── Phase 3: cocoon falls, spikes retract simultaneously ─────────────────
    private static void retractAndDrop(Player player, Location deathLoc, World world,
                                        List<Block> placedIce, MythicBladesPlugin plugin) {
        world.playSound(deathLoc, Sound.BLOCK_GLASS_BREAK, 2f, 0.25f);
        world.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1.5f);
        player.sendMessage("§b❄§f Absolute Zero — §bDescending.");

        // Drop the player now
        player.setVelocity(new Vector(0, -2.2, 0));

        // Spikes retract — outer rings first, inward (reverse of eruption)
        int[] ringRadii  = {30, 24, 18, 14, 10, 6};
        int[] ringDelays = { 0,  3,  6,  9, 13, 17}; // staggered inward retraction

        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (t > 35) {
                    // All ice removed
                    for (Block b : placedIce)
                        if (b.getType() == Material.ICE) b.setType(Material.AIR);
                    placedIce.clear();
                    cancel();
                    return;
                }

                for (int ri = 0; ri < ringRadii.length; ri++) {
                    if (t < ringDelays[ri]) continue;
                    int localT = t - ringDelays[ri];
                    double br  = ringRadii[ri];
                    // Height shrinks from max back to 0
                    double currentH = Math.max(0, 12 - localT * 0.7);
                    int spikeCount  = 6 + (ringRadii.length - 1 - ri) * 3;

                    for (int i = 0; i < spikeCount; i++) {
                        double a    = (Math.PI * 2 / spikeCount) * i;
                        double thisH = (i % 2 == 0) ? currentH : currentH * 0.65;
                        for (double h = 0; h <= thisH; h += 0.6) {
                            world.spawnParticle(Particle.SNOWFLAKE,
                                deathLoc.clone().add(Math.cos(a) * br, h, Math.sin(a) * br),
                                1, 0.05, 0.05, 0.05, 0.03);
                        }
                    }
                }

                if (t % 5 == 0)
                    world.playSound(deathLoc, Sound.BLOCK_GLASS_BREAK, 0.8f, 1.0f + t * 0.03f);

                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // Wait for player to land
        waitForLanding(player, world, plugin);
    }

    // ── Wait for landing ──────────────────────────────────────────────────────
    private static void waitForLanding(Player player, World world, MythicBladesPlugin plugin) {
        new BukkitRunnable() {
            int wait = 0;
            @Override public void run() {
                if (!player.isOnline()) { player.setInvulnerable(false); cancel(); return; }
                wait++;
                if (wait < 4) return;
                if (player.isOnGround() || wait > 80) {
                    player.setInvulnerable(false);
                    cancel();
                    impactShockwave(player, world, plugin);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ── Phase 4: impact shockwave — 30 block radius ───────────────────────────
    private static void impactShockwave(Player player, World world, MythicBladesPlugin plugin) {
        Location impact = player.getLocation();

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

        // Announce impact to everyone nearby
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (p.getLocation().distance(impact) < 60)
                world.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.4f);
        }

        new BukkitRunnable() {
            double r = 1.0;
            final Set<UUID> hit = new HashSet<>();
            @Override public void run() {
                if (r > 30) { cancel(); return; }

                // Double ring — a main ring and a trailing inner ring
                ParticleUtils.ring(world, Particle.SNOWFLAKE,  impact.clone().add(0, 0.15, 0), r,      36);
                ParticleUtils.ring(world, Particle.SNOWFLAKE,  impact.clone().add(0, 0.5,  0), r * 0.85, 28);
                if ((int)(r) % 3 == 0)
                    ParticleUtils.ring(world, Particle.END_ROD, impact.clone().add(0, 0.8, 0), r * 0.7, 18);

                // Vertical shards at the wave front
                for (int i = 0; i < 8; i++) {
                    double a = (Math.PI * 2 / 8) * i + r * 0.1;
                    for (double h = 0; h <= 2.5; h += 0.5) {
                        world.spawnParticle(Particle.SNOWFLAKE,
                            impact.clone().add(Math.cos(a) * r, h, Math.sin(a) * r),
                            1, 0.05, 0.05, 0.05, 0.02);
                    }
                }

                // Hit detection on the advancing ring band
                for (Entity e : world.getNearbyEntities(impact, r + 1.5, 4, r + 1.5)) {
                    if (!(e instanceof LivingEntity le) || e == player || !hit.add(e.getUniqueId())) continue;
                    double dist = e.getLocation().distance(impact);
                    if (dist > r + 1.8) continue;
                    le.damage(35.0, player);
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 160, 4));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 120, 2));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
                    Vector kb = le.getLocation().toVector().subtract(impact.toVector())
                        .normalize().multiply(1.8).setY(1.1);
                    le.setVelocity(kb);
                }

                r += 0.75;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
