package net.minecraft.util;

import net.minecraft.entity.Entity;

public class MovingObjectPosition {
    public MovingObjectPosition.MovingObjectType typeOfHit;
    public EnumFacing sideHit;
    public Vec3 hitVec;
    public Entity entityHit;
    private BlockPos blockPos;

    public MovingObjectPosition(Vec3 hitVecIn, EnumFacing facing, BlockPos blockPosIn) {
        this(MovingObjectPosition.MovingObjectType.BLOCK, hitVecIn, facing, blockPosIn);
    }

    public MovingObjectPosition(Vec3 p_i45552_1_, EnumFacing facing) {
        this(MovingObjectPosition.MovingObjectType.BLOCK, p_i45552_1_, facing, BlockPos.ORIGIN);
    }

    public MovingObjectPosition(Entity entityIn) {
        this(entityIn, new Vec3(entityIn.posX, entityIn.posY, entityIn.posZ));
    }

    public MovingObjectPosition(MovingObjectPosition.MovingObjectType typeOfHitIn, Vec3 hitVecIn, EnumFacing sideHitIn, BlockPos blockPosIn) {
        typeOfHit = typeOfHitIn;
        blockPos = blockPosIn;
        sideHit = sideHitIn;
        hitVec = new Vec3(hitVecIn.xCoord(), hitVecIn.yCoord(), hitVecIn.zCoord());
    }

    public MovingObjectPosition(Entity entityHitIn, Vec3 hitVecIn) {
        typeOfHit = MovingObjectPosition.MovingObjectType.ENTITY;
        entityHit = entityHitIn;
        hitVec = hitVecIn;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public String toString() {
        return "HitResult{type=" + typeOfHit + ", blockpos=" + blockPos + ", f=" + sideHit + ", pos=" + hitVec + ", entity=" + entityHit + '}';
    }

    public enum MovingObjectType {
        MISS,
        BLOCK,
        ENTITY
    }
}
