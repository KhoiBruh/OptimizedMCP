package net.minecraft.client.particle;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class EntityDiggingFX extends EntityFX {
    private final IBlockState sourceState;
    private BlockPos sourcePos;

    protected EntityDiggingFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, IBlockState state) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        sourceState = state;
        setParticleIcon(Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state));
        particleGravity = state.getBlock().blockParticleGravity;
        particleRed = particleGreen = particleBlue = 0.6F;
        particleScale /= 2.0F;
    }

    public EntityDiggingFX setBlockPos(BlockPos pos) {
        sourcePos = pos;

        if (sourceState.getBlock() == Blocks.grass) {
            return this;
        } else {
            int i = sourceState.getBlock().colorMultiplier(worldObj, pos);
            particleRed *= (float) (i >> 16 & 255) / 255.0F;
            particleGreen *= (float) (i >> 8 & 255) / 255.0F;
            particleBlue *= (float) (i & 255) / 255.0F;
            return this;
        }
    }

    public EntityDiggingFX func_174845_l() {
        sourcePos = new BlockPos(posX, posY, posZ);
        Block block = sourceState.getBlock();

        if (block == Blocks.grass) {
            return this;
        } else {
            int i = block.getRenderColor(sourceState);
            particleRed *= (float) (i >> 16 & 255) / 255.0F;
            particleGreen *= (float) (i >> 8 & 255) / 255.0F;
            particleBlue *= (float) (i & 255) / 255.0F;
            return this;
        }
    }

    public int getFXLayer() {
        return 1;
    }

    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        float f = ((float) particleTextureIndexX + particleTextureJitterX / 4.0F) / 16.0F;
        float f1 = f + 0.015609375F;
        float f2 = ((float) particleTextureIndexY + particleTextureJitterY / 4.0F) / 16.0F;
        float f3 = f2 + 0.015609375F;
        float f4 = 0.1F * particleScale;

        if (particleIcon != null) {
            f = particleIcon.getInterpolatedU(particleTextureJitterX / 4.0F * 16.0F);
            f1 = particleIcon.getInterpolatedU((particleTextureJitterX + 1.0F) / 4.0F * 16.0F);
            f2 = particleIcon.getInterpolatedV(particleTextureJitterY / 4.0F * 16.0F);
            f3 = particleIcon.getInterpolatedV((particleTextureJitterY + 1.0F) / 4.0F * 16.0F);
        }

        float f5 = (float) (prevPosX + (posX - prevPosX) * (double) partialTicks - interpPosX);
        float f6 = (float) (prevPosY + (posY - prevPosY) * (double) partialTicks - interpPosY);
        float f7 = (float) (prevPosZ + (posZ - prevPosZ) * (double) partialTicks - interpPosZ);
        int i = getBrightnessForRender(partialTicks);
        int j = i >> 16 & 65535;
        int k = i & 65535;
        worldRendererIn.pos(f5 - rotationX * f4 - rotationXY * f4, f6 - rotationZ * f4, f7 - rotationYZ * f4 - rotationXZ * f4).tex(f, f3).color(particleRed, particleGreen, particleBlue, 1.0F).lightmap(j, k).endVertex();
        worldRendererIn.pos(f5 - rotationX * f4 + rotationXY * f4, f6 + rotationZ * f4, f7 - rotationYZ * f4 + rotationXZ * f4).tex(f, f2).color(particleRed, particleGreen, particleBlue, 1.0F).lightmap(j, k).endVertex();
        worldRendererIn.pos(f5 + rotationX * f4 + rotationXY * f4, f6 + rotationZ * f4, f7 + rotationYZ * f4 + rotationXZ * f4).tex(f1, f2).color(particleRed, particleGreen, particleBlue, 1.0F).lightmap(j, k).endVertex();
        worldRendererIn.pos(f5 + rotationX * f4 - rotationXY * f4, f6 - rotationZ * f4, f7 + rotationYZ * f4 - rotationXZ * f4).tex(f1, f3).color(particleRed, particleGreen, particleBlue, 1.0F).lightmap(j, k).endVertex();
    }

    public int getBrightnessForRender(float partialTicks) {
        int i = super.getBrightnessForRender(partialTicks);
        int j = 0;

        if (worldObj.isBlockLoaded(sourcePos)) {
            j = worldObj.getCombinedLight(sourcePos, 0);
        }

        return i == 0 ? j : i;
    }

    public static class Factory implements IParticleFactory {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
            return (new EntityDiggingFX(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, Block.getStateById(p_178902_15_[0]))).func_174845_l();
        }
    }
}
