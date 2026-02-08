package fr.greanor.desacoudre;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import java.util.HashMap;
import java.util.Map;
import java.util.List;


public class GameManager {

    private boolean running = false;
    private Location spawnLocation;
    private Location poolCenter;
    private int poolRadius = 10;
    private Location poolCorner1; // coin inférieur
    private Location poolCorner2; // coin supérieur

    public void setPoolCorners(Location corner1, Location corner2) {
        this.poolCorner1 = corner1;
        this.poolCorner2 = corner2;
    }

    private final Map<Player, Material> playerColors = new HashMap<>();

    private final List<Material> woolColors = List.of(
            Material.RED_WOOL,
            Material.BLUE_WOOL,
            Material.GREEN_WOOL,
            Material.YELLOW_WOOL,
            Material.PINK_WOOL,
            Material.ORANGE_WOOL,
            Material.PURPLE_WOOL,
            Material.BLACK_WOOL,
            Material.WHITE_WOOL,
            Material.CYAN_WOOL
    );

    private final Set<Player> alivePlayers = new HashSet<>();

    public void startGame() {

        if (spawnLocation == null && poolCenter == null) {
            Bukkit.broadcastMessage("§c⚠ Le point de spawn et la piscine ne sont pas définis !");
            return;
        }

        if (running) {
            Bukkit.broadcastMessage("§c⚠ Le jeu est déjà en cours !");
            return;
        }

        alivePlayers.clear();
        alivePlayers.addAll(Bukkit.getOnlinePlayers());

        if (alivePlayers.size() < 1) {
            Bukkit.broadcastMessage("Il faut au moins 1 joueurs pour démarrer !");
            return;
        }

        running = true;

        for (Player p : alivePlayers) {
            p.teleport(spawnLocation);
        }

        assignColors();
        Bukkit.broadcastMessage("§aCouleurs assignées aux joueurs !");

        Bukkit.broadcastMessage("Dé à Coudre démarré !");
        Bukkit.broadcastMessage("Joueurs en vie : §e" + alivePlayers.size());
    }

    private void assignColors() {
        int index = 0;
        for (Player p : alivePlayers) {
            if (index >= woolColors.size()) break;
            playerColors.put(p, woolColors.get(index));
            index++;
        }
    }

    public void eliminatePlayer(Player player) {
        if (!alivePlayers.contains(player)) return;

        alivePlayers.remove(player);
        player.sendMessage("§cTu es éliminé !");
        Bukkit.broadcastMessage("§e" + player.getName() + " a été éliminé !");

        // Vérifier s'il reste des joueurs
        if (alivePlayers.isEmpty()) {
            Bukkit.broadcastMessage("§cPlus aucun joueur en vie !");
            resetPool(); // réinitialiser automatiquement la piscine
            running = false;
        } else {
            checkVictory(); // vérifier victoire si 1 joueur restant
        }
    }

    private void checkVictory() {
        if (alivePlayers.size() == 1) {
            Player winner = alivePlayers.iterator().next();
            Bukkit.broadcastMessage("§6Le gagnant est §a" + winner.getName() + "§6 !");
            resetPool(); // réinitialiser la piscine après victoire
            running = false;
        }
    }

    private void endGame() {
        resetPool(); // remettre la piscine à zéro
        alivePlayers.clear();
        playerColors.clear();
        running = false;

        // Téléportation des joueurs survivants (ou tous les joueurs) au spawn
        if (spawnLocation != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.teleport(spawnLocation);
            }
        }

        Bukkit.broadcastMessage("§eLa partie est terminée. Prête pour un nouveau round !");
    }

    private void resetPool() {
        if (poolCorner1 == null || poolCorner2 == null) return;

        int xMin = Math.min(poolCorner1.getBlockX(), poolCorner2.getBlockX());
        int yMin = Math.min(poolCorner1.getBlockY(), poolCorner2.getBlockY());
        int zMin = Math.min(poolCorner1.getBlockZ(), poolCorner2.getBlockZ());

        int xMax = Math.max(poolCorner1.getBlockX(), poolCorner2.getBlockX());
        int yMax = Math.max(poolCorner1.getBlockY(), poolCorner2.getBlockY());
        int zMax = Math.max(poolCorner1.getBlockZ(), poolCorner2.getBlockZ());

        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    Block block = poolCorner1.getWorld().getBlockAt(x, y, z);
                    block.setType(Material.WATER);
                }
            }
        }

        Bukkit.broadcastMessage("§aLa piscine a été réinitialisée !");
    }

    public Material getColor(Player player) {
        return playerColors.get(player);
    }

    public boolean isRunning() {
        return running;
    }

    public Set<Player> getAlivePlayers() {
        return alivePlayers;
    }

    public void setSpawnLocation(Location loc) {
        this.spawnLocation = loc;
    }

    public void setPoolCenter(Location loc) {
        this.poolCenter = loc;
    }

    public Location getPoolCenter() {
        return poolCenter;
    }

    public int getPoolRadius() {
        return poolRadius;
    }
}