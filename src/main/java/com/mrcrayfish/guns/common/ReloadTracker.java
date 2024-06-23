package com.mrcrayfish.guns.common;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.client.handler.GunRenderingHandler;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.S2CMessageGunSound;
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
    private int reserveAmmo = 0;
    private int reloadSoundState = 0;

    private ReloadTracker(Player player)
    {
        this.startTick = player.tickCount;
        this.slot = player.getInventory().selected;
        this.stack = player.getInventory().getSelected();
        this.gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
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
        return tag.getInt("AmmoCount") >= GunEnchantmentHelper.getAmmoCapacity(this.stack, this.gun);
    }

    private boolean hasNoAmmo(Player player)
    {
        return Gun.findAmmo(player, this.gun.getProjectile().getItem()).stack().isEmpty();
    }

    private boolean canReload(Player player)
    {
        int deltaTicks = player.tickCount - this.startTick;
        int interval = GunEnchantmentHelper.getRealReloadSpeed(this.stack);
        return deltaTicks > 0 && deltaTicks % interval == 0;
    }

    private float getReloadProgress(Player player)
    {
        int deltaTicks = player.tickCount - this.startTick;
        int interval = GunEnchantmentHelper.getRealReloadSpeed(this.stack);
        return ((float) deltaTicks) / ((float) interval);
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
        int maxAmmo = GunEnchantmentHelper.getAmmoCapacity(this.stack, this.gun);
    	int ammoPerItem = this.gun.getGeneral().getAmmoPerItem();
    	int trueReloadAmount = (this.gun.getGeneral().getUseMagReload() ? maxAmmo : this.gun.getGeneral().getReloadAmount());
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
            
            if ((ammoLoaded<trueReloadAmount || this.gun.getGeneral().getUseMagReload()) && !endReload)
            context = Gun.findAmmo(player, this.gun.getProjectile().getItem());
        }
        
        int ammoAfterLoad = getInventoryAmmo(player);
        DelayedTask.runAfter(1, () ->
        {
        	GunRenderingHandler.get().forceSetReserveAmmo(ammoAfterLoad);
        });

        ResourceLocation reloadSound = this.gun.getSounds().getReload();
        this.reloadSoundState=0;
        if(reloadSound != null && !this.gun.getGeneral().getUseMagReload())
        {
            double radius = Config.SERVER.reloadMaxDistance.get();
            double soundX = player.getX();
            double soundY = player.getY() + 1.0;
            double soundZ = player.getZ();
            S2CMessageGunSound message = new S2CMessageGunSound(reloadSound, SoundSource.PLAYERS, (float) soundX, (float) soundY, (float) soundZ, 1.0F, 1.0F, player.getId(), false, true);
            PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level, soundX, soundY, soundZ, radius), message);
        }
    }
    
    public static void attemptReload(Player player)
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
            if(!tracker.isSameWeapon(player) || tracker.isWeaponFull() || tracker.hasNoAmmo(player))
            {
                RELOAD_TRACKER_MAP.remove(player);
                ModSyncedDataKeys.RELOADING.setValue(player, false);
                return;
            }
            if(tracker.canReload(player))
            {
                tracker.increaseAmmo(player);
                final Gun gun = tracker.gun;
                if(tracker.isWeaponFull() || tracker.hasNoAmmo(player) || gun.getGeneral().getUseMagReload())
                {
                    final Player finalPlayer = player;
                    ModSyncedDataKeys.RELOADING.setValue(finalPlayer, false);
                    //ModSyncedDataKeys.SWITCHTIME.setValue(player, gun.getGeneral().getPostReloadCooldown());
                    ModSyncedDataKeys.SWITCHTIME.setValue(finalPlayer, 8);
                    if (!gun.getGeneral().getUseMagReload())
                    {
	                    DelayedTask.runAfter(4, () ->
	                    {
	                        ResourceLocation cockSound = gun.getSounds().getCock();
	                        if(cockSound != null)
	                        playReloadSound(finalPlayer, cockSound);
	                    });
	                    }
                    else
                    playMagReloadEndSound(finalPlayer);
                    
                    RELOAD_TRACKER_MAP.remove(player);
                }
            }
            else
            {
                final Gun gun = tracker.gun;
                if (gun.getGeneral().getUseMagReload())
                {
                	if (tracker.reloadSoundState==0 && tracker.getReloadProgress(player)>=0.25)
                	{
                		playMagReloadMidSound1(player);
                		tracker.reloadSoundState++;
                	}
                	else
                	if (tracker.reloadSoundState==1 && tracker.getReloadProgress(player)>=0.5)
                	{
                		playMagReloadMidSound2(player);
                		tracker.reloadSoundState++;
                	}
                	else
                	if (tracker.reloadSoundState==2 && tracker.getReloadProgress(player)>=0.75)
                	{
                		playMagReloadMidSound3(player);
                		tracker.reloadSoundState++;
                	}
                }
                else
                {
                	if (tracker.reloadSoundState==0 && tracker.getReloadProgress(player)>=0.25)
                	{
                		playReloadCycleMidSound1(player);
                		tracker.reloadSoundState++;
                	}
                	else
                	if (tracker.reloadSoundState==1 && tracker.getReloadProgress(player)>=0.5)
                	{
                		playReloadCycleMidSound2(player);
                		tracker.reloadSoundState++;
                	}
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
            double soundY = player.getY() + 1.0;
            double soundZ = player.getZ();
            double radius = Config.SERVER.reloadMaxDistance.get();
            S2CMessageGunSound messageSound = new S2CMessageGunSound(sound, SoundSource.PLAYERS, (float) soundX, (float) soundY, (float) soundZ, 1.0F, 1.0F, player.getId(), false, true);
            PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level, soundX, soundY, soundZ, radius), messageSound);
        }
    }
    
    public static void playReloadCycleMidSound1(Player player)
    {
    	if(!RELOAD_TRACKER_MAP.containsKey(player))
    		return;
    	ReloadTracker tracker = RELOAD_TRACKER_MAP.get(player);
        ResourceLocation sound = tracker.gun.getSounds().getReloadCycleMiddle1();
        if (sound != null)
        playReloadSound(player, sound);
    }
    
    public static void playReloadCycleMidSound2(Player player)
    {
    	if(!RELOAD_TRACKER_MAP.containsKey(player))
    		return;
    	ReloadTracker tracker = RELOAD_TRACKER_MAP.get(player);
        ResourceLocation sound = tracker.gun.getSounds().getReloadCycleMiddle2();
        if (sound != null)
        playReloadSound(player, sound);
    }
    
    public static void playMagReloadMidSound1(Player player)
    {
    	if(!RELOAD_TRACKER_MAP.containsKey(player))
    		return;
    	ReloadTracker tracker = RELOAD_TRACKER_MAP.get(player);
        ResourceLocation sound = tracker.gun.getSounds().getMagReloadMiddle1();
        if (sound == null)
        sound = tracker.gun.getSounds().getReload();
        if (sound != null)
        playReloadSound(player, sound);
    }
    
    public static void playMagReloadMidSound2(Player player)
    {
    	if(!RELOAD_TRACKER_MAP.containsKey(player))
    		return;
    	ReloadTracker tracker = RELOAD_TRACKER_MAP.get(player);
        ResourceLocation sound = tracker.gun.getSounds().getMagReloadMiddle2();
        if (sound != null)
        playReloadSound(player, sound);
    }
    
    public static void playMagReloadMidSound3(Player player)
    {
    	if(!RELOAD_TRACKER_MAP.containsKey(player))
    		return;
    	ReloadTracker tracker = RELOAD_TRACKER_MAP.get(player);
        ResourceLocation sound = tracker.gun.getSounds().getMagReloadMiddle3();
        if (sound == null)
        sound = tracker.gun.getSounds().getReload();
        if (sound != null)
        playReloadSound(player, sound);
    }
    
    public static void playMagReloadEndSound(Player player)
    {
    	if(!RELOAD_TRACKER_MAP.containsKey(player))
    		return;
    	ReloadTracker tracker = RELOAD_TRACKER_MAP.get(player);
        ResourceLocation sound = tracker.gun.getSounds().getMagReloadEnd();
        if (sound == null)
        sound = tracker.gun.getSounds().getCock();
        if (sound != null)
        playReloadSound(player, sound);
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
            attemptReload(player);
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
