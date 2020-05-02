package de.marvin2k0.projectwinter.listener;

import de.marvin2k0.projectwinter.ProjectWinter;
import de.marvin2k0.projectwinter.game.GamePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener
{
    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        if (ProjectWinter.gamePlayers.containsKey(event.getPlayer()))
        {
            GamePlayer gp = ProjectWinter.gamePlayers.get(event.getPlayer());
            gp.leave();

            ProjectWinter.gamePlayers.remove(event.getPlayer());
        }
    }
}
