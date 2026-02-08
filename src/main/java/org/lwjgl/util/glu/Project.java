package org.lwjgl.util.glu;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Project extends Util {
    private static final Matrix4f matrix = new Matrix4f();
    private static final Matrix4f finalMatrix = new Matrix4f();
    private static final Matrix4f tempMatrix = new Matrix4f();

    private static final Vector4f in = new Vector4f();
    private static final Vector4f out = new Vector4f();

    private static final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    public static void gluPerspective(float fovy, float aspect, float zNear, float zFar) {
        float radians = (float) (fovy / 2 * Math.PI / 180);
        float deltaZ = zFar - zNear;
        float sine = (float) Math.sin(radians);

        if (deltaZ == 0 || sine == 0 || aspect == 0) return;

        float cotangent = (float) Math.cos(radians) / sine;

        matrix.identity();

        matrix.m00(cotangent / aspect);
        matrix.m11(cotangent);
        matrix.m22(-(zFar + zNear) / deltaZ);

        matrix.m23(-1);
        matrix.m32(-2 * zNear * zFar / deltaZ);
        matrix.m33(0);

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
        matrix.lookAt(
                eyex, eyey, eyez,
                centerx, centery, centerz,
                upx, upy, upz
        );

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


        in.set(objx, objy, objz, 1F);

        tempMatrix.set(modelMatrix);
        tempMatrix.transform(in, out);

        tempMatrix.set(projMatrix);
        tempMatrix.transform(out, in);

        if (in.w == 0) return false;

        float w = (1 / in.w) * 0.5f;

        in.x = in.x * w + 0.5f;
        in.y = in.y * w + 0.5f;
        in.z = in.z * w + 0.5f;

        win_pos.put(0, in.x * viewport.get(viewport.position() + 2) + viewport.get(viewport.position()));
        win_pos.put(1, in.y * viewport.get(viewport.position() + 3) + viewport.get(viewport.position() + 1));
        win_pos.put(2, in.z);

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
        finalMatrix.set(projMatrix);
        finalMatrix.mul(tempMatrix);

        in.x = (winx - viewport.get(viewport.position())) / viewport.get(viewport.position() + 2);
        in.y = (winy - viewport.get(viewport.position() + 1)) / viewport.get(viewport.position() + 3);
        in.z = winz;
        in.w = 1;

        in.x = in.x * 2 - 1;
        in.y = in.y * 2 - 1;
        in.z = in.z * 2 - 1;

        finalMatrix.transform(in, out);

        if (out.w == 0) return;

        float w = 1 / out.w;

        obj_pos.put(obj_pos.position(), out.x * w);
        obj_pos.put(obj_pos.position() + 1, out.y * w);
        obj_pos.put(obj_pos.position() + 2, out.z * w);

    }

    public static void gluPickMatrix(
            float x,
            float y,
            float deltaX,
            float deltaY,
            IntBuffer viewport
    ) {
        if (deltaX <= 0 || deltaY <= 0) return;

        glTranslatef(
                (viewport.get(viewport.position() + 2) - 2 * (x - viewport.get(viewport.position()))) / deltaX,
                (viewport.get(viewport.position() + 3) - 2 * (y - viewport.get(viewport.position() + 1))) / deltaY,
                0
        );

        glScalef(
                viewport.get(viewport.position() + 2) / deltaX,
                viewport.get(viewport.position() + 3) / deltaY,
                1.0f
        );
    }
}