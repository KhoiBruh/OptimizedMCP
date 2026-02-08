package net.minecraft.client.model;

import net.minecraft.client.renderer.*;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.entity.model.anim.ModelUpdater;
import net.optifine.model.ModelSprite;
import net.optifine.shaders.Shaders;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class ModelRenderer {
    public final String boxName;
    public float textureWidth;
    public float textureHeight;
    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;
    public float rotateAngleX;
    public float rotateAngleY;
    public float rotateAngleZ;
    public boolean mirror;
    public boolean showModel;
    public boolean isHidden;
    public List<ModelBox> cubeList;
    public List<ModelRenderer> childModels;
    public float offsetX;
    public float offsetY;
    public float offsetZ;
    public List spriteList;
    public boolean mirrorV;
    public float scaleX;
    public float scaleY;
    public float scaleZ;
    private int textureOffsetX;
    private int textureOffsetY;
    private boolean compiled;
    private int displayList;
    private final ModelBase baseModel;
    private int countResetDisplayList;
    private ResourceLocation textureLocation;
    private String id;
    private ModelUpdater modelUpdater;
    private final RenderGlobal renderGlobal;

    public ModelRenderer(ModelBase model, String boxNameIn) {
        spriteList = new ArrayList();
        mirrorV = false;
        scaleX = 1.0F;
        scaleY = 1.0F;
        scaleZ = 1.0F;
        textureLocation = null;
        id = null;
        renderGlobal = Config.getRenderGlobal();
        textureWidth = 64.0F;
        textureHeight = 32.0F;
        showModel = true;
        cubeList = new ArrayList<>();
        baseModel = model;
        model.boxList.add(this);
        boxName = boxNameIn;
        setTextureSize(model.textureWidth, model.textureHeight);
    }

    public ModelRenderer(ModelBase model) {
        this(model, null);
    }

    public ModelRenderer(ModelBase model, int texOffX, int texOffY) {
        this(model);
        setTextureOffset(texOffX, texOffY);
    }

    public void addChild(ModelRenderer renderer) {
        if (childModels == null) {
            childModels = new ArrayList<>();
        }

        childModels.add(renderer);
    }

    public ModelRenderer setTextureOffset(int x, int y) {
        textureOffsetX = x;
        textureOffsetY = y;
        return this;
    }

    public ModelRenderer addBox(String partName, float offX, float offY, float offZ, int width, int height, int depth) {
        partName = boxName + "." + partName;
        TextureOffset textureoffset = baseModel.getTextureOffset(partName);
        setTextureOffset(textureoffset.textureOffsetX(), textureoffset.textureOffsetY());
        cubeList.add((new ModelBox(this, textureOffsetX, textureOffsetY, offX, offY, offZ, width, height, depth, 0.0F)).setBoxName(partName));
        return this;
    }

    public ModelRenderer addBox(float offX, float offY, float offZ, int width, int height, int depth) {
        cubeList.add(new ModelBox(this, textureOffsetX, textureOffsetY, offX, offY, offZ, width, height, depth, 0.0F));
        return this;
    }

    public ModelRenderer addBox(float p_178769_1_, float p_178769_2_, float p_178769_3_, int p_178769_4_, int p_178769_5_, int p_178769_6_, boolean p_178769_7_) {
        cubeList.add(new ModelBox(this, textureOffsetX, textureOffsetY, p_178769_1_, p_178769_2_, p_178769_3_, p_178769_4_, p_178769_5_, p_178769_6_, 0.0F, p_178769_7_));
        return this;
    }

    public void addBox(float p_78790_1_, float p_78790_2_, float p_78790_3_, int width, int height, int depth, float scaleFactor) {
        cubeList.add(new ModelBox(this, textureOffsetX, textureOffsetY, p_78790_1_, p_78790_2_, p_78790_3_, width, height, depth, scaleFactor));
    }

    public void setRotationPoint(float rotationPointXIn, float rotationPointYIn, float rotationPointZIn) {
        rotationPointX = rotationPointXIn;
        rotationPointY = rotationPointYIn;
        rotationPointZ = rotationPointZIn;
    }

    public void render(float p_78785_1_) {
        if (!isHidden && showModel) {
            checkResetDisplayList();

            if (!compiled) {
                compileDisplayList(p_78785_1_);
            }

            int i = 0;

            if (textureLocation != null && !renderGlobal.renderOverlayDamaged) {
                if (renderGlobal.renderOverlayEyes) {
                    return;
                }

                i = GlStateManager.getBoundTexture();
                Config.getTextureManager().bindTexture(textureLocation);
            }

            if (modelUpdater != null) {
                modelUpdater.update();
            }

            boolean flag = scaleX != 1.0F || scaleY != 1.0F || scaleZ != 1.0F;
            GlStateManager.translate(offsetX, offsetY, offsetZ);

            if (rotateAngleX == 0.0F && rotateAngleY == 0.0F && rotateAngleZ == 0.0F) {
                if (rotationPointX == 0.0F && rotationPointY == 0.0F && rotationPointZ == 0.0F) {
                    if (flag) {
                        GlStateManager.scale(scaleX, scaleY, scaleZ);
                    }

                    GlStateManager.callList(displayList);

                    if (childModels != null) {
                        for (ModelRenderer childModel : childModels) {
                            childModel.render(p_78785_1_);
                        }
                    }

                    if (flag) {
                        GlStateManager.scale(1.0F / scaleX, 1.0F / scaleY, 1.0F / scaleZ);
                    }
                } else {
                    GlStateManager.translate(rotationPointX * p_78785_1_, rotationPointY * p_78785_1_, rotationPointZ * p_78785_1_);

                    if (flag) {
                        GlStateManager.scale(scaleX, scaleY, scaleZ);
                    }

                    GlStateManager.callList(displayList);

                    if (childModels != null) {
                        for (ModelRenderer childModel : childModels) {
                            childModel.render(p_78785_1_);
                        }
                    }

                    if (flag) {
                        GlStateManager.scale(1.0F / scaleX, 1.0F / scaleY, 1.0F / scaleZ);
                    }

                    GlStateManager.translate(-rotationPointX * p_78785_1_, -rotationPointY * p_78785_1_, -rotationPointZ * p_78785_1_);
                }
            } else {
                GlStateManager.pushMatrix();
                GlStateManager.translate(rotationPointX * p_78785_1_, rotationPointY * p_78785_1_, rotationPointZ * p_78785_1_);

                if (rotateAngleZ != 0.0F) {
                    GlStateManager.rotate(rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
                }

                if (rotateAngleY != 0.0F) {
                    GlStateManager.rotate(rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
                }

                if (rotateAngleX != 0.0F) {
                    GlStateManager.rotate(rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
                }

                if (flag) {
                    GlStateManager.scale(scaleX, scaleY, scaleZ);
                }

                GlStateManager.callList(displayList);

                if (childModels != null) {
                    for (ModelRenderer childModel : childModels) {
                        childModel.render(p_78785_1_);
                    }
                }

                GlStateManager.popMatrix();
            }

            GlStateManager.translate(-offsetX, -offsetY, -offsetZ);

            if (i != 0) {
                GlStateManager.bindTexture(i);
            }
        }
    }

    public void renderWithRotation(float p_78791_1_) {
        if (!isHidden && showModel) {
            checkResetDisplayList();

            if (!compiled) {
                compileDisplayList(p_78791_1_);
            }

            int i = 0;

            if (textureLocation != null && !renderGlobal.renderOverlayDamaged) {
                if (renderGlobal.renderOverlayEyes) {
                    return;
                }

                i = GlStateManager.getBoundTexture();
                Config.getTextureManager().bindTexture(textureLocation);
            }

            if (modelUpdater != null) {
                modelUpdater.update();
            }

            boolean flag = scaleX != 1.0F || scaleY != 1.0F || scaleZ != 1.0F;
            GlStateManager.pushMatrix();
            GlStateManager.translate(rotationPointX * p_78791_1_, rotationPointY * p_78791_1_, rotationPointZ * p_78791_1_);

            if (rotateAngleY != 0.0F) {
                GlStateManager.rotate(rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
            }

            if (rotateAngleX != 0.0F) {
                GlStateManager.rotate(rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
            }

            if (rotateAngleZ != 0.0F) {
                GlStateManager.rotate(rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
            }

            if (flag) {
                GlStateManager.scale(scaleX, scaleY, scaleZ);
            }

            GlStateManager.callList(displayList);

            if (childModels != null) {
                for (ModelRenderer childModel : childModels) {
                    childModel.render(p_78791_1_);
                }
            }

            GlStateManager.popMatrix();

            if (i != 0) {
                GlStateManager.bindTexture(i);
            }
        }
    }

    public void postRender(float scale) {
        if (!isHidden && showModel) {
            checkResetDisplayList();

            if (!compiled) {
                compileDisplayList(scale);
            }

            if (rotateAngleX == 0.0F && rotateAngleY == 0.0F && rotateAngleZ == 0.0F) {
                if (rotationPointX != 0.0F || rotationPointY != 0.0F || rotationPointZ != 0.0F) {
                    GlStateManager.translate(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);
                }
            } else {
                GlStateManager.translate(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);

                if (rotateAngleZ != 0.0F) {
                    GlStateManager.rotate(rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
                }

                if (rotateAngleY != 0.0F) {
                    GlStateManager.rotate(rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
                }

                if (rotateAngleX != 0.0F) {
                    GlStateManager.rotate(rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
                }
            }
        }
    }

    private void compileDisplayList(float scale) {
        if (displayList == 0) {
            displayList = GLAllocation.generateDisplayLists(1);
        }

        GL11.glNewList(displayList, GL11.GL_COMPILE);
        WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();

        for (ModelBox modelBox : cubeList) {
            modelBox.render(worldrenderer, scale);
        }

        for (Object o : spriteList) {
            ModelSprite modelsprite = (ModelSprite) o;
            modelsprite.render(Tessellator.getInstance(), scale);
        }

        GL11.glEndList();
        compiled = true;
    }

    public ModelRenderer setTextureSize(int textureWidthIn, int textureHeightIn) {
        textureWidth = (float) textureWidthIn;
        textureHeight = (float) textureHeightIn;
        return this;
    }

    public void addSprite(float p_addSprite_1_, float p_addSprite_2_, float p_addSprite_3_, int p_addSprite_4_, int p_addSprite_5_, int p_addSprite_6_, float p_addSprite_7_) {
        spriteList.add(new ModelSprite(this, textureOffsetX, textureOffsetY, p_addSprite_1_, p_addSprite_2_, p_addSprite_3_, p_addSprite_4_, p_addSprite_5_, p_addSprite_6_, p_addSprite_7_));
    }

    public boolean getCompiled() {
        return compiled;
    }

    public int getDisplayList() {
        return displayList;
    }

    private void checkResetDisplayList() {
        if (countResetDisplayList != Shaders.countResetDisplayLists) {
            compiled = false;
            countResetDisplayList = Shaders.countResetDisplayLists;
        }
    }

    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }

    public void setTextureLocation(ResourceLocation p_setTextureLocation_1_) {
        textureLocation = p_setTextureLocation_1_;
    }

    public String getId() {
        return id;
    }

    public void setId(String p_setId_1_) {
        id = p_setId_1_;
    }

    public void addBox(int[][] p_addBox_1_, float p_addBox_2_, float p_addBox_3_, float p_addBox_4_, float p_addBox_5_, float p_addBox_6_, float p_addBox_7_, float p_addBox_8_) {
        cubeList.add(new ModelBox(this, p_addBox_1_, p_addBox_2_, p_addBox_3_, p_addBox_4_, p_addBox_5_, p_addBox_6_, p_addBox_7_, p_addBox_8_, mirror));
    }

    public ModelRenderer getChild(String p_getChild_1_) {
        if (p_getChild_1_ == null) {
            return null;
        } else {
            if (childModels != null) {
                for (ModelRenderer modelrenderer : childModels) {
                    if (p_getChild_1_.equals(modelrenderer.id)) {
                        return modelrenderer;
                    }
                }
            }

            return null;
        }
    }

    public ModelRenderer getChildDeep(String p_getChildDeep_1_) {
        if (p_getChildDeep_1_ == null) {
            return null;
        } else {
            ModelRenderer modelrenderer = getChild(p_getChildDeep_1_);

            if (modelrenderer != null) {
                return modelrenderer;
            } else {
                if (childModels != null) {
                    for (ModelRenderer modelrenderer1 : childModels) {
                        ModelRenderer modelrenderer2 = modelrenderer1.getChildDeep(p_getChildDeep_1_);

                        if (modelrenderer2 != null) {
                            return modelrenderer2;
                        }
                    }
                }

                return null;
            }
        }
    }

    public void setModelUpdater(ModelUpdater p_setModelUpdater_1_) {
        modelUpdater = p_setModelUpdater_1_;
    }

    public String toString() {
        return "id: " + id + ", boxes: " + (cubeList != null ? cubeList.size() : null) + ", submodels: " + (childModels != null ? childModels.size() : null);
    }
}
