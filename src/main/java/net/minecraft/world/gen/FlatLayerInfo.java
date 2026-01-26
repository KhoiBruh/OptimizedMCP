package net.minecraft.world.gen;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

public class FlatLayerInfo {
    private final int field_175902_a;
    private IBlockState layerMaterial;
    private final int layerCount;
    private int layerMinimumY;

    public FlatLayerInfo(int p_i45467_1_, Block p_i45467_2_) {
        this(3, p_i45467_1_, p_i45467_2_);
    }

    public FlatLayerInfo(int p_i45627_1_, int height, Block layerMaterialIn) {
        field_175902_a = p_i45627_1_;
        layerCount = height;
        layerMaterial = layerMaterialIn.getDefaultState();
    }

    public FlatLayerInfo(int p_i45628_1_, int p_i45628_2_, Block p_i45628_3_, int p_i45628_4_) {
        this(p_i45628_1_, p_i45628_2_, p_i45628_3_);
        layerMaterial = p_i45628_3_.getStateFromMeta(p_i45628_4_);
    }

    public int getLayerCount() {
        return layerCount;
    }

    public IBlockState getLayerMaterial() {
        return layerMaterial;
    }

    private Block getLayerMaterialBlock() {
        return layerMaterial.getBlock();
    }

    private int getFillBlockMeta() {
        return layerMaterial.getBlock().getMetaFromState(layerMaterial);
    }

    public int getMinY() {
        return layerMinimumY;
    }

    public void setMinY(int minY) {
        layerMinimumY = minY;
    }

    public String toString() {
        String s;

        if (field_175902_a >= 3) {
            ResourceLocation resourcelocation = Block.blockRegistry.getNameForObject(getLayerMaterialBlock());
            s = resourcelocation == null ? "null" : resourcelocation.toString();

            if (layerCount > 1) {
                s = layerCount + "*" + s;
            }
        } else {
            s = Integer.toString(Block.getIdFromBlock(getLayerMaterialBlock()));

            if (layerCount > 1) {
                s = layerCount + "x" + s;
            }
        }

        int i = getFillBlockMeta();

        if (i > 0) {
            s = s + ":" + i;
        }

        return s;
    }
}
