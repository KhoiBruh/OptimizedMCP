package net.optifine.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.init.Blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListQuadsOverlay {
    private final List<BakedQuad> listQuads = new ArrayList<>();
    private final List<IBlockState> listBlockStates = new ArrayList<>();
    private final List<BakedQuad> listQuadsSingle = Arrays.asList(new BakedQuad[1]);

    public void addQuad(BakedQuad quad, IBlockState blockState) {
        if (quad != null) {
            listQuads.add(quad);
            listBlockStates.add(blockState);
        }
    }

    public int size() {
        return listQuads.size();
    }

    public BakedQuad getQuad(int index) {
        return listQuads.get(index);
    }

    public IBlockState getBlockState(int index) {
        return index >= 0 && index < listBlockStates.size() ? listBlockStates.get(index) : Blocks.air.getDefaultState();
    }

    public List<BakedQuad> getListQuadsSingle(BakedQuad quad) {
        listQuadsSingle.set(0, quad);
        return listQuadsSingle;
    }

    public void clear() {
        listQuads.clear();
        listBlockStates.clear();
    }
}
