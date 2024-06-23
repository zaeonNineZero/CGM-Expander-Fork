package com.mrcrayfish.guns.client.util;

import com.mrcrayfish.framework.api.serialize.DataArray;
import com.mrcrayfish.framework.api.serialize.DataNumber;
import com.mrcrayfish.framework.api.serialize.DataObject;
import com.mrcrayfish.framework.api.serialize.DataType;
import com.mrcrayfish.guns.cache.ObjectCache;
import com.mrcrayfish.guns.item.attachment.IAttachment;
import com.mrcrayfish.guns.item.attachment.IAttachment.Type;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Helper class for more complex gun animations, including attachment and hand movements
 */
public final class GunAnimationHelper
{
    public static final String WEAPON_KEY = "cgm:weapon";
    public static final String CACHE_KEY = "properties";

    public static void resetCache()
    {
        ObjectCache.getInstance(CACHE_KEY).reset();
    }
    
	public static float getAnimationValue(ItemStack weapon, float progress, String param)
	{
		float cooldownDivider = getCooldownDivider(weapon);
        float cooldownOffset = getCooldownOffset(weapon);
        float intensity = getAnimIntensity(weapon) +1;
        
        float cooldown = progress*cooldownDivider;
        float cooldown_a = cooldown-cooldownOffset;

        float cooldown_b = Math.min(Math.max(cooldown_a*intensity,0),1);
        float cooldown_c = Math.min(Math.max((-cooldown_a*intensity)+intensity,0),1);
        float cooldown_d = Math.min(cooldown_b,cooldown_c);
        
        return cooldown_d;
	}
	
	public static float getBoltAnimationValue(ItemStack weapon, float progress, String param)
	{
		float cooldownDivider = getCooldownDivider(weapon);
        float cooldownOffset = getCooldownOffset(weapon);
        float intensity = getAnimIntensity(weapon) +1;
        float boltLeadTime = getBoltLeadTime(weapon);
        if (param.equals("viewModel"))
        boltLeadTime = getViewmodelBoltLeadTime(weapon);
        
        float cooldown = progress*cooldownDivider;
        float cooldown_a = cooldown-cooldownOffset;

        float cooldown_b = Math.min(Math.max(cooldown_a*intensity+boltLeadTime,0),1);
        float cooldown_c = Math.min(Math.max((-cooldown_a*intensity+boltLeadTime)+intensity,0),1);
        float cooldown_d = Math.min(cooldown_b,cooldown_c);
        return cooldown_d;
	}
	
	public static float getHandBoltAnimationValue(ItemStack weapon, boolean isRearHand, float progress)
	{
		float cooldownDivider = getCooldownDivider(weapon);
        float cooldownOffset = getCooldownOffset(weapon);
        float intensity = getAnimIntensity(weapon) +1;
        float boltLeadTime = getHandBoltLeadTime(weapon, isRearHand);
        
        float cooldown = progress*cooldownDivider;
        float cooldown_a = cooldown-cooldownOffset;

        float cooldown_b = Math.min(Math.max(cooldown_a*intensity+boltLeadTime,0),1);
        float cooldown_c = Math.min(Math.max((-cooldown_a*intensity+boltLeadTime)+intensity,0),1);
        float cooldown_d = Math.min(cooldown_b,cooldown_c);
        return cooldown_d;
	}
	
	protected static Vec3 applyAnimation(ItemStack weapon, Vec3 input, float progress, String param)
	{
		return input.scale(getAnimationValue(weapon, progress, param));
		//return new Vec3(input.x*applyAnimation(weapon,progress),input.y*applyAnimation(weapon,progress),input.z*applyAnimation(weapon,progress));
	}
	
	protected static Vec3 applyBoltAnimation(ItemStack weapon, Vec3 input, float progress, String param)
	{
		return input.scale(getBoltAnimationValue(weapon, progress,param));
	}
	
	protected static Vec3 applyHandBoltAnimation(ItemStack weapon, Vec3 input, boolean isRearHand, float progress)
	{
		return input.scale(getHandBoltAnimationValue(weapon, isRearHand, progress));
	}
	
	public static Vec3 getViewModelTranslation(ItemStack weapon, float progress)
	{
		Vec3 anim1 = getViewModelAnimTranslation(weapon, false);
		Vec3 anim2 = getViewModelAnimTranslation(weapon, true);
		
		anim1 = applyAnimation(weapon, anim1, progress, "viewModel");
		anim2 = applyBoltAnimation(weapon, anim2, progress, "viewModel");
		
		Vec3 animation = anim1.add(anim2);
		return animation;
	}
	
	public static Vec3 getViewModelRotation(ItemStack weapon, float progress)
	{
		Vec3 anim1 = getViewModelAnimRotation(weapon, false);
		Vec3 anim2 = getViewModelAnimRotation(weapon, true);
		
		anim1 = applyAnimation(weapon, anim1, progress, "viewModel");
		anim2 = applyBoltAnimation(weapon, anim2, progress, "viewModel");
		
		Vec3 animation = anim1.add(anim2);
		return animation;
	}
	
	public static Vec3 getComponentTranslation(ItemStack weapon, float progress)
	{
		Vec3 anim1 = getComponentAnimTranslation(weapon, false);
		Vec3 anim2 = getComponentAnimTranslation(weapon, true);
		
		anim1 = applyAnimation(weapon, anim1, progress, "component");
		anim2 = applyBoltAnimation(weapon, anim2, progress, "component");
		
		Vec3 animation = anim1.add(anim2);
		return animation;
	}
	
	public static Vec3 getComponentRotation(ItemStack weapon, float progress)
	{
		Vec3 anim1 = getComponentAnimRotation(weapon, false);
		Vec3 anim2 = getComponentAnimRotation(weapon, true);
		
		anim1 = applyAnimation(weapon, anim1, progress, "component");
		anim2 = applyBoltAnimation(weapon, anim2, progress, "component");
		
		Vec3 animation = anim1.add(anim2);
		return animation;
	}
	
	public static Vec3 getAttachmentTranslation(ItemStack weapon, IAttachment.Type type, float progress)
	{
		if (hasAttachmentAnimation(weapon, type))
		{
			Vec3 animation = getAttachmentAnimTranslation(weapon, type);
			animation = applyAnimation(weapon, animation, progress, type.toString());
			return animation;
		}
		return Vec3.ZERO;
	}
	
	public static Vec3 getHandTranslation(ItemStack weapon, boolean isRearHand, float progress)
	{
		if (hasHandAnimation(weapon, isRearHand))
		{
			Vec3 anim1 = getHandAnimationTranslation(weapon, isRearHand, false);
			Vec3 anim2 = getHandAnimationTranslation(weapon, isRearHand, true);
			
			anim1 = applyAnimation(weapon, anim1, progress, "hands");
			anim2 = applyHandBoltAnimation(weapon, anim2, isRearHand, progress);
			
			Vec3 animation = anim1.add(anim2);
			return animation.scale(getHandAnimScalar(weapon));
		}
		return Vec3.ZERO;
	}
	
	
	
	
	
	/* Property Helpers for Fire Animations */
	
	public static boolean hasAttachmentAnimation(ItemStack weapon, Type type) {
		DataObject scopeObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "attachments", type.getSerializeKey());
        return scopeObject.has("animTranslate", DataType.ARRAY);
	}
	public static boolean hasHandAnimation(ItemStack weapon, boolean isRearHand) {
		DataObject handObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "hands", isRearHand ? "rear" : "forward");
        return handObject.has("animTranslate", DataType.ARRAY);
	}
	

	public static float getCooldownDivider(ItemStack weapon) {
		DataObject animationObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "animation");
        if(animationObject.has("cooldownDivider", DataType.NUMBER))
        {
        	DataNumber cooldownDivider = animationObject.getDataNumber("cooldownDivider");
        	return cooldownDivider.asFloat();
		}
        
        return 1F;
	}
	public static float getCooldownOffset(ItemStack weapon) {
		DataObject animationObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "animation");
        if(animationObject.has("cooldownOffset", DataType.NUMBER))
        {
        	DataNumber cooldownOffset = animationObject.getDataNumber("cooldownOffset");
            return cooldownOffset.asFloat();
		}
        
        return 0F;
	}
	public static float getAnimIntensity(ItemStack weapon) {
		DataObject animationObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "animation");
        if(animationObject.has("animIntensity", DataType.NUMBER))
        {
        	DataNumber animIntensity = animationObject.getDataNumber("animIntensity");
            return animIntensity.asFloat();
		}
        
        return 1F;
	}
	public static float getBoltLeadTime(ItemStack weapon) {
		DataObject animationObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "animation");
        if(animationObject.has("boltLeadTime", DataType.NUMBER))
        {
        	DataNumber boltLeadTime = animationObject.getDataNumber("boltLeadTime");
            return boltLeadTime.asFloat();
		}
        
        return 0F;
	}
	public static float getViewmodelBoltLeadTime(ItemStack weapon) {
		DataObject animationObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "animation", "viewModel");
        if(animationObject.has("boltLeadTime", DataType.NUMBER))
        {
        	DataNumber boltLeadTime = animationObject.getDataNumber("boltLeadTime");
            return boltLeadTime.asFloat();
		}
        
        return 0F;
	}
	public static float getHandBoltLeadTime(ItemStack weapon, boolean isRearHand) {
		DataObject handObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "hands", isRearHand ? "rear" : "forward");
        if(handObject.has("boltLeadTime", DataType.NUMBER))
        {
        	DataNumber boltLeadTime = handObject.getDataNumber("boltLeadTime");
            return boltLeadTime.asFloat();
		}
        
        return getBoltLeadTime(weapon);
	}
	

	public static double getHandAnimScalar(ItemStack weapon) {
		DataObject handObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "hands");
        if(handObject.has("animScalar", DataType.NUMBER))
        {
        	DataNumber animScalar = handObject.getDataNumber("animScalar");
        	return animScalar.asDouble()*2.0;
		}
        
        return 1.6;
	}
	

	public static Vec3 getViewModelAnimTranslation(ItemStack weapon, boolean isBoltAnim) {
		DataObject animationObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "animation", "viewModel");
		if (animationObject.has("animTranslate", DataType.ARRAY) && !isBoltAnim)
		{
			DataArray translationArray = animationObject.getDataArray("animTranslate");
			if (translationArray!=null)
            return PropertyHelper.arrayToVec3(translationArray, Vec3.ZERO);
		}
		if (animationObject.has("boltAnimTranslate", DataType.ARRAY) && isBoltAnim)
		{
			DataArray translationArray = animationObject.getDataArray("boltAnimTranslate");
			if (translationArray!=null)
            return PropertyHelper.arrayToVec3(translationArray, Vec3.ZERO);
		}
		
		return Vec3.ZERO;
	}
	public static Vec3 getViewModelAnimRotation(ItemStack weapon, boolean isBoltAnim) {
		DataObject animationObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "animation", "viewModel");
		if (animationObject.has("animRotation", DataType.ARRAY) && !isBoltAnim)
		{
			DataArray rotationArray = animationObject.getDataArray("animRotation");
			if (rotationArray!=null)
            return PropertyHelper.arrayToVec3(rotationArray, Vec3.ZERO);
		}
		if (animationObject.has("boltAnimRotation", DataType.ARRAY) && isBoltAnim)
		{
			DataArray rotationArray = animationObject.getDataArray("boltAnimRotation");
			if (rotationArray!=null)
            return PropertyHelper.arrayToVec3(rotationArray, Vec3.ZERO);
		}
		
		return Vec3.ZERO;
	}
	
	public static Vec3 getComponentAnimTranslation(ItemStack weapon, boolean isBoltAnim) {
		DataObject animationObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "animation", "component");
		if (animationObject.has("animTranslate", DataType.ARRAY) && !isBoltAnim)
		{
			DataArray translationArray = animationObject.getDataArray("animTranslate");
			if (translationArray!=null)
            return PropertyHelper.arrayToVec3(translationArray, Vec3.ZERO);
		}
		if (animationObject.has("boltAnimTranslate", DataType.ARRAY) && isBoltAnim)
		{
			DataArray translationArray = animationObject.getDataArray("boltAnimTranslate");
			if (translationArray!=null)
            return PropertyHelper.arrayToVec3(translationArray, Vec3.ZERO);
		}
		
		return Vec3.ZERO;
	}
	public static Vec3 getComponentAnimRotation(ItemStack weapon, boolean isBoltAnim) {
		DataObject animationObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "animation", "component");
		if (animationObject.has("animRotation", DataType.ARRAY) && !isBoltAnim)
		{
			DataArray rotationArray = animationObject.getDataArray("animRotation");
			if (rotationArray!=null)
            return PropertyHelper.arrayToVec3(rotationArray, Vec3.ZERO);
		}
		if (animationObject.has("boltAnimRotation", DataType.ARRAY) && isBoltAnim)
		{
			DataArray rotationArray = animationObject.getDataArray("boltAnimRotation");
			if (rotationArray!=null)
            return PropertyHelper.arrayToVec3(rotationArray, Vec3.ZERO);
		}
		
		return Vec3.ZERO;
	}
	
	public static Vec3 getAttachmentAnimTranslation(ItemStack weapon, IAttachment.Type type) {
		DataObject scopeObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "attachments", type.getSerializeKey());
		if (scopeObject.has("animTranslate", DataType.ARRAY))
		{
			DataArray translationArray = scopeObject.getDataArray("animTranslate");
			if (translationArray!=null)
            return PropertyHelper.arrayToVec3(translationArray, Vec3.ZERO);
		}
		
		return Vec3.ZERO;
	}
	
	public static Vec3 getHandAnimationTranslation(ItemStack weapon, boolean isRearHand, boolean isBoltAnim) {
		DataObject handObject = PropertyHelper.getObjectByPath(weapon, WEAPON_KEY, "hands", isRearHand ? "rear" : "forward");
		if (handObject.has("animTranslate", DataType.ARRAY) && !isBoltAnim)
		{
			DataArray translationArray = handObject.getDataArray("animTranslate");
			if (translationArray!=null)
            return PropertyHelper.arrayToVec3(translationArray, Vec3.ZERO);
		}
		if (handObject.has("boltAnimTranslate", DataType.ARRAY) && isBoltAnim)
		{
			DataArray translationArray = handObject.getDataArray("boltAnimTranslate");
			if (translationArray!=null)
            return PropertyHelper.arrayToVec3(translationArray, Vec3.ZERO);
		}
		
		return Vec3.ZERO;
	}
}