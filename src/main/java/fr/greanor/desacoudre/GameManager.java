package fr.greanor.desacoudre;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;

import org.bukkit.Material;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class GameManager {

    private boolean running = false;
    private Location spawnLocation; // Spawn global (spectateurs)
    private Location divingBoardLocation; // Position du plongeoir
    private String currentPoolName; // Nom de la piscine active
    private final PoolManager poolManager;

    private final Map<Player, Material> playerColors = new HashMap<>();
    private final Map<Player, Boolean> playersInWater = new HashMap<>();

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

    private final List<Player> playerQueue = new ArrayList<>(); // File d'attente des joueurs
    private Player currentPlayer = null; // Joueur qui doit sauter
    private final Set<Player> alivePlayers = new HashSet<>();

    public GameManager(PoolManager poolManager) {
        this.poolManager = poolManager;
    }

    public void startGame(String poolName) {
        PoolManager.PoolData poolData = poolManager.getPoolData(poolName);

        if (poolData == null) {
            Bukkit.broadcastMessage("§c⚠ La piscine '" + poolName + "' n'existe pas !");
            return;
        }

        // Charger le spawn et le diving de la piscine si disponibles
        Location poolSpawn = poolData.spawnLocation;
        Location poolDiving = poolData.divingBoardLocation;

        // Si la piscine a ses propres positions, les utiliser
        if (poolSpawn != null) {
            spawnLocation = poolSpawn;
        }
        if (poolDiving != null) {
            divingBoardLocation = poolDiving;
        }

        // Vérification finale
        if (spawnLocation == null) {
            Bukkit.broadcastMessage("§c⚠ Le point de spawn n'est pas défini pour cette piscine !");
            Bukkit.broadcastMessage("§7Utilisez /dac setspawn <nom_piscine>");
            return;
        }

        if (divingBoardLocation == null) {
            Bukkit.broadcastMessage("§c⚠ Le plongeoir n'est pas défini pour cette piscine !");
            Bukkit.broadcastMessage("§7Utilisez /dac setdiving <nom_piscine>");
            return;
        }

        if (running) {
            Bukkit.broadcastMessage("§c⚠ Le jeu est déjà en cours !");
            return;
        }

        alivePlayers.clear();
        alivePlayers.addAll(Bukkit.getOnlinePlayers());

        if (alivePlayers.size() < 1) {
            Bukkit.broadcastMessage("§cIl faut au moins 2 joueurs pour démarrer !");
            return;
        }

        running = true;
        currentPoolName = poolName;

        // Initialiser la file d'attente
        playerQueue.clear();
        playerQueue.addAll(alivePlayers);

        // Téléporter tous les joueurs au spawn global
        for (Player p : alivePlayers) {
            p.teleport(spawnLocation);
        }

        assignColors();
        Bukkit.broadcastMessage("§a✓ Couleurs assignées aux joueurs !");

        Bukkit.broadcastMessage("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Bukkit.broadcastMessage("§6§l    DÉ À COUDRE DÉMARRÉ !");
        Bukkit.broadcastMessage("§aPiscine : §f" + poolName);
        Bukkit.broadcastMessage("§aJoueurs en vie : §e" + alivePlayers.size());
        Bukkit.broadcastMessage("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // Lancer le premier tour
        nextTurn();
    }

    private void assignColors() {
        int index = 0;
        for (Player p : alivePlayers) {
            if (index >= woolColors.size()) break;
            playerColors.put(p, woolColors.get(index));
            p.sendMessage("§aTa couleur : §f" + woolColors.get(index).name());
            index++;
        }
    }

    public void eliminatePlayer(Player player) {
        if (!alivePlayers.contains(player)) return;

        alivePlayers.remove(player);
        playerQueue.remove(player); // Retirer de la file d'attente

        player.sendMessage("§c§l✗ TU ES ÉLIMINÉ !");
        player.teleport(spawnLocation); // Le mettre au spawn avec les spectateurs

        Bukkit.broadcastMessage("§e⚡ " + player.getName() + " a été éliminé ! §7(" + alivePlayers.size() + " restants)");

        // Si c'était le tour du joueur éliminé, passer au suivant
        if (player.equals(currentPlayer)) {
            nextTurn();
        }

        if (alivePlayers.isEmpty()) {
            Bukkit.broadcastMessage("§c§lPlus aucun joueur en vie !");
            endGame();
        } else if (alivePlayers.size() == 1) {
            checkVictory();
        }
    }

    private void checkVictory() {
        if (alivePlayers.size() == 1) {
            Player winner = alivePlayers.iterator().next();
            Bukkit.broadcastMessage("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            Bukkit.broadcastMessage("§6§l    🏆 VICTOIRE 🏆");
            Bukkit.broadcastMessage("§aLe gagnant est §e§l" + winner.getName() + "§a !");
            Bukkit.broadcastMessage("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            endGame();
        }
    }

    private void endGame() {
        resetPool();
        alivePlayers.clear();
        playerColors.clear();
        playersInWater.clear();
        playerQueue.clear();
        currentPlayer = null;
        running = false;

        if (spawnLocation != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.teleport(spawnLocation);
            }
        }

        Bukkit.broadcastMessage("§7La partie est terminée. Prêt pour un nouveau round !");
    }

    private void resetPool() {
        if (currentPoolName == null) return;

        Location[] pool = poolManager.getPool(currentPoolName);
        if (pool == null) return;

        Location corner1 = pool[0];
        Location corner2 = pool[1];

        int xMin = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int yMin = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int zMin = Math.min(corner1.getBlockZ(), corner2.getBlockZ());

        int xMax = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int yMax = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int zMax = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    Block block = corner1.getWorld().getBlockAt(x, y, z);
                    block.setType(Material.WATER);
                }
            }
        }

        Bukkit.broadcastMessage("§a✓ La piscine a été réinitialisée !");
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

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setDivingBoardLocation(Location loc) {
        this.divingBoardLocation = loc;
    }

    public Location getDivingBoardLocation() {
        return divingBoardLocation;
    }

    // Définir le spawn pour une piscine spécifique
    public void setPoolSpawn(String poolName, Location loc) {
        poolManager.setPoolSpawn(poolName, loc);
    }

    // Définir le diving pour une piscine spécifique
    public void setPoolDiving(String poolName, Location loc) {
        poolManager.setPoolDiving(poolName, loc);
    }

    public boolean isInWater(Player player) {
        return playersInWater.getOrDefault(player, false);
    }

    public void setInWater(Player player, boolean inWater) {
        playersInWater.put(player, inWater);
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    // Passer au tour suivant
    public void nextTurn() {
        if (!running || playerQueue.isEmpty()) return;

        // Prendre le prochain joueur
        currentPlayer = playerQueue.remove(0);

        // Le remettre à la fin de la file
        playerQueue.add(currentPlayer);

        // Téléporter le joueur au plongeoir
        currentPlayer.teleport(divingBoardLocation);

        // Annoncer le tour
        Bukkit.broadcastMessage("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Bukkit.broadcastMessage("§6⚡ C'est au tour de §e§l" + currentPlayer.getName() + " §6!");
        Bukkit.broadcastMessage("§7Couleur : §f" + playerColors.get(currentPlayer).name());
        Bukkit.broadcastMessage("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        currentPlayer.sendMessage("§a§l➤ C'EST TON TOUR ! SAUTE DANS LA PISCINE !");
    }

    // Appelé quand un joueur a terminé son saut
    public void onPlayerJumped(Player player) {
        if (player.equals(currentPlayer)) {
            // Téléporter le joueur au spawn global
            player.teleport(spawnLocation);

            // Passer au tour suivant après un court délai
            Bukkit.getScheduler().runTaskLater(
                    Bukkit.getPluginManager().getPlugin("DesACoudre"),
                    this::nextTurn,
                    20L // 1 seconde
            );
        }
    }

    // Vérifie si le joueur est dans la zone de la piscine active
    public boolean isInPoolZone(Location location) {
        if (currentPoolName == null) return false;

        Location[] pool = poolManager.getPool(currentPoolName);
        if (pool == null) return false;

        Location corner1 = pool[0];
        Location corner2 = pool[1];

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        int xMin = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int xMax = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int yMin = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int yMax = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int zMin = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int zMax = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

        return x >= xMin && x <= xMax &&
                y >= yMin && y <= yMax &&
                z >= zMin && z <= zMax;
    }
}