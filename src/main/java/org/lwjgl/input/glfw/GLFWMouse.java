package org.lwjgl.input.glfw;

import org.lwjgl.input.IMouse;
import org.lwjgl.glfw.*;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EventQueue;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class GLFWMouse implements IMouse {
    private final EventQueue eventQueue = new EventQueue(Mouse.EVENT_SIZE);
    private final ByteBuffer tempEvent = ByteBuffer.allocate(Mouse.EVENT_SIZE);

    private GLFWMouseButtonCallback buttonCallback;
    private GLFWCursorPosCallback posCallback;
    private GLFWScrollCallback scrollCallback;
    private GLFWCursorEnterCallback cursorEnterCallback;

    private long windowHandle;
    private boolean grabbed;
    private boolean isInsideWindow;

    private int lastX;
    private int lastY;
    private int accumDx;
    private int accumDy;
    private int accumDz;

    private final byte[] buttonStates = new byte[getButtonCount()];
    private boolean firstMove = true;

    @Override
    public void createMouse() {
        windowHandle = Display.getHandle();

        buttonCallback = GLFWMouseButtonCallback.create((window, button, action, mods) -> {
            byte state = GLFW.GLFW_PRESS == action ? (byte) 1 : (byte) 0;
            putMouseEvent((byte) button, state, 0, System.nanoTime());
            if (button < buttonStates.length) buttonStates[button] = state;
        });

        posCallback = GLFWCursorPosCallback.create((window, posX, posY) -> {
            int x = (int) posX;
            int y = Display.getHeight() - 1 - (int) posY;
            int dx = x - lastX;
            int dy = y - lastY;

            if (firstMove) {
                firstMove = false;
                dx = dy = 0;
                lastX = x;
                lastY = y;
            }

            if (0 != dx || 0 != dy) {
                accumDx += dx;
                accumDy += dy;
                lastX = x;
                lastY = y;
                long nanos = System.nanoTime();

                if (grabbed) {
                    putMouseEventWithCoords((byte) -1, (byte) 0, dx, dy, 0, nanos);
                } else {
                    putMouseEventWithCoords((byte) -1, (byte) 0, x, y, 0, nanos);
                }
            }
        });

        scrollCallback = GLFWScrollCallback.create((window, offsetX, offsetY) -> {
            accumDz += (int) offsetY;
            putMouseEvent((byte) -1, (byte) 0, (int) offsetY, System.nanoTime());
        });

        cursorEnterCallback = GLFWCursorEnterCallback.create((window, entered) ->
                isInsideWindow = entered
        );

        GLFW.glfwSetMouseButtonCallback(windowHandle, buttonCallback);
        GLFW.glfwSetCursorPosCallback(windowHandle, posCallback);
        GLFW.glfwSetScrollCallback(windowHandle, scrollCallback);
        GLFW.glfwSetCursorEnterCallback(windowHandle, cursorEnterCallback);
    }

    private void putMouseEvent(byte button, byte state, int dz, long nanos) {
        if (grabbed) putMouseEventWithCoords(button, state, 0, 0, dz, nanos);
        else putMouseEventWithCoords(button, state, lastX, lastY, dz, nanos);
    }

    private void putMouseEventWithCoords(byte button, byte state, int coord1, int coord2, int dz, long nanos) {
        tempEvent.clear();
        tempEvent.put(button).put(state).putInt(coord1).putInt(coord2).putInt(dz).putLong(nanos);
        tempEvent.flip();
        eventQueue.putEvent(tempEvent);
    }

    @Override
    public void destroyMouse() {
        buttonCallback.free();
        posCallback.free();
        scrollCallback.free();
        cursorEnterCallback.free();
    }

    private void reset() {
        eventQueue.clearEvents();
        accumDx = accumDy = 0;
    }

    @Override
    public void pollMouse(IntBuffer pos, ByteBuffer buttons) {
        if (grabbed) {
            pos.put(0, accumDx);
            pos.put(1, accumDy);
        } else {
            pos.put(0, lastX);
            pos.put(1, lastY);
        }

        pos.put(2, accumDz);
        accumDx = accumDy = accumDz = 0;
        for (int i = 0; i < buttonStates.length; i++) {
            buttons.put(i, buttonStates[i]);
        }
    }

    @Override
    public void readMouse(ByteBuffer readBuffer) {
        eventQueue.copyEvents(readBuffer);
    }

    @Override
    public void setCursorPosition(int x, int y) {
        lastX = x;
        lastY = y;
        int mode = GLFW.glfwGetInputMode(windowHandle, GLFW.GLFW_CURSOR);

        GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        GLFW.glfwSetCursorPos(windowHandle, x, y);
        GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, mode);
    }

    @Override
    public void grabMouse(boolean grab) {
        GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, grab ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
        grabbed = grab;
        reset();
    }

    @Override
    public boolean hasWheel() {
        return true;
    }

    @Override
    public int getButtonCount() {
        return GLFW.GLFW_MOUSE_BUTTON_LAST + 1;
    }

    @Override
    public boolean isInsideWindow() {
        return isInsideWindow;
    }

}
