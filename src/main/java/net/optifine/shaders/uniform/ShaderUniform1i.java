package net.optifine.shaders.uniform;

import org.lwjgl.opengl.ARBShaderObjects;

public class ShaderUniform1i extends ShaderUniformBase {
    private int[] programValues;

    public ShaderUniform1i(String name) {
        super(name);
        resetValue();
    }

    public int getValue() {
        int i = getProgram();
        return programValues[i];
    }

    public void setValue(int valueNew) {
        int i = getProgram();
        int j = programValues[i];

        if (valueNew != j) {
            programValues[i] = valueNew;
            int k = getLocation();

            if (k >= 0) {
                ARBShaderObjects.glUniform1iARB(k, valueNew);
                checkGLError();
            }
        }
    }

    protected void onProgramSet(int program) {
        if (program >= programValues.length) {
            int[] aint = programValues;
            int[] aint1 = new int[program + 10];
            System.arraycopy(aint, 0, aint1, 0, aint.length);

            for (int i = aint.length; i < aint1.length; ++i) {
                aint1[i] = Integer.MIN_VALUE;
            }

            programValues = aint1;
        }
    }

    protected void resetValue() {
        programValues = new int[]{Integer.MIN_VALUE};
    }
}
