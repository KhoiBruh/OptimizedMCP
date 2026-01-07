package net.minecraft.entity;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.event.HoverEvent;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public abstract class Entity implements ICommandSender {
    private static final AxisAlignedBB ZERO_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    private static int nextEntityID;
    private final CommandResultStats cmdResultStats;
    public double renderDistanceWeight;
    public boolean preventEntitySpawning;
    public Entity riddenByEntity;
    public Entity ridingEntity;
    public boolean forceSpawn;
    public World worldObj;
    public double prevPosX;
    public double prevPosY;
    public double prevPosZ;
    public double posX;
    public double posY;
    public double posZ;
    public double motionX;
    public double motionY;
    public double motionZ;
    public float rotationYaw;
    public float rotationPitch;
    public float prevRotationYaw;
    public float prevRotationPitch;
    public boolean onGround;
    public boolean isCollidedHorizontally;
    public boolean isCollidedVertically;
    public boolean isCollided;
    public boolean velocityChanged;
    public boolean isDead;
    public float width;
    public float height;
    public float prevDistanceWalkedModified;
    public float distanceWalkedModified;
    public float distanceWalkedOnStepModified;
    public float fallDistance;
    public double lastTickPosX;
    public double lastTickPosY;
    public double lastTickPosZ;
    public float stepHeight;
    public boolean noClip;
    public float entityCollisionReduction;
    public int ticksExisted;
    public int fireResistance;
    public int hurtResistantTime;
    public boolean addedToChunk;
    public int chunkCoordX;
    public int chunkCoordY;
    public int chunkCoordZ;
    public int serverPosX;
    public int serverPosY;
    public int serverPosZ;
    public boolean ignoreFrustumCheck;
    public boolean isAirBorne;
    public int timeUntilPortal;
    public int dimension;
    protected boolean isInWeb;
    protected Random rand;
    protected boolean inWater;
    protected boolean firstUpdate;
    protected boolean isImmuneToFire;
    protected DataWatcher dataWatcher;
    protected boolean inPortal;
    protected int portalCounter;
    protected BlockPos lastPortalPos;
    protected Vec3 lastPortalVec;
    protected EnumFacing teleportDirection;
    protected UUID entityUniqueID;
    private int entityId;
    private AxisAlignedBB boundingBox;
    private boolean isOutsideBorder;
    private int nextStepDistance;
    private int fire;
    private double entityRiderPitchDelta;
    private double entityRiderYawDelta;
    private boolean invulnerable;

    public Entity(World worldIn) {
        entityId = nextEntityID++;
        renderDistanceWeight = 1.0D;
        boundingBox = ZERO_AABB;
        width = 0.6F;
        height = 1.8F;
        nextStepDistance = 1;
        rand = new Random();
        fireResistance = 1;
        firstUpdate = true;
        entityUniqueID = MathHelper.getRandomUuid(rand);
        cmdResultStats = new CommandResultStats();
        worldObj = worldIn;
        setPosition(0.0D, 0.0D, 0.0D);

        if (worldIn != null) {
            dimension = worldIn.provider.getDimensionId();
        }

        dataWatcher = new DataWatcher(this);
        dataWatcher.addObject(0, (byte) 0);
        dataWatcher.addObject(1, (short) 300);
        dataWatcher.addObject(3, (byte) 0);
        dataWatcher.addObject(2, "");
        dataWatcher.addObject(4, (byte) 0);
        entityInit();
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int id) {
        entityId = id;
    }

    public void onKillCommand() {
        setDead();
    }

    protected abstract void entityInit();

    public DataWatcher getDataWatcher() {
        return dataWatcher;
    }

    public boolean equals(Object p_equals_1_) {
        return p_equals_1_ instanceof Entity && ((Entity) p_equals_1_).entityId == entityId;
    }

    public int hashCode() {
        return entityId;
    }

    protected void preparePlayerToSpawn() {
        if (worldObj != null) {
            while (posY > 0.0D && posY < 256.0D) {
                setPosition(posX, posY, posZ);

                if (worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty()) {
                    break;
                }

                ++posY;
            }

            motionX = motionY = motionZ = 0.0D;
            rotationPitch = 0.0F;
        }
    }

    public void setDead() {
        isDead = true;
    }

    protected void setSize(float width, float height) {
        if (width != this.width || height != this.height) {
            float f = this.width;
            this.width = width;
            this.height = height;
            boundingBox = new AxisAlignedBB(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.minX + (double) this.width, boundingBox.minY + (double) this.height, boundingBox.minZ + (double) this.width);

            if (this.width > f && !firstUpdate && !worldObj.isRemote) {
                moveEntity(f - this.width, 0.0D, f - this.width);
            }
        }
    }

    protected void setRotation(float yaw, float pitch) {
        rotationYaw = yaw % 360.0F;
        rotationPitch = pitch % 360.0F;
    }

    public void setPosition(double x, double y, double z) {
        posX = x;
        posY = y;
        posZ = z;
        float f = width / 2.0F;
        float f1 = height;
        boundingBox = new AxisAlignedBB(x - (double) f, y, z - (double) f, x + (double) f, y + (double) f1, z + (double) f);
    }

    public void setAngles(float yaw, float pitch) {
        float f = rotationPitch;
        float f1 = rotationYaw;
        rotationYaw = (float) ((double) rotationYaw + (double) yaw * 0.15D);
        rotationPitch = (float) ((double) rotationPitch - (double) pitch * 0.15D);
        rotationPitch = MathHelper.clamp_float(rotationPitch, -90.0F, 90.0F);
        prevRotationPitch += rotationPitch - f;
        prevRotationYaw += rotationYaw - f1;
    }

    public void onUpdate() {
        onEntityUpdate();
    }

    public void onEntityUpdate() {
        worldObj.theProfiler.startSection("entityBaseTick");

        if (ridingEntity != null && ridingEntity.isDead) {
            ridingEntity = null;
        }

        prevDistanceWalkedModified = distanceWalkedModified;
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        prevRotationPitch = rotationPitch;
        prevRotationYaw = rotationYaw;

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

        spawnRunningParticles();
        handleWaterMovement();

        if (worldObj.isRemote) {
            fire = 0;
        } else if (fire > 0) {
            if (isImmuneToFire) {
                fire -= 4;

                if (fire < 0) {
                    fire = 0;
                }
            } else {
                if (fire % 20 == 0) {
                    attackEntityFrom(DamageSource.onFire, 1.0F);
                }

                --fire;
            }
        }

        if (isInLava()) {
            setOnFireFromLava();
            fallDistance *= 0.5F;
        }

        if (posY < -64.0D) {
            kill();
        }

        if (!worldObj.isRemote) {
            setFlag(0, fire > 0);
        }

        firstUpdate = false;
        worldObj.theProfiler.endSection();
    }

    public int getMaxInPortalTime() {
        return 0;
    }

    protected void setOnFireFromLava() {
        if (!isImmuneToFire) {
            attackEntityFrom(DamageSource.lava, 4.0F);
            setFire(15);
        }
    }

    public void setFire(int seconds) {
        int i = seconds * 20;
        i = EnchantmentProtection.getFireTimeForEntity(this, i);

        if (fire < i) {
            fire = i;
        }
    }

    public void extinguish() {
        fire = 0;
    }

    protected void kill() {
        setDead();
    }

    public boolean isOffsetPositionInLiquid(double x, double y, double z) {
        AxisAlignedBB axisalignedbb = boundingBox.offset(x, y, z);
        return isLiquidPresentInAABB(axisalignedbb);
    }

    private boolean isLiquidPresentInAABB(AxisAlignedBB bb) {
        return worldObj.getCollidingBoundingBoxes(this, bb).isEmpty() && !worldObj.isAnyLiquid(bb);
    }

    public void moveEntity(double x, double y, double z) {
        if (noClip) {
            boundingBox = boundingBox.offset(x, y, z);
            resetPositionToBB();
        } else {
            worldObj.theProfiler.startSection("move");
            double d0 = posX;
            double d1 = posY;
            double d2 = posZ;

            if (isInWeb) {
                isInWeb = false;
                x *= 0.25D;
                y *= 0.05000000074505806D;
                z *= 0.25D;
                motionX = 0.0D;
                motionY = 0.0D;
                motionZ = 0.0D;
            }

            double d3 = x;
            double d4 = y;
            double d5 = z;
            boolean flag = onGround && isSneaking() && this instanceof EntityPlayer;

            if (flag) {
                double d6;

                for (d6 = 0.05D; x != 0.0D && worldObj.getCollidingBoundingBoxes(this, boundingBox.offset(x, -1.0D, 0.0D)).isEmpty(); d3 = x) {
                    if (x < d6 && x >= -d6) {
                        x = 0.0D;
                    } else if (x > 0.0D) {
                        x -= d6;
                    } else {
                        x += d6;
                    }
                }

                for (; z != 0.0D && worldObj.getCollidingBoundingBoxes(this, boundingBox.offset(0.0D, -1.0D, z)).isEmpty(); d5 = z) {
                    if (z < d6 && z >= -d6) {
                        z = 0.0D;
                    } else if (z > 0.0D) {
                        z -= d6;
                    } else {
                        z += d6;
                    }
                }

                for (; x != 0.0D && z != 0.0D && worldObj.getCollidingBoundingBoxes(this, boundingBox.offset(x, -1.0D, z)).isEmpty(); d5 = z) {
                    if (x < d6 && x >= -d6) {
                        x = 0.0D;
                    } else if (x > 0.0D) {
                        x -= d6;
                    } else {
                        x += d6;
                    }

                    d3 = x;

                    if (z < d6 && z >= -d6) {
                        z = 0.0D;
                    } else if (z > 0.0D) {
                        z -= d6;
                    } else {
                        z += d6;
                    }
                }
            }

            List<AxisAlignedBB> list1 = worldObj.getCollidingBoundingBoxes(this, boundingBox.addCoord(x, y, z));
            AxisAlignedBB axisalignedbb = boundingBox;

            for (AxisAlignedBB axisalignedbb1 : list1) {
                y = axisalignedbb1.calculateYOffset(boundingBox, y);
            }

            boundingBox = boundingBox.offset(0.0D, y, 0.0D);
            boolean flag1 = onGround || d4 != y && d4 < 0.0D;

            for (AxisAlignedBB axisalignedbb2 : list1) {
                x = axisalignedbb2.calculateXOffset(boundingBox, x);
            }

            boundingBox = boundingBox.offset(x, 0.0D, 0.0D);

            for (AxisAlignedBB axisalignedbb13 : list1) {
                z = axisalignedbb13.calculateZOffset(boundingBox, z);
            }

            boundingBox = boundingBox.offset(0.0D, 0.0D, z);

            if (stepHeight > 0.0F && flag1 && (d3 != x || d5 != z)) {
                double d11 = x;
                double d7 = y;
                double d8 = z;
                AxisAlignedBB axisalignedbb3 = boundingBox;
                boundingBox = axisalignedbb;
                y = stepHeight;
                List<AxisAlignedBB> list = worldObj.getCollidingBoundingBoxes(this, boundingBox.addCoord(d3, y, d5));
                AxisAlignedBB axisalignedbb4 = boundingBox;
                AxisAlignedBB axisalignedbb5 = axisalignedbb4.addCoord(d3, 0.0D, d5);
                double d9 = y;

                for (AxisAlignedBB axisalignedbb6 : list) {
                    d9 = axisalignedbb6.calculateYOffset(axisalignedbb5, d9);
                }

                axisalignedbb4 = axisalignedbb4.offset(0.0D, d9, 0.0D);
                double d15 = d3;

                for (AxisAlignedBB axisalignedbb7 : list) {
                    d15 = axisalignedbb7.calculateXOffset(axisalignedbb4, d15);
                }

                axisalignedbb4 = axisalignedbb4.offset(d15, 0.0D, 0.0D);
                double d16 = d5;

                for (AxisAlignedBB axisalignedbb8 : list) {
                    d16 = axisalignedbb8.calculateZOffset(axisalignedbb4, d16);
                }

                axisalignedbb4 = axisalignedbb4.offset(0.0D, 0.0D, d16);
                AxisAlignedBB axisalignedbb14 = boundingBox;
                double d17 = y;

                for (AxisAlignedBB axisalignedbb9 : list) {
                    d17 = axisalignedbb9.calculateYOffset(axisalignedbb14, d17);
                }

                axisalignedbb14 = axisalignedbb14.offset(0.0D, d17, 0.0D);
                double d18 = d3;

                for (AxisAlignedBB axisalignedbb10 : list) {
                    d18 = axisalignedbb10.calculateXOffset(axisalignedbb14, d18);
                }

                axisalignedbb14 = axisalignedbb14.offset(d18, 0.0D, 0.0D);
                double d19 = d5;

                for (AxisAlignedBB axisalignedbb11 : list) {
                    d19 = axisalignedbb11.calculateZOffset(axisalignedbb14, d19);
                }

                axisalignedbb14 = axisalignedbb14.offset(0.0D, 0.0D, d19);
                double d20 = d15 * d15 + d16 * d16;
                double d10 = d18 * d18 + d19 * d19;

                if (d20 > d10) {
                    x = d15;
                    z = d16;
                    y = -d9;
                    boundingBox = axisalignedbb4;
                } else {
                    x = d18;
                    z = d19;
                    y = -d17;
                    boundingBox = axisalignedbb14;
                }

                for (AxisAlignedBB axisalignedbb12 : list) {
                    y = axisalignedbb12.calculateYOffset(boundingBox, y);
                }

                boundingBox = boundingBox.offset(0.0D, y, 0.0D);

                if (d11 * d11 + d8 * d8 >= x * x + z * z) {
                    x = d11;
                    y = d7;
                    z = d8;
                    boundingBox = axisalignedbb3;
                }
            }

            worldObj.theProfiler.endSection();
            worldObj.theProfiler.startSection("rest");
            resetPositionToBB();
            isCollidedHorizontally = d3 != x || d5 != z;
            isCollidedVertically = d4 != y;
            onGround = isCollidedVertically && d4 < 0.0D;
            isCollided = isCollidedHorizontally || isCollidedVertically;
            int i = MathHelper.floor_double(posX);
            int j = MathHelper.floor_double(posY - 0.20000000298023224D);
            int k = MathHelper.floor_double(posZ);
            BlockPos blockpos = new BlockPos(i, j, k);
            Block block1 = worldObj.getBlockState(blockpos).getBlock();

            if (block1.getMaterial() == Material.air) {
                Block block = worldObj.getBlockState(blockpos.down()).getBlock();

                if (block instanceof BlockFence || block instanceof BlockWall || block instanceof BlockFenceGate) {
                    block1 = block;
                    blockpos = blockpos.down();
                }
            }

            updateFallState(y, onGround, block1, blockpos);

            if (d3 != x) {
                motionX = 0.0D;
            }

            if (d5 != z) {
                motionZ = 0.0D;
            }

            if (d4 != y) {
                block1.onLanded(worldObj, this);
            }

            if (canTriggerWalking() && !flag && ridingEntity == null) {
                double d12 = posX - d0;
                double d13 = posY - d1;
                double d14 = posZ - d2;

                if (block1 != Blocks.ladder) {
                    d13 = 0.0D;
                }

                if (block1 != null && onGround) {
                    block1.onEntityCollidedWithBlock(worldObj, blockpos, this);
                }

                distanceWalkedModified = (float) ((double) distanceWalkedModified + (double) MathHelper.sqrt_double(d12 * d12 + d14 * d14) * 0.6D);
                distanceWalkedOnStepModified = (float) ((double) distanceWalkedOnStepModified + (double) MathHelper.sqrt_double(d12 * d12 + d13 * d13 + d14 * d14) * 0.6D);

                if (distanceWalkedOnStepModified > (float) nextStepDistance && block1.getMaterial() != Material.air) {
                    nextStepDistance = (int) distanceWalkedOnStepModified + 1;

                    if (isInWater()) {
                        float f = MathHelper.sqrt_double(motionX * motionX * 0.20000000298023224D + motionY * motionY + motionZ * motionZ * 0.20000000298023224D) * 0.35F;

                        if (f > 1.0F) {
                            f = 1.0F;
                        }

                        playSound(getSwimSound(), f, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.4F);
                    }

                    playStepSound(blockpos, block1);
                }
            }

            try {
                doBlockCollisions();
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being checked for collision");
                addEntityCrashInfo(crashreportcategory);
                throw new ReportedException(crashreport);
            }

            boolean flag2 = isWet();

            if (worldObj.isFlammableWithin(boundingBox.contract(0.001D, 0.001D, 0.001D))) {
                dealFireDamage(1);

                if (!flag2) {
                    ++fire;

                    if (fire == 0) {
                        setFire(8);
                    }
                }
            } else if (fire <= 0) {
                fire = -fireResistance;
            }

            if (flag2 && fire > 0) {
                playSound("random.fizz", 0.7F, 1.6F + (rand.nextFloat() - rand.nextFloat()) * 0.4F);
                fire = -fireResistance;
            }

            worldObj.theProfiler.endSection();
        }
    }

    private void resetPositionToBB() {
        posX = (boundingBox.minX + boundingBox.maxX) / 2.0D;
        posY = boundingBox.minY;
        posZ = (boundingBox.minZ + boundingBox.maxZ) / 2.0D;
    }

    protected String getSwimSound() {
        return "game.neutral.swim";
    }

    protected void doBlockCollisions() {
        BlockPos blockpos = new BlockPos(boundingBox.minX + 0.001D, boundingBox.minY + 0.001D, boundingBox.minZ + 0.001D);
        BlockPos blockpos1 = new BlockPos(boundingBox.maxX - 0.001D, boundingBox.maxY - 0.001D, boundingBox.maxZ - 0.001D);

        if (worldObj.isAreaLoaded(blockpos, blockpos1)) {
            for (int i = blockpos.getX(); i <= blockpos1.getX(); ++i) {
                for (int j = blockpos.getY(); j <= blockpos1.getY(); ++j) {
                    for (int k = blockpos.getZ(); k <= blockpos1.getZ(); ++k) {
                        BlockPos blockpos2 = new BlockPos(i, j, k);
                        IBlockState iblockstate = worldObj.getBlockState(blockpos2);

                        try {
                            iblockstate.getBlock().onEntityCollidedWithBlock(worldObj, blockpos2, iblockstate, this);
                        } catch (Throwable throwable) {
                            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Colliding entity with block");
                            CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being collided with");
                            CrashReportCategory.addBlockInfo(crashreportcategory, blockpos2, iblockstate);
                            throw new ReportedException(crashreport);
                        }
                    }
                }
            }
        }
    }

    protected void playStepSound(BlockPos pos, Block blockIn) {
        Block.SoundType block$soundtype = blockIn.stepSound;

        if (worldObj.getBlockState(pos.up()).getBlock() == Blocks.snow_layer) {
            block$soundtype = Blocks.snow_layer.stepSound;
            playSound(block$soundtype.getStepSound(), block$soundtype.getVolume() * 0.15F, block$soundtype.getFrequency());
        } else if (!blockIn.getMaterial().isLiquid()) {
            playSound(block$soundtype.getStepSound(), block$soundtype.getVolume() * 0.15F, block$soundtype.getFrequency());
        }
    }

    public void playSound(String name, float volume, float pitch) {
        if (!isSilent()) {
            worldObj.playSoundAtEntity(this, name, volume, pitch);
        }
    }

    public boolean isSilent() {
        return dataWatcher.getWatchableObjectByte(4) == 1;
    }

    public void setSilent(boolean isSilent) {
        dataWatcher.updateObject(4, (byte) (isSilent ? 1 : 0));
    }

    protected boolean canTriggerWalking() {
        return true;
    }

    protected void updateFallState(double y, boolean onGroundIn, Block blockIn, BlockPos pos) {
        if (onGroundIn) {
            if (fallDistance > 0.0F) {
                if (blockIn != null) {
                    blockIn.onFallenUpon(worldObj, pos, this, fallDistance);
                } else {
                    fall(fallDistance, 1.0F);
                }

                fallDistance = 0.0F;
            }
        } else if (y < 0.0D) {
            fallDistance = (float) ((double) fallDistance - y);
        }
    }

    public AxisAlignedBB getCollisionBoundingBox() {
        return null;
    }

    protected void dealFireDamage(int amount) {
        if (!isImmuneToFire) {
            attackEntityFrom(DamageSource.inFire, (float) amount);
        }
    }

    public final boolean isImmuneToFire() {
        return isImmuneToFire;
    }

    public void fall(float distance, float damageMultiplier) {
        if (riddenByEntity != null) {
            riddenByEntity.fall(distance, damageMultiplier);
        }
    }

    public boolean isWet() {
        return inWater || worldObj.isRainingAt(new BlockPos(posX, posY, posZ)) || worldObj.isRainingAt(new BlockPos(posX, posY + (double) height, posZ));
    }

    public boolean isInWater() {
        return inWater;
    }

    public boolean handleWaterMovement() {
        if (worldObj.handleMaterialAcceleration(boundingBox.expand(0.0D, -0.4000000059604645D, 0.0D).contract(0.001D, 0.001D, 0.001D), Material.water, this)) {
            if (!inWater && !firstUpdate) {
                resetHeight();
            }

            fallDistance = 0.0F;
            inWater = true;
            fire = 0;
        } else {
            inWater = false;
        }

        return inWater;
    }

    protected void resetHeight() {
        float f = MathHelper.sqrt_double(motionX * motionX * 0.20000000298023224D + motionY * motionY + motionZ * motionZ * 0.20000000298023224D) * 0.2F;

        if (f > 1.0F) {
            f = 1.0F;
        }

        playSound(getSplashSound(), f, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.4F);
        float f1 = (float) MathHelper.floor_double(boundingBox.minY);

        for (int i = 0; (float) i < 1.0F + width * 20.0F; ++i) {
            float f2 = (rand.nextFloat() * 2.0F - 1.0F) * width;
            float f3 = (rand.nextFloat() * 2.0F - 1.0F) * width;
            worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX + (double) f2, f1 + 1.0F, posZ + (double) f3, motionX, motionY - (double) (rand.nextFloat() * 0.2F), motionZ);
        }

        for (int j = 0; (float) j < 1.0F + width * 20.0F; ++j) {
            float f4 = (rand.nextFloat() * 2.0F - 1.0F) * width;
            float f5 = (rand.nextFloat() * 2.0F - 1.0F) * width;
            worldObj.spawnParticle(EnumParticleTypes.WATER_SPLASH, posX + (double) f4, f1 + 1.0F, posZ + (double) f5, motionX, motionY, motionZ);
        }
    }

    public void spawnRunningParticles() {
        if (isSprinting() && !isInWater()) {
            createRunningParticles();
        }
    }

    protected void createRunningParticles() {
        int i = MathHelper.floor_double(posX);
        int j = MathHelper.floor_double(posY - 0.20000000298023224D);
        int k = MathHelper.floor_double(posZ);
        BlockPos blockpos = new BlockPos(i, j, k);
        IBlockState iblockstate = worldObj.getBlockState(blockpos);
        Block block = iblockstate.getBlock();

        if (block.getRenderType() != -1) {
            worldObj.spawnParticle(EnumParticleTypes.BLOCK_CRACK, posX + ((double) rand.nextFloat() - 0.5D) * (double) width, boundingBox.minY + 0.1D, posZ + ((double) rand.nextFloat() - 0.5D) * (double) width, -motionX * 4.0D, 1.5D, -motionZ * 4.0D, Block.getStateId(iblockstate));
        }
    }

    protected String getSplashSound() {
        return "game.neutral.swim.splash";
    }

    public boolean isInsideOfMaterial(Material materialIn) {
        double d0 = posY + (double) getEyeHeight();
        BlockPos blockpos = new BlockPos(posX, d0, posZ);
        IBlockState iblockstate = worldObj.getBlockState(blockpos);
        Block block = iblockstate.getBlock();

        if (block.getMaterial() == materialIn) {
            float f = BlockLiquid.getLiquidHeightPercent(iblockstate.getBlock().getMetaFromState(iblockstate)) - 0.11111111F;
            float f1 = (float) (blockpos.getY() + 1) - f;
            boolean flag = d0 < (double) f1;
            return (flag || !(this instanceof EntityPlayer)) && flag;
        } else {
            return false;
        }
    }

    public boolean isInLava() {
        return worldObj.isMaterialInBB(boundingBox.expand(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D), Material.lava);
    }

    public void moveFlying(float strafe, float forward, float friction) {
        float f = strafe * strafe + forward * forward;

        if (f >= 1.0E-4F) {
            f = MathHelper.sqrt_float(f);

            if (f < 1.0F) {
                f = 1.0F;
            }

            f = friction / f;
            strafe = strafe * f;
            forward = forward * f;
            float f1 = MathHelper.sin(rotationYaw * (float) Math.PI / 180.0F);
            float f2 = MathHelper.cos(rotationYaw * (float) Math.PI / 180.0F);
            motionX += strafe * f2 - forward * f1;
            motionZ += forward * f2 + strafe * f1;
        }
    }

    public int getBrightnessForRender(float partialTicks) {
        BlockPos blockpos = new BlockPos(posX, posY + (double) getEyeHeight(), posZ);
        return worldObj.isBlockLoaded(blockpos) ? worldObj.getCombinedLight(blockpos, 0) : 0;
    }

    public float getBrightness(float partialTicks) {
        BlockPos blockpos = new BlockPos(posX, posY + (double) getEyeHeight(), posZ);
        return worldObj.isBlockLoaded(blockpos) ? worldObj.getLightBrightness(blockpos) : 0.0F;
    }

    public void setWorld(World worldIn) {
        worldObj = worldIn;
    }

    public void setPositionAndRotation(double x, double y, double z, float yaw, float pitch) {
        prevPosX = posX = x;
        prevPosY = posY = y;
        prevPosZ = posZ = z;
        prevRotationYaw = rotationYaw = yaw;
        prevRotationPitch = rotationPitch = pitch;
        double d0 = prevRotationYaw - yaw;

        if (d0 < -180.0D) {
            prevRotationYaw += 360.0F;
        }

        if (d0 >= 180.0D) {
            prevRotationYaw -= 360.0F;
        }

        setPosition(posX, posY, posZ);
        setRotation(yaw, pitch);
    }

    public void moveToBlockPosAndAngles(BlockPos pos, float rotationYawIn, float rotationPitchIn) {
        setLocationAndAngles((double) pos.getX() + 0.5D, pos.getY(), (double) pos.getZ() + 0.5D, rotationYawIn, rotationPitchIn);
    }

    public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
        lastTickPosX = prevPosX = posX = x;
        lastTickPosY = prevPosY = posY = y;
        lastTickPosZ = prevPosZ = posZ = z;
        rotationYaw = yaw;
        rotationPitch = pitch;
        setPosition(posX, posY, posZ);
    }

    public float getDistanceToEntity(Entity entityIn) {
        float f = (float) (posX - entityIn.posX);
        float f1 = (float) (posY - entityIn.posY);
        float f2 = (float) (posZ - entityIn.posZ);
        return MathHelper.sqrt_float(f * f + f1 * f1 + f2 * f2);
    }

    public double getDistanceSq(double x, double y, double z) {
        double d0 = posX - x;
        double d1 = posY - y;
        double d2 = posZ - z;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public double getDistanceSq(BlockPos pos) {
        return pos.distanceSq(posX, posY, posZ);
    }

    public double getDistanceSqToCenter(BlockPos pos) {
        return pos.distanceSqToCenter(posX, posY, posZ);
    }

    public double getDistance(double x, double y, double z) {
        double d0 = posX - x;
        double d1 = posY - y;
        double d2 = posZ - z;
        return MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);
    }

    public double getDistanceSqToEntity(Entity entityIn) {
        double d0 = posX - entityIn.posX;
        double d1 = posY - entityIn.posY;
        double d2 = posZ - entityIn.posZ;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public void onCollideWithPlayer(EntityPlayer entityIn) {
    }

    public void applyEntityCollision(Entity entityIn) {
        if (entityIn.riddenByEntity != this && entityIn.ridingEntity != this) {
            if (!entityIn.noClip && !noClip) {
                double d0 = entityIn.posX - posX;
                double d1 = entityIn.posZ - posZ;
                double d2 = MathHelper.abs_max(d0, d1);

                if (d2 >= 0.009999999776482582D) {
                    d2 = MathHelper.sqrt_double(d2);
                    d0 = d0 / d2;
                    d1 = d1 / d2;
                    double d3 = 1.0D / d2;

                    if (d3 > 1.0D) {
                        d3 = 1.0D;
                    }

                    d0 = d0 * d3;
                    d1 = d1 * d3;
                    d0 = d0 * 0.05000000074505806D;
                    d1 = d1 * 0.05000000074505806D;
                    d0 = d0 * (double) (1.0F - entityCollisionReduction);
                    d1 = d1 * (double) (1.0F - entityCollisionReduction);

                    if (riddenByEntity == null) {
                        addVelocity(-d0, 0.0D, -d1);
                    }

                    if (entityIn.riddenByEntity == null) {
                        entityIn.addVelocity(d0, 0.0D, d1);
                    }
                }
            }
        }
    }

    public void addVelocity(double x, double y, double z) {
        motionX += x;
        motionY += y;
        motionZ += z;
        isAirBorne = true;
    }

    protected void setBeenAttacked() {
        velocityChanged = true;
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isEntityInvulnerable(source)) {
            return false;
        } else {
            setBeenAttacked();
            return false;
        }
    }

    public Vec3 getLook(float partialTicks) {
        if (partialTicks == 1.0F) {
            return getVectorForRotation(rotationPitch, rotationYaw);
        } else {
            float f = prevRotationPitch + (rotationPitch - prevRotationPitch) * partialTicks;
            float f1 = prevRotationYaw + (rotationYaw - prevRotationYaw) * partialTicks;
            return getVectorForRotation(f, f1);
        }
    }

    protected final Vec3 getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3(f1 * f2, f3, f * f2);
    }

    public Vec3 getPositionEyes(float partialTicks) {
        if (partialTicks == 1.0F) {
            return new Vec3(posX, posY + (double) getEyeHeight(), posZ);
        } else {
            double d0 = prevPosX + (posX - prevPosX) * (double) partialTicks;
            double d1 = prevPosY + (posY - prevPosY) * (double) partialTicks + (double) getEyeHeight();
            double d2 = prevPosZ + (posZ - prevPosZ) * (double) partialTicks;
            return new Vec3(d0, d1, d2);
        }
    }

    public MovingObjectPosition rayTrace(double blockReachDistance, float partialTicks) {
        Vec3 vec3 = getPositionEyes(partialTicks);
        Vec3 vec31 = getLook(partialTicks);
        Vec3 vec32 = vec3.addVector(vec31.xCoord() * blockReachDistance, vec31.yCoord() * blockReachDistance, vec31.zCoord() * blockReachDistance);
        return worldObj.rayTraceBlocks(vec3, vec32, false, false, true);
    }

    public boolean canBeCollidedWith() {
        return false;
    }

    public boolean canBePushed() {
        return false;
    }

    public void addToPlayerScore(Entity entityIn, int amount) {
    }

    public boolean isInRangeToRender3d(double x, double y, double z) {
        double d0 = posX - x;
        double d1 = posY - y;
        double d2 = posZ - z;
        double d3 = d0 * d0 + d1 * d1 + d2 * d2;
        return isInRangeToRenderDist(d3);
    }

    public boolean isInRangeToRenderDist(double distance) {
        double d0 = boundingBox.getAverageEdgeLength();

        if (Double.isNaN(d0)) {
            d0 = 1.0D;
        }

        d0 = d0 * 64.0D * renderDistanceWeight;
        return distance < d0 * d0;
    }

    public boolean writeMountToNBT(NBTTagCompound tagCompund) {
        String s = getEntityString();

        if (!isDead && s != null) {
            tagCompund.setString("id", s);
            writeToNBT(tagCompund);
            return true;
        } else {
            return false;
        }
    }

    public boolean writeToNBTOptional(NBTTagCompound tagCompund) {
        String s = getEntityString();

        if (!isDead && s != null && riddenByEntity == null) {
            tagCompund.setString("id", s);
            writeToNBT(tagCompund);
            return true;
        } else {
            return false;
        }
    }

    public void writeToNBT(NBTTagCompound tagCompund) {
        try {
            tagCompund.setTag("Pos", newDoubleNBTList(posX, posY, posZ));
            tagCompund.setTag("Motion", newDoubleNBTList(motionX, motionY, motionZ));
            tagCompund.setTag("Rotation", newFloatNBTList(rotationYaw, rotationPitch));
            tagCompund.setFloat("FallDistance", fallDistance);
            tagCompund.setShort("Fire", (short) fire);
            tagCompund.setShort("Air", (short) getAir());
            tagCompund.setBoolean("OnGround", onGround);
            tagCompund.setInteger("Dimension", dimension);
            tagCompund.setBoolean("Invulnerable", invulnerable);
            tagCompund.setInteger("PortalCooldown", timeUntilPortal);
            tagCompund.setLong("UUIDMost", entityUniqueID.getMostSignificantBits());
            tagCompund.setLong("UUIDLeast", entityUniqueID.getLeastSignificantBits());

            if (getCustomNameTag() != null && !getCustomNameTag().isEmpty()) {
                tagCompund.setString("CustomName", getCustomNameTag());
                tagCompund.setBoolean("CustomNameVisible", getAlwaysRenderNameTag());
            }

            cmdResultStats.writeStatsToNBT(tagCompund);

            if (isSilent()) {
                tagCompund.setBoolean("Silent", isSilent());
            }

            writeEntityToNBT(tagCompund);

            if (ridingEntity != null) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();

                if (ridingEntity.writeMountToNBT(nbttagcompound)) {
                    tagCompund.setTag("Riding", nbttagcompound);
                }
            }
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Saving entity NBT");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being saved");
            addEntityCrashInfo(crashreportcategory);
            throw new ReportedException(crashreport);
        }
    }

    public void readFromNBT(NBTTagCompound tagCompund) {
        try {
            NBTTagList nbttaglist = tagCompund.getTagList("Pos", 6);
            NBTTagList nbttaglist1 = tagCompund.getTagList("Motion", 6);
            NBTTagList nbttaglist2 = tagCompund.getTagList("Rotation", 5);
            motionX = nbttaglist1.getDoubleAt(0);
            motionY = nbttaglist1.getDoubleAt(1);
            motionZ = nbttaglist1.getDoubleAt(2);

            if (Math.abs(motionX) > 10.0D) {
                motionX = 0.0D;
            }

            if (Math.abs(motionY) > 10.0D) {
                motionY = 0.0D;
            }

            if (Math.abs(motionZ) > 10.0D) {
                motionZ = 0.0D;
            }

            prevPosX = lastTickPosX = posX = nbttaglist.getDoubleAt(0);
            prevPosY = lastTickPosY = posY = nbttaglist.getDoubleAt(1);
            prevPosZ = lastTickPosZ = posZ = nbttaglist.getDoubleAt(2);
            prevRotationYaw = rotationYaw = nbttaglist2.getFloatAt(0);
            prevRotationPitch = rotationPitch = nbttaglist2.getFloatAt(1);
            setRotationYawHead(rotationYaw);
            setRenderYawOffset(rotationYaw);
            fallDistance = tagCompund.getFloat("FallDistance");
            fire = tagCompund.getShort("Fire");
            setAir(tagCompund.getShort("Air"));
            onGround = tagCompund.getBoolean("OnGround");
            dimension = tagCompund.getInteger("Dimension");
            invulnerable = tagCompund.getBoolean("Invulnerable");
            timeUntilPortal = tagCompund.getInteger("PortalCooldown");

            if (tagCompund.hasKey("UUIDMost", 4) && tagCompund.hasKey("UUIDLeast", 4)) {
                entityUniqueID = new UUID(tagCompund.getLong("UUIDMost"), tagCompund.getLong("UUIDLeast"));
            } else if (tagCompund.hasKey("UUID", 8)) {
                entityUniqueID = UUID.fromString(tagCompund.getString("UUID"));
            }

            setPosition(posX, posY, posZ);
            setRotation(rotationYaw, rotationPitch);

            if (tagCompund.hasKey("CustomName", 8) && !tagCompund.getString("CustomName").isEmpty()) {
                setCustomNameTag(tagCompund.getString("CustomName"));
            }

            setAlwaysRenderNameTag(tagCompund.getBoolean("CustomNameVisible"));
            cmdResultStats.readStatsFromNBT(tagCompund);
            setSilent(tagCompund.getBoolean("Silent"));
            readEntityFromNBT(tagCompund);

            if (shouldSetPosAfterLoading()) {
                setPosition(posX, posY, posZ);
            }
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Loading entity NBT");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being loaded");
            addEntityCrashInfo(crashreportcategory);
            throw new ReportedException(crashreport);
        }
    }

    protected boolean shouldSetPosAfterLoading() {
        return true;
    }

    protected final String getEntityString() {
        return EntityList.getEntityString(this);
    }

    protected abstract void readEntityFromNBT(NBTTagCompound tagCompund);

    protected abstract void writeEntityToNBT(NBTTagCompound tagCompound);

    public void onChunkLoad() {
    }

    protected NBTTagList newDoubleNBTList(double... numbers) {
        NBTTagList nbttaglist = new NBTTagList();

        for (double d0 : numbers) {
            nbttaglist.appendTag(new NBTTagDouble(d0));
        }

        return nbttaglist;
    }

    protected NBTTagList newFloatNBTList(float... numbers) {
        NBTTagList nbttaglist = new NBTTagList();

        for (float f : numbers) {
            nbttaglist.appendTag(new NBTTagFloat(f));
        }

        return nbttaglist;
    }

    public EntityItem dropItem(Item itemIn, int size) {
        return dropItemWithOffset(itemIn, size, 0.0F);
    }

    public EntityItem dropItemWithOffset(Item itemIn, int size, float offsetY) {
        return entityDropItem(new ItemStack(itemIn, size, 0), offsetY);
    }

    public EntityItem entityDropItem(ItemStack itemStackIn, float offsetY) {
        if (itemStackIn.stackSize != 0 && itemStackIn.getItem() != null) {
            EntityItem entityitem = new EntityItem(worldObj, posX, posY + (double) offsetY, posZ, itemStackIn);
            entityitem.setDefaultPickupDelay();
            worldObj.spawnEntityInWorld(entityitem);
            return entityitem;
        } else {
            return null;
        }
    }

    public boolean isEntityAlive() {
        return !isDead;
    }

    public boolean isEntityInsideOpaqueBlock() {
        if (noClip) {
            return false;
        } else {
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

            for (int i = 0; i < 8; ++i) {
                int j = MathHelper.floor_double(posY + (double) (((float) ((i) % 2) - 0.5F) * 0.1F) + (double) getEyeHeight());
                int k = MathHelper.floor_double(posX + (double) (((float) ((i >> 1) % 2) - 0.5F) * width * 0.8F));
                int l = MathHelper.floor_double(posZ + (double) (((float) ((i >> 2) % 2) - 0.5F) * width * 0.8F));

                if (blockpos$mutableblockpos.getX() != k || blockpos$mutableblockpos.getY() != j || blockpos$mutableblockpos.getZ() != l) {
                    blockpos$mutableblockpos.set(k, j, l);

                    if (worldObj.getBlockState(blockpos$mutableblockpos).getBlock().isVisuallyOpaque()) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public boolean interactFirst(EntityPlayer playerIn) {
        return false;
    }

    public AxisAlignedBB getCollisionBox(Entity entityIn) {
        return null;
    }

    public void updateRidden() {
        if (ridingEntity.isDead) {
            ridingEntity = null;
        } else {
            motionX = 0.0D;
            motionY = 0.0D;
            motionZ = 0.0D;
            onUpdate();

            if (ridingEntity != null) {
                ridingEntity.updateRiderPosition();
                entityRiderYawDelta += ridingEntity.rotationYaw - ridingEntity.prevRotationYaw;

                for (entityRiderPitchDelta += ridingEntity.rotationPitch - ridingEntity.prevRotationPitch; entityRiderYawDelta >= 180.0D; entityRiderYawDelta -= 360.0D) {
                }

                while (entityRiderYawDelta < -180.0D) {
                    entityRiderYawDelta += 360.0D;
                }

                while (entityRiderPitchDelta >= 180.0D) {
                    entityRiderPitchDelta -= 360.0D;
                }

                while (entityRiderPitchDelta < -180.0D) {
                    entityRiderPitchDelta += 360.0D;
                }

                double d0 = entityRiderYawDelta * 0.5D;
                double d1 = entityRiderPitchDelta * 0.5D;
                float f = 10.0F;

                if (d0 > (double) f) {
                    d0 = f;
                }

                if (d0 < (double) (-f)) {
                    d0 = -f;
                }

                if (d1 > (double) f) {
                    d1 = f;
                }

                if (d1 < (double) (-f)) {
                    d1 = -f;
                }

                entityRiderYawDelta -= d0;
                entityRiderPitchDelta -= d1;
            }
        }
    }

    public void updateRiderPosition() {
        if (riddenByEntity != null) {
            riddenByEntity.setPosition(posX, posY + getMountedYOffset() + riddenByEntity.getYOffset(), posZ);
        }
    }

    public double getYOffset() {
        return 0.0D;
    }

    public double getMountedYOffset() {
        return (double) height * 0.75D;
    }

    public void mountEntity(Entity entityIn) {
        entityRiderPitchDelta = 0.0D;
        entityRiderYawDelta = 0.0D;

        if (entityIn == null) {
            if (ridingEntity != null) {
                setLocationAndAngles(ridingEntity.posX, ridingEntity.boundingBox.minY + (double) ridingEntity.height, ridingEntity.posZ, rotationYaw, rotationPitch);
                ridingEntity.riddenByEntity = null;
            }

            ridingEntity = null;
        } else {
            if (ridingEntity != null) {
                ridingEntity.riddenByEntity = null;
            }

            if (entityIn != null) {
                for (Entity entity = entityIn.ridingEntity; entity != null; entity = entity.ridingEntity) {
                    if (entity == this) {
                        return;
                    }
                }
            }

            ridingEntity = entityIn;
            entityIn.riddenByEntity = this;
        }
    }

    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean p_180426_10_) {
        setPosition(x, y, z);
        setRotation(yaw, pitch);
        List<AxisAlignedBB> list = worldObj.getCollidingBoundingBoxes(this, boundingBox.contract(0.03125D, 0.0D, 0.03125D));

        if (!list.isEmpty()) {
            double d0 = 0.0D;

            for (AxisAlignedBB axisalignedbb : list) {
                if (axisalignedbb.maxY > d0) {
                    d0 = axisalignedbb.maxY;
                }
            }

            y = y + (d0 - boundingBox.minY);
            setPosition(x, y, z);
        }
    }

    public float getCollisionBorderSize() {
        return 0.1F;
    }

    public Vec3 getLookVec() {
        return null;
    }

    public void setPortal(BlockPos pos) {
        if (timeUntilPortal > 0) {
            timeUntilPortal = getPortalCooldown();
        } else {
            if (!worldObj.isRemote && !pos.equals(lastPortalPos)) {
                lastPortalPos = pos;
                BlockPattern.PatternHelper blockpattern$patternhelper = Blocks.portal.func_181089_f(worldObj, pos);
                double d0 = blockpattern$patternhelper.getFinger().getAxis() == EnumFacing.Axis.X ? (double) blockpattern$patternhelper.getPos().getZ() : (double) blockpattern$patternhelper.getPos().getX();
                double d1 = blockpattern$patternhelper.getFinger().getAxis() == EnumFacing.Axis.X ? posZ : posX;
                d1 = Math.abs(MathHelper.func_181160_c(d1 - (double) (blockpattern$patternhelper.getFinger().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? 1 : 0), d0, d0 - (double) blockpattern$patternhelper.func_181118_d()));
                double d2 = MathHelper.func_181160_c(posY - 1.0D, blockpattern$patternhelper.getPos().getY(), blockpattern$patternhelper.getPos().getY() - blockpattern$patternhelper.func_181119_e());
                lastPortalVec = new Vec3(d1, d2, 0.0D);
                teleportDirection = blockpattern$patternhelper.getFinger();
            }

            inPortal = true;
        }
    }

    public int getPortalCooldown() {
        return 300;
    }

    public void setVelocity(double x, double y, double z) {
        motionX = x;
        motionY = y;
        motionZ = z;
    }

    public void handleStatusUpdate(byte id) {
    }

    public void performHurtAnimation() {
    }

    public ItemStack[] getInventory() {
        return null;
    }

    public void setCurrentItemOrArmor(int slotIn, ItemStack stack) {
    }

    public boolean isBurning() {
        boolean flag = worldObj != null && worldObj.isRemote;
        return !isImmuneToFire && (fire > 0 || flag && getFlag(0));
    }

    public boolean isRiding() {
        return ridingEntity != null;
    }

    public boolean isSneaking() {
        return getFlag(1);
    }

    public void setSneaking(boolean sneaking) {
        setFlag(1, sneaking);
    }

    public boolean isSprinting() {
        return getFlag(3);
    }

    public void setSprinting(boolean sprinting) {
        setFlag(3, sprinting);
    }

    public boolean isInvisible() {
        return getFlag(5);
    }

    public void setInvisible(boolean invisible) {
        setFlag(5, invisible);
    }

    public boolean isInvisibleToPlayer(EntityPlayer player) {
        return !player.isSpectator() && isInvisible();
    }

    public boolean isEating() {
        return getFlag(4);
    }

    public void setEating(boolean eating) {
        setFlag(4, eating);
    }

    protected boolean getFlag(int flag) {
        return (dataWatcher.getWatchableObjectByte(0) & 1 << flag) != 0;
    }

    protected void setFlag(int flag, boolean set) {
        byte b0 = dataWatcher.getWatchableObjectByte(0);

        if (set) {
            dataWatcher.updateObject(0, (byte) (b0 | 1 << flag));
        } else {
            dataWatcher.updateObject(0, (byte) (b0 & ~(1 << flag)));
        }
    }

    public int getAir() {
        return dataWatcher.getWatchableObjectShort(1);
    }

    public void setAir(int air) {
        dataWatcher.updateObject(1, (short) air);
    }

    public void onStruckByLightning(EntityLightningBolt lightningBolt) {
        attackEntityFrom(DamageSource.lightningBolt, 5.0F);
        ++fire;

        if (fire == 0) {
            setFire(8);
        }
    }

    public void onKillEntity(EntityLivingBase entityLivingIn) {
    }

    protected boolean pushOutOfBlocks(double x, double y, double z) {
        BlockPos blockpos = new BlockPos(x, y, z);
        double d0 = x - (double) blockpos.getX();
        double d1 = y - (double) blockpos.getY();
        double d2 = z - (double) blockpos.getZ();
        List<AxisAlignedBB> list = worldObj.getCollisionBoxes(boundingBox);

        if (list.isEmpty() && !worldObj.isBlockFullCube(blockpos)) {
            return false;
        } else {
            int i = 3;
            double d3 = 9999.0D;

            if (!worldObj.isBlockFullCube(blockpos.west()) && d0 < d3) {
                d3 = d0;
                i = 0;
            }

            if (!worldObj.isBlockFullCube(blockpos.east()) && 1.0D - d0 < d3) {
                d3 = 1.0D - d0;
                i = 1;
            }

            if (!worldObj.isBlockFullCube(blockpos.up()) && 1.0D - d1 < d3) {
                d3 = 1.0D - d1;
                i = 3;
            }

            if (!worldObj.isBlockFullCube(blockpos.north()) && d2 < d3) {
                d3 = d2;
                i = 4;
            }

            if (!worldObj.isBlockFullCube(blockpos.south()) && 1.0D - d2 < d3) {
                d3 = 1.0D - d2;
                i = 5;
            }

            float f = rand.nextFloat() * 0.2F + 0.1F;

            if (i == 0) {
                motionX = -f;
            }

            if (i == 1) {
                motionX = f;
            }

            if (i == 3) {
                motionY = f;
            }

            if (i == 4) {
                motionZ = -f;
            }

            if (i == 5) {
                motionZ = f;
            }

            return true;
        }
    }

    public void setInWeb() {
        isInWeb = true;
        fallDistance = 0.0F;
    }

    public String getName() {
        if (hasCustomName()) {
            return getCustomNameTag();
        } else {
            String s = EntityList.getEntityString(this);

            if (s == null) {
                s = "generic";
            }

            return StatCollector.translateToLocal("entity." + s + ".name");
        }
    }

    public Entity[] getParts() {
        return null;
    }

    public boolean isEntityEqual(Entity entityIn) {
        return this == entityIn;
    }

    public float getRotationYawHead() {
        return 0.0F;
    }

    public void setRotationYawHead(float rotation) {
    }

    public void setRenderYawOffset(float offset) {
    }

    public boolean canAttackWithItem() {
        return true;
    }

    public boolean hitByEntity(Entity entityIn) {
        return false;
    }

    public String toString() {
        return String.format("%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f]", getClass().getSimpleName(), getName(), entityId, worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(), posX, posY, posZ);
    }

    public boolean isEntityInvulnerable(DamageSource source) {
        return invulnerable && source != DamageSource.outOfWorld && !source.isCreativePlayer();
    }

    public void copyLocationAndAnglesFrom(Entity entityIn) {
        setLocationAndAngles(entityIn.posX, entityIn.posY, entityIn.posZ, entityIn.rotationYaw, entityIn.rotationPitch);
    }

    public void copyDataFromOld(Entity entityIn) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        entityIn.writeToNBT(nbttagcompound);
        readFromNBT(nbttagcompound);
        timeUntilPortal = entityIn.timeUntilPortal;
        lastPortalPos = entityIn.lastPortalPos;
        lastPortalVec = entityIn.lastPortalVec;
        teleportDirection = entityIn.teleportDirection;
    }

    public void travelToDimension(int dimensionId) {
        if (!worldObj.isRemote && !isDead) {
            worldObj.theProfiler.startSection("changeDimension");
            MinecraftServer minecraftserver = MinecraftServer.getServer();
            int i = dimension;
            WorldServer worldserver = minecraftserver.worldServerForDimension(i);
            WorldServer worldserver1 = minecraftserver.worldServerForDimension(dimensionId);
            dimension = dimensionId;

            if (i == 1 && dimensionId == 1) {
                worldserver1 = minecraftserver.worldServerForDimension(0);
                dimension = 0;
            }

            worldObj.removeEntity(this);
            isDead = false;
            worldObj.theProfiler.startSection("reposition");
            minecraftserver.getConfigurationManager().transferEntityToWorld(this, i, worldserver, worldserver1);
            worldObj.theProfiler.endStartSection("reloading");
            Entity entity = EntityList.createEntityByName(EntityList.getEntityString(this), worldserver1);

            if (entity != null) {
                entity.copyDataFromOld(this);

                if (i == 1 && dimensionId == 1) {
                    BlockPos blockpos = worldObj.getTopSolidOrLiquidBlock(worldserver1.getSpawnPoint());
                    entity.moveToBlockPosAndAngles(blockpos, entity.rotationYaw, entity.rotationPitch);
                }

                worldserver1.spawnEntityInWorld(entity);
            }

            isDead = true;
            worldObj.theProfiler.endSection();
            worldserver.resetUpdateEntityTick();
            worldserver1.resetUpdateEntityTick();
            worldObj.theProfiler.endSection();
        }
    }

    public float getExplosionResistance(Explosion explosionIn, World worldIn, BlockPos pos, IBlockState blockStateIn) {
        return blockStateIn.getBlock().getExplosionResistance(this);
    }

    public boolean verifyExplosion(Explosion explosionIn, World worldIn, BlockPos pos, IBlockState blockStateIn, float p_174816_5_) {
        return true;
    }

    public int getMaxFallHeight() {
        return 3;
    }

    public Vec3 func_181014_aG() {
        return lastPortalVec;
    }

    public EnumFacing getTeleportDirection() {
        return teleportDirection;
    }

    public boolean doesEntityNotTriggerPressurePlate() {
        return false;
    }

    public void addEntityCrashInfo(CrashReportCategory category) {
        category.addCrashSectionCallable("Entity Type", () -> EntityList.getEntityString(Entity.this) + " (" + Entity.this.getClass().getCanonicalName() + ")");
        category.addCrashSection("Entity ID", entityId);
        category.addCrashSectionCallable("Entity Name", this::getName);
        category.addCrashSection("Entity's Exact location", String.format("%.2f, %.2f, %.2f", posX, posY, posZ));
        category.addCrashSection("Entity's Block location", CrashReportCategory.getCoordinateInfo(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ)));
        category.addCrashSection("Entity's Momentum", String.format("%.2f, %.2f, %.2f", motionX, motionY, motionZ));
        category.addCrashSectionCallable("Entity's Rider", () -> riddenByEntity.toString());
        category.addCrashSectionCallable("Entity's Vehicle", () -> ridingEntity.toString());
    }

    public boolean canRenderOnFire() {
        return isBurning();
    }

    public UUID getUniqueID() {
        return entityUniqueID;
    }

    public boolean isPushedByWater() {
        return true;
    }

    public IChatComponent getDisplayName() {
        ChatComponentText chatcomponenttext = new ChatComponentText(getName());
        chatcomponenttext.getChatStyle().setChatHoverEvent(getHoverEvent());
        chatcomponenttext.getChatStyle().setInsertion(entityUniqueID.toString());
        return chatcomponenttext;
    }

    public String getCustomNameTag() {
        return dataWatcher.getWatchableObjectString(2);
    }

    public void setCustomNameTag(String name) {
        dataWatcher.updateObject(2, name);
    }

    public boolean hasCustomName() {
        return !dataWatcher.getWatchableObjectString(2).isEmpty();
    }

    public boolean getAlwaysRenderNameTag() {
        return dataWatcher.getWatchableObjectByte(3) == 1;
    }

    public void setAlwaysRenderNameTag(boolean alwaysRenderNameTag) {
        dataWatcher.updateObject(3, (byte) (alwaysRenderNameTag ? 1 : 0));
    }

    public void setPositionAndUpdate(double x, double y, double z) {
        setLocationAndAngles(x, y, z, rotationYaw, rotationPitch);
    }

    public boolean getAlwaysRenderNameTagForRender() {
        return getAlwaysRenderNameTag();
    }

    public void onDataWatcherUpdate(int dataID) {
    }

    public EnumFacing getHorizontalFacing() {
        return EnumFacing.getHorizontal(MathHelper.floor_double((double) (rotationYaw * 4.0F / 360.0F) + 0.5D) & 3);
    }

    protected HoverEvent getHoverEvent() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        String s = EntityList.getEntityString(this);
        nbttagcompound.setString("id", entityUniqueID.toString());

        if (s != null) {
            nbttagcompound.setString("type", s);
        }

        nbttagcompound.setString("name", getName());
        return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new ChatComponentText(nbttagcompound.toString()));
    }

    public boolean isSpectatedByPlayer(EntityPlayerMP player) {
        return true;
    }

    public AxisAlignedBB getEntityBoundingBox() {
        return boundingBox;
    }

    public void setEntityBoundingBox(AxisAlignedBB bb) {
        boundingBox = bb;
    }

    public float getEyeHeight() {
        return height * 0.85F;
    }

    public boolean isOutsideBorder() {
        return isOutsideBorder;
    }

    public void setOutsideBorder(boolean outsideBorder) {
        isOutsideBorder = outsideBorder;
    }

    public boolean replaceItemInInventory(int inventorySlot, ItemStack itemStackIn) {
        return false;
    }

    public void addChatMessage(IChatComponent component) {
    }

    public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
        return true;
    }

    public BlockPos getPosition() {
        return new BlockPos(posX, posY + 0.5D, posZ);
    }

    public Vec3 getPositionVector() {
        return new Vec3(posX, posY, posZ);
    }

    public World getEntityWorld() {
        return worldObj;
    }

    public Entity getCommandSenderEntity() {
        return this;
    }

    public boolean sendCommandFeedback() {
        return false;
    }

    public void setCommandStat(CommandResultStats.Type type, int amount) {
        cmdResultStats.setCommandStatScore(this, type, amount);
    }

    public CommandResultStats getCommandStats() {
        return cmdResultStats;
    }

    public void setCommandStats(Entity entityIn) {
        cmdResultStats.addAllStats(entityIn.cmdResultStats);
    }

    public NBTTagCompound getNBTTagCompound() {
        return null;
    }

    public void clientUpdateEntityNBT(NBTTagCompound compound) {
    }

    public boolean interactAt(EntityPlayer player, Vec3 targetVec3) {
        return false;
    }

    public boolean isImmuneToExplosions() {
        return false;
    }

    protected void applyEnchantments(EntityLivingBase entityLivingBaseIn, Entity entityIn) {
        if (entityIn instanceof EntityLivingBase) {
            EnchantmentHelper.applyThornEnchantments((EntityLivingBase) entityIn, entityLivingBaseIn);
        }

        EnchantmentHelper.applyArthropodEnchantments(entityLivingBaseIn, entityIn);
    }
}
