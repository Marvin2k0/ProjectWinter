package de.marvin2k0.projectwinter.game;

import de.marvin2k0.projectwinter.ProjectWinter;
import de.marvin2k0.projectwinter.util.CountdownTimer;
import de.marvin2k0.projectwinter.util.Locations;
import de.marvin2k0.projectwinter.util.Text;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Random;

public class Game
{
    public static ArrayList<Player> dead = new ArrayList<>();
    private static ArrayList<Game> games = new ArrayList<>();
    private static int MAX_PLAYERS = 12;
    private static int MIN_PLAYERS = 2;

    private ArrayList<GamePlayer> players = new ArrayList<>();
    private ArrayList<Location> objectives = new ArrayList<>();
    private GamePlayer[] traitors;
    private Random random;

    public boolean hasStarted;
    public boolean win;
    private String name;

    public static Game getGame(String name)
    {
        for (Game game : games)
        {
            if (game.getName().equalsIgnoreCase(name))
                return game;
        }

        Game game = new Game(name);
        games.add(game);
        game.hasStarted = false;

        return game;
    }

    public void startGame()
    {
        hasStarted = true;
        setTraitors();

        for (GamePlayer gp : players)
            gp.inLobby = false;
    }

    public void start()
    {
        for (int i = 0; i < 2; i++)
        {
            Location spawn = Locations.get(getName() + ".spawn");

            int randX = random.nextInt(95);
            int multX = ((random.nextInt(2) == 0) ? -1 : 1);
            double x = spawn.getX() + randX * multX;
            System.out.println("Rand x " + randX + " " + multX);

            int randZ = random.nextInt(95);
            int multZ = ((random.nextInt(2) == 0) ? -1 : 1);
            double z = spawn.getZ() + randZ * multZ;
            System.out.println("rand z " + randZ + " " + multZ);

            double y = spawn.getWorld().getHighestBlockYAt((int) x, (int) z);

            System.out.println("build obj at" + x + " " + y + " " + z);
            Location loc = new Location(spawn.getWorld(), x, y, z);
            objectives.add(loc);
            buildObjective(loc);
        }

        CountdownTimer timer = new CountdownTimer(ProjectWinter.instance, 20,
                () -> {
                },
                () -> startGame(),
                (t) -> sendMessage(Text.get("countdown").replace("%timer%", t.getSecondsLeft() + ""))
        );

        timer.scheduleTimer();
    }

    private void setTraitors()
    {
        int amount = ((int) (players.size() * 0.2)) == 0 ? 1 : (int) (players.size() * 0.2);
        traitors = new GamePlayer[amount];

        for (int i = 0; i < amount; i++)
        {
            GamePlayer gp = players.get((int) (System.currentTimeMillis() % players.size()));
            traitors[i] = gp;
            gp.getPlayer().sendMessage(Text.get("traitor"));
        }
    }

    public void join(Player player)
    {
        if (ProjectWinter.instance.gamePlayers.containsKey(player))
        {
            player.sendMessage(Text.get("alreadyingame"));
            return;
        }

        GamePlayer gp = new GamePlayer(this, player);

        if (hasStarted)
        {
            gp.getPlayer().sendMessage(Text.get("alreadystarted"));
            ProjectWinter.gamePlayers.remove(gp.getPlayer());
            return;
        }
        if (!players.contains(gp))
            players.add(gp);

        gp.inLobby = true;
        player.setHealth(player.getHealthScale());
        player.setFoodLevel(20);
        player.setGameMode(GameMode.SURVIVAL);
        teleportToLobby(player);

        sendMessage(Text.get("gamejoin").replace("%player%", player.getName()));

        if (players.size() >= MIN_PLAYERS && !hasStarted)
        {
            start();
        }
    }

    public void sendMessage(String msg)
    {
        for (GamePlayer gp : players)
        {
            gp.getPlayer().sendMessage(msg);
        }
    }

    public void leave(GamePlayer gp)
    {
        if (players.contains(gp))
            players.remove(gp);

        if (isTraitor(gp))
        {
            for (int i = 0; i < traitors.length; i++)
            {
                if (traitors[i] == gp)
                    traitors[i] = null;
            }
        }

        if (ProjectWinter.gamePlayers.containsKey(gp.getPlayer()))
            ProjectWinter.gamePlayers.remove(gp.getPlayer());

        Player player = gp.getPlayer();
        player.setAllowFlight(false);
        player.setFlying(false);
        player.teleport(Locations.get("lobby"));

        for (Player p : Bukkit.getOnlinePlayers())
        {
            p.showPlayer(player);
        }

        if (!win)
            checkWin("leave");
    }

    public void teleportToLobby(Player player)
    {
        if (ProjectWinter.instance.getGameConfig().isSet(getName() + ".lobby"))
        {
            player.teleport(Locations.get(getName() + ".lobby"));
            player.sendMessage(Text.get("welcome"));
        }
        else
        {
            System.out.println("§4LOBBY HAS NOT BEEN SET!");
        }
    }

    public void die(GamePlayer gp)
    {
        Player player = gp.getPlayer();
        player.setHealth(player.getHealthScale());

        for (int i = 0; i < traitors.length; i++)
        {
            if (gp == traitors[i])
                traitors[i] = null;
        }


        for (GamePlayer g : players)
            g.getPlayer().hidePlayer(player);

        player.setAllowFlight(true);
        player.setFlying(true);
        dead.add(player);
        player.sendMessage("§7You died. Type §b/lobby §7to leave the game");

        checkWin("die");
    }

    public void buildObjective(Location loc)
    {
        World world = loc.getWorld();
        for (int i = 0; i < 5; i++)
        {
            for (int j = 0; j < 5; j++)
            {
                world.getBlockAt(new Location(world, loc.getX() + i, loc.getY() - 2, loc.getZ() + j)).setType(Material.DIAMOND_BLOCK);

                if (i == 2 && j == 2)
                {
                    world.getBlockAt(new Location(world, loc.getX() + i, loc.getY() - 1, loc.getZ() + j)).setType(Material.GLOWSTONE);
                    world.getBlockAt(new Location(world, loc.getX() + i, loc.getY(), loc.getZ() + j)).setType(Material.GLASS);
                    world.getBlockAt(new Location(world, loc.getX() + i, loc.getY() + 1, loc.getZ() + j)).setType(Material.CHEST);
                    Chest chest = (Chest) world.getBlockAt(new Location(world, loc.getX() + i, loc.getY() + 1, loc.getZ() + j)).getState();
                    chest.getInventory().addItem(new ItemStack(Material.BEACON));
                    continue;
                }

                world.getBlockAt(new Location(world, loc.getX() + i, loc.getY(), loc.getZ() + j)).setType(Material.COBBLESTONE);

                if ((i == 0 && j == 0) || (i == 0 && j == 4) || (i == 4 && j == 0) || (i == 4 && j == 4))
                {
                    world.getBlockAt(new Location(world, loc.getX() + i, loc.getY(), loc.getZ() + j)).setType(Material.GLOWSTONE);
                }
            }
        }
    }

    private void setChests()
    {
        int radius = 50;

        try
        {
            radius = Integer.valueOf(Text.get("mapsize", false));
        }
        catch (NumberFormatException e)
        {
        }

        Location loc = Locations.get(getName() + ".spawn");
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

    private void removeChests()
    {
        int radius = 50;

        try
        {
            radius = Integer.valueOf(Text.get("mapsize", false));
        }
        catch (NumberFormatException e)
        {
        }

        Location loc = Locations.get(getName() + ".spawn");

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
                        Chest chest = (Chest) loc.getWorld().getBlockAt(l).getState();
                        chest.getInventory().clear();

                        loc.getWorld().getBlockAt(l).setType(Material.AIR);
                        amount++;
                    }
                }
            }
        }

        System.out.println("Changed " + amount + " blocks to air");
    }

    public void removeObjectives(Location loc)
    {
        System.out.println("removing");
        World world = loc.getWorld();
        for (int i = 0; i < 5; i++)
        {
            for (int j = 0; j < 5; j++)
            {
                world.getBlockAt(new Location(world, loc.getX() + i, loc.getY() - 2, loc.getZ() + j)).setType(Material.AIR);

                if (i == 2 && j == 2)
                {
                    world.getBlockAt(new Location(world, loc.getX() + i, loc.getY() - 1, loc.getZ() + j)).setType(Material.AIR);
                    world.getBlockAt(new Location(world, loc.getX() + i, loc.getY(), loc.getZ() + j)).setType(Material.AIR);
                    world.getBlockAt(new Location(world, loc.getX() + i, loc.getY() + 1, loc.getZ() + j)).setType(Material.AIR);
                    continue;
                }

                if ((i == 0 && j == 0) || (i == 0 && j == 4) || (i == 4 && j == 0) || (i == 4 && j == 4))
                {
                    world.getBlockAt(new Location(world, loc.getX() + i, loc.getY() + 1, loc.getZ() + j)).setType(Material.AIR);
                }

                world.getBlockAt(new Location(world, loc.getX() + i, loc.getY(), loc.getZ() + j)).setType(Material.AIR);
            }
        }

        System.out.println("removed obj at " + loc.getX() + " " + loc.getY() + " " + loc.getZ());
    }

    public void reset()
    {
        hasStarted = false;
        win = false;

        removeChests();
        setChests();

        for (Location loc : objectives)
            removeObjectives(loc);

        objectives.clear();

        hasStarted = false;
    }

    public void checkWin(String str)
    {
        System.out.println(str);

        try
        {
            if (checkTraitorWin() && hasStarted && !win)
            {
                win = true;

                for (GamePlayer gp : players)
                {
                    gp.getPlayer().sendMessage(Text.get("traitorwin"));
                    leave(gp);
                }

                for (Player player : dead)
                {
                    player.teleport(Locations.get("lobby"));
                    player.sendMessage(Text.get("traitorwin"));
                }
            }
            else if (checkGoodWin() && hasStarted && !win)
            {
                win = true;
                System.out.println("die guten haben gewonnen");

                for (GamePlayer gp : players)
                {
                    gp.getPlayer().sendMessage(Text.get("goodwin"));
                    leave(gp);
                }

                for (Player player : dead)
                {
                    player.teleport(Locations.get("lobby"));
                    player.sendMessage(Text.get("goodwin"));
                }
            }
        }
        catch (Exception e)
        {
        }

        if (win)
        {
            reset();
        }
    }

    public boolean checkTraitorWin()
    {
        for (GamePlayer g : players)
        {
            if (!isTraitor(g))
            {
                System.out.println(g.getPlayer().getName() + " ist kein traitor");
                return false;
            }
        }

        return true;
    }

    public boolean checkGoodWin()
    {
        for (GamePlayer g : traitors)
        {
            if (g != null)
            {
                return false;
            }
        }

        return true;
    }

    public boolean isTraitor(GamePlayer gp)
    {
        if (traitors == null)
            return false;

        for (int i = 0; i < traitors.length; i++)
        {
            if (traitors[i] == gp)
                return true;
        }

        return false;
    }

    public Location getSpawn()
    {
        return Locations.get(getName() + ".spawn");
    }

    public String getName()
    {
        return this.name;
    }

    public static boolean inGame(Player player)
    {
        return ProjectWinter.gamePlayers.get(player) != null;
    }

    public static Game getGameFromName(String name)
    {
        if (exists(name))
        {
            for (Game g : games)
            {
                if (g.getName().equalsIgnoreCase(name))
                    return g;
            }
        }

        return null;
    }

    public static boolean exists(String game)
    {
        for (Game g : games)
        {
            if (g.getName().equalsIgnoreCase(game))
                return true;
        }

        return false;
    }

    public static ArrayList<Game> getGames()
    {
        return games;
    }

    private Game(String name)
    {
        random = new Random();

        this.hasStarted = false;
        this.name = name;
    }
}
