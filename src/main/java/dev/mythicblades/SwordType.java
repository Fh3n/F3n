package dev.mythicblades;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;

import java.util.List;

public enum SwordType {

    BLADE_OF_THAW(
        "blade_of_thaw", "✦ Blade of Thaw", Tier.SINGULARITY,
        TextColor.color(0xA8D8EA), Material.DIAMOND_SWORD,
        List.of(
            "§b§o\"The ice does not just freeze; it remembers.\"",
            "§b§o\"And when it thaws, it carries the weight of an entire winter.\"",
            "",
            "§8Seven blades. The scholars agree. The legends confirm it.",
            "§8But in the oldest texts, buried beneath centuries of frost:",
            "§8'And the eighth wept, and the world grew cold.'",
            "",
            "§b❄ [RMB] Glacial Monolith §7— Ice pillar eruption, shockwave + freeze",
            "§b❄ [F] Ring of Sentinels §7— Orbiting shards that counter-strike on hit",
            "§5★ [SHIFT+RMB] Absolute Zero §7— Survive death, full restore, 15s meltdown"
        ),
        List.of("glacial_monolith", "ring_of_sentinels", "absolute_zero")
    ),

    KAGURA_NO_TACHI(
        "kagura_no_tachi", "✦ Kagura no Tachi — Blade of the Divine Dance", Tier.SINGULARITY,
        TextColor.color(0xDA70D6), Material.NETHERITE_SWORD,
        List.of(
            "§5§o\"One half screaming. One half silent.\"",
            "",
            "§7Enma was born from the deepest pits of ruin.",
            "§7Ame no Habakiri wept the first time it drew blood.",
            "§7No one knows what happens when hell takes heaven's hand.",
            "",
            "§c✦ [RMB] Dual Resonance §7— Paired fire and holy sweep, forward drill",
            "§5★ [SHIFT+RMB] Tenchi Kaimei §7— Heaven and hell descend simultaneously",
            "§a✦ Enma Wither §7— Nullified. Regen II + Strength III active.",
            "",
            "§8Singularity. The only rival to Blade of Thaw."
        ),
        List.of("dual_resonance", "tenchi_kaimei")
    ),

    EXCALIBUR(
        "excalibur", "✦ Excalibur, Sword of Promised Victory", Tier.MYTHIC,
        TextColor.color(0xFFD700), Material.GOLDEN_SWORD,
        List.of(
            "§6§o\"Not chosen. Decided.\"",
            "",
            "§7The light it carries is not kindness. It is inevitability.",
            "",
            "§e☀ [RMB] Twin Strike §7— Double 90° forward arc, Slowness + burst",
            "§e✦ [F] Holy Pulse §7— Radiant shockwave, blinds + launches all nearby",
            "§e★ [SHIFT+RMB] Heaven's Descent §7— Sky pillar, lightning rain, 7s invuln"
        ),
        List.of("twin_strike", "holy_pulse", "excalibur_ult")
    ),

    EA(
        "ea", "✦ Ea, Sword of Rupture", Tier.MYTHIC,
        TextColor.color(0x8B0000), Material.NETHERITE_SWORD,
        List.of(
            "§4§o\"Before the world had a name, this blade already existed.\"",
            "",
            "§7It does not cut flesh. It cuts the concept of flesh.",
            "",
            "§c⚔ [RMB] Sword Barrage §7— Launch phantom blades at all nearby enemies",
            "§c✦ [F] Void Slash §7— Wide rupture beam, spatial distortion",
            "§4★ [SHIFT+RMB] Enuma Elish §7— Void pillar descends from above. Run."
        ),
        List.of("sword_barrage", "void_slash", "enuma_elish")
    ),

    MURASAME(
        "murasame", "✦ One-Cut Killer: Murasame", Tier.MYTHIC,
        TextColor.color(0x8B0000), Material.NETHERITE_SWORD,
        List.of(
            "§4§o\"One strike. No cure.\"",
            "",
            "§7The poison moves like a verdict — calm, certain, final.",
            "",
            "§c☠ [RMB] Curse Mark §7— Detonate all marked targets in range",
            "§4★ [SHIFT+RMB] Berserk §7— Strength V, Speed III, 15s; debuff after"
        ),
        List.of("lethal_poison", "berserk_mode")
    ),

    ENMA(
        "enma", "✦ Enma", Tier.MYTHIC,
        TextColor.color(0xFF4500), Material.NETHERITE_SWORD,
        List.of(
            "§6§o\"It tolerates. It tests. Every swing costs something.\"",
            "",
            "§c☠ Life Drain §7— Wither passive; mitigated by Ame no Habakiri",
            "§6★ [SHIFT+RMB] Hakai Slash §7— 50-block chaos cleave, 5 wide",
            "",
            "§8Fuse with Ame no Habakiri (both Awakened) → Kagura no Tachi"
        ),
        List.of("life_drain", "hakai_slash")
    ),

    AME_NO_HABAKIRI(
        "ame_no_habakiri", "✦ Ame no Habakiri", Tier.MYTHIC,
        TextColor.color(0xE0E0FF), Material.NETHERITE_SWORD,
        List.of(
            "§f§o\"Nothing is beyond cutting.\"",
            "",
            "§7The Ender Dragon does not know this blade exists. It will.",
            "",
            "§b⚔ [RMB] Heavenly Parry §7— Leap + shockwave on landing",
            "§f★ [SHIFT+RMB] Divine Severance §7— 50-block holy beam, bypasses armor",
            "",
            "§8Fuse with Enma (both Awakened) → Kagura no Tachi"
        ),
        List.of("heavenly_parry", "divine_severance")
    ),

    NICHIRIN(
        "nichirin", "✦ Crimson Nichirin — Sun Breathing", Tier.LEGENDARY,
        TextColor.color(0xFF6347), Material.DIAMOND_SWORD,
        List.of(
            "§c§o\"A blade that turned red through sheer will alone.\"",
            "",
            "§7A scar the sword wears on behalf of its wielder.",
            "",
            "§e☀ Regen Null §7— Passive: enemies cannot regenerate in combat",
            "§c⚡ [RMB] Flame Hashira §7— Teleport-dash, ignite all in path",
            "§c✦ [F] Flame Sweep §7— Solar arc, wide fire explosion",
            "§6★ [SHIFT+RMB] Hinokami Kagura §7— X-sequence solar strikes + finisher"
        ),
        List.of("flame_hashira", "flame_sweep", "hinokami_kagura")
    ),

    SENBONZAKURA(
        "senbonzakura", "✦ Senbonzakura", Tier.LEGENDARY,
        TextColor.color(0xFFB7C5), Material.DIAMOND_SWORD,
        List.of(
            "§d§o\"A thousand blades. You will never see them coming.\"",
            "",
            "§7They say it is beautiful. They say it is the last thing they remember.",
            "",
            "§d✿ Petal Bleed §7— Passive: every hit bleeds for 5 seconds",
            "§d✦ [RMB] Scatter §7— Thousand-blade storm, shreds all nearby",
            "§d⚡ [F] Petal Prison §7— Root + bleed cage on all nearby enemies",
            "§5★ [SHIFT+RMB] Kageyoshi §7— Blossom domain. Annihilation."
        ),
        List.of("petal_bleed", "scatter", "petal_prison", "kageyoshi")
    );

    private final String id;
    private final String displayName;
    private final Tier tier;
    private final TextColor color;
    private final Material material;
    private final List<String> lore;
    private final List<String> skillIds;

    SwordType(String id, String displayName, Tier tier, TextColor color,
              Material material, List<String> lore, List<String> skillIds) {
        this.id = id; this.displayName = displayName; this.tier = tier;
        this.color = color; this.material = material;
        this.lore = lore; this.skillIds = skillIds;
    }

    public String getId()             { return id; }
    public String getDisplayName()    { return displayName; }
    public Tier getTier()             { return tier; }
    public TextColor getColor()       { return color; }
    public Material getMaterial()     { return material; }
    public List<String> getLore()     { return lore; }
    public List<String> getSkillIds() { return skillIds; }

    public enum Tier {
        LEGENDARY("§6◆ Legendary"),
        MYTHIC("§c◆◆ Mythic"),
        SINGULARITY("§5◆◆◆ Singularity");

        private final String display;
        Tier(String d) { this.display = d; }
        public String getDisplay() { return display; }
    }
}
