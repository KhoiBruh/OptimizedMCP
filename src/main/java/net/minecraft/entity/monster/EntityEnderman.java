package net.minecraft.entity.monster;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.*;

public class EntityEnderman extends EntityMob {
    private static final UUID attackingSpeedBoostModifierUUID = UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0");
    private static final AttributeModifier attackingSpeedBoostModifier = (new AttributeModifier(attackingSpeedBoostModifierUUID, "Attacking speed boost", 0.15000000596046448D, 0)).setSaved(false);
    private static final Set<Block> carriableBlocks = Sets.newIdentityHashSet();

    static {
        carriableBlocks.add(Blocks.grass);
        carriableBlocks.add(Blocks.dirt);
        carriableBlocks.add(Blocks.sand);
        carriableBlocks.add(Blocks.gravel);
        carriableBlocks.add(Blocks.yellow_flower);
        carriableBlocks.add(Blocks.red_flower);
        carriableBlocks.add(Blocks.brown_mushroom);
        carriableBlocks.add(Blocks.red_mushroom);
        carriableBlocks.add(Blocks.tnt);
        carriableBlocks.add(Blocks.cactus);
        carriableBlocks.add(Blocks.clay);
        carriableBlocks.add(Blocks.pumpkin);
        carriableBlocks.add(Blocks.melon_block);
        carriableBlocks.add(Blocks.mycelium);
    }

    private boolean isAggressive;

    public EntityEnderman(World worldIn) {
        super(worldIn);
        setSize(0.6F, 2.9F);
        stepHeight = 1.0F;
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(2, new EntityAIAttackOnCollide(this, 1.0D, false));
        tasks.addTask(7, new EntityAIWander(this, 1.0D));
        tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(8, new EntityAILookIdle(this));
        tasks.addTask(10, new EntityEnderman.AIPlaceBlock(this));
        tasks.addTask(11, new EntityEnderman.AITakeBlock(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(2, new EntityEnderman.AIFindPlayer(this));
        targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityEndermite.class, 10, true, false, p_apply_1_ -> p_apply_1_.isSpawnedByPlayer()));
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(40.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.30000001192092896D);
        getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(7.0D);
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(64.0D);
    }

    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(16, (short) 0);
        dataWatcher.addObject(17, (byte) 0);
        dataWatcher.addObject(18, (byte) 0);
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        IBlockState iblockstate = getHeldBlockState();
        tagCompound.setShort("carried", (short) Block.getIdFromBlock(iblockstate.getBlock()));
        tagCompound.setShort("carriedData", (short) iblockstate.getBlock().getMetaFromState(iblockstate));
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        IBlockState iblockstate;

        if (tagCompund.hasKey("carried", 8)) {
            iblockstate = Block.getBlockFromName(tagCompund.getString("carried")).getStateFromMeta(tagCompund.getShort("carriedData") & 65535);
        } else {
            iblockstate = Block.getBlockById(tagCompund.getShort("carried")).getStateFromMeta(tagCompund.getShort("carriedData") & 65535);
        }

        setHeldBlockState(iblockstate);
    }

    private boolean shouldAttackPlayer(EntityPlayer player) {
        ItemStack itemstack = player.inventory.armorInventory[3];

        if (itemstack != null && itemstack.getItem() == Item.getItemFromBlock(Blocks.pumpkin)) {
            return false;
        } else {
            Vec3 vec3 = player.getLook(1.0F).normalize();
            Vec3 vec31 = new Vec3(posX - player.posX, getEntityBoundingBox().minY + (double) (height / 2.0F) - (player.posY + (double) player.getEyeHeight()), posZ - player.posZ);
            double d0 = vec31.lengthVector();
            vec31 = vec31.normalize();
            double d1 = vec3.dotProduct(vec31);
            return d1 > 1.0D - 0.025D / d0 && player.canEntityBeSeen(this);
        }
    }

    public float getEyeHeight() {
        return 2.55F;
    }

    public void onLivingUpdate() {
        if (worldObj.isRemote) {
            for (int i = 0; i < 2; ++i) {
                worldObj.spawnParticle(EnumParticleTypes.PORTAL, posX + (rand.nextDouble() - 0.5D) * (double) width, posY + rand.nextDouble() * (double) height - 0.25D, posZ + (rand.nextDouble() - 0.5D) * (double) width, (rand.nextDouble() - 0.5D) * 2.0D, -rand.nextDouble(), (rand.nextDouble() - 0.5D) * 2.0D);
            }
        }

        isJumping = false;
        super.onLivingUpdate();
    }

    protected void updateAITasks() {
        if (isWet()) {
            attackEntityFrom(DamageSource.drown, 1.0F);
        }

        if (isScreaming() && !isAggressive && rand.nextInt(100) == 0) {
            setScreaming(false);
        }

        if (worldObj.isDaytime()) {
            float f = getBrightness(1.0F);

            if (f > 0.5F && worldObj.canSeeSky(new BlockPos(this)) && rand.nextFloat() * 30.0F < (f - 0.4F) * 2.0F) {
                setAttackTarget(null);
                setScreaming(false);
                isAggressive = false;
                teleportRandomly();
            }
        }

        super.updateAITasks();
    }

    protected boolean teleportRandomly() {
        double d0 = posX + (rand.nextDouble() - 0.5D) * 64.0D;
        double d1 = posY + (double) (rand.nextInt(64) - 32);
        double d2 = posZ + (rand.nextDouble() - 0.5D) * 64.0D;
        return teleportTo(d0, d1, d2);
    }

    protected boolean teleportToEntity(Entity p_70816_1_) {
        Vec3 vec3 = new Vec3(posX - p_70816_1_.posX, getEntityBoundingBox().minY + (double) (height / 2.0F) - p_70816_1_.posY + (double) p_70816_1_.getEyeHeight(), posZ - p_70816_1_.posZ);
        vec3 = vec3.normalize();
        double d0 = 16.0D;
        double d1 = posX + (rand.nextDouble() - 0.5D) * 8.0D - vec3.xCoord() * d0;
        double d2 = posY + (double) (rand.nextInt(16) - 8) - vec3.yCoord() * d0;
        double d3 = posZ + (rand.nextDouble() - 0.5D) * 8.0D - vec3.zCoord() * d0;
        return teleportTo(d1, d2, d3);
    }

    protected boolean teleportTo(double x, double y, double z) {
        double d0 = posX;
        double d1 = posY;
        double d2 = posZ;
        posX = x;
        posY = y;
        posZ = z;
        boolean flag = false;
        BlockPos blockpos = new BlockPos(posX, posY, posZ);

        if (worldObj.isBlockLoaded(blockpos)) {
            boolean flag1 = false;

            while (!flag1 && blockpos.getY() > 0) {
                BlockPos blockpos1 = blockpos.down();
                Block block = worldObj.getBlockState(blockpos1).getBlock();

                if (block.getMaterial().blocksMovement()) {
                    flag1 = true;
                } else {
                    --posY;
                    blockpos = blockpos1;
                }
            }

            if (flag1) {
                super.setPositionAndUpdate(posX, posY, posZ);

                if (worldObj.getCollidingBoundingBoxes(this, getEntityBoundingBox()).isEmpty() && !worldObj.isAnyLiquid(getEntityBoundingBox())) {
                    flag = true;
                }
            }
        }

        if (!flag) {
            setPosition(d0, d1, d2);
            return false;
        } else {
            int i = 128;

            for (int j = 0; j < i; ++j) {
                double d6 = (double) j / ((double) i - 1.0D);
                float f = (rand.nextFloat() - 0.5F) * 0.2F;
                float f1 = (rand.nextFloat() - 0.5F) * 0.2F;
                float f2 = (rand.nextFloat() - 0.5F) * 0.2F;
                double d3 = d0 + (posX - d0) * d6 + (rand.nextDouble() - 0.5D) * (double) width * 2.0D;
                double d4 = d1 + (posY - d1) * d6 + rand.nextDouble() * (double) height;
                double d5 = d2 + (posZ - d2) * d6 + (rand.nextDouble() - 0.5D) * (double) width * 2.0D;
                worldObj.spawnParticle(EnumParticleTypes.PORTAL, d3, d4, d5, f, f1, f2);
            }

            worldObj.playSoundEffect(d0, d1, d2, "mob.endermen.portal", 1.0F, 1.0F);
            playSound("mob.endermen.portal", 1.0F, 1.0F);
            return true;
        }
    }

    protected String getLivingSound() {
        return isScreaming() ? "mob.endermen.scream" : "mob.endermen.idle";
    }

    protected String getHurtSound() {
        return "mob.endermen.hit";
    }

    protected String getDeathSound() {
        return "mob.endermen.death";
    }

    protected Item getDropItem() {
        return Items.ender_pearl;
    }

    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        Item item = getDropItem();

        if (item != null) {
            int i = rand.nextInt(2 + lootingModifier);

            for (int j = 0; j < i; ++j) {
                dropItem(item, 1);
            }
        }
    }

    public IBlockState getHeldBlockState() {
        return Block.getStateById(dataWatcher.getWatchableObjectShort(16) & 65535);
    }

    public void setHeldBlockState(IBlockState state) {
        dataWatcher.updateObject(16, (short) (Block.getStateId(state) & 65535));
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isEntityInvulnerable(source)) {
            return false;
        } else {
            if (source.getEntity() == null || !(source.getEntity() instanceof EntityEndermite)) {
                if (!worldObj.isRemote) {
                    setScreaming(true);
                }

                if (source instanceof EntityDamageSource && source.getEntity() instanceof EntityPlayer) {
                    if (source.getEntity() instanceof EntityPlayerMP && ((EntityPlayerMP) source.getEntity()).theItemInWorldManager.isCreative()) {
                        setScreaming(false);
                    } else {
                        isAggressive = true;
                    }
                }

                if (source instanceof EntityDamageSourceIndirect) {
                    isAggressive = false;

                    for (int i = 0; i < 64; ++i) {
                        if (teleportRandomly()) {
                            return true;
                        }
                    }

                    return false;
                }
            }

            boolean flag = super.attackEntityFrom(source, amount);

            if (source.isUnblockable() && rand.nextInt(10) != 0) {
                teleportRandomly();
            }

            return flag;
        }
    }

    public boolean isScreaming() {
        return dataWatcher.getWatchableObjectByte(18) > 0;
    }

    public void setScreaming(boolean screaming) {
        dataWatcher.updateObject(18, (byte) (screaming ? 1 : 0));
    }

    static class AIFindPlayer extends EntityAINearestAttackableTarget {
        private EntityPlayer player;
        private int field_179450_h;
        private int field_179451_i;
        private final EntityEnderman enderman;

        public AIFindPlayer(EntityEnderman p_i45842_1_) {
            super(p_i45842_1_, EntityPlayer.class, true);
            enderman = p_i45842_1_;
        }

        public boolean shouldExecute() {
            double d0 = getTargetDistance();
            List<EntityPlayer> list = taskOwner.worldObj.getEntitiesWithinAABB(EntityPlayer.class, taskOwner.getEntityBoundingBox().expand(d0, 4.0D, d0), targetEntitySelector);
            list.sort(theNearestAttackableTargetSorter);

            if (list.isEmpty()) {
                return false;
            } else {
                player = list.getFirst();
                return true;
            }
        }

        public void startExecuting() {
            field_179450_h = 5;
            field_179451_i = 0;
        }

        public void resetTask() {
            player = null;
            enderman.setScreaming(false);
            IAttributeInstance iattributeinstance = enderman.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
            iattributeinstance.removeModifier(EntityEnderman.attackingSpeedBoostModifier);
            super.resetTask();
        }

        public boolean continueExecuting() {
            if (player != null) {
                if (!enderman.shouldAttackPlayer(player)) {
                    return false;
                } else {
                    enderman.isAggressive = true;
                    enderman.faceEntity(player, 10.0F, 10.0F);
                    return true;
                }
            } else {
                return super.continueExecuting();
            }
        }

        public void updateTask() {
            if (player != null) {
                if (--field_179450_h <= 0) {
                    targetEntity = player;
                    player = null;
                    super.startExecuting();
                    enderman.playSound("mob.endermen.stare", 1.0F, 1.0F);
                    enderman.setScreaming(true);
                    IAttributeInstance iattributeinstance = enderman.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
                    iattributeinstance.applyModifier(EntityEnderman.attackingSpeedBoostModifier);
                }
            } else {
                if (targetEntity != null) {
                    if (targetEntity instanceof EntityPlayer && enderman.shouldAttackPlayer((EntityPlayer) targetEntity)) {
                        if (targetEntity.getDistanceSqToEntity(enderman) < 16.0D) {
                            enderman.teleportRandomly();
                        }

                        field_179451_i = 0;
                    } else if (targetEntity.getDistanceSqToEntity(enderman) > 256.0D && field_179451_i++ >= 30 && enderman.teleportToEntity(targetEntity)) {
                        field_179451_i = 0;
                    }
                }

                super.updateTask();
            }
        }
    }

    static class AIPlaceBlock extends EntityAIBase {
        private final EntityEnderman enderman;

        public AIPlaceBlock(EntityEnderman p_i45843_1_) {
            enderman = p_i45843_1_;
        }

        public boolean shouldExecute() {
            return enderman.worldObj.getGameRules().getBoolean("mobGriefing") && (enderman.getHeldBlockState().getBlock().getMaterial() != Material.air && enderman.getRNG().nextInt(2000) == 0);
        }

        public void updateTask() {
            Random random = enderman.getRNG();
            World world = enderman.worldObj;
            int i = MathHelper.floor_double(enderman.posX - 1.0D + random.nextDouble() * 2.0D);
            int j = MathHelper.floor_double(enderman.posY + random.nextDouble() * 2.0D);
            int k = MathHelper.floor_double(enderman.posZ - 1.0D + random.nextDouble() * 2.0D);
            BlockPos blockpos = new BlockPos(i, j, k);
            Block block = world.getBlockState(blockpos).getBlock();
            Block block1 = world.getBlockState(blockpos.down()).getBlock();

            if (func_179474_a(world, blockpos, enderman.getHeldBlockState().getBlock(), block, block1)) {
                world.setBlockState(blockpos, enderman.getHeldBlockState(), 3);
                enderman.setHeldBlockState(Blocks.air.getDefaultState());
            }
        }

        private boolean func_179474_a(World worldIn, BlockPos p_179474_2_, Block p_179474_3_, Block p_179474_4_, Block p_179474_5_) {
            return p_179474_3_.canPlaceBlockAt(worldIn, p_179474_2_) && (p_179474_4_.getMaterial() == Material.air && (p_179474_5_.getMaterial() != Material.air && p_179474_5_.isFullCube()));
        }
    }

    static class AITakeBlock extends EntityAIBase {
        private final EntityEnderman enderman;

        public AITakeBlock(EntityEnderman p_i45841_1_) {
            enderman = p_i45841_1_;
        }

        public boolean shouldExecute() {
            return enderman.worldObj.getGameRules().getBoolean("mobGriefing") && (enderman.getHeldBlockState().getBlock().getMaterial() == Material.air && enderman.getRNG().nextInt(20) == 0);
        }

        public void updateTask() {
            Random random = enderman.getRNG();
            World world = enderman.worldObj;
            int i = MathHelper.floor_double(enderman.posX - 2.0D + random.nextDouble() * 4.0D);
            int j = MathHelper.floor_double(enderman.posY + random.nextDouble() * 3.0D);
            int k = MathHelper.floor_double(enderman.posZ - 2.0D + random.nextDouble() * 4.0D);
            BlockPos blockpos = new BlockPos(i, j, k);
            IBlockState iblockstate = world.getBlockState(blockpos);
            Block block = iblockstate.getBlock();

            if (EntityEnderman.carriableBlocks.contains(block)) {
                enderman.setHeldBlockState(iblockstate);
                world.setBlockState(blockpos, Blocks.air.getDefaultState());
            }
        }
    }
}
