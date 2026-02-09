package net.optifine.shaders;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.nio.FloatBuffer;
import java.util.Arrays;

public class SMath {
    static void multiplyMat4xMat4(float[] matOut, float[] matA, float[] matB) {
        Matrix4f a = new Matrix4f().set(matA);
        Matrix4f b = new Matrix4f().set(matB);
        a.mul(b).get(matOut);
    }

    static void multiplyMat4xVec4(float[] vecOut, float[] matA, float[] vecB) {
        Matrix4f mat = new Matrix4f().set(matA);
        Vector4f vec = new Vector4f(vecB[0], vecB[1], vecB[2], vecB[3]);
        mat.transform(vec);
        vecOut[0] = vec.x;
        vecOut[1] = vec.y;
        vecOut[2] = vec.z;
        vecOut[3] = vec.w;
    }

    static void invertMat4(float[] matOut, float[] m) {
        Matrix4f mat = new Matrix4f().set(m);
        mat.invert().get(matOut);
    }

    static void invertMat4FBFA(FloatBuffer fbInvOut, FloatBuffer fbMatIn, float[] faInv, float[] faMat) {
        fbMatIn.get(faMat);
        invertMat4(faInv, faMat);
        fbInvOut.put(faInv);
    }
}
