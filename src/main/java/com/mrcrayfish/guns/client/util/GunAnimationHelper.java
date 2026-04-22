package com.mrcrayfish.guns.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mrcrayfish.framework.api.client.FrameworkClientAPI;
import com.mrcrayfish.framework.api.serialize.DataArray;
import com.mrcrayfish.framework.api.serialize.DataBoolean;
import com.mrcrayfish.framework.api.serialize.DataNumber;
import com.mrcrayfish.framework.api.serialize.DataObject;
import com.mrcrayfish.framework.api.serialize.DataString;
import com.mrcrayfish.framework.api.serialize.DataType;
import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.cache.ObjectCache;
import com.mrcrayfish.guns.client.AnimationLoader;
import com.mrcrayfish.guns.client.AnimationMetaLoader;
import com.mrcrayfish.guns.client.MetaLoader;
import com.mrcrayfish.guns.client.handler.GunRenderingHandler;
import com.mrcrayfish.guns.client.handler.ReloadHandler;
import com.mrcrayfish.guns.client.handler.ShootingHandler;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.item.IMeta;
import com.mrcrayfish.guns.item.attachment.IAttachment;
import com.mrcrayfish.guns.item.attachment.IAttachment.Type;
import com.mrcrayfish.guns.util.GunCompositeStatHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Author: zaeonNineZero
 * 
 * Helper class for more complex gun animations, including attachment and hand movements.
 * These animations are built around a custom keyframe-based animation system built
 * specifically for CGM Expanded. This system could someday be expanded upon into a
 * more comprehensive animation system.
 */
public final class GunAnimationHelper
{
	public static final String ANIMATION_KEY = "cgm:animations";
	private static final boolean useLegacyLoader=true;
	static boolean doMetaLoadMessage=true;
	static boolean doHasAnimationMessage=true;
	static boolean doTryingMetaLoadMessage=true;
	static boolean doParentMessage1=true;
	static boolean doParentMessage2=true;


    
	/* Smart animation methods for selecting animations based on given parameters */
    public static String getSmartAnimationType(ItemStack weapon, Player player, float partialTicks)
    {
    	if (!(weapon.getItem() instanceof GunItem))
    	return "none";
    	
    	double reloadTransitionProgress = ReloadHandler.get().getReloadProgress(partialTicks);
    	int weaponSwitchTick = ShootingHandler.get().getWeaponSwitchTick();
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
    		}
    	    return "reload";
		}
		else if (reloadTransitionProgress<=0.0)
		{
			ItemCooldowns tracker = player.getCooldowns();
			ResourceLocation weapKey = lookForParentAnimation("fire", getItemLocationKey(weapon));
            if (getAnimationBoolean("fire", weapKey, "syncToCooldown", true))
            {
                float cooldown = tracker.getCooldownPercent(weapon.getItem(), Minecraft.getInstance().getFrameTime());
            	if (cooldown>0 && weaponSwitchTick==-1)
            		return "fire";
			}
            else
            {
            	float animationSpeed = (float) getAnimationValue("fire", weapKey, "animationSpeed");
            	int lastFiretick = GunRenderingHandler.get().getLastFireTick();
        		float fireProgress = ((player.tickCount-lastFiretick)+partialTicks)*animationSpeed;
        		int totalFrames = Math.max(getAnimationFrames("fire", weapKey),1);
                if (fireProgress<totalFrames && weaponSwitchTick==-1)
                	return "fire";
            }
		}
        if(hasAnimation("draw", weapon) && weaponSwitchTick!=-1 && player.tickCount>weaponSwitchTick && reloadTransitionProgress<=0.0)
        {
        	ResourceLocation weapKey = lookForParentAnimation("draw", getItemLocationKey(weapon));
        	float animationSpeed = (float) getAnimationValue("draw", weapKey, "animationSpeed");
    		float drawProgress = ((player.tickCount-weaponSwitchTick)+partialTicks)*animationSpeed;
    		int totalFrames = Math.max(getAnimationFrames("draw", weapKey),1);
        	
    		if ((drawProgress<totalFrames+1 || player.tickCount<weaponSwitchTick+7) && player.getMainHandItem() == weapon)
        	return "draw";
        }
    	int weaponInspectTick = ShootingHandler.get().getWeaponInspectTick();
        if(hasAnimation("inspect", weapon) && weaponInspectTick!=-1 && player.tickCount>weaponInspectTick && reloadTransitionProgress<=0.0)
        {
        	ResourceLocation weapKey = lookForParentAnimation("inspect", getItemLocationKey(weapon));
        	float animationSpeed = (float) getAnimationValue("inspect", weapKey, "animationSpeed");
    		float inspectProgress = ((player.tickCount-weaponInspectTick)+partialTicks)*animationSpeed;
    		int totalFrames = Math.max(getAnimationFrames("inspect", weapKey),1);
        	
    		if (inspectProgress<totalFrames+1 && player.getMainHandItem() == weapon)
        	return "inspect";
        }
    	
    	return "none";
    }
    
    
    public static Vec3 getSmartAnimationTrans(ItemStack weapon, Player player, float partialTicks, String component)
    {
    	String animType = getSmartAnimationType(weapon, player, partialTicks);
    	return getSpecificAnimationTrans(animType, weapon, player, partialTicks, component);
    }
    public static Vec3 getSmartAnimationRot(ItemStack weapon, Player player, float partialTicks, String component)
    {
    	String animType = getSmartAnimationType(weapon, player, partialTicks);
    	return getSpecificAnimationRot(animType, weapon, player, partialTicks, component);
    }
    public static Vec3 getSmartAnimationRotOffset(ItemStack weapon, Player player, float partialTicks, String component)
    {
    	String animType = getSmartAnimationType(weapon, player, partialTicks);
    	return getRotationOffsetPoint(animType, lookForParentAnimation(animType, getItemLocationKey(weapon)), component);
    }
    public static float getSmartAnimationProgress(ItemStack weapon, Player player, float partialTicks, String component)
    {
    	String animType = getSmartAnimationType(weapon, player, partialTicks);
    	return getSpecificAnimationProgress(animType, weapon, player, partialTicks);
    }
    
    
    public static Vec3 getSpecificAnimationTrans(String animType, ItemStack weapon, Player player, float partialTicks, String component)
    {
    	ResourceLocation weapKey = lookForParentAnimation(animType, getItemLocationKey(weapon));
    	float progress = getSpecificAnimationProgress(animType, weapon, player, partialTicks);
    	if (animType.equals("inspect"))
    	{
    		return getAnimationTrans("inspect", weapon, progress, component);
    	}
    	if (animType.equals("draw"))
    	{
    		return getAnimationTrans("draw", weapon, progress, component);
    	}
    	if (animType.equals("reloadStart"))
    	{
    		return getAnimationTrans("reloadStart", weapon, progress, component);
    	}
    	if (animType.equals("reloadEnd"))
    	{
    		return getAnimationTrans("reloadEnd", weapon, progress, component);
    	}
    	if (animType.equals("reload") && hasAnimation("reload", weapon))
    	{
    		float reloadTransitionProgress = ReloadHandler.get().getReloadProgress(partialTicks);
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
    		if (progress>0)
            return getAnimationTrans("fire", weapon, progress, component);
    	}
    	if (animType.equals("recoil") && hasAnimation("recoil", weapon))
    	{
            return getAnimationTrans("recoil", weapon, progress, component);
    	}
    	
    	return Vec3.ZERO;
    }
    public static Vec3 getSpecificAnimationRot(String animType, ItemStack weapon, Player player, float partialTicks, String component)
    {
    	ResourceLocation weapKey = lookForParentAnimation(animType, getItemLocationKey(weapon));
    	float progress = getSpecificAnimationProgress(animType, weapon, player, partialTicks);
    	if (animType.equals("draw"))
    	{
    		return getAnimationRot("draw", weapon, progress, component);
    	}
    	if (animType.equals("inspect"))
    	{
    		return getAnimationRot("inspect", weapon, progress, component);
    	}
    	if (animType.equals("reloadStart"))
    	{
			return getAnimationRot("reloadStart", weapon, progress, component);
    	}
    	if (animType.equals("reloadEnd"))
    	{
    		return getAnimationRot("reloadEnd", weapon, progress, component);
    	}
    	if (animType.equals("reload") && hasAnimation("reload", weapon))
    	{
    		float reloadTransitionProgress = ReloadHandler.get().getReloadProgress(partialTicks);
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
            if (progress>0)
            return getAnimationRot("fire", weapon, progress, component);
    	}
    	if (animType.equals("recoil") && hasAnimation("recoil", weapon))
    	{
            return getAnimationRot("recoil", weapon, progress, component);
    	}
    	
    	return Vec3.ZERO;
    }
    public static Vec3 getSpecificAnimationRotOffset(String animType, ItemStack weapon, String component)
    {
    	return getRotationOffsetPoint(animType, lookForParentAnimation(animType, getItemLocationKey(weapon)), component);
    }
    public static float getSpecificAnimationProgress(String animType, ItemStack weapon, Player player, float partialTicks)
    {
    	ResourceLocation weapKey = lookForParentAnimation(animType, getItemLocationKey(weapon));
    	if (animType.equals("draw"))
    	{
    		float animationSpeed = (float) getAnimationValue(animType, weapKey, "animationSpeed");
        	int weaponSwitchTick = ShootingHandler.get().getWeaponSwitchTick();
    		float drawProgress = ((player.tickCount-weaponSwitchTick)+partialTicks)*animationSpeed;
    		int totalFrames = Math.max(getAnimationFrames(animType, weapKey),1);
    		return drawProgress/totalFrames;
    	}
    	if (animType.equals("inspect"))
    	{
    		float animationSpeed = (float) getAnimationValue(animType, weapKey, "animationSpeed");
        	int weaponInspectTick = ShootingHandler.get().getWeaponInspectTick();
    		float inspectProgress = ((player.tickCount-weaponInspectTick)+partialTicks)*animationSpeed;
    		int totalFrames = Math.max(getAnimationFrames(animType, weapKey),1);
    		return inspectProgress/totalFrames;
    	}
    	if (animType.equals("reloadStart"))
    	{
    		return ReloadHandler.get().getReloadProgress(partialTicks);
    	}
    	if (animType.equals("reloadEnd"))
    	{
    		float reloadTransitionProgress = ReloadHandler.get().getReloadProgress(partialTicks);
    		return 1-reloadTransitionProgress;
    	}
    	if (animType.equals("reload") && hasAnimation("reload", weapon))
    	{
    		float progress = ((GunItem) (weapon.getItem())).getModifiedGun(weapon).getGeneral().usesMagReload() ? GunRenderingHandler.get().getReloadDeltaTime(weapon) : GunRenderingHandler.get().getReloadCycleProgress(weapon);
    	    return progress;
    	}
    	if (animType.equals("fire") && hasAnimation("fire", weapon))
    	{
    		if (getAnimationBoolean(animType, weapKey, "syncToCooldown", true))
    		{
    			ItemCooldowns tracker = player.getCooldowns();
	            float cooldown = tracker.getCooldownPercent(weapon.getItem(), Minecraft.getInstance().getFrameTime());
	            float fireRate = GunCompositeStatHelper.getCompositeRate(weapon, player);
	            float maxRate = getAnimationValueFloat(animType, weapKey, "maxRate");
	            if (maxRate>1)
	            {
	                float cooldownDivider = 1.0F*Math.max(fireRate/maxRate,1);
	                float cooldownOffset = cooldownDivider - 1.0F;
	            	cooldown = (cooldown*cooldownDivider)-cooldownOffset;
    			}
	            
	            if (cooldown>0)
	            return 1-cooldown;
    		}
    		else
    		{
	            float animationSpeed = (float) getAnimationValue(animType, weapKey, "animationSpeed");
            	int lastFiretick = GunRenderingHandler.get().getLastFireTick();
        		float fireProgress = ((player.tickCount-lastFiretick)+partialTicks)*animationSpeed;
        		int totalFrames = Math.max(getAnimationFrames(animType, weapKey),1);
        		return fireProgress/totalFrames;
    		}
    	}
    	if (animType.equals("recoil") && hasAnimation("recoil", weapon))
    	{
    		if (!getAnimationBoolean(animType, weapKey, "syncToCooldown"))
    		{
    			float animationSpeed = (float) getAnimationValue(animType, weapKey, "animationSpeed");
            	int lastFiretick = GunRenderingHandler.get().getLastFireTick();
        		float recoilProgress = ((player.tickCount-lastFiretick)+partialTicks)*animationSpeed;
        		int totalFrames = Math.max(getAnimationFrames(animType, weapKey),1);
        		return recoilProgress/totalFrames;
    		}
    		else
    		{
	    		ItemCooldowns tracker = player.getCooldowns();
	            float cooldown = tracker.getCooldownPercent(weapon.getItem(), Minecraft.getInstance().getFrameTime());
	            float fireRate = GunCompositeStatHelper.getCompositeRate(weapon, player);
	            float maxRate = getAnimationValueFloat(animType, weapKey, "maxRate");
	            if (maxRate>1)
	            {
	                float cooldownDivider = 1.0F*Math.max(fireRate/maxRate,1);
	                float cooldownOffset = cooldownDivider - 1.0F;
	            	cooldown = (cooldown*cooldownDivider)-cooldownOffset;
    			}
	            
	            if (cooldown>0)
	            return 1-cooldown;
    		}
    	}
    	
    	return 0;
    }
    
    
    
    
    
	/* 3D Vector builders for keyframe-based animations */
    
	public static Vec3 getAnimationTrans(String animationType, ItemStack weapon, float progress, String component)
	{
		ResourceLocation weapKey = lookForParentAnimation(animationType, getItemLocationKey(weapon));
		
		String animSuffix = getReloadAnimSuffix(animationType, weapKey);
		if (!animationType.isEmpty())
			animationType = animationType+animSuffix;
		
		Vec3 blendedTransforms = Vec3.ZERO;
		float scaledProgress = getScaledProgress(animationType, weapKey, progress);
		int currentFrame = getCurrentFrame(weapon, scaledProgress);
		int priorFrame = findPriorFrame(animationType, weapKey, component, currentFrame, "translation");
		int nextFrame = findNextFrame(animationType, weapKey, component, currentFrame+1, "translation");
		float frameProgress = Math.max(scaledProgress - ((float) priorFrame), 0);
		
		String priorAnimType = animationType;
		String nextAnimType = animationType;
		int frameDiv = Math.max(Math.abs(nextFrame-priorFrame),1);
		
		if (animationType.contains("reloadStart"))
		{
			if (nextFrame>=GunAnimationHelper.getAnimationFrames(animationType, weapKey))
			{
				nextFrame = 0;
				nextAnimType = "reload"+animSuffix;
			}
			if (currentFrame>=GunAnimationHelper.getAnimationFrames(animationType, weapKey))
			{
				priorFrame = 0;
				priorAnimType = "reload"+animSuffix;
			}
		}
		if (animationType.contains("reloadEnd"))
		{
			if (priorFrame<1)
			{
				int reloadFrames = GunAnimationHelper.getAnimationFrames("reload"+animSuffix, weapKey);
				priorFrame = findPriorFrame("reload"+animSuffix, weapKey, component, reloadFrames, "translation");
				priorAnimType = "reload"+animSuffix;
			}
			if (currentFrame<0)
			{
				int reloadFrames = GunAnimationHelper.getAnimationFrames("reload"+animSuffix, weapKey);
				nextFrame = findPriorFrame("reload"+animSuffix, weapKey, component, reloadFrames, "translation");
				nextAnimType = "reload"+animSuffix;
			}
		}
		if (animationType.equals("reload") || animationType.contains("reload_"))
		{
			float delta = GunRenderingHandler.get().getReloadDeltaTime(weapon);
			if (priorFrame==0 && delta>0.8F)
			priorFrame = GunAnimationHelper.getAnimationFrames(animationType, weapKey);
		}
		
		Vec3 priorTransforms = getAnimTranslation(priorAnimType, weapKey, component, priorFrame, weapon);
		Vec3 nextTransforms = getAnimTranslation(nextAnimType, weapKey, component, nextFrame, weapon);
		Easings easing = getAnimEasing(nextAnimType, weapKey, component, findPriorFrame(nextAnimType, weapKey, component, nextFrame, "translation"), false);
		double easeFactor = getEaseFactor(easing, frameProgress/frameDiv);
		blendedTransforms = priorTransforms.lerp(nextTransforms, Mth.clamp(easeFactor, 0F,1F));
		
		//Left handed compat
		if (component.equals("viewModel") /*|| component.equals("gunModel")*/)
		{
			Minecraft mc = Minecraft.getInstance();
			boolean rightHand = mc.options.mainHand().get().equals(HumanoidArm.RIGHT);
			
			if (!rightHand)
			blendedTransforms.multiply(-1, 1, 1);
		}
		
		return blendedTransforms;
	}
    
	public static Vec3 getAnimationRot(String animationType, ItemStack weapon, float progress, String component)
	{
		ResourceLocation weapKey = lookForParentAnimation(animationType, getItemLocationKey(weapon));
		
		String animSuffix = getReloadAnimSuffix(animationType, weapKey);
		if (!animationType.isEmpty())
			animationType = animationType+animSuffix;
		
		Vec3 blendedTransforms = Vec3.ZERO;
		float scaledProgress = getScaledProgress(animationType, weapKey, progress);
		int currentFrame = getCurrentFrame(weapon, scaledProgress);
		int priorFrame = findPriorFrame(animationType, weapKey, component, currentFrame, "rotation");
		int nextFrame = findNextFrame(animationType, weapKey, component, currentFrame+1, "rotation");
		float frameProgress = Math.max(scaledProgress - ((float) priorFrame), 0);
		
		String priorAnimType = animationType;
		String nextAnimType = animationType;
		int frameDiv = Math.max(Math.abs(nextFrame-priorFrame),1);
		
		if (animationType.contains("reloadStart"))
		{
			if (nextFrame>=GunAnimationHelper.getAnimationFrames(animationType, weapKey))
			{
				nextFrame = 0;
				nextAnimType = "reload"+animSuffix;
			}
			if (currentFrame>=GunAnimationHelper.getAnimationFrames(animationType, weapKey))
			{
				priorFrame = 0;
				priorAnimType = "reload"+animSuffix;
			}
		}
		if (animationType.contains("reloadEnd"))
		{
			if (priorFrame<1)
			{
				int reloadFrames = GunAnimationHelper.getAnimationFrames("reload"+animSuffix, weapKey);
				priorFrame = findPriorFrame("reload"+animSuffix, weapKey, component, reloadFrames, "translation");
				priorAnimType = "reload"+animSuffix;
			}
			if (currentFrame<0)
			{
				int reloadFrames = GunAnimationHelper.getAnimationFrames("reload"+animSuffix, weapKey);
				nextFrame = findPriorFrame("reload"+animSuffix, weapKey, component, reloadFrames, "translation");
				nextAnimType = "reload"+animSuffix;
			}
		}
		if (animationType.equals("reload") || animationType.contains("reload_"))
		{
			float delta = GunRenderingHandler.get().getReloadDeltaTime(weapon);
			if (priorFrame==0 && delta>0.8F)
			priorFrame = GunAnimationHelper.getAnimationFrames(animationType, weapKey);
		}
		
		Vec3 priorTransforms = getAnimRotation(priorAnimType, weapKey, component, priorFrame);
		Vec3 nextTransforms = getAnimRotation(nextAnimType, weapKey, component, nextFrame);
		Easings easing = getAnimEasing(nextAnimType, weapKey, component, findPriorFrame(nextAnimType, weapKey, component, nextFrame, "rotation"), true);
		double easeFactor = getEaseFactor(easing, frameProgress/frameDiv);
		blendedTransforms = priorTransforms.lerp(nextTransforms, Mth.clamp(easeFactor, 0F,1F));
		
		//Left handed compat
		if (component.equals("viewModel") /*|| component.equals("gunModel")*/)
		{
			Minecraft mc = Minecraft.getInstance();
			boolean rightHand = mc.options.mainHand().get().equals(HumanoidArm.RIGHT);
			
			if (!rightHand)
			blendedTransforms.multiply(1, -1, -1);
		}
		
		return blendedTransforms;
	}

	public static double getAnimationValue(String animationType, ItemStack weapon, float progress, String component, String valueName)
	{
		return getAnimationValue(animationType, weapon, progress, component, valueName, 0);
	}
	public static double getAnimationValue(String animationType, ItemStack weapon, float progress, String component, String valueName, double returnVal)
	{
		ResourceLocation weapKey = lookForParentAnimation(animationType, getItemLocationKey(weapon));
		
		animationType = addReloadAnimSuffix(animationType, weapKey);
		
		float scaledProgress = getScaledProgress(animationType, weapKey, progress);
		int currentFrame = getCurrentFrame(weapon, scaledProgress);
		int priorFrame = findPriorFrameValue(animationType, weapKey, component, currentFrame, valueName);
		
		double animationValue = getAnimationValue(animationType, weapKey, component, priorFrame, valueName, returnVal);
		
		return animationValue;
	}

	
    
	/* Reload animation suffix calculators */
	public static String getReloadAnimSuffix(String animationType, ResourceLocation weapKey)
	{
		boolean magReload = ReloadHandler.get().isDoMagReload();
		boolean emptyReload = ReloadHandler.get().isReloadFromEmpty();
		String animSuffix = "";
		if (animationType.contains("reload") && (magReload || emptyReload))
		{
			if (magReload && emptyReload && hasAnimation(animationType + "_EmptyMag", weapKey))
			animSuffix = "_EmptyMag";
			else
			if (magReload && hasAnimation(animationType + "_Mag", weapKey))
			animSuffix = "_Mag";
			else
			if (emptyReload && hasAnimation(animationType + "_Empty", weapKey))
			animSuffix = "_Empty";
		}
		
		return animSuffix;
	}
	
	public static String addReloadAnimSuffix(String animationType, ResourceLocation weapKey)
	{
		String animSuffix = getReloadAnimSuffix(animationType, weapKey);
		if (!animationType.isEmpty())
			animationType = animationType+animSuffix;
		
		return animationType;
	}
	
	public static String addReloadAnimSuffix(String animationType, ItemStack weapon)
	{
		ResourceLocation weapKey = lookForParentAnimation(animationType, getItemLocationKey(weapon));
		return addReloadAnimSuffix(animationType, weapKey);
	}

	/* Animation calculators methods for more advanced control */
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
	public static float getScaledProgress(String animationType, ItemStack weapon, float progress)
	{
		ResourceLocation weapKey = lookForParentAnimation(animationType, getItemLocationKey(weapon));
		return getScaledProgress(animationType, weapKey, progress);
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

	
    
	/* Animation application methods to speed up animation implementation */
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
		rotateAroundOffset(poseStack, rotations, GunAnimationHelper.getRotationOffsetPoint(addReloadAnimSuffix(animationType, weapKey), weapKey, component));
	}
	
	
	
	// Public data getters for advanced animation manipulation.
	public static Vec3 getAnimationArrayPublic(String animationType, ResourceLocation weapKey, String component, String transform) {
		return getAnimationArray(animationType, weapKey, component, transform);
	}
	public static Vec3 getAnimationArrayPublic(String animationType, ResourceLocation weapKey, String component, int frame, String transform) {
		return getAnimationArray(animationType, weapKey, component, frame, transform);
	}
	
	public static double getAnimationValuePublic(String animationType, ResourceLocation weapKey, String transform) {
		return getAnimationValue(animationType, weapKey, transform);
	}
	public static double getAnimationValuePublic(String animationType, ResourceLocation weapKey, String transform, double returnVal) {
		return getAnimationValue(animationType, weapKey, transform, returnVal);
	}
	
	public static double getAnimationValuePublic(String animationType, ItemStack weapon, String transform) {
		return getAnimationValue(animationType, lookForParentAnimation(animationType, getItemLocationKey(weapon)), transform);
	}
	public static double getAnimationValuePublic(String animationType, ItemStack weapon, String transform, double returnVal) {
		return getAnimationValue(animationType, lookForParentAnimation(animationType, getItemLocationKey(weapon)), transform, returnVal);
	}
	public static double getAnimationValuePublic(String animationType, ItemStack weapon, String component, String transform) {
		return getAnimationValue(animationType, lookForParentAnimation(animationType, getItemLocationKey(weapon)), component, transform, 0);
	}
	public static double getAnimationValuePublic(String animationType, ItemStack weapon, String component, String transform, double returnVal) {
		return getAnimationValue(animationType, lookForParentAnimation(animationType, getItemLocationKey(weapon)), component, transform, returnVal);
	}
	
	public static double getAnimationValuePublic(String animationType, ResourceLocation weapKey, String component, int frame, String transform) {
		return getAnimationValue(animationType, weapKey, component, frame, transform, 0);
	}
	public static double getAnimationValuePublic(String animationType, ResourceLocation weapKey, String component, int frame, String transform, double returnVal) {
		return getAnimationValue(animationType, weapKey, component, frame, transform, returnVal);
	}
	

	public static double getAnimationValuePublic(ItemStack weapon, Player player, float partialTicks, String component, String transform, double returnVal) {
		String animationType = getSmartAnimationType(weapon, player, partialTicks);
		return getAnimationValue(animationType, lookForParentAnimation(animationType, getItemLocationKey(weapon)), transform, returnVal);
	}
	
	
	
	/* Property Helpers for animations */
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
	public static int getAnimationFrames(String animationType, ItemStack weapon) {
		ResourceLocation weapKey = lookForParentAnimation(animationType, getItemLocationKey(weapon));
		return getAnimationFrames(animationType, weapKey);
	}
	
	
	// Rotation offset points
	public static Vec3 getRotationOffsetPoint(String animationType, ResourceLocation weapKey, String component) {
		if (!animationType.contains("_"))
			animationType = addReloadAnimSuffix(animationType, weapKey);
		
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
	
	static int findPriorFrameValue(String animationType, ResourceLocation weapKey, String component, int frame, String transform) {
		int returnFrame=-1;
		for (int i=frame; returnFrame==-1 && i>=0; i--)
		{
			DataObject frameObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType, component, ""+i);
			if (frameObject!=null)
			{
				if (frameObject.has(transform, DataType.NUMBER))
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
	
	
	// Animation values
	static Vec3 getAnimationArray(String animationType, ResourceLocation weapKey, String component, String transform) {
		DataObject transformObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType, component);
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
	
	static boolean getAnimationBoolean(String animationType, ResourceLocation weapKey, String transform) {
		return getAnimationBoolean(animationType, weapKey, transform, false);
	}
	
	static boolean getAnimationBoolean(String animationType, ResourceLocation weapKey, String transform, boolean defaultReturn) {
		DataObject transformObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType);
		if (transformObject.has(transform, DataType.BOOLEAN))
		{
			DataBoolean transformData = transformObject.getDataBoolean(transform);
			if (transformData!=null)
			{
	        	//GunMod.LOGGER.info("Animation System: Found " + transform + " number data of " + weapKey);
				return transformData.asBoolean();
			}
        	//GunMod.LOGGER.info("Animation System: Found animation object of " + weapKey + " but did not find " + transform + " number data");
			
		}
		
		return defaultReturn;
	}
	
	static double getAnimationValue(String animationType, ResourceLocation weapKey, String transform) {
		DataObject transformObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType);
		if (transformObject.has(transform, DataType.NUMBER))
		{
			DataNumber transformData = transformObject.getDataNumber(transform);
			if (transformData!=null)
			{
	        	//GunMod.LOGGER.info("Animation System: Found " + transform + " number data of " + weapKey);
				return transformData.asDouble();
			}
        	//GunMod.LOGGER.info("Animation System: Found animation object of " + weapKey + " but did not find " + transform + " number data");
			
		}
		
		return 0;
	}
	static double getAnimationValue(String animationType, ResourceLocation weapKey, String transform, double defaultReturn) {
		DataObject transformObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType);
		if (transformObject.has(transform, DataType.NUMBER))
		{
			DataNumber transformData = transformObject.getDataNumber(transform);
			if (transformData!=null)
			{
	        	//GunMod.LOGGER.info("Animation System: Found " + transform + " number data of " + weapKey);
				return transformData.asDouble();
			}
        	//GunMod.LOGGER.info("Animation System: Found animation object of " + weapKey + " but did not find " + transform + " number data");
			
		}
		
		return defaultReturn;
	}
	static float getAnimationValueFloat(String animationType, ResourceLocation weapKey, String transform) {
		return (float) getAnimationValue(animationType, weapKey, transform);
	}
	
	static double getAnimationValue(String animationType, ResourceLocation weapKey, String component, String transform, double returnVal) {
		DataObject transformObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType, component);
		if (transformObject.has(transform, DataType.NUMBER))
		{
			DataNumber transformData = transformObject.getDataNumber(transform);
			if (transformData!=null)
			return transformData.asDouble();
		}
		
		return returnVal;
	}
	static float getAnimationValueFloat(String animationType, ResourceLocation weapKey, String component, String transform, double returnVal) {
		return (float) getAnimationValue(animationType, weapKey, component, transform, returnVal);
	}
	
	static double getAnimationValue(String animationType, ResourceLocation weapKey, String component, int frame, String transform, double returnVal) {
		DataObject transformObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType, component, ""+frame);
		if (transformObject.has(transform, DataType.NUMBER))
		{
			DataNumber transformData = transformObject.getDataNumber(transform);
			if (transformData!=null)
			return transformData.asDouble();
		}
		
		return returnVal;
	}
	static float getAnimationValueFloat(String animationType, ResourceLocation weapKey, String component, int frame, String transform, double returnVal) {
		return (float) getAnimationValue(animationType, weapKey, component, frame, transform, returnVal);
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

	
	public static int getAnimationSoundEventCount(String animationType, ItemStack weapon) {
		ResourceLocation weapKey = lookForParentAnimation(animationType, getItemLocationKey(weapon));
		DataObject audioObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType, "sounds");
		if (audioObject.has("count", DataType.NUMBER))
		{
			DataNumber count = audioObject.getDataNumber("count");
			if (count!=null)
            return count.asInt();
		}
		return 0;
	}
	public static String getAnimationSoundEventID(String animationType, ItemStack weapon, int soundIntID) {
		ResourceLocation weapKey = lookForParentAnimation(animationType, getItemLocationKey(weapon));
		DataObject audioObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType, "sounds", String.valueOf(soundIntID));
		if (audioObject.has("id", DataType.STRING))
		{
			DataString audioStringData = audioObject.getDataString("id");
			if (audioStringData!=null)
			{
				return audioStringData.asString();
			}
		}
		return null;
	}
	public static float getAnimationSoundParamFloat(String animationType, ItemStack weapon, int soundIntID, String parameter, float defaultValue) {
		ResourceLocation weapKey = lookForParentAnimation(animationType, getItemLocationKey(weapon));
		DataObject audioObject = getObjectByPath(weapKey, ANIMATION_KEY, animationType, "sounds", String.valueOf(soundIntID));
		if (audioObject.has(parameter, DataType.NUMBER))
		{
			DataNumber audioThresoldData = audioObject.getDataNumber(parameter);
			if (audioThresoldData!=null)
			{
				return audioThresoldData.asFloat();
			}
		}
		return defaultValue;
	}
	public static float getAnimationSoundParamFloat(String animationType, ItemStack weapon, int soundIntID, String parameter) {
		return getAnimationSoundParamFloat(animationType, weapon, soundIntID, parameter, -1);
	}
	
	
	
	// Additional methods to aid with interfacing with the animation system.
	@SuppressWarnings("deprecation")
	public static ResourceLocation getItemLocationKey(ItemStack stack)
	{
		ResourceLocation location = stack.getItem().builtInRegistryHolder().key().location();
        return location;
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
        	GunMod.LOGGER.info("Animation System: Attempting to load animation data with resource key: " + location.toString());
        	doTryingMetaLoadMessage=false;
    	}
        if (useLegacyLoader)
        	return AnimationMetaLoader.getInstance().getData(location);
        return AnimationLoader.getInstance().getData(location);
    }
}