package org.lwjgl.util.glu;

import static org.lwjgl.opengl.ARBImaging.GL_TABLE_TOO_LARGE;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGR;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL30.GL_INVALID_FRAMEBUFFER_OPERATION;

public class Util {
    protected static int compPerPix(int format) {
        return switch (format) {
            case GL_COLOR_INDEX, GL_STENCIL_INDEX, GL_DEPTH_COMPONENT, GL_RED, GL_GREEN, GL_BLUE, GL_ALPHA, GL_LUMINANCE -> 1;
            case GL_LUMINANCE_ALPHA -> 2;
            case GL_RGB, GL_BGR -> 3;
            case GL_RGBA, GL_BGRA -> 4;
            default -> -1;
        };
    }

    protected static int nearestPower(int value) {
        int i;

        i = 1;

        if (0 == value) return -1;

        for (; ; ) {
            switch (value) {
                case 1 -> {
                    return i;
                }
                case 3 -> {
                    return i << 2;
                }
            }

            value >>= 1;
            i <<= 1;
        }
    }

    protected static int bytesPerPixel(int format, int type) {
        int n = switch (format) {
            case GL_COLOR_INDEX, GL_STENCIL_INDEX, GL_DEPTH_COMPONENT, GL_RED, GL_GREEN, GL_BLUE, GL_ALPHA, GL_LUMINANCE -> 1;
            case GL_LUMINANCE_ALPHA -> 2;
            case GL_RGB, GL_BGR -> 3;
            case GL_RGBA, GL_BGRA -> 4;
            default -> 0;
        };

        int m = switch (type) {
            case GL_UNSIGNED_BYTE, GL_BYTE, GL_BITMAP -> 1;
            case GL_UNSIGNED_SHORT, GL_SHORT -> 2;
            case GL_UNSIGNED_INT, GL_FLOAT, GL_INT -> 4;
            default -> 0;
        };

        return n * m;
    }

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