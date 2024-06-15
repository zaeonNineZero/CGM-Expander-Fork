package com.mrcrayfish.guns.util;

import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.item.GunItem;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Author: MrCrayfish
 */
public class GunCompositeStatHelper
{
	// This helper delivers composite stats derived from GunModifierHelper and GunEnchantmentHelper.
	
	public static int getCompositeRate(ItemStack weapon, Gun modifiedGun, Player player)
    {
        int a = GunEnchantmentHelper.getRate(weapon, modifiedGun);
        int b = GunModifierHelper.getModifiedRate(weapon, a);
        return GunEnchantmentHelper.getRampUpRate(player, weapon, b);
    }
	public static int getCompositeRate(ItemStack weapon, Player player) {
		// Version of getCompositeRate that only requires an ItemStack and Player input
    	Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
		int a = GunEnchantmentHelper.getRate(weapon, modifiedGun);
        int b = GunModifierHelper.getModifiedRate(weapon, a);
        return GunEnchantmentHelper.getRampUpRate(player, weapon, b);
	}
	
	public static int getCompositeBaseRate(ItemStack weapon, Gun modifiedGun)
    {
        int a = GunEnchantmentHelper.getRate(weapon, modifiedGun);
        return GunModifierHelper.getModifiedRate(weapon, a);
    }
	public static int getCompositeBaseRate(ItemStack weapon) {
		// Version of getCompositeBaseRate that only requires an ItemStack input
    	Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
		int a = GunEnchantmentHelper.getRate(weapon, modifiedGun);
		return GunModifierHelper.getModifiedRate(weapon, a);
	}
	
	public static float getCompositeSpread(ItemStack weapon, Gun modifiedGun)
    {
        //float a = GunEnchantmentHelper.getSpread(weapon, modifiedGun);
		//return GunModifierHelper.getModifiedSpread(weapon, a);
        return GunModifierHelper.getModifiedSpread(weapon, modifiedGun.getGeneral().getSpread());
    }
	
	public static float getCompositeMinSpread(ItemStack weapon, Gun modifiedGun)
    {
        //float a = GunEnchantmentHelper.getMinSpread(weapon, modifiedGun);
		//return GunModifierHelper.getModifiedSpread(weapon, a);
        return GunModifierHelper.getModifiedSpread(weapon, modifiedGun.getGeneral().getRestingSpread());
    }
}
