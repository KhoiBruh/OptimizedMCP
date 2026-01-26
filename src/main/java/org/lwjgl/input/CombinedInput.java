package org.lwjgl.input;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class CombinedInput implements Input {
    private final IKeyboard keyboardImpl;
    private final IMouse mouseImpl;

    public CombinedInput(IKeyboard keyboard, IMouse mouse) {
        keyboardImpl = keyboard;
        mouseImpl = mouse;
    }

    @Override
    public void createKeyboard() {
        keyboardImpl.createKeyboard();
    }

    @Override
    public void destroyKeyboard() {
        keyboardImpl.destroyKeyboard();
    }

    @Override
    public void pollKeyboard(ByteBuffer keyDown) {
        keyboardImpl.pollKeyboard(keyDown);
    }

    @Override
    public void readKeyboard(ByteBuffer readBuffer) {
        keyboardImpl.readKeyboard(readBuffer);
    }


    @Override
    public void createMouse() {
        mouseImpl.createMouse();
    }

    @Override
    public void destroyMouse() {
        mouseImpl.destroyMouse();
    }

    @Override
    public void pollMouse(IntBuffer pos, ByteBuffer buttons) {
        mouseImpl.pollMouse(pos, buttons);
    }

    @Override
    public void readMouse(ByteBuffer readBuffer) {
        mouseImpl.readMouse(readBuffer);
    }

    @Override
    public void setCursorPosition(int x, int y) {
        mouseImpl.setCursorPosition(x, y);
    }

    @Override
    public void grabMouse(boolean grab) {
        mouseImpl.grabMouse(grab);
    }

    @Override
    public boolean hasWheel() {
        return mouseImpl.hasWheel();
    }

    @Override
    public int getButtonCount() {
        return mouseImpl.getButtonCount();
    }

    @Override
    public boolean isInsideWindow() {
        return mouseImpl.isInsideWindow();
    }
}
