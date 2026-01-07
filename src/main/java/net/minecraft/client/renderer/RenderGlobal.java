package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.chunk.*;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.RenderItemFrame;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySignRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemRecord;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.*;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.optifine.*;
import net.optifine.model.BlockModelUtils;
import net.optifine.render.ChunkVisibility;
import net.optifine.render.CloudRenderer;
import net.optifine.render.RenderEnv;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersRender;
import net.optifine.shaders.ShadowUtils;
import net.optifine.shaders.gui.GuiShaderOptions;
import net.optifine.util.ChunkUtils;
import net.optifine.util.RenderChunkUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.concurrent.Callable;

public class RenderGlobal implements IWorldAccess, IResourceManagerReloadListener {
    private static final Logger logger = LogManager.getLogger();
    private static final ResourceLocation locationMoonPhasesPng = new ResourceLocation("textures/environment/moon_phases.png");
    private static final ResourceLocation locationSunPng = new ResourceLocation("textures/environment/sun.png");
    private static final ResourceLocation locationCloudsPng = new ResourceLocation("textures/environment/clouds.png");
    private static final ResourceLocation locationEndSkyPng = new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation locationForcefieldPng = new ResourceLocation("textures/misc/forcefield.png");
    private static final Set SET_ALL_FACINGS = Collections.unmodifiableSet(new HashSet(Arrays.asList(EnumFacing.VALUES)));
    private static int renderEntitiesCounter = 0;
    public final Minecraft mc;
    public final Map<Integer, DestroyBlockProgress> damagedBlocks = Maps.newHashMap();
    private final TextureManager renderEngine;
    private final RenderManager renderManager;
    private final Set<TileEntity> setTileEntities = Sets.newHashSet();
    private final Map<BlockPos, ISound> mapSoundPositions = Maps.newHashMap();
    private final TextureAtlasSprite[] destroyBlockIcons = new TextureAtlasSprite[10];
    private final ChunkRenderDispatcher renderDispatcher = new ChunkRenderDispatcher();
    private final Vector4f[] debugTerrainMatrix = new Vector4f[8];
    private final Vector3d debugTerrainFrustumPosition = new Vector3d();
    public boolean displayListEntitiesDirty = true;
    public Entity renderedEntity;
    public Set chunksToResortTransparency = new LinkedHashSet();
    public Set chunksToUpdateForced = new LinkedHashSet();
    public boolean renderOverlayDamaged = false;
    public boolean renderOverlayEyes = false;
    IRenderChunkFactory renderChunkFactory;
    private WorldClient theWorld;
    private Set<RenderChunk> chunksToUpdate = Sets.newLinkedHashSet();
    private List<RenderGlobal.ContainerLocalRenderInformation> renderInfos = Lists.newArrayListWithCapacity(69696);
    private ViewFrustum viewFrustum;
    private int starGLCallList = -1;
    private int glSkyList = -1;
    private int glSkyList2 = -1;
    private final VertexFormat vertexBufferFormat;
    private VertexBuffer starVBO;
    private VertexBuffer skyVBO;
    private VertexBuffer sky2VBO;
    private int cloudTickCounter;
    private Framebuffer entityOutlineFramebuffer;
    private ShaderGroup entityOutlineShader;
    private double frustumUpdatePosX = Double.MIN_VALUE;
    private double frustumUpdatePosY = Double.MIN_VALUE;
    private double frustumUpdatePosZ = Double.MIN_VALUE;
    private int frustumUpdatePosChunkX = Integer.MIN_VALUE;
    private int frustumUpdatePosChunkY = Integer.MIN_VALUE;
    private int frustumUpdatePosChunkZ = Integer.MIN_VALUE;
    private double lastViewEntityX = Double.MIN_VALUE;
    private double lastViewEntityY = Double.MIN_VALUE;
    private double lastViewEntityZ = Double.MIN_VALUE;
    private double lastViewEntityPitch = Double.MIN_VALUE;
    private double lastViewEntityYaw = Double.MIN_VALUE;
    private ChunkRenderContainer renderContainer;
    private int renderDistanceChunks = -1;
    private int renderEntitiesStartupCounter = 2;
    private int countEntitiesTotal;
    private int countEntitiesRendered;
    private int countEntitiesHidden;
    private boolean debugFixTerrainFrustum = false;
    private ClippingHelper debugFixedClippingHelper;
    private boolean vboEnabled = false;
    private double prevRenderSortX;
    private double prevRenderSortY;
    private double prevRenderSortZ;
    private final CloudRenderer cloudRenderer;
    private final Deque visibilityDeque = new ArrayDeque();
    private List renderInfosEntities = new ArrayList(1024);
    private List renderInfosTileEntities = new ArrayList(1024);
    private final List renderInfosNormal = new ArrayList(1024);
    private final List renderInfosEntitiesNormal = new ArrayList(1024);
    private final List renderInfosTileEntitiesNormal = new ArrayList(1024);
    private final List renderInfosShadow = new ArrayList(1024);
    private final List renderInfosEntitiesShadow = new ArrayList(1024);
    private final List renderInfosTileEntitiesShadow = new ArrayList(1024);
    private int renderDistance = 0;
    private int renderDistanceSq = 0;
    private int countTileEntitiesRendered;
    private IChunkProvider worldChunkProvider = null;
    private LongHashMap worldChunkProviderMap = null;
    private int countLoadedChunksPrev = 0;
    private final RenderEnv renderEnv = new RenderEnv(Blocks.air.getDefaultState(), new BlockPos(0, 0, 0));
    private boolean firstWorldLoad = false;

    public RenderGlobal(Minecraft mcIn) {
        cloudRenderer = new CloudRenderer(mcIn);
        mc = mcIn;
        renderManager = mcIn.getRenderManager();
        renderEngine = mcIn.getTextureManager();
        renderEngine.bindTexture(locationForcefieldPng);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GlStateManager.bindTexture(0);
        updateDestroyBlockIcons();
        vboEnabled = OpenGlHelper.useVbo();

        if (vboEnabled) {
            renderContainer = new VboRenderList();
            renderChunkFactory = new VboChunkFactory();
        } else {
            renderContainer = new RenderList();
            renderChunkFactory = new ListChunkFactory();
        }

        vertexBufferFormat = new VertexFormat();
        vertexBufferFormat.addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.POSITION, 3));
        generateStars();
        generateSky();
        generateSky2();
    }

    public static void drawSelectionBoundingBox(AxisAlignedBB boundingBox) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION);
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION);
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(1, DefaultVertexFormats.POSITION);
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        tessellator.draw();
    }

    public static void drawOutlinedBoundingBox(AxisAlignedBB boundingBox, int red, int green, int blue, int alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        worldrenderer.begin(1, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
    }

    public void onResourceManagerReload(IResourceManager resourceManager) {
        updateDestroyBlockIcons();
    }

    private void updateDestroyBlockIcons() {
        TextureMap texturemap = mc.getTextureMapBlocks();

        for (int i = 0; i < destroyBlockIcons.length; ++i) {
            destroyBlockIcons[i] = texturemap.getAtlasSprite("minecraft:blocks/destroy_stage_" + i);
        }
    }

    public void makeEntityOutlineShader() {
        if (OpenGlHelper.shadersSupported) {
            if (ShaderLinkHelper.getStaticShaderLinkHelper() == null) {
                ShaderLinkHelper.setNewStaticShaderLinkHelper();
            }

            ResourceLocation resourcelocation = new ResourceLocation("shaders/post/entity_outline.json");

            try {
                entityOutlineShader = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), resourcelocation);
                entityOutlineShader.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
                entityOutlineFramebuffer = entityOutlineShader.getFramebufferRaw("final");
            } catch (IOException ioexception) {
                logger.warn("Failed to load shader: " + resourcelocation, ioexception);
                entityOutlineShader = null;
                entityOutlineFramebuffer = null;
            } catch (JsonSyntaxException jsonsyntaxexception) {
                logger.warn("Failed to load shader: " + resourcelocation, jsonsyntaxexception);
                entityOutlineShader = null;
                entityOutlineFramebuffer = null;
            }
        } else {
            entityOutlineShader = null;
            entityOutlineFramebuffer = null;
        }
    }

    public void renderEntityOutlineFramebuffer() {
        if (isRenderEntityOutlines()) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            entityOutlineFramebuffer.framebufferRenderExt(mc.displayWidth, mc.displayHeight, false);
            GlStateManager.disableBlend();
        }
    }

    protected boolean isRenderEntityOutlines() {
        return !Config.isFastRender() && !Config.isShaders() && !Config.isAntialiasing() && entityOutlineFramebuffer != null && entityOutlineShader != null && mc.thePlayer != null && mc.thePlayer.isSpectator() && mc.gameSettings.keyBindSpectatorOutlines.isKeyDown();
    }

    private void generateSky2() {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        if (sky2VBO != null) {
            sky2VBO.deleteGlBuffers();
        }

        if (glSkyList2 >= 0) {
            GLAllocation.deleteDisplayLists(glSkyList2);
            glSkyList2 = -1;
        }

        if (vboEnabled) {
            sky2VBO = new VertexBuffer(vertexBufferFormat);
            renderSky(worldrenderer, -16.0F, true);
            worldrenderer.finishDrawing();
            worldrenderer.reset();
            sky2VBO.bufferData(worldrenderer.getByteBuffer());
        } else {
            glSkyList2 = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(glSkyList2, GL11.GL_COMPILE);
            renderSky(worldrenderer, -16.0F, true);
            tessellator.draw();
            GL11.glEndList();
        }
    }

    private void generateSky() {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        if (skyVBO != null) {
            skyVBO.deleteGlBuffers();
        }

        if (glSkyList >= 0) {
            GLAllocation.deleteDisplayLists(glSkyList);
            glSkyList = -1;
        }

        if (vboEnabled) {
            skyVBO = new VertexBuffer(vertexBufferFormat);
            renderSky(worldrenderer, 16.0F, false);
            worldrenderer.finishDrawing();
            worldrenderer.reset();
            skyVBO.bufferData(worldrenderer.getByteBuffer());
        } else {
            glSkyList = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(glSkyList, GL11.GL_COMPILE);
            renderSky(worldrenderer, 16.0F, false);
            tessellator.draw();
            GL11.glEndList();
        }
    }

    private void renderSky(WorldRenderer worldRendererIn, float posY, boolean reverseX) {
        int i = 64;
        int j = 6;
        worldRendererIn.begin(7, DefaultVertexFormats.POSITION);
        int k = (renderDistance / 64 + 1) * 64 + 64;

        for (int l = -k; l <= k; l += 64) {
            for (int i1 = -k; i1 <= k; i1 += 64) {
                float f = (float) l;
                float f1 = (float) (l + 64);

                if (reverseX) {
                    f1 = (float) l;
                    f = (float) (l + 64);
                }

                worldRendererIn.pos(f, posY, i1).endVertex();
                worldRendererIn.pos(f1, posY, i1).endVertex();
                worldRendererIn.pos(f1, posY, i1 + 64).endVertex();
                worldRendererIn.pos(f, posY, i1 + 64).endVertex();
            }
        }
    }

    private void generateStars() {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        if (starVBO != null) {
            starVBO.deleteGlBuffers();
        }

        if (starGLCallList >= 0) {
            GLAllocation.deleteDisplayLists(starGLCallList);
            starGLCallList = -1;
        }

        if (vboEnabled) {
            starVBO = new VertexBuffer(vertexBufferFormat);
            renderStars(worldrenderer);
            worldrenderer.finishDrawing();
            worldrenderer.reset();
            starVBO.bufferData(worldrenderer.getByteBuffer());
        } else {
            starGLCallList = GLAllocation.generateDisplayLists(1);
            GlStateManager.pushMatrix();
            GL11.glNewList(starGLCallList, GL11.GL_COMPILE);
            renderStars(worldrenderer);
            tessellator.draw();
            GL11.glEndList();
            GlStateManager.popMatrix();
        }
    }

    private void renderStars(WorldRenderer worldRendererIn) {
        Random random = new Random(10842L);
        worldRendererIn.begin(7, DefaultVertexFormats.POSITION);

        for (int i = 0; i < 1500; ++i) {
            double d0 = random.nextFloat() * 2.0F - 1.0F;
            double d1 = random.nextFloat() * 2.0F - 1.0F;
            double d2 = random.nextFloat() * 2.0F - 1.0F;
            double d3 = 0.15F + random.nextFloat() * 0.1F;
            double d4 = d0 * d0 + d1 * d1 + d2 * d2;

            if (d4 < 1.0D && d4 > 0.01D) {
                d4 = 1.0D / Math.sqrt(d4);
                d0 = d0 * d4;
                d1 = d1 * d4;
                d2 = d2 * d4;
                double d5 = d0 * 100.0D;
                double d6 = d1 * 100.0D;
                double d7 = d2 * 100.0D;
                double d8 = Math.atan2(d0, d2);
                double d9 = Math.sin(d8);
                double d10 = Math.cos(d8);
                double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
                double d12 = Math.sin(d11);
                double d13 = Math.cos(d11);
                double d14 = random.nextDouble() * Math.PI * 2.0D;
                double d15 = Math.sin(d14);
                double d16 = Math.cos(d14);

                for (int j = 0; j < 4; ++j) {
                    double d17 = 0.0D;
                    double d18 = (double) ((j & 2) - 1) * d3;
                    double d19 = (double) ((j + 1 & 2) - 1) * d3;
                    double d20 = 0.0D;
                    double d21 = d18 * d16 - d19 * d15;
                    double d22 = d19 * d16 + d18 * d15;
                    double d23 = d21 * d12 + 0.0D * d13;
                    double d24 = 0.0D * d12 - d21 * d13;
                    double d25 = d24 * d9 - d22 * d10;
                    double d26 = d22 * d9 + d24 * d10;
                    worldRendererIn.pos(d5 + d25, d6 + d23, d7 + d26).endVertex();
                }
            }
        }
    }

    public void setWorldAndLoadRenderers(WorldClient worldClientIn) {
        if (theWorld != null) {
            theWorld.removeWorldAccess(this);
        }

        frustumUpdatePosX = Double.MIN_VALUE;
        frustumUpdatePosY = Double.MIN_VALUE;
        frustumUpdatePosZ = Double.MIN_VALUE;
        frustumUpdatePosChunkX = Integer.MIN_VALUE;
        frustumUpdatePosChunkY = Integer.MIN_VALUE;
        frustumUpdatePosChunkZ = Integer.MIN_VALUE;
        renderManager.set(worldClientIn);
        theWorld = worldClientIn;

        if (Config.isDynamicLights()) {
            DynamicLights.clear();
        }

        ChunkVisibility.reset();
        worldChunkProvider = null;
        worldChunkProviderMap = null;
        renderEnv.reset(null, null);
        Shaders.checkWorldChanged(theWorld);

        if (worldClientIn != null) {
            worldClientIn.addWorldAccess(this);
            loadRenderers();
        } else {
            chunksToUpdate.clear();
            clearRenderInfos();

            if (viewFrustum != null) {
                viewFrustum.deleteGlResources();
            }

            viewFrustum = null;
        }
    }

    public void loadRenderers() {
        if (theWorld != null) {
            displayListEntitiesDirty = true;
            Blocks.leaves.setGraphicsLevel(Config.isTreesFancy());
            Blocks.leaves2.setGraphicsLevel(Config.isTreesFancy());
            BlockModelRenderer.updateAoLightValue();

            if (Config.isDynamicLights()) {
                DynamicLights.clear();
            }

            SmartAnimations.update();
            renderDistanceChunks = mc.gameSettings.renderDistanceChunks;
            renderDistance = renderDistanceChunks * 16;
            renderDistanceSq = renderDistance * renderDistance;
            boolean flag = vboEnabled;
            vboEnabled = OpenGlHelper.useVbo();

            if (flag && !vboEnabled) {
                renderContainer = new RenderList();
                renderChunkFactory = new ListChunkFactory();
            } else if (!flag && vboEnabled) {
                renderContainer = new VboRenderList();
                renderChunkFactory = new VboChunkFactory();
            }

            generateStars();
            generateSky();
            generateSky2();

            if (viewFrustum != null) {
                viewFrustum.deleteGlResources();
            }

            stopChunkUpdates();

            synchronized (setTileEntities) {
                setTileEntities.clear();
            }

            viewFrustum = new ViewFrustum(theWorld, mc.gameSettings.renderDistanceChunks, this, renderChunkFactory);

            if (theWorld != null) {
                Entity entity = mc.getRenderViewEntity();

                if (entity != null) {
                    viewFrustum.updateChunkPositions(entity.posX, entity.posZ);
                }
            }

            renderEntitiesStartupCounter = 2;
        }

        if (mc.thePlayer == null) {
            firstWorldLoad = true;
        }
    }

    protected void stopChunkUpdates() {
        chunksToUpdate.clear();
        renderDispatcher.stopChunkUpdates();
    }

    public void createBindEntityOutlineFbs(int width, int height) {
        if (OpenGlHelper.shadersSupported && entityOutlineShader != null) {
            entityOutlineShader.createBindFramebuffers(width, height);
        }
    }

    public void renderEntities(Entity renderViewEntity, ICamera camera, float partialTicks) {
        int i = 0;

        if (renderEntitiesStartupCounter > 0) {
            if (i > 0) {
                return;
            }

            --renderEntitiesStartupCounter;
        } else {
            double d0 = renderViewEntity.prevPosX + (renderViewEntity.posX - renderViewEntity.prevPosX) * (double) partialTicks;
            double d1 = renderViewEntity.prevPosY + (renderViewEntity.posY - renderViewEntity.prevPosY) * (double) partialTicks;
            double d2 = renderViewEntity.prevPosZ + (renderViewEntity.posZ - renderViewEntity.prevPosZ) * (double) partialTicks;
            theWorld.theProfiler.startSection("prepare");
            TileEntityRendererDispatcher.instance.cacheActiveRenderInfo(theWorld, mc.getTextureManager(), mc.fontRendererObj, mc.getRenderViewEntity(), partialTicks);
            renderManager.cacheActiveRenderInfo(theWorld, mc.fontRendererObj, mc.getRenderViewEntity(), mc.pointedEntity, mc.gameSettings, partialTicks);
            ++renderEntitiesCounter;

            if (i == 0) {
                countEntitiesTotal = 0;
                countEntitiesRendered = 0;
                countEntitiesHidden = 0;
                countTileEntitiesRendered = 0;
            }

            Entity entity = mc.getRenderViewEntity();
            double d3 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
            double d4 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
            double d5 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
            TileEntityRendererDispatcher.staticPlayerX = d3;
            TileEntityRendererDispatcher.staticPlayerY = d4;
            TileEntityRendererDispatcher.staticPlayerZ = d5;
            renderManager.setRenderPosition(d3, d4, d5);
            mc.entityRenderer.enableLightmap();
            theWorld.theProfiler.endStartSection("global");
            List<Entity> list = theWorld.getLoadedEntityList();

            if (i == 0) {
                countEntitiesTotal = list.size();
            }

            if (Config.isFogOff() && mc.entityRenderer.fogStandard) {
                GlStateManager.disableFog();
            }

            for (int j = 0; j < theWorld.weatherEffects.size(); ++j) {
                Entity entity1 = theWorld.weatherEffects.get(j);

                ++countEntitiesRendered;

                if (entity1.isInRangeToRender3d(d0, d1, d2)) {
                    renderManager.renderEntitySimple(entity1, partialTicks);
                }
            }

            if (isRenderEntityOutlines()) {
                GlStateManager.depthFunc(519);
                GlStateManager.disableFog();
                entityOutlineFramebuffer.framebufferClear();
                entityOutlineFramebuffer.bindFramebuffer(false);
                theWorld.theProfiler.endStartSection("entityOutlines");
                RenderHelper.disableStandardItemLighting();
                renderManager.setRenderOutlines(true);

                for (Entity entity3 : list) {
                    boolean flag2 = mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase) mc.getRenderViewEntity()).isPlayerSleeping();
                    boolean flag3 = entity3.isInRangeToRender3d(d0, d1, d2) && (entity3.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(entity3.getEntityBoundingBox()) || entity3.riddenByEntity == mc.thePlayer) && entity3 instanceof EntityPlayer;

                    if ((entity3 != mc.getRenderViewEntity() || mc.gameSettings.thirdPersonView != 0 || flag2) && flag3) {
                        renderManager.renderEntitySimple(entity3, partialTicks);
                    }
                }

                renderManager.setRenderOutlines(false);
                RenderHelper.enableStandardItemLighting();
                GlStateManager.depthMask(false);
                entityOutlineShader.loadShaderGroup(partialTicks);
                GlStateManager.enableLighting();
                GlStateManager.depthMask(true);
                mc.getFramebuffer().bindFramebuffer(false);
                GlStateManager.enableFog();
                GlStateManager.enableBlend();
                GlStateManager.enableColorMaterial();
                GlStateManager.depthFunc(515);
                GlStateManager.enableDepth();
                GlStateManager.enableAlpha();
            }

            theWorld.theProfiler.endStartSection("entities");
            boolean flag6 = Config.isShaders();

            if (flag6) {
                Shaders.beginEntities();
            }

            RenderItemFrame.updateItemRenderDistance();
            boolean flag7 = mc.gameSettings.fancyGraphics;
            mc.gameSettings.fancyGraphics = Config.isDroppedItemsFancy();
            boolean flag8 = Shaders.isShadowPass && !mc.thePlayer.isSpectator();
            label926:

            for (Object o : renderInfosEntities) {
                RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation = (ContainerLocalRenderInformation) o;
                Chunk chunk = renderglobal$containerlocalrenderinformation.renderChunk.getChunk();
                ClassInheritanceMultiMap<Entity> classinheritancemultimap = chunk.getEntityLists()[renderglobal$containerlocalrenderinformation.renderChunk.getPosition().getY() / 16];

                if (!classinheritancemultimap.isEmpty()) {
                    Iterator iterator = classinheritancemultimap.iterator();

                    while (true) {
                        Entity entity2;
                        boolean flag4;

                        while (true) {
                            if (!iterator.hasNext()) {
                                continue label926;
                            }

                            entity2 = (Entity) iterator.next();

                            flag4 = renderManager.shouldRender(entity2, camera, d0, d1, d2) || entity2.riddenByEntity == mc.thePlayer;

                            if (!flag4) {
                                break;
                            }

                            boolean flag5 = mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase) mc.getRenderViewEntity()).isPlayerSleeping();

                            if ((entity2 != mc.getRenderViewEntity() || flag8 || mc.gameSettings.thirdPersonView != 0 || flag5) && (entity2.posY < 0.0D || entity2.posY >= 256.0D || theWorld.isBlockLoaded(new BlockPos(entity2)))) {
                                ++countEntitiesRendered;
                                renderedEntity = entity2;

                                if (flag6) {
                                    Shaders.nextEntity(entity2);
                                }

                                renderManager.renderEntitySimple(entity2, partialTicks);
                                renderedEntity = null;
                                break;
                            }

                        }

                        if (!flag4 && entity2 instanceof EntityWitherSkull) {
                            renderedEntity = entity2;

                            if (flag6) {
                                Shaders.nextEntity(entity2);
                            }

                            mc.getRenderManager().renderWitherSkull(entity2, partialTicks);
                            renderedEntity = null;
                        }
                    }
                }
            }

            mc.gameSettings.fancyGraphics = flag7;

            if (flag6) {
                Shaders.endEntities();
                Shaders.beginBlockEntities();
            }

            theWorld.theProfiler.endStartSection("blockentities");
            RenderHelper.enableStandardItemLighting();

            TileEntityRendererDispatcher.instance.preDrawBatch();

            TileEntitySignRenderer.updateTextRenderDistance();
            label1408:

            for (Object o : renderInfosTileEntities) {
                RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation1 = (ContainerLocalRenderInformation) o;
                List<TileEntity> list1 = renderglobal$containerlocalrenderinformation1.renderChunk.getCompiledChunk().getTileEntities();

                if (!list1.isEmpty()) {
                    Iterator iterator1 = list1.iterator();

                    while (true) {
                        TileEntity tileentity1;

                        while (true) {
                            if (!iterator1.hasNext()) {
                                continue label1408;
                            }

                            tileentity1 = (TileEntity) iterator1.next();

                            break;
                        }

                        if (flag6) {
                            Shaders.nextBlockEntity(tileentity1);
                        }

                        TileEntityRendererDispatcher.instance.renderTileEntity(tileentity1, partialTicks, -1);
                        ++countTileEntitiesRendered;
                    }
                }
            }

            synchronized (setTileEntities) {
                for (TileEntity tileentity : setTileEntities) {
                    if (flag6) {
                        Shaders.nextBlockEntity(tileentity);
                    }

                    TileEntityRendererDispatcher.instance.renderTileEntity(tileentity, partialTicks, -1);
                }
            }

            TileEntityRendererDispatcher.instance.drawBatch(i);

            renderOverlayDamaged = true;
            preRenderDamagedBlocks();

            for (DestroyBlockProgress destroyblockprogress : damagedBlocks.values()) {
                BlockPos blockpos = destroyblockprogress.getPosition();
                TileEntity tileentity2 = theWorld.getTileEntity(blockpos);

                if (tileentity2 instanceof TileEntityChest tileentitychest) {

                    if (tileentitychest.adjacentChestXNeg != null) {
                        blockpos = blockpos.offset(EnumFacing.WEST);
                        tileentity2 = theWorld.getTileEntity(blockpos);
                    } else if (tileentitychest.adjacentChestZNeg != null) {
                        blockpos = blockpos.offset(EnumFacing.NORTH);
                        tileentity2 = theWorld.getTileEntity(blockpos);
                    }
                }

                Block block = theWorld.getBlockState(blockpos).getBlock();
                boolean flag9;

                flag9 = tileentity2 != null && (block instanceof BlockChest || block instanceof BlockEnderChest || block instanceof BlockSign || block instanceof BlockSkull);

                if (flag9) {
                    if (flag6) {
                        Shaders.nextBlockEntity(tileentity2);
                    }

                    TileEntityRendererDispatcher.instance.renderTileEntity(tileentity2, partialTicks, destroyblockprogress.getPartialBlockDamage());
                }
            }

            postRenderDamagedBlocks();
            renderOverlayDamaged = false;

            if (flag6) {
                Shaders.endBlockEntities();
            }

            --renderEntitiesCounter;
            mc.entityRenderer.disableLightmap();
            mc.mcProfiler.endSection();
        }
    }

    public String getDebugInfoRenders() {
        int i = viewFrustum.renderChunks.length;
        int j = 0;

        for (RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation : renderInfos) {
            CompiledChunk compiledchunk = renderglobal$containerlocalrenderinformation.renderChunk.compiledChunk;

            if (compiledchunk != CompiledChunk.DUMMY && !compiledchunk.isEmpty()) {
                ++j;
            }
        }

        return String.format("C: %d/%d %sD: %d, %s", j, i, mc.renderChunksMany ? "(s) " : "", renderDistanceChunks, renderDispatcher.getDebugInfo());
    }

    public String getDebugInfoEntities() {
        return "E: " + countEntitiesRendered + "/" + countEntitiesTotal + ", B: " + countEntitiesHidden + ", I: " + (countEntitiesTotal - countEntitiesHidden - countEntitiesRendered) + ", " + Config.getVersionDebug();
    }

    public void setupTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator) {
        if (mc.gameSettings.renderDistanceChunks != renderDistanceChunks) {
            loadRenderers();
        }

        theWorld.theProfiler.startSection("camera");
        double d0 = viewEntity.posX - frustumUpdatePosX;
        double d1 = viewEntity.posY - frustumUpdatePosY;
        double d2 = viewEntity.posZ - frustumUpdatePosZ;

        if (frustumUpdatePosChunkX != viewEntity.chunkCoordX || frustumUpdatePosChunkY != viewEntity.chunkCoordY || frustumUpdatePosChunkZ != viewEntity.chunkCoordZ || d0 * d0 + d1 * d1 + d2 * d2 > 16.0D) {
            frustumUpdatePosX = viewEntity.posX;
            frustumUpdatePosY = viewEntity.posY;
            frustumUpdatePosZ = viewEntity.posZ;
            frustumUpdatePosChunkX = viewEntity.chunkCoordX;
            frustumUpdatePosChunkY = viewEntity.chunkCoordY;
            frustumUpdatePosChunkZ = viewEntity.chunkCoordZ;
            viewFrustum.updateChunkPositions(viewEntity.posX, viewEntity.posZ);
        }

        if (Config.isDynamicLights()) {
            DynamicLights.update(this);
        }

        theWorld.theProfiler.endStartSection("renderlistcamera");
        double d3 = viewEntity.lastTickPosX + (viewEntity.posX - viewEntity.lastTickPosX) * partialTicks;
        double d4 = viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY) * partialTicks;
        double d5 = viewEntity.lastTickPosZ + (viewEntity.posZ - viewEntity.lastTickPosZ) * partialTicks;
        renderContainer.initialize(d3, d4, d5);
        theWorld.theProfiler.endStartSection("cull");

        if (debugFixedClippingHelper != null) {
            Frustum frustum = new Frustum(debugFixedClippingHelper);
            frustum.setPosition(debugTerrainFrustumPosition.x, debugTerrainFrustumPosition.y, debugTerrainFrustumPosition.z);
            camera = frustum;
        }

        mc.mcProfiler.endStartSection("culling");
        BlockPos blockpos = new BlockPos(d3, d4 + (double) viewEntity.getEyeHeight(), d5);
        RenderChunk renderchunk = viewFrustum.getRenderChunk(blockpos);
        new BlockPos(MathHelper.floor_double(d3 / 16.0D) * 16, MathHelper.floor_double(d4 / 16.0D) * 16, MathHelper.floor_double(d5 / 16.0D) * 16);
        displayListEntitiesDirty = displayListEntitiesDirty || !chunksToUpdate.isEmpty() || viewEntity.posX != lastViewEntityX || viewEntity.posY != lastViewEntityY || viewEntity.posZ != lastViewEntityZ || (double) viewEntity.rotationPitch != lastViewEntityPitch || (double) viewEntity.rotationYaw != lastViewEntityYaw;
        lastViewEntityX = viewEntity.posX;
        lastViewEntityY = viewEntity.posY;
        lastViewEntityZ = viewEntity.posZ;
        lastViewEntityPitch = viewEntity.rotationPitch;
        lastViewEntityYaw = viewEntity.rotationYaw;
        boolean flag = debugFixedClippingHelper != null;
        mc.mcProfiler.endStartSection("update");
        Lagometer.timerVisibility.start();
        int i = getCountLoadedChunks();

        if (i != countLoadedChunksPrev) {
            countLoadedChunksPrev = i;
            displayListEntitiesDirty = true;
        }

        int j = 256;

        if (!ChunkVisibility.isFinished()) {
            displayListEntitiesDirty = true;
        }

        if (!flag && displayListEntitiesDirty && Config.isIntegratedServerRunning()) {
            j = ChunkVisibility.getMaxChunkY(theWorld, viewEntity, renderDistanceChunks);
        }

        RenderChunk renderchunk1 = viewFrustum.getRenderChunk(new BlockPos(viewEntity.posX, viewEntity.posY, viewEntity.posZ));

        if (Shaders.isShadowPass) {
            renderInfos = renderInfosShadow;
            renderInfosEntities = renderInfosEntitiesShadow;
            renderInfosTileEntities = renderInfosTileEntitiesShadow;

            if (!flag && displayListEntitiesDirty) {
                clearRenderInfos();

                if (renderchunk1 != null && renderchunk1.getPosition().getY() > j) {
                    renderInfosEntities.add(renderchunk1.getRenderInfo());
                }

                Iterator<RenderChunk> iterator = ShadowUtils.makeShadowChunkIterator(theWorld, partialTicks, viewEntity, renderDistanceChunks, viewFrustum);

                while (iterator.hasNext()) {
                    RenderChunk renderchunk2 = iterator.next();

                    if (renderchunk2 != null && renderchunk2.getPosition().getY() <= j) {
                        RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation = renderchunk2.getRenderInfo();

                        if (!renderchunk2.compiledChunk.isEmpty() || renderchunk2.isNeedsUpdate()) {
                            renderInfos.add(renderglobal$containerlocalrenderinformation);
                        }

                        if (ChunkUtils.hasEntities(renderchunk2.getChunk())) {
                            renderInfosEntities.add(renderglobal$containerlocalrenderinformation);
                        }

                        if (!renderchunk2.getCompiledChunk().getTileEntities().isEmpty()) {
                            renderInfosTileEntities.add(renderglobal$containerlocalrenderinformation);
                        }
                    }
                }
            }
        } else {
            renderInfos = renderInfosNormal;
            renderInfosEntities = renderInfosEntitiesNormal;
            renderInfosTileEntities = renderInfosTileEntitiesNormal;
        }

        if (!flag && displayListEntitiesDirty && !Shaders.isShadowPass) {
            displayListEntitiesDirty = false;
            clearRenderInfos();
            visibilityDeque.clear();
            Deque deque = visibilityDeque;
            boolean flag1 = mc.renderChunksMany;

            if (renderchunk != null && renderchunk.getPosition().getY() <= j) {
                boolean flag2 = false;
                RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation4 = new RenderGlobal.ContainerLocalRenderInformation(renderchunk, null, 0);
                Set set1 = SET_ALL_FACINGS;

                if (set1.size() == 1) {
                    Vector3f vector3f = getViewVector(viewEntity, partialTicks);
                    EnumFacing enumfacing2 = EnumFacing.getFacingFromVector(vector3f.x, vector3f.y, vector3f.z).getOpposite();
                    set1.remove(enumfacing2);
                }

                if (set1.isEmpty()) {
                    flag2 = true;
                }

                if (flag2 && !playerSpectator) {
                    renderInfos.add(renderglobal$containerlocalrenderinformation4);
                } else {
                    if (playerSpectator && theWorld.getBlockState(blockpos).getBlock().isOpaqueCube()) {
                        flag1 = false;
                    }

                    renderchunk.setFrameIndex(frameCount);
                    deque.add(renderglobal$containerlocalrenderinformation4);
                }
            } else {
                int j1 = blockpos.getY() > 0 ? Math.min(j, 248) : 8;

                if (renderchunk1 != null) {
                    renderInfosEntities.add(renderchunk1.getRenderInfo());
                }

                for (int k = -renderDistanceChunks; k <= renderDistanceChunks; ++k) {
                    for (int l = -renderDistanceChunks; l <= renderDistanceChunks; ++l) {
                        RenderChunk renderchunk3 = viewFrustum.getRenderChunk(new BlockPos((k << 4) + 8, j1, (l << 4) + 8));

                        if (renderchunk3 != null && renderchunk3.isBoundingBoxInFrustum(camera, frameCount)) {
                            renderchunk3.setFrameIndex(frameCount);
                            RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation1 = renderchunk3.getRenderInfo();
                            renderglobal$containerlocalrenderinformation1.initialize(null, 0);
                            deque.add(renderglobal$containerlocalrenderinformation1);
                        }
                    }
                }
            }

            mc.mcProfiler.startSection("iteration");
            boolean flag3 = Config.isFogOn();

            while (!deque.isEmpty()) {
                RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation5 = (RenderGlobal.ContainerLocalRenderInformation) deque.poll();
                RenderChunk renderchunk6 = renderglobal$containerlocalrenderinformation5.renderChunk;
                EnumFacing enumfacing1 = renderglobal$containerlocalrenderinformation5.facing;
                CompiledChunk compiledchunk = renderchunk6.compiledChunk;

                if (!compiledchunk.isEmpty() || renderchunk6.isNeedsUpdate()) {
                    renderInfos.add(renderglobal$containerlocalrenderinformation5);
                }

                if (ChunkUtils.hasEntities(renderchunk6.getChunk())) {
                    renderInfosEntities.add(renderglobal$containerlocalrenderinformation5);
                }

                if (!compiledchunk.getTileEntities().isEmpty()) {
                    renderInfosTileEntities.add(renderglobal$containerlocalrenderinformation5);
                }

                for (EnumFacing enumfacing : flag1 ? ChunkVisibility.getFacingsNotOpposite(renderglobal$containerlocalrenderinformation5.setFacing) : EnumFacing.VALUES) {
                    if (!flag1 || enumfacing1 == null || compiledchunk.isVisible(enumfacing1.getOpposite(), enumfacing)) {
                        RenderChunk renderchunk4 = getRenderChunkOffset(blockpos, renderchunk6, enumfacing, flag3, j);

                        if (renderchunk4 != null && renderchunk4.setFrameIndex(frameCount) && renderchunk4.isBoundingBoxInFrustum(camera, frameCount)) {
                            int i1 = renderglobal$containerlocalrenderinformation5.setFacing | 1 << enumfacing.ordinal();
                            RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation2 = renderchunk4.getRenderInfo();
                            renderglobal$containerlocalrenderinformation2.initialize(enumfacing, i1);
                            deque.add(renderglobal$containerlocalrenderinformation2);
                        }
                    }
                }
            }

            mc.mcProfiler.endSection();
        }

        mc.mcProfiler.endStartSection("captureFrustum");

        if (debugFixTerrainFrustum) {
            fixTerrainFrustum(d3, d4, d5);
            debugFixTerrainFrustum = false;
        }

        Lagometer.timerVisibility.end();

        if (Shaders.isShadowPass) {
            Shaders.mcProfilerEndSection();
        } else {
            mc.mcProfiler.endStartSection("rebuildNear");
            renderDispatcher.clearChunkUpdates();
            Set<RenderChunk> set = chunksToUpdate;
            chunksToUpdate = Sets.newLinkedHashSet();
            Lagometer.timerChunkUpdate.start();

            for (RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation3 : renderInfos) {
                RenderChunk renderchunk5 = renderglobal$containerlocalrenderinformation3.renderChunk;

                if (renderchunk5.isNeedsUpdate() || set.contains(renderchunk5)) {
                    displayListEntitiesDirty = true;
                    BlockPos blockpos1 = renderchunk5.getPosition();
                    boolean flag4 = blockpos.distanceSq(blockpos1.getX() + 8, blockpos1.getY() + 8, blockpos1.getZ() + 8) < 768.0D;

                    if (!flag4) {
                        chunksToUpdate.add(renderchunk5);
                    } else if (!renderchunk5.isPlayerUpdate()) {
                        chunksToUpdateForced.add(renderchunk5);
                    } else {
                        mc.mcProfiler.startSection("build near");
                        renderDispatcher.updateChunkNow(renderchunk5);
                        renderchunk5.setNeedsUpdate(false);
                        mc.mcProfiler.endSection();
                    }
                }
            }

            Lagometer.timerChunkUpdate.end();
            chunksToUpdate.addAll(set);
            mc.mcProfiler.endSection();
        }
    }

    private boolean isPositionInRenderChunk(BlockPos pos, RenderChunk renderChunkIn) {
        BlockPos blockpos = renderChunkIn.getPosition();
        return MathHelper.abs_int(pos.getX() - blockpos.getX()) <= 16 && (MathHelper.abs_int(pos.getY() - blockpos.getY()) <= 16 && MathHelper.abs_int(pos.getZ() - blockpos.getZ()) <= 16);
    }

    private Set<EnumFacing> getVisibleFacings(BlockPos pos) {
        VisGraph visgraph = new VisGraph();
        BlockPos blockpos = new BlockPos(pos.getX() >> 4 << 4, pos.getY() >> 4 << 4, pos.getZ() >> 4 << 4);
        Chunk chunk = theWorld.getChunkFromBlockCoords(blockpos);

        for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(blockpos, blockpos.add(15, 15, 15))) {
            if (chunk.getBlock(blockpos$mutableblockpos).isOpaqueCube()) {
                visgraph.func_178606_a(blockpos$mutableblockpos);
            }
        }
        return visgraph.func_178609_b(pos);
    }

    private RenderChunk getRenderChunkOffset(BlockPos p_getRenderChunkOffset_1_, RenderChunk p_getRenderChunkOffset_2_, EnumFacing p_getRenderChunkOffset_3_, boolean p_getRenderChunkOffset_4_, int p_getRenderChunkOffset_5_) {
        RenderChunk renderchunk = p_getRenderChunkOffset_2_.getRenderChunkNeighbour(p_getRenderChunkOffset_3_);

        if (renderchunk == null) {
            return null;
        } else if (renderchunk.getPosition().getY() > p_getRenderChunkOffset_5_) {
            return null;
        } else {
            if (p_getRenderChunkOffset_4_) {
                BlockPos blockpos = renderchunk.getPosition();
                int i = p_getRenderChunkOffset_1_.getX() - blockpos.getX();
                int j = p_getRenderChunkOffset_1_.getZ() - blockpos.getZ();
                int k = i * i + j * j;

                if (k > renderDistanceSq) {
                    return null;
                }
            }

            return renderchunk;
        }
    }

    private void fixTerrainFrustum(double x, double y, double z) {
        debugFixedClippingHelper = new ClippingHelperImpl();
        ((ClippingHelperImpl) debugFixedClippingHelper).init();
        Matrix4f matrix4f = new Matrix4f(FloatBuffer.wrap(debugFixedClippingHelper.modelviewMatrix));
        matrix4f.transpose();
        Matrix4f matrix4f1 = new Matrix4f(FloatBuffer.wrap(debugFixedClippingHelper.projectionMatrix));
        matrix4f1.transpose();
        Matrix4f matrix4f2 = new Matrix4f();
        matrix4f1.mul(matrix4f, matrix4f2);
        matrix4f2.invert();
        debugTerrainFrustumPosition.x = x;
        debugTerrainFrustumPosition.y = y;
        debugTerrainFrustumPosition.z = z;
        debugTerrainMatrix[0] = new Vector4f(-1.0F, -1.0F, -1.0F, 1.0F);
        debugTerrainMatrix[1] = new Vector4f(1.0F, -1.0F, -1.0F, 1.0F);
        debugTerrainMatrix[2] = new Vector4f(1.0F, 1.0F, -1.0F, 1.0F);
        debugTerrainMatrix[3] = new Vector4f(-1.0F, 1.0F, -1.0F, 1.0F);
        debugTerrainMatrix[4] = new Vector4f(-1.0F, -1.0F, 1.0F, 1.0F);
        debugTerrainMatrix[5] = new Vector4f(1.0F, -1.0F, 1.0F, 1.0F);
        debugTerrainMatrix[6] = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
        debugTerrainMatrix[7] = new Vector4f(-1.0F, 1.0F, 1.0F, 1.0F);

        for (int i = 0; i < 8; ++i) {
            //Matrix4f.transform(matrix4f2, this.debugTerrainMatrix[i], this.debugTerrainMatrix[i]);
            matrix4f2.transform(debugTerrainMatrix[i]);
            debugTerrainMatrix[i].x /= debugTerrainMatrix[i].w;
            debugTerrainMatrix[i].y /= debugTerrainMatrix[i].w;
            debugTerrainMatrix[i].z /= debugTerrainMatrix[i].w;
            debugTerrainMatrix[i].w = 1.0F;
        }
    }

    protected Vector3f getViewVector(Entity entityIn, double partialTicks) {
        float f = (float) ((double) entityIn.prevRotationPitch + (double) (entityIn.rotationPitch - entityIn.prevRotationPitch) * partialTicks);
        float f1 = (float) ((double) entityIn.prevRotationYaw + (double) (entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks);

        if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2) {
            f += 180.0F;
        }

        float f2 = MathHelper.cos(-f1 * 0.017453292F - (float) Math.PI);
        float f3 = MathHelper.sin(-f1 * 0.017453292F - (float) Math.PI);
        float f4 = -MathHelper.cos(-f * 0.017453292F);
        float f5 = MathHelper.sin(-f * 0.017453292F);
        return new Vector3f(f3 * f4, f5, f2 * f4);
    }

    public int renderBlockLayer(EnumWorldBlockLayer blockLayerIn, double partialTicks, int pass, Entity entityIn) {
        RenderHelper.disableStandardItemLighting();

        if (blockLayerIn == EnumWorldBlockLayer.TRANSLUCENT && !Shaders.isShadowPass) {
            mc.mcProfiler.startSection("translucent_sort");
            double d0 = entityIn.posX - prevRenderSortX;
            double d1 = entityIn.posY - prevRenderSortY;
            double d2 = entityIn.posZ - prevRenderSortZ;

            if (d0 * d0 + d1 * d1 + d2 * d2 > 1.0D) {
                prevRenderSortX = entityIn.posX;
                prevRenderSortY = entityIn.posY;
                prevRenderSortZ = entityIn.posZ;
                int k = 0;
                chunksToResortTransparency.clear();

                for (RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation : renderInfos) {
                    if (renderglobal$containerlocalrenderinformation.renderChunk.compiledChunk.isLayerStarted(blockLayerIn) && k++ < 15) {
                        chunksToResortTransparency.add(renderglobal$containerlocalrenderinformation.renderChunk);
                    }
                }
            }

            mc.mcProfiler.endSection();
        }

        mc.mcProfiler.startSection("filterempty");
        int l = 0;
        boolean flag = blockLayerIn == EnumWorldBlockLayer.TRANSLUCENT;
        int i1 = flag ? renderInfos.size() - 1 : 0;
        int i = flag ? -1 : renderInfos.size();
        int j1 = flag ? -1 : 1;

        for (int j = i1; j != i; j += j1) {
            RenderChunk renderchunk = renderInfos.get(j).renderChunk;

            if (!renderchunk.getCompiledChunk().isLayerEmpty(blockLayerIn)) {
                ++l;
                renderContainer.addRenderChunk(renderchunk, blockLayerIn);
            }
        }

        if (l == 0) {
            mc.mcProfiler.endSection();
            return l;
        } else {
            if (Config.isFogOff() && mc.entityRenderer.fogStandard) {
                GlStateManager.disableFog();
            }

            mc.mcProfiler.endStartSection("render_" + blockLayerIn);
            renderBlockLayer(blockLayerIn);
            mc.mcProfiler.endSection();
            return l;
        }
    }

    @SuppressWarnings("incomplete-switch")
    private void renderBlockLayer(EnumWorldBlockLayer blockLayerIn) {
        mc.entityRenderer.enableLightmap();

        if (OpenGlHelper.useVbo()) {
            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        }

        if (Config.isShaders()) {
            ShadersRender.preRenderChunkLayer(blockLayerIn);
        }

        renderContainer.renderChunkLayer(blockLayerIn);

        if (Config.isShaders()) {
            ShadersRender.postRenderChunkLayer(blockLayerIn);
        }

        if (OpenGlHelper.useVbo()) {
            for (VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements()) {
                VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
                int i = vertexformatelement.getIndex();

                switch (vertexformatelement$enumusage) {
                    case POSITION:
                        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                        break;

                    case UV:
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + i);
                        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                        break;

                    case COLOR:
                        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
                        GlStateManager.resetColor();
                }
            }
        }

        mc.entityRenderer.disableLightmap();
    }

    private void cleanupDamagedBlocks(Iterator<DestroyBlockProgress> iteratorIn) {
        while (iteratorIn.hasNext()) {
            DestroyBlockProgress destroyblockprogress = iteratorIn.next();
            int i = destroyblockprogress.getCreationCloudUpdateTick();

            if (cloudTickCounter - i > 400) {
                iteratorIn.remove();
            }
        }
    }

    public void updateClouds() {
        if (Config.isShaders()) {
            if (Keyboard.isKeyDown(61) && Keyboard.isKeyDown(24)) {
                GuiShaderOptions guishaderoptions = new GuiShaderOptions(null, Config.getGameSettings());
                Config.getMinecraft().displayGuiScreen(guishaderoptions);
            }

            if (Keyboard.isKeyDown(61) && Keyboard.isKeyDown(19)) {
                Shaders.uninit();
                Shaders.loadShaderPack();
            }
        }

        ++cloudTickCounter;

        if (cloudTickCounter % 20 == 0) {
            cleanupDamagedBlocks(damagedBlocks.values().iterator());
        }
    }

    private void renderSkyEnd() {
        if (Config.isSkyEnabled()) {
            GlStateManager.disableFog();
            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.depthMask(false);
            renderEngine.bindTexture(locationEndSkyPng);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();

            for (int i = 0; i < 6; ++i) {
                GlStateManager.pushMatrix();

                if (i == 1) {
                    GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 2) {
                    GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 3) {
                    GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 4) {
                    GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                }

                if (i == 5) {
                    GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
                }

                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                int j = 40;
                int k = 40;
                int l = 40;

                if (Config.isCustomColors()) {
                    Vec3 vec3 = new Vec3((double) j / 255.0D, (double) k / 255.0D, (double) l / 255.0D);
                    vec3 = CustomColors.getWorldSkyColor(vec3, theWorld, mc.getRenderViewEntity(), 0.0F);
                    j = (int) (vec3.xCoord() * 255.0D);
                    k = (int) (vec3.yCoord() * 255.0D);
                    l = (int) (vec3.zCoord() * 255.0D);
                }

                worldrenderer.pos(-100.0D, -100.0D, -100.0D).tex(0.0D, 0.0D).color(j, k, l, 255).endVertex();
                worldrenderer.pos(-100.0D, -100.0D, 100.0D).tex(0.0D, 16.0D).color(j, k, l, 255).endVertex();
                worldrenderer.pos(100.0D, -100.0D, 100.0D).tex(16.0D, 16.0D).color(j, k, l, 255).endVertex();
                worldrenderer.pos(100.0D, -100.0D, -100.0D).tex(16.0D, 0.0D).color(j, k, l, 255).endVertex();
                tessellator.draw();
                GlStateManager.popMatrix();
            }

            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
        }
    }

    public void renderSky(float partialTicks, int pass) {
        if (mc.theWorld.provider.getDimensionId() == 1) {
            renderSkyEnd();
        } else if (mc.theWorld.provider.isSurfaceWorld()) {
            GlStateManager.disableTexture2D();
            boolean flag = Config.isShaders();

            if (flag) {
                Shaders.disableTexture2D();
            }

            Vec3 vec3 = theWorld.getSkyColor(mc.getRenderViewEntity(), partialTicks);
            vec3 = CustomColors.getSkyColor(vec3, mc.theWorld, mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY + 1.0D, mc.getRenderViewEntity().posZ);

            if (flag) {
                Shaders.setSkyColor(vec3);
            }

            float f = (float) vec3.xCoord();
            float f1 = (float) vec3.yCoord();
            float f2 = (float) vec3.zCoord();

            if (pass != 2) {
                float f3 = (f * 30.0F + f1 * 59.0F + f2 * 11.0F) / 100.0F;
                float f4 = (f * 30.0F + f1 * 70.0F) / 100.0F;
                float f5 = (f * 30.0F + f2 * 70.0F) / 100.0F;
                f = f3;
                f1 = f4;
                f2 = f5;
            }

            GlStateManager.color(f, f1, f2);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            GlStateManager.depthMask(false);
            GlStateManager.enableFog();

            if (flag) {
                Shaders.enableFog();
            }

            GlStateManager.color(f, f1, f2);

            if (flag) {
                Shaders.preSkyList();
            }

            if (Config.isSkyEnabled()) {
                if (vboEnabled) {
                    skyVBO.bindBuffer();
                    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                    GL11.glVertexPointer(3, GL11.GL_FLOAT, 12, 0L);
                    skyVBO.drawArrays(7);
                    skyVBO.unbindBuffer();
                    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                } else {
                    GlStateManager.callList(glSkyList);
                }
            }

            GlStateManager.disableFog();

            if (flag) {
                Shaders.disableFog();
            }

            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.disableStandardItemLighting();
            float[] afloat = theWorld.provider.calcSunriseSunsetColors(theWorld.getCelestialAngle(partialTicks), partialTicks);

            if (afloat != null && Config.isSunMoonEnabled()) {
                GlStateManager.disableTexture2D();

                if (flag) {
                    Shaders.disableTexture2D();
                }

                GlStateManager.shadeModel(7425);
                GlStateManager.pushMatrix();
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(MathHelper.sin(theWorld.getCelestialAngleRadians(partialTicks)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                float f6 = afloat[0];
                float f7 = afloat[1];
                float f8 = afloat[2];

                if (pass != 2) {
                    float f9 = (f6 * 30.0F + f7 * 59.0F + f8 * 11.0F) / 100.0F;
                    float f10 = (f6 * 30.0F + f7 * 70.0F) / 100.0F;
                    float f11 = (f6 * 30.0F + f8 * 70.0F) / 100.0F;
                    f6 = f9;
                    f7 = f10;
                    f8 = f11;
                }

                worldrenderer.begin(6, DefaultVertexFormats.POSITION_COLOR);
                worldrenderer.pos(0.0D, 100.0D, 0.0D).color(f6, f7, f8, afloat[3]).endVertex();
                int j = 16;

                for (int l = 0; l <= 16; ++l) {
                    float f18 = (float) l * (float) Math.PI * 2.0F / 16.0F;
                    float f12 = MathHelper.sin(f18);
                    float f13 = MathHelper.cos(f18);
                    worldrenderer.pos(f12 * 120.0F, f13 * 120.0F, -f13 * 40.0F * afloat[3]).color(afloat[0], afloat[1], afloat[2], 0.0F).endVertex();
                }

                tessellator.draw();
                GlStateManager.popMatrix();
                GlStateManager.shadeModel(7424);
            }

            GlStateManager.enableTexture2D();

            if (flag) {
                Shaders.enableTexture2D();
            }

            GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
            GlStateManager.pushMatrix();
            float f15 = 1.0F - theWorld.getRainStrength(partialTicks);
            GlStateManager.color(1.0F, 1.0F, 1.0F, f15);
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            CustomSky.renderSky(theWorld, renderEngine, partialTicks);

            if (flag) {
                Shaders.preCelestialRotate();
            }

            GlStateManager.rotate(theWorld.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);

            if (flag) {
                Shaders.postCelestialRotate();
            }

            float f16 = 30.0F;

            if (Config.isSunTexture()) {
                renderEngine.bindTexture(locationSunPng);
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
                worldrenderer.pos(-f16, 100.0D, -f16).tex(0.0D, 0.0D).endVertex();
                worldrenderer.pos(f16, 100.0D, -f16).tex(1.0D, 0.0D).endVertex();
                worldrenderer.pos(f16, 100.0D, f16).tex(1.0D, 1.0D).endVertex();
                worldrenderer.pos(-f16, 100.0D, f16).tex(0.0D, 1.0D).endVertex();
                tessellator.draw();
            }

            f16 = 20.0F;

            if (Config.isMoonTexture()) {
                renderEngine.bindTexture(locationMoonPhasesPng);
                int i = theWorld.getMoonPhase();
                int k = i % 4;
                int i1 = i / 4 % 2;
                float f19 = (float) (k) / 4.0F;
                float f21 = (float) (i1) / 2.0F;
                float f23 = (float) (k + 1) / 4.0F;
                float f14 = (float) (i1 + 1) / 2.0F;
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
                worldrenderer.pos(-f16, -100.0D, f16).tex(f23, f14).endVertex();
                worldrenderer.pos(f16, -100.0D, f16).tex(f19, f14).endVertex();
                worldrenderer.pos(f16, -100.0D, -f16).tex(f19, f21).endVertex();
                worldrenderer.pos(-f16, -100.0D, -f16).tex(f23, f21).endVertex();
                tessellator.draw();
            }

            GlStateManager.disableTexture2D();

            if (flag) {
                Shaders.disableTexture2D();
            }

            float f17 = theWorld.getStarBrightness(partialTicks) * f15;

            if (f17 > 0.0F && Config.isStarsEnabled() && !CustomSky.hasSkyLayers(theWorld)) {
                GlStateManager.color(f17, f17, f17, f17);

                if (vboEnabled) {
                    starVBO.bindBuffer();
                    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                    GL11.glVertexPointer(3, GL11.GL_FLOAT, 12, 0L);
                    starVBO.drawArrays(7);
                    starVBO.unbindBuffer();
                    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                } else {
                    GlStateManager.callList(starGLCallList);
                }
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableFog();

            if (flag) {
                Shaders.enableFog();
            }

            GlStateManager.popMatrix();
            GlStateManager.disableTexture2D();

            if (flag) {
                Shaders.disableTexture2D();
            }

            GlStateManager.color(0.0F, 0.0F, 0.0F);
            double d0 = mc.thePlayer.getPositionEyes(partialTicks).yCoord() - theWorld.getHorizon();

            if (d0 < 0.0D) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(0.0F, 12.0F, 0.0F);

                if (vboEnabled) {
                    sky2VBO.bindBuffer();
                    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                    GL11.glVertexPointer(3, GL11.GL_FLOAT, 12, 0L);
                    sky2VBO.drawArrays(7);
                    sky2VBO.unbindBuffer();
                    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                } else {
                    GlStateManager.callList(glSkyList2);
                }

                GlStateManager.popMatrix();
                float f20 = 1.0F;
                float f22 = -((float) (d0 + 65.0D));
                float f24 = -1.0F;
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                worldrenderer.pos(-1.0D, f22, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, f22, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, f22, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, f22, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, f22, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, f22, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, f22, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, f22, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
            }

            if (theWorld.provider.isSkyColored()) {
                GlStateManager.color(f * 0.2F + 0.04F, f1 * 0.2F + 0.04F, f2 * 0.6F + 0.1F);
            } else {
                GlStateManager.color(f, f1, f2);
            }

            if (mc.gameSettings.renderDistanceChunks <= 4) {
                GlStateManager.color(mc.entityRenderer.fogColorRed, mc.entityRenderer.fogColorGreen, mc.entityRenderer.fogColorBlue);
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, -((float) (d0 - 16.0D)), 0.0F);

            if (Config.isSkyEnabled()) {
                if (vboEnabled) {
                    sky2VBO.bindBuffer();
                    GlStateManager.glEnableClientState(32884);
                    GlStateManager.glVertexPointer(3, 5126, 12, 0);
                    sky2VBO.drawArrays(7);
                    sky2VBO.unbindBuffer();
                    GlStateManager.glDisableClientState(32884);
                } else {
                    GlStateManager.callList(glSkyList2);
                }
            }

            GlStateManager.popMatrix();
            GlStateManager.enableTexture2D();

            if (flag) {
                Shaders.enableTexture2D();
            }

            GlStateManager.depthMask(true);
        }
    }

    public void renderClouds(float partialTicks, int pass) {
        if (!Config.isCloudsOff()) {
            if (mc.theWorld.provider.isSurfaceWorld()) {
                if (Config.isShaders()) {
                    Shaders.beginClouds();
                }

                if (Config.isCloudsFancy()) {
                    renderCloudsFancy(partialTicks, pass);
                } else {
                    float f9 = partialTicks;
                    partialTicks = 0.0F;
                    GlStateManager.disableCull();
                    float f10 = (float) (mc.getRenderViewEntity().lastTickPosY + (mc.getRenderViewEntity().posY - mc.getRenderViewEntity().lastTickPosY) * (double) partialTicks);
                    int i = 32;
                    int j = 8;
                    Tessellator tessellator = Tessellator.getInstance();
                    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                    renderEngine.bindTexture(locationCloudsPng);
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                    Vec3 vec3 = theWorld.getCloudColour(partialTicks);
                    float f = (float) vec3.xCoord();
                    float f1 = (float) vec3.yCoord();
                    float f2 = (float) vec3.zCoord();
                    cloudRenderer.prepareToRender(false, cloudTickCounter, f9, vec3);

                    if (cloudRenderer.shouldUpdateGlList()) {
                        cloudRenderer.startUpdateGlList();

                        if (pass != 2) {
                            float f3 = (f * 30.0F + f1 * 59.0F + f2 * 11.0F) / 100.0F;
                            float f4 = (f * 30.0F + f1 * 70.0F) / 100.0F;
                            float f5 = (f * 30.0F + f2 * 70.0F) / 100.0F;
                            f = f3;
                            f1 = f4;
                            f2 = f5;
                        }

                        float f11 = 4.8828125E-4F;
                        double d2 = (float) cloudTickCounter + partialTicks;
                        double d0 = mc.getRenderViewEntity().prevPosX + (mc.getRenderViewEntity().posX - mc.getRenderViewEntity().prevPosX) * (double) partialTicks + d2 * 0.029999999329447746D;
                        double d1 = mc.getRenderViewEntity().prevPosZ + (mc.getRenderViewEntity().posZ - mc.getRenderViewEntity().prevPosZ) * (double) partialTicks;
                        int k = MathHelper.floor_double(d0 / 2048.0D);
                        int l = MathHelper.floor_double(d1 / 2048.0D);
                        d0 = d0 - (double) (k * 2048);
                        d1 = d1 - (double) (l * 2048);
                        float f6 = theWorld.provider.getCloudHeight() - f10 + 0.33F;
                        f6 = f6 + mc.gameSettings.ofCloudsHeight * 128.0F;
                        float f7 = (float) (d0 * 4.8828125E-4D);
                        float f8 = (float) (d1 * 4.8828125E-4D);
                        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

                        for (int i1 = -256; i1 < 256; i1 += 32) {
                            for (int j1 = -256; j1 < 256; j1 += 32) {
                                worldrenderer.pos(i1, f6, j1 + 32).tex((float) (i1) * 4.8828125E-4F + f7, (float) (j1 + 32) * 4.8828125E-4F + f8).color(f, f1, f2, 0.8F).endVertex();
                                worldrenderer.pos(i1 + 32, f6, j1 + 32).tex((float) (i1 + 32) * 4.8828125E-4F + f7, (float) (j1 + 32) * 4.8828125E-4F + f8).color(f, f1, f2, 0.8F).endVertex();
                                worldrenderer.pos(i1 + 32, f6, j1).tex((float) (i1 + 32) * 4.8828125E-4F + f7, (float) (j1) * 4.8828125E-4F + f8).color(f, f1, f2, 0.8F).endVertex();
                                worldrenderer.pos(i1, f6, j1).tex((float) (i1) * 4.8828125E-4F + f7, (float) (j1) * 4.8828125E-4F + f8).color(f, f1, f2, 0.8F).endVertex();
                            }
                        }

                        tessellator.draw();
                        cloudRenderer.endUpdateGlList();
                    }

                    cloudRenderer.renderGlList();
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.disableBlend();
                    GlStateManager.enableCull();
                }

                if (Config.isShaders()) {
                    Shaders.endClouds();
                }
            }
        }
    }

    public boolean hasCloudFog(double x, double y, double z, float partialTicks) {
        return false;
    }

    private void renderCloudsFancy(float partialTicks, int pass) {
        partialTicks = 0.0F;
        GlStateManager.disableCull();
        float f = (float) (mc.getRenderViewEntity().lastTickPosY + (mc.getRenderViewEntity().posY - mc.getRenderViewEntity().lastTickPosY) * (double) partialTicks);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        float f1 = 12.0F;
        float f2 = 4.0F;
        double d0 = (float) cloudTickCounter + partialTicks;
        double d1 = (mc.getRenderViewEntity().prevPosX + (mc.getRenderViewEntity().posX - mc.getRenderViewEntity().prevPosX) * (double) partialTicks + d0 * 0.029999999329447746D) / 12.0D;
        double d2 = (mc.getRenderViewEntity().prevPosZ + (mc.getRenderViewEntity().posZ - mc.getRenderViewEntity().prevPosZ) * (double) partialTicks) / 12.0D + 0.33000001311302185D;
        float f3 = theWorld.provider.getCloudHeight() - f + 0.33F;
        f3 = f3 + mc.gameSettings.ofCloudsHeight * 128.0F;
        int i = MathHelper.floor_double(d1 / 2048.0D);
        int j = MathHelper.floor_double(d2 / 2048.0D);
        d1 = d1 - (double) (i * 2048);
        d2 = d2 - (double) (j * 2048);
        renderEngine.bindTexture(locationCloudsPng);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Vec3 vec3 = theWorld.getCloudColour(partialTicks);
        float f4 = (float) vec3.xCoord();
        float f5 = (float) vec3.yCoord();
        float f6 = (float) vec3.zCoord();
        cloudRenderer.prepareToRender(true, cloudTickCounter, partialTicks, vec3);

        if (pass != 2) {
            float f7 = (f4 * 30.0F + f5 * 59.0F + f6 * 11.0F) / 100.0F;
            float f8 = (f4 * 30.0F + f5 * 70.0F) / 100.0F;
            float f9 = (f4 * 30.0F + f6 * 70.0F) / 100.0F;
            f4 = f7;
            f5 = f8;
            f6 = f9;
        }

        float f26 = f4 * 0.9F;
        float f27 = f5 * 0.9F;
        float f28 = f6 * 0.9F;
        float f10 = f4 * 0.7F;
        float f11 = f5 * 0.7F;
        float f12 = f6 * 0.7F;
        float f13 = f4 * 0.8F;
        float f14 = f5 * 0.8F;
        float f15 = f6 * 0.8F;
        float f16 = 0.00390625F;
        float f17 = (float) MathHelper.floor_double(d1) * 0.00390625F;
        float f18 = (float) MathHelper.floor_double(d2) * 0.00390625F;
        float f19 = (float) (d1 - (double) MathHelper.floor_double(d1));
        float f20 = (float) (d2 - (double) MathHelper.floor_double(d2));
        int k = 8;
        int l = 4;
        float f21 = 9.765625E-4F;
        GlStateManager.scale(12.0F, 1.0F, 12.0F);

        for (int i1 = 0; i1 < 2; ++i1) {
            if (i1 == 0) {
                GlStateManager.colorMask(false, false, false, false);
            } else {
                switch (pass) {
                    case 0:
                        GlStateManager.colorMask(false, true, true, true);
                        break;

                    case 1:
                        GlStateManager.colorMask(true, false, false, true);
                        break;

                    case 2:
                        GlStateManager.colorMask(true, true, true, true);
                }
            }

            cloudRenderer.renderGlList();
        }

        if (cloudRenderer.shouldUpdateGlList()) {
            cloudRenderer.startUpdateGlList();

            for (int l1 = -3; l1 <= 4; ++l1) {
                for (int j1 = -3; j1 <= 4; ++j1) {
                    worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
                    float f22 = (float) (l1 * 8);
                    float f23 = (float) (j1 * 8);
                    float f24 = f22 - f19;
                    float f25 = f23 - f20;

                    if (f3 > -5.0F) {
                        worldrenderer.pos(f24 + 0.0F, f3 + 0.0F, f25 + 8.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                        worldrenderer.pos(f24 + 8.0F, f3 + 0.0F, f25 + 8.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                        worldrenderer.pos(f24 + 8.0F, f3 + 0.0F, f25 + 0.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                        worldrenderer.pos(f24 + 0.0F, f3 + 0.0F, f25 + 0.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                    }

                    if (f3 <= 5.0F) {
                        worldrenderer.pos(f24 + 0.0F, f3 + 4.0F - 9.765625E-4F, f25 + 8.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                        worldrenderer.pos(f24 + 8.0F, f3 + 4.0F - 9.765625E-4F, f25 + 8.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                        worldrenderer.pos(f24 + 8.0F, f3 + 4.0F - 9.765625E-4F, f25 + 0.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                        worldrenderer.pos(f24 + 0.0F, f3 + 4.0F - 9.765625E-4F, f25 + 0.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                    }

                    if (l1 > -1) {
                        for (int k1 = 0; k1 < 8; ++k1) {
                            worldrenderer.pos(f24 + (float) k1 + 0.0F, f3 + 0.0F, f25 + 8.0F).tex((f22 + (float) k1 + 0.5F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos(f24 + (float) k1 + 0.0F, f3 + 4.0F, f25 + 8.0F).tex((f22 + (float) k1 + 0.5F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos(f24 + (float) k1 + 0.0F, f3 + 4.0F, f25 + 0.0F).tex((f22 + (float) k1 + 0.5F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos(f24 + (float) k1 + 0.0F, f3 + 0.0F, f25 + 0.0F).tex((f22 + (float) k1 + 0.5F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                        }
                    }

                    if (l1 <= 1) {
                        for (int i2 = 0; i2 < 8; ++i2) {
                            worldrenderer.pos(f24 + (float) i2 + 1.0F - 9.765625E-4F, f3 + 0.0F, f25 + 8.0F).tex((f22 + (float) i2 + 0.5F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos(f24 + (float) i2 + 1.0F - 9.765625E-4F, f3 + 4.0F, f25 + 8.0F).tex((f22 + (float) i2 + 0.5F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos(f24 + (float) i2 + 1.0F - 9.765625E-4F, f3 + 4.0F, f25 + 0.0F).tex((f22 + (float) i2 + 0.5F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos(f24 + (float) i2 + 1.0F - 9.765625E-4F, f3 + 0.0F, f25 + 0.0F).tex((f22 + (float) i2 + 0.5F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                        }
                    }

                    if (j1 > -1) {
                        for (int j2 = 0; j2 < 8; ++j2) {
                            worldrenderer.pos(f24 + 0.0F, f3 + 4.0F, f25 + (float) j2 + 0.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + (float) j2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                            worldrenderer.pos(f24 + 8.0F, f3 + 4.0F, f25 + (float) j2 + 0.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + (float) j2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                            worldrenderer.pos(f24 + 8.0F, f3 + 0.0F, f25 + (float) j2 + 0.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + (float) j2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                            worldrenderer.pos(f24 + 0.0F, f3 + 0.0F, f25 + (float) j2 + 0.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + (float) j2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                        }
                    }

                    if (j1 <= 1) {
                        for (int k2 = 0; k2 < 8; ++k2) {
                            worldrenderer.pos(f24 + 0.0F, f3 + 4.0F, f25 + (float) k2 + 1.0F - 9.765625E-4F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + (float) k2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                            worldrenderer.pos(f24 + 8.0F, f3 + 4.0F, f25 + (float) k2 + 1.0F - 9.765625E-4F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + (float) k2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                            worldrenderer.pos(f24 + 8.0F, f3 + 0.0F, f25 + (float) k2 + 1.0F - 9.765625E-4F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + (float) k2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                            worldrenderer.pos(f24 + 0.0F, f3 + 0.0F, f25 + (float) k2 + 1.0F - 9.765625E-4F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + (float) k2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                        }
                    }

                    tessellator.draw();
                }
            }

            cloudRenderer.endUpdateGlList();
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
    }

    public void updateChunks(long finishTimeNano) {
        finishTimeNano = (long) ((double) finishTimeNano + 1.0E8D);
        displayListEntitiesDirty |= renderDispatcher.runChunkUploads(finishTimeNano);

        if (!chunksToUpdateForced.isEmpty()) {
            Iterator iterator = chunksToUpdateForced.iterator();

            while (iterator.hasNext()) {
                RenderChunk renderchunk = (RenderChunk) iterator.next();

                if (!renderDispatcher.updateChunkLater(renderchunk)) {
                    break;
                }

                renderchunk.setNeedsUpdate(false);
                iterator.remove();
                chunksToUpdate.remove(renderchunk);
                chunksToResortTransparency.remove(renderchunk);
            }
        }

        if (!chunksToResortTransparency.isEmpty()) {
            Iterator iterator2 = chunksToResortTransparency.iterator();

            if (iterator2.hasNext()) {
                RenderChunk renderchunk2 = (RenderChunk) iterator2.next();

                if (renderDispatcher.updateTransparencyLater(renderchunk2)) {
                    iterator2.remove();
                }
            }
        }

        double d1 = 0.0D;
        int i = Config.getUpdatesPerFrame();

        if (!chunksToUpdate.isEmpty()) {
            Iterator<RenderChunk> iterator1 = chunksToUpdate.iterator();

            while (iterator1.hasNext()) {
                RenderChunk renderchunk1 = iterator1.next();
                boolean flag = renderchunk1.isChunkRegionEmpty();
                boolean flag1;

                if (flag) {
                    flag1 = renderDispatcher.updateChunkNow(renderchunk1);
                } else {
                    flag1 = renderDispatcher.updateChunkLater(renderchunk1);
                }

                if (!flag1) {
                    break;
                }

                renderchunk1.setNeedsUpdate(false);
                iterator1.remove();

                if (!flag) {
                    double d0 = 2.0D * RenderChunkUtils.getRelativeBufferSize(renderchunk1);
                    d1 += d0;

                    if (d1 > (double) i) {
                        break;
                    }
                }
            }
        }
    }

    public void renderWorldBorder(Entity entityIn, float partialTicks) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        WorldBorder worldborder = theWorld.getWorldBorder();
        double d0 = mc.gameSettings.renderDistanceChunks * 16;

        if (entityIn.posX >= worldborder.maxX() - d0 || entityIn.posX <= worldborder.minX() + d0 || entityIn.posZ >= worldborder.maxZ() - d0 || entityIn.posZ <= worldborder.minZ() + d0) {
            if (Config.isShaders()) {
                Shaders.pushProgram();
                Shaders.useProgram(Shaders.ProgramTexturedLit);
            }

            double d1 = 1.0D - worldborder.getClosestDistance(entityIn) / d0;
            d1 = Math.pow(d1, 4.0D);
            double d2 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
            double d3 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
            double d4 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
            renderEngine.bindTexture(locationForcefieldPng);
            GlStateManager.depthMask(false);
            GlStateManager.pushMatrix();
            int i = worldborder.getStatus().getID();
            float f = (float) (i >> 16 & 255) / 255.0F;
            float f1 = (float) (i >> 8 & 255) / 255.0F;
            float f2 = (float) (i & 255) / 255.0F;
            GlStateManager.color(f, f1, f2, (float) d1);
            GlStateManager.doPolygonOffset(-3.0F, -3.0F);
            GlStateManager.enablePolygonOffset();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.enableAlpha();
            GlStateManager.disableCull();
            float f3 = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F;
            float f4 = 0.0F;
            float f5 = 0.0F;
            float f6 = 128.0F;
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.setTranslation(-d2, -d3, -d4);
            double d5 = Math.max(MathHelper.floor_double(d4 - d0), worldborder.minZ());
            double d6 = Math.min(MathHelper.ceiling_double_int(d4 + d0), worldborder.maxZ());

            if (d2 > worldborder.maxX() - d0) {
                float f7 = 0.0F;

                for (double d7 = d5; d7 < d6; f7 += 0.5F) {
                    double d8 = Math.min(1.0D, d6 - d7);
                    float f8 = (float) d8 * 0.5F;
                    worldrenderer.pos(worldborder.maxX(), 256.0D, d7).tex(f3 + f7, f3 + 0.0F).endVertex();
                    worldrenderer.pos(worldborder.maxX(), 256.0D, d7 + d8).tex(f3 + f8 + f7, f3 + 0.0F).endVertex();
                    worldrenderer.pos(worldborder.maxX(), 0.0D, d7 + d8).tex(f3 + f8 + f7, f3 + 128.0F).endVertex();
                    worldrenderer.pos(worldborder.maxX(), 0.0D, d7).tex(f3 + f7, f3 + 128.0F).endVertex();
                    ++d7;
                }
            }

            if (d2 < worldborder.minX() + d0) {
                float f9 = 0.0F;

                for (double d9 = d5; d9 < d6; f9 += 0.5F) {
                    double d12 = Math.min(1.0D, d6 - d9);
                    float f12 = (float) d12 * 0.5F;
                    worldrenderer.pos(worldborder.minX(), 256.0D, d9).tex(f3 + f9, f3 + 0.0F).endVertex();
                    worldrenderer.pos(worldborder.minX(), 256.0D, d9 + d12).tex(f3 + f12 + f9, f3 + 0.0F).endVertex();
                    worldrenderer.pos(worldborder.minX(), 0.0D, d9 + d12).tex(f3 + f12 + f9, f3 + 128.0F).endVertex();
                    worldrenderer.pos(worldborder.minX(), 0.0D, d9).tex(f3 + f9, f3 + 128.0F).endVertex();
                    ++d9;
                }
            }

            d5 = Math.max(MathHelper.floor_double(d2 - d0), worldborder.minX());
            d6 = Math.min(MathHelper.ceiling_double_int(d2 + d0), worldborder.maxX());

            if (d4 > worldborder.maxZ() - d0) {
                float f10 = 0.0F;

                for (double d10 = d5; d10 < d6; f10 += 0.5F) {
                    double d13 = Math.min(1.0D, d6 - d10);
                    float f13 = (float) d13 * 0.5F;
                    worldrenderer.pos(d10, 256.0D, worldborder.maxZ()).tex(f3 + f10, f3 + 0.0F).endVertex();
                    worldrenderer.pos(d10 + d13, 256.0D, worldborder.maxZ()).tex(f3 + f13 + f10, f3 + 0.0F).endVertex();
                    worldrenderer.pos(d10 + d13, 0.0D, worldborder.maxZ()).tex(f3 + f13 + f10, f3 + 128.0F).endVertex();
                    worldrenderer.pos(d10, 0.0D, worldborder.maxZ()).tex(f3 + f10, f3 + 128.0F).endVertex();
                    ++d10;
                }
            }

            if (d4 < worldborder.minZ() + d0) {
                float f11 = 0.0F;

                for (double d11 = d5; d11 < d6; f11 += 0.5F) {
                    double d14 = Math.min(1.0D, d6 - d11);
                    float f14 = (float) d14 * 0.5F;
                    worldrenderer.pos(d11, 256.0D, worldborder.minZ()).tex(f3 + f11, f3 + 0.0F).endVertex();
                    worldrenderer.pos(d11 + d14, 256.0D, worldborder.minZ()).tex(f3 + f14 + f11, f3 + 0.0F).endVertex();
                    worldrenderer.pos(d11 + d14, 0.0D, worldborder.minZ()).tex(f3 + f14 + f11, f3 + 128.0F).endVertex();
                    worldrenderer.pos(d11, 0.0D, worldborder.minZ()).tex(f3 + f11, f3 + 128.0F).endVertex();
                    ++d11;
                }
            }

            tessellator.draw();
            worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
            GlStateManager.enableCull();
            GlStateManager.disableAlpha();
            GlStateManager.doPolygonOffset(0.0F, 0.0F);
            GlStateManager.disablePolygonOffset();
            GlStateManager.enableAlpha();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
            GlStateManager.depthMask(true);

            if (Config.isShaders()) {
                Shaders.popProgram();
            }
        }
    }

    private void preRenderDamagedBlocks() {
        GlStateManager.tryBlendFuncSeparate(774, 768, 1, 0);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
        GlStateManager.doPolygonOffset(-1.0F, -10.0F);
        GlStateManager.enablePolygonOffset();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableAlpha();
        GlStateManager.pushMatrix();

        if (Config.isShaders()) {
            ShadersRender.beginBlockDamage();
        }
    }

    private void postRenderDamagedBlocks() {
        GlStateManager.disableAlpha();
        GlStateManager.doPolygonOffset(0.0F, 0.0F);
        GlStateManager.disablePolygonOffset();
        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();

        if (Config.isShaders()) {
            ShadersRender.endBlockDamage();
        }
    }

    public void drawBlockDamageTexture(Tessellator tessellatorIn, WorldRenderer worldRendererIn, Entity entityIn, float partialTicks) {
        double d0 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
        double d1 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
        double d2 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;

        if (!damagedBlocks.isEmpty()) {
            renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            preRenderDamagedBlocks();
            worldRendererIn.begin(7, DefaultVertexFormats.BLOCK);
            worldRendererIn.setTranslation(-d0, -d1, -d2);
            worldRendererIn.noColor();
            Iterator<DestroyBlockProgress> iterator = damagedBlocks.values().iterator();

            while (iterator.hasNext()) {
                DestroyBlockProgress destroyblockprogress = iterator.next();
                BlockPos blockpos = destroyblockprogress.getPosition();
                double d3 = (double) blockpos.getX() - d0;
                double d4 = (double) blockpos.getY() - d1;
                double d5 = (double) blockpos.getZ() - d2;
                Block block = theWorld.getBlockState(blockpos).getBlock();
                boolean flag;

                flag = !(block instanceof BlockChest) && !(block instanceof BlockEnderChest) && !(block instanceof BlockSign) && !(block instanceof BlockSkull);

                if (flag) {
                    if (d3 * d3 + d4 * d4 + d5 * d5 > 1024.0D) {
                        iterator.remove();
                    } else {
                        IBlockState iblockstate = theWorld.getBlockState(blockpos);

                        if (iblockstate.getBlock().getMaterial() != Material.air) {
                            int i = destroyblockprogress.getPartialBlockDamage();
                            TextureAtlasSprite textureatlassprite = destroyBlockIcons[i];
                            BlockRendererDispatcher blockrendererdispatcher = mc.getBlockRendererDispatcher();
                            blockrendererdispatcher.renderBlockDamage(iblockstate, blockpos, textureatlassprite, theWorld);
                        }
                    }
                }
            }

            tessellatorIn.draw();
            worldRendererIn.setTranslation(0.0D, 0.0D, 0.0D);
            postRenderDamagedBlocks();
        }
    }

    public void drawSelectionBox(EntityPlayer player, MovingObjectPosition movingObjectPositionIn, int execute, float partialTicks) {
        if (execute == 0 && movingObjectPositionIn.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
            GL11.glLineWidth(2.0F);
            GlStateManager.disableTexture2D();

            if (Config.isShaders()) {
                Shaders.disableTexture2D();
            }

            GlStateManager.depthMask(false);
            float f = 0.002F;
            BlockPos blockpos = movingObjectPositionIn.getBlockPos();
            Block block = theWorld.getBlockState(blockpos).getBlock();

            if (block.getMaterial() != Material.air && theWorld.getWorldBorder().contains(blockpos)) {
                block.setBlockBoundsBasedOnState(theWorld, blockpos);
                double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
                double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
                double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;
                AxisAlignedBB axisalignedbb = block.getSelectedBoundingBox(theWorld, blockpos);
                Block.EnumOffsetType block$enumoffsettype = block.getOffsetType();

                if (block$enumoffsettype != Block.EnumOffsetType.NONE) {
                    axisalignedbb = BlockModelUtils.getOffsetBoundingBox(axisalignedbb, block$enumoffsettype, blockpos);
                }

                drawSelectionBoundingBox(axisalignedbb.expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D).offset(-d0, -d1, -d2));
            }

            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();

            if (Config.isShaders()) {
                Shaders.enableTexture2D();
            }

            GlStateManager.disableBlend();
        }
    }

    private void markBlocksForUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
        viewFrustum.markBlocksForUpdate(x1, y1, z1, x2, y2, z2);
    }

    public void markBlockForUpdate(BlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        markBlocksForUpdate(i - 1, j - 1, k - 1, i + 1, j + 1, k + 1);
    }

    public void notifyLightSet(BlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        markBlocksForUpdate(i - 1, j - 1, k - 1, i + 1, j + 1, k + 1);
    }

    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
        markBlocksForUpdate(x1 - 1, y1 - 1, z1 - 1, x2 + 1, y2 + 1, z2 + 1);
    }

    public void playRecord(String recordName, BlockPos blockPosIn) {
        ISound isound = mapSoundPositions.get(blockPosIn);

        if (isound != null) {
            mc.getSoundHandler().stopSound(isound);
            mapSoundPositions.remove(blockPosIn);
        }

        if (recordName != null) {
            ItemRecord itemrecord = ItemRecord.getRecord(recordName);

            if (itemrecord != null) {
                mc.ingameGUI.setRecordPlayingMessage(itemrecord.getRecordNameLocal());
            }

            PositionedSoundRecord positionedsoundrecord = PositionedSoundRecord.create(new ResourceLocation(recordName), (float) blockPosIn.getX(), (float) blockPosIn.getY(), (float) blockPosIn.getZ());
            mapSoundPositions.put(blockPosIn, positionedsoundrecord);
            mc.getSoundHandler().playSound(positionedsoundrecord);
        }
    }

    public void playSound(String soundName, double x, double y, double z, float volume, float pitch) {
    }

    public void playSoundToNearExcept(EntityPlayer except, String soundName, double x, double y, double z, float volume, float pitch) {
    }

    public void spawnParticle(int particleID, boolean ignoreRange, final double xCoord, final double yCoord, final double zCoord, double xOffset, double yOffset, double zOffset, int... parameters) {
        try {
            spawnEntityFX(particleID, ignoreRange, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, parameters);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception while adding particle");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being added");
            crashreportcategory.addCrashSection("ID", particleID);

            if (parameters != null) {
                crashreportcategory.addCrashSection("Parameters", parameters);
            }

            crashreportcategory.addCrashSectionCallable("Position", new Callable<String>() {
                public String call() throws Exception {
                    return CrashReportCategory.getCoordinateInfo(xCoord, yCoord, zCoord);
                }
            });
            throw new ReportedException(crashreport);
        }
    }

    private void spawnParticle(EnumParticleTypes particleIn, double xCoord, double yCoord, double zCoord, double xOffset, double yOffset, double zOffset, int... parameters) {
        spawnParticle(particleIn.getParticleID(), particleIn.getShouldIgnoreRange(), xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, parameters);
    }

    private EntityFX spawnEntityFX(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xOffset, double yOffset, double zOffset, int... parameters) {
        if (mc != null && mc.getRenderViewEntity() != null && mc.effectRenderer != null) {
            int i = mc.gameSettings.particleSetting;

            if (i == 1 && theWorld.rand.nextInt(3) == 0) {
                i = 2;
            }

            double d0 = mc.getRenderViewEntity().posX - xCoord;
            double d1 = mc.getRenderViewEntity().posY - yCoord;
            double d2 = mc.getRenderViewEntity().posZ - zCoord;

            if (particleID == EnumParticleTypes.EXPLOSION_HUGE.getParticleID() && !Config.isAnimatedExplosion()) {
                return null;
            } else if (particleID == EnumParticleTypes.EXPLOSION_LARGE.getParticleID() && !Config.isAnimatedExplosion()) {
                return null;
            } else if (particleID == EnumParticleTypes.EXPLOSION_NORMAL.getParticleID() && !Config.isAnimatedExplosion()) {
                return null;
            } else if (particleID == EnumParticleTypes.SUSPENDED.getParticleID() && !Config.isWaterParticles()) {
                return null;
            } else if (particleID == EnumParticleTypes.SUSPENDED_DEPTH.getParticleID() && !Config.isVoidParticles()) {
                return null;
            } else if (particleID == EnumParticleTypes.SMOKE_NORMAL.getParticleID() && !Config.isAnimatedSmoke()) {
                return null;
            } else if (particleID == EnumParticleTypes.SMOKE_LARGE.getParticleID() && !Config.isAnimatedSmoke()) {
                return null;
            } else if (particleID == EnumParticleTypes.SPELL_MOB.getParticleID() && !Config.isPotionParticles()) {
                return null;
            } else if (particleID == EnumParticleTypes.SPELL_MOB_AMBIENT.getParticleID() && !Config.isPotionParticles()) {
                return null;
            } else if (particleID == EnumParticleTypes.SPELL.getParticleID() && !Config.isPotionParticles()) {
                return null;
            } else if (particleID == EnumParticleTypes.SPELL_INSTANT.getParticleID() && !Config.isPotionParticles()) {
                return null;
            } else if (particleID == EnumParticleTypes.SPELL_WITCH.getParticleID() && !Config.isPotionParticles()) {
                return null;
            } else if (particleID == EnumParticleTypes.PORTAL.getParticleID() && !Config.isPortalParticles()) {
                return null;
            } else if (particleID == EnumParticleTypes.FLAME.getParticleID() && !Config.isAnimatedFlame()) {
                return null;
            } else if (particleID == EnumParticleTypes.REDSTONE.getParticleID() && !Config.isAnimatedRedstone()) {
                return null;
            } else if (particleID == EnumParticleTypes.DRIP_WATER.getParticleID() && !Config.isDrippingWaterLava()) {
                return null;
            } else if (particleID == EnumParticleTypes.DRIP_LAVA.getParticleID() && !Config.isDrippingWaterLava()) {
                return null;
            } else if (particleID == EnumParticleTypes.FIREWORKS_SPARK.getParticleID() && !Config.isFireworkParticles()) {
                return null;
            } else {
                if (!ignoreRange) {
                    double d3 = 256.0D;

                    if (particleID == EnumParticleTypes.CRIT.getParticleID()) {
                        d3 = 38416.0D;
                    }

                    if (d0 * d0 + d1 * d1 + d2 * d2 > d3) {
                        return null;
                    }

                    if (i > 1) {
                        return null;
                    }
                }

                EntityFX entityfx = mc.effectRenderer.spawnEffectParticle(particleID, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, parameters);

                if (particleID == EnumParticleTypes.WATER_BUBBLE.getParticleID()) {
                    CustomColors.updateWaterFX(entityfx, theWorld, xCoord, yCoord, zCoord, renderEnv);
                }

                if (particleID == EnumParticleTypes.WATER_SPLASH.getParticleID()) {
                    CustomColors.updateWaterFX(entityfx, theWorld, xCoord, yCoord, zCoord, renderEnv);
                }

                if (particleID == EnumParticleTypes.WATER_DROP.getParticleID()) {
                    CustomColors.updateWaterFX(entityfx, theWorld, xCoord, yCoord, zCoord, renderEnv);
                }

                if (particleID == EnumParticleTypes.TOWN_AURA.getParticleID()) {
                    CustomColors.updateMyceliumFX(entityfx);
                }

                if (particleID == EnumParticleTypes.PORTAL.getParticleID()) {
                    CustomColors.updatePortalFX(entityfx);
                }

                if (particleID == EnumParticleTypes.REDSTONE.getParticleID()) {
                    CustomColors.updateReddustFX(entityfx, theWorld, xCoord, yCoord, zCoord);
                }

                return entityfx;
            }
        } else {
            return null;
        }
    }

    public void onEntityAdded(Entity entityIn) {
        RandomEntities.entityLoaded(entityIn, theWorld);
    }

    public void onEntityRemoved(Entity entityIn) {
        if (Config.isDynamicLights()) {
            DynamicLights.entityRemoved(entityIn, this);
        }
    }

    public void deleteAllDisplayLists() {
    }

    public void broadcastSound(int soundID, BlockPos pos, int data) {
        switch (soundID) {
            case 1013:
            case 1018:
                if (mc.getRenderViewEntity() != null) {
                    double d0 = (double) pos.getX() - mc.getRenderViewEntity().posX;
                    double d1 = (double) pos.getY() - mc.getRenderViewEntity().posY;
                    double d2 = (double) pos.getZ() - mc.getRenderViewEntity().posZ;
                    double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                    double d4 = mc.getRenderViewEntity().posX;
                    double d5 = mc.getRenderViewEntity().posY;
                    double d6 = mc.getRenderViewEntity().posZ;

                    if (d3 > 0.0D) {
                        d4 += d0 / d3 * 2.0D;
                        d5 += d1 / d3 * 2.0D;
                        d6 += d2 / d3 * 2.0D;
                    }

                    if (soundID == 1013) {
                        theWorld.playSound(d4, d5, d6, "mob.wither.spawn", 1.0F, 1.0F, false);
                    } else {
                        theWorld.playSound(d4, d5, d6, "mob.enderdragon.end", 5.0F, 1.0F, false);
                    }
                }

            default:
        }
    }

    public void playAuxSFX(EntityPlayer player, int sfxType, BlockPos blockPosIn, int data) {
        Random random = theWorld.rand;

        switch (sfxType) {
            case 1000:
                theWorld.playSoundAtPos(blockPosIn, "random.click", 1.0F, 1.0F, false);
                break;

            case 1001:
                theWorld.playSoundAtPos(blockPosIn, "random.click", 1.0F, 1.2F, false);
                break;

            case 1002:
                theWorld.playSoundAtPos(blockPosIn, "random.bow", 1.0F, 1.2F, false);
                break;

            case 1003:
                theWorld.playSoundAtPos(blockPosIn, "random.door_open", 1.0F, theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1004:
                theWorld.playSoundAtPos(blockPosIn, "random.fizz", 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F, false);
                break;

            case 1005:
                if (Item.getItemById(data) instanceof ItemRecord) {
                    theWorld.playRecord(blockPosIn, "records." + ((ItemRecord) Item.getItemById(data)).recordName);
                } else {
                    theWorld.playRecord(blockPosIn, null);
                }

                break;

            case 1006:
                theWorld.playSoundAtPos(blockPosIn, "random.door_close", 1.0F, theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1007:
                theWorld.playSoundAtPos(blockPosIn, "mob.ghast.charge", 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1008:
                theWorld.playSoundAtPos(blockPosIn, "mob.ghast.fireball", 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1009:
                theWorld.playSoundAtPos(blockPosIn, "mob.ghast.fireball", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1010:
                theWorld.playSoundAtPos(blockPosIn, "mob.zombie.wood", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1011:
                theWorld.playSoundAtPos(blockPosIn, "mob.zombie.metal", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1012:
                theWorld.playSoundAtPos(blockPosIn, "mob.zombie.woodbreak", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1014:
                theWorld.playSoundAtPos(blockPosIn, "mob.wither.shoot", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1015:
                theWorld.playSoundAtPos(blockPosIn, "mob.bat.takeoff", 0.05F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1016:
                theWorld.playSoundAtPos(blockPosIn, "mob.zombie.infect", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1017:
                theWorld.playSoundAtPos(blockPosIn, "mob.zombie.unfect", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1020:
                theWorld.playSoundAtPos(blockPosIn, "random.anvil_break", 1.0F, theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1021:
                theWorld.playSoundAtPos(blockPosIn, "random.anvil_use", 1.0F, theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1022:
                theWorld.playSoundAtPos(blockPosIn, "random.anvil_land", 0.3F, theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 2000:
                int i = data % 3 - 1;
                int j = data / 3 % 3 - 1;
                double d0 = (double) blockPosIn.getX() + (double) i * 0.6D + 0.5D;
                double d1 = (double) blockPosIn.getY() + 0.5D;
                double d2 = (double) blockPosIn.getZ() + (double) j * 0.6D + 0.5D;

                for (int i1 = 0; i1 < 10; ++i1) {
                    double d15 = random.nextDouble() * 0.2D + 0.01D;
                    double d16 = d0 + (double) i * 0.01D + (random.nextDouble() - 0.5D) * (double) j * 0.5D;
                    double d17 = d1 + (random.nextDouble() - 0.5D) * 0.5D;
                    double d18 = d2 + (double) j * 0.01D + (random.nextDouble() - 0.5D) * (double) i * 0.5D;
                    double d19 = (double) i * d15 + random.nextGaussian() * 0.01D;
                    double d20 = -0.03D + random.nextGaussian() * 0.01D;
                    double d21 = (double) j * d15 + random.nextGaussian() * 0.01D;
                    spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d16, d17, d18, d19, d20, d21);
                }

                return;

            case 2001:
                Block block = Block.getBlockById(data & 4095);

                if (block.getMaterial() != Material.air) {
                    mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(block.stepSound.getBreakSound()), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getFrequency() * 0.8F, (float) blockPosIn.getX() + 0.5F, (float) blockPosIn.getY() + 0.5F, (float) blockPosIn.getZ() + 0.5F));
                }

                mc.effectRenderer.addBlockDestroyEffects(blockPosIn, block.getStateFromMeta(data >> 12 & 255));
                break;

            case 2002:
                double d3 = blockPosIn.getX();
                double d4 = blockPosIn.getY();
                double d5 = blockPosIn.getZ();

                for (int k = 0; k < 8; ++k) {
                    spawnParticle(EnumParticleTypes.ITEM_CRACK, d3, d4, d5, random.nextGaussian() * 0.15D, random.nextDouble() * 0.2D, random.nextGaussian() * 0.15D, Item.getIdFromItem(Items.potionitem), data);
                }

                int j1 = Items.potionitem.getColorFromDamage(data);
                float f = (float) (j1 >> 16 & 255) / 255.0F;
                float f1 = (float) (j1 >> 8 & 255) / 255.0F;
                float f2 = (float) (j1 >> 0 & 255) / 255.0F;
                EnumParticleTypes enumparticletypes = EnumParticleTypes.SPELL;

                if (Items.potionitem.isEffectInstant(data)) {
                    enumparticletypes = EnumParticleTypes.SPELL_INSTANT;
                }

                for (int k1 = 0; k1 < 100; ++k1) {
                    double d7 = random.nextDouble() * 4.0D;
                    double d9 = random.nextDouble() * Math.PI * 2.0D;
                    double d11 = Math.cos(d9) * d7;
                    double d23 = 0.01D + random.nextDouble() * 0.5D;
                    double d24 = Math.sin(d9) * d7;
                    EntityFX entityfx = spawnEntityFX(enumparticletypes.getParticleID(), enumparticletypes.getShouldIgnoreRange(), d3 + d11 * 0.1D, d4 + 0.3D, d5 + d24 * 0.1D, d11, d23, d24);

                    if (entityfx != null) {
                        float f3 = 0.75F + random.nextFloat() * 0.25F;
                        entityfx.setRBGColorF(f * f3, f1 * f3, f2 * f3);
                        entityfx.multiplyVelocity((float) d7);
                    }
                }

                theWorld.playSoundAtPos(blockPosIn, "game.potion.smash", 1.0F, theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 2003:
                double d6 = (double) blockPosIn.getX() + 0.5D;
                double d8 = blockPosIn.getY();
                double d10 = (double) blockPosIn.getZ() + 0.5D;

                for (int l1 = 0; l1 < 8; ++l1) {
                    spawnParticle(EnumParticleTypes.ITEM_CRACK, d6, d8, d10, random.nextGaussian() * 0.15D, random.nextDouble() * 0.2D, random.nextGaussian() * 0.15D, Item.getIdFromItem(Items.ender_eye));
                }

                for (double d22 = 0.0D; d22 < (Math.PI * 2D); d22 += 0.15707963267948966D) {
                    spawnParticle(EnumParticleTypes.PORTAL, d6 + Math.cos(d22) * 5.0D, d8 - 0.4D, d10 + Math.sin(d22) * 5.0D, Math.cos(d22) * -5.0D, 0.0D, Math.sin(d22) * -5.0D);
                    spawnParticle(EnumParticleTypes.PORTAL, d6 + Math.cos(d22) * 5.0D, d8 - 0.4D, d10 + Math.sin(d22) * 5.0D, Math.cos(d22) * -7.0D, 0.0D, Math.sin(d22) * -7.0D);
                }

                return;

            case 2004:
                for (int l = 0; l < 20; ++l) {
                    double d12 = (double) blockPosIn.getX() + 0.5D + ((double) theWorld.rand.nextFloat() - 0.5D) * 2.0D;
                    double d13 = (double) blockPosIn.getY() + 0.5D + ((double) theWorld.rand.nextFloat() - 0.5D) * 2.0D;
                    double d14 = (double) blockPosIn.getZ() + 0.5D + ((double) theWorld.rand.nextFloat() - 0.5D) * 2.0D;
                    theWorld.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d12, d13, d14, 0.0D, 0.0D, 0.0D);
                    theWorld.spawnParticle(EnumParticleTypes.FLAME, d12, d13, d14, 0.0D, 0.0D, 0.0D);
                }

                return;

            case 2005:
                ItemDye.spawnBonemealParticles(theWorld, blockPosIn, data);
        }
    }

    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
        if (progress >= 0 && progress < 10) {
            DestroyBlockProgress destroyblockprogress = damagedBlocks.get(breakerId);

            if (destroyblockprogress == null || destroyblockprogress.getPosition().getX() != pos.getX() || destroyblockprogress.getPosition().getY() != pos.getY() || destroyblockprogress.getPosition().getZ() != pos.getZ()) {
                destroyblockprogress = new DestroyBlockProgress(breakerId, pos);
                damagedBlocks.put(breakerId, destroyblockprogress);
            }

            destroyblockprogress.setPartialBlockDamage(progress);
            destroyblockprogress.setCloudUpdateTick(cloudTickCounter);
        } else {
            damagedBlocks.remove(breakerId);
        }
    }

    public void setDisplayListEntitiesDirty() {
        displayListEntitiesDirty = true;
    }

    public boolean hasNoChunkUpdates() {
        return chunksToUpdate.isEmpty() && renderDispatcher.hasChunkUpdates();
    }

    public void resetClouds() {
        cloudRenderer.reset();
    }

    public int getCountRenderers() {
        return viewFrustum.renderChunks.length;
    }

    public int getCountActiveRenderers() {
        return renderInfos.size();
    }

    public int getCountEntitiesRendered() {
        return countEntitiesRendered;
    }

    public int getCountTileEntitiesRendered() {
        return countTileEntitiesRendered;
    }

    public int getCountLoadedChunks() {
        if (theWorld == null) {
            return 0;
        } else {
            IChunkProvider ichunkprovider = theWorld.getChunkProvider();

            if (ichunkprovider == null) {
                return 0;
            } else {
                if (ichunkprovider != worldChunkProvider) {
                    worldChunkProvider = ichunkprovider;
                    worldChunkProviderMap = null;
                }

                return worldChunkProviderMap == null ? 0 : worldChunkProviderMap.getNumHashElements();
            }
        }
    }

    public int getCountChunksToUpdate() {
        return chunksToUpdate.size();
    }

    public RenderChunk getRenderChunk(BlockPos p_getRenderChunk_1_) {
        return viewFrustum.getRenderChunk(p_getRenderChunk_1_);
    }

    public WorldClient getWorld() {
        return theWorld;
    }

    private void clearRenderInfos() {
        if (renderEntitiesCounter > 0) {
            renderInfos = new ArrayList(renderInfos.size() + 16);
            renderInfosEntities = new ArrayList(renderInfosEntities.size() + 16);
            renderInfosTileEntities = new ArrayList(renderInfosTileEntities.size() + 16);
        } else {
            renderInfos.clear();
            renderInfosEntities.clear();
            renderInfosTileEntities.clear();
        }
    }

    public void onPlayerPositionSet() {
        if (firstWorldLoad) {
            loadRenderers();
            firstWorldLoad = false;
        }
    }

    public void pauseChunkUpdates() {
        if (renderDispatcher != null) {
            renderDispatcher.pauseChunkUpdates();
        }
    }

    public void resumeChunkUpdates() {
        if (renderDispatcher != null) {
            renderDispatcher.resumeChunkUpdates();
        }
    }

    public void updateTileEntities(Collection<TileEntity> tileEntitiesToRemove, Collection<TileEntity> tileEntitiesToAdd) {
        synchronized (setTileEntities) {
            setTileEntities.removeAll(tileEntitiesToRemove);
            setTileEntities.addAll(tileEntitiesToAdd);
        }
    }

    public static class ContainerLocalRenderInformation {
        final RenderChunk renderChunk;
        EnumFacing facing;
        int setFacing;

        public ContainerLocalRenderInformation(RenderChunk p_i2_1_, EnumFacing p_i2_2_, int p_i2_3_) {
            renderChunk = p_i2_1_;
            facing = p_i2_2_;
            setFacing = p_i2_3_;
        }

        public void setFacingBit(byte p_setFacingBit_1_, EnumFacing p_setFacingBit_2_) {
            setFacing = setFacing | p_setFacingBit_1_ | 1 << p_setFacingBit_2_.ordinal();
        }

        public boolean isFacingBit(EnumFacing p_isFacingBit_1_) {
            return (setFacing & 1 << p_isFacingBit_1_.ordinal()) > 0;
        }

        private void initialize(EnumFacing p_initialize_1_, int p_initialize_2_) {
            facing = p_initialize_1_;
            setFacing = p_initialize_2_;
        }
    }
}
