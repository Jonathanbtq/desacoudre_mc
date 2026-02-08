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
    private final Map<String, PoolData> pools = new HashMap<>();

    // Classe interne pour stocker les données d'une piscine
    public static class PoolData {
        public Location corner1;
        public Location corner2;
        public Location spawnLocation;
        public Location divingBoardLocation;

        public PoolData(Location corner1, Location corner2) {
            this.corner1 = corner1;
            this.corner2 = corner2;
        }

        public PoolData(Location corner1, Location corner2, Location spawn, Location diving) {
            this.corner1 = corner1;
            this.corner2 = corner2;
            this.spawnLocation = spawn;
            this.divingBoardLocation = diving;
        }

        public Location[] getCorners() {
            return new Location[]{corner1, corner2};
        }
    }

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
                String spawn = config.getString("pools." + key + ".spawn");
                String diving = config.getString("pools." + key + ".diving");

                Location corner1 = stringToLocation(c1);
                Location corner2 = stringToLocation(c2);
                Location spawnLoc = spawn != null ? stringToLocation(spawn) : null;
                Location divingLoc = diving != null ? stringToLocation(diving) : null;

                pools.put(key, new PoolData(corner1, corner2, spawnLoc, divingLoc));
            }
        }
    }

    public void savePools() {
        for (Map.Entry<String, PoolData> entry : pools.entrySet()) {
            String poolName = entry.getKey();
            PoolData data = entry.getValue();

            config.set("pools." + poolName + ".corner1", locationToString(data.corner1));
            config.set("pools." + poolName + ".corner2", locationToString(data.corner2));

            if (data.spawnLocation != null) {
                config.set("pools." + poolName + ".spawn", locationToString(data.spawnLocation));
            }

            if (data.divingBoardLocation != null) {
                config.set("pools." + poolName + ".diving", locationToString(data.divingBoardLocation));
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addPool(String name, Location corner1, Location corner2) {
        pools.put(name, new PoolData(corner1, corner2));
        savePools();
    }

    public Location[] getPool(String name) {
        PoolData data = pools.get(name);
        return data != null ? data.getCorners() : null;
    }

    public PoolData getPoolData(String name) {
        return pools.get(name);
    }

    public void setPoolSpawn(String name, Location spawn) {
        PoolData data = pools.get(name);
        if (data != null) {
            data.spawnLocation = spawn;
            savePools();
        }
    }

    public void setPoolDiving(String name, Location diving) {
        PoolData data = pools.get(name);
        if (data != null) {
            data.divingBoardLocation = diving;
            savePools();
        }
    }

    public Set<String> listPools() {
        return pools.keySet();
    }

    // Convertir Location <-> String
    private String locationToString(Location loc) {
        return loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ","
                + loc.getWorld().getName() + "," + loc.getYaw() + "," + loc.getPitch();
    }

    private Location stringToLocation(String s) {
        String[] parts = s.split(",");
        World world = Bukkit.getWorld(parts[3]);

        Location loc = new Location(world,
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]));

        // Si yaw et pitch sont présents, les ajouter
        if (parts.length >= 6) {
            loc.setYaw(Float.parseFloat(parts[4]));
            loc.setPitch(Float.parseFloat(parts[5]));
        }

        return loc;
    }
}