package net.minecraft.pathfinding;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

public class PathEntity {
    private final PathPoint[] points;
    private int currentPathIndex;
    private int pathLength;

    public PathEntity(PathPoint[] pathpoints) {
        points = pathpoints;
        pathLength = pathpoints.length;
    }

    public void incrementPathIndex() {
        ++currentPathIndex;
    }

    public boolean isFinished() {
        return currentPathIndex >= pathLength;
    }

    public PathPoint getFinalPathPoint() {
        return pathLength > 0 ? points[pathLength - 1] : null;
    }

    public PathPoint getPathPointFromIndex(int index) {
        return points[index];
    }

    public int getCurrentPathLength() {
        return pathLength;
    }

    public void setCurrentPathLength(int length) {
        pathLength = length;
    }

    public int getCurrentPathIndex() {
        return currentPathIndex;
    }

    public void setCurrentPathIndex(int currentPathIndexIn) {
        currentPathIndex = currentPathIndexIn;
    }

    public Vec3 getVectorFromIndex(Entity entityIn, int index) {
        double d0 = (double) points[index].xCoord + (double) ((int) (entityIn.width + 1.0F)) * 0.5D;
        double d1 = points[index].yCoord;
        double d2 = (double) points[index].zCoord + (double) ((int) (entityIn.width + 1.0F)) * 0.5D;
        return new Vec3(d0, d1, d2);
    }

    public Vec3 getPosition(Entity entityIn) {
        return getVectorFromIndex(entityIn, currentPathIndex);
    }

    public boolean isSamePath(PathEntity pathentityIn) {
        if (pathentityIn == null) {
            return false;
        } else if (pathentityIn.points.length != points.length) {
            return false;
        } else {
            for (int i = 0; i < points.length; ++i) {
                if (points[i].xCoord != pathentityIn.points[i].xCoord || points[i].yCoord != pathentityIn.points[i].yCoord || points[i].zCoord != pathentityIn.points[i].zCoord) {
                    return false;
                }
            }

            return true;
        }
    }

    public boolean isDestinationSame(Vec3 vec) {
        PathPoint pathpoint = getFinalPathPoint();
        return pathpoint != null && pathpoint.xCoord == (int) vec.xCoord() && pathpoint.zCoord == (int) vec.zCoord();
    }
}
