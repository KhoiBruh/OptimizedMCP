package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.client.renderer.StitcherException;
import net.minecraft.util.MathHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Stitcher {
    private final int mipmapLevelStitcher;
    private final Set<Stitcher.Holder> setStitchHolders = Sets.newHashSetWithExpectedSize(256);
    private final List<Stitcher.Slot> stitchSlots = Lists.newArrayListWithCapacity(256);
    private final int maxWidth;
    private final int maxHeight;
    private final boolean forcePowerOf2;
    private final int maxTileDimension;
    private int currentWidth;
    private int currentHeight;

    public Stitcher(int maxTextureWidth, int maxTextureHeight, boolean p_i45095_3_, int p_i45095_4_, int mipmapLevel) {
        mipmapLevelStitcher = mipmapLevel;
        maxWidth = maxTextureWidth;
        maxHeight = maxTextureHeight;
        forcePowerOf2 = p_i45095_3_;
        maxTileDimension = p_i45095_4_;
    }

    private static int getMipmapDimension(int p_147969_0_, int p_147969_1_) {
        return (p_147969_0_ >> p_147969_1_) + ((p_147969_0_ & (1 << p_147969_1_) - 1) == 0 ? 0 : 1) << p_147969_1_;
    }

    public int getCurrentWidth() {
        return currentWidth;
    }

    public int getCurrentHeight() {
        return currentHeight;
    }

    public void addSprite(TextureAtlasSprite p_110934_1_) {
        Stitcher.Holder stitcher$holder = new Stitcher.Holder(p_110934_1_, mipmapLevelStitcher);

        if (maxTileDimension > 0) {
            stitcher$holder.setNewDimension(maxTileDimension);
        }

        setStitchHolders.add(stitcher$holder);
    }

    public void doStitch() {
        Stitcher.Holder[] astitcher$holder = setStitchHolders.toArray(new Holder[0]);
        Arrays.sort(astitcher$holder);

        for (Stitcher.Holder stitcher$holder : astitcher$holder) {
            if (!allocateSlot(stitcher$holder)) {
                String s = String.format("Unable to fit: %s, size: %dx%d, atlas: %dx%d, atlasMax: %dx%d - Maybe try a lower resolution resourcepack?", stitcher$holder.getAtlasSprite().getIconName(), stitcher$holder.getAtlasSprite().getIconWidth(), stitcher$holder.getAtlasSprite().getIconHeight(), currentWidth, currentHeight, maxWidth, maxHeight);
                throw new StitcherException(stitcher$holder, s);
            }
        }

        if (forcePowerOf2) {
            currentWidth = MathHelper.roundUpToPowerOfTwo(currentWidth);
            currentHeight = MathHelper.roundUpToPowerOfTwo(currentHeight);
        }
    }

    public List<TextureAtlasSprite> getStichSlots() {
        List<Stitcher.Slot> list = new ArrayList<>();

        for (Stitcher.Slot stitcher$slot : stitchSlots) {
            stitcher$slot.getAllStitchSlots(list);
        }

        List<TextureAtlasSprite> list1 = new ArrayList<>();

        for (Stitcher.Slot stitcher$slot1 : list) {
            Stitcher.Holder stitcher$holder = stitcher$slot1.getStitchHolder();
            TextureAtlasSprite textureatlassprite = stitcher$holder.getAtlasSprite();
            textureatlassprite.initSprite(currentWidth, currentHeight, stitcher$slot1.getOriginX(), stitcher$slot1.getOriginY(), stitcher$holder.isRotated());
            list1.add(textureatlassprite);
        }

        return list1;
    }

    private boolean allocateSlot(Stitcher.Holder p_94310_1_) {
        for (Slot stitchSlot : stitchSlots) {
            if (stitchSlot.addSlot(p_94310_1_)) {
                return true;
            }

            p_94310_1_.rotate();

            if (stitchSlot.addSlot(p_94310_1_)) {
                return true;
            }

            p_94310_1_.rotate();
        }

        return expandAndAllocateSlot(p_94310_1_);
    }

    private boolean expandAndAllocateSlot(Stitcher.Holder p_94311_1_) {
        int i = Math.min(p_94311_1_.getWidth(), p_94311_1_.getHeight());
        boolean flag = currentWidth == 0 && currentHeight == 0;
        boolean flag1;

        if (forcePowerOf2) {
            int j = MathHelper.roundUpToPowerOfTwo(currentWidth);
            int k = MathHelper.roundUpToPowerOfTwo(currentHeight);
            int l = MathHelper.roundUpToPowerOfTwo(currentWidth + i);
            int i1 = MathHelper.roundUpToPowerOfTwo(currentHeight + i);
            boolean flag2 = l <= maxWidth;
            boolean flag3 = i1 <= maxHeight;

            if (!flag2 && !flag3) {
                return false;
            }

            boolean flag4 = j != l;
            boolean flag5 = k != i1;

            if (flag4 ^ flag5) {
                flag1 = !flag4;
            } else {
                flag1 = flag2 && j <= k;
            }
        } else {
            boolean flag6 = currentWidth + i <= maxWidth;
            boolean flag7 = currentHeight + i <= maxHeight;

            if (!flag6 && !flag7) {
                return false;
            }

            flag1 = flag6 && (flag || currentWidth <= currentHeight);
        }

        int j1 = Math.max(p_94311_1_.getWidth(), p_94311_1_.getHeight());

        if (MathHelper.roundUpToPowerOfTwo((!flag1 ? currentHeight : currentWidth) + j1) > (!flag1 ? maxHeight : maxWidth)) {
            return false;
        } else {
            Stitcher.Slot stitcher$slot;

            if (flag1) {
                if (p_94311_1_.getWidth() > p_94311_1_.getHeight()) {
                    p_94311_1_.rotate();
                }

                if (currentHeight == 0) {
                    currentHeight = p_94311_1_.getHeight();
                }

                stitcher$slot = new Stitcher.Slot(currentWidth, 0, p_94311_1_.getWidth(), currentHeight);
                currentWidth += p_94311_1_.getWidth();
            } else {
                stitcher$slot = new Stitcher.Slot(0, currentHeight, currentWidth, p_94311_1_.getHeight());
                currentHeight += p_94311_1_.getHeight();
            }

            stitcher$slot.addSlot(p_94311_1_);
            stitchSlots.add(stitcher$slot);
            return true;
        }
    }

    public static class Holder implements Comparable<Stitcher.Holder> {
        private final TextureAtlasSprite theTexture;
        private final int width;
        private final int height;
        private final int mipmapLevelHolder;
        private boolean rotated;
        private float scaleFactor = 1.0F;

        public Holder(TextureAtlasSprite p_i45094_1_, int p_i45094_2_) {
            theTexture = p_i45094_1_;
            width = p_i45094_1_.getIconWidth();
            height = p_i45094_1_.getIconHeight();
            mipmapLevelHolder = p_i45094_2_;
            rotated = Stitcher.getMipmapDimension(height, p_i45094_2_) > Stitcher.getMipmapDimension(width, p_i45094_2_);
        }

        public TextureAtlasSprite getAtlasSprite() {
            return theTexture;
        }

        public int getWidth() {
            return rotated ? Stitcher.getMipmapDimension((int) ((float) height * scaleFactor), mipmapLevelHolder) : Stitcher.getMipmapDimension((int) ((float) width * scaleFactor), mipmapLevelHolder);
        }

        public int getHeight() {
            return rotated ? Stitcher.getMipmapDimension((int) ((float) width * scaleFactor), mipmapLevelHolder) : Stitcher.getMipmapDimension((int) ((float) height * scaleFactor), mipmapLevelHolder);
        }

        public void rotate() {
            rotated = !rotated;
        }

        public boolean isRotated() {
            return rotated;
        }

        public void setNewDimension(int p_94196_1_) {
            if (width > p_94196_1_ && height > p_94196_1_) {
                scaleFactor = (float) p_94196_1_ / (float) Math.min(width, height);
            }
        }

        public String toString() {
            return "Holder{width=" + width + ", height=" + height + '}';
        }

        public int compareTo(Stitcher.Holder p_compareTo_1_) {
            int i;

            if (getHeight() == p_compareTo_1_.getHeight()) {
                if (getWidth() == p_compareTo_1_.getWidth()) {
                    if (theTexture.getIconName() == null) {
                        return p_compareTo_1_.theTexture.getIconName() == null ? 0 : -1;
                    }

                    return theTexture.getIconName().compareTo(p_compareTo_1_.theTexture.getIconName());
                }

                i = getWidth() < p_compareTo_1_.getWidth() ? 1 : -1;
            } else {
                i = getHeight() < p_compareTo_1_.getHeight() ? 1 : -1;
            }

            return i;
        }
    }

    public static class Slot {
        private final int originX;
        private final int originY;
        private final int width;
        private final int height;
        private List<Stitcher.Slot> subSlots;
        private Stitcher.Holder holder;

        public Slot(int p_i1277_1_, int p_i1277_2_, int widthIn, int heightIn) {
            originX = p_i1277_1_;
            originY = p_i1277_2_;
            width = widthIn;
            height = heightIn;
        }

        public Stitcher.Holder getStitchHolder() {
            return holder;
        }

        public int getOriginX() {
            return originX;
        }

        public int getOriginY() {
            return originY;
        }

        public boolean addSlot(Stitcher.Holder holderIn) {
            if (holder != null) {
                return false;
            } else {
                int i = holderIn.getWidth();
                int j = holderIn.getHeight();

                if (i <= width && j <= height) {
                    if (i == width && j == height) {
                        holder = holderIn;
                        return true;
                    } else {
                        if (subSlots == null) {
                            subSlots = Lists.newArrayListWithCapacity(1);
                            subSlots.add(new Stitcher.Slot(originX, originY, i, j));
                            int k = width - i;
                            int l = height - j;

                            if (l > 0 && k > 0) {
                                int i1 = Math.max(height, k);
                                int j1 = Math.max(width, l);

                                if (i1 >= j1) {
                                    subSlots.add(new Stitcher.Slot(originX, originY + j, i, l));
                                    subSlots.add(new Stitcher.Slot(originX + i, originY, k, height));
                                } else {
                                    subSlots.add(new Stitcher.Slot(originX + i, originY, k, j));
                                    subSlots.add(new Stitcher.Slot(originX, originY + j, width, l));
                                }
                            } else if (k == 0) {
                                subSlots.add(new Stitcher.Slot(originX, originY + j, i, l));
                            } else if (l == 0) {
                                subSlots.add(new Stitcher.Slot(originX + i, originY, k, j));
                            }
                        }

                        for (Stitcher.Slot stitcher$slot : subSlots) {
                            if (stitcher$slot.addSlot(holderIn)) {
                                return true;
                            }
                        }

                        return false;
                    }
                } else {
                    return false;
                }
            }
        }

        public void getAllStitchSlots(List<Stitcher.Slot> p_94184_1_) {
            if (holder != null) {
                p_94184_1_.add(this);
            } else if (subSlots != null) {
                for (Stitcher.Slot stitcher$slot : subSlots) {
                    stitcher$slot.getAllStitchSlots(p_94184_1_);
                }
            }
        }

        public String toString() {
            return "Slot{originX=" + originX + ", originY=" + originY + ", width=" + width + ", height=" + height + ", texture=" + holder + ", subSlots=" + subSlots + '}';
        }
    }
}
