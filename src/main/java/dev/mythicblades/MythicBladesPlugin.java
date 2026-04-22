package dev.mythicblades;

import dev.mythicblades.listeners.*;
import dev.mythicblades.managers.*;
import dev.mythicblades.ui.SkillHotbarManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MythicBladesPlugin extends JavaPlugin {

    private static MythicBladesPlugin instance;
    private ConfigManager configManager;
    private SwordManager swordManager;
    private OwnershipManager ownershipManager;
    private AwakeningManager awakeningManager;
    private FusionManager fusionManager;
    private SkillHotbarManager skillHotbarManager;
    private CooldownManager cooldownManager;
    private BuffManager buffManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.configManager      = new ConfigManager(this);
        this.cooldownManager    = new CooldownManager();
        this.swordManager       = new SwordManager(this);
        this.ownershipManager   = new OwnershipManager(this);
        this.awakeningManager   = new AwakeningManager(this);
        this.fusionManager      = new FusionManager(this);
        this.skillHotbarManager = new SkillHotbarManager(this);
        this.buffManager        = new BuffManager(this);

        getServer().getPluginManager().registerEvents(new SwordCraftListener(this),   this);
        getServer().getPluginManager().registerEvents(new SwordSkillListener(this),   this);
        getServer().getPluginManager().registerEvents(new SwordPassiveListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathReturnListener(this),  this);
        getServer().getPluginManager().registerEvents(new FusionListener(this),       this);
        getServer().getPluginManager().registerEvents(new SkillHotbarListener(this),  this);
        getServer().getPluginManager().registerEvents(new CollectAllListener(this),   this);

        getCommand("mythicblades").setExecutor(new MythicBladesCommand(this));

        getLogger().info("MythicBlades awakened. Eight blades wait.");
    }

    @Override
    public void onDisable() {
        ownershipManager.saveData();
        awakeningManager.saveData();
        getLogger().info("MythicBlades sealed.");
    }

    public static MythicBladesPlugin getInstance()      { return instance; }
    public ConfigManager getConfigManager()              { return configManager; }
    public SwordManager getSwordManager()                { return swordManager; }
    public OwnershipManager getOwnershipManager()        { return ownershipManager; }
    public AwakeningManager getAwakeningManager()        { return awakeningManager; }
    public FusionManager getFusionManager()              { return fusionManager; }
    public SkillHotbarManager getSkillHotbarManager()    { return skillHotbarManager; }
    public CooldownManager getCooldownManager()          { return cooldownManager; }
    public BuffManager getBuffManager()                  { return buffManager; }
}
