package dev.mythicblades.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ParticleUtils {

    public static void spawn(World w, Particle p, Location l, int n,
                              double ox, double oy, double oz, double spd) {
        if (w == null || p == null || l == null) return;
        try { w.spawnParticle(p, l, n, ox, oy, oz, spd); } catch (Exception ignored) {}
    }

    public static void ring(World w, Particle p, Location center, double radius, int points) {
        for (int i = 0; i < points; i++) {
            double a = (2 * Math.PI / points) * i;
            spawn(w, p, center.clone().add(Math.cos(a) * radius, 0, Math.sin(a) * radius), 1, 0, 0.05, 0, 0.01);
        }
    }

    public static void line(World w, Particle p, Location from, Location to, double step) {
        Vector dir = to.toVector().subtract(from.toVector());
        double len = dir.length();
        dir.normalize().multiply(step);
        Location cur = from.clone();
        for (double d = 0; d < len; d += step) {
            spawn(w, p, cur, 1, 0, 0, 0, 0);
            cur.add(dir);
        }
    }

    public static void sphere(World w, Particle p, Location center, double radius, int count) {
        for (int i = 0; i < count; i++) {
            double theta = Math.random() * 2 * Math.PI;
            double phi   = Math.random() * Math.PI;
            double x = radius * Math.sin(phi) * Math.cos(theta);
            double y = radius * Math.cos(phi);
            double z = radius * Math.sin(phi) * Math.sin(theta);
            spawn(w, p, center.clone().add(x, y, z), 1, 0, 0, 0, 0);
        }
    }

    // Aura methods — kept minimal; called every 6 ticks so counts are already low
    public static void frostAura(Player p) {
        Location l = p.getLocation().add(0, 1, 0);
        spawn(p.getWorld(), Particle.SNOWFLAKE, l, 2, 0.3, 0.5, 0.3, 0.01);
        spawn(p.getWorld(), Particle.END_ROD,   l, 1, 0.2, 0.4, 0.2, 0.01);
    }

    public static void holyAura(Player p) {
        Location l = p.getLocation().add(0, 1, 0);
        spawn(p.getWorld(), Particle.END_ROD, l, 2, 0.3, 0.5, 0.3, 0.02);
        spawn(p.getWorld(), Particle.ENCHANT, l, 2, 0.4, 0.6, 0.4, 0.1);
    }

    public static void voidAura(Player p) {
        Location l = p.getLocation().add(0, 1, 0);
        spawn(p.getWorld(), Particle.PORTAL,        l, 3, 0.3, 0.5, 0.3, 0.3);
        spawn(p.getWorld(), Particle.DRAGON_BREATH, l, 1, 0.2, 0.3, 0.2, 0.01);
    }

    public static void bloodAura(Player p) {
        spawn(p.getWorld(), Particle.DAMAGE_INDICATOR, p.getLocation().add(0, 1, 0), 1, 0.2, 0.4, 0.2, 0.01);
    }

    public static void enmaAura(Player p) {
        spawn(p.getWorld(), Particle.FLAME, p.getLocation().add(0, 1, 0), 3, 0.3, 0.5, 0.3, 0.04);
    }

    public static void habakiriAura(Player p) {
        Location l = p.getLocation().add(0, 1, 0);
        spawn(p.getWorld(), Particle.END_ROD, l, 2, 0.2, 0.4, 0.2, 0.02);
        spawn(p.getWorld(), Particle.ENCHANT, l, 2, 0.3, 0.5, 0.3, 0.05);
    }

    public static void nichirinAura(Player p) {
        Location l = p.getLocation().add(0, 1, 0);
        spawn(p.getWorld(), Particle.FLAME, l, 2, 0.2, 0.4, 0.2, 0.03);
    }

    public static void senbonAura(Player p) {
        Location l = p.getLocation().add(0, 1, 0);
        spawn(p.getWorld(), Particle.CHERRY_LEAVES, l, 3, 0.5, 0.7, 0.5, 0.04);
        spawn(p.getWorld(), Particle.ENCHANT,       l, 1, 0.3, 0.5, 0.3, 0.08);
    }

    public static void kaguraAura(Player p) {
        Location l = p.getLocation().add(0, 1, 0);
        spawn(p.getWorld(), Particle.FLAME,   l, 2, 0.3, 0.5, 0.3, 0.03);
        spawn(p.getWorld(), Particle.END_ROD, l, 1, 0.2, 0.4, 0.2, 0.02);
    }

    public static void sentinelOrbit(Player p, int count) {
        Location center = p.getLocation().add(0, 1, 0);
        double t = System.currentTimeMillis() / 450.0;
        World w = p.getWorld();
        for (int i = 0; i < count; i++) {
            double a = (2 * Math.PI / count) * i + t;
            Location pt = center.clone().add(Math.cos(a) * 1.8, 0, Math.sin(a) * 1.8);
            spawn(w, Particle.SNOWFLAKE, pt, 1, 0, 0, 0, 0);
        }
    }
}
