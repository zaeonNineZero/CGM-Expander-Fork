package com.mrcrayfish.guns.client.util;

import com.mrcrayfish.guns.cache.ObjectCache;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.interfaces.IGunModifier;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.item.attachment.IAttachment;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Helper class for more complex gun animations, including attachment and hand movements
 */
public final class GunReloadAnimationHelper
{
    public static final String CACHE_KEY = "properties";

    public static void resetCache()
    {
        ObjectCache.getInstance(CACHE_KEY).reset();
    }
    
	public static Vec3 applyAnimation(ItemStack weapon, float progress, String param)
	{
		Vec3 blendedFrame = Vec3.ZERO;
		int frameCount = 5;
		float scaledProgress = progress*(frameCount-1);
		
		Vec3 priorFrame = Vec3.ZERO;
		Vec3 nextFrame = Vec3.ZERO;
		blendedFrame = priorFrame.lerp(nextFrame, scaledProgress);
		
		return blendedFrame;
	}
}