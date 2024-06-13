package com.mrcrayfish.guns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.guns.client.network.ClientPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class S2CMessageBlood extends PlayMessage<S2CMessageBlood>
{
    private double x;
    private double y;
    private double z;
    private boolean allowBlood;
    private boolean isHeadshot;

    public S2CMessageBlood() {}

    public S2CMessageBlood(double x, double y, double z, boolean allowBlood, boolean isHeadshot)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.allowBlood = allowBlood;
        this.isHeadshot = isHeadshot;
    }

    @Override
    public void encode(S2CMessageBlood message, FriendlyByteBuf buffer)
    {
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
        buffer.writeBoolean(message.allowBlood);
        buffer.writeBoolean(message.isHeadshot);
    }

    @Override
    public S2CMessageBlood decode(FriendlyByteBuf buffer)
    {
        return new S2CMessageBlood(buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readBoolean(), buffer.readBoolean());
    }

    @Override
    public void handle(S2CMessageBlood message, MessageContext context)
    {
        context.execute(() -> ClientPlayHandler.handleMessageBlood(message));
        context.setHandled(true);
    }

    public double getX()
    {
        return this.x;
    }

    public double getY()
    {
        return this.y;
    }

    public double getZ()
    {
        return this.z;
    }

    public boolean getAllowBlood()
    {
        return this.allowBlood;
    }

    public boolean isHeadshot()
    {
        return this.isHeadshot;
    }
}
