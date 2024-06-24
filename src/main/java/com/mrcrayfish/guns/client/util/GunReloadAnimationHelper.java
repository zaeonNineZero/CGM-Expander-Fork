package com.mrcrayfish.guns.client.util;

import com.mrcrayfish.framework.api.serialize.DataArray;
import com.mrcrayfish.framework.api.serialize.DataNumber;
import com.mrcrayfish.framework.api.serialize.DataObject;
import com.mrcrayfish.framework.api.serialize.DataString;
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
		Vec3 blendedTransforms = Vec3.ZERO;
		int animationFrames = getReloadFrames(weapon);
		float scaledProgress = Mth.clamp(progress*(animationFrames)+0.12F, 0,animationFrames);
		int currentFrame = getCurrentFrame(weapon, progress);
		int nextFrame = (int) Mth.clamp(currentFrame+1, 0,animationFrames);
		
		Vec3 priorTransforms = getReloadAnimTrans(weapon, type, currentFrame);
		Vec3 nextTransforms = getReloadAnimTrans(weapon, type, nextFrame);
		Easings easing = getReloadAnimEasing(weapon, type, nextFrame, false);
		double easeFactor = getEaseFactor(easing, scaledProgress % 1);
		blendedTransforms = priorTransforms.lerp(nextTransforms, Math.min(easeFactor,1));
		
		return blendedTransforms;
	}
    
	public static Vec3 getAnimationRot(ItemStack weapon, float progress, String type)
	{
		Vec3 blendedTransforms = Vec3.ZERO;
		int animationFrames = getReloadFrames(weapon);
		float scaledProgress = Mth.clamp(progress*(animationFrames)+0.12F, 0,animationFrames);
		int currentFrame = (int) Math.min(Math.floor(scaledProgress), animationFrames);
		int nextFrame = (int) Mth.clamp(currentFrame+1, 0,animationFrames);
		
		Vec3 priorTransforms = getReloadAnimRot(weapon, type, Mth.clamp(currentFrame, 0,animationFrames));
		Vec3 nextTransforms = getReloadAnimRot(weapon, type, Mth.clamp(nextFrame, 0,animationFrames));
		Easings easing = getReloadAnimEasing(weapon, type, nextFrame, true);
		double easeFactor = getEaseFactor(easing, scaledProgress % 1);
		blendedTransforms = priorTransforms.lerp(nextTransforms, Math.min(easeFactor,1));
		
		return blendedTransforms;
	}

	
    
	/* Reload animation calculators for more advanced control */
    // Frames
	public static int getCurrentFrame(ItemStack weapon, float progress)
	{
		int animationFrames = getReloadFrames(weapon);
		float scaledProgress = Mth.clamp(progress*(animationFrames)+0.12F, 0,animationFrames);
		return (int) Math.min(Math.floor(scaledProgress), animationFrames);
	}
	
	public static int getNextFrame(ItemStack weapon, float progress)
	{
		int animationFrames = getReloadFrames(weapon);
		int currentFrame = getCurrentFrame(weapon, progress);
		return (int) Mth.clamp(currentFrame+1, 0,animationFrames);
	}
	public static int getNextFrame(ItemStack weapon, int currentFrame)
	{
		int animationFrames = getReloadFrames(weapon);
		return (int) Mth.clamp(currentFrame+1, 0,animationFrames);
	}
	
	//Easing
	public static double getEaseFactor(Easings easing, float progress)
	{
		double easeFactor = Mth.clamp(easing.apply(progress),0,1);
		
		return Mth.clamp(easeFactor,0,1);
	}
	public static double getReversedEaseFactor(Easings easing, float progress)
	{
		double easeFactor = Mth.clamp(1 - easing.apply(1 - progress),0,1);
		
		return Mth.clamp(easeFactor,0,1);
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
	public static Vec3 getReloadViewModelBaseTrans(ItemStack weapon) {
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
	public static Vec3 getReloadViewModelBaseRot(ItemStack weapon) {
		DataObject frameObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "reloadAnimation", "viewModel");
		if (frameObject.has("rotation", DataType.ARRAY))
		{
			DataArray rotationArray = frameObject.getDataArray("rotation");
			if (rotationArray!=null)
            return PropertyHelper.arrayToVec3(rotationArray, Vec3.ZERO);
		}
		
		return Vec3.ZERO;
	}
	
	// Objects
	public static Easings getReloadStartEasing(ItemStack weapon, String type) {
		DataObject frameObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "reloadAnimation", type);
		if (frameObject!=null)
		{
			if (frameObject.has("startEasing", DataType.STRING))
			{
				DataString easing = frameObject.getDataString("startEasing");
				if (easing!=null)
					return (Easings.byName(easing.asString()));
			}
		}
		else
		frameObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "reloadAnimation", type, "0");
		if (frameObject.has("easing", DataType.STRING))
		{
			DataString easing = frameObject.getDataString("easing");
			if (easing!=null)
				return (Easings.byName(easing.asString()));
		}
		
		return Easings.LINEAR;
	}
	public static Easings getReloadStartEasing(ItemStack weapon, String type, boolean easeRotationInstead) {
		DataObject frameObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "reloadAnimation", type);
		if (frameObject!=null)
		{
			if (frameObject.has("startEasing", DataType.STRING))
			{
				DataString easing = frameObject.getDataString("startEasing");
				if (easing!=null)
					return (Easings.byName(easing.asString()));
			}
			else
			{
				if (frameObject.has("startTransEasing", DataType.STRING) && !easeRotationInstead)
				{
					DataString easing = frameObject.getDataString("startTransEasing");
					if (easing!=null)
						return (Easings.byName(easing.asString()));
				}
				else
				if (frameObject.has("startRotEasing", DataType.STRING) && easeRotationInstead)
				{
					DataString easing = frameObject.getDataString("startRotEasing");
					if (easing!=null)
						return (Easings.byName(easing.asString()));
				}
			}
		}
		else
		frameObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "reloadAnimation", type, "0");
		if (frameObject.has("easing", DataType.STRING))
		{
			DataString easing = frameObject.getDataString("easing");
			if (easing!=null)
				return (Easings.byName(easing.asString()));
		}
		else
		{
			if (frameObject.has("transEasing", DataType.STRING) && !easeRotationInstead)
			{
				DataString easing = frameObject.getDataString("transEasing");
				if (easing!=null)
					return (Easings.byName(easing.asString()));
			}
			else
			if (frameObject.has("rotEasing", DataType.STRING) && easeRotationInstead)
			{
				DataString easing = frameObject.getDataString("rotEasing");
				if (easing!=null)
					return (Easings.byName(easing.asString()));
			}
		}
		
		return Easings.LINEAR;
	}
	
	public static Easings getReloadEndEasing(ItemStack weapon, String type) {
		DataObject frameObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "reloadAnimation", type);
		if (frameObject.has("endEasing", DataType.STRING))
		{
			DataString easing = frameObject.getDataString("endEasing");
			if (easing!=null)
				return (Easings.byName(easing.asString()));
		}
		
		return Easings.EASE_OUT_QUAD;
	}
	public static Easings getReloadEndEasing(ItemStack weapon, String type, boolean easeRotationInstead) {
		DataObject frameObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "reloadAnimation", type);
		if (frameObject.has("endEasing", DataType.STRING))
		{
			DataString easing = frameObject.getDataString("endEasing");
			if (easing!=null)
				return (Easings.byName(easing.asString()));
		}
		else
		{
			if (frameObject.has("endTransEasing", DataType.STRING) && !easeRotationInstead)
			{
				DataString easing = frameObject.getDataString("endTransEasing");
				if (easing!=null)
					return (Easings.byName(easing.asString()));
			}
			else
			if (frameObject.has("endRotEasing", DataType.STRING) && easeRotationInstead)
			{
				DataString easing = frameObject.getDataString("endRotEasing");
				if (easing!=null)
					return (Easings.byName(easing.asString()));
			}
		}
		
		return Easings.EASE_OUT_QUAD;
	}
	
	// Frames
	private static Vec3 getReloadAnimTrans(ItemStack weapon, String type, int frame) {
		DataObject frameObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "reloadAnimation", type, ""+frame);
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
		DataObject frameObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "reloadAnimation", type, ""+frame);
		if (frameObject.has("rotation", DataType.ARRAY))
		{
			DataArray rotationArray = frameObject.getDataArray("rotation");
			if (rotationArray!=null)
            return PropertyHelper.arrayToVec3(rotationArray, Vec3.ZERO);
		}
		
		return Vec3.ZERO;
	}
	private static Easings getReloadAnimEasing(ItemStack weapon, String type, int frame, boolean easeRotationInstead) {
		DataObject frameObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "reloadAnimation", type, ""+frame);
		if (frameObject.has("easing", DataType.STRING))
		{
			DataString easing = frameObject.getDataString("easing");
			if (easing!=null)
				return (Easings.byName(easing.asString()));
		}
		else
		{
			if (frameObject.has("transEasing", DataType.STRING) && !easeRotationInstead)
			{
				DataString easing = frameObject.getDataString("transEasing");
				if (easing!=null)
					return (Easings.byName(easing.asString()));
			}
			else
			if (frameObject.has("rotEasing", DataType.STRING) && easeRotationInstead)
			{
				DataString easing = frameObject.getDataString("rotEasing");
				if (easing!=null)
					return (Easings.byName(easing.asString()));
			}
		}
		
		return Easings.LINEAR;
	}
}