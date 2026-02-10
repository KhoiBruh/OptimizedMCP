package net.minecraft.client.gui;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.src.Config;
import net.minecraft.util.*;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SkyBlock;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import net.optifine.SmartAnimations;
import net.optifine.TextureAnimations;
import net.optifine.util.MemoryMonitor;
import net.optifine.util.NativeMemory;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Map.Entry;

public class GuiOverlayDebug extends Gui {
    private final Minecraft mc;
    private final FontRenderer fontRenderer;
    private final String gpuVendor;
    private final String gpuRenderer;
    private final String gpuVersion;
    private String debugOF = null;
    private List<String> debugInfoLeft = null;
    private List<String> debugInfoRight = null;
    private long updateInfoLeftTimeMs = 0L;
    private long updateInfoRightTimeMs = 0L;

    public GuiOverlayDebug(Minecraft mc) {
        this.mc = mc;
        fontRenderer = mc.fontRendererObj;
        gpuVendor = GL11.glGetString(GL11.GL_VENDOR);
        gpuRenderer = GL11.glGetString(GL11.GL_RENDERER);
        gpuVersion = GL11.glGetString(GL11.GL_VERSION);
    }

    private static long bytesToMb(long bytes) {
        return bytes / 1024L / 1024L;
    }

    public void renderDebugInfo(ScaledResolution scaledResolutionIn) {
        mc.mcProfiler.startSection("debug");
        GlStateManager.pushMatrix();
        renderDebugInfoLeft();
        renderDebugInfoRight(scaledResolutionIn);
        GlStateManager.popMatrix();

        if (mc.gameSettings.showLagometer) {
            renderLagometer();
        }

        mc.mcProfiler.endSection();
    }

    private boolean isReducedDebug() {
        return mc.thePlayer.hasReducedDebug() || mc.gameSettings.reducedDebugInfo;
    }

    protected void renderDebugInfoLeft() {
        List<String> list = debugInfoLeft;

        if (list == null || System.currentTimeMillis() > updateInfoLeftTimeMs) {
            list = call();
            debugInfoLeft = list;
            updateInfoLeftTimeMs = System.currentTimeMillis() + 100L;
        }

        for (int i = 0; i < list.size(); ++i) {
            String s = list.get(i);

            if (!Strings.isNullOrEmpty(s)) {
                int j = fontRenderer.FONT_HEIGHT;
                int k = fontRenderer.getStringWidth(s);
                int l = 2;
                int i1 = 2 + j * i;
                drawRect(1, i1 - 1, 2 + k + 1, i1 + j - 1, -1873784752);
                fontRenderer.drawString(s, 2, i1, 14737632);
            }
        }
    }

    protected void renderDebugInfoRight(ScaledResolution scaledRes) {
        List<String> list = debugInfoRight;

        if (list == null || System.currentTimeMillis() > updateInfoRightTimeMs) {
            list = getDebugInfoRight();
            debugInfoRight = list;
            updateInfoRightTimeMs = System.currentTimeMillis() + 100L;
        }

        for (int i = 0; i < list.size(); ++i) {
            String s = list.get(i);

            if (!Strings.isNullOrEmpty(s)) {
                int j = fontRenderer.FONT_HEIGHT;
                int k = fontRenderer.getStringWidth(s);
                int l = scaledRes.getScaledWidth() - 2 - k;
                int i1 = 2 + j * i;
                drawRect(l - 1, i1 - 1, l + k + 1, i1 + j - 1, -1873784752);
                fontRenderer.drawString(s, l, i1, 14737632);
            }
        }
    }

    @SuppressWarnings("incomplete-switch")
    protected List<String> call() {
        BlockPos blockpos = new BlockPos(mc.getRenderViewEntity().posX,
                mc.getRenderViewEntity().getEntityBoundingBox().minY, mc.getRenderViewEntity().posZ);

        if (mc.debug != debugOF) {
            StringBuilder stringbuffer = new StringBuilder(mc.debug);
            int i = Config.getFpsMin();
            int j = mc.debug.indexOf(" fps ");

            if (j >= 0) {
                stringbuffer.insert(j, "/" + i);
            }

            if (Config.isSmoothFps()) {
                stringbuffer.append(" sf");
            }

            if (Config.isFastRender()) {
                stringbuffer.append(" fr");
            }

            if (Config.isAnisotropicFiltering()) {
                stringbuffer.append(" af");
            }

            if (Config.isAntialiasing()) {
                stringbuffer.append(" aa");
            }

            if (Config.isRenderRegions()) {
                stringbuffer.append(" reg");
            }

            if (Config.isShaders()) {
                stringbuffer.append(" sh");
            }

            mc.debug = stringbuffer.toString();
            debugOF = mc.debug;
        }

        StringBuilder stringbuilder = new StringBuilder();
        TextureMap texturemap = Config.getTextureMap();
        stringbuilder.append(", A: ");

        if (SmartAnimations.isActive()) {
            stringbuilder.append(texturemap.getCountAnimationsActive() + TextureAnimations.getCountAnimationsActive());
            stringbuilder.append("/");
        }

        stringbuilder.append(texturemap.getCountAnimations() + TextureAnimations.getCountAnimations());
        String s1 = stringbuilder.toString();

        if (isReducedDebug()) {
            return Lists.newArrayList(
                    "Minecraft 1.8.9 (" + mc.getVersion() + "/" + ClientBrandRetriever.getClientModName() + ")",
                    mc.debug,
                    mc.renderGlobal.getDebugInfoRenders(),
                    mc.renderGlobal.getDebugInfoEntities(),
                    "P: " + mc.effectRenderer.getStatistics() + ". T: " + mc.theWorld.getDebugLoadedEntities()
                            + s1,
                    mc.theWorld.getProviderName(), "",
                    String.format("Chunk-relative: %d %d %d", blockpos.getX() & 15, blockpos.getY() & 15,
                            blockpos.getZ() & 15));
        } else {
            Entity entity = mc.getRenderViewEntity();
            Direction enumfacing = entity.getHorizontalFacing();
            String s = switch (enumfacing) {
                case NORTH -> "Towards negative Z";
                case SOUTH -> "Towards positive Z";
                case WEST -> "Towards negative X";
                case EAST -> "Towards positive X";
                default -> "Invalid";
            };

            List<String> list = Lists
                    .newArrayList(
                            "Minecraft 1.8.9 (" + mc.getVersion() + "/" + ClientBrandRetriever.getClientModName()
                                    + ")",
                            mc.debug, mc.renderGlobal.getDebugInfoRenders(),
                            mc.renderGlobal.getDebugInfoEntities(),
                            "P: " + mc.effectRenderer.getStatistics() + ". T: "
                                    + mc.theWorld.getDebugLoadedEntities() + s1,
                            mc.theWorld.getProviderName(), "",
                            String.format("XYZ: %.3f / %.5f / %.3f", mc.getRenderViewEntity().posX,
                                    mc.getRenderViewEntity().getEntityBoundingBox().minY,
                                    mc.getRenderViewEntity().posZ),
                            String.format("Block: %d %d %d", blockpos.getX(), blockpos.getY(), blockpos.getZ()),
                            String.format("Chunk: %d %d %d in %d %d %d", blockpos.getX() & 15, blockpos.getY() & 15,
                                    blockpos.getZ() & 15, blockpos.getX() >> 4, blockpos.getY() >> 4,
                                    blockpos.getZ() >> 4),
                            String.format("Facing: %s (%s) (%.1f / %.1f)", enumfacing, s,
                                    MathHelper.wrapAngle(entity.rotationYaw),
                                    MathHelper.wrapAngle(entity.rotationPitch)));

            if (mc.theWorld != null && mc.theWorld.isBlockLoaded(blockpos)) {
                Chunk chunk = mc.theWorld.getChunkFromBlockCoords(blockpos);
                list.add("Biome: " + chunk.getBiome(blockpos, mc.theWorld.getWorldChunkManager()).biomeName);
                list.add("Light: " + chunk.getLightSubtracted(blockpos, 0) + " ("
                        + chunk.getLightFor(SkyBlock.SKY, blockpos) + " sky, "
                        + chunk.getLightFor(SkyBlock.BLOCK, blockpos) + " block)");
                DifficultyInstance difficultyinstance = mc.theWorld.getDifficultyForLocation(blockpos);

                if (mc.isIntegratedServerRunning() && mc.getIntegratedServer() != null) {
                    EntityPlayerMP entityplayermp = mc.getIntegratedServer().getConfigurationManager()
                            .getPlayerByUUID(mc.thePlayer.getUniqueID());

                    if (entityplayermp != null) {
                        DifficultyInstance difficultyinstance1 = mc.getIntegratedServer()
                                .getDifficultyAsync(entityplayermp.worldObj, new BlockPos(entityplayermp));

                        if (difficultyinstance1 != null) {
                            difficultyinstance = difficultyinstance1;
                        }
                    }
                }

                list.add(String.format("Local Difficulty: %.2f (Day %d)", difficultyinstance.getAdditionalDifficulty(),
                        mc.theWorld.getWorldTime() / 24000L));
            }

            if (mc.entityRenderer != null && mc.entityRenderer.isShaderActive()) {
                list.add("Shader: " + mc.entityRenderer.getShaderGroup().getShaderGroupName());
            }

            if (mc.objectMouseOver != null
                    && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                    && mc.objectMouseOver.getBlockPos() != null) {
                BlockPos blockpos1 = mc.objectMouseOver.getBlockPos();
                list.add(String.format("Looking at: %d %d %d", blockpos1.getX(), blockpos1.getY(), blockpos1.getZ()));
            }

            return list;
        }
    }

    protected List<String> getDebugInfoRight() {
        long i = Runtime.getRuntime().maxMemory();
        long j = Runtime.getRuntime().totalMemory();
        long k = Runtime.getRuntime().freeMemory();
        long l = j - k;
        List<String> list = Lists.newArrayList(
                String.format("Java: %s %dbit", System.getProperty("java.version"), mc.isJava64bit() ? 64 : 32),
                String.format("Mem: % 2d%% %03d/%03dMB", l * 100L / i, bytesToMb(l), bytesToMb(i)),
                String.format("Allocated: % 2d%% %03dMB", j * 100L / i, bytesToMb(j)),
                "",
                "GC: " + MemoryMonitor.getAllocationRateMb() + "MB/s",
                "",
                String.format("CPU: %s", OpenGlHelper.getCpu()),
                "",
                String.format("Display: %dx%d (%s)", Display.getWidth(), Display.getHeight(), gpuVendor),
                gpuRenderer,
                gpuVersion
        );
        long i1 = NativeMemory.getBufferAllocated();
        long j1 = NativeMemory.getBufferMaximum();
        String s = "Native: " + bytesToMb(i1) + "/" + bytesToMb(j1) + "MB";
        list.add(4, s);

        if (!isReducedDebug()) {
            if (mc.objectMouseOver != null
                    && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                    && mc.objectMouseOver.getBlockPos() != null) {
                BlockPos blockpos = mc.objectMouseOver.getBlockPos();
                IBlockState iblockstate = mc.theWorld.getBlockState(blockpos);

                if (mc.theWorld.getWorldType() != WorldType.DEBUG_WORLD) {
                    iblockstate = iblockstate.getBlock().getActualState(iblockstate, mc.theWorld, blockpos);
                }

                list.add("");
                list.add(String.valueOf(Block.blockRegistry.getNameForObject(iblockstate.getBlock())));

                for (Entry<IProperty, Comparable> entry : iblockstate.getProperties().entrySet()) {
                    String s1 = entry.getValue().toString();

                    if (entry.getValue() == Boolean.TRUE) {
                        s1 = ChatFormat.GREEN + s1;
                    } else if (entry.getValue() == Boolean.FALSE) {
                        s1 = ChatFormat.RED + s1;
                    }

                    list.add(entry.getKey().getName() + ": " + s1);
                }
            }

        }
        return list;
    }

    private void renderLagometer() {
    }

    private int getFrameColor(int p_181552_1_, int p_181552_2_, int p_181552_3_, int p_181552_4_) {
        return p_181552_1_ < p_181552_3_ ? blendColors(-16711936, -256, (float) p_181552_1_ / (float) p_181552_3_)
                : blendColors(-256, -65536,
                (float) (p_181552_1_ - p_181552_3_) / (float) (p_181552_4_ - p_181552_3_));
    }

    private int blendColors(int p_181553_1_, int p_181553_2_, float p_181553_3_) {
        int i = p_181553_1_ >> 24 & 255;
        int j = p_181553_1_ >> 16 & 255;
        int k = p_181553_1_ >> 8 & 255;
        int l = p_181553_1_ & 255;
        int i1 = p_181553_2_ >> 24 & 255;
        int j1 = p_181553_2_ >> 16 & 255;
        int k1 = p_181553_2_ >> 8 & 255;
        int l1 = p_181553_2_ & 255;
        int i2 = MathHelper.clamp((int) ((float) i + (float) (i1 - i) * p_181553_3_), 0, 255);
        int j2 = MathHelper.clamp((int) ((float) j + (float) (j1 - j) * p_181553_3_), 0, 255);
        int k2 = MathHelper.clamp((int) ((float) k + (float) (k1 - k) * p_181553_3_), 0, 255);
        int l2 = MathHelper.clamp((int) ((float) l + (float) (l1 - l) * p_181553_3_), 0, 255);
        return i2 << 24 | j2 << 16 | k2 << 8 | l2;
    }
}
