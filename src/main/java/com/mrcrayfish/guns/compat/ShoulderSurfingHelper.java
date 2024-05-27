package com.mrcrayfish.guns.compat;

import com.mrcrayfish.guns.GunMod;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Author: MrCrayfish
 */
public class ShoulderSurfingHelper
{
    private static boolean disable = false;
    private static Method getShoulderInstance;
    private static Method isShoulderSurfing;

    public static boolean isShoulderSurfing()
    {
        if(!GunMod.shoulderSurfingLoaded || disable)
            return false;

        try
        {
            init();
            Object object = getShoulderInstance.invoke(null);
            return (boolean) isShoulderSurfing.invoke(object);
        }
        catch(InvocationTargetException | IllegalAccessException e)
        {
            disable = true;
        }
        return false;
    }

    private static void init()
    {
        if(getShoulderInstance == null)
        {
            try
            {
                Class<?> shoulderInstance = Class.forName("com.github.exopandora.shouldersurfing.client.ShoulderInstance");
                getShoulderInstance = shoulderInstance.getDeclaredMethod("getInstance");
                isShoulderSurfing = shoulderInstance.getDeclaredMethod("doShoulderSurfing");
            }
            catch(ClassNotFoundException | NoSuchMethodException ignored)
            {
                disable = true;
            }
        }
    }
}
