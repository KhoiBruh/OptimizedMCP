package net.minecraft.block.state.pattern;

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;

public class BlockPattern {
    private final Predicate<BlockWorldState>[][][] blockMatches;
    private final int fingerLength;
    private final int thumbLength;
    private final int palmLength;

    public BlockPattern(Predicate<BlockWorldState>[][][] predicatesIn) {
        blockMatches = predicatesIn;
        fingerLength = predicatesIn.length;

        if (fingerLength > 0) {
            thumbLength = predicatesIn[0].length;

            if (thumbLength > 0) {
                palmLength = predicatesIn[0][0].length;
            } else {
                palmLength = 0;
            }
        } else {
            thumbLength = 0;
            palmLength = 0;
        }
    }

    public static LoadingCache<BlockPos, BlockWorldState> func_181627_a(World p_181627_0_, boolean p_181627_1_) {
        return CacheBuilder.newBuilder().build(new BlockPattern.CacheLoader(p_181627_0_, p_181627_1_));
    }

    protected static BlockPos translateOffset(BlockPos pos, EnumFacing finger, EnumFacing thumb, int palmOffset, int thumbOffset, int fingerOffset) {
        if (finger != thumb && finger != thumb.getOpposite()) {
            Vec3i vec3i = new Vec3i(finger.getFrontOffsetX(), finger.getFrontOffsetY(), finger.getFrontOffsetZ());
            Vec3i vec3i1 = new Vec3i(thumb.getFrontOffsetX(), thumb.getFrontOffsetY(), thumb.getFrontOffsetZ());
            Vec3i vec3i2 = vec3i.crossProduct(vec3i1);
            return pos.add(vec3i1.getX() * -thumbOffset + vec3i2.getX() * palmOffset + vec3i.getX() * fingerOffset, vec3i1.getY() * -thumbOffset + vec3i2.getY() * palmOffset + vec3i.getY() * fingerOffset, vec3i1.getZ() * -thumbOffset + vec3i2.getZ() * palmOffset + vec3i.getZ() * fingerOffset);
        } else {
            throw new IllegalArgumentException("Invalid forwards & up combination");
        }
    }

    public int getThumbLength() {
        return thumbLength;
    }

    public int getPalmLength() {
        return palmLength;
    }

    private BlockPattern.PatternHelper checkPatternAt(BlockPos pos, EnumFacing finger, EnumFacing thumb, LoadingCache<BlockPos, BlockWorldState> lcache) {
        for (int i = 0; i < palmLength; ++i) {
            for (int j = 0; j < thumbLength; ++j) {
                for (int k = 0; k < fingerLength; ++k) {
                    if (!blockMatches[k][j][i].apply(lcache.getUnchecked(translateOffset(pos, finger, thumb, i, j, k)))) {
                        return null;
                    }
                }
            }
        }

        return new BlockPattern.PatternHelper(pos, finger, thumb, lcache, palmLength, thumbLength, fingerLength);
    }

    public BlockPattern.PatternHelper match(World worldIn, BlockPos pos) {
        LoadingCache<BlockPos, BlockWorldState> loadingcache = func_181627_a(worldIn, false);
        int i = Math.max(Math.max(palmLength, thumbLength), fingerLength);

        for (BlockPos blockpos : BlockPos.getAllInBox(pos, pos.add(i - 1, i - 1, i - 1))) {
            for (EnumFacing enumfacing : EnumFacing.values()) {
                for (EnumFacing enumfacing1 : EnumFacing.values()) {
                    if (enumfacing1 != enumfacing && enumfacing1 != enumfacing.getOpposite()) {
                        BlockPattern.PatternHelper blockpattern$patternhelper = checkPatternAt(blockpos, enumfacing, enumfacing1, loadingcache);

                        if (blockpattern$patternhelper != null) {
                            return blockpattern$patternhelper;
                        }
                    }
                }
            }
        }

        return null;
    }

    static class CacheLoader extends com.google.common.cache.CacheLoader<BlockPos, BlockWorldState> {
        private final World world;
        private final boolean field_181626_b;

        public CacheLoader(World worldIn, boolean p_i46460_2_) {
            world = worldIn;
            field_181626_b = p_i46460_2_;
        }

        public BlockWorldState load(BlockPos p_load_1_) {
            return new BlockWorldState(world, p_load_1_, field_181626_b);
        }
    }

    public static class PatternHelper {
        private final BlockPos pos;
        private final EnumFacing finger;
        private final EnumFacing thumb;
        private final LoadingCache<BlockPos, BlockWorldState> lcache;
        private final int field_181120_e;
        private final int field_181121_f;
        private final int field_181122_g;

        public PatternHelper(BlockPos posIn, EnumFacing fingerIn, EnumFacing thumbIn, LoadingCache<BlockPos, BlockWorldState> lcacheIn, int p_i46378_5_, int p_i46378_6_, int p_i46378_7_) {
            pos = posIn;
            finger = fingerIn;
            thumb = thumbIn;
            lcache = lcacheIn;
            field_181120_e = p_i46378_5_;
            field_181121_f = p_i46378_6_;
            field_181122_g = p_i46378_7_;
        }

        public BlockPos getPos() {
            return pos;
        }

        public EnumFacing getFinger() {
            return finger;
        }

        public EnumFacing getThumb() {
            return thumb;
        }

        public int func_181118_d() {
            return field_181120_e;
        }

        public int func_181119_e() {
            return field_181121_f;
        }

        public BlockWorldState translateOffset(int palmOffset, int thumbOffset, int fingerOffset) {
            return lcache.getUnchecked(BlockPattern.translateOffset(pos, getFinger(), getThumb(), palmOffset, thumbOffset, fingerOffset));
        }

        public String toString() {
            return MoreObjects.toStringHelper(this).add("up", thumb).add("forwards", finger).add("frontTopLeft", pos).toString();
        }
    }
}
