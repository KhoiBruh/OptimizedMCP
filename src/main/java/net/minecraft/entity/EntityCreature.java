package net.minecraft.entity;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public abstract class EntityCreature extends EntityLiving {
    public static final UUID FLEEING_SPEED_MODIFIER_UUID = UUID.fromString("E199AD21-BA8A-4C53-8D13-6182D5C69D3A");
    public static final AttributeModifier FLEEING_SPEED_MODIFIER = (new AttributeModifier(FLEEING_SPEED_MODIFIER_UUID, "Fleeing speed bonus", 2.0D, 2)).setSaved(false);
    private BlockPos homePosition = BlockPos.ORIGIN;
    private float maximumHomeDistance = -1.0F;
    private final EntityAIBase aiBase = new EntityAIMoveTowardsRestriction(this, 1.0D);
    private boolean isMovementAITaskSet;

    public EntityCreature(World worldIn) {
        super(worldIn);
    }

    public float getBlockPathWeight(BlockPos pos) {
        return 0.0F;
    }

    public boolean getCanSpawnHere() {
        return super.getCanSpawnHere() && getBlockPathWeight(new BlockPos(posX, getEntityBoundingBox().minY, posZ)) >= 0.0F;
    }

    public boolean hasPath() {
        return !navigator.noPath();
    }

    public boolean isWithinHomeDistanceCurrentPosition() {
        return isWithinHomeDistanceFromPosition(new BlockPos(this));
    }

    public boolean isWithinHomeDistanceFromPosition(BlockPos pos) {
        return maximumHomeDistance == -1.0F || homePosition.distanceSq(pos) < (double) (maximumHomeDistance * maximumHomeDistance);
    }

    public void setHomePosAndDistance(BlockPos pos, int distance) {
        homePosition = pos;
        maximumHomeDistance = (float) distance;
    }

    public BlockPos getHomePosition() {
        return homePosition;
    }

    public float getMaximumHomeDistance() {
        return maximumHomeDistance;
    }

    public void detachHome() {
        maximumHomeDistance = -1.0F;
    }

    public boolean hasHome() {
        return maximumHomeDistance != -1.0F;
    }

    protected void updateLeashedState() {
        super.updateLeashedState();

        if (getLeashed() && getLeashedToEntity() != null && getLeashedToEntity().worldObj == worldObj) {
            Entity entity = getLeashedToEntity();
            setHomePosAndDistance(new BlockPos((int) entity.posX, (int) entity.posY, (int) entity.posZ), 5);
            float f = getDistanceToEntity(entity);

            if (this instanceof EntityTameable && ((EntityTameable) this).isSitting()) {
                if (f > 10.0F) {
                    clearLeashed(true, true);
                }

                return;
            }

            if (!isMovementAITaskSet) {
                tasks.addTask(2, aiBase);

                if (getNavigator() instanceof PathNavigateGround) {
                    ((PathNavigateGround) getNavigator()).setAvoidsWater(false);
                }

                isMovementAITaskSet = true;
            }

            func_142017_o(f);

            if (f > 4.0F) {
                getNavigator().tryMoveToEntityLiving(entity, 1.0D);
            }

            if (f > 6.0F) {
                double d0 = (entity.posX - posX) / (double) f;
                double d1 = (entity.posY - posY) / (double) f;
                double d2 = (entity.posZ - posZ) / (double) f;
                motionX += d0 * Math.abs(d0) * 0.4D;
                motionY += d1 * Math.abs(d1) * 0.4D;
                motionZ += d2 * Math.abs(d2) * 0.4D;
            }

            if (f > 10.0F) {
                clearLeashed(true, true);
            }
        } else if (!getLeashed() && isMovementAITaskSet) {
            isMovementAITaskSet = false;
            tasks.removeTask(aiBase);

            if (getNavigator() instanceof PathNavigateGround) {
                ((PathNavigateGround) getNavigator()).setAvoidsWater(true);
            }

            detachHome();
        }
    }

    protected void func_142017_o(float p_142017_1_) {
    }
}
