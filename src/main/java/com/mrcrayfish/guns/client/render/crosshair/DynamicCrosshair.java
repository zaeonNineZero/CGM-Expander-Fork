package com.mrcrayfish.guns.client.render.crosshair;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.client.handler.AimingHandler;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.common.SpreadTracker;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import com.mrcrayfish.guns.item.GunItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Author: MrCrayfish
 */
public class DynamicCrosshair extends Crosshair
{
    private static final ResourceLocation DYNAMIC_CROSSHAIR = new ResourceLocation(Reference.MOD_ID, "textures/crosshair/dynamic.png");

    private float scale;
    private float prevScale;
    private float fireBloom;
    private float prevFireBloom;

    public DynamicCrosshair()
    {
        super(new ResourceLocation(Reference.MOD_ID, "dynamic"));
    }

    @Override
    public void tick()
    {
        this.prevScale = this.scale;
        this.scale *= 0.5F;
        this.prevFireBloom = this.fireBloom;
        if (this.fireBloom > 0)
        {
        	float i = (float) Config.COMMON.projectileSpread.spreadThreshold.get()/20;
        	this.fireBloom -= Math.min(4F/(Math.max(i,1)), this.fireBloom);
        }
    }

    @Override
    public void onGunFired()
    {
        this.prevScale = 0.0F;
        this.scale = 0.6F;
        this.fireBloom = 4.0F;
    }

    @Override
    public void render(Minecraft mc, PoseStack stack, int windowWidth, int windowHeight, float partialTicks)
    {
        float alpha = 1.0F;// - (float) AimingHandler.get().getNormalisedAdsProgress();
        float size1 = 5F;
        float size2 = 1F;
        float spread = 0F;
        if (mc.player != null)
        {
        	ItemStack heldItem = mc.player.getMainHandItem();
            if((heldItem.getItem() instanceof GunItem gunItem))
            {
            	GunItem gun = (GunItem) heldItem.getItem();
            	Gun modifiedGun = gun.getModifiedGun(heldItem);
            	float aiming = 1.0F - (float) AimingHandler.get().getNormalisedAdsProgress();
            	float spreadModifier = ((SpreadTracker.get(mc.player).getSpread(gun)+(1F/Math.max(Config.COMMON.projectileSpread.maxCount.get(),1F)))*Math.min(Mth.lerp(partialTicks, this.prevFireBloom, this.fireBloom),1F));
            	float baseSpread = modifiedGun.getGeneral().getSpread();
            	float minSpread = modifiedGun.getGeneral().getRestingSpread();
            	minSpread = (minSpread > 0F ? minSpread : (modifiedGun.getGeneral().isAlwaysSpread() ? baseSpread : 0));
            	spread = Mth.lerp(spreadModifier,minSpread,baseSpread)*(aiming * modifiedGun.getGeneral().getSpreadAdsReduction());
            }
        }
        
        float baseScale = 1F + Mth.lerp(partialTicks, this.prevScale, this.scale);
        float scale = baseScale + (spread);
        float scaleSize = (scale/4F) + 0.25F;

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();

        // Left
        stack.pushPose();
        {
            Matrix4f matrix = stack.last().pose();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, DYNAMIC_CROSSHAIR);
            float sizeX = size1;
            float sizeY = size2;
            
            stack.translate(windowWidth / 2F -0.5, windowHeight / 2F, 0);
            stack.translate(-sizeX + 0.2F, -sizeY / 2F, 0);
            stack.translate(-scale*2, 0, 0);
            
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.vertex(matrix, 0, sizeY, 0).uv(0, 2F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, sizeY, 0).uv(10F/16F, 2F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, 0, 0).uv(10F/16F, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, 0, 0, 0).uv(0, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            BufferUploader.drawWithShader(buffer.end());
        }
        stack.popPose();
        
        // Right
        stack.pushPose();
        {
            Matrix4f matrix = stack.last().pose();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, DYNAMIC_CROSSHAIR);
            float sizeX = size1;
            float sizeY = size2;
            
            stack.translate(windowWidth / 2F -0.5, windowHeight / 2F, 0);
            stack.translate(-0.2F, -sizeY / 2F, 0);
            stack.translate(scale*2, 0, 0);
            
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.vertex(matrix, 0, sizeY, 0).uv(0, 5F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, sizeY, 0).uv(10F/16F, 5F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, 0, 0).uv(10F/16F, 3F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, 0, 0, 0).uv(0, 3F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            BufferUploader.drawWithShader(buffer.end());
        }
        stack.popPose();
        
        // Top
        stack.pushPose();
        {
            Matrix4f matrix = stack.last().pose();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, DYNAMIC_CROSSHAIR);
            float sizeX = size2;
            float sizeY = size1;
            
            stack.translate(windowWidth / 2F, windowHeight / 2F, 0);
            stack.translate(-sizeX, -sizeY + 0.2F, 0);
            stack.translate(0, -scale*2, 0);
            
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.vertex(matrix, 0, sizeY, 0).uv(13F/16F, 10F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, sizeY, 0).uv(11F/16F, 10F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, 0, 0).uv(13F/16F, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, 0, 0, 0).uv(11F/16F, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            BufferUploader.drawWithShader(buffer.end());
        }
        stack.popPose();
        
        // Bottom
        stack.pushPose();
        {
            Matrix4f matrix = stack.last().pose();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, DYNAMIC_CROSSHAIR);
            float sizeX = size2;
            float sizeY = size1;
            
            stack.translate(windowWidth / 2F, windowHeight / 2F, 0);
            stack.translate(-sizeX, -0.2F, 0);
            stack.translate(0, scale*2, 0);
            
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.vertex(matrix, 0, sizeY, 0).uv(14F/16F, 10F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, sizeY, 0).uv(1, 10F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, 0, 0).uv(1, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, 0, 0, 0).uv(14F/16F, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            BufferUploader.drawWithShader(buffer.end());
        }
        stack.popPose();

        
        RenderSystem.defaultBlendFunc();
    }
}
