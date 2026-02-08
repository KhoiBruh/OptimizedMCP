package net.minecraft.audio;

public interface ISoundSource {
    void play();
    void stop();
    void pause();

    void setVolume(float volume);
    void setPitch(float pitch);
    void setLooping(boolean loop);
    void setPosition(float x, float y, float z);
    void setVelocity(float x, float y, float z);
    void setAttenuation(float refDist, float maxDist, float rolloff);
    void updateVolumeWithMaster(float masterVolume);

    void cleanup();
    boolean playing();
    boolean paused();
}
