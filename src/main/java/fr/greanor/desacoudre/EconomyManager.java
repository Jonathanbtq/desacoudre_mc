package fr.greanor.desacoudre;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {
    private Economy economy = null;
    private boolean enabled = false;

    public EconomyManager() {
        setupEconomy();
    }

    // Initialiser Vault
    private boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            Bukkit.getLogger().warning("Vault n'est pas installé ! Le système de mises est désactivé.");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager()
                .getRegistration(Economy.class);

        if (rsp == null) {
            Bukkit.getLogger().warning("Aucun plugin d'économie trouvé ! Le système de mises est désactivé.");
            return false;
        }

        economy = rsp.getProvider();
        enabled = true;
        Bukkit.getLogger().info("Système d'économie activé avec " + economy.getName());
        return true;
    }

    // Vérifier si l'économie est activée
    public boolean isEnabled() {
        return enabled && economy != null;
    }

    // Vérifier si un joueur a assez d'argent
    public boolean hasEnough(Player player, double amount) {
        if (!isEnabled()) return true; // Si pas d'économie, on autorise
        return economy.has(player, amount);
    }

    // Retirer de l'argent à un joueur
    public boolean withdraw(Player player, double amount) {
        if (!isEnabled()) return true;

        if (!hasEnough(player, amount)) {
            player.sendMessage("§cVous n'avez pas assez d'argent ! (§e" + format(amount) + "§c nécessaires)");
            return false;
        }

        economy.withdrawPlayer(player, amount);
        return true;
    }

    // Donner de l'argent à un joueur
    public boolean deposit(Player player, double amount) {
        if (!isEnabled()) return true;

        economy.depositPlayer(player, amount);
        return true;
    }

    // Obtenir le solde d'un joueur
    public double getBalance(Player player) {
        if (!isEnabled()) return 0;
        return economy.getBalance(player);
    }

    // Formater l'argent avec le symbole monétaire
    public String format(double amount) {
        if (!isEnabled()) return String.valueOf(amount);
        return economy.format(amount);
    }

    // Gérer une transaction de duel (retirer les mises)
    public boolean processDuelBet(Player p1, Player p2, double bet) {
        if (!isEnabled()) {
            // Mode simulation sans économie
            p1.sendMessage("§7[Mode simulation] Mise de " + bet + " retirée");
            p2.sendMessage("§7[Mode simulation] Mise de " + bet + " retirée");
            return true;
        }

        // Vérifier que les deux joueurs ont assez
        if (!hasEnough(p1, bet)) {
            p1.sendMessage("§cVous n'avez pas assez d'argent pour cette mise !");
            p2.sendMessage("§c" + p1.getName() + " n'a pas assez d'argent !");
            return false;
        }

        if (!hasEnough(p2, bet)) {
            p2.sendMessage("§cVous n'avez pas assez d'argent pour cette mise !");
            p1.sendMessage("§c" + p2.getName() + " n'a pas assez d'argent !");
            return false;
        }

        // Retirer les mises
        withdraw(p1, bet);
        withdraw(p2, bet);

        p1.sendMessage("§7Mise de §e" + format(bet) + " §7retirée");
        p2.sendMessage("§7Mise de §e" + format(bet) + " §7retirée");

        return true;
    }

    // Donner les gains au gagnant
    public void payWinner(Player winner, double totalPrize) {
        if (!isEnabled()) {
            winner.sendMessage("§7[Mode simulation] Vous gagnez " + totalPrize);
            return;
        }

        deposit(winner, totalPrize);
        winner.sendMessage("§a§l+ " + format(totalPrize) + " §a!");
    }

    // Rembourser les deux joueurs (annulation)
    public void refundPlayers(Player p1, Player p2, double bet) {
        if (!isEnabled()) {
            p1.sendMessage("§7[Mode simulation] Remboursement de " + bet);
            p2.sendMessage("§7[Mode simulation] Remboursement de " + bet);
            return;
        }

        deposit(p1, bet);
        deposit(p2, bet);

        p1.sendMessage("§aVotre mise de §e" + format(bet) + " §avous a été remboursée");
        p2.sendMessage("§aVotre mise de §e" + format(bet) + " §avous a été remboursée");
    }
}
