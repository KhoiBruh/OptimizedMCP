package org.lwjgl.input;

import org.lwjgl.input.glfw.GLFWKeyboard;
import org.lwjgl.input.glfw.GLFWMouse;

public class InputUtil {
    private static Input input;

    public static Input getOrCreateInput() {
        if (input == null) input = createInput();
        return input;
    }

    private static Input createInput() {
        return new CombinedInput(new GLFWKeyboard(), new GLFWMouse());
    }
}
