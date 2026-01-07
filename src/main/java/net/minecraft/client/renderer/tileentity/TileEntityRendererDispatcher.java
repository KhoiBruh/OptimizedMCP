package net.minecraft.client.renderer.tileentity;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;
import net.optifine.EmissiveTextures;

import java.util.Map;

public class TileEntityRendererDispatcher {
    public static TileEntityRendererDispatcher instance = new TileEntityRendererDispatcher();
    public static double staticPlayerX;
    public static double staticPlayerY;
    public static double staticPlayerZ;
    public Map<Class, TileEntitySpecialRenderer> mapSpecialRenderers = Maps.newHashMap();
    public FontRenderer fontRenderer;
    public TextureManager renderEngine;
    public World worldObj;
    public Entity entity;
    public float entityYaw;
    public float entityPitch;
    public double entityX;
    public double entityY;
    public double entityZ;
    public TileEntity tileEntityRendered;
    private final Tessellator batchBuffer = new Tessellator(2097152);
    private boolean drawingBatch = false;

    private TileEntityRendererDispatcher() {
        mapSpecialRenderers.put(TileEntitySign.class, new TileEntitySignRenderer());
        mapSpecialRenderers.put(TileEntityMobSpawner.class, new TileEntityMobSpawnerRenderer());
        mapSpecialRenderers.put(TileEntityPiston.class, new TileEntityPistonRenderer());
        mapSpecialRenderers.put(TileEntityChest.class, new TileEntityChestRenderer());
        mapSpecialRenderers.put(TileEntityEnderChest.class, new TileEntityEnderChestRenderer());
        mapSpecialRenderers.put(TileEntityEnchantmentTable.class, new TileEntityEnchantmentTableRenderer());
        mapSpecialRenderers.put(TileEntityEndPortal.class, new TileEntityEndPortalRenderer());
        mapSpecialRenderers.put(TileEntityBeacon.class, new TileEntityBeaconRenderer());
        mapSpecialRenderers.put(TileEntitySkull.class, new TileEntitySkullRenderer());
        mapSpecialRenderers.put(TileEntityBanner.class, new TileEntityBannerRenderer());

        for (TileEntitySpecialRenderer<?> tileentityspecialrenderer : mapSpecialRenderers.values()) {
            tileentityspecialrenderer.setRendererDispatcher(this);
        }
    }

    public <T extends TileEntity> TileEntitySpecialRenderer<T> getSpecialRendererByClass(Class<? extends TileEntity> teClass) {
        TileEntitySpecialRenderer<? extends TileEntity> tileentityspecialrenderer = (TileEntitySpecialRenderer) mapSpecialRenderers.get(teClass);

        if (tileentityspecialrenderer == null && teClass != TileEntity.class) {
            tileentityspecialrenderer = getSpecialRendererByClass((Class<? extends TileEntity>) teClass.getSuperclass());
            mapSpecialRenderers.put(teClass, tileentityspecialrenderer);
        }

        return (TileEntitySpecialRenderer<T>) tileentityspecialrenderer;
    }

    public <T extends TileEntity> TileEntitySpecialRenderer<T> getSpecialRenderer(TileEntity tileEntityIn) {
        return tileEntityIn != null && !tileEntityIn.isInvalid() ? getSpecialRendererByClass(tileEntityIn.getClass()) : null;
    }

    public void cacheActiveRenderInfo(World worldIn, TextureManager textureManagerIn, FontRenderer fontrendererIn, Entity entityIn, float partialTicks) {
        if (worldObj != worldIn) {
            setWorld(worldIn);
        }

        renderEngine = textureManagerIn;
        entity = entityIn;
        fontRenderer = fontrendererIn;
        entityYaw = entityIn.prevRotationYaw + (entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks;
        entityPitch = entityIn.prevRotationPitch + (entityIn.rotationPitch - entityIn.prevRotationPitch) * partialTicks;
        entityX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
        entityY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
        entityZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;
    }

    public void renderTileEntity(TileEntity tileentityIn, float partialTicks, int destroyStage) {
        if (tileentityIn.getDistanceSq(entityX, entityY, entityZ) < tileentityIn.getMaxRenderDistanceSquared()) {
            boolean flag = drawingBatch;

            if (flag) {
                RenderHelper.enableStandardItemLighting();
                int i = worldObj.getCombinedLight(tileentityIn.getPos(), 0);
                int j = i % 65536;
                int k = i / 65536;
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }

            BlockPos blockpos = tileentityIn.getPos();

            if (!worldObj.isBlockLoaded(blockpos, false)) {
                return;
            }

            if (EmissiveTextures.isActive()) {
                EmissiveTextures.beginRender();
            }

            renderTileEntityAt(tileentityIn, (double) blockpos.getX() - staticPlayerX, (double) blockpos.getY() - staticPlayerY, (double) blockpos.getZ() - staticPlayerZ, partialTicks, destroyStage);

            if (EmissiveTextures.isActive()) {
                if (EmissiveTextures.hasEmissive()) {
                    EmissiveTextures.beginRenderEmissive();
                    renderTileEntityAt(tileentityIn, (double) blockpos.getX() - staticPlayerX, (double) blockpos.getY() - staticPlayerY, (double) blockpos.getZ() - staticPlayerZ, partialTicks, destroyStage);
                    EmissiveTextures.endRenderEmissive();
                }

                EmissiveTextures.endRender();
            }
        }
    }

    public void renderTileEntityAt(TileEntity tileEntityIn, double x, double y, double z, float partialTicks) {
        renderTileEntityAt(tileEntityIn, x, y, z, partialTicks, -1);
    }

    public void renderTileEntityAt(TileEntity tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage) {
        TileEntitySpecialRenderer<TileEntity> tileentityspecialrenderer = getSpecialRenderer(tileEntityIn);

        if (tileentityspecialrenderer != null) {
            try {
                tileEntityRendered = tileEntityIn;

                if (drawingBatch) {
                    tileentityspecialrenderer.renderTileEntityFast(tileEntityIn, x, y, z, partialTicks, destroyStage, batchBuffer.getWorldRenderer());
                } else {
                    tileentityspecialrenderer.renderTileEntityAt(tileEntityIn, x, y, z, partialTicks, destroyStage);
                }

                tileEntityRendered = null;
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Block Entity");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Block Entity Details");
                tileEntityIn.addInfoToCrashReport(crashreportcategory);
                throw new ReportedException(crashreport);
            }
        }
    }

    public void setWorld(World worldIn) {
        worldObj = worldIn;
    }

    public FontRenderer getFontRenderer() {
        return fontRenderer;
    }

    public void preDrawBatch() {
        batchBuffer.getWorldRenderer().begin(7, DefaultVertexFormats.BLOCK);
        drawingBatch = true;
    }

    public void drawBatch(int p_drawBatch_1_) {
        renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(7425);
        } else {
            GlStateManager.shadeModel(7424);
        }

        if (p_drawBatch_1_ > 0) {
            batchBuffer.getWorldRenderer().sortVertexData((float) staticPlayerX, (float) staticPlayerY, (float) staticPlayerZ);
        }

        batchBuffer.draw();
        RenderHelper.enableStandardItemLighting();
        drawingBatch = false;
    }
}
