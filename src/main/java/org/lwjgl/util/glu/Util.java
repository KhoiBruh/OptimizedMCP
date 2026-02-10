package org.lwjgl.util.glu;

import static org.lwjgl.opengl.ARBImaging.GL_TABLE_TOO_LARGE;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGR;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL30.GL_INVALID_FRAMEBUFFER_OPERATION;

public class Util {
    public static String translateGLErrorString(int errorCode) {
        return switch (errorCode) {
            case GL_NO_ERROR -> "No error";
            case GL_INVALID_ENUM -> "Invalid enum";
            case GL_INVALID_VALUE -> "Invalid value";
            case GL_INVALID_OPERATION -> "Invalid operation";
            case GL_STACK_OVERFLOW -> "Stack overflow";
            case GL_STACK_UNDERFLOW -> "Stack underflow";
            case GL_OUT_OF_MEMORY -> "Out of memory";
            case GL_TABLE_TOO_LARGE -> "Table too large";
            case GL_INVALID_FRAMEBUFFER_OPERATION -> "Invalid framebuffer operation";
            default -> null;
        };
    }
}