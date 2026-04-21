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

public class HabakiriSkills {

    private static final String SKILL_PARRY = "heavenly_parry";
    private static final String ULT_SEVERANCE = "divine_severance";

    // ── Passive ─────────────────────────
    public static void applyWaterPassive(LivingEntity target, Player attacker) {
        target.damage(3.0, attacker);
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0));
        ParticleUtils.spawnWaterHitEffect(target.getLocation());
    }

    public static void godSlayerInfo(Player player, MythicBladesPlugin plugin) {
        double mult = plugin.getConfigManager()
                .getDouble("swords.ame_no_habakiri.god_slayer_multiplier", 3.5);
        player.sendMessage("§bGod-Slayer (Passive): " + mult +
                "x damage vs Ender Dragon, Wither, Elder Guardian, Warden.");
    }

    // ── Heavenly Parry (Hammer-style) ─────────────────────────
    public static void heavenlyParry(Player player, MythicBladesPlugin plugin) {
        if (!player.isOnline()) return;

        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), SKILL_PARRY)) {
            player.sendMessage("§fHeavenly Parry: " +
                    cd.getRemainingSeconds(player.getUniqueId(), SKILL_PARRY) + "s");
            return;
        }
        cd.set(player.getUniqueId(), SKILL_PARRY,
                plugin.getConfigManager().getCooldownMs("ame_no_habakiri", "heavenly_parry"));

        World world = player.getWorld();
        double dmg = plugin.getConfigManager().getDamage("ame_no_habakiri", "heavenly_parry");
        double radius = 6.0;

        player.sendMessage("§f✦ Heavenly Parry!");
        world.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 2f);

        // Jump straight up
        player.setVelocity(new Vector(0, 1.5, 0));

        // Runnable to detect landing
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                if (player.isOnGround()) {
                    Location landing = player.getLocation().add(0, 0.5, 0);
                    world.playSound(landing, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.5f);
                    world.spawnParticle(Particle.SWEEP_ATTACK, landing, 15, 1, 0.5, 1, 0.1);

                    for (Entity e : world.getNearbyEntities(landing, radius, 2, radius)) {
                        if (e instanceof LivingEntity le && le != player) {
                            le.damage(dmg, player);
                            le.setVelocity(new Vector(0, 0.5, 0));
                        }
                    }
                    cancel(); // Stop runnable after landing effect
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ── Divine Severance ─────────────────────────
    public static void divineSeverance(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), ULT_SEVERANCE)) {
            player.sendMessage("§fDivine Severance: " +
                    cd.getRemainingSeconds(player.getUniqueId(), ULT_SEVERANCE) + "s");
            return;
        }
        cd.set(player.getUniqueId(), ULT_SEVERANCE,
                plugin.getConfigManager().getCooldownMs("ame_no_habakiri", "divine_severance"));

        World world = player.getWorld();
        Location start = player.getLocation().add(0, 1, 0);
        Vector dir = start.getDirection().normalize();
        double range = 30;
        double dmg = plugin.getConfigManager().getDamage("ame_no_habakiri", "divine_severance");

        player.sendMessage("§f§l★ DIVINE SEVERANCE");
        world.playSound(start, Sound.BLOCK_BEACON_POWER_SELECT, 1f, 2f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 40, 1));

        Set<UUID> hit = new HashSet<>();

        new BukkitRunnable() {
            double step = 0;
            Location cur = start.clone();

            @Override
            public void run() {
                if (!player.isOnline() || step > range) {
                    cancel();
                    return;
                }

                cur.add(dir.clone().multiply(1.5));
                if (step % 2 == 0) {
                    world.spawnParticle(Particle.SWEEP_ATTACK, cur, 5, 1, 0.5, 1, 0.05);
                }

                for (Entity e : world.getNearbyEntities(cur, 3, 2, 3)) {
                    if (e instanceof LivingEntity le && e != player && hit.add(e.getUniqueId())) {
                        le.damage(dmg, player);
                        le.setVelocity(new Vector(0, 0.5, 0));
                    }
                }

                step++;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        // Final strike effect
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                world.strikeLightningEffect(start.clone().add(dir.clone().multiply(range)));
                world.playSound(start, Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.5f);
            }
        }.runTaskLater(plugin, 25L);
    }
}