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
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.client.handler.AimingHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Author: MrCrayfish
 */
public class TexturedCrosshair extends Crosshair
{
    private ResourceLocation texture;
    private boolean blend;
    
    private float scale;
    private float prevScale;

    public TexturedCrosshair(ResourceLocation id)
    {
        this(id, true);
    }

    public TexturedCrosshair(ResourceLocation id, boolean blend)
    {
        super(id);
        this.texture = new ResourceLocation(id.getNamespace(), "textures/crosshair/" + id.getPath() + ".png");
        this.blend = blend;
    }

    @Override
    public void tick()
    {
        this.prevScale = this.scale;
        this.scale *= 0.5F;
    }

    @Override
    public void onGunFired()
    {
        this.prevScale = 0;
        this.scale = 0.3F;
    }

    @Override
    public void render(Minecraft mc, PoseStack stack, int windowWidth, int windowHeight, float partialTicks)
    {
        stack.pushPose();

        float alpha = 1.0F;// - (float) AimingHandler.get().getNormalisedAdsProgress();
        float size = 9.0F;
        float scale = 1F + Mth.lerp(partialTicks, this.prevScale, this.scale);
        stack.translate(Math.round((windowWidth) / 2F)-0.5, Math.round((windowHeight) / 2F)-0.5, 0);
        stack.scale(scale, scale, scale);
        stack.translate(-size / 2F, -size / 2F, 0);

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.texture);
        RenderSystem.enableBlend();
        
        boolean doBlend = this.blend && Config.CLIENT.display.blendCrosshair.get();

        if(doBlend)
        {
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }

        Matrix4f matrix = stack.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(matrix, 0, size, 0).uv(0, 1).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, size, size, 0).uv(1, 1).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, size, 0, 0).uv(1, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, 0, 0, 0).uv(0, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        BufferUploader.drawWithShader(buffer.end());

        if(doBlend)
        {
            RenderSystem.defaultBlendFunc();
        }

        stack.popPose();
    }
}
