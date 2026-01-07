package net.optifine.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

public class CloudRenderer {
    int cloudTickCounter;
    float partialTicks;
    private final Minecraft mc;
    private boolean updated = false;
    private boolean renderFancy = false;
    private Vec3 cloudColor;
    private boolean updateRenderFancy = false;
    private int updateCloudTickCounter = 0;
    private Vec3 updateCloudColor = new Vec3(-1.0D, -1.0D, -1.0D);
    private double updatePlayerX = 0.0D;
    private double updatePlayerY = 0.0D;
    private double updatePlayerZ = 0.0D;
    private final int glListClouds;

    public CloudRenderer(Minecraft mc) {
        this.mc = mc;
        glListClouds = GLAllocation.generateDisplayLists(1);
    }

    public void prepareToRender(boolean renderFancy, int cloudTickCounter, float partialTicks, Vec3 cloudColor) {
        this.renderFancy = renderFancy;
        this.cloudTickCounter = cloudTickCounter;
        this.partialTicks = partialTicks;
        this.cloudColor = cloudColor;
    }

    public boolean shouldUpdateGlList() {
        if (!updated) {
            return true;
        } else if (renderFancy != updateRenderFancy) {
            return true;
        } else if (cloudTickCounter >= updateCloudTickCounter + 20) {
            return true;
        } else if (Math.abs(cloudColor.xCoord() - updateCloudColor.xCoord()) > 0.003D) {
            return true;
        } else if (Math.abs(cloudColor.yCoord() - updateCloudColor.yCoord()) > 0.003D) {
            return true;
        } else if (Math.abs(cloudColor.zCoord() - updateCloudColor.zCoord()) > 0.003D) {
            return true;
        } else {
            Entity entity = mc.getRenderViewEntity();
            boolean flag = updatePlayerY + (double) entity.getEyeHeight() < 128.0D + (double) (mc.gameSettings.ofCloudsHeight * 128.0F);
            boolean flag1 = entity.prevPosY + (double) entity.getEyeHeight() < 128.0D + (double) (mc.gameSettings.ofCloudsHeight * 128.0F);
            return flag1 != flag;
        }
    }

    public void startUpdateGlList() {
        GL11.glNewList(glListClouds, GL11.GL_COMPILE);
    }

    public void endUpdateGlList() {
        GL11.glEndList();
        updateRenderFancy = renderFancy;
        updateCloudTickCounter = cloudTickCounter;
        updateCloudColor = cloudColor;
        updatePlayerX = mc.getRenderViewEntity().prevPosX;
        updatePlayerY = mc.getRenderViewEntity().prevPosY;
        updatePlayerZ = mc.getRenderViewEntity().prevPosZ;
        updated = true;
        GlStateManager.resetColor();
    }

    public void renderGlList() {
        Entity entity = mc.getRenderViewEntity();
        double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks;
        double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks;
        double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks;
        double d3 = (float) (cloudTickCounter - updateCloudTickCounter) + partialTicks;
        float f = (float) (d0 - updatePlayerX + d3 * 0.03D);
        float f1 = (float) (d1 - updatePlayerY);
        float f2 = (float) (d2 - updatePlayerZ);
        GlStateManager.pushMatrix();

        if (renderFancy) {
            GlStateManager.translate(-f / 12.0F, -f1, -f2 / 12.0F);
        } else {
            GlStateManager.translate(-f, -f1, -f2);
        }

        GlStateManager.callList(glListClouds);
        GlStateManager.popMatrix();
        GlStateManager.resetColor();
    }

    public void reset() {
        updated = false;
    }
}
