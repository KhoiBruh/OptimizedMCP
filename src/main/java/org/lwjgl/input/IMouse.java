package org.lwjgl.input;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public interface IMouse {
    void createMouse();
    void destroyMouse();
    void pollMouse(IntBuffer pos, ByteBuffer buttons);
    void readMouse(ByteBuffer readBuffer);
    void setCursorPosition(int x, int y);
    void grabMouse(boolean grab);
    boolean hasWheel();
    int getButtonCount();
    boolean isInsideWindow();
}
