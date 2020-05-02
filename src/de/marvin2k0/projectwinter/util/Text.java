package de.marvin2k0.projectwinter.util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class Text
{
    static FileConfiguration config;
    static Plugin plugin;

    public static String get(String path)
    {
        return path.equalsIgnoreCase("prefix") ? get(path, false) : get(path, true);
    }

    public static String get(String path, boolean prefix)
    {
        return ChatColor.translateAlternateColorCodes('&', prefix ? config.getString("prefix") + " " + config.getString(path) : config.getString(path));
    }

    public static void setUp(Plugin plugin)
    {
        Text.plugin = plugin;
        Text.config = plugin.getConfig();

        config.options().copyDefaults(true);
        config.addDefault("prefix", "&7[&bProjectWinter&7]");
        config.addDefault("noplayer", "&cOnly players can execute this command");
        config.addDefault("welcome", "&7Welcome to &bProject Winter!");
        config.addDefault("gameleave", "&7You left the game!");
        config.addDefault("notingame", "&7You are not in a game!");
        config.addDefault("loading", "&7Loading..");
        config.addDefault("nonum", "&cOnly enter numbers!");
        config.addDefault("setspawn", "&7Entry point set for game %game%");
        config.addDefault("alreadyingame", "&7You are already in a game!");
        config.addDefault("chatradius", 20);
        config.addDefault("mapsize", 100);

        saveConfig();
    }

    private static void saveConfig()
    {
        plugin.saveConfig();
    }
}
