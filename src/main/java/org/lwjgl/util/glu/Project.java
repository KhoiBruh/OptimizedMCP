package org.lwjgl.util.glu;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Project extends Util {
    private static final Matrix4f matrix = new Matrix4f();
    private static final Matrix4f finalMatrix = new Matrix4f();
    private static final Matrix4f tempMatrix = new Matrix4f();

    private static final Vector3f tempVec = new Vector3f();

    private static final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    private static final int[] viewportArray = new int[4];

    public static void gluPerspective(float fovy, float aspect, float zNear, float zFar) {
        float radians = (float) Math.toRadians(fovy);

        matrix.setPerspective(radians, aspect, zNear, zFar);

        matrixBuffer.clear();
        matrix.get(matrixBuffer);
        glMultMatrixf(matrixBuffer);
    }

    public static void gluLookAt(
            float eyex,
            float eyey,
            float eyez,
            float centerx,
            float centery,
            float centerz,
            float upx,
            float upy,
            float upz
    ) {
        matrix.identity();
        matrix.lookAt(eyex, eyey, eyez, centerx, centery, centerz, upx, upy, upz);

        matrixBuffer.clear();
        matrix.get(matrixBuffer);
        glMultMatrixf(matrixBuffer);
    }

    public static boolean gluProject(
            float objx,
            float objy,
            float objz,
            FloatBuffer modelMatrix,
            FloatBuffer projMatrix,
            IntBuffer viewport,
            FloatBuffer win_pos
    ) {
        tempMatrix.set(modelMatrix);
        finalMatrix.set(projMatrix).mul(tempMatrix);

        int vp = viewport.position();
        viewportArray[0] = viewport.get(vp);
        viewportArray[1] = viewport.get(vp + 1);
        viewportArray[2] = viewport.get(vp + 2);
        viewportArray[3] = viewport.get(vp + 3);

        tempVec.set(objx, objy, objz);
        finalMatrix.project(tempVec, viewportArray, tempVec);

        if (!Float.isFinite(tempVec.x) || !Float.isFinite(tempVec.y)) return false;

        win_pos.put(0, tempVec.x);
        win_pos.put(1, tempVec.y);
        win_pos.put(2, tempVec.z);

        return true;
    }

    public static void gluUnProject(
            float winx,
            float winy,
            float winz,
            FloatBuffer modelMatrix,
            FloatBuffer projMatrix,
            IntBuffer viewport,
            FloatBuffer obj_pos
    ) {
        tempMatrix.set(modelMatrix);
        finalMatrix.set(projMatrix).mul(tempMatrix);

        int vp = viewport.position();
        viewportArray[0] = viewport.get(vp);
        viewportArray[1] = viewport.get(vp + 1);
        viewportArray[2] = viewport.get(vp + 2);
        viewportArray[3] = viewport.get(vp + 3);

        tempVec.set(winx, winy, winz);
        finalMatrix.unproject(tempVec, viewportArray, tempVec);

        if (!Float.isFinite(tempVec.x)) return;

        obj_pos.put(obj_pos.position(), tempVec.x);
        obj_pos.put(obj_pos.position() + 1, tempVec.y);
        obj_pos.put(obj_pos.position() + 2, tempVec.z);
    }
}