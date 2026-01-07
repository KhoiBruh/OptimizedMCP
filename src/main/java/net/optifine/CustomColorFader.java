package net.optifine;

import net.minecraft.src.Config;
import net.minecraft.util.Vec3;

public class CustomColorFader {
    private Vec3 color = null;
    private long timeUpdate = System.currentTimeMillis();

    public Vec3 getColor(double x, double y, double z) {
        if (color == null) {
            color = new Vec3(x, y, z);
        } else {
            long i = System.currentTimeMillis();
            long j = i - timeUpdate;

            if (j != 0L) {
                timeUpdate = i;

                if (!(Math.abs(x - color.xCoord()) < 0.004D) || !(Math.abs(y - color.yCoord()) < 0.004D) || !(Math.abs(z - color.zCoord()) < 0.004D)) {
                    double d0 = (double) j * 0.001D;
                    d0 = Config.limit(d0, 0.0D, 1.0D);
                    double d1 = x - color.xCoord();
                    double d2 = y - color.yCoord();
                    double d3 = z - color.zCoord();
                    double d4 = color.xCoord() + d1 * d0;
                    double d5 = color.yCoord() + d2 * d0;
                    double d6 = color.zCoord() + d3 * d0;
                    color = new Vec3(d4, d5, d6);
                }
            }
        }
        return color;
    }
}
