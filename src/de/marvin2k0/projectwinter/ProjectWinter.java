package de.marvin2k0.projectwinter;

import de.marvin2k0.projectwinter.game.Game;
import de.marvin2k0.projectwinter.game.GamePlayer;
import de.marvin2k0.projectwinter.listener.*;
import de.marvin2k0.projectwinter.util.Locations;
import de.marvin2k0.projectwinter.util.Text;
import net.minecraft.server.v1_7_R4.WorldGenerator;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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

        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        getServer().getPluginManager().registerEvents(new SignListener(), this);
        getServer().getPluginManager().registerEvents(new CancelEvents(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);

        getCommand("projectwinter").setExecutor(this);
        getCommand("setlobby").setExecutor(new LobbyCommand());
        getCommand("lobby").setExecutor(new LobbyCommand());
    }

    @Override
    public void onDisable()
    {
        for (Game game : Game.getGames())
        {
            System.out.println("resetting for game " + game.getName());
            game.reset();
        }
    }

    private void addGames()
    {
        Map<String, Object> section = getGameConfig().getConfigurationSection("").getValues(false);

        for (Map.Entry<String, Object> entry : section.entrySet())
        {
            if (!entry.getKey().equalsIgnoreCase("lobby") && getGameConfig().isSet(entry.getKey() + ".spawn"))
            {
                Game.getGame(entry.getKey());
                System.out.println("added game " + entry.getKey());
            }
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

        if (args[0].equalsIgnoreCase("loot") && player.hasPermission("pw.loot"))
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
                        Chest chest = (Chest) loc.getWorld().getBlockAt(l).getState();
                        chest.getInventory().addItem(new ItemStack(Material.FIRE));
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
                player.sendMessage("§cUsage: /" + label + " reset <game>");
                return true;
            }


           if (!Game.exists(args[1]))
           {
               return true;
           }

           Game game = Game.getGameFromName(args[1]);
           game.reset();
        }

        else if (args[0].equalsIgnoreCase("setlobby") && player.hasPermission("pw.setlobby"))
        {
            if (args.length != 2)
            {
                player.sendMessage("§cUsage: /pw setlobby <game>");
                return true;
            }

            String gameName = args[1];
            Locations.setLocation(gameName + ".lobby", player.getLocation());

            Game game = Game.getGame(gameName);
            player.sendMessage(Text.get("setlobby").replace("%game%", game.getName()));
            return true;
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

        else if (args[0].equalsIgnoreCase("world"))
        {
            WorldCreator worldCreator = new WorldCreator("projectwinter");
            World projectwinter = Bukkit.getWorld("projectwinter");
            player.sendMessage(Text.get("loading"));
            worldCreator.createWorld();

            player.teleport(projectwinter.getSpawnLocation());
            player.sendMessage(Text.get("welcome"));
            return true;
        }

        player.sendMessage("§cInvalid command!");
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
