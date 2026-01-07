package net.optifine.shaders.uniform;

import net.optifine.shaders.Shaders;
import org.lwjgl.opengl.ARBShaderObjects;

import java.util.Arrays;

public abstract class ShaderUniformBase {
    private final String name;
    private int program = 0;
    private int[] locations = new int[]{-1};

    public ShaderUniformBase(String name) {
        this.name = name;
    }

    private void expandLocations() {
        if (program >= locations.length) {
            int[] aint = new int[program * 2];
            Arrays.fill(aint, Integer.MIN_VALUE);
            System.arraycopy(locations, 0, aint, 0, locations.length);
            locations = aint;
        }
    }

    protected abstract void onProgramSet(int var1);

    public String getName() {
        return name;
    }

    public int getProgram() {
        return program;
    }

    public void setProgram(int program) {
        if (this.program != program) {
            this.program = program;
            expandLocations();
            onProgramSet(program);
        }
    }

    public int getLocation() {
        if (program <= 0) {
            return -1;
        } else {
            int i = locations[program];

            if (i == Integer.MIN_VALUE) {
                i = ARBShaderObjects.glGetUniformLocationARB(program, name);
                locations[program] = i;
            }

            return i;
        }
    }

    public boolean isDefined() {
        return getLocation() >= 0;
    }

    public void disable() {
        locations[program] = -1;
    }

    public void reset() {
        program = 0;
        locations = new int[]{-1};
        resetValue();
    }

    protected abstract void resetValue();

    protected void checkGLError() {
        if (Shaders.checkGLError(name) != 0) {
            disable();
        }
    }

    public String toString() {
        return name;
    }
}
