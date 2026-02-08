package net.minecraft.village;

import net.minecraft.util.BlockPos;
import net.minecraft.util.Direction;

public class VillageDoorInfo {
    private final BlockPos doorBlockPos;
    private final BlockPos insideBlock;
    private final Direction insideDirection;
    private int lastActivityTimestamp;
    private boolean isDetachedFromVillageFlag;
    private int doorOpeningRestrictionCounter;

    public VillageDoorInfo(BlockPos pos, int p_i45871_2_, int p_i45871_3_, int p_i45871_4_) {
        this(pos, getFaceDirection(p_i45871_2_, p_i45871_3_), p_i45871_4_);
    }

    public VillageDoorInfo(BlockPos pos, Direction facing, int timestamp) {
        doorBlockPos = pos;
        insideDirection = facing;
        insideBlock = pos.offset(facing, 2);
        lastActivityTimestamp = timestamp;
    }

    private static Direction getFaceDirection(int deltaX, int deltaZ) {
        return deltaX < 0 ? Direction.WEST : (deltaX > 0 ? Direction.EAST : (deltaZ < 0 ? Direction.NORTH : Direction.SOUTH));
    }

    public int getDistanceSquared(int x, int y, int z) {
        return (int) doorBlockPos.distanceSq(x, y, z);
    }

    public int getDistanceToDoorBlockSq(BlockPos pos) {
        return (int) pos.distanceSq(doorBlockPos);
    }

    public int getDistanceToInsideBlockSq(BlockPos pos) {
        return (int) insideBlock.distanceSq(pos);
    }

    public boolean func_179850_c(BlockPos pos) {
        int i = pos.getX() - doorBlockPos.getX();
        int j = pos.getZ() - doorBlockPos.getY();
        return i * insideDirection.getFrontOffsetX() + j * insideDirection.getFrontOffsetZ() >= 0;
    }

    public void resetDoorOpeningRestrictionCounter() {
        doorOpeningRestrictionCounter = 0;
    }

    public void incrementDoorOpeningRestrictionCounter() {
        ++doorOpeningRestrictionCounter;
    }

    public int getDoorOpeningRestrictionCounter() {
        return doorOpeningRestrictionCounter;
    }

    public BlockPos getDoorBlockPos() {
        return doorBlockPos;
    }

    public BlockPos getInsideBlockPos() {
        return insideBlock;
    }

    public int getInsideOffsetX() {
        return insideDirection.getFrontOffsetX() * 2;
    }

    public int getInsideOffsetZ() {
        return insideDirection.getFrontOffsetZ() * 2;
    }

    public int getInsidePosY() {
        return lastActivityTimestamp;
    }

    public void func_179849_a(int timestamp) {
        lastActivityTimestamp = timestamp;
    }

    public boolean getIsDetachedFromVillageFlag() {
        return isDetachedFromVillageFlag;
    }

    public void setIsDetachedFromVillageFlag(boolean detached) {
        isDetachedFromVillageFlag = detached;
    }
}
