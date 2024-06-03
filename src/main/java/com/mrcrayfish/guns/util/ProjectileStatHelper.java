package com.mrcrayfish.guns.util;

import com.mrcrayfish.guns.entity.ProjectileEntity;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * Author: MrCrayfish
 */
public class ProjectileStatHelper
{
	// This helper handles calculations related to projectiles, including damage, modified stats, etc.
	
	public static float getArmorReducedDamage(ProjectileEntity bullet, LivingEntity entity, float damage)
    {
        if (!(entity instanceof LivingEntity))
        	return damage;
        
        float reducedDamage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getArmorValue(), (float) entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
	    
	    float bypassLevel = Mth.clamp(bullet.getProjectile().getArmorBypass(),0,1);
	    return Mth.lerp(bypassLevel, reducedDamage, damage);
    }
	
	public static float getProtectionBypassDamage(ProjectileEntity bullet, LivingEntity entity, float damage, DamageSource source)
    {
		if (!(entity instanceof LivingEntity) || damage==0)
	        return 0;
		
		int protection = Mth.clamp(EnchantmentHelper.getDamageProtection(entity.getArmorSlots(), source), 0, 20);
	    float reducedDamage = CombatRules.getDamageAfterMagicAbsorb(damage, (float) protection);
	    float damageReductionFactor = reducedDamage/damage;
	    float finalDamage = damage/damageReductionFactor;
	    
	    float bypassLevel = Mth.clamp(bullet.getProjectile().getProtectionBypass(),0,1);
		return (finalDamage-damage)*bypassLevel;
    }
}
