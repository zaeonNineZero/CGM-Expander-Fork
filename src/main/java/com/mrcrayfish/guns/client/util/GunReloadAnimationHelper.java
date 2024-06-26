package com.mrcrayfish.guns.client.util;

import com.mrcrayfish.framework.api.serialize.DataArray;
import com.mrcrayfish.framework.api.serialize.DataNumber;
import com.mrcrayfish.framework.api.serialize.DataObject;
import com.mrcrayfish.framework.api.serialize.DataString;
import com.mrcrayfish.framework.api.serialize.DataType;
import com.mrcrayfish.guns.cache.ObjectCache;
import com.mrcrayfish.guns.client.handler.GunRenderingHandler;
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
		float delta = GunRenderingHandler.get().getReloadDeltaTime(weapon);
		float scaledProgress = getScaledProgress(weapon, progress);
		int currentFrame = getCurrentFrame(weapon, scaledProgress);
		int priorFrame = findPriorFrame(weapon, type, currentFrame, "translation");
		int nextFrame = findNextFrame(weapon, type, currentFrame+1, "translation");
		int frameDiv = Math.max(Math.abs(nextFrame-priorFrame),1);
		float frameProgress = Math.max(scaledProgress - ((float) priorFrame), 0);
		
		if (priorFrame==0 && delta>0.9F)
		priorFrame = getReloadFrames(weapon);
		
		Vec3 priorTransforms = getReloadAnimTrans(weapon, type, priorFrame);
		Vec3 nextTransforms = getReloadAnimTrans(weapon, type, nextFrame);
		Easings easing = getReloadAnimEasing(weapon, type, findPriorFrame(weapon, type, nextFrame, "translation"), false);
		double easeFactor = getEaseFactor(easing, frameProgress/frameDiv);
		blendedTransforms = priorTransforms.lerp(nextTransforms, Mth.clamp(easeFactor, 0F,1F));
		
		return blendedTransforms;
	}
    
	public static Vec3 getAnimationRot(ItemStack weapon, float progress, String type)
	{
		Vec3 blendedTransforms = Vec3.ZERO;
		float delta = GunRenderingHandler.get().getReloadDeltaTime(weapon);
		float scaledProgress = getScaledProgress(weapon, progress);
		int currentFrame = getCurrentFrame(weapon, scaledProgress);
		int priorFrame = findPriorFrame(weapon, type, currentFrame, "rotation");
		int nextFrame = findNextFrame(weapon, type, currentFrame+1, "rotation");
		int frameDiv = Math.max(Math.abs(nextFrame-priorFrame),1);
		float frameProgress = Math.max(scaledProgress - ((float) priorFrame), 0);
		
		if (priorFrame==0 && delta>0.9F)
		priorFrame = getReloadFrames(weapon);
		
		Vec3 priorTransforms = getReloadAnimRot(weapon, type, priorFrame);
		Vec3 nextTransforms = getReloadAnimRot(weapon, type, nextFrame);
		Easings easing = getReloadAnimEasing(weapon, type, findPriorFrame(weapon, type, nextFrame, "rotation"), true);
		double easeFactor = getEaseFactor(easing, frameProgress/frameDiv);
		blendedTransforms = priorTransforms.lerp(nextTransforms, Mth.clamp(easeFactor, 0F,1F));
		
		return blendedTransforms;
	}

	
    
	/* Reload animation calculators for more advanced control */
    // Frames
	public static float getScaledProgress(ItemStack weapon, float progress)
	{
		int animationFrames = getReloadFrames(weapon);
		return Mth.clamp(progress*(animationFrames)-0.05F, 0,animationFrames);
		//return Mth.clamp(progress*(animationFrames)+0.14F, 0,animationFrames);
	}
	
	public static int getCurrentFrame(ItemStack weapon, float scaledProgress)
	{
		return (int) Math.floor(scaledProgress);
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
		if (frameObject.has("startEasing", DataType.STRING))
		{
			DataString easing = frameObject.getDataString("startEasing");
			if (easing!=null)
				return (Easings.byName(easing.asString()));
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
		if (frameObject!=null && frameObject.has("startEasing", DataType.STRING))
		{
			DataString easing = frameObject.getDataString("startEasing");
			if (easing!=null)
				return (Easings.byName(easing.asString()));
		}
		else
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
		else
		frameObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "reloadAnimation", type, "0");
		if (frameObject!=null && frameObject.has("easing", DataType.STRING))
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
		if (frameObject!=null && frameObject.has("endEasing", DataType.STRING))
		{
			DataString easing = frameObject.getDataString("endEasing");
			if (easing!=null)
				return (Easings.byName(easing.asString()));
		}
		
		return Easings.EASE_OUT_QUAD;
	}
	public static Easings getReloadEndEasing(ItemStack weapon, String type, boolean easeRotationInstead) {
		DataObject frameObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "reloadAnimation", type);
		if (frameObject!=null && frameObject.has("endEasing", DataType.STRING))
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
	private static int findPriorFrame(ItemStack weapon, String type, int frame, String transform) {
		int returnFrame=-1;
		for (int i=frame; returnFrame==-1 && i>=0; i--)
		{
			DataObject frameObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "reloadAnimation", type, ""+i);
			if (frameObject!=null)
			{
				if (frameObject.has(transform, DataType.ARRAY))
					returnFrame = i;
				//return frame;
				/*else
				if (transform.equals("easing") && frameObject.has(transform, DataType.STRING))
					return frame;
				else
				if (transform.equals("transEasing") || transform.equals("rotEasing"))
				{
					if (frameObject.has(transform, DataType.STRING))
						return frame;
					else
					if (frameObject.has("easing", DataType.STRING))
						return frame;
				}*/
			}
		}
		
		if (returnFrame!=-1)
		return returnFrame;
		else
		return 0;
	}
	
	private static int findNextFrame(ItemStack weapon, String type, int frame, String transform) {
		int returnFrame=-1;
		for (int i=frame; returnFrame==-1 && i<=getReloadFrames(weapon); i++)
		{
			DataObject frameObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "reloadAnimation", type, ""+i);
			if (frameObject!=null)
			{
				if (frameObject.has(transform, DataType.ARRAY))
					returnFrame = i;
					//return frame;
				/*else
				if (transform.equals("easing") && frameObject.has(transform, DataType.STRING))
					return frame;
				else
				if (transform.equals("transEasing") || transform.equals("rotEasing"))
				{
					if (frameObject.has(transform, DataType.STRING))
						return frame;
					else
					if (frameObject.has("easing", DataType.STRING))
						return frame;
				}*/
			}
		}
		
		if (returnFrame!=-1)
		return returnFrame;
		else
		return findPriorFrame(weapon, type, frame, transform);
	}
	
	
	// Animation Values
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