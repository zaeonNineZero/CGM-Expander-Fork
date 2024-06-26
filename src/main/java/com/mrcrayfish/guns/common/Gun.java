package com.mrcrayfish.guns.common;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.annotation.Ignored;
import com.mrcrayfish.guns.annotation.Optional;
import com.mrcrayfish.guns.client.ClientHandler;
import com.mrcrayfish.guns.compat.BackpackHelper;
import com.mrcrayfish.guns.debug.Debug;
import com.mrcrayfish.guns.debug.IDebugWidget;
import com.mrcrayfish.guns.debug.IEditorMenu;
import com.mrcrayfish.guns.debug.client.screen.EditorScreen;
import com.mrcrayfish.guns.debug.client.screen.widget.DebugButton;
import com.mrcrayfish.guns.debug.client.screen.widget.DebugSlider;
import com.mrcrayfish.guns.debug.client.screen.widget.DebugToggle;
import com.mrcrayfish.guns.init.ModEnchantments;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.item.ScopeItem;
import com.mrcrayfish.guns.item.attachment.IAttachment;
import com.mrcrayfish.guns.item.attachment.impl.Scope;
import com.mrcrayfish.guns.util.GunJsonUtil;
import com.mrcrayfish.guns.util.SuperBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class Gun implements INBTSerializable<CompoundTag>, IEditorMenu
{
    protected General general = new General();
    protected FireModes fireModes = new FireModes();
    protected Projectile projectile = new Projectile();
    protected Sounds sounds = new Sounds();
    protected Display display = new Display();
    protected Modules modules = new Modules();

    public General getGeneral()
    {
        return this.general;
    }

    public FireModes getFireModes()
    {
        return this.fireModes;
    }

    public Projectile getProjectile()
    {
        return this.projectile;
    }

    public Sounds getSounds()
    {
        return this.sounds;
    }

    public Display getDisplay()
    {
        return this.display;
    }

    public Modules getModules()
    {
        return this.modules;
    }

    @Override
    public Component getEditorLabel()
    {
        return Component.literal("Gun");
    }

    @Override
    public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets)
    {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ItemStack heldItem = Objects.requireNonNull(Minecraft.getInstance().player).getMainHandItem();
            ItemStack scope = Gun.getScopeStack(heldItem);
            if(scope.getItem() instanceof ScopeItem scopeItem)
            {
                widgets.add(Pair.of(scope.getItem().getName(scope), () -> new DebugButton(Component.literal("Edit"), btn -> {
                    Minecraft.getInstance().setScreen(ClientHandler.createEditorScreen(Debug.getScope(scopeItem)));
                })));
            }

            widgets.add(Pair.of(this.modules.getEditorLabel(), () -> new DebugButton(Component.literal(">"), btn -> {
                Minecraft.getInstance().setScreen(ClientHandler.createEditorScreen(this.modules));
            })));
        });
    }

    public static class General implements INBTSerializable<CompoundTag>
    {
        @Optional
        private boolean auto = false;
        private int rate;
        @Optional
        private int burstCount = 0;
        @Optional
        private int burstCooldown = -1;
        @Ignored
        private GripType gripType = GripType.ONE_HANDED;
        @Optional
        private int defaultColor = -1;
        private int maxAmmo;
        @Optional
        private int overCapacityAmmo = 0;
        @Optional
        private boolean infiniteAmmo = false;
        @Optional
        private int reloadAmount = 1;
        @Optional
        private int itemsPerAmmo = 1;
        @Optional
        private int ammoPerItem = 1;
        @Optional
        private int reloadRate = 10;
        @Optional
        private int reloadStartDelay = 5;
        @Optional
        private int reloadInterruptDelay = 5;
        @Optional
        private int reloadEndDelay = -1;
        @Optional
        private boolean useMagReload = false;
        @Optional
        private int magReloadTime = 20;
        @Optional
        private float reloadAllowedCooldown = 1F;
        @Optional
        private int energyCapacity = 0;
        @Optional
        private int energyPerShot = 0;
        @Optional
        private float recoilAngle;
        @Optional
        private float recoilKick;
        @Optional
        private float recoilDurationOffset;
        @Optional
        private float recoilAdsReduction = 0.2F;
        @Optional
        private int projectileAmount = 1;
        @Optional
        private boolean alwaysSpread;
        @Optional
        private float spread;
        @Optional
        private float restingSpread = 0F;
        @Optional
        private float spreadAdsReduction = 0.5F;
        @Optional
        private boolean useShotgunSpread = false;
        @Optional
        private double adsSpeed = 1;
        @Optional
        private boolean doRampUp = false;
        @Optional
        private int rampUpShotsNeeded = 8;

        @Override
        public CompoundTag serializeNBT()
        {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("Auto", this.auto);
            tag.putInt("Rate", this.rate);
            tag.putInt("BurstCount", this.burstCount);
            tag.putInt("BurstCooldown", this.burstCooldown);
            tag.putString("GripType", this.gripType.getId().toString());
            tag.putInt("DefaultColor", this.defaultColor);
            tag.putInt("MaxAmmo", this.maxAmmo);
            tag.putInt("OverCapacityAmmo", this.overCapacityAmmo);
            tag.putBoolean("InfiniteAmmo", this.infiniteAmmo);
            tag.putInt("ReloadAmount", this.reloadAmount);
            tag.putInt("ItemsPerAmmo", this.itemsPerAmmo);
            tag.putInt("AmmoPerItem", this.ammoPerItem);
            tag.putInt("ReloadRate", this.reloadRate);
            tag.putInt("ReloadStartDelay", this.reloadStartDelay);
            tag.putInt("ReloadInterruptDelay", this.reloadInterruptDelay);
            tag.putInt("ReloadEndDelay", this.reloadEndDelay);
            tag.putBoolean("UseMagReload", this.useMagReload);
            tag.putInt("MagReloadTime", this.magReloadTime);
            tag.putFloat("ReloadAllowedCooldown", this.reloadAllowedCooldown);
            tag.putInt("EnergyCapacity", this.energyCapacity);
            tag.putInt("EnergyPerShot", this.energyPerShot);
            tag.putFloat("RecoilAngle", this.recoilAngle);
            tag.putFloat("RecoilKick", this.recoilKick);
            tag.putFloat("RecoilDurationOffset", this.recoilDurationOffset);
            tag.putFloat("RecoilAdsReduction", this.recoilAdsReduction);
            tag.putInt("ProjectileAmount", this.projectileAmount);
            tag.putBoolean("AlwaysSpread", this.alwaysSpread);
            tag.putFloat("Spread", this.spread);
            tag.putFloat("RestingSpread", this.restingSpread);
            tag.putFloat("SpreadAdsReduction", this.spreadAdsReduction);
            tag.putBoolean("UseShotgunSpread", this.useShotgunSpread);
            tag.putDouble("ADSSpeed", this.adsSpeed);
            tag.putBoolean("DoRampUp", this.doRampUp);
            tag.putInt("RampUpShotsNeeded", this.rampUpShotsNeeded);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag)
        {
            if(tag.contains("Auto", Tag.TAG_ANY_NUMERIC))
            {
                this.auto = tag.getBoolean("Auto");
            }
            if(tag.contains("Rate", Tag.TAG_ANY_NUMERIC))
            {
                this.rate = tag.getInt("Rate");
            }
            if(tag.contains("BurstCount", Tag.TAG_ANY_NUMERIC))
            {
                this.burstCount = tag.getInt("BurstCount");
            }
            if(tag.contains("BurstCooldown", Tag.TAG_ANY_NUMERIC))
            {
                this.burstCooldown = tag.getInt("BurstCooldown");
            }
            if(tag.contains("GripType", Tag.TAG_STRING))
            {
                this.gripType = GripType.getType(ResourceLocation.tryParse(tag.getString("GripType")));
            }
            if(tag.contains("DefaultColor", Tag.TAG_ANY_NUMERIC))
            {
                this.defaultColor = tag.getInt("DefaultColor");
            }
            if(tag.contains("MaxAmmo", Tag.TAG_ANY_NUMERIC))
            {
                this.maxAmmo = tag.getInt("MaxAmmo");
            }
            if(tag.contains("OverCapacityAmmo", Tag.TAG_ANY_NUMERIC))
            {
                this.overCapacityAmmo = tag.getInt("OverCapacityAmmo");
            }
            if(tag.contains("InfiniteAmmo", Tag.TAG_ANY_NUMERIC))
            {
                this.infiniteAmmo = tag.getBoolean("InfiniteAmmo");
            }
            
            if(tag.contains("ReloadAmount", Tag.TAG_ANY_NUMERIC))
            {
                this.reloadAmount = tag.getInt("ReloadAmount");
            }
            else
          	if(tag.contains("ReloadSpeed", Tag.TAG_ANY_NUMERIC))
          	{
            	this.reloadAmount = tag.getInt("ReloadSpeed");
         	}
            
            if(tag.contains("ItemsPerAmmo", Tag.TAG_ANY_NUMERIC))
            {
                this.itemsPerAmmo = tag.getInt("ItemsPerAmmo");
            }
            if(tag.contains("AmmoPerItem", Tag.TAG_ANY_NUMERIC))
            {
                this.ammoPerItem = tag.getInt("AmmoPerItem");
            }
            if(tag.contains("ReloadRate", Tag.TAG_ANY_NUMERIC))
            {
                this.reloadRate = tag.getInt("ReloadRate");
            }
            if(tag.contains("ReloadStartDelay", Tag.TAG_ANY_NUMERIC))
            {
                this.reloadStartDelay = tag.getInt("ReloadStartDelay");
            }
            if(tag.contains("ReloadInterruptDelay", Tag.TAG_ANY_NUMERIC))
            {
                this.reloadInterruptDelay = tag.getInt("ReloadInterruptDelay");
            }
            if(tag.contains("ReloadEndDelay", Tag.TAG_ANY_NUMERIC))
            {
                this.reloadEndDelay = tag.getInt("ReloadEndDelay");
            }
            if(tag.contains("UseMagReload", Tag.TAG_ANY_NUMERIC))
            {
                this.useMagReload = tag.getBoolean("UseMagReload");
            }
            if(tag.contains("MagReloadTime", Tag.TAG_ANY_NUMERIC))
            {
                this.magReloadTime = tag.getInt("MagReloadTime");
            }
            if(tag.contains("ReloadAllowedCooldown", Tag.TAG_ANY_NUMERIC))
            {
                this.reloadAllowedCooldown = tag.getFloat("ReloadAllowedCooldown");
            }
            if(tag.contains("EnergyCapacity", Tag.TAG_ANY_NUMERIC))
            {
                this.energyCapacity = tag.getInt("EnergyCapacity");
            }
            if(tag.contains("EnergyPerShot", Tag.TAG_ANY_NUMERIC))
            {
                this.energyPerShot = tag.getInt("EnergyPerShot");
            }
            if(tag.contains("RecoilAngle", Tag.TAG_ANY_NUMERIC))
            {
                this.recoilAngle = tag.getFloat("RecoilAngle");
            }
            if(tag.contains("RecoilKick", Tag.TAG_ANY_NUMERIC))
            {
                this.recoilKick = tag.getFloat("RecoilKick");
            }
            if(tag.contains("RecoilDurationOffset", Tag.TAG_ANY_NUMERIC))
            {
                this.recoilDurationOffset = tag.getFloat("RecoilDurationOffset");
            }
            if(tag.contains("RecoilAdsReduction", Tag.TAG_ANY_NUMERIC))
            {
                this.recoilAdsReduction = tag.getFloat("RecoilAdsReduction");
            }
            if(tag.contains("ProjectileAmount", Tag.TAG_ANY_NUMERIC))
            {
                this.projectileAmount = tag.getInt("ProjectileAmount");
            }
            if(tag.contains("AlwaysSpread", Tag.TAG_ANY_NUMERIC))
            {
                this.alwaysSpread = tag.getBoolean("AlwaysSpread");
            }
            if(tag.contains("Spread", Tag.TAG_ANY_NUMERIC))
            {
                this.spread = tag.getFloat("Spread");
            }
            if(tag.contains("RestingSpread", Tag.TAG_ANY_NUMERIC))
            {
                this.restingSpread = tag.getFloat("RestingSpread");
            }
            if(tag.contains("SpreadAdsReduction", Tag.TAG_ANY_NUMERIC))
            {
                this.spreadAdsReduction = tag.getFloat("SpreadAdsReduction");
            }
            if(tag.contains("UseShotgunSpread", Tag.TAG_ANY_NUMERIC))
            {
                this.useShotgunSpread = tag.getBoolean("UseShotgunSpread");
            }
            if(tag.contains("ADSSpeed", Tag.TAG_ANY_NUMERIC))
            {
                this.adsSpeed = tag.getDouble("ADSSpeed");
            }
            if(tag.contains("DoRampUp", Tag.TAG_ANY_NUMERIC))
            {
                this.doRampUp = tag.getBoolean("DoRampUp");
            }
            if(tag.contains("RampUpShotsNeeded", Tag.TAG_ANY_NUMERIC))
            {
                this.rampUpShotsNeeded = tag.getInt("RampUpShotsNeeded");
            }
        }

        public JsonObject toJsonObject()
        {
            Preconditions.checkArgument(this.rate > 0, "Rate must be more than zero");
            Preconditions.checkArgument(this.defaultColor == -1 || this.defaultColor>=0, "Default color must be a valid RGBA-integer-format color; use -1 to disable this.");
            Preconditions.checkArgument(this.maxAmmo > 0, "Max ammo must be more than zero");
            Preconditions.checkArgument(this.burstCount >= 0, "Burst count cannot be negative; set to zero to disable bursts");
            Preconditions.checkArgument(this.burstCount != 1, "Burst count must be greater than one, or equal to zero; set to zero to disable bursts");
            Preconditions.checkArgument(this.burstCooldown >= 0, "Burst cooldown cannot be negative; set to zero to disable the cooldown");
            Preconditions.checkArgument(this.overCapacityAmmo > 0, "Over Capacity bonus ammo must be more than zero");
            Preconditions.checkArgument(this.reloadAmount >= 1, "Reload amount must be more than or equal to one");
            Preconditions.checkArgument(this.itemsPerAmmo >= 1, "Items Per Ammo must be more than or equal to one");
            Preconditions.checkArgument(this.ammoPerItem >= 1, "Ammo Per Item must be more than or equal to one");
            Preconditions.checkArgument(this.reloadRate >= 1, "Reload rate must be more than or equal to one");
            Preconditions.checkArgument(this.magReloadTime >= 1, "Mag reload time must be more than or equal to one");
            Preconditions.checkArgument(this.energyCapacity >= 0, "Energy capacity must be more than or equal to zero");
            Preconditions.checkArgument(this.energyPerShot >= 0, "Energy usage per shot must be more than or equal to zero");
            Preconditions.checkArgument(this.reloadAllowedCooldown >= 0.0F && this.reloadAllowedCooldown <= 1.0F, "Reload allowed cooldown must be between 0.0 and 1.0");
            Preconditions.checkArgument(this.recoilAngle >= 0.0F, "Recoil angle must be more than or equal to zero");
            Preconditions.checkArgument(this.recoilKick >= 0.0F, "Recoil kick must be more than or equal to zero");
            Preconditions.checkArgument(this.recoilDurationOffset >= 0.0F && this.recoilDurationOffset <= 1.0F, "Recoil duration offset must be between 0.0 and 1.0");
            Preconditions.checkArgument(this.recoilAdsReduction >= 0.0F && this.recoilAdsReduction <= 1.0F, "Recoil ADS reduction must be between 0.0 and 1.0");
            Preconditions.checkArgument(this.projectileAmount >= 1, "Projectile amount must be more than or equal to one");
            Preconditions.checkArgument(this.spread >= 0.0F, "Spread must be more than or equal to zero");
            Preconditions.checkArgument(this.restingSpread >= 0.0F, "Spread must be more than or equal to zero");
            Preconditions.checkArgument(this.spreadAdsReduction >= 0.0F && this.spreadAdsReduction <= 1.0F, "Spread ADS reduction must be between 0.0 and 1.0");
            Preconditions.checkArgument(this.adsSpeed > 0.0, "ADS Speed must be more than zero");
            Preconditions.checkArgument(this.rampUpShotsNeeded > 0.0, "Shots to Full Ramp Up must be more than zero");
            JsonObject object = new JsonObject();
            if(this.auto) object.addProperty("auto", true);
            object.addProperty("rate", this.rate);
            if(this.burstCount != 0) object.addProperty("burstCount", this.burstCount);
            if(this.burstCooldown != -1) object.addProperty("burstCooldown", this.burstCooldown);
            object.addProperty("gripType", this.gripType.getId().toString());
            if(this.defaultColor != 1) object.addProperty("defaultColor", this.defaultColor);
            object.addProperty("maxAmmo", this.maxAmmo);
            object.addProperty("overCapacityAmmo", this.overCapacityAmmo);
            if(this.infiniteAmmo != false) object.addProperty("infiniteAmmo", this.infiniteAmmo);
            if(this.reloadAmount != 1) object.addProperty("reloadAmount", this.reloadAmount);
            if(this.itemsPerAmmo != 1) object.addProperty("itemsPerAmmo", this.itemsPerAmmo);
            if(this.ammoPerItem != 1) object.addProperty("ammoPerItem", this.ammoPerItem);
            if(this.reloadRate != 10) object.addProperty("reloadRate", this.reloadRate);
            if(this.reloadStartDelay != 5) object.addProperty("reloadStartDelay", this.reloadStartDelay);
            if(this.reloadInterruptDelay != 5) object.addProperty("reloadInterruptDelay", this.reloadInterruptDelay);
            if(this.reloadEndDelay != -1) object.addProperty("reloadEndDelay", this.reloadEndDelay);
            if(this.useMagReload != false) object.addProperty("useMagReload", this.useMagReload);
            if(this.magReloadTime != 20) object.addProperty("magReloadTime", this.magReloadTime);
            if(this.reloadAllowedCooldown != 1) object.addProperty("reloadAllowedCooldown", this.reloadAllowedCooldown);
            if(this.recoilAngle != 0.0F) object.addProperty("recoilAngle", this.recoilAngle);
            if(this.recoilKick != 0.0F) object.addProperty("recoilKick", this.recoilKick);
            if(this.recoilDurationOffset != 0.0F) object.addProperty("recoilDurationOffset", this.recoilDurationOffset);
            if(this.recoilAdsReduction != 0.2F) object.addProperty("recoilAdsReduction", this.recoilAdsReduction);
            if(this.projectileAmount != 1) object.addProperty("projectileAmount", this.projectileAmount);
            if(this.alwaysSpread) object.addProperty("alwaysSpread", true);
            if(this.spread != 0.0F) object.addProperty("spread", this.spread);
            if(this.restingSpread != 0.0F) object.addProperty("restingSpread", this.restingSpread);
            if(this.spreadAdsReduction != 0.5F) object.addProperty("spreadAdsReduction", this.spread);
            if(this.useShotgunSpread) object.addProperty("useShotgunSpread", true);
            if(this.adsSpeed != 1) object.addProperty("adsSpeed", this.adsSpeed);
            if(this.doRampUp) object.addProperty("doRampUp", false);
            if(this.rampUpShotsNeeded != 8) object.addProperty("rampUpShotsNeeded", this.rampUpShotsNeeded);
            return object;
        }

        /**
         * @return A copy of the general get
         */
        public General copy()
        {
            General general = new General();
            general.auto = this.auto;
            general.rate = this.rate;
            general.burstCount = this.burstCount;
            general.burstCooldown = this.burstCooldown;
            general.gripType = this.gripType;
            general.defaultColor = this.defaultColor;
            general.maxAmmo = this.maxAmmo;
            general.overCapacityAmmo = this.overCapacityAmmo;
            general.infiniteAmmo = this.infiniteAmmo;
            general.reloadAmount = this.reloadAmount;
            general.ammoPerItem = this.ammoPerItem;
            general.reloadAmount = this.reloadAmount;
            general.reloadRate = this.reloadRate;
            general.reloadStartDelay = this.reloadStartDelay;
            general.reloadInterruptDelay = this.reloadInterruptDelay;
            general.reloadEndDelay = this.reloadEndDelay;
            general.useMagReload = this.useMagReload;
            general.magReloadTime = this.magReloadTime;
            general.energyCapacity = this.energyCapacity;
            general.energyPerShot = this.energyPerShot;
            general.reloadAllowedCooldown = this.reloadAllowedCooldown;
            general.recoilAngle = this.recoilAngle;
            general.recoilKick = this.recoilKick;
            general.recoilDurationOffset = this.recoilDurationOffset;
            general.recoilAdsReduction = this.recoilAdsReduction;
            general.projectileAmount = this.projectileAmount;
            general.alwaysSpread = this.alwaysSpread;
            general.spread = this.spread;
            general.restingSpread = this.restingSpread;
            general.spreadAdsReduction = this.spreadAdsReduction;
            general.useShotgunSpread = this.useShotgunSpread;
            general.adsSpeed = this.adsSpeed;
            general.doRampUp = this.doRampUp;
            general.rampUpShotsNeeded = this.rampUpShotsNeeded;
            return general;
        }

        /**
         * @return If this gun is automatic or not
         */
        public boolean isAuto()
        {
            return this.auto;
        }

        /**
         * @return The fire rate of this weapon in ticks
         */
        public int getRate()
        {
            return this.rate;
        }

        /**
         * @return If the gun fires in bursts or not
         */
		public boolean hasBurstFire() {
			return this.burstCount > 0;
		}

        /**
         * @return How many shots this weapon fires per burst.
         * A value of zero disables burst fire.
         */
        public int getBurstCount()
        {
            return this.burstCount;
        }

        /**
         * @return The extra cooldown period after a burst finishes, in ticks
         */
        public int getBurstCooldown()
        {
        	/*if (burstCooldown<0)
        	{
        		int defaultBurstCooldown = (isAuto() ? 3 : 1 );
        		return defaultBurstCooldown;
        	}*/
        	
            return this.burstCooldown;
        }

        /**
         * @return The type of grip this weapon uses
         */
        public GripType getGripType()
        {
            return this.gripType;
        }

        /**
         * @return The default color of the gun without dyes
         * A value of -1 indicates no default color override,
         * in which case the standard white color will be used.
         */
        public int getDefaultColor()
        {
            return this.defaultColor;
        }

        /**
         * @return The maximum amount of ammo this weapon can hold
         */
        public int getMaxAmmo()
        {
            return this.maxAmmo;
        }

        /**
         * @return The bonus to MaxAmmo provided by one level of the Over Capacity enchantment.
         */
        public int getOverCapacityAmmo()
        {
            return this.overCapacityAmmo;
        }

        /**
         * @return Whether the gun has infinite ammo.
         */
        public boolean getInfiniteAmmo()
        {
            return this.infiniteAmmo;
        }


        /**
         * @return The amount of ammo to add to the weapon each reload cycle
         */
        public int getReloadAmount()
        {
            return this.reloadAmount;
        }
        
        /**
         * @return The amount of ammo to add to the weapon each reload cycle
         */
        public int getItemsPerAmmo()
        {
            return this.itemsPerAmmo;
        }
        
        /**
         * @return The amount of ammo to add to the weapon each reload cycle
         */
        public int getAmmoPerItem()
        {
            return this.ammoPerItem;
        }

        /**
         * @return The speed of each reload cycle in ticks. The lower the value, the faster each reload cycle.
         */
        public int getReloadRate()
        {
            return this.reloadRate;
        }

        /**
         * @return The delay (in ticks) before the main reload cycle starts.
         * This is also used as a fallback for the Interrupt Delay.
         */
        public int getReloadStartDelay()
        {
            return this.reloadStartDelay;
        }

        /**
         * @return The delay (in ticks) that occurs after a reload is interrupted.
         */
        public int getReloadInterruptDelay()
        {
        	if (this.reloadInterruptDelay<0)
                return getReloadEndDelay();
        	
        	return this.reloadInterruptDelay;
        }

        /**
         * @return The delay (in ticks) that occurs after a complete reload.
         * This is not used when a reload ends via interruption.
         */
        public int getReloadEndDelay()
        {
            if (this.reloadEndDelay<0)
                return this.reloadStartDelay;
        	
        	return this.reloadEndDelay;
        }

        /**
         * @return Whether to use the new magazine-style reload, where all ammo is loaded at the end of the reload cycle.
         */
        public boolean usesMagReload()
        {
            return this.useMagReload;
        }
        /**
         * @return Whether to use the new magazine-style reload, where all ammo is loaded at the end of the reload cycle.
         * This is a legacy version of the 'usesMagReload' method with identical functionality.
         */
        public boolean getUseMagReload()
        {
            return this.useMagReload;
        }

        /**
         * @return The speed of magazine reloads in ticks. The lower the value, shorter the reload time.
         */
        public int getMagReloadTime()
        {
            return this.magReloadTime;
        }

        /**
         * @return The speed of magazine reloads in ticks. The lower the value, shorter the reload time.
         */
        public int getEnergyCapacity()
        {
            return this.energyCapacity;
        }

        /**
         * @return The speed of magazine reloads in ticks. The lower the value, shorter the reload time.
         */
        public int getEnergyPerShot()
        {
            return this.energyPerShot;
        }

        /**
         * @return The amount of recoil this gun produces upon firing in degrees
         */
        public float getReloadAllowedCooldown()
        {
            return this.reloadAllowedCooldown;
        }

        /**
         * @return The amount of recoil this gun produces upon firing in degrees
         */
        public float getRecoilAngle()
        {
            return this.recoilAngle;
        }

        /**
         * @return The amount of kick this gun produces upon firing
         */
        public float getRecoilKick()
        {
            return this.recoilKick;
        }

        /**
         * @return The duration offset for recoil. This reduces the duration of recoil animation
         */
        public float getRecoilDurationOffset()
        {
            return this.recoilDurationOffset;
        }

        /**
         * @return The amount of recoil reduction applied when aiming with this weapon.
         */
        public float getRecoilAdsReduction()
        {
            return this.recoilAdsReduction;
        }

        /**
         * @return The amount of projectiles this weapon fires
         */
        public int getProjectileAmount()
        {
            return this.projectileAmount;
        }

        /**
         * @return If this weapon should always spread it's projectiles according to {@link #getSpread()}
         */
        public boolean isAlwaysSpread()
        {
            return this.alwaysSpread;
        }

        /**
         * @return The maximum amount of degrees applied to the initial pitch and yaw direction of
         * the fired projectile.
         */
        public float getSpread()
        {
            return this.spread;
        }

        /**
         * @return The resting spread of the gun - using this overrides the alwaysSpread parameter.
         */
        public float getRestingSpread()
        {
            return this.restingSpread;
        }

        /**
         * @return The amount of spread reduction applied when aiming with this weapon.
         */
        public float getSpreadAdsReduction()
        {
            return this.spreadAdsReduction;
        }

        /**
         * @return If enabled, spread reduction from non-barrel attachments will be heavily reduced.
         */
        public boolean usesShotgunSpread()
        {
            return this.useShotgunSpread;
        }

        /**
         * @return The base speed modifier to the gun's aiming speed.
         */
        public double getADSSpeed()
        {
            return this.adsSpeed;
        }

        /**
         * @return Whether the gun has the Ramp Up effect.
         */
        public boolean hasDoRampUp()
        {
            return this.doRampUp;
        }

        /**
         * @return Whether the gun has the Ramp Up effect.
         */
        public int getRampUpShotsNeeded()
        {
            return this.rampUpShotsNeeded;
        }
    }

    public static class FireModes implements INBTSerializable<CompoundTag>
    {
    	@Optional
        private boolean useFireModes;
    	@Optional
        private boolean hasSemiMode;
    	@Optional
        private boolean hasAutoMode;
    	@Optional
        private boolean hasBurstMode;
    	@Optional
        private boolean useAutoBurst;
    	@Optional
        private int burstCount;

        @Override
        public CompoundTag serializeNBT()
        {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("UseFireModes", this.useFireModes);
            tag.putBoolean("HasSemiMode", this.hasSemiMode);
            tag.putBoolean("HasAutoMode", this.hasAutoMode);
            tag.putBoolean("HasBurstMode", this.hasBurstMode);
            tag.putBoolean("UseAutoBurst", this.useAutoBurst);
            tag.putInt("BurstCount", this.burstCount);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag)
        {
            if(tag.contains("UseFireModes", Tag.TAG_ANY_NUMERIC))
            {
                this.useFireModes = tag.getBoolean("UseFireModes");
            }
            if(tag.contains("HasSemiMode", Tag.TAG_ANY_NUMERIC))
            {
                this.hasSemiMode = tag.getBoolean("HasSemiMode");
            }
            if(tag.contains("HasAutoMode", Tag.TAG_ANY_NUMERIC))
            {
                this.hasAutoMode = tag.getBoolean("HasAutoMode");
            }
            if(tag.contains("HasBurstMode", Tag.TAG_ANY_NUMERIC))
            {
                this.hasBurstMode = tag.getBoolean("HasBurstMode");
            }
            if(tag.contains("UseAutoBurst", Tag.TAG_ANY_NUMERIC))
            {
                this.useAutoBurst = tag.getBoolean("UseAutoBurst");
            }
            if(tag.contains("BurstCount", Tag.TAG_ANY_NUMERIC))
            {
                this.burstCount = tag.getInt("BurstCount");
            }
        }

        public JsonObject toJsonObject()
        {
            JsonObject object = new JsonObject();
            Preconditions.checkArgument(this.burstCount > 1 || this.burstCount == 0, "Burst count must be greater than one, or equal to zero; set to zero to use general.burstCount.");
            object.addProperty("useFireModes", this.useFireModes);
            if(this.hasSemiMode) object.addProperty("hasSemiMode", this.hasSemiMode);
            if(this.hasAutoMode) object.addProperty("hasAutoMode", this.hasAutoMode);
            if(this.hasBurstMode) object.addProperty("hasBurstMode", this.hasBurstMode);
            if(this.useAutoBurst) object.addProperty("useAutoBurst", this.useAutoBurst);
            if(this.burstCount != 0) object.addProperty("burstCount", this.burstCount);
            return object;
        }

        public FireModes copy()
        {
        	FireModes fireModes = new FireModes();
            fireModes.useFireModes = this.useFireModes;
            fireModes.hasSemiMode = this.hasSemiMode;
            fireModes.hasAutoMode = this.hasAutoMode;
            fireModes.hasBurstMode = this.hasBurstMode;
            fireModes.useAutoBurst = this.useAutoBurst;
            fireModes.burstCount = this.burstCount;
            return fireModes;
        }

        /**
         * @return Whether this weapon uses the new fire mode system.
         */
        public boolean usesFireModes()
        {
            return this.useFireModes;
        }

        /**
         * @return Whether this weapon uses the new fire mode system.
         */
        public boolean hasAnyFireMode()
        {
            return this.hasSemiMode || this.hasAutoMode || this.hasBurstMode;
        }

        /**
         * @return Whether this weapon has the semi-automatic fire mode.
         */
        public boolean hasSemiMode()
        {
            return this.hasSemiMode;
        }

        /**
         * @return Whether this weapon has the automatic fire mode.
         */
        public boolean hasAutoMode()
        {
            return this.hasAutoMode;
        }

        /**
         * @return Whether this weapon has the burst fire mode.
         */
        public boolean hasBurstMode()
        {
            return this.hasBurstMode;
        }

        /**
         * @return Whether the weapon uses 'auto-burst' when in the burst fire mode.
         * 'Auto-burst' causes the gun to automatically fire another burst after a short delay,
         * as long as the fire button is held. This is the same behavior that occurs when 'isAuto'
         * is set to true with a burst-fire weapon.
         */
        public boolean usesAutoBurst()
        {
            return this.useAutoBurst;
        }

        /**
         * @return How many shots this weapon fires per burst in burst-fire mode.
         * The weapon must have access to the burst fire mode for this to have any effect.
         * A value of zero indicates the gun should use the general.burstCount parameter.
         */
        @Nullable
        public int getBurstCount()
        {
            return this.burstCount;
        }
    }

    public static class Projectile implements INBTSerializable<CompoundTag>
    {
        private ResourceLocation item = new ResourceLocation(Reference.MOD_ID, "basic_ammo");
        @Optional
        @Nullable
        private ResourceLocation projectileItem;
        @Optional
        @Nullable
        private String projectileOverride;
        @Optional
        private boolean visible;
        private float damage;
        @Optional
        private float maxRangeDamageMultiplier = 0;
        @Optional
        private int maxPierceCount = 0;
        @Optional
        private int collateralMaxPierce = 4;
        @Optional
        private float pierceDamagePenalty = 0.2F;
        @Optional
        private float pierceDamageMaxPenalty = 0.8F;
        @Optional
        private float headshotExtraDamage = 0;
        @Optional
        private float headshotMultiplierBonus = 0;
        @Optional
        private float headshotMultiplierMin = 1;
        @Optional
        private float headshotMultiplierOverride = 0;
        @Optional
        private float armorBypass = 0;
        @Optional
        private float protectionBypass = 0.5F;
        
        private float size;
        private double speed;
        private int life;
        @Optional
        private boolean gravity;
        @Optional
        private double gravityStrength = 1.0;
        @Optional
        private boolean damageReduceOverLife;
        @Optional
        private int trailColor = 0xFFD289;
        @Optional
        private double trailLengthMultiplier = 1.0;

        @Override
        public CompoundTag serializeNBT()
        {
            CompoundTag tag = new CompoundTag();
            tag.putString("Item", this.item.toString());
            
            if (projectileItem != null)  // Null check to prevent issues
            tag.putString("ProjectileItem", this.projectileItem.toString());
            else tag.putString("ProjectileItem", this.item.toString());
            if (projectileOverride != null)  // Null check to prevent issues
            tag.putString("ProjectileOverride", this.projectileOverride.toString());
            else tag.putString("ProjectileOverride", "null");
            tag.putBoolean("Visible", this.visible);
            tag.putFloat("Damage", this.damage);
            tag.putFloat("MaxRangeDamageMultiplier", this.maxRangeDamageMultiplier);
            tag.putInt("MaxPierceCount", this.maxPierceCount);
            tag.putInt("CollateralMaxPierce", this.collateralMaxPierce);
            tag.putFloat("PierceDamagePenalty", this.pierceDamagePenalty);
            tag.putFloat("PierceDamageMaxPenalty", this.pierceDamageMaxPenalty);
            tag.putFloat("HeadshotExtraDamage", this.headshotExtraDamage);
            tag.putFloat("HeadshotMultiplierBonus", this.headshotMultiplierBonus);
            tag.putFloat("HeadshotMultiplierMin", this.headshotMultiplierMin);
            tag.putFloat("HeadshotMultiplierOverride", this.headshotMultiplierOverride);
            tag.putFloat("ArmorBypass", this.armorBypass);
            tag.putFloat("ProtectionBypass", this.protectionBypass);
            tag.putFloat("Size", this.size);
            tag.putDouble("Speed", this.speed);
            tag.putInt("Life", this.life);
            tag.putBoolean("Gravity", this.gravity);
            tag.putDouble("GravityStrength", this.gravityStrength);
            tag.putBoolean("DamageReduceOverLife", this.damageReduceOverLife);
            tag.putInt("TrailColor", this.trailColor);
            tag.putDouble("TrailLengthMultiplier", this.trailLengthMultiplier);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag)
        {
            if(tag.contains("Item", Tag.TAG_STRING))
            {
                this.item = new ResourceLocation(tag.getString("Item"));
                this.projectileItem = new ResourceLocation(tag.getString("Item"));
            }
            if(tag.contains("ProjectileItem", Tag.TAG_STRING))
            {
                this.projectileItem = new ResourceLocation(tag.getString("ProjectileItem"));
            }
            if(tag.contains("ProjectileOverride", Tag.TAG_STRING))
            {
                this.projectileOverride = tag.getString("ProjectileOverride");
            }
            if(tag.contains("Visible", Tag.TAG_ANY_NUMERIC))
            {
                this.visible = tag.getBoolean("Visible");
            }
            if(tag.contains("Damage", Tag.TAG_ANY_NUMERIC))
            {
                this.damage = tag.getFloat("Damage");
            }
            if(tag.contains("MaxRangeDamageMultiplier", Tag.TAG_ANY_NUMERIC))
            {
                this.maxRangeDamageMultiplier = tag.getFloat("MaxRangeDamageMultiplier");
            }
            if(tag.contains("MaxPierceCount", Tag.TAG_ANY_NUMERIC))
            {
                this.maxPierceCount = tag.getInt("MaxPierceCount");
            }
            if(tag.contains("CollateralMaxPierce", Tag.TAG_ANY_NUMERIC))
            {
                this.collateralMaxPierce = tag.getInt("CollateralMaxPierce");
            }
            if(tag.contains("PierceDamagePenalty", Tag.TAG_ANY_NUMERIC))
            {
                this.pierceDamagePenalty = tag.getFloat("PierceDamagePenalty");
            }
            if(tag.contains("PierceDamageMaxPenalty", Tag.TAG_ANY_NUMERIC))
            {
                this.pierceDamageMaxPenalty = tag.getFloat("PierceDamageMaxPenalty");
            }
            if(tag.contains("HeadshotExtraDamage", Tag.TAG_ANY_NUMERIC))
            {
                this.headshotExtraDamage = tag.getFloat("HeadshotExtraDamage");
            }
            if(tag.contains("HeadshotMultiplierBonus", Tag.TAG_ANY_NUMERIC))
            {
                this.headshotMultiplierBonus = tag.getFloat("HeadshotMultiplierBonus");
            }
            if(tag.contains("HeadshotMultiplierMin", Tag.TAG_ANY_NUMERIC))
            {
                this.headshotMultiplierMin = tag.getFloat("HeadshotMultiplierMin");
            }
            if(tag.contains("HeadshotMultiplierOverride", Tag.TAG_ANY_NUMERIC))
            {
                this.headshotMultiplierOverride = tag.getFloat("HeadshotMultiplierOverride");
            }
            if(tag.contains("ArmorBypass", Tag.TAG_ANY_NUMERIC))
            {
                this.armorBypass = tag.getFloat("ArmorBypass");
            }
            if(tag.contains("ProtectionBypass", Tag.TAG_ANY_NUMERIC))
            {
                this.protectionBypass = tag.getFloat("ProtectionBypass");
            }
            if(tag.contains("Size", Tag.TAG_ANY_NUMERIC))
            {
                this.size = tag.getFloat("Size");
            }
            if(tag.contains("Speed", Tag.TAG_ANY_NUMERIC))
            {
                this.speed = tag.getDouble("Speed");
            }
            if(tag.contains("Life", Tag.TAG_ANY_NUMERIC))
            {
                this.life = tag.getInt("Life");
            }
            if(tag.contains("Gravity", Tag.TAG_ANY_NUMERIC))
            {
                this.gravity = tag.getBoolean("Gravity");
            }
            if(tag.contains("GravityStrength", Tag.TAG_ANY_NUMERIC))
            {
                this.gravityStrength = tag.getDouble("GravityStrength");
            }
            if(tag.contains("DamageReduceOverLife", Tag.TAG_ANY_NUMERIC))
            {
                this.damageReduceOverLife = tag.getBoolean("DamageReduceOverLife");
            }
            if(tag.contains("TrailColor", Tag.TAG_ANY_NUMERIC))
            {
                this.trailColor = tag.getInt("TrailColor");
            }
            if(tag.contains("TrailLengthMultiplier", Tag.TAG_ANY_NUMERIC))
            {
                this.trailLengthMultiplier = tag.getDouble("TrailLengthMultiplier");
            }
        }

        public JsonObject toJsonObject()
        {
            Preconditions.checkArgument(this.damage >= 0.0F, "Damage must be more than or equal to zero");
            Preconditions.checkArgument(this.maxRangeDamageMultiplier >= 0.0F, "Damage Multiplier at Max Range must be more than or equal to zero");
            Preconditions.checkArgument(this.maxPierceCount >= 0 || this.maxPierceCount==-1, "Maximum pierce count may only be positive or -1; set to -1 to enable infinite piercing");
            Preconditions.checkArgument(this.collateralMaxPierce >= 0 || this.collateralMaxPierce==-1, "Maximum pierce count may only be positive or -1; set to -1 to enable infinite piercing");
            Preconditions.checkArgument(this.pierceDamagePenalty >= 0.0F && this.pierceDamagePenalty < 0.9F, "Pierce damage penalty must be between 0.0 and 0.9");
            Preconditions.checkArgument(this.pierceDamageMaxPenalty >= 0.0F && this.pierceDamageMaxPenalty < 0.9F, "Pierce damage maximum penalty must be between 0.0 and 0.9");
            Preconditions.checkArgument(this.size >= 0.0F, "Projectile size must be more than or equal to zero");
            Preconditions.checkArgument(this.speed >= 0.0, "Projectile speed must be more than or equal to zero");
            Preconditions.checkArgument(this.life > 0, "Projectile life must be more than zero");
            Preconditions.checkArgument(this.trailLengthMultiplier >= 0.0, "Projectile trail length multiplier must be more than or equal to zero");
            Preconditions.checkArgument(this.headshotExtraDamage >= 0.0F, "Headshot extra damage must be more than or equal to zero");
            Preconditions.checkArgument(this.headshotMultiplierBonus >= 0.0F, "Headshot multiplier bonus must be more than or equal to zero");
            Preconditions.checkArgument(this.headshotMultiplierMin >= 1.0F, "Headshot multiplier minimum must be more than or equal to one");
            Preconditions.checkArgument(this.headshotMultiplierOverride >= 0.0F, "Headshot multiplier override cannot be negative - set to zero to disable it");
            Preconditions.checkArgument(this.armorBypass >= 0.0F && this.armorBypass <= 1.0F, "Armor bypass multiplier must be between 0.0 and 1.0");
            Preconditions.checkArgument(this.protectionBypass >= 0.0F && this.protectionBypass <= 1.0F, "Protection bypass multiplier must be between 0.0 and 1.0");
            JsonObject object = new JsonObject();
            object.addProperty("item", this.item.toString());
            if(projectileItem!=null) object.addProperty("projectileItem", this.projectileItem.toString());
            if(projectileOverride!=null) object.addProperty("projectileOverride", this.projectileOverride.toString());
            else object.addProperty("projectileOverride", "null");
            if(this.visible) object.addProperty("visible", true);
            object.addProperty("damage", this.damage);
            if(this.maxRangeDamageMultiplier != 0.0F) object.addProperty("maxRangeDamageMultiplier", this.maxRangeDamageMultiplier);
            if(this.maxPierceCount != 0) object.addProperty("maxPierceCount", this.maxPierceCount);
            if(this.collateralMaxPierce != 5) object.addProperty("collateralMaxPierce", this.collateralMaxPierce);
            if(this.pierceDamagePenalty != 0.2) object.addProperty("pierceDamagePenalty", this.pierceDamagePenalty);
            if(this.pierceDamageMaxPenalty != 0.8) object.addProperty("pierceDamageMaxPenalty", this.pierceDamageMaxPenalty);
            if(this.headshotExtraDamage != 0.0F) object.addProperty("headshotExtraDamage", this.headshotExtraDamage);
            if(this.headshotMultiplierBonus != 0.0F) object.addProperty("headshotMultiplierBonus", this.headshotMultiplierBonus);
            if(this.headshotMultiplierMin != 1.0F) object.addProperty("headshotMultiplierMin", this.headshotMultiplierMin);
            if(this.headshotMultiplierOverride != 0.0F) object.addProperty("headshotMultiplierOverride", this.headshotMultiplierOverride);
            object.addProperty("size", this.size);
            object.addProperty("speed", this.speed);
            object.addProperty("life", this.life);
            if(this.gravity) object.addProperty("gravity", true);
            if(this.gravityStrength != 1) object.addProperty("gravityStrength", true);
            if(this.damageReduceOverLife) object.addProperty("damageReduceOverLife", this.damageReduceOverLife);
            if(this.trailColor != 0xFFD289) object.addProperty("trailColor", this.trailColor);
            if(this.trailLengthMultiplier != 1.0) object.addProperty("trailLengthMultiplier", this.trailLengthMultiplier);
            return object;
        }

        public Projectile copy()
        {
            Projectile projectile = new Projectile();
            projectile.item = this.item;
            projectile.projectileItem = this.projectileItem;
            projectile.projectileOverride = this.projectileOverride;
            projectile.visible = this.visible;
            projectile.damage = this.damage;
            projectile.maxRangeDamageMultiplier = this.maxRangeDamageMultiplier;
            projectile.maxPierceCount = this.maxPierceCount;
            projectile.collateralMaxPierce = this.collateralMaxPierce;
            projectile.pierceDamagePenalty = this.pierceDamagePenalty;
            projectile.pierceDamageMaxPenalty = this.pierceDamageMaxPenalty;
            projectile.headshotExtraDamage = this.headshotExtraDamage;
            projectile.headshotMultiplierBonus = this.headshotMultiplierBonus;
            projectile.headshotMultiplierMin = this.headshotMultiplierMin;
            projectile.headshotMultiplierOverride = this.headshotMultiplierOverride;
            projectile.armorBypass = this.armorBypass;
            projectile.protectionBypass = this.protectionBypass;
            projectile.size = this.size;
            projectile.speed = this.speed;
            projectile.life = this.life;
            projectile.gravity = this.gravity;
            projectile.gravityStrength = this.gravityStrength;
            projectile.damageReduceOverLife = this.damageReduceOverLife;
            projectile.trailColor = this.trailColor;
            projectile.trailLengthMultiplier = this.trailLengthMultiplier;
            return projectile;
        }

        /**
         * @return The registry id of the ammo item
         */
        public ResourceLocation getItem()
        {
            return this.item;
        }

        /**
         * @return The registry id of the item used for selecting the projectile factory.
         * This allows the selection of any existing projectile factory.
         */
        public ResourceLocation getProjectileItem()
        {
        	if (this.projectileItem==null)
        		return this.item;
        	return this.projectileItem;
        }

        /**
         * @return The id of a projectile factory override.
         * This bypasses the normal projectile factory registry, allowing any projectile
         * factory to be called regardless of the ammo item used.
         */
        public String getProjectileOverride()
        {
            return this.projectileOverride;
        }

        /**
         * @return If this projectile should be visible when rendering
         */
        public boolean isVisible()
        {
            return this.visible;
        }

        /**
         * @return The damage caused by this projectile
         */
        public float getDamage()
        {
            return this.damage;
        }

        /**
         * @return The projectile's damage at the end of its life.
         * This allows for decreasing (or increasing) the projectile's damage output the further it travels.
         * Automatically enables DamageReduceOverLife for values above 0 but below 1.
         */
        public float getMaxRangeDamageMultiplier()
        {
            return this.maxRangeDamageMultiplier;
        }

        /**
         * @return The maxmimum number of entities the projectile can pierce in its base state.
         * A value of 0 disables piercing, while -1 enables infinite piercing.
         * For values of 0 or higher, the actual entity hit count will be maxPierceCount+1.
         */
        public int getMaxPierceCount()
        {
        	if (this.maxPierceCount == -1)
        	return -1;
            
            return Math.max(this.maxPierceCount,0);
        }

        /**
         * @return The maxmimum number of entities the projectile can pierce in its base state.
         * A value of 0 disables piercing, while -1 enables infinite piercing.
         * For values of 0 or higher, the actual entity hit count will be maxPierceCount+1.
         */
        public float getPierceDamageMaxPenalty()
        {
        	return Mth.clamp(this.pierceDamageMaxPenalty,0.0F,0.9F);
        }

        /**
         * @return The maxmimum number of entities the projectile can pierce with the collateral enchantment.
         * If this is less than maxPierceCount, then maxPierceCount is returned instead.
         * If either is -1, then -1 is returned to indicate infinite piercing.
         */
        public int getCollateralMaxPierce()
        {
            int pierceMin = Math.min(this.maxPierceCount, this.collateralMaxPierce);
            int pierceMax = Math.max(this.maxPierceCount, this.collateralMaxPierce);

            if (this.maxPierceCount == -1 || this.collateralMaxPierce == -1)
        	return pierceMin;

        	return Math.max(pierceMax,0);
        }

        /**
         * @return The penalty to damage that applies with each entity pierced.
         * This penalty applies subtractively, reducing the damage by the same amount every hit.
         * Setting pierceDamagePenalty therefore disables the penalty.
         * Note the damage penalty caps at pierceDamageMaxPenalty.
         */
        public float getPierceDamagePenalty()
        {
        	return this.pierceDamagePenalty;
        }

        /**
         * @return The extra damage (after multpliers) caused by landing a headshot.
         */
        public float getHeadshotExtraDamage()
        {
            return this.headshotExtraDamage;
        }

        /**
         * @return A multiplier to damage caused by landing a headshot, added on top of the standard headshot multiplier.
         */
        public float getHeadshotMultiplierBonus()
        {
            return this.headshotMultiplierBonus;
        }

        /**
         * @return The minimum damage multiplier to apply to headshots.
         */
        public float getHeadshotMultiplierMin()
        {
            return this.headshotMultiplierMin;
        }

        /**
         * @return If not equal to zero, overrides the standard headshot multiplier.
         */
        public float getHeadshotMultiplierOverride()
        {
            return this.headshotMultiplierOverride;
        }

        /**
         * @return How much of the projectile's damage is not reduced by armor.
         */
        public float getArmorBypass()
        {
            return this.armorBypass;
        }

        /**
         * @return How much of the projectile's damage is not reduced by protection enchantments.
         */
        public float getProtectionBypass()
        {
            return this.protectionBypass;
        }

        /**
         * @return The size of the projectile entity bounding box
         */
        public float getSize()
        {
            return this.size;
        }

        /**
         * @return The speed the projectile moves every tick
         */
        public double getSpeed()
        {
            return this.speed;
        }

        /**
         * @return The amount of ticks before this projectile is removed
         */
        public int getLife()
        {
            return this.life;
        }

        /**
         * @return If gravity should be applied to the projectile
         */
        public boolean isGravity()
        {
            return this.gravity;
        }

        /**
         * @return The speed the projectile moves every tick
         */
        public double getGravity()
        {
            return this.gravityStrength;
        }

        /**
         * @return If the damage should reduce the further the projectile travels
         */
        public boolean isDamageReduceOverLife()
        {
            if (maxRangeDamageMultiplier>0)
            	return true;
        	return this.damageReduceOverLife;
        }

        /**
         * @return The color of the projectile trail in rgba integer format
         */
        public int getTrailColor()
        {
            return this.trailColor;
        }

        /**
         * @return The multiplier to change the length of the projectile trail
         */
        public double getTrailLengthMultiplier()
        {
            return this.trailLengthMultiplier;
        }
    }

    public static class Sounds implements INBTSerializable<CompoundTag>
    {
        @Optional
        @Nullable
        private ResourceLocation fire;
        @Optional
        @Nullable
        private ResourceLocation reload;

        @Optional
        private int reloadFrames = 1;
        @Optional
        @Nullable
        private ResourceLocation reloadStart;
        @Optional
        @Nullable
        private ResourceLocation reloadEarly;
        @Optional
        private float reloadEarlyThreshold = 0.25F;
        @Optional
        @Nullable
        private ResourceLocation reloadMid;
        @Optional
        private float reloadMidThreshold = 0.5F;
        @Optional
        @Nullable
        private ResourceLocation reloadLate;
        @Optional
        private float reloadLateThreshold = 0.75F;
        @Optional
        @Nullable
        private ResourceLocation reloadEnd;
        
        @Optional
        @Nullable
        private ResourceLocation reloadClipOut;
        @Optional
        private float reloadClipOutThreshold = 0.33F;
        @Optional
        @Nullable
        private ResourceLocation reloadClipIn;
        @Optional
        private float reloadClipInThreshold = 0.67F;
        
        @Optional
        @Nullable
        private ResourceLocation cock;
        @Optional
        @Nullable
        private ResourceLocation drawGun;
        @Optional
        @Nullable
        private ResourceLocation silencedFire;
        @Optional
        @Nullable
        private ResourceLocation enchantedFire;
        @Optional
        @Nullable
        private ResourceLocation weaponSelect;
        @Optional
        @Nullable
        private ResourceLocation emptyClick;
        @Optional
        @Nullable
        private ResourceLocation fireSwitch;

        @Override
        public CompoundTag serializeNBT()
        {
            CompoundTag tag = new CompoundTag();
            if(this.fire != null)
            {
                tag.putString("Fire", this.fire.toString());
            }
            if(this.reload != null)
            {
                tag.putString("Reload", this.reload.toString());
            }

            tag.putInt("ReloadFrames", this.reloadFrames);
            if(this.reloadStart != null)
            {
                tag.putString("ReloadStart", this.reloadStart.toString());
            }
            if(this.reloadEarly != null)
            {
                tag.putString("ReloadMid", this.reloadEarly.toString());
            }
            tag.putFloat("ReloadMidThreshold", this.reloadEarlyThreshold);
            if(this.reloadMid != null)
            {
                tag.putString("ReloadMid", this.reloadMid.toString());
            }
            tag.putFloat("ReloadMidThreshold", this.reloadMidThreshold);
            if(this.reloadLate != null)
            {
                tag.putString("ReloadLate", this.reloadLate.toString());
            }
            tag.putFloat("ReloadLateThreshold", this.reloadLateThreshold);
            if(this.reloadEnd != null)
            {
                tag.putString("ReloadEnd", this.reloadEnd.toString());
            }
            
            if(this.reloadClipOut != null)
            {
                tag.putString("ReloadClipOut", this.reloadClipOut.toString());
            }
            tag.putFloat("ReloadClipOutThreshold", this.reloadClipOutThreshold);
            if(this.reloadClipIn != null)
            {
                tag.putString("ReloadClipIn", this.reloadClipIn.toString());
            }
            tag.putFloat("ReloadClipInThreshold", this.reloadClipInThreshold);
            
            if(this.cock != null)
            {
                tag.putString("Cock", this.cock.toString());
            }
            if(this.silencedFire != null)
            {
                tag.putString("SilencedFire", this.silencedFire.toString());
            }
            if(this.enchantedFire != null)
            {
                tag.putString("EnchantedFire", this.enchantedFire.toString());
            }
            if(this.weaponSelect != null)
            {
                tag.putString("WeaponSelect", this.weaponSelect.toString());
            }
            if(this.emptyClick != null)
            {
                tag.putString("EmptyClick", this.emptyClick.toString());
            }
            if(this.fireSwitch != null)
            {
                tag.putString("FireSwitch", this.fireSwitch.toString());
            }
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag)
        {
            if(tag.contains("Fire", Tag.TAG_STRING))
            {
                this.fire = this.createSound(tag, "Fire");
            }
            if(tag.contains("Reload", Tag.TAG_STRING))
            {
                this.reload = this.createSound(tag, "Reload");
            }

            if(tag.contains("ReloadFrames", Tag.TAG_ANY_NUMERIC))
            {
                this.reloadFrames = tag.getInt("ReloadFrames");
            }
            if(tag.contains("ReloadStart", Tag.TAG_STRING))
            {
                this.reloadStart = this.createSound(tag, "ReloadStart");
            }
            if(tag.contains("ReloadEarly", Tag.TAG_STRING))
            {
                this.reloadEarly = this.createSound(tag, "ReloadEarly");
            }
            if(tag.contains("ReloadEarlyThreshold", Tag.TAG_ANY_NUMERIC))
            {
                this.reloadEarlyThreshold = tag.getFloat("ReloadEarlyThreshold");
            }
            if(tag.contains("ReloadMid", Tag.TAG_STRING))
            {
                this.reloadMid = this.createSound(tag, "ReloadMid");
            }
            if(tag.contains("ReloadMidThreshold", Tag.TAG_ANY_NUMERIC))
            {
                this.reloadMidThreshold = tag.getFloat("ReloadMidThreshold");
            }
            if(tag.contains("ReloadLate", Tag.TAG_STRING))
            {
                this.reloadLate = this.createSound(tag, "ReloadLate");
            }
            if(tag.contains("ReloadLateThreshold", Tag.TAG_ANY_NUMERIC))
            {
                this.reloadLateThreshold = tag.getFloat("ReloadLateThreshold");
            }
            if(tag.contains("ReloadEnd", Tag.TAG_STRING))
            {
                this.reloadEnd = this.createSound(tag, "ReloadEnd");
            }
            
            if(tag.contains("ReloadClipOut", Tag.TAG_STRING))
            {
                this.reloadClipOut = this.createSound(tag, "ReloadClipOut");
            }
            if(tag.contains("ReloadClipOutThreshold", Tag.TAG_ANY_NUMERIC))
            {
                this.reloadClipOutThreshold = tag.getFloat("ReloadClipOutThreshold");
            }
            if(tag.contains("ReloadClipIn", Tag.TAG_STRING))
            {
                this.reloadClipIn = this.createSound(tag, "ReloadClipIn");
            }
            if(tag.contains("ReloadClipInThreshold", Tag.TAG_ANY_NUMERIC))
            {
                this.reloadClipInThreshold = tag.getFloat("ReloadClipInThreshold");
            }
            
            if(tag.contains("Cock", Tag.TAG_STRING))
            {
                this.cock = this.createSound(tag, "Cock");
            }
            if(tag.contains("SilencedFire", Tag.TAG_STRING))
            {
                this.silencedFire = this.createSound(tag, "SilencedFire");
            }
            if(tag.contains("EnchantedFire", Tag.TAG_STRING))
            {
                this.enchantedFire = this.createSound(tag, "EnchantedFire");
            }
            if(tag.contains("WeaponSelect", Tag.TAG_STRING))
            {
                this.weaponSelect = this.createSound(tag, "WeaponSelect");
            }
            if(tag.contains("EmptyClick", Tag.TAG_STRING))
            {
                this.emptyClick = this.createSound(tag, "EmptyClick");
            }
            if(tag.contains("FireSwitch", Tag.TAG_STRING))
            {
                this.fireSwitch = this.createSound(tag, "FireSwitch");
            }
        }

        public JsonObject toJsonObject()
        {
            JsonObject object = new JsonObject();
            if(this.fire != null)
            {
                object.addProperty("fire", this.fire.toString());
            }
            if(this.reload != null)
            {
                object.addProperty("reload", this.reload.toString());
            }

            if(this.reloadFrames != 1) object.addProperty("reloadFrames", this.reloadFrames);
            if(this.reloadStart != null)
            {
                object.addProperty("reloadStart", this.reloadStart.toString());
            }
            if(this.reloadEarly != null)
            {
                object.addProperty("reloadEarly", this.reloadEarly.toString());
            }
            if(this.reloadLateThreshold != 0.25F) object.addProperty("reloadEarlyThreshold", this.reloadEarlyThreshold);
            if(this.reloadMid != null)
            {
                object.addProperty("reloadMid", this.reloadMid.toString());
            }
            if(this.reloadMidThreshold != 0.5F) object.addProperty("reloadMidThreshold", this.reloadMidThreshold);
            if(this.reloadLate != null)
            {
                object.addProperty("reloadLate", this.reloadLate.toString());
            }
            if(this.reloadLateThreshold != 0.75F) object.addProperty("reloadLateThreshold", this.reloadLateThreshold);
            if(this.reloadEnd != null)
            {
                object.addProperty("reloadEnd", this.reloadEnd.toString());
            }
            
            if(this.reloadClipOut != null)
            {
                object.addProperty("reloadClipOut", this.reloadClipOut.toString());
            }
            if(this.reloadClipOutThreshold != 0.33F) object.addProperty("reloadClipOutThreshold", this.reloadClipOutThreshold);
            if(this.reloadClipIn != null)
            {
                object.addProperty("reloadClipIn", this.reloadClipIn.toString());
            }
            if(this.reloadClipInThreshold != 0.67F) object.addProperty("reloadClipInThreshold", this.reloadClipInThreshold);
            
            if(this.cock != null)
            {
                object.addProperty("cock", this.cock.toString());
            }
            if(this.silencedFire != null)
            {
                object.addProperty("silencedFire", this.silencedFire.toString());
            }
            if(this.enchantedFire != null)
            {
                object.addProperty("enchantedFire", this.enchantedFire.toString());
            }
            if(this.weaponSelect != null)
            {
                object.addProperty("weaponSelect", this.weaponSelect.toString());
            }
            if(this.emptyClick != null)
            {
                object.addProperty("emptyClick", this.emptyClick.toString());
            }
            if(this.fireSwitch != null)
            {
                object.addProperty("fireSwitch", this.fireSwitch.toString());
            }
            return object;
        }

        public Sounds copy()
        {
            Sounds sounds = new Sounds();
            sounds.fire = this.fire;
            sounds.reload = this.reload;
            sounds.reloadFrames = this.reloadFrames;
            sounds.reloadStart = this.reloadStart;
            sounds.reloadEarly = this.reloadEarly;
            sounds.reloadEarlyThreshold = this.reloadEarlyThreshold;
            sounds.reloadMid = this.reloadMid;
            sounds.reloadMidThreshold = this.reloadMidThreshold;
            sounds.reloadLate = this.reloadLate;
            sounds.reloadLateThreshold = this.reloadLateThreshold;
            sounds.reloadEnd = this.reloadEnd;
            sounds.reloadClipOut = this.reloadClipOut;
            sounds.reloadClipOutThreshold = this.reloadClipOutThreshold;
            sounds.reloadClipIn = this.reloadClipIn;
            sounds.reloadClipInThreshold = this.reloadClipInThreshold;
            sounds.cock = this.cock;
            sounds.silencedFire = this.silencedFire;
            sounds.enchantedFire = this.enchantedFire;
            sounds.weaponSelect = this.weaponSelect;
            sounds.emptyClick = this.emptyClick;
            sounds.fireSwitch = this.fireSwitch;
            return sounds;
        }

        @Nullable
        private ResourceLocation createSound(CompoundTag tag, String key)
        {
            String sound = tag.getString(key);
            return sound.isEmpty() ? null : new ResourceLocation(sound);
        }

        /**
         * @return The registry id of the sound event when firing this weapon
         */
        @Nullable
        public ResourceLocation getFire()
        {
            return this.fire;
        }

        /**
         * @return The registry id of the sound event when reloading this weapon
         * This is a general sound event that won't be used when the other reload sound
         * events are defined.
         */
        @Nullable
        public ResourceLocation getReload()
        {
            return this.reload;
        }

        /**
         * @return Whether the gun has any extra reload sounds defined.
         */
        public Boolean hasExtraReloadSounds()
        {
            return
            this.reloadEarly!=null ||
            this.reloadMid!=null ||
            this.reloadLate!=null ||
            this.reloadEnd!=null ||
            this.reloadClipOut!=null ||
            this.reloadClipIn!=null;
        }

        /**
         * @return The registry id of the sound event when starting to reload this weapon
         */
        @Nullable
        public ResourceLocation getReloadStart()
        {
            return this.reloadStart;
        }

        /**
         * @return The registry id of a custom sound event when reloading a weapon.
         * This plays when reloadEarlyThreshold is reached. For best results, set the
         * sound to play before the mid and late reload sounds.
         */
        @Nullable
        public ResourceLocation getReloadEarly()
        {
            return this.reloadEarly;
        }
        /**
         * @return The threshold of the reload cycle at which the reloadEarly sound plays.
         * This can range from 0 to 1.
         */
        @Nullable
        public float getReloadEarlyThreshold()
        {
            return this.reloadEarlyThreshold/reloadFrames;
        }

        /**
         * @return The registry id of a custom sound event when reloading a weapon.
         * This plays when reloadMidThreshold is reached. For best results, set the
         * sound to play after the early reload sound and before the late reload sound.
         */
        @Nullable
        public ResourceLocation getReloadMid()
        {
            return this.reloadMid;
        }
        /**
         * @return The threshold of the reload cycle at which the reloadMid sound plays.
         * This can range from 0 to 1.
         */
        @Nullable
        public float getReloadMidThreshold()
        {
            return this.reloadMidThreshold/reloadFrames;
        }

        /**
         * @return The registry id of a custom sound event when reloading a weapon.
         * This plays when reloadLateThreshold is reached. For best results, set the
         * sound to play after the early and mid reload sounds.
         */
        @Nullable
        public ResourceLocation getReloadLate()
        {
            return this.reloadLate;
        }
        /**
         * @return The threshold of the reload cycle at which the reloadLate sound plays.
         * This can range from 0 to 1.
         */
        @Nullable
        public float getReloadLateThreshold()
        {
            return this.reloadLateThreshold/reloadFrames;
        }

        /**
         * @return The registry id of the sound event when finishing reloading a weapon.
         * This does not trigger when a reload is interrupted.
         */
        @Nullable
        public ResourceLocation getReloadEnd()
        {
            return this.reloadEnd;
        }

        /**
         * @return The registry id of a custom sound event when reloading a weapon.
         * This sound event is intended to be used for magazine-style reloads, but
         * can be used with either reload type.
         * This plays when reloadClipOutThreshold is reached.
         */
        @Nullable
        public ResourceLocation getReloadClipOut()
        {
            return this.reloadClipOut;
        }
        /**
         * @return The threshold of the reload cycle at which the reloadClipOut sound plays.
         * This can range from 0 to 1.
         */
        @Nullable
        public float getReloadClipOutThreshold()
        {
            return this.reloadClipOutThreshold/reloadFrames;
        }

        /**
         * @return The registry id of a custom sound event when reloading a weapon.
         * This sound event is intended to be used for magazine-style reloads, but
         * can be used with either reload type.
         * This plays when reloadClipInThreshold is reached.
         */
        @Nullable
        public ResourceLocation getReloadClipIn()
        {
            return this.reloadClipIn;
        }
        /**
         * @return The threshold of the reload cycle at which the reloadClipIn sound plays.
         * This can range from 0 to 1.
         */
        @Nullable
        public float getReloadClipInThreshold()
        {
            return this.reloadClipInThreshold/reloadFrames;
        }

        /**
         * @return The registry id of the sound event when cocking/chambering this weapon
         * This normally plays when finishing reloading a weapon without mag reloads, but may also be called
         * as a fallback for the mag reload end sound in the event that no custom sounds are loaded.
         * It also can be called after a weapon fires, for cycling bolts or slides.
         */
        @Nullable
        public ResourceLocation getCock()
        {
            return this.cock;
        }

        /**
         * @return The registry id of the sound event when firing this weapon while it's silenced
         */
        @Nullable
        public ResourceLocation getSilencedFire()
        {
            return this.silencedFire;
        }

        /**
         * @return The registry id of the sound event when firing this weapon while it's enchanted
         */
        @Nullable
        public ResourceLocation getEnchantedFire()
        {
            return this.enchantedFire;
        }

        /**
         * @return The registry id of the sound event when selecting this weapon.
         */
        @Nullable
        public ResourceLocation getWeaponSelect()
        {
        	return this.weaponSelect;
        }

        /**
         * @return The registry id of the sound event when attempting to shoot while the weapon is empty or out of energy.
         */
        @Nullable
        public ResourceLocation getEmptyClick()
        {
            if (this.emptyClick==null)
            	return new ResourceLocation(Reference.MOD_ID, "item.empty_click");
        	return this.emptyClick;
        }

        /**
         * @return The registry id of the sound event when switching the weapon's fire mode.
         */
        @Nullable
        public ResourceLocation getFireSwitch()
        {
            if (this.fireSwitch==null)
            	return getEmptyClick();
        	return this.fireSwitch;
        }
    }

    public static class Display implements INBTSerializable<CompoundTag>
    {
        @Optional
        @Nullable
        protected Flash flash;
        protected ForwardHandPos forwardHand;
        protected RearHandPos rearHand;

        @Nullable
        public Flash getFlash()
        {
            return this.flash;
        }

        @Nullable
        public ForwardHandPos getForwardHand()
        {
            return this.forwardHand;
        }

        @Nullable
        public RearHandPos getRearHand()
        {
            return this.rearHand;
        }

        public static class Flash extends Positioned
        {
            private double size = 0.5;

            @Override
            public CompoundTag serializeNBT()
            {
                CompoundTag tag = super.serializeNBT();
                tag.putDouble("Size", this.size);
                return tag;
            }

            @Override
            public void deserializeNBT(CompoundTag tag)
            {
                super.deserializeNBT(tag);
                if(tag.contains("Size", Tag.TAG_ANY_NUMERIC))
                {
                    this.size = tag.getDouble("Size");
                }
            }

            @Override
            public JsonObject toJsonObject()
            {
                Preconditions.checkArgument(this.size >= 0, "Muzzle flash size must be more than or equal to zero");
                JsonObject object = super.toJsonObject();
                if(this.size != 0.5)
                {
                    object.addProperty("size", this.size);
                }
                return object;
            }

            public Flash copy()
            {
                Flash flash = new Flash();
                flash.size = this.size;
                flash.xOffset = this.xOffset;
                flash.yOffset = this.yOffset;
                flash.zOffset = this.zOffset;
                return flash;
            }

            /**
             * @return The size/scale of the muzzle flash render
             */
            public double getSize()
            {
                return this.size;
            }
        }
        
        public static class ForwardHandPos extends Positioned
        {
            
            public ForwardHandPos copy()
            {
            	ForwardHandPos forwardHand = new ForwardHandPos();
            	forwardHand.xOffset = this.xOffset;
            	forwardHand.yOffset = this.yOffset;
                forwardHand.zOffset = this.zOffset;
                return forwardHand;
            }
        }
        
        public static class RearHandPos extends Positioned
        {
            
            public RearHandPos copy()
            {
            	RearHandPos rearHand = new RearHandPos();
            	rearHand.xOffset = this.xOffset;
            	rearHand.yOffset = this.yOffset;
                rearHand.zOffset = this.zOffset;
                return rearHand;
            }
        }

        @Override
        public CompoundTag serializeNBT()
        {
            CompoundTag tag = new CompoundTag();
            if(this.flash != null)
            {
                tag.put("Flash", this.flash.serializeNBT());
            }
            if(this.forwardHand != null)
            {
                tag.put("ForwardHand", this.forwardHand.serializeNBT());
            }
            if(this.rearHand != null)
            {
                tag.put("RearHand", this.rearHand.serializeNBT());
            }
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag)
        {
            if(tag.contains("Flash", Tag.TAG_COMPOUND))
            {
                CompoundTag flashTag = tag.getCompound("Flash");
                if(!flashTag.isEmpty())
                {
                    Flash flash = new Flash();
                    flash.deserializeNBT(tag.getCompound("Flash"));
                    this.flash = flash;
                }
                else
                {
                    this.flash = null;
                }
            }
            if(tag.contains("ForwardHand", Tag.TAG_COMPOUND))
            {
                CompoundTag handTag = tag.getCompound("ForwardHand");
                if(!handTag.isEmpty())
                {
                    ForwardHandPos hand = new ForwardHandPos();
                    hand.deserializeNBT(tag.getCompound("ForwardHand"));
                    this.forwardHand = hand;
                }
                else
                {
                    this.forwardHand = null;
                }
            }
            if(tag.contains("RearHand", Tag.TAG_COMPOUND))
            {
                CompoundTag handTag = tag.getCompound("RearHand");
                if(!handTag.isEmpty())
                {
                    RearHandPos hand = new RearHandPos();
                    hand.deserializeNBT(tag.getCompound("RearHand"));
                    this.rearHand = hand;
                }
                else
                {
                    this.rearHand = null;
                }
            }
        }

        public JsonObject toJsonObject()
        {
            JsonObject object = new JsonObject();
            if(this.flash != null)
            {
                GunJsonUtil.addObjectIfNotEmpty(object, "flash", this.flash.toJsonObject());
            }
            if(this.forwardHand != null)
            {
                GunJsonUtil.addObjectIfNotEmpty(object, "forwardHand", this.forwardHand.toJsonObject());
            }
            if(this.rearHand != null)
            {
                GunJsonUtil.addObjectIfNotEmpty(object, "rearHand", this.rearHand.toJsonObject());
            }
            return object;
        }

        public Display copy()
        {
            Display display = new Display();
            if(this.flash != null)
            {
                display.flash = this.flash.copy();
            }
            if(this.forwardHand != null)
            {
                display.forwardHand = this.forwardHand.copy();
            }
            if(this.rearHand != null)
            {
                display.rearHand = this.rearHand.copy();
            }
            return display;
        }
    }

    public static class Modules implements INBTSerializable<CompoundTag>, IEditorMenu
    {
        private transient Zoom cachedZoom;

        @Optional
        @Nullable
        private Zoom zoom;
        private Attachments attachments = new Attachments();

        @Nullable
        public Zoom getZoom()
        {
            return this.zoom;
        }

        public Attachments getAttachments()
        {
            return this.attachments;
        }

        @Override
        public Component getEditorLabel()
        {
            return Component.literal("Modules");
        }

        @Override
        public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets)
        {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                widgets.add(Pair.of(Component.literal("Enabled Iron Sights"), () -> new DebugToggle(this.zoom != null, val -> {
                    if(val) {
                        if(this.cachedZoom != null) {
                            this.zoom = this.cachedZoom;
                        } else {
                            this.zoom = new Zoom();
                            this.cachedZoom = this.zoom;
                        }
                    } else {
                        this.cachedZoom = this.zoom;
                        this.zoom = null;
                    }
                })));

                widgets.add(Pair.of(Component.literal("Adjust Iron Sights"), () -> new DebugButton(Component.literal(">"), btn -> {
                    if(btn.active && this.zoom != null) {
                        Minecraft.getInstance().setScreen(ClientHandler.createEditorScreen(this.zoom));
                    }
                }, () -> this.zoom != null)));
            });
        }

        public static class Zoom extends Positioned implements IEditorMenu
        {
            @Optional
            private float fovModifier;

            @Override
            public CompoundTag serializeNBT()
            {
                CompoundTag tag = super.serializeNBT();
                tag.putFloat("FovModifier", this.fovModifier);
                return tag;
            }

            @Override
            public void deserializeNBT(CompoundTag tag)
            {
                super.deserializeNBT(tag);
                if(tag.contains("FovModifier", Tag.TAG_ANY_NUMERIC))
                {
                    this.fovModifier = tag.getFloat("FovModifier");
                }
            }

            public JsonObject toJsonObject()
            {
                JsonObject object = super.toJsonObject();
                object.addProperty("fovModifier", this.fovModifier);
                return object;
            }

            public Zoom copy()
            {
                Zoom zoom = new Zoom();
                zoom.fovModifier = this.fovModifier;
                zoom.xOffset = this.xOffset;
                zoom.yOffset = this.yOffset;
                zoom.zOffset = this.zOffset;
                return zoom;
            }

            @Override
            public Component getEditorLabel()
            {
                return Component.literal("Zoom");
            }

            @Override
            public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets)
            {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    widgets.add(Pair.of(Component.literal("FOV Modifier"), () -> new DebugSlider(0.0, 1.0, this.fovModifier, 0.01, 3, val -> {
                        this.fovModifier = val.floatValue();
                    })));
                });
            }

            public float getFovModifier()
            {
                return this.fovModifier;
            }

            public static Builder builder()
            {
                return new Builder();
            }

            public static class Builder extends AbstractBuilder<Builder> {}

            protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends Positioned.AbstractBuilder<T>
            {
                protected final Zoom zoom;

                protected AbstractBuilder()
                {
                    this(new Zoom());
                }

                protected AbstractBuilder(Zoom zoom)
                {
                    super(zoom);
                    this.zoom = zoom;
                }

                public T setFovModifier(float fovModifier)
                {
                    this.zoom.fovModifier = fovModifier;
                    return this.self();
                }

                @Override
                public Zoom build()
                {
                    return this.zoom.copy();
                }
            }
        }

        public static class Attachments implements INBTSerializable<CompoundTag>
        {
            @Optional
            @Nullable
            private ScaledPositioned scope;
            @Optional
            @Nullable
            private ScaledPositioned barrel;
            @Optional
            @Nullable
            private ScaledPositioned stock;
            @Optional
            @Nullable
            private ScaledPositioned underBarrel;

            @Nullable
            public ScaledPositioned getScope()
            {
                return this.scope;
            }

            @Nullable
            public ScaledPositioned getBarrel()
            {
                return this.barrel;
            }

            @Nullable
            public ScaledPositioned getStock()
            {
                return this.stock;
            }

            @Nullable
            public ScaledPositioned getUnderBarrel()
            {
                return this.underBarrel;
            }

            @Override
            public CompoundTag serializeNBT()
            {
                CompoundTag tag = new CompoundTag();
                if(this.scope != null)
                {
                    tag.put("Scope", this.scope.serializeNBT());
                }
                if(this.barrel != null)
                {
                    tag.put("Barrel", this.barrel.serializeNBT());
                }
                if(this.stock != null)
                {
                    tag.put("Stock", this.stock.serializeNBT());
                }
                if(this.underBarrel != null)
                {
                    tag.put("UnderBarrel", this.underBarrel.serializeNBT());
                }
                return tag;
            }

            @Override
            public void deserializeNBT(CompoundTag tag)
            {
                if(tag.contains("Scope", Tag.TAG_COMPOUND))
                {
                    this.scope = this.createScaledPositioned(tag, "Scope");
                }
                if(tag.contains("Barrel", Tag.TAG_COMPOUND))
                {
                    this.barrel = this.createScaledPositioned(tag, "Barrel");
                }
                if(tag.contains("Stock", Tag.TAG_COMPOUND))
                {
                    this.stock = this.createScaledPositioned(tag, "Stock");
                }
                if(tag.contains("UnderBarrel", Tag.TAG_COMPOUND))
                {
                    this.underBarrel = this.createScaledPositioned(tag, "UnderBarrel");
                }
            }

            public JsonObject toJsonObject()
            {
                JsonObject object = new JsonObject();
                if(this.scope != null)
                {
                    object.add("scope", this.scope.toJsonObject());
                }
                if(this.barrel != null)
                {
                    object.add("barrel", this.barrel.toJsonObject());
                }
                if(this.stock != null)
                {
                    object.add("stock", this.stock.toJsonObject());
                }
                if(this.underBarrel != null)
                {
                    object.add("underBarrel", this.underBarrel.toJsonObject());
                }
                return object;
            }

            public Attachments copy()
            {
                Attachments attachments = new Attachments();
                if(this.scope != null)
                {
                    attachments.scope = this.scope.copy();
                }
                if(this.barrel != null)
                {
                    attachments.barrel = this.barrel.copy();
                }
                if(this.stock != null)
                {
                    attachments.stock = this.stock.copy();
                }
                if(this.underBarrel != null)
                {
                    attachments.underBarrel = this.underBarrel.copy();
                }
                return attachments;
            }

            @Nullable
            private ScaledPositioned createScaledPositioned(CompoundTag tag, String key)
            {
                CompoundTag attachment = tag.getCompound(key);
                return attachment.isEmpty() ? null : new ScaledPositioned(attachment);
            }
        }

        @Override
        public CompoundTag serializeNBT()
        {
            CompoundTag tag = new CompoundTag();
            if(this.zoom != null)
            {
                tag.put("Zoom", this.zoom.serializeNBT());
            }
            tag.put("Attachments", this.attachments.serializeNBT());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag)
        {
            if(tag.contains("Zoom", Tag.TAG_COMPOUND))
            {
                Zoom zoom = new Zoom();
                zoom.deserializeNBT(tag.getCompound("Zoom"));
                this.zoom = zoom;
            }
            if(tag.contains("Attachments", Tag.TAG_COMPOUND))
            {
                this.attachments.deserializeNBT(tag.getCompound("Attachments"));
            }
        }

        public JsonObject toJsonObject()
        {
            JsonObject object = new JsonObject();
            if(this.zoom != null)
            {
                object.add("zoom", this.zoom.toJsonObject());
            }
            GunJsonUtil.addObjectIfNotEmpty(object, "attachments", this.attachments.toJsonObject());
            return object;
        }

        public Modules copy()
        {
            Modules modules = new Modules();
            if(this.zoom != null)
            {
                modules.zoom = this.zoom.copy();
            }
            modules.attachments = this.attachments.copy();
            return modules;
        }
    }

    public static class Positioned implements INBTSerializable<CompoundTag>
    {
        @Optional
        protected double xOffset;
        @Optional
        protected double yOffset;
        @Optional
        protected double zOffset;

        @Override
        public CompoundTag serializeNBT()
        {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("XOffset", this.xOffset);
            tag.putDouble("YOffset", this.yOffset);
            tag.putDouble("ZOffset", this.zOffset);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag)
        {
            if(tag.contains("XOffset", Tag.TAG_ANY_NUMERIC))
            {
                this.xOffset = tag.getDouble("XOffset");
            }
            if(tag.contains("YOffset", Tag.TAG_ANY_NUMERIC))
            {
                this.yOffset = tag.getDouble("YOffset");
            }
            if(tag.contains("ZOffset", Tag.TAG_ANY_NUMERIC))
            {
                this.zOffset = tag.getDouble("ZOffset");
            }
        }

        public JsonObject toJsonObject()
        {
            JsonObject object = new JsonObject();
            if(this.xOffset != 0)
            {
                object.addProperty("xOffset", this.xOffset);
            }
            if(this.yOffset != 0)
            {
                object.addProperty("yOffset", this.yOffset);
            }
            if(this.zOffset != 0)
            {
                object.addProperty("zOffset", this.zOffset);
            }
            return object;
        }

        public double getXOffset()
        {
            return this.xOffset;
        }

        public double getYOffset()
        {
            return this.yOffset;
        }

        public double getZOffset()
        {
            return this.zOffset;
        }

        public Positioned copy()
        {
            Positioned positioned = new Positioned();
            positioned.xOffset = this.xOffset;
            positioned.yOffset = this.yOffset;
            positioned.zOffset = this.zOffset;
            return positioned;
        }

        public static class Builder extends AbstractBuilder<Builder> {}

        protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends SuperBuilder<Positioned, T>
        {
            private final Positioned positioned;

            private AbstractBuilder()
            {
                this(new Positioned());
            }

            protected AbstractBuilder(Positioned positioned)
            {
                this.positioned = positioned;
            }

            public T setOffset(double xOffset, double yOffset, double zOffset)
            {
                this.positioned.xOffset = xOffset;
                this.positioned.yOffset = yOffset;
                this.positioned.zOffset = zOffset;
                return this.self();
            }

            public T setXOffset(double xOffset)
            {
                this.positioned.xOffset = xOffset;
                return this.self();
            }

            public T setYOffset(double yOffset)
            {
                this.positioned.yOffset = yOffset;
                return this.self();
            }

            public T setZOffset(double zOffset)
            {
                this.positioned.zOffset = zOffset;
                return this.self();
            }

            @Override
            public Positioned build()
            {
                return this.positioned.copy();
            }
        }
    }

    public static class ScaledPositioned extends Positioned
    {
        @Optional
        protected double scale = 1.0;

        public ScaledPositioned() {}

        public ScaledPositioned(CompoundTag tag)
        {
            this.deserializeNBT(tag);
        }

        @Override
        public CompoundTag serializeNBT()
        {
            CompoundTag tag = super.serializeNBT();
            tag.putDouble("Scale", this.scale);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag)
        {
            super.deserializeNBT(tag);
            if(tag.contains("Scale", Tag.TAG_ANY_NUMERIC))
            {
                this.scale = tag.getDouble("Scale");
            }
        }

        @Override
        public JsonObject toJsonObject()
        {
            JsonObject object = super.toJsonObject();
            if(this.scale != 1.0)
            {
                object.addProperty("scale", this.scale);
            }
            return object;
        }

        public double getScale()
        {
            return this.scale;
        }

        @Override
        public ScaledPositioned copy()
        {
            ScaledPositioned positioned = new ScaledPositioned();
            positioned.xOffset = this.xOffset;
            positioned.yOffset = this.yOffset;
            positioned.zOffset = this.zOffset;
            positioned.scale = this.scale;
            return positioned;
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag tag = new CompoundTag();
        tag.put("General", this.general.serializeNBT());
        tag.put("FireModes", this.fireModes.serializeNBT());
        tag.put("Projectile", this.projectile.serializeNBT());
        tag.put("Sounds", this.sounds.serializeNBT());
        tag.put("Display", this.display.serializeNBT());
        tag.put("Modules", this.modules.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag)
    {
        if(tag.contains("General", Tag.TAG_COMPOUND))
        {
            this.general.deserializeNBT(tag.getCompound("General"));
        }
        if(tag.contains("FireModes", Tag.TAG_COMPOUND))
        {
            this.fireModes.deserializeNBT(tag.getCompound("FireModes"));
        }
        if(tag.contains("Projectile", Tag.TAG_COMPOUND))
        {
            this.projectile.deserializeNBT(tag.getCompound("Projectile"));
        }
        if(tag.contains("Sounds", Tag.TAG_COMPOUND))
        {
            this.sounds.deserializeNBT(tag.getCompound("Sounds"));
        }
        if(tag.contains("Display", Tag.TAG_COMPOUND))
        {
            this.display.deserializeNBT(tag.getCompound("Display"));
        }
        if(tag.contains("Modules", Tag.TAG_COMPOUND))
        {
            this.modules.deserializeNBT(tag.getCompound("Modules"));
        }
    }

    public JsonObject toJsonObject()
    {
        JsonObject object = new JsonObject();
        object.add("general", this.general.toJsonObject());
        object.add("projectile", this.projectile.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object, "fireModes", this.fireModes.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object, "sounds", this.sounds.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object, "display", this.display.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object, "modules", this.modules.toJsonObject());
        return object;
    }

    public static Gun create(CompoundTag tag)
    {
        Gun gun = new Gun();
        gun.deserializeNBT(tag);
        return gun;
    }

    public Gun copy()
    {
        Gun gun = new Gun();
        gun.general = this.general.copy();
        gun.fireModes = this.fireModes.copy();
        gun.projectile = this.projectile.copy();
        gun.sounds = this.sounds.copy();
        gun.display = this.display.copy();
        gun.modules = this.modules.copy();
        return gun;
    }

    public boolean canAttachType(@Nullable IAttachment.Type type)
    {
        if(this.modules.attachments != null && type != null)
        {
            switch(type)
            {
                case SCOPE:
                    return this.modules.attachments.scope != null;
                case BARREL:
                    return this.modules.attachments.barrel != null;
                case STOCK:
                    return this.modules.attachments.stock != null;
                case UNDER_BARREL:
                    return this.modules.attachments.underBarrel != null;
            }
        }
        return false;
    }

    @Nullable
    public ScaledPositioned getAttachmentPosition(IAttachment.Type type)
    {
        if(this.modules.attachments != null)
        {
            switch(type)
            {
                case SCOPE:
                    return this.modules.attachments.scope;
                case BARREL:
                    return this.modules.attachments.barrel;
                case STOCK:
                    return this.modules.attachments.stock;
                case UNDER_BARREL:
                    return this.modules.attachments.underBarrel;
            }
        }
        return null;
    }

    public boolean canAimDownSight()
    {
        return this.canAttachType(IAttachment.Type.SCOPE) || this.modules.zoom != null;
    }

    public static ItemStack getScopeStack(ItemStack gun)
    {
        CompoundTag compound = gun.getTag();
        if(compound != null && compound.contains("Attachments", Tag.TAG_COMPOUND))
        {
            CompoundTag attachment = compound.getCompound("Attachments");
            if(attachment.contains("Scope", Tag.TAG_COMPOUND))
            {
                return ItemStack.of(attachment.getCompound("Scope"));
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean hasAttachmentEquipped(ItemStack stack, Gun gun, IAttachment.Type type)
    {
        if(!gun.canAttachType(type))
            return false;

        CompoundTag compound = stack.getTag();
        if(compound != null && compound.contains("Attachments", Tag.TAG_COMPOUND))
        {
            CompoundTag attachment = compound.getCompound("Attachments");
            return attachment.contains(type.getTagKey(), Tag.TAG_COMPOUND);
        }
        return false;
    }

    @Nullable
    public static Scope getScope(ItemStack gun)
    {
        CompoundTag compound = gun.getTag();
        if(compound != null && compound.contains("Attachments", Tag.TAG_COMPOUND))
        {
            CompoundTag attachment = compound.getCompound("Attachments");
            if(attachment.contains("Scope", Tag.TAG_COMPOUND))
            {
                ItemStack scopeStack = ItemStack.of(attachment.getCompound("Scope"));
                Scope scope = null;
                if(scopeStack.getItem() instanceof ScopeItem scopeItem)
                {
                    if(GunMod.isDebugging())
                    {
                        return Debug.getScope(scopeItem);
                    }
                    scope = scopeItem.getProperties();
                }
                return scope;
            }
        }
        return null;
    }

    public static ItemStack getAttachment(IAttachment.Type type, ItemStack gun)
    {
        CompoundTag compound = gun.getTag();
        if(compound != null && compound.contains("Attachments", Tag.TAG_COMPOUND))
        {
            CompoundTag attachment = compound.getCompound("Attachments");
            if(attachment.contains(type.getTagKey(), Tag.TAG_COMPOUND))
            {
                return ItemStack.of(attachment.getCompound(type.getTagKey()));
            }
        }
        return ItemStack.EMPTY;
    }

    public static float getAdditionalDamage(ItemStack gunStack)
    {
        CompoundTag tag = gunStack.getOrCreateTag();
        return tag.getFloat("AdditionalDamage");
    }

    public static AmmoContext findAmmo(Player player, ResourceLocation id)
    {
    	ItemStack gunStack = player.getInventory().getSelected();
        if(player.isCreative() || hasUnlimitedReloads(gunStack))
        {
            Item item = ForgeRegistries.ITEMS.getValue(id);
            ItemStack ammo = item != null ? new ItemStack(item, Integer.MAX_VALUE) : ItemStack.EMPTY;
            return new AmmoContext(ammo, null);
        }
        for(int i = 0; i < player.getInventory().getContainerSize(); ++i)
        {
            ItemStack stack = player.getInventory().getItem(i);
            if(isAmmo(stack, id))
            {
                return new AmmoContext(stack, player.getInventory());
            }
        }
        if(GunMod.backpackedLoaded)
        {
            return BackpackHelper.findAmmo(player, id);
        }
        return AmmoContext.NONE;
    }
    
    public static int getReserveAmmoCount(Player player, ResourceLocation id)
    {
    	int ammoCount = 0;
    	for(int i = 0; i < player.getInventory().getContainerSize(); ++i)
        {
            ItemStack stack = player.getInventory().getItem(i);
            if(isAmmo(stack, id))
            {
            	AmmoContext context = new AmmoContext(stack, player.getInventory());
            	ammoCount += context.stack().getCount();
            }
        }
        if(GunMod.backpackedLoaded)
        {
            AmmoContext context = BackpackHelper.findAmmo(player, id);;
        	ammoCount += context.stack().getCount();
        }
        return ammoCount;
    }

    public static boolean hasUnlimitedReloads(ItemStack gunStack)
    {
        CompoundTag tag = gunStack.getOrCreateTag();
        return tag.getBoolean("UnlimitedReloads") || tag.getBoolean("UnlimitReload");
    }

    public static boolean hasRampUp(ItemStack gunStack)
    {
        Gun modifiedGun = ((GunItem) gunStack.getItem()).getModifiedGun(gunStack);
        return modifiedGun.getGeneral().hasDoRampUp();
    }

    public static boolean isAmmo(ItemStack stack, ResourceLocation id)
    {
        return stack != null && Objects.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()), id);
    }

    public static boolean hasAmmo(ItemStack gunStack)
    {
        CompoundTag tag = gunStack.getOrCreateTag();
        return hasInfiniteAmmo(gunStack) || tag.getInt("AmmoCount") > 0;
    }

    public static boolean hasInfiniteAmmo(ItemStack gunStack)
    {
        CompoundTag tag = gunStack.getOrCreateTag();
        Gun modifiedGun = ((GunItem) gunStack.getItem()).getModifiedGun(gunStack);
        return tag.getBoolean("IgnoreAmmo") || modifiedGun.getGeneral().getInfiniteAmmo();
    }

    public static boolean canShoot(ItemStack gunStack)
    {
        CompoundTag tag = gunStack.getOrCreateTag();
        Gun modifiedGun = ((GunItem) gunStack.getItem()).getModifiedGun(gunStack);
        return (!usesEnergy(gunStack)) || (tag.getInt("Energy")>=modifiedGun.getGeneral().getEnergyPerShot());
    }

    public static boolean usesEnergy(ItemStack gunStack)
    {
        Gun modifiedGun = ((GunItem) gunStack.getItem()).getModifiedGun(gunStack);
        return modifiedGun.getGeneral().getEnergyPerShot()>0 && modifiedGun.getGeneral().getEnergyCapacity()>0;
    }
    
    public static int getDefaultFireMode(ItemStack gunStack)
    {
        Gun modifiedGun = ((GunItem) gunStack.getItem()).getModifiedGun(gunStack);
        
        if (modifiedGun.getFireModes().usesFireModes() && modifiedGun.getFireModes().hasAnyFireMode())
        {
        	if (modifiedGun.getFireModes().hasAutoMode())
            	return 1;
        	else
        	if (modifiedGun.getFireModes().hasBurstMode())
            	return 2;
        	else
        	if (modifiedGun.getFireModes().hasSemiMode())
            	return 0;
        }
        else
        {
        	if (modifiedGun.getGeneral().hasBurstFire())
            	return 2;
        	else
            if (modifiedGun.getGeneral().isAuto())
            	return 1;
        }
        
    	return 0;
    }

    public static int getFireMode(ItemStack gunStack)
    {
        CompoundTag tag = gunStack.getOrCreateTag();
        Gun modifiedGun = ((GunItem) gunStack.getItem()).getModifiedGun(gunStack);
        
        if ((!tag.contains("FireMode", Tag.TAG_INT)) || (tag.getInt("FireMode")<0 || tag.getInt("FireMode")>2))
        	return getDefaultFireMode(gunStack);
        
        if (modifiedGun.getFireModes().usesFireModes())
        {
        	return tag.getInt("FireMode");
        }
        else
        {
        	if (modifiedGun.getGeneral().hasBurstFire())
        		return 2;
        	else
            if (modifiedGun.getGeneral().isAuto())
            	return 1;
            else
            	return 0;
        }
    }

    public static boolean isAuto(ItemStack gunStack)
    {
        Gun modifiedGun = ((GunItem) gunStack.getItem()).getModifiedGun(gunStack);
        return (modifiedGun.getFireModes().usesFireModes() && getFireMode(gunStack)==1) || (!modifiedGun.getFireModes().usesFireModes() && modifiedGun.getGeneral().isAuto());
    }

    public static boolean hasBurstFire(ItemStack gunStack)
    {
        Gun modifiedGun = ((GunItem) gunStack.getItem()).getModifiedGun(gunStack);
        return (modifiedGun.getFireModes().usesFireModes() && getFireMode(gunStack)==2) || (!modifiedGun.getFireModes().usesFireModes() && modifiedGun.getGeneral().hasBurstFire());
    }

    public static int getBurstCount(ItemStack gunStack)
    {
        Gun modifiedGun = ((GunItem) gunStack.getItem()).getModifiedGun(gunStack);
        int finalBurstCount = 3;
        if (modifiedGun.getFireModes().usesFireModes())
        {
        	if (modifiedGun.getFireModes().getBurstCount()>1)
        	finalBurstCount = modifiedGun.getFireModes().getBurstCount();
        	else
            if (modifiedGun.getGeneral().getBurstCount()>1)
        	finalBurstCount = modifiedGun.getGeneral().getBurstCount();
        }
        else
        finalBurstCount = modifiedGun.getGeneral().getBurstCount();
        
        return finalBurstCount;
    }

    public static int getBurstCooldown(ItemStack gunStack)
    {
        Gun modifiedGun = ((GunItem) gunStack.getItem()).getModifiedGun(gunStack);
        int finalBurstCooldown = (hasAutoBurst(gunStack) ? 3 : 1 );
        
        if (modifiedGun.getGeneral().getBurstCooldown()>=0)
        finalBurstCooldown = modifiedGun.getGeneral().getBurstCooldown();
        
        return finalBurstCooldown;
    }

    public static boolean hasAutoBurst(ItemStack gunStack)
    {
        Gun modifiedGun = ((GunItem) gunStack.getItem()).getModifiedGun(gunStack);
        if (modifiedGun.getFireModes().usesFireModes())
        {
        	if (modifiedGun.getFireModes().hasBurstMode() && modifiedGun.getFireModes().usesAutoBurst())
            	return true;
        	else
            if (!modifiedGun.getFireModes().hasAnyFireMode() && modifiedGun.getGeneral().hasBurstFire() && modifiedGun.getGeneral().isAuto())
            	return true;
        }
        else
        if (modifiedGun.getGeneral().hasBurstFire() && modifiedGun.getGeneral().isAuto())
        	return true;
        
        return false;
        //return (modifiedGun.getFireModes().usesFireModes() && modifiedGun.getFireModes().usesAutoBurst()) || (!modifiedGun.getFireModes().hasAnyFireMode() && modifiedGun.getGeneral().isAuto());
    }

    public static boolean canDoSemiFire(ItemStack gunStack)
    {
        Gun modifiedGun = ((GunItem) gunStack.getItem()).getModifiedGun(gunStack);
        return modifiedGun.getFireModes().hasSemiMode() || (!modifiedGun.getFireModes().hasAnyFireMode());
    }

    public static boolean canDoAutoFire(ItemStack gunStack)
    {
        Gun modifiedGun = ((GunItem) gunStack.getItem()).getModifiedGun(gunStack);
        return modifiedGun.getFireModes().hasAutoMode() || (!modifiedGun.getFireModes().hasAnyFireMode() && modifiedGun.getGeneral().isAuto() && !modifiedGun.getGeneral().hasBurstFire());
    }

    public static boolean canDoBurstFire(ItemStack gunStack)
    {
        Gun modifiedGun = ((GunItem) gunStack.getItem()).getModifiedGun(gunStack);
        return modifiedGun.getFireModes().hasBurstMode() || (!modifiedGun.getFireModes().hasAnyFireMode() && modifiedGun.getGeneral().hasBurstFire());
    }

    public static float getFovModifier(ItemStack stack, Gun modifiedGun)
    {
        float modifier = 0.0F;
        if(hasAttachmentEquipped(stack, modifiedGun, IAttachment.Type.SCOPE))
        {
            Scope scope = Gun.getScope(stack);
            if(scope != null)
            {
                if(scope.getFovModifier() < 1.0F)
                {
                    return Mth.clamp(scope.getFovModifier(), 0.01F, 1.0F);
                }
                modifier -= scope.getAdditionalZoom();
            }
        }
        Modules.Zoom zoom = modifiedGun.getModules().getZoom();
        return zoom != null ? modifier + zoom.getFovModifier() : 0F;
    }

    public static class Builder
    {
        private final Gun gun;

        private Builder()
        {
            this.gun = new Gun();
        }

        public static Builder create()
        {
            return new Builder();
        }

        public Gun build()
        {
            return this.gun.copy(); //Copy since the builder could be used again
        }

        public Builder setAuto(boolean auto)
        {
            this.gun.general.auto = auto;
            return this;
        }

        public Builder setFireRate(int rate)
        {
            this.gun.general.rate = rate;
            return this;
        }

        public Builder setBurstCount(int burstCount)
        {
            this.gun.general.burstCount = burstCount;
            return this;
        }

        public Builder setBurstCooldown(int burstCooldown)
        {
            this.gun.general.burstCooldown = burstCooldown;
            return this;
        }

        public Builder setGripType(GripType gripType)
        {
            this.gun.general.gripType = gripType;
            return this;
        }

        public Builder setMaxAmmo(int maxAmmo)
        {
            this.gun.general.maxAmmo = maxAmmo;
            return this;
        }

        public Builder setOverCapacityAmmo(int overCapacityAmmo)
        {
            this.gun.general.overCapacityAmmo = overCapacityAmmo;
            return this;
        }

        public Builder setInfiniteAmmo(boolean infiniteAmmo)
        {
            this.gun.general.infiniteAmmo = infiniteAmmo;
            return this;
        }

        public Builder setReloadAmount(int reloadAmount)
        {
            this.gun.general.reloadAmount = reloadAmount;
            return this;
        }

        public Builder setReloadRate(int reloadRate)
        {
            this.gun.general.reloadRate = reloadRate;
            return this;
        }

        public Builder setUseMagReload(boolean useMagReload)
        {
            this.gun.general.useMagReload = useMagReload;
            return this;
        }

        public Builder setMagReloadTime(int magReloadTime)
        {
            this.gun.general.magReloadTime = magReloadTime;
            return this;
        }

        public Builder setEnergyCapacity(int energyCapacity)
        {
            this.gun.general.energyCapacity = energyCapacity;
            return this;
        }

        public Builder setEnergyPerShot(int energyPerShot)
        {
            this.gun.general.energyPerShot = energyPerShot;
            return this;
        }

        public Builder setRecoilAngle(float recoilAngle)
        {
            this.gun.general.recoilAngle = recoilAngle;
            return this;
        }

        public Builder setRecoilKick(float recoilKick)
        {
            this.gun.general.recoilKick = recoilKick;
            return this;
        }

        public Builder setRecoilDurationOffset(float recoilDurationOffset)
        {
            this.gun.general.recoilDurationOffset = recoilDurationOffset;
            return this;
        }

        public Builder setRecoilAdsReduction(float recoilAdsReduction)
        {
            this.gun.general.recoilAdsReduction = recoilAdsReduction;
            return this;
        }

        public Builder setProjectileAmount(int projectileAmount)
        {
            this.gun.general.projectileAmount = projectileAmount;
            return this;
        }

        public Builder setAlwaysSpread(boolean alwaysSpread)
        {
            this.gun.general.alwaysSpread = alwaysSpread;
            return this;
        }

        public Builder setSpread(float spread)
        {
            this.gun.general.spread = spread;
            return this;
        }

        public Builder setRestingSpread(float restingSpread)
        {
            this.gun.general.restingSpread = restingSpread;
            return this;
        }

        public Builder setSpreadAdsReduction(float spreadAdsReduction)
        {
            this.gun.general.spreadAdsReduction = spreadAdsReduction;
            return this;
        }

        public Builder setAdsSpeed(float adsSpeed)
        {
            this.gun.general.adsSpeed = adsSpeed;
            return this;
        }

        public Builder setDoRampUp(boolean doRampUp)
        {
            this.gun.general.doRampUp = doRampUp;
            return this;
        }

        public Builder setRampUpShotsNeeded(int rampUpShotsNeeded)
        {
            this.gun.general.rampUpShotsNeeded = rampUpShotsNeeded;
            return this;
        }

        public Builder setAmmo(Item item)
        {
            this.gun.projectile.item = ForgeRegistries.ITEMS.getKey(item);
            return this;
        }

        public Builder setProjectileItem(Item item)
        {
            this.gun.projectile.projectileItem = ForgeRegistries.ITEMS.getKey(item);
            return this;
        }

        public Builder setProjectileOverride(String override)
        {
            this.gun.projectile.projectileOverride = override;
            return this;
        }

        public Builder setProjectileVisible(boolean visible)
        {
            this.gun.projectile.visible = visible;
            return this;
        }

        public Builder setProjectileSize(float size)
        {
            this.gun.projectile.size = size;
            return this;
        }

        public Builder setProjectileSpeed(double speed)
        {
            this.gun.projectile.speed = speed;
            return this;
        }

        public Builder setProjectileLife(int life)
        {
            this.gun.projectile.life = life;
            return this;
        }

        public Builder setProjectileAffectedByGravity(boolean gravity)
        {
            this.gun.projectile.gravity = gravity;
            return this;
        }

        public Builder setProjectileGravityStrength(double gravity)
        {
            this.gun.projectile.gravityStrength = gravity;
            return this;
        }

        public Builder setProjectileTrailColor(int trailColor)
        {
            this.gun.projectile.trailColor = trailColor;
            return this;
        }

        public Builder setProjectileTrailLengthMultiplier(int trailLengthMultiplier)
        {
            this.gun.projectile.trailLengthMultiplier = trailLengthMultiplier;
            return this;
        }

        public Builder setDamage(float damage)
        {
            this.gun.projectile.damage = damage;
            return this;
        }

        public Builder setMaxRangeDamageMultiplier(float maxRangeDamageMultiplier)
        {
            this.gun.projectile.maxRangeDamageMultiplier = maxRangeDamageMultiplier;
            return this;
        }

        public Builder setMaxPierceCount(int maxPierceCount)
        {
            this.gun.projectile.maxPierceCount = maxPierceCount;
            return this;
        }

        public Builder setHeadshotExtraDamage(float headshotExtraDamage)
        {
            this.gun.projectile.headshotExtraDamage = headshotExtraDamage;
            return this;
        }

        public Builder setHeadshotMultiplierBonus(float headshotMultiplierBonus)
        {
            this.gun.projectile.headshotMultiplierBonus = headshotMultiplierBonus;
            return this;
        }

        public Builder setHeadshotMultiplierMin(float headshotMultiplierMin)
        {
            this.gun.projectile.headshotMultiplierMin = headshotMultiplierMin;
            return this;
        }

        public Builder setHeadshotMultiplierOverride(float headshotMultiplierOverride)
        {
            this.gun.projectile.headshotMultiplierOverride = headshotMultiplierOverride;
            return this;
        }

        public Builder setReduceDamageOverLife(boolean damageReduceOverLife)
        {
            this.gun.projectile.damageReduceOverLife = damageReduceOverLife;
            return this;
        }

        public Builder setFireSound(SoundEvent sound)
        {
            this.gun.sounds.fire = ForgeRegistries.SOUND_EVENTS.getKey(sound);
            return this;
        }

        public Builder setReloadSound(SoundEvent sound)
        {
            this.gun.sounds.reload = ForgeRegistries.SOUND_EVENTS.getKey(sound);
            return this;
        }

        public Builder setReloadStartSound(SoundEvent sound)
        {
            this.gun.sounds.reloadStart = ForgeRegistries.SOUND_EVENTS.getKey(sound);
            return this;
        }

        public Builder setCockSound(SoundEvent sound)
        {
            this.gun.sounds.cock = ForgeRegistries.SOUND_EVENTS.getKey(sound);
            return this;
        }

        public Builder setSilencedFireSound(SoundEvent sound)
        {
            this.gun.sounds.silencedFire = ForgeRegistries.SOUND_EVENTS.getKey(sound);
            return this;
        }

        public Builder setEnchantedFireSound(SoundEvent sound)
        {
            this.gun.sounds.enchantedFire = ForgeRegistries.SOUND_EVENTS.getKey(sound);
            return this;
        }

        @Deprecated(since = "1.3.0", forRemoval = true)
        public Builder setMuzzleFlash(double size, double xOffset, double yOffset, double zOffset)
        {
            Display.Flash flash = new Display.Flash();
            flash.size = size;
            flash.xOffset = xOffset;
            flash.yOffset = yOffset;
            flash.zOffset = zOffset;
            this.gun.display.flash = flash;
            return this;
        }

        public Builder setZoom(float fovModifier, double xOffset, double yOffset, double zOffset)
        {
            Modules.Zoom zoom = new Modules.Zoom();
            zoom.fovModifier = fovModifier;
            zoom.xOffset = xOffset;
            zoom.yOffset = yOffset;
            zoom.zOffset = zOffset;
            this.gun.modules.zoom = zoom;
            return this;
        }

        @Deprecated(since = "1.3.0", forRemoval = true)
        public Builder setZoom(Modules.Zoom.Builder builder)
        {
            this.gun.modules.zoom = builder.build();
            return this;
        }

        @Deprecated(since = "1.3.0", forRemoval = true)
        public Builder setScope(float scale, double xOffset, double yOffset, double zOffset)
        {
            ScaledPositioned positioned = new ScaledPositioned();
            positioned.scale = scale;
            positioned.xOffset = xOffset;
            positioned.yOffset = yOffset;
            positioned.zOffset = zOffset;
            this.gun.modules.attachments.scope = positioned;
            return this;
        }

        @Deprecated(since = "1.3.0", forRemoval = true)
        public Builder setBarrel(float scale, double xOffset, double yOffset, double zOffset)
        {
            ScaledPositioned positioned = new ScaledPositioned();
            positioned.scale = scale;
            positioned.xOffset = xOffset;
            positioned.yOffset = yOffset;
            positioned.zOffset = zOffset;
            this.gun.modules.attachments.barrel = positioned;
            return this;
        }

        @Deprecated(since = "1.3.0", forRemoval = true)
        public Builder setStock(float scale, double xOffset, double yOffset, double zOffset)
        {
            ScaledPositioned positioned = new ScaledPositioned();
            positioned.scale = scale;
            positioned.xOffset = xOffset;
            positioned.yOffset = yOffset;
            positioned.zOffset = zOffset;
            this.gun.modules.attachments.stock = positioned;
            return this;
        }

        @Deprecated(since = "1.3.0", forRemoval = true)
        public Builder setUnderBarrel(float scale, double xOffset, double yOffset, double zOffset)
        {
            ScaledPositioned positioned = new ScaledPositioned();
            positioned.scale = scale;
            positioned.xOffset = xOffset;
            positioned.yOffset = yOffset;
            positioned.zOffset = zOffset;
            this.gun.modules.attachments.underBarrel = positioned;
            return this;
        }
    }
}
