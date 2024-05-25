package com.mrcrayfish.guns.client.render.pose;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.client.handler.ReloadHandler;
import com.mrcrayfish.guns.client.util.RenderUtil;
import com.mrcrayfish.guns.common.GripType;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.common.Gun.Display.*;
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
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Author: MrCrayfish
 */
public class PistolCustomPose extends WeaponPose
{
    @Override
    protected AimPose getUpPose()
    {
        AimPose upPose = new AimPose();
        upPose.getIdle().setRenderYawOffset(20F).setItemRotation(new Vector3f(17.5F, 0F, 10F)).setItemTranslate(new Vector3f(0, 0, -1)).setRightArm(new LimbPose().setRotationAngleX(-140F).setRotationAngleY(-40F).setRotationPointX(-5).setRotationPointY(3).setRotationPointZ(0)).setLeftArm(new LimbPose().setRotationAngleX(-160F).setRotationAngleY(15F).setRotationAngleZ(-30F).setRotationPointY(2).setRotationPointZ(-1));
        upPose.getAiming().setRenderYawOffset(20F).setItemRotation(new Vector3f(-2.5F, 0F, 20F)).setItemTranslate(new Vector3f(-1, 0, -1)).setRightArm(new LimbPose().setRotationAngleX(-160F).setRotationAngleY(-45F).setRotationPointX(-5).setRotationPointY(3).setRotationPointZ(0)).setLeftArm(new LimbPose().setRotationAngleX(-170F).setRotationAngleY(15F).setRotationAngleZ(-35F).setRotationPointY(1).setRotationPointZ(0));
        return upPose;
    }

    @Override
    protected AimPose getForwardPose()
    {
        AimPose forwardPose = new AimPose();
        forwardPose.getIdle().setRenderYawOffset(20F).setItemRotation(new Vector3f(-7.5F, -12.5F, -5F)).setItemTranslate(new Vector3f(0.4F, 0, -1)).setRightArm(new LimbPose().setRotationAngleX(-75F).setRotationAngleY(-35F).setRotationAngleZ(0F).setRotationPointX(-5).setRotationPointY(2).setRotationPointZ(1)).setLeftArm(new LimbPose().setRotationAngleX(-67F).setRotationAngleY(40F).setRotationAngleZ(5F).setRotationPointY(2).setRotationPointZ(-1));
        forwardPose.getAiming().setRenderYawOffset(20F).setItemRotation(new Vector3f(-25F, -12.5F, -5.5F)).setItemTranslate(new Vector3f(0.4F, 0, -1)).setRightArm(new LimbPose().setRotationAngleX(-92F).setRotationAngleY(-35F).setRotationAngleZ(0F).setRotationPointX(-5).setRotationPointY(2)).setLeftArm(new LimbPose().setRotationAngleX(-93F).setRotationAngleY(40F).setRotationAngleZ(5F).setRotationPointY(2).setRotationPointZ(0));
        return forwardPose;
    }

    @Override
    protected AimPose getDownPose()
    {
        AimPose downPose = new AimPose();
        downPose.getIdle().setRenderYawOffset(20F).setItemRotation(new Vector3f(-37.5F, -5F, 0F)).setItemTranslate(new Vector3f(0, -0.5F, -1.5F)).setRightArm(new LimbPose().setRotationAngleX(-30F).setRotationAngleY(-65F).setRotationAngleZ(0F).setRotationPointX(-5).setRotationPointY(2)).setLeftArm(new LimbPose().setRotationAngleX(-5F).setRotationAngleY(-20F).setRotationAngleZ(25F).setRotationPointY(5).setRotationPointZ(0));
        downPose.getAiming().setRenderYawOffset(20F).setItemRotation(new Vector3f(-42.5F, -5F, -10F)).setItemTranslate(new Vector3f(0, -0.5F, -1F)).setRightArm(new LimbPose().setRotationAngleX(-30F).setRotationAngleY(-65F).setRotationAngleZ(0F).setRotationPointX(-5).setRotationPointY(1)).setLeftArm(new LimbPose().setRotationAngleX(-10F).setRotationAngleY(-25F).setRotationAngleZ(35F).setRotationPointY(5).setRotationPointZ(0));
        return downPose;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyPlayerModelRotation(Player player, ModelPart rightArm, ModelPart leftArm, ModelPart head, InteractionHand hand, float aimProgress)
    {
        super.applyPlayerModelRotation(player, rightArm, leftArm, head, hand, aimProgress);
        float angle = this.getPlayerPitch(player);
        head.xRot = (float) Math.toRadians(angle > 0.0 ? angle * 70F : angle * 90F);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyPlayerPreRender(Player player, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer)
    {
       super.applyPlayerPreRender(player, hand, aimProgress, poseStack, buffer);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyHeldItemTransforms(Player player, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer)
    {
        super.applyHeldItemTransforms(player, hand, aimProgress, poseStack, buffer);
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

        // Front arm holding the barrel
        poseStack.pushPose();
        {
            ForwardHandPos posHand = gun.getDisplay().getForwardHand();
            double xOffset = (posHand != null ? posHand.getXOffset() : 0);
            double yOffset = (posHand != null ? posHand.getYOffset() : 0);
            double zOffset = (posHand != null ? posHand.getZOffset() : 0);
        	float reloadProgress = ReloadHandler.get().getReloadProgress(partialTicks);
            poseStack.translate(reloadProgress * 0.5, -reloadProgress, -reloadProgress * 0.5);

            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate((2.9 + xOffset) * 0.0625 * side, (2.2 + yOffset) * 0.0625, (-11.2 + zOffset) * 0.0625);
            //poseStack.translate((1.55) * 0.0625 * side, (0.4) * 0.0625, (-3.5) * 0.0625);
            poseStack.translate((armWidth / 2.0) * 0.0625 * side, 0, 0);
            poseStack.translate(-0.3125 * side, -0.1, -0.4375);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(75F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(25F * -side));

            RenderUtil.renderFirstPersonArm((LocalPlayer) player, hand.getOpposite(), poseStack, buffer, light);
        }
        poseStack.popPose();

        // Back arm holding the handle
        poseStack.pushPose();
        {
            RearHandPos posHand = gun.getDisplay().getRearHand();
            double xOffset = (posHand != null ? posHand.getXOffset() : 0);
            double yOffset = (posHand != null ? posHand.getYOffset() : 0);
            double zOffset = (posHand != null ? posHand.getZOffset() : 0);
            poseStack.translate(0, 0.1, -0.675);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate((-1.7 + xOffset) * 0.0625 * side, (0 + yOffset) * 0.0625, (3.2 + zOffset) * 0.0625);
            //poseStack.translate((-4.0) * 0.0625 * side, (0) * 0.0625, (0) * 0.0625);
            poseStack.translate(-(armWidth / 2.0) * 0.0625 * side, 0, 0);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(80F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(12F * side));

            RenderUtil.renderFirstPersonArm((LocalPlayer) player, hand, poseStack, buffer, light);
        }
        poseStack.popPose();
    }

    @Override
    public boolean applyOffhandTransforms(Player player, PlayerModel model, ItemStack stack, PoseStack poseStack, float partialTicks)
    {
        return GripType.applyBackTransforms(player, poseStack);
    }

    @Override
    public boolean canRenderOffhandItem()
    {
        return true;
    }

    @Override
    public double getFallSwayZOffset()
    {
        return 0.5;
    }
}
