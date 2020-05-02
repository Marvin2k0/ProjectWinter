package de.marvin2k0.projectwinter.game;

import de.marvin2k0.projectwinter.ProjectWinter;
import de.marvin2k0.projectwinter.util.CountdownTimer;
import de.marvin2k0.projectwinter.util.Locations;
import de.marvin2k0.projectwinter.util.Text;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Game
{
    private static ArrayList<Game> games = new ArrayList<>();
    private static int MAX_PLAYERS = 2;

    private ArrayList<GamePlayer> players = new ArrayList<>();

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

    public void start()
    {
        //TODO: Start game
        hasStarted = true;

        CountdownTimer timer = new CountdownTimer(ProjectWinter.instance, 20,
                () -> {},
                () -> {},
                (t) -> System.out.println(t.getSecondsLeft())
        );

        timer.scheduleTimer();
    }

    public void join(Player player)
    {
        if (ProjectWinter.instance.gamePlayers.containsKey(player))
        {
            player.sendMessage(Text.get("alreadyingame"));
            return;
        }

        GamePlayer gp = new GamePlayer(this, player);
        teleportToLobby(player);

        if (!players.contains(gp))
            players.add(gp);

        System.out.println("There are now " + players.size() + " players in game");

        if (players.size() >= MAX_PLAYERS * 0.5 && !hasStarted)
        {
            start();
        }
    }

    public void leave(GamePlayer gp)
    {
        if (players.contains(gp))
            players.remove(gp);

        if (ProjectWinter.gamePlayers.containsKey(gp.getPlayer()))
            ProjectWinter.gamePlayers.remove(gp.getPlayer());
    }

    public void teleportToLobby(Player player)
    {
        if (ProjectWinter.instance.getGameConfig().isSet(getName() + ".spawn"))
        {
            player.teleport(Locations.get(getName() + ".spawn"));
            player.sendMessage(Text.get("welcome"));
        }
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
