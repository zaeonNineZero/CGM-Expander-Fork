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
    private static boolean disable1 = false;
    private static boolean disable2 = false;
    private static Method getShoulderInstance;
    private static Method isShoulderSurfing;

    public static boolean isShoulderSurfing()
    {
        if(!GunMod.shoulderSurfingLoaded)
            return false;
        
        if (!disable1)
        try
        {
            init();
            Object object = getShoulderInstance.invoke(null);
            return (boolean) isShoulderSurfing.invoke(object);
        }
        catch(InvocationTargetException | IllegalAccessException | NullPointerException e)
        {
            disable1 = true;
        }
        else
        if (!disable2)
        try
        {
            init();
            Object object = getShoulderInstance.invoke(null);
            return (boolean) isShoulderSurfing.invoke(object);
        }
        catch(InvocationTargetException | IllegalAccessException | NullPointerException e)
        {
            disable2 = true;
        }
        return false;
    }

    private static void init()
    {
        if(getShoulderInstance == null)
        {
            try
            {
                Class<?> shoulderSurfingImpl = Class.forName("com.github.exopandora.shouldersurfing.client.ShoulderSurfingImpl");
                getShoulderInstance = shoulderSurfingImpl.getDeclaredMethod("getInstance");
                isShoulderSurfing = shoulderSurfingImpl.getDeclaredMethod("isShoulderSurfing");
            }
            catch(ClassNotFoundException | NoSuchMethodException | NullPointerException ignored)
            {
                disable1 = true;
            }
            if (disable1)
            try
            {
                Class<?> shoulderInstance = Class.forName("com.github.exopandora.shouldersurfing.client.ShoulderInstance");
                getShoulderInstance = shoulderInstance.getDeclaredMethod("getInstance");
                isShoulderSurfing = shoulderInstance.getDeclaredMethod("doShoulderSurfing");
            }
            catch(ClassNotFoundException | NoSuchMethodException | NullPointerException ignored)
            {
                disable2 = true;
            }
        }
    }
}
