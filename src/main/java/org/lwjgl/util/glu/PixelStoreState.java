package org.lwjgl.util.glu;

import static org.lwjgl.opengl.GL11.*;

class PixelStoreState extends Util {

    public int unpackRowLength;
    public int unpackAlignment;
    public int unpackSkipRows;
    public int unpackSkipPixels;
    public int packRowLength;
    public int packAlignment;
    public int packSkipRows;
    public int packSkipPixels;

    PixelStoreState() {
        load();
    }

    public void load() {
        unpackRowLength = glGetInteger(GL_UNPACK_ROW_LENGTH);
        unpackAlignment = glGetInteger(GL_UNPACK_ALIGNMENT);
        unpackSkipRows = glGetInteger(GL_UNPACK_SKIP_ROWS);
        unpackSkipPixels = glGetInteger(GL_UNPACK_SKIP_PIXELS);
        packRowLength = glGetInteger(GL_PACK_ROW_LENGTH);
        packAlignment = glGetInteger(GL_PACK_ALIGNMENT);
        packSkipRows = glGetInteger(GL_PACK_SKIP_ROWS);
        packSkipPixels = glGetInteger(GL_PACK_SKIP_PIXELS);
    }

    public void save() {
        glPixelStorei(GL_UNPACK_ROW_LENGTH, unpackRowLength);
        glPixelStorei(GL_UNPACK_ALIGNMENT, unpackAlignment);
        glPixelStorei(GL_UNPACK_SKIP_ROWS, unpackSkipRows);
        glPixelStorei(GL_UNPACK_SKIP_PIXELS, unpackSkipPixels);
        glPixelStorei(GL_PACK_ROW_LENGTH, packRowLength);
        glPixelStorei(GL_PACK_ALIGNMENT, packAlignment);
        glPixelStorei(GL_PACK_SKIP_ROWS, packSkipRows);
        glPixelStorei(GL_PACK_SKIP_PIXELS, packSkipPixels);
    }
}
