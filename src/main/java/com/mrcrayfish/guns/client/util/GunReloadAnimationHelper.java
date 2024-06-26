package com.mrcrayfish.guns.client.util;

import com.mrcrayfish.framework.api.serialize.DataObject;
import com.mrcrayfish.framework.api.serialize.DataString;
import com.mrcrayfish.framework.api.serialize.DataType;
import com.mrcrayfish.guns.cache.ObjectCache;
import com.mrcrayfish.guns.client.handler.GunRenderingHandler;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Helper class for reload animations using the Common Animation System.
 */
public final class GunReloadAnimationHelper
{
    public static final String ANIMATION_KEY = "cgm:animations";
    
    
    
    /* Property Helpers for Reload Animations */
	// Easings
	public static Easings getReloadStartEasing(ResourceLocation weapKey, String type) {
		DataObject frameObject = GunAnimationHelper.getObjectByPath(weapKey, ANIMATION_KEY, "reload", type);
		if (frameObject.has("startEasing", DataType.STRING))
		{
			DataString easing = frameObject.getDataString("startEasing");
			if (easing!=null)
				return (Easings.byName(easing.asString()));
		}
		else
		frameObject = GunAnimationHelper.getObjectByPath(weapKey, ANIMATION_KEY, "reload", type, "0");
		if (frameObject.has("easing", DataType.STRING))
		{
			DataString easing = frameObject.getDataString("easing");
			if (easing!=null)
				return (Easings.byName(easing.asString()));
		}
		
		return Easings.LINEAR;
	}
	public static Easings getReloadStartEasing(ResourceLocation weapKey, String type, boolean easeRotationInstead) {
		DataObject frameObject = GunAnimationHelper.getObjectByPath(weapKey, ANIMATION_KEY, "reload", type);
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
		frameObject = GunAnimationHelper.getObjectByPath(weapKey, ANIMATION_KEY, "reload", type, "0");
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
	
	public static Easings getReloadEndEasing(ResourceLocation weapKey, String type) {
		DataObject frameObject = GunAnimationHelper.getObjectByPath(weapKey, ANIMATION_KEY, "reload", type);
		if (frameObject!=null && frameObject.has("endEasing", DataType.STRING))
		{
			DataString easing = frameObject.getDataString("endEasing");
			if (easing!=null)
				return (Easings.byName(easing.asString()));
		}
		
		return Easings.EASE_OUT_QUAD;
	}
	public static Easings getReloadEndEasing(ResourceLocation weapKey, String type, boolean easeRotationInstead) {
		DataObject frameObject = GunAnimationHelper.getObjectByPath(weapKey, ANIMATION_KEY, "reload", type);
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
}