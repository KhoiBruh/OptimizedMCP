package org.lwjgl.util.glu;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Project extends Util {

    private static final float[] IDENTITY_MATRIX = new float[]{
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
    };

    private static final FloatBuffer matrix = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer finalMatrix = BufferUtils.createFloatBuffer(16);

    private static final FloatBuffer tempMatrix = BufferUtils.createFloatBuffer(16);
    private static final float[] in = new float[4];
    private static final float[] out = new float[4];

    private static final float[] forward = new float[3];
    private static final float[] side = new float[3];
    private static final float[] up = new float[3];

    private static void __gluMakeIdentityf(FloatBuffer m) {
        int oldPos = m.position();
        m.put(IDENTITY_MATRIX);
        m.position(oldPos);
    }

    private static void __gluMultMatrixVecf(FloatBuffer finalMatrix, float[] in, float[] out) {
        for (int i = 0; 4 > i; i++) {
            out[i] =
                    in[0] * finalMatrix.get(finalMatrix.position() + i)
                            + in[1] * finalMatrix.get(finalMatrix.position() + 4 + i)
                            + in[2] * finalMatrix.get(finalMatrix.position() + 2 * 4 + i)
                            + in[3] * finalMatrix.get(finalMatrix.position() + 3 * 4 + i);

        }
    }

    private static boolean __gluInvertMatrixf(FloatBuffer src, FloatBuffer inverse) {
        int i, j, k, swap;
        float t;
        FloatBuffer temp = tempMatrix;


        for (i = 0; 16 > i; i++) {
            temp.put(i, src.get(i + src.position()));
        }
        __gluMakeIdentityf(inverse);

        for (i = 0; 4 > i; i++) {
            swap = i;
            for (j = i + 1; 4 > j; j++) {
                if (Math.abs(temp.get(j * 4 + i)) > Math.abs(temp.get(i * 4 + i))) {
                    swap = j;
                }
            }

            if (swap != i) {
                for (k = 0; 4 > k; k++) {
                    t = temp.get(i * 4 + k);
                    temp.put(i * 4 + k, temp.get(swap * 4 + k));
                    temp.put(swap * 4 + k, t);

                    t = inverse.get(i * 4 + k);
                    inverse.put(i * 4 + k, inverse.get(swap * 4 + k));
                    inverse.put(swap * 4 + k, t);
                }
            }

            if (0 == temp.get(i * 4 + i)) return false;

            t = temp.get(i * 4 + i);
            for (k = 0; 4 > k; k++) {
                temp.put(i * 4 + k, temp.get(i * 4 + k) / t);
                inverse.put(i * 4 + k, inverse.get(i * 4 + k) / t);
            }
            for (j = 0; 4 > j; j++) {
                if (j != i) {
                    t = temp.get(j * 4 + i);
                    for (k = 0; 4 > k; k++) {
                        temp.put(j * 4 + k, temp.get(j * 4 + k) - temp.get(i * 4 + k) * t);
                        inverse.put(j * 4 + k, inverse.get(j * 4 + k) - inverse.get(i * 4 + k) * t);
                    }
                }
            }
        }
        return true;
    }

    private static void __gluMultMatricesf(FloatBuffer a, FloatBuffer b, FloatBuffer r) {
        for (int i = 0; 4 > i; i++) {
            for (int j = 0; 4 > j; j++) {
                r.put(r.position() + i * 4 + j,
                        a.get(a.position() + i * 4) * b.get(b.position() + j) + a.get(a.position() + i * 4 + 1) * b.get(b.position() + 4 + j) + a.get(a.position() + i * 4 + 2) * b.get(b.position() + 2 * 4 + j) + a.get(a.position() + i * 4 + 3) * b.get(b.position() + 3 * 4 + j));
            }
        }
    }

    public static void gluPerspective(float fovy, float aspect, float zNear, float zFar) {
        float sine;
        float cotangent;
        float deltaZ;
        float radians = (float) (fovy / 2 * Math.PI / 180);

        deltaZ = zFar - zNear;
        sine = (float) Math.sin(radians);

        if ((0 == deltaZ) || (0 == sine) || (0 == aspect)) {
            return;
        }

        cotangent = (float) Math.cos(radians) / sine;

        __gluMakeIdentityf(matrix);

        matrix.put(0, cotangent / aspect);
        matrix.put(4 + 1, cotangent);
        matrix.put(2 * 4 + 2, -(zFar + zNear) / deltaZ);
        matrix.put(2 * 4 + 3, -1);
        matrix.put(3 * 4 + 2, -2 * zNear * zFar / deltaZ);
        matrix.put(3 * 4 + 3, 0);

        glMultMatrixf(matrix);
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
            float upz) {
        float[] forward = Project.forward;
        float[] side = Project.side;
        float[] up = Project.up;

        forward[0] = centerx - eyex;
        forward[1] = centery - eyey;
        forward[2] = centerz - eyez;

        up[0] = upx;
        up[1] = upy;
        up[2] = upz;

        normalize(forward);

        cross(forward, up, side);
        normalize(side);

        cross(side, forward, up);

        __gluMakeIdentityf(matrix);
        matrix.put(0, side[0]);
        matrix.put(4, side[1]);
        matrix.put(2 * 4, side[2]);

        matrix.put(1, up[0]);
        matrix.put(4 + 1, up[1]);
        matrix.put(2 * 4 + 1, up[2]);

        matrix.put(2, -forward[0]);
        matrix.put(4 + 2, -forward[1]);
        matrix.put(2 * 4 + 2, -forward[2]);

        glMultMatrixf(matrix);
        glTranslatef(-eyex, -eyey, -eyez);
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

        float[] in = Project.in;
        float[] out = Project.out;

        in[0] = objx;
        in[1] = objy;
        in[2] = objz;
        in[3] = 1.0f;

        __gluMultMatrixVecf(modelMatrix, in, out);
        __gluMultMatrixVecf(projMatrix, out, in);

        if (0.0 == in[3]) return false;

        in[3] = (1.0f / in[3]) * 0.5f;

        in[0] = in[0] * in[3] + 0.5f;
        in[1] = in[1] * in[3] + 0.5f;
        in[2] = in[2] * in[3] + 0.5f;

        win_pos.put(0, in[0] * viewport.get(viewport.position() + 2) + viewport.get(viewport.position()));
        win_pos.put(1, in[1] * viewport.get(viewport.position() + 3) + viewport.get(viewport.position() + 1));
        win_pos.put(2, in[2]);

        return true;
    }

    public static boolean gluUnProject(
            float winx,
            float winy,
            float winz,
            FloatBuffer modelMatrix,
            FloatBuffer projMatrix,
            IntBuffer viewport,
            FloatBuffer obj_pos
    ) {
        float[] in = Project.in;
        float[] out = Project.out;

        __gluMultMatricesf(modelMatrix, projMatrix, finalMatrix);

        if (!__gluInvertMatrixf(finalMatrix, finalMatrix)) return false;

        in[0] = winx;
        in[1] = winy;
        in[2] = winz;
        in[3] = 1.0f;

        in[0] = (in[0] - viewport.get(viewport.position())) / viewport.get(viewport.position() + 2);
        in[1] = (in[1] - viewport.get(viewport.position() + 1)) / viewport.get(viewport.position() + 3);

        in[0] = in[0] * 2 - 1;
        in[1] = in[1] * 2 - 1;
        in[2] = in[2] * 2 - 1;

        __gluMultMatrixVecf(finalMatrix, in, out);

        if (0.0 == out[3]) return false;

        out[3] = 1.0f / out[3];

        obj_pos.put(obj_pos.position(), out[0] * out[3]);
        obj_pos.put(obj_pos.position() + 1, out[1] * out[3]);
        obj_pos.put(obj_pos.position() + 2, out[2] * out[3]);

        return true;
    }

    public static void gluPickMatrix(
            float x,
            float y,
            float deltaX,
            float deltaY,
            IntBuffer viewport
    ) {
        if (0 >= deltaX || 0 >= deltaY) return;

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