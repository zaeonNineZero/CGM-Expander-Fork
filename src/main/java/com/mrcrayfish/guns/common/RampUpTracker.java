package com.mrcrayfish.guns.common;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.S2CMessageGunSound;
import com.mrcrayfish.guns.util.GunEnchantmentHelper;
import com.mrcrayfish.guns.util.GunModifierHelper;

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
public class RampUpTracker
{
    private static final Map<Player, RampUpTracker> RAMPUP_TRACKER_MAP = new WeakHashMap<>();

    private int rampdownTick;
    private final int slot;
    private final ItemStack stack;
    private final Gun gun;
    
    // This tracker handles the accelerating fire-rate of guns with the Ramp Up enchantment/parameter.

    private RampUpTracker(Player player)
    {
        this.rampdownTick = player.tickCount;
        this.slot = player.getInventory().selected;
        this.stack = player.getInventory().getSelected();
        this.gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
    }

    /**
     * Tests if the current item the player is holding is the same as the one being fired
     *
     * @param player the player to check
     * @return True if it's the same weapon and slot
     */
    private boolean isSameWeapon(Player player)
    {
        return !this.stack.isEmpty() && player.getInventory().selected == this.slot && player.getInventory().getSelected() == this.stack;
    }

    private boolean shouldDoRampdown(Player player)
    {
        int deltaTicks = player.tickCount - this.rampdownTick;
        int minTickDelay = GunEnchantmentHelper.getRampUpMaxRate(stack,gun);
        return deltaTicks > 0 && deltaTicks % minTickDelay == 0 && !ModSyncedDataKeys.SHOOTING.getValue(player);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START && !event.player.level.isClientSide)
        {
            Player player = event.player;
            if(!RAMPUP_TRACKER_MAP.containsKey(player))
            {
                if(!(player.getInventory().getSelected().getItem() instanceof GunItem))
                {
                	ModSyncedDataKeys.RAMPUPSHOT.setValue(player, 0);
                    return;
                }
                RAMPUP_TRACKER_MAP.put(player, new RampUpTracker(player));
            }
            RampUpTracker tracker = RAMPUP_TRACKER_MAP.get(player);
            if (ModSyncedDataKeys.SHOOTING.getValue(player))
            	tracker.rampdownTick = player.tickCount;
            boolean resetRampdown = false;
            
            if(player.getInventory().getSelected().getItem() instanceof GunItem)
            {
            	if (tracker.isSameWeapon(player))
            	{
            		int rampUpShot = ModSyncedDataKeys.RAMPUPSHOT.getValue(player);
                	if (rampUpShot > GunEnchantmentHelper.getRampUpMaxShots())
                		rampUpShot = GunEnchantmentHelper.getRampUpMaxShots();
                	if (tracker.shouldDoRampdown(player) && rampUpShot>0)
                    {
                		rampUpShot-=1;
                    	tracker.rampdownTick = player.tickCount;
                    }
                	if (ModSyncedDataKeys.RAMPUPSHOT.getValue(player) != rampUpShot)
                    	ModSyncedDataKeys.RAMPUPSHOT.setValue(player, rampUpShot);
            	}
            	else
                {
                	resetRampdown = true;
                }
            }
            else resetRampdown = true;
            
            if (resetRampdown)
            {
            	ModSyncedDataKeys.RAMPUPSHOT.setValue(player, 0);
            	if(RAMPUP_TRACKER_MAP.containsKey(player))
                {
            		RAMPUP_TRACKER_MAP.remove(player);
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
            server.execute(() -> RAMPUP_TRACKER_MAP.remove(event.getEntity()));
        }
    }
}
