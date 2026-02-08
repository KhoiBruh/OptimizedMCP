package net.minecraft.client.audio;

import com.google.common.collect.*;
import net.minecraft.audio.ISoundSource;
import net.minecraft.audio.SoundSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.*;
import java.util.Map.Entry;

public class SoundManager {
    private static final Marker LOG_MARKER = MarkerManager.getMarker("SOUNDS");
    private static final Logger logger = LogManager.getLogger();
    private final SoundHandler soundHandler;
    private final GameSettings options;
    private final Map<String, ISound> playingSounds = HashBiMap.create();
    private final Map<ISound, String> invPlayingSounds;
    private final Multimap<SoundCategory, String> categorySounds;
    private final List<ITickableSound> tickableSounds;
    private final Map<ISound, Integer> delayedSounds;
    private final Map<String, Integer> playingSoundsStopTime;
    private SoundManager.SoundSystemStarterThread soundSystem;
    private boolean loaded;
    private int playTime = 0;
    private final Map<ISound, SoundPoolEntry> playingSoundPoolEntries;

    public SoundManager(SoundHandler handler, GameSettings settings) {
        invPlayingSounds = ((BiMap) playingSounds).inverse();
        playingSoundPoolEntries = new HashMap<>();
        categorySounds = HashMultimap.create();
        tickableSounds = new ArrayList<>();
        delayedSounds = new HashMap<>();
        playingSoundsStopTime = new HashMap<>();
        soundHandler = handler;
        options = settings;
    }

    private static URL getURLForSoundResource(final ResourceLocation p_148612_0_) {
        String s = String.format("%s:%s:%s", "mcsounddomain", p_148612_0_.getResourceDomain(), p_148612_0_.getResourcePath());
        URLStreamHandler urlstreamhandler = new URLStreamHandler() {
            protected URLConnection openConnection(final URL p_openConnection_1_) {
                return new URLConnection(p_openConnection_1_) {
                    public void connect() {
                    }

                    public InputStream getInputStream() throws IOException {
                        return Minecraft.getMinecraft().getResourceManager().getResource(p_148612_0_).getInputStream();
                    }
                };
            }
        };

        try {
            return new URL(null, s, urlstreamhandler);
        } catch (MalformedURLException var4) {
            throw new Error("TODO: Sanely handle url exception! :D");
        }
    }

    public void reloadSoundSystem() {
        unloadSoundSystem();
        loadSoundSystem();
    }

    private synchronized void loadSoundSystem() {
        if (!loaded) {
            try {
                (new Thread(() -> {
                    soundSystem = new SoundSystemStarterThread();
                    soundSystem.init();
                    loaded = true;
                    soundSystem.setMasterVolume(options.getSoundLevel(SoundCategory.MASTER));
                    logger.info(LOG_MARKER, "Sound system started");
                }, "Sound Library Loader")).start();
            } catch (RuntimeException runtimeexception) {
                logger.error(LOG_MARKER, "Error starting SoundSystem. Turning off sounds & music", runtimeexception);
                options.setSoundLevel(SoundCategory.MASTER, 0.0F);
                options.saveOptions();
            }
        }
    }

    private float getSoundCategoryVolume(SoundCategory category) {
        return category != null && category != SoundCategory.MASTER ? options.getSoundLevel(category) : 1.0F;
    }

    public void setSoundCategoryVolume(SoundCategory category, float volume) {
        if (loaded) {
            if (category == SoundCategory.MASTER) {
                soundSystem.setMasterVolume(volume);
            } else {
                for (String s : categorySounds.get(category)) {
                    ISound isound = playingSounds.get(s);
                    float f = getNormalizedVolume(isound, playingSoundPoolEntries.get(isound), category);

                    if (f <= 0.0F) {
                        stopSound(isound);
                    } else {
                        soundSystem.setVolume(s, f);
                    }
                }
            }
        }
    }

    public void unloadSoundSystem() {
        if (loaded) {
            stopAllSounds();
            soundSystem.cleanup();
            loaded = false;
        }
    }

    public void stopAllSounds() {
        if (loaded) {
            for (String s : playingSounds.keySet()) {
                soundSystem.stop(s);
            }

            playingSounds.clear();
            delayedSounds.clear();
            tickableSounds.clear();
            categorySounds.clear();
            playingSoundPoolEntries.clear();
            playingSoundsStopTime.clear();
        }
    }

    public void updateAllSounds() {
        ++playTime;

        for (ITickableSound itickablesound : tickableSounds) {
            itickablesound.update();

            if (itickablesound.isDonePlaying()) {
                stopSound(itickablesound);
            } else {
                String s = invPlayingSounds.get(itickablesound);
                soundSystem.setVolume(s, getNormalizedVolume(itickablesound, playingSoundPoolEntries.get(itickablesound), soundHandler.getSound(itickablesound.getSoundLocation()).getSoundCategory()));
                soundSystem.setPitch(s, getNormalizedPitch(itickablesound, playingSoundPoolEntries.get(itickablesound)));
                soundSystem.setPosition(s, itickablesound.getXPosF(), itickablesound.getYPosF(), itickablesound.getZPosF());
            }
        }

        Iterator<Entry<String, ISound>> iterator = playingSounds.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<String, ISound> entry = iterator.next();
            String s1 = entry.getKey();
            ISound isound = entry.getValue();

            if (!soundSystem.playing(s1)) {
                int i = playingSoundsStopTime.get(s1);

                if (i <= playTime) {
                    int j = isound.getRepeatDelay();

                    if (isound.canRepeat() && j > 0) {
                        delayedSounds.put(isound, playTime + j);
                    }

                    iterator.remove();
                    logger.debug(LOG_MARKER, "Removed channel {} because it's not playing anymore", new Object[]{s1});
                    soundSystem.removeSource(s1);
                    playingSoundsStopTime.remove(s1);
                    playingSoundPoolEntries.remove(isound);

                    try {
                        categorySounds.remove(soundHandler.getSound(isound.getSoundLocation()).getSoundCategory(), s1);
                    } catch (RuntimeException var8) {
                    }

                    if (isound instanceof ITickableSound) {
                        tickableSounds.remove(isound);
                    }
                }
            }
        }

        Iterator<Entry<ISound, Integer>> iterator1 = delayedSounds.entrySet().iterator();

        while (iterator1.hasNext()) {
            Entry<ISound, Integer> entry1 = iterator1.next();

            if (playTime >= entry1.getValue()) {
                ISound isound1 = entry1.getKey();

                if (isound1 instanceof ITickableSound) ((ITickableSound) isound1).update();

                playSound(isound1);
                iterator1.remove();
            }
        }
    }

    public boolean isSoundPlaying(ISound sound) {
        if (loaded) {
            String s = invPlayingSounds.get(sound);
            return s != null && (soundSystem.playing(s) || playingSoundsStopTime.containsKey(s) && playingSoundsStopTime.get(s) <= playTime);
        } else return false;
    }

    public void stopSound(ISound sound) {
        if (loaded) {
            String s = invPlayingSounds.get(sound);

            if (s != null) soundSystem.stop(s);
        }
    }

    public void playSound(ISound sound) {
        if (loaded) {
            if (soundSystem.getMasterVolume() <= 0.0F) {
                logger.debug(LOG_MARKER, "Skipped playing soundEvent: {}, master volume was zero", new Object[]{sound.getSoundLocation()});
            } else {
                SoundEventAccessorComposite soundeventaccessorcomposite = soundHandler.getSound(sound.getSoundLocation());

                if (soundeventaccessorcomposite == null) {
                    logger.warn(LOG_MARKER, "Unable to play unknown soundEvent: {}", new Object[]{sound.getSoundLocation()});
                } else {
                    SoundPoolEntry soundpoolentry = soundeventaccessorcomposite.cloneEntry();

                    if (soundpoolentry == SoundHandler.missing_sound) {
                        logger.warn(LOG_MARKER, "Unable to play empty soundEvent: {}", new Object[]{soundeventaccessorcomposite.getSoundEventLocation()});
                    } else {
                        float f = sound.getVolume();
                        float f1 = 16.0F;

                        if (f > 1.0F) {
                            f1 *= f;
                        }

                        SoundCategory soundcategory = soundeventaccessorcomposite.getSoundCategory();
                        float f2 = getNormalizedVolume(sound, soundpoolentry, soundcategory);
                        double d0 = getNormalizedPitch(sound, soundpoolentry);
                        ResourceLocation resourcelocation = soundpoolentry.getSoundPoolEntryLocation();

                        if (f2 == 0.0F) {
                            logger.debug(LOG_MARKER, "Skipped playing sound {}, volume was zero.", new Object[]{resourcelocation});
                        } else {
                            boolean flag = sound.canRepeat() && sound.getRepeatDelay() == 0;
                            String s = UUID.randomUUID().toString();

//                            if (soundpoolentry.isStreamingSound()) {
//                                soundSystem.loadStreamingSound(false, s, getURLForSoundResource(resourcelocation), resourcelocation.toString(), flag, sound.getXPosF(), sound.getYPosF(), sound.getZPosF(), sound.getAttenuationType().getTypeInt(), f1);
//                            } else {
//                                soundSystem.loadStaticSound(false, s, getURLForSoundResource(resourcelocation), resourcelocation.toString(), flag, sound.getXPosF(), sound.getYPosF(), sound.getZPosF(), sound.getAttenuationType().getTypeInt(), f1);
//                            }
                            try (InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(resourcelocation).getInputStream()) {
                                if (soundpoolentry.isStreamingSound()) {
                                    soundSystem.loadStreamingSound(s, stream, flag, sound.getXPosF(), sound.getYPosF(), sound.getZPosF(), sound.getAttenuationType().getTypeInt(), f1);
                                } else {
                                    soundSystem.loadStaticSound(s, stream, flag, sound.getXPosF(), sound.getYPosF(), sound.getZPosF(), sound.getAttenuationType().getTypeInt(), f1);
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            logger.debug(LOG_MARKER, "Playing sound {} for event {} as channel {}", new Object[]{soundpoolentry.getSoundPoolEntryLocation(), soundeventaccessorcomposite.getSoundEventLocation(), s});
                            soundSystem.setPitch(s, (float) d0);
                            soundSystem.setVolume(s, f2);
                            soundSystem.play(s);
                            playingSoundsStopTime.put(s, playTime + 20);
                            playingSounds.put(s, sound);
                            playingSoundPoolEntries.put(sound, soundpoolentry);

                            if (soundcategory != SoundCategory.MASTER) {
                                categorySounds.put(soundcategory, s);
                            }

                            if (sound instanceof ITickableSound) {
                                tickableSounds.add((ITickableSound) sound);
                            }
                        }
                    }
                }
            }
        }
    }

    private float getNormalizedPitch(ISound sound, SoundPoolEntry entry) {
        return (float) MathHelper.clamp_double((double) sound.getPitch() * entry.getPitch(), 0.5D, 2.0D);
    }

    private float getNormalizedVolume(ISound sound, SoundPoolEntry entry, SoundCategory category) {
        return (float) MathHelper.clamp_double((double) sound.getVolume() * entry.getVolume(), 0.0D, 1.0D) * getSoundCategoryVolume(category);
    }

    public void pauseAllSounds() {
        for (String s : playingSounds.keySet()) {
            logger.debug(LOG_MARKER, "Pausing channel {}", new Object[]{s});
            soundSystem.pause(s);
        }
    }

    public void resumeAllSounds() {
        for (String s : playingSounds.keySet()) {
            logger.debug(LOG_MARKER, "Resuming channel {}", new Object[]{s});
            soundSystem.play(s);
        }
    }

    public void playDelayedSound(ISound sound, int delay) {
        delayedSounds.put(sound, playTime + delay);
    }

    public void setListener(EntityPlayer player, float p_148615_2_) {
        if (loaded && player != null) {
            float f = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * p_148615_2_;
            float f1 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * p_148615_2_;
            double d0 = player.prevPosX + (player.posX - player.prevPosX) * (double) p_148615_2_;
            double d1 = player.prevPosY + (player.posY - player.prevPosY) * (double) p_148615_2_ + (double) player.getEyeHeight();
            double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) p_148615_2_;
            float f2 = MathHelper.cos((f1 + 90.0F) * 0.017453292F);
            float f3 = MathHelper.sin((f1 + 90.0F) * 0.017453292F);
            float f4 = MathHelper.cos(-f * 0.017453292F);
            float f5 = MathHelper.sin(-f * 0.017453292F);
            float f6 = MathHelper.cos((-f + 90.0F) * 0.017453292F);
            float f7 = MathHelper.sin((-f + 90.0F) * 0.017453292F);
            float f8 = f2 * f4;
            float f9 = f3 * f4;
            float f10 = f2 * f6;
            float f11 = f3 * f6;
            soundSystem.setListenerPosition((float) d0, (float) d1, (float) d2);
            soundSystem.setListenerOrientation(f8, f5, f9, f10, f7, f11);
        }
    }

    static class SoundSystemStarterThread extends SoundSystem {
        private static final Object lock = new Object();
        private SoundSystemStarterThread() {
        }

        public boolean playing(String sound) {

            synchronized (lock) {
                ISoundSource source = getSound(sound);
                return source != null && (source.playing() || source.paused());
            }
        }
    }
}
