package net.minecraft.client.particle;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityLavaFX extends EntityFX {
    private final float lavaParticleScale;

    protected EntityLavaFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D);
        motionX *= 0.800000011920929D;
        motionY *= 0.800000011920929D;
        motionZ *= 0.800000011920929D;
        motionY = rand.nextFloat() * 0.4F + 0.05F;
        particleRed = particleGreen = particleBlue = 1.0F;
        particleScale *= rand.nextFloat() * 2.0F + 0.2F;
        lavaParticleScale = particleScale;
        particleMaxAge = (int) (16.0D / (Math.random() * 0.8D + 0.2D));
        noClip = false;
        setParticleTextureIndex(49);
    }

    public int getBrightnessForRender(float partialTicks) {
        float f = ((float) particleAge + partialTicks) / (float) particleMaxAge;
        int i = super.getBrightnessForRender(partialTicks);
        int j = 240;
        int k = i >> 16 & 255;
        return j | k << 16;
    }

    public float getBrightness(float partialTicks) {
        return 1.0F;
    }

    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        float f = ((float) particleAge + partialTicks) / (float) particleMaxAge;
        particleScale = lavaParticleScale * (1.0F - f * f);
        super.renderParticle(worldRendererIn, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }

    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        if (particleAge++ >= particleMaxAge) {
            setDead();
        }

        float f = (float) particleAge / (float) particleMaxAge;

        if (rand.nextFloat() > f) {
            worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY, posZ, motionX, motionY, motionZ);
        }

        motionY -= 0.03D;
        moveEntity(motionX, motionY, motionZ);
        motionX *= 0.9990000128746033D;
        motionY *= 0.9990000128746033D;
        motionZ *= 0.9990000128746033D;

        if (onGround) {
            motionX *= 0.699999988079071D;
            motionZ *= 0.699999988079071D;
        }
    }

    public static class Factory implements IParticleFactory {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
            return new EntityLavaFX(worldIn, xCoordIn, yCoordIn, zCoordIn);
        }
    }
}
