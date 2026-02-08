package net.minecraft.audio;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.*;

public class StreamingSound implements ISoundSource, Runnable {
    private static final int BUFFER_COUNT = 4;
    private static final int BUFFER_SIZE = 0x8000;
    private static final int MAX_WAIT_TIME = BUFFER_COUNT + 5;

    private final int source;
    private final int[] buffers = new int[BUFFER_COUNT];
    private final ByteBuffer fileDataRef;
    private final int channels;
    private final int sampleRate;
    private final Object vorbisLock = new Object();
    private long decoder;
    private volatile boolean threadRunning = false;
    private volatile boolean playRequested = false;
    private volatile boolean looping = true;
    private Thread streamThread;

    private float baseVolume = 1.0f;

    public StreamingSound(ByteBuffer fileData) {
        fileDataRef = fileData;

        source = alGenSources();
        for (int i = 0; i < BUFFER_COUNT; i++) {
            buffers[i] = alGenBuffers();
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            int[] err = new int[1];
            decoder = stb_vorbis_open_memory(fileData, err, null);
            if (decoder == 0L) throw new RuntimeException("stb_vorbis_open_memory failed, err=" + err[0]);

            STBVorbisInfo info = STBVorbisInfo.malloc(stack);
            stb_vorbis_get_info(decoder, info);
            channels = info.channels();
            sampleRate = info.sample_rate();
        }

        for (int buf : buffers) {
            if (!streamBuffer(buf)) break;
        }
        alSourceQueueBuffers(source, buffers);
    }

    private boolean streamBuffer(int bufferId) {
        ShortBuffer pcm = BufferUtils.createShortBuffer(BUFFER_SIZE * channels);

        synchronized (vorbisLock) {
            if (decoder == 0L) {
                return false;
            }
            int samples = stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm);
            if (samples <= 0) {
                return false;
            }
            pcm.limit(samples * channels);
        }

        int format = (channels == 1) ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;
        alBufferData(bufferId, format, pcm, sampleRate);
        return true;
    }

    @Override
    public synchronized void play() {
        playRequested = true;
        if (streamThread == null || !streamThread.isAlive()) {
            threadRunning = true;
            streamThread = new Thread(this, "StreamingSoundThread");
            streamThread.setDaemon(true);
            streamThread.start();
        }

        int queued = alGetSourcei(source, AL_BUFFERS_QUEUED);
        int state = alGetSourcei(source, AL_SOURCE_STATE);
        if (queued > 0 && state != AL_PLAYING) alSourcePlay(source);
    }

    @Override
    public synchronized void pause() {
        playRequested = false;
        alSourcePause(source);
    }

    @Override
    public synchronized void stop() {
        playRequested = false;
        alSourceStop(source);
    }

    @Override
    public void setVolume(float volume) {
        baseVolume = volume;
        updateVolumeWithMaster(1.0f);
    }

    @Override
    public void setPitch(float pitch) {
        alSourcef(source, AL_PITCH, pitch);
    }

    @Override
    public void setLooping(boolean loop) {
        looping = loop;
    }

    @Override
    public void setPosition(float x, float y, float z) {
        alSource3f(source, AL_POSITION, x, y, z);
    }

    @Override
    public void setVelocity(float x, float y, float z) {
        alSource3f(source, AL_VELOCITY, x, y, z);
    }

    @Override
    public void setAttenuation(float refDist, float maxDist, float rolloff) {
        alSourcef(source, AL_REFERENCE_DISTANCE, refDist);
        alSourcef(source, AL_MAX_DISTANCE, maxDist);
        alSourcef(source, AL_ROLLOFF_FACTOR, rolloff);
    }

    @Override
    public void updateVolumeWithMaster(float masterVolume) {
        alSourcef(source, AL_GAIN, baseVolume * masterVolume);
    }

    @Override
    public boolean playing() {
        return alGetSourcei(source, AL_SOURCE_STATE) == AL_PLAYING && playRequested;
    }

    @Override
    public boolean paused() {
        return !playRequested && alGetSourcei(source, AL_SOURCE_STATE) == AL_PAUSED;
    }

    @Override
    public void run() {
        try {
            while (threadRunning) {
                if (!playRequested) {
                    Thread.sleep(MAX_WAIT_TIME);
                    continue;
                }

                int processed = alGetSourcei(source, AL_BUFFERS_PROCESSED);
                int queuedBefore = alGetSourcei(source, AL_BUFFERS_QUEUED);
                boolean wasUnderrun = (queuedBefore == 0);

                for (int i = 0; i < processed; i++) {
                    int buf = alSourceUnqueueBuffers(source);

                    boolean filled = streamBuffer(buf);

                    if (!filled && looping) {
                        synchronized (vorbisLock) {
                            if (decoder != 0L) {
                                stb_vorbis_seek_start(decoder);
                                filled = streamBuffer(buf);
                            }
                        }
                    }

                    if (filled) alSourceQueueBuffers(source, buf);
                }

                int queuedAfter = alGetSourcei(source, AL_BUFFERS_QUEUED);
                int state = alGetSourcei(source, AL_SOURCE_STATE);
                if (wasUnderrun && queuedAfter > 0 && state != AL_PLAYING && playRequested) alSourcePlay(source);

                if (state != AL_PLAYING && queuedAfter > 0 && playRequested) alSourcePlay(source);

                int adaptive = Math.max(2, Math.min(MAX_WAIT_TIME, MAX_WAIT_TIME - queuedAfter));
                Thread.sleep(adaptive);
            }
        } catch (InterruptedException ignored) {}
    }

    @Override
    public synchronized void cleanup() {
        playRequested = false;
        threadRunning = false;
        alSourceStop(source);

        if (streamThread != null) {
            try {
                streamThread.join(2000);
            } catch (InterruptedException ignored) {
            }
            streamThread = null;
        }

        synchronized (vorbisLock) {
            if (decoder != 0L) {
                stb_vorbis_close(decoder);
                decoder = 0L;
            }
        }

        for (int buf : buffers) alDeleteBuffers(buf);
        alDeleteSources(source);
    }
}
