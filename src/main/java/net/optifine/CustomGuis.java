package net.optifine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.entity.Entity;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.optifine.override.PlayerControllerOF;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.ResUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

public class CustomGuis {
    public static final boolean isChristmas = isChristmas();
    private static final Minecraft mc = Config.getMinecraft();
    private static final PlayerControllerOF playerControllerOF = null;
    private static CustomGuiProperties[][] guiProperties = null;

    public static ResourceLocation getTextureLocation(ResourceLocation loc) {
        if (guiProperties == null) {
            return loc;
        } else {
            GuiScreen guiscreen = mc.currentScreen;

            if (!(guiscreen instanceof GuiContainer)) {
                return loc;
            } else if (loc.getResourceDomain().equals("minecraft") && loc.getResourcePath().startsWith("textures/gui/")) {
                return loc;
            } else {
                return loc;
            }
        }
    }

    private static ResourceLocation getTexturePos(CustomGuiProperties.Container container, BlockPos pos, IBlockAccess blockAccess, ResourceLocation loc, GuiScreen screen) {
        CustomGuiProperties[] acustomguiproperties = guiProperties[container.ordinal()];

        if (acustomguiproperties != null) {
            for (CustomGuiProperties customguiproperties : acustomguiproperties) {
                if (customguiproperties.matchesPos(container, pos, blockAccess, screen)) {
                    return customguiproperties.getTextureLocation(loc);
                }
            }

        }
        return loc;
    }

    private static ResourceLocation getTextureEntity(CustomGuiProperties.Container container, Entity entity, IBlockAccess blockAccess, ResourceLocation loc) {
        CustomGuiProperties[] acustomguiproperties = guiProperties[container.ordinal()];

        if (acustomguiproperties != null) {
            for (CustomGuiProperties customguiproperties : acustomguiproperties) {
                if (customguiproperties.matchesEntity(container, entity, blockAccess)) {
                    return customguiproperties.getTextureLocation(loc);
                }
            }

        }
        return loc;
    }

    public static void update() {
        guiProperties = null;

        if (Config.isCustomGuis()) {
            List<List<CustomGuiProperties>> list = new ArrayList<>();
            IResourcePack[] airesourcepack = Config.getResourcePacks();

            for (int i = airesourcepack.length - 1; i >= 0; --i) {
                IResourcePack iresourcepack = airesourcepack[i];
                update(iresourcepack, list);
            }

            guiProperties = propertyListToArray(list);
        }
    }

    private static CustomGuiProperties[][] propertyListToArray(List<List<CustomGuiProperties>> listProps) {
        if (listProps.isEmpty()) {
            return null;
        } else {
            CustomGuiProperties[][] acustomguiproperties = new CustomGuiProperties[CustomGuiProperties.Container.VALUES.length][];

            for (int i = 0; i < acustomguiproperties.length; ++i) {
                if (listProps.size() > i) {
                    List<CustomGuiProperties> list = listProps.get(i);

                    if (list != null) {
                        CustomGuiProperties[] acustomguiproperties1 = list.toArray(new CustomGuiProperties[0]);
                        acustomguiproperties[i] = acustomguiproperties1;
                    }
                }
            }

            return acustomguiproperties;
        }
    }

    private static void update(IResourcePack rp, List<List<CustomGuiProperties>> listProps) {
        String[] astring = ResUtils.collectFiles(rp, "optifine/gui/container/", ".properties", null);
        Arrays.sort(astring);

        for (String s : astring) {
            Config.dbg("CustomGuis: " + s);

            try {
                ResourceLocation resourcelocation = new ResourceLocation(s);
                InputStream inputstream = rp.getInputStream(resourcelocation);

                if (inputstream == null) {
                    Config.warn("CustomGuis file not found: " + s);
                } else {
                    Properties properties = new PropertiesOrdered();
                    properties.load(inputstream);
                    inputstream.close();
                    CustomGuiProperties customguiproperties = new CustomGuiProperties(properties, s);

                    if (customguiproperties.isValid(s)) {
                        addToList(customguiproperties, listProps);
                    }
                }
            } catch (FileNotFoundException var9) {
                Config.warn("CustomGuis file not found: " + s);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    private static void addToList(CustomGuiProperties cgp, List<List<CustomGuiProperties>> listProps) {
        if (cgp.getContainer() == null) {
            warn("Invalid container: " + cgp.getContainer());
        } else {
            int i = cgp.getContainer().ordinal();

            while (listProps.size() <= i) {
                listProps.add(null);
            }

            List<CustomGuiProperties> list = listProps.get(i);

            if (list == null) {
                list = new ArrayList<>();
                listProps.set(i, list);
            }

            list.add(cgp);
        }
    }

    private static boolean isChristmas() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) >= 24 && calendar.get(Calendar.DATE) <= 26;
    }

    private static void warn(String str) {
        Config.warn("[CustomGuis] " + str);
    }
}
