package net.optifine.config;

public class GlVersion {
    private final int major;
    private final int minor;
    private final int release;
    private final String suffix;

    public GlVersion(int major, int minor) {
        this(major, minor, 0);
    }

    public GlVersion(int major, int minor, int release) {
        this(major, minor, release, null);
    }

    public GlVersion(int major, int minor, int release, String suffix) {
        this.major = major;
        this.minor = minor;
        this.release = release;
        this.suffix = suffix;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getRelease() {
        return release;
    }

    public int toInt() {
        return minor > 9 ? major * 100 + minor : (release > 9 ? major * 100 + minor * 10 + 9 : major * 100 + minor * 10 + release);
    }

    public String toString() {
        return suffix == null ? major + "." + minor + "." + release : major + "." + minor + "." + release + suffix;
    }
}
