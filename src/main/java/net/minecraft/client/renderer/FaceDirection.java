package net.minecraft.client.renderer;

import net.minecraft.util.Direction;

public enum FaceDirection {
    DOWN(new VertexInfo[]{new VertexInfo(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.SOUTH_INDEX), new VertexInfo(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.NORTH_INDEX), new VertexInfo(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.NORTH_INDEX), new VertexInfo(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.SOUTH_INDEX)}),
    UP(new VertexInfo[]{new VertexInfo(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.NORTH_INDEX), new VertexInfo(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.SOUTH_INDEX), new VertexInfo(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.SOUTH_INDEX), new VertexInfo(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.NORTH_INDEX)}),
    NORTH(new VertexInfo[]{new VertexInfo(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.NORTH_INDEX), new VertexInfo(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.NORTH_INDEX), new VertexInfo(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.NORTH_INDEX), new VertexInfo(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.NORTH_INDEX)}),
    SOUTH(new VertexInfo[]{new VertexInfo(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.SOUTH_INDEX), new VertexInfo(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.SOUTH_INDEX), new VertexInfo(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.SOUTH_INDEX), new VertexInfo(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.SOUTH_INDEX)}),
    WEST(new VertexInfo[]{new VertexInfo(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.NORTH_INDEX), new VertexInfo(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.NORTH_INDEX), new VertexInfo(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.SOUTH_INDEX), new VertexInfo(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.SOUTH_INDEX)}),
    EAST(new VertexInfo[]{new VertexInfo(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.SOUTH_INDEX), new VertexInfo(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.SOUTH_INDEX), new VertexInfo(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.NORTH_INDEX), new VertexInfo(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.NORTH_INDEX)});

    private static final FaceDirection[] facings = new FaceDirection[6];

    static {
        facings[FaceDirection.Constants.DOWN_INDEX] = DOWN;
        facings[FaceDirection.Constants.UP_INDEX] = UP;
        facings[FaceDirection.Constants.NORTH_INDEX] = NORTH;
        facings[FaceDirection.Constants.SOUTH_INDEX] = SOUTH;
        facings[FaceDirection.Constants.WEST_INDEX] = WEST;
        facings[FaceDirection.Constants.EAST_INDEX] = EAST;
    }

    private final VertexInfo[] vertexInfos;

    FaceDirection(VertexInfo[] vertexInfosIn) {
        vertexInfos = vertexInfosIn;
    }

    public static FaceDirection getFacing(Direction facing) {
        return facings[facing.getIndex()];
    }

    public VertexInfo getVertexInformation(int index) {
        return vertexInfos[index];
    }

    public static final class Constants {
        public static final int SOUTH_INDEX = Direction.SOUTH.getIndex();
        public static final int UP_INDEX = Direction.UP.getIndex();
        public static final int EAST_INDEX = Direction.EAST.getIndex();
        public static final int NORTH_INDEX = Direction.NORTH.getIndex();
        public static final int DOWN_INDEX = Direction.DOWN.getIndex();
        public static final int WEST_INDEX = Direction.WEST.getIndex();
    }

    public static class VertexInfo {
        public final int xIndex;
        public final int yIndex;
        public final int zIndex;

        private VertexInfo(int xIndexIn, int yIndexIn, int zIndexIn) {
            xIndex = xIndexIn;
            yIndex = yIndexIn;
            zIndex = zIndexIn;
        }
    }
}
