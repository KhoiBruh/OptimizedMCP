package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLiving;

public class EntityJumpHelper {
    protected boolean isJumping;
    private final EntityLiving entity;

    public EntityJumpHelper(EntityLiving entityIn) {
        entity = entityIn;
    }

    public void setJumping() {
        isJumping = true;
    }

    public void doJump() {
        entity.setJumping(isJumping);
        isJumping = false;
    }
}
