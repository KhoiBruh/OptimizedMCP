package net.optifine.player;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;

public class PlayerItemRenderer {
    private final int attachTo;
    private final ModelRenderer modelRenderer;

    public PlayerItemRenderer(int attachTo, ModelRenderer modelRenderer) {
        this.attachTo = attachTo;
        this.modelRenderer = modelRenderer;
    }

    public void render(ModelBiped modelBiped, float scale) {
        ModelRenderer modelrenderer = PlayerItemModel.getAttachModel(modelBiped, attachTo);

        if (modelrenderer != null) {
            modelrenderer.postRender(scale);
        }

        modelRenderer.render(scale);
    }
}
