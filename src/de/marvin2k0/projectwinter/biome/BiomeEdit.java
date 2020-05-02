package de.marvin2k0.projectwinter.biome;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;

public class BiomeEdit
{
    public static void changeBiome(String Biome)
    {
        try
        {
            String mojangPath = "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
            Class clazz = Class.forName(mojangPath + ".BiomeBase");

            Field plainsField = clazz.getDeclaredField(Biome);
            plainsField.setAccessible(true);
            Object plainsBiome = plainsField.get(null);

            Field biomesField = clazz.getDeclaredField("biomes");
            biomesField.setAccessible(true);
            Object[] biomes = (Object[]) biomesField.get(null);

            for (int i = 0; i < biomes.length; i++)
            {
                biomes[i] = plainsBiome;
            }

            biomesField.set(null, biomes);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}