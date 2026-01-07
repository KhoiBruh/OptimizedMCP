package net.optifine.shaders;

import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.optifine.BlockPosM;

import java.util.Iterator;

public class Iterator3d implements Iterator<BlockPos> {
    private final IteratorAxis iteratorAxis;
    private final BlockPosM blockPos = new BlockPosM(0, 0, 0);
    private int axis = 0;
    private final int kX;
    private final int kY;
    private final int kZ;

    public Iterator3d(BlockPos posStart, BlockPos posEnd, int width, int height) {
        boolean flag = posStart.getX() > posEnd.getX();
        boolean flag1 = posStart.getY() > posEnd.getY();
        boolean flag2 = posStart.getZ() > posEnd.getZ();
        posStart = reverseCoord(posStart, flag, flag1, flag2);
        posEnd = reverseCoord(posEnd, flag, flag1, flag2);
        kX = flag ? -1 : 1;
        kY = flag1 ? -1 : 1;
        kZ = flag2 ? -1 : 1;
        Vec3 vec3 = new Vec3(posEnd.getX() - posStart.getX(), posEnd.getY() - posStart.getY(), posEnd.getZ() - posStart.getZ());
        Vec3 vec31 = vec3.normalize();
        Vec3 vec32 = new Vec3(1.0D, 0.0D, 0.0D);
        double d0 = vec31.dotProduct(vec32);
        double d1 = Math.abs(d0);
        Vec3 vec33 = new Vec3(0.0D, 1.0D, 0.0D);
        double d2 = vec31.dotProduct(vec33);
        double d3 = Math.abs(d2);
        Vec3 vec34 = new Vec3(0.0D, 0.0D, 1.0D);
        double d4 = vec31.dotProduct(vec34);
        double d5 = Math.abs(d4);

        if (d5 >= d3 && d5 >= d1) {
            axis = 2;
            BlockPos blockpos3 = new BlockPos(posStart.getZ(), posStart.getY() - width, posStart.getX() - height);
            BlockPos blockpos5 = new BlockPos(posEnd.getZ(), posStart.getY() + width + 1, posStart.getX() + height + 1);
            int k = posEnd.getZ() - posStart.getZ();
            double d9 = (double) (posEnd.getY() - posStart.getY()) / ((double) k);
            double d11 = (double) (posEnd.getX() - posStart.getX()) / ((double) k);
            iteratorAxis = new IteratorAxis(blockpos3, blockpos5, d9, d11);
        } else if (d3 >= d1 && d3 >= d5) {
            axis = 1;
            BlockPos blockpos2 = new BlockPos(posStart.getY(), posStart.getX() - width, posStart.getZ() - height);
            BlockPos blockpos4 = new BlockPos(posEnd.getY(), posStart.getX() + width + 1, posStart.getZ() + height + 1);
            int j = posEnd.getY() - posStart.getY();
            double d8 = (double) (posEnd.getX() - posStart.getX()) / ((double) j);
            double d10 = (double) (posEnd.getZ() - posStart.getZ()) / ((double) j);
            iteratorAxis = new IteratorAxis(blockpos2, blockpos4, d8, d10);
        } else {
            axis = 0;
            BlockPos blockpos = new BlockPos(posStart.getX(), posStart.getY() - width, posStart.getZ() - height);
            BlockPos blockpos1 = new BlockPos(posEnd.getX(), posStart.getY() + width + 1, posStart.getZ() + height + 1);
            int i = posEnd.getX() - posStart.getX();
            double d6 = (double) (posEnd.getY() - posStart.getY()) / ((double) i);
            double d7 = (double) (posEnd.getZ() - posStart.getZ()) / ((double) i);
            iteratorAxis = new IteratorAxis(blockpos, blockpos1, d6, d7);
        }
    }

    private BlockPos reverseCoord(BlockPos pos, boolean revX, boolean revY, boolean revZ) {
        if (revX) {
            pos = new BlockPos(-pos.getX(), pos.getY(), pos.getZ());
        }

        if (revY) {
            pos = new BlockPos(pos.getX(), -pos.getY(), pos.getZ());
        }

        if (revZ) {
            pos = new BlockPos(pos.getX(), pos.getY(), -pos.getZ());
        }

        return pos;
    }

    public boolean hasNext() {
        return iteratorAxis.hasNext();
    }

    public BlockPos next() {
        BlockPos blockpos = iteratorAxis.next();

        return switch (axis) {
            case 1 -> {
                blockPos.setXyz(blockpos.getY() * kX, blockpos.getX() * kY, blockpos.getZ() * kZ);
                yield blockPos;
            }
            case 2 -> {
                blockPos.setXyz(blockpos.getZ() * kX, blockpos.getY() * kY, blockpos.getX() * kZ);
                yield blockPos;
            }
            default -> {
                blockPos.setXyz(blockpos.getX() * kX, blockpos.getY() * kY, blockpos.getZ() * kZ);
                yield blockPos;
            }
        };
    }

    public void remove() {
        throw new RuntimeException("Not supported");
    }
}
