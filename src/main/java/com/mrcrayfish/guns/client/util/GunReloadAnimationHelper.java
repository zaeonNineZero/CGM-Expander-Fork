package com.mrcrayfish.guns.client.util;

import com.mrcrayfish.framework.api.serialize.DataArray;
import com.mrcrayfish.framework.api.serialize.DataNumber;
import com.mrcrayfish.framework.api.serialize.DataObject;
import com.mrcrayfish.framework.api.serialize.DataType;
import com.mrcrayfish.guns.cache.ObjectCache;
import com.mrcrayfish.guns.item.attachment.IAttachment;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Helper class for more complex gun animations, including attachment and hand movements
 */
public final class GunReloadAnimationHelper
{
    public static final String WEAPON_KEY = "cgm:weapon";
    public static final String CACHE_KEY = "properties";

    public static void resetCache()
    {
        ObjectCache.getInstance(CACHE_KEY).reset();
    }
    
    
    
	/* 3D Vector builders for reload animations */
    
	public static Vec3 getAnimationTrans(ItemStack weapon, float progress, String type)
	{
		Vec3 blendedFrame = Vec3.ZERO;
		int animationFrames = getReloadFrames(weapon);
		float scaledProgress = progress*(animationFrames);
		int currentFrame = (int) Math.floor(scaledProgress);
		
		Vec3 priorFrame = getReloadAnimTrans(weapon, type, currentFrame);
		Vec3 nextFrame = getReloadAnimTrans(weapon, type, currentFrame+1);
		blendedFrame = priorFrame.lerp(nextFrame, scaledProgress-currentFrame);
		
		return blendedFrame;
	}
    
	public static Vec3 getAnimationRot(ItemStack weapon, float progress, String type)
	{
		Vec3 blendedFrame = Vec3.ZERO;
		int animationFrames = getReloadFrames(weapon);
		float scaledProgress = progress*(animationFrames);
		int currentFrame = (int) Math.floor(scaledProgress);
		
		Vec3 priorFrame = getReloadAnimRot(weapon, type, currentFrame);
		Vec3 nextFrame = getReloadAnimRot(weapon, type, currentFrame+1);
		blendedFrame = priorFrame.lerp(nextFrame, scaledProgress-currentFrame);
		
		return blendedFrame;
	}
    
	public static int getCurrentFrame(ItemStack weapon, float progress, String type)
	{
		int animationFrames = getReloadFrames(weapon);
		float scaledProgress = progress*(animationFrames-1);
		return (int) Math.floor(scaledProgress);
	}
	
	
	
	
	
	/* Property Helpers for Reload Animations */
	// General
	public static boolean hasCustomReloadAnimation(ItemStack weapon) {
		DataObject animObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "reloadAnimation");
		if (animObject.has("frames", DataType.NUMBER))
		return true;
		
		return false;
	}
	
	private static int getReloadFrames(ItemStack weapon) {
		DataObject animObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "reloadAnimation");
		if (animObject.has("frames", DataType.NUMBER))
		{
			DataNumber frames = animObject.getDataNumber("frames");
			if (frames!=null)
            return frames.asInt();
		}
		
		return 1;
	}
	
	// Base View Model
	@SuppressWarnings("unused")
	private static Vec3 getReloadViewModelBaseTrans(ItemStack weapon) {
		DataObject frameObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "reloadAnimation", "viewModel");
		if (frameObject.has("translation", DataType.ARRAY))
		{
			DataArray translationArray = frameObject.getDataArray("translation");
			if (translationArray!=null)
			{
				Vec3 translations = PropertyHelper.arrayToVec3(translationArray, Vec3.ZERO);
				return translations;
			}
		}
		
		return Vec3.ZERO;
	}
	@SuppressWarnings("unused")
	private static Vec3 getReloadViewModelBaseRot(ItemStack weapon) {
		DataObject frameObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "reloadAnimation", "viewModel");
		if (frameObject.has("rotation", DataType.ARRAY))
		{
			DataArray rotationArray = frameObject.getDataArray("rotation");
			if (rotationArray!=null)
            return PropertyHelper.arrayToVec3(rotationArray, Vec3.ZERO);
		}
		
		return Vec3.ZERO;
	}
	
	// Frames
	private static Vec3 getReloadAnimTrans(ItemStack weapon, String type, int frame) {
		DataObject frameObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "reloadAnimation", type, "frame"+frame);
		if (frameObject.has("translation", DataType.ARRAY))
		{
			DataArray translationArray = frameObject.getDataArray("translation");
			if (translationArray!=null)
			{
				Vec3 translations = PropertyHelper.arrayToVec3(translationArray, Vec3.ZERO);
				if(type.equals("forwardHand") || type.equals("rearHand"))
				translations.scale(PropertyHelper.getHandPosScalar(weapon));
				return translations;
			}
		}
		
		return Vec3.ZERO;
	}
	private static Vec3 getReloadAnimRot(ItemStack weapon, String type, int frame) {
		DataObject frameObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "reloadAnimation", type, "frame"+frame);
		if (frameObject.has("rotation", DataType.ARRAY))
		{
			DataArray rotationArray = frameObject.getDataArray("rotation");
			if (rotationArray!=null)
            return PropertyHelper.arrayToVec3(rotationArray, Vec3.ZERO);
		}
		
		return Vec3.ZERO;
	}
}