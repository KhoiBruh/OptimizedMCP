package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

import java.util.Random;

public class BlockPackedIce extends Block {
    public BlockPackedIce() {
        super(Material.packedIce);
        slipperiness = 0.98F;
        setCreativeTab(CreativeTabs.tabBlock);
    }

    public int quantityDropped(Random random) {
        return 0;
    }
}
