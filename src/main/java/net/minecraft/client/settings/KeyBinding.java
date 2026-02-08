package net.minecraft.client.settings;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.IntHashMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KeyBinding implements Comparable<KeyBinding> {
    private static final List<KeyBinding> keybindArray = new ArrayList<>();
    private static final IntHashMap<KeyBinding> hash = new IntHashMap<>();
    private static final Set<String> keybindSet = new HashSet<>();
    private final String keyDescription;
    private final int keyCodeDefault;
    private final String keyCategory;
    private int keyCode;
    private boolean pressed;
    private int pressTime;

    public KeyBinding(String description, int keyCode, String category) {
        keyDescription = description;
        this.keyCode = keyCode;
        keyCodeDefault = keyCode;
        keyCategory = category;
        keybindArray.add(this);
        hash.addKey(keyCode, this);
        keybindSet.add(category);
    }

    public static void onTick(int keyCode) {
        if (keyCode != 0) {
            KeyBinding keybinding = hash.lookup(keyCode);

            if (keybinding != null) {
                ++keybinding.pressTime;
            }
        }
    }

    public static void setKeyBindState(int keyCode, boolean pressed) {
        if (keyCode != 0) {
            KeyBinding keybinding = hash.lookup(keyCode);

            if (keybinding != null) {
                keybinding.pressed = pressed;
            }
        }
    }

    public static void unPressAllKeys() {
        for (KeyBinding keybinding : keybindArray) {
            keybinding.unpressKey();
        }
    }

    public static void resetKeyBindingArrayAndHash() {
        hash.clearMap();

        for (KeyBinding keybinding : keybindArray) {
            hash.addKey(keybinding.keyCode, keybinding);
        }
    }

    public static Set<String> getKeybinds() {
        return keybindSet;
    }

    public boolean isKeyDown() {
        return pressed;
    }

    public String getKeyCategory() {
        return keyCategory;
    }

    public boolean isPressed() {
        if (pressTime == 0) {
            return false;
        } else {
            --pressTime;
            return true;
        }
    }

    private void unpressKey() {
        pressTime = 0;
        pressed = false;
    }

    public String getKeyDescription() {
        return keyDescription;
    }

    public int getKeyCodeDefault() {
        return keyCodeDefault;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public int compareTo(KeyBinding p_compareTo_1_) {
        int i = I18n.format(keyCategory).compareTo(I18n.format(p_compareTo_1_.keyCategory));

        if (i == 0) {
            i = I18n.format(keyDescription).compareTo(I18n.format(p_compareTo_1_.keyDescription));
        }

        return i;
    }
}
