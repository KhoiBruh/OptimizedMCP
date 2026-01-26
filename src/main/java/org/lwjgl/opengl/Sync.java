package org.lwjgl.opengl;

public class Sync {
    private static final long NANOS_IN_SECOND = 1000L * 1000L * 1000L;

    private static long nextFrame;

    private static boolean initialised;

    private static final RunningAvg sleepDurations = new RunningAvg(10);
    private static final RunningAvg yieldDurations = new RunningAvg(10);

    public static void sync(int fps) {
        if (fps <= 0) return;
        if (!initialised) initialise();

        try {
            for (long t0 = getTime(), t1; (nextFrame - t0) > sleepDurations.avg(); t0 = t1) {
                Thread.sleep(1);
                sleepDurations.add((t1 = getTime()) - t0);
            }

            sleepDurations.dampenForLowResTicker();

            for (long t0 = getTime(), t1; (nextFrame - t0) > yieldDurations.avg(); t0 = t1) {
                Thread.yield();
                yieldDurations.add((t1 = getTime()) - t0);
            }
        } catch (InterruptedException ignored) {
        }

        nextFrame = Math.max(nextFrame + NANOS_IN_SECOND / fps, getTime());
    }

    private static void initialise() {
        initialised = true;

        sleepDurations.init(1000 * 1000);
        yieldDurations.init((int) (-(getTime() - getTime()) * 1.333));

        nextFrame = getTime();

        String osName = System.getProperty("os.name");

        if (osName.startsWith("Win")) {
            Thread.ofVirtual().name("LWJGL Timer").start(() -> {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (Exception ignored) {
                }
            });
        }
    }

    private static long getTime() {
        return System.nanoTime();
    }

    private static class RunningAvg {
        private static final long DAMPEN_THRESHOLD = 10 * 1000L * 1000L;
        private static final float DAMPEN_FACTOR = 0.9f;
        private final long[] slots;
        private int offset;

        public RunningAvg(int slotCount) {
            slots = new long[slotCount];
            offset = 0;
        }

        public void init(long value) {
            while (offset < slots.length) {
                slots[offset++] = value;
            }
        }

        public void add(long value) {
            slots[offset++ % slots.length] = value;
            offset %= slots.length;
        }

        public long avg() {
            long sum = 0;
            for (long slot : slots) {
                sum += slot;
            }
            return sum / slots.length;
        }

        public void dampenForLowResTicker() {
            if (DAMPEN_THRESHOLD < avg()) {
                for (int i = 0; i < slots.length; i++) {
                    slots[i] *= DAMPEN_FACTOR;
                }
            }
        }
    }
}