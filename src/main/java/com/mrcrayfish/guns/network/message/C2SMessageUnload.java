package com.mrcrayfish.guns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.guns.common.network.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class C2SMessageUnload extends PlayMessage<C2SMessageUnload>
{
	private boolean partial;

    public C2SMessageUnload() {}

    public C2SMessageUnload(boolean partial)
    {
        this.partial = partial;
    }

    @Override
    public void encode(C2SMessageUnload message, FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(message.partial);
    }

    @Override
    public C2SMessageUnload decode(FriendlyByteBuf buffer)
    {
        boolean partial = buffer.readBoolean();
        return new C2SMessageUnload(partial);
    }

    @Override
    public void handle(C2SMessageUnload message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null && !player.isSpectator())
            {
                ServerPlayHandler.handleUnload(player, message.partial);
            }
        });
        context.setHandled(true);
    }
}
