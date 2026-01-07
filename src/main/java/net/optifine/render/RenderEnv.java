package net.optifine.render;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BreakingFour;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.optifine.BlockPosM;
import net.optifine.model.ListQuadsOverlay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public class RenderEnv {
    private IBlockState blockState;
    private BlockPos blockPos;
    private int blockId = -1;
    private int metadata = -1;
    private int breakingAnimation = -1;
    private int smartLeaves = -1;
    private final float[] quadBounds = new float[EnumFacing.VALUES.length * 2];
    private final BitSet boundsFlags = new BitSet(3);
    private final BlockModelRenderer.AmbientOcclusionFace aoFace = new BlockModelRenderer.AmbientOcclusionFace();
    private BlockPosM colorizerBlockPosM = null;
    private boolean[] borderFlags = null;
    private boolean[] borderFlags2 = null;
    private boolean[] borderFlags3 = null;
    private EnumFacing[] borderDirections = null;
    private final List<BakedQuad> listQuadsCustomizer = new ArrayList<>();
    private final List<BakedQuad> listQuadsCtmMultipass = new ArrayList<>();
    private final BakedQuad[] arrayQuadsCtm1 = new BakedQuad[1];
    private final BakedQuad[] arrayQuadsCtm2 = new BakedQuad[2];
    private final BakedQuad[] arrayQuadsCtm3 = new BakedQuad[3];
    private final BakedQuad[] arrayQuadsCtm4 = new BakedQuad[4];
    private RegionRenderCacheBuilder regionRenderCacheBuilder = null;
    private final ListQuadsOverlay[] listsQuadsOverlay = new ListQuadsOverlay[EnumWorldBlockLayer.values().length];
    private boolean overlaysRendered = false;

    public RenderEnv(IBlockState blockState, BlockPos blockPos) {
        this.blockState = blockState;
        this.blockPos = blockPos;
    }

    public void reset(IBlockState blockStateIn, BlockPos blockPosIn) {
        if (blockState != blockStateIn || blockPos != blockPosIn) {
            blockState = blockStateIn;
            blockPos = blockPosIn;
            blockId = -1;
            metadata = -1;
            breakingAnimation = -1;
            smartLeaves = -1;
            boundsFlags.clear();
        }
    }

    public int getBlockId() {
        if (blockId < 0) {
            if (blockState instanceof BlockStateBase blockstatebase) {
                blockId = blockstatebase.getBlockId();
            } else {
                blockId = Block.getIdFromBlock(blockState.getBlock());
            }
        }

        return blockId;
    }

    public int getMetadata() {
        if (metadata < 0) {
            if (blockState instanceof BlockStateBase blockstatebase) {
                metadata = blockstatebase.getMetadata();
            } else {
                metadata = blockState.getBlock().getMetaFromState(blockState);
            }
        }

        return metadata;
    }

    public float[] getQuadBounds() {
        return quadBounds;
    }

    public BitSet getBoundsFlags() {
        return boundsFlags;
    }

    public BlockModelRenderer.AmbientOcclusionFace getAoFace() {
        return aoFace;
    }

    public boolean isBreakingAnimation(List<BakedQuad> listQuads) {
        if (breakingAnimation == -1 && !listQuads.isEmpty()) {
            if (listQuads.getFirst() instanceof BreakingFour) {
                breakingAnimation = 1;
            } else {
                breakingAnimation = 0;
            }
        }

        return breakingAnimation == 1;
    }

    public boolean isBreakingAnimation(BakedQuad quad) {
        if (breakingAnimation < 0) {
            if (quad instanceof BreakingFour) {
                breakingAnimation = 1;
            } else {
                breakingAnimation = 0;
            }
        }

        return breakingAnimation == 1;
    }

    public boolean isBreakingAnimation() {
        return breakingAnimation == 1;
    }

    public IBlockState getBlockState() {
        return blockState;
    }

    public BlockPosM getColorizerBlockPosM() {
        if (colorizerBlockPosM == null) {
            colorizerBlockPosM = new BlockPosM(0, 0, 0);
        }

        return colorizerBlockPosM;
    }

    public boolean[] getBorderFlags() {
        if (borderFlags == null) {
            borderFlags = new boolean[4];
        }

        return borderFlags;
    }

    public boolean[] getBorderFlags2() {
        if (borderFlags2 == null) {
            borderFlags2 = new boolean[4];
        }

        return borderFlags2;
    }

    public boolean[] getBorderFlags3() {
        if (borderFlags3 == null) {
            borderFlags3 = new boolean[4];
        }

        return borderFlags3;
    }

    public EnumFacing[] getBorderDirections() {
        if (borderDirections == null) {
            borderDirections = new EnumFacing[4];
        }

        return borderDirections;
    }

    public boolean isSmartLeaves() {
        if (smartLeaves == -1) {
            if (Config.isTreesSmart() && blockState.getBlock() instanceof BlockLeaves) {
                smartLeaves = 1;
            } else {
                smartLeaves = 0;
            }
        }

        return smartLeaves == 1;
    }

    public List<BakedQuad> getListQuadsCustomizer() {
        return listQuadsCustomizer;
    }

    public BakedQuad[] getArrayQuadsCtm(BakedQuad quad) {
        arrayQuadsCtm1[0] = quad;
        return arrayQuadsCtm1;
    }

    public BakedQuad[] getArrayQuadsCtm(BakedQuad quad0, BakedQuad quad1) {
        arrayQuadsCtm2[0] = quad0;
        arrayQuadsCtm2[1] = quad1;
        return arrayQuadsCtm2;
    }

    public BakedQuad[] getArrayQuadsCtm(BakedQuad quad0, BakedQuad quad1, BakedQuad quad2) {
        arrayQuadsCtm3[0] = quad0;
        arrayQuadsCtm3[1] = quad1;
        arrayQuadsCtm3[2] = quad2;
        return arrayQuadsCtm3;
    }

    public BakedQuad[] getArrayQuadsCtm(BakedQuad quad0, BakedQuad quad1, BakedQuad quad2, BakedQuad quad3) {
        arrayQuadsCtm4[0] = quad0;
        arrayQuadsCtm4[1] = quad1;
        arrayQuadsCtm4[2] = quad2;
        arrayQuadsCtm4[3] = quad3;
        return arrayQuadsCtm4;
    }

    public List<BakedQuad> getListQuadsCtmMultipass(BakedQuad[] quads) {
        listQuadsCtmMultipass.clear();

        if (quads != null) {
            listQuadsCtmMultipass.addAll(Arrays.asList(quads));
        }

        return listQuadsCtmMultipass;
    }

    public RegionRenderCacheBuilder getRegionRenderCacheBuilder() {
        return regionRenderCacheBuilder;
    }

    public void setRegionRenderCacheBuilder(RegionRenderCacheBuilder regionRenderCacheBuilder) {
        this.regionRenderCacheBuilder = regionRenderCacheBuilder;
    }

    public ListQuadsOverlay getListQuadsOverlay(EnumWorldBlockLayer layer) {
        ListQuadsOverlay listquadsoverlay = listsQuadsOverlay[layer.ordinal()];

        if (listquadsoverlay == null) {
            listquadsoverlay = new ListQuadsOverlay();
            listsQuadsOverlay[layer.ordinal()] = listquadsoverlay;
        }

        return listquadsoverlay;
    }

    public boolean isOverlaysRendered() {
        return overlaysRendered;
    }

    public void setOverlaysRendered(boolean overlaysRendered) {
        this.overlaysRendered = overlaysRendered;
    }
}
