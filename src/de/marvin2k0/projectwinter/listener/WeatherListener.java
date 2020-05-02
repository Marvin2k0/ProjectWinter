package de.marvin2k0.projectwinter.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WeatherListener implements Listener
{
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event)
    {
        event.setCancelled(!event.toWeatherState());
    }
}