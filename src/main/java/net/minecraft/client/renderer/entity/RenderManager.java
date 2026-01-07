package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.RenderEnderCrystal;
import net.minecraft.client.renderer.tileentity.RenderItemFrame;
import net.minecraft.client.renderer.tileentity.RenderWitherSkull;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityMinecartMobSpawner;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.projectile.*;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.optifine.entity.model.CustomEntityModels;
import net.optifine.player.PlayerItemsLayer;
import net.optifine.shaders.Shaders;

import java.util.Collections;
import java.util.Map;

public class RenderManager {
    public TextureManager renderEngine;
    public World worldObj;
    public Entity livingPlayer;
    public Entity pointedEntity;
    public float playerViewY;
    public float playerViewX;
    public GameSettings options;
    public double viewerPosX;
    public double viewerPosY;
    public double viewerPosZ;
    public Render renderRender = null;
    private Map<Class<? extends Entity>, Render<? extends Entity>> entityRenderMap = Maps.newHashMap();
    private final Map<String, RenderPlayer> skinMap = Maps.newHashMap();
    private final RenderPlayer playerRenderer;
    private FontRenderer textRenderer;
    private double renderPosX;
    private double renderPosY;
    private double renderPosZ;
    private boolean renderOutlines = false;
    private boolean renderShadow = true;
    private boolean debugBoundingBox = false;

    public RenderManager(TextureManager renderEngineIn, RenderItem itemRendererIn) {
        renderEngine = renderEngineIn;
        entityRenderMap.put(EntityCaveSpider.class, new RenderCaveSpider(this));
        entityRenderMap.put(EntitySpider.class, new RenderSpider<>(this));
        entityRenderMap.put(EntityPig.class, new RenderPig(this, new ModelPig(), 0.7F));
        entityRenderMap.put(EntitySheep.class, new RenderSheep(this, new ModelSheep2(), 0.7F));
        entityRenderMap.put(EntityCow.class, new RenderCow(this, new ModelCow(), 0.7F));
        entityRenderMap.put(EntityMooshroom.class, new RenderMooshroom(this, new ModelCow(), 0.7F));
        entityRenderMap.put(EntityWolf.class, new RenderWolf(this, new ModelWolf(), 0.5F));
        entityRenderMap.put(EntityChicken.class, new RenderChicken(this, new ModelChicken(), 0.3F));
        entityRenderMap.put(EntityOcelot.class, new RenderOcelot(this, new ModelOcelot(), 0.4F));
        entityRenderMap.put(EntityRabbit.class, new RenderRabbit(this, new ModelRabbit(), 0.3F));
        entityRenderMap.put(EntitySilverfish.class, new RenderSilverfish(this));
        entityRenderMap.put(EntityEndermite.class, new RenderEndermite(this));
        entityRenderMap.put(EntityCreeper.class, new RenderCreeper(this));
        entityRenderMap.put(EntityEnderman.class, new RenderEnderman(this));
        entityRenderMap.put(EntitySnowman.class, new RenderSnowMan(this));
        entityRenderMap.put(EntitySkeleton.class, new RenderSkeleton(this));
        entityRenderMap.put(EntityWitch.class, new RenderWitch(this));
        entityRenderMap.put(EntityBlaze.class, new RenderBlaze(this));
        entityRenderMap.put(EntityPigZombie.class, new RenderPigZombie(this));
        entityRenderMap.put(EntityZombie.class, new RenderZombie(this));
        entityRenderMap.put(EntitySlime.class, new RenderSlime(this, new ModelSlime(16), 0.25F));
        entityRenderMap.put(EntityMagmaCube.class, new RenderMagmaCube(this));
        entityRenderMap.put(EntityGiantZombie.class, new RenderGiantZombie(this, new ModelZombie(), 0.5F, 6.0F));
        entityRenderMap.put(EntityGhast.class, new RenderGhast(this));
        entityRenderMap.put(EntitySquid.class, new RenderSquid(this, new ModelSquid(), 0.7F));
        entityRenderMap.put(EntityVillager.class, new RenderVillager(this));
        entityRenderMap.put(EntityIronGolem.class, new RenderIronGolem(this));
        entityRenderMap.put(EntityBat.class, new RenderBat(this));
        entityRenderMap.put(EntityGuardian.class, new RenderGuardian(this));
        entityRenderMap.put(EntityDragon.class, new RenderDragon(this));
        entityRenderMap.put(EntityEnderCrystal.class, new RenderEnderCrystal(this));
        entityRenderMap.put(EntityWither.class, new RenderWither(this));
        entityRenderMap.put(Entity.class, new RenderEntity(this));
        entityRenderMap.put(EntityPainting.class, new RenderPainting(this));
        entityRenderMap.put(EntityItemFrame.class, new RenderItemFrame(this, itemRendererIn));
        entityRenderMap.put(EntityLeashKnot.class, new RenderLeashKnot(this));
        entityRenderMap.put(EntityArrow.class, new RenderArrow(this));
        entityRenderMap.put(EntitySnowball.class, new RenderSnowball<>(this, Items.snowball, itemRendererIn));
        entityRenderMap.put(EntityEnderPearl.class, new RenderSnowball<>(this, Items.ender_pearl, itemRendererIn));
        entityRenderMap.put(EntityEnderEye.class, new RenderSnowball<>(this, Items.ender_eye, itemRendererIn));
        entityRenderMap.put(EntityEgg.class, new RenderSnowball<>(this, Items.egg, itemRendererIn));
        entityRenderMap.put(EntityPotion.class, new RenderPotion(this, itemRendererIn));
        entityRenderMap.put(EntityExpBottle.class, new RenderSnowball<>(this, Items.experience_bottle, itemRendererIn));
        entityRenderMap.put(EntityFireworkRocket.class, new RenderSnowball<>(this, Items.fireworks, itemRendererIn));
        entityRenderMap.put(EntityLargeFireball.class, new RenderFireball(this, 2.0F));
        entityRenderMap.put(EntitySmallFireball.class, new RenderFireball(this, 0.5F));
        entityRenderMap.put(EntityWitherSkull.class, new RenderWitherSkull(this));
        entityRenderMap.put(EntityItem.class, new RenderEntityItem(this, itemRendererIn));
        entityRenderMap.put(EntityXPOrb.class, new RenderXPOrb(this));
        entityRenderMap.put(EntityTNTPrimed.class, new RenderTNTPrimed(this));
        entityRenderMap.put(EntityFallingBlock.class, new RenderFallingBlock(this));
        entityRenderMap.put(EntityArmorStand.class, new ArmorStandRenderer(this));
        entityRenderMap.put(EntityMinecartTNT.class, new RenderTntMinecart(this));
        entityRenderMap.put(EntityMinecartMobSpawner.class, new RenderMinecartMobSpawner(this));
        entityRenderMap.put(EntityMinecart.class, new RenderMinecart<>(this));
        entityRenderMap.put(EntityBoat.class, new RenderBoat(this));
        entityRenderMap.put(EntityFishHook.class, new RenderFish(this));
        entityRenderMap.put(EntityHorse.class, new RenderHorse(this, new ModelHorse(), 0.75F));
        entityRenderMap.put(EntityLightningBolt.class, new RenderLightningBolt(this));
        playerRenderer = new RenderPlayer(this);
        skinMap.put("default", playerRenderer);
        skinMap.put("slim", new RenderPlayer(this, true));
        PlayerItemsLayer.register(skinMap);
    }

    public void setRenderPosition(double renderPosXIn, double renderPosYIn, double renderPosZIn) {
        renderPosX = renderPosXIn;
        renderPosY = renderPosYIn;
        renderPosZ = renderPosZIn;
    }

    public <T extends Entity> Render<T> getEntityClassRenderObject(Class<? extends Entity> entityClass) {
        Render<? extends Entity> render = (Render) entityRenderMap.get(entityClass);

        if (render == null && entityClass != Entity.class) {
            render = getEntityClassRenderObject((Class<? extends Entity>) entityClass.getSuperclass());
            entityRenderMap.put(entityClass, render);
        }

        return (Render<T>) render;
    }

    public <T extends Entity> Render<T> getEntityRenderObject(Entity entityIn) {
        if (entityIn instanceof AbstractClientPlayer) {
            String s = ((AbstractClientPlayer) entityIn).getSkinType();
            RenderPlayer renderplayer = skinMap.get(s);
            return (Render<T>) (renderplayer != null ? renderplayer : playerRenderer);
        } else {
            return getEntityClassRenderObject(entityIn.getClass());
        }
    }

    public void cacheActiveRenderInfo(World worldIn, FontRenderer textRendererIn, Entity livingPlayerIn, Entity pointedEntityIn, GameSettings optionsIn, float partialTicks) {
        worldObj = worldIn;
        options = optionsIn;
        livingPlayer = livingPlayerIn;
        pointedEntity = pointedEntityIn;
        textRenderer = textRendererIn;

        if (livingPlayerIn instanceof EntityLivingBase && ((EntityLivingBase) livingPlayerIn).isPlayerSleeping()) {
            IBlockState iblockstate = worldIn.getBlockState(new BlockPos(livingPlayerIn));
            Block block = iblockstate.getBlock();

            if (block == Blocks.bed) {
                int j = iblockstate.getValue(BlockBed.FACING).getHorizontalIndex();
                playerViewY = (float) (j * 90 + 180);
                playerViewX = 0.0F;
            }
        } else {
            playerViewY = livingPlayerIn.prevRotationYaw + (livingPlayerIn.rotationYaw - livingPlayerIn.prevRotationYaw) * partialTicks;
            playerViewX = livingPlayerIn.prevRotationPitch + (livingPlayerIn.rotationPitch - livingPlayerIn.prevRotationPitch) * partialTicks;
        }

        if (optionsIn.thirdPersonView == 2) {
            playerViewY += 180.0F;
        }

        viewerPosX = livingPlayerIn.lastTickPosX + (livingPlayerIn.posX - livingPlayerIn.lastTickPosX) * (double) partialTicks;
        viewerPosY = livingPlayerIn.lastTickPosY + (livingPlayerIn.posY - livingPlayerIn.lastTickPosY) * (double) partialTicks;
        viewerPosZ = livingPlayerIn.lastTickPosZ + (livingPlayerIn.posZ - livingPlayerIn.lastTickPosZ) * (double) partialTicks;
    }

    public void setPlayerViewY(float playerViewYIn) {
        playerViewY = playerViewYIn;
    }

    public boolean isRenderShadow() {
        return renderShadow;
    }

    public void setRenderShadow(boolean renderShadowIn) {
        renderShadow = renderShadowIn;
    }

    public boolean isDebugBoundingBox() {
        return debugBoundingBox;
    }

    public void setDebugBoundingBox(boolean debugBoundingBoxIn) {
        debugBoundingBox = debugBoundingBoxIn;
    }

    public boolean renderEntitySimple(Entity entityIn, float partialTicks) {
        return renderEntityStatic(entityIn, partialTicks, false);
    }

    public boolean shouldRender(Entity entityIn, ICamera camera, double camX, double camY, double camZ) {
        Render<Entity> render = getEntityRenderObject(entityIn);
        return render != null && render.shouldRender(entityIn, camera, camX, camY, camZ);
    }

    public boolean renderEntityStatic(Entity entity, float partialTicks, boolean hideDebugBox) {
        if (entity.ticksExisted == 0) {
            entity.lastTickPosX = entity.posX;
            entity.lastTickPosY = entity.posY;
            entity.lastTickPosZ = entity.posZ;
        }

        double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
        double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
        double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
        float f = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
        int i = entity.getBrightnessForRender(partialTicks);

        if (entity.isBurning()) {
            i = 15728880;
        }

        int j = i % 65536;
        int k = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        return doRenderEntity(entity, d0 - renderPosX, d1 - renderPosY, d2 - renderPosZ, f, partialTicks, hideDebugBox);
    }

    public void renderWitherSkull(Entity entityIn, float partialTicks) {
        double d0 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
        double d1 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
        double d2 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;
        Render<Entity> render = getEntityRenderObject(entityIn);

        if (render != null && renderEngine != null) {
            int i = entityIn.getBrightnessForRender(partialTicks);
            int j = i % 65536;
            int k = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            render.renderName(entityIn, d0 - renderPosX, d1 - renderPosY, d2 - renderPosZ);
        }
    }

    public boolean renderEntityWithPosYaw(Entity entityIn, double x, double y, double z, float entityYaw, float partialTicks) {
        return doRenderEntity(entityIn, x, y, z, entityYaw, partialTicks, false);
    }

    public boolean doRenderEntity(Entity entity, double x, double y, double z, float entityYaw, float partialTicks, boolean hideDebugBox) {
        Render<Entity> render = null;

        try {
            render = getEntityRenderObject(entity);

            if (render != null && renderEngine != null) {
                try {
                    if (render instanceof RendererLivingEntity) {
                        ((RendererLivingEntity) render).setRenderOutlines(renderOutlines);
                    }

                    if (CustomEntityModels.isActive()) {
                        renderRender = render;
                    }

                    render.doRender(entity, x, y, z, entityYaw, partialTicks);
                } catch (Throwable throwable2) {
                    throw new ReportedException(CrashReport.makeCrashReport(throwable2, "Rendering entity in world"));
                }

                try {
                    if (!renderOutlines) {
                        render.doRenderShadowAndFire(entity, x, y, z, entityYaw, partialTicks);
                    }
                } catch (Throwable throwable1) {
                    throw new ReportedException(CrashReport.makeCrashReport(throwable1, "Post-rendering entity in world"));
                }

                if (debugBoundingBox && !entity.isInvisible() && !hideDebugBox) {
                    try {
                        renderDebugBoundingBox(entity, x, y, z, entityYaw, partialTicks);
                    } catch (Throwable throwable) {
                        throw new ReportedException(CrashReport.makeCrashReport(throwable, "Rendering entity hitbox in world"));
                    }
                }
            } else return renderEngine == null;

            return true;
        } catch (Throwable throwable3) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable3, "Rendering entity in world");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being rendered");
            entity.addEntityCrashInfo(crashreportcategory);
            CrashReportCategory crashreportcategory1 = crashreport.makeCategory("Renderer details");
            crashreportcategory1.addCrashSection("Assigned renderer", render);
            crashreportcategory1.addCrashSection("Location", CrashReportCategory.getCoordinateInfo(x, y, z));
            crashreportcategory1.addCrashSection("Rotation", entityYaw);
            crashreportcategory1.addCrashSection("Delta", partialTicks);
            throw new ReportedException(crashreport);
        }
    }

    private void renderDebugBoundingBox(Entity entityIn, double x, double y, double z, float entityYaw, float partialTicks) {
        if (!Shaders.isShadowPass) {
            GlStateManager.depthMask(false);
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            GlStateManager.disableBlend();
            float f = entityIn.width / 2.0F;
            AxisAlignedBB axisalignedbb = entityIn.getEntityBoundingBox();
            AxisAlignedBB axisalignedbb1 = new AxisAlignedBB(axisalignedbb.minX - entityIn.posX + x, axisalignedbb.minY - entityIn.posY + y, axisalignedbb.minZ - entityIn.posZ + z, axisalignedbb.maxX - entityIn.posX + x, axisalignedbb.maxY - entityIn.posY + y, axisalignedbb.maxZ - entityIn.posZ + z);
            RenderGlobal.drawOutlinedBoundingBox(axisalignedbb1, 255, 255, 255, 255);

            if (entityIn instanceof EntityLivingBase) {
                float f1 = 0.01F;
                RenderGlobal.drawOutlinedBoundingBox(new AxisAlignedBB(x - (double) f, y + (double) entityIn.getEyeHeight() - 0.009999999776482582D, z - (double) f, x + (double) f, y + (double) entityIn.getEyeHeight() + 0.009999999776482582D, z + (double) f), 255, 0, 0, 255);
            }

            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            Vec3 vec3 = entityIn.getLook(partialTicks);
            worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
            worldrenderer.pos(x, y + (double) entityIn.getEyeHeight(), z).color(0, 0, 255, 255).endVertex();
            worldrenderer.pos(x + vec3.xCoord() * 2.0D, y + (double) entityIn.getEyeHeight() + vec3.yCoord() * 2.0D, z + vec3.zCoord() * 2.0D).color(0, 0, 255, 255).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
        }
    }

    public void set(World worldIn) {
        worldObj = worldIn;
    }

    public double getDistanceToCamera(double x, double y, double z) {
        double d0 = x - viewerPosX;
        double d1 = y - viewerPosY;
        double d2 = z - viewerPosZ;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public FontRenderer getFontRenderer() {
        return textRenderer;
    }

    public void setRenderOutlines(boolean renderOutlinesIn) {
        renderOutlines = renderOutlinesIn;
    }

    public Map<Class<? extends Entity>, Render<? extends Entity>> getEntityRenderMap() {
        return entityRenderMap;
    }

    public void setEntityRenderMap(Map p_setEntityRenderMap_1_) {
        entityRenderMap = p_setEntityRenderMap_1_;
    }

    public Map<String, RenderPlayer> getSkinMap() {
        return Collections.unmodifiableMap(skinMap);
    }
}
