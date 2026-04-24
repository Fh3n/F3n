package dev.mythicblades.listeners;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.SwordType;
import dev.mythicblades.swords.*;
import dev.mythicblades.utils.ParticleUtils;
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
        MurasameSkills.startHudTask(plugin);
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
                            ParticleUtils.frostAura(player);
                            BladeOfThawSkills.tickSentinels(player, plugin);
                        }
                        case EXCALIBUR       -> ParticleUtils.holyAura(player);
                        case EA              -> ParticleUtils.voidAura(player);
                        case MURASAME        -> ParticleUtils.bloodAura(player);
                        case ENMA -> {
                            ParticleUtils.enmaAura(player);
                            boolean hasHabakiri = hasInInventory(player, SwordType.AME_NO_HABAKIRI);
                            int witherLevel = plugin.getConfigManager().swordInt("enma", "wither_self.amplifier", 1);
                            if (!hasHabakiri) {
                                player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40, witherLevel - 1, true, false, false));
                            } else {
                                int regenAmp = plugin.getConfigManager().swordInt("enma", "paired_regen.regen_amplifier", 1);
                                int strAmp   = plugin.getConfigManager().swordInt("enma", "paired_regen.strength_amplifier", 2);
                                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, regenAmp, true, false, false));
                                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,     40, strAmp,   true, false, false));
                            }
                        }
                        case AME_NO_HABAKIRI -> ParticleUtils.habakiriAura(player);
                        case NICHIRIN -> {
                            ParticleUtils.nichirinAura(player);
                            NichirinSkills.spawnFireTrail(player, plugin);
                        }
                        case SENBONZAKURA    -> ParticleUtils.senbonAura(player);
                        case KAGURA_NO_TACHI -> {
                            ParticleUtils.kaguraAura(player);
                            int regenAmp = plugin.getConfigManager().swordInt("kagura_no_tachi", "self_buffs.regen_amplifier", 2);
                            int strAmp   = plugin.getConfigManager().swordInt("kagura_no_tachi", "self_buffs.strength_amplifier", 3);
                            int resAmp   = plugin.getConfigManager().swordInt("kagura_no_tachi", "self_buffs.resistance_amplifier", 1);
                            int buffDur  = plugin.getConfigManager().swordInt("kagura_no_tachi", "self_buffs.buff_duration", 40);
                            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, buffDur, regenAmp, true, false, false));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,     buffDur, strAmp,   true, false, false));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE,   buffDur, resAmp,   true, false, false));
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
            case BLADE_OF_THAW   -> BladeOfThawSkills.applyFrostPassive(target, player, plugin);
            case EXCALIBUR       -> ExcaliburSkills.applyLightPassive(target, player, plugin);
            case EA              -> EaSkills.applyVoidPassive(target, player, plugin);
            case MURASAME        -> MurasameSkills.applyMurasameCurse(target, player, plugin);
            case ENMA            -> EnmaSkills.applyEnmaPassive(target, player, plugin);
            case AME_NO_HABAKIRI -> HabakiriSkills.applyWaterPassive(target, player, plugin);
            case NICHIRIN        -> NichirinSkills.applyBurnPassive(target, player, plugin);
            case SENBONZAKURA    -> SenbonzakuraSkills.applyPetalBleed(target, player, plugin);
            case KAGURA_NO_TACHI -> KaguraSkills.applyKaguraPassive(target, player, plugin);
        }

        if (type == SwordType.AME_NO_HABAKIRI || type == SwordType.KAGURA_NO_TACHI) {
            applyGodSlayer(event, target, plugin);
        }
    }

    private void applyGodSlayer(EntityDamageByEntityEvent event, LivingEntity target, MythicBladesPlugin plugin) {
        double mult     = plugin.getConfigManager().swordVal("ame_no_habakiri", "god_slayer.multiplier", 3.5);
        double baseMult = plugin.getConfigManager().swordVal("ame_no_habakiri", "god_slayer.base_multiplier", 1.4);
        String name = target.getType().name();
        if (name.equals("ENDER_DRAGON") || name.equals("WITHER") ||
            name.equals("ELDER_GUARDIAN") || name.equals("WARDEN")) {
            event.setDamage(event.getDamage() * mult);
        }
        event.setDamage(event.getDamage() * baseMult);
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        ItemStack item = killer.getInventory().getItemInMainHand();
        SwordType type = plugin.getSwordManager().getSwordType(item);
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
