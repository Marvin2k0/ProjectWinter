package de.marvin2k0.projectwinter.game;

import de.marvin2k0.projectwinter.ProjectWinter;
import org.bukkit.entity.Player;

public class GamePlayer
{
    private Game game;
    private Player player;

    public GamePlayer(Game game, Player player)
    {
        this.game = game;
        this.player = player;

        if (!ProjectWinter.gamePlayers.containsKey(player))
            ProjectWinter.gamePlayers.put(player, this);
    }

    public void leave()
    {
        this.getGame().leave(this);
    }

    public Game getGame()
    {
        return game;
    }

    public Player getPlayer()
    {
        return player;
    }
}
