package org.lwjgl.input.glfw;

import org.lwjgl.input.IKeyboard;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EventQueue;

import java.nio.ByteBuffer;

public class GLFWKeyboard implements IKeyboard {
    private static final int[] KEYS = new int[Keyboard.KEYBOARD_SIZE];

    static {
        KEYS[0x00] = Keyboard.KEY_NONE;
        KEYS[GLFW.GLFW_KEY_SPACE] = Keyboard.KEY_SPACE;
        KEYS[GLFW.GLFW_KEY_APOSTROPHE] = Keyboard.KEY_APOSTROPHE;
        KEYS[GLFW.GLFW_KEY_COMMA] = Keyboard.KEY_COMMA;
        KEYS[GLFW.GLFW_KEY_MINUS] = Keyboard.KEY_MINUS;
        KEYS[GLFW.GLFW_KEY_PERIOD] = Keyboard.KEY_PERIOD;
        KEYS[GLFW.GLFW_KEY_SLASH] = Keyboard.KEY_SLASH;
        KEYS[GLFW.GLFW_KEY_0] = Keyboard.KEY_0;
        KEYS[GLFW.GLFW_KEY_1] = Keyboard.KEY_1;
        KEYS[GLFW.GLFW_KEY_2] = Keyboard.KEY_2;
        KEYS[GLFW.GLFW_KEY_3] = Keyboard.KEY_3;
        KEYS[GLFW.GLFW_KEY_4] = Keyboard.KEY_4;
        KEYS[GLFW.GLFW_KEY_5] = Keyboard.KEY_5;
        KEYS[GLFW.GLFW_KEY_6] = Keyboard.KEY_6;
        KEYS[GLFW.GLFW_KEY_7] = Keyboard.KEY_7;
        KEYS[GLFW.GLFW_KEY_8] = Keyboard.KEY_8;
        KEYS[GLFW.GLFW_KEY_9] = Keyboard.KEY_9;
        KEYS[GLFW.GLFW_KEY_SEMICOLON] = Keyboard.KEY_SEMICOLON;
        KEYS[GLFW.GLFW_KEY_EQUAL] = Keyboard.KEY_EQUALS;
        KEYS[GLFW.GLFW_KEY_A] = Keyboard.KEY_A;
        KEYS[GLFW.GLFW_KEY_B] = Keyboard.KEY_B;
        KEYS[GLFW.GLFW_KEY_C] = Keyboard.KEY_C;
        KEYS[GLFW.GLFW_KEY_D] = Keyboard.KEY_D;
        KEYS[GLFW.GLFW_KEY_E] = Keyboard.KEY_E;
        KEYS[GLFW.GLFW_KEY_F] = Keyboard.KEY_F;
        KEYS[GLFW.GLFW_KEY_G] = Keyboard.KEY_G;
        KEYS[GLFW.GLFW_KEY_H] = Keyboard.KEY_H;
        KEYS[GLFW.GLFW_KEY_I] = Keyboard.KEY_I;
        KEYS[GLFW.GLFW_KEY_J] = Keyboard.KEY_J;
        KEYS[GLFW.GLFW_KEY_K] = Keyboard.KEY_K;
        KEYS[GLFW.GLFW_KEY_L] = Keyboard.KEY_L;
        KEYS[GLFW.GLFW_KEY_M] = Keyboard.KEY_M;
        KEYS[GLFW.GLFW_KEY_N] = Keyboard.KEY_N;
        KEYS[GLFW.GLFW_KEY_O] = Keyboard.KEY_O;
        KEYS[GLFW.GLFW_KEY_P] = Keyboard.KEY_P;
        KEYS[GLFW.GLFW_KEY_Q] = Keyboard.KEY_Q;
        KEYS[GLFW.GLFW_KEY_R] = Keyboard.KEY_R;
        KEYS[GLFW.GLFW_KEY_S] = Keyboard.KEY_S;
        KEYS[GLFW.GLFW_KEY_T] = Keyboard.KEY_T;
        KEYS[GLFW.GLFW_KEY_U] = Keyboard.KEY_U;
        KEYS[GLFW.GLFW_KEY_V] = Keyboard.KEY_V;
        KEYS[GLFW.GLFW_KEY_W] = Keyboard.KEY_W;
        KEYS[GLFW.GLFW_KEY_X] = Keyboard.KEY_X;
        KEYS[GLFW.GLFW_KEY_Y] = Keyboard.KEY_Y;
        KEYS[GLFW.GLFW_KEY_Z] = Keyboard.KEY_Z;
        KEYS[GLFW.GLFW_KEY_LEFT_BRACKET] = Keyboard.KEY_LBRACKET;
        KEYS[GLFW.GLFW_KEY_BACKSLASH] = Keyboard.KEY_BACKSLASH;
        KEYS[GLFW.GLFW_KEY_RIGHT_BRACKET] = Keyboard.KEY_RBRACKET;
        KEYS[GLFW.GLFW_KEY_GRAVE_ACCENT] = Keyboard.KEY_GRAVE;
        KEYS[GLFW.GLFW_KEY_WORLD_1] = Keyboard.KEY_WORLD_1;
        KEYS[GLFW.GLFW_KEY_WORLD_2] = Keyboard.KEY_WORLD_2;
        KEYS[GLFW.GLFW_KEY_ESCAPE] = Keyboard.KEY_ESCAPE;
        KEYS[GLFW.GLFW_KEY_ENTER] = Keyboard.KEY_RETURN;
        KEYS[GLFW.GLFW_KEY_TAB] = Keyboard.KEY_TAB;
        KEYS[GLFW.GLFW_KEY_BACKSPACE] = Keyboard.KEY_BACK;
        KEYS[GLFW.GLFW_KEY_INSERT] = Keyboard.KEY_INSERT;
        KEYS[GLFW.GLFW_KEY_DELETE] = Keyboard.KEY_DELETE;
        KEYS[GLFW.GLFW_KEY_RIGHT] = Keyboard.KEY_RIGHT;
        KEYS[GLFW.GLFW_KEY_LEFT] = Keyboard.KEY_LEFT;
        KEYS[GLFW.GLFW_KEY_DOWN] = Keyboard.KEY_DOWN;
        KEYS[GLFW.GLFW_KEY_UP] = Keyboard.KEY_UP;
        KEYS[GLFW.GLFW_KEY_PAGE_UP] = Keyboard.KEY_PRIOR;
        KEYS[GLFW.GLFW_KEY_PAGE_DOWN] = Keyboard.KEY_NEXT;
        KEYS[GLFW.GLFW_KEY_HOME] = Keyboard.KEY_HOME;
        KEYS[GLFW.GLFW_KEY_END] = Keyboard.KEY_END;
        KEYS[GLFW.GLFW_KEY_CAPS_LOCK] = Keyboard.KEY_CAPITAL;
        KEYS[GLFW.GLFW_KEY_SCROLL_LOCK] = Keyboard.KEY_SCROLL;
        KEYS[GLFW.GLFW_KEY_NUM_LOCK] = Keyboard.KEY_NUMLOCK;
        KEYS[GLFW.GLFW_KEY_PRINT_SCREEN] = Keyboard.KEY_PRINT_SCREEN;
        KEYS[GLFW.GLFW_KEY_PAUSE] = Keyboard.KEY_PAUSE;
        KEYS[GLFW.GLFW_KEY_F1] = Keyboard.KEY_F1;
        KEYS[GLFW.GLFW_KEY_F2] = Keyboard.KEY_F2;
        KEYS[GLFW.GLFW_KEY_F3] = Keyboard.KEY_F3;
        KEYS[GLFW.GLFW_KEY_F4] = Keyboard.KEY_F4;
        KEYS[GLFW.GLFW_KEY_F5] = Keyboard.KEY_F5;
        KEYS[GLFW.GLFW_KEY_F6] = Keyboard.KEY_F6;
        KEYS[GLFW.GLFW_KEY_F7] = Keyboard.KEY_F7;
        KEYS[GLFW.GLFW_KEY_F8] = Keyboard.KEY_F8;
        KEYS[GLFW.GLFW_KEY_F9] = Keyboard.KEY_F9;
        KEYS[GLFW.GLFW_KEY_F10] = Keyboard.KEY_F10;
        KEYS[GLFW.GLFW_KEY_F11] = Keyboard.KEY_F11;
        KEYS[GLFW.GLFW_KEY_F12] = Keyboard.KEY_F12;
        KEYS[GLFW.GLFW_KEY_F13] = Keyboard.KEY_F13;
        KEYS[GLFW.GLFW_KEY_F14] = Keyboard.KEY_F14;
        KEYS[GLFW.GLFW_KEY_F15] = Keyboard.KEY_F15;
        KEYS[GLFW.GLFW_KEY_F16] = Keyboard.KEY_F16;
        KEYS[GLFW.GLFW_KEY_F17] = Keyboard.KEY_F17;
        KEYS[GLFW.GLFW_KEY_F18] = Keyboard.KEY_F18;
        KEYS[GLFW.GLFW_KEY_F19] = Keyboard.KEY_F19;
        KEYS[GLFW.GLFW_KEY_F20] = Keyboard.KEY_F20;
        KEYS[GLFW.GLFW_KEY_F21] = Keyboard.KEY_F21;
        KEYS[GLFW.GLFW_KEY_F22] = Keyboard.KEY_F22;
        KEYS[GLFW.GLFW_KEY_F23] = Keyboard.KEY_F23;
        KEYS[GLFW.GLFW_KEY_F24] = Keyboard.KEY_F24;
        KEYS[GLFW.GLFW_KEY_F25] = Keyboard.KEY_F25;
        KEYS[GLFW.GLFW_KEY_KP_0] = Keyboard.KEY_NUMPAD0;
        KEYS[GLFW.GLFW_KEY_KP_1] = Keyboard.KEY_NUMPAD1;
        KEYS[GLFW.GLFW_KEY_KP_2] = Keyboard.KEY_NUMPAD2;
        KEYS[GLFW.GLFW_KEY_KP_3] = Keyboard.KEY_NUMPAD3;
        KEYS[GLFW.GLFW_KEY_KP_4] = Keyboard.KEY_NUMPAD4;
        KEYS[GLFW.GLFW_KEY_KP_5] = Keyboard.KEY_NUMPAD5;
        KEYS[GLFW.GLFW_KEY_KP_6] = Keyboard.KEY_NUMPAD6;
        KEYS[GLFW.GLFW_KEY_KP_7] = Keyboard.KEY_NUMPAD7;
        KEYS[GLFW.GLFW_KEY_KP_8] = Keyboard.KEY_NUMPAD8;
        KEYS[GLFW.GLFW_KEY_KP_9] = Keyboard.KEY_NUMPAD9;
        KEYS[GLFW.GLFW_KEY_KP_DECIMAL] = Keyboard.KEY_DECIMAL;
        KEYS[GLFW.GLFW_KEY_KP_DIVIDE] = Keyboard.KEY_DIVIDE;
        KEYS[GLFW.GLFW_KEY_KP_MULTIPLY] = Keyboard.KEY_MULTIPLY;
        KEYS[GLFW.GLFW_KEY_KP_SUBTRACT] = Keyboard.KEY_SUBTRACT;
        KEYS[GLFW.GLFW_KEY_KP_ADD] = Keyboard.KEY_ADD;
        KEYS[GLFW.GLFW_KEY_KP_ENTER] = Keyboard.KEY_NUMPADENTER;
        KEYS[GLFW.GLFW_KEY_KP_EQUAL] = Keyboard.KEY_NUMPADEQUALS;
        KEYS[GLFW.GLFW_KEY_LEFT_SHIFT] = Keyboard.KEY_LSHIFT;
        KEYS[GLFW.GLFW_KEY_LEFT_CONTROL] = Keyboard.KEY_LCONTROL;
        KEYS[GLFW.GLFW_KEY_LEFT_ALT] = Keyboard.KEY_LMENU;
        KEYS[GLFW.GLFW_KEY_LEFT_SUPER] = Keyboard.KEY_LMETA;
        KEYS[GLFW.GLFW_KEY_RIGHT_SHIFT] = Keyboard.KEY_RSHIFT;
        KEYS[GLFW.GLFW_KEY_RIGHT_CONTROL] = Keyboard.KEY_RCONTROL;
        KEYS[GLFW.GLFW_KEY_RIGHT_ALT] = Keyboard.KEY_RMENU;
        KEYS[GLFW.GLFW_KEY_RIGHT_SUPER] = Keyboard.KEY_RMETA;
        KEYS[GLFW.GLFW_KEY_MENU] = Keyboard.KEY_MENU;
    }

    private final byte[] keyDown = new byte[Keyboard.KEYBOARD_SIZE];
    private final EventQueue eventQueue = new EventQueue(Keyboard.EVENT_SIZE);
    private final ByteBuffer tempEvent = ByteBuffer.allocate(Keyboard.EVENT_SIZE);
    private GLFWKeyCallback keyCallback;
    private GLFWCharCallback charCallback;

    public static int translateKeyFromGLFW(int key) {
        if (key == -1) {
            return KEYS[0];
        } else if (key < KEYS.length) {
            return KEYS[key];
        } else return key;
    }

    @Override
    public void createKeyboard() {

        keyCallback = GLFWKeyCallback.create((window, glfwKey, scancode, action, mods) -> {

            int key = translateKeyFromGLFW(glfwKey);

            switch (action) {
                case GLFW.GLFW_PRESS -> keyDown[key] = 1;
                case GLFW.GLFW_RELEASE -> keyDown[key] = 0;
            }

            putKeyboardEvent(key, keyDown[key], 0, System.nanoTime(), GLFW.GLFW_REPEAT == action);
        });

        charCallback = GLFWCharCallback.create((window, codepoint) ->
                putKeyboardEvent(-1, (byte) 1, codepoint, System.nanoTime(), false)
        );

        long windowHandle = Display.getHandle();
        GLFW.glfwSetKeyCallback(windowHandle, keyCallback);
        GLFW.glfwSetCharCallback(windowHandle, charCallback);
    }

    private void putKeyboardEvent(int keycode, byte state, int ch, long nanos, boolean repeat) {
        tempEvent.clear();
        tempEvent.putInt(keycode).put(state).putInt(ch).putLong(nanos).put(repeat ? (byte) 1 : (byte) 0);
        tempEvent.flip();
        eventQueue.putEvent(tempEvent);
    }

    @Override
    public void destroyKeyboard() {
        keyCallback.free();
        charCallback.free();
    }

    @Override
    public void pollKeyboard(ByteBuffer keyDown) {
        int oldPos = keyDown.position();
        keyDown.put(this.keyDown);
        keyDown.position(oldPos);
    }

    @Override
    public void readKeyboard(ByteBuffer readBuffer) {
        eventQueue.copyEvents(readBuffer);
    }
}
