package de.marvin2k0.projectwinter.listener;

import de.marvin2k0.projectwinter.ProjectWinter;
import de.marvin2k0.projectwinter.game.Game;
import de.marvin2k0.projectwinter.game.GamePlayer;
import de.marvin2k0.projectwinter.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class CancelEvents implements Listener
{
    @EventHandler
    public void onMove(PlayerMoveEvent event)
    {
        Player player = event.getPlayer();

        if (!Game.inGame(player))
            return;

        GamePlayer gp = ProjectWinter.gamePlayers.get(player);
        Game game = gp.getGame();

        if (player.getLocation().distance(game.getSpawn()) >= Integer.valueOf(Text.get("mapsize", false)))
        {
            player.sendMessage("Entferne dich nicht zu weit von deinem Team!");
            Vector vector = game.getSpawn().toVector().subtract(player.getLocation().toVector());
            player.setVelocity(vector.normalize());
            return;
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event)
    {
        Player player = event.getPlayer();
        GamePlayer gp = null;

        if (!Game.inGame(player))
            return;

        event.setCancelled(true);
    }
}
