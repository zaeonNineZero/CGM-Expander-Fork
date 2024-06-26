package com.mrcrayfish.guns.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mrcrayfish.framework.api.client.FrameworkClientAPI;
import com.mrcrayfish.framework.api.serialize.DataArray;
import com.mrcrayfish.framework.api.serialize.DataNumber;
import com.mrcrayfish.framework.api.serialize.DataObject;
import com.mrcrayfish.framework.api.serialize.DataString;
import com.mrcrayfish.framework.api.serialize.DataType;
import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.cache.ObjectCache;
import com.mrcrayfish.guns.client.AnimationMetaLoader;
import com.mrcrayfish.guns.client.MetaLoader;
import com.mrcrayfish.guns.client.handler.GunRenderingHandler;
import com.mrcrayfish.guns.client.handler.ReloadHandler;
import com.mrcrayfish.guns.item.IMeta;
import com.mrcrayfish.guns.item.attachment.IAttachment;
import com.mrcrayfish.guns.item.attachment.IAttachment.Type;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Helper class for more complex gun animations, including attachment and hand movements.
 * These animations are built around a "Common Animation System", a keyframe-based animation
 * system built specifically for CGM Expanded.
 */
public final class GunAnimationHelper
{
	public static final String ANIMATION_KEY = "cgm:animations";
	static boolean doMetaLoadMessage=false;
	static boolean doHasAnimationMessage=false;
	static boolean doTryingMetaLoadMessage=false;


	
    
	/* Smart animation methods for selecting animations based on given parameters */
    public static String getSmartAnimationType(ItemStack weapon, Player player, float partialTicks)
    {
    	if (ReloadHandler.get().getReloadProgress(partialTicks) > 0) 
    	{
    		float reloadTransitionProgress = ReloadHandler.get().getReloadProgress(partialTicks);
    		if (reloadTransitionProgress>0 && hasAnimation("reload", weapon))
    		{
    			if (reloadTransitionProgress<1)
    			{
    				float delta = GunRenderingHandler.get().getReloadDeltaTime(weapon);
		    		if (hasAnimation("reloadStart", weapon) && ReloadHandler.get().doReloadStartAnimation() && delta <= 0.5)
		    		{
		    			return "reloadStart";
		    		}
		    		else
		        	if (hasAnimation("reloadEnd", weapon) && ReloadHandler.get().doReloadFinishAnimation())
		        	{
		        		return "reloadEnd";
		        	}
	    		}
        	    return "reload";
    		}
    		else
    		if (hasAnimation("fire", weapon))
    		{
    			ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
                float cooldown = tracker.getCooldownPercent(weapon.getItem(), Minecraft.getInstance().getFrameTime());
                if (cooldown>0);
                {
                	return "fire";
                }
    		} 
    	}
    	
    	return "none";
    }
    
    public static Vec3 getSmartAnimationTrans(ItemStack weapon, Player player, float partialTicks, String component)
    {
    	String animType = getSmartAnimationType(weapon, player, partialTicks);
    	if (animType.equals("reloadStart"))
    	{
    		float reloadTransitionProgress = ReloadHandler.get().getReloadProgress(partialTicks);
    		float delta = GunRenderingHandler.get().getReloadDeltaTime(weapon);
    		return getAnimationTrans("reloadStart", weapon, reloadTransitionProgress, component);
    	}
    	if (animType.equals("reloadEnd"))
    	{
    		float reloadTransitionProgress = ReloadHandler.get().getReloadProgress(partialTicks);
    		float delta = GunRenderingHandler.get().getReloadDeltaTime(weapon);
    		return getAnimationTrans("reloadEnd", weapon, 1-reloadTransitionProgress, component);
    		
    	}
    	if (animType.equals("reload"))
    	{
    		float reloadTransitionProgress = ReloadHandler.get().getReloadProgress(partialTicks);
    	    float progress = GunRenderingHandler.get().getReloadCycleProgress(weapon);
    	    Vec3 transforms = getAnimationTrans("reload", weapon, progress, component).scale(reloadTransitionProgress);
    	    
    		Easings easing = GunReloadAnimationHelper.getReloadStartEasing(lookForParentAnimation("reload", getItemLocationKey(weapon)), component);
    		{
    			if (!ReloadHandler.get().getReloading(player))
    			easing = GunReloadAnimationHelper.getReloadEndEasing(lookForParentAnimation("reload", getItemLocationKey(weapon)), component);
    		}
    	    float finalReloadTransition = (float) getEaseFactor(easing, reloadTransitionProgress);
    	    return transforms.scale(finalReloadTransition);
    	}
    	if (animType.equals("fire"))
    	{
    		ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
            float cooldown = tracker.getCooldownPercent(weapon.getItem(), Minecraft.getInstance().getFrameTime());
            if (cooldown>0);
            {
            	float progress = 1-cooldown;
            	return getAnimationTrans("fire", weapon, progress, component);
            }
    	}
    	
    	return Vec3.ZERO;
    }
    public static Vec3 getSmartAnimationRot(ItemStack weapon, Player player, float partialTicks, String component)
    {
    	String animType = getSmartAnimationType(weapon, player, partialTicks);
    	if (animType.equals("reloadStart"))
    	{
    		float reloadTransitionProgress = ReloadHandler.get().getReloadProgress(partialTicks);
    		float delta = GunRenderingHandler.get().getReloadDeltaTime(weapon);
			return getAnimationRot("reloadStart", weapon, reloadTransitionProgress, component);
    	}
    	if (animType.equals("reloadEnd"))
    	{
    		float reloadTransitionProgress = ReloadHandler.get().getReloadProgress(partialTicks);
    		float delta = GunRenderingHandler.get().getReloadDeltaTime(weapon);
    		return getAnimationRot("reloadEnd", weapon, 1-reloadTransitionProgress, component);
    		
    	}
    	if (animType.equals("reload"))
    	{
    		float reloadTransitionProgress = ReloadHandler.get().getReloadProgress(partialTicks);
    	    float progress = GunRenderingHandler.get().getReloadCycleProgress(weapon);
    	    Vec3 transforms = getAnimationRot("reload", weapon, progress, component).scale(reloadTransitionProgress);
    	    
    	    Easings easing = GunReloadAnimationHelper.getReloadStartEasing(lookForParentAnimation("reload", getItemLocationKey(weapon)), component);
    		{
    			if (!ReloadHandler.get().getReloading(player))
    			easing = GunReloadAnimationHelper.getReloadEndEasing(lookForParentAnimation("reload", getItemLocationKey(weapon)), component);
    		}
    	    float finalReloadTransition = (float) getEaseFactor(easing, reloadTransitionProgress);
    	    return transforms.scale(finalReloadTransition);
    	}
    	if (animType.equals("fire"))
    	{
    		ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
            float cooldown = tracker.getCooldownPercent(weapon.getItem(), Minecraft.getInstance().getFrameTime());
            if (cooldown>0);
            {
            	float progress = 1-cooldown;
            	return getAnimationRot("fire", weapon, progress, component);
            }
    	}
    	
    	return Vec3.ZERO;
    }
    
    
    
    
    
	/* 3D Vector builders for keyframe-based animations */
    
	public static Vec3 getAnimationTrans(String animationType, ItemStack weapon, float progress, String component)
	{
		Vec3 blendedTransforms = Vec3.ZERO;
		ResourceLocation weapKey = lookForParentAnimation(animationType, getItemLocationKey(weapon));
		float scaledProgress = getScaledProgress(animationType, weapKey, progress);
		int currentFrame = getCurrentFrame(weapon, scaledProgress);
		int priorFrame = findPriorFrame(animationType, weapKey, component, currentFrame, "translation");
		int nextFrame = findNextFrame(animationType, weapKey, component, currentFrame+1, "translation");
		int frameDiv = Math.max(Math.abs(nextFrame-priorFrame),1);
		float frameProgress = Math.max(scaledProgress - ((float) priorFrame), 0);
		
		if (animationType.equals("reload"))
		{
			float delta = GunRenderingHandler.get().getReloadDeltaTime(weapon);
			if (priorFrame==0 && delta>0.9F)
			priorFrame = GunAnimationHelper.getAnimationFrames("reload", weapKey);
		}
		
		Vec3 priorTransforms = getAnimTranslation(animationType, weapKey, component, priorFrame, weapon);
		Vec3 nextTransforms = getAnimTranslation(animationType, weapKey, component, nextFrame, weapon);
		Easings easing = getAnimEasing(animationType, weapKey, component, findPriorFrame(animationType, weapKey, component, nextFrame, "translation"), false);
		double easeFactor = getEaseFactor(easing, frameProgress/frameDiv);
		blendedTransforms = priorTransforms.lerp(nextTransforms, Mth.clamp(easeFactor, 0F,1F));
		
		return blendedTransforms;
	}
    
	public static Vec3 getAnimationRot(String animationType, ItemStack weapon, float progress, String component)
	{
		Vec3 blendedTransforms = Vec3.ZERO;
		ResourceLocation weapKey = lookForParentAnimation(animationType, getItemLocationKey(weapon));
		float scaledProgress = getScaledProgress(animationType, weapKey, progress);
		int currentFrame = getCurrentFrame(weapon, scaledProgress);
		int priorFrame = findPriorFrame(animationType, weapKey, component, currentFrame, "rotation");
		int nextFrame = findNextFrame(animationType, weapKey, component, currentFrame+1, "rotation");
		int frameDiv = Math.max(Math.abs(nextFrame-priorFrame),1);
		float frameProgress = Math.max(scaledProgress - ((float) priorFrame), 0);
		
		if (animationType.equals("reload"))
		{
			float delta = GunRenderingHandler.get().getReloadDeltaTime(weapon);
			if (priorFrame==0 && delta>0.9F)
			priorFrame = GunAnimationHelper.getAnimationFrames("reload", weapKey);
		}
		
		Vec3 priorTransforms = getAnimRotation(animationType, weapKey, component, priorFrame);
		Vec3 nextTransforms = getAnimRotation(animationType, weapKey, component, nextFrame);
		Easings easing = getAnimEasing(animationType, weapKey, component, findPriorFrame(animationType, weapKey, component, nextFrame, "rotation"), true);
		double easeFactor = getEaseFactor(easing, frameProgress/frameDiv);
		blendedTransforms = priorTransforms.lerp(nextTransforms, Mth.clamp(easeFactor, 0F,1F));
		
		return blendedTransforms;
	}

	
    
	/* Reload animation calculators methods for more advanced control */
    // Frames
	public static float getScaledProgress(String animationType, ResourceLocation weapKey, float progress)
	{
		int animationFrames = getAnimationFrames(animationType, weapKey);
		return Mth.clamp(progress*(animationFrames)-0.05F, 0,animationFrames);
		//return Mth.clamp(progress*(animationFrames)+0.14F, 0,animationFrames);
	}
	
	public static int getCurrentFrame(ItemStack weapon, float scaledProgress)
	{
		return (int) Math.floor(scaledProgress);
	}
	
	//Easings
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

	
    
	/* Reload animation application methods to speed up animation implementation */
    // Rotations
	public static void rotateAroundOffset(PoseStack poseStack, Vec3 rotations, Vec3 offsets)
	{
    	poseStack.translate(-offsets.x, -offsets.y, -offsets.z);
    	poseStack.mulPose(Vector3f.XP.rotationDegrees((float) rotations.x));
    	poseStack.mulPose(Vector3f.YP.rotationDegrees((float) rotations.y));
    	poseStack.mulPose(Vector3f.ZP.rotationDegrees((float) rotations.z));
    	poseStack.translate(offsets.x, offsets.y, offsets.z);
	}
	public static void rotateAroundOffset(PoseStack poseStack, Vec3 rotations, String animationType, ItemStack weapon, String component)
	{
		ResourceLocation weapKey = lookForParentAnimation(animationType, getItemLocationKey(weapon));
		rotateAroundOffset(poseStack, rotations, GunAnimationHelper.getRotationOffsetPoint(animationType, weapKey, component));
	}
	
	
	
	
	
	/* Property Helpers for Reload Animations */
	// General
	public static boolean hasAnimation(String animationType, ResourceLocation weapKey) {
		DataObject animObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType);
		if (animObject.has("frames", DataType.NUMBER))
		{
	        if (doHasAnimationMessage)
	        {
	        	GunMod.LOGGER.info("Animation System: Successfully detected a valid animation!");
	        	doHasAnimationMessage=false;
	    	}
			return true;
		}
		
		return false;
	}
	public static boolean hasAnimation(String animationType, ItemStack weapon) {
		return hasAnimation(animationType, lookForParentAnimation(animationType, getItemLocationKey(weapon)));
	}
	
	static ResourceLocation lookForParentAnimation(String animationType, ResourceLocation weapKey) {
		ResourceLocation output = weapKey;
		
		DataObject animObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType);
		if (animObject.has("parent", DataType.STRING))
		{
			DataString parent = animObject.getDataString("parent");
			output = new ResourceLocation(parent.asString());
		}
		else
		{
			animObject = getObjectByPath(weapKey, ANIMATION_KEY);
			if (animObject.has("parent", DataType.STRING))
			{
				DataString parent = animObject.getDataString("parent");
				output = new ResourceLocation(parent.asString());
			}
		}
		
		
		return output;
	}
	
	static int getAnimationFrames(String animationType, ResourceLocation weapKey) {
		DataObject animObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType);
		if (animObject.has("frames", DataType.NUMBER))
		{
			DataNumber frames = animObject.getDataNumber("frames");
			if (frames!=null)
            return frames.asInt();
		}
		
		return 1;
	}
	
	
	// Rotation Offset Points
	public static Vec3 getRotationOffsetPoint(String animationType, ResourceLocation weapKey, String component) {
		DataObject offsetObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType, component);
		if (offsetObject.has("rotOffset", DataType.ARRAY))
		{
			DataArray offsetArray = offsetObject.getDataArray("rotOffset");
			if (offsetArray!=null)
            return PropertyHelper.arrayToVec3(offsetArray, Vec3.ZERO);
		}
		
		return Vec3.ZERO;
	}
	
	
	// Frames
	static int findPriorFrame(String animationType, ResourceLocation weapKey, String component, int frame, String transform) {
		int returnFrame=-1;
		for (int i=frame; returnFrame==-1 && i>=0; i--)
		{
			DataObject frameObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType, component, ""+i);
			if (frameObject!=null)
			{
				if (frameObject.has(transform, DataType.ARRAY))
					returnFrame = i;
			}
		}
		
		if (returnFrame!=-1)
		return returnFrame;
		else
		return 0;
	}
	
	static int findNextFrame(String animationType, ResourceLocation weapKey, String component, int frame, String transform) {
		int returnFrame=-1;
		for (int i=frame; returnFrame==-1 && i<=getAnimationFrames(animationType, weapKey); i++)
		{
			DataObject frameObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType, component, ""+i);
			if (frameObject!=null)
			{
				if (frameObject.has(transform, DataType.ARRAY))
					returnFrame = i;
			}
		}
		
		if (returnFrame!=-1)
		return returnFrame;
		else
		return findPriorFrame(animationType, weapKey, component, frame, transform);
	}
	
	
	// Animation Values
	static Vec3 getAnimTranslation(String animationType, ResourceLocation weapKey, String component, int frame) {
		DataObject frameObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType, component, ""+frame);
		if (frameObject.has("translation", DataType.ARRAY))
		{
			DataArray translationArray = frameObject.getDataArray("translation");
			if (translationArray!=null)
			{
				Vec3 translations = PropertyHelper.arrayToVec3(translationArray, Vec3.ZERO);
				if(component.equals("forwardHand") || component.equals("rearHand"))
				return translations;
			}
		}
		
		return Vec3.ZERO;
	}
	static Vec3 getAnimTranslation(String animationType, ResourceLocation weapKey, String component, int frame, ItemStack weapon) {
		DataObject frameObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType, component, ""+frame);
		if (frameObject.has("translation", DataType.ARRAY))
		{
			DataArray translationArray = frameObject.getDataArray("translation");
			if (translationArray!=null)
			{
				Vec3 translations = PropertyHelper.arrayToVec3(translationArray, Vec3.ZERO);
				if(component.equals("forwardHand") || component.equals("rearHand"))
				translations.scale(PropertyHelper.getHandPosScalar(weapon));
				return translations;
			}
		}
		
		return Vec3.ZERO;
	}
	static Vec3 getAnimRotation(String animationType, ResourceLocation weapKey, String component, int frame) {
		DataObject frameObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType, component, ""+frame);
		if (frameObject.has("rotation", DataType.ARRAY))
		{
			DataArray rotationArray = frameObject.getDataArray("rotation");
			if (rotationArray!=null)
            return PropertyHelper.arrayToVec3(rotationArray, Vec3.ZERO);
		}
		
		return Vec3.ZERO;
	}
	static Easings getAnimEasing(String animationType, ResourceLocation weapKey, String component, int frame, boolean easeRotationInstead) {
		DataObject frameObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType, component, ""+frame);
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
	
	
	
	// Additional methods to aid with interfacing with the animation system.
	public static ResourceLocation getItemLocationKey(ItemStack stack)
	{
        return stack.getItem().builtInRegistryHolder().key().location();
	}
	public static String getItemString(ItemStack stack)
	{
        ResourceLocation key = stack.getItem().builtInRegistryHolder().key().location();
		return key.getPath();
	}
	public static String getItemNamespaceString(ItemStack stack)
	{
        ResourceLocation key = stack.getItem().builtInRegistryHolder().key().location();
		return key.getNamespace();
	}
	public static String[] getItemStringArray(ItemStack stack)
	{
		String[] strings = {getItemNamespaceString(stack), getItemString(stack)};
		return strings;
	}
	
	
	
	// Copies of methods from PropertyHelper, reworked to support animations.
	static DataObject getObjectByPath(ResourceLocation locationKey, String ... path)
    {
		DataObject result = getCustomData(locationKey);
        if (!result.isEmpty() && doMetaLoadMessage)
        {
        	GunMod.LOGGER.info("Animation System: Successfully retrieved a data object from animation meta loader!");
        	doMetaLoadMessage=false;
    	}
        for(String key : path)
        {
            if(result.has(key, DataType.OBJECT))
            {
                result = result.getDataObject(key);
                continue;
            }
            return DataObject.EMPTY;
        }
        return result;
    }
    private static DataObject getCustomData(ResourceLocation location)
    {
        if (doTryingMetaLoadMessage)
        {
        	GunMod.LOGGER.info("Animation System: Attempting to load animation data with resource key: " + location);
        	doTryingMetaLoadMessage=false;
    	}
        return AnimationMetaLoader.getInstance().getData(location);
    }
}