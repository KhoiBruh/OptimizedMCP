package net.minecraft.util;

import net.minecraft.client.Minecraft;

public class Timer {
    public int elapsedTicks;
    public float renderPartialTicks;
    public float timerSpeed = 1.0F;
    public float elapsedPartialTicks;
    float ticksPerSecond;
    private double lastHRTime;
    private long lastSyncSysClock;
    private long lastSyncHRClock;
    private long counter;
    private double timeSyncAdjustment = 1.0D;

    public Timer(float tps) {
        ticksPerSecond = tps;
        lastSyncSysClock = Minecraft.getSystemTime();
        lastSyncHRClock = System.nanoTime() / 1000000L;
    }

    public void updateTimer() {
        long i = Minecraft.getSystemTime();
        long j = i - lastSyncSysClock;
        long k = System.nanoTime() / 1000000L;
        double d0 = (double) k / 1000.0D;

        if (j <= 1000L && j >= 0L) {
            counter += j;

            if (counter > 1000L) {
                long l = k - lastSyncHRClock;
                double d1 = (double) counter / (double) l;
                timeSyncAdjustment += (d1 - timeSyncAdjustment) * 0.20000000298023224D;
                lastSyncHRClock = k;
                counter = 0L;
            }

            if (counter < 0L) {
                lastSyncHRClock = k;
            }
        } else {
            lastHRTime = d0;
        }

        lastSyncSysClock = i;
        double d2 = (d0 - lastHRTime) * timeSyncAdjustment;
        lastHRTime = d0;
        d2 = MathHelper.clamp_double(d2, 0.0D, 1.0D);
        elapsedPartialTicks = (float) ((double) elapsedPartialTicks + d2 * (double) timerSpeed * (double) ticksPerSecond);
        elapsedTicks = (int) elapsedPartialTicks;
        elapsedPartialTicks -= (float) elapsedTicks;

        if (elapsedTicks > 10) {
            elapsedTicks = 10;
        }

        renderPartialTicks = elapsedPartialTicks;
    }
}
