package fr.greanor.desacoudre;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DacCommand implements CommandExecutor {

    private final GameManager gameManager;
    private final PoolManager poolManager;

    public DacCommand(GameManager gameManager, PoolManager poolManager) {
        this.gameManager = gameManager;
        this.poolManager = poolManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cSeuls les joueurs peuvent utiliser cette commande.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§6§l    DÉ À COUDRE");
            player.sendMessage("§e/dac start <piscine> §7- Démarre une partie");
            player.sendMessage("§e/dac setspawn <piscine> §7- Définit le spawn de la piscine");
            player.sendMessage("§e/dac setdiving <piscine> §7- Définit le plongeoir de la piscine");
            player.sendMessage("§e/dac listpools §7- Liste les piscines");
            player.sendMessage("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            return true;
        }

        // /dac setspawn <nom_piscine>
        if (args[0].equalsIgnoreCase("setspawn")) {
            if (args.length < 2) {
                player.sendMessage("§cUsage : /dac setspawn <nom_piscine>");
                player.sendMessage("§7Piscines disponibles : " + String.join(", ", poolManager.listPools()));
                return true;
            }

            String poolName = args[1];
            if (poolManager.getPool(poolName) == null) {
                player.sendMessage("§cLa piscine '" + poolName + "' n'existe pas !");
                return true;
            }

            gameManager.setPoolSpawn(poolName, player.getLocation());
            player.sendMessage("§a✓ Spawn défini pour la piscine '§e" + poolName + "§a' !");
            return true;
        }

        // /dac setdiving <nom_piscine>
        if (args[0].equalsIgnoreCase("setdiving")) {
            if (args.length < 2) {
                player.sendMessage("§cUsage : /dac setdiving <nom_piscine>");
                player.sendMessage("§7Piscines disponibles : " + String.join(", ", poolManager.listPools()));
                return true;
            }

            String poolName = args[1];
            if (poolManager.getPool(poolName) == null) {
                player.sendMessage("§cLa piscine '" + poolName + "' n'existe pas !");
                return true;
            }

            gameManager.setPoolDiving(poolName, player.getLocation());
            player.sendMessage("§a✓ Plongeoir défini pour la piscine '§e" + poolName + "§a' !");
            return true;
        }

        // /dac start <nom_piscine>
        if (args[0].equalsIgnoreCase("start")) {
            if (args.length < 2) {
                player.sendMessage("§cUsage : /dac start <nom_piscine>");
                player.sendMessage("§7Piscines disponibles : " + String.join(", ", poolManager.listPools()));
                return true;
            }

            String poolName = args[1];
            gameManager.startGame(poolName);
            return true;
        }

        // /dac listpools
        if (args[0].equalsIgnoreCase("listpools")) {
            if (poolManager.listPools().isEmpty()) {
                player.sendMessage("§cAucune piscine créée. Utilisez /createpool <nom>");
            } else {
                player.sendMessage("§aPiscines disponibles :");
                for (String poolName : poolManager.listPools()) {
                    player.sendMessage("  §7- §e" + poolName);
                }
            }
            return true;
        }

        player.sendMessage("§cCommande inconnue. Utilisez /dac pour voir l'aide.");
        return true;
    }
}