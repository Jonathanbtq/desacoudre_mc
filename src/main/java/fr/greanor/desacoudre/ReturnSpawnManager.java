package fr.greanor.desacoudre;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ReturnSpawnManager {
    private final File file;
    private Location returnSpawn;

    public ReturnSpawnManager(File dataFolder) {
        this.file = new File(dataFolder, "returnspawn.yml");
        load();
    }

    public void setReturnSpawn(Location loc) {
        this.returnSpawn = loc;
        save();
    }

    public Location getReturnSpawn() {
        return returnSpawn;
    }

    private void load() {
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String worldName = config.getString("world");
        double x = config.getDouble("x");
        double y = config.getDouble("y");
        double z = config.getDouble("z");
        float yaw = (float) config.getDouble("yaw", 0);
        float pitch = (float) config.getDouble("pitch", 0);
        if (worldName != null) {
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                returnSpawn = new Location(world, x, y, z, yaw, pitch);
            }
        }
    }

    private void save() {
        if (returnSpawn == null) return;
        FileConfiguration config = new YamlConfiguration();
        config.set("world", returnSpawn.getWorld().getName());
        config.set("x", returnSpawn.getX());
        config.set("y", returnSpawn.getY());
        config.set("z", returnSpawn.getZ());
        config.set("yaw", returnSpawn.getYaw());
        config.set("pitch", returnSpawn.getPitch());
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Ajout : gestion des statistiques de gains/pertes
    public void addWin(String playerName, double gain) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String key = "stats." + playerName + ".wins";
        String gainKey = "stats." + playerName + ".gains";
        config.set(key, config.getInt(key, 0) + 1);
        config.set(gainKey, config.getDouble(gainKey, 0.0) + gain);
        saveStats(config);
    }

    public void addLoss(String playerName, double loss) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String key = "stats." + playerName + ".losses";
        String lossKey = "stats." + playerName + ".lossesAmount";
        config.set(key, config.getInt(key, 0) + 1);
        config.set(lossKey, config.getDouble(lossKey, 0.0) + loss);
        saveStats(config);
    }

    public int getWins(String playerName) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config.getInt("stats." + playerName + ".wins", 0);
    }
    public int getLosses(String playerName) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config.getInt("stats." + playerName + ".losses", 0);
    }
    public double getTotalGains(String playerName) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config.getDouble("stats." + playerName + ".gains", 0.0);
    }
    public double getTotalLosses(String playerName) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config.getDouble("stats." + playerName + ".lossesAmount", 0.0);
    }

    private void saveStats(FileConfiguration config) {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
