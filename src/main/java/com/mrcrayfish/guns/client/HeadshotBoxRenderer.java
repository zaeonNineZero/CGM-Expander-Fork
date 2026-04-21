package com.mrcrayfish.guns.client;

import com.mrcrayfish.guns.common.BoundingBoxManager;
import com.mrcrayfish.guns.interfaces.IHeadshotBox;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class HeadshotBoxRenderer
{
	
	@SubscribeEvent
	public void onRenderLevelStage(RenderLevelStageEvent event)
	{
		if(event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
		{
			return;
		}
		
		Minecraft mc = Minecraft.getInstance();
		if(mc.level == null || !mc.getEntityRenderDispatcher().shouldRenderHitBoxes())
		{
			return;
		}
		
		PoseStack poseStack = event.getPoseStack();
		Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
		float partialTick = event.getPartialTick();
		
		for(Entity entity : mc.level.entitiesForRendering())
		{
			if(!(entity instanceof LivingEntity living))
			{
				continue;
			}
			
			if(living == mc.player && mc.options.getCameraType().isFirstPerson())
			{
				continue;
			}
			
			IHeadshotBox<LivingEntity> headshotBox = (IHeadshotBox<LivingEntity>) BoundingBoxManager.getHeadshotBoxes(living.getType());
			if(headshotBox == null)
			{
				continue;
			}
			
			AABB headBox = headshotBox.getHeadshotBox(living);
			if(headBox == null)
			{
				continue;
			}
			
			double x = Mth.lerp(partialTick, entity.xOld, entity.getX());
			double y = Mth.lerp(partialTick, entity.yOld, entity.getY());
			double z = Mth.lerp(partialTick, entity.zOld, entity.getZ());
			Vec3 interpolatedPos = new Vec3(x, y, z);
			
			headBox = headBox.move(interpolatedPos);
			renderHitbox(poseStack, headBox, cameraPos);
		}
	}
	
	private void renderHitbox(PoseStack poseStack, AABB aabb, Vec3 cameraPos)
	{
		double camX = cameraPos.x;
		double camY = cameraPos.y;
		double camZ = cameraPos.z;
		
		VertexConsumer vertexConsumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines());
		
		LevelRenderer.renderLineBox(poseStack, vertexConsumer, aabb.move(-camX, -camY, -camZ), 1.0F, 0.0F, 0.0F, 1.0F);
	}
}