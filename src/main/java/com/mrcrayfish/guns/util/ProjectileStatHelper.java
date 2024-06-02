package com.mrcrayfish.guns.util;

import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.entity.ProjectileEntity;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * Author: MrCrayfish
 */
public class ProjectileStatHelper
{
	// This helper handles calculations related to projectiles, including damage, modified stats, etc.
	
	public static float getArmorBypassDamage(ProjectileEntity bullet, LivingEntity entity, float damage)
    {
        if (!(entity instanceof LivingEntity))
        	return damage;
        
        float reducedDamage = CombatRules.getDamageAfterAbsorb(damage, (float)entity.getArmorValue(), (float)entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
        float bypassDamage = damage*(damage/Math.max(reducedDamage,0.001F));

	    float bypassLevel = bullet.getProjectile().getArmorBypass();
		return Mth.lerp(bypassLevel*0.595F, damage, bypassDamage);
    }
	
	public static float getProtectionBypassDamage(ProjectileEntity bullet, LivingEntity entity, float damage, DamageSource source)
    {
		if (!(entity instanceof LivingEntity))
	        return damage;
		
		int protectors = Mth.clamp(EnchantmentHelper.getDamageProtection(entity.getArmorSlots(), source), 0, 20);
	   	float reducedDamage = CombatRules.getDamageAfterMagicAbsorb(damage, (float) protectors);
	    float bypassDamage = damage*(damage/Math.max(reducedDamage,0.001F));
	    
	    float bypassLevel = bullet.getProjectile().getProtectionBypass();
		return Mth.lerp(bypassLevel*0.595F, damage, bypassDamage);
    }
}
