package net.optifine.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.VboRenderList;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.src.Config;
import net.minecraft.util.EnumWorldBlockLayer;
import net.optifine.util.LinkedList;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class VboRegion {
    private final int vertexBytes;
    private int glBufferId = OpenGlHelper.glGenBuffers();
    private int capacity = 4096;
    private int positionTop = 0;
    private int sizeUsed;
    private final LinkedList<VboRange> rangeList = new LinkedList<>();
    private VboRange compactRangeLast = null;
    private IntBuffer bufferIndexVertex;
    private IntBuffer bufferCountVertex;
    private int drawMode;

    public VboRegion(EnumWorldBlockLayer layer) {
        bufferIndexVertex = Config.createDirectIntBuffer(capacity);
        bufferCountVertex = Config.createDirectIntBuffer(capacity);
        drawMode = 7;
        vertexBytes = DefaultVertexFormats.BLOCK.getNextOffset();
        bindBuffer();
        long i = toBytes(capacity);
        OpenGlHelper.glBufferData(OpenGlHelper.GL_ARRAY_BUFFER, i, OpenGlHelper.GL_STATIC_DRAW);
        unbindBuffer();
    }

    public void bufferData(ByteBuffer data, VboRange range) {
        int i = range.getPosition();
        int j = range.getSize();
        int k = toVertex(data.limit());

        if (k <= 0) {
            if (i >= 0) {
                range.setPosition(-1);
                range.setSize(0);
                rangeList.remove(range.getNode());
                sizeUsed -= j;
            }
        } else {
            if (k > j) {
                range.setPosition(positionTop);
                range.setSize(k);
                positionTop += k;

                if (i >= 0) {
                    rangeList.remove(range.getNode());
                }

                rangeList.addLast(range.getNode());
            }

            range.setSize(k);
            sizeUsed += k - j;
            checkVboSize(range.getPositionNext());
            long l = toBytes(range.getPosition());
            bindBuffer();
            OpenGlHelper.glBufferSubData(OpenGlHelper.GL_ARRAY_BUFFER, l, data);
            unbindBuffer();

            if (positionTop > sizeUsed * 11 / 10) {
                compactRanges(1);
            }
        }
    }

    private void compactRanges(int countMax) {
        if (!rangeList.isEmpty()) {
            VboRange vborange = compactRangeLast;

            if (vborange == null || !rangeList.contains(vborange.getNode())) {
                vborange = rangeList.getFirst().getItem();
            }

            int i;
            VboRange vborange1 = vborange.getPrev();

            if (vborange1 == null) {
                i = 0;
            } else {
                i = vborange1.getPositionNext();
            }

            int j = 0;

            while (vborange != null && j < countMax) {
                ++j;

                if (vborange.getPosition() == i) {
                    i += vborange.getSize();
                    vborange = vborange.getNext();
                } else {
                    int k = vborange.getPosition() - i;

                    if (vborange.getSize() <= k) {
                        copyVboData(vborange.getPosition(), i, vborange.getSize());
                        vborange.setPosition(i);
                        i += vborange.getSize();
                        vborange = vborange.getNext();
                    } else {
                        checkVboSize(positionTop + vborange.getSize());
                        copyVboData(vborange.getPosition(), positionTop, vborange.getSize());
                        vborange.setPosition(positionTop);
                        positionTop += vborange.getSize();
                        VboRange vborange2 = vborange.getNext();
                        rangeList.remove(vborange.getNode());
                        rangeList.addLast(vborange.getNode());
                        vborange = vborange2;
                    }
                }
            }

            if (vborange == null) {
                positionTop = rangeList.getLast().getItem().getPositionNext();
            }

            compactRangeLast = vborange;
        }
    }

    private void checkVboSize(int sizeMin) {
        if (capacity < sizeMin) {
            expandVbo(sizeMin);
        }
    }

    private void copyVboData(int posFrom, int posTo, int size) {
        long i = toBytes(posFrom);
        long j = toBytes(posTo);
        long k = toBytes(size);
        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_COPY_READ_BUFFER, glBufferId);
        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_COPY_WRITE_BUFFER, glBufferId);
        OpenGlHelper.glCopyBufferSubData(OpenGlHelper.GL_COPY_READ_BUFFER, OpenGlHelper.GL_COPY_WRITE_BUFFER, i, j, k);
        Config.checkGlError("Copy VBO range");
        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_COPY_READ_BUFFER, 0);
        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_COPY_WRITE_BUFFER, 0);
    }

    private void expandVbo(int sizeMin) {
        int i;

        for (i = capacity * 6 / 4; i < sizeMin; i = i * 6 / 4) {
        }

        long j = toBytes(capacity);
        long k = toBytes(i);
        int l = OpenGlHelper.glGenBuffers();
        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, l);
        OpenGlHelper.glBufferData(OpenGlHelper.GL_ARRAY_BUFFER, k, OpenGlHelper.GL_STATIC_DRAW);
        Config.checkGlError("Expand VBO");
        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_COPY_READ_BUFFER, glBufferId);
        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_COPY_WRITE_BUFFER, l);
        OpenGlHelper.glCopyBufferSubData(OpenGlHelper.GL_COPY_READ_BUFFER, OpenGlHelper.GL_COPY_WRITE_BUFFER, 0L, 0L, j);
        Config.checkGlError("Copy VBO: " + k);
        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_COPY_READ_BUFFER, 0);
        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_COPY_WRITE_BUFFER, 0);
        OpenGlHelper.glDeleteBuffers(glBufferId);
        bufferIndexVertex = Config.createDirectIntBuffer(i);
        bufferCountVertex = Config.createDirectIntBuffer(i);
        glBufferId = l;
        capacity = i;
    }

    public void bindBuffer() {
        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, glBufferId);
    }

    public void drawArrays(int drawMode, VboRange range) {
        if (this.drawMode != drawMode) {
            if (bufferIndexVertex.position() > 0) {
                throw new IllegalArgumentException("Mixed region draw modes: " + this.drawMode + " != " + drawMode);
            }

            this.drawMode = drawMode;
        }

        bufferIndexVertex.put(range.getPosition());
        bufferCountVertex.put(range.getSize());
    }

    public void finishDraw(VboRenderList vboRenderList) {
        bindBuffer();
        vboRenderList.setupArrayPointers();
        bufferIndexVertex.flip();
        bufferCountVertex.flip();
        GlStateManager.glMultiDrawArrays(drawMode, bufferIndexVertex, bufferCountVertex);
        bufferIndexVertex.limit(bufferIndexVertex.capacity());
        bufferCountVertex.limit(bufferCountVertex.capacity());

        if (positionTop > sizeUsed * 11 / 10) {
            compactRanges(1);
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

    private long toBytes(int vertex) {
        return (long) vertex * (long) vertexBytes;
    }

    private int toVertex(long bytes) {
        return (int) (bytes / (long) vertexBytes);
    }

}
