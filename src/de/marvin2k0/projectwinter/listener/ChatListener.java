package de.marvin2k0.projectwinter.listener;

import de.marvin2k0.projectwinter.ProjectWinter;
import de.marvin2k0.projectwinter.game.Game;
import de.marvin2k0.projectwinter.game.GamePlayer;
import de.marvin2k0.projectwinter.util.Text;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

public class ChatListener implements Listener
{
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event)
    {
        Player player = event.getPlayer();
        GamePlayer gp = null;

        if ((gp = ProjectWinter.gamePlayers.get(player)) == null)
            return;

        event.getRecipients().clear();
        event.getRecipients().add(player);

        int radius = Integer.valueOf(Text.get("chatradius", false));

        List<Entity> entities = player.getNearbyEntities(radius, radius, radius);

        for (Entity e : entities)
        {
            if (e instanceof Player && Game.inGame((Player) e))
            {
                event.getRecipients().add((Player) e);
            }
        }
    }
}
