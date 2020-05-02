package de.marvin2k0.projectwinter;

import de.marvin2k0.projectwinter.biome.BiomeEdit;
import de.marvin2k0.projectwinter.game.Game;
import de.marvin2k0.projectwinter.game.GamePlayer;
import de.marvin2k0.projectwinter.listener.*;
import de.marvin2k0.projectwinter.util.Locations;
import de.marvin2k0.projectwinter.util.Text;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ProjectWinter extends JavaPlugin
{
    public static HashMap<Player, GamePlayer> gamePlayers = new HashMap<>();
    public static ProjectWinter instance;

    private FileConfiguration gameConfig;
    private File gameFile;

    @Override
    public void onEnable()
    {
        gameFile = new File(getDataFolder().getPath() + "/games.yml");
        gameConfig = YamlConfiguration.loadConfiguration(gameFile);

        Text.setUp(this);
        Locations.setUp(this);
        addGames();

        instance = this;

        Bukkit.getConsoleSender().sendMessage(Text.get("prefix"));

        getServer().getPluginManager().registerEvents(new WeatherListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        getServer().getPluginManager().registerEvents(new SignListener(), this);
        getServer().getPluginManager().registerEvents(new CancelEvents(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);

        getCommand("projectwinter").setExecutor(this);
    }

    private void addGames()
    {
        Map<String, Object> section = getGameConfig().getConfigurationSection("").getValues(false);

        for (Map.Entry<String, Object> entry : section.entrySet())
        {
            Game.getGame(entry.getKey());
            System.out.println("added game " + entry.getKey());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(Text.get("noplayer"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0)
        {
            player.sendMessage("§cInvalid command!");
            return true;
        }

        if (args[0].equalsIgnoreCase("create") && player.hasPermission("pw.create"))
        {
            World world = null;

            if (Bukkit.getWorld("projectwinter") == null)
            {
                BiomeEdit.changeBiome("COLD_TAIGA");
                WorldCreator worldCreator = new WorldCreator("projectwinter");
                worldCreator.type(WorldType.LARGE_BIOMES);
                player.sendMessage(Text.get("loading"));
                world = worldCreator.createWorld();
                world.setMonsterSpawnLimit(0);
                world.setAnimalSpawnLimit(0);
            }
            else
            {
                world = Bukkit.getWorld("projectwinter");
            }

            Location loc = world.getHighestBlockAt(world.getSpawnLocation()).getLocation();
            player.teleport(loc);
            player.sendMessage(Text.get("welcome"));
            return true;
        }

        else if (args[0].equalsIgnoreCase("loot") && player.hasPermission("pw.loot"))
        {
            if (args.length != 2)
            {
                player.sendMessage("§cUsage: /" + label + " <loot> <radius>");
                return true;
            }

            int radius = 50;

            try
            {
                radius = Integer.valueOf(args[1]);
            }
            catch (NumberFormatException e)
            {
                player.sendMessage(Text.get("nonum"));
            }

            Location loc = player.getLocation();
            Random random = new Random();

            double minX = loc.getX() - radius;
            double maxX = loc.getX() + radius;
            double minZ = loc.getZ() - radius;
            double maxZ = loc.getZ() + radius;

            int amount = 0;

            for (double i = minX; i <= maxX; i++)
            {
                for (double j = minZ; j <= maxZ; j++)
                {
                    if (random.nextInt(2000) <= 4)
                    {
                        Location l = new Location(loc.getWorld(), i, loc.getWorld().getHighestBlockYAt((int) i, (int) j), j);
                        loc.getWorld().getBlockAt(l).setType(Material.CHEST);
                        amount++;
                    }
                }
            }

            System.out.println("changed " + amount + " blocks to chest");
        }

        else if (args[0].equalsIgnoreCase("reset") && player.hasPermission("pw.reset"))
        {
            if (args.length != 2)
            {
                player.sendMessage("§cUsage: /" + label + " <loot> <radius>");
                return true;
            }

            int radius = 50;

            try
            {
                radius = Integer.valueOf(args[1]);
            }
            catch (NumberFormatException e)
            {
                player.sendMessage(Text.get("nonum"));
            }

            Location loc = player.getLocation();

            double minX = loc.getX() - radius;
            double maxX = loc.getX() + radius;
            double minZ = loc.getZ() - radius;
            double maxZ = loc.getZ() + radius;

            int amount = 0;

            for (double i = minX; i <= maxX; i++)
            {
                for (double j = minZ; j <= maxZ; j++)
                {
                    for (double x = 0; x <= 255; x++)
                    {
                        Location l = new Location(loc.getWorld(), i, x, j);

                        if (loc.getWorld().getBlockAt(l).getType() == Material.CHEST)
                        {
                            loc.getWorld().getBlockAt(l).setType(Material.AIR);
                            amount++;
                        }
                    }
                }
            }

            System.out.println("Changed " + amount + " blocks to air");
        }

        else if (args[0].equalsIgnoreCase("setspawn") && player.hasPermission("pw.setspawn"))
        {
            if (args.length != 2)
            {
                player.sendMessage("§cUsage: /pw setspawn <game>");
                return true;
            }

            String gameName = args[1];
            Locations.setLocation(gameName + ".spawn", player.getLocation());

            Game game = Game.getGame(gameName);
            player.sendMessage(Text.get("setspawn").replace("%game%", game.getName()));

            return true;
        }

        else if (args[0].equalsIgnoreCase("leave"))
        {
            GamePlayer gp = null;

            if ((gp = gamePlayers.get(player)) != null)
            {
                gp.leave();
                player.sendMessage(Text.get("gameleave"));
                return true;
            }
            else
            {
                player.sendMessage(Text.get("notingame"));
                return true;
            }
        }

        return true;
    }

    public FileConfiguration getGameConfig()
    {
        return gameConfig;
    }

    public void reloadGameConfig()
    {
        try
        {
            gameConfig.load(gameFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InvalidConfigurationException e)
        {
            e.printStackTrace();
        }
    }

    public void saveGameConfig()
    {
        try
        {
            gameConfig.save(gameFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
