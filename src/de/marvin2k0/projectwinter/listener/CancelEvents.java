package de.marvin2k0.projectwinter.listener;

import de.marvin2k0.projectwinter.ProjectWinter;
import de.marvin2k0.projectwinter.game.Game;
import de.marvin2k0.projectwinter.game.GamePlayer;
import de.marvin2k0.projectwinter.util.CountdownTimer;
import de.marvin2k0.projectwinter.util.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class CancelEvents implements Listener
{
    private ArrayList<GamePlayer> coldTimer = new ArrayList<>();
    private ArrayList<GamePlayer> cold1 = new ArrayList<>();
    private ArrayList<GamePlayer> cold2 = new ArrayList<>();
    private ArrayList<GamePlayer> cold3 = new ArrayList<>();
    private ArrayList<GamePlayer> poison = new ArrayList<>();


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

        if (!(event.getDamager() instanceof Player))
            return;

        Player player = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        if (Game.dead.contains(damager))
        {
            event.setCancelled(true);
            return;
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

        if (poison.contains(gp))
        {
            Location loc = gp.getPlayer().getLocation();
            byte skyLight = loc.getBlock().getLightFromSky();
            byte ambLight = loc.getBlock().getLightFromBlocks();
            double warmth = ambLight * 1.75 - skyLight;

            if (warmth > 0 || hasItem(Material.FIRE, (byte) 0, 1, gp.getPlayer()))
            {
                poison.remove(gp);
                gp.getPlayer().removePotionEffect(PotionEffectType.POISON);
                gp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 5, 2), false);

                if (hasItem(Material.FIRE, (byte) 0, 1, gp.getPlayer()))
                {
                    removeItem(1, Material.FIRE, (byte) 0, gp.getPlayer());
                    gp.getPlayer().sendMessage(Text.get("fireused"));

                    if (cold1.contains(gp))
                        cold1.remove(gp);

                    if (cold2.contains(gp))
                        cold2.remove(gp);

                    if (cold3.contains(gp))
                        cold3.remove(gp);
                }
            }
        }

        if (!coldTimer.contains(gp) && game.hasStarted)
        {
            coldTimer.add(gp);

            new CountdownTimer(ProjectWinter.instance, 30,
                    () -> {
                    },
                    () -> coldMessage(gp),
                    (t) -> System.out.println(gp.getPlayer().getName() + " " + t.getSecondsLeft())
            ).scheduleTimer();
        }
    }

    public void coldMessage(GamePlayer gp)
    {
        if (coldTimer.contains(gp))
            coldTimer.remove(gp);

        Location loc = gp.getPlayer().getLocation();
        byte skyLight = loc.getBlock().getLightFromSky();
        byte ambLight = loc.getBlock().getLightFromBlocks();
        double warmth = ambLight * 1.75 - skyLight;
        String msg = "";

        if (hasItem(Material.FIRE, (byte) 0, 1, gp.getPlayer()))
        {
            warmth += 15;
        }

        if (warmth > 20)
            warmth = 20;

        gp.setWarmth(warmth);

        if (warmth < 0 || (hasItem(Material.FIRE, (byte) 0, 1, gp.getPlayer()) && warmth - 15 < 0))
        {
            msg = Text.get("coldmessage");

            if (hasItem(Material.FIRE, (byte) 0, 1, gp.getPlayer()))
            {
                removeItem(1, Material.FIRE, (byte) 0, gp.getPlayer());
                msg = Text.get("fireused");

                if (cold1.contains(gp))
                    cold1.remove(gp);

                if (cold2.contains(gp))
                    cold2.remove(gp);

                if (cold3.contains(gp))
                    cold3.remove(gp);
            }
            else
            {
                if (cold1.contains(gp))
                {
                    cold1.remove(gp);
                    cold2.add(gp);
                }
                else if (cold2.contains(gp))
                {
                    cold2.remove(gp);
                    cold3.add(gp);
                }
                else if (cold3.contains(gp))
                {
                    cold3.remove(gp);
                    poison.add(gp);
                    gp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20*60, 1, false));
                    msg = Text.get("freezing");
                }
                else
                {
                    cold1.add(gp);
                }
            }
        }

        System.out.println(gp.getPlayer().getName() + " " + warmth);

        if (!msg.isEmpty())
            gp.getPlayer().sendMessage(msg);
    }

    private boolean hasItem(Material material, byte data, int quantity, Player p)
    {
        int item = 0;
        ItemStack[] arrayOfItemStack;
        int x = (arrayOfItemStack = p.getInventory().getContents()).length;

        for (int i = 0; i < x; i++)
        {
            ItemStack contents = arrayOfItemStack[i];
            if (contents != null && contents.getType() != Material.AIR && contents.getType() == material && contents
                    .getData().getData() == data)
                item += contents.getAmount();
        }
        if (item < quantity)
            return false;
        return true;
    }

    private void removeItem(int q, Material material, byte data, Player p)
    {
        ItemStack[] arrayOfItemStack;
        int inventoryItemsSize = (arrayOfItemStack = p.getInventory().getContents()).length;

        for (int i = 0; i < inventoryItemsSize; i++)
        {
            ItemStack contents = arrayOfItemStack[i];

            if (contents != null && contents.getType() != Material.AIR && contents.getType() == material && contents
                    .getData().getData() == data)

                if ((q >= 64 && contents.getAmount() == 64) || q == contents.getAmount())
                {
                    p.getInventory().remove(contents);
                }
                else
                {
                    ItemStack n = new ItemStack(contents.getType(), contents.getAmount() - q);
                    p.getInventory().remove(contents);
                    p.getInventory().setItem(i, n);
                }
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
