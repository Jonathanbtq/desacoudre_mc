package fr.greanor.desacoudre.Listener;

import fr.greanor.desacoudre.GameManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class GameListener implements Listener {

    private final GameManager gameManager;

    public GameListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        if (!gameManager.isRunning()) return;

        Player player = event.getPlayer();

        if (!gameManager.getAlivePlayers().contains(player)) return;

        // Vérifier si c'est le tour du joueur
        if (!player.equals(gameManager.getCurrentPlayer())) {
            // Ce n'est pas le tour de ce joueur, on ne fait rien
            return;
        }

        // Vérifier si le joueur est dans la zone de la piscine
        if (!gameManager.isInPoolZone(player.getLocation())) return;

        // Bloc où se trouve le joueur (pas en dessous)
        Block blockAtPlayer = player.getLocation().getBlock();
        Material playerWool = gameManager.getColor(player);

        // Le joueur touche l'eau pour la première fois (il est DANS l'eau)
        if (blockAtPlayer.getType() == Material.WATER && playerWool != null && !gameManager.isInWater(player)) {
            // Marquer le joueur comme étant dans l'eau
            gameManager.setInWater(player, true);

            // Placer la laine à l'endroit où le joueur a touché l'eau
            blockAtPlayer.setType(playerWool);

            player.sendMessage("§a§l💧 SPLASH ! §aTon bloc a été placé.");

            // Notifier le GameManager que le joueur a terminé son saut
            gameManager.onPlayerJumped(player);

            // Réinitialiser le flag
            gameManager.setInWater(player, false);
            return;
        }

        // Élimination si le joueur touche de la laine (bloc déjà pris)
        if (blockAtPlayer.getType().name().endsWith("_WOOL")) {
            gameManager.eliminatePlayer(player);
        }
    }
}