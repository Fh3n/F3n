package dev.mythicblades.swords;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BladeOfThawSkills {

    private static final Map<UUID, Boolean> resurrectionReady = new HashMap<>();
    private static final Map<UUID, Boolean> sentinelsActive = new HashMap<>();

    // ───────────── SENTINELS ─────────────
    public static void toggleSentinels(Player player) {
        sentinelsActive.put(player.getUniqueId(), !sentinelsActive.getOrDefault(player.getUniqueId(), false));
        player.sendMessage("Sentinels toggled!");
    }

    public static void tickSentinels(Player player) {
        // Placeholder logic
        player.sendMessage("Ticking sentinels...");
    }

    public static boolean areSentinelsActive(UUID uuid) {
        return sentinelsActive.getOrDefault(uuid, false);
    }

    // ───────────── GLACIAL MONOLITH ─────────────
    public static void glacialMonolith(Player player) {
        World world = player.getWorld();
        Location base = player.getLocation();

        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, 4));

        for (int y = 0; y <= 3; y++) {
            Location loc = base.clone().add(0, y, 0);
            world.spawnParticle(Particle.BLOCK, loc, 10, 0.2, 0.2, 0.2, Material.ICE.createBlockData());
        }

        for (int angle = 0; angle < 360; angle += 30) {
            Vector dir = new Vector(Math.cos(Math.toRadians(angle)), 0, Math.sin(Math.toRadians(angle)));
            Location spikeLoc = base.clone().add(dir.multiply(10));
            world.spawnParticle(Particle.BLOCK, spikeLoc, 5, 0.3, 0.3, 0.3, Material.ICE.createBlockData());
        }

        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("MythicBlades"), () -> {
            for (double r = 1; r <= 20; r += 2) {
                for (double a = 0; a < 360; a += 20) {
                    double rad = Math.toRadians(a);
                    Location loc = base.clone().add(Math.cos(rad) * r, 0, Math.sin(rad) * r);
                    world.spawnParticle(Particle.BLOCK, loc, 3, 0.3, 0.3, 0.3, Material.ICE.createBlockData());
                    world.spawnParticle(Particle.BLOCK, loc, 3, 0.3, 0.3, 0.3, Material.DIRT.createBlockData());
                }
            }
        }, 60L);
    }

    // ───────────── FROST PASSIVE ─────────────
    public static void applyFrostPassive(LivingEntity target, Player owner) {
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
    }

    // ───────────── RESURRECTION ─────────────
    public static boolean isResurrectionReady(UUID uuid) {
        return resurrectionReady.getOrDefault(uuid, false);
    }

    public static void triggerResurrection(Player player) {
        if (!isResurrectionReady(player.getUniqueId())) return;
        resurrectionReady.put(player.getUniqueId(), false);
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 1f);
    }
}