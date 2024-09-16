package com.mrcrayfish.guns.item.attachment.impl;

import com.mrcrayfish.guns.interfaces.IGunModifier;

/**
 * An attachment class related to tactical attachments. Use {@link #create(IGunModifier...)} to create an
 * get.
 * <p>
 * Author: MrCrayfish
 */
public class Tactical extends Attachment
{
    private Tactical(IGunModifier... modifier)
    {
        super(modifier);
    }

    /**
     * Creates a tactical get
     *
     * @param modifier an array of gun modifiers
     * @return a tactical get
     */
    public static Tactical create(IGunModifier... modifier)
    {
        return new Tactical(modifier);
    }
}
