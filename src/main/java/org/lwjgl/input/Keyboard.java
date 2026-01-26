package org.lwjgl.input;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.Display;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class Keyboard {
    public static final int EVENT_SIZE = 4 + 1 + 4 + 8 + 1;
    public static final int KEYBOARD_SIZE = GLFW.GLFW_KEY_LAST + 1;
    public static final int CHAR_NONE = '\0';
    private static final int BUFFER_SIZE = 50;
    private static final ByteBuffer keyDownBuffer = BufferUtils.createByteBuffer(KEYBOARD_SIZE);
    private static final String[] keyNames = new String[KEYBOARD_SIZE];
    private static final Map<String, Integer> keyMap = new HashMap<>(253);

    public static final int KEY_NONE = register("NONE", 0x00);
    public static final int KEY_SPACE = register("SPACE", 57);
    public static final int KEY_APOSTROPHE = register("APOSTROPHE", 40);
    public static final int KEY_COMMA = register("COMMA", 51);
    public static final int KEY_MINUS = register("MINUS", 12);
    public static final int KEY_PERIOD = register("PERIOD", 52);
    public static final int KEY_SLASH = register("SLASH", 53);
    public static final int KEY_0 = register("0", 11);
    public static final int KEY_1 = register("1", 2);
    public static final int KEY_2 = register("2", 3);
    public static final int KEY_3 = register("3", 4);
    public static final int KEY_4 = register("4", 5);
    public static final int KEY_5 = register("5", 6);
    public static final int KEY_6 = register("6", 7);
    public static final int KEY_7 = register("7", 8);
    public static final int KEY_8 = register("8", 9);
    public static final int KEY_9 = register("9", 10);
    public static final int KEY_SEMICOLON = register("SEMICOLON", 39);
    public static final int KEY_EQUALS = register("EQUALS", 13);
    public static final int KEY_A = register("A", 30);
    public static final int KEY_B = register("B", 48);
    public static final int KEY_C = register("C", 46);
    public static final int KEY_D = register("D", 32);
    public static final int KEY_E = register("E", 18);
    public static final int KEY_F = register("F", 33);
    public static final int KEY_G = register("G", 34);
    public static final int KEY_H = register("H", 35);
    public static final int KEY_I = register("I", 23);

    public static final int KEY_J = register("J", 36);
    public static final int KEY_K = register("K", 37);
    public static final int KEY_L = register("L", 38);
    public static final int KEY_M = register("M", 50);
    public static final int KEY_N = register("N", 49);
    public static final int KEY_O = register("O", 24);
    public static final int KEY_P = register("P", 25);
    public static final int KEY_Q = register("Q", 16);
    public static final int KEY_R = register("R", 19);
    public static final int KEY_S = register("S", 31);
    public static final int KEY_T = register("T", 20);
    public static final int KEY_U = register("U", 22);
    public static final int KEY_V = register("V", 47);
    public static final int KEY_W = register("W", 17);
    public static final int KEY_X = register("X", 45);
    public static final int KEY_Y = register("Y", 21);
    public static final int KEY_Z = register("Z", 44);
    public static final int KEY_LBRACKET = register("LBRACKET", 26);
    public static final int KEY_BACKSLASH = register("BACKSLASH", 43);
    public static final int KEY_RBRACKET = register("RBRACKET", 27);
    public static final int KEY_GRAVE = register("GRAVE", 41);
    public static final int KEY_WORLD_1 = register("WORLD_1", 161);
    public static final int KEY_WORLD_2 = register("WORLD_2", 162);
    public static final int KEY_ESCAPE = register("ESCAPE", 1);
    public static final int KEY_RETURN = register("RETURN", 28);
    public static final int KEY_TAB = register("TAB", 15);
    public static final int KEY_BACK = register("BACK", 14);
    public static final int KEY_INSERT = register("INSERT", 210);
    public static final int KEY_DELETE = register("DELETE", 211);
    public static final int KEY_RIGHT = register("RIGHT", 205);
    public static final int KEY_LEFT = register("LEFT", 203);
    public static final int KEY_DOWN = register("DOWN", 208);
    public static final int KEY_UP = register("UP", 200);
    public static final int KEY_PRIOR = register("PRIOR", 201);
    public static final int KEY_NEXT = register("NEXT", 209);
    public static final int KEY_HOME = register("HOME", 199);
    public static final int KEY_END = register("END", 207);
    public static final int KEY_CAPITAL = register("CAPITAL", 58);
    public static final int KEY_SCROLL = register("SCROLL", 70);
    public static final int KEY_NUMLOCK = register("NUMLOCK", 69);
    public static final int KEY_PRINT_SCREEN = register("PRINT_SCREEN", 28);
    public static final int KEY_PAUSE = register("PAUSE", 197);
    public static final int KEY_F1 = register("F1", 59);
    public static final int KEY_F2 = register("F2", 60);
    public static final int KEY_F3 = register("F3", 61);
    public static final int KEY_F4 = register("F4", 62);
    public static final int KEY_F5 = register("F5", 63);
    public static final int KEY_F6 = register("F6", 64);
    public static final int KEY_F7 = register("F7", 65);
    public static final int KEY_F8 = register("F8", 66);
    public static final int KEY_F9 = register("F9", 67);
    public static final int KEY_F10 = register("F10", 68);
    public static final int KEY_F11 = register("F11", 87);
    public static final int KEY_F12 = register("F12", 88);
    public static final int KEY_F13 = register("F13", 100);
    public static final int KEY_F14 = register("F14", 101);
    public static final int KEY_F15 = register("F15", 102);
    public static final int KEY_F16 = register("F16", 103);
    public static final int KEY_F17 = register("F17", 104);
    public static final int KEY_F18 = register("F18", 105);
    public static final int KEY_F19 = register("F19", 113);
    public static final int KEY_F20 = register("F20", 309);
    public static final int KEY_F21 = register("F21", 310);
    public static final int KEY_F22 = register("F22", 311);
    public static final int KEY_F23 = register("F23", 312);
    public static final int KEY_F24 = register("F24", 313);
    public static final int KEY_F25 = register("F25", 314);
    public static final int KEY_NUMPAD0 = register("NUMPAD0", 82);
    public static final int KEY_NUMPAD1 = register("NUMPAD1", 79);
    public static final int KEY_NUMPAD2 = register("NUMPAD2", 80);
    public static final int KEY_NUMPAD3 = register("NUMPAD3", 81);
    public static final int KEY_NUMPAD4 = register("NUMPAD4", 75);
    public static final int KEY_NUMPAD5 = register("NUMPAD5", 76);
    public static final int KEY_NUMPAD6 = register("NUMPAD6", 77);
    public static final int KEY_NUMPAD7 = register("NUMPAD7", 71);
    public static final int KEY_NUMPAD8 = register("NUMPAD8", 72);
    public static final int KEY_NUMPAD9 = register("NUMPAD9", 73);
    public static final int KEY_DECIMAL = register("DECIMAL", 83);
    public static final int KEY_DIVIDE = register("DIVIDE", 181);
    public static final int KEY_MULTIPLY = register("MULTIPLY", 55);
    public static final int KEY_SUBTRACT = register("SUBTRACT", 74);
    public static final int KEY_ADD = register("ADD", 78);
    public static final int KEY_NUMPADENTER = register("NUMPADENTER", 156);
    public static final int KEY_NUMPADEQUALS = register("NUMPADEQUALS", 141);
    public static final int KEY_LSHIFT = register("LSHIFT", 42);
    public static final int KEY_LCONTROL = register("LCONTROL", 29);
    public static final int KEY_LMENU = register("LMENU", 56);
    public static final int KEY_LMETA = register("LMETA", 219);
    public static final int KEY_RSHIFT = register("RSHIFT", 54);
    public static final int KEY_RCONTROL = register("RCONTROL", 157);
    public static final int KEY_RMENU = register("RMENU", 184);
    public static final int KEY_RMETA = register("RMETA", 220);
    public static final int KEY_MENU = register("MENU", 348);

    private static boolean created;
    private static boolean repeat;
    private static ByteBuffer readBuffer;
    private static final KeyEvent current_event = new KeyEvent();
    private static final KeyEvent tmp_event = new KeyEvent();
    private static boolean initialized;
    private static Input implementation;

    private Keyboard() {
    }

    private static void initialize() {
        if (initialized) return;
        Sys.initialize();
        initialized = true;
    }

    private static void create(Input impl) {
        if (created) return;
        if (!initialized) initialize();

        implementation = impl;
        implementation.createKeyboard();
        created = true;
        readBuffer = ByteBuffer.allocate(EVENT_SIZE * BUFFER_SIZE);
        reset();
    }

    public static void create() throws LWJGLException {
        if (!Display.isCreated()) throw new IllegalStateException("Display must be created.");

        create(InputUtil.getOrCreateInput());
    }

    private static void reset() {
        readBuffer.limit(0);

        for (int i = 0; i < keyDownBuffer.remaining(); i++) {
            keyDownBuffer.put(i, (byte) 0);
        }

        current_event.reset();
    }

    public static boolean isCreated() {
        return created;
    }

    public static void destroy() {
        if (!created)
            return;
        created = false;
        implementation.destroyKeyboard();
        reset();
    }

    public static void poll() {
        if (!created) throw new IllegalStateException("Keyboard must be created before you can poll the device");
        implementation.pollKeyboard(keyDownBuffer);
        read();
    }

    private static void read() {
        readBuffer.compact();
        implementation.readKeyboard(readBuffer);
        readBuffer.flip();
    }

    public static boolean isKeyDown(int key) {
        if (!created) throw new IllegalStateException("Keyboard must be created before you can query key state");
        return keyDownBuffer.get(key) != 0;
    }

    public static synchronized String getKeyName(int key) {
        return keyNames[key];
    }

    public static synchronized int getKeyIndex(String keyName) {
        return keyMap.getOrDefault(keyName, KEY_NONE);
    }

    public static int getNumKeyboardEvents() {
        if (!created) throw new IllegalStateException("Keyboard must be created before you can read events");
        int old_position = readBuffer.position();
        int num_events = 0;

        while (readNext(tmp_event) && (!tmp_event.repeat || repeat)) {
            num_events++;
        }

        readBuffer.position(old_position);
        return num_events;
    }

    public static boolean next() {
        if (!created) throw new IllegalStateException("Keyboard must be created before you can read events");

        boolean result;
        while ((result = readNext(current_event)) && current_event.repeat && !repeat);

        return result;
    }

    public static void enableRepeatEvents(boolean enable) {
        repeat = enable;
    }

    public static boolean isRepeat() {
        return repeat;
    }

    private static boolean readNext(KeyEvent event) {
        if (readBuffer.hasRemaining()) {
            event.key = readBuffer.getInt();
            event.state = 0 != readBuffer.get();
            event.character = readBuffer.getInt();
            event.nanos = readBuffer.getLong();
            event.repeat = 1 == readBuffer.get();
            return true;
        } else
            return false;
    }

    public static int getKeyCount() {
        return keyMap.size();
    }

    public static char getEventCharacter() {
        return (char) current_event.character;
    }

    public static int getEventKey() {
        return current_event.key;
    }

    public static boolean getEventKeyState() {
        return current_event.state;
    }

    public static long getEventNanoseconds() {
        return current_event.nanos;
    }

    public static boolean isRepeatEvent() {
        return current_event.repeat;
    }

    private static int register(String name, int lwjglCode) {
        keyNames[lwjglCode] = name;
        keyMap.put(name, lwjglCode);
        return lwjglCode;
    }

    private static final class KeyEvent {
        private int character;
        private int key;
        private boolean state;
        private long nanos;
        private boolean repeat;

        private void reset() {
            character = 0;
            key = 0;
            state = false;
            repeat = false;
        }
    }
}
