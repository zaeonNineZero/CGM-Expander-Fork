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
import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.client.handler.AimingHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Author: MrCrayfish
 */
public class DynamicCrosshair extends Crosshair
{
    private static final ResourceLocation DYNAMIC_CROSSHAIR = new ResourceLocation(Reference.MOD_ID, "textures/crosshair/dynamic.png");

    private float scale;
    private float prevScale;

    public DynamicCrosshair()
    {
        super(new ResourceLocation(Reference.MOD_ID, "dynamic"));
    }

    @Override
    public void tick()
    {
        this.prevScale = this.scale;
        this.scale *= 0.6F;
    }

    @Override
    public void onGunFired()
    {
        this.prevScale = 1.0F;
        this.scale = 0.3F;
    }

    @Override
    public void render(Minecraft mc, PoseStack stack, int windowWidth, int windowHeight, float partialTicks)
    {
        float alpha = 1.0F;// - (float) AimingHandler.get().getNormalisedAdsProgress();
        float size1 = 5F;
        float size2 = 1F;
        float scale = 1F + Mth.lerp(partialTicks, this.prevScale, this.scale);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();

        //Left
        stack.pushPose();
        {
            Matrix4f matrix = stack.last().pose();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, DYNAMIC_CROSSHAIR);
            float sizeX = size1;
            float sizeY = size2;
            
            stack.translate(windowWidth / 2F, windowHeight / 2F, 0);
            stack.translate(-sizeX -1, -sizeY / 2F, 0);
            stack.translate(-scale, 0, 0);
            
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.vertex(matrix, 0, sizeY, 0).uv(0, 2F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, sizeY, 0).uv(10F/16F, 2F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, 0, 0).uv(10F/16F, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, 0, 0, 0).uv(0, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            BufferUploader.drawWithShader(buffer.end());
        }
        stack.popPose();
        //Right
        stack.pushPose();
        {
            Matrix4f matrix = stack.last().pose();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, DYNAMIC_CROSSHAIR);
            float sizeX = size1;
            float sizeY = size2;
            
            stack.translate(windowWidth / 2F, windowHeight / 2F, 0);
            stack.translate(1, -sizeY / 2F, 0);
            stack.translate(scale, 0, 0);
            
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.vertex(matrix, 0, sizeY, 0).uv(0, 5F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, sizeY, 0).uv(10F/16F, 5F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, 0, 0).uv(10F/16F, 3F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, 0, 0, 0).uv(0, 3F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            BufferUploader.drawWithShader(buffer.end());
        }
        stack.popPose();

        RenderSystem.defaultBlendFunc();
    }
}
