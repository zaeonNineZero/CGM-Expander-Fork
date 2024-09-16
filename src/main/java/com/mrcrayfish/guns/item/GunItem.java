package com.mrcrayfish.guns.item;

import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.client.GunItemStackRenderer;
import com.mrcrayfish.guns.client.KeyBinds;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.common.NetworkGunManager;
import com.mrcrayfish.guns.debug.Debug;
import com.mrcrayfish.guns.enchantment.EnchantmentTypes;
import com.mrcrayfish.guns.util.GunCompositeStatHelper;
import com.mrcrayfish.guns.util.GunEnchantmentHelper;
import com.mrcrayfish.guns.util.GunModifierHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Consumer;

public class GunItem extends Item implements IColored, IMeta
{
    private WeakHashMap<CompoundTag, Gun> modifiedGunCache = new WeakHashMap<>();

    private Gun gun = new Gun();

    public GunItem(Item.Properties properties)
    {
        super(properties);
    }

    public void setGun(NetworkGunManager.Supplier supplier)
    {
        this.gun = supplier.getGun();
    }

    public Gun getGun()
    {
        return this.gun;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt){
        return new GunEnergyStorage(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag)
    {
        Gun modifiedGun = this.getModifiedGun(stack);

        Item ammo = ForgeRegistries.ITEMS.getValue(modifiedGun.getProjectile().getItem());
        if(ammo != null && (!Gun.hasInfiniteAmmo(stack) && !Gun.usesEnergy(stack)))
        {
        	tooltip.add(Component.translatable("info.cgm.ammo_type", Component.translatable(ammo.getDescriptionId()).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY));
        }

        String additionalDamageText = "";
        CompoundTag tagCompound = stack.getTag();
        if(tagCompound != null)
        {
            if(Gun.hasInfiniteAmmo(stack))
            {
                if(!Gun.usesEnergy(stack))
                	tooltip.add(Component.translatable("info.cgm.ignore_ammo").withStyle(ChatFormatting.AQUA));
            }
            else
            {
                int ammoCount = tagCompound.getInt("AmmoCount");
                tooltip.add(Component.translatable("info.cgm.ammo", ChatFormatting.WHITE.toString() + ammoCount + "/" + GunCompositeStatHelper.getAmmoCapacity(stack, modifiedGun)).withStyle(ChatFormatting.GRAY));
            }
            if(Gun.usesEnergy(stack))
            {
                    int energy = tagCompound.getInt("Energy");
                	tooltip.add(Component.translatable("info.cgm.energy", ChatFormatting.WHITE.toString() + energy + "/" + modifiedGun.getGeneral().getEnergyCapacity()).withStyle(ChatFormatting.DARK_AQUA));
            }
        }
        
        if (Screen.hasShiftDown())
        {
        	tooltip.add(Component.translatable("info.cgm.gun_details").withStyle(ChatFormatting.GOLD));

        	// Additional Damage
        	if(tagCompound != null)
            {
                if(tagCompound.contains("AdditionalDamage", Tag.TAG_ANY_NUMERIC))
                {
                    float additionalDamage = tagCompound.getFloat("AdditionalDamage");
                    additionalDamage += GunModifierHelper.getAdditionalDamage(stack);

                    if(additionalDamage > 0)
                    {
                        additionalDamageText = ChatFormatting.GREEN + " +" + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(additionalDamage);
                    }
                    else if(additionalDamage < 0)
                    {
                        additionalDamageText = ChatFormatting.RED + " " + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(additionalDamage);
                    }
                }
            }
        	
        	// Damage
            float damage = modifiedGun.getProjectile().getDamage();
            damage = GunModifierHelper.getModifiedProjectileDamage(stack, damage);
            damage = GunEnchantmentHelper.getAcceleratorDamage(stack, damage);
            tooltip.add(Component.translatable("info.cgm.damage", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage) + additionalDamageText).withStyle(ChatFormatting.GRAY));

        	// Fire Rate
            int rate = modifiedGun.getGeneral().getRate();
            rate = GunCompositeStatHelper.getCompositeBaseRate(stack, modifiedGun);
            tooltip.add(Component.translatable("info.cgm.rate", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(rate)).withStyle(ChatFormatting.GRAY));

        	// Recoil
            float recoil = modifiedGun.getGeneral().getRecoilAngle();
            //recoil = GunCompositeStatHelper.getCompositeRecoil(stack, modifiedGun);
            recoil *= 1.0F - GunModifierHelper.getRecoilModifier(stack);
            //tooltip.add(Component.translatable("info.cgm.recoil", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(recoil)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("info.cgm.recoil").withStyle(ChatFormatting.GRAY).append(Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(recoil) + "°").withStyle(ChatFormatting.WHITE)));
            
            // Spread
            float spread = modifiedGun.getGeneral().getSpread();
            float minSpread = modifiedGun.getGeneral().getRestingSpread();
            spread = GunCompositeStatHelper.getCompositeSpread(stack, modifiedGun);
            minSpread = GunCompositeStatHelper.getCompositeMinSpread(stack, modifiedGun);
            boolean isAlwaysSpread = (minSpread<=0 && modifiedGun.getGeneral().isAlwaysSpread());
            minSpread = (minSpread<=0 ? (isAlwaysSpread ? spread : 0) : minSpread);
            if ((minSpread!=spread) && ((minSpread>0) || (!isAlwaysSpread)))
            tooltip.add(Component.translatable("info.cgm.spread").withStyle(ChatFormatting.GRAY).append(Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(minSpread) + "° Min. - " + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(spread) + "° Max.").withStyle(ChatFormatting.WHITE)));
            else
            tooltip.add(Component.translatable("info.cgm.spread").withStyle(ChatFormatting.GRAY).append(Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(spread) + "°").withStyle(ChatFormatting.WHITE)));

        	// ADS Spread
            float adsSpread = spread * (1-(modifiedGun.getGeneral().getSpreadAdsReduction()));
            float adsMinSpread = minSpread * (1-(modifiedGun.getGeneral().getSpreadAdsReduction()));
            if (adsSpread!=spread)
            {
            	//if (adsSpread>0)
            	//tooltip.add(Component.translatable("info.cgm.ads_spread").withStyle(ChatFormatting.GRAY).append(Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(adsSpread) + "°").withStyle(ChatFormatting.WHITE)));
            	if ((adsMinSpread!=adsSpread) && ((adsMinSpread>0) || (!isAlwaysSpread)))
                tooltip.add(Component.translatable("info.cgm.ads_spread").withStyle(ChatFormatting.GRAY).append(Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(adsMinSpread) + "° Min. - " + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(adsSpread) + "° Max.").withStyle(ChatFormatting.WHITE)));
                else
                tooltip.add(Component.translatable("info.cgm.ads_spread").withStyle(ChatFormatting.GRAY).append(Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(adsSpread) + "°").withStyle(ChatFormatting.WHITE)));
            }
            
        }
        else
        {
        	// Helper tooltips
        	tooltip.add(Component.translatable("info.cgm.attachment_help", KeyBinds.KEY_ATTACHMENTS.getTranslatedKeyMessage().getString().toUpperCase(Locale.ENGLISH)).withStyle(ChatFormatting.YELLOW));
        	tooltip.add(Component.translatable("info.cgm.gun_details_help").withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity)
    {
        return true;
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> stacks)
    {
        if(this.allowedIn(group))
        {
            ItemStack stack = new ItemStack(this);
            stack.getOrCreateTag().putInt("AmmoCount", this.gun.getGeneral().getMaxAmmo());
            stack.getOrCreateTag().putInt("Energy", 0);
            stacks.add(stack);
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return slotChanged;
    }

    @Override
    public boolean isBarVisible(ItemStack stack)
    {
        CompoundTag tagCompound = stack.getOrCreateTag();
        Gun modifiedGun = this.getModifiedGun(stack);
        return !tagCompound.getBoolean("IgnoreAmmo") && tagCompound.getInt("AmmoCount") < GunCompositeStatHelper.getAmmoCapacity(stack, modifiedGun);
    }

    @Override
    public int getBarWidth(ItemStack stack)
    {
        CompoundTag tagCompound = stack.getOrCreateTag();
        Gun modifiedGun = this.getModifiedGun(stack);
        return (int) (13.0 * (tagCompound.getInt("AmmoCount") / (double) GunCompositeStatHelper.getAmmoCapacity(stack, modifiedGun)));
    }

    @Override
    public int getBarColor(ItemStack stack)
    {
        return Objects.requireNonNull(ChatFormatting.YELLOW.getColor());
    }

    public Gun getModifiedGun(ItemStack stack)
    {
        CompoundTag tagCompound = stack.getTag();
        if(tagCompound != null && tagCompound.contains("Gun", Tag.TAG_COMPOUND))
        {
            return this.modifiedGunCache.computeIfAbsent(tagCompound, item ->
            {
                if(tagCompound.getBoolean("Custom"))
                {
                    return Gun.create(tagCompound.getCompound("Gun"));
                }
                else
                {
                    Gun gunCopy = this.gun.copy();
                    gunCopy.deserializeNBT(tagCompound.getCompound("Gun"));
                    return gunCopy;
                }
            });
        }
        if(GunMod.isDebugging())
        {
            return Debug.getGun(this);
        }
        return this.gun;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
    {
        if(enchantment.category == EnchantmentTypes.SEMI_AUTO_GUN)
        {
            Gun modifiedGun = this.getModifiedGun(stack);
            return !modifiedGun.getGeneral().isAuto() || modifiedGun.getFireModes().hasSemiMode();
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public boolean isEnchantable(ItemStack stack)
    {
        return this.getMaxStackSize(stack) == 1;
    }

    @Override
    public int getEnchantmentValue()
    {
        return 5;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer)
    {
        consumer.accept(new IClientItemExtensions()
        {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer()
            {
                return new GunItemStackRenderer();
            }
        });
    }
    
    // Everything below is related to energy storage and transfer mechanics via Forge's EnergyStorage capability.

    public static void setCurrentEnergy(ItemStack stack, int Amount)
    {
    	stack.getOrCreateTag().putInt("Energy", Amount);
    }

    public static int getCurrentEnergy(ItemStack stack)
    {
    	return stack.getOrCreateTag().getInt("Energy");
    }

    public static int getEnergyCapacity(ItemStack stack)
    {
    	GunItem gunItem = (GunItem) stack.getItem();
    	Gun modifiedGun = gunItem.getModifiedGun(stack);
    	return modifiedGun.getGeneral().getEnergyCapacity();
    }

    public static int getTransferCap(ItemStack stack)
    {
    	GunItem gunItem = (GunItem) stack.getItem();
    	Gun modifiedGun = gunItem.getModifiedGun(stack);
    	return modifiedGun.getGeneral().getEnergyCapacity()/80;
    }
    
    public static class GunEnergyStorage implements IEnergyStorage, ICapabilityProvider {

        
        private final LazyOptional<IEnergyStorage> user = LazyOptional.of(() -> this);
        private final ItemStack stack;
        
        public GunEnergyStorage(ItemStack stack){
            this.stack = stack;
        }
    
        public void setCurrentEnergy(int amount){
            GunItem.setCurrentEnergy(this.stack, amount);
        }
    
        @Override
        public int getEnergyStored(){
            return GunItem.getCurrentEnergy(this.stack);
        }
    
        @Override
        public int getMaxEnergyStored(){
            return GunItem.getEnergyCapacity(this.stack);
        }
    
        @Override
        public boolean canExtract(){
            return this.getEnergyStored() > 0;
        }
    
        @Override
        public boolean canReceive(){
            return this.getEnergyStored() < this.getMaxEnergyStored();
        }
    
        @Override
        public int receiveEnergy(int receiveEnergy, boolean simulate){
            if (!canReceive()){
                return 0;
            }
        
            int stored = this.getEnergyStored();
            int energyReceived = Math.min(this.getMaxEnergyStored() - stored, Math.min(GunItem.getTransferCap(this.stack), receiveEnergy));
            if (!simulate){
                this.setCurrentEnergy(stored + energyReceived);
            }
            return energyReceived;
        }
    
        @Override
        public int extractEnergy(int extractEnergy, boolean simulate){
            if (!canExtract()){
                return 0;
            }
        
            int stored = this.getEnergyStored();
            int energyExtracted = Math.min(stored, Math.min(GunItem.getTransferCap(this.stack), extractEnergy));
            if (!simulate){
                this.setCurrentEnergy(stored - energyExtracted);
            }
            return energyExtracted;
        }
        
        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side){
            return CapabilityEnergy.ENERGY.orEmpty(cap, user);
        }
    }
}
