package com.mrcrayfish.guns.client.render.pose;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.client.handler.ReloadHandler;
import com.mrcrayfish.guns.client.util.GunAnimationHelper;
import com.mrcrayfish.guns.client.util.PropertyHelper;
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
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Author: MrCrayfish
 */
public class TwoHandedShortPose extends WeaponPose
{
    @Override
    protected AimPose getUpPose()
    {
        AimPose upPose = new AimPose();
        upPose.getIdle().setRenderYawOffset(30F).setItemRotation(new Vector3f(60F, 0F, 10F)).setRightArm(new LimbPose().setRotationAngleX(-120F).setRotationAngleY(-40F).setRotationPointX(-5).setRotationPointY(3).setRotationPointZ(0)).setLeftArm(new LimbPose().setRotationAngleX(-135F).setRotationAngleY(30F).setRotationAngleZ(-25F).setRotationPointX(-10).setRotationPointY(0).setRotationPointZ(-1));
        upPose.getAiming().setRenderYawOffset(35F).setItemRotation(new Vector3f(40F, 0F, 30F)).setItemTranslate(new Vector3f(-1, 0, 0)).setRightArm(new LimbPose().setRotationAngleX(-140F).setRotationAngleY(-45F).setRotationPointX(-5).setRotationPointY(3).setRotationPointZ(0)).setLeftArm(new LimbPose().setRotationAngleX(-145F).setRotationAngleY(40F).setRotationAngleZ(-30F).setRotationPointX(-10).setRotationPointY(-1).setRotationPointZ(0));
        return upPose;
    }

    @Override
    protected AimPose getForwardPose()
    {
        AimPose forwardPose = new AimPose();
        forwardPose.getIdle().setRenderYawOffset(35F).setItemRotation(new Vector3f(30F, -11F, 0F)).setRightArm(new LimbPose().setRotationAngleX(-60F).setRotationAngleY(-45F).setRotationAngleZ(0F).setRotationPointX(-5).setRotationPointY(2).setRotationPointZ(1)).setLeftArm(new LimbPose().setRotationAngleX(-63F).setRotationAngleY(10F).setRotationAngleZ(5F).setRotationPointY(2).setRotationPointZ(-1));
        forwardPose.getAiming().setRenderYawOffset(40F).setItemRotation(new Vector3f(5F, -16F, 0F)).setRightArm(new LimbPose().setRotationAngleX(-85F).setRotationAngleY(-55F).setRotationAngleZ(0F).setRotationPointX(-5).setRotationPointY(2)).setLeftArm(new LimbPose().setRotationAngleX(-87F).setRotationAngleY(10F).setRotationAngleZ(5F).setRotationPointY(2).setRotationPointZ(0));
        return forwardPose;
    }

    @Override
    protected AimPose getDownPose()
    {
        AimPose downPose = new AimPose();
        downPose.getIdle().setRenderYawOffset(40F).setItemRotation(new Vector3f(-15F, -5F, 0F)).setItemTranslate(new Vector3f(0, -0.5F, 0.5F)).setRightArm(new LimbPose().setRotationAngleX(-30F).setRotationAngleY(-65F).setRotationAngleZ(0F).setRotationPointX(-5).setRotationPointY(2)).setLeftArm(new LimbPose().setRotationAngleX(-5F).setRotationAngleY(-25F).setRotationAngleZ(25F).setRotationPointY(5).setRotationPointZ(0));
        downPose.getAiming().setRenderYawOffset(45F).setItemRotation(new Vector3f(-20F, -5F, -10F)).setItemTranslate(new Vector3f(0, -0.5F, 1F)).setRightArm(new LimbPose().setRotationAngleX(-30F).setRotationAngleY(-65F).setRotationAngleZ(0F).setRotationPointX(-5).setRotationPointY(1)).setLeftArm(new LimbPose().setRotationAngleX(-10F).setRotationAngleY(-30F).setRotationAngleZ(35F).setRotationPointY(5).setRotationPointZ(0));
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
        
        ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
        float cooldown = tracker.getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());

        // Front arm holding the barrel
        poseStack.pushPose();
        {
        	Vec3 posHand = PropertyHelper.getHandPosition(stack, gun, false);
            double xOffset = (posHand != null ? posHand.x : 0);
            double yOffset = (posHand != null ? posHand.y : 0);
            double zOffset = (posHand != null ? posHand.z : 0);
        	float reloadProgress = ReloadHandler.get().getReloadProgress(partialTicks);
            poseStack.translate(reloadProgress * 0.5, -reloadProgress, -reloadProgress * 0.5);

            Vec3 animate = GunAnimationHelper.getHandTranslation(stack, false, cooldown);

            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate((1.55 + xOffset + animate.x) * 0.0625 * side, (0.6 + yOffset + animate.y) * 0.0625, (-3.5 - zOffset - animate.z) * 0.0625);
            //poseStack.translate((1.55) * 0.0625 * side, (0.4) * 0.0625, (-3.5) * 0.0625);
            poseStack.translate((armWidth / 2.0) * 0.0625 * side, 0, 0);
            poseStack.translate(-0.3125 * side, -0.1, -0.4375);

            poseStack.mulPose(Vector3f.XP.rotationDegrees(80F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(10F * -side));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(35F * -side));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-35F));

            RenderUtil.renderFirstPersonArm((LocalPlayer) player, hand.getOpposite(), poseStack, buffer, light);
        }
        poseStack.popPose();

        // Back arm holding the handle
        poseStack.pushPose();
        {
        	Vec3 posHand = PropertyHelper.getHandPosition(stack, gun, true);
            double xOffset = (posHand != null ? posHand.x : 0);
            double yOffset = (posHand != null ? posHand.y : 0);
            double zOffset = (posHand != null ? posHand.z : 0);
            
            Vec3 animate = GunAnimationHelper.getHandTranslation(stack, true, cooldown);
            
            poseStack.translate(0, 0.1, -0.675);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate((-4.0 + xOffset + animate.x) * 0.0625 * side, (0 + yOffset + animate.y) * 0.0625, (0 - zOffset - animate.z) * 0.0625);
            //poseStack.translate((-4.0) * 0.0625 * side, (0) * 0.0625, (0) * 0.0625);
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
