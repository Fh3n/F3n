package dev.mythicblades.listeners; // Fixed package to match listener folder

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

public class SwordSkillListener { // Fixed class name to match file

    private static final Map<UUID, Boolean> resurrectionReady = new HashMap<>();
    private static final Map<UUID, Boolean> sentinelsActive = new HashMap<>();

    public static void toggleSentinels(Player player) {
        sentinelsActive.put(player.getUniqueId(), !sentinelsActive.getOrDefault(player.getUniqueId(), false));
        player.sendMessage("Sentinels toggled!");
    }

    public static void tickSentinels(Player player) {
        // Placeholder for sentinels logic
    }

    public static boolean areSentinelsActive(UUID uuid) {
        return sentinelsActive.getOrDefault(uuid, false);
    }

    public static void glacialMonolith(Player player) {
        World world = player.getWorld();
        Location base = player.getLocation();

        // Updated to RESISTANCE
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 4));

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

    public static void applyFrostPassive(LivingEntity target, Player owner) {
        // Updated to SLOWNESS
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
    }

    public static void triggerResurrection(Player player) {
        if (!resurrectionReady.getOrDefault(player.getUniqueId(), false)) return;
        resurrectionReady.put(player.getUniqueId(), false);
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 1f);
    }
}