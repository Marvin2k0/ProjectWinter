package de.marvin2k0.projectwinter;

import de.marvin2k0.projectwinter.game.Game;
import de.marvin2k0.projectwinter.util.Locations;
import de.marvin2k0.projectwinter.util.Text;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LobbyCommand implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(Text.get("noplayer"));
            return true;
        }

        Player player = (Player) sender;

        if (label.equalsIgnoreCase("setlobby"))
        {
            Locations.setLocation("lobby", player.getLocation());
            player.sendMessage("§aLobby has been set!");
            return true;
        }

        else if (label.equalsIgnoreCase("lobby"))
        {
            Location lobby = null;

            try
            {
                lobby = Locations.get("lobby");
            }
            catch (NullPointerException e)
            {
                player.sendMessage("§cLobb has not been set! -> /setlobby");
                return true;
            }

            if (Game.inGame(player))
                ProjectWinter.gamePlayers.get(player).leave();

            player.teleport(lobby);
            return true;
        }

        return true;
    }
}
