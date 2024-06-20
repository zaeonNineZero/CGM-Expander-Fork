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
public class SwitchGunTracker
{
    private static final Map<Player, SwitchGunTracker> SWITCHGUN_TRACKER_MAP = new WeakHashMap<>();

    private final int slot;
    private final ItemStack stack;
    private final Gun gun;

    private SwitchGunTracker(Player player)
    {
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
    
    private int getInventoryAmmo(Player player, Gun gun)
    {
    	return Gun.getReserveAmmoCount(player, gun.getProjectile().getItem());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START && !event.player.level.isClientSide)
        {
            Player player = event.player;
            if(!SWITCHGUN_TRACKER_MAP.containsKey(player))
            {
                if(!(player.getInventory().getSelected().getItem() instanceof GunItem))
                {
                	ModSyncedDataKeys.SWITCHTIME.setValue(player, 5);
                    return;
                }
                SWITCHGUN_TRACKER_MAP.put(player, new SwitchGunTracker(player));
            }
            SwitchGunTracker tracker = SWITCHGUN_TRACKER_MAP.get(player);
            boolean doGunSwitch = false;
            
            //Reload and weapon switch cooldown logic
            if(player.getInventory().getSelected().getItem() instanceof GunItem)
            {
            	if (tracker.isSameWeapon(player))
            	{
            		int switch_cooldown = ModSyncedDataKeys.SWITCHTIME.getValue(player);
            		if (switch_cooldown > 0)
            			switch_cooldown--;
            		if (switch_cooldown != ModSyncedDataKeys.SWITCHTIME.getValue(player))
            			ModSyncedDataKeys.SWITCHTIME.setValue(player, switch_cooldown);
            	}
            	else
                {
            		doGunSwitch = true;
                }
            }
            else doGunSwitch = true;
            
            if (doGunSwitch)
            {
            	ModSyncedDataKeys.SWITCHTIME.setValue(player, 5);
            	/*if(player.getInventory().getSelected().getItem() instanceof GunItem)
            	{
            		ItemStack newStack = player.getInventory().getSelected();
            		Gun newGun = ((GunItem) newStack.getItem()).getModifiedGun(newStack);
                	GunRenderingHandler.get().forceSetReserveAmmo(tracker.getInventoryAmmo(player, newGun));
            	}*/
            	if(SWITCHGUN_TRACKER_MAP.containsKey(player))
                {
            		SWITCHGUN_TRACKER_MAP.remove(player);
                }
            	return;
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerEvent.PlayerLoggedOutEvent event)
    {
        MinecraftServer server = event.getEntity().getServer();
        if(server != null)
        {
            server.execute(() -> SWITCHGUN_TRACKER_MAP.remove(event.getEntity()));
        }
    }
}
