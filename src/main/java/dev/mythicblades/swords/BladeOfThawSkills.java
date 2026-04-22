package dev.mythicblades.swords;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.utils.ParticleUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BladeOfThawSkills {

    private static final Map<UUID, Boolean> resurrectionReady = new HashMap<>();
    private static final Map<UUID, Boolean> sentinelsActive   = new HashMap<>();
    private static final Map<UUID, Long>    meltdownEnd       = new HashMap<>();

    // ── Passive ──────────────────────────────────────────────────────────────
    public static void applyFrostPassive(LivingEntity target, Player owner, MythicBladesPlugin plugin) {
        int amp = plugin.getConfigManager().skillInt("blade_of_thaw", "passive", "slow_amplifier", 1);
        int dur = plugin.getConfigManager().skillInt("blade_of_thaw", "passive", "slow_duration", 40);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, dur, amp));
        // Sentinel counter-strike on hit
        if (areSentinelsActive(owner.getUniqueId())) {
            double dmg = plugin.getConfigManager().skill("blade_of_thaw", "sentinels", "counter_damage", 15.0);
            target.damage(dmg, owner);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3));
            ParticleUtils.spawn(target.getWorld(), Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0),
                15, 0.4, 0.4, 0.4, 0.08);
        }
    }

    // ── Sentinels (F-key toggle) ──────────────────────────────────────────────
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
    // Ice pillar eruption under the player, shockwave + freeze ring outward
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
        double radius  = plugin.getConfigManager().skill("blade_of_thaw", "glacial_monolith", "radius", 8.0);
        double dmg     = plugin.getConfigManager().skill("blade_of_thaw", "glacial_monolith", "damage", 45.0);

        player.sendMessage("§b❄ Glacial Monolith!");
        world.playSound(base, Sound.BLOCK_GLASS_BREAK, 1f, 0.5f);
        world.playSound(base, Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.6f);

        // Pillar eruption — rising ice particles
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

        // Shockwave ring expanding outward
        new BukkitRunnable() {
            double r = 1;
            @Override public void run() {
                if (r > radius + 2) { cancel(); return; }
                ParticleUtils.ring(world, Particle.SNOWFLAKE, base.clone().add(0, 0.1, 0), r, 24);
                // Damage entities caught in ring
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

        // Self-buff
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80,
            plugin.getConfigManager().skillInt("blade_of_thaw", "glacial_monolith", "resistance_amplifier", 3)));
    }

    // ── Absolute Zero (Shift+RMB) — arm resurrection ──────────────────────────
    public static void armResurrection(UUID uuid) {
        resurrectionReady.put(uuid, true);
    }

    public static boolean isResurrectionReady(UUID uuid) {
        return resurrectionReady.getOrDefault(uuid, false);
    }

    // Called when Blade of Thaw owner would die — cancel death, trigger meltdown
    public static void triggerResurrection(Player player, MythicBladesPlugin plugin) {
        if (!isResurrectionReady(player.getUniqueId())) return;
        resurrectionReady.put(player.getUniqueId(), false);

        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        player.setFoodLevel(20);

        World world = player.getWorld();
        Location loc = player.getLocation();

        world.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1f, 0.6f);
        world.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, 1f, 2f);

        // Broadcast
        String msg = plugin.getConfigManager().getMessage("absolute_zero_broadcast", "{player}", player.getName());
        plugin.getServer().getOnlinePlayers().forEach(p -> {
            if (!p.equals(player)) p.sendMessage(msg);
        });
        player.sendMessage("§b❄§f Absolute Zero — §bResurrection triggered. §7Meltdown begins.");

        // Meltdown phase: massive AOE for duration
        int meltTicks = plugin.getConfigManager().skillInt("blade_of_thaw", "absolute_zero", "meltdown_duration", 15) * 20;
        meltdownEnd.put(player.getUniqueId(), System.currentTimeMillis() + meltTicks * 50L);

        // Give invuln briefly
        player.setInvulnerable(true);

        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (!player.isOnline() || t >= meltTicks) {
                    player.setInvulnerable(false);
                    meltdownEnd.remove(player.getUniqueId());
                    cancel();
                    return;
                }
                // AOE damage pulse every 20 ticks
                if (t % 20 == 0) {
                    for (Entity e : world.getNearbyEntities(player.getLocation(), 12, 6, 12)) {
                        if (!(e instanceof LivingEntity le) || e == player) continue;
                        le.damage(8.0, player);
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3));
                    }
                    // Expanding freeze ring
                    double r = 4 + (t / 20.0) * 2;
                    ParticleUtils.ring(world, Particle.SNOWFLAKE, player.getLocation(), r, 32);
                    world.spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 6, 0.5, 0.5, 0.5, 0.1);
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
