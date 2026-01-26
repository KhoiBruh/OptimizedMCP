package net.optifine.shaders;

import net.minecraft.util.BlockPos;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IteratorAxis implements Iterator<BlockPos> {
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0, 0, 0);

    private final int yDelta;
    private final int zDelta;

    private final int xEnd;
    private int yStart;
    private int yEnd;
    private int zStart;
    private int zEnd;
    private int xNext;
    private int yNext;
    private int zNext;

    private boolean hasNext;

    public IteratorAxis(BlockPos posStart, BlockPos posEnd, int yDelta, int zDelta) {
        this.yDelta = yDelta;
        this.zDelta = zDelta;
        int xStart = posStart.getX();
        xEnd = posEnd.getX();
        yStart = posStart.getY();
        yEnd = posEnd.getY();
        zStart = posStart.getZ();
        zEnd = posEnd.getZ();
        xNext = xStart;
        yNext = yStart;
        zNext = zStart;
        hasNext = xNext < xEnd && yNext < yEnd && zNext < zEnd;
    }

    public boolean hasNext() {
        return hasNext;
    }

    public BlockPos next() {
        if (hasNext) {
            pos.set(xNext, yNext, zNext);
            nextPos();
            hasNext = xNext < xEnd && yNext < yEnd && zNext < zEnd;
            return pos;
        } else throw new NoSuchElementException();
    }

    private void nextPos() {
        ++zNext;

        if (zNext >= zEnd) {
            zNext = zStart;
            ++yNext;

            if (yNext >= yEnd) {
                yNext = yStart;
                yStart += yDelta;
                yEnd += yDelta;
                yNext = yStart;
                zStart += zDelta;
                zEnd += zDelta;
                zNext = zStart;
                ++xNext;
            }
        }
    }

    public void remove() {
        throw new RuntimeException("Not implemented");
    }
}
