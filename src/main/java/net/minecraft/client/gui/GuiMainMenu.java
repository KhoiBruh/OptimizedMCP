package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.optifine.CustomPanorama;
import net.optifine.CustomPanoramaProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.Project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GuiMainMenu extends GuiScreen implements GuiYesNoCallback {
    public static final String field_96138_a = "Please click " + EnumChatFormatting.UNDERLINE + "here"
            + EnumChatFormatting.RESET + " for more information.";
    private static final AtomicInteger field_175373_f = new AtomicInteger(0);
    private static final Logger logger = LogManager.getLogger();
    private static final Random RANDOM = new Random();
    private static final ResourceLocation splashTexts = new ResourceLocation("texts/splashes.txt");
    private static final ResourceLocation minecraftTitleTextures = new ResourceLocation(
            "textures/gui/title/minecraft.png");
    private static final ResourceLocation[] titlePanoramaPaths = new ResourceLocation[]{
            new ResourceLocation("textures/gui/title/background/panorama_0.png"),
            new ResourceLocation("textures/gui/title/background/panorama_1.png"),
            new ResourceLocation("textures/gui/title/background/panorama_2.png"),
            new ResourceLocation("textures/gui/title/background/panorama_3.png"),
            new ResourceLocation("textures/gui/title/background/panorama_4.png"),
            new ResourceLocation("textures/gui/title/background/panorama_5.png")};
    private final Object threadLock = new Object();
    public String splashText;
    private final float updateCounter;
    private int panoramaTimer;
    private DynamicTexture viewportTexture;
    private final boolean field_175375_v = true;
    private String openGLWarning1;
    private String openGLWarning2;
    private String openGLWarningLink;
    private int field_92024_r;
    private int field_92023_s;
    private int field_92022_t;
    private int field_92021_u;
    private int field_92020_v;
    private int field_92019_w;
    private ResourceLocation backgroundTexture;

    public GuiMainMenu() {
        openGLWarning2 = field_96138_a;
        splashText = "missingno";
        BufferedReader bufferedreader = null;

        try {
            List<String> list = new ArrayList<>();
            bufferedreader = new BufferedReader(new InputStreamReader(
                    Minecraft.getMinecraft().getResourceManager().getResource(splashTexts).getInputStream(),
                    StandardCharsets.UTF_8));
            String s;

            while ((s = bufferedreader.readLine()) != null) {
                s = s.trim();

                if (!s.isEmpty()) {
                    list.add(s);
                }
            }

            if (!list.isEmpty()) {
                while (true) {
                    splashText = list.get(RANDOM.nextInt(list.size()));

                    if (splashText.hashCode() != 125780783) {
                        break;
                    }
                }
            }
        } catch (IOException var12) {
        } finally {
            if (bufferedreader != null) {
                try {
                    bufferedreader.close();
                } catch (IOException var11) {
                }
            }
        }

        updateCounter = RANDOM.nextFloat();
        openGLWarning1 = "";

        if (!GLContext.getCapabilities().OpenGL20 && !OpenGlHelper.areShadersSupported()) {
            openGLWarning1 = I18n.format("title.oldgl1");
            openGLWarning2 = I18n.format("title.oldgl2");
            openGLWarningLink = "https://help.mojang.com/customer/portal/articles/325948?ref=game";
        }
    }

    public void updateScreen() {
        ++panoramaTimer;

    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    protected void keyTyped(char typedChar, int keyCode) {
    }

    public void initGui() {
        viewportTexture = new DynamicTexture(256, 256);
        backgroundTexture = mc.getTextureManager().getDynamicTextureLocation("background",
                viewportTexture);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        if (calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) == 24) {
            splashText = "Merry X-mas!";
        } else if (calendar.get(Calendar.MONTH) + 1 == 1 && calendar.get(Calendar.DATE) == 1) {
            splashText = "Happy new year!";
        } else if (calendar.get(Calendar.MONTH) + 1 == 10 && calendar.get(Calendar.DATE) == 31) {
            splashText = "OOoooOOOoooo! Spooky!";
        }

        int i = 24;
        int j = height / 4 + 48;

        addSingleplayerMultiplayerButtons(j, 24);

        buttonList.add(new GuiButton(0, width / 2 - 100, j + 72 + 12, 98, 20,
                I18n.format("menu.options")));
        buttonList.add(
                new GuiButton(4, width / 2 + 2, j + 72 + 12, 98, 20, I18n.format("menu.quit")));
        buttonList.add(new GuiButtonLanguage(5, width / 2 - 124, j + 72 + 12));

        synchronized (threadLock) {
            field_92023_s = fontRendererObj.getStringWidth(openGLWarning1);
            field_92024_r = fontRendererObj.getStringWidth(openGLWarning2);
            int k = Math.max(field_92023_s, field_92024_r);
            field_92022_t = (width - k) / 2;
            field_92021_u = buttonList.getFirst().yPosition - 24;
            field_92020_v = field_92022_t + k;
            field_92019_w = field_92021_u + 24;
        }
    }

    private void addSingleplayerMultiplayerButtons(int p_73969_1_, int p_73969_2_) {
        buttonList.add(
                new GuiButton(1, width / 2 - 100, p_73969_1_, I18n.format("menu.singleplayer")));
        buttonList.add(new GuiButton(2, width / 2 - 100, p_73969_1_ + p_73969_2_,
                I18n.format("menu.multiplayer")));
    }

    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
        }

        if (button.id == 5) {
            mc.displayGuiScreen(new GuiLanguage(this, mc.gameSettings, mc.getLanguageManager()));
        }

        if (button.id == 1) {
            mc.displayGuiScreen(new GuiSelectWorld(this));
        }

        if (button.id == 2) {
            mc.displayGuiScreen(new GuiMultiplayer(this));
        }

        if (button.id == 4) {
            mc.shutdown();
        }
    }

    public void confirmClicked(boolean result, int id) {
        if (id == 13) {
            if (result) {
                try {
                    Class<?> oclass = Class.forName("java.awt.Desktop");
                    Object object = oclass.getMethod("getDesktop", new Class[0]).invoke(null);
                    oclass.getMethod("browse", new Class[]{URI.class}).invoke(object,
                            new URI(openGLWarningLink));
                } catch (Throwable throwable) {
                    logger.error("Couldn't open link", throwable);
                }
            }

            mc.displayGuiScreen(this);
        }
    }

    private void drawPanorama(int p_73970_1_, int p_73970_2_, float p_73970_3_) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.matrixMode(5889);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        Project.gluPerspective(120.0F, 1.0F, 0.05F, 10.0F);
        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        int i = 8;
        int j = 64;
        CustomPanoramaProperties custompanoramaproperties = CustomPanorama.getCustomPanoramaProperties();

        if (custompanoramaproperties != null) {
            j = custompanoramaproperties.getBlur1();
        }

        for (int k = 0; k < j; ++k) {
            GlStateManager.pushMatrix();
            float f = ((float) (k % i) / (float) i - 0.5F) / 64.0F;
            float f1 = ((float) (k / i) / (float) i - 0.5F) / 64.0F;
            float f2 = 0.0F;
            GlStateManager.translate(f, f1, f2);
            GlStateManager.rotate(MathHelper.sin(((float) panoramaTimer + p_73970_3_) / 400.0F) * 25.0F + 20.0F,
                    1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-((float) panoramaTimer + p_73970_3_) * 0.1F, 0.0F, 1.0F, 0.0F);

            for (int l = 0; l < 6; ++l) {
                GlStateManager.pushMatrix();

                if (l == 1) {
                    GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                }

                if (l == 2) {
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                }

                if (l == 3) {
                    GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                }

                if (l == 4) {
                    GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                }

                if (l == 5) {
                    GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
                }

                ResourceLocation[] aresourcelocation = titlePanoramaPaths;

                if (custompanoramaproperties != null) {
                    aresourcelocation = custompanoramaproperties.getPanoramaLocations();
                }

                mc.getTextureManager().bindTexture(aresourcelocation[l]);
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                int i1 = 255 / (k + 1);
                float f3 = 0.0F;
                worldrenderer.pos(-1.0D, -1.0D, 1.0D).tex(0.0D, 0.0D).color(255, 255, 255, i1).endVertex();
                worldrenderer.pos(1.0D, -1.0D, 1.0D).tex(1.0D, 0.0D).color(255, 255, 255, i1).endVertex();
                worldrenderer.pos(1.0D, 1.0D, 1.0D).tex(1.0D, 1.0D).color(255, 255, 255, i1).endVertex();
                worldrenderer.pos(-1.0D, 1.0D, 1.0D).tex(0.0D, 1.0D).color(255, 255, 255, i1).endVertex();
                tessellator.draw();
                GlStateManager.popMatrix();
            }

            GlStateManager.popMatrix();
            GlStateManager.colorMask(true, true, true, false);
        }

        worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.matrixMode(5889);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
    }

    private void rotateAndBlurSkybox(float p_73968_1_) {
        mc.getTextureManager().bindTexture(backgroundTexture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, 256, 256);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.colorMask(true, true, true, false);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        GlStateManager.disableAlpha();
        int i = 3;
        int j = 3;
        CustomPanoramaProperties custompanoramaproperties = CustomPanorama.getCustomPanoramaProperties();

        if (custompanoramaproperties != null) {
            j = custompanoramaproperties.getBlur2();
        }

        for (int k = 0; k < j; ++k) {
            float f = 1.0F / (float) (k + 1);
            int l = width;
            int i1 = height;
            float f1 = (float) (k - i / 2) / 256.0F;
            worldrenderer.pos(l, i1, zLevel).tex(0.0F + f1, 1.0D)
                    .color(1.0F, 1.0F, 1.0F, f).endVertex();
            worldrenderer.pos(l, 0.0D, zLevel).tex(1.0F + f1, 1.0D)
                    .color(1.0F, 1.0F, 1.0F, f).endVertex();
            worldrenderer.pos(0.0D, 0.0D, zLevel).tex(1.0F + f1, 0.0D)
                    .color(1.0F, 1.0F, 1.0F, f).endVertex();
            worldrenderer.pos(0.0D, i1, zLevel).tex(0.0F + f1, 0.0D)
                    .color(1.0F, 1.0F, 1.0F, f).endVertex();
        }

        tessellator.draw();
        GlStateManager.enableAlpha();
        GlStateManager.colorMask(true, true, true, true);
    }

    private void renderSkybox(int p_73971_1_, int p_73971_2_, float p_73971_3_) {
        mc.getFramebuffer().unbindFramebuffer();
        GlStateManager.viewport(0, 0, 256, 256);
        drawPanorama(p_73971_1_, p_73971_2_, p_73971_3_);
        rotateAndBlurSkybox(p_73971_3_);
        int i = 3;
        CustomPanoramaProperties custompanoramaproperties = CustomPanorama.getCustomPanoramaProperties();

        if (custompanoramaproperties != null) {
            i = custompanoramaproperties.getBlur3();
        }

        for (int j = 0; j < i; ++j) {
            rotateAndBlurSkybox(p_73971_3_);
            rotateAndBlurSkybox(p_73971_3_);
        }

        mc.getFramebuffer().bindFramebuffer(true);
        GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight);
        float f2 = width > height ? 120.0F / (float) width : 120.0F / (float) height;
        float f = (float) height * f2 / 256.0F;
        float f1 = (float) width * f2 / 256.0F;
        int k = width;
        int l = height;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(0.0D, l, zLevel).tex(0.5F - f, 0.5F + f1)
                .color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        worldrenderer.pos(k, l, zLevel).tex(0.5F - f, 0.5F - f1)
                .color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        worldrenderer.pos(k, 0.0D, zLevel).tex(0.5F + f, 0.5F - f1)
                .color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        worldrenderer.pos(0.0D, 0.0D, zLevel).tex(0.5F + f, 0.5F + f1)
                .color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        tessellator.draw();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.disableAlpha();
        renderSkybox(mouseX, mouseY, partialTicks);
        GlStateManager.enableAlpha();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        int i = 274;
        int j = width / 2 - i / 2;
        int k = 30;
        int l = -2130706433;
        int i1 = 16777215;
        int j1 = 0;
        int k1 = Integer.MIN_VALUE;
        CustomPanoramaProperties custompanoramaproperties = CustomPanorama.getCustomPanoramaProperties();

        if (custompanoramaproperties != null) {
            l = custompanoramaproperties.getOverlay1Top();
            i1 = custompanoramaproperties.getOverlay1Bottom();
            j1 = custompanoramaproperties.getOverlay2Top();
            k1 = custompanoramaproperties.getOverlay2Bottom();
        }

        if (l != 0 || i1 != 0) {
            drawGradientRect(0, 0, width, height, l, i1);
        }

        if (j1 != 0 || k1 != 0) {
            drawGradientRect(0, 0, width, height, j1, k1);
        }

        mc.getTextureManager().bindTexture(minecraftTitleTextures);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        if ((double) updateCounter < 1.0E-4D) {
            drawTexturedModalRect(j, k, 0, 0, 99, 44);
            drawTexturedModalRect(j + 99, k, 129, 0, 27, 44);
            drawTexturedModalRect(j + 99 + 26, k, 126, 0, 3, 44);
            drawTexturedModalRect(j + 99 + 26 + 3, k, 99, 0, 26, 44);
            drawTexturedModalRect(j + 155, k, 0, 45, 155, 44);
        } else {
            drawTexturedModalRect(j, k, 0, 0, 155, 44);
            drawTexturedModalRect(j + 155, k, 0, 45, 155, 44);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) (width / 2 + 90), 70.0F, 0.0F);
        GlStateManager.rotate(-20.0F, 0.0F, 0.0F, 1.0F);
        float f = 1.8F - MathHelper.abs(
                MathHelper.sin((float) (Minecraft.getSystemTime() % 1000L) / 1000.0F * (float) Math.PI * 2.0F) * 0.1F);
        f = f * 100.0F / (float) (fontRendererObj.getStringWidth(splashText) + 32);
        GlStateManager.scale(f, f, f);
        drawCenteredString(fontRendererObj, splashText, 0, -8, -256);
        GlStateManager.popMatrix();
        String s = "Minecraft 1.8.9";

        drawString(fontRendererObj, s, 2, height - 10, -1);

        String s2 = "Copyright Mojang AB. Do not distribute!";
        drawString(fontRendererObj, s2, width - fontRendererObj.getStringWidth(s2) - 2,
                height - 10, -1);

        if (openGLWarning1 != null && !openGLWarning1.isEmpty()) {
            drawRect(field_92022_t - 2, field_92021_u - 2, field_92020_v + 2, field_92019_w - 1,
                    1428160512);
            drawString(fontRendererObj, openGLWarning1, field_92022_t, field_92021_u, -1);
            drawString(fontRendererObj, openGLWarning2, (width - field_92024_r) / 2,
                    buttonList.getFirst().yPosition - 12, -1);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        synchronized (threadLock) {
            if (!openGLWarning1.isEmpty() && mouseX >= field_92022_t && mouseX <= field_92020_v
                    && mouseY >= field_92021_u && mouseY <= field_92019_w) {
                GuiConfirmOpenLink guiconfirmopenlink = new GuiConfirmOpenLink(this, openGLWarningLink, 13, true);
                guiconfirmopenlink.disableSecurityWarning();
                mc.displayGuiScreen(guiconfirmopenlink);
            }
        }
    }
}
