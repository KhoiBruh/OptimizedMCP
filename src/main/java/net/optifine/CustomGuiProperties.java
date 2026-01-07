package net.optifine;

import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.src.Config;
import net.minecraft.tileentity.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.biome.BiomeGenBase;
import net.optifine.config.*;
import net.optifine.util.StrUtils;
import net.optifine.util.TextureUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CustomGuiProperties {
    private static final CustomGuiProperties.EnumVariant[] VARIANTS_HORSE = new CustomGuiProperties.EnumVariant[]{
            CustomGuiProperties.EnumVariant.HORSE, CustomGuiProperties.EnumVariant.DONKEY,
            CustomGuiProperties.EnumVariant.MULE, CustomGuiProperties.EnumVariant.LLAMA};
    private static final CustomGuiProperties.EnumVariant[] VARIANTS_DISPENSER = new CustomGuiProperties.EnumVariant[]{
            CustomGuiProperties.EnumVariant.DISPENSER, CustomGuiProperties.EnumVariant.DROPPER};
    private static final CustomGuiProperties.EnumVariant[] VARIANTS_INVALID = new CustomGuiProperties.EnumVariant[0];
    private static final EnumDyeColor[] COLORS_INVALID = new EnumDyeColor[0];
    private static final ResourceLocation ANVIL_GUI_TEXTURE = new ResourceLocation("textures/gui/container/anvil.png");
    private static final ResourceLocation BEACON_GUI_TEXTURE = new ResourceLocation(
            "textures/gui/container/beacon.png");
    private static final ResourceLocation BREWING_STAND_GUI_TEXTURE = new ResourceLocation(
            "textures/gui/container/brewing_stand.png");
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation(
            "textures/gui/container/generic_54.png");
    private static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURE = new ResourceLocation(
            "textures/gui/container/crafting_table.png");
    private static final ResourceLocation HORSE_GUI_TEXTURE = new ResourceLocation("textures/gui/container/horse.png");
    private static final ResourceLocation DISPENSER_GUI_TEXTURE = new ResourceLocation(
            "textures/gui/container/dispenser.png");
    private static final ResourceLocation ENCHANTMENT_TABLE_GUI_TEXTURE = new ResourceLocation(
            "textures/gui/container/enchanting_table.png");
    private static final ResourceLocation FURNACE_GUI_TEXTURE = new ResourceLocation(
            "textures/gui/container/furnace.png");
    private static final ResourceLocation HOPPER_GUI_TEXTURE = new ResourceLocation(
            "textures/gui/container/hopper.png");
    private static final ResourceLocation INVENTORY_GUI_TEXTURE = new ResourceLocation(
            "textures/gui/container/inventory.png");
    private static final ResourceLocation SHULKER_BOX_GUI_TEXTURE = new ResourceLocation(
            "textures/gui/container/shulker_box.png");
    private static final ResourceLocation VILLAGER_GUI_TEXTURE = new ResourceLocation(
            "textures/gui/container/villager.png");
    private final String fileName;
    private final String basePath;
    private final CustomGuiProperties.EnumContainer container;
    private final Map<ResourceLocation, ResourceLocation> textureLocations;
    private final NbtTagValue nbtName;
    private final BiomeGenBase[] biomes;
    private final RangeListInt heights;
    private final Boolean large;
    private final Boolean trapped;
    private final Boolean christmas;
    private final Boolean ender;
    private final RangeListInt levels;
    private final VillagerProfession[] professions;
    private final CustomGuiProperties.EnumVariant[] variants;
    private final EnumDyeColor[] colors;

    public CustomGuiProperties(Properties props, String path) {
        ConnectedParser connectedparser = new ConnectedParser("CustomGuis");
        fileName = connectedparser.parseName(path);
        basePath = connectedparser.parseBasePath(path);
        container = (CustomGuiProperties.EnumContainer) connectedparser.parseEnum(props.getProperty("container"),
                CustomGuiProperties.EnumContainer.values(), "container");
        textureLocations = parseTextureLocations(props, "texture", container, "textures/gui/", basePath);
        nbtName = connectedparser.parseNbtTagValue("name", props.getProperty("name"));
        biomes = connectedparser.parseBiomes(props.getProperty("biomes"));
        heights = connectedparser.parseRangeListInt(props.getProperty("heights"));
        large = connectedparser.parseBooleanObject(props.getProperty("large"));
        trapped = connectedparser.parseBooleanObject(props.getProperty("trapped"));
        christmas = connectedparser.parseBooleanObject(props.getProperty("christmas"));
        ender = connectedparser.parseBooleanObject(props.getProperty("ender"));
        levels = connectedparser.parseRangeListInt(props.getProperty("levels"));
        professions = connectedparser.parseProfessions(props.getProperty("professions"));
        CustomGuiProperties.EnumVariant[] acustomguiproperties$enumvariant = getContainerVariants(container);
        variants = (CustomGuiProperties.EnumVariant[]) connectedparser.parseEnums(props.getProperty("variants"),
                acustomguiproperties$enumvariant, "variants", VARIANTS_INVALID);
        colors = parseEnumDyeColors(props.getProperty("colors"));
    }

    private static CustomGuiProperties.EnumVariant[] getContainerVariants(CustomGuiProperties.EnumContainer cont) {
        return cont == CustomGuiProperties.EnumContainer.HORSE ? VARIANTS_HORSE
                : (cont == CustomGuiProperties.EnumContainer.DISPENSER ? VARIANTS_DISPENSER
                : new CustomGuiProperties.EnumVariant[0]);
    }

    private static EnumDyeColor[] parseEnumDyeColors(String str) {
        if (str == null) {
            return null;
        } else {
            str = str.toLowerCase();
            String[] astring = Config.tokenize(str, " ");
            EnumDyeColor[] aenumdyecolor = new EnumDyeColor[astring.length];

            for (int i = 0; i < astring.length; ++i) {
                String s = astring[i];
                EnumDyeColor enumdyecolor = parseEnumDyeColor(s);

                if (enumdyecolor == null) {
                    warn("Invalid color: " + s);
                    return COLORS_INVALID;
                }

                aenumdyecolor[i] = enumdyecolor;
            }

            return aenumdyecolor;
        }
    }

    private static EnumDyeColor parseEnumDyeColor(String str) {
        if (str != null) {
            EnumDyeColor[] aenumdyecolor = EnumDyeColor.values();

            for (EnumDyeColor enumdyecolor : aenumdyecolor) {
                if (enumdyecolor.getName().equals(str)) {
                    return enumdyecolor;
                }

                if (enumdyecolor.getUnlocalizedName().equals(str)) {
                    return enumdyecolor;
                }
            }

        }
        return null;
    }

    private static ResourceLocation parseTextureLocation(String str, String basePath) {
        if (str == null) {
            return null;
        } else {
            str = str.trim();
            String s = TextureUtils.fixResourcePath(str, basePath);

            if (!s.endsWith(".png")) {
                s = s + ".png";
            }

            return new ResourceLocation(basePath + "/" + s);
        }
    }

    private static Map<ResourceLocation, ResourceLocation> parseTextureLocations(Properties props, String property,
                                                                                 CustomGuiProperties.EnumContainer container, String pathPrefix, String basePath) {
        Map<ResourceLocation, ResourceLocation> map = new HashMap<>();
        String s = props.getProperty(property);

        if (s != null) {
            ResourceLocation resourcelocation = getGuiTextureLocation(container);
            ResourceLocation resourcelocation1 = parseTextureLocation(s, basePath);

            if (resourcelocation != null && resourcelocation1 != null) {
                map.put(resourcelocation, resourcelocation1);
            }
        }

        String s5 = property + ".";

        for (Object o : props.keySet()) {
            String s1 = (String) o;
            if (s1.startsWith(s5)) {
                String s2 = s1.substring(s5.length());
                s2 = s2.replace('\\', '/');
                s2 = StrUtils.removePrefixSuffix(s2, "/", ".png");
                String s3 = pathPrefix + s2 + ".png";
                String s4 = props.getProperty(s1);
                ResourceLocation resourcelocation2 = new ResourceLocation(s3);
                ResourceLocation resourcelocation3 = parseTextureLocation(s4, basePath);
                map.put(resourcelocation2, resourcelocation3);
            }
        }

        return map;
    }

    private static ResourceLocation getGuiTextureLocation(CustomGuiProperties.EnumContainer container) {
        if (container == null) {
            return null;
        } else {
            return switch (container) {
                case ANVIL -> ANVIL_GUI_TEXTURE;
                case BEACON -> BEACON_GUI_TEXTURE;
                case BREWING_STAND -> BREWING_STAND_GUI_TEXTURE;
                case CHEST -> CHEST_GUI_TEXTURE;
                case CRAFTING -> CRAFTING_TABLE_GUI_TEXTURE;
                case CREATIVE -> null;
                case DISPENSER -> DISPENSER_GUI_TEXTURE;
                case ENCHANTMENT -> ENCHANTMENT_TABLE_GUI_TEXTURE;
                case FURNACE -> FURNACE_GUI_TEXTURE;
                case HOPPER -> HOPPER_GUI_TEXTURE;
                case HORSE -> HORSE_GUI_TEXTURE;
                case INVENTORY -> INVENTORY_GUI_TEXTURE;
                case SHULKER_BOX -> SHULKER_BOX_GUI_TEXTURE;
                case VILLAGER -> VILLAGER_GUI_TEXTURE;
            };
        }
    }

    private static void warn(String str) {
        Config.warn("[CustomGuis] " + str);
    }

    public static String getName(GuiScreen screen) {
        IWorldNameable iworldnameable = getWorldNameable(screen);
        return iworldnameable == null ? null : iworldnameable.getDisplayName().getUnformattedText();
    }

    private static IWorldNameable getWorldNameable(GuiScreen screen) {
        return switch (screen) {
            case GuiBeacon guiBeacon -> (IWorldNameable) guiBeacon.tileBeacon;
            case GuiBrewingStand guiBrewingStand -> (IWorldNameable) guiBrewingStand.tileBrewingStand;
            case GuiChest guiChest -> (IWorldNameable) guiChest.lowerChestInventory;
            case GuiDispenser guiDispenser -> (IWorldNameable) guiDispenser.dispenserInventory;
            case GuiEnchantment guiEnchantment -> guiEnchantment.field_175380_I;
            case GuiFurnace guiFurnace -> (IWorldNameable) guiFurnace.tileFurnace;
            case GuiHopper guiHopper -> (IWorldNameable) guiHopper.hopperInventory;
            case null, default -> null;
        };
    }

    public boolean isValid(String path) {
        if (fileName != null && !fileName.isEmpty()) {
            if (basePath == null) {
                warn("No base path found: " + path);
                return false;
            } else if (container == null) {
                warn("No container found: " + path);
                return false;
            } else if (textureLocations.isEmpty()) {
                warn("No texture found: " + path);
                return false;
            } else if (professions == ConnectedParser.PROFESSIONS_INVALID) {
                warn("Invalid professions or careers: " + path);
                return false;
            } else if (variants == VARIANTS_INVALID) {
                warn("Invalid variants: " + path);
                return false;
            } else if (colors == COLORS_INVALID) {
                warn("Invalid colors: " + path);
                return false;
            } else {
                return true;
            }
        } else {
            warn("No name found: " + path);
            return false;
        }
    }

    private boolean matchesGeneral(CustomGuiProperties.EnumContainer ec, BlockPos pos, IBlockAccess blockAccess) {
        if (container != ec) {
            return false;
        } else {
            if (biomes != null) {
                BiomeGenBase biomegenbase = blockAccess.getBiomeGenForCoords(pos);

                if (!Matches.biome(biomegenbase, biomes)) {
                    return false;
                }
            }

            return heights == null || heights.isInRange(pos.getY());
        }
    }

    public boolean matchesPos(CustomGuiProperties.EnumContainer ec, BlockPos pos, IBlockAccess blockAccess,
                              GuiScreen screen) {
        if (!matchesGeneral(ec, pos, blockAccess)) {
            return false;
        } else {
            if (nbtName != null) {
                String s = getName(screen);

                if (!nbtName.matchesValue(s)) {
                    return false;
                }
            }

            return switch (ec) {
                case BEACON -> matchesBeacon(pos, blockAccess);
                case CHEST -> matchesChest(pos, blockAccess);
                case DISPENSER -> matchesDispenser(pos, blockAccess);
                default -> true;
            };
        }
    }

    private boolean matchesBeacon(BlockPos pos, IBlockAccess blockAccess) {
        TileEntity tileentity = blockAccess.getTileEntity(pos);

        if (!(tileentity instanceof TileEntityBeacon tileentitybeacon)) {
            return false;
        } else {

            if (levels != null) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                tileentitybeacon.writeToNBT(nbttagcompound);
                int i = nbttagcompound.getInteger("Levels");

                return levels.isInRange(i);
            }

            return true;
        }
    }

    private boolean matchesChest(BlockPos pos, IBlockAccess blockAccess) {
        TileEntity tileentity = blockAccess.getTileEntity(pos);

        if (tileentity instanceof TileEntityChest tileentitychest) {
            return matchesChest(tileentitychest, pos, blockAccess);
        } else if (tileentity instanceof TileEntityEnderChest tileentityenderchest) {
            return matchesEnderChest(tileentityenderchest, pos, blockAccess);
        } else {
            return false;
        }
    }

    private boolean matchesChest(TileEntityChest tec, BlockPos pos, IBlockAccess blockAccess) {
        boolean flag = tec.adjacentChestXNeg != null || tec.adjacentChestXPos != null || tec.adjacentChestZNeg != null
                || tec.adjacentChestZPos != null;
        boolean flag1 = tec.getChestType() == 1;
        boolean flag2 = CustomGuis.isChristmas;
        boolean flag3 = false;
        return matchesChest(flag, flag1, flag2, flag3);
    }

    private boolean matchesEnderChest(TileEntityEnderChest teec, BlockPos pos, IBlockAccess blockAccess) {
        return matchesChest(false, false, false, true);
    }

    private boolean matchesChest(boolean isLarge, boolean isTrapped, boolean isChristmas, boolean isEnder) {
        return (large == null || large == isLarge) && ((trapped == null || trapped == isTrapped) && ((christmas == null || christmas == isChristmas) && (ender == null || ender == isEnder)));
    }

    private boolean matchesDispenser(BlockPos pos, IBlockAccess blockAccess) {
        TileEntity tileentity = blockAccess.getTileEntity(pos);

        if (!(tileentity instanceof TileEntityDispenser tileentitydispenser)) {
            return false;
        } else {

            if (variants != null) {
                CustomGuiProperties.EnumVariant customguiproperties$enumvariant = getDispenserVariant(tileentitydispenser);

                return Config.equalsOne(customguiproperties$enumvariant, variants);
            }

            return true;
        }
    }

    private CustomGuiProperties.EnumVariant getDispenserVariant(TileEntityDispenser ted) {
        return ted instanceof TileEntityDropper ? CustomGuiProperties.EnumVariant.DROPPER
                : CustomGuiProperties.EnumVariant.DISPENSER;
    }

    public boolean matchesEntity(CustomGuiProperties.EnumContainer ec, Entity entity, IBlockAccess blockAccess) {
        if (!matchesGeneral(ec, entity.getPosition(), blockAccess)) {
            return false;
        } else {
            if (nbtName != null) {
                String s = entity.getName();

                if (!nbtName.matchesValue(s)) {
                    return false;
                }
            }

            return switch (ec) {
                case HORSE -> matchesHorse(entity, blockAccess);
                case VILLAGER -> matchesVillager(entity, blockAccess);
                default -> true;
            };
        }
    }

    private boolean matchesVillager(Entity entity, IBlockAccess blockAccess) {
        if (!(entity instanceof EntityVillager entityvillager)) {
            return false;
        } else {

            if (professions != null) {
                int i = entityvillager.getProfession();
                int j = entityvillager.careerId;

                if (j < 0) {
                    return false;
                }

                boolean flag = false;

                for (VillagerProfession villagerprofession : professions) {
                    if (villagerprofession.matches(i, j)) {
                        flag = true;
                        break;
                    }
                }

                return flag;
            }

            return true;
        }
    }

    private boolean matchesHorse(Entity entity, IBlockAccess blockAccess) {
        if (!(entity instanceof EntityHorse entityhorse)) {
            return false;
        } else {

            if (variants != null) {
                CustomGuiProperties.EnumVariant customguiproperties$enumvariant = getHorseVariant(entityhorse);

                return Config.equalsOne(customguiproperties$enumvariant, variants);
            }

            return true;
        }
    }

    private CustomGuiProperties.EnumVariant getHorseVariant(EntityHorse entity) {
        int i = entity.getHorseType();

        return switch (i) {
            case 0 -> EnumVariant.HORSE;
            case 1 -> EnumVariant.DONKEY;
            case 2 -> EnumVariant.MULE;
            default -> null;
        };
    }

    public CustomGuiProperties.EnumContainer getContainer() {
        return container;
    }

    public ResourceLocation getTextureLocation(ResourceLocation loc) {
        ResourceLocation resourcelocation = textureLocations.get(loc);
        return resourcelocation == null ? loc : resourcelocation;
    }

    public String toString() {
        return "name: " + fileName + ", container: " + container + ", textures: " + textureLocations;
    }

    public enum EnumContainer {
        ANVIL,
        BEACON,
        BREWING_STAND,
        CHEST,
        CRAFTING,
        DISPENSER,
        ENCHANTMENT,
        FURNACE,
        HOPPER,
        HORSE,
        VILLAGER,
        SHULKER_BOX,
        CREATIVE,
        INVENTORY;

        public static final CustomGuiProperties.EnumContainer[] VALUES = values();
    }

    private enum EnumVariant {
        HORSE,
        DONKEY,
        MULE,
        LLAMA,
        DISPENSER,
        DROPPER
    }
}
