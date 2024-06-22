package com.mrcrayfish.guns.enchantment;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.item.GunItem;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * Author: MrCrayfish
 */
public class EnchantmentTypes
{
    public static final EnchantmentCategory GUN = EnchantmentCategory.create(Reference.MOD_ID + ":gun", item -> item instanceof GunItem);
    public static final EnchantmentCategory SEMI_AUTO_GUN = EnchantmentCategory.create(Reference.MOD_ID + ":semi_auto_gun", item -> item instanceof GunItem && !((GunItem) item).getGun().getGeneral().isAuto());
    public static final EnchantmentCategory AUTO_GUN = EnchantmentCategory.create(Reference.MOD_ID + ":auto_gun", item -> item instanceof GunItem && Gun.canDoAutoFire(new ItemStack(item, 1)));
    public static final EnchantmentCategory GUN_SUPPORTS_RAMPUP = EnchantmentCategory.create(Reference.MOD_ID + ":gun_supports_rampup", item -> item instanceof GunItem && Gun.canDoAutoFire(new ItemStack(item, 1)) && (!((GunItem) item).getGun().getGeneral().hasDoRampUp() || ((GunItem) item).getGun().getGeneral().getRate()>1));
}
