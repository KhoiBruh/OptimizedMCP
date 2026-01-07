package net.minecraft.client.renderer.vertex;

import net.minecraft.client.renderer.OpenGlHelper;
import net.optifine.render.VboRange;
import net.optifine.render.VboRegion;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

public class VertexBuffer {
    private final VertexFormat vertexFormat;
    private int glBufferId;
    private int count;
    private VboRegion vboRegion;
    private VboRange vboRange;
    private int drawMode;

    public VertexBuffer(VertexFormat vertexFormatIn) {
        vertexFormat = vertexFormatIn;
        glBufferId = OpenGlHelper.glGenBuffers();
    }

    public void bindBuffer() {
        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, glBufferId);
    }

    public void bufferData(ByteBuffer p_181722_1_) {
        if (vboRegion != null) {
            vboRegion.bufferData(p_181722_1_, vboRange);
        } else {
            bindBuffer();
            OpenGlHelper.glBufferData(OpenGlHelper.GL_ARRAY_BUFFER, p_181722_1_, 35044);
            unbindBuffer();
            count = p_181722_1_.limit() / vertexFormat.getNextOffset();
        }
    }

    public void drawArrays(int mode) {
        if (drawMode > 0) {
            mode = drawMode;
        }

        if (vboRegion != null) {
            vboRegion.drawArrays(mode, vboRange);
        } else {
            GL11.glDrawArrays(mode, 0, count);
        }
    }

    public void unbindBuffer() {
        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
    }

    public void deleteGlBuffers() {
        if (glBufferId >= 0) {
            OpenGlHelper.glDeleteBuffers(glBufferId);
            glBufferId = -1;
        }
    }

    public VboRegion getVboRegion() {
        return vboRegion;
    }

    public void setVboRegion(VboRegion p_setVboRegion_1_) {
        if (p_setVboRegion_1_ != null) {
            deleteGlBuffers();
            vboRegion = p_setVboRegion_1_;
            vboRange = new VboRange();
        }
    }

    public VboRange getVboRange() {
        return vboRange;
    }

    public int getDrawMode() {
        return drawMode;
    }

    public void setDrawMode(int p_setDrawMode_1_) {
        drawMode = p_setDrawMode_1_;
    }
}
