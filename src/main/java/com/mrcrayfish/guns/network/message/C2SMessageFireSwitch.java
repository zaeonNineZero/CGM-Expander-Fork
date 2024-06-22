package com.mrcrayfish.guns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.common.network.ServerPlayHandler;
import com.mrcrayfish.guns.event.GunReloadEvent;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class C2SMessageFireSwitch extends PlayMessage<C2SMessageFireSwitch>
{
    private int fireMode;

    public C2SMessageFireSwitch() {}

    public C2SMessageFireSwitch(int fireMode)
    {
        this.fireMode = fireMode;
    }

    @Override
    public void encode(C2SMessageFireSwitch message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.fireMode);
    }

    @Override
    public C2SMessageFireSwitch decode(FriendlyByteBuf buffer)
    {
        return new C2SMessageFireSwitch(buffer.readInt());
    }

    @Override
    public void handle(C2SMessageFireSwitch message, MessageContext context)
    {
        context.execute(() ->
        {
        	ServerPlayer player = context.getPlayer();
            if(player != null && !player.isSpectator())
            {
	        	//GunMod.LOGGER.info("Received fire mode switch message from " + context.getPlayer().getName());
	        	ServerPlayHandler.handleFireSwitch(message, player);
            }
        });
        context.setHandled(true);
    }

    public int getFireMode()
    {
        return this.fireMode;
    }
}
