package dev.mythicblades.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<String, Long> cooldowns = new HashMap<>();

    private String key(UUID player, String skill) { return player + ":" + skill; }

    public void set(UUID player, String skill, long durationMillis) {
        cooldowns.put(key(player, skill), System.currentTimeMillis() + durationMillis);
    }

    public boolean isOnCooldown(UUID player, String skill) {
        Long expiry = cooldowns.get(key(player, skill));
        return expiry != null && System.currentTimeMillis() < expiry;
    }

    public long getRemainingSeconds(UUID player, String skill) {
        Long expiry = cooldowns.get(key(player, skill));
        if (expiry == null) return 0;
        long r = expiry - System.currentTimeMillis();
        return r > 0 ? (r / 1000) + 1 : 0;
    }

    public void clear(UUID player, String skill) { cooldowns.remove(key(player, skill)); }
}
