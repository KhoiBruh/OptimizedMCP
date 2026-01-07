package net.optifine.shaders;

public enum ProgramStage {
    NONE(""),
    SHADOW("shadow"),
    GBUFFERS("gbuffers"),
    DEFERRED("deferred"),
    COMPOSITE("composite");

    ProgramStage(String name) {
    }

}
