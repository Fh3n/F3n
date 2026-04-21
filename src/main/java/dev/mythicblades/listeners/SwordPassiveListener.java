package dev.mythicblades.listeners;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.SwordType;
import dev.mythicblades.swords.*;
import dev.mythicblades.utils.ParticleUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class SwordPassiveListener implements Listener {

    private final MythicBladesPlugin plugin;

    public SwordPassiveListener(MythicBladesPlugin plugin) {
        this.plugin = plugin;
        startAuraTicker();
    }

    private void startAuraTicker() {
        new BukkitRunnable() {
            @Override public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    SwordType type = plugin.getSwordManager().getSwordType(item);
                    if (type == null) continue;

                    switch (type) {
                        case BLADE_OF_THAW -> {
                            ParticleUtils.spawnFrostAura(player, plugin);
                            BladeOfThawSkills.tickSentinels(player, plugin);
                        }
                        case EXCALIBUR       -> ParticleUtils.spawnHolyAura(player, plugin);
                        case EA              -> ParticleUtils.spawnVoidAura(player, plugin);
                        case MURASAME        -> ParticleUtils.spawnBloodAura(player, plugin);
                        case ENMA -> {
                            ParticleUtils.spawnEnmaAura(player, plugin);
                            boolean hasHabakiri = hasInInventory(player, SwordType.AME_NO_HABAKIRI);
                            int witherLevel = plugin.getConfigManager().getInt("swords.enma.wither_level", 1);
                            if (!hasHabakiri) {
                                player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40, witherLevel-1, true, false, false));
                            } else {
                                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 1, true, false, false));
                                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,     40, 2, true, false, false));
                            }
                        }
                        case AME_NO_HABAKIRI -> ParticleUtils.spawnHabakiriAura(player, plugin);
                        case NICHIRIN -> {
                            ParticleUtils.spawnNichirinAura(player, plugin);
                            NichirinSkills.spawnFireTrail(player, plugin);
                        }
                        case SENBONZAKURA    -> ParticleUtils.spawnSenbonzakuraAura(player, plugin);
                        case KAGURA_NO_TACHI -> {
                            ParticleUtils.spawnKaguraAura(player, plugin);
                            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 2, true, false, false));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,     40, 3, true, false, false));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE,   40, 1, true, false, false));
                        }
                    }
                    plugin.getSkillHotbarManager().showSkillBar(player, type);
                }
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        SwordType type = plugin.getSwordManager().getSwordType(item);
        if (type == null) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        switch (type) {
            case BLADE_OF_THAW   -> BladeOfThawSkills.applyFrostPassive(target, player);
            case EXCALIBUR       -> ExcaliburSkills.applyLightPassive(target, player, plugin);
            case EA              -> EaSkills.applyVoidPassive(target, player);
            case MURASAME        -> MurasameSkills.applyMurasameCurse(target, player);
            case ENMA            -> EnmaSkills.applyEnmaPassive(target, player);
            case AME_NO_HABAKIRI -> HabakiriSkills.applyWaterPassive(target, player);
            case NICHIRIN        -> NichirinSkills.applyBurnPassive(target, player);
            case SENBONZAKURA    -> SenbonzakuraSkills.applyPetalBleed(target, player, plugin);
            case KAGURA_NO_TACHI -> KaguraSkills.applyKaguraPassive(target, player, plugin);
        }

        // God-slayer check for Habakiri and Kagura
        if (type == SwordType.AME_NO_HABAKIRI || type == SwordType.KAGURA_NO_TACHI) {
            applyGodSlayer(event, target);
        }

        // Blade of Thaw sentinel strike on hit
        if (type == SwordType.BLADE_OF_THAW && BladeOfThawSkills.areSentinelsActive(player.getUniqueId())) {
            double sentinelDmg = plugin.getConfigManager().getDouble("swords.blade_of_thaw.sentinels.counter_damage", 15.0);
            target.damage(sentinelDmg, player);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3));
            ParticleUtils.spawnGlacialImpact(target.getLocation(), plugin);
        }
    }

    private void applyGodSlayer(EntityDamageByEntityEvent event, LivingEntity target) {
        double mult = plugin.getConfigManager().getDouble("swords.ame_no_habakiri.god_slayer_multiplier", 3.5);
        String name = target.getType().name();
        if (name.equals("ENDER_DRAGON") || name.equals("WITHER") ||
            name.equals("ELDER_GUARDIAN") || name.equals("WARDEN")) {
            event.setDamage(event.getDamage() * mult);
        }
        event.setDamage(event.getDamage() * 1.4);
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        ItemStack item = killer.getInventory().getItemInMainHand();
        SwordType type = plugin.getSwordManager().getSwordType(item);
        if (type == null) return;
        // Only Enma and Ame no Habakiri track awakening kills
        if (type == SwordType.ENMA || type == SwordType.AME_NO_HABAKIRI) {
            plugin.getAwakeningManager().addKill(killer, type);
        }
    }

    private boolean hasInInventory(Player player, SwordType type) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && plugin.getSwordManager().getSwordType(item) == type) return true;
        }
        return false;
    }
}
