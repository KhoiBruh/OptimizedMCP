package net.minecraft.tileentity;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockFlower;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

import java.util.List;

public class TileEntityBanner extends TileEntity {
    private int baseColor;
    private NBTTagList patterns;
    private boolean field_175119_g;
    private List<TileEntityBanner.EnumBannerPattern> patternList;
    private List<EnumDyeColor> colorList;
    private String patternResourceLocation;

    public static void setBaseColorAndPatterns(NBTTagCompound compound, int baseColorIn, NBTTagList patternsIn) {
        compound.setInteger("Base", baseColorIn);

        if (patternsIn != null) {
            compound.setTag("Patterns", patternsIn);
        }
    }

    public static int getBaseColor(ItemStack stack) {
        NBTTagCompound nbttagcompound = stack.getSubCompound("BlockEntityTag", false);
        return nbttagcompound != null && nbttagcompound.hasKey("Base") ? nbttagcompound.getInteger("Base") : stack.getMetadata();
    }

    public static int getPatterns(ItemStack stack) {
        NBTTagCompound nbttagcompound = stack.getSubCompound("BlockEntityTag", false);
        return nbttagcompound != null && nbttagcompound.hasKey("Patterns") ? nbttagcompound.getTagList("Patterns", 10).tagCount() : 0;
    }

    public static void removeBannerData(ItemStack stack) {
        NBTTagCompound nbttagcompound = stack.getSubCompound("BlockEntityTag", false);

        if (nbttagcompound != null && nbttagcompound.hasKey("Patterns", 9)) {
            NBTTagList nbttaglist = nbttagcompound.getTagList("Patterns", 10);

            if (nbttaglist.tagCount() > 0) {
                nbttaglist.removeTag(nbttaglist.tagCount() - 1);

                if (nbttaglist.hasNoTags()) {
                    stack.getTagCompound().removeTag("BlockEntityTag");

                    if (stack.getTagCompound().hasNoTags()) {
                        stack.setTagCompound(null);
                    }
                }
            }
        }
    }

    public void setItemValues(ItemStack stack) {
        patterns = null;

        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("BlockEntityTag", 10)) {
            NBTTagCompound nbttagcompound = stack.getTagCompound().getCompoundTag("BlockEntityTag");

            if (nbttagcompound.hasKey("Patterns")) {
                patterns = (NBTTagList) nbttagcompound.getTagList("Patterns", 10).copy();
            }

            if (nbttagcompound.hasKey("Base", 99)) {
                baseColor = nbttagcompound.getInteger("Base");
            } else {
                baseColor = stack.getMetadata() & 15;
            }
        } else {
            baseColor = stack.getMetadata() & 15;
        }

        patternList = null;
        colorList = null;
        patternResourceLocation = "";
        field_175119_g = true;
    }

    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        setBaseColorAndPatterns(compound, baseColor, patterns);
    }

    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        baseColor = compound.getInteger("Base");
        patterns = compound.getTagList("Patterns", 10);
        patternList = null;
        colorList = null;
        patternResourceLocation = null;
        field_175119_g = true;
    }

    public Packet getDescriptionPacket() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(pos, 6, nbttagcompound);
    }

    public int getBaseColor() {
        return baseColor;
    }

    public List<TileEntityBanner.EnumBannerPattern> getPatternList() {
        initializeBannerData();
        return patternList;
    }

    public NBTTagList getPatterns() {
        return patterns;
    }

    public List<EnumDyeColor> getColorList() {
        initializeBannerData();
        return colorList;
    }

    public String getPatternResourceLocation() {
        initializeBannerData();
        return patternResourceLocation;
    }

    private void initializeBannerData() {
        if (patternList == null || colorList == null || patternResourceLocation == null) {
            if (!field_175119_g) {
                patternResourceLocation = "";
            } else {
                patternList = Lists.newArrayList();
                colorList = Lists.newArrayList();
                patternList.add(TileEntityBanner.EnumBannerPattern.BASE);
                colorList.add(EnumDyeColor.byDyeDamage(baseColor));
                patternResourceLocation = "b" + baseColor;

                if (patterns != null) {
                    for (int i = 0; i < patterns.tagCount(); ++i) {
                        NBTTagCompound nbttagcompound = patterns.getCompoundTagAt(i);
                        TileEntityBanner.EnumBannerPattern tileentitybanner$enumbannerpattern = TileEntityBanner.EnumBannerPattern.getPatternByID(nbttagcompound.getString("Pattern"));

                        if (tileentitybanner$enumbannerpattern != null) {
                            patternList.add(tileentitybanner$enumbannerpattern);
                            int j = nbttagcompound.getInteger("Color");
                            colorList.add(EnumDyeColor.byDyeDamage(j));
                            patternResourceLocation = patternResourceLocation + tileentitybanner$enumbannerpattern.getPatternID() + j;
                        }
                    }
                }
            }
        }
    }

    public enum EnumBannerPattern {
        BASE("base", "b"),
        SQUARE_BOTTOM_LEFT("square_bottom_left", "bl", "   ", "   ", "#  "),
        SQUARE_BOTTOM_RIGHT("square_bottom_right", "br", "   ", "   ", "  #"),
        SQUARE_TOP_LEFT("square_top_left", "tl", "#  ", "   ", "   "),
        SQUARE_TOP_RIGHT("square_top_right", "tr", "  #", "   ", "   "),
        STRIPE_BOTTOM("stripe_bottom", "bs", "   ", "   ", "###"),
        STRIPE_TOP("stripe_top", "ts", "###", "   ", "   "),
        STRIPE_LEFT("stripe_left", "ls", "#  ", "#  ", "#  "),
        STRIPE_RIGHT("stripe_right", "rs", "  #", "  #", "  #"),
        STRIPE_CENTER("stripe_center", "cs", " # ", " # ", " # "),
        STRIPE_MIDDLE("stripe_middle", "ms", "   ", "###", "   "),
        STRIPE_DOWNRIGHT("stripe_downright", "drs", "#  ", " # ", "  #"),
        STRIPE_DOWNLEFT("stripe_downleft", "dls", "  #", " # ", "#  "),
        STRIPE_SMALL("small_stripes", "ss", "# #", "# #", "   "),
        CROSS("cross", "cr", "# #", " # ", "# #"),
        STRAIGHT_CROSS("straight_cross", "sc", " # ", "###", " # "),
        TRIANGLE_BOTTOM("triangle_bottom", "bt", "   ", " # ", "# #"),
        TRIANGLE_TOP("triangle_top", "tt", "# #", " # ", "   "),
        TRIANGLES_BOTTOM("triangles_bottom", "bts", "   ", "# #", " # "),
        TRIANGLES_TOP("triangles_top", "tts", " # ", "# #", "   "),
        DIAGONAL_LEFT("diagonal_left", "ld", "## ", "#  ", "   "),
        DIAGONAL_RIGHT("diagonal_up_right", "rd", "   ", "  #", " ##"),
        DIAGONAL_LEFT_MIRROR("diagonal_up_left", "lud", "   ", "#  ", "## "),
        DIAGONAL_RIGHT_MIRROR("diagonal_right", "rud", " ##", "  #", "   "),
        CIRCLE_MIDDLE("circle", "mc", "   ", " # ", "   "),
        RHOMBUS_MIDDLE("rhombus", "mr", " # ", "# #", " # "),
        HALF_VERTICAL("half_vertical", "vh", "## ", "## ", "## "),
        HALF_HORIZONTAL("half_horizontal", "hh", "###", "###", "   "),
        HALF_VERTICAL_MIRROR("half_vertical_right", "vhr", " ##", " ##", " ##"),
        HALF_HORIZONTAL_MIRROR("half_horizontal_bottom", "hhb", "   ", "###", "###"),
        BORDER("border", "bo", "###", "# #", "###"),
        CURLY_BORDER("curly_border", "cbo", new ItemStack(Blocks.vine)),
        CREEPER("creeper", "cre", new ItemStack(Items.skull, 1, 4)),
        GRADIENT("gradient", "gra", "# #", " # ", " # "),
        GRADIENT_UP("gradient_up", "gru", " # ", " # ", "# #"),
        BRICKS("bricks", "bri", new ItemStack(Blocks.brick_block)),
        SKULL("skull", "sku", new ItemStack(Items.skull, 1, 1)),
        FLOWER("flower", "flo", new ItemStack(Blocks.red_flower, 1, BlockFlower.EnumFlowerType.OXEYE_DAISY.getMeta())),
        MOJANG("mojang", "moj", new ItemStack(Items.golden_apple, 1, 1));

        private final String patternName;
        private final String patternID;
        private final String[] craftingLayers;
        private ItemStack patternCraftingStack;

        EnumBannerPattern(String name, String id) {
            craftingLayers = new String[3];
            patternName = name;
            patternID = id;
        }

        EnumBannerPattern(String name, String id, ItemStack craftingItem) {
            this(name, id);
            patternCraftingStack = craftingItem;
        }

        EnumBannerPattern(String name, String id, String craftingTop, String craftingMid, String craftingBot) {
            this(name, id);
            craftingLayers[0] = craftingTop;
            craftingLayers[1] = craftingMid;
            craftingLayers[2] = craftingBot;
        }

        public static TileEntityBanner.EnumBannerPattern getPatternByID(String id) {
            for (TileEntityBanner.EnumBannerPattern tileentitybanner$enumbannerpattern : values()) {
                if (tileentitybanner$enumbannerpattern.patternID.equals(id)) {
                    return tileentitybanner$enumbannerpattern;
                }
            }

            return null;
        }

        public String getPatternName() {
            return patternName;
        }

        public String getPatternID() {
            return patternID;
        }

        public String[] getCraftingLayers() {
            return craftingLayers;
        }

        public boolean hasValidCrafting() {
            return patternCraftingStack != null || craftingLayers[0] != null;
        }

        public boolean hasCraftingStack() {
            return patternCraftingStack != null;
        }

        public ItemStack getCraftingStack() {
            return patternCraftingStack;
        }
    }
}
