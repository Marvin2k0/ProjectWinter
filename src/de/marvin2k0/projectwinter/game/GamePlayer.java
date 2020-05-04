package de.marvin2k0.projectwinter.game;

import de.marvin2k0.projectwinter.ProjectWinter;
import org.bukkit.entity.Player;

public class GamePlayer
{
    private Game game;
    private Player player;
    public boolean inLobby = true;
    private double warmth;

    public GamePlayer(Game game, Player player)
    {
        this.game = game;
        this.player = player;
        this.warmth = 20;

        if (!ProjectWinter.gamePlayers.containsKey(player))
            ProjectWinter.gamePlayers.put(player, this);
    }

    public void leave()
    {
        this.getGame().leave(this);
    }

    public double getWarmth()
    {
        return warmth;
    }
    public void setWarmth(double warmth)
    {
        this.warmth = warmth;
    }

    public Game getGame()
    {
        return game;
    }

    public void setGame(Game game)
    {
        this.game  = game;
    }

    public Player getPlayer()
    {
        return player;
    }
}
