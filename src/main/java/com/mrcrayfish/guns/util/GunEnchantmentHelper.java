package com.mrcrayfish.guns.util;

import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.common.SpreadTracker;
import com.mrcrayfish.guns.init.ModEnchantments;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.particles.TrailData;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class GunEnchantmentHelper
{
    public static ParticleOptions getParticle(ItemStack weapon)
    {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(weapon);
        if(enchantments.containsKey(ModEnchantments.FIRE_STARTER.get()))
        {
            return ParticleTypes.LAVA;
        }
        else if(enchantments.containsKey(ModEnchantments.PUNCTURING.get()))
        {
            return ParticleTypes.ENCHANTED_HIT;
        }
        return new TrailData(weapon.isEnchanted());
    }

    public static int getRealReloadSpeed(ItemStack weapon)
    {
        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
        if (modifiedGun.getGeneral().getUseMagReload())
        return getMagReloadSpeed(weapon);
        else
        return getReloadInterval(weapon);
    }


    public static int getReloadInterval(ItemStack weapon)
    {
        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
        int interval = modifiedGun.getGeneral().getReloadRate();
        
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.QUICK_HANDS.get(), weapon);
        if(level > 0)
        {
            interval -= Math.round((3*(interval/10)) * level);
        }
        return Math.max(interval, 1);
    }

    public static int getMagReloadSpeed(ItemStack weapon)
    {
        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
        //float rawSpeed = modifiedGun.getGeneral().getMagReloadTime() * (1F + 0.5F*((float) EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.OVER_CAPACITY.get(), weapon)));
        //int speed = (int) rawSpeed;
        int speed = modifiedGun.getGeneral().getMagReloadTime() + (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.OVER_CAPACITY.get(), weapon)*4);
        
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.QUICK_HANDS.get(), weapon);
        if(level > 0)
        {
        	speed -= Math.round(((speed/4)) * level);
        }
        return Math.max(speed, 4);
    }

    public static int getRate(ItemStack weapon, Gun modifiedGun)
    {
        int rate = modifiedGun.getGeneral().getRate();
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.TRIGGER_FINGER.get(), weapon);
        if(level > 0)
        {
            float newRate = rate * (0.25F * level);
            rate -= Mth.clamp(newRate, 0, rate);
        }
        return rate;
    }

    public static int getRampUpRate(Player player,ItemStack weapon, int baseRate)
    {
        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
        if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.TRIGGER_FINGER.get(), weapon) > 0)
        	return baseRate;

        int maxRate = getRampUpMaxRate(weapon, baseRate);
        int minRate = getRampUpMinRate(maxRate);
        int newRate = baseRate;
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RAMP_UP.get(), weapon);
        if(level > 0 || modifiedGun.getGeneral().hasDoRampUp())
        {
        	int rampUpShot = ModSyncedDataKeys.RAMPUPSHOT.getValue(player);
        	float rampLog = (float) (Math.log((float) rampUpShot+1)/Math.log((float) getRampUpMaxShots(modifiedGun)));
        	//float rampDiv = ((float) rampUpShot)/((float) getRampUpMaxShots(modifiedGun));
        	//float rampFactor = (rampLog+rampDiv)/2;
        	float rampFactor = rampLog;
        	float rampedRate = (float) Math.ceil((float) Mth.lerp(rampFactor,minRate,maxRate));
            newRate = (int) Math.max(rampedRate, maxRate);
        }
        return newRate;
    }
    
    public static int getRampUpMaxShots(Gun gun)
    {
    	 return gun.getGeneral().getRampUpShotsNeeded();
    }
    
    public static int getRampUpMinRate(int rate)
    {
    	 return rate+3;
    }
    
    public static int getRampUpMaxRate(ItemStack weapon, int rate)
    {
    	int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RAMP_UP.get(), weapon); 
    	if (level<1)
        	return rate;
    	int maxRate = (int) Math.ceil((float) (rate/1.5)-0.5);
        	return Math.max(maxRate, 1);
    }
    
    public static int getRampUpMaxRate(ItemStack weapon, Gun modifiedGun)
    {
        int rate = modifiedGun.getGeneral().getRate();
    	int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RAMP_UP.get(), weapon); 
    	if (level<1)
        	return rate;
    	int maxRate = (int) Math.ceil((float) (rate/1.5)-0.5);
        	return Math.max(maxRate, 1);
    }

    public static double getAimDownSightSpeed(ItemStack weapon)
    {
    	if(!(weapon.getItem() instanceof GunItem))
            return 1;
    	
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.LIGHTWEIGHT.get(), weapon);
        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
        double speed = modifiedGun.getGeneral().getADSSpeed();
        speed += Math.max(Math.min((level)*0.5,2.5),0.01);
        return Math.max(speed,0.01);
    }

    public static int getAmmoCapacity(ItemStack weapon, Gun modifiedGun)
    {
        int capacity = modifiedGun.getGeneral().getMaxAmmo();
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

    public static double getProjectileSpeedModifier(ItemStack weapon)
    {
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ACCELERATOR.get(), weapon);
        if(level > 0)
        {
            return 1.0 + 0.5 * level;
        }
        return 1.0;
    }

    public static float getAcceleratorDamage(ItemStack weapon, float damage)
    {
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ACCELERATOR.get(), weapon);
        if(level > 0)
        {
            return damage + damage * (0.1F * level);
        }
        return damage;
    }

    public static float getSplitShotProjectileCount(ItemStack weapon, float projectiles)
    {
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ACCELERATOR.get(), weapon);
        if(level > 0)
        {
            return projectiles + level;
        }
        return projectiles;
    }

    public static float getDeadeyeHeadshotMultiplier(ItemStack weapon, float multiplier)
    {
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ACCELERATOR.get(), weapon);
        if(level > 0)
        {
            return multiplier + multiplier * ((1F/3F) * level);
        }
        return multiplier;
    }

    public static float getSplitShotDamage(ItemStack weapon, float damage)
    {
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ACCELERATOR.get(), weapon);
        if(level > 0)
        {
            return damage + damage * (0.05F * level);
        }
        return damage;
    }

    public static float getStabilizingRecoilModifier(ItemStack weapon)
    {
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ACCELERATOR.get(), weapon);
        if(level > 0)
        {
            return Mth.clamp(1F - (0.2F * level),0F,1F);
        }
        return 1F;
    }

    public static float getPuncturingChance(ItemStack weapon)
    {
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.PUNCTURING.get(), weapon);
        return level * 0.05F;
    }
}
