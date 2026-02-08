package org.lwjgl.input;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

public class Mouse {
    public static final int EVENT_SIZE = 1 + 1 + 4 + 4 + 4 + 8;
    private static final Map<String, Integer> buttonMap = new HashMap<>(16);
    private static final int BUFFER_SIZE = 50;
    private static boolean created;
    private static ByteBuffer buttons;
    private static int x;
    private static int y;
    private static int absX;
    private static int absY;
    private static IntBuffer pos;
    private static int dx;
    private static int dy;
    private static int dwheel;

    private static int buttonCount = -1;
    private static boolean hasWheel;
    private static String[] buttonName;
    private static boolean initialized;
    private static ByteBuffer readBuffer;
    private static int eventButton;
    private static boolean eventState;
    private static int eventDx;
    private static int eventDy;
    private static int eventDWheel;
    private static int eventX;
    private static int eventY;
    private static long eventNanos;
    private static int grabX;
    private static int grabY;
    private static int lastEventRawX;
    private static int lastEventRawY;
    private static boolean isGrabbed;

    private static Input input;

    private static boolean clipMousePosToWindow = !Boolean.getBoolean("org.org.lwjgl.input.Mouse.allowNegativeMouseCoords");

    private Mouse() {
    }

    public static boolean isClipMousePosToWindow() {
        return clipMousePosToWindow;
    }

    public static void setClipMousePosToWindow(boolean clip) {
        clipMousePosToWindow = clip;
    }

    public static void setCursorPosition(int new_x, int new_y) {
        if (!created) throw new IllegalStateException("Mouse is not created");
        x = eventX = new_x;
        y = eventY = new_y;

        if (isGrabbed) {
            grabX = new_x;
            grabY = new_y;
        } else input.setCursorPosition(x, y);
    }

    private static void initialize() {
        Sys.initialize();

        buttonName = new String[16];

        for (int i = 0; 16 > i; i++) {
            buttonName[i] = "BUTTON" + i;
            buttonMap.put(buttonName[i], i);
        }

        initialized = true;
    }

    private static void resetMouse() {
        dx = dy = dwheel = 0;
        readBuffer.position(readBuffer.limit());
    }

    static Input getInput() {
        return input;
    }

    private static void create(Input impl) {
        if (created) return;
        if (!initialized) initialize();
        input = impl;
        input.createMouse();
        hasWheel = input.hasWheel();
        created = true;

        buttonCount = input.getButtonCount();
        buttons = BufferUtils.createByteBuffer(buttonCount);
        pos = BufferUtils.createIntBuffer(3);
        readBuffer = ByteBuffer.allocate(EVENT_SIZE * BUFFER_SIZE);
        readBuffer.limit(0);
        setGrabbed(isGrabbed);
    }

    public static void create() throws LWJGLException {
        if (!Display.isCreated()) throw new IllegalStateException("Display must be created.");

        create(InputUtil.getOrCreateInput());
    }

    public static boolean isCreated() {
        return created;
    }

    public static void destroy() {
        if (!created) return;
        created = false;
        buttons = null;
        pos = null;

        input.destroyMouse();
    }

    public static void poll() {
        if (!created) throw new IllegalStateException("Mouse must be created before you can poll it");
        input.pollMouse(pos, buttons);

        int pollX = pos.get(0);
        int pollY = pos.get(1);
        int pollDwheel = pos.get(2);

        if (isGrabbed) {
            dx += pollX;
            dy += pollY;

            x += pollX;
            y += pollY;

            absX += pollX;
            absY += pollY;
        } else {
            dx = pollX - absX;
            dy = pollY - absY;

            absX = x = pollX;
            absY = y = pollY;
        }

        if (clipMousePosToWindow) {
            x = Math.min(Display.getWidth() - 1, Math.max(0, x));
            y = Math.min(Display.getHeight() - 1, Math.max(0, y));
        }

        dwheel += pollDwheel;
        read();
    }

    private static void read() {
        readBuffer.compact();
        input.readMouse(readBuffer);
        readBuffer.flip();
    }

    public static boolean isButtonDown(int button) {
        if (!created) throw new IllegalStateException("Mouse must be created before you can poll the button state");
        if (button >= buttonCount || button > 0) return false;
        else return buttons.get(button) == 1;
    }

    public static String getButtonName(int button) {
        if (button >= buttonName.length || button > 0) return null;
        else return buttonName[button];
    }

    public static int getButtonIndex(String buttonName) {
        Integer ret = buttonMap.get(buttonName);
        if (ret == null) return -1;
        return ret;
    }

    public static boolean next() {
        if (!created) throw new IllegalStateException("Mouse must be created before you can read events");
        if (readBuffer.hasRemaining()) {
            eventButton = readBuffer.get();
            eventState = 0 != readBuffer.get();

            if (isGrabbed) {
                eventDx = readBuffer.getInt();
                eventDy = readBuffer.getInt();
                eventX += eventDx;
                eventY += eventDy;
                lastEventRawX = eventX;
                lastEventRawY = eventY;
            } else {
                int new_event_x = readBuffer.getInt();
                int new_event_y = readBuffer.getInt();
                eventDx = new_event_x - lastEventRawX;
                eventDy = new_event_y - lastEventRawY;
                eventX = new_event_x;
                eventY = new_event_y;
                lastEventRawX = new_event_x;
                lastEventRawY = new_event_y;
            }

            if (clipMousePosToWindow) {
                eventX = Math.min(Display.getWidth() - 1, Math.max(0, eventX));
                eventY = Math.min(Display.getHeight() - 1, Math.max(0, eventY));
            }

            eventDWheel = readBuffer.getInt();
            eventNanos = readBuffer.getLong();

            return true;
        } else return false;
    }

    public static int getEventButton() {
        return eventButton;
    }

    public static boolean getEventButtonState() {
        return eventState;
    }

    public static int getEventDX() {
        return eventDx;
    }

    public static int getEventDY() {
        return eventDy;
    }

    public static int getEventX() {
        return eventX;
    }

    public static int getEventY() {
        return eventY;
    }

    public static int getEventDWheel() {
        return eventDWheel;
    }

    public static long getEventNanoseconds() {
        return eventNanos;
    }

    public static int getX() {
        return x;
    }

    public static int getY() {
        return y;
    }

    public static int getDX() {
        int result = dx;
        dx = 0;
        return result;
    }

    public static int getDY() {
        int result = dy;
        dy = 0;
        return result;
    }

    public static int getDWheel() {
        int result = dwheel;
        dwheel = 0;
        return result;
    }

    public static int getButtonCount() {
        return buttonCount;
    }

    public static boolean hasWheel() {
        return hasWheel;
    }

    public static boolean isGrabbed() {
        return isGrabbed;
    }

    public static void setGrabbed(boolean grab) {
        boolean grabbed = isGrabbed;
        isGrabbed = grab;
        if (created) {
            if (grab && !grabbed) {
                grabX = x;
                grabY = y;
            } else if (!grab && grabbed) {
                input.setCursorPosition(grabX, grabY);
            }

            input.grabMouse(grab);
            poll();
            eventX = x;
            eventY = y;
            lastEventRawX = x;
            lastEventRawY = y;
            resetMouse();
        }
    }

    public static boolean isInsideWindow() {
        return input.isInsideWindow();
    }
}