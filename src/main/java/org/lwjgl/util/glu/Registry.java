package org.lwjgl.util.glu;

import static org.lwjgl.util.glu.GLU.GLU_EXTENSIONS;
import static org.lwjgl.util.glu.GLU.GLU_VERSION;

public class Registry extends Util {
    private static final String versionString = "1.3";
    private static final String extensionString = "GLU_EXT_nurbs_tessellator " + "GLU_EXT_object_space_tess ";

    public static String gluGetString(int name) {
        return switch (name) {
            case GLU_VERSION -> versionString;
            case GLU_EXTENSIONS -> extensionString;
            default -> null;
        };
    }

    public static boolean gluCheckExtension(String extName, String extString) {
        return null != extString && null != extName && extString.contains(extName);
    }
}