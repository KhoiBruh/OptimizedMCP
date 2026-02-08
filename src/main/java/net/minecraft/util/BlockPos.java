package net.minecraft.util;

import com.google.common.collect.AbstractIterator;
import net.minecraft.entity.Entity;

public class BlockPos extends Vec3i {
    public static final BlockPos ORIGIN = new BlockPos(0, 0, 0);
    private static final int NUM_X_BITS = 1 + MathHelper.calculateLogBaseTwo(MathHelper.roundUpToPowerOfTwo(30000000));
    private static final int NUM_Z_BITS = NUM_X_BITS;
    private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;
    private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;
    private static final int Y_SHIFT = NUM_Z_BITS;
    private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;
    private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;
    private static final long X_MASK = (1L << NUM_X_BITS) - 1L;

    public BlockPos(int x, int y, int z) {
        super(x, y, z);
    }

    public BlockPos(double x, double y, double z) {
        super(x, y, z);
    }

    public BlockPos(Entity source) {
        this(source.posX, source.posY, source.posZ);
    }

    public BlockPos(Vec3 source) {
        this(source.xCoord(), source.yCoord(), source.zCoord());
    }

    public BlockPos(Vec3i source) {
        this(source.getX(), source.getY(), source.getZ());
    }

    public static BlockPos fromLong(long serialized) {
        int i = (int) (serialized << 64 - X_SHIFT - NUM_X_BITS >> 64 - NUM_X_BITS);
        int j = (int) (serialized << 64 - Y_SHIFT - NUM_Y_BITS >> 64 - NUM_Y_BITS);
        int k = (int) (serialized << 64 - NUM_Z_BITS >> 64 - NUM_Z_BITS);
        return new BlockPos(i, j, k);
    }

    public static Iterable<BlockPos> getAllInBox(BlockPos from, BlockPos to) {
        final BlockPos blockpos = new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        final BlockPos blockpos1 = new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
        return () -> new AbstractIterator<>() {
            private BlockPos lastReturned = null;

            protected BlockPos computeNext() {
                if (lastReturned == null) {
                    lastReturned = blockpos;
                    return lastReturned;
                } else if (lastReturned.equals(blockpos1)) {
                    return endOfData();
                } else {
                    int i = lastReturned.getX();
                    int j = lastReturned.getY();
                    int k = lastReturned.getZ();

                    if (i < blockpos1.getX()) {
                        ++i;
                    } else if (j < blockpos1.getY()) {
                        i = blockpos.getX();
                        ++j;
                    } else if (k < blockpos1.getZ()) {
                        i = blockpos.getX();
                        j = blockpos.getY();
                        ++k;
                    }

                    lastReturned = new BlockPos(i, j, k);
                    return lastReturned;
                }
            }
        };
    }

    public static Iterable<BlockPos.MutableBlockPos> getAllInBoxMutable(BlockPos from, BlockPos to) {
        final BlockPos blockpos = new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        final BlockPos blockpos1 = new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
        return () -> new AbstractIterator<>() {
            private MutableBlockPos theBlockPos = null;

            protected MutableBlockPos computeNext() {
                if (theBlockPos == null) {
                    theBlockPos = new MutableBlockPos(blockpos.getX(), blockpos.getY(), blockpos.getZ());
                    return theBlockPos;
                } else if (theBlockPos.equals(blockpos1)) {
                    return endOfData();
                } else {
                    int i = theBlockPos.getX();
                    int j = theBlockPos.getY();
                    int k = theBlockPos.getZ();

                    if (i < blockpos1.getX()) {
                        ++i;
                    } else if (j < blockpos1.getY()) {
                        i = blockpos.getX();
                        ++j;
                    } else if (k < blockpos1.getZ()) {
                        i = blockpos.getX();
                        j = blockpos.getY();
                        ++k;
                    }

                    theBlockPos.x = i;
                    theBlockPos.y = j;
                    theBlockPos.z = k;
                    return theBlockPos;
                }
            }
        };
    }

    public BlockPos add(double x, double y, double z) {
        return x == 0.0D && y == 0.0D && z == 0.0D ? this : new BlockPos((double) getX() + x, (double) getY() + y, (double) getZ() + z);
    }

    public BlockPos add(int x, int y, int z) {
        return x == 0 && y == 0 && z == 0 ? this : new BlockPos(getX() + x, getY() + y, getZ() + z);
    }

    public BlockPos add(Vec3i vec) {
        return vec.getX() == 0 && vec.getY() == 0 && vec.getZ() == 0 ? this : new BlockPos(getX() + vec.getX(), getY() + vec.getY(), getZ() + vec.getZ());
    }

    public BlockPos subtract(Vec3i vec) {
        return vec.getX() == 0 && vec.getY() == 0 && vec.getZ() == 0 ? this : new BlockPos(getX() - vec.getX(), getY() - vec.getY(), getZ() - vec.getZ());
    }

    public BlockPos up() {
        return up(1);
    }

    public BlockPos up(int n) {
        return offset(Direction.UP, n);
    }

    public BlockPos down() {
        return down(1);
    }

    public BlockPos down(int n) {
        return offset(Direction.DOWN, n);
    }

    public BlockPos north() {
        return north(1);
    }

    public BlockPos north(int n) {
        return offset(Direction.NORTH, n);
    }

    public BlockPos south() {
        return south(1);
    }

    public BlockPos south(int n) {
        return offset(Direction.SOUTH, n);
    }

    public BlockPos west() {
        return west(1);
    }

    public BlockPos west(int n) {
        return offset(Direction.WEST, n);
    }

    public BlockPos east() {
        return east(1);
    }

    public BlockPos east(int n) {
        return offset(Direction.EAST, n);
    }

    public BlockPos offset(Direction facing) {
        return offset(facing, 1);
    }

    public BlockPos offset(Direction facing, int n) {
        return n == 0 ? this : new BlockPos(getX() + facing.getFrontOffsetX() * n, getY() + facing.getFrontOffsetY() * n, getZ() + facing.getFrontOffsetZ() * n);
    }

    public BlockPos crossProduct(Vec3i vec) {
        return new BlockPos(getY() * vec.getZ() - getZ() * vec.getY(), getZ() * vec.getX() - getX() * vec.getZ(), getX() * vec.getY() - getY() * vec.getX());
    }

    public long toLong() {
        return ((long) getX() & X_MASK) << X_SHIFT | ((long) getY() & Y_MASK) << Y_SHIFT | ((long) getZ() & Z_MASK);
    }

    public static final class MutableBlockPos extends BlockPos {
        private int x;
        private int y;
        private int z;

        public MutableBlockPos() {
            this(0, 0, 0);
        }

        public MutableBlockPos(int x_, int y_, int z_) {
            super(0, 0, 0);
            x = x_;
            y = y_;
            z = z_;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }

        public BlockPos.MutableBlockPos set(int xIn, int yIn, int zIn) {
            x = xIn;
            y = yIn;
            z = zIn;
            return this;
        }
    }
}
