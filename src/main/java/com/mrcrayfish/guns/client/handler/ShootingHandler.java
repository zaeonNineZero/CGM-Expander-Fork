package com.mrcrayfish.guns.client.handler;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.client.KeyBinds;
import com.mrcrayfish.guns.common.GripType;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.compat.PlayerReviveHelper;
import com.mrcrayfish.guns.event.GunFireEvent;
import com.mrcrayfish.guns.init.ModSounds;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.C2SMessageReload;
import com.mrcrayfish.guns.network.message.C2SMessageShoot;
import com.mrcrayfish.guns.network.message.C2SMessageShooting;
import com.mrcrayfish.guns.util.GunCompositeStatHelper;
import com.mrcrayfish.guns.util.GunEnchantmentHelper;
import com.mrcrayfish.guns.util.GunModifierHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Author: MrCrayfish
 */
public class ShootingHandler
{
    private static ShootingHandler instance;

    public static ShootingHandler get()
    {
        if(instance == null)
        {
            instance = new ShootingHandler();
        }
        return instance;
    }

    private boolean shooting;
    private boolean doEmptyClick;

    private int slot = -1;

    private ShootingHandler() {}

    private boolean isInGame()
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.getOverlay() != null)
            return false;
        if(mc.screen != null)
            return false;
        if(!mc.mouseHandler.isMouseGrabbed())
            return false;
        return mc.isWindowActive();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onMouseClick(InputEvent.InteractionKeyMappingTriggered event)
    {
        if(event.isCanceled())
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if(player == null)
            return;

        if(PlayerReviveHelper.isBleeding(player))
            return;

        if(Config.CLIENT.controls.flipControls.get() ? event.isUseItem() : event.isAttack())
        {
            ItemStack heldItem = player.getMainHandItem();
            if(heldItem.getItem() instanceof GunItem gunItem)
            {
                event.setSwingHand(false);
                event.setCanceled(true);
            }
        }
        else 
        {
        	if(Config.CLIENT.controls.flipControls.get() ? event.isAttack() : event.isUseItem())
	        {
	            ItemStack heldItem = player.getMainHandItem();
	            if(heldItem.getItem() instanceof GunItem gunItem)
	            {
	                if(event.getHand() == InteractionHand.OFF_HAND)
	                {
	                    // Allow shields to be used if weapon is one-handed
	                    if(player.getOffhandItem().getItem() == Items.SHIELD)
	                    {
	                        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
	                        if(modifiedGun.getGeneral().getGripType() == GripType.ONE_HANDED || modifiedGun.getGeneral().getGripType() == GripType.PISTOL_CUSTOM)
	                        {
	                            return;
	                        }
	                    }
	                    event.setCanceled(true);
	                    event.setSwingHand(false);
	                    return;
	                }
	                if(Config.CLIENT.controls.flipControls.get() || AimingHandler.get().isZooming() && AimingHandler.get().isLookingAtInteractableBlock())
	                {
	                    event.setCanceled(true);
	                    event.setSwingHand(false);
	                }
	            }
	        }
        }
    }

	@SubscribeEvent
    public void onHandleShooting(TickEvent.ClientTickEvent event)
    {
        if(event.phase != TickEvent.Phase.START)
            return;

        if(!this.isInGame())
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if(player != null)
        {
            ItemStack heldItem = player.getMainHandItem();
            if(heldItem.getItem() instanceof GunItem && !isEmpty(player, heldItem) && !PlayerReviveHelper.isBleeding(player))
            {
                Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
                boolean shooting = (KeyBinds.getShootMapping().isDown() && !ModSyncedDataKeys.ONBURSTCOOLDOWN.getValue(player)) || (ModSyncedDataKeys.BURSTCOUNT.getValue(player)>0 && gun.getGeneral().hasBurstFire());
                if(GunMod.controllableLoaded)
                {
                    shooting |= ControllerHandler.isShooting();
                }
                if(shooting)
                {
                    if(!this.shooting)
                    {
                        this.shooting = true;
                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageShooting(true));
                    }
                }
                else if(this.shooting)
                {
                    this.shooting = false;
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageShooting(false));
                }
            }
            else if(this.shooting)
            {
                this.shooting = false;
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageShooting(false));
            }
        }
        else
        {
            this.shooting = false;
        }
    }

    @SubscribeEvent
    public void onPostClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase != TickEvent.Phase.END)
            return;

        if(!isInGame())
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if(player != null)
        {
            if(PlayerReviveHelper.isBleeding(player))
                return;

            if (!isSameWeapon(player))
            {
            	ModSyncedDataKeys.SWITCHTIME.setValue(player, 1);
            	ModSyncedDataKeys.BURSTCOUNT.setValue(player, 0);
                if(player.getMainHandItem().getItem() instanceof GunItem)
            	GunRenderingHandler.get().updateReserveAmmo(player);
            }
            
            ItemStack heldItem = player.getMainHandItem();
            if(heldItem.getItem() instanceof GunItem)
            {
            	Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
            	
            	if(KeyBinds.getShootMapping().isDown() || (ModSyncedDataKeys.BURSTCOUNT.getValue(player)>0 && gun.getGeneral().hasBurstFire()))
                {
                    this.fire(player, heldItem);
                    boolean doAutoFire = (gun.getGeneral().isAuto());
                    if(!doAutoFire)
                    {
                    	KeyBinds.getShootMapping().setDown(false);
                    }
                }
            	else
                doEmptyClick = true;
            }
            
            // Update stack and slot variables
            slot = player.getInventory().selected;
        }
    }

    private boolean canFire(Player player, ItemStack heldItem)
    {
    	if(!(heldItem.getItem() instanceof GunItem))
            return false;
        
        if(player.isSpectator())
            return false;
        
        if(ModSyncedDataKeys.RELOADING.getValue(player)) //*NEW* Disallow firing while reloading, and cancel reload.
        {
        	GunItem gunItem = (GunItem) heldItem.getItem();
        	if (!gunItem.getModifiedGun(heldItem).getGeneral().getUseMagReload())
        	{
        		ModSyncedDataKeys.RELOADING.setValue(player, false);
        		PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
        	}
        	return false;
        }
        
        if(ModSyncedDataKeys.SWITCHTIME.getValue(player) > 0) //*NEW* Disallow firing during the weapon switch/reload time.
        {
        	return false;
        }
        
        if(ModSyncedDataKeys.ONBURSTCOOLDOWN.getValue(player)) //*NEW* Disallow firing during the burst cooldown period.
        {
        	GunItem gunItem = (GunItem) heldItem.getItem();
        	if (gunItem.getModifiedGun(heldItem).getGeneral().hasBurstFire() && ModSyncedDataKeys.BURSTCOUNT.getValue(player)<=0)
        	return false;
        }

        if(player.getUseItem().getItem() == Items.SHIELD)
            return false;
        
        return true;
	}

    private boolean isEmpty(Player player, ItemStack heldItem)
    {
    	if(!(heldItem.getItem() instanceof GunItem))
            return false;
        
        if(player.isSpectator())
            return false;

        if((!Gun.hasAmmo(heldItem) || !Gun.canShoot(heldItem)) && !player.isCreative())
            return true;
        return false;
	}
    
    private boolean canUseTrigger(Player player, ItemStack heldItem)
    {
    	if(!(heldItem.getItem() instanceof GunItem))
            return false;
        
        if(player.isSpectator())
            return false;
        
        if(ModSyncedDataKeys.RELOADING.getValue(player))
        {
        	return false;
        }
        
        if(ModSyncedDataKeys.SWITCHTIME.getValue(player) > 0)
        {
        	return false;
        }
        
        if(ModSyncedDataKeys.ONBURSTCOOLDOWN.getValue(player))
        {
        	GunItem gunItem = (GunItem) heldItem.getItem();
        	if (gunItem.getModifiedGun(heldItem).getGeneral().hasBurstFire() && ModSyncedDataKeys.BURSTCOUNT.getValue(player)<=0)
        	return false;
        }

        if(player.getUseItem().getItem() == Items.SHIELD)
            return false;
        
        return true;
	}

    public void fire(Player player, ItemStack heldItem)
    {
        if(!(heldItem.getItem() instanceof GunItem))
            return;

        if(isEmpty(player, heldItem))
        {	

            ItemCooldowns tracker = player.getCooldowns();
            if(!tracker.isOnCooldown(heldItem.getItem()))
            {
            	if (doEmptyClick && heldItem.getItem() instanceof GunItem gunItem && canUseTrigger(player, heldItem))
	        	{
		            Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(gunItem.getModifiedGun(heldItem).getSounds().getEmptyClick(), SoundSource.PLAYERS, 0.8F, 1.0F, Minecraft.getInstance().level.getRandom(), false, 0, SoundInstance.Attenuation.NONE, 0, 0, 0, true));
		        	doEmptyClick = false;
	        	}
        	}
        	if (ModSyncedDataKeys.BURSTCOUNT.getValue(player)>0)
        	ModSyncedDataKeys.BURSTCOUNT.setValue(player, 0);
        	return;
        }
        
        if(player.isSprinting()) //*NEW* Stop sprinting when attempting to shoot a gun.
            player.setSprinting(false);
        
        if(!canFire(player, heldItem))
            return;
        
        ItemCooldowns tracker = player.getCooldowns();
        if(!tracker.isOnCooldown(heldItem.getItem()))
        {
            GunItem gunItem = (GunItem) heldItem.getItem();
            Gun modifiedGun = gunItem.getModifiedGun(heldItem);

            if(MinecraftForge.EVENT_BUS.post(new GunFireEvent.Pre(player, heldItem)))
                return;

            /*int rate = GunEnchantmentHelper.getRate(heldItem, modifiedGun);
            rate = GunModifierHelper.getModifiedRate(heldItem, rate);
            rate = GunEnchantmentHelper.getRampUpRate(player, heldItem, rate);*/
            int rate = GunCompositeStatHelper.getCompositeRate(heldItem,modifiedGun,player);
            tracker.addCooldown(heldItem.getItem(), rate);
            ModSyncedDataKeys.RAMPUPSHOT.setValue(player, ModSyncedDataKeys.RAMPUPSHOT.getValue(player)+1);
            
            int gunBurstCount = modifiedGun.getGeneral().getBurstCount();
            if (gunBurstCount > 0)
            {
                // Burst has not begun yet:
            	if (ModSyncedDataKeys.BURSTCOUNT.getValue(player)<=0)
            	ModSyncedDataKeys.BURSTCOUNT.setValue(player, gunBurstCount-1);
            	else
            	// When there are shots remaining in burst:
            	if (ModSyncedDataKeys.BURSTCOUNT.getValue(player)>0)
            	ModSyncedDataKeys.BURSTCOUNT.setValue(player, ModSyncedDataKeys.BURSTCOUNT.getValue(player)-1);
            }
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageShoot(player));

            MinecraftForge.EVENT_BUS.post(new GunFireEvent.Post(player, heldItem));
        }
    }
    
    private boolean isSameWeapon(Player player)
    {
        if (slot==-1)
        	return true;
    	return player.getInventory().selected == slot;
    }
}
