package org.lwjgl.input;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class CombinedInput implements Input {
    private final IKeyboard keyboard;
    private final IMouse mouse;

    public CombinedInput(IKeyboard keyboard, IMouse mouse) {
        this.keyboard = keyboard;
        this.mouse = mouse;
    }

    @Override
    public void createKeyboard() {
        keyboard.createKeyboard();
    }

    @Override
    public void destroyKeyboard() {
        keyboard.destroyKeyboard();
    }

    @Override
    public void pollKeyboard(ByteBuffer keyDown) {
        keyboard.pollKeyboard(keyDown);
    }

    @Override
    public void readKeyboard(ByteBuffer readBuffer) {
        keyboard.readKeyboard(readBuffer);
    }


    @Override
    public void createMouse() {
        mouse.createMouse();
    }

    @Override
    public void destroyMouse() {
        mouse.destroyMouse();
    }

    @Override
    public void pollMouse(IntBuffer pos, ByteBuffer buttons) {
        mouse.pollMouse(pos, buttons);
    }

    @Override
    public void readMouse(ByteBuffer readBuffer) {
        mouse.readMouse(readBuffer);
    }

    @Override
    public void setCursorPosition(int x, int y) {
        mouse.setCursorPosition(x, y);
    }

    @Override
    public void grabMouse(boolean grab) {
        mouse.grabMouse(grab);
    }

    @Override
    public boolean hasWheel() {
        return mouse.hasWheel();
    }

    @Override
    public int getButtonCount() {
        return mouse.getButtonCount();
    }

    @Override
    public boolean isInsideWindow() {
        return mouse.isInsideWindow();
    }
}
