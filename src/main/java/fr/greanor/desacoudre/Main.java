package fr.greanor.desacoudre;

import org.bukkit.plugin.java.JavaPlugin;
import fr.greanor.desacoudre.Listener.GameListener;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        //Initialisation du GameManager
        GameManager gameManager = new GameManager();

        // Enregistrement de la commande /dac
        this.getCommand("dac").setExecutor(new DacCommand(gameManager));

        getServer().getPluginManager().registerEvents(
            new GameListener(gameManager),
            this
        );

        getLogger().info("Plugin Dé à Coudre démarré !");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin Dé à Coudre arrêté !");
    }
}
