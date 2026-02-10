package net.minecraft.world.pathfinder;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;

public abstract class NodeProcessor {
    protected IBlockAccess blockaccess;
    protected IntHashMap<PathPoint> pointMap = new IntHashMap<>();
    protected int entitySizeX;
    protected int entitySizeY;
    protected int entitySizeZ;

    public void initProcessor(IBlockAccess iblockaccessIn, Entity entityIn) {
        blockaccess = iblockaccessIn;
        pointMap.clearMap();
        entitySizeX = MathHelper.floor(entityIn.width + 1.0F);
        entitySizeY = MathHelper.floor(entityIn.height + 1.0F);
        entitySizeZ = MathHelper.floor(entityIn.width + 1.0F);
    }

    public void postProcess() {
    }

    protected PathPoint openPoint(int x, int y, int z) {
        int i = PathPoint.makeHash(x, y, z);
        PathPoint pathpoint = pointMap.lookup(i);

        if (pathpoint == null) {
            pathpoint = new PathPoint(x, y, z);
            pointMap.addKey(i, pathpoint);
        }

        return pathpoint;
    }

    public abstract PathPoint getPathPointTo(Entity entityIn);

    public abstract PathPoint getPathPointToCoords(Entity entityIn, double x, double y, double target);

    public abstract int findPathOptions(PathPoint[] pathOptions, Entity entityIn, PathPoint currentPoint, PathPoint targetPoint, float maxDistance);
}
