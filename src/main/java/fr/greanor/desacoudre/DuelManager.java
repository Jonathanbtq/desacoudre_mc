package fr.greanor.desacoudre;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class DuelManager {

    private final GameManager gameManager;
    private final PoolManager poolManager;
    private final EconomyManager economyManager; // AJOUT de l'économie

    // Stockage des demandes de duel en attente
    private final Map<UUID, DuelRequest> pendingDuels = new HashMap<>();

    // Duel en cours
    private Duel currentDuel = null;

    // Classe interne pour une demande de duel
    public static class DuelRequest {
        public Player challenger; // Celui qui défie
        public Player target; // Celui qui est défié
        public Double bet; // Mise (peut être null)
        public long expirationTime; // Temps d'expiration

        public DuelRequest(Player challenger, Player target, Double bet) {
            this.challenger = challenger;
            this.target = target;
            this.bet = bet;
            this.expirationTime = System.currentTimeMillis() + 60000; // 60 secondes
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }

    // Classe interne pour un duel en cours
    public static class Duel {
        public Player player1;
        public Player player2;
        public Double bet;
        public String poolName;
        public Set<Player> alivePlayers;
        public Player currentPlayer;
        public List<Player> playerQueue;

        public Duel(Player p1, Player p2, Double bet, String poolName) {
            this.player1 = p1;
            this.player2 = p2;
            this.bet = bet;
            this.poolName = poolName;
            this.alivePlayers = new HashSet<>();
            this.alivePlayers.add(p1);
            this.alivePlayers.add(p2);
            this.playerQueue = new ArrayList<>();
            this.playerQueue.add(p1);
            this.playerQueue.add(p2);
        }
    }

    // CONSTRUCTEUR MODIFIÉ - Ajout de EconomyManager
    public DuelManager(GameManager gameManager, PoolManager poolManager, EconomyManager economyManager) {
        this.gameManager = gameManager;
        this.poolManager = poolManager;
        this.economyManager = economyManager;
    }

    // Créer une demande de duel
    public boolean createDuelRequest(Player challenger, Player target, Double bet) {
        // Vérifications
        if (challenger.equals(target)) {
            challenger.sendMessage("§cVous ne pouvez pas vous défier vous-même !");
            return false;
        }

        if (!target.isOnline()) {
            challenger.sendMessage("§cCe joueur n'est pas en ligne !");
            return false;
        }

        if (gameManager.isRunning()) {
            challenger.sendMessage("§cUne partie est déjà en cours !");
            return false;
        }

        if (currentDuel != null) {
            challenger.sendMessage("§cUn duel est déjà en cours !");
            return false;
        }

        // Vérifier si une demande existe déjà
        if (pendingDuels.containsKey(target.getUniqueId())) {
            challenger.sendMessage("§cCe joueur a déjà une demande de duel en attente !");
            return false;
        }

        // NOUVEAU : Vérifier les soldes si une mise est définie
        if (bet != null && bet > 0 && economyManager.isEnabled()) {
            if (!economyManager.hasEnough(challenger, bet)) {
                challenger.sendMessage("§cVous n'avez pas assez d'argent pour cette mise !");
                challenger.sendMessage("§7Solde requis : §e" + economyManager.format(bet));
                challenger.sendMessage("§7Votre solde : §e" + economyManager.format(economyManager.getBalance(challenger)));
                return false;
            }

            if (!economyManager.hasEnough(target, bet)) {
                challenger.sendMessage("§c" + target.getName() + " n'a pas assez d'argent pour cette mise !");
                return false;
            }
        }

        // Créer la demande
        DuelRequest request = new DuelRequest(challenger, target, bet);
        pendingDuels.put(target.getUniqueId(), request);

        // Messages
        if (bet != null && bet > 0) {
            target.sendMessage("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            target.sendMessage("§6⚔ §e§l" + challenger.getName() + " §6vous défie en duel !");
            target.sendMessage("§aMise : §e" + economyManager.format(bet) + " §achacun");
            target.sendMessage("§7Le gagnant remporte : §e" + economyManager.format(bet * 2));
            target.sendMessage("§7Tapez §e/dac duel accept " + challenger.getName() + " §7pour accepter");
            target.sendMessage("§7Cette demande expire dans 60 secondes");
            target.sendMessage("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            challenger.sendMessage("§aDemande de duel envoyée à §e" + target.getName() + " §a(Mise: §e" + economyManager.format(bet) + "§a)");
        } else {
            target.sendMessage("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            target.sendMessage("§6⚔ §e§l" + challenger.getName() + " §6vous défie en duel !");
            target.sendMessage("§7Tapez §e/dac duel accept " + challenger.getName() + " §7pour accepter");
            target.sendMessage("§7Cette demande expire dans 60 secondes");
            target.sendMessage("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            challenger.sendMessage("§aDemande de duel envoyée à §e" + target.getName() + " §a(Duel amical)");
        }

        return true;
    }

    // Accepter un duel
    public boolean acceptDuel(Player accepter, String challengerName) {
        DuelRequest request = pendingDuels.get(accepter.getUniqueId());

        if (request == null) {
            accepter.sendMessage("§cVous n'avez pas de demande de duel en attente !");
            return false;
        }

        if (!request.challenger.getName().equalsIgnoreCase(challengerName)) {
            accepter.sendMessage("§cCe n'est pas le bon joueur !");
            return false;
        }

        if (request.isExpired()) {
            pendingDuels.remove(accepter.getUniqueId());
            accepter.sendMessage("§cCette demande de duel a expiré !");
            return false;
        }

        // Trouver une map de tournoi disponible
        String tournamentPool = findTournamentPool();
        if (tournamentPool == null) {
            accepter.sendMessage("§cAucune map de tournoi n'est disponible !");
            request.challenger.sendMessage("§cAucune map de tournoi n'est disponible !");
            pendingDuels.remove(accepter.getUniqueId());
            return false;
        }

        // Retirer la demande
        pendingDuels.remove(accepter.getUniqueId());

        // Démarrer le duel
        startDuel(request.challenger, accepter, request.bet, tournamentPool);
        return true;
    }

    // Trouver une pool de tournoi (commence par "tournament_" ou "duel_")
    private String findTournamentPool() {
        for (String poolName : poolManager.listPools()) {
            if (poolName.toLowerCase().startsWith("tournament_") ||
                    poolName.toLowerCase().startsWith("duel_")) {
                return poolName;
            }
        }
        return null; // Aucune map de tournoi trouvée
    }

    // Démarrer le duel - autonome (ne dépend plus de GameManager)
    private void startDuel(Player p1, Player p2, Double bet, String poolName) {
        // NOUVEAU : Traiter la transaction si une mise est définie
        if (bet != null && bet > 0) {
            if (!economyManager.processDuelBet(p1, p2, bet)) {
                // Transaction échouée, annuler le duel
                p1.sendMessage("§cLe duel a été annulé (problème de transaction)");
                p2.sendMessage("§cLe duel a été annulé (problème de transaction)");
                return;
            }
        }

        currentDuel = new Duel(p1, p2, bet, poolName);

        // Téléportation des joueurs
        PoolManager.PoolData poolData = poolManager.getPoolData(poolName);
        if (poolData != null && poolData.spawnLocation != null) {
            p1.teleport(poolData.spawnLocation);
            p2.teleport(poolData.spawnLocation);
        }

        // Attribution des couleurs (rouge et bleu)
        p1.sendMessage("§aTa couleur : §cROUGE");
        p2.sendMessage("§aTa couleur : §9BLEU");

        // Annoncer le duel
        Bukkit.broadcastMessage("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Bukkit.broadcastMessage("§6§l⚔ DUEL EN COURS ⚔");
        Bukkit.broadcastMessage("§e" + p1.getName() + " §7VS §e" + p2.getName());
        if (bet != null && bet > 0) {
            Bukkit.broadcastMessage("§aMise : §e" + economyManager.format(bet) + " §7chacun");
            Bukkit.broadcastMessage("§7Gains du gagnant : §e" + economyManager.format(bet * 2));
        }
        Bukkit.broadcastMessage("§7Map : §f" + poolName);
        Bukkit.broadcastMessage("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // Démarrer le tour du premier joueur (aléatoire)
        Collections.shuffle(currentDuel.playerQueue);
        nextTurn();
    }

    // Gérer le tour du duel (1v1)
    private void nextTurn() {
        if (currentDuel == null || currentDuel.playerQueue.isEmpty()) return;
        currentDuel.currentPlayer = currentDuel.playerQueue.remove(0);
        currentDuel.playerQueue.add(currentDuel.currentPlayer);
        currentDuel.currentPlayer.sendMessage("§aC'est ton tour de jouer !");
        Bukkit.broadcastMessage("§eAu tour de §l" + currentDuel.currentPlayer.getName() + "§e !");
    }

    // Terminer le duel - MODIFIÉ avec paiement du gagnant
    public void endDuel(Player winner) {
        if (currentDuel == null) return;

        Player loser = currentDuel.player1.equals(winner) ? currentDuel.player2 : currentDuel.player1;

        Bukkit.broadcastMessage("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Bukkit.broadcastMessage("§6§l🏆 FIN DU DUEL 🏆");
        Bukkit.broadcastMessage("§aGagnant : §e§l" + winner.getName());

        if (currentDuel.bet != null && currentDuel.bet > 0) {
            double totalWinnings = currentDuel.bet * 2;

            // NOUVEAU : Donner les gains au gagnant
            economyManager.payWinner(winner, totalWinnings);

            Bukkit.broadcastMessage("§e" + winner.getName() + " §aremporte §e" + economyManager.format(totalWinnings) + " §a!");
            loser.sendMessage("§c§l- " + economyManager.format(currentDuel.bet) + " §c(Vous perdez votre mise)");
        }

        Bukkit.broadcastMessage("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        currentDuel = null;
    }

    // Annuler le duel (déconnexion, etc.) - MODIFIÉ avec remboursement
    public void cancelDuel() {
        if (currentDuel == null) return;

        Bukkit.broadcastMessage("§c⚠ Le duel a été annulé !");

        // NOUVEAU : Rembourser les mises si nécessaire
        if (currentDuel.bet != null && currentDuel.bet > 0) {
            economyManager.refundPlayers(currentDuel.player1, currentDuel.player2, currentDuel.bet);
        }

        currentDuel = null;
    }

    // Getters
    public Duel getCurrentDuel() {
        return currentDuel;
    }

    public boolean isDuelInProgress() {
        return currentDuel != null;
    }

    // Nettoyer les demandes expirées (à appeler périodiquement)
    public void cleanExpiredRequests() {
        pendingDuels.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}

