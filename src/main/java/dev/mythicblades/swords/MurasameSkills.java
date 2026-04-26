package dev.mythicblades.swords;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.utils.ParticleUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MurasameSkills {

    private static final Map<UUID, Integer> curseStacks = new HashMap<>();
    private static final Set<UUID>          marked      = new HashSet<>();

    public static void applyMurasameCurse(LivingEntity target, Player attacker, MythicBladesPlugin plugin) {
        UUID id = target.getUniqueId();
        int maxStacks = plugin.getConfigManager().skillInt("murasame", "passive", "max_stacks", 5);
        int stacks = Math.min(curseStacks.getOrDefault(id, 0) + 1, maxStacks);
        curseStacks.put(id, stacks);
        marked.add(id);

        int witDur = plugin.getConfigManager().skillInt("murasame", "passive", "wither_duration", 80);
        int witAmp = plugin.getConfigManager().skillInt("murasame", "passive", "wither_amplifier", 1);
        int wkDur  = plugin.getConfigManager().skillInt("murasame", "passive", "weakness_duration", 60);

        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, witDur, witAmp));
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, wkDur, 0));

        ParticleUtils.spawn(target.getWorld(), Particle.DAMAGE_INDICATOR,
            target.getLocation().add(0, 1, 0), 2, 0.2, 0.3, 0.2, 0.05);

        if (target instanceof Player p) {
            p.sendActionBar(Component.text(getCurseBar(stacks)));
        }
    }

    private static String getCurseBar(int stacks) {
        return switch (stacks) {
            case 1 -> "§7☠ Curse I";
            case 2 -> "§c☠ Curse II";
            case 3 -> "§4☠☠ Curse III";
            case 4 -> "§5☠☠ Curse IV";
            default -> "§5§l☠☠ CURSE MAXIMUM ☠☠";
        };
    }

    public static void updateCurseHud(Player player) {
        int stacks = curseStacks.getOrDefault(player.getUniqueId(), 0);
        if (stacks <= 0) return;
        player.sendActionBar(Component.text(getCurseBar(stacks)));
    }

    public static void startHudTask(MythicBladesPlugin plugin) {
        // Every 8 ticks (was 5) — HUD updates don't need to be this frequent
        new BukkitRunnable() {
            @Override public void run() {
                for (Player p : plugin.getServer().getOnlinePlayers()) updateCurseHud(p);
            }
        }.runTaskTimer(plugin, 0L, 8L);
    }

    public static void lethalPoisonActive(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), "lethal_poison")) {
            player.sendMessage("§4Curse Mark: " + cd.getRemainingSeconds(player.getUniqueId(), "lethal_poison") + "s");
            return;
        }
        cd.set(player.getUniqueId(), "lethal_poison", plugin.getConfigManager().skillCooldownMs("murasame", "lethal_poison"));

        double baseDmg  = plugin.getConfigManager().skill("murasame", "lethal_poison", "damage", 10.0);
        double radius   = plugin.getConfigManager().skill("murasame", "lethal_poison", "radius", 6.0);
        int witDur      = plugin.getConfigManager().skillInt("murasame", "lethal_poison", "wither_duration", 120);
        int witAmp      = plugin.getConfigManager().skillInt("murasame", "lethal_poison", "wither_amplifier", 1);
        int slwDur      = plugin.getConfigManager().skillInt("murasame", "lethal_poison", "slowness_duration", 100);
        int slwAmp      = plugin.getConfigManager().skillInt("murasame", "lethal_poison", "slowness_amplifier", 2);
        int stacksApply = plugin.getConfigManager().skillInt("murasame", "lethal_poison", "curse_stacks_applied", 3);

        World world = player.getWorld();
        Location loc = player.getLocation().add(0, 1, 0);

        player.sendMessage("§4☠ CURSE MARK — Death begins.");
        world.playSound(loc, Sound.ENTITY_WITHER_AMBIENT, 1f, 0.6f);
        world.spawnParticle(Particle.SMOKE, loc, 8, 0.5, 0.5, 0.5, 0.02);

        for (Entity e : world.getNearbyEntities(loc, radius, radius, radius)) {
            if (!(e instanceof LivingEntity le) || e == player || le.isDead()) continue;
            UUID id = le.getUniqueId();
            int existingStacks = curseStacks.getOrDefault(id, 0);
            double detonationDmg = baseDmg + existingStacks * 4.0;
            le.damage(detonationDmg, player);
            curseStacks.put(id, stacksApply);
            marked.add(id);
            le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, witDur, witAmp));
            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slwDur, slwAmp));
            ParticleUtils.spawn(world, Particle.DAMAGE_INDICATOR, le.getLocation().add(0, 1, 0), 5, 0.4, 0.4, 0.4, 0.1);
            if (le instanceof Player p) p.sendMessage("§4☠ YOU ARE MARKED.");
        }
    }

    public static void berserkMode(Player player, MythicBladesPlugin plugin) {
        var cd = plugin.getCooldownManager();
        if (cd.isOnCooldown(player.getUniqueId(), "berserk_mode")) {
            player.sendMessage("§4Berserk: " + cd.getRemainingSeconds(player.getUniqueId(), "berserk_mode") + "s");
            return;
        }
        cd.set(player.getUniqueId(), "berserk_mode", plugin.getConfigManager().skillCooldownMs("murasame", "berserk_mode"));

        int durTicks  = plugin.getConfigManager().skillInt("murasame", "berserk_mode", "duration", 15) * 20;
        double aoeDmg = plugin.getConfigManager().skill("murasame", "berserk_mode", "aura_damage_base", 5.0);
        double aoeR   = plugin.getConfigManager().skill("murasame", "berserk_mode", "aura_radius", 4.0);
        int interval  = plugin.getConfigManager().skillInt("murasame", "berserk_mode", "aura_tick_interval", 15);
        int strAmp    = plugin.getConfigManager().skillInt("murasame", "berserk_mode", "strength_amplifier", 3);
        int spdAmp    = plugin.getConfigManager().skillInt("murasame", "berserk_mode", "speed_amplifier", 2);

        World world = player.getWorld();

        player.sendMessage("§4§l★ CURSE OVERFLOW — BERSERK");
        world.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 0.5f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, durTicks, strAmp));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durTicks, spdAmp));

        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (!player.isOnline() || tick >= durTicks) {
                    cancel();
                    berserkAftermath(player, plugin);
                    return;
                }
                Location loc = player.getLocation().add(0, 1, 0);
                // Particle every 8 ticks (was every 4)
                if (tick % 8 == 0)
                    world.spawnParticle(Particle.DAMAGE_INDICATOR, loc, 1, 0.4, 0.5, 0.4, 0.03);

                if (tick % interval == 0) {
                    for (Entity e : world.getNearbyEntities(player.getLocation(), aoeR, aoeR, aoeR)) {
                        if (!(e instanceof LivingEntity le) || e == player || le.isDead()) continue;
                        UUID id = le.getUniqueId();
                        int stacks = curseStacks.getOrDefault(id, 0) + 1;
                        curseStacks.put(id, stacks);
                        le.damage(aoeDmg + stacks, player);
                        le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1));
                        ParticleUtils.spawn(world, Particle.DAMAGE_INDICATOR,
                            le.getLocation().add(0, 1, 0), 3, 0.2, 0.3, 0.2, 0.05);
                    }
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static void berserkAftermath(Player player, MythicBladesPlugin plugin) {
        if (!player.isOnline()) return;
        int wkDur  = plugin.getConfigManager().skillInt("murasame", "berserk_mode", "aftermath_weakness_duration", 200);
        int slwDur = plugin.getConfigManager().skillInt("murasame", "berserk_mode", "aftermath_slowness_duration", 200);
        player.sendMessage("§4☠ The blade demands repayment.");
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, wkDur, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slwDur, 1));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.7f, 0.4f);
    }

    public static void clearCurse(UUID id) {
        curseStacks.remove(id);
        marked.remove(id);
    }
}
