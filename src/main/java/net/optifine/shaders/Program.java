package net.optifine.shaders;

import net.optifine.render.GlAlphaState;
import net.optifine.render.GlBlendState;
import net.optifine.shaders.config.RenderScale;

import java.nio.IntBuffer;
import java.util.Arrays;

public class Program {
    private final int index;
    private final String name;
    private final ProgramStage programStage;
    private final Program programBackup;
    private final Boolean[] buffersFlip = new Boolean[8];
    private final boolean[] toggleColorTextures = new boolean[8];
    private GlAlphaState alphaState;
    private GlBlendState blendState;
    private RenderScale renderScale;
    private int id;
    private int ref;
    private String drawBufSettings;
    private IntBuffer drawBuffers;
    private IntBuffer drawBuffersBuffer;
    private int compositeMipmapSetting;
    private int countInstances;

    public Program(int index, String name, ProgramStage programStage, Program programBackup) {
        this.index = index;
        this.name = name;
        this.programStage = programStage;
        this.programBackup = programBackup;
    }

    public Program(int index, String name, ProgramStage programStage, boolean ownBackup) {
        this.index = index;
        this.name = name;
        this.programStage = programStage;
        programBackup = ownBackup ? this : null;
    }

    public void resetProperties() {
        alphaState = null;
        blendState = null;
        renderScale = null;
        Arrays.fill(buffersFlip, null);
    }

    public void resetId() {
        id = 0;
        ref = 0;
    }

    public void resetConfiguration() {
        drawBufSettings = null;
        compositeMipmapSetting = 0;
        countInstances = 0;

        if (drawBuffersBuffer == null) {
            drawBuffersBuffer = Shaders.nextIntBuffer(8);
        }
    }

    public void copyFrom(Program p) {
        id = p.getId();
        alphaState = p.getAlphaState();
        blendState = p.getBlendState();
        renderScale = p.getRenderScale();
        System.arraycopy(p.getBuffersFlip(), 0, buffersFlip, 0, buffersFlip.length);
        drawBufSettings = p.getDrawBufSettings();
        drawBuffers = p.getDrawBuffers();
        compositeMipmapSetting = p.getCompositeMipmapSetting();
        countInstances = p.getCountInstances();
        System.arraycopy(p.getToggleColorTextures(), 0, toggleColorTextures, 0, toggleColorTextures.length);
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public ProgramStage getProgramStage() {
        return programStage;
    }

    public Program getProgramBackup() {
        return programBackup;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRef() {
        return ref;
    }

    public void setRef(int ref) {
        this.ref = ref;
    }

    public String getDrawBufSettings() {
        return drawBufSettings;
    }

    public void setDrawBufSettings(String drawBufSettings) {
        this.drawBufSettings = drawBufSettings;
    }

    public IntBuffer getDrawBuffers() {
        return drawBuffers;
    }

    public void setDrawBuffers(IntBuffer drawBuffers) {
        this.drawBuffers = drawBuffers;
    }

    public IntBuffer getDrawBuffersBuffer() {
        return drawBuffersBuffer;
    }

    public int getCompositeMipmapSetting() {
        return compositeMipmapSetting;
    }

    public void setCompositeMipmapSetting(int compositeMipmapSetting) {
        this.compositeMipmapSetting = compositeMipmapSetting;
    }

    public int getCountInstances() {
        return countInstances;
    }

    public void setCountInstances(int countInstances) {
        this.countInstances = countInstances;
    }

    public GlAlphaState getAlphaState() {
        return alphaState;
    }

    public void setAlphaState(GlAlphaState alphaState) {
        this.alphaState = alphaState;
    }

    public GlBlendState getBlendState() {
        return blendState;
    }

    public void setBlendState(GlBlendState blendState) {
        this.blendState = blendState;
    }

    public RenderScale getRenderScale() {
        return renderScale;
    }

    public void setRenderScale(RenderScale renderScale) {
        this.renderScale = renderScale;
    }

    public Boolean[] getBuffersFlip() {
        return buffersFlip;
    }

    public boolean[] getToggleColorTextures() {
        return toggleColorTextures;
    }

    public String getRealProgramName() {
        if (id == 0) {
            return "none";
        } else {
            Program program;

            for (program = this; program.getRef() != id; program = program.getProgramBackup()) {
                if (program.getProgramBackup() == null || program.getProgramBackup() == program) {
                    return "unknown";
                }
            }

            return program.getName();
        }
    }

    public String toString() {
        return "name: " + name + ", id: " + id + ", ref: " + ref + ", real: " + getRealProgramName();
    }
}
