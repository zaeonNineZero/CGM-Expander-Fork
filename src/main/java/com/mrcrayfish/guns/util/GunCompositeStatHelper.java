package com.mrcrayfish.guns.util;

import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.init.ModEnchantments;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.item.attachment.IAttachment;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

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


	public static int getAmmoCapacity(ItemStack weapon) {
		Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
		return getAmmoCapacity(weapon, modifiedGun);
	}
    public static int getAmmoCapacity(ItemStack weapon, Gun modifiedGun)
    {

        int capacity = Gun.getModifiedAmmoCapacity(weapon);
        int extraCapacity = modifiedGun.getGeneral().getOverCapacityAmmo();
        if (extraCapacity <= 0)
        extraCapacity = modifiedGun.getGeneral().getMaxAmmo()/2;
        
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.OVER_CAPACITY.get(), weapon);
        if(level > 0)
        {
            capacity += Math.max(level, extraCapacity * level);
        }
        return capacity;
    }
	
	public static int getRealReloadSpeed(ItemStack weapon, boolean magReload, boolean reloadFromEmpty)
    {
        if (magReload)
        	return getMagReloadSpeed(weapon, reloadFromEmpty);

        return getReloadInterval(weapon, reloadFromEmpty);
    }


    public static int getReloadInterval(ItemStack weapon, boolean reloadFromEmpty)
    {
        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
        int baseInterval = modifiedGun.getGeneral().getReloadRate();
        int interval = modifiedGun.getGeneral().getReloadRate();
        
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.QUICK_HANDS.get(), weapon);
        if(level > 0)
        {
            interval -= Math.round((3*(baseInterval/10)) * level);
        }
        return Math.max(interval, 1);
    }

    public static int getMagReloadSpeed(ItemStack weapon, boolean reloadFromEmpty)
    {
        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
        ItemStack magStack = Gun.getAttachment(IAttachment.Type.byTagKey("Magazine"), weapon);
        int baseSpeed = modifiedGun.getGeneral().getMagReloadTime();
        double reloadSpeedModifier = 1;
        if(!magStack.isEmpty())
        {
        	if (magStack.getItem().builtInRegistryHolder().key().location().getPath().equals("light_magazine"))
        	{
        		reloadSpeedModifier = modifiedGun.getGeneral().getLightMagReloadTimeModifier();
        	}
            else
            if (magStack.getItem().builtInRegistryHolder().key().location().getPath().equals("extended_magazine"))
            {
            	reloadSpeedModifier = modifiedGun.getGeneral().getExtendedMagReloadTimeModifier();
        	}
        }
        
        int speed = (int) Math.round((baseSpeed + (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.OVER_CAPACITY.get(), weapon)*4)) * reloadSpeedModifier);
        if (reloadFromEmpty)
        {
        	baseSpeed = modifiedGun.getGeneral().getMagReloadFromEmptyTime();
        	speed = (int) Math.round((baseSpeed + (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.OVER_CAPACITY.get(), weapon)*4)) * reloadSpeedModifier);
    	}
        
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.QUICK_HANDS.get(), weapon);
        if(level > 0)
        {
        	speed -= Math.round(((speed/4)) * level);
        }
        return Math.max(speed, 4);
    }
}
