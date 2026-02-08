package net.minecraft.client.audio;

import net.minecraft.util.RegistrySimple;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class SoundRegistry extends RegistrySimple<ResourceLocation, SoundEventAccessorComposite> {
    private Map<ResourceLocation, SoundEventAccessorComposite> soundRegistry;

    protected Map<ResourceLocation, SoundEventAccessorComposite> createUnderlyingMap() {
        soundRegistry = new HashMap<>();
        return soundRegistry;
    }

    public void registerSound(SoundEventAccessorComposite p_148762_1_) {
        putObject(p_148762_1_.getSoundEventLocation(), p_148762_1_);
    }

    public void clearMap() {
        soundRegistry.clear();
    }
}
