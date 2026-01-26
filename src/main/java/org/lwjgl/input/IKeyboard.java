package org.lwjgl.input;

import java.nio.ByteBuffer;

public interface IKeyboard {
    void createKeyboard();
    void destroyKeyboard();
    void pollKeyboard(ByteBuffer keyDown);
    void readKeyboard(ByteBuffer readBuffer);
}
