package dev.mythicblades;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;

import java.util.List;

public enum SwordType {

    BLADE_OF_THAW(
        "blade_of_thaw",
        "✦ Blade of Thaw",
        Tier.SINGULARITY,
        TextColor.color(0xA8D8EA),
        Material.DIAMOND_SWORD,
        List.of(
            "§b§o\"The ice does not just freeze; it remembers.\"",
            "§b§o\"And when it thaws, it unleashes the weight of an entire winter.\"",
            "",
            "§8They say there are seven. The scholars agree.",
            "§8The legends confirm it. But in the oldest texts,",
            "§8buried beneath centuries of frost, a single line:",
            "§8'And the eighth wept, and the world grew cold.'",
            "§8Some say it does not exist. Others say it found them.",
            "",
            "§b❄ [RMB] Glacial Monolith §7— Ice projectile, massive AOE + Slowness VII",
            "§b❄ [F] Ring of Sentinels §7— 6 orbiting shards, toggle",
            "§5★ [SHIFT+RMB] Absolute Zero §7— Survive death, full heal, 15s meltdown"
        ),
        List.of("glacial_monolith", "ring_of_sentinels", "absolute_zero")
    ),

    KAGURA_NO_TACHI(
        "kagura_no_tachi",
        "✦ Kagura no Tachi — Blade of the Divine Dance",
        Tier.SINGULARITY,
        TextColor.color(0xDA70D6),
        Material.NETHERITE_SWORD,
        List.of(
            "§5§o\"One half screaming. One half silent.\"",
            "§5§o\"Everything in its path ceases to exist with a grace it did not deserve.\"",
            "",
            "§7Enma was born from the deepest pits of ruin.",
            "§7Ame no Habakiri wept the first time it drew blood.",
            "§7It has never stopped.",
            "§7No one knows what happens when hell reaches out",
            "§7and heaven takes its hand.",
            "",
            "§c✦ [RMB] Dual Resonance §7— Fire and holy 30-block dual slashes",
            "§5★ [SHIFT+RMB] Tenchi Kaimei §7— Fire erupts below, holy light crashes above",
            "§a✦ Enma Wither §7— Fully nullified. Regen II + Strength III active.",
            "",
            "§8The only one of its kind. Singularity. Eternal."
        ),
        List.of("dual_resonance", "tenchi_kaimei")
    ),

    EXCALIBUR(
        "excalibur",
        "✦ Excalibur, Sword of Promised Victory",
        Tier.MYTHIC,
        TextColor.color(0xFFD700),
        Material.GOLDEN_SWORD,
        List.of(
            "§6§o\"Pulled not from stone, but from the very promise of victory itself.\"",
            "",
            "§7It does not choose the strong. It does not choose the worthy.",
            "§7It chooses the one who has already decided they will not lose.",
            "§7The light it carries is not kindness. It is inevitability.",
            "",
            "§e☀ [RMB] Twin Strike §7— Double 90° forward slash, massive AOE",
            "§e✦ [F] Holy Pulse §7— Holy shockwave, blinds + launches all nearby",
            "§e★ [SHIFT+RMB] Excalibur §7— 30-block beam, 10s lightning rain, invulnerable"
        ),
        List.of("twin_strike", "holy_pulse", "excalibur_ult")
    ),

    EA(
        "ea",
        "✦ Ea, Sword of Rupture",
        Tier.MYTHIC,
        TextColor.color(0x8B0000),
        Material.NETHERITE_SWORD,
        List.of(
            "§4§o\"Before the world had a name, this blade already existed.\"",
            "",
            "§7It does not cut flesh. It cuts the idea of flesh.",
            "§7It does not destroy kingdoms.",
            "§7It destroys the concept of ground they stood on.",
            "",
            "§c⚔ [RMB] Sword Barrage §7— Launch phantom swords at all approaching enemies",
            "§c✦ [F] Void Slash §7— 30-block red lightning slash, 5 blocks wide",
            "§4★ [SHIFT+RMB] Enuma Elish §7— 30-block void beam + red lightning storm, invulnerable"
        ),
        List.of("sword_barrage", "void_slash", "enuma_elish")
    ),

    MURASAME(
        "murasame",
        "✦ One-Cut Killer: Murasame",
        Tier.MYTHIC,
        TextColor.color(0x8B0000),
        Material.NETHERITE_SWORD,
        List.of(
            "§4§o\"One strike. No cure.\"",
            "",
            "§7Every sword has a purpose. Murasame's is simple: one cut.",
            "§7The poison moves through the blood like a verdict —",
            "§7calm, certain, final. There is no antidote.",
            "",
            "§c☠ [RMB] Lethal Poison §7— Heart-stopping curse on all nearby",
            "§4★ [SHIFT+RMB] Berserk Mode §7— Strength V, Speed IV, 15s; permanent debuff after"
        ),
        List.of("lethal_poison", "berserk_mode")
    ),

    ENMA(
        "enma",
        "✦ Enma",
        Tier.MYTHIC,
        TextColor.color(0xFF4500),
        Material.NETHERITE_SWORD,
        List.of(
            "§6§o\"Most blades serve their wielder. Enma does not serve anyone.\"",
            "",
            "§7It tolerates. It tests. Every swing costs something.",
            "§7The truly terrifying ones smile and ask for more.",
            "",
            "§c☠ Life Drain §7— Wither II passive; mitigated by Ame no Habakiri",
            "§6★ [SHIFT+RMB] Hakai Slash §7— 50-block, 5-wide chaos cleave",
            "",
            "§8Fuse with Ame no Habakiri (both Awakened) → Kagura no Tachi"
        ),
        List.of("life_drain", "hakai_slash")
    ),

    AME_NO_HABAKIRI(
        "ame_no_habakiri",
        "✦ Ame no Habakiri",
        Tier.MYTHIC,
        TextColor.color(0xE0E0FF),
        Material.NETHERITE_SWORD,
        List.of(
            "§f§o\"The gods did not create this blade to be wielded.\"",
            "",
            "§7They created it to remind things that were too certain",
            "§7of their own invincibility — that nothing is beyond cutting.",
            "§7The Ender Dragon does not know this blade exists. It will.",
            "",
            "§b⚔ [RMB] Heavenly Parry §7— Invulnerable dash + Speed III + Strength III burst",
            "§f★ [SHIFT+RMB] Divine Severance §7— 50-block holy beam, bypasses all armor",
            "",
            "§8Fuse with Enma (both Awakened) → Kagura no Tachi"
        ),
        List.of("heavenly_parry", "divine_severance")
    ),

    NICHIRIN(
        "nichirin",
        "✦ Crimson Nichirin — Sun Breathing",
        Tier.LEGENDARY,
        TextColor.color(0xFF6347),
        Material.DIAMOND_SWORD,
        List.of(
            "§c§o\"A blade that turned red through sheer will alone.\"",
            "",
            "§7A Nichirin blade reflects the sun. In the hands of someone",
            "§7who pushed past every limit, it flushes red and stays.",
            "§7It is a scar the sword wears on behalf of its wielder.",
            "",
            "§e☀ Regen Null §7— Passive: enemies cannot regenerate health in combat",
            "§c⚡ [RMB] Flame Hashira §7— Teleport-dash spiral, ignite all in path",
            "§c✦ [F] Flame Sweep §7— Massive solar arc explosion, 10-block radius",
            "§6★ [SHIFT+RMB] Hinokami Kagura §7— 12 escalating solar strikes + Strength buff"
        ),
        List.of("flame_hashira", "flame_sweep", "hinokami_kagura")
    ),

    SENBONZAKURA(
        "senbonzakura",
        "✦ Senbonzakura",
        Tier.LEGENDARY,
        TextColor.color(0xFFB7C5),
        Material.DIAMOND_SWORD,
        List.of(
            "§d§o\"A thousand blades. You will never see them coming.\"",
            "",
            "§7There is an old story about a swordsman who loved",
            "§7cherry blossoms so deeply that when he died,",
            "§7his blade shattered — not from weakness, but from grief.",
            "§7They say it is beautiful. They say it is the last thing they remember.",
            "",
            "§d✿ Petal Bleed §7— Passive: every hit bleeds the target for 5 seconds",
            "§d✦ [RMB] Scatter §7— Thousand-blade storm, shreds all nearby",
            "§d⚡ [F] Petal Prison §7— Root + bleed cage on all nearby enemies",
            "§5★ [SHIFT+RMB] Senbonzakura Kageyoshi §7— Two blade forests. Annihilation."
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
        Tier(String display) { this.display = display; }
        public String getDisplay() { return display; }
    }
}
