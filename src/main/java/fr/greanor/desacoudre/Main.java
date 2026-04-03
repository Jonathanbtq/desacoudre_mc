package fr.greanor.desacoudre;

import org.bukkit.plugin.java.JavaPlugin;
import fr.greanor.desacoudre.Listener.GameListener;
import fr.greanor.desacoudre.EconomyManager;
import org.bukkit.Bukkit;

public class Main extends JavaPlugin {

    private static ReturnSpawnManager returnSpawnManager;

    @Override
    public void onEnable() {
        // Initialisation du PoolManager
        PoolManager poolManager = new PoolManager(this);

        // Initialisation du GameManager (avec référence au PoolManager)
        GameManager gameManager = new GameManager(poolManager);

        // Initialisation du DuelManager
        EconomyManager economyManager = new EconomyManager();
        DuelManager duelManager = new DuelManager(gameManager, poolManager, economyManager);
//
//        // Lier le DuelManager au GameManager
//        gameManager.setDuelManager(duelManager);

        // Enregistrement de /createpool
        PoolCommands poolCommands = new PoolCommands(poolManager);
        this.getCommand("createpool").setExecutor(poolCommands);
        Bukkit.getPluginManager().registerEvents(poolCommands, this);

        // Enreguistrement de /deletepool
        this.getCommand("deletepool").setExecutor(poolCommands);

        // Enregistrement de /dac (avec accès au PoolManager)
        this.getCommand("dac").setExecutor(new DacCommand(gameManager, poolManager, duelManager));

        // Enregistrement du listener du jeu
        getServer().getPluginManager().registerEvents(
                new GameListener(gameManager),
                this
        );

        // Tâche périodique pour nettoyer les demandes de duel expirées
//        Bukkit.getScheduler().runTaskTimer(this, () -> {
//            duelManager.cleanExpiredRequests();
//        }, 0L, 1200L);

        returnSpawnManager = new ReturnSpawnManager(getDataFolder());

        getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        getLogger().info("  Plugin Dé à Coudre démarré !");
        getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin Dé à Coudre arrêté !");
    }

    public static ReturnSpawnManager getReturnSpawnManager() {
        return returnSpawnManager;
    }
}