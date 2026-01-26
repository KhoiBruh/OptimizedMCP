package net.optifine.entity.model.anim;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntity;
import net.optifine.entity.model.CustomModelRenderer;
import net.optifine.entity.model.ModelAdapter;
import net.optifine.expr.IExpression;

public class ModelResolver implements IModelResolver {
    private final ModelAdapter modelAdapter;
    private final ModelBase model;
    private final CustomModelRenderer[] customModelRenderers;
    private ModelRenderer thisModelRenderer;
    private ModelRenderer partModelRenderer;
    private final IRenderResolver renderResolver;

    public ModelResolver(ModelAdapter modelAdapter, ModelBase model, CustomModelRenderer[] customModelRenderers) {
        this.modelAdapter = modelAdapter;
        this.model = model;
        this.customModelRenderers = customModelRenderers;
        Class<?> oclass = modelAdapter.getEntityClass();

        if (TileEntity.class.isAssignableFrom(oclass)) {
            renderResolver = new RenderResolverTileEntity();
        } else {
            renderResolver = new RenderResolverEntity();
        }
    }

    public IExpression getExpression(String name) {
        IExpression iexpression = getModelVariable(name);

        if (iexpression != null) {
            return iexpression;
        } else {
            return renderResolver.getParameter(name);
        }
    }

    private ModelRenderer getModelRenderer(String name) {
        if (name == null) {
            return null;
        } else if (name.contains(":")) {
            String[] astring = Config.tokenize(name, ":");
            ModelRenderer modelrenderer3 = getModelRenderer(astring[0]);

            for (int j = 1; j < astring.length; ++j) {
                String s = astring[j];
                ModelRenderer modelrenderer4 = modelrenderer3.getChildDeep(s);

                if (modelrenderer4 == null) {
                    return null;
                }

                modelrenderer3 = modelrenderer4;
            }

            return modelrenderer3;
        } else if (thisModelRenderer != null && name.equals("this")) {
            return thisModelRenderer;
        } else if (partModelRenderer != null && name.equals("part")) {
            return partModelRenderer;
        } else {
            ModelRenderer modelrenderer = modelAdapter.getModelRenderer(model, name);

            if (modelrenderer != null) {
                return modelrenderer;
            } else {
                for (CustomModelRenderer custommodelrenderer : customModelRenderers) {
                    ModelRenderer modelrenderer1 = custommodelrenderer.getModelRenderer();

                    if (name.equals(modelrenderer1.getId())) {
                        return modelrenderer1;
                    }

                    ModelRenderer modelrenderer2 = modelrenderer1.getChildDeep(name);

                    if (modelrenderer2 != null) {
                        return modelrenderer2;
                    }
                }

                return null;
            }
        }
    }

    public ModelVariableFloat getModelVariable(String name) {
        String[] astring = Config.tokenize(name, ".");

        if (astring.length != 2) {
            return null;
        } else {
            String s = astring[0];
            String s1 = astring[1];
            ModelRenderer modelrenderer = getModelRenderer(s);

            if (modelrenderer == null) {
                return null;
            } else {
                ModelVariableType modelvariabletype = ModelVariableType.parse(s1);
                return modelvariabletype == null ? null : new ModelVariableFloat(name, modelrenderer, modelvariabletype);
            }
        }
    }

    public void setPartModelRenderer(ModelRenderer partModelRenderer) {
        this.partModelRenderer = partModelRenderer;
    }

    public void setThisModelRenderer(ModelRenderer thisModelRenderer) {
        this.thisModelRenderer = thisModelRenderer;
    }
}
