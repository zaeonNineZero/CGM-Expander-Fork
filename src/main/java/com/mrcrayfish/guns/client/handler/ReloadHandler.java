package com.mrcrayfish.guns.client.handler;

import com.mrcrayfish.guns.client.KeyBinds;
import com.mrcrayfish.guns.common.AmmoContext;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.event.GunReloadEvent;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.C2SMessageReload;
import com.mrcrayfish.guns.network.message.C2SMessageUnload;
import com.mrcrayfish.guns.util.GunCompositeStatHelper;
import com.mrcrayfish.guns.util.GunEnchantmentHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Author: MrCrayfish
 */
public class ReloadHandler
{
    private static ReloadHandler instance;

    public static ReloadHandler get()
    {
        if(instance == null)
        {
            instance = new ReloadHandler();
        }
        return instance;
    }

    private int startReloadTick;
    private double reloadTimer;
    private double prevReloadTimer;
    private boolean doMagReload = false;
    private boolean reloadFromEmpty = false;
    private int storedReloadDelay;
    private int reloadingSlot;
    private boolean reloadStart;
    private boolean reloadFinish;

    private ReloadHandler()
    {
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase != TickEvent.Phase.END)
            return;

        this.prevReloadTimer = this.reloadTimer;

        Player player = Minecraft.getInstance().player;
        if(player != null)
        {
            this.updateReloadDelay(player);
            if(ModSyncedDataKeys.RELOADING.getValue(player))
            {
                if(this.reloadingSlot != player.getInventory().selected)
                {
                    this.setReloading(false, false);
                }
            }
            else
            {
            	ItemStack stack = player.getMainHandItem();
                if(stack.getItem() instanceof GunItem)
                {
                    CompoundTag tag = stack.getTag();
                    if(tag != null && !tag.contains("IgnoreAmmo", Tag.TAG_BYTE))
                    {
                    	if(tag.getInt("AmmoCount") > GunCompositeStatHelper.getAmmoCapacity(stack))
                    	{
                    		this.setReloading(false, true);
                            PacketHandler.getPlayChannel().sendToServer(new C2SMessageUnload(true));
                        	GunRenderingHandler.get().stageReserveAmmoUpdate(2);
                    	}
                    }
                }
            }

            this.updateReloadTimer(player);
        }
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.Key event)
    {
        Player player = Minecraft.getInstance().player;
        if(player == null)
            return;

        if(KeyBinds.KEY_RELOAD.isDown() && event.getAction() == GLFW.GLFW_PRESS)
        {
        	if (reloadTimer<=0 || reloadTimer>=1)
        	{
        		ItemStack stack = player.getMainHandItem();
                if(stack.getItem() instanceof GunItem)
                {
                    CompoundTag tag = stack.getTag();
                    if(tag != null && !tag.contains("IgnoreAmmo", Tag.TAG_BYTE))
                    {
                    	Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
                    	if(tag.getInt("AmmoCount") < GunCompositeStatHelper.getAmmoCapacity(stack, gun))
                    	this.setReloading(!ModSyncedDataKeys.RELOADING.getValue(player), true);
                    }
                }
        	}
            KeyBinds.KEY_RELOAD.setDown(false);
            if(player.getMainHandItem().getItem() instanceof GunItem)
            GunRenderingHandler.get().updateReserveAmmo(player);
        }
        if(KeyBinds.KEY_UNLOAD.consumeClick() && event.getAction() == GLFW.GLFW_PRESS && reloadTimer<=0)
        {
            this.setReloading(false, true);
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageUnload(false));
            if(player.getMainHandItem().getItem() instanceof GunItem)
        	GunRenderingHandler.get().stageReserveAmmoUpdate(2);
        }
    }

    public void setReloading(boolean reloading)
    {
    	setReloading(reloading, false);
    }
    
    public void setReloading(boolean reloading, boolean fromInput)
    {
        Player player = Minecraft.getInstance().player;
        if(player != null)
        {
            if(reloading)
            {
                ItemStack stack = player.getMainHandItem();
                if(stack.getItem() instanceof GunItem)
                {
                    CompoundTag tag = stack.getTag();
                    if(tag != null && !tag.contains("IgnoreAmmo", Tag.TAG_BYTE))
                    {
                    	if (!Gun.hasAmmo(stack))
                    		reloadFromEmpty = true;
                    	else
                        	reloadFromEmpty = false;

                    	if (Gun.usesMagReloads(stack))
                    		doMagReload = true;
                    	else
                    		doMagReload = false;
                    	
                        Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
                        if (Gun.findAmmo((Player) player, gun.getProjectile().getItem()) == AmmoContext.NONE && !Gun.hasUnlimitedReloads(stack))
                        	return;
                        
                        ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
                        float cooldown = 0F;
                        cooldown = tracker.getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());
                        if (cooldown > gun.getGeneral().getReloadAllowedCooldown())
                            return;
                        if(tag.getInt("AmmoCount") >= GunCompositeStatHelper.getAmmoCapacity(stack, gun))
                            return;
                        if(MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Pre(player, stack)))
                            return;
                        ModSyncedDataKeys.RELOADING.setValue(player, true);
                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(true));
                        this.reloadingSlot = player.getInventory().selected;
                        MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Post(player, stack));
                        GunRenderingHandler.get().updateReserveAmmo(player, gun);
                    	reloadFinish = true;
                    	reloadStart = true;
                    }
                }
            }
            else
            {
            	ItemStack stack = player.getMainHandItem();
            	GunRenderingHandler.get().getReloadDeltaTime(stack);
            	if (fromInput)
            	reloadFinish = false;
            	
            	// Debug 1
                /*if(stack.getItem() instanceof GunItem gunItem)
                {
    		    	float interval = GunEnchantmentHelper.getRealReloadSpeed(stack, ReloadHandler.get().isDoMagReload(), ReloadHandler.get().isReloadFromEmpty());
    		    	String logOutput = 
    		    		"Cancelling after " + (Math.round(GunRenderingHandler.get().getReloadDeltaTime(stack)*interval*10)/10)
    		    		+ " ticks (" + (Math.round(GunRenderingHandler.get().getReloadDeltaTime(stack)*1000)/10) + "%)"
    		    		+ ", and at " + (Math.round(GunRenderingHandler.get().getReloadCycleProgress(stack)*interval*10)/10)
    		    		+ " ticks (" + (Math.round(GunRenderingHandler.get().getReloadCycleProgress(stack)*1000)/10) + "%)"
    		    		+ " into the current reload cycle."
    		    		+ " (Full reload cycle is " + interval + " ticks long.)"
    		    	;
                	GunMod.LOGGER.info(logOutput);
                }*/
                    
                ModSyncedDataKeys.RELOADING.setValue(player, false);
                ModSyncedDataKeys.SWITCHTIME.setValue(player, storedReloadDelay+1);
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
                this.reloadingSlot = -1;

            	// Debug 2
                /*if(stack.getItem() instanceof GunItem gunItem)
                {
    		    	float interval = GunEnchantmentHelper.getRealReloadSpeed(stack, ReloadHandler.get().isDoMagReload(), ReloadHandler.get().isReloadFromEmpty());
    		    	String logOutput = 
    		    		"Reload cancelled after " + (Math.round(GunRenderingHandler.get().getReloadDeltaTime(stack)*interval*10)/10)
    		    		+ " ticks (" + (Math.round(GunRenderingHandler.get().getReloadDeltaTime(stack)*1000)/10) + "%)"
    		    		+ ", and at " + (Math.round(GunRenderingHandler.get().getReloadCycleProgress(stack)*interval*10)/10)
    		    		+ " ticks (" + (Math.round(GunRenderingHandler.get().getReloadCycleProgress(stack)*1000)/10) + "%)"
    		    		+ " into the current reload cycle. (" + interval + " ticks per cycle)"
    		    	;
                	GunMod.LOGGER.info(logOutput);
                }*/
            }
        }
    }
    
    public boolean getReloading(Player player)
    {
    	return (ModSyncedDataKeys.RELOADING.getValue(player));
    }

    private void updateReloadDelay(Player player)
    {
    	int reloadStartDelay = 5;
    	int reloadInterruptDelay = 5;
    	int reloadEndDelay = 5;
    	ItemStack stack = player.getMainHandItem();
    	if(player.getMainHandItem().getItem() instanceof GunItem gun)
    	{
    		reloadStartDelay = Math.max(reloadFromEmpty ? gun.getModifiedGun(stack).getGeneral().getReloadEmptyStartDelay() : gun.getModifiedGun(stack).getGeneral().getReloadStartDelay(),1);
    		reloadInterruptDelay = Math.max(reloadFromEmpty ? gun.getModifiedGun(stack).getGeneral().getReloadEmptyInterruptDelay() : gun.getModifiedGun(stack).getGeneral().getReloadInterruptDelay(),5);
    		reloadEndDelay = Math.max(reloadFromEmpty ? gun.getModifiedGun(stack).getGeneral().getReloadEmptyEndDelay() : gun.getModifiedGun(stack).getGeneral().getReloadEndDelay(),1);
    	}
    	storedReloadDelay = (reloadFinish && !getReloading(player)) ? reloadEndDelay : ((reloadStart && getReloading(player)) ? reloadStartDelay : reloadInterruptDelay);
    }

    private void updateReloadTimer(Player player)
    {
    	double reloadDelay = storedReloadDelay;
    	
        if(getReloading(player))
        {
            if(this.startReloadTick == -1)
            {
                this.startReloadTick = player.tickCount;
            }
            if(this.reloadTimer < 1)
            {
                this.reloadTimer+=1/reloadDelay;
            }
            else
            {
            	if (reloadStart)
            	reloadStart = false;
            }
        }
        else
        {
            if(this.reloadTimer > 0)
            {
                this.reloadTimer-=1/reloadDelay;
            }
            if(reloadTimer<=0 && this.startReloadTick != -1)
            {
                this.startReloadTick = -1;
            }
        }
        reloadTimer=Mth.clamp(reloadTimer ,0,1);
    }

    public int getStartReloadTick()
    {
        return this.startReloadTick;
    }

    public double getReloadTimer()
    {
        return this.reloadTimer;
    }

    public float getReloadProgress(float partialTicks)
    {
        return (float) Mth.lerp(partialTicks, this.prevReloadTimer, this.reloadTimer);
        //return (this.prevReloadTimer + (this.reloadTimer - this.prevReloadTimer) * partialTicks) / 5;
    }

    public boolean doReloadStartAnimation()
    {
        return reloadStart;
    }

    public boolean doReloadFinishAnimation()
    {
        return reloadFinish;
    }

    public boolean isDoMagReload()
    {
        return doMagReload;
    }

    public boolean isReloadFromEmpty()
    {
        return reloadFromEmpty;
    }

    // This method allows the ShootingHandler to tell the ReloadHandler when a weapon was switched.
	public void weaponSwitched()
	{
		reloadStart = false;
		reloadFinish = false;
		reloadTimer = 0;
		prevReloadTimer = 0;
	}
}
