package net.optifine;

import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.biome.BiomeGenBase;
import net.optifine.util.TileEntityUtils;

public class RandomTileEntity implements IRandomEntity {
    private TileEntity tileEntity;

    public int getId() {
        return Config.getRandom(tileEntity.getPos(), tileEntity.getBlockMetadata());
    }

    public BlockPos getSpawnPosition() {
        return tileEntity.getPos();
    }

    public String getName() {
        return TileEntityUtils.getTileEntityName(tileEntity);
    }

    public BiomeGenBase getSpawnBiome() {
        return tileEntity.getWorld().getBiomeGenForCoords(tileEntity.getPos());
    }

    public int getHealth() {
        return -1;
    }

    public int getMaxHealth() {
        return -1;
    }

    public void setTileEntity(TileEntity tileEntity) {
        this.tileEntity = tileEntity;
    }
}
