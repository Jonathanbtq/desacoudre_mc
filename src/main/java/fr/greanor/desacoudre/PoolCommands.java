package fr.greanor.desacoudre;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;
import java.util.HashMap;
import java.util.Map;

public class PoolCommands implements CommandExecutor, Listener {

    private final PoolManager poolManager;

    // Stockage temporaire par joueur
    private final Map<Player, String> creatingPool = new HashMap<>();
    private final Map<Player, Location> firstCorner = new HashMap<>();

    public PoolCommands(PoolManager poolManager) {
        this.poolManager = poolManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cSeul un joueur peut exécuter cette commande !");
            return true;
        }

        if (command.getName().equalsIgnoreCase("createpool")) {
            if (args.length != 1) {
                player.sendMessage("§cUsage : /createpool <nom>");
                return true;
            }

            String poolName = args[0];

            if (poolManager.getPool(poolName) != null) {
                player.sendMessage("§cUne piscine avec ce nom existe déjà !");
                return true;
            }

            creatingPool.put(player, poolName);
            player.sendMessage("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§a✓ Mode création activé pour la piscine '§e" + poolName + "§a'");
            player.sendMessage("§7Clique avec un §ebâton §7sur le §apremier coin §7de la piscine");
            player.sendMessage("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            return true;
        }

        return false;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if (!creatingPool.containsKey(player)) return;

        // On veut seulement le clic avec main principale
        if (event.getHand() != EquipmentSlot.HAND) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.STICK) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        event.setCancelled(true); // Empêche l'action normale du bâton

        Location loc = clickedBlock.getLocation();
        String poolName = creatingPool.get(player);

        if (!firstCorner.containsKey(player)) {
            // Premier coin
            clickedBlock.setType(Material.LIME_WOOL);
            firstCorner.put(player, loc);
            player.sendMessage("§a✓ Premier coin enregistré à §f" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
            player.sendMessage("§7Clique maintenant sur le §adeuxième coin§7 (coin opposé)");
        } else {
            // Deuxième coin -> créer la piscine
            clickedBlock.setType(Material.LIME_WOOL);
            Location corner1 = firstCorner.get(player);
            Location corner2 = loc;

            poolManager.addPool(poolName, corner1, corner2);
            firstCorner.remove(player);
            creatingPool.remove(player);

            player.sendMessage("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§a✓ Piscine '§e" + poolName + "§a' créée avec succès !");
            player.sendMessage("§7Utilisez §e/dac setspawn §7pour définir le point de spawn");
            player.sendMessage("§7Puis §e/dac start " + poolName + " §7pour commencer");
            player.sendMessage("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        }
    }
}