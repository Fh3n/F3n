package dev.mythicblades.utils;

import dev.mythicblades.MythicBladesPlugin;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ParticleUtils {

    private static void spawn(World w, Particle p, Location l, int n, double ox, double oy, double oz, double spd) {
        if (w == null || p == null || l == null) return;
        try { w.spawnParticle(p, l, n, ox, oy, oz, spd); } catch (Exception ignored) {}
    }

    // ── Blade of Thaw — light ice blue, snowflakes, END_ROD ──────────────────
    public static void spawnFrostAura(Player p, MythicBladesPlugin pl) {
        Location l = p.getLocation().add(0, 1, 0);
        spawn(p.getWorld(), Particle.SNOWFLAKE, l, 4, 0.3, 0.5, 0.3, 0.01);
        spawn(p.getWorld(), Particle.END_ROD,   l, 1, 0.2, 0.4, 0.2, 0.01);
    }

    public static void spawnGlacialImpact(Location l, MythicBladesPlugin pl) {
        World w = l.getWorld();
        spawn(w, Particle.SNOWFLAKE,    l, 30, 1.5, 0.5, 1.5, 0.08);
        spawn(w, Particle.END_ROD,      l, 15, 1.0, 0.5, 1.0, 0.05);
        spawn(w, Particle.ITEM_SNOWBALL,l, 20, 1.0, 0.5, 1.0, 0.15);
        spawn(w, Particle.EXPLOSION,    l,  2, 0.5, 0.2, 0.5, 0);
    }

    public static void spawnIceSentinelOrbit(Player p, int count) {
        Location center = p.getLocation().add(0, 1, 0);
        double t = System.currentTimeMillis() / 450.0;
        World w = p.getWorld();
        for (int i = 0; i < count; i++) {
            double a = (2 * Math.PI / count) * i + t;
            Location pt = center.clone().add(Math.cos(a) * 1.8, 0, Math.sin(a) * 1.8);
            spawn(w, Particle.SNOWFLAKE, pt, 1, 0, 0, 0, 0);
            spawn(w, Particle.END_ROD,   pt, 1, 0, 0, 0, 0);
        }
    }

    public static void spawnCryoCocoon(Player p) {
        Location l = p.getLocation().add(0, 1, 0);
        World w = p.getWorld();
        for (int i = 0; i < 8; i++) {
            double a = Math.random() * 2 * Math.PI, y = Math.random() * 2 - 1;
            spawn(w, Particle.SNOWFLAKE, l.clone().add(Math.cos(a)*0.6, y, Math.sin(a)*0.6), 2, 0, 0, 0, 0);
            spawn(w, Particle.END_ROD,   l.clone().add(Math.cos(a)*0.8, y, Math.sin(a)*0.8), 1, 0, 0, 0, 0);
        }
    }

    public static void spawnFrostHitEffect(Location l) {
        World w = l.getWorld();
        spawn(w, Particle.SNOWFLAKE,    l, 8, 0.3, 0.3, 0.3, 0.05);
        spawn(w, Particle.ITEM_SNOWBALL,l, 5, 0.2, 0.2, 0.2, 0.1);
    }

    // ── Excalibur — gold END_ROD + ENCHANT, holy white ───────────────────────
    public static void spawnHolyAura(Player p, MythicBladesPlugin pl) {
        Location l = p.getLocation().add(0, 1, 0);
        spawn(p.getWorld(), Particle.END_ROD, l, 3, 0.3, 0.5, 0.3, 0.02);
        spawn(p.getWorld(), Particle.ENCHANT, l, 4, 0.4, 0.6, 0.4, 0.1);
    }

    public static void spawnLightHitEffect(Location l) {
        World w = l.getWorld();
        spawn(w, Particle.END_ROD, l, 6, 0.2, 0.3, 0.2, 0.03);
        spawn(w, Particle.ENCHANT, l, 5, 0.3, 0.3, 0.3, 0.1);
    }

    public static void spawnExcaliburUltRing(Location center, double radius, World w) {
        int points = Math.max(24, (int)(radius * 5));
        for (int i = 0; i < points; i++) {
            double a = (2 * Math.PI / points) * i;
            Location pt = center.clone().add(Math.cos(a)*radius, 0.1, Math.sin(a)*radius);
            spawn(w, Particle.END_ROD, pt, 1, 0, 0.1, 0, 0.02);
            spawn(w, Particle.ENCHANT, pt, 1, 0, 0.1, 0, 0.05);
        }
    }

    /** Excalibur ult — pillar of END_ROD rising from target to sky */
    public static void spawnExcaliburPillar(Location base, World w) {
        for (int y = 0; y < 80; y++) {
            Location pt = base.clone().add(0, y * 0.8, 0);
            spawn(w, Particle.END_ROD, pt, 4, 0.5, 0, 0.5, 0.02);
            spawn(w, Particle.ENCHANT, pt, 3, 0.6, 0, 0.6, 0.1);
        }
    }

    // ── Ea — PORTAL + DRAGON_BREATH, void purple energy ──────────────────────
    public static void spawnVoidAura(Player p, MythicBladesPlugin pl) {
        Location l = p.getLocation().add(0, 1, 0);
        spawn(p.getWorld(), Particle.PORTAL,        l, 5, 0.3, 0.5, 0.3, 0.3);
        spawn(p.getWorld(), Particle.DRAGON_BREATH, l, 2, 0.2, 0.3, 0.2, 0.01);
    }

    public static void spawnVoidHitEffect(Location l) {
        World w = l.getWorld();
        spawn(w, Particle.PORTAL,        l, 10, 0.3, 0.3, 0.3, 0.3);
        spawn(w, Particle.DRAGON_BREATH, l,  5, 0.2, 0.2, 0.2, 0.02);
    }

    /** Ea drill beam — rotating portal segments, no lightning */
    public static void spawnEaDrillPoint(Location l, int step, World w) {
        for (int seg = 0; seg < 3; seg++) {
            double a = Math.toRadians(step * 12 + seg * 120);
            Location pt = l.clone().add(Math.cos(a)*1.0, 0, Math.sin(a)*1.0);
            spawn(w, Particle.PORTAL,        pt, 5, 0.1, 0.2, 0.1, 0.2);
            spawn(w, Particle.DRAGON_BREATH, pt, 3, 0.1, 0.1, 0.1, 0.03);
        }
        spawn(w, Particle.PORTAL, l, 3, 0.2, 0.2, 0.2, 0.1);
    }

    /** Ea ult rupture — expanding void ring, no lightning */
    public static void spawnEnumaRupture(Location center, double radius, World w) {
        int points = Math.max(20, (int)(radius * 4));
        for (int i = 0; i < points; i++) {
            double a = (2 * Math.PI / points) * i;
            Location pt = center.clone().add(Math.cos(a)*radius, 0, Math.sin(a)*radius);
            spawn(w, Particle.PORTAL,        pt, 4, 0.1, 0.5, 0.1, 0.3);
            spawn(w, Particle.DRAGON_BREATH, pt, 2, 0.1, 0.3, 0.1, 0.02);
        }
    }

    // ── Murasame — DAMAGE_INDICATOR + EFFECT (poison green) ──────────────────
    public static void spawnBloodAura(Player p, MythicBladesPlugin pl) {
        Location l = p.getLocation().add(0, 1, 0);
        spawn(p.getWorld(), Particle.DAMAGE_INDICATOR, l, 2, 0.2, 0.4, 0.2, 0.01);
    }

    public static void spawnPoisonStrike(Location l, MythicBladesPlugin pl) {
        World w = l.getWorld();
        spawn(w, Particle.EFFECT,            l, 15, 0.4, 0.4, 0.4, 0.1);
        spawn(w, Particle.DAMAGE_INDICATOR,  l,  8, 0.5, 0.5, 0.5, 0.1);
    }

    public static void spawnBerserkAura(Player p) {
        Location l = p.getLocation().add(0, 1, 0);
        spawn(p.getWorld(), Particle.DAMAGE_INDICATOR, l, 3, 0.5, 0.8, 0.5, 0.04);
        spawn(p.getWorld(), Particle.FLAME,            l, 2, 0.3, 0.5, 0.3, 0.03);
    }

    // ── Enma — FLAME only, no smoke ───────────────────────────────────────────
    public static void spawnEnmaAura(Player p, MythicBladesPlugin pl) {
        Location l = p.getLocation().add(0, 1, 0);
        spawn(p.getWorld(), Particle.FLAME, l, 5, 0.3, 0.5, 0.3, 0.04);
    }

    public static void spawnEnmaHitEffect(Location l) {
        World w = l.getWorld();
        spawn(w, Particle.FLAME,  l, 10, 0.3, 0.3, 0.3, 0.06);
        spawn(w, Particle.LAVA,   l,  3, 0.2, 0.2, 0.2, 0);
    }

    public static void spawnHakaiSlashParticles(Location l, MythicBladesPlugin pl) {
        World w = l.getWorld();
        spawn(w, Particle.FLAME, l, 5, 0.15, 0.4, 0.15, 0.05);
        spawn(w, Particle.LAVA,  l, 2, 0.1,  0.2, 0.1,  0);
    }

    // ── Ame no Habakiri — END_ROD + ENCHANT, silver-white ────────────────────
    public static void spawnHabakiriAura(Player p, MythicBladesPlugin pl) {
        Location l = p.getLocation().add(0, 1, 0);
        spawn(p.getWorld(), Particle.END_ROD, l, 3, 0.2, 0.4, 0.2, 0.02);
        spawn(p.getWorld(), Particle.ENCHANT, l, 3, 0.3, 0.5, 0.3, 0.05);
    }

    public static void spawnWaterHitEffect(Location l) {
        World w = l.getWorld();
        spawn(w, Particle.END_ROD, l, 8, 0.3, 0.3, 0.3, 0.02);
        spawn(w, Particle.SPLASH,  l, 8, 0.3, 0.3, 0.3, 0.15);
    }

    public static void spawnParryRing(Location center, double radius, World w) {
        int points = Math.max(16, (int)(radius * 4));
        for (int i = 0; i < points; i++) {
            double a = (2 * Math.PI / points) * i;
            spawn(w, Particle.END_ROD, center.clone().add(Math.cos(a)*radius, 0, Math.sin(a)*radius), 1, 0, 0.1, 0, 0.01);
        }
        spawn(w, Particle.END_ROD, center, 20, radius*0.4, 0.3, radius*0.4, 0.05);
    }

    // ── Nichirin — FLAME + LAVA, hot scarlet red ──────────────────────────────
    public static void spawnNichirinAura(Player p, MythicBladesPlugin pl) {
        Location l = p.getLocation().add(0, 1, 0);
        spawn(p.getWorld(), Particle.FLAME, l, 3, 0.2, 0.4, 0.2, 0.03);
        spawn(p.getWorld(), Particle.LAVA,  l, 1, 0.1, 0.3, 0.1, 0);
    }

    public static void spawnNichirinTrail(Location l, World w) {
        spawn(w, Particle.FLAME, l, 4, 0.2, 0.1, 0.2, 0.04);
        spawn(w, Particle.LAVA,  l, 1, 0.1, 0.1, 0.1, 0);
    }

    public static void spawnBurnHitEffect(Location l) {
        World w = l.getWorld();
        spawn(w, Particle.FLAME, l, 10, 0.3, 0.3, 0.3, 0.06);
        spawn(w, Particle.LAVA,  l,  4, 0.2, 0.2, 0.2, 0);
    }

    public static void spawnHinokamiSpiral(Location center) {
        World w = center.getWorld();
        for (int i = 0; i < 48; i++) {
            double a = Math.toRadians(i * 7.5), y = i * 0.06;
            spawn(w, Particle.FLAME, center.clone().add(Math.cos(a)*0.3, y, Math.sin(a)*0.3), 2, 0.05, 0.05, 0.05, 0.02);
        }
        spawn(w, Particle.EXPLOSION, center.clone().add(0, 1.8, 0), 1, 0.3, 0.2, 0.3, 0);
    }

    // ── Senbonzakura — CHERRY_LEAVES only, clean sakura ──────────────────────
    public static void spawnSenbonzakuraAura(Player p, MythicBladesPlugin pl) {
        Location l = p.getLocation().add(0, 1, 0);
        spawn(p.getWorld(), Particle.CHERRY_LEAVES, l, 6, 0.5, 0.7, 0.5, 0.04);
        spawn(p.getWorld(), Particle.ENCHANT,       l, 2, 0.3, 0.5, 0.3, 0.08);
    }

    public static void spawnPetalBleedOrbit(Entity target, MythicBladesPlugin pl) {
        Location l = target.getLocation().add(0, 1, 0);
        double t = System.currentTimeMillis() / 400.0;
        for (int i = 0; i < 6; i++) {
            double a = (2 * Math.PI / 6) * i + t;
            spawn(l.getWorld(), Particle.CHERRY_LEAVES, l.clone().add(Math.cos(a)*0.8, 0, Math.sin(a)*0.8), 2, 0.05, 0.1, 0.05, 0);
        }
    }

    public static void spawnScatterStorm(Location center, MythicBladesPlugin pl) {
        World w = center.getWorld();
        spawn(w, Particle.CHERRY_LEAVES, center, 80, 5, 2, 5, 0.3);
        spawn(w, Particle.ENCHANT,       center, 30, 4, 2, 4, 0.8);
        spawn(w, Particle.END_ROD,       center, 20, 3, 1, 3, 0.15);
    }

    public static void spawnKageyoshi(Location center, MythicBladesPlugin pl) {
        World w = center.getWorld();
        spawn(w, Particle.CHERRY_LEAVES, center, 200, 10, 3, 10, 0.5);
        spawn(w, Particle.ENCHANT,       center, 100,  8, 3,  8, 1.0);
        spawn(w, Particle.END_ROD,       center,  60,  6, 2,  6, 0.3);
        spawn(w, Particle.EXPLOSION,     center,   5,  3, 1,  3, 0);
        for (int i = 0; i < 8; i++) {
            double a = Math.toRadians(45 * i);
            Location col = center.clone().add(Math.cos(a)*7, 0, Math.sin(a)*7);
            for (int y = 0; y < 16; y++)
                spawn(w, Particle.CHERRY_LEAVES, col.clone().add(0, y*0.5, 0), 4, 0.3, 0.1, 0.3, 0.04);
        }
    }

    // ── Kagura no Tachi — FLAME + END_ROD alternating, duality ──────────────
    public static void spawnKaguraAura(Player p, MythicBladesPlugin pl) {
        Location l = p.getLocation().add(0, 1, 0);
        spawn(p.getWorld(), Particle.FLAME,   l, 3, 0.3, 0.5, 0.3, 0.03);
        spawn(p.getWorld(), Particle.END_ROD, l, 2, 0.2, 0.4, 0.2, 0.02);
    }

    public static void spawnTenchiKaimei(Location target, MythicBladesPlugin pl) {
        World w = target.getWorld();
        // Fire erupts from below
        for (int y = 0; y < 25; y++) {
            Location pt = target.clone().add(0, y * 0.5, 0);
            spawn(w, Particle.FLAME, pt, 6, 0.8, 0.1, 0.8, 0.05);
            spawn(w, Particle.LAVA,  pt, 2, 0.3, 0.1, 0.3, 0);
        }
        // Holy light descends from above
        for (int y = 0; y < 25; y++) {
            Location pt = target.clone().add(0, 12 - y * 0.5, 0);
            spawn(w, Particle.END_ROD, pt, 5, 0.5, 0.1, 0.5, 0.02);
            spawn(w, Particle.ENCHANT, pt, 3, 0.4, 0.1, 0.4, 0.08);
        }
        spawn(w, Particle.EXPLOSION,     target, 5, 3, 1, 3, 0);
        spawn(w, Particle.DRAGON_BREATH, target, 30, 4, 2, 4, 0.1);
    }

    public static void spawnBladeArc(Location center, Vector dir, double range, World w) {
        for (int i = -45; i <= 45; i += 6) {
            double rad = Math.toRadians(i);
            double cos = Math.cos(rad), sin = Math.sin(rad);
            Vector arc = new Vector(dir.getX()*cos + dir.getZ()*sin, 0, -dir.getX()*sin + dir.getZ()*cos).normalize().multiply(range);
            Location end = center.clone().add(arc);
            // Draw line from center to end
            Vector step = end.toVector().subtract(center.toVector()).normalize().multiply(0.5);
            Location cur = center.clone();
            double len = center.distance(end);
            for (double d = 0; d < len; d += 0.5) {
                cur.add(step);
                spawn(w, Particle.FLAME,   cur, 2, 0.05, 0.1, 0.05, 0.02);
                spawn(w, Particle.END_ROD, cur, 1, 0.05, 0.05, 0.05, 0.01);
            }
        }
    }
}
