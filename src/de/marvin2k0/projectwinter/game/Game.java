package de.marvin2k0.projectwinter.game;

import de.marvin2k0.projectwinter.ProjectWinter;
import de.marvin2k0.projectwinter.util.CountdownTimer;
import de.marvin2k0.projectwinter.util.Locations;
import de.marvin2k0.projectwinter.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Game
{
    public static ArrayList<Player> dead = new ArrayList<>();
    private static ArrayList<Game> games = new ArrayList<>();
    private static int MAX_PLAYERS = 12;
    private static int MIN_PLAYERS = 2;

    private ArrayList<GamePlayer> players = new ArrayList<>();
    private GamePlayer[] traitors;

    public boolean hasStarted;
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
        setTraitors();

        for (GamePlayer gp : players)
            gp.inLobby = false;
    }

    public void start()
    {
        hasStarted = true;

        CountdownTimer timer = new CountdownTimer(ProjectWinter.instance, 20,
                () -> {},
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
            GamePlayer gp = players.get((int)(System.currentTimeMillis() % players.size()));
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

        if (ProjectWinter.gamePlayers.containsKey(gp.getPlayer()))
            ProjectWinter.gamePlayers.remove(gp.getPlayer());

        Player player = gp.getPlayer();
        player.setAllowFlight(false);
        player.setFlying(false);

        for (Player p : Bukkit.getOnlinePlayers())
        {
            p.showPlayer(player);
            System.out.println("set " + player.getName() + " visible for " + p.getName());
        }

        System.out.println("player left");
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

        players.remove(gp);

        for (GamePlayer g : players)
            g.getPlayer().hidePlayer(player);

        player.setAllowFlight(true);
        player.setFlying(true);
        dead.add(player);

        checkWin();
    }

    public void checkWin()
    {
        //TODO
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
        this.hasStarted = false;
        this.name = name;
    }
}
