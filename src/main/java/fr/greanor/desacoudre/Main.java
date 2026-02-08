package fr.greanor.desacoudre;

import org.bukkit.plugin.java.JavaPlugin;
import fr.greanor.desacoudre.Listener.GameListener;
import org.bukkit.Bukkit;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Initialisation du PoolManager
        PoolManager poolManager = new PoolManager(this);

        // Initialisation du GameManager (avec référence au PoolManager)
        GameManager gameManager = new GameManager(poolManager);

        // Enregistrement de /createpool
        PoolCommands poolCommands = new PoolCommands(poolManager);
        this.getCommand("createpool").setExecutor(poolCommands);
        Bukkit.getPluginManager().registerEvents(poolCommands, this);

        // Enregistrement de /dac (avec accès au PoolManager)
        this.getCommand("dac").setExecutor(new DacCommand(gameManager, poolManager));

        // Enregistrement du listener du jeu
        getServer().getPluginManager().registerEvents(
                new GameListener(gameManager),
                this
        );

        getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        getLogger().info("  Plugin Dé à Coudre démarré !");
        getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin Dé à Coudre arrêté !");
    }
}