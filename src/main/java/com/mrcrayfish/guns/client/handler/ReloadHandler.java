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
            this.setReloading(!ModSyncedDataKeys.RELOADING.getValue(player));
            KeyBinds.KEY_RELOAD.setDown(false);
            if(player.getMainHandItem().getItem() instanceof GunItem)
            GunRenderingHandler.get().updateReserveAmmo(player);
        }
        if(KeyBinds.KEY_UNLOAD.consumeClick() && event.getAction() == GLFW.GLFW_PRESS)
        {
            this.setReloading(false);
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageUnload());
            if(player.getMainHandItem().getItem() instanceof GunItem)
        	GunRenderingHandler.get().stageReserveAmmoUpdate(2);
        }
    }

    public void setReloading(boolean reloading)
    {
    	setReloading(reloading, true);
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
                        Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
                        if (Gun.findAmmo((Player) player, gun.getProjectile().getItem()) == AmmoContext.NONE && !Gun.hasUnlimitedReloads(stack))
                        	return;
                        
                        ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
                        float cooldown = 0F;
                        cooldown = tracker.getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());
                        if (cooldown > gun.getGeneral().getReloadAllowedCooldown())
                            return;
                        if(tag.getInt("AmmoCount") >= GunEnchantmentHelper.getAmmoCapacity(stack, gun))
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
            	if (fromInput)
            	{
            		reloadFinish = false;
            		reloadStart = false;
            	}
                ModSyncedDataKeys.RELOADING.setValue(player, false);
                ModSyncedDataKeys.SWITCHTIME.setValue(player, storedReloadDelay+1);
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
                this.reloadingSlot = -1;
            }
        }
    }
    
    public boolean getReloading(Player player)
    {
        if(ModSyncedDataKeys.RELOADING.getValue(player))
        return true;
        else
        return false;
    }

    private void updateReloadDelay(Player player)
    {
    	int reloadStartDelay = 5;
    	int reloadInterruptDelay = 5;
    	int reloadEndDelay = 5;
    	ItemStack stack = player.getMainHandItem();
    	if(player.getMainHandItem().getItem() instanceof GunItem gun)
    	{
    		reloadStartDelay = Math.max(gun.getModifiedGun(stack).getGeneral().getReloadStartDelay(),1);
    		reloadInterruptDelay = Math.max(gun.getModifiedGun(stack).getGeneral().getReloadInterruptDelay(),5);
    		reloadEndDelay = Math.max(gun.getModifiedGun(stack).getGeneral().getReloadEndDelay(),1);
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
        }
        else
        {
            if(this.startReloadTick != -1)
            {
                this.startReloadTick = -1;
            }
            if(this.reloadTimer > 0)
            {
                this.reloadTimer-=1/reloadDelay;
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
}
