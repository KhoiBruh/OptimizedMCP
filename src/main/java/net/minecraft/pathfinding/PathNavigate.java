package net.minecraft.pathfinding;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

import java.util.List;

public abstract class PathNavigate {
    private final IAttributeInstance pathSearchRange;
    private final PathFinder pathFinder;
    protected EntityLiving theEntity;
    protected World worldObj;
    protected PathEntity currentPath;
    protected double speed;
    private int totalTicks;
    private int ticksAtLastPos;
    private Vec3 lastPosCheck = new Vec3(0.0D, 0.0D, 0.0D);
    private float heightRequirement = 1.0F;

    public PathNavigate(EntityLiving entitylivingIn, World worldIn) {
        theEntity = entitylivingIn;
        worldObj = worldIn;
        pathSearchRange = entitylivingIn.getEntityAttribute(SharedMonsterAttributes.followRange);
        pathFinder = getPathFinder();
    }

    protected abstract PathFinder getPathFinder();

    public void setSpeed(double speedIn) {
        speed = speedIn;
    }

    public float getPathSearchRange() {
        return (float) pathSearchRange.getAttributeValue();
    }

    public final PathEntity getPathToXYZ(double x, double y, double z) {
        return getPathToPos(new BlockPos(MathHelper.floor_double(x), (int) y, MathHelper.floor_double(z)));
    }

    public PathEntity getPathToPos(BlockPos pos) {
        if (!canNavigate()) {
            return null;
        } else {
            float f = getPathSearchRange();
            worldObj.theProfiler.startSection("pathfind");
            BlockPos blockpos = new BlockPos(theEntity);
            int i = (int) (f + 8.0F);
            ChunkCache chunkcache = new ChunkCache(worldObj, blockpos.add(-i, -i, -i), blockpos.add(i, i, i), 0);
            PathEntity pathentity = pathFinder.createEntityPathTo(chunkcache, theEntity, pos, f);
            worldObj.theProfiler.endSection();
            return pathentity;
        }
    }

    public boolean tryMoveToXYZ(double x, double y, double z, double speedIn) {
        PathEntity pathentity = getPathToXYZ(MathHelper.floor_double(x), (int) y, MathHelper.floor_double(z));
        return setPath(pathentity, speedIn);
    }

    public void setHeightRequirement(float jumpHeight) {
        heightRequirement = jumpHeight;
    }

    public PathEntity getPathToEntityLiving(Entity entityIn) {
        if (!canNavigate()) {
            return null;
        } else {
            float f = getPathSearchRange();
            worldObj.theProfiler.startSection("pathfind");
            BlockPos blockpos = (new BlockPos(theEntity)).up();
            int i = (int) (f + 16.0F);
            ChunkCache chunkcache = new ChunkCache(worldObj, blockpos.add(-i, -i, -i), blockpos.add(i, i, i), 0);
            PathEntity pathentity = pathFinder.createEntityPathTo(chunkcache, theEntity, entityIn, f);
            worldObj.theProfiler.endSection();
            return pathentity;
        }
    }

    public boolean tryMoveToEntityLiving(Entity entityIn, double speedIn) {
        PathEntity pathentity = getPathToEntityLiving(entityIn);
        return pathentity != null && setPath(pathentity, speedIn);
    }

    public boolean setPath(PathEntity pathentityIn, double speedIn) {
        if (pathentityIn == null) {
            currentPath = null;
            return false;
        } else {
            if (!pathentityIn.isSamePath(currentPath)) {
                currentPath = pathentityIn;
            }

            removeSunnyPath();

            if (currentPath.getCurrentPathLength() == 0) {
                return false;
            } else {
                speed = speedIn;
                Vec3 vec3 = getEntityPosition();
                ticksAtLastPos = totalTicks;
                lastPosCheck = vec3;
                return true;
            }
        }
    }

    public PathEntity getPath() {
        return currentPath;
    }

    public void onUpdateNavigation() {
        ++totalTicks;

        if (!noPath()) {
            if (canNavigate()) {
                pathFollow();
            } else if (currentPath != null && currentPath.getCurrentPathIndex() < currentPath.getCurrentPathLength()) {
                Vec3 vec3 = getEntityPosition();
                Vec3 vec31 = currentPath.getVectorFromIndex(theEntity, currentPath.getCurrentPathIndex());

                if (vec3.yCoord() > vec31.yCoord() && !theEntity.onGround && MathHelper.floor_double(vec3.xCoord()) == MathHelper.floor_double(vec31.xCoord()) && MathHelper.floor_double(vec3.zCoord()) == MathHelper.floor_double(vec31.zCoord())) {
                    currentPath.setCurrentPathIndex(currentPath.getCurrentPathIndex() + 1);
                }
            }

            if (!noPath()) {
                Vec3 vec32 = currentPath.getPosition(theEntity);

                if (vec32 != null) {
                    AxisAlignedBB axisalignedbb1 = (new AxisAlignedBB(vec32.xCoord(), vec32.yCoord(), vec32.zCoord(), vec32.xCoord(), vec32.yCoord(), vec32.zCoord())).expand(0.5D, 0.5D, 0.5D);
                    List<AxisAlignedBB> list = worldObj.getCollidingBoundingBoxes(theEntity, axisalignedbb1.addCoord(0.0D, -1.0D, 0.0D));
                    double d0 = -1.0D;
                    axisalignedbb1 = axisalignedbb1.offset(0.0D, 1.0D, 0.0D);

                    for (AxisAlignedBB axisalignedbb : list) {
                        d0 = axisalignedbb.calculateYOffset(axisalignedbb1, d0);
                    }

                    theEntity.getMoveHelper().setMoveTo(vec32.xCoord(), vec32.yCoord() + d0, vec32.zCoord(), speed);
                }
            }
        }
    }

    protected void pathFollow() {
        Vec3 vec3 = getEntityPosition();
        int i = currentPath.getCurrentPathLength();

        for (int j = currentPath.getCurrentPathIndex(); j < currentPath.getCurrentPathLength(); ++j) {
            if (currentPath.getPathPointFromIndex(j).yCoord != (int) vec3.yCoord()) {
                i = j;
                break;
            }
        }

        float f = theEntity.width * theEntity.width * heightRequirement;

        for (int k = currentPath.getCurrentPathIndex(); k < i; ++k) {
            Vec3 vec31 = currentPath.getVectorFromIndex(theEntity, k);

            if (vec3.squareDistanceTo(vec31) < (double) f) {
                currentPath.setCurrentPathIndex(k + 1);
            }
        }

        int j1 = MathHelper.ceiling_float_int(theEntity.width);
        int k1 = (int) theEntity.height + 1;

        for (int i1 = i - 1; i1 >= currentPath.getCurrentPathIndex(); --i1) {
            if (isDirectPathBetweenPoints(vec3, currentPath.getVectorFromIndex(theEntity, i1), j1, k1, j1)) {
                currentPath.setCurrentPathIndex(i1);
                break;
            }
        }

        checkForStuck(vec3);
    }

    protected void checkForStuck(Vec3 positionVec3) {
        if (totalTicks - ticksAtLastPos > 100) {
            if (positionVec3.squareDistanceTo(lastPosCheck) < 2.25D) {
                clearPathEntity();
            }

            ticksAtLastPos = totalTicks;
            lastPosCheck = positionVec3;
        }
    }

    public boolean noPath() {
        return currentPath == null || currentPath.isFinished();
    }

    public void clearPathEntity() {
        currentPath = null;
    }

    protected abstract Vec3 getEntityPosition();

    protected abstract boolean canNavigate();

    protected boolean isInLiquid() {
        return theEntity.isInWater() || theEntity.isInLava();
    }

    protected void removeSunnyPath() {
    }

    protected abstract boolean isDirectPathBetweenPoints(Vec3 posVec31, Vec3 posVec32, int sizeX, int sizeY, int sizeZ);
}
