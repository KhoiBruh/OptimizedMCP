package net.minecraft.client.renderer;

import net.minecraft.util.BlockPos;

public class DestroyBlockProgress {
    private final int miningPlayerEntId;
    private final BlockPos position;
    private int partialBlockProgress;
    private int createdAtCloudUpdateTick;

    public DestroyBlockProgress(int miningPlayerEntIdIn, BlockPos positionIn) {
        miningPlayerEntId = miningPlayerEntIdIn;
        position = positionIn;
    }

    public BlockPos getPosition() {
        return position;
    }

    public int getPartialBlockDamage() {
        return partialBlockProgress;
    }

    public void setPartialBlockDamage(int damage) {
        if (damage > 10) {
            damage = 10;
        }

        partialBlockProgress = damage;
    }

    public void setCloudUpdateTick(int createdAtCloudUpdateTickIn) {
        createdAtCloudUpdateTick = createdAtCloudUpdateTickIn;
    }

    public int getCreationCloudUpdateTick() {
        return createdAtCloudUpdateTick;
    }
}
