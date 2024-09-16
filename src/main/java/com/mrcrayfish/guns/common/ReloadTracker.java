package com.mrcrayfish.guns.common;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.client.handler.GunRenderingHandler;
import com.mrcrayfish.guns.common.Gun.ReloadSoundsBase;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.S2CMessageGunSound;
import com.mrcrayfish.guns.util.GunCompositeStatHelper;
import com.mrcrayfish.guns.util.GunEnchantmentHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ReloadTracker
{
    private static final Map<Player, ReloadTracker> RELOAD_TRACKER_MAP = new WeakHashMap<>();

    private final int startTick;
    private final int slot;
    private final ItemStack stack;
    private final Gun gun;
    private final int reloadStartDelay;
    private final int reloadEndDelay;
    private final boolean doMagReload;
    private final boolean reloadFromEmpty;
    private int delayedStartTick;
    private int cycleStartTick;
    private int reserveAmmo = 0;
    private boolean reloadEarlyState;
    private boolean reloadMidState;
    private boolean reloadLateState;
    private boolean reloadClipOutState;
    private boolean reloadClipInState;
    private boolean resetSoundStates;

    private ReloadTracker(Player player)
    {
        this.startTick = player.tickCount;
        this.delayedStartTick = player.tickCount;
        this.slot = player.getInventory().selected;
        this.stack = player.getInventory().getSelected();
        this.doMagReload = Gun.usesMagReloads(stack);
        
        if (stack.getItem() instanceof GunItem)
        this.reloadFromEmpty = (Gun.hasAmmo(stack) ? false : true);
        else
        this.reloadFromEmpty = false;
        	
        this.gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
        if (reloadFromEmpty)
        this.reloadStartDelay = Math.max(gun.getGeneral().getReloadEmptyStartDelay(),0);
        else
        this.reloadStartDelay = Math.max(gun.getGeneral().getReloadStartDelay(),0);
        if (reloadFromEmpty)
        this.reloadEndDelay = Math.max(gun.getGeneral().getReloadEmptyEndDelay(),1);
        else
        this.reloadEndDelay = Math.max(gun.getGeneral().getReloadEndDelay(),1);
    }

    /**
     * Tests if the current item the player is holding is the same as the one being reloaded
     *
     * @param player the player to check
     * @return True if it's the same weapon and slot
     */
    private boolean isSameWeapon(Player player)
    {
        return !this.stack.isEmpty() && player.getInventory().selected == this.slot && player.getInventory().getSelected() == this.stack;
    }

    /**
     * @return
     */
    private boolean isWeaponFull()
    {
        CompoundTag tag = this.stack.getOrCreateTag();
        return tag.getInt("AmmoCount") >= GunCompositeStatHelper.getAmmoCapacity(this.stack, this.gun);
    }

    private boolean hasNoAmmo(Player player)
    {
        return Gun.findAmmo(player, this.gun.getProjectile().getItem()).stack().isEmpty();
    }

    private boolean canReload(Player player)
    {
    	double deltaTicks = player.tickCount - (this.delayedStartTick);
        double interval = GunCompositeStatHelper.getRealReloadSpeed(this.stack, doMagReload, reloadFromEmpty) * 1.0;
        return deltaTicks > 0 && deltaTicks % interval == 0;
    }

    private boolean reloadCycleEnd(Player player)
    {
        int deltaTicks = player.tickCount - (this.delayedStartTick);
        int interval = GunCompositeStatHelper.getRealReloadSpeed(this.stack, doMagReload, reloadFromEmpty);
        return deltaTicks > 0 && deltaTicks % interval == 0;
    }

    private float getReloadProgress(Player player)
    {
        int deltaTicks = player.tickCount - (this.delayedStartTick);
        int interval = GunCompositeStatHelper.getRealReloadSpeed(this.stack, doMagReload, reloadFromEmpty);
        float output = (Math.max(deltaTicks,0) % interval);
        return (output) / interval;
    }
    
    private int getInventoryAmmo(Player player)
    {
    	return Gun.getReserveAmmoCount(player, this.gun.getProjectile().getItem());
    }

    private void increaseAmmo(Player player)
    {
    	int ammoLoaded = 0;
        AmmoContext context = Gun.findAmmo(player, this.gun.getProjectile().getItem());
    	boolean endReload = false;

    	int attempts = 0;
    	int itemsLoaded = 0;
        int maxAmmo = GunCompositeStatHelper.getAmmoCapacity(this.stack, this.gun);
    	int ammoPerItem = this.gun.getGeneral().getAmmoPerItem();
    	int trueReloadAmount = (doMagReload ? maxAmmo : this.gun.getGeneral().getReloadAmount());
        while (ammoLoaded<trueReloadAmount && attempts<64 && !endReload)
        {
        	attempts++;
        	ItemStack ammo = context.stack();
            if(!ammo.isEmpty())
            {
            	int amount = Math.min(ammo.getCount(), trueReloadAmount);
                CompoundTag tag = this.stack.getTag();
                if(tag != null)
                {
                    amount = Math.min(amount, maxAmmo - tag.getInt("AmmoCount"));
                    if (tag.getInt("AmmoCount") < GunCompositeStatHelper.getAmmoCapacity(stack, gun))
                    tag.putInt("AmmoCount", tag.getInt("AmmoCount") + amount);
                    ammoLoaded += amount;
                    
                    if (tag.getInt("AmmoCount") >= maxAmmo)
                    endReload = true;
                }
                ammo.shrink((int) Math.ceil(amount/ammoPerItem));

                // Trigger that the container changed
                Container container = context.container();
                if(container != null)
                {
                    container.setChanged();
                }
            }
            else
            endReload = true;
            
            if ((ammoLoaded<trueReloadAmount || doMagReload) && !endReload)
            context = Gun.findAmmo(player, this.gun.getProjectile().getItem());
        }
        
        int ammoAfterLoad = getInventoryAmmo(player);
        DelayedTask.runAfter(1, () ->
        {
        	GunRenderingHandler.get().forceSetReserveAmmo(ammoAfterLoad);
        });
        
        this.resetSoundStates = true;
        this.resetAnimationSounds();

        ResourceLocation reloadSound = this.gun.getSounds().getReload();
    	
        if(reloadSound != null && (!gun.getGeneral().usesMagReload() && !Gun.hasExtraReloadSounds(gun)))
        {
            double radius = Config.SERVER.reloadMaxDistance.get();
            double soundX = player.getX();
            double soundY = player.getY() + 1.0;
            double soundZ = player.getZ();
            S2CMessageGunSound message = new S2CMessageGunSound(reloadSound, SoundSource.PLAYERS, (float) soundX, (float) soundY, (float) soundZ, 1.0F, 1.0F, player.getId(), false, true);
            PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level, soundX, soundY, soundZ, radius), message);
        }
    }
    
    public static void handleReload(Player player)
    {
    	if(ModSyncedDataKeys.RELOADING.getValue(player))
        {
            if(!RELOAD_TRACKER_MAP.containsKey(player))
            {
                if(!(player.getInventory().getSelected().getItem() instanceof GunItem))
                {
                    ModSyncedDataKeys.RELOADING.setValue(player, false);
                    return;
                }
                RELOAD_TRACKER_MAP.put(player, new ReloadTracker(player));
            }
            ReloadTracker tracker = RELOAD_TRACKER_MAP.get(player);
            if(!tracker.isSameWeapon(player) || tracker.hasNoAmmo(player))
            {
                RELOAD_TRACKER_MAP.remove(player);
                ModSyncedDataKeys.RELOADING.setValue(player, false);
                return;
            }
            if (player.tickCount - (tracker.startTick) < tracker.reloadStartDelay)
            {
            	CompoundTag tag = tracker.stack.getTag();
            	if(tag == null || tag.getInt("AmmoCount") >= GunCompositeStatHelper.getAmmoCapacity(tracker.stack, tracker.gun))
            	{
            		ModSyncedDataKeys.RELOADING.setValue(player, false);
            		return;
            	}
            	
            	tracker.delayedStartTick = player.tickCount;
            	return;
        	}
            
            // Extra reload sound logic
            {
                final Gun gun = tracker.gun;
                if (!tracker.reloadEarlyState)
            	{
            		ReloadSoundsBase soundObj = Gun.findReloadSoundObj(tracker.gun, "reloadEarly", tracker.doMagReload, tracker.reloadFromEmpty);
            		if (tracker.getReloadProgress(player)>=Gun.getReloadSoundTimings(gun, soundObj, "reloadEarly", tracker.doMagReload, tracker.reloadFromEmpty) || tracker.reloadCycleEnd(player))
            		{
            			playReloadSound(player, "reloadEarly");
            			tracker.reloadEarlyState=true;
            		}
            	}
            	if (!tracker.reloadMidState)
            	{
            		ReloadSoundsBase soundObj = Gun.findReloadSoundObj(tracker.gun, "reloadMid", tracker.doMagReload, tracker.reloadFromEmpty);
            		if (tracker.getReloadProgress(player)>=Gun.getReloadSoundTimings(gun, soundObj, "reloadMid", tracker.doMagReload, tracker.reloadFromEmpty) || tracker.reloadCycleEnd(player))
            		{
            			playReloadSound(player, "reloadMid");
            			tracker.reloadMidState=true;
            		}
            	}
            	//if (!tracker.reloadLateState && tracker.getReloadProgress(player)>=gun.getSounds().getReloadLateThreshold())
            	if (!tracker.reloadLateState)
            	{
            		ReloadSoundsBase soundObj = Gun.findReloadSoundObj(tracker.gun, "reloadLate", tracker.doMagReload, tracker.reloadFromEmpty);
            		if (tracker.getReloadProgress(player)>=Gun.getReloadSoundTimings(gun, soundObj, "reloadLate", tracker.doMagReload, tracker.reloadFromEmpty) || tracker.reloadCycleEnd(player))
            		{
            			playReloadSound(player, "reloadLate");
            			tracker.reloadLateState=true;
            		}
            	}
            	if (!tracker.reloadClipOutState)
            	{
                	//if (gun.getSounds().hasExtraReloadSounds() && tracker.getReloadProgress(player)>=gun.getSounds().getReloadClipOutThreshold()
            		ReloadSoundsBase soundObj = Gun.findReloadSoundObj(tracker.gun, "reloadClipOut", tracker.doMagReload, tracker.reloadFromEmpty);
            		if ((Gun.hasExtraReloadSounds(gun) && (tracker.getReloadProgress(player)>=Gun.getReloadSoundTimings(gun, soundObj, "reloadClipOut", tracker.doMagReload, tracker.reloadFromEmpty) || tracker.reloadCycleEnd(player)))
                	|| (!Gun.hasExtraReloadSounds(gun) && gun.getGeneral().usesMagReload() && tracker.getReloadProgress(player)>=0.25F))
                	{
            			if (Gun.hasExtraReloadSounds(gun))
                    	playReloadSound(player, "reloadClipOut");
                    	else
                        if (gun.getGeneral().usesMagReload())
                        playReloadSound(player, "reload");
                		tracker.reloadClipOutState=true;
            		}
            	}
            	if (!tracker.reloadClipInState)
            	{
                	//if (gun.getSounds().hasExtraReloadSounds() && tracker.getReloadProgress(player)>=gun.getSounds().getReloadClipInThreshold()
            		ReloadSoundsBase soundObj = Gun.findReloadSoundObj(tracker.gun, "reloadClipIn", tracker.doMagReload, tracker.reloadFromEmpty);
                    if ((Gun.hasExtraReloadSounds(gun) && (tracker.getReloadProgress(player)>=Gun.getReloadSoundTimings(gun, soundObj, "reloadClipIn", tracker.doMagReload, tracker.reloadFromEmpty) || tracker.reloadCycleEnd(player)))
                	|| (!Gun.hasExtraReloadSounds(gun) && gun.getGeneral().usesMagReload() && tracker.getReloadProgress(player)>=0.75F))
                	{
                		if (Gun.hasExtraReloadSounds(gun))
                    	playReloadSound(player, "reloadClipIn");
                    	else
                    	if (gun.getGeneral().usesMagReload())
                        playReloadSound(player, "reload");
                    	tracker.reloadClipInState=true;
            		}
            	}
            	
            	if (tracker.resetSoundStates)
            	tracker.resetSoundStates = false;
            }
            
            if(tracker.canReload(player))
                tracker.increaseAmmo(player);
            
            if(tracker.reloadCycleEnd(player))
            {
                final Gun gun = tracker.gun;
                if(tracker.isWeaponFull() || tracker.hasNoAmmo(player) || tracker.doMagReload)
                {
                    final Player finalPlayer = player;
                    ModSyncedDataKeys.RELOADING.setValue(finalPlayer, false);
                    ModSyncedDataKeys.SWITCHTIME.setValue(player, tracker.reloadEndDelay);
                	String soundType = "cock";
                	if (Gun.hasExtraReloadSounds(gun))
                	{
                		soundType = "reloadEnd";
                	}
                	final ResourceLocation finalSound = getReloadSound(finalPlayer, soundType);
                	
                    if (!tracker.doMagReload)
                    {
                    	if (Gun.hasExtraReloadSounds(gun))
                    	{
                    		ReloadSoundsBase soundObj = Gun.findReloadSoundObj(tracker.gun, soundType, tracker.doMagReload, tracker.reloadFromEmpty);
                    		if (Gun.getReloadSoundTimings(gun, soundObj, soundType, tracker.doMagReload, tracker.reloadFromEmpty)>0)
    	                    {
    	                    	DelayedTask.runAfter((int) Gun.getReloadSoundTimings(gun, soundObj, soundType, tracker.doMagReload, tracker.reloadFromEmpty), () ->
    		                    {
    		                        playReloadSound(finalPlayer, finalSound);
    		                    });
    	                    }
	                    	else
	                        playReloadSound(finalPlayer, finalSound);
                    	}
                    	else
                    	DelayedTask.runAfter(4, () ->
	                    {
	                        ResourceLocation cockSound = tracker.gun.getSounds().getCock();
	                        playReloadSound(finalPlayer, cockSound);
	                    });
                    }
                    else
                    {
                    	//if (Gun.getReloadSoundTimings(tracker.stack, gun, "getReloadEndDelay", tracker.reloadFromEmpty)>0)
                    	ReloadSoundsBase soundObj = Gun.findReloadSoundObj(tracker.gun, soundType, tracker.doMagReload, tracker.reloadFromEmpty);
                		if (Gun.getReloadSoundTimings(gun, soundObj, soundType, tracker.doMagReload, tracker.reloadFromEmpty)>0)
	                    {
	                    	DelayedTask.runAfter((int) Gun.getReloadSoundTimings(gun, soundObj, soundType, tracker.doMagReload, tracker.reloadFromEmpty), () ->
		                    {
		                        playReloadSound(finalPlayer, finalSound);
		                    });
	                    }
                    	else
                        playReloadSound(finalPlayer, finalSound);
                    }
                    
                    RELOAD_TRACKER_MAP.remove(player);
                }
            }
        }
        else if(RELOAD_TRACKER_MAP.containsKey(player))
        {
            RELOAD_TRACKER_MAP.remove(player);
        }
    }

    private static void playReloadSound(Player player, ResourceLocation sound)
    {
    	if(sound != null && player.isAlive())
        {
            double soundX = player.getX();
            double soundY = player.getY() + player.getEyeHeight();
            double soundZ = player.getZ();
            double radius = Config.SERVER.reloadMaxDistance.get();
            S2CMessageGunSound messageSound = new S2CMessageGunSound(sound, SoundSource.PLAYERS, (float) soundX, (float) soundY, (float) soundZ, 1.0F, 1.0F, player.getId(), false, true);
            PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level, soundX, soundY, soundZ, radius), messageSound);
        }
    }
    
    public static ResourceLocation getReloadSound(Player player, String soundType)
    {
    	if(!RELOAD_TRACKER_MAP.containsKey(player))
    		return null;
    	ReloadTracker tracker = RELOAD_TRACKER_MAP.get(player);
        ResourceLocation sound = null;
        ReloadSoundsBase soundObj = Gun.findReloadSoundObj(tracker.gun, soundType, tracker.doMagReload, tracker.reloadFromEmpty);

        /*if(soundType == "start")
        	sound = tracker.gun.getSounds().getReloadStart();
        else
        if(soundType == "early")
        	sound = tracker.gun.getSounds().getReloadEarly();
        else
        if(soundType == "mid")
        	sound = tracker.gun.getSounds().getReloadMid();
        else
        if(soundType == "late")
        	sound = tracker.gun.getSounds().getReloadLate();
        else
        if(soundType == "end")
        	sound = tracker.gun.getSounds().getReloadEnd();
        else
        if(soundType == "clipOut")
        	sound = tracker.gun.getSounds().getReloadClipOut();
        else
        if(soundType == "clipIn")
        	sound = tracker.gun.getSounds().getReloadClipIn();
        else
        if(soundType == "reload")
        	sound = tracker.gun.getSounds().getReload();
        else*/
        if(soundType == "cock")
        	sound = tracker.gun.getSounds().getCock();
        else
        	sound = Gun.getReloadSound(tracker.gun, soundObj, soundType, tracker.doMagReload, tracker.reloadFromEmpty);

        return sound;
    }
    
    public static void playReloadSound(Player player, String soundType)
    {
    	if(!RELOAD_TRACKER_MAP.containsKey(player))
    		return;
    	ResourceLocation sound = getReloadSound(player, soundType);
        
        if (sound != null)
        playReloadSound(player, sound);
    }
    
    private void resetAnimationSounds()
    {

    	this.reloadEarlyState = false;
        this.reloadMidState = false;
        this.reloadLateState = false;
        this.reloadClipOutState = false;
        this.reloadClipInState = false;
    }
    
    public int getReserveAmmo()
    {
    	return reserveAmmo;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START && !event.player.level.isClientSide)
        {
            Player player = event.player;
            // Reload logic
            handleReload(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerEvent.PlayerLoggedOutEvent event)
    {
        MinecraftServer server = event.getEntity().getServer();
        if(server != null)
        {
            server.execute(() -> RELOAD_TRACKER_MAP.remove(event.getEntity()));
        }
    }
}
