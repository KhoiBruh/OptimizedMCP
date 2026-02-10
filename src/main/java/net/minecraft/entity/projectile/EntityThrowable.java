package net.minecraft.entity.projectile;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;
import java.util.UUID;

public abstract class EntityThrowable extends Entity implements IProjectile {
    public int throwableShake;
    protected boolean inGround;
    private int xTile = -1;
    private int yTile = -1;
    private int zTile = -1;
    private Block inTile;
    private EntityLivingBase thrower;
    private String throwerName;
    private int ticksInGround;
    private int ticksInAir;

    public EntityThrowable(World worldIn) {
        super(worldIn);
        setSize(0.25F, 0.25F);
    }

    public EntityThrowable(World worldIn, EntityLivingBase throwerIn) {
        super(worldIn);
        thrower = throwerIn;
        setSize(0.25F, 0.25F);
        setLocationAndAngles(throwerIn.posX, throwerIn.posY + (double) throwerIn.getEyeHeight(), throwerIn.posZ, throwerIn.rotationYaw, throwerIn.rotationPitch);
        posX -= MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
        posY -= 0.10000000149011612D;
        posZ -= MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
        setPosition(posX, posY, posZ);
        float f = 0.4F;
        motionX = -MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI) * f;
        motionZ = MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI) * f;
        motionY = -MathHelper.sin((rotationPitch + getInaccuracy()) / 180.0F * (float) Math.PI) * f;
        setThrowableHeading(motionX, motionY, motionZ, getVelocity(), 1.0F);
    }

    public EntityThrowable(World worldIn, double x, double y, double z) {
        super(worldIn);
        ticksInGround = 0;
        setSize(0.25F, 0.25F);
        setPosition(x, y, z);
    }

    protected void entityInit() {
    }

    public boolean isInRangeToRenderDist(double distance) {
        double d0 = getEntityBoundingBox().getAverageEdgeLength() * 4.0D;

        if (Double.isNaN(d0)) {
            d0 = 4.0D;
        }

        d0 = d0 * 64.0D;
        return distance < d0 * d0;
    }

    protected float getVelocity() {
        return 1.5F;
    }

    protected float getInaccuracy() {
        return 0.0F;
    }

    public void setThrowableHeading(double x, double y, double z, float velocity, float inaccuracy) {
        float f = MathHelper.sqrt(x * x + y * y + z * z);
        x = x / (double) f;
        y = y / (double) f;
        z = z / (double) f;
        x = x + rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
        y = y + rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
        z = z + rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
        x = x * (double) velocity;
        y = y * (double) velocity;
        z = z * (double) velocity;
        motionX = x;
        motionY = y;
        motionZ = z;
        float f1 = MathHelper.sqrt(x * x + z * z);
        prevRotationYaw = rotationYaw = (float) (MathHelper.atan2(x, z) * 180.0D / Math.PI);
        prevRotationPitch = rotationPitch = (float) (MathHelper.atan2(y, f1) * 180.0D / Math.PI);
        ticksInGround = 0;
    }

    public void setVelocity(double x, double y, double z) {
        motionX = x;
        motionY = y;
        motionZ = z;

        if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F) {
            float f = MathHelper.sqrt(x * x + z * z);
            prevRotationYaw = rotationYaw = (float) (MathHelper.atan2(x, z) * 180.0D / Math.PI);
            prevRotationPitch = rotationPitch = (float) (MathHelper.atan2(y, f) * 180.0D / Math.PI);
        }
    }

    public void onUpdate() {
        lastTickPosX = posX;
        lastTickPosY = posY;
        lastTickPosZ = posZ;
        super.onUpdate();

        if (throwableShake > 0) {
            --throwableShake;
        }

        if (inGround) {
            if (worldObj.getBlockState(new BlockPos(xTile, yTile, zTile)).getBlock() == inTile) {
                ++ticksInGround;

                if (ticksInGround == 1200) {
                    setDead();
                }

                return;
            }

            inGround = false;
            motionX *= rand.nextFloat() * 0.2F;
            motionY *= rand.nextFloat() * 0.2F;
            motionZ *= rand.nextFloat() * 0.2F;
            ticksInGround = 0;
            ticksInAir = 0;
        } else {
            ++ticksInAir;
        }

        Vec3 vec3 = new Vec3(posX, posY, posZ);
        Vec3 vec31 = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
        MovingObjectPosition movingobjectposition = worldObj.rayTraceBlocks(vec3, vec31);
        vec3 = new Vec3(posX, posY, posZ);
        vec31 = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);

        if (movingobjectposition != null) {
            vec31 = new Vec3(movingobjectposition.hitVec.xCoord(), movingobjectposition.hitVec.yCoord(), movingobjectposition.hitVec.zCoord());
        }

        if (!worldObj.isRemote) {
            Entity entity = null;
            List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
            double d0 = 0.0D;
            EntityLivingBase entitylivingbase = getThrower();

            for (Entity entity1 : list) {
                if (entity1.canBeCollidedWith() && (entity1 != entitylivingbase || ticksInAir >= 5)) {
                    float f = 0.3F;
                    AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f, f, f);
                    MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(vec3, vec31);

                    if (movingobjectposition1 != null) {
                        double d1 = vec3.squareDistanceTo(movingobjectposition1.hitVec);

                        if (d1 < d0 || d0 == 0.0D) {
                            entity = entity1;
                            d0 = d1;
                        }
                    }
                }
            }

            if (entity != null) {
                movingobjectposition = new MovingObjectPosition(entity);
            }
        }

        if (movingobjectposition != null) {
            if (movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && worldObj.getBlockState(movingobjectposition.getBlockPos()).getBlock() == Blocks.portal) {
                setPortal(movingobjectposition.getBlockPos());
            } else {
                onImpact(movingobjectposition);
            }
        }

        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        float f1 = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
        rotationYaw = (float) (MathHelper.atan2(motionX, motionZ) * 180.0D / Math.PI);

        for (rotationPitch = (float) (MathHelper.atan2(motionY, f1) * 180.0D / Math.PI); rotationPitch - prevRotationPitch < -180.0F; prevRotationPitch -= 360.0F) {
        }

        while (rotationPitch - prevRotationPitch >= 180.0F) {
            prevRotationPitch += 360.0F;
        }

        while (rotationYaw - prevRotationYaw < -180.0F) {
            prevRotationYaw -= 360.0F;
        }

        while (rotationYaw - prevRotationYaw >= 180.0F) {
            prevRotationYaw += 360.0F;
        }

        rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
        rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
        float f2 = 0.99F;
        float f3 = getGravityVelocity();

        if (isInWater()) {
            for (int i = 0; i < 4; ++i) {
                float f4 = 0.25F;
                worldObj.spawnParticle(ParticleTypes.WATER_BUBBLE, posX - motionX * (double) f4, posY - motionY * (double) f4, posZ - motionZ * (double) f4, motionX, motionY, motionZ);
            }

            f2 = 0.8F;
        }

        motionX *= f2;
        motionY *= f2;
        motionZ *= f2;
        motionY -= f3;
        setPosition(posX, posY, posZ);
    }

    protected float getGravityVelocity() {
        return 0.03F;
    }

    protected abstract void onImpact(MovingObjectPosition p_70184_1_);

    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        tagCompound.setShort("xTile", (short) xTile);
        tagCompound.setShort("yTile", (short) yTile);
        tagCompound.setShort("zTile", (short) zTile);
        ResourceLocation resourcelocation = Block.blockRegistry.getNameForObject(inTile);
        tagCompound.setString("inTile", resourcelocation == null ? "" : resourcelocation.toString());
        tagCompound.setByte("shake", (byte) throwableShake);
        tagCompound.setByte("inGround", (byte) (inGround ? 1 : 0));

        if ((throwerName == null || throwerName.isEmpty()) && thrower instanceof EntityPlayer) {
            throwerName = thrower.getName();
        }

        tagCompound.setString("ownerName", throwerName == null ? "" : throwerName);
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        xTile = tagCompund.getShort("xTile");
        yTile = tagCompund.getShort("yTile");
        zTile = tagCompund.getShort("zTile");

        if (tagCompund.hasKey("inTile", 8)) {
            inTile = Block.getBlockFromName(tagCompund.getString("inTile"));
        } else {
            inTile = Block.getBlockById(tagCompund.getByte("inTile") & 255);
        }

        throwableShake = tagCompund.getByte("shake") & 255;
        inGround = tagCompund.getByte("inGround") == 1;
        thrower = null;
        throwerName = tagCompund.getString("ownerName");

        if (throwerName != null && throwerName.isEmpty()) {
            throwerName = null;
        }

        thrower = getThrower();
    }

    public EntityLivingBase getThrower() {
        if (thrower == null && throwerName != null && !throwerName.isEmpty()) {
            thrower = worldObj.getPlayerEntityByName(throwerName);

            if (thrower == null && worldObj instanceof WorldServer) {
                try {
                    Entity entity = ((WorldServer) worldObj).getEntityFromUuid(UUID.fromString(throwerName));

                    if (entity instanceof EntityLivingBase) {
                        thrower = (EntityLivingBase) entity;
                    }
                } catch (Throwable var2) {
                    thrower = null;
                }
            }
        }

        return thrower;
    }
}
