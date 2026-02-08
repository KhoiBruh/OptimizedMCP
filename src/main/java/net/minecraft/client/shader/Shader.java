package net.minecraft.client.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import org.joml.Matrix4f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Shader {
    public final Framebuffer framebufferIn;
    public final Framebuffer framebufferOut;
    private final ShaderManager manager;
    private final List<Object> listAuxFramebuffers = new ArrayList<>();
    private final List<String> listAuxNames = new ArrayList<>();
    private final List<Integer> listAuxWidths = new ArrayList<>();
    private final List<Integer> listAuxHeights = new ArrayList<>();
    private Matrix4f projectionMatrix;

    public Shader(IResourceManager p_i45089_1_, String p_i45089_2_, Framebuffer p_i45089_3_, Framebuffer p_i45089_4_) throws IOException {
        manager = new ShaderManager(p_i45089_1_, p_i45089_2_);
        framebufferIn = p_i45089_3_;
        framebufferOut = p_i45089_4_;
    }

    public void deleteShader() {
        manager.deleteShader();
    }

    public void addAuxFramebuffer(String p_148041_1_, Object p_148041_2_, int p_148041_3_, int p_148041_4_) {
        listAuxNames.add(listAuxNames.size(), p_148041_1_);
        listAuxFramebuffers.add(listAuxFramebuffers.size(), p_148041_2_);
        listAuxWidths.add(listAuxWidths.size(), p_148041_3_);
        listAuxHeights.add(listAuxHeights.size(), p_148041_4_);
    }

    private void preLoadShader() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableAlpha();
        GlStateManager.disableFog();
        GlStateManager.disableLighting();
        GlStateManager.disableColorMaterial();
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(0);
    }

    public void setProjectionMatrix(Matrix4f p_148045_1_) {
        projectionMatrix = p_148045_1_;
    }

    public void loadShader(float p_148042_1_) {
        preLoadShader();
        framebufferIn.unbindFramebuffer();
        float f = (float) framebufferOut.framebufferTextureWidth;
        float f1 = (float) framebufferOut.framebufferTextureHeight;
        GlStateManager.viewport(0, 0, (int) f, (int) f1);
        manager.addSamplerTexture("DiffuseSampler", framebufferIn);

        for (int i = 0; i < listAuxFramebuffers.size(); ++i) {
            manager.addSamplerTexture(listAuxNames.get(i), listAuxFramebuffers.get(i));
            manager.getShaderUniformOrDefault("AuxSize" + i).set((float) listAuxWidths.get(i), (float) listAuxHeights.get(i));
        }

        manager.getShaderUniformOrDefault("ProjMat").set(projectionMatrix);
        manager.getShaderUniformOrDefault("InSize").set((float) framebufferIn.framebufferTextureWidth, (float) framebufferIn.framebufferTextureHeight);
        manager.getShaderUniformOrDefault("OutSize").set(f, f1);
        manager.getShaderUniformOrDefault("Time").set(p_148042_1_);
        Minecraft minecraft = Minecraft.getMinecraft();
        manager.getShaderUniformOrDefault("ScreenSize").set((float) minecraft.displayWidth, (float) minecraft.displayHeight);
        manager.useShader();
        framebufferOut.framebufferClear();
        framebufferOut.bindFramebuffer(false);
        GlStateManager.depthMask(false);
        GlStateManager.colorMask(true, true, true, true);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(0.0D, f1, 500.0D).color(255, 255, 255, 255).endVertex();
        worldrenderer.pos(f, f1, 500.0D).color(255, 255, 255, 255).endVertex();
        worldrenderer.pos(f, 0.0D, 500.0D).color(255, 255, 255, 255).endVertex();
        worldrenderer.pos(0.0D, 0.0D, 500.0D).color(255, 255, 255, 255).endVertex();
        tessellator.draw();
        GlStateManager.depthMask(true);
        GlStateManager.colorMask(true, true, true, true);
        manager.endShader();
        framebufferOut.unbindFramebuffer();
        framebufferIn.unbindFramebufferTexture();

        for (Object object : listAuxFramebuffers) {
            if (object instanceof Framebuffer) {
                ((Framebuffer) object).unbindFramebufferTexture();
            }
        }
    }

    public ShaderManager getShaderManager() {
        return manager;
    }
}
