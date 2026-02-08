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

        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;

        // Vérifier que c'est dans la piscine
        Location pool = gameManager.getPoolCenter();
        if (pool == null) return;

        double distance = player.getLocation().distance(pool);
        if (distance > gameManager.getPoolRadius()) return;

        // Placer la laine de sa couleur sous le joueur si le bloc est de l'eau
        Block blockBelow = player.getLocation().subtract(0, 1, 0).getBlock();
        Material playerWool = gameManager.getColor(player);

        if (blockBelow.getType() == Material.WATER && playerWool != null) {
            blockBelow.setType(playerWool);
            player.sendMessage("§aSplash ! Bloc de ta couleur placé.");
        }

        // Élimination si le joueur marche sur une laine (la sienne ou celle d'un autre)
        if (blockBelow.getType().name().endsWith("_WOOL")) {
            gameManager.eliminatePlayer(player);
        }
    }
}
