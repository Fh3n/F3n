package dev.mythicblades.swords;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.utils.ParticleUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class EnmaSkills {

    private static final String SKILL_STRENGTH = "enma_strength";
    private static final String ULT_HAKAI      = "hakai_slash";

    // ── PASSIVE (clean cursed pressure) ─────────────────────────
    public static void applyEnmaPassive(LivingEntity target, Player attacker) {

        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1, false, true, true));

        // small delayed “curse echo” instead of instant spam
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t++ > 3 || target.isDead()) {
                    cancel();
                    return;
                }
                target.damage(1.0, attacker);
            }
        }.runTaskTimer(MythicBladesPlugin.getPlugin(MythicBladesPlugin.class), 10L, 10L);

        if (Math.random() < 0.4) {
            ParticleUtils.spawnEnmaHitEffect(target.getLocation());
        }
    }

    // ── ENMA STRENGTH BURST ─────────────────────────
    public static void enmaStrengthBurst(Player player, MythicBladesPlugin plugin) {

        var cd = plugin.getCooldownManager();

        if (cd.isOnCooldown(player.getUniqueId(), SKILL_STRENGTH)) {
            player.sendMessage("§6Enma Strength: " +
                    cd.getRemainingSeconds(player.getUniqueId(), SKILL_STRENGTH) + "s");
            return;
        }

        cd.set(player.getUniqueId(), SKILL_STRENGTH, 30000);

        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 2000, 4));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 300, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));

        World world = player.getWorld();
        Location loc = player.getLocation().add(0, 1, 0);

        world.playSound(loc, Sound.ENTITY_WITHER_AMBIENT, 1f, 0.6f);

        world.spawnParticle(Particle.FLAME, loc, 6, 0.3, 0.5, 0.3, 0.02);

        player.sendMessage("§6Enma devours your limits. §cStrength V active.");
    }

    // ── INFO ─────────────────────────
    public static void drainInfo(Player player, MythicBladesPlugin plugin) {
        player.sendMessage("§6Enma drains life force. Cursed pressure builds over time.");
    }

    // ── HAKAI SLASH (UPGRADED ANIME CUT + FRACTURE AFTERIMAGE) ─────────────────────────
    public static void hakaiSlash(Player player, MythicBladesPlugin plugin) {

        var cd = plugin.getCooldownManager();

        if (cd.isOnCooldown(player.getUniqueId(), ULT_HAKAI)) {
            player.sendMessage("§cHakai Slash: " +
                    cd.getRemainingSeconds(player.getUniqueId(), ULT_HAKAI) + "s");
            return;
        }

        cd.set(player.getUniqueId(), ULT_HAKAI,
                plugin.getConfigManager().getCooldownMs("enma", "hakai_slash"));

        Vector dir = player.getLocation().getDirection().normalize();
        Location start = player.getLocation().add(0, 1, 0);
        World world = player.getWorld();

        double dmg = plugin.getConfigManager().getDamage("enma", "hakai_slash");

        double range = plugin.getConfigManager().getDouble(
                "swords.enma.hakai_slash.range_blocks", 45.0);

        double width = plugin.getConfigManager().getDouble(
                "swords.enma.hakai_slash.width_blocks", 5.0);

        player.sendMessage("§c§l★ HAKAI SLASH — The world fractures.");
        world.playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1f, 0.4f);

        int steps = (int)(range * 1.3);

        new BukkitRunnable() {

            int step = 0;
            Location cur = start.clone();

            @Override
            public void run() {

                if (step >= steps || step > 100) {

                    // ── FRACTURE AFTERIMAGE (anime delay effect) ──
                    world.playSound(cur, Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 0.6f);

                    new BukkitRunnable() {
                        double r = 1;

                        @Override
                        public void run() {
                            if (r > 6) {
                                cancel();
                                return;
                            }

                            world.spawnParticle(Particle.SMOKE, cur, (int)(10 / r), r, r, r, 0.01);
                            r += 1.2;
                        }
                    }.runTaskTimer(plugin, 0L, 5L);

                    cancel();
                    return;
                }

                cur.add(dir.clone().multiply(0.8));

                Vector perp = dir.clone().crossProduct(new Vector(0, 1, 0));
                if (perp.lengthSquared() == 0) perp = new Vector(1, 0, 0);
                perp.normalize();

                // ── MAIN SLASH ──
                if (step % 2 == 0) {
                    ParticleUtils.spawnHakaiSlashParticles(cur.clone(), plugin);
                }

                // ── LEFT / RIGHT ECHO SLASH (anime air cuts) ──
                if (step % 4 == 0) {
                    Location left = cur.clone().add(perp.clone().multiply(-width * 0.6));
                    Location right = cur.clone().add(perp.clone().multiply(width * 0.6));

                    world.spawnParticle(Particle.SMOKE, left, 1, 0, 0, 0, 0.01);
                    world.spawnParticle(Particle.SMOKE, right, 1, 0, 0, 0, 0.01);
                }

                // ── DAMAGE HIT ──
                if (step % 2 == 0) {
                    for (Entity e : world.getNearbyEntities(cur, width / 2, 2.5, width / 2)) {

                        if (e == player || !(e instanceof LivingEntity le)) continue;

                        le.damage(dmg, player);
                        le.setFireTicks(120);

                        le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1));
                        le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 2));

                        world.spawnParticle(Particle.SMOKE, le.getLocation().add(0, 1, 0), 1);
                    }
                }

                if (step % 6 == 0) {
                    world.playSound(cur, Sound.ENTITY_BLAZE_HURT, 0.15f, 0.6f);
                }

                step++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}