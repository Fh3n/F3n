package dev.mythicblades.listeners;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.SwordType;
import dev.mythicblades.swords.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class SwordSkillListener implements Listener {

    private final MythicBladesPlugin plugin;

    public SwordSkillListener(MythicBladesPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
            event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        SwordType type = plugin.getSwordManager().getSwordType(item);
        if (type == null) return;

        event.setCancelled(true);
        boolean shift = player.isSneaking();

        switch (type) {
            case BLADE_OF_THAW -> {
                if (shift) {
                    BladeOfThawSkills.armResurrection(player.getUniqueId());
                    player.sendMessage("§b❄ Absolute Zero armed. You will survive the next fatal hit.");
                } else {
                    BladeOfThawSkills.glacialMonolith(player, plugin);
                }
            }
            case EXCALIBUR -> {
                if (shift) ExcaliburSkills.excaliburUlt(player, plugin);
                else       ExcaliburSkills.twinStrike(player, plugin);
            }
            case EA -> {
                if (shift) EaSkills.enumaElish(player, plugin);
                else       EaSkills.swordBarrage(player, plugin);
            }
            case MURASAME -> {
                if (shift) MurasameSkills.berserkMode(player, plugin);
                else       MurasameSkills.lethalPoisonActive(player, plugin);
            }
            case ENMA -> {
                if (shift) EnmaSkills.hakaiSlash(player, plugin);
            }
            case AME_NO_HABAKIRI -> {
                if (shift) HabakiriSkills.divineSeverance(player, plugin);
                else       HabakiriSkills.heavenlyParry(player, plugin);
            }
            case NICHIRIN -> {
                if (shift) NichirinSkills.hinokamiKagura(player, plugin);
                else       NichirinSkills.flameHashira(player, plugin);
            }
            case SENBONZAKURA -> {
                if (shift) SenbonzakuraSkills.kageyoshi(player, plugin);
                else       SenbonzakuraSkills.scatter(player, plugin);
            }
            case KAGURA_NO_TACHI -> {
                if (shift) KaguraSkills.tenchiKaimei(player, plugin);
                else       KaguraSkills.dualResonance(player, plugin);
            }
        }
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        SwordType type = plugin.getSwordManager().getSwordType(item);
        if (type == null) return;

        event.setCancelled(true);

        switch (type) {
            case BLADE_OF_THAW   -> BladeOfThawSkills.toggleSentinels(player, plugin);
            case EXCALIBUR       -> ExcaliburSkills.holyPulse(player, plugin);
            case EA              -> EaSkills.voidSlash(player, plugin);
            case ENMA            -> EnmaSkills.drainInfo(player, plugin);
            case AME_NO_HABAKIRI -> HabakiriSkills.godSlayerInfo(player, plugin);
            case NICHIRIN        -> NichirinSkills.flameSweep(player, plugin);
            case SENBONZAKURA    -> SenbonzakuraSkills.petalPrison(player, plugin);
        }
    }
}
