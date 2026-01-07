package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityFX extends Entity {
    public static double interpPosX;
    public static double interpPosY;
    public static double interpPosZ;
    protected int particleTextureIndexX;
    protected int particleTextureIndexY;
    protected float particleTextureJitterX;
    protected float particleTextureJitterY;
    protected int particleAge;
    protected int particleMaxAge;
    protected float particleScale;
    protected float particleGravity;
    protected float particleRed;
    protected float particleGreen;
    protected float particleBlue;
    protected float particleAlpha;
    protected TextureAtlasSprite particleIcon;

    protected EntityFX(World worldIn, double posXIn, double posYIn, double posZIn) {
        super(worldIn);
        particleAlpha = 1.0F;
        setSize(0.2F, 0.2F);
        setPosition(posXIn, posYIn, posZIn);
        lastTickPosX = prevPosX = posXIn;
        lastTickPosY = prevPosY = posYIn;
        lastTickPosZ = prevPosZ = posZIn;
        particleRed = particleGreen = particleBlue = 1.0F;
        particleTextureJitterX = rand.nextFloat() * 3.0F;
        particleTextureJitterY = rand.nextFloat() * 3.0F;
        particleScale = (rand.nextFloat() * 0.5F + 0.5F) * 2.0F;
        particleMaxAge = (int) (4.0F / (rand.nextFloat() * 0.9F + 0.1F));
        particleAge = 0;
    }

    public EntityFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        this(worldIn, xCoordIn, yCoordIn, zCoordIn);
        motionX = xSpeedIn + (Math.random() * 2.0D - 1.0D) * 0.4000000059604645D;
        motionY = ySpeedIn + (Math.random() * 2.0D - 1.0D) * 0.4000000059604645D;
        motionZ = zSpeedIn + (Math.random() * 2.0D - 1.0D) * 0.4000000059604645D;
        float f = (float) (Math.random() + Math.random() + 1.0D) * 0.15F;
        float f1 = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
        motionX = motionX / (double) f1 * (double) f * 0.4000000059604645D;
        motionY = motionY / (double) f1 * (double) f * 0.4000000059604645D + 0.10000000149011612D;
        motionZ = motionZ / (double) f1 * (double) f * 0.4000000059604645D;
    }

    public EntityFX multiplyVelocity(float multiplier) {
        motionX *= multiplier;
        motionY = (motionY - 0.10000000149011612D) * (double) multiplier + 0.10000000149011612D;
        motionZ *= multiplier;
        return this;
    }

    public EntityFX multipleParticleScaleBy(float scale) {
        setSize(0.2F * scale, 0.2F * scale);
        particleScale *= scale;
        return this;
    }

    public void setRBGColorF(float particleRedIn, float particleGreenIn, float particleBlueIn) {
        particleRed = particleRedIn;
        particleGreen = particleGreenIn;
        particleBlue = particleBlueIn;
    }

    public void setAlphaF(float alpha) {
        if (particleAlpha == 1.0F && alpha < 1.0F) {
            Minecraft.getMinecraft().effectRenderer.moveToAlphaLayer(this);
        } else if (particleAlpha < 1.0F && alpha == 1.0F) {
            Minecraft.getMinecraft().effectRenderer.moveToNoAlphaLayer(this);
        }

        particleAlpha = alpha;
    }

    public float getRedColorF() {
        return particleRed;
    }

    public float getGreenColorF() {
        return particleGreen;
    }

    public float getBlueColorF() {
        return particleBlue;
    }

    public float getAlpha() {
        return particleAlpha;
    }

    protected boolean canTriggerWalking() {
        return false;
    }

    protected void entityInit() {
    }

    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        if (particleAge++ >= particleMaxAge) {
            setDead();
        }

        motionY -= 0.04D * (double) particleGravity;
        moveEntity(motionX, motionY, motionZ);
        motionX *= 0.9800000190734863D;
        motionY *= 0.9800000190734863D;
        motionZ *= 0.9800000190734863D;

        if (onGround) {
            motionX *= 0.699999988079071D;
            motionZ *= 0.699999988079071D;
        }
    }

    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        float f = (float) particleTextureIndexX / 16.0F;
        float f1 = f + 0.0624375F;
        float f2 = (float) particleTextureIndexY / 16.0F;
        float f3 = f2 + 0.0624375F;
        float f4 = 0.1F * particleScale;

        if (particleIcon != null) {
            f = particleIcon.getMinU();
            f1 = particleIcon.getMaxU();
            f2 = particleIcon.getMinV();
            f3 = particleIcon.getMaxV();
        }

        float f5 = (float) (prevPosX + (posX - prevPosX) * (double) partialTicks - interpPosX);
        float f6 = (float) (prevPosY + (posY - prevPosY) * (double) partialTicks - interpPosY);
        float f7 = (float) (prevPosZ + (posZ - prevPosZ) * (double) partialTicks - interpPosZ);
        int i = getBrightnessForRender(partialTicks);
        int j = i >> 16 & 65535;
        int k = i & 65535;
        worldRendererIn.pos(f5 - rotationX * f4 - rotationXY * f4, f6 - rotationZ * f4, f7 - rotationYZ * f4 - rotationXZ * f4).tex(f1, f3).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(j, k).endVertex();
        worldRendererIn.pos(f5 - rotationX * f4 + rotationXY * f4, f6 + rotationZ * f4, f7 - rotationYZ * f4 + rotationXZ * f4).tex(f1, f2).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(j, k).endVertex();
        worldRendererIn.pos(f5 + rotationX * f4 + rotationXY * f4, f6 + rotationZ * f4, f7 + rotationYZ * f4 + rotationXZ * f4).tex(f, f2).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(j, k).endVertex();
        worldRendererIn.pos(f5 + rotationX * f4 - rotationXY * f4, f6 - rotationZ * f4, f7 + rotationYZ * f4 - rotationXZ * f4).tex(f, f3).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(j, k).endVertex();
    }

    public int getFXLayer() {
        return 0;
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound) {
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund) {
    }

    public void setParticleIcon(TextureAtlasSprite icon) {
        int i = getFXLayer();

        if (i == 1) {
            particleIcon = icon;
        } else {
            throw new RuntimeException("Invalid call to Particle.setTex, use coordinate methods");
        }
    }

    public void setParticleTextureIndex(int particleTextureIndex) {
        if (getFXLayer() != 0) {
            throw new RuntimeException("Invalid call to Particle.setMiscTex");
        } else {
            particleTextureIndexX = particleTextureIndex % 16;
            particleTextureIndexY = particleTextureIndex / 16;
        }
    }

    public void nextTextureIndexX() {
        ++particleTextureIndexX;
    }

    public boolean canAttackWithItem() {
        return false;
    }

    public String toString() {
        return getClass().getSimpleName() + ", Pos (" + posX + "," + posY + "," + posZ + "), RGBA (" + particleRed + "," + particleGreen + "," + particleBlue + "," + particleAlpha + "), Age " + particleAge;
    }
}
