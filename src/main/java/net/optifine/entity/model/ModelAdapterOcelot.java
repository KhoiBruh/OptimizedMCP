package net.optifine.entity.model;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelOcelot;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderOcelot;
import net.minecraft.entity.passive.EntityOcelot;

public class ModelAdapterOcelot extends ModelAdapter {
    private static Map<String, Integer> mapPartFields = null;

    public ModelAdapterOcelot() {
        super(EntityOcelot.class, "ocelot", 0.4F);
    }

    public ModelBase makeModel() {
        return new ModelOcelot();
    }

    public ModelRenderer getModelRenderer(ModelBase model, String modelPart) {
        if (!(model instanceof ModelOcelot)) {
            return null;
        } else {
            ModelOcelot modelocelot = (ModelOcelot) model;
            Map<String, Integer> map = getMapPartFields();

            if (map.containsKey(modelPart)) {
                int i = map.get(modelPart).intValue();
                switch (i) {
                    case 0:
                        return modelocelot.ocelotBackLeftLeg;
                    case 1:
                        return modelocelot.ocelotBackRightLeg;
                    case 2:
                        return modelocelot.ocelotFrontLeftLeg;
                    case 3:
                        return modelocelot.ocelotFrontRightLeg;
                    case 4:
                        return modelocelot.ocelotTail;
                    case 5:
                        return modelocelot.ocelotTail2;
                    case 6:
                        return modelocelot.ocelotHead;
                    case 7:
                        return modelocelot.ocelotBody;
                    default:
                        return null;
                }
            } else {
                return null;
            }
        }
    }

    public String[] getModelRendererNames() {
        return new String[] { "back_left_leg", "back_right_leg", "front_left_leg", "front_right_leg", "tail", "tail2",
                "head", "body" };
    }

    private static Map<String, Integer> getMapPartFields() {
        if (mapPartFields != null) {
            return mapPartFields;
        } else {
            mapPartFields = new HashMap();
            mapPartFields.put("back_left_leg", Integer.valueOf(0));
            mapPartFields.put("back_right_leg", Integer.valueOf(1));
            mapPartFields.put("front_left_leg", Integer.valueOf(2));
            mapPartFields.put("front_right_leg", Integer.valueOf(3));
            mapPartFields.put("tail", Integer.valueOf(4));
            mapPartFields.put("tail2", Integer.valueOf(5));
            mapPartFields.put("head", Integer.valueOf(6));
            mapPartFields.put("body", Integer.valueOf(7));
            return mapPartFields;
        }
    }

    public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize) {
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        RenderOcelot renderocelot = new RenderOcelot(rendermanager, modelBase, shadowSize);
        return renderocelot;
    }
}
