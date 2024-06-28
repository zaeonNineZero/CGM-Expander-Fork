package com.mrcrayfish.guns.client.render.pose;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.client.handler.GunRenderingHandler;
import com.mrcrayfish.guns.client.handler.ReloadHandler;
import com.mrcrayfish.guns.client.util.Easings;
import com.mrcrayfish.guns.client.util.GunAnimationHelper;
import com.mrcrayfish.guns.client.util.GunLegacyAnimationHelper;
import com.mrcrayfish.guns.client.util.GunReloadAnimationHelper;
import com.mrcrayfish.guns.client.util.PropertyHelper;
import com.mrcrayfish.guns.client.util.RenderUtil;
import com.mrcrayfish.guns.common.GripType;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.common.Gun.Display.ForwardHandPos;
import com.mrcrayfish.guns.common.Gun.Display.RearHandPos;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import com.mrcrayfish.guns.item.GunItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Author: MrCrayfish
 */
public class TwoHandedPose extends WeaponPose
{
    @Override
    protected AimPose getUpPose()
    {
        AimPose upPose = new AimPose();
        upPose.getIdle().setRenderYawOffset(45F).setItemRotation(new Vector3f(60F, 0F, 10F)).setRightArm(new LimbPose().setRotationAngleX(-120F).setRotationAngleY(-55F).setRotationPointX(-5).setRotationPointY(3).setRotationPointZ(0)).setLeftArm(new LimbPose().setRotationAngleX(-160F).setRotationAngleY(-20F).setRotationAngleZ(-30F).setRotationPointY(2).setRotationPointZ(-1));
        upPose.getAiming().setRenderYawOffset(45F).setItemRotation(new Vector3f(40F, 0F, 30F)).setItemTranslate(new Vector3f(-1, 0, 0)).setRightArm(new LimbPose().setRotationAngleX(-140F).setRotationAngleY(-55F).setRotationPointX(-5).setRotationPointY(3).setRotationPointZ(0)).setLeftArm(new LimbPose().setRotationAngleX(-170F).setRotationAngleY(-20F).setRotationAngleZ(-35F).setRotationPointY(1).setRotationPointZ(0));
        return upPose;
    }

    @Override
    protected AimPose getForwardPose()
    {
        AimPose forwardPose = new AimPose();
        forwardPose.getIdle().setRenderYawOffset(45F).setItemRotation(new Vector3f(30F, -11F, 0F)).setRightArm(new LimbPose().setRotationAngleX(-60F).setRotationAngleY(-55F).setRotationAngleZ(0F).setRotationPointX(-5).setRotationPointY(2).setRotationPointZ(1)).setLeftArm(new LimbPose().setRotationAngleX(-65F).setRotationAngleY(-10F).setRotationAngleZ(5F).setRotationPointY(2).setRotationPointZ(-1));
        forwardPose.getAiming().setRenderYawOffset(45F).setItemRotation(new Vector3f(5F, -21F, 0F)).setRightArm(new LimbPose().setRotationAngleX(-85F).setRotationAngleY(-65F).setRotationAngleZ(0F).setRotationPointX(-5).setRotationPointY(2)).setLeftArm(new LimbPose().setRotationAngleX(-90F).setRotationAngleY(-15F).setRotationAngleZ(0F).setRotationPointY(2).setRotationPointZ(0));
        return forwardPose;
    }

    @Override
    protected AimPose getDownPose()
    {
        AimPose downPose = new AimPose();
        downPose.getIdle().setRenderYawOffset(45F).setItemRotation(new Vector3f(-15F, -5F, 0F)).setItemTranslate(new Vector3f(0, -0.5F, 0.5F)).setRightArm(new LimbPose().setRotationAngleX(-30F).setRotationAngleY(-65F).setRotationAngleZ(0F).setRotationPointX(-5).setRotationPointY(2)).setLeftArm(new LimbPose().setRotationAngleX(-5F).setRotationAngleY(-20F).setRotationAngleZ(20F).setRotationPointY(5).setRotationPointZ(0));
        downPose.getAiming().setRenderYawOffset(45F).setItemRotation(new Vector3f(-20F, -5F, -10F)).setItemTranslate(new Vector3f(0, -0.5F, 1F)).setRightArm(new LimbPose().setRotationAngleX(-30F).setRotationAngleY(-65F).setRotationAngleZ(0F).setRotationPointX(-5).setRotationPointY(1)).setLeftArm(new LimbPose().setRotationAngleX(-10F).setRotationAngleY(-20F).setRotationAngleZ(30F).setRotationPointY(5).setRotationPointZ(0));
        return downPose;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyPlayerModelRotation(Player player, ModelPart rightArm, ModelPart leftArm, ModelPart head, InteractionHand hand, float aimProgress)
    {
        if(Config.CLIENT.display.oldAnimations.get())
        {
            Minecraft mc = Minecraft.getInstance();
            boolean right = mc.options.mainHand().get() == HumanoidArm.RIGHT ? hand == InteractionHand.MAIN_HAND : hand == InteractionHand.OFF_HAND;
            ModelPart mainArm = right ? rightArm : leftArm;
            ModelPart secondaryArm = right ? leftArm : rightArm;
            mainArm.xRot = head.xRot;
            mainArm.yRot = head.yRot;
            mainArm.zRot = head.zRot;
            secondaryArm.xRot = head.xRot;
            secondaryArm.yRot = head.yRot;
            secondaryArm.zRot = head.zRot;
            mainArm.xRot = (float) Math.toRadians(-55F + aimProgress * -30F);
            mainArm.yRot = (float) Math.toRadians((-45F + aimProgress * -20F) * (right ? 1F : -1F));
            secondaryArm.xRot = (float) Math.toRadians(-42F + aimProgress * -48F);
            secondaryArm.yRot = (float) Math.toRadians((-15F + aimProgress * 5F) * (right ? 1F : -1F));
        }
        else
        {
            super.applyPlayerModelRotation(player, rightArm, leftArm, head, hand, aimProgress);
            float angle = this.getPlayerPitch(player);
            head.xRot = (float) Math.toRadians(angle > 0.0 ? angle * 70F : angle * 90F);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyPlayerPreRender(Player player, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer)
    {
        if(Config.CLIENT.display.oldAnimations.get())
        {
            boolean right = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT ? hand == InteractionHand.MAIN_HAND : hand == InteractionHand.OFF_HAND;
            player.yBodyRotO = player.yRotO + (right ? 25F : -25F) + aimProgress * (right ? 20F : -20F);
            player.yBodyRot = player.getYRot() + (right ? 25F : -25F) + aimProgress * (right ? 20F : -20F);
        }
        else
        {
            super.applyPlayerPreRender(player, hand, aimProgress, poseStack, buffer);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyHeldItemTransforms(Player player, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer)
    {
        if(Config.CLIENT.display.oldAnimations.get())
        {
            if(hand == InteractionHand.MAIN_HAND)
            {
                boolean right = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT ? hand == InteractionHand.MAIN_HAND : hand == InteractionHand.OFF_HAND;
                poseStack.translate(0, 0, 0.05);
                float invertRealProgress = 1.0F - aimProgress;
                poseStack.mulPose(Vector3f.ZP.rotationDegrees((25F * invertRealProgress) * (right ? 1F : -1F)));
                poseStack.mulPose(Vector3f.YP.rotationDegrees((30F * invertRealProgress + aimProgress * -20F) * (right ? 1F : -1F)));
                poseStack.mulPose(Vector3f.XP.rotationDegrees(25F * invertRealProgress + aimProgress * 5F));
            }
        }
        else
        {
            super.applyHeldItemTransforms(player, hand, aimProgress, poseStack, buffer);
        }
    }

    @Override
    public void renderFirstPersonArms(Player player, HumanoidArm hand, ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light, float partialTicks)
    {
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180F));

        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, player.level, player, 0);
        float translateX = model.getTransforms().firstPersonRightHand.translation.x();
        int side = hand.getOpposite() == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(translateX * side, 0, 0);

        boolean slim = Minecraft.getInstance().player.getModelName().equals("slim");
        float armWidth = slim ? 3.0F : 4.0F;
        
        if (!(stack.getItem() instanceof GunItem))
        	return;
        GunItem gunStack = (GunItem) stack.getItem();
        Gun gun = gunStack.getModifiedGun(stack);
        
        ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
        float cooldown = tracker.getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());

        // Front arm holding the barrel
        poseStack.pushPose();
        {
        	Vec3 posHand = PropertyHelper.getHandPosition(stack, gun, false);
        	
        	Vec3 translations = GunAnimationHelper.getSmartAnimationTrans(stack, player, partialTicks, "forwardHand");
            Vec3 rotations = GunAnimationHelper.getSmartAnimationRot(stack, player, partialTicks, "forwardHand");
        	if(!GunAnimationHelper.hasAnimation("fire", stack) && GunAnimationHelper.getSmartAnimationType(stack, player, partialTicks)=="fire")
        	{
        		translations = GunLegacyAnimationHelper.getHandTranslation(stack, false, cooldown);
        	}
        	else
        	if(!GunAnimationHelper.hasAnimation("reload", stack))
        	{
        		float reloadProg = ReloadHandler.get().getReloadProgress(partialTicks);
                poseStack.translate(0, (-24 * reloadProg) * 0.0625, (-6 * reloadProg) * 0.0625);
        	}
            
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate((4.0 + posHand.x) * 0.0625 * side, (0 + posHand.y) * 0.0625, (0 - posHand.z) * 0.0625);
            //poseStack.translate(4.0 * 0.0625 * side, 0, 0;

            String animType = GunAnimationHelper.getSmartAnimationType(stack, player, partialTicks);
            poseStack.translate(translations.x * side * 0.0625, translations.y * 0.0625, -translations.z * 0.0625);
            GunAnimationHelper.rotateAroundOffset(poseStack, rotations, animType, stack, "forwardHand");
            
            poseStack.translate((armWidth / 2.0) * 0.0625 * side, 0, 0);
            poseStack.translate(-0.3125 * side, -0.1, -0.4375);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(80F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(15F * -side));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(15F * -side));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-35F));

        	if(GunAnimationHelper.hasAnimation("reload", stack) || ReloadHandler.get().getReloadProgress(partialTicks) < 1)
            RenderUtil.renderFirstPersonArm((LocalPlayer) player, hand.getOpposite(), poseStack, buffer, light);
        }
        poseStack.popPose();

        // Back arm holding the handle
        poseStack.pushPose();
        {
        	Vec3 posHand = PropertyHelper.getHandPosition(stack, gun, true);
        	
        	Vec3 translations = GunAnimationHelper.getSmartAnimationTrans(stack, player, partialTicks, "rearHand");
            Vec3 rotations = GunAnimationHelper.getSmartAnimationRot(stack, player, partialTicks, "rearHand");
        	if(!GunAnimationHelper.hasAnimation("fire", stack) && GunAnimationHelper.getSmartAnimationType(stack, player, partialTicks)=="fire")
        	{
        		translations = GunLegacyAnimationHelper.getHandTranslation(stack, true, cooldown);
        	}
            
            poseStack.translate(0, 0.1, -0.675);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate((-4.0 + posHand.x) * 0.0625 * side, (0 + posHand.y) * 0.0625, (0 - posHand.z) * 0.0625);
            //poseStack.translate(-4.0 * 0.0625 * side, 0, 0);

            String animType = GunAnimationHelper.getSmartAnimationType(stack, player, partialTicks);
            poseStack.translate(translations.x * side * 0.0625, translations.y * 0.0625, -translations.z * 0.0625);
            GunAnimationHelper.rotateAroundOffset(poseStack, rotations, animType, stack, "rearHand");
            
            poseStack.translate(-(armWidth / 2.0) * 0.0625 * side, 0, 0);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(80F));

            RenderUtil.renderFirstPersonArm((LocalPlayer) player, hand, poseStack, buffer, light);
        }
        poseStack.popPose();
    }

    @Override
    public boolean applyOffhandTransforms(Player player, PlayerModel model, ItemStack stack, PoseStack poseStack, float partialTicks)
    {
        return GripType.applyBackTransforms(player, poseStack);
    }
}
