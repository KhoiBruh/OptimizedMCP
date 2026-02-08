package net.minecraft.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.achievement.GuiAchievement;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.main.GameConfiguration;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.*;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.Timer;
import net.minecraft.util.Util;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.util.glu.GLU;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class Minecraft implements IThreadListener {
    public static final boolean isRunningOnMac = Util.getOSType() == Util.EnumOS.OSX;
    private static final Logger logger = LogManager.getLogger();
    private static final ResourceLocation locationMojangPng = new ResourceLocation("textures/gui/title/mojang.png");
    private static final List<DisplayMode> macDisplayModes = Lists
            .newArrayList(new DisplayMode(2560, 1600), new DisplayMode(2880, 1800));
    public static byte[] memoryReserve = new byte[10485760];
    private static Minecraft theMinecraft;
    private static int debugFPS;
    public final File mcDataDir;
    public final FrameTimer frameTimer = new FrameTimer();
    public final Profiler mcProfiler = new Profiler();
    public final DefaultResourcePack mcDefaultResourcePack;
    private final File fileResourcepacks;
    private final PropertyMap profileProperties;
    private final Session session;
    private final File fileAssets;
    private final String launchedVersion;
    private final Proxy proxy;
    private final boolean jvm64bit;
    private final IMetadataSerializer metadataSerializer_ = new IMetadataSerializer();
    private final List<IResourcePack> defaultResourcePacks = Lists.newArrayList();
    private final MinecraftSessionService sessionService;
    private final Queue<FutureTask<?>> scheduledTasks = Queues.newArrayDeque();
    private final Thread mcThread = Thread.currentThread();
    public PlayerControllerMP playerController;
    public int displayWidth;
    public int displayHeight;
    public WorldClient theWorld;
    public RenderGlobal renderGlobal;
    public EntityPlayerSP thePlayer;
    public Entity pointedEntity;
    public EffectRenderer effectRenderer;
    public FontRenderer fontRendererObj;
    public FontRenderer standardGalacticFontRenderer;
    public GuiScreen currentScreen;
    public LoadingScreenRenderer loadingScreen;
    public EntityRenderer entityRenderer;
    public GuiAchievement guiAchievement;
    public GuiIngame ingameGUI;
    public boolean skipRenderWorld;
    public MovingObjectPosition objectMouseOver;
    public GameSettings gameSettings;
    public MouseHelper mouseHelper;
    public boolean inGameHasFocus;
    public String debug = "";
    public boolean renderChunksMany = true;
    long systemTime = getSystemTime();
    long startNanoTime = System.nanoTime();
    volatile boolean running = true;
    long debugUpdateTime = getSystemTime();
    int fpsCounter;
    long prevFrameTime = -1L;
    private ServerData currentServerData;
    private TextureManager renderEngine;
    private boolean fullscreen;
    private boolean hasCrashed;
    private CrashReport crashReporter;
    private final Timer timer = new Timer(20.0F);
    private RenderManager renderManager;
    private RenderItem renderItem;
    private ItemRenderer itemRenderer;
    private Entity renderViewEntity;
    private boolean isGamePaused;
    private int leftClickCounter;
    private final int tempDisplayWidth;
    private final int tempDisplayHeight;
    private IntegratedServer theIntegratedServer;
    private ISaveFormat saveLoader;
    private int rightClickDelayTimer;
    private String serverName;
    private int serverPort;
    private int joinPlayerCounter;
    private NetworkManager myNetworkManager;
    private boolean integratedServerIsRunning;
    private long debugCrashKeyPressTime = -1L;
    private IReloadableResourceManager mcResourceManager;
    private ResourcePackRepository mcResourcePackRepository;
    private LanguageManager mcLanguageManager;
    private Framebuffer framebufferMc;
    private TextureMap textureMapBlocks;
    private SoundHandler mcSoundHandler;
    private MusicTicker mcMusicTicker;
    private ResourceLocation mojangLogo;
    private SkinManager skinManager;
    private ModelManager modelManager;
    private BlockRendererDispatcher blockRenderDispatcher;
    private String debugProfilerName = "root";

    public Minecraft(GameConfiguration gameConfig) {
        theMinecraft = this;
        mcDataDir = gameConfig.folderInfo().mcDataDir();
        fileAssets = gameConfig.folderInfo().assetsDir();
        fileResourcepacks = gameConfig.folderInfo().resourcePacksDir();
        launchedVersion = gameConfig.gameInfo().version();
        profileProperties = gameConfig.userInfo().profileProperties();
        mcDefaultResourcePack = new DefaultResourcePack(
                (new ResourceIndex(gameConfig.folderInfo().assetsDir(), gameConfig.folderInfo().assetIndex()))
                        .getResourceMap());
        proxy = gameConfig.userInfo().proxy() == null ? Proxy.NO_PROXY : gameConfig.userInfo().proxy();
        sessionService = (new YggdrasilAuthenticationService(proxy,
                UUID.randomUUID().toString())).createMinecraftSessionService();
        session = gameConfig.userInfo().session();
        logger.info("Setting user: {}", session.username());
        logger.info("(Session ID is {})", session.getSessionID());
        displayWidth = gameConfig.displayInfo().width() > 0 ? gameConfig.displayInfo().width() : 1;
        displayHeight = gameConfig.displayInfo().height() > 0 ? gameConfig.displayInfo().height() : 1;
        tempDisplayWidth = gameConfig.displayInfo().width();
        tempDisplayHeight = gameConfig.displayInfo().height();
        fullscreen = gameConfig.displayInfo().fullscreen();
        jvm64bit = isJvm64bit();
        theIntegratedServer = new IntegratedServer(this);

        if (gameConfig.serverInfo().serverName() != null) {
            serverName = gameConfig.serverInfo().serverName();
            serverPort = gameConfig.serverInfo().serverPort();
        }

        ImageIO.setUseCache(false);
        Bootstrap.register();
    }

    private static boolean isJvm64bit() {
        String[] astring = new String[]{"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"};

        for (String s : astring) {
            String s1 = System.getProperty(s);

            if (s1 != null && s1.contains("64")) {
                return true;
            }
        }

        return false;
    }

    public static boolean isGuiEnabled() {
        return theMinecraft == null || !theMinecraft.gameSettings.hideGUI;
    }

    public static boolean isAmbientOcclusionEnabled() {
        return theMinecraft != null && theMinecraft.gameSettings.ambientOcclusion != 0;
    }

    public static Minecraft getMinecraft() {
        return theMinecraft;
    }

    public static void stopIntegratedServer() {
        if (theMinecraft != null) {
            IntegratedServer integratedserver = theMinecraft.theIntegratedServer;

            if (integratedserver != null) {
                integratedserver.stopServer();
            }
        }
    }

    public static long getSystemTime() {
        return Sys.getTime() * 1000L / Sys.getTimerResolution();
    }

    public static int getDebugFPS() {
        return debugFPS;
    }

    public static Map<String, String> getSessionInfo() {
        Map<String, String> map = Maps.newHashMap();
        map.put("X-Minecraft-Username", theMinecraft.session.username());
        map.put("X-Minecraft-UUID", theMinecraft.session.playerID());
        map.put("X-Minecraft-Version", "1.8.9");
        return map;
    }

    public void run() {
        running = true;

        try {
            startGame();
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Initializing game");
            crashreport.makeCategory("Initialization");
            displayCrashReport(addGraphicsAndWorldToCrashReport(crashreport));
            return;
        }

        while (true) {
            try {
                while (running) {
                    if (!hasCrashed || crashReporter == null) {
                        try {
                            runGameLoop();
                        } catch (OutOfMemoryError var10) {
                            freeMemory();
                            displayGuiScreen(new GuiMemoryErrorScreen());
                            System.gc();
                        }
                    } else {
                        displayCrashReport(crashReporter);
                    }
                }
            } catch (MinecraftError var12) {
                break;
            } catch (ReportedException reportedexception) {
                addGraphicsAndWorldToCrashReport(reportedexception.getCrashReport());
                freeMemory();
                logger.fatal("Reported exception thrown!", reportedexception);
                displayCrashReport(reportedexception.getCrashReport());
                break;
            } catch (Throwable throwable1) {
                CrashReport crashreport1 = addGraphicsAndWorldToCrashReport(new CrashReport("Unexpected error", throwable1));
                freeMemory();
                logger.fatal("Unreported exception thrown!", throwable1);
                displayCrashReport(crashreport1);
                break;
            } finally {
                shutdownMinecraftApplet();
            }
        }
    }

    private void startGame() throws LWJGLException {
        gameSettings = new GameSettings(this, mcDataDir);
        defaultResourcePacks.add(mcDefaultResourcePack);
        startTimerHackThread();

        if (gameSettings.overrideHeight > 0 && gameSettings.overrideWidth > 0) {
            displayWidth = gameSettings.overrideWidth;
            displayHeight = gameSettings.overrideHeight;
        }

        logger.info("LWJGL Version: {}", Sys.VERSION);
        setWindowIcon();
        setInitialDisplayMode();
        createDisplay();
        OpenGlHelper.initializeTextures();
        framebufferMc = new Framebuffer(displayWidth, displayHeight, true);
        framebufferMc.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        registerMetadataSerializers();
        mcResourcePackRepository = new ResourcePackRepository(fileResourcepacks,
                new File(mcDataDir, "server-resource-packs"), mcDefaultResourcePack, metadataSerializer_,
                gameSettings);
        mcResourceManager = new SimpleReloadableResourceManager(metadataSerializer_);
        mcLanguageManager = new LanguageManager(metadataSerializer_, gameSettings.language);
        mcResourceManager.registerReloadListener(mcLanguageManager);
        refreshResources();
        renderEngine = new TextureManager(mcResourceManager);
        mcResourceManager.registerReloadListener(renderEngine);
        drawSplashScreen(renderEngine);
        skinManager = new SkinManager(renderEngine, new File(fileAssets, "skins"), sessionService);
        saveLoader = new AnvilSaveConverter(new File(mcDataDir, "saves"));
        mcSoundHandler = new SoundHandler(mcResourceManager, gameSettings);
        mcResourceManager.registerReloadListener(mcSoundHandler);
        mcMusicTicker = new MusicTicker(this);
        fontRendererObj = new FontRenderer(gameSettings, new ResourceLocation("textures/font/ascii.png"),
                renderEngine, false);

        if (gameSettings.language != null) {
            fontRendererObj.setUnicodeFlag(isUnicode());
            fontRendererObj.setBidiFlag(mcLanguageManager.isCurrentLanguageBidirectional());
        }

        standardGalacticFontRenderer = new FontRenderer(gameSettings,
                new ResourceLocation("textures/font/ascii_sga.png"), renderEngine, false);
        mcResourceManager.registerReloadListener(fontRendererObj);
        mcResourceManager.registerReloadListener(standardGalacticFontRenderer);
        mcResourceManager.registerReloadListener(new GrassColorReloadListener());
        mcResourceManager.registerReloadListener(new FoliageColorReloadListener());
        AchievementList.openInventory.setStatStringFormatter(str -> {
            try {
                return String.format(str, GameSettings
                        .getKeyDisplayString(gameSettings.keyBindInventory.getKeyCode()));
            } catch (Exception exception) {
                return "Error: " + exception.getLocalizedMessage();
            }
        });
        mouseHelper = new MouseHelper();
        checkGLError("Pre startup");
        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(7425);
        GlStateManager.clearDepth(1.0D);
        GlStateManager.enableDepth();
        GlStateManager.depthFunc(515);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.cullFace(1029);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);
        checkGLError("Startup");
        textureMapBlocks = new TextureMap("textures");
        textureMapBlocks.setMipmapLevels(gameSettings.mipmapLevels);
        renderEngine.loadTickableTexture(TextureMap.locationBlocksTexture, textureMapBlocks);
        renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        textureMapBlocks.setBlurMipmapDirect(false, gameSettings.mipmapLevels > 0);
        modelManager = new ModelManager(textureMapBlocks);
        mcResourceManager.registerReloadListener(modelManager);
        renderItem = new RenderItem(renderEngine, modelManager);
        renderManager = new RenderManager(renderEngine, renderItem);
        itemRenderer = new ItemRenderer(this);
        mcResourceManager.registerReloadListener(renderItem);
        entityRenderer = new EntityRenderer(this, mcResourceManager);
        mcResourceManager.registerReloadListener(entityRenderer);
        blockRenderDispatcher = new BlockRendererDispatcher(modelManager.getBlockModelShapes(),
                gameSettings);
        mcResourceManager.registerReloadListener(blockRenderDispatcher);
        renderGlobal = new RenderGlobal(this);
        mcResourceManager.registerReloadListener(renderGlobal);
        guiAchievement = new GuiAchievement(this);
        GlStateManager.viewport(0, 0, displayWidth, displayHeight);
        effectRenderer = new EffectRenderer(theWorld, renderEngine);
        checkGLError("Post startup");
        ingameGUI = new GuiIngame(this);

        if (serverName != null) {
            displayGuiScreen(new GuiConnecting(new GuiMainMenu(), this, serverName, serverPort));
        } else {
            displayGuiScreen(new GuiMainMenu());
        }

        renderEngine.deleteTexture(mojangLogo);
        mojangLogo = null;
        loadingScreen = new LoadingScreenRenderer(this);

        if (gameSettings.fullScreen && !fullscreen) toggleFullscreen();

        try {
            Display.setVSyncEnabled(gameSettings.enableVsync);
        } catch (OpenGLException var2) {
            gameSettings.enableVsync = false;
            gameSettings.saveOptions();
        }

        renderGlobal.makeEntityOutlineShader();
    }

    private void registerMetadataSerializers() {
        metadataSerializer_.registerMetadataSectionType(new TextureMetadataSectionSerializer(),
                TextureMetadataSection.class);
        metadataSerializer_.registerMetadataSectionType(new FontMetadataSectionSerializer(),
                FontMetadataSection.class);
        metadataSerializer_.registerMetadataSectionType(new AnimationMetadataSectionSerializer(),
                AnimationMetadataSection.class);
        metadataSerializer_.registerMetadataSectionType(new PackMetadataSectionSerializer(),
                PackMetadataSection.class);
        metadataSerializer_.registerMetadataSectionType(new LanguageMetadataSectionSerializer(),
                LanguageMetadataSection.class);
    }

    private void createDisplay() throws LWJGLException {
        Display.setResizable(true);
        Display.setTitle("Minecraft 1.8.9");

        try {
            Display.create((new PixelFormat()).withDepthBits(24));
        } catch (LWJGLException lwjglexception) {
            logger.error("Couldn't set pixel format", lwjglexception);

            if (fullscreen) updateDisplayMode();

            Display.create();
        }
    }

    private void setInitialDisplayMode() throws LWJGLException {
        if (fullscreen) {
            Display.setFullscreen(true);
            DisplayMode displaymode = Display.getDisplayMode();
            displayWidth = Math.max(1, displaymode.getWidth());
            displayHeight = Math.max(1, displaymode.getHeight());
        } else Display.setDisplayMode(new DisplayMode(displayWidth, displayHeight));
    }

    private void setWindowIcon() {
        Util.EnumOS util$enumos = Util.getOSType();

        if (util$enumos != Util.EnumOS.OSX) {

            try(
                var inputstream = mcDefaultResourcePack.getInputStreamAssets(new ResourceLocation("icons/icon_16x16.png"));
                var inputstream1 = mcDefaultResourcePack.getInputStreamAssets(new ResourceLocation("icons/icon_32x32.png"))
            ) {
                if (inputstream != null && inputstream1 != null) Display.setIcon(
                        new ByteBuffer[]{readImageToBuffer(inputstream), readImageToBuffer(inputstream1)}
                );
            } catch (IOException ioexception) {
                logger.error("Couldn't set icon", ioexception);
            }
        }
    }

    public Framebuffer getFramebuffer() {
        return framebufferMc;
    }

    public String getVersion() {
        return launchedVersion;
    }

    private void startTimerHackThread() {
        Thread thread = new Thread("Timer hack thread") {
            public void run() {
                while (running) {
                    try {
                        Thread.sleep(2147483647L);
                    } catch (InterruptedException var2) {
                    }
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    public void crashed(CrashReport crash) {
        hasCrashed = true;
        crashReporter = crash;
    }

    public void displayCrashReport(CrashReport crashReportIn) {
        File file1 = new File(theMinecraft.mcDataDir, "crash-reports");
        File file2 = new File(file1,
                "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-client.txt");
        Bootstrap.printToSYSOUT(crashReportIn.getCompleteReport());

        if (crashReportIn.getFile() != null) {
            Bootstrap.printToSYSOUT("#@!@# Game crashed! Crash report saved to: #@!@# " + crashReportIn.getFile());
            System.exit(-1);
        } else if (crashReportIn.saveToFile(file2)) {
            Bootstrap.printToSYSOUT("#@!@# Game crashed! Crash report saved to: #@!@# " + file2.getAbsolutePath());
            System.exit(-1);
        } else {
            Bootstrap.printToSYSOUT("#@?@# Game crashed! Crash report could not be saved. #@?@#");
            System.exit(-2);
        }
    }

    public boolean isUnicode() {
        return mcLanguageManager.isCurrentLocaleUnicode() || gameSettings.forceUnicodeFont;
    }

    public void refreshResources() {
        List<IResourcePack> list = Lists.newArrayList(defaultResourcePacks);

        for (ResourcePackRepository.Entry resourcepackrepository$entry : mcResourcePackRepository.getRepositoryEntries()) {
            list.add(resourcepackrepository$entry.getResourcePack());
        }

        if (mcResourcePackRepository.getResourcePackInstance() != null)
            list.add(mcResourcePackRepository.getResourcePackInstance());

        try {
            mcResourceManager.reloadResources(list);
        } catch (RuntimeException runtimeexception) {
            logger.info("Caught error stitching, removing all assigned resourcepacks",
                    runtimeexception);
            list.clear();
            list.addAll(defaultResourcePacks);
            mcResourcePackRepository.setRepositories(Collections.emptyList());
            mcResourceManager.reloadResources(list);
            gameSettings.resourcePacks.clear();
            gameSettings.incompatibleResourcePacks.clear();
            gameSettings.saveOptions();
        }

        mcLanguageManager.parseLanguageMetadata(list);

        if (renderGlobal != null) renderGlobal.loadRenderers();
    }

    private ByteBuffer readImageToBuffer(InputStream imageStream) throws IOException {
        BufferedImage bufferedimage = ImageIO.read(imageStream);
        int[] rgb = bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), null, 0, bufferedimage.getWidth());
        ByteBuffer bytebuffer = ByteBuffer.allocate(4 * rgb.length);

        for (int i : rgb) {
            bytebuffer.putInt(i << 8 | i >> 24 & 255);
        }

        bytebuffer.flip();
        return bytebuffer;
    }

    private void updateDisplayMode() throws LWJGLException {
        Set<DisplayMode> set = Sets.newHashSet();
        Collections.addAll(set, Display.getAvailableDisplayModes());
        DisplayMode displaymode = Display.getDesktopDisplayMode();

        if (!set.contains(displaymode) && Util.getOSType() == Util.EnumOS.OSX) {
            label53:

            for (DisplayMode displaymode1 : macDisplayModes) {
                boolean flag = true;

                for (DisplayMode displaymode2 : set) {
                    if (displaymode2.getBitsPerPixel() == 32 && displaymode2.getWidth() == displaymode1.getWidth()
                            && displaymode2.getHeight() == displaymode1.getHeight()) {
                        flag = false;
                        break;
                    }
                }

                if (!flag) {
                    Iterator<DisplayMode> iterator = set.iterator();
                    DisplayMode displaymode3;

                    do {
                        if (!iterator.hasNext()) continue label53;

                        displaymode3 = iterator.next();
                    } while (
                            displaymode3.getBitsPerPixel() != 32
                            || displaymode3.getWidth() != displaymode1.getWidth() / 2
                            || displaymode3.getHeight() != displaymode1.getHeight() / 2
                    );

                    displaymode = displaymode3;
                }
            }
        }

        Display.setDisplayMode(displaymode);
        displayWidth = displaymode.getWidth();
        displayHeight = displaymode.getHeight();
    }

    private void drawSplashScreen(TextureManager textureManagerInstance) {
        ScaledResolution scaledresolution = new ScaledResolution(this);
        int i = scaledresolution.getScaleFactor();
        Framebuffer framebuffer = new Framebuffer(scaledresolution.getScaledWidth() * i,
                scaledresolution.getScaledHeight() * i, true);
        framebuffer.bindFramebuffer(false);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, scaledresolution.getScaledWidth(),
                scaledresolution.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();

        try (var inputstream = mcDefaultResourcePack.getInputStream(locationMojangPng)) {
            mojangLogo = textureManagerInstance.getDynamicTextureLocation("logo", new DynamicTexture(ImageIO.read(inputstream)));
            textureManagerInstance.bindTexture(mojangLogo);
        } catch (IOException ioexception) {
            logger.error("Unable to load logo: {}", locationMojangPng, ioexception);
        }

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(0.0D, displayHeight, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255)
                .endVertex();
        worldrenderer.pos(displayWidth, displayHeight, 0.0D).tex(0.0D, 0.0D)
                .color(255, 255, 255, 255).endVertex();
        worldrenderer.pos(displayWidth, 0.0D, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).endVertex();
        worldrenderer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).endVertex();
        tessellator.draw();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int j = 256;
        int k = 256;
        draw((scaledresolution.getScaledWidth() - j) / 2, (scaledresolution.getScaledHeight() - k) / 2, 0, 0, j, k,
                255, 255, 255, 255);
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        framebuffer.unbindFramebuffer();
        framebuffer.framebufferRender(scaledresolution.getScaledWidth() * i, scaledresolution.getScaledHeight() * i);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        updateDisplay();
    }

    public void draw(int posX, int posY, int texU, int texV, int width, int height, int red, int green, int blue, int alpha) {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(posX, posY + height, 0.0D)
                .tex((float) texU * f, (float) (texV + height) * f1)
                .color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(posX + width, posY + height, 0.0D)
                .tex((float) (texU + width) * f, (float) (texV + height) * f1)
                .color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(posX + width, posY, 0.0D)
                .tex((float) (texU + width) * f, (float) texV * f1).color(red, green, blue, alpha)
                .endVertex();
        worldrenderer.pos(posX, posY, 0.0D)
                .tex((float) texU * f, (float) texV * f1).color(red, green, blue, alpha)
                .endVertex();
        Tessellator.getInstance().draw();
    }

    public ISaveFormat getSaveLoader() {
        return saveLoader;
    }

    public void displayGuiScreen(GuiScreen guiScreenIn) {
        if (currentScreen != null) currentScreen.onGuiClosed();

        if (guiScreenIn == null && theWorld == null) {
            guiScreenIn = new GuiMainMenu();
        } else if (guiScreenIn == null && thePlayer.getHealth() <= 0.0F) {
            guiScreenIn = new GuiGameOver();
        }

        if (guiScreenIn instanceof GuiMainMenu) {
            gameSettings.showDebugInfo = false;
            ingameGUI.getChatGUI().clearChatMessages();
        }

        currentScreen = guiScreenIn;

        if (guiScreenIn != null) {
            setIngameNotInFocus();
            ScaledResolution scaledresolution = new ScaledResolution(this);
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
            guiScreenIn.setWorldAndResolution(this, i, j);
            skipRenderWorld = false;
        } else {
            mcSoundHandler.resumeSounds();
            setIngameFocus();
        }
    }

    private void checkGLError(String message) {
        int i = GL11.glGetError();

        if (i != 0) {
            String s = GLU.gluErrorString(i);
            logger.error("########## GL ERROR ##########");
            logger.error("@ {}", message);
            logger.error("{}: {}", i, s);
        }
    }

    public void shutdownMinecraftApplet() {
        try {
            logger.info("Stopping!");

            try {
                loadWorld(null);
            } catch (Throwable var5) {
            }

            mcSoundHandler.unloadSounds();
        } finally {
            Display.destroy();

            if (!hasCrashed) System.exit(0);
        }

        System.gc();
    }

    private void runGameLoop() throws IOException {
        long i = System.nanoTime();
        mcProfiler.startSection("root");

        if (Display.isCreated() && Display.isCloseRequested()) shutdown();

        if (isGamePaused && theWorld != null) {
            float f = timer.renderPartialTicks;
            timer.updateTimer();
            timer.renderPartialTicks = f;
        } else timer.updateTimer();

        mcProfiler.startSection("scheduledExecutables");

        synchronized (scheduledTasks) {
            while (!scheduledTasks.isEmpty()) {
                Util.runTask(scheduledTasks.poll(), logger);
            }
        }

        mcProfiler.endSection();
        long l = System.nanoTime();
        mcProfiler.startSection("tick");

        for (int j = 0; j < timer.elapsedTicks; ++j) {
            runTick();
        }

        mcProfiler.endStartSection("preRenderErrors");
        long i1 = System.nanoTime() - l;
        checkGLError("Pre render");
        mcProfiler.endStartSection("sound");
        mcSoundHandler.setListener(thePlayer, timer.renderPartialTicks);
        mcProfiler.endSection();
        mcProfiler.startSection("render");
        GlStateManager.pushMatrix();
        GlStateManager.clear(16640);
        framebufferMc.bindFramebuffer(true);
        mcProfiler.startSection("display");
        GlStateManager.enableTexture2D();

        if (thePlayer != null && thePlayer.isEntityInsideOpaqueBlock()) gameSettings.thirdPersonView = 0;

        mcProfiler.endSection();

        if (!skipRenderWorld) {
            mcProfiler.endStartSection("gameRenderer");
            entityRenderer.updateCameraAndRender(timer.renderPartialTicks, i);
            mcProfiler.endSection();
        }

        mcProfiler.endSection();

        if (gameSettings.showDebugInfo && gameSettings.showDebugProfilerChart && !gameSettings.hideGUI) {
            if (!mcProfiler.profilingEnabled) mcProfiler.clearProfiling();

            mcProfiler.profilingEnabled = true;
            displayDebugInfo(i1);
        } else {
            mcProfiler.profilingEnabled = false;
            prevFrameTime = System.nanoTime();
        }

        guiAchievement.updateAchievementWindow();
        framebufferMc.unbindFramebuffer();
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        framebufferMc.framebufferRender(displayWidth, displayHeight);
        GlStateManager.popMatrix();
        mcProfiler.startSection("root");
        updateDisplay();
        Thread.yield();
        checkGLError("Post render");
        ++fpsCounter;
        isGamePaused = isSingleplayer() && currentScreen != null && currentScreen.doesGuiPauseGame()
                && !theIntegratedServer.getPublic();
        long k = System.nanoTime();
        frameTimer.addFrame(k - startNanoTime);
        startNanoTime = k;

        while (getSystemTime() >= debugUpdateTime + 1000L) {
            debugFPS = fpsCounter;
            debug = String.format("%d fps (%d chunk update%s) T: %s%s%s%s%s",
                    debugFPS, RenderChunk.renderChunksUpdated,
                    RenderChunk.renderChunksUpdated != 1 ? "s" : "",
                    (float) gameSettings.limitFramerate == GameSettings.Options.FRAMERATE_LIMIT
                            .getValueMax() ? "inf" : Integer.valueOf(gameSettings.limitFramerate),
                    gameSettings.enableVsync ? " vsync" : "",
                    gameSettings.fancyGraphics ? "" : " fast",
                    gameSettings.clouds == 0 ? ""
                            : (gameSettings.clouds == 1 ? " fast-clouds" : " fancy-clouds"),
                    OpenGlHelper.useVbo() ? " vbo" : "");
            RenderChunk.renderChunksUpdated = 0;
            debugUpdateTime += 1000L;
            fpsCounter = 0;
        }

        if (isFramerateLimitBelowMax()) {
            mcProfiler.startSection("fpslimit_wait");
            Display.sync(getLimitFramerate());
            mcProfiler.endSection();
        }

        mcProfiler.endSection();
    }

    public void updateDisplay() {
        mcProfiler.startSection("display_update");
        Display.update();
        mcProfiler.endSection();
        checkWindowResize();
    }

    protected void checkWindowResize() {
        if (!fullscreen && Display.wasResized()) {
            int i = displayWidth;
            int j = displayHeight;
            displayWidth = Display.getWidth();
            displayHeight = Display.getHeight();

            if (displayWidth != i || displayHeight != j) {
                if (displayWidth <= 0) displayWidth = 1;
                if (displayHeight <= 0) displayHeight = 1;

                resize(displayWidth, displayHeight);
            }
        }
    }

    public int getLimitFramerate() {
        return theWorld == null && currentScreen != null ? 30 : gameSettings.limitFramerate;
    }

    public boolean isFramerateLimitBelowMax() {
        return (float) getLimitFramerate() < GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
    }

    public void freeMemory() {
        try {
            memoryReserve = new byte[0];
            renderGlobal.deleteAllDisplayLists();
        } catch (Throwable var3) {
        }

        try {
            System.gc();
            loadWorld(null);
        } catch (Throwable var2) {
        }

        System.gc();
    }

    private void updateDebugProfilerName(int keyCount) {
        List<Profiler.Result> list = mcProfiler.getProfilingData(debugProfilerName);

        if (list != null && !list.isEmpty()) {
            Profiler.Result profiler$result = list.removeFirst();

            if (keyCount == 0) {
                if (!profiler$result.field_76331_c.isEmpty()) {
                    int i = debugProfilerName.lastIndexOf('.');

                    if (i >= 0) debugProfilerName = debugProfilerName.substring(0, i);
                }
            } else {
                --keyCount;

                if (keyCount < list.size() && !((Profiler.Result) list.get(keyCount)).field_76331_c.equals("unspecified")) {
                    if (!debugProfilerName.isEmpty()) debugProfilerName = debugProfilerName + ".";

                    debugProfilerName = debugProfilerName + list.get(keyCount).field_76331_c;
                }
            }
        }
    }

    private void displayDebugInfo(long elapsedTicksTime) {
        if (mcProfiler.profilingEnabled) {
            List<Profiler.Result> list = mcProfiler.getProfilingData(debugProfilerName);
            Profiler.Result profiler$result = list.removeFirst();
            GlStateManager.clear(256);
            GlStateManager.matrixMode(5889);
            GlStateManager.enableColorMaterial();
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0D, displayWidth, displayHeight, 0.0D, 1000.0D, 3000.0D);
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0F, 0.0F, -2000.0F);
            GL11.glLineWidth(1.0F);
            GlStateManager.disableTexture2D();
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            int i = 160;
            int j = displayWidth - i - 10;
            int k = displayHeight - i * 2;
            GlStateManager.enableBlend();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            worldrenderer
                    .pos((float) j - (float) i * 1.1F, (float) k - (float) i * 0.6F - 16.0F, 0.0D)
                    .color(200, 0, 0, 0).endVertex();
            worldrenderer.pos((float) j - (float) i * 1.1F, k + i * 2, 0.0D).color(200, 0, 0, 0)
                    .endVertex();
            worldrenderer.pos((float) j + (float) i * 1.1F, k + i * 2, 0.0D).color(200, 0, 0, 0)
                    .endVertex();
            worldrenderer
                    .pos((float) j + (float) i * 1.1F, (float) k - (float) i * 0.6F - 16.0F, 0.0D)
                    .color(200, 0, 0, 0).endVertex();
            tessellator.draw();
            GlStateManager.disableBlend();
            double d0 = 0.0D;

            for (Profiler.Result profiler$result1 : list) {
                int i1 = MathHelper.floor_double(profiler$result1.field_76332_a / 4.0D) + 1;
                worldrenderer.begin(6, DefaultVertexFormats.POSITION_COLOR);
                int j1 = profiler$result1.getColor();
                int k1 = j1 >> 16 & 255;
                int l1 = j1 >> 8 & 255;
                int i2 = j1 & 255;
                worldrenderer.pos(j, k, 0.0D).color(k1, l1, i2, 255).endVertex();

                for (int j2 = i1; j2 >= 0; --j2) {
                    float f = (float) ((d0 + profiler$result1.field_76332_a * (double) j2 / (double) i1) * Math.PI
                            * 2.0D / 100.0D);
                    float f1 = MathHelper.sin(f) * (float) i;
                    float f2 = MathHelper.cos(f) * (float) i * 0.5F;
                    worldrenderer.pos((float) j + f1, (float) k - f2, 0.0D).color(k1, l1, i2, 255)
                            .endVertex();
                }

                tessellator.draw();
                worldrenderer.begin(5, DefaultVertexFormats.POSITION_COLOR);

                for (int i3 = i1; i3 >= 0; --i3) {
                    float f3 = (float) ((d0 + profiler$result1.field_76332_a * (double) i3 / (double) i1) * Math.PI
                            * 2.0D / 100.0D);
                    float f4 = MathHelper.sin(f3) * (float) i;
                    float f5 = MathHelper.cos(f3) * (float) i * 0.5F;
                    worldrenderer.pos((float) j + f4, (float) k - f5, 0.0D)
                            .color(k1 >> 1, l1 >> 1, i2 >> 1, 255).endVertex();
                    worldrenderer.pos((float) j + f4, (float) k - f5 + 10.0F, 0.0D)
                            .color(k1 >> 1, l1 >> 1, i2 >> 1, 255).endVertex();
                }

                tessellator.draw();
                d0 += profiler$result1.field_76332_a;
            }

            DecimalFormat decimalformat = new DecimalFormat("##0.00");
            GlStateManager.enableTexture2D();
            String s = "";

            if (!profiler$result.field_76331_c.equals("unspecified")) {
                s = s + "[0] ";
            }

            if (profiler$result.field_76331_c.isEmpty()) {
                s = s + "ROOT ";
            } else {
                s = s + profiler$result.field_76331_c + " ";
            }

            int l2 = 16777215;
            fontRendererObj.drawStringWithShadow(s, (float) (j - i), (float) (k - i / 2 - 16), l2);
            fontRendererObj.drawStringWithShadow(s = decimalformat.format(profiler$result.field_76330_b) + "%",
                    (float) (j + i - fontRendererObj.getStringWidth(s)), (float) (k - i / 2 - 16), l2);

            for (int k2 = 0; k2 < list.size(); ++k2) {
                Profiler.Result profiler$result2 = list.get(k2);
                String s1 = "";

                if (profiler$result2.field_76331_c.equals("unspecified")) {
                    s1 = s1 + "[?] ";
                } else {
                    s1 = s1 + "[" + (k2 + 1) + "] ";
                }

                s1 = s1 + profiler$result2.field_76331_c;
                fontRendererObj.drawStringWithShadow(s1, (float) (j - i), (float) (k + i / 2 + k2 * 8 + 20),
                        profiler$result2.getColor());
                fontRendererObj.drawStringWithShadow(
                        s1 = decimalformat.format(profiler$result2.field_76332_a) + "%",
                        (float) (j + i - 50 - fontRendererObj.getStringWidth(s1)),
                        (float) (k + i / 2 + k2 * 8 + 20), profiler$result2.getColor());
                fontRendererObj.drawStringWithShadow(
                        s1 = decimalformat.format(profiler$result2.field_76330_b) + "%",
                        (float) (j + i - fontRendererObj.getStringWidth(s1)), (float) (k + i / 2 + k2 * 8 + 20),
                        profiler$result2.getColor());
            }
        }
    }

    public void shutdown() {
        running = false;
    }

    public void setIngameFocus() {
        if (Display.isActive()) {
            if (!inGameHasFocus) {
                inGameHasFocus = true;
                mouseHelper.grabMouseCursor();
                displayGuiScreen(null);
                leftClickCounter = 10000;
            }
        }
    }

    public void setIngameNotInFocus() {
        if (inGameHasFocus) {
            KeyBinding.unPressAllKeys();
            inGameHasFocus = false;
            mouseHelper.ungrabMouseCursor();
        }
    }

    public void displayInGameMenu() {
        if (currentScreen == null) {
            displayGuiScreen(new GuiIngameMenu());

            if (isSingleplayer() && !theIntegratedServer.getPublic()) {
                mcSoundHandler.pauseSounds();
            }
        }
    }

    private void sendClickBlockToController(boolean leftClick) {
        if (!leftClick) leftClickCounter = 0;

        if (leftClickCounter <= 0 && !thePlayer.isUsingItem()) {
            if (leftClick && objectMouseOver != null
                    && objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                BlockPos blockpos = objectMouseOver.getBlockPos();

                if (theWorld.getBlockState(blockpos).getBlock().getMaterial() != Material.air
                        && playerController.onPlayerDamageBlock(blockpos, objectMouseOver.sideHit)) {
                    effectRenderer.addBlockHitEffects(blockpos, objectMouseOver.sideHit);
                    thePlayer.swingItem();
                }
            } else playerController.resetBlockRemoving();
        }
    }

    private void clickMouse() {
        if (leftClickCounter <= 0) {
            thePlayer.swingItem();

            if (objectMouseOver == null) {
                logger.error("Null returned as 'hitResult', this shouldn't happen!");

                if (playerController.isNotCreative()) leftClickCounter = 10;
            } else switch (objectMouseOver.typeOfHit) {
                case MISS -> {
                    if (playerController.isNotCreative()) leftClickCounter = 10;
                }

                case ENTITY -> playerController.attackEntity(thePlayer, objectMouseOver.entityHit);

                case BLOCK -> {
                    BlockPos blockpos = objectMouseOver.getBlockPos();

                    if (theWorld.getBlockState(blockpos).getBlock().getMaterial() != Material.air) {
                        playerController.clickBlock(blockpos, objectMouseOver.sideHit);
                    }
                }
            }
        }
    }

    private void rightClickMouse() {
        if (!playerController.getIsHittingBlock()) {
            rightClickDelayTimer = 4;
            boolean flag = true;
            ItemStack itemstack = thePlayer.inventory.getCurrentItem();

            if (objectMouseOver != null) {
                switch (objectMouseOver.typeOfHit) {
                    case ENTITY -> {
                        if (playerController.isPlayerRightClickingOnEntity(thePlayer, objectMouseOver.entityHit, objectMouseOver)) {
                            flag = false;
                        } else if (playerController.interactWithEntitySendPacket(thePlayer, objectMouseOver.entityHit)) {
                            flag = false;
                        }
                    }

                    case BLOCK -> {
                        BlockPos blockpos = objectMouseOver.getBlockPos();

                        if (theWorld.getBlockState(blockpos).getBlock().getMaterial() != Material.air) {
                            int i = itemstack != null ? itemstack.stackSize : 0;

                            if (playerController.onPlayerRightClick(thePlayer, theWorld, itemstack, blockpos, objectMouseOver.sideHit, objectMouseOver.hitVec)) {
                                flag = false;
                                thePlayer.swingItem();
                            }

                            if (itemstack == null) return;

                            if (itemstack.stackSize == 0) {
                                thePlayer.inventory.mainInventory[thePlayer.inventory.currentItem] = null;
                            } else if (itemstack.stackSize != i || playerController.isInCreativeMode()) {
                                entityRenderer.itemRenderer.resetEquippedProgress();
                            }
                        }
                    }
                }
            } else logger.warn("Null returned as 'hitResult', this shouldn't happen!");

            if (flag) {
                ItemStack itemstack1 = thePlayer.inventory.getCurrentItem();

                if (itemstack1 != null && playerController.sendUseItem(thePlayer, theWorld, itemstack1))
                    entityRenderer.itemRenderer.resetEquippedProgress2();
            }
        }
    }

    public void toggleFullscreen() {
        try {
            fullscreen = !fullscreen;
            gameSettings.fullScreen = fullscreen;

            if (fullscreen) {
                updateDisplayMode();
                displayWidth = Display.getDisplayMode().getWidth();
                displayHeight = Display.getDisplayMode().getHeight();

            } else {
                Display.setDisplayMode(new DisplayMode(tempDisplayWidth, tempDisplayHeight));
                displayWidth = tempDisplayWidth;
                displayHeight = tempDisplayHeight;
            }

            if (displayWidth <= 0) displayWidth = 1;
            if (displayHeight <= 0) displayHeight = 1;

            if (currentScreen != null) {
                resize(displayWidth, displayHeight);
            } else {
                updateFramebufferSize();
            }

            Display.setFullscreen(fullscreen);
            Display.setVSyncEnabled(gameSettings.enableVsync);
            updateDisplay();
        } catch (Exception exception) {
            logger.error("Couldn't toggle fullscreen", exception);
        }
    }

    private void resize(int width, int height) {
        displayWidth = Math.max(1, width);
        displayHeight = Math.max(1, height);

        if (currentScreen != null) {
            ScaledResolution scaledresolution = new ScaledResolution(this);
            currentScreen.onResize(this, scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight());
        }

        loadingScreen = new LoadingScreenRenderer(this);
        updateFramebufferSize();
    }

    private void updateFramebufferSize() {
        framebufferMc.createBindFramebuffer(displayWidth, displayHeight);
        if (entityRenderer != null) entityRenderer.updateShaderGroupSize(displayWidth, displayHeight);
    }

    public MusicTicker getMusicTicker() {
        return mcMusicTicker;
    }

    public void runTick() throws IOException {
        if (rightClickDelayTimer > 0) --rightClickDelayTimer;

        mcProfiler.startSection("gui");

        if (!isGamePaused) ingameGUI.updateTick();

        mcProfiler.endSection();
        entityRenderer.getMouseOver(1.0F);
        mcProfiler.startSection("gameMode");

        if (!isGamePaused && theWorld != null) playerController.updateController();

        mcProfiler.endStartSection("textures");

        if (!isGamePaused) renderEngine.tick();

        if (currentScreen == null && thePlayer != null) {
            if (thePlayer.getHealth() <= 0.0F) {
                displayGuiScreen(null);
            } else if (thePlayer.isPlayerSleeping() && theWorld != null) {
                displayGuiScreen(new GuiSleepMP());
            }
        } else if (currentScreen != null && currentScreen instanceof GuiSleepMP
                && !thePlayer.isPlayerSleeping()) {
            displayGuiScreen(null);
        }

        if (currentScreen != null) leftClickCounter = 10000;

        if (currentScreen != null) {
            try {
                currentScreen.handleInput();
            } catch (Throwable throwable1) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Updating screen events");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Affected screen");
                crashreportcategory.addCrashSectionCallable("Screen name", () -> currentScreen.getClass().getCanonicalName());
                throw new ReportedException(crashreport);
            }

            if (currentScreen != null) {
                try {
                    currentScreen.updateScreen();
                } catch (Throwable throwable) {
                    CrashReport crashreport1 = CrashReport.makeCrashReport(throwable, "Ticking screen");
                    CrashReportCategory crashreportcategory1 = crashreport1.makeCategory("Affected screen");
                    crashreportcategory1.addCrashSectionCallable("Screen name", () -> currentScreen.getClass().getCanonicalName());
                    throw new ReportedException(crashreport1);
                }
            }
        }

        if (currentScreen == null || currentScreen.allowUserInput) {
            mcProfiler.endStartSection("mouse");

            while (Mouse.next()) {
                int i = Mouse.getEventButton();
                KeyBinding.setKeyBindState(i - 100, Mouse.getEventButtonState());

                if (Mouse.getEventButtonState()) {
                    if (thePlayer.isSpectator() && i == 2) {
                        ingameGUI.getSpectatorGui().func_175261_b();
                    } else {
                        KeyBinding.onTick(i - 100);
                    }
                }

                long i1 = getSystemTime() - systemTime;

                if (i1 <= 200L) {
                    int j = Mouse.getEventDWheel();

                    if (j != 0) {
                        if (thePlayer.isSpectator()) {
                            j = j < 0 ? -1 : 1;

                            if (!ingameGUI.getSpectatorGui().func_175262_a()) {
                                float f = MathHelper.clamp_float(thePlayer.capabilities.getFlySpeed() + (float) j * 0.005F, 0.0F, 0.2F);
                                thePlayer.capabilities.setFlySpeed(f);
                            } else ingameGUI.getSpectatorGui().func_175259_b(-j);
                        } else thePlayer.inventory.changeCurrentItem(j);
                    }

                    if (currentScreen == null) {
                        if (!inGameHasFocus && Mouse.getEventButtonState()) setIngameFocus();
                    } else {
                        currentScreen.handleMouseInput();
                    }
                }
            }

            if (leftClickCounter > 0) --leftClickCounter;

            mcProfiler.endStartSection("keyboard");

            while (Keyboard.next()) {
                int k = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
                KeyBinding.setKeyBindState(k, Keyboard.getEventKeyState());

                if (Keyboard.getEventKeyState()) {
                    KeyBinding.onTick(k);
                }

                if (debugCrashKeyPressTime > 0L) {
                    if (getSystemTime() - debugCrashKeyPressTime >= 6000L) {
                        throw new ReportedException(new CrashReport("Manually triggered debug crash", new Throwable()));
                    }

                    if (!Keyboard.isKeyDown(46) || !Keyboard.isKeyDown(61)) {
                        debugCrashKeyPressTime = -1L;
                    }
                } else if (Keyboard.isKeyDown(46) && Keyboard.isKeyDown(61)) {
                    debugCrashKeyPressTime = getSystemTime();
                }

                dispatchKeypresses();

                if (Keyboard.getEventKeyState()) {
                    if (k == 62 && entityRenderer != null) {
                        entityRenderer.switchUseShader();
                    }

                    if (currentScreen == null) {
                        if (k == 1) displayInGameMenu();

                        if (k == 32 && Keyboard.isKeyDown(61) && ingameGUI != null) {
                            ingameGUI.getChatGUI().clearChatMessages();
                        }

                        if (k == 31 && Keyboard.isKeyDown(61)) refreshResources();

                        if (k == 20 && Keyboard.isKeyDown(61)) {
                            refreshResources();
                        }

                        if (k == 33 && Keyboard.isKeyDown(61)) {
                            gameSettings.setOptionValue(GameSettings.Options.RENDER_DISTANCE,
                                    GuiScreen.isShiftKeyDown() ? -1 : 1);
                        }

                        if (k == 30 && Keyboard.isKeyDown(61)) {
                            renderGlobal.loadRenderers();
                        }

                        if (k == 35 && Keyboard.isKeyDown(61)) {
                            gameSettings.advancedItemTooltips = !gameSettings.advancedItemTooltips;
                            gameSettings.saveOptions();
                        }

                        if (k == 48 && Keyboard.isKeyDown(61)) {
                            renderManager.setDebugBoundingBox(!renderManager.isDebugBoundingBox());
                        }

                        if (k == 25 && Keyboard.isKeyDown(61)) {
                            gameSettings.pauseOnLostFocus = !gameSettings.pauseOnLostFocus;
                            gameSettings.saveOptions();
                        }

                        if (k == 59) {
                            gameSettings.hideGUI = !gameSettings.hideGUI;
                        }

                        if (k == 61) {
                            gameSettings.showDebugInfo = !gameSettings.showDebugInfo;
                            gameSettings.showDebugProfilerChart = GuiScreen.isShiftKeyDown();
                            gameSettings.showLagometer = GuiScreen.isAltKeyDown();
                        }

                        if (gameSettings.keyBindTogglePerspective.isPressed()) {
                            ++gameSettings.thirdPersonView;

                            if (gameSettings.thirdPersonView > 2) {
                                gameSettings.thirdPersonView = 0;
                            }

                            if (gameSettings.thirdPersonView == 0) {
                                entityRenderer.loadEntityShader(renderViewEntity);
                            } else if (gameSettings.thirdPersonView == 1) {
                                entityRenderer.loadEntityShader(null);
                            }

                            renderGlobal.setDisplayListEntitiesDirty();
                        }

                        if (gameSettings.keyBindSmoothCamera.isPressed()) {
                            gameSettings.smoothCamera = !gameSettings.smoothCamera;
                        }
                    } else currentScreen.handleKeyboardInput();

                    if (gameSettings.showDebugInfo && gameSettings.showDebugProfilerChart) {
                        if (k == 11) updateDebugProfilerName(0);

                        for (int j1 = 0; j1 < 9; ++j1) {
                            if (k == 2 + j1) {
                                updateDebugProfilerName(j1 + 1);
                            }
                        }
                    }
                }
            }

            for (int l = 0; l < 9; ++l) {
                if (gameSettings.keyBindsHotbar[l].isPressed()) {
                    if (thePlayer.isSpectator()) {
                        ingameGUI.getSpectatorGui().func_175260_a(l);
                    } else {
                        thePlayer.inventory.currentItem = l;
                    }
                }
            }

            boolean flag = gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN;

            while (gameSettings.keyBindInventory.isPressed()) {
                if (playerController.isRidingHorse()) {
                    thePlayer.sendHorseInventory();
                } else {
                    getNetHandler().addToSendQueue(
                            new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                    displayGuiScreen(new GuiInventory(thePlayer));
                }
            }

            while (gameSettings.keyBindDrop.isPressed()) {
                if (!thePlayer.isSpectator()) thePlayer.dropOneItem(GuiScreen.isCtrlKeyDown());
            }

            while (gameSettings.keyBindChat.isPressed() && flag) {
                displayGuiScreen(new GuiChat());
            }

            if (currentScreen == null && gameSettings.keyBindCommand.isPressed() && flag) {
                displayGuiScreen(new GuiChat("/"));
            }

            if (!thePlayer.isUsingItem()) {
                while (gameSettings.keyBindAttack.isPressed()) clickMouse();
                while (gameSettings.keyBindUseItem.isPressed()) rightClickMouse();
                while (gameSettings.keyBindPickBlock.isPressed()) middleClickMouse();
            } else if (!gameSettings.keyBindUseItem.isKeyDown()) playerController.onStoppedUsingItem(thePlayer);

            if (gameSettings.keyBindUseItem.isKeyDown() && rightClickDelayTimer == 0 && !thePlayer.isUsingItem()) {
                rightClickMouse();
            }

            sendClickBlockToController(currentScreen == null && gameSettings.keyBindAttack.isKeyDown() && inGameHasFocus);
        }

        if (theWorld != null) {
            if (thePlayer != null) {
                ++joinPlayerCounter;

                if (joinPlayerCounter == 30) {
                    joinPlayerCounter = 0;
                    theWorld.joinEntityInSurroundings(thePlayer);
                }
            }

            mcProfiler.endStartSection("gameRenderer");

            if (!isGamePaused) entityRenderer.updateRenderer();

            mcProfiler.endStartSection("levelRenderer");

            if (!isGamePaused) renderGlobal.updateClouds();

            mcProfiler.endStartSection("level");

            if (!isGamePaused) {
                if (theWorld.getLastLightningBolt() > 0) {
                    theWorld.setLastLightningBolt(theWorld.getLastLightningBolt() - 1);
                }

                theWorld.updateEntities();
            }
        } else if (entityRenderer.isShaderActive()) entityRenderer.stopUseShader();

        if (!isGamePaused) {
            mcMusicTicker.update();
            mcSoundHandler.update();
        }

        if (theWorld != null) {
            if (!isGamePaused) {
                theWorld.setAllowedSpawnTypes(theWorld.getDifficulty() != EnumDifficulty.PEACEFUL, true);

                try {
                    theWorld.tick();
                } catch (Throwable throwable2) {
                    CrashReport crashreport2 = CrashReport.makeCrashReport(throwable2, "Exception in world tick");

                    if (theWorld == null) {
                        CrashReportCategory crashreportcategory2 = crashreport2.makeCategory("Affected level");
                        crashreportcategory2.addCrashSection("Problem", "Level is null!");
                    } else {
                        theWorld.addWorldInfoToCrashReport(crashreport2);
                    }

                    throw new ReportedException(crashreport2);
                }
            }

            mcProfiler.endStartSection("animateTick");

            if (!isGamePaused && theWorld != null) {
                theWorld.doVoidFogParticles(MathHelper.floor_double(thePlayer.posX),
                        MathHelper.floor_double(thePlayer.posY), MathHelper.floor_double(thePlayer.posZ));
            }

            mcProfiler.endStartSection("particles");

            if (!isGamePaused) effectRenderer.updateEffects();
        } else if (myNetworkManager != null) {
            mcProfiler.endStartSection("pendingConnection");
            myNetworkManager.processReceivedPackets();
        }

        mcProfiler.endSection();
        systemTime = getSystemTime();
    }

    public void launchIntegratedServer(String folderName, String worldName, WorldSettings worldSettingsIn) {
        loadWorld(null);
        System.gc();
        ISaveHandler isavehandler = saveLoader.getSaveLoader(folderName, false);
        WorldInfo worldinfo = isavehandler.loadWorldInfo();

        if (worldinfo == null && worldSettingsIn != null) {
            worldinfo = new WorldInfo(worldSettingsIn, folderName);
            isavehandler.saveWorldInfo(worldinfo);
        }

        if (worldSettingsIn == null) worldSettingsIn = new WorldSettings(worldinfo);

        try {
            theIntegratedServer = new IntegratedServer(this, folderName, worldName, worldSettingsIn);
            theIntegratedServer.startServerThread();
            integratedServerIsRunning = true;
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Starting integrated server");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Starting integrated server");
            crashreportcategory.addCrashSection("Level ID", folderName);
            crashreportcategory.addCrashSection("Level Name", worldName);
            throw new ReportedException(crashreport);
        }

        loadingScreen.displaySavingString(I18n.format("menu.loadingLevel"));

        while (!theIntegratedServer.serverIsInRunLoop()) {
            String s = theIntegratedServer.getUserMessage();

            if (s != null) {
                loadingScreen.displayLoadingString(I18n.format(s));
            } else {
                loadingScreen.displayLoadingString("");
            }
        }

        displayGuiScreen(null);
        SocketAddress socketaddress = theIntegratedServer.getNetworkSystem().addLocalEndpoint();
        NetworkManager networkmanager = NetworkManager.provideLocalClient(socketaddress);
        networkmanager.setNetHandler(new NetHandlerLoginClient(networkmanager, this, null));
        networkmanager.sendPacket(new C00Handshake(47, socketaddress.toString(), 0, EnumConnectionState.LOGIN));
        networkmanager.sendPacket(new C00PacketLoginStart(session.getProfile()));
        myNetworkManager = networkmanager;
    }

    public void loadWorld(WorldClient worldClientIn) {
        loadWorld(worldClientIn, "");
    }

    public void loadWorld(WorldClient worldClientIn, String loadingMessage) {
        if (worldClientIn == null) {
            NetHandlerPlayClient nethandlerplayclient = getNetHandler();

            if (nethandlerplayclient != null) nethandlerplayclient.cleanup();

            if (theIntegratedServer != null && theIntegratedServer.isAnvilFileSet()) {
                theIntegratedServer.initiateShutdown();
                theIntegratedServer.setStaticInstance();
            }

            theIntegratedServer = null;
            guiAchievement.clearAchievements();
            entityRenderer.getMapItemRenderer().clearLoadedMaps();
        }

        renderViewEntity = null;
        myNetworkManager = null;

        if (loadingScreen != null) {
            loadingScreen.resetProgressAndMessage(loadingMessage);
            loadingScreen.displayLoadingString("");
        }

        if (worldClientIn == null && theWorld != null) {
            mcResourcePackRepository.clearResourcePack();
            ingameGUI.resetPlayersOverlayFooterHeader();
            currentServerData = null;
            integratedServerIsRunning = false;
        }

        mcSoundHandler.stopSounds();
        theWorld = worldClientIn;

        if (worldClientIn != null) {
            if (renderGlobal != null) renderGlobal.setWorldAndLoadRenderers(worldClientIn);
            if (effectRenderer != null) effectRenderer.clearEffects(worldClientIn);

            if (thePlayer == null) {
                thePlayer = playerController.func_178892_a(worldClientIn, new StatFileWriter());
                playerController.flipPlayer(thePlayer);
            }

            thePlayer.preparePlayerToSpawn();
            worldClientIn.spawnEntityInWorld(thePlayer);
            thePlayer.movementInput = new MovementInputFromOptions(gameSettings);
            playerController.setPlayerCapabilities(thePlayer);
            renderViewEntity = thePlayer;
        } else {
            saveLoader.flushCache();
            thePlayer = null;
        }

        System.gc();
        systemTime = 0L;
    }

    public void setDimensionAndSpawnPlayer(int dimension) {
        theWorld.setInitialSpawnLocation();
        theWorld.removeAllEntities();
        int i = 0;
        String s = null;

        if (thePlayer != null) {
            i = thePlayer.getEntityId();
            theWorld.removeEntity(thePlayer);
            s = thePlayer.getClientBrand();
        }

        renderViewEntity = null;
        EntityPlayerSP entityplayersp = thePlayer;
        thePlayer = playerController.func_178892_a(theWorld,
                thePlayer == null ? new StatFileWriter() : thePlayer.getStatFileWriter());
        thePlayer.getDataWatcher().updateWatchedObjectsFromList(entityplayersp.getDataWatcher().getAllWatched());
        thePlayer.dimension = dimension;
        renderViewEntity = thePlayer;
        thePlayer.preparePlayerToSpawn();
        thePlayer.setClientBrand(s);
        theWorld.spawnEntityInWorld(thePlayer);
        playerController.flipPlayer(thePlayer);
        thePlayer.movementInput = new MovementInputFromOptions(gameSettings);
        thePlayer.setEntityId(i);
        playerController.setPlayerCapabilities(thePlayer);
        thePlayer.setReducedDebug(entityplayersp.hasReducedDebug());

        if (currentScreen instanceof GuiGameOver) displayGuiScreen(null);
    }

    public NetHandlerPlayClient getNetHandler() {
        return thePlayer != null ? thePlayer.sendQueue : null;
    }

    private void middleClickMouse() {
        if (objectMouseOver != null) {
            boolean flag = thePlayer.capabilities.isCreativeMode;
            int i = 0;
            boolean flag1 = false;
            TileEntity tileentity = null;
            Item item;

            if (objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                BlockPos blockpos = objectMouseOver.getBlockPos();
                Block block = theWorld.getBlockState(blockpos).getBlock();

                if (block.getMaterial() == Material.air) {
                    return;
                }

                item = block.getItem(theWorld, blockpos);

                if (item == null) {
                    return;
                }

                if (flag && GuiScreen.isCtrlKeyDown()) {
                    tileentity = theWorld.getTileEntity(blockpos);
                }

                Block block1 = item instanceof ItemBlock && !block.isFlowerPot() ? Block.getBlockFromItem(item) : block;
                i = block1.getDamageValue(theWorld, blockpos);
                flag1 = item.getHasSubtypes();
            } else {
                if (objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY
                        || objectMouseOver.entityHit == null || !flag) {
                    return;
                }

                switch (objectMouseOver.entityHit) {
                    case EntityPainting entityPainting -> item = Items.painting;
                    case EntityLeashKnot entityLeashKnot -> item = Items.lead;
                    case EntityItemFrame entityitemframe -> {
                        ItemStack itemstack = entityitemframe.getDisplayedItem();

                        if (itemstack != null) {
                            item = itemstack.getItem();
                            i = itemstack.getMetadata();
                            flag1 = true;
                        } else item = Items.item_frame;
                    }
                    case EntityMinecart entityminecart -> item = switch (entityminecart.getMinecartType()) {
                        case FURNACE -> Items.furnace_minecart;
                        case CHEST -> Items.chest_minecart;
                        case TNT -> Items.tnt_minecart;
                        case HOPPER -> Items.hopper_minecart;
                        case COMMAND_BLOCK -> Items.command_block_minecart;
                        default -> Items.minecart;
                    };
                    case EntityBoat entityBoat -> item = Items.boat;
                    case EntityArmorStand entityArmorStand -> item = Items.armor_stand;
                    default -> {
                        item = Items.spawn_egg;
                        i = EntityList.getEntityID(objectMouseOver.entityHit);
                        flag1 = true;

                        if (!EntityList.entityEggs.containsKey(i)) {
                            return;
                        }
                    }
                }
            }

            InventoryPlayer inventoryplayer = thePlayer.inventory;

            if (tileentity != null) {
                ItemStack itemstack1 = pickBlockWithNBT(item, i, tileentity);
                inventoryplayer.setInventorySlotContents(inventoryplayer.currentItem, itemstack1);
            } else inventoryplayer.setCurrentItem(item, i, flag1, flag);

            if (flag) {
                int j = thePlayer.inventoryContainer.inventorySlots.size() - 9 + inventoryplayer.currentItem;
                playerController.sendSlotPacket(inventoryplayer.getStackInSlot(inventoryplayer.currentItem), j);
            }
        }
    }

    private ItemStack pickBlockWithNBT(Item itemIn, int meta, TileEntity tileEntityIn) {
        ItemStack itemstack = new ItemStack(itemIn, 1, meta);
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        tileEntityIn.writeToNBT(nbttagcompound);

        if (itemIn == Items.skull && nbttagcompound.hasKey("Owner")) {
            NBTTagCompound nbttagcompound2 = nbttagcompound.getCompoundTag("Owner");
            NBTTagCompound nbttagcompound3 = new NBTTagCompound();
            nbttagcompound3.setTag("SkullOwner", nbttagcompound2);
            itemstack.setTagCompound(nbttagcompound3);
        } else {
            itemstack.setTagInfo("BlockEntityTag", nbttagcompound);
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            NBTTagList nbttaglist = new NBTTagList();
            nbttaglist.appendTag(new NBTTagString("(+NBT)"));
            nbttagcompound1.setTag("Lore", nbttaglist);
            itemstack.setTagInfo("display", nbttagcompound1);
        }
        return itemstack;
    }

    public CrashReport addGraphicsAndWorldToCrashReport(CrashReport theCrash) {
        theCrash.getCategory().addCrashSectionCallable("Launched Version", () -> launchedVersion);
        theCrash.getCategory().addCrashSectionCallable("LWJGL", () -> Sys.VERSION);
        theCrash.getCategory().addCrashSectionCallable("OpenGL", () -> GL11.glGetString(GL11.GL_RENDERER) + " GL version " + GL11.glGetString(GL11.GL_VERSION) + ", "
                + GL11.glGetString(GL11.GL_VENDOR));
        theCrash.getCategory().addCrashSectionCallable("GL Caps", OpenGlHelper::getLogText);
        theCrash.getCategory().addCrashSectionCallable("Using VBOs", () -> gameSettings.useVbo ? "Yes" : "No");
        theCrash.getCategory().addCrashSectionCallable("Is Modded", () -> {
            String s = ClientBrandRetriever.getClientModName();
            return !s.equals("vanilla") ? "Definitely; Client brand changed to '" + s + "'"
                    : (Minecraft.class.getSigners() == null ? "Very likely; Jar signature invalidated"
                    : "Probably not. Jar signature remains and client brand is untouched.");
        });
        theCrash.getCategory().addCrashSectionCallable("Type", () -> "Client (map_client.txt)");
        theCrash.getCategory().addCrashSectionCallable("Resource Packs", () -> {
            StringBuilder stringbuilder = new StringBuilder();

            for (String s : gameSettings.resourcePacks) {
                if (!stringbuilder.isEmpty()) stringbuilder.append(", ");

                stringbuilder.append(s);

                if (gameSettings.incompatibleResourcePacks.contains(s)) stringbuilder.append(" (incompatible)");
            }

            return stringbuilder.toString();
        });
        theCrash.getCategory().addCrashSectionCallable("Current Language", () -> mcLanguageManager.getCurrentLanguage().toString());
        theCrash.getCategory().addCrashSectionCallable("Profiler Position", () -> mcProfiler.profilingEnabled ? mcProfiler.getNameOfLastSection()
                : "N/A (disabled)");
        theCrash.getCategory().addCrashSectionCallable("CPU", OpenGlHelper::getCpu);

        if (theWorld != null) theWorld.addWorldInfoToCrashReport(theCrash);

        return theCrash;
    }

    public ListenableFuture<Object> scheduleResourcesRefresh() {
        return addScheduledTask(this::refreshResources);
    }

    public void setServerData(ServerData serverDataIn) {
        currentServerData = serverDataIn;
    }

    public ServerData getCurrentServerData() {
        return currentServerData;
    }

    public boolean isIntegratedServerRunning() {
        return integratedServerIsRunning;
    }

    public boolean isSingleplayer() {
        return integratedServerIsRunning && theIntegratedServer != null;
    }

    public IntegratedServer getIntegratedServer() {
        return theIntegratedServer;
    }

    public boolean isFullScreen() {
        return fullscreen;
    }

    public Session getSession() {
        return session;
    }

    public PropertyMap getProfileProperties() {
        if (profileProperties.isEmpty()) {
            GameProfile gameprofile = sessionService.fillProfileProperties(session.getProfile(), false);
            profileProperties.putAll(gameprofile.getProperties());
        }

        return profileProperties;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public TextureManager getTextureManager() {
        return renderEngine;
    }

    public IResourceManager getResourceManager() {
        return mcResourceManager;
    }

    public ResourcePackRepository getResourcePackRepository() {
        return mcResourcePackRepository;
    }

    public LanguageManager getLanguageManager() {
        return mcLanguageManager;
    }

    public TextureMap getTextureMapBlocks() {
        return textureMapBlocks;
    }

    public boolean isJava64bit() {
        return jvm64bit;
    }

    public boolean isGamePaused() {
        return isGamePaused;
    }

    public SoundHandler getSoundHandler() {
        return mcSoundHandler;
    }

    public MusicTicker.MusicType getAmbientMusicType() {
        return thePlayer != null ? (thePlayer.worldObj.provider instanceof WorldProviderHell
                ? MusicTicker.MusicType.NETHER
                : (thePlayer.worldObj.provider instanceof WorldProviderEnd
                ? (BossStatus.bossName != null && BossStatus.statusBarTime > 0 ? MusicTicker.MusicType.END_BOSS
                : MusicTicker.MusicType.END)
                : (thePlayer.capabilities.isCreativeMode && thePlayer.capabilities.allowFlying
                ? MusicTicker.MusicType.CREATIVE
                : MusicTicker.MusicType.GAME)))
                : MusicTicker.MusicType.MENU;
    }

    public void dispatchKeypresses() {
        int i = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() : Keyboard.getEventKey();

        if (i != 0 && !Keyboard.isRepeatEvent()) {
            if (!(currentScreen instanceof GuiControls)
                    || ((GuiControls) currentScreen).time <= getSystemTime() - 20L) {
                if (Keyboard.getEventKeyState()) {
                    if (i == gameSettings.keyBindFullscreen.getKeyCode()) {
                        toggleFullscreen();
                    } else if (i == gameSettings.keyBindScreenshot.getKeyCode()) {
                        ingameGUI.getChatGUI().printChatMessage(ScreenShotHelper.saveScreenshot(mcDataDir,
                                displayWidth, displayHeight, framebufferMc));
                    }
                }
            }
        }
    }

    public MinecraftSessionService getSessionService() {
        return sessionService;
    }

    public SkinManager getSkinManager() {
        return skinManager;
    }

    public Entity getRenderViewEntity() {
        return renderViewEntity;
    }

    public void setRenderViewEntity(Entity viewingEntity) {
        renderViewEntity = viewingEntity;
        entityRenderer.loadEntityShader(viewingEntity);
    }

    public <V> ListenableFuture<V> addScheduledTask(Callable<V> callableToSchedule) {
        Objects.requireNonNull(callableToSchedule);

        if (!isCallingFromMinecraftThread()) {
            ListenableFutureTask<V> listenablefuturetask = ListenableFutureTask.create(callableToSchedule);

            synchronized (scheduledTasks) {
                scheduledTasks.add(listenablefuturetask);
                return listenablefuturetask;
            }
        } else {
            try {
                return Futures.immediateFuture(callableToSchedule.call());
            } catch (Exception exception) {
                return Futures.immediateFailedFuture(exception);
            }
        }
    }

    public ListenableFuture<Object> addScheduledTask(Runnable runnableToSchedule) {
        Objects.requireNonNull(runnableToSchedule);
        return addScheduledTask(Executors.callable(runnableToSchedule));
    }

    public boolean isCallingFromMinecraftThread() {
        return Thread.currentThread() == mcThread;
    }

    public BlockRendererDispatcher getBlockRendererDispatcher() {
        return blockRenderDispatcher;
    }

    public RenderManager getRenderManager() {
        return renderManager;
    }

    public RenderItem getRenderItem() {
        return renderItem;
    }

    public ItemRenderer getItemRenderer() {
        return itemRenderer;
    }

    public FrameTimer getFrameTimer() {
        return frameTimer;
    }
}
