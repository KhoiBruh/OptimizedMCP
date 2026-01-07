package net.optifine;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockPos;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.UUID;

public class RandomEntity implements IRandomEntity {
    private Entity entity;

    public int getId() {
        UUID uuid = entity.getUniqueID();
        long i = uuid.getLeastSignificantBits();
        return (int) (i & 2147483647L);
    }

    public BlockPos getSpawnPosition() {
        return entity.getDataWatcher().spawnPosition;
    }

    public BiomeGenBase getSpawnBiome() {
        return entity.getDataWatcher().spawnBiome;
    }

    public String getName() {
        return entity.hasCustomName() ? entity.getCustomNameTag() : null;
    }

    public int getHealth() {
        if (!(entity instanceof EntityLiving entityliving)) {
            return 0;
        } else {
            return (int) entityliving.getHealth();
        }
    }

    public int getMaxHealth() {
        if (!(entity instanceof EntityLiving entityliving)) {
            return 0;
        } else {
            return (int) entityliving.getMaxHealth();
        }
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}
