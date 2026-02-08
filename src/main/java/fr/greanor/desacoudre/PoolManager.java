package fr.greanor.desacoudre;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PoolManager {

    private final File file;
    private final FileConfiguration config;
    private final Map<String, Location[]> pools = new HashMap<>();

    public PoolManager(JavaPlugin plugin) {
        file = new File(plugin.getDataFolder(), "pools.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        loadPools();
    }

    private void loadPools() {
        if (config.contains("pools")) {
            for (String key : config.getConfigurationSection("pools").getKeys(false)) {
                String c1 = config.getString("pools." + key + ".corner1");
                String c2 = config.getString("pools." + key + ".corner2");
                pools.put(key, new Location[]{stringToLocation(c1), stringToLocation(c2)});
            }
        }
    }

    public void savePools() {
        for (Map.Entry<String, Location[]> entry : pools.entrySet()) {
            config.set("pools." + entry.getKey() + ".corner1", locationToString(entry.getValue()[0]));
            config.set("pools." + entry.getKey() + ".corner2", locationToString(entry.getValue()[1]));
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addPool(String name, Location corner1, Location corner2) {
        pools.put(name, new Location[]{corner1, corner2});
        savePools();
    }

    public Location[] getPool(String name) {
        return pools.get(name);
    }

    public Set<String> listPools() {
        return pools.keySet();
    }

    // Convertir Location <-> String
    private String locationToString(Location loc) {
        return loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + "," + loc.getWorld().getName();
    }

    private Location stringToLocation(String s) {
        String[] parts = s.split(",");
        World world = Bukkit.getWorld(parts[3]);
        return new Location(world, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }
}
