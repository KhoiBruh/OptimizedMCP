package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatFormat;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.optifine.CustomPanorama;
import net.optifine.CustomPanoramaProperties;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.Project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GuiMainMenu extends GuiScreen implements GuiYesNoCallback {
    public static final String field_96138_a = "Please click " + ChatFormat.UNDERLINE + "here" + ChatFormat.RESET + " for more information.";
//    private static final Logger logger = LogManager.getLogger();
    private static final Random RANDOM = new Random();
    private static final ResourceLocation splashTexts = new ResourceLocation("texts/splashes.txt");
    private static final ResourceLocation minecraftTitleTextures = new ResourceLocation("textures/gui/title/minecraft.png");
    private static final ResourceLocation[] titlePanoramaPaths = new ResourceLocation[]{
            new ResourceLocation("textures/gui/title/background/panorama_0.png"),
            new ResourceLocation("textures/gui/title/background/panorama_1.png"),
            new ResourceLocation("textures/gui/title/background/panorama_2.png"),
            new ResourceLocation("textures/gui/title/background/panorama_3.png"),
            new ResourceLocation("textures/gui/title/background/panorama_4.png"),
            new ResourceLocation("textures/gui/title/background/panorama_5.png")
    };
    private final Object threadLock = new Object();
    public String splashText;
    private final float updateCounter;
    private int panoramaTimer;
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

        try (var bufferedreader = new BufferedReader(new InputStreamReader(Minecraft.getMinecraft().getResourceManager().getResource(splashTexts).getInputStream(), StandardCharsets.UTF_8))) {
            List<String> list = new ArrayList<>();

            String s;

            while ((s = bufferedreader.readLine()) != null) {
                s = s.trim();
                if (!s.isEmpty()) list.add(s);
            }

            if (!list.isEmpty()) {
                do {
                    splashText = list.get(RANDOM.nextInt(list.size()));
                } while (splashText.hashCode() == 125780783);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        panoramaTimer++;
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    protected void keyTyped(char typedChar, int keyCode) {
    }

    public void initGui() {
        DynamicTexture viewportTexture = new DynamicTexture(256, 256);
        backgroundTexture = mc.getTextureManager().getDynamicTextureLocation("background", viewportTexture);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        switch (calendar.get(Calendar.DATE)) {
            case 1 -> splashText = "Happy new year!";
            case 24 -> splashText = "Merry X-mas!";
            case 31 -> splashText = "OOoooOOOoooo! Spooky!";
        }

//        if (calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) == 24) {
//
//        } else if (calendar.get(Calendar.MONTH) + 1 == 1 && calendar.get(Calendar.DATE) == 1) {
//
//        } else if (calendar.get(Calendar.MONTH) + 1 == 10 && calendar.get(Calendar.DATE) == 31) {
//
//        }

        int j = height / 4 + 48;

        buttonList.add(new GuiButton(0, width / 2 - 100, j, I18n.format("menu.singleplayer")));
        buttonList.add(new GuiButton(1, width / 2 - 100, j + 24, I18n.format("menu.multiplayer")));

        buttonList.add(new GuiButtonLanguage(2, width / 2 - 124, j + 72 + 12));
        buttonList.add(new GuiButton(3, width / 2 - 100, j + 72 + 12, 98, 20, I18n.format("menu.options")));
        buttonList.add(new GuiButton(4, width / 2 + 2, j + 72 + 12, 98, 20, I18n.format("menu.quit")));


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

    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0 -> mc.displayGuiScreen(new GuiSelectWorld(this));
            case 1 -> mc.displayGuiScreen(new GuiMultiplayer(this));
            case 2 -> mc.displayGuiScreen(new GuiLanguage(this, mc.gameSettings, mc.getLanguageManager()));
            case 3 -> mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
            case 4 -> mc.shutdown();
        }
    }

    public void confirmClicked(boolean result, int id) {
        if (id == 13) {
            if (result) Sys.openURL(openGLWarningLink);
            mc.displayGuiScreen(this);
        }
    }

    private void drawPanorama(float partialTicks) {
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

        if (custompanoramaproperties != null) j = custompanoramaproperties.getBlur1();

        for (int k = 0; k < j; k++) {
            GlStateManager.pushMatrix();
            float f = ((float) (k % i) / i - 0.5F) / 64.0F;
            float f1 = ((float) (k / i) / i - 0.5F) / 64.0F;
            float f2 = 0;
            GlStateManager.translate(f, f1, f2);
            GlStateManager.rotate(MathHelper.sin((panoramaTimer + partialTicks) / 400) * 25.0F + 20.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-(panoramaTimer + partialTicks) * 0.1F, 0.0F, 1.0F, 0.0F);

            for (int l = 0; l < 6; ++l) {
                GlStateManager.pushMatrix();

                switch (l) {
                    case 1 -> GlStateManager.rotate(90, 0, 1, 0);
                    case 2 -> GlStateManager.rotate(180, 0, 1, 0);
                    case 3 -> GlStateManager.rotate(-90, 0, 1, 0);
                    case 4 -> GlStateManager.rotate(90, 1, 0, 0);
                    case 5 -> GlStateManager.rotate(-90, 1, 0, 0);
                }

                ResourceLocation[] aresourcelocation = titlePanoramaPaths;

                if (custompanoramaproperties != null) aresourcelocation = custompanoramaproperties.getPanoramaLocations();

                mc.getTextureManager().bindTexture(aresourcelocation[l]);
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                int i1 = 255 / (k + 1);
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

        worldrenderer.setTranslation(0, 0, 0);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.matrixMode(5889);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
    }

    private void rotateAndBlurSkybox() {
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

        if (custompanoramaproperties != null) j = custompanoramaproperties.getBlur2();

        for (int k = 0; k < j; k++) {
            float f = 1F / (k + 1);
            int l = width;
            int i1 = height;
            float f1 = (k - i / 2F) / 256F;

            worldrenderer.pos(l, i1, zLevel).tex(f1, 1).color(1, 1, 1, f).endVertex();
            worldrenderer.pos(l, 0, zLevel).tex(f1 + 1, 1).color(1, 1, 1, f).endVertex();
            worldrenderer.pos(0, 0, zLevel).tex(f1 + 1, 0).color(1, 1, 1, f).endVertex();
            worldrenderer.pos(0, i1, zLevel).tex(f1, 0).color(1, 1, 1, f).endVertex();
        }

        tessellator.draw();
        GlStateManager.enableAlpha();
        GlStateManager.colorMask(true, true, true, true);
    }

    private void renderSkybox(float partialTicks) {
        mc.getFramebuffer().unbindFramebuffer();
        GlStateManager.viewport(0, 0, 256, 256);
        drawPanorama(partialTicks);
        rotateAndBlurSkybox();
        int i = 3;
        CustomPanoramaProperties custompanoramaproperties = CustomPanorama.getCustomPanoramaProperties();

        if (custompanoramaproperties != null) i = custompanoramaproperties.getBlur3();

        for (int j = 0; j < i; ++j) {
            rotateAndBlurSkybox();
            rotateAndBlurSkybox();
        }

        mc.getFramebuffer().bindFramebuffer(true);
        GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight);
        float f2 = width > height ? 120F / width : 120F / height;
        float f = height * f2 / 256;
        float f1 = width * f2 / 256;
        int k = width;
        int l = height;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(0, l, zLevel).tex(0.5F - f, 0.5F + f1).color(1F, 1F, 1F, 1F).endVertex();
        worldrenderer.pos(k, l, zLevel).tex(0.5F - f, 0.5F - f1).color(1F, 1F, 1F, 1F).endVertex();
        worldrenderer.pos(k, 0, zLevel).tex(0.5F + f, 0.5F - f1).color(1F, 1F, 1F, 1F).endVertex();
        worldrenderer.pos(0, 0, zLevel).tex(0.5F + f, 0.5F + f1).color(1F, 1F, 1F, 1F).endVertex();
        tessellator.draw();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.disableAlpha();
        renderSkybox(partialTicks);
        GlStateManager.enableAlpha();
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

        drawGradientRect(0, 0, width, height, l, i1);
        drawGradientRect(0, 0, width, height, j1, k1);

        mc.getTextureManager().bindTexture(minecraftTitleTextures);

        if (updateCounter < 1.0E-4D) {
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
        GlStateManager.translate(width / 2F + 90, 70, 0);
        GlStateManager.rotate(-20, 0, 0, 1);
        float f = 1.8F - MathHelper.abs(MathHelper.sin((float) (Minecraft.getSystemTime() % 1000L) / 1000 * (float) Math.PI * 2) * 0.1F);
        f = f * 100 / (fontRendererObj.getStringWidth(splashText) + 32);
        GlStateManager.scale(f, f, f);
        drawCenteredString(fontRendererObj, splashText, 0, -8, -256);
        GlStateManager.popMatrix();
        String s = "Minecraft 1.8.9";

        drawString(fontRendererObj, s, 2, height - 10, -1);

        String s2 = "Copyright Mojang AB. Do not distribute!";
        drawString(fontRendererObj, s2, width - fontRendererObj.getStringWidth(s2) - 2, height - 10, -1);

        if (openGLWarning1 != null && !openGLWarning1.isEmpty()) {
            drawRect(field_92022_t - 2, field_92021_u - 2, field_92020_v + 2, field_92019_w - 1, 1428160512);
            drawString(fontRendererObj, openGLWarning1, field_92022_t, field_92021_u, -1);
            drawString(fontRendererObj, openGLWarning2, (width - field_92024_r) / 2, buttonList.getFirst().yPosition - 12, -1);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        synchronized (threadLock) {
            if (!openGLWarning1.isEmpty() && mouseX >= field_92022_t && mouseX <= field_92020_v && mouseY >= field_92021_u && mouseY <= field_92019_w) {
                GuiConfirmOpenLink guiconfirmopenlink = new GuiConfirmOpenLink(this, openGLWarningLink, 13, true);
                guiconfirmopenlink.disableSecurityWarning();
                mc.displayGuiScreen(guiconfirmopenlink);
            }
        }
    }
}
