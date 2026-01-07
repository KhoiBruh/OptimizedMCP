package net.optifine.shaders.uniform;

import org.lwjgl.opengl.ARBShaderObjects;

public class ShaderUniform3f extends ShaderUniformBase {
    private float[][] programValues;

    public ShaderUniform3f(String name) {
        super(name);
        resetValue();
    }

    public void setValue(float v0, float v1, float v2) {
        int i = getProgram();
        float[] afloat = programValues[i];

        if (afloat[0] != v0 || afloat[1] != v1 || afloat[2] != v2) {
            afloat[0] = v0;
            afloat[1] = v1;
            afloat[2] = v2;
            int j = getLocation();

            if (j >= 0) {
                ARBShaderObjects.glUniform3fARB(j, v0, v1, v2);
                checkGLError();
            }
        }
    }

    public float[] getValue() {
        int i = getProgram();
        return programValues[i];
    }

    protected void onProgramSet(int program) {
        if (program >= programValues.length) {
            float[][] afloat = programValues;
            float[][] afloat1 = new float[program + 10][];
            System.arraycopy(afloat, 0, afloat1, 0, afloat.length);
            programValues = afloat1;
        }

        if (programValues[program] == null) {
            programValues[program] = new float[]{-3.4028235E38F, -3.4028235E38F, -3.4028235E38F};
        }
    }

    protected void resetValue() {
        programValues = new float[][]{{-3.4028235E38F, -3.4028235E38F, -3.4028235E38F}};
    }
}
