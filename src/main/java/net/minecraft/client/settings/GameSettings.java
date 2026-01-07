package net.minecraft.client.settings;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.minecraft.src.Config;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.optifine.*;
import net.optifine.shaders.Shaders;
import net.optifine.util.KeyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameSettings {
    public static final int DEFAULT = 0;
    public static final int FAST = 1;
    public static final int FANCY = 2;
    public static final int OFF = 3;
    public static final int SMART = 4;
    public static final int ANIM_ON = 0;
    public static final int ANIM_GENERATED = 1;
    public static final int ANIM_OFF = 2;
    public static final String DEFAULT_STR = "Default";
    private static final Logger logger = LogManager.getLogger();
    private static final Gson gson = new Gson();
    private static final ParameterizedType typeListString = new ParameterizedType() {
        public Type[] getActualTypeArguments() {
            return new Type[]{String.class};
        }

        public Type getRawType() {
            return List.class;
        }

        public Type getOwnerType() {
            return null;
        }
    };
    private static final String[] GUISCALES = new String[]{"options.guiScale.auto", "options.guiScale.small", "options.guiScale.normal", "options.guiScale.large"};
    private static final String[] PARTICLES = new String[]{"options.particles.all", "options.particles.decreased", "options.particles.minimal"};
    private static final String[] AMBIENT_OCCLUSIONS = new String[]{"options.ao.off", "options.ao.min", "options.ao.max"};
    private static final String[] CLOUDS_TYPES = new String[]{"options.off", "options.graphics.fast", "options.graphics.fancy"};
    private static final int[] OF_TREES_VALUES = new int[]{0, 1, 4, 2};
    private static final int[] OF_DYNAMIC_LIGHTS = new int[]{3, 1, 2};
    private static final String[] KEYS_DYNAMIC_LIGHTS = new String[]{"options.off", "options.graphics.fast", "options.graphics.fancy"};
    private final Set<EnumPlayerModelParts> setModelParts = Sets.newHashSet(EnumPlayerModelParts.values());
    public float mouseSensitivity = 0.5F;
    public boolean invertMouse;
    public int renderDistanceChunks = -1;
    public boolean viewBobbing = true;
    public boolean anaglyph;
    public boolean fboEnable = true;
    public int limitFramerate = 120;
    public int clouds = 2;
    public boolean fancyGraphics = true;
    public int ambientOcclusion = 2;
    public List<String> resourcePacks = Lists.newArrayList();
    public List<String> incompatibleResourcePacks = Lists.newArrayList();
    public EntityPlayer.EnumChatVisibility chatVisibility = EntityPlayer.EnumChatVisibility.FULL;
    public boolean chatColours = true;
    public boolean chatLinks = true;
    public boolean chatLinksPrompt = true;
    public float chatOpacity = 1.0F;
    public boolean snooperEnabled = true;
    public boolean fullScreen;
    public boolean enableVsync = true;
    public boolean useVbo = false;
    public boolean allowBlockAlternatives = true;
    public boolean reducedDebugInfo = false;
    public boolean hideServerAddress;
    public boolean advancedItemTooltips;
    public boolean pauseOnLostFocus = true;
    public boolean touchscreen;
    public int overrideWidth;
    public int overrideHeight;
    public boolean heldItemTooltips = true;
    public float chatScale = 1.0F;
    public float chatWidth = 1.0F;
    public float chatHeightUnfocused = 0.44366196F;
    public float chatHeightFocused = 1.0F;
    public boolean showInventoryAchievementHint = true;
    public int mipmapLevels = 4;
    public boolean useNativeTransport = true;
    public boolean entityShadows = true;
    public KeyBinding keyBindForward = new KeyBinding("key.forward", 17, "key.categories.movement");
    public KeyBinding keyBindLeft = new KeyBinding("key.left", 30, "key.categories.movement");
    public KeyBinding keyBindBack = new KeyBinding("key.back", 31, "key.categories.movement");
    public KeyBinding keyBindRight = new KeyBinding("key.right", 32, "key.categories.movement");
    public KeyBinding keyBindJump = new KeyBinding("key.jump", 57, "key.categories.movement");
    public KeyBinding keyBindSneak = new KeyBinding("key.sneak", 42, "key.categories.movement");
    public KeyBinding keyBindSprint = new KeyBinding("key.sprint", 29, "key.categories.movement");
    public KeyBinding keyBindInventory = new KeyBinding("key.inventory", 18, "key.categories.inventory");
    public KeyBinding keyBindUseItem = new KeyBinding("key.use", -99, "key.categories.gameplay");
    public KeyBinding keyBindDrop = new KeyBinding("key.drop", 16, "key.categories.gameplay");
    public KeyBinding keyBindAttack = new KeyBinding("key.attack", -100, "key.categories.gameplay");
    public KeyBinding keyBindPickBlock = new KeyBinding("key.pickItem", -98, "key.categories.gameplay");
    public KeyBinding keyBindChat = new KeyBinding("key.chat", 20, "key.categories.multiplayer");
    public KeyBinding keyBindPlayerList = new KeyBinding("key.playerlist", 15, "key.categories.multiplayer");
    public KeyBinding keyBindCommand = new KeyBinding("key.command", 53, "key.categories.multiplayer");
    public KeyBinding keyBindScreenshot = new KeyBinding("key.screenshot", 60, "key.categories.misc");
    public KeyBinding keyBindTogglePerspective = new KeyBinding("key.togglePerspective", 63, "key.categories.misc");
    public KeyBinding keyBindSmoothCamera = new KeyBinding("key.smoothCamera", 0, "key.categories.misc");
    public KeyBinding keyBindFullscreen = new KeyBinding("key.fullscreen", 87, "key.categories.misc");
    public KeyBinding keyBindSpectatorOutlines = new KeyBinding("key.spectatorOutlines", 0, "key.categories.misc");
    public KeyBinding[] keyBindsHotbar = new KeyBinding[]{new KeyBinding("key.hotbar.1", 2, "key.categories.inventory"), new KeyBinding("key.hotbar.2", 3, "key.categories.inventory"), new KeyBinding("key.hotbar.3", 4, "key.categories.inventory"), new KeyBinding("key.hotbar.4", 5, "key.categories.inventory"), new KeyBinding("key.hotbar.5", 6, "key.categories.inventory"), new KeyBinding("key.hotbar.6", 7, "key.categories.inventory"), new KeyBinding("key.hotbar.7", 8, "key.categories.inventory"), new KeyBinding("key.hotbar.8", 9, "key.categories.inventory"), new KeyBinding("key.hotbar.9", 10, "key.categories.inventory")};
    public KeyBinding[] keyBindings;
    public EnumDifficulty difficulty;
    public boolean hideGUI;
    public int thirdPersonView;
    public boolean showDebugInfo;
    public boolean showDebugProfilerChart;
    public boolean showLagometer;
    public String lastServer;
    public boolean smoothCamera;
    public boolean debugCamEnable;
    public float fovSetting;
    public float gammaSetting;
    public float saturation;
    public int guiScale;
    public int particleSetting;
    public String language;
    public boolean forceUnicodeFont;
    public int ofFogType = 1;
    public float ofFogStart = 0.8F;
    public int ofMipmapType = 0;
    public boolean ofOcclusionFancy = false;
    public boolean ofSmoothFps = false;
    public boolean ofSmoothWorld = Config.isSingleProcessor();
    public boolean ofLazyChunkLoading = Config.isSingleProcessor();
    public boolean ofRenderRegions = false;
    public boolean ofSmartAnimations = false;
    public float ofAoLevel = 1.0F;
    public int ofAaLevel = 0;
    public int ofAfLevel = 1;
    public int ofClouds = 0;
    public float ofCloudsHeight = 0.0F;
    public int ofTrees = 0;
    public int ofRain = 0;
    public int ofDroppedItems = 0;
    public int ofBetterGrass = 3;
    public int ofAutoSaveTicks = 4000;
    public boolean ofLagometer = false;
    public boolean ofProfiler = false;
    public boolean ofShowFps = false;
    public boolean ofWeather = true;
    public boolean ofSky = true;
    public boolean ofStars = true;
    public boolean ofSunMoon = true;
    public int ofVignette = 0;
    public int ofChunkUpdates = 1;
    public boolean ofChunkUpdatesDynamic = false;
    public int ofTime = 0;
    public boolean ofClearWater = false;
    public boolean ofBetterSnow = false;
    public String ofFullscreenMode = "Default";
    public boolean ofSwampColors = true;
    public boolean ofRandomEntities = true;
    public boolean ofSmoothBiomes = true;
    public boolean ofCustomFonts = true;
    public boolean ofCustomColors = true;
    public boolean ofCustomSky = true;
    public boolean ofShowCapes = true;
    public int ofConnectedTextures = 2;
    public boolean ofCustomItems = true;
    public boolean ofNaturalTextures = false;
    public boolean ofEmissiveTextures = true;
    public boolean ofFastMath = false;
    public boolean ofFastRender = false;
    public int ofTranslucentBlocks = 0;
    public boolean ofDynamicFov = true;
    public boolean ofAlternateBlocks = true;
    public int ofDynamicLights = 3;
    public boolean ofCustomEntityModels = true;
    public boolean ofCustomGuis = true;
    public boolean ofShowGlErrors = true;
    public int ofScreenshotSize = 1;
    public int ofAnimatedWater = 0;
    public int ofAnimatedLava = 0;
    public boolean ofAnimatedFire = true;
    public boolean ofAnimatedPortal = true;
    public boolean ofAnimatedRedstone = true;
    public boolean ofAnimatedExplosion = true;
    public boolean ofAnimatedFlame = true;
    public boolean ofAnimatedSmoke = true;
    public boolean ofVoidParticles = true;
    public boolean ofWaterParticles = true;
    public boolean ofRainSplash = true;
    public boolean ofPortalParticles = true;
    public boolean ofPotionParticles = true;
    public boolean ofFireworkParticles = true;
    public boolean ofDrippingWaterLava = true;
    public boolean ofAnimatedTerrain = true;
    public boolean ofAnimatedTextures = true;
    public KeyBinding ofKeyBindZoom;
    protected Minecraft mc;
    private final Map<SoundCategory, Float> mapSoundLevels = Maps.newEnumMap(SoundCategory.class);
    private File optionsFile;
    private File optionsFileOF;

    public GameSettings(Minecraft mcIn, File optionsFileIn) {
        keyBindings = ArrayUtils.addAll(new KeyBinding[]{keyBindAttack, keyBindUseItem, keyBindForward, keyBindLeft, keyBindBack, keyBindRight, keyBindJump, keyBindSneak, keyBindSprint, keyBindDrop, keyBindInventory, keyBindChat, keyBindPlayerList, keyBindPickBlock, keyBindCommand, keyBindScreenshot, keyBindTogglePerspective, keyBindSmoothCamera, keyBindFullscreen, keyBindSpectatorOutlines}, keyBindsHotbar);
        difficulty = EnumDifficulty.NORMAL;
        lastServer = "";
        fovSetting = 70.0F;
        language = "en_US";
        forceUnicodeFont = false;
        mc = mcIn;
        optionsFile = new File(optionsFileIn, "options.txt");

        if (mcIn.isJava64bit() && Runtime.getRuntime().maxMemory() >= 1000000000L) {
            GameSettings.Options.RENDER_DISTANCE.setValueMax(32.0F);
            long i = 1000000L;

            if (Runtime.getRuntime().maxMemory() >= 1500L * i) {
                GameSettings.Options.RENDER_DISTANCE.setValueMax(48.0F);
            }

            if (Runtime.getRuntime().maxMemory() >= 2500L * i) {
                GameSettings.Options.RENDER_DISTANCE.setValueMax(64.0F);
            }
        } else {
            GameSettings.Options.RENDER_DISTANCE.setValueMax(16.0F);
        }

        renderDistanceChunks = mcIn.isJava64bit() ? 12 : 8;
        optionsFileOF = new File(optionsFileIn, "optionsof.txt");
        limitFramerate = (int) GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
        ofKeyBindZoom = new KeyBinding("of.key.zoom", 46, "key.categories.misc");
        keyBindings = ArrayUtils.add(keyBindings, ofKeyBindZoom);
        KeyUtils.fixKeyConflicts(keyBindings, new KeyBinding[]{ofKeyBindZoom});
        renderDistanceChunks = 8;
        loadOptions();
        Config.initGameSettings(this);
    }

    public GameSettings() {
        keyBindings = ArrayUtils.addAll(new KeyBinding[]{keyBindAttack, keyBindUseItem, keyBindForward, keyBindLeft, keyBindBack, keyBindRight, keyBindJump, keyBindSneak, keyBindSprint, keyBindDrop, keyBindInventory, keyBindChat, keyBindPlayerList, keyBindPickBlock, keyBindCommand, keyBindScreenshot, keyBindTogglePerspective, keyBindSmoothCamera, keyBindFullscreen, keyBindSpectatorOutlines}, keyBindsHotbar);
        difficulty = EnumDifficulty.NORMAL;
        lastServer = "";
        fovSetting = 70.0F;
        language = "en_US";
        forceUnicodeFont = false;
    }

    public static String getKeyDisplayString(int key) {
        return key < 0 ? I18n.format("key.mouseButton", key + 101) : (key < 256 ? Keyboard.getKeyName(key) : String.format("%c", (char) (key - 256)).toUpperCase());
    }

    public static boolean isKeyDown(KeyBinding key) {
        return key.getKeyCode() != 0 && (key.getKeyCode() < 0 ? Mouse.isButtonDown(key.getKeyCode() + 100) : Keyboard.isKeyDown(key.getKeyCode()));
    }

    private static String getTranslation(String[] strArray, int index) {
        if (index < 0 || index >= strArray.length) {
            index = 0;
        }

        return I18n.format(strArray[index]);
    }

    private static int nextValue(int p_nextValue_0_, int[] p_nextValue_1_) {
        int i = indexOf(p_nextValue_0_, p_nextValue_1_);

        if (i < 0) {
            return p_nextValue_1_[0];
        } else {
            ++i;

            if (i >= p_nextValue_1_.length) {
                i = 0;
            }

            return p_nextValue_1_[i];
        }
    }

    private static int limit(int p_limit_0_, int[] p_limit_1_) {
        int i = indexOf(p_limit_0_, p_limit_1_);
        return i < 0 ? p_limit_1_[0] : p_limit_0_;
    }

    private static int indexOf(int p_indexOf_0_, int[] p_indexOf_1_) {
        for (int i = 0; i < p_indexOf_1_.length; ++i) {
            if (p_indexOf_1_[i] == p_indexOf_0_) {
                return i;
            }
        }

        return -1;
    }

    public void setOptionKeyBinding(KeyBinding key, int keyCode) {
        key.setKeyCode(keyCode);
        saveOptions();
    }

    public void setOptionFloatValue(GameSettings.Options settingsOption, float value) {
        setOptionFloatValueOF(settingsOption, value);

        if (settingsOption == GameSettings.Options.SENSITIVITY) {
            mouseSensitivity = value;
        }

        if (settingsOption == GameSettings.Options.FOV) {
            fovSetting = value;
        }

        if (settingsOption == GameSettings.Options.GAMMA) {
            gammaSetting = value;
        }

        if (settingsOption == GameSettings.Options.FRAMERATE_LIMIT) {
            limitFramerate = (int) value;
            enableVsync = false;

            if (limitFramerate <= 0) {
                limitFramerate = (int) GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
                enableVsync = true;
            }

            updateVSync();
        }

        if (settingsOption == GameSettings.Options.CHAT_OPACITY) {
            chatOpacity = value;
            mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (settingsOption == GameSettings.Options.CHAT_HEIGHT_FOCUSED) {
            chatHeightFocused = value;
            mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (settingsOption == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED) {
            chatHeightUnfocused = value;
            mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (settingsOption == GameSettings.Options.CHAT_WIDTH) {
            chatWidth = value;
            mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (settingsOption == GameSettings.Options.CHAT_SCALE) {
            chatScale = value;
            mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (settingsOption == GameSettings.Options.MIPMAP_LEVELS) {
            int i = mipmapLevels;
            mipmapLevels = (int) value;

            if ((float) i != value) {
                mc.getTextureMapBlocks().setMipmapLevels(mipmapLevels);
                mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
                mc.getTextureMapBlocks().setBlurMipmapDirect(false, mipmapLevels > 0);
                mc.scheduleResourcesRefresh();
            }
        }

        if (settingsOption == GameSettings.Options.BLOCK_ALTERNATIVES) {
            allowBlockAlternatives = !allowBlockAlternatives;
            mc.renderGlobal.loadRenderers();
        }

        if (settingsOption == GameSettings.Options.RENDER_DISTANCE) {
            renderDistanceChunks = (int) value;
            mc.renderGlobal.setDisplayListEntitiesDirty();
        }
    }

    public void setOptionValue(GameSettings.Options settingsOption, int value) {
        setOptionValueOF(settingsOption, value);

        if (settingsOption == GameSettings.Options.INVERT_MOUSE) {
            invertMouse = !invertMouse;
        }

        if (settingsOption == GameSettings.Options.GUI_SCALE) {
            guiScale += value;

            if (GuiScreen.isShiftKeyDown()) {
                guiScale = 0;
            }

            DisplayMode displaymode = Config.getLargestDisplayMode();
            int i = displaymode.getWidth() / 320;
            int j = displaymode.getHeight() / 240;
            int k = Math.min(i, j);

            if (guiScale < 0) {
                guiScale = k - 1;
            }

            if (mc.isUnicode() && guiScale % 2 != 0) {
                guiScale += value;
            }

            if (guiScale < 0 || guiScale >= k) {
                guiScale = 0;
            }
        }

        if (settingsOption == GameSettings.Options.PARTICLES) {
            particleSetting = (particleSetting + value) % 3;
        }

        if (settingsOption == GameSettings.Options.VIEW_BOBBING) {
            viewBobbing = !viewBobbing;
        }

        if (settingsOption == GameSettings.Options.RENDER_CLOUDS) {
            clouds = (clouds + value) % 3;
        }

        if (settingsOption == GameSettings.Options.FORCE_UNICODE_FONT) {
            forceUnicodeFont = !forceUnicodeFont;
            mc.fontRendererObj.setUnicodeFlag(mc.getLanguageManager().isCurrentLocaleUnicode() || forceUnicodeFont);
        }

        if (settingsOption == GameSettings.Options.FBO_ENABLE) {
            fboEnable = !fboEnable;
        }

        if (settingsOption == GameSettings.Options.ANAGLYPH) {
            if (!anaglyph && Config.isShaders()) {
                Config.showGuiMessage(Lang.get("of.message.an.shaders1"), Lang.get("of.message.an.shaders2"));
                return;
            }

            anaglyph = !anaglyph;
            mc.refreshResources();
        }

        if (settingsOption == GameSettings.Options.GRAPHICS) {
            fancyGraphics = !fancyGraphics;
            updateRenderClouds();
            mc.renderGlobal.loadRenderers();
        }

        if (settingsOption == GameSettings.Options.AMBIENT_OCCLUSION) {
            ambientOcclusion = (ambientOcclusion + value) % 3;
            mc.renderGlobal.loadRenderers();
        }

        if (settingsOption == GameSettings.Options.CHAT_VISIBILITY) {
            chatVisibility = EntityPlayer.EnumChatVisibility.getEnumChatVisibility((chatVisibility.getChatVisibility() + value) % 3);
        }

        if (settingsOption == GameSettings.Options.CHAT_COLOR) {
            chatColours = !chatColours;
        }

        if (settingsOption == GameSettings.Options.CHAT_LINKS) {
            chatLinks = !chatLinks;
        }

        if (settingsOption == GameSettings.Options.CHAT_LINKS_PROMPT) {
            chatLinksPrompt = !chatLinksPrompt;
        }

        if (settingsOption == GameSettings.Options.SNOOPER_ENABLED) {
            snooperEnabled = !snooperEnabled;
        }

        if (settingsOption == GameSettings.Options.TOUCHSCREEN) {
            touchscreen = !touchscreen;
        }

        if (settingsOption == GameSettings.Options.USE_FULLSCREEN) {
            fullScreen = !fullScreen;

            if (mc.isFullScreen() != fullScreen) {
                mc.toggleFullscreen();
            }
        }

        if (settingsOption == GameSettings.Options.ENABLE_VSYNC) {
            enableVsync = !enableVsync;
            Display.setVSyncEnabled(enableVsync);
        }

        if (settingsOption == GameSettings.Options.USE_VBO) {
            useVbo = !useVbo;
            mc.renderGlobal.loadRenderers();
        }

        if (settingsOption == GameSettings.Options.BLOCK_ALTERNATIVES) {
            allowBlockAlternatives = !allowBlockAlternatives;
            mc.renderGlobal.loadRenderers();
        }

        if (settingsOption == GameSettings.Options.REDUCED_DEBUG_INFO) {
            reducedDebugInfo = !reducedDebugInfo;
        }

        if (settingsOption == GameSettings.Options.ENTITY_SHADOWS) {
            entityShadows = !entityShadows;
        }

        saveOptions();
    }

    public float getOptionFloatValue(GameSettings.Options settingOption) {
        float f = getOptionFloatValueOF(settingOption);
        return f != Float.MAX_VALUE ? f : (settingOption == GameSettings.Options.FOV ? fovSetting : (settingOption == GameSettings.Options.GAMMA ? gammaSetting : (settingOption == GameSettings.Options.SATURATION ? saturation : (settingOption == GameSettings.Options.SENSITIVITY ? mouseSensitivity : (settingOption == GameSettings.Options.CHAT_OPACITY ? chatOpacity : (settingOption == GameSettings.Options.CHAT_HEIGHT_FOCUSED ? chatHeightFocused : (settingOption == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED ? chatHeightUnfocused : (settingOption == GameSettings.Options.CHAT_SCALE ? chatScale : (settingOption == GameSettings.Options.CHAT_WIDTH ? chatWidth : (settingOption == GameSettings.Options.FRAMERATE_LIMIT ? (float) limitFramerate : (settingOption == GameSettings.Options.MIPMAP_LEVELS ? (float) mipmapLevels : (settingOption == GameSettings.Options.RENDER_DISTANCE ? (float) renderDistanceChunks : 0F))))))))))));
    }

    public boolean getOptionOrdinalValue(GameSettings.Options settingOption) {
        return switch (settingOption) {
            case INVERT_MOUSE -> invertMouse;
            case VIEW_BOBBING -> viewBobbing;
            case ANAGLYPH -> anaglyph;
            case FBO_ENABLE -> fboEnable;
            case CHAT_COLOR -> chatColours;
            case CHAT_LINKS -> chatLinks;
            case CHAT_LINKS_PROMPT -> chatLinksPrompt;
            case SNOOPER_ENABLED -> snooperEnabled;
            case USE_FULLSCREEN -> fullScreen;
            case ENABLE_VSYNC -> enableVsync;
            case USE_VBO -> useVbo;
            case TOUCHSCREEN -> touchscreen;
            case FORCE_UNICODE_FONT -> forceUnicodeFont;
            case BLOCK_ALTERNATIVES -> allowBlockAlternatives;
            case REDUCED_DEBUG_INFO -> reducedDebugInfo;
            case ENTITY_SHADOWS -> entityShadows;
            default -> false;
        };
    }

    public String getKeyBinding(GameSettings.Options settingOption) {
        String s = getKeyBindingOF(settingOption);

        if (s != null) {
            return s;
        } else {
            String s1 = I18n.format(settingOption.getEnumString()) + ": ";

            if (settingOption.getEnumFloat()) {
                float f1 = getOptionFloatValue(settingOption);
                float f = settingOption.normalizeValue(f1);
                return settingOption == GameSettings.Options.MIPMAP_LEVELS && (double) f1 >= 4.0D ? s1 + Lang.get("of.general.max") : (settingOption == GameSettings.Options.SENSITIVITY ? (f == 0.0F ? s1 + I18n.format("options.sensitivity.min") : (f == 1.0F ? s1 + I18n.format("options.sensitivity.max") : s1 + (int) (f * 200.0F) + "%")) : (settingOption == GameSettings.Options.FOV ? (f1 == 70.0F ? s1 + I18n.format("options.fov.min") : (f1 == 110.0F ? s1 + I18n.format("options.fov.max") : s1 + (int) f1)) : (settingOption == GameSettings.Options.FRAMERATE_LIMIT ? (f1 == settingOption.valueMax ? s1 + I18n.format("options.framerateLimit.max") : s1 + (int) f1 + " fps") : (settingOption == GameSettings.Options.RENDER_CLOUDS ? (f1 == settingOption.valueMin ? s1 + I18n.format("options.cloudHeight.min") : s1 + ((int) f1 + 128)) : (settingOption == GameSettings.Options.GAMMA ? (f == 0.0F ? s1 + I18n.format("options.gamma.min") : (f == 1.0F ? s1 + I18n.format("options.gamma.max") : s1 + "+" + (int) (f * 100.0F) + "%")) : (settingOption == GameSettings.Options.SATURATION ? s1 + (int) (f * 400.0F) + "%" : (settingOption == GameSettings.Options.CHAT_OPACITY ? s1 + (int) (f * 90.0F + 10.0F) + "%" : (settingOption == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED ? s1 + GuiNewChat.calculateChatboxHeight(f) + "px" : (settingOption == GameSettings.Options.CHAT_HEIGHT_FOCUSED ? s1 + GuiNewChat.calculateChatboxHeight(f) + "px" : (settingOption == GameSettings.Options.CHAT_WIDTH ? s1 + GuiNewChat.calculateChatboxWidth(f) + "px" : (settingOption == GameSettings.Options.RENDER_DISTANCE ? s1 + (int) f1 + " chunks" : (settingOption == GameSettings.Options.MIPMAP_LEVELS ? (f1 == 0.0F ? s1 + I18n.format("options.off") : s1 + (int) f1) : (f == 0.0F ? s1 + I18n.format("options.off") : s1 + (int) (f * 100.0F) + "%")))))))))))));
            } else if (settingOption.getEnumBoolean()) {
                boolean flag = getOptionOrdinalValue(settingOption);
                return flag ? s1 + I18n.format("options.on") : s1 + I18n.format("options.off");
            } else if (settingOption == GameSettings.Options.GUI_SCALE) {
                return guiScale >= GUISCALES.length ? s1 + guiScale + "x" : s1 + getTranslation(GUISCALES, guiScale);
            } else if (settingOption == GameSettings.Options.CHAT_VISIBILITY) {
                return s1 + I18n.format(chatVisibility.getResourceKey());
            } else if (settingOption == GameSettings.Options.PARTICLES) {
                return s1 + getTranslation(PARTICLES, particleSetting);
            } else if (settingOption == GameSettings.Options.AMBIENT_OCCLUSION) {
                return s1 + getTranslation(AMBIENT_OCCLUSIONS, ambientOcclusion);
            } else if (settingOption == GameSettings.Options.RENDER_CLOUDS) {
                return s1 + getTranslation(CLOUDS_TYPES, clouds);
            } else if (settingOption == GameSettings.Options.GRAPHICS) {
                if (fancyGraphics) {
                    return s1 + I18n.format("options.graphics.fancy");
                } else {
                    String s2 = "options.graphics.fast";
                    return s1 + I18n.format("options.graphics.fast");
                }
            } else {
                return s1;
            }
        }
    }

    public void loadOptions() {
        FileInputStream fileinputstream = null;
        label2:
        {
            try {
                if (optionsFile.exists()) {
                    BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(fileinputstream = new FileInputStream(optionsFile)));
                    String s = "";
                    mapSoundLevels.clear();

                    while ((s = bufferedreader.readLine()) != null) {
                        try {
                            String[] astring = s.split(":");

                            if (astring[0].equals("mouseSensitivity")) {
                                mouseSensitivity = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("fov")) {
                                fovSetting = parseFloat(astring[1]) * 40.0F + 70.0F;
                            }

                            if (astring[0].equals("gamma")) {
                                gammaSetting = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("saturation")) {
                                saturation = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("invertYMouse")) {
                                invertMouse = astring[1].equals("true");
                            }

                            if (astring[0].equals("renderDistance")) {
                                renderDistanceChunks = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("guiScale")) {
                                guiScale = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("particles")) {
                                particleSetting = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("bobView")) {
                                viewBobbing = astring[1].equals("true");
                            }

                            if (astring[0].equals("anaglyph3d")) {
                                anaglyph = astring[1].equals("true");
                            }

                            if (astring[0].equals("maxFps")) {
                                limitFramerate = Integer.parseInt(astring[1]);

                                if (enableVsync) {
                                    limitFramerate = (int) GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
                                }

                                if (limitFramerate <= 0) {
                                    limitFramerate = (int) GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
                                }
                            }

                            if (astring[0].equals("fboEnable")) {
                                fboEnable = astring[1].equals("true");
                            }

                            if (astring[0].equals("difficulty")) {
                                difficulty = EnumDifficulty.getDifficultyEnum(Integer.parseInt(astring[1]));
                            }

                            if (astring[0].equals("fancyGraphics")) {
                                fancyGraphics = astring[1].equals("true");
                                updateRenderClouds();
                            }

                            if (astring[0].equals("ao")) {
                                if (astring[1].equals("true")) {
                                    ambientOcclusion = 2;
                                } else if (astring[1].equals("false")) {
                                    ambientOcclusion = 0;
                                } else {
                                    ambientOcclusion = Integer.parseInt(astring[1]);
                                }
                            }

                            if (astring[0].equals("renderClouds")) {
                                switch (astring[1]) {
                                    case "true" -> clouds = 2;
                                    case "false" -> clouds = 0;
                                    case "fast" -> clouds = 1;
                                }
                            }

                            if (astring[0].equals("resourcePacks")) {
                                resourcePacks = gson.fromJson(s.substring(s.indexOf(58) + 1), typeListString);

                                if (resourcePacks == null) {
                                    resourcePacks = Lists.newArrayList();
                                }
                            }

                            if (astring[0].equals("incompatibleResourcePacks")) {
                                incompatibleResourcePacks = gson.fromJson(s.substring(s.indexOf(58) + 1), typeListString);

                                if (incompatibleResourcePacks == null) {
                                    incompatibleResourcePacks = Lists.newArrayList();
                                }
                            }

                            if (astring[0].equals("lastServer") && astring.length >= 2) {
                                lastServer = s.substring(s.indexOf(58) + 1);
                            }

                            if (astring[0].equals("lang") && astring.length >= 2) {
                                language = astring[1];
                            }

                            if (astring[0].equals("chatVisibility")) {
                                chatVisibility = EntityPlayer.EnumChatVisibility.getEnumChatVisibility(Integer.parseInt(astring[1]));
                            }

                            if (astring[0].equals("chatColors")) {
                                chatColours = astring[1].equals("true");
                            }

                            if (astring[0].equals("chatLinks")) {
                                chatLinks = astring[1].equals("true");
                            }

                            if (astring[0].equals("chatLinksPrompt")) {
                                chatLinksPrompt = astring[1].equals("true");
                            }

                            if (astring[0].equals("chatOpacity")) {
                                chatOpacity = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("snooperEnabled")) {
                                snooperEnabled = astring[1].equals("true");
                            }

                            if (astring[0].equals("fullscreen")) {
                                fullScreen = astring[1].equals("true");
                            }

                            if (astring[0].equals("enableVsync")) {
                                enableVsync = astring[1].equals("true");

                                if (enableVsync) {
                                    limitFramerate = (int) GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
                                }

                                updateVSync();
                            }

                            if (astring[0].equals("useVbo")) {
                                useVbo = astring[1].equals("true");
                            }

                            if (astring[0].equals("hideServerAddress")) {
                                hideServerAddress = astring[1].equals("true");
                            }

                            if (astring[0].equals("advancedItemTooltips")) {
                                advancedItemTooltips = astring[1].equals("true");
                            }

                            if (astring[0].equals("pauseOnLostFocus")) {
                                pauseOnLostFocus = astring[1].equals("true");
                            }

                            if (astring[0].equals("touchscreen")) {
                                touchscreen = astring[1].equals("true");
                            }

                            if (astring[0].equals("overrideHeight")) {
                                overrideHeight = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("overrideWidth")) {
                                overrideWidth = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("heldItemTooltips")) {
                                heldItemTooltips = astring[1].equals("true");
                            }

                            if (astring[0].equals("chatHeightFocused")) {
                                chatHeightFocused = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("chatHeightUnfocused")) {
                                chatHeightUnfocused = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("chatScale")) {
                                chatScale = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("chatWidth")) {
                                chatWidth = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("showInventoryAchievementHint")) {
                                showInventoryAchievementHint = astring[1].equals("true");
                            }

                            if (astring[0].equals("mipmapLevels")) {
                                mipmapLevels = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("forceUnicodeFont")) {
                                forceUnicodeFont = astring[1].equals("true");
                            }

                            if (astring[0].equals("allowBlockAlternatives")) {
                                allowBlockAlternatives = astring[1].equals("true");
                            }

                            if (astring[0].equals("reducedDebugInfo")) {
                                reducedDebugInfo = astring[1].equals("true");
                            }

                            if (astring[0].equals("useNativeTransport")) {
                                useNativeTransport = astring[1].equals("true");
                            }

                            if (astring[0].equals("entityShadows")) {
                                entityShadows = astring[1].equals("true");
                            }

                            for (KeyBinding keybinding : keyBindings) {
                                if (astring[0].equals("key_" + keybinding.getKeyDescription())) {
                                    keybinding.setKeyCode(Integer.parseInt(astring[1]));
                                }
                            }

                            for (SoundCategory soundcategory : SoundCategory.values()) {
                                if (astring[0].equals("soundCategory_" + soundcategory.getCategoryName())) {
                                    mapSoundLevels.put(soundcategory, parseFloat(astring[1]));
                                }
                            }

                            for (EnumPlayerModelParts enumplayermodelparts : EnumPlayerModelParts.values()) {
                                if (astring[0].equals("modelPart_" + enumplayermodelparts.getPartName())) {
                                    setModelPartEnabled(enumplayermodelparts, astring[1].equals("true"));
                                }
                            }
                        } catch (Exception exception) {
                            logger.warn("Skipping bad option: {}", s);
                            exception.printStackTrace();
                        }
                    }

                    KeyBinding.resetKeyBindingArrayAndHash();
                    bufferedreader.close();
                    break label2;
                }
            } catch (Exception exception1) {
                logger.error("Failed to load options", exception1);
                break label2;
            } finally {
                IOUtils.closeQuietly(fileinputstream);
            }

            return;
        }
        loadOfOptions();
    }

    private float parseFloat(String str) {
        return str.equals("true") ? 1.0F : (str.equals("false") ? 0.0F : Float.parseFloat(str));
    }

    public void saveOptions() {
        try {
            PrintWriter printwriter = new PrintWriter(new FileWriter(optionsFile));
            printwriter.println("invertYMouse:" + invertMouse);
            printwriter.println("mouseSensitivity:" + mouseSensitivity);
            printwriter.println("fov:" + (fovSetting - 70.0F) / 40.0F);
            printwriter.println("gamma:" + gammaSetting);
            printwriter.println("saturation:" + saturation);
            printwriter.println("renderDistance:" + renderDistanceChunks);
            printwriter.println("guiScale:" + guiScale);
            printwriter.println("particles:" + particleSetting);
            printwriter.println("bobView:" + viewBobbing);
            printwriter.println("anaglyph3d:" + anaglyph);
            printwriter.println("maxFps:" + limitFramerate);
            printwriter.println("fboEnable:" + fboEnable);
            printwriter.println("difficulty:" + difficulty.getDifficultyId());
            printwriter.println("fancyGraphics:" + fancyGraphics);
            printwriter.println("ao:" + ambientOcclusion);

            switch (clouds) {
                case 0:
                    printwriter.println("renderClouds:false");
                    break;

                case 1:
                    printwriter.println("renderClouds:fast");
                    break;

                case 2:
                    printwriter.println("renderClouds:true");
            }

            printwriter.println("resourcePacks:" + gson.toJson(resourcePacks));
            printwriter.println("incompatibleResourcePacks:" + gson.toJson(incompatibleResourcePacks));
            printwriter.println("lastServer:" + lastServer);
            printwriter.println("lang:" + language);
            printwriter.println("chatVisibility:" + chatVisibility.getChatVisibility());
            printwriter.println("chatColors:" + chatColours);
            printwriter.println("chatLinks:" + chatLinks);
            printwriter.println("chatLinksPrompt:" + chatLinksPrompt);
            printwriter.println("chatOpacity:" + chatOpacity);
            printwriter.println("snooperEnabled:" + snooperEnabled);
            printwriter.println("fullscreen:" + fullScreen);
            printwriter.println("enableVsync:" + enableVsync);
            printwriter.println("useVbo:" + useVbo);
            printwriter.println("hideServerAddress:" + hideServerAddress);
            printwriter.println("advancedItemTooltips:" + advancedItemTooltips);
            printwriter.println("pauseOnLostFocus:" + pauseOnLostFocus);
            printwriter.println("touchscreen:" + touchscreen);
            printwriter.println("overrideWidth:" + overrideWidth);
            printwriter.println("overrideHeight:" + overrideHeight);
            printwriter.println("heldItemTooltips:" + heldItemTooltips);
            printwriter.println("chatHeightFocused:" + chatHeightFocused);
            printwriter.println("chatHeightUnfocused:" + chatHeightUnfocused);
            printwriter.println("chatScale:" + chatScale);
            printwriter.println("chatWidth:" + chatWidth);
            printwriter.println("showInventoryAchievementHint:" + showInventoryAchievementHint);
            printwriter.println("mipmapLevels:" + mipmapLevels);
            printwriter.println("forceUnicodeFont:" + forceUnicodeFont);
            printwriter.println("allowBlockAlternatives:" + allowBlockAlternatives);
            printwriter.println("reducedDebugInfo:" + reducedDebugInfo);
            printwriter.println("useNativeTransport:" + useNativeTransport);
            printwriter.println("entityShadows:" + entityShadows);

            for (KeyBinding keybinding : keyBindings) {
                printwriter.println("key_" + keybinding.getKeyDescription() + ":" + keybinding.getKeyCode());
            }

            for (SoundCategory soundcategory : SoundCategory.values()) {
                printwriter.println("soundCategory_" + soundcategory.getCategoryName() + ":" + getSoundLevel(soundcategory));
            }

            for (EnumPlayerModelParts enumplayermodelparts : EnumPlayerModelParts.values()) {
                printwriter.println("modelPart_" + enumplayermodelparts.getPartName() + ":" + setModelParts.contains(enumplayermodelparts));
            }

            printwriter.close();
        } catch (Exception exception) {
            logger.error("Failed to save options", exception);
        }

        saveOfOptions();
        sendSettingsToServer();
    }

    public float getSoundLevel(SoundCategory sndCategory) {
        return mapSoundLevels.getOrDefault(sndCategory, 1.0F);
    }

    public void setSoundLevel(SoundCategory sndCategory, float soundLevel) {
        mc.getSoundHandler().setSoundLevel(sndCategory, soundLevel);
        mapSoundLevels.put(sndCategory, soundLevel);
    }

    public void sendSettingsToServer() {
        if (mc.thePlayer != null) {
            int i = 0;

            for (EnumPlayerModelParts enumplayermodelparts : setModelParts) {
                i |= enumplayermodelparts.getPartMask();
            }

            mc.thePlayer.sendQueue.addToSendQueue(new C15PacketClientSettings(language, renderDistanceChunks, chatVisibility, chatColours, i));
        }
    }

    public Set<EnumPlayerModelParts> getModelParts() {
        return ImmutableSet.copyOf(setModelParts);
    }

    public void setModelPartEnabled(EnumPlayerModelParts modelPart, boolean enable) {
        if (enable) {
            setModelParts.add(modelPart);
        } else {
            setModelParts.remove(modelPart);
        }

        sendSettingsToServer();
    }

    public void switchModelPartEnabled(EnumPlayerModelParts modelPart) {
        if (!getModelParts().contains(modelPart)) {
            setModelParts.add(modelPart);
        } else {
            setModelParts.remove(modelPart);
        }

        sendSettingsToServer();
    }

    public int shouldRenderClouds() {
        return renderDistanceChunks >= 4 ? clouds : 0;
    }

    public boolean isUsingNativeTransport() {
        return useNativeTransport;
    }

    private void setOptionFloatValueOF(GameSettings.Options p_setOptionFloatValueOF_1_, float p_setOptionFloatValueOF_2_) {
        if (p_setOptionFloatValueOF_1_ == GameSettings.Options.CLOUD_HEIGHT) {
            ofCloudsHeight = p_setOptionFloatValueOF_2_;
            mc.renderGlobal.resetClouds();
        }

        if (p_setOptionFloatValueOF_1_ == GameSettings.Options.AO_LEVEL) {
            ofAoLevel = p_setOptionFloatValueOF_2_;
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionFloatValueOF_1_ == GameSettings.Options.AA_LEVEL) {
            int i = (int) p_setOptionFloatValueOF_2_;

            if (i > 0 && Config.isShaders()) {
                Config.showGuiMessage(Lang.get("of.message.aa.shaders1"), Lang.get("of.message.aa.shaders2"));
                return;
            }

            int[] aint = new int[]{0, 2, 4, 6, 8, 12, 16};
            ofAaLevel = 0;

            for (int k : aint) {
                if (i >= k) {
                    ofAaLevel = k;
                }
            }

            ofAaLevel = Config.limit(ofAaLevel, 0, 16);
        }

        if (p_setOptionFloatValueOF_1_ == GameSettings.Options.AF_LEVEL) {
            int k = (int) p_setOptionFloatValueOF_2_;

            if (k > 1 && Config.isShaders()) {
                Config.showGuiMessage(Lang.get("of.message.af.shaders1"), Lang.get("of.message.af.shaders2"));
                return;
            }

            for (ofAfLevel = 1; ofAfLevel * 2 <= k; ofAfLevel *= 2) {
            }

            ofAfLevel = Config.limit(ofAfLevel, 1, 16);
            mc.refreshResources();
        }

        if (p_setOptionFloatValueOF_1_ == GameSettings.Options.MIPMAP_TYPE) {
            int l = (int) p_setOptionFloatValueOF_2_;
            ofMipmapType = Config.limit(l, 0, 3);
            mc.refreshResources();
        }

        if (p_setOptionFloatValueOF_1_ == GameSettings.Options.FULLSCREEN_MODE) {
            int i1 = (int) p_setOptionFloatValueOF_2_ - 1;
            String[] astring = Config.getDisplayModeNames();

            if (i1 < 0 || i1 >= astring.length) {
                ofFullscreenMode = "Default";
                return;
            }

            ofFullscreenMode = astring[i1];
        }
    }

    private float getOptionFloatValueOF(GameSettings.Options p_getOptionFloatValueOF_1_) {
        if (p_getOptionFloatValueOF_1_ == GameSettings.Options.CLOUD_HEIGHT) {
            return ofCloudsHeight;
        } else if (p_getOptionFloatValueOF_1_ == GameSettings.Options.AO_LEVEL) {
            return ofAoLevel;
        } else if (p_getOptionFloatValueOF_1_ == GameSettings.Options.AA_LEVEL) {
            return (float) ofAaLevel;
        } else if (p_getOptionFloatValueOF_1_ == GameSettings.Options.AF_LEVEL) {
            return (float) ofAfLevel;
        } else if (p_getOptionFloatValueOF_1_ == GameSettings.Options.MIPMAP_TYPE) {
            return (float) ofMipmapType;
        } else if (p_getOptionFloatValueOF_1_ == GameSettings.Options.FRAMERATE_LIMIT) {
            return (float) limitFramerate == GameSettings.Options.FRAMERATE_LIMIT.getValueMax() && enableVsync ? 0.0F : (float) limitFramerate;
        } else if (p_getOptionFloatValueOF_1_ == GameSettings.Options.FULLSCREEN_MODE) {
            if (ofFullscreenMode.equals("Default")) {
                return 0.0F;
            } else {
                List<String> list = Arrays.asList(Config.getDisplayModeNames());
                int i = list.indexOf(ofFullscreenMode);
                return i < 0 ? 0.0F : (float) (i + 1);
            }
        } else {
            return Float.MAX_VALUE;
        }
    }

    private void setOptionValueOF(GameSettings.Options p_setOptionValueOF_1_, int p_setOptionValueOF_2_) {
        if (p_setOptionValueOF_1_ == GameSettings.Options.FOG_FANCY) {
            switch (ofFogType) {
                case 1:
                    ofFogType = 2;

                    if (!Config.isFancyFogAvailable()) {
                        ofFogType = 3;
                    }

                    break;

                case 2:
                    ofFogType = 3;
                    break;

                case 3:
                    ofFogType = 1;
                    break;

                default:
                    ofFogType = 1;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.FOG_START) {
            ofFogStart += 0.2F;

            if (ofFogStart > 0.81F) {
                ofFogStart = 0.2F;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SMOOTH_FPS) {
            ofSmoothFps = !ofSmoothFps;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SMOOTH_WORLD) {
            ofSmoothWorld = !ofSmoothWorld;
            Config.updateThreadPriorities();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CLOUDS) {
            ++ofClouds;

            if (ofClouds > 3) {
                ofClouds = 0;
            }

            updateRenderClouds();
            mc.renderGlobal.resetClouds();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.TREES) {
            ofTrees = nextValue(ofTrees, OF_TREES_VALUES);
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.DROPPED_ITEMS) {
            ++ofDroppedItems;

            if (ofDroppedItems > 2) {
                ofDroppedItems = 0;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.RAIN) {
            ++ofRain;

            if (ofRain > 3) {
                ofRain = 0;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_WATER) {
            ++ofAnimatedWater;

            if (ofAnimatedWater == 1) {
                ++ofAnimatedWater;
            }

            if (ofAnimatedWater > 2) {
                ofAnimatedWater = 0;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_LAVA) {
            ++ofAnimatedLava;

            if (ofAnimatedLava == 1) {
                ++ofAnimatedLava;
            }

            if (ofAnimatedLava > 2) {
                ofAnimatedLava = 0;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_FIRE) {
            ofAnimatedFire = !ofAnimatedFire;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_PORTAL) {
            ofAnimatedPortal = !ofAnimatedPortal;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_REDSTONE) {
            ofAnimatedRedstone = !ofAnimatedRedstone;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_EXPLOSION) {
            ofAnimatedExplosion = !ofAnimatedExplosion;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_FLAME) {
            ofAnimatedFlame = !ofAnimatedFlame;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_SMOKE) {
            ofAnimatedSmoke = !ofAnimatedSmoke;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.VOID_PARTICLES) {
            ofVoidParticles = !ofVoidParticles;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.WATER_PARTICLES) {
            ofWaterParticles = !ofWaterParticles;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.PORTAL_PARTICLES) {
            ofPortalParticles = !ofPortalParticles;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.POTION_PARTICLES) {
            ofPotionParticles = !ofPotionParticles;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.FIREWORK_PARTICLES) {
            ofFireworkParticles = !ofFireworkParticles;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.DRIPPING_WATER_LAVA) {
            ofDrippingWaterLava = !ofDrippingWaterLava;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_TERRAIN) {
            ofAnimatedTerrain = !ofAnimatedTerrain;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_TEXTURES) {
            ofAnimatedTextures = !ofAnimatedTextures;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.RAIN_SPLASH) {
            ofRainSplash = !ofRainSplash;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.LAGOMETER) {
            ofLagometer = !ofLagometer;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SHOW_FPS) {
            ofShowFps = !ofShowFps;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.AUTOSAVE_TICKS) {
            int i = 900;
            ofAutoSaveTicks = Math.max(ofAutoSaveTicks / i * i, i);
            ofAutoSaveTicks *= 2;

            if (ofAutoSaveTicks > 32 * i) {
                ofAutoSaveTicks = i;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.BETTER_GRASS) {
            ++ofBetterGrass;

            if (ofBetterGrass > 3) {
                ofBetterGrass = 1;
            }

            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CONNECTED_TEXTURES) {
            ++ofConnectedTextures;

            if (ofConnectedTextures > 3) {
                ofConnectedTextures = 1;
            }

            if (ofConnectedTextures == 2) {
                mc.renderGlobal.loadRenderers();
            } else {
                mc.refreshResources();
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.WEATHER) {
            ofWeather = !ofWeather;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SKY) {
            ofSky = !ofSky;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.STARS) {
            ofStars = !ofStars;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SUN_MOON) {
            ofSunMoon = !ofSunMoon;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.VIGNETTE) {
            ++ofVignette;

            if (ofVignette > 2) {
                ofVignette = 0;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CHUNK_UPDATES) {
            ++ofChunkUpdates;

            if (ofChunkUpdates > 5) {
                ofChunkUpdates = 1;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CHUNK_UPDATES_DYNAMIC) {
            ofChunkUpdatesDynamic = !ofChunkUpdatesDynamic;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.TIME) {
            ++ofTime;

            if (ofTime > 2) {
                ofTime = 0;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CLEAR_WATER) {
            ofClearWater = !ofClearWater;
            updateWaterOpacity();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.PROFILER) {
            ofProfiler = !ofProfiler;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.BETTER_SNOW) {
            ofBetterSnow = !ofBetterSnow;
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SWAMP_COLORS) {
            ofSwampColors = !ofSwampColors;
            CustomColors.updateUseDefaultGrassFoliageColors();
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.RANDOM_ENTITIES) {
            ofRandomEntities = !ofRandomEntities;
            RandomEntities.update();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SMOOTH_BIOMES) {
            ofSmoothBiomes = !ofSmoothBiomes;
            CustomColors.updateUseDefaultGrassFoliageColors();
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CUSTOM_FONTS) {
            ofCustomFonts = !ofCustomFonts;
            mc.fontRendererObj.onResourceManagerReload(Config.getResourceManager());
            mc.standardGalacticFontRenderer.onResourceManagerReload(Config.getResourceManager());
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CUSTOM_COLORS) {
            ofCustomColors = !ofCustomColors;
            CustomColors.update();
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CUSTOM_ITEMS) {
            ofCustomItems = !ofCustomItems;
            mc.refreshResources();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CUSTOM_SKY) {
            ofCustomSky = !ofCustomSky;
            CustomSky.update();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SHOW_CAPES) {
            ofShowCapes = !ofShowCapes;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.NATURAL_TEXTURES) {
            ofNaturalTextures = !ofNaturalTextures;
            NaturalTextures.update();
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.EMISSIVE_TEXTURES) {
            ofEmissiveTextures = !ofEmissiveTextures;
            mc.refreshResources();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.FAST_MATH) {
            ofFastMath = !ofFastMath;
            MathHelper.fastMath = ofFastMath;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.FAST_RENDER) {
            if (!ofFastRender && Config.isShaders()) {
                Config.showGuiMessage(Lang.get("of.message.fr.shaders1"), Lang.get("of.message.fr.shaders2"));
                return;
            }

            ofFastRender = !ofFastRender;

            if (ofFastRender) {
                mc.entityRenderer.stopUseShader();
            }

            Config.updateFramebufferSize();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.TRANSLUCENT_BLOCKS) {
            if (ofTranslucentBlocks == 0) {
                ofTranslucentBlocks = 1;
            } else if (ofTranslucentBlocks == 1) {
                ofTranslucentBlocks = 2;
            } else if (ofTranslucentBlocks == 2) {
                ofTranslucentBlocks = 0;
            } else {
                ofTranslucentBlocks = 0;
            }

            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.LAZY_CHUNK_LOADING) {
            ofLazyChunkLoading = !ofLazyChunkLoading;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.RENDER_REGIONS) {
            ofRenderRegions = !ofRenderRegions;
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SMART_ANIMATIONS) {
            ofSmartAnimations = !ofSmartAnimations;
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.DYNAMIC_FOV) {
            ofDynamicFov = !ofDynamicFov;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ALTERNATE_BLOCKS) {
            ofAlternateBlocks = !ofAlternateBlocks;
            mc.refreshResources();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.DYNAMIC_LIGHTS) {
            ofDynamicLights = nextValue(ofDynamicLights, OF_DYNAMIC_LIGHTS);
            DynamicLights.removeLights(mc.renderGlobal);
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SCREENSHOT_SIZE) {
            ++ofScreenshotSize;

            if (ofScreenshotSize > 4) {
                ofScreenshotSize = 1;
            }

            if (!OpenGlHelper.isFramebufferEnabled()) {
                ofScreenshotSize = 1;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CUSTOM_ENTITY_MODELS) {
            ofCustomEntityModels = !ofCustomEntityModels;
            mc.refreshResources();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CUSTOM_GUIS) {
            ofCustomGuis = !ofCustomGuis;
            CustomGuis.update();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SHOW_GL_ERRORS) {
            ofShowGlErrors = !ofShowGlErrors;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.HELD_ITEM_TOOLTIPS) {
            heldItemTooltips = !heldItemTooltips;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ADVANCED_TOOLTIPS) {
            advancedItemTooltips = !advancedItemTooltips;
        }
    }

    private String getKeyBindingOF(GameSettings.Options p_getKeyBindingOF_1_) {
        String s = I18n.format(p_getKeyBindingOF_1_.getEnumString()) + ": ";

        if (s == null) {
            s = p_getKeyBindingOF_1_.getEnumString();
        }

        if (p_getKeyBindingOF_1_ == GameSettings.Options.RENDER_DISTANCE) {
            int i1 = (int) getOptionFloatValue(p_getKeyBindingOF_1_);
            String s1 = I18n.format("options.renderDistance.tiny");
            int i = 2;

            if (i1 >= 4) {
                s1 = I18n.format("options.renderDistance.short");
                i = 4;
            }

            if (i1 >= 8) {
                s1 = I18n.format("options.renderDistance.normal");
                i = 8;
            }

            if (i1 >= 16) {
                s1 = I18n.format("options.renderDistance.far");
                i = 16;
            }

            if (i1 >= 32) {
                s1 = Lang.get("of.options.renderDistance.extreme");
                i = 32;
            }

            if (i1 >= 48) {
                s1 = Lang.get("of.options.renderDistance.insane");
                i = 48;
            }

            if (i1 >= 64) {
                s1 = Lang.get("of.options.renderDistance.ludicrous");
                i = 64;
            }

            int j = renderDistanceChunks - i;
            String s2 = s1;

            if (j > 0) {
                s2 = s1 + "+";
            }

            return s + i1 + " " + s2;
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.FOG_FANCY) {
            return switch (ofFogType) {
                case 1 -> s + Lang.getFast();
                case 2 -> s + Lang.getFancy();
                case 3 -> s + Lang.getOff();
                default -> s + Lang.getOff();
            };
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.FOG_START) {
            return s + ofFogStart;
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.MIPMAP_TYPE) {
            return switch (ofMipmapType) {
                case 0 -> s + Lang.get("of.options.mipmap.nearest");
                case 1 -> s + Lang.get("of.options.mipmap.linear");
                case 2 -> s + Lang.get("of.options.mipmap.bilinear");
                case 3 -> s + Lang.get("of.options.mipmap.trilinear");
                default -> s + "of.options.mipmap.nearest";
            };
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.SMOOTH_FPS) {
            return ofSmoothFps ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.SMOOTH_WORLD) {
            return ofSmoothWorld ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.CLOUDS) {
            return switch (ofClouds) {
                case 1 -> s + Lang.getFast();
                case 2 -> s + Lang.getFancy();
                case 3 -> s + Lang.getOff();
                default -> s + Lang.getDefault();
            };
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.TREES) {
            return switch (ofTrees) {
                case 1 -> s + Lang.getFast();
                case 2 -> s + Lang.getFancy();
                default -> s + Lang.getDefault();
                case 4 -> s + Lang.get("of.general.smart");
            };
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.DROPPED_ITEMS) {
            return switch (ofDroppedItems) {
                case 1 -> s + Lang.getFast();
                case 2 -> s + Lang.getFancy();
                default -> s + Lang.getDefault();
            };
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.RAIN) {
            return switch (ofRain) {
                case 1 -> s + Lang.getFast();
                case 2 -> s + Lang.getFancy();
                case 3 -> s + Lang.getOff();
                default -> s + Lang.getDefault();
            };
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.ANIMATED_WATER) {
            return switch (ofAnimatedWater) {
                case 1 -> s + Lang.get("of.options.animation.dynamic");
                case 2 -> s + Lang.getOff();
                default -> s + Lang.getOn();
            };
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.ANIMATED_LAVA) {
            return switch (ofAnimatedLava) {
                case 1 -> s + Lang.get("of.options.animation.dynamic");
                case 2 -> s + Lang.getOff();
                default -> s + Lang.getOn();
            };
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.ANIMATED_FIRE) {
            return ofAnimatedFire ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.ANIMATED_PORTAL) {
            return ofAnimatedPortal ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.ANIMATED_REDSTONE) {
            return ofAnimatedRedstone ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.ANIMATED_EXPLOSION) {
            return ofAnimatedExplosion ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.ANIMATED_FLAME) {
            return ofAnimatedFlame ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.ANIMATED_SMOKE) {
            return ofAnimatedSmoke ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.VOID_PARTICLES) {
            return ofVoidParticles ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.WATER_PARTICLES) {
            return ofWaterParticles ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.PORTAL_PARTICLES) {
            return ofPortalParticles ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.POTION_PARTICLES) {
            return ofPotionParticles ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.FIREWORK_PARTICLES) {
            return ofFireworkParticles ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.DRIPPING_WATER_LAVA) {
            return ofDrippingWaterLava ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.ANIMATED_TERRAIN) {
            return ofAnimatedTerrain ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.ANIMATED_TEXTURES) {
            return ofAnimatedTextures ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.RAIN_SPLASH) {
            return ofRainSplash ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.LAGOMETER) {
            return ofLagometer ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.SHOW_FPS) {
            return ofShowFps ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.AUTOSAVE_TICKS) {
            int l = 900;
            return ofAutoSaveTicks <= l ? s + Lang.get("of.options.save.45s") : (ofAutoSaveTicks <= 2 * l ? s + Lang.get("of.options.save.90s") : (ofAutoSaveTicks <= 4 * l ? s + Lang.get("of.options.save.3min") : (ofAutoSaveTicks <= 8 * l ? s + Lang.get("of.options.save.6min") : (ofAutoSaveTicks <= 16 * l ? s + Lang.get("of.options.save.12min") : s + Lang.get("of.options.save.24min")))));
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.BETTER_GRASS) {
            return switch (ofBetterGrass) {
                case 1 -> s + Lang.getFast();
                case 2 -> s + Lang.getFancy();
                default -> s + Lang.getOff();
            };
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.CONNECTED_TEXTURES) {
            return switch (ofConnectedTextures) {
                case 1 -> s + Lang.getFast();
                case 2 -> s + Lang.getFancy();
                default -> s + Lang.getOff();
            };
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.WEATHER) {
            return ofWeather ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.SKY) {
            return ofSky ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.STARS) {
            return ofStars ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.SUN_MOON) {
            return ofSunMoon ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.VIGNETTE) {
            return switch (ofVignette) {
                case 1 -> s + Lang.getFast();
                case 2 -> s + Lang.getFancy();
                default -> s + Lang.getDefault();
            };
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.CHUNK_UPDATES) {
            return s + ofChunkUpdates;
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.CHUNK_UPDATES_DYNAMIC) {
            return ofChunkUpdatesDynamic ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.TIME) {
            return ofTime == 1 ? s + Lang.get("of.options.time.dayOnly") : (ofTime == 2 ? s + Lang.get("of.options.time.nightOnly") : s + Lang.getDefault());
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.CLEAR_WATER) {
            return ofClearWater ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.AA_LEVEL) {
            String s3 = "";

            if (ofAaLevel != Config.getAntialiasingLevel()) {
                s3 = " (" + Lang.get("of.general.restart") + ")";
            }

            return ofAaLevel == 0 ? s + Lang.getOff() + s3 : s + ofAaLevel + s3;
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.AF_LEVEL) {
            return ofAfLevel == 1 ? s + Lang.getOff() : s + ofAfLevel;
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.PROFILER) {
            return ofProfiler ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.BETTER_SNOW) {
            return ofBetterSnow ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.SWAMP_COLORS) {
            return ofSwampColors ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.RANDOM_ENTITIES) {
            return ofRandomEntities ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.SMOOTH_BIOMES) {
            return ofSmoothBiomes ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.CUSTOM_FONTS) {
            return ofCustomFonts ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.CUSTOM_COLORS) {
            return ofCustomColors ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.CUSTOM_SKY) {
            return ofCustomSky ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.SHOW_CAPES) {
            return ofShowCapes ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.CUSTOM_ITEMS) {
            return ofCustomItems ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.NATURAL_TEXTURES) {
            return ofNaturalTextures ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.EMISSIVE_TEXTURES) {
            return ofEmissiveTextures ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.FAST_MATH) {
            return ofFastMath ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.FAST_RENDER) {
            return ofFastRender ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.TRANSLUCENT_BLOCKS) {
            return ofTranslucentBlocks == 1 ? s + Lang.getFast() : (ofTranslucentBlocks == 2 ? s + Lang.getFancy() : s + Lang.getDefault());
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.LAZY_CHUNK_LOADING) {
            return ofLazyChunkLoading ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.RENDER_REGIONS) {
            return ofRenderRegions ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.SMART_ANIMATIONS) {
            return ofSmartAnimations ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.DYNAMIC_FOV) {
            return ofDynamicFov ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.ALTERNATE_BLOCKS) {
            return ofAlternateBlocks ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.DYNAMIC_LIGHTS) {
            int k = indexOf(ofDynamicLights, OF_DYNAMIC_LIGHTS);
            return s + getTranslation(KEYS_DYNAMIC_LIGHTS, k);
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.SCREENSHOT_SIZE) {
            return ofScreenshotSize <= 1 ? s + Lang.getDefault() : s + ofScreenshotSize + "x";
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.CUSTOM_ENTITY_MODELS) {
            return ofCustomEntityModels ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.CUSTOM_GUIS) {
            return ofCustomGuis ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.SHOW_GL_ERRORS) {
            return ofShowGlErrors ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.FULLSCREEN_MODE) {
            return ofFullscreenMode.equals("Default") ? s + Lang.getDefault() : s + ofFullscreenMode;
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.HELD_ITEM_TOOLTIPS) {
            return heldItemTooltips ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.ADVANCED_TOOLTIPS) {
            return advancedItemTooltips ? s + Lang.getOn() : s + Lang.getOff();
        } else if (p_getKeyBindingOF_1_ == GameSettings.Options.FRAMERATE_LIMIT) {
            float f = getOptionFloatValue(p_getKeyBindingOF_1_);
            return f == 0.0F ? s + Lang.get("of.options.framerateLimit.vsync") : (f == p_getKeyBindingOF_1_.valueMax ? s + I18n.format("options.framerateLimit.max") : s + (int) f + " fps");
        } else {
            return null;
        }
    }

    public void loadOfOptions() {
        try {
            File file1 = optionsFileOF;

            if (!file1.exists()) {
                file1 = optionsFile;
            }

            if (!file1.exists()) {
                return;
            }

            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(new FileInputStream(file1), StandardCharsets.UTF_8));
            String s = "";

            while ((s = bufferedreader.readLine()) != null) {
                try {
                    String[] astring = s.split(":");

                    if (astring[0].equals("ofRenderDistanceChunks") && astring.length >= 2) {
                        renderDistanceChunks = Integer.parseInt(astring[1]);
                        renderDistanceChunks = Config.limit(renderDistanceChunks, 2, 1024);
                    }

                    if (astring[0].equals("ofFogType") && astring.length >= 2) {
                        ofFogType = Integer.parseInt(astring[1]);
                        ofFogType = Config.limit(ofFogType, 1, 3);
                    }

                    if (astring[0].equals("ofFogStart") && astring.length >= 2) {
                        ofFogStart = Float.parseFloat(astring[1]);

                        if (ofFogStart < 0.2F) {
                            ofFogStart = 0.2F;
                        }

                        if (ofFogStart > 0.81F) {
                            ofFogStart = 0.8F;
                        }
                    }

                    if (astring[0].equals("ofMipmapType") && astring.length >= 2) {
                        ofMipmapType = Integer.parseInt(astring[1]);
                        ofMipmapType = Config.limit(ofMipmapType, 0, 3);
                    }

                    if (astring[0].equals("ofOcclusionFancy") && astring.length >= 2) {
                        ofOcclusionFancy = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofSmoothFps") && astring.length >= 2) {
                        ofSmoothFps = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofSmoothWorld") && astring.length >= 2) {
                        ofSmoothWorld = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofAoLevel") && astring.length >= 2) {
                        ofAoLevel = Float.parseFloat(astring[1]);
                        ofAoLevel = Config.limit(ofAoLevel, 0.0F, 1.0F);
                    }

                    if (astring[0].equals("ofClouds") && astring.length >= 2) {
                        ofClouds = Integer.parseInt(astring[1]);
                        ofClouds = Config.limit(ofClouds, 0, 3);
                        updateRenderClouds();
                    }

                    if (astring[0].equals("ofCloudsHeight") && astring.length >= 2) {
                        ofCloudsHeight = Float.parseFloat(astring[1]);
                        ofCloudsHeight = Config.limit(ofCloudsHeight, 0.0F, 1.0F);
                    }

                    if (astring[0].equals("ofTrees") && astring.length >= 2) {
                        ofTrees = Integer.parseInt(astring[1]);
                        ofTrees = limit(ofTrees, OF_TREES_VALUES);
                    }

                    if (astring[0].equals("ofDroppedItems") && astring.length >= 2) {
                        ofDroppedItems = Integer.parseInt(astring[1]);
                        ofDroppedItems = Config.limit(ofDroppedItems, 0, 2);
                    }

                    if (astring[0].equals("ofRain") && astring.length >= 2) {
                        ofRain = Integer.parseInt(astring[1]);
                        ofRain = Config.limit(ofRain, 0, 3);
                    }

                    if (astring[0].equals("ofAnimatedWater") && astring.length >= 2) {
                        ofAnimatedWater = Integer.parseInt(astring[1]);
                        ofAnimatedWater = Config.limit(ofAnimatedWater, 0, 2);
                    }

                    if (astring[0].equals("ofAnimatedLava") && astring.length >= 2) {
                        ofAnimatedLava = Integer.parseInt(astring[1]);
                        ofAnimatedLava = Config.limit(ofAnimatedLava, 0, 2);
                    }

                    if (astring[0].equals("ofAnimatedFire") && astring.length >= 2) {
                        ofAnimatedFire = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedPortal") && astring.length >= 2) {
                        ofAnimatedPortal = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedRedstone") && astring.length >= 2) {
                        ofAnimatedRedstone = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedExplosion") && astring.length >= 2) {
                        ofAnimatedExplosion = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedFlame") && astring.length >= 2) {
                        ofAnimatedFlame = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedSmoke") && astring.length >= 2) {
                        ofAnimatedSmoke = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofVoidParticles") && astring.length >= 2) {
                        ofVoidParticles = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofWaterParticles") && astring.length >= 2) {
                        ofWaterParticles = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofPortalParticles") && astring.length >= 2) {
                        ofPortalParticles = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofPotionParticles") && astring.length >= 2) {
                        ofPotionParticles = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofFireworkParticles") && astring.length >= 2) {
                        ofFireworkParticles = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofDrippingWaterLava") && astring.length >= 2) {
                        ofDrippingWaterLava = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedTerrain") && astring.length >= 2) {
                        ofAnimatedTerrain = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedTextures") && astring.length >= 2) {
                        ofAnimatedTextures = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofRainSplash") && astring.length >= 2) {
                        ofRainSplash = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofLagometer") && astring.length >= 2) {
                        ofLagometer = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofShowFps") && astring.length >= 2) {
                        ofShowFps = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofAutoSaveTicks") && astring.length >= 2) {
                        ofAutoSaveTicks = Integer.parseInt(astring[1]);
                        ofAutoSaveTicks = Config.limit(ofAutoSaveTicks, 40, 40000);
                    }

                    if (astring[0].equals("ofBetterGrass") && astring.length >= 2) {
                        ofBetterGrass = Integer.parseInt(astring[1]);
                        ofBetterGrass = Config.limit(ofBetterGrass, 1, 3);
                    }

                    if (astring[0].equals("ofConnectedTextures") && astring.length >= 2) {
                        ofConnectedTextures = Integer.parseInt(astring[1]);
                        ofConnectedTextures = Config.limit(ofConnectedTextures, 1, 3);
                    }

                    if (astring[0].equals("ofWeather") && astring.length >= 2) {
                        ofWeather = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofSky") && astring.length >= 2) {
                        ofSky = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofStars") && astring.length >= 2) {
                        ofStars = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofSunMoon") && astring.length >= 2) {
                        ofSunMoon = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofVignette") && astring.length >= 2) {
                        ofVignette = Integer.parseInt(astring[1]);
                        ofVignette = Config.limit(ofVignette, 0, 2);
                    }

                    if (astring[0].equals("ofChunkUpdates") && astring.length >= 2) {
                        ofChunkUpdates = Integer.parseInt(astring[1]);
                        ofChunkUpdates = Config.limit(ofChunkUpdates, 1, 5);
                    }

                    if (astring[0].equals("ofChunkUpdatesDynamic") && astring.length >= 2) {
                        ofChunkUpdatesDynamic = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofTime") && astring.length >= 2) {
                        ofTime = Integer.parseInt(astring[1]);
                        ofTime = Config.limit(ofTime, 0, 2);
                    }

                    if (astring[0].equals("ofClearWater") && astring.length >= 2) {
                        ofClearWater = Boolean.parseBoolean(astring[1]);
                        updateWaterOpacity();
                    }

                    if (astring[0].equals("ofAaLevel") && astring.length >= 2) {
                        ofAaLevel = Integer.parseInt(astring[1]);
                        ofAaLevel = Config.limit(ofAaLevel, 0, 16);
                    }

                    if (astring[0].equals("ofAfLevel") && astring.length >= 2) {
                        ofAfLevel = Integer.parseInt(astring[1]);
                        ofAfLevel = Config.limit(ofAfLevel, 1, 16);
                    }

                    if (astring[0].equals("ofProfiler") && astring.length >= 2) {
                        ofProfiler = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofBetterSnow") && astring.length >= 2) {
                        ofBetterSnow = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofSwampColors") && astring.length >= 2) {
                        ofSwampColors = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofRandomEntities") && astring.length >= 2) {
                        ofRandomEntities = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofSmoothBiomes") && astring.length >= 2) {
                        ofSmoothBiomes = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofCustomFonts") && astring.length >= 2) {
                        ofCustomFonts = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofCustomColors") && astring.length >= 2) {
                        ofCustomColors = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofCustomItems") && astring.length >= 2) {
                        ofCustomItems = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofCustomSky") && astring.length >= 2) {
                        ofCustomSky = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofShowCapes") && astring.length >= 2) {
                        ofShowCapes = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofNaturalTextures") && astring.length >= 2) {
                        ofNaturalTextures = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofEmissiveTextures") && astring.length >= 2) {
                        ofEmissiveTextures = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofLazyChunkLoading") && astring.length >= 2) {
                        ofLazyChunkLoading = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofRenderRegions") && astring.length >= 2) {
                        ofRenderRegions = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofSmartAnimations") && astring.length >= 2) {
                        ofSmartAnimations = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofDynamicFov") && astring.length >= 2) {
                        ofDynamicFov = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofAlternateBlocks") && astring.length >= 2) {
                        ofAlternateBlocks = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofDynamicLights") && astring.length >= 2) {
                        ofDynamicLights = Integer.parseInt(astring[1]);
                        ofDynamicLights = limit(ofDynamicLights, OF_DYNAMIC_LIGHTS);
                    }

                    if (astring[0].equals("ofScreenshotSize") && astring.length >= 2) {
                        ofScreenshotSize = Integer.parseInt(astring[1]);
                        ofScreenshotSize = Config.limit(ofScreenshotSize, 1, 4);
                    }

                    if (astring[0].equals("ofCustomEntityModels") && astring.length >= 2) {
                        ofCustomEntityModels = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofCustomGuis") && astring.length >= 2) {
                        ofCustomGuis = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofShowGlErrors") && astring.length >= 2) {
                        ofShowGlErrors = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofFullscreenMode") && astring.length >= 2) {
                        ofFullscreenMode = astring[1];
                    }

                    if (astring[0].equals("ofFastMath") && astring.length >= 2) {
                        ofFastMath = Boolean.parseBoolean(astring[1]);
                        MathHelper.fastMath = ofFastMath;
                    }

                    if (astring[0].equals("ofFastRender") && astring.length >= 2) {
                        ofFastRender = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofTranslucentBlocks") && astring.length >= 2) {
                        ofTranslucentBlocks = Integer.parseInt(astring[1]);
                        ofTranslucentBlocks = Config.limit(ofTranslucentBlocks, 0, 2);
                    }

                    if (astring[0].equals("key_" + ofKeyBindZoom.getKeyDescription())) {
                        ofKeyBindZoom.setKeyCode(Integer.parseInt(astring[1]));
                    }
                } catch (Exception exception) {
                    Config.dbg("Skipping bad option: " + s);
                    exception.printStackTrace();
                }
            }

            KeyUtils.fixKeyConflicts(keyBindings, new KeyBinding[]{ofKeyBindZoom});
            KeyBinding.resetKeyBindingArrayAndHash();
            bufferedreader.close();
        } catch (Exception exception1) {
            Config.warn("Failed to load options");
            exception1.printStackTrace();
        }
    }

    public void saveOfOptions() {
        try {
            PrintWriter printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(optionsFileOF), StandardCharsets.UTF_8));
            printwriter.println("ofFogType:" + ofFogType);
            printwriter.println("ofFogStart:" + ofFogStart);
            printwriter.println("ofMipmapType:" + ofMipmapType);
            printwriter.println("ofOcclusionFancy:" + ofOcclusionFancy);
            printwriter.println("ofSmoothFps:" + ofSmoothFps);
            printwriter.println("ofSmoothWorld:" + ofSmoothWorld);
            printwriter.println("ofAoLevel:" + ofAoLevel);
            printwriter.println("ofClouds:" + ofClouds);
            printwriter.println("ofCloudsHeight:" + ofCloudsHeight);
            printwriter.println("ofTrees:" + ofTrees);
            printwriter.println("ofDroppedItems:" + ofDroppedItems);
            printwriter.println("ofRain:" + ofRain);
            printwriter.println("ofAnimatedWater:" + ofAnimatedWater);
            printwriter.println("ofAnimatedLava:" + ofAnimatedLava);
            printwriter.println("ofAnimatedFire:" + ofAnimatedFire);
            printwriter.println("ofAnimatedPortal:" + ofAnimatedPortal);
            printwriter.println("ofAnimatedRedstone:" + ofAnimatedRedstone);
            printwriter.println("ofAnimatedExplosion:" + ofAnimatedExplosion);
            printwriter.println("ofAnimatedFlame:" + ofAnimatedFlame);
            printwriter.println("ofAnimatedSmoke:" + ofAnimatedSmoke);
            printwriter.println("ofVoidParticles:" + ofVoidParticles);
            printwriter.println("ofWaterParticles:" + ofWaterParticles);
            printwriter.println("ofPortalParticles:" + ofPortalParticles);
            printwriter.println("ofPotionParticles:" + ofPotionParticles);
            printwriter.println("ofFireworkParticles:" + ofFireworkParticles);
            printwriter.println("ofDrippingWaterLava:" + ofDrippingWaterLava);
            printwriter.println("ofAnimatedTerrain:" + ofAnimatedTerrain);
            printwriter.println("ofAnimatedTextures:" + ofAnimatedTextures);
            printwriter.println("ofRainSplash:" + ofRainSplash);
            printwriter.println("ofLagometer:" + ofLagometer);
            printwriter.println("ofShowFps:" + ofShowFps);
            printwriter.println("ofAutoSaveTicks:" + ofAutoSaveTicks);
            printwriter.println("ofBetterGrass:" + ofBetterGrass);
            printwriter.println("ofConnectedTextures:" + ofConnectedTextures);
            printwriter.println("ofWeather:" + ofWeather);
            printwriter.println("ofSky:" + ofSky);
            printwriter.println("ofStars:" + ofStars);
            printwriter.println("ofSunMoon:" + ofSunMoon);
            printwriter.println("ofVignette:" + ofVignette);
            printwriter.println("ofChunkUpdates:" + ofChunkUpdates);
            printwriter.println("ofChunkUpdatesDynamic:" + ofChunkUpdatesDynamic);
            printwriter.println("ofTime:" + ofTime);
            printwriter.println("ofClearWater:" + ofClearWater);
            printwriter.println("ofAaLevel:" + ofAaLevel);
            printwriter.println("ofAfLevel:" + ofAfLevel);
            printwriter.println("ofProfiler:" + ofProfiler);
            printwriter.println("ofBetterSnow:" + ofBetterSnow);
            printwriter.println("ofSwampColors:" + ofSwampColors);
            printwriter.println("ofRandomEntities:" + ofRandomEntities);
            printwriter.println("ofSmoothBiomes:" + ofSmoothBiomes);
            printwriter.println("ofCustomFonts:" + ofCustomFonts);
            printwriter.println("ofCustomColors:" + ofCustomColors);
            printwriter.println("ofCustomItems:" + ofCustomItems);
            printwriter.println("ofCustomSky:" + ofCustomSky);
            printwriter.println("ofShowCapes:" + ofShowCapes);
            printwriter.println("ofNaturalTextures:" + ofNaturalTextures);
            printwriter.println("ofEmissiveTextures:" + ofEmissiveTextures);
            printwriter.println("ofLazyChunkLoading:" + ofLazyChunkLoading);
            printwriter.println("ofRenderRegions:" + ofRenderRegions);
            printwriter.println("ofSmartAnimations:" + ofSmartAnimations);
            printwriter.println("ofDynamicFov:" + ofDynamicFov);
            printwriter.println("ofAlternateBlocks:" + ofAlternateBlocks);
            printwriter.println("ofDynamicLights:" + ofDynamicLights);
            printwriter.println("ofScreenshotSize:" + ofScreenshotSize);
            printwriter.println("ofCustomEntityModels:" + ofCustomEntityModels);
            printwriter.println("ofCustomGuis:" + ofCustomGuis);
            printwriter.println("ofShowGlErrors:" + ofShowGlErrors);
            printwriter.println("ofFullscreenMode:" + ofFullscreenMode);
            printwriter.println("ofFastMath:" + ofFastMath);
            printwriter.println("ofFastRender:" + ofFastRender);
            printwriter.println("ofTranslucentBlocks:" + ofTranslucentBlocks);
            printwriter.println("key_" + ofKeyBindZoom.getKeyDescription() + ":" + ofKeyBindZoom.getKeyCode());
            printwriter.close();
        } catch (Exception exception) {
            Config.warn("Failed to save options");
            exception.printStackTrace();
        }
    }

    private void updateRenderClouds() {
        switch (ofClouds) {
            case 1:
                clouds = 1;
                break;

            case 2:
                clouds = 2;
                break;

            case 3:
                clouds = 0;
                break;

            default:
                if (fancyGraphics) {
                    clouds = 2;
                } else {
                    clouds = 1;
                }
        }
    }

    public void resetSettings() {
        renderDistanceChunks = 8;
        viewBobbing = true;
        anaglyph = false;
        limitFramerate = (int) GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
        enableVsync = false;
        updateVSync();
        mipmapLevels = 4;
        fancyGraphics = true;
        ambientOcclusion = 2;
        clouds = 2;
        fovSetting = 70.0F;
        gammaSetting = 0.0F;
        guiScale = 0;
        particleSetting = 0;
        heldItemTooltips = true;
        useVbo = false;
        forceUnicodeFont = false;
        ofFogType = 1;
        ofFogStart = 0.8F;
        ofMipmapType = 0;
        ofOcclusionFancy = false;
        ofSmartAnimations = false;
        ofSmoothFps = false;
        Config.updateAvailableProcessors();
        ofSmoothWorld = Config.isSingleProcessor();
        ofLazyChunkLoading = false;
        ofRenderRegions = false;
        ofFastMath = false;
        ofFastRender = false;
        ofTranslucentBlocks = 0;
        ofDynamicFov = true;
        ofAlternateBlocks = true;
        ofDynamicLights = 3;
        ofScreenshotSize = 1;
        ofCustomEntityModels = true;
        ofCustomGuis = true;
        ofShowGlErrors = true;
        ofAoLevel = 1.0F;
        ofAaLevel = 0;
        ofAfLevel = 1;
        ofClouds = 0;
        ofCloudsHeight = 0.0F;
        ofTrees = 0;
        ofRain = 0;
        ofBetterGrass = 3;
        ofAutoSaveTicks = 4000;
        ofLagometer = false;
        ofShowFps = false;
        ofProfiler = false;
        ofWeather = true;
        ofSky = true;
        ofStars = true;
        ofSunMoon = true;
        ofVignette = 0;
        ofChunkUpdates = 1;
        ofChunkUpdatesDynamic = false;
        ofTime = 0;
        ofClearWater = false;
        ofBetterSnow = false;
        ofFullscreenMode = "Default";
        ofSwampColors = true;
        ofRandomEntities = true;
        ofSmoothBiomes = true;
        ofCustomFonts = true;
        ofCustomColors = true;
        ofCustomItems = true;
        ofCustomSky = true;
        ofShowCapes = true;
        ofConnectedTextures = 2;
        ofNaturalTextures = false;
        ofEmissiveTextures = true;
        ofAnimatedWater = 0;
        ofAnimatedLava = 0;
        ofAnimatedFire = true;
        ofAnimatedPortal = true;
        ofAnimatedRedstone = true;
        ofAnimatedExplosion = true;
        ofAnimatedFlame = true;
        ofAnimatedSmoke = true;
        ofVoidParticles = true;
        ofWaterParticles = true;
        ofRainSplash = true;
        ofPortalParticles = true;
        ofPotionParticles = true;
        ofFireworkParticles = true;
        ofDrippingWaterLava = true;
        ofAnimatedTerrain = true;
        ofAnimatedTextures = true;
        Shaders.setShaderPack("OFF");
        Shaders.configAntialiasingLevel = 0;
        Shaders.uninit();
        Shaders.storeConfig();
        updateWaterOpacity();
        mc.refreshResources();
        saveOptions();
    }

    public void updateVSync() {
        Display.setVSyncEnabled(enableVsync);
    }

    private void updateWaterOpacity() {
        if (Config.isIntegratedServerRunning()) {
            Config.waterOpacityChanged = true;
        }

        ClearWater.updateWaterOpacity(this, mc.theWorld);
    }

    public void setAllAnimations(boolean p_setAllAnimations_1_) {
        int i = p_setAllAnimations_1_ ? 0 : 2;
        ofAnimatedWater = i;
        ofAnimatedLava = i;
        ofAnimatedFire = p_setAllAnimations_1_;
        ofAnimatedPortal = p_setAllAnimations_1_;
        ofAnimatedRedstone = p_setAllAnimations_1_;
        ofAnimatedExplosion = p_setAllAnimations_1_;
        ofAnimatedFlame = p_setAllAnimations_1_;
        ofAnimatedSmoke = p_setAllAnimations_1_;
        ofVoidParticles = p_setAllAnimations_1_;
        ofWaterParticles = p_setAllAnimations_1_;
        ofRainSplash = p_setAllAnimations_1_;
        ofPortalParticles = p_setAllAnimations_1_;
        ofPotionParticles = p_setAllAnimations_1_;
        ofFireworkParticles = p_setAllAnimations_1_;
        particleSetting = p_setAllAnimations_1_ ? 0 : 2;
        ofDrippingWaterLava = p_setAllAnimations_1_;
        ofAnimatedTerrain = p_setAllAnimations_1_;
        ofAnimatedTextures = p_setAllAnimations_1_;
    }

    public enum Options {
        INVERT_MOUSE("options.invertMouse", false, true),
        SENSITIVITY("options.sensitivity", true, false),
        FOV("options.fov", true, false, 30.0F, 110.0F, 1.0F),
        GAMMA("options.gamma", true, false),
        SATURATION("options.saturation", true, false),
        RENDER_DISTANCE("options.renderDistance", true, false, 2.0F, 16.0F, 1.0F),
        VIEW_BOBBING("options.viewBobbing", false, true),
        ANAGLYPH("options.anaglyph", false, true),
        FRAMERATE_LIMIT("options.framerateLimit", true, false, 0.0F, 260.0F, 5.0F),
        FBO_ENABLE("options.fboEnable", false, true),
        RENDER_CLOUDS("options.renderClouds", false, false),
        GRAPHICS("options.graphics", false, false),
        AMBIENT_OCCLUSION("options.ao", false, false),
        GUI_SCALE("options.guiScale", false, false),
        PARTICLES("options.particles", false, false),
        CHAT_VISIBILITY("options.chat.visibility", false, false),
        CHAT_COLOR("options.chat.color", false, true),
        CHAT_LINKS("options.chat.links", false, true),
        CHAT_OPACITY("options.chat.opacity", true, false),
        CHAT_LINKS_PROMPT("options.chat.links.prompt", false, true),
        SNOOPER_ENABLED("options.snooper", false, true),
        USE_FULLSCREEN("options.fullscreen", false, true),
        ENABLE_VSYNC("options.vsync", false, true),
        USE_VBO("options.vbo", false, true),
        TOUCHSCREEN("options.touchscreen", false, true),
        CHAT_SCALE("options.chat.scale", true, false),
        CHAT_WIDTH("options.chat.width", true, false),
        CHAT_HEIGHT_FOCUSED("options.chat.height.focused", true, false),
        CHAT_HEIGHT_UNFOCUSED("options.chat.height.unfocused", true, false),
        MIPMAP_LEVELS("options.mipmapLevels", true, false, 0.0F, 4.0F, 1.0F),
        FORCE_UNICODE_FONT("options.forceUnicodeFont", false, true),
        BLOCK_ALTERNATIVES("options.blockAlternatives", false, true),
        REDUCED_DEBUG_INFO("options.reducedDebugInfo", false, true),
        ENTITY_SHADOWS("options.entityShadows", false, true),
        FOG_FANCY("of.options.FOG_FANCY", false, false),
        FOG_START("of.options.FOG_START", false, false),
        MIPMAP_TYPE("of.options.MIPMAP_TYPE", true, false, 0.0F, 3.0F, 1.0F),
        SMOOTH_FPS("of.options.SMOOTH_FPS", false, false),
        CLOUDS("of.options.CLOUDS", false, false),
        CLOUD_HEIGHT("of.options.CLOUD_HEIGHT", true, false),
        TREES("of.options.TREES", false, false),
        RAIN("of.options.RAIN", false, false),
        ANIMATED_WATER("of.options.ANIMATED_WATER", false, false),
        ANIMATED_LAVA("of.options.ANIMATED_LAVA", false, false),
        ANIMATED_FIRE("of.options.ANIMATED_FIRE", false, false),
        ANIMATED_PORTAL("of.options.ANIMATED_PORTAL", false, false),
        AO_LEVEL("of.options.AO_LEVEL", true, false),
        LAGOMETER("of.options.LAGOMETER", false, false),
        SHOW_FPS("of.options.SHOW_FPS", false, false),
        AUTOSAVE_TICKS("of.options.AUTOSAVE_TICKS", false, false),
        BETTER_GRASS("of.options.BETTER_GRASS", false, false),
        ANIMATED_REDSTONE("of.options.ANIMATED_REDSTONE", false, false),
        ANIMATED_EXPLOSION("of.options.ANIMATED_EXPLOSION", false, false),
        ANIMATED_FLAME("of.options.ANIMATED_FLAME", false, false),
        ANIMATED_SMOKE("of.options.ANIMATED_SMOKE", false, false),
        WEATHER("of.options.WEATHER", false, false),
        SKY("of.options.SKY", false, false),
        STARS("of.options.STARS", false, false),
        SUN_MOON("of.options.SUN_MOON", false, false),
        VIGNETTE("of.options.VIGNETTE", false, false),
        CHUNK_UPDATES("of.options.CHUNK_UPDATES", false, false),
        CHUNK_UPDATES_DYNAMIC("of.options.CHUNK_UPDATES_DYNAMIC", false, false),
        TIME("of.options.TIME", false, false),
        CLEAR_WATER("of.options.CLEAR_WATER", false, false),
        SMOOTH_WORLD("of.options.SMOOTH_WORLD", false, false),
        VOID_PARTICLES("of.options.VOID_PARTICLES", false, false),
        WATER_PARTICLES("of.options.WATER_PARTICLES", false, false),
        RAIN_SPLASH("of.options.RAIN_SPLASH", false, false),
        PORTAL_PARTICLES("of.options.PORTAL_PARTICLES", false, false),
        POTION_PARTICLES("of.options.POTION_PARTICLES", false, false),
        FIREWORK_PARTICLES("of.options.FIREWORK_PARTICLES", false, false),
        PROFILER("of.options.PROFILER", false, false),
        DRIPPING_WATER_LAVA("of.options.DRIPPING_WATER_LAVA", false, false),
        BETTER_SNOW("of.options.BETTER_SNOW", false, false),
        FULLSCREEN_MODE("of.options.FULLSCREEN_MODE", true, false, 0.0F, (float) Config.getDisplayModes().length, 1.0F),
        ANIMATED_TERRAIN("of.options.ANIMATED_TERRAIN", false, false),
        SWAMP_COLORS("of.options.SWAMP_COLORS", false, false),
        RANDOM_ENTITIES("of.options.RANDOM_ENTITIES", false, false),
        SMOOTH_BIOMES("of.options.SMOOTH_BIOMES", false, false),
        CUSTOM_FONTS("of.options.CUSTOM_FONTS", false, false),
        CUSTOM_COLORS("of.options.CUSTOM_COLORS", false, false),
        SHOW_CAPES("of.options.SHOW_CAPES", false, false),
        CONNECTED_TEXTURES("of.options.CONNECTED_TEXTURES", false, false),
        CUSTOM_ITEMS("of.options.CUSTOM_ITEMS", false, false),
        AA_LEVEL("of.options.AA_LEVEL", true, false, 0.0F, 16.0F, 1.0F),
        AF_LEVEL("of.options.AF_LEVEL", true, false, 1.0F, 16.0F, 1.0F),
        ANIMATED_TEXTURES("of.options.ANIMATED_TEXTURES", false, false),
        NATURAL_TEXTURES("of.options.NATURAL_TEXTURES", false, false),
        EMISSIVE_TEXTURES("of.options.EMISSIVE_TEXTURES", false, false),
        HELD_ITEM_TOOLTIPS("of.options.HELD_ITEM_TOOLTIPS", false, false),
        DROPPED_ITEMS("of.options.DROPPED_ITEMS", false, false),
        LAZY_CHUNK_LOADING("of.options.LAZY_CHUNK_LOADING", false, false),
        CUSTOM_SKY("of.options.CUSTOM_SKY", false, false),
        FAST_MATH("of.options.FAST_MATH", false, false),
        FAST_RENDER("of.options.FAST_RENDER", false, false),
        TRANSLUCENT_BLOCKS("of.options.TRANSLUCENT_BLOCKS", false, false),
        DYNAMIC_FOV("of.options.DYNAMIC_FOV", false, false),
        DYNAMIC_LIGHTS("of.options.DYNAMIC_LIGHTS", false, false),
        ALTERNATE_BLOCKS("of.options.ALTERNATE_BLOCKS", false, false),
        CUSTOM_ENTITY_MODELS("of.options.CUSTOM_ENTITY_MODELS", false, false),
        ADVANCED_TOOLTIPS("of.options.ADVANCED_TOOLTIPS", false, false),
        SCREENSHOT_SIZE("of.options.SCREENSHOT_SIZE", false, false),
        CUSTOM_GUIS("of.options.CUSTOM_GUIS", false, false),
        RENDER_REGIONS("of.options.RENDER_REGIONS", false, false),
        SHOW_GL_ERRORS("of.options.SHOW_GL_ERRORS", false, false),
        SMART_ANIMATIONS("of.options.SMART_ANIMATIONS", false, false);

        private final boolean enumFloat;
        private final boolean enumBoolean;
        private final String enumString;
        private final float valueStep;
        private final float valueMin;
        private float valueMax;

        Options(String str, boolean isFloat, boolean isBoolean) {
            this(str, isFloat, isBoolean, 0.0F, 1.0F, 0.0F);
        }

        Options(String str, boolean isFloat, boolean isBoolean, float valMin, float valMax, float valStep) {
            enumString = str;
            enumFloat = isFloat;
            enumBoolean = isBoolean;
            valueMin = valMin;
            valueMax = valMax;
            valueStep = valStep;
        }

        public static GameSettings.Options getEnumOptions(int ordinal) {
            for (GameSettings.Options gamesettings$options : values()) {
                if (gamesettings$options.returnEnumOrdinal() == ordinal) {
                    return gamesettings$options;
                }
            }

            return null;
        }

        public boolean getEnumFloat() {
            return enumFloat;
        }

        public boolean getEnumBoolean() {
            return enumBoolean;
        }

        public int returnEnumOrdinal() {
            return ordinal();
        }

        public String getEnumString() {
            return enumString;
        }

        public float getValueMax() {
            return valueMax;
        }

        public void setValueMax(float value) {
            valueMax = value;
        }

        public float normalizeValue(float value) {
            return MathHelper.clamp_float((snapToStepClamp(value) - valueMin) / (valueMax - valueMin), 0.0F, 1.0F);
        }

        public float denormalizeValue(float value) {
            return snapToStepClamp(valueMin + (valueMax - valueMin) * MathHelper.clamp_float(value, 0.0F, 1.0F));
        }

        public float snapToStepClamp(float value) {
            value = snapToStep(value);
            return MathHelper.clamp_float(value, valueMin, valueMax);
        }

        private float snapToStep(float value) {
            if (valueStep > 0.0F) {
                value = valueStep * (float) Math.round(value / valueStep);
            }

            return value;
        }
    }
}
