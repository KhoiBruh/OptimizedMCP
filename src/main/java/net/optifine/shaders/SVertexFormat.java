package net.optifine.shaders;

import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

public class SVertexFormat {
    public static final VertexFormat defVertexFormatTextured = makeDefVertexFormatTextured();

    public static VertexFormat makeDefVertexFormatBlock() {
        VertexFormat vertexformat = new VertexFormat();
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 3));
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.UBYTE, VertexFormatElement.Usage.COLOR, 4));
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.UV, 2));
        vertexformat.addElement(new VertexFormatElement(1, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.UV, 2));
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.NORMAL, 3));
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.PADDING, 1));
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.PADDING, 2));
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.PADDING, 4));
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.PADDING, 4));
        return vertexformat;
    }

    public static VertexFormat makeDefVertexFormatItem() {
        VertexFormat vertexformat = new VertexFormat();
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 3));
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.UBYTE, VertexFormatElement.Usage.COLOR, 4));
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.UV, 2));
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.PADDING, 2));
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.NORMAL, 3));
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.PADDING, 1));
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.PADDING, 2));
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.PADDING, 4));
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.PADDING, 4));
        return vertexformat;
    }

    public static VertexFormat makeDefVertexFormatTextured() {
        VertexFormat vertexformat = new VertexFormat();
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 3));
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.UBYTE, VertexFormatElement.Usage.PADDING, 4));
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.UV, 2));
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.PADDING, 2));
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.NORMAL, 3));
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.PADDING, 1));
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.PADDING, 2));
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.PADDING, 4));
        vertexformat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.PADDING, 4));
        return vertexformat;
    }

    public static void copy(VertexFormat src, VertexFormat dst) {
        if (src != null && dst != null) {
            dst.clear();

            for (int i = 0; i < src.getElementCount(); ++i) {
                dst.addElement(src.getElement(i));
            }
        }
    }
}
