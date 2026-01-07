package net.optifine.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.List;

public class ModelUtils {
    public static IBakedModel duplicateModel(IBakedModel model) {
        List<BakedQuad> list = duplicateQuadList(model.getGeneralQuads());
        EnumFacing[] aenumfacing = EnumFacing.VALUES;
        List<List<BakedQuad>> list1 = new ArrayList<>();

        for (EnumFacing enumfacing : aenumfacing) {
            List<BakedQuad> list2 = model.getFaceQuads(enumfacing);
            List<BakedQuad> list3 = duplicateQuadList(list2);
            list1.add(list3);
        }

        return new SimpleBakedModel(list, list1, model.isAmbientOcclusion(), model.isGui3d(), model.getParticleTexture(), model.getItemCameraTransforms());
    }

    public static List<BakedQuad> duplicateQuadList(List<BakedQuad> lists) {
        List<BakedQuad> list = new ArrayList<>();

        for (BakedQuad bakedquad : lists) {
            BakedQuad bakedquad1 = duplicateQuad(bakedquad);
            list.add(bakedquad1);
        }

        return list;
    }

    public static BakedQuad duplicateQuad(BakedQuad quad) {
        return new BakedQuad(quad.getVertexData().clone(), quad.getTintIndex(), quad.getFace(), quad.getSprite());
    }
}
