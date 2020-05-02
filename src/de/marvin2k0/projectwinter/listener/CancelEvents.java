package de.marvin2k0.projectwinter.listener;

import de.marvin2k0.projectwinter.ProjectWinter;
import de.marvin2k0.projectwinter.game.Game;
import de.marvin2k0.projectwinter.game.GamePlayer;
import de.marvin2k0.projectwinter.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class CancelEvents implements Listener
{
    @EventHandler
    public void onDeath(PlayerDeathEvent event)
    {
        Player player = event.getEntity();

        if (Game.inGame(player))
        {
            GamePlayer gp = ProjectWinter.gamePlayers.get(player);
            gp.getGame().die(gp);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event)
    {
        if (!(event.getEntity() instanceof Player))
            return;

        Player player = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        if (Game.dead.contains(damager))
        {
            event.setCancelled(true);
            System.out.println("kann nicht schlagen");
        }

        if (!Game.inGame(player))
            return;

        GamePlayer gp = ProjectWinter.gamePlayers.get(player);

        if (gp.inLobby)
            event.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event)
    {
        Player player = event.getPlayer();

        if (!Game.inGame(player))
            return;

        GamePlayer gp = ProjectWinter.gamePlayers.get(player);
        Game game = gp.getGame();

        if ((player.getLocation().getWorld() == game.getSpawn().getWorld()) && (player.getLocation().distance(game.getSpawn()) >= Integer.valueOf(Text.get("mapsize", false))))
        {
            player.sendMessage(Text.get("toofar"));
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
