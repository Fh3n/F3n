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

    public static void applyWaterPassive(LivingEntity target, Player attacker, MythicBladesPlugin plugin) {
        double bonus = plugin.getConfigManager().skill("ame_no_habakiri", "passive", "bonus_damage", 3.0);
        int wkDur    = plugin.getConfigManager().skillInt("ame_no_habakiri", "passive", "weakness_duration", 40);
        target.damage(bonus, attacker);
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, wkDur, 0));
        ParticleUtils.spawn(target.getWorld(), Particle.END_ROD,
            target.getLocation().add(0, 1, 0), 4, 0.2, 0.2, 0.2, 0.02);
    }

    public static void godSlayerInfo(Player player, MythicBladesPlugin plugin) {
        double mult = plugin.getConfigManager().swordVal("ame_no_habakiri", "god_slayer.multiplier", 3.5);
        player.sendMessage("§bGod-Slayer (Passive): §f" + mult + "x §bdamage vs Ender Dragon, Wither, Elder Guardian, Warden.");
    }

    public static void heavenlyParry(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), "heavenly_parry")) {
            player.sendMessage("§fHeavenly Parry: " + cd.getRemainingSeconds(player.getUniqueId(), "heavenly_parry") + "s");
            return;
        }
        cd.set(player.getUniqueId(), "heavenly_parry", plugin.getConfigManager().skillCooldownMs("ame_no_habakiri", "heavenly_parry"));

        double dmg     = plugin.getConfigManager().skill("ame_no_habakiri", "heavenly_parry", "damage", 20.0);
        double r       = plugin.getConfigManager().skill("ame_no_habakiri", "heavenly_parry", "radius", 6.0);
        double launchY = plugin.getConfigManager().skill("ame_no_habakiri", "heavenly_parry", "launch_velocity_y", 1.5);
        double kbY     = plugin.getConfigManager().skill("ame_no_habakiri", "heavenly_parry", "knockback_y", 0.5);

        World world = player.getWorld();

        player.sendMessage("§f✦ Heavenly Parry!");
        world.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 2f);

        player.setInvulnerable(true);
        player.setVelocity(new Vector(0, launchY, 0));
        ParticleUtils.ring(world, Particle.END_ROD, player.getLocation().add(0, 0.5, 0), r * 0.5, 16);

        new BukkitRunnable() {
            int wait = 0;
            @Override public void run() {
                if (!player.isOnline()) { player.setInvulnerable(false); cancel(); return; }
                wait++;
                if (wait < 5) return;
                if (player.isOnGround()) {
                    player.setInvulnerable(false);
                    Location landing = player.getLocation().add(0, 0.5, 0);
                    world.playSound(landing, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.5f);
                    world.spawnParticle(Particle.SWEEP_ATTACK, landing, 15, 1, 0.5, 1, 0.1);
                    ParticleUtils.ring(world, Particle.END_ROD, landing, r, 24);
                    world.spawnParticle(Particle.ENCHANT, landing, 20, r * 0.4, 0.3, r * 0.4, 0.5);
                    for (Entity e : world.getNearbyEntities(landing, r, 2, r)) {
                        if (!(e instanceof LivingEntity le) || e == player || le.isDead()) continue;
                        le.damage(dmg, player);
                        le.setVelocity(new Vector(0, kbY, 0));
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public static void divineSeverance(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), "divine_severance")) {
            player.sendMessage("§fDivine Severance: " + cd.getRemainingSeconds(player.getUniqueId(), "divine_severance") + "s");
            return;
        }
        cd.set(player.getUniqueId(), "divine_severance", plugin.getConfigManager().skillCooldownMs("ame_no_habakiri", "divine_severance"));

        double dmg    = plugin.getConfigManager().skill("ame_no_habakiri", "divine_severance", "damage", 50.0);
        double range  = plugin.getConfigManager().skill("ame_no_habakiri", "divine_severance", "range", 30.0);
        double stepSz = plugin.getConfigManager().skill("ame_no_habakiri", "divine_severance", "step_size", 1.5);
        double hbox   = plugin.getConfigManager().skill("ame_no_habakiri", "divine_severance", "hitbox", 3.0);
        double kbY    = plugin.getConfigManager().skill("ame_no_habakiri", "divine_severance", "knockback_y", 0.5);
        int strDur    = plugin.getConfigManager().skillInt("ame_no_habakiri", "divine_severance", "strength_duration", 40);

        World world = player.getWorld();
        Location start = player.getLocation().add(0, 1, 0);
        Vector dir = start.getDirection().normalize();

        player.sendMessage("§f§l★ DIVINE SEVERANCE");
        world.playSound(start, Sound.BLOCK_BEACON_POWER_SELECT, 1f, 2f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, strDur,
            plugin.getConfigManager().skillInt("ame_no_habakiri", "divine_severance", "strength_amplifier", 1)));

        Set<UUID> hit = new HashSet<>();

        new BukkitRunnable() {
            double step = 0;
            Location cur = start.clone();
            @Override public void run() {
                if (!player.isOnline() || step > range) {
                    world.strikeLightningEffect(cur);
                    world.playSound(cur, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.2f);
                    cancel();
                    return;
                }
                cur.add(dir.clone().multiply(stepSz));
                if (step % 2 == 0) {
                    world.spawnParticle(Particle.SWEEP_ATTACK, cur, 5, 1, 0.5, 1, 0.05);
                    world.spawnParticle(Particle.END_ROD, cur, 3, 0.3, 0.3, 0.3, 0.03);
                }
                for (Entity e : world.getNearbyEntities(cur, hbox, 2, hbox)) {
                    if (!(e instanceof LivingEntity le) || e == player || le.isDead() || !hit.add(e.getUniqueId())) continue;
                    le.damage(dmg, player);
                    le.setVelocity(new Vector(0, kbY, 0));
                }
                step++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
