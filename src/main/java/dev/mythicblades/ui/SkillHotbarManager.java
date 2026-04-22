package dev.mythicblades.ui;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.SwordType;
import dev.mythicblades.managers.CooldownManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkillHotbarManager {

    private final MythicBladesPlugin plugin;
    private final Map<UUID, BukkitTask> hotbarTasks  = new HashMap<>();
    private final Map<UUID, SwordType>  lastSwordType = new HashMap<>();

    private static final Map<SwordType, String[]> SKILL_LABELS = new HashMap<>();
    private static final Map<SwordType, String[]> SKILL_KEYS   = new HashMap<>();

    static {
        SKILL_LABELS.put(SwordType.BLADE_OF_THAW, new String[]{
            "§b[RMB] Glacial Monolith", "§b[F] Sentinels: Toggle", "§5[SHIFT+RMB] Absolute Zero ★"});
        SKILL_LABELS.put(SwordType.KAGURA_NO_TACHI, new String[]{
            "§5[RMB] Dual Resonance", "§5[SHIFT+RMB] Tenchi Kaimei ★", ""});
        SKILL_LABELS.put(SwordType.EXCALIBUR, new String[]{
            "§e[RMB] Twin Strike", "§e[F] Holy Pulse", "§e[SHIFT+RMB] Heaven's Descent ★"});
        SKILL_LABELS.put(SwordType.EA, new String[]{
            "§c[RMB] Sword Barrage", "§c[F] Void Slash", "§4[SHIFT+RMB] Enuma Elish ★"});
        SKILL_LABELS.put(SwordType.MURASAME, new String[]{
            "§4[RMB] Curse Mark", "§8—", "§4[SHIFT+RMB] Berserk ★"});
        SKILL_LABELS.put(SwordType.ENMA, new String[]{
            "§6[F] Drain Info", "§8—", "§c[SHIFT+RMB] Hakai Slash ★"});
        SKILL_LABELS.put(SwordType.AME_NO_HABAKIRI, new String[]{
            "§f[RMB] Heavenly Parry", "§b[F] God-Slayer Info", "§f[SHIFT+RMB] Divine Severance ★"});
        SKILL_LABELS.put(SwordType.NICHIRIN, new String[]{
            "§c[RMB] Flame Hashira", "§e[F] Flame Sweep", "§6[SHIFT+RMB] Hinokami Kagura ★"});
        SKILL_LABELS.put(SwordType.SENBONZAKURA, new String[]{
            "§d[RMB] Scatter", "§d[F] Petal Prison", "§5[SHIFT+RMB] Kageyoshi ★"});

        SKILL_KEYS.put(SwordType.BLADE_OF_THAW,   new String[]{"glacial_monolith", "", ""});
        SKILL_KEYS.put(SwordType.KAGURA_NO_TACHI,  new String[]{"dual_resonance", "", "tenchi_kaimei"});
        SKILL_KEYS.put(SwordType.EXCALIBUR,        new String[]{"twin_strike", "holy_pulse", "excalibur_ult"});
        SKILL_KEYS.put(SwordType.EA,               new String[]{"sword_barrage", "void_slash", "enuma_elish"});
        SKILL_KEYS.put(SwordType.MURASAME,         new String[]{"lethal_poison", "", "berserk_mode"});
        SKILL_KEYS.put(SwordType.ENMA,             new String[]{"", "", "hakai_slash"});
        SKILL_KEYS.put(SwordType.AME_NO_HABAKIRI,  new String[]{"heavenly_parry", "", "divine_severance"});
        SKILL_KEYS.put(SwordType.NICHIRIN,         new String[]{"flame_hashira", "flame_sweep", "hinokami_kagura"});
        SKILL_KEYS.put(SwordType.SENBONZAKURA,     new String[]{"scatter", "petal_prison", "kageyoshi"});
    }

    public SkillHotbarManager(MythicBladesPlugin plugin) { this.plugin = plugin; }

    public void showSkillBar(Player player, SwordType type) {
        if (lastSwordType.get(player.getUniqueId()) == type) return;
        lastSwordType.put(player.getUniqueId(), type);
        stopHotbar(player);

        String[] labels = SKILL_LABELS.get(type);
        String[] keys   = SKILL_KEYS.get(type);
        if (labels == null) return;

        CooldownManager cd = plugin.getCooldownManager();

        BukkitTask task = new BukkitRunnable() {
            @Override public void run() {
                if (!player.isOnline()) { cancel(); return; }
                StringBuilder bar = new StringBuilder();
                for (int i = 0; i < labels.length; i++) {
                    String label = labels[i];
                    if (label.isEmpty() || label.equals("§8—")) continue;
                    if (bar.length() > 0) bar.append("  §8|  ");
                    String key = (keys != null && i < keys.length) ? keys[i] : "";
                    if (!key.isEmpty() && cd.isOnCooldown(player.getUniqueId(), key)) {
                        long rem = cd.getRemainingSeconds(player.getUniqueId(), key);
                        bar.append("§8").append(stripColor(label)).append(" §7(").append(rem).append("s)");
                    } else {
                        bar.append(label);
                    }
                }
                String tier = "§8[" + type.getTier().getDisplay() + "§8] §8| ";
                player.sendActionBar(Component.text(tier + bar).decoration(TextDecoration.ITALIC, false));
            }
        }.runTaskTimer(plugin, 0L, 5L);

        hotbarTasks.put(player.getUniqueId(), task);
    }

    public void clearSkillBar(Player player) {
        stopHotbar(player);
        lastSwordType.remove(player.getUniqueId());
        player.sendActionBar(Component.empty());
    }

    private void stopHotbar(Player player) {
        BukkitTask task = hotbarTasks.remove(player.getUniqueId());
        if (task != null) task.cancel();
    }

    private String stripColor(String s) { return s.replaceAll("§[0-9a-fk-or]", ""); }
}
