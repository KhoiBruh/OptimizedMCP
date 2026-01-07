package net.minecraft.entity.passive;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCarrot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.*;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityRabbit extends EntityAnimal {
    private final EntityRabbit.AIAvoidEntity<EntityWolf> aiAvoidWolves;
    private int field_175540_bm = 0;
    private int field_175535_bn = 0;
    private boolean field_175536_bo = false;
    private boolean field_175537_bp = false;
    private int currentMoveTypeDuration = 0;
    private EntityRabbit.EnumMoveType moveType = EntityRabbit.EnumMoveType.HOP;
    private int carrotTicks = 0;
    private final EntityPlayer field_175543_bt = null;

    public EntityRabbit(World worldIn) {
        super(worldIn);
        setSize(0.6F, 0.7F);
        jumpHelper = new EntityRabbit.RabbitJumpHelper(this);
        moveHelper = new EntityRabbit.RabbitMoveHelper(this);
        ((PathNavigateGround) getNavigator()).setAvoidsWater(true);
        navigator.setHeightRequirement(2.5F);
        tasks.addTask(1, new EntityAISwimming(this));
        tasks.addTask(1, new EntityRabbit.AIPanic(this, 1.33D));
        tasks.addTask(2, new EntityAITempt(this, 1.0D, Items.carrot, false));
        tasks.addTask(2, new EntityAITempt(this, 1.0D, Items.golden_carrot, false));
        tasks.addTask(2, new EntityAITempt(this, 1.0D, Item.getItemFromBlock(Blocks.yellow_flower), false));
        tasks.addTask(3, new EntityAIMate(this, 0.8D));
        tasks.addTask(5, new EntityRabbit.AIRaidFarm(this));
        tasks.addTask(5, new EntityAIWander(this, 0.6D));
        tasks.addTask(11, new EntityAIWatchClosest(this, EntityPlayer.class, 10.0F));
        aiAvoidWolves = new EntityRabbit.AIAvoidEntity<>(this, EntityWolf.class, 16.0F, 1.33D, 1.33D);
        tasks.addTask(4, aiAvoidWolves);
        setMovementSpeed(0.0D);
    }

    protected float getJumpUpwardsMotion() {
        return moveHelper.isUpdating() && moveHelper.getY() > posY + 0.5D ? 0.5F : moveType.func_180074_b();
    }

    public void setMoveType(EntityRabbit.EnumMoveType type) {
        moveType = type;
    }

    public float func_175521_o(float p_175521_1_) {
        return field_175535_bn == 0 ? 0.0F : ((float) field_175540_bm + p_175521_1_) / (float) field_175535_bn;
    }

    public void setMovementSpeed(double newSpeed) {
        getNavigator().setSpeed(newSpeed);
        moveHelper.setMoveTo(moveHelper.getX(), moveHelper.getY(), moveHelper.getZ(), newSpeed);
    }

    public void setJumping(boolean jump, EntityRabbit.EnumMoveType moveTypeIn) {
        super.setJumping(jump);

        if (!jump) {
            if (moveType == EntityRabbit.EnumMoveType.ATTACK) {
                moveType = EntityRabbit.EnumMoveType.HOP;
            }
        } else {
            setMovementSpeed(1.5D * (double) moveTypeIn.getSpeed());
            playSound(getJumpingSound(), getSoundVolume(), ((rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F) * 0.8F);
        }

        field_175536_bo = jump;
    }

    public void doMovementAction(EntityRabbit.EnumMoveType movetype) {
        setJumping(true, movetype);
        field_175535_bn = movetype.func_180073_d();
        field_175540_bm = 0;
    }

    public boolean func_175523_cj() {
        return field_175536_bo;
    }

    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(18, (byte) 0);
    }

    public void updateAITasks() {
        if (moveHelper.getSpeed() > 0.8D) {
            moveType = EnumMoveType.SPRINT;
        } else if (moveType != EntityRabbit.EnumMoveType.ATTACK) {
            moveType = EnumMoveType.HOP;
        }

        if (currentMoveTypeDuration > 0) {
            --currentMoveTypeDuration;
        }

        if (carrotTicks > 0) {
            carrotTicks -= rand.nextInt(3);

            if (carrotTicks < 0) {
                carrotTicks = 0;
            }
        }

        if (onGround) {
            if (!field_175537_bp) {
                setJumping(false, EntityRabbit.EnumMoveType.NONE);
                func_175517_cu();
            }

            if (getRabbitType() == 99 && currentMoveTypeDuration == 0) {
                EntityLivingBase entitylivingbase = getAttackTarget();

                if (entitylivingbase != null && getDistanceSqToEntity(entitylivingbase) < 16.0D) {
                    calculateRotationYaw(entitylivingbase.posX, entitylivingbase.posZ);
                    moveHelper.setMoveTo(entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ, moveHelper.getSpeed());
                    doMovementAction(EntityRabbit.EnumMoveType.ATTACK);
                    field_175537_bp = true;
                }
            }

            EntityRabbit.RabbitJumpHelper entityrabbit$rabbitjumphelper = (EntityRabbit.RabbitJumpHelper) jumpHelper;

            if (!entityrabbit$rabbitjumphelper.getIsJumping()) {
                if (moveHelper.isUpdating() && currentMoveTypeDuration == 0) {
                    PathEntity pathentity = navigator.getPath();
                    Vec3 vec3 = new Vec3(moveHelper.getX(), moveHelper.getY(), moveHelper.getZ());

                    if (pathentity != null && pathentity.getCurrentPathIndex() < pathentity.getCurrentPathLength()) {
                        vec3 = pathentity.getPosition(this);
                    }

                    calculateRotationYaw(vec3.xCoord(), vec3.zCoord());
                    doMovementAction(moveType);
                }
            } else if (!entityrabbit$rabbitjumphelper.func_180065_d()) {
                func_175518_cr();
            }
        }

        field_175537_bp = onGround;
    }

    public void spawnRunningParticles() {
    }

    private void calculateRotationYaw(double x, double z) {
        rotationYaw = (float) (MathHelper.atan2(z - posZ, x - posX) * 180.0D / Math.PI) - 90.0F;
    }

    private void func_175518_cr() {
        ((EntityRabbit.RabbitJumpHelper) jumpHelper).func_180066_a(true);
    }

    private void func_175520_cs() {
        ((EntityRabbit.RabbitJumpHelper) jumpHelper).func_180066_a(false);
    }

    private void updateMoveTypeDuration() {
        currentMoveTypeDuration = getMoveTypeDuration();
    }

    private void func_175517_cu() {
        updateMoveTypeDuration();
        func_175520_cs();
    }

    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (field_175540_bm != field_175535_bn) {
            if (field_175540_bm == 0 && !worldObj.isRemote) {
                worldObj.setEntityState(this, (byte) 1);
            }

            ++field_175540_bm;
        } else if (field_175535_bn != 0) {
            field_175540_bm = 0;
            field_175535_bn = 0;
        }
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.30000001192092896D);
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setInteger("RabbitType", getRabbitType());
        tagCompound.setInteger("MoreCarrotTicks", carrotTicks);
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        setRabbitType(tagCompund.getInteger("RabbitType"));
        carrotTicks = tagCompund.getInteger("MoreCarrotTicks");
    }

    protected String getJumpingSound() {
        return "mob.rabbit.hop";
    }

    protected String getLivingSound() {
        return "mob.rabbit.idle";
    }

    protected String getHurtSound() {
        return "mob.rabbit.hurt";
    }

    protected String getDeathSound() {
        return "mob.rabbit.death";
    }

    public boolean attackEntityAsMob(Entity entityIn) {
        if (getRabbitType() == 99) {
            playSound("mob.attack", 1.0F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
            return entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 8.0F);
        } else {
            return entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 3.0F);
        }
    }

    public int getTotalArmorValue() {
        return getRabbitType() == 99 ? 8 : super.getTotalArmorValue();
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        return !isEntityInvulnerable(source) && super.attackEntityFrom(source, amount);
    }

    protected void addRandomDrop() {
        entityDropItem(new ItemStack(Items.rabbit_foot, 1), 0.0F);
    }

    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        int i = rand.nextInt(2) + rand.nextInt(1 + lootingModifier);

        for (int j = 0; j < i; ++j) {
            dropItem(Items.rabbit_hide, 1);
        }

        i = rand.nextInt(2);

        for (int k = 0; k < i; ++k) {
            if (isBurning()) {
                dropItem(Items.cooked_rabbit, 1);
            } else {
                dropItem(Items.rabbit, 1);
            }
        }
    }

    private boolean isRabbitBreedingItem(Item itemIn) {
        return itemIn == Items.carrot || itemIn == Items.golden_carrot || itemIn == Item.getItemFromBlock(Blocks.yellow_flower);
    }

    public EntityRabbit createChild(EntityAgeable ageable) {
        EntityRabbit entityrabbit = new EntityRabbit(worldObj);

        if (ageable instanceof EntityRabbit) {
            entityrabbit.setRabbitType(rand.nextBoolean() ? getRabbitType() : ((EntityRabbit) ageable).getRabbitType());
        }

        return entityrabbit;
    }

    public boolean isBreedingItem(ItemStack stack) {
        return stack != null && isRabbitBreedingItem(stack.getItem());
    }

    public int getRabbitType() {
        return dataWatcher.getWatchableObjectByte(18);
    }

    public void setRabbitType(int rabbitTypeId) {
        if (rabbitTypeId == 99) {
            tasks.removeTask(aiAvoidWolves);
            tasks.addTask(4, new EntityRabbit.AIEvilAttack(this));
            targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
            targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
            targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityWolf.class, true));

            if (!hasCustomName()) {
                setCustomNameTag(StatCollector.translateToLocal("entity.KillerBunny.name"));
            }
        }

        dataWatcher.updateObject(18, (byte) rabbitTypeId);
    }

    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        int i = rand.nextInt(6);
        boolean flag = false;

        if (livingdata instanceof EntityRabbit.RabbitTypeData) {
            i = ((EntityRabbit.RabbitTypeData) livingdata).typeData;
            flag = true;
        } else {
            livingdata = new EntityRabbit.RabbitTypeData(i);
        }

        setRabbitType(i);

        if (flag) {
            setGrowingAge(-24000);
        }

        return livingdata;
    }

    private boolean isCarrotEaten() {
        return carrotTicks == 0;
    }

    protected int getMoveTypeDuration() {
        return moveType.getDuration();
    }

    protected void createEatingParticles() {
        worldObj.spawnParticle(EnumParticleTypes.BLOCK_DUST, posX + (double) (rand.nextFloat() * width * 2.0F) - (double) width, posY + 0.5D + (double) (rand.nextFloat() * height), posZ + (double) (rand.nextFloat() * width * 2.0F) - (double) width, 0.0D, 0.0D, 0.0D, Block.getStateId(Blocks.carrots.getStateFromMeta(7)));
        carrotTicks = 100;
    }

    public void handleStatusUpdate(byte id) {
        if (id == 1) {
            createRunningParticles();
            field_175535_bn = 10;
            field_175540_bm = 0;
        } else {
            super.handleStatusUpdate(id);
        }
    }

    enum EnumMoveType {
        NONE(0.0F, 0.0F, 30, 1),
        HOP(0.8F, 0.2F, 20, 10),
        STEP(1.0F, 0.45F, 14, 14),
        SPRINT(1.75F, 0.4F, 1, 8),
        ATTACK(2.0F, 0.7F, 7, 8);

        private final float speed;
        private final float field_180077_g;
        private final int duration;
        private final int field_180085_i;

        EnumMoveType(float typeSpeed, float p_i45866_4_, int typeDuration, int p_i45866_6_) {
            speed = typeSpeed;
            field_180077_g = p_i45866_4_;
            duration = typeDuration;
            field_180085_i = p_i45866_6_;
        }

        public float getSpeed() {
            return speed;
        }

        public float func_180074_b() {
            return field_180077_g;
        }

        public int getDuration() {
            return duration;
        }

        public int func_180073_d() {
            return field_180085_i;
        }
    }

    static class AIAvoidEntity<T extends Entity> extends EntityAIAvoidEntity<T> {
        private final EntityRabbit entityInstance;

        public AIAvoidEntity(EntityRabbit rabbit, Class<T> p_i46403_2_, float p_i46403_3_, double p_i46403_4_, double p_i46403_6_) {
            super(rabbit, p_i46403_2_, p_i46403_3_, p_i46403_4_, p_i46403_6_);
            entityInstance = rabbit;
        }

        public void updateTask() {
            super.updateTask();
        }
    }

    static class AIEvilAttack extends EntityAIAttackOnCollide {
        public AIEvilAttack(EntityRabbit rabbit) {
            super(rabbit, EntityLivingBase.class, 1.4D, true);
        }

        protected double func_179512_a(EntityLivingBase attackTarget) {
            return 4.0F + attackTarget.width;
        }
    }

    static class AIPanic extends EntityAIPanic {
        private final EntityRabbit theEntity;

        public AIPanic(EntityRabbit rabbit, double speedIn) {
            super(rabbit, speedIn);
            theEntity = rabbit;
        }

        public void updateTask() {
            super.updateTask();
            theEntity.setMovementSpeed(speed);
        }
    }

    static class AIRaidFarm extends EntityAIMoveToBlock {
        private final EntityRabbit rabbit;
        private boolean field_179498_d;
        private boolean field_179499_e = false;

        public AIRaidFarm(EntityRabbit rabbitIn) {
            super(rabbitIn, 0.699999988079071D, 16);
            rabbit = rabbitIn;
        }

        public boolean shouldExecute() {
            if (runDelay <= 0) {
                if (!rabbit.worldObj.getGameRules().getBoolean("mobGriefing")) {
                    return false;
                }

                field_179499_e = false;
                field_179498_d = rabbit.isCarrotEaten();
            }

            return super.shouldExecute();
        }

        public boolean continueExecuting() {
            return field_179499_e && super.continueExecuting();
        }

        public void startExecuting() {
            super.startExecuting();
        }

        public void resetTask() {
            super.resetTask();
        }

        public void updateTask() {
            super.updateTask();
            rabbit.getLookHelper().setLookPosition((double) destinationBlock.getX() + 0.5D, destinationBlock.getY() + 1, (double) destinationBlock.getZ() + 0.5D, 10.0F, (float) rabbit.getVerticalFaceSpeed());

            if (getIsAboveDestination()) {
                World world = rabbit.worldObj;
                BlockPos blockpos = destinationBlock.up();
                IBlockState iblockstate = world.getBlockState(blockpos);
                Block block = iblockstate.getBlock();

                if (field_179499_e && block instanceof BlockCarrot && iblockstate.getValue(BlockCarrot.AGE) == 7) {
                    world.setBlockState(blockpos, Blocks.air.getDefaultState(), 2);
                    world.destroyBlock(blockpos, true);
                    rabbit.createEatingParticles();
                }

                field_179499_e = false;
                runDelay = 10;
            }
        }

        protected boolean shouldMoveTo(World worldIn, BlockPos pos) {
            Block block = worldIn.getBlockState(pos).getBlock();

            if (block == Blocks.farmland) {
                pos = pos.up();
                IBlockState iblockstate = worldIn.getBlockState(pos);
                block = iblockstate.getBlock();

                if (block instanceof BlockCarrot && iblockstate.getValue(BlockCarrot.AGE) == 7 && field_179498_d && !field_179499_e) {
                    field_179499_e = true;
                    return true;
                }
            }

            return false;
        }
    }

    static class RabbitMoveHelper extends EntityMoveHelper {
        private final EntityRabbit theEntity;

        public RabbitMoveHelper(EntityRabbit rabbit) {
            super(rabbit);
            theEntity = rabbit;
        }

        public void onUpdateMoveHelper() {
            if (theEntity.onGround && !theEntity.func_175523_cj()) {
                theEntity.setMovementSpeed(0.0D);
            }

            super.onUpdateMoveHelper();
        }
    }

    public static class RabbitTypeData implements IEntityLivingData {
        public int typeData;

        public RabbitTypeData(int type) {
            typeData = type;
        }
    }

    public class RabbitJumpHelper extends EntityJumpHelper {
        private final EntityRabbit theEntity;
        private boolean field_180068_d = false;

        public RabbitJumpHelper(EntityRabbit rabbit) {
            super(rabbit);
            theEntity = rabbit;
        }

        public boolean getIsJumping() {
            return isJumping;
        }

        public boolean func_180065_d() {
            return field_180068_d;
        }

        public void func_180066_a(boolean p_180066_1_) {
            field_180068_d = p_180066_1_;
        }

        public void doJump() {
            if (isJumping) {
                theEntity.doMovementAction(EntityRabbit.EnumMoveType.STEP);
                isJumping = false;
            }
        }
    }
}
