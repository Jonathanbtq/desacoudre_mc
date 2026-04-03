package fr.greanor.desacoudre;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Bukkit;

public class DacCommand implements CommandExecutor {

    private final GameManager gameManager;
    private final PoolManager poolManager;
    private final DuelManager duelManager;

    public DacCommand(GameManager gameManager, PoolManager poolManager, DuelManager duelManager) {
        this.gameManager = gameManager;
        this.poolManager = poolManager;
        this.duelManager = duelManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cSeuls les joueurs peuvent utiliser cette commande.");
            return true;
        }

        if (args.length == 0) {
            // Afficher la page 1 par défaut
            showHelpPage(player, 1);
            return true;
        }

        // /dac help [page]
        if (args[0].equalsIgnoreCase("help")) {
            int page = 1;
            if (args.length >= 2) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cNuméro de page invalide !");
                    return true;
                }
            }
            showHelpPage(player, page);
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

        if (args[0].equalsIgnoreCase("tp")) {
            if (args.length < 2) {
                player.sendMessage("§cUsage : /dac tp <nom_piscine>");
                player.sendMessage("§7Piscines disponibles : " + String.join(", ", poolManager.listPools()));
                return true;
            }

            String poolName = args[1];
            if (poolManager.getPool(poolName) == null) {
                player.sendMessage("§cLa piscine '" + poolName + "' n'existe pas !");
                return true;
            }

            Location spawn = poolManager.getPoolData(poolName).spawnLocation;
            if (spawn == null) {
                player.sendMessage("§cLe spawn de la piscine '" + poolName + "' n'est pas défini !");
                return true;
            }

            player.teleport(spawn);
            player.sendMessage("§a✓ Téléporté au spawn de la piscine '§e" + poolName + "§a' !");
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
            if (!player.isOp() && !player.hasPermission("dac.admin")) {
                player.sendMessage("§cVous n'avez pas la permission de lancer une partie.");
                return true;
            }
            if (args.length < 2) {
                player.sendMessage("§cUsage : /dac start <nom_piscine>");
                player.sendMessage("§7Piscines disponibles : " + String.join(", ", poolManager.listPools()));
                return true;
            }
            String poolName = args[1];
            Bukkit.broadcastMessage("§e[Dé à Coudre] §aUne nouvelle partie commence ! Faites §e/dac join §apour participer !");
            gameManager.startGame(poolName);
            return true;
        }

        //Dac stop partie
        if (args[0].equalsIgnoreCase("stop")) {
            if (args.length < 2) {
                player.sendMessage("§cUsage : /dac stop <nom_piscine>");
                return true;
            }

            gameManager.stopParty(args[1]);
            player.sendMessage("§cPartie arrêtée !");
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

        // /dac duel <joueur> [mise]
        if (args[0].equalsIgnoreCase("duel")) {
            if (args.length < 2) {
                player.sendMessage("§cUsage : /dac duel <joueur> [mise]");
                player.sendMessage("§7Exemple : /dac duel Steve 100");
                return true;
            }

            // /dac duel accept <joueur>
            if (args[1].equalsIgnoreCase("accept")) {
                if (args.length < 3) {
                    player.sendMessage("§cUsage : /dac duel accept <joueur>");
                    return true;
                }

                String challengerName = args[2];
                duelManager.acceptDuel(player, challengerName);
                return true;
            }

            // /dac duel <joueur> [mise]
            String targetName = args[1];
            Player target = Bukkit.getPlayer(targetName);

            if (target == null) {
                player.sendMessage("§cJoueur introuvable !");
                return true;
            }

            Double bet = null;
            if (args.length >= 3) {
                try {
                    bet = Double.parseDouble(args[2]);
                    if (bet <= 0) {
                        player.sendMessage("§cLa mise doit être positive !");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("§cMise invalide !");
                    return true;
                }
            }

            duelManager.createDuelRequest(player, target, bet);
            return true;
        }

        // /dac join
        if (args[0].equalsIgnoreCase("join")) {
            if (gameManager.isWaitingForPlayers()) {
                boolean joined = gameManager.joinGame(player);
                if (joined) {
                    player.sendMessage("§aVous avez rejoint la partie !");
                }
            } else {
                player.sendMessage("§cAucune partie n'est en attente de joueurs actuellement.");
            }
            return true;
        }

        // /dac party start
        if (args.length >= 2 && args[0].equalsIgnoreCase("party") && args[1].equalsIgnoreCase("start")) {
            if (!player.isOp() && !player.hasPermission("dac.admin")) {
                player.sendMessage("§cVous n'avez pas la permission de lancer la partie.");
                return true;
            }
            boolean launched = gameManager.launchGameByAdmin();
            if (launched) {
                player.sendMessage("§aLa partie a été lancée !");
            }
            return true;
        }

        // /dac setreturnspawn
        if (args[0].equalsIgnoreCase("setreturnspawn")) {
            if (!player.isOp() && !player.hasPermission("dac.admin")) {
                player.sendMessage("§cVous n'avez pas la permission de définir le point de retour.");
                return true;
            }
            ReturnSpawnManager returnSpawnManager = Main.getReturnSpawnManager();
            returnSpawnManager.setReturnSpawn(player.getLocation());
            player.sendMessage("§aPoint de retour défini à votre position actuelle !");
            return true;
        }

        player.sendMessage("§cCommande inconnue. Utilisez /dac pour voir l'aide.");
        return true;
    }

    // Afficher une page d'aide spécifique
    private void showHelpPage(Player player, int page) {
        int totalPages = 3; // Nombre total de pages

        if (page < 1 || page > totalPages) {
            player.sendMessage("§cPage invalide ! Pages disponibles : 1-" + totalPages);
            return;
        }

        player.sendMessage("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§6§l  DÉ À COUDRE §7- Page " + page + "/" + totalPages);
        player.sendMessage("");

        switch (page) {
            case 1: // Page 1 : Commandes de base
                player.sendMessage("§6§l► Gestion des piscines");
                player.sendMessage("§e/createpool <nom> §7- Crée une piscine");
                player.sendMessage("§e/deletepool <nom> §7- Supprime une piscine");
                player.sendMessage("§e/dac listpools §7- Liste les piscines");
                player.sendMessage("§e/dac tp <nom> §7- TP au spawn de la piscine");
                player.sendMessage("");
                player.sendMessage("§6§l► Configuration");
                player.sendMessage("§e/dac setspawn <piscine> §7- Définit le spawn");
                player.sendMessage("§e/dac setdiving <piscine> §7- Définit le plongeoir");
                break;

            case 2: // Page 2 : Jeu et duels
                player.sendMessage("§6§l► Parties normales");
                player.sendMessage("§e/dac start <piscine> §7- Démarre une partie");
                player.sendMessage("");
                player.sendMessage("§6§l► Système de duels");
                player.sendMessage("§e/dac duel <joueur> §7- Duel amical");
                player.sendMessage("§e/dac duel <joueur> <mise> §7- Duel avec mise");
                player.sendMessage("§e/dac duel accept <joueur> §7- Accepte un duel");
                player.sendMessage("");
                player.sendMessage("§7Les duels se jouent sur des maps");
                player.sendMessage("§7préfixées §etournament_§7 ou §eduel_");
                break;

            case 3: // Page 3 : Informations et astuces
                player.sendMessage("§6§l► Informations");
                player.sendMessage("§7• Les piscines sont sauvegardées");
                player.sendMessage("§7  automatiquement dans pools.yml");
                player.sendMessage("§7• Chaque piscine a son propre spawn");
                player.sendMessage("§7  et plongeoir sauvegardés");
                player.sendMessage("");
                player.sendMessage("§6§l► Astuces");
                player.sendMessage("§7• Utilisez un §ebâton§7 pour définir");
                player.sendMessage("§7  les coins de la piscine");
                player.sendMessage("§7• Les mises de duel doublent pour");
                player.sendMessage("§7  le gagnant (mise x2)");
                break;
        }

        player.sendMessage("");

        // Navigation
        StringBuilder navigation = new StringBuilder("§7[");
        for (int i = 1; i <= totalPages; i++) {
            if (i == page) {
                navigation.append("§e§l").append(i);
            } else {
                navigation.append("§7").append(i);
            }
            if (i < totalPages) {
                navigation.append(" §8| ");
            }
        }
        navigation.append("§7]");

        player.sendMessage(navigation.toString());

        if (page < totalPages) {
            player.sendMessage("§7Page suivante : §e/dac help " + (page + 1));
        }

        player.sendMessage("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}