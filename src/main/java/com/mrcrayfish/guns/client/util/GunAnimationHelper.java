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
import com.mrcrayfish.guns.item.GunItem;
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
	static boolean doParentMessage1=true;
	static boolean doParentMessage2=true;


    
	/* Smart animation methods for selecting animations based on given parameters */
    public static String getSmartAnimationType(ItemStack weapon, Player player, float partialTicks)
    {
    	double reloadTransitionProgress = ReloadHandler.get().getReloadProgress(partialTicks);
    	if (reloadTransitionProgress>0.0 && hasAnimation("reload", weapon))
		{
			if (reloadTransitionProgress<1.0)
			{
				float delta = GunRenderingHandler.get().getReloadDeltaTime(weapon);
	    		if (hasAnimation("reloadStart", weapon) && ReloadHandler.get().getReloading(player) && delta < 0.4F && ReloadHandler.get().doReloadStartAnimation())
	    		{
	    			return "reloadStart";
	    		}
	    		else
	        	if (hasAnimation("reloadEnd", weapon) && !ReloadHandler.get().getReloading(player) && ReloadHandler.get().doReloadFinishAnimation())
	        	{
	        		return "reloadEnd";
	        	}
	        	else
	        	return "reload";
    		}
			else
    	    return "reload";
		}
		else
		{
			ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
            float cooldown = tracker.getCooldownPercent(weapon.getItem(), Minecraft.getInstance().getFrameTime());
            if (cooldown>0)
            {
            	return "fire";
            }
		}
    	
    	return "none";
    }
    
    public static Vec3 getSmartAnimationTrans(ItemStack weapon, Player player, float partialTicks, String component)
    {
    	String animType = getSmartAnimationType(weapon, player, partialTicks);
    	ResourceLocation weapKey = lookForParentAnimation(animType, getItemLocationKey(weapon));
    	if (animType.equals("reloadStart"))
    	{
    		float reloadTransitionProgress = ReloadHandler.get().getReloadProgress(partialTicks);
    		return getAnimationTrans("reloadStart", weapon, reloadTransitionProgress, component);
    	}
    	if (animType.equals("reloadEnd"))
    	{
    		float reloadTransitionProgress = ReloadHandler.get().getReloadProgress(partialTicks);
    		return getAnimationTrans("reloadEnd", weapon, 1-reloadTransitionProgress, component);
    	}
    	if (animType.equals("reload") && hasAnimation("reload", weapon))
    	{
    		float reloadTransitionProgress = ReloadHandler.get().getReloadProgress(partialTicks);
    	    float progress = ((GunItem) (weapon.getItem())).getModifiedGun(weapon).getGeneral().usesMagReload() ? GunRenderingHandler.get().getReloadDeltaTime(weapon) : GunRenderingHandler.get().getReloadCycleProgress(weapon);
    	    Vec3 transforms = getAnimationTrans("reload", weapon, progress, component).scale(reloadTransitionProgress);
    	    
    	    Easings easing = GunReloadAnimationHelper.getReloadStartEasing(weapKey, component);
    	    float finalReloadTransition = (float) getEaseFactor(easing, reloadTransitionProgress);
    		if (!ReloadHandler.get().getReloading(player))
    		{
    			easing = GunReloadAnimationHelper.getReloadEndEasing(weapKey, component);
        	    finalReloadTransition = (float) getReversedEaseFactor(easing, reloadTransitionProgress);
    		}
    	    return transforms.scale(finalReloadTransition);
    	}
    	if (animType.equals("fire") && hasAnimation("fire", weapon))
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
    	ResourceLocation weapKey = lookForParentAnimation(animType, getItemLocationKey(weapon));
    	if (animType.equals("reloadStart"))
    	{
    		float reloadTransitionProgress = ReloadHandler.get().getReloadProgress(partialTicks);
			return getAnimationRot("reloadStart", weapon, reloadTransitionProgress, component);
    	}
    	if (animType.equals("reloadEnd"))
    	{
    		float reloadTransitionProgress = ReloadHandler.get().getReloadProgress(partialTicks);
    		return getAnimationRot("reloadEnd", weapon, 1-reloadTransitionProgress, component);
    		
    	}
    	if (animType.equals("reload") && hasAnimation("reload", weapon))
    	{
    		float reloadTransitionProgress = ReloadHandler.get().getReloadProgress(partialTicks);
    		float progress = ((GunItem) (weapon.getItem())).getModifiedGun(weapon).getGeneral().usesMagReload() ? GunRenderingHandler.get().getReloadDeltaTime(weapon) : GunRenderingHandler.get().getReloadCycleProgress(weapon);
    	    Vec3 transforms = getAnimationRot("reload", weapon, progress, component);
    	    
    	    Easings easing = GunReloadAnimationHelper.getReloadStartEasing(weapKey, component);
    	    float finalReloadTransition = (float) getEaseFactor(easing, reloadTransitionProgress);
    		if (!ReloadHandler.get().getReloading(player))
    		{
    			easing = GunReloadAnimationHelper.getReloadEndEasing(weapKey, component);
        	    finalReloadTransition = (float) getReversedEaseFactor(easing, reloadTransitionProgress);
    		}
    	    return transforms.scale(finalReloadTransition);
    	}
    	if (animType.equals("fire") && hasAnimation("fire", weapon))
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
    public static Vec3 getSmartAnimationRotOffset(ItemStack weapon, Player player, float partialTicks, String component)
    {
    	String animType = getSmartAnimationType(weapon, player, partialTicks);
    	return getRotationOffsetPoint(animType, lookForParentAnimation(animType, getItemLocationKey(weapon)), component);
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
		
		String priorAnimType = animationType;
		String nextAnimType = animationType;
		
		if (animationType.equals("reloadStart"))
		{
			if (nextFrame>=GunAnimationHelper.getAnimationFrames(animationType, weapKey))
			{
				nextFrame = 0;
				nextAnimType = "reload";
			}
			if (priorFrame>=GunAnimationHelper.getAnimationFrames(animationType, weapKey))
			{
				priorFrame = 0;
				priorAnimType = "reload";
			}
		}
		if (animationType.equals("reloadEnd"))
		{
			if (priorFrame<1)
			{
				priorFrame = GunAnimationHelper.getAnimationFrames("reload", weapKey);
				priorAnimType = "reload";
			}
			if (nextFrame<1)
			{
				nextFrame = GunAnimationHelper.getAnimationFrames("reload", weapKey);
				nextAnimType = "reload";
			}
		}
		if (animationType.equals("reload"))
		{
			float delta = GunRenderingHandler.get().getReloadDeltaTime(weapon);
			if (priorFrame==0 && delta>0.8F)
			priorFrame = GunAnimationHelper.getAnimationFrames("reload", weapKey);
		}
		
		Vec3 priorTransforms = getAnimTranslation(priorAnimType, weapKey, component, priorFrame, weapon);
		Vec3 nextTransforms = getAnimTranslation(nextAnimType, weapKey, component, nextFrame, weapon);
		Easings easing = getAnimEasing(nextAnimType, weapKey, component, findPriorFrame(nextAnimType, weapKey, component, nextFrame, "translation"), false);
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
		
		String priorAnimType = animationType;
		String nextAnimType = animationType;
		
		if (animationType.equals("reloadStart"))
		{
			if (nextFrame>=GunAnimationHelper.getAnimationFrames(animationType, weapKey))
			{
				nextFrame = 0;
				nextAnimType = "reload";
			}
			if (priorFrame>=GunAnimationHelper.getAnimationFrames(animationType, weapKey))
			{
				priorFrame = 0;
				priorAnimType = "reload";
			}
		}
		if (animationType.equals("reloadEnd"))
		{
			if (priorFrame<=0)
			{
				priorFrame = GunAnimationHelper.getAnimationFrames("reload", weapKey);
				priorAnimType = "reload";
			}
			if (nextFrame<=0)
			{
				nextFrame = GunAnimationHelper.getAnimationFrames("reload", weapKey);
				nextAnimType = "reload";
			}
		}
		if (animationType.equals("reload"))
		{
			float delta = GunRenderingHandler.get().getReloadDeltaTime(weapon);
			if (priorFrame==0 && nextFrame<GunAnimationHelper.getAnimationFrames(animationType, weapKey) && delta>0.8F)
			priorFrame = GunAnimationHelper.getAnimationFrames("reload", weapKey);
		}
		
		Vec3 priorTransforms = getAnimRotation(priorAnimType, weapKey, component, priorFrame);
		Vec3 nextTransforms = getAnimRotation(nextAnimType, weapKey, component, nextFrame);
		Easings easing = getAnimEasing(nextAnimType, weapKey, component, findPriorFrame(nextAnimType, weapKey, component, nextFrame, "rotation"), true);
		double easeFactor = getEaseFactor(easing, frameProgress/frameDiv);
		blendedTransforms = priorTransforms.lerp(nextTransforms, Mth.clamp(easeFactor, 0F,1F));
		
		return blendedTransforms;
	}

	
    
	/* Reload animation calculators methods for more advanced control */
    // Frames
	public static float getScaledProgress(String animationType, ResourceLocation weapKey, float progress)
	{
		int animationFrames = getAnimationFrames(animationType, weapKey);
		float offset = getAnimationValueFloat(animationType, weapKey, "progressOffset");
		float progress1 = Mth.clamp((progress*animationFrames)+offset, 0,animationFrames);
		float progress2 = Mth.clamp((progress*animationFrames)/1.1F, 0,animationFrames);
		float progress3 = Mth.clamp((progress*animationFrames)*1.1F, 0,animationFrames);
		
		return Mth.clamp(progress1, progress2, progress3);
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
    	double scaleFactor = 0.0625;
		poseStack.translate(-offsets.x * scaleFactor, offsets.y * scaleFactor, offsets.z * scaleFactor);
    	poseStack.mulPose(Vector3f.YP.rotationDegrees((float) rotations.y));
    	poseStack.mulPose(Vector3f.XP.rotationDegrees((float) rotations.x));
    	poseStack.mulPose(Vector3f.ZP.rotationDegrees((float) rotations.z));
    	poseStack.translate(offsets.x * scaleFactor, -offsets.y * scaleFactor, -offsets.z * scaleFactor);
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
		DataObject animObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType);
		if (animObject.has("parent", DataType.STRING))
		{
			DataString parent = animObject.getDataString("parent");
			String[] splitString = parent.asString().split(":");
			if (doParentMessage1)
			{
				GunMod.LOGGER.info("Animation System (1): Successfully detected the parent object of " + weapKey + ". Parent object is " + new ResourceLocation(splitString[0],splitString[1]).toString());
				doParentMessage1 = false;
			}
			return new ResourceLocation(splitString[0],splitString[1]);
		}
		else
		{
			animObject = getObjectByPath(weapKey, ANIMATION_KEY);
			if (animObject.has("parent", DataType.STRING))
			{
				DataString parent = animObject.getDataString("parent");
				String[] splitString = parent.asString().split(":");
				if (doParentMessage2)
				{
					GunMod.LOGGER.info("Animation System (2): Successfully detected the parent object of " + weapKey + ". Parent object is " + new ResourceLocation(splitString[0],splitString[1]).toString());
					doParentMessage2 = false;
				}
				return new ResourceLocation(splitString[0],splitString[1]);
			}
		}
		
		
		return weapKey;
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
	/*public static Vec3 getRotationOffsetPoint(String animationType, ResourceLocation weapKey, String component) {
		DataObject offsetObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType, component);
		if (offsetObject.has("rotOffset", DataType.ARRAY))
		{
			DataArray offsetArray = offsetObject.getDataArray("rotOffset");
			if (offsetArray!=null)
            return PropertyHelper.arrayToVec3(offsetArray, Vec3.ZERO);
		}
		
		return Vec3.ZERO;
	}*/
	
	
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
	static Vec3 getAnimationArray(String animationType, ResourceLocation weapKey, String component, int frame, String transform) {
		DataObject transformObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType, component, ""+frame);
		if (transformObject.has(transform, DataType.ARRAY))
		{
			DataArray transformArray = transformObject.getDataArray(transform);
			if (transformArray!=null)
			{
				Vec3 transforms = PropertyHelper.arrayToVec3(transformArray, Vec3.ZERO);
				return transforms;
			}
		}
		
		return Vec3.ZERO;
	}
	
	static double getAnimationValue(String animationType, ResourceLocation weapKey, String transform) {
		DataObject transformObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType);
		if (transformObject.has(transform, DataType.NUMBER))
		{
			DataNumber transformData = transformObject.getDataNumber(transform);
			if (transformData!=null)
			{
	        	//GunMod.LOGGER.info("Animation System: Found" + transform + " number data of " + weapKey);
				return transformData.asDouble();
			}
        	//GunMod.LOGGER.info("Animation System: Found animation object of " + weapKey + " but did not find " + transform + " number data");
			
		}
		
		return 0;
	}
	static float getAnimationValueFloat(String animationType, ResourceLocation weapKey, String transform) {
		return (float) getAnimationValue(animationType, weapKey, transform);
	}
	
	static double getAnimationValue(String animationType, ResourceLocation weapKey, String component, int frame, String transform) {
		DataObject transformObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType, component, ""+frame);
		if (transformObject.has(transform, DataType.NUMBER))
		{
			DataNumber transformData = transformObject.getDataNumber(transform);
			if (transformData!=null)
			return transformData.asDouble();
		}
		
		return 0;
	}
	static float getAnimationValueFloat(String animationType, ResourceLocation weapKey, String component, int frame, String transform) {
		return (float) getAnimationValue(animationType, weapKey, component, frame, transform);
	}
	
	static Vec3 getAnimTranslation(String animationType, ResourceLocation weapKey, String component, int frame) {
		DataObject frameObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType, component, ""+frame);
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