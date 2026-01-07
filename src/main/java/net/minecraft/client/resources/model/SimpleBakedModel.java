package net.minecraft.client.resources.model;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BreakingFour;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import java.util.List;

public class SimpleBakedModel implements IBakedModel {
    protected final List<BakedQuad> generalQuads;
    protected final List<List<BakedQuad>> faceQuads;
    protected final boolean ambientOcclusion;
    protected final boolean gui3d;
    protected final TextureAtlasSprite texture;
    protected final ItemCameraTransforms cameraTransforms;

    public SimpleBakedModel(List<BakedQuad> generalQuadsIn, List<List<BakedQuad>> faceQuadsIn, boolean ambientOcclusionIn, boolean gui3dIn, TextureAtlasSprite textureIn, ItemCameraTransforms cameraTransformsIn) {
        generalQuads = generalQuadsIn;
        faceQuads = faceQuadsIn;
        ambientOcclusion = ambientOcclusionIn;
        gui3d = gui3dIn;
        texture = textureIn;
        cameraTransforms = cameraTransformsIn;
    }

    public List<BakedQuad> getFaceQuads(EnumFacing facing) {
        return faceQuads.get(facing.ordinal());
    }

    public List<BakedQuad> getGeneralQuads() {
        return generalQuads;
    }

    public boolean isAmbientOcclusion() {
        return ambientOcclusion;
    }

    public boolean isGui3d() {
        return gui3d;
    }

    public boolean isBuiltInRenderer() {
        return false;
    }

    public TextureAtlasSprite getParticleTexture() {
        return texture;
    }

    public ItemCameraTransforms getItemCameraTransforms() {
        return cameraTransforms;
    }

    public static class Builder {
        private final List<BakedQuad> builderGeneralQuads;
        private final List<List<BakedQuad>> builderFaceQuads;
        private final boolean builderAmbientOcclusion;
        private TextureAtlasSprite builderTexture;
        private final boolean builderGui3d;
        private final ItemCameraTransforms builderCameraTransforms;

        public Builder(ModelBlock model) {
            this(model.isAmbientOcclusion(), model.isGui3d(), model.getAllTransforms());
        }

        public Builder(IBakedModel bakedModel, TextureAtlasSprite texture) {
            this(bakedModel.isAmbientOcclusion(), bakedModel.isGui3d(), bakedModel.getItemCameraTransforms());
            builderTexture = bakedModel.getParticleTexture();

            for (EnumFacing enumfacing : EnumFacing.values()) {
                addFaceBreakingFours(bakedModel, texture, enumfacing);
            }

            addGeneralBreakingFours(bakedModel, texture);
        }

        private Builder(boolean ambientOcclusion, boolean gui3d, ItemCameraTransforms cameraTransforms) {
            builderGeneralQuads = Lists.newArrayList();
            builderFaceQuads = Lists.newArrayListWithCapacity(6);

            for (EnumFacing enumfacing : EnumFacing.values()) {
                builderFaceQuads.add(Lists.newArrayList());
            }

            builderAmbientOcclusion = ambientOcclusion;
            builderGui3d = gui3d;
            builderCameraTransforms = cameraTransforms;
        }

        private void addFaceBreakingFours(IBakedModel bakedModel, TextureAtlasSprite texture, EnumFacing facing) {
            for (BakedQuad bakedquad : bakedModel.getFaceQuads(facing)) {
                addFaceQuad(facing, new BreakingFour(bakedquad, texture));
            }
        }

        private void addGeneralBreakingFours(IBakedModel p_177647_1_, TextureAtlasSprite texture) {
            for (BakedQuad bakedquad : p_177647_1_.getGeneralQuads()) {
                addGeneralQuad(new BreakingFour(bakedquad, texture));
            }
        }

        public SimpleBakedModel.Builder addFaceQuad(EnumFacing facing, BakedQuad quad) {
            builderFaceQuads.get(facing.ordinal()).add(quad);
            return this;
        }

        public SimpleBakedModel.Builder addGeneralQuad(BakedQuad quad) {
            builderGeneralQuads.add(quad);
            return this;
        }

        public SimpleBakedModel.Builder setTexture(TextureAtlasSprite texture) {
            builderTexture = texture;
            return this;
        }

        public IBakedModel makeBakedModel() {
            if (builderTexture == null) {
                throw new RuntimeException("Missing particle!");
            } else {
                return new SimpleBakedModel(builderGeneralQuads, builderFaceQuads, builderAmbientOcclusion, builderGui3d, builderTexture, builderCameraTransforms);
            }
        }
    }
}
