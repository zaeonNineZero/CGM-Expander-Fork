package com.mrcrayfish.guns.item.attachment;

import com.mrcrayfish.guns.item.attachment.impl.Magazine;

/**
 * An interface to turn an any item into a tactical attachment. This is useful if your item extends a
 * custom item class otherwise {@link com.mrcrayfish.guns.item.BarrelItem} can be used instead of
 * this interface.
 * <p>
 * Author: Ocelot, MrCrayfish
 */
public interface IMagazine extends IAttachment<Magazine>
{
    /**
     * @return The type of this attachment
     */
    @Override
    default Type getType()
    {
        return Type.MAGAZINE;
    }
}
