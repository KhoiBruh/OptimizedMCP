package net.optifine.entity.model.anim;

public class ModelUpdater {
    private final ModelVariableUpdater[] modelVariableUpdaters;

    public ModelUpdater(ModelVariableUpdater[] modelVariableUpdaters) {
        this.modelVariableUpdaters = modelVariableUpdaters;
    }

    public void update() {
        for (ModelVariableUpdater modelvariableupdater : modelVariableUpdaters) {
            modelvariableupdater.update();
        }
    }

    public boolean initialize(IModelResolver mr) {
        for (ModelVariableUpdater modelvariableupdater : modelVariableUpdaters) {
            if (!modelvariableupdater.initialize(mr)) {
                return false;
            }
        }

        return true;
    }
}
