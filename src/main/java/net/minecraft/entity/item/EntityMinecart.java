package net.minecraft.entity.item;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailPowered;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityMinecartCommandBlock;
import net.minecraft.entity.ai.EntityMinecartMobSpawner;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Map;

public abstract class EntityMinecart extends Entity implements IWorldNameable {
    private static final int[][][] matrix = new int[][][]{{{0, 0, -1}, {0, 0, 1}}, {{-1, 0, 0}, {1, 0, 0}}, {{-1, -1, 0}, {1, 0, 0}}, {{-1, 0, 0}, {1, -1, 0}}, {{0, 0, -1}, {0, -1, 1}}, {{0, -1, -1}, {0, 0, 1}}, {{0, 0, 1}, {1, 0, 0}}, {{0, 0, 1}, {-1, 0, 0}}, {{0, 0, -1}, {-1, 0, 0}}, {{0, 0, -1}, {1, 0, 0}}};
    private boolean isInReverse;
    private String entityName;
    private int turnProgress;
    private double minecartX;
    private double minecartY;
    private double minecartZ;
    private double minecartYaw;
    private double minecartPitch;
    private double velocityX;
    private double velocityY;
    private double velocityZ;

    public EntityMinecart(World worldIn) {
        super(worldIn);
        preventEntitySpawning = true;
        setSize(0.98F, 0.7F);
    }

    public EntityMinecart(World worldIn, double x, double y, double z) {
        this(worldIn);
        setPosition(x, y, z);
        motionX = 0.0D;
        motionY = 0.0D;
        motionZ = 0.0D;
        prevPosX = x;
        prevPosY = y;
        prevPosZ = z;
    }

    public static EntityMinecart getMinecart(World worldIn, double x, double y, double z, EntityMinecart.EnumMinecartType type) {
        return switch (type) {
            case CHEST -> new EntityMinecartChest(worldIn, x, y, z);
            case FURNACE -> new EntityMinecartFurnace(worldIn, x, y, z);
            case TNT -> new EntityMinecartTNT(worldIn, x, y, z);
            case SPAWNER -> new EntityMinecartMobSpawner(worldIn, x, y, z);
            case HOPPER -> new EntityMinecartHopper(worldIn, x, y, z);
            case COMMAND_BLOCK -> new EntityMinecartCommandBlock(worldIn, x, y, z);
            default -> new EntityMinecartEmpty(worldIn, x, y, z);
        };
    }

    protected boolean canTriggerWalking() {
        return false;
    }

    protected void entityInit() {
        dataWatcher.addObject(17, 0);
        dataWatcher.addObject(18, 1);
        dataWatcher.addObject(19, 0.0F);
        dataWatcher.addObject(20, 0);
        dataWatcher.addObject(21, 6);
        dataWatcher.addObject(22, (byte) 0);
    }

    public AxisAlignedBB getCollisionBox(Entity entityIn) {
        return entityIn.canBePushed() ? entityIn.getEntityBoundingBox() : null;
    }

    public boolean canBePushed() {
        return true;
    }

    public double getMountedYOffset() {
        return 0.0D;
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (!worldObj.isRemote && !isDead) {
            if (isEntityInvulnerable(source)) {
                return false;
            } else {
                setRollingDirection(-getRollingDirection());
                setRollingAmplitude(10);
                setBeenAttacked();
                setDamage(getDamage() + amount * 10.0F);
                boolean flag = source.getEntity() instanceof EntityPlayer && ((EntityPlayer) source.getEntity()).capabilities.isCreativeMode;

                if (flag || getDamage() > 40.0F) {
                    if (riddenByEntity != null) {
                        riddenByEntity.mountEntity(null);
                    }

                    if (flag && !hasCustomName()) {
                        setDead();
                    } else {
                        killMinecart(source);
                    }
                }

                return true;
            }
        } else {
            return true;
        }
    }

    public void killMinecart(DamageSource source) {
        setDead();

        if (worldObj.getGameRules().getBoolean("doEntityDrops")) {
            ItemStack itemstack = new ItemStack(Items.minecart, 1);

            if (entityName != null) {
                itemstack.setStackDisplayName(entityName);
            }

            entityDropItem(itemstack, 0.0F);
        }
    }

    public void performHurtAnimation() {
        setRollingDirection(-getRollingDirection());
        setRollingAmplitude(10);
        setDamage(getDamage() + getDamage() * 10.0F);
    }

    public boolean canBeCollidedWith() {
        return !isDead;
    }

    public void setDead() {
        super.setDead();
    }

    public void onUpdate() {
        if (getRollingAmplitude() > 0) {
            setRollingAmplitude(getRollingAmplitude() - 1);
        }

        if (getDamage() > 0.0F) {
            setDamage(getDamage() - 1.0F);
        }

        if (posY < -64.0D) {
            kill();
        }

        if (!worldObj.isRemote && worldObj instanceof WorldServer) {
            worldObj.theProfiler.startSection("portal");
            MinecraftServer minecraftserver = ((WorldServer) worldObj).getMinecraftServer();
            int i = getMaxInPortalTime();

            if (inPortal) {
                if (minecraftserver.getAllowNether()) {
                    if (ridingEntity == null && portalCounter++ >= i) {
                        portalCounter = i;
                        timeUntilPortal = getPortalCooldown();
                        int j;

                        if (worldObj.provider.getDimensionId() == -1) {
                            j = 0;
                        } else {
                            j = -1;
                        }

                        travelToDimension(j);
                    }

                    inPortal = false;
                }
            } else {
                if (portalCounter > 0) {
                    portalCounter -= 4;
                }

                if (portalCounter < 0) {
                    portalCounter = 0;
                }
            }

            if (timeUntilPortal > 0) {
                --timeUntilPortal;
            }

            worldObj.theProfiler.endSection();
        }

        if (worldObj.isRemote) {
            if (turnProgress > 0) {
                double d4 = posX + (minecartX - posX) / (double) turnProgress;
                double d5 = posY + (minecartY - posY) / (double) turnProgress;
                double d6 = posZ + (minecartZ - posZ) / (double) turnProgress;
                double d1 = MathHelper.wrapAngleTo180_double(minecartYaw - (double) rotationYaw);
                rotationYaw = (float) ((double) rotationYaw + d1 / (double) turnProgress);
                rotationPitch = (float) ((double) rotationPitch + (minecartPitch - (double) rotationPitch) / (double) turnProgress);
                --turnProgress;
                setPosition(d4, d5, d6);
                setRotation(rotationYaw, rotationPitch);
            } else {
                setPosition(posX, posY, posZ);
                setRotation(rotationYaw, rotationPitch);
            }
        } else {
            prevPosX = posX;
            prevPosY = posY;
            prevPosZ = posZ;
            motionY -= 0.03999999910593033D;
            int k = MathHelper.floor_double(posX);
            int l = MathHelper.floor_double(posY);
            int i1 = MathHelper.floor_double(posZ);

            if (BlockRailBase.isRailBlock(worldObj, new BlockPos(k, l - 1, i1))) {
                --l;
            }

            BlockPos blockpos = new BlockPos(k, l, i1);
            IBlockState iblockstate = worldObj.getBlockState(blockpos);

            if (BlockRailBase.isRailBlock(iblockstate)) {
                func_180460_a(blockpos, iblockstate);

                if (iblockstate.getBlock() == Blocks.activator_rail) {
                    onActivatorRailPass(k, l, i1, iblockstate.getValue(BlockRailPowered.POWERED));
                }
            } else {
                moveDerailedMinecart();
            }

            doBlockCollisions();
            rotationPitch = 0.0F;
            double d0 = prevPosX - posX;
            double d2 = prevPosZ - posZ;

            if (d0 * d0 + d2 * d2 > 0.001D) {
                rotationYaw = (float) (MathHelper.atan2(d2, d0) * 180.0D / Math.PI);

                if (isInReverse) {
                    rotationYaw += 180.0F;
                }
            }

            double d3 = MathHelper.wrapAngleTo180_float(rotationYaw - prevRotationYaw);

            if (d3 < -170.0D || d3 >= 170.0D) {
                rotationYaw += 180.0F;
                isInReverse = !isInReverse;
            }

            setRotation(rotationYaw, rotationPitch);

            for (Entity entity : worldObj.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().expand(0.20000000298023224D, 0.0D, 0.20000000298023224D))) {
                if (entity != riddenByEntity && entity.canBePushed() && entity instanceof EntityMinecart) {
                    entity.applyEntityCollision(this);
                }
            }

            if (riddenByEntity != null && riddenByEntity.isDead) {
                if (riddenByEntity.ridingEntity == this) {
                    riddenByEntity.ridingEntity = null;
                }

                riddenByEntity = null;
            }

            handleWaterMovement();
        }
    }

    protected double getMaximumSpeed() {
        return 0.4D;
    }

    public void onActivatorRailPass(int x, int y, int z, boolean receivingPower) {
    }

    protected void moveDerailedMinecart() {
        double d0 = getMaximumSpeed();
        motionX = MathHelper.clamp_double(motionX, -d0, d0);
        motionZ = MathHelper.clamp_double(motionZ, -d0, d0);

        if (onGround) {
            motionX *= 0.5D;
            motionY *= 0.5D;
            motionZ *= 0.5D;
        }

        moveEntity(motionX, motionY, motionZ);

        if (!onGround) {
            motionX *= 0.949999988079071D;
            motionY *= 0.949999988079071D;
            motionZ *= 0.949999988079071D;
        }
    }

    @SuppressWarnings("incomplete-switch")
    protected void func_180460_a(BlockPos p_180460_1_, IBlockState p_180460_2_) {
        fallDistance = 0.0F;
        Vec3 vec3 = func_70489_a(posX, posY, posZ);
        posY = p_180460_1_.getY();
        boolean flag = false;
        boolean flag1 = false;
        BlockRailBase blockrailbase = (BlockRailBase) p_180460_2_.getBlock();

        if (blockrailbase == Blocks.golden_rail) {
            flag = p_180460_2_.getValue(BlockRailPowered.POWERED);
            flag1 = !flag;
        }

        double d0 = 0.0078125D;
        BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = p_180460_2_.getValue(blockrailbase.getShapeProperty());

        switch (blockrailbase$enumraildirection) {
            case ASCENDING_EAST:
                motionX -= 0.0078125D;
                ++posY;
                break;

            case ASCENDING_WEST:
                motionX += 0.0078125D;
                ++posY;
                break;

            case ASCENDING_NORTH:
                motionZ += 0.0078125D;
                ++posY;
                break;

            case ASCENDING_SOUTH:
                motionZ -= 0.0078125D;
                ++posY;
        }

        int[][] aint = matrix[blockrailbase$enumraildirection.getMetadata()];
        double d1 = aint[1][0] - aint[0][0];
        double d2 = aint[1][2] - aint[0][2];
        double d3 = Math.sqrt(d1 * d1 + d2 * d2);
        double d4 = motionX * d1 + motionZ * d2;

        if (d4 < 0.0D) {
            d1 = -d1;
            d2 = -d2;
        }

        double d5 = Math.sqrt(motionX * motionX + motionZ * motionZ);

        if (d5 > 2.0D) {
            d5 = 2.0D;
        }

        motionX = d5 * d1 / d3;
        motionZ = d5 * d2 / d3;

        if (riddenByEntity instanceof EntityLivingBase) {
            double d6 = ((EntityLivingBase) riddenByEntity).moveForward;

            if (d6 > 0.0D) {
                double d7 = -Math.sin(riddenByEntity.rotationYaw * (float) Math.PI / 180.0F);
                double d8 = Math.cos(riddenByEntity.rotationYaw * (float) Math.PI / 180.0F);
                double d9 = motionX * motionX + motionZ * motionZ;

                if (d9 < 0.01D) {
                    motionX += d7 * 0.1D;
                    motionZ += d8 * 0.1D;
                    flag1 = false;
                }
            }
        }

        if (flag1) {
            double d17 = Math.sqrt(motionX * motionX + motionZ * motionZ);

            if (d17 < 0.03D) {
                motionX *= 0.0D;
                motionY *= 0.0D;
                motionZ *= 0.0D;
            } else {
                motionX *= 0.5D;
                motionY *= 0.0D;
                motionZ *= 0.5D;
            }
        }

        double d18;
        double d19 = (double) p_180460_1_.getX() + 0.5D + (double) aint[0][0] * 0.5D;
        double d20 = (double) p_180460_1_.getZ() + 0.5D + (double) aint[0][2] * 0.5D;
        double d21 = (double) p_180460_1_.getX() + 0.5D + (double) aint[1][0] * 0.5D;
        double d10 = (double) p_180460_1_.getZ() + 0.5D + (double) aint[1][2] * 0.5D;
        d1 = d21 - d19;
        d2 = d10 - d20;

        if (d1 == 0.0D) {
            posX = (double) p_180460_1_.getX() + 0.5D;
            d18 = posZ - (double) p_180460_1_.getZ();
        } else if (d2 == 0.0D) {
            posZ = (double) p_180460_1_.getZ() + 0.5D;
            d18 = posX - (double) p_180460_1_.getX();
        } else {
            double d11 = posX - d19;
            double d12 = posZ - d20;
            d18 = (d11 * d1 + d12 * d2) * 2.0D;
        }

        posX = d19 + d1 * d18;
        posZ = d20 + d2 * d18;
        setPosition(posX, posY, posZ);
        double d22 = motionX;
        double d23 = motionZ;

        if (riddenByEntity != null) {
            d22 *= 0.75D;
            d23 *= 0.75D;
        }

        double d13 = getMaximumSpeed();
        d22 = MathHelper.clamp_double(d22, -d13, d13);
        d23 = MathHelper.clamp_double(d23, -d13, d13);
        moveEntity(d22, 0.0D, d23);

        if (aint[0][1] != 0 && MathHelper.floor_double(posX) - p_180460_1_.getX() == aint[0][0] && MathHelper.floor_double(posZ) - p_180460_1_.getZ() == aint[0][2]) {
            setPosition(posX, posY + (double) aint[0][1], posZ);
        } else if (aint[1][1] != 0 && MathHelper.floor_double(posX) - p_180460_1_.getX() == aint[1][0] && MathHelper.floor_double(posZ) - p_180460_1_.getZ() == aint[1][2]) {
            setPosition(posX, posY + (double) aint[1][1], posZ);
        }

        applyDrag();
        Vec3 vec31 = func_70489_a(posX, posY, posZ);

        if (vec31 != null && vec3 != null) {
            double d14 = (vec3.yCoord() - vec31.yCoord()) * 0.05D;
            d5 = Math.sqrt(motionX * motionX + motionZ * motionZ);

            if (d5 > 0.0D) {
                motionX = motionX / d5 * (d5 + d14);
                motionZ = motionZ / d5 * (d5 + d14);
            }

            setPosition(posX, vec31.yCoord(), posZ);
        }

        int j = MathHelper.floor_double(posX);
        int i = MathHelper.floor_double(posZ);

        if (j != p_180460_1_.getX() || i != p_180460_1_.getZ()) {
            d5 = Math.sqrt(motionX * motionX + motionZ * motionZ);
            motionX = d5 * (double) (j - p_180460_1_.getX());
            motionZ = d5 * (double) (i - p_180460_1_.getZ());
        }

        if (flag) {
            double d15 = Math.sqrt(motionX * motionX + motionZ * motionZ);

            if (d15 > 0.01D) {
                double d16 = 0.06D;
                motionX += motionX / d15 * d16;
                motionZ += motionZ / d15 * d16;
            } else if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.EAST_WEST) {
                if (worldObj.getBlockState(p_180460_1_.west()).getBlock().isNormalCube()) {
                    motionX = 0.02D;
                } else if (worldObj.getBlockState(p_180460_1_.east()).getBlock().isNormalCube()) {
                    motionX = -0.02D;
                }
            } else if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.NORTH_SOUTH) {
                if (worldObj.getBlockState(p_180460_1_.north()).getBlock().isNormalCube()) {
                    motionZ = 0.02D;
                } else if (worldObj.getBlockState(p_180460_1_.south()).getBlock().isNormalCube()) {
                    motionZ = -0.02D;
                }
            }
        }
    }

    protected void applyDrag() {
        if (riddenByEntity != null) {
            motionX *= 0.996999979019165D;
            motionY *= 0.0D;
            motionZ *= 0.996999979019165D;
        } else {
            motionX *= 0.9599999785423279D;
            motionY *= 0.0D;
            motionZ *= 0.9599999785423279D;
        }
    }

    public Vec3 func_70495_a(double p_70495_1_, double p_70495_3_, double p_70495_5_, double p_70495_7_) {
        int i = MathHelper.floor_double(p_70495_1_);
        int j = MathHelper.floor_double(p_70495_3_);
        int k = MathHelper.floor_double(p_70495_5_);

        if (BlockRailBase.isRailBlock(worldObj, new BlockPos(i, j - 1, k))) {
            --j;
        }

        IBlockState iblockstate = worldObj.getBlockState(new BlockPos(i, j, k));

        if (BlockRailBase.isRailBlock(iblockstate)) {
            BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = iblockstate.getValue(((BlockRailBase) iblockstate.getBlock()).getShapeProperty());
            p_70495_3_ = j;

            if (blockrailbase$enumraildirection.isAscending()) {
                p_70495_3_ = j + 1;
            }

            int[][] aint = matrix[blockrailbase$enumraildirection.getMetadata()];
            double d0 = aint[1][0] - aint[0][0];
            double d1 = aint[1][2] - aint[0][2];
            double d2 = Math.sqrt(d0 * d0 + d1 * d1);
            d0 = d0 / d2;
            d1 = d1 / d2;
            p_70495_1_ = p_70495_1_ + d0 * p_70495_7_;
            p_70495_5_ = p_70495_5_ + d1 * p_70495_7_;

            if (aint[0][1] != 0 && MathHelper.floor_double(p_70495_1_) - i == aint[0][0] && MathHelper.floor_double(p_70495_5_) - k == aint[0][2]) {
                p_70495_3_ += aint[0][1];
            } else if (aint[1][1] != 0 && MathHelper.floor_double(p_70495_1_) - i == aint[1][0] && MathHelper.floor_double(p_70495_5_) - k == aint[1][2]) {
                p_70495_3_ += aint[1][1];
            }

            return func_70489_a(p_70495_1_, p_70495_3_, p_70495_5_);
        } else {
            return null;
        }
    }

    public Vec3 func_70489_a(double p_70489_1_, double p_70489_3_, double p_70489_5_) {
        int i = MathHelper.floor_double(p_70489_1_);
        int j = MathHelper.floor_double(p_70489_3_);
        int k = MathHelper.floor_double(p_70489_5_);

        if (BlockRailBase.isRailBlock(worldObj, new BlockPos(i, j - 1, k))) {
            --j;
        }

        IBlockState iblockstate = worldObj.getBlockState(new BlockPos(i, j, k));

        if (BlockRailBase.isRailBlock(iblockstate)) {
            BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = iblockstate.getValue(((BlockRailBase) iblockstate.getBlock()).getShapeProperty());
            int[][] aint = matrix[blockrailbase$enumraildirection.getMetadata()];
            double d0;
            double d1 = (double) i + 0.5D + (double) aint[0][0] * 0.5D;
            double d2 = (double) j + 0.0625D + (double) aint[0][1] * 0.5D;
            double d3 = (double) k + 0.5D + (double) aint[0][2] * 0.5D;
            double d4 = (double) i + 0.5D + (double) aint[1][0] * 0.5D;
            double d5 = (double) j + 0.0625D + (double) aint[1][1] * 0.5D;
            double d6 = (double) k + 0.5D + (double) aint[1][2] * 0.5D;
            double d7 = d4 - d1;
            double d8 = (d5 - d2) * 2.0D;
            double d9 = d6 - d3;

            if (d7 == 0.0D) {
                d0 = p_70489_5_ - (double) k;
            } else if (d9 == 0.0D) {
                d0 = p_70489_1_ - (double) i;
            } else {
                double d10 = p_70489_1_ - d1;
                double d11 = p_70489_5_ - d3;
                d0 = (d10 * d7 + d11 * d9) * 2.0D;
            }

            p_70489_1_ = d1 + d7 * d0;
            p_70489_3_ = d2 + d8 * d0;
            p_70489_5_ = d3 + d9 * d0;

            if (d8 < 0.0D) {
                ++p_70489_3_;
            }

            if (d8 > 0.0D) {
                p_70489_3_ += 0.5D;
            }

            return new Vec3(p_70489_1_, p_70489_3_, p_70489_5_);
        } else {
            return null;
        }
    }

    protected void readEntityFromNBT(NBTTagCompound tagCompund) {
        if (tagCompund.getBoolean("CustomDisplayTile")) {
            int i = tagCompund.getInteger("DisplayData");

            if (tagCompund.hasKey("DisplayTile", 8)) {
                Block block = Block.getBlockFromName(tagCompund.getString("DisplayTile"));

                if (block == null) {
                    func_174899_a(Blocks.air.getDefaultState());
                } else {
                    func_174899_a(block.getStateFromMeta(i));
                }
            } else {
                Block block1 = Block.getBlockById(tagCompund.getInteger("DisplayTile"));

                if (block1 == null) {
                    func_174899_a(Blocks.air.getDefaultState());
                } else {
                    func_174899_a(block1.getStateFromMeta(i));
                }
            }

            setDisplayTileOffset(tagCompund.getInteger("DisplayOffset"));
        }

        if (tagCompund.hasKey("CustomName", 8) && !tagCompund.getString("CustomName").isEmpty()) {
            entityName = tagCompund.getString("CustomName");
        }
    }

    protected void writeEntityToNBT(NBTTagCompound tagCompound) {
        if (hasDisplayTile()) {
            tagCompound.setBoolean("CustomDisplayTile", true);
            IBlockState iblockstate = getDisplayTile();
            ResourceLocation resourcelocation = Block.blockRegistry.getNameForObject(iblockstate.getBlock());
            tagCompound.setString("DisplayTile", resourcelocation == null ? "" : resourcelocation.toString());
            tagCompound.setInteger("DisplayData", iblockstate.getBlock().getMetaFromState(iblockstate));
            tagCompound.setInteger("DisplayOffset", getDisplayTileOffset());
        }

        if (entityName != null && !entityName.isEmpty()) {
            tagCompound.setString("CustomName", entityName);
        }
    }

    public void applyEntityCollision(Entity entityIn) {
        if (!worldObj.isRemote) {
            if (!entityIn.noClip && !noClip) {
                if (entityIn != riddenByEntity) {
                    if (entityIn instanceof EntityLivingBase && !(entityIn instanceof EntityPlayer) && !(entityIn instanceof EntityIronGolem) && getMinecartType() == EntityMinecart.EnumMinecartType.RIDEABLE && motionX * motionX + motionZ * motionZ > 0.01D && riddenByEntity == null && entityIn.ridingEntity == null) {
                        entityIn.mountEntity(this);
                    }

                    double d0 = entityIn.posX - posX;
                    double d1 = entityIn.posZ - posZ;
                    double d2 = d0 * d0 + d1 * d1;

                    if (d2 >= 9.999999747378752E-5D) {
                        d2 = MathHelper.sqrt_double(d2);
                        d0 = d0 / d2;
                        d1 = d1 / d2;
                        double d3 = 1.0D / d2;

                        if (d3 > 1.0D) {
                            d3 = 1.0D;
                        }

                        d0 = d0 * d3;
                        d1 = d1 * d3;
                        d0 = d0 * 0.10000000149011612D;
                        d1 = d1 * 0.10000000149011612D;
                        d0 = d0 * (double) (1.0F - entityCollisionReduction);
                        d1 = d1 * (double) (1.0F - entityCollisionReduction);
                        d0 = d0 * 0.5D;
                        d1 = d1 * 0.5D;

                        if (entityIn instanceof EntityMinecart) {
                            double d4 = entityIn.posX - posX;
                            double d5 = entityIn.posZ - posZ;
                            Vec3 vec3 = (new Vec3(d4, 0.0D, d5)).normalize();
                            Vec3 vec31 = (new Vec3(MathHelper.cos(rotationYaw * (float) Math.PI / 180.0F), 0.0D, MathHelper.sin(rotationYaw * (float) Math.PI / 180.0F))).normalize();
                            double d6 = Math.abs(vec3.dotProduct(vec31));

                            if (d6 < 0.800000011920929D) {
                                return;
                            }

                            double d7 = entityIn.motionX + motionX;
                            double d8 = entityIn.motionZ + motionZ;

                            if (((EntityMinecart) entityIn).getMinecartType() == EntityMinecart.EnumMinecartType.FURNACE && getMinecartType() != EntityMinecart.EnumMinecartType.FURNACE) {
                                motionX *= 0.20000000298023224D;
                                motionZ *= 0.20000000298023224D;
                                addVelocity(entityIn.motionX - d0, 0.0D, entityIn.motionZ - d1);
                                entityIn.motionX *= 0.949999988079071D;
                                entityIn.motionZ *= 0.949999988079071D;
                            } else if (((EntityMinecart) entityIn).getMinecartType() != EntityMinecart.EnumMinecartType.FURNACE && getMinecartType() == EntityMinecart.EnumMinecartType.FURNACE) {
                                entityIn.motionX *= 0.20000000298023224D;
                                entityIn.motionZ *= 0.20000000298023224D;
                                entityIn.addVelocity(motionX + d0, 0.0D, motionZ + d1);
                                motionX *= 0.949999988079071D;
                                motionZ *= 0.949999988079071D;
                            } else {
                                d7 = d7 / 2.0D;
                                d8 = d8 / 2.0D;
                                motionX *= 0.20000000298023224D;
                                motionZ *= 0.20000000298023224D;
                                addVelocity(d7 - d0, 0.0D, d8 - d1);
                                entityIn.motionX *= 0.20000000298023224D;
                                entityIn.motionZ *= 0.20000000298023224D;
                                entityIn.addVelocity(d7 + d0, 0.0D, d8 + d1);
                            }
                        } else {
                            addVelocity(-d0, 0.0D, -d1);
                            entityIn.addVelocity(d0 / 4.0D, 0.0D, d1 / 4.0D);
                        }
                    }
                }
            }
        }
    }

    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean p_180426_10_) {
        minecartX = x;
        minecartY = y;
        minecartZ = z;
        minecartYaw = yaw;
        minecartPitch = pitch;
        turnProgress = posRotationIncrements + 2;
        motionX = velocityX;
        motionY = velocityY;
        motionZ = velocityZ;
    }

    public void setVelocity(double x, double y, double z) {
        velocityX = motionX = x;
        velocityY = motionY = y;
        velocityZ = motionZ = z;
    }

    public float getDamage() {
        return dataWatcher.getWatchableObjectFloat(19);
    }

    public void setDamage(float p_70492_1_) {
        dataWatcher.updateObject(19, p_70492_1_);
    }

    public int getRollingAmplitude() {
        return dataWatcher.getWatchableObjectInt(17);
    }

    public void setRollingAmplitude(int p_70497_1_) {
        dataWatcher.updateObject(17, p_70497_1_);
    }

    public int getRollingDirection() {
        return dataWatcher.getWatchableObjectInt(18);
    }

    public void setRollingDirection(int p_70494_1_) {
        dataWatcher.updateObject(18, p_70494_1_);
    }

    public abstract EntityMinecart.EnumMinecartType getMinecartType();

    public IBlockState getDisplayTile() {
        return !hasDisplayTile() ? getDefaultDisplayTile() : Block.getStateById(getDataWatcher().getWatchableObjectInt(20));
    }

    public IBlockState getDefaultDisplayTile() {
        return Blocks.air.getDefaultState();
    }

    public int getDisplayTileOffset() {
        return !hasDisplayTile() ? getDefaultDisplayTileOffset() : getDataWatcher().getWatchableObjectInt(21);
    }

    public void setDisplayTileOffset(int p_94086_1_) {
        getDataWatcher().updateObject(21, p_94086_1_);
        setHasDisplayTile(true);
    }

    public int getDefaultDisplayTileOffset() {
        return 6;
    }

    public void func_174899_a(IBlockState p_174899_1_) {
        getDataWatcher().updateObject(20, Block.getStateId(p_174899_1_));
        setHasDisplayTile(true);
    }

    public boolean hasDisplayTile() {
        return getDataWatcher().getWatchableObjectByte(22) == 1;
    }

    public void setHasDisplayTile(boolean p_94096_1_) {
        getDataWatcher().updateObject(22, (byte) (p_94096_1_ ? 1 : 0));
    }

    public String getName() {
        return entityName != null ? entityName : super.getName();
    }

    public boolean hasCustomName() {
        return entityName != null;
    }

    public String getCustomNameTag() {
        return entityName;
    }

    public void setCustomNameTag(String name) {
        entityName = name;
    }

    public IChatComponent getDisplayName() {
        if (hasCustomName()) {
            ChatComponentText chatcomponenttext = new ChatComponentText(entityName);
            chatcomponenttext.getChatStyle().setChatHoverEvent(getHoverEvent());
            chatcomponenttext.getChatStyle().setInsertion(getUniqueID().toString());
            return chatcomponenttext;
        } else {
            ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation(getName());
            chatcomponenttranslation.getChatStyle().setChatHoverEvent(getHoverEvent());
            chatcomponenttranslation.getChatStyle().setInsertion(getUniqueID().toString());
            return chatcomponenttranslation;
        }
    }

    public enum EnumMinecartType {
        RIDEABLE(0, "MinecartRideable"),
        CHEST(1, "MinecartChest"),
        FURNACE(2, "MinecartFurnace"),
        TNT(3, "MinecartTNT"),
        SPAWNER(4, "MinecartSpawner"),
        HOPPER(5, "MinecartHopper"),
        COMMAND_BLOCK(6, "MinecartCommandBlock");

        private static final Map<Integer, EntityMinecart.EnumMinecartType> ID_LOOKUP = Maps.newHashMap();

        static {
            for (EntityMinecart.EnumMinecartType entityminecart$enumminecarttype : values()) {
                ID_LOOKUP.put(entityminecart$enumminecarttype.networkID, entityminecart$enumminecarttype);
            }
        }

        private final int networkID;
        private final String name;

        EnumMinecartType(int networkID, String name) {
            this.networkID = networkID;
            this.name = name;
        }

        public static EntityMinecart.EnumMinecartType byNetworkID(int id) {
            EntityMinecart.EnumMinecartType entityminecart$enumminecarttype = ID_LOOKUP.get(id);
            return entityminecart$enumminecarttype == null ? RIDEABLE : entityminecart$enumminecarttype;
        }

        public int getNetworkID() {
            return networkID;
        }

        public String getName() {
            return name;
        }
    }
}
