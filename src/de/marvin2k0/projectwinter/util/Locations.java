package de.marvin2k0.projectwinter.util;

import de.marvin2k0.projectwinter.ProjectWinter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class Locations
{
    private static FileConfiguration config;
    private static ProjectWinter plugin;

    public static Location get(String path)
    {
        plugin.reloadGameConfig();

        World world = Bukkit.getWorld(config.getString(path + ".world"));
        double y = config.getDouble(path + ".y");
        double x = config.getDouble(path + ".x");
        double z = config.getDouble(path + ".z");
        double yaw = config.getDouble(path + ".yaw");
        double pitch = config.getDouble(path + ".pitch");

        return new Location(world, x, y, z, (float) yaw, (float) pitch);
    }

    public static void setLocation(String path, Location location)
    {
        if (config == null)
        {
            System.out.println("cnfig istn ull");
        }
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());

        plugin.saveGameConfig();
    }

    public static void setUp(ProjectWinter plugin)
    {
        Locations.plugin = plugin;
        config = plugin.getGameConfig();
    }
}
