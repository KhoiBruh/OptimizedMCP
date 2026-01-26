package net.minecraft.entity.passive;

import net.minecraft.block.material.Material;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntitySquid extends EntityWaterMob {
    public float squidPitch;
    public float prevSquidPitch;
    public float squidYaw;
    public float prevSquidYaw;
    public float squidRotation;
    public float prevSquidRotation;
    public float tentacleAngle;
    public float lastTentacleAngle;
    private float randomMotionSpeed;
    private float rotationVelocity;
    private float field_70871_bB;
    private float randomMotionVecX;
    private float randomMotionVecY;
    private float randomMotionVecZ;

    public EntitySquid(World worldIn) {
        super(worldIn);
        setSize(0.95F, 0.95F);
        rand.setSeed(1 + getEntityId());
        rotationVelocity = 1.0F / (rand.nextFloat() + 1.0F) * 0.2F;
        tasks.addTask(0, new EntitySquid.AIMoveRandom(this));
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10.0D);
    }

    public float getEyeHeight() {
        return height * 0.5F;
    }

    protected String getHurtSound() {
        return null;
    }

    protected String getDeathSound() {
        return null;
    }

    protected float getSoundVolume() {
        return 0.4F;
    }

    protected boolean canTriggerWalking() {
        return false;
    }

    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        int i = rand.nextInt(3 + lootingModifier) + 1;

        for (int j = 0; j < i; ++j) {
            entityDropItem(new ItemStack(Items.dye, 1, EnumDyeColor.BLACK.getDyeDamage()), 0.0F);
        }
    }

    public boolean isInWater() {
        return worldObj.handleMaterialAcceleration(getEntityBoundingBox().expand(0.0D, -0.6000000238418579D, 0.0D), Material.water, this);
    }

    public void onLivingUpdate() {
        super.onLivingUpdate();
        prevSquidPitch = squidPitch;
        prevSquidYaw = squidYaw;
        prevSquidRotation = squidRotation;
        lastTentacleAngle = tentacleAngle;
        squidRotation += rotationVelocity;

        if ((double) squidRotation > (Math.PI * 2D)) {
            if (worldObj.isRemote) {
                squidRotation = ((float) Math.PI * 2F);
            } else {
                squidRotation = (float) ((double) squidRotation - (Math.PI * 2D));

                if (rand.nextInt(10) == 0) {
                    rotationVelocity = 1.0F / (rand.nextFloat() + 1.0F) * 0.2F;
                }

                worldObj.setEntityState(this, (byte) 19);
            }
        }

        if (inWater) {
            if (squidRotation < (float) Math.PI) {
                float f = squidRotation / (float) Math.PI;
                tentacleAngle = MathHelper.sin(f * f * (float) Math.PI) * (float) Math.PI * 0.25F;

                if ((double) f > 0.75D) {
                    randomMotionSpeed = 1.0F;
                    field_70871_bB = 1.0F;
                } else {
                    field_70871_bB *= 0.8F;
                }
            } else {
                tentacleAngle = 0.0F;
                randomMotionSpeed *= 0.9F;
                field_70871_bB *= 0.99F;
            }

            if (!worldObj.isRemote) {
                motionX = randomMotionVecX * randomMotionSpeed;
                motionY = randomMotionVecY * randomMotionSpeed;
                motionZ = randomMotionVecZ * randomMotionSpeed;
            }

            float f1 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
            renderYawOffset += (-((float) MathHelper.atan2(motionX, motionZ)) * 180.0F / (float) Math.PI - renderYawOffset) * 0.1F;
            rotationYaw = renderYawOffset;
            squidYaw = (float) ((double) squidYaw + Math.PI * (double) field_70871_bB * 1.5D);
            squidPitch += (-((float) MathHelper.atan2(f1, motionY)) * 180.0F / (float) Math.PI - squidPitch) * 0.1F;
        } else {
            tentacleAngle = MathHelper.abs(MathHelper.sin(squidRotation)) * (float) Math.PI * 0.25F;

            if (!worldObj.isRemote) {
                motionX = 0.0D;
                motionY -= 0.08D;
                motionY *= 0.9800000190734863D;
                motionZ = 0.0D;
            }

            squidPitch = (float) ((double) squidPitch + (double) (-90.0F - squidPitch) * 0.02D);
        }
    }

    public void moveEntityWithHeading(float strafe, float forward) {
        moveEntity(motionX, motionY, motionZ);
    }

    public boolean getCanSpawnHere() {
        return posY > 45.0D && posY < (double) worldObj.getSeaLevel() && super.getCanSpawnHere();
    }

    public void handleStatusUpdate(byte id) {
        if (id == 19) {
            squidRotation = 0.0F;
        } else {
            super.handleStatusUpdate(id);
        }
    }

    public void func_175568_b(float randomMotionVecXIn, float randomMotionVecYIn, float randomMotionVecZIn) {
        randomMotionVecX = randomMotionVecXIn;
        randomMotionVecY = randomMotionVecYIn;
        randomMotionVecZ = randomMotionVecZIn;
    }

    public boolean func_175567_n() {
        return randomMotionVecX != 0.0F || randomMotionVecY != 0.0F || randomMotionVecZ != 0.0F;
    }

    static class AIMoveRandom extends EntityAIBase {
        private final EntitySquid squid;

        public AIMoveRandom(EntitySquid p_i45859_1_) {
            squid = p_i45859_1_;
        }

        public boolean shouldExecute() {
            return true;
        }

        public void updateTask() {
            int i = squid.getAge();

            if (i > 100) {
                squid.func_175568_b(0.0F, 0.0F, 0.0F);
            } else if (squid.getRNG().nextInt(50) == 0 || !squid.inWater || !squid.func_175567_n()) {
                float f = squid.getRNG().nextFloat() * (float) Math.PI * 2.0F;
                float f1 = MathHelper.cos(f) * 0.2F;
                float f2 = -0.1F + squid.getRNG().nextFloat() * 0.2F;
                float f3 = MathHelper.sin(f) * 0.2F;
                squid.func_175568_b(f1, f2, f3);
            }
        }
    }
}
