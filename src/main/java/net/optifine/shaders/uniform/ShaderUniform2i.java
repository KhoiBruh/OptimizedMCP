package net.optifine.shaders.uniform;

import org.lwjgl.opengl.ARBShaderObjects;

public class ShaderUniform2i extends ShaderUniformBase {
    private int[][] programValues;

    public ShaderUniform2i(String name) {
        super(name);
        resetValue();
    }

    public void setValue(int v0, int v1) {
        int i = getProgram();
        int[] aint = programValues[i];

        if (aint[0] != v0 || aint[1] != v1) {
            aint[0] = v0;
            aint[1] = v1;
            int j = getLocation();

            if (j >= 0) {
                ARBShaderObjects.glUniform2iARB(j, v0, v1);
                checkGLError();
            }
        }
    }

    public int[] getValue() {
        int i = getProgram();
        return programValues[i];
    }

    protected void onProgramSet(int program) {
        if (program >= programValues.length) {
            int[][] aint = programValues;
            int[][] aint1 = new int[program + 10][];
            System.arraycopy(aint, 0, aint1, 0, aint.length);
            programValues = aint1;
        }

        if (programValues[program] == null) {
            programValues[program] = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE};
        }
    }

    protected void resetValue() {
        programValues = new int[][]{{Integer.MIN_VALUE, Integer.MIN_VALUE}};
    }
}
