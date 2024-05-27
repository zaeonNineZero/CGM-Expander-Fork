package com.mrcrayfish.guns.util;

import com.mrcrayfish.guns.common.Gun;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Author: MrCrayfish
 */
public class GunCompositeStatHelper
{
	public static int getCompositeRate(ItemStack weapon, Gun modifiedGun, Player player)
    {
        int a = GunEnchantmentHelper.getRate(weapon, modifiedGun);
        int b = GunModifierHelper.getModifiedRate(weapon, a);
        return GunEnchantmentHelper.getRampUpRate(player, weapon, b);
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
