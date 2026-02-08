package net.minecraft.client.particle;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.src.Config;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.*;

public class EffectRenderer {
    private static final ResourceLocation particleTextures = new ResourceLocation("textures/particle/particles.png");
    protected World worldObj;
    private final List<EntityFX>[][] fxLayers = new List[4][];
    private final List<EntityParticleEmitter> particleEmitters = new ArrayList<>();
    private final TextureManager renderer;
    private final Random rand = new Random();
    private final Map<Integer, IParticleFactory> particleTypes = new HashMap<>();

    public EffectRenderer(World worldIn, TextureManager rendererIn) {
        worldObj = worldIn;
        renderer = rendererIn;

        for (int i = 0; i < 4; ++i) {
            fxLayers[i] = new List[2];

            for (int j = 0; j < 2; ++j) {
                fxLayers[i][j] = new ArrayList<>();
            }
        }

        registerVanillaParticles();
    }

    private void registerVanillaParticles() {
        registerParticle(ParticleTypes.EXPLOSION_NORMAL.getParticleID(), new EntityExplodeFX.Factory());
        registerParticle(ParticleTypes.WATER_BUBBLE.getParticleID(), new EntityBubbleFX.Factory());
        registerParticle(ParticleTypes.WATER_SPLASH.getParticleID(), new EntitySplashFX.Factory());
        registerParticle(ParticleTypes.WATER_WAKE.getParticleID(), new EntityFishWakeFX.Factory());
        registerParticle(ParticleTypes.WATER_DROP.getParticleID(), new EntityRainFX.Factory());
        registerParticle(ParticleTypes.SUSPENDED.getParticleID(), new EntitySuspendFX.Factory());
        registerParticle(ParticleTypes.SUSPENDED_DEPTH.getParticleID(), new EntityAuraFX.Factory());
        registerParticle(ParticleTypes.CRIT.getParticleID(), new EntityCrit2FX.Factory());
        registerParticle(ParticleTypes.CRIT_MAGIC.getParticleID(), new EntityCrit2FX.MagicFactory());
        registerParticle(ParticleTypes.SMOKE_NORMAL.getParticleID(), new EntitySmokeFX.Factory());
        registerParticle(ParticleTypes.SMOKE_LARGE.getParticleID(), new EntityCritFX.Factory());
        registerParticle(ParticleTypes.SPELL.getParticleID(), new EntitySpellParticleFX.Factory());
        registerParticle(ParticleTypes.SPELL_INSTANT.getParticleID(), new EntitySpellParticleFX.InstantFactory());
        registerParticle(ParticleTypes.SPELL_MOB.getParticleID(), new EntitySpellParticleFX.MobFactory());
        registerParticle(ParticleTypes.SPELL_MOB_AMBIENT.getParticleID(), new EntitySpellParticleFX.AmbientMobFactory());
        registerParticle(ParticleTypes.SPELL_WITCH.getParticleID(), new EntitySpellParticleFX.WitchFactory());
        registerParticle(ParticleTypes.DRIP_WATER.getParticleID(), new EntityDropParticleFX.WaterFactory());
        registerParticle(ParticleTypes.DRIP_LAVA.getParticleID(), new EntityDropParticleFX.LavaFactory());
        registerParticle(ParticleTypes.VILLAGER_ANGRY.getParticleID(), new EntityHeartFX.AngryVillagerFactory());
        registerParticle(ParticleTypes.VILLAGER_HAPPY.getParticleID(), new EntityAuraFX.HappyVillagerFactory());
        registerParticle(ParticleTypes.TOWN_AURA.getParticleID(), new EntityAuraFX.Factory());
        registerParticle(ParticleTypes.NOTE.getParticleID(), new EntityNoteFX.Factory());
        registerParticle(ParticleTypes.PORTAL.getParticleID(), new EntityPortalFX.Factory());
        registerParticle(ParticleTypes.ENCHANTMENT_TABLE.getParticleID(), new EntityEnchantmentTableParticleFX.EnchantmentTable());
        registerParticle(ParticleTypes.FLAME.getParticleID(), new EntityFlameFX.Factory());
        registerParticle(ParticleTypes.LAVA.getParticleID(), new EntityLavaFX.Factory());
        registerParticle(ParticleTypes.FOOTSTEP.getParticleID(), new EntityFootStepFX.Factory());
        registerParticle(ParticleTypes.CLOUD.getParticleID(), new EntityCloudFX.Factory());
        registerParticle(ParticleTypes.REDSTONE.getParticleID(), new EntityReddustFX.Factory());
        registerParticle(ParticleTypes.SNOWBALL.getParticleID(), new EntityBreakingFX.SnowballFactory());
        registerParticle(ParticleTypes.SNOW_SHOVEL.getParticleID(), new EntitySnowShovelFX.Factory());
        registerParticle(ParticleTypes.SLIME.getParticleID(), new EntityBreakingFX.SlimeFactory());
        registerParticle(ParticleTypes.HEART.getParticleID(), new EntityHeartFX.Factory());
        registerParticle(ParticleTypes.BARRIER.getParticleID(), new Barrier.Factory());
        registerParticle(ParticleTypes.ITEM_CRACK.getParticleID(), new EntityBreakingFX.Factory());
        registerParticle(ParticleTypes.BLOCK_CRACK.getParticleID(), new EntityDiggingFX.Factory());
        registerParticle(ParticleTypes.BLOCK_DUST.getParticleID(), new EntityBlockDustFX.Factory());
        registerParticle(ParticleTypes.EXPLOSION_HUGE.getParticleID(), new EntityHugeExplodeFX.Factory());
        registerParticle(ParticleTypes.EXPLOSION_LARGE.getParticleID(), new EntityLargeExplodeFX.Factory());
        registerParticle(ParticleTypes.FIREWORKS_SPARK.getParticleID(), new EntityFirework.Factory());
        registerParticle(ParticleTypes.MOB_APPEARANCE.getParticleID(), new MobAppearance.Factory());
    }

    public void registerParticle(int id, IParticleFactory particleFactory) {
        particleTypes.put(id, particleFactory);
    }

    public void emitParticleAtEntity(Entity entityIn, ParticleTypes particleTypes) {
        particleEmitters.add(new EntityParticleEmitter(worldObj, entityIn, particleTypes));
    }

    public EntityFX spawnEffectParticle(int particleId, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        IParticleFactory iparticlefactory = particleTypes.get(particleId);

        if (iparticlefactory != null) {
            EntityFX entityfx = iparticlefactory.getEntityFX(particleId, worldObj, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);

            if (entityfx != null) {
                addEffect(entityfx);
                return entityfx;
            }
        }

        return null;
    }

    public void addEffect(EntityFX effect) {
        if (effect != null) {
            if (!(effect instanceof EntityFirework.SparkFX) || Config.isFireworkParticles()) {
                int i = effect.getFXLayer();
                int j = effect.getAlpha() != 1.0F ? 0 : 1;

                if (fxLayers[i][j].size() >= 4000) {
                    fxLayers[i][j].removeFirst();
                }

                fxLayers[i][j].add(effect);
            }
        }
    }

    public void updateEffects() {
        for (int i = 0; i < 4; ++i) {
            updateEffectLayer(i);
        }

        List<EntityParticleEmitter> list = new ArrayList<>();

        for (EntityParticleEmitter entityparticleemitter : particleEmitters) {
            entityparticleemitter.onUpdate();

            if (entityparticleemitter.isDead) {
                list.add(entityparticleemitter);
            }
        }

        particleEmitters.removeAll(list);
    }

    private void updateEffectLayer(int layer) {
        for (int i = 0; i < 2; ++i) {
            updateEffectAlphaLayer(fxLayers[layer][i]);
        }
    }

    private void updateEffectAlphaLayer(List<EntityFX> entitiesFX) {
        List<EntityFX> list = new ArrayList<>();
        long i = System.currentTimeMillis();
        int j = entitiesFX.size();

        for (EntityFX entityfx : Lists.newArrayList(entitiesFX)) {
            tickParticle(entityfx);

            if (entityfx.isDead) {
                list.add(entityfx);
            }

            --j;

            if (System.currentTimeMillis() > i + 20L) {
                break;
            }
        }

        if (j > 0) {
            int l = j;

            for (Iterator<EntityFX> iterator = entitiesFX.iterator(); iterator.hasNext() && l > 0; --l) {
                EntityFX entityfx1 = iterator.next();
                entityfx1.setDead();
                iterator.remove();
            }
        }

        entitiesFX.removeAll(list);
    }

    private void tickParticle(final EntityFX particle) {
        try {
            particle.onUpdate();
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Ticking Particle");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being ticked");
            final int i = particle.getFXLayer();
            crashreportcategory.addCrashSectionCallable("Particle", particle::toString);
            crashreportcategory.addCrashSectionCallable("Particle Type", () -> i == 0 ? "MISC_TEXTURE" : (i == 1 ? "TERRAIN_TEXTURE" : (i == 3 ? "ENTITY_PARTICLE_TEXTURE" : "Unknown - " + i)));
            throw new ReportedException(crashreport);
        }
    }

    public void renderParticles(Entity entityIn, float partialTicks) {
        float f = ActiveRenderInfo.getRotationX();
        float f1 = ActiveRenderInfo.getRotationZ();
        float f2 = ActiveRenderInfo.getRotationYZ();
        float f3 = ActiveRenderInfo.getRotationXY();
        float f4 = ActiveRenderInfo.getRotationXZ();
        EntityFX.interpPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
        EntityFX.interpPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
        EntityFX.interpPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.alphaFunc(516, 0.003921569F);
        Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(worldObj, entityIn, partialTicks);
        boolean flag = block.getMaterial() == Material.water;

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 2; ++j) {
                final int i_f = i;

                if (!fxLayers[i][j].isEmpty()) {
                    switch (j) {
                        case 0:
                            GlStateManager.depthMask(false);
                            break;

                        case 1:
                            GlStateManager.depthMask(true);
                    }

                    switch (i) {
                        case 0:
                        default:
                            renderer.bindTexture(particleTextures);
                            break;

                        case 1:
                            renderer.bindTexture(TextureMap.locationBlocksTexture);
                    }

                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    Tessellator tessellator = Tessellator.getInstance();
                    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                    worldrenderer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

                    for (int k = 0; k < fxLayers[i][j].size(); ++k) {
                        final EntityFX entityfx = fxLayers[i][j].get(k);

                        try {
                            if (flag || !(entityfx instanceof EntitySuspendFX)) {
                                entityfx.renderParticle(worldrenderer, entityIn, partialTicks, f, f4, f1, f2, f3);
                            }
                        } catch (Throwable throwable) {
                            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Particle");
                            CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being rendered");
                            crashreportcategory.addCrashSectionCallable("Particle", () -> entityfx.toString());
                            crashreportcategory.addCrashSectionCallable("Particle Type", () -> i_f == 0 ? "MISC_TEXTURE" : i_f == 1 ? "TERRAIN_TEXTURE" : "Unknown - " + i_f);
                            throw new ReportedException(crashreport);
                        }
                    }

                    tessellator.draw();
                }
            }
        }

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(516, 0.1F);
    }

    public void renderLitParticles(Entity entityIn, float partialTick) {
        float f = 0.017453292F;
        float f1 = MathHelper.cos(entityIn.rotationYaw * 0.017453292F);
        float f2 = MathHelper.sin(entityIn.rotationYaw * 0.017453292F);
        float f3 = -f2 * MathHelper.sin(entityIn.rotationPitch * 0.017453292F);
        float f4 = f1 * MathHelper.sin(entityIn.rotationPitch * 0.017453292F);
        float f5 = MathHelper.cos(entityIn.rotationPitch * 0.017453292F);

        for (int i = 0; i < 2; ++i) {
            List<EntityFX> list = fxLayers[3][i];

            if (!list.isEmpty()) {
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldrenderer = tessellator.getWorldRenderer();

                for (EntityFX entityfx : list) {
                    entityfx.renderParticle(worldrenderer, entityIn, partialTick, f1, f5, f2, f3, f4);
                }
            }
        }
    }

    public void clearEffects(World worldIn) {
        worldObj = worldIn;

        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 2; ++j) {
                fxLayers[i][j].clear();
            }
        }

        particleEmitters.clear();
    }

    public void addBlockDestroyEffects(BlockPos pos, IBlockState state) {
        boolean flag;

        flag = state.getBlock().getMaterial() != Material.air;

        if (flag) {
            state = state.getBlock().getActualState(state, worldObj, pos);
            int l = 4;

            for (int i = 0; i < l; ++i) {
                for (int j = 0; j < l; ++j) {
                    for (int k = 0; k < l; ++k) {
                        double d0 = (double) pos.getX() + ((double) i + 0.5D) / (double) l;
                        double d1 = (double) pos.getY() + ((double) j + 0.5D) / (double) l;
                        double d2 = (double) pos.getZ() + ((double) k + 0.5D) / (double) l;
                        addEffect((new EntityDiggingFX(worldObj, d0, d1, d2, d0 - (double) pos.getX() - 0.5D, d1 - (double) pos.getY() - 0.5D, d2 - (double) pos.getZ() - 0.5D, state)).setBlockPos(pos));
                    }
                }
            }
        }
    }

    public void addBlockHitEffects(BlockPos pos, Direction side) {
        IBlockState iblockstate = worldObj.getBlockState(pos);
        Block block = iblockstate.getBlock();

        if (block.getRenderType() != -1) {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            float f = 0.1F;
            double d0 = (double) i + rand.nextDouble() * (block.getBlockBoundsMaxX() - block.getBlockBoundsMinX() - (double) (f * 2.0F)) + (double) f + block.getBlockBoundsMinX();
            double d1 = (double) j + rand.nextDouble() * (block.getBlockBoundsMaxY() - block.getBlockBoundsMinY() - (double) (f * 2.0F)) + (double) f + block.getBlockBoundsMinY();
            double d2 = (double) k + rand.nextDouble() * (block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ() - (double) (f * 2.0F)) + (double) f + block.getBlockBoundsMinZ();

            if (side == Direction.DOWN) {
                d1 = (double) j + block.getBlockBoundsMinY() - (double) f;
            }

            if (side == Direction.UP) {
                d1 = (double) j + block.getBlockBoundsMaxY() + (double) f;
            }

            if (side == Direction.NORTH) {
                d2 = (double) k + block.getBlockBoundsMinZ() - (double) f;
            }

            if (side == Direction.SOUTH) {
                d2 = (double) k + block.getBlockBoundsMaxZ() + (double) f;
            }

            if (side == Direction.WEST) {
                d0 = (double) i + block.getBlockBoundsMinX() - (double) f;
            }

            if (side == Direction.EAST) {
                d0 = (double) i + block.getBlockBoundsMaxX() + (double) f;
            }

            addEffect((new EntityDiggingFX(worldObj, d0, d1, d2, 0.0D, 0.0D, 0.0D, iblockstate)).setBlockPos(pos).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
        }
    }

    public void moveToAlphaLayer(EntityFX effect) {
        moveToLayer(effect, 1, 0);
    }

    public void moveToNoAlphaLayer(EntityFX effect) {
        moveToLayer(effect, 0, 1);
    }

    private void moveToLayer(EntityFX effect, int layerFrom, int layerTo) {
        for (int i = 0; i < 4; ++i) {
            if (fxLayers[i][layerFrom].contains(effect)) {
                fxLayers[i][layerFrom].remove(effect);
                fxLayers[i][layerTo].add(effect);
            }
        }
    }

    public String getStatistics() {
        int i = 0;

        for (int j = 0; j < 4; ++j) {
            for (int k = 0; k < 2; ++k) {
                i += fxLayers[j][k].size();
            }
        }

        return "" + i;
    }

    public void addBlockHitEffects(BlockPos p_addBlockHitEffects_1_, MovingObjectPosition p_addBlockHitEffects_2_) {
        IBlockState iblockstate = worldObj.getBlockState(p_addBlockHitEffects_1_);

        if (iblockstate != null) {
            addBlockHitEffects(p_addBlockHitEffects_1_, p_addBlockHitEffects_2_.sideHit);
        }
    }
}
