package net.minecraft.entity.monster;

import net.minecraft.block.Block;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class EntityZombie extends EntityMob {
    protected static final IAttribute reinforcementChance = (new RangedAttribute(null, "zombie.spawnReinforcements", 0.0D, 0.0D, 1.0D)).setDescription("Spawn Reinforcements Chance");
    private static final UUID babySpeedBoostUUID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
    private static final AttributeModifier babySpeedBoostModifier = new AttributeModifier(babySpeedBoostUUID, "Baby speed boost", 0.5D, 1);
    private final EntityAIBreakDoor breakDoor = new EntityAIBreakDoor(this);
    private int conversionTime;
    private boolean isBreakDoorsTaskSet = false;
    private float zombieWidth = -1.0F;
    private float zombieHeight;

    public EntityZombie(World worldIn) {
        super(worldIn);
        ((PathNavigateGround) getNavigator()).setBreakDoors(true);
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(2, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
        tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        tasks.addTask(7, new EntityAIWander(this, 1.0D));
        tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(8, new EntityAILookIdle(this));
        applyEntityAI();
        setSize(0.6F, 1.95F);
    }

    protected void applyEntityAI() {
        tasks.addTask(4, new EntityAIAttackOnCollide(this, EntityVillager.class, 1.0D, true));
        tasks.addTask(4, new EntityAIAttackOnCollide(this, EntityIronGolem.class, 1.0D, true));
        tasks.addTask(6, new EntityAIMoveThroughVillage(this, 1.0D, false));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, EntityPigZombie.class));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityVillager.class, false));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityIronGolem.class, true));
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(35.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.23000000417232513D);
        getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(3.0D);
        getAttributeMap().registerAttribute(reinforcementChance).setBaseValue(rand.nextDouble() * 0.10000000149011612D);
    }

    protected void entityInit() {
        super.entityInit();
        getDataWatcher().addObject(12, (byte) 0);
        getDataWatcher().addObject(13, (byte) 0);
        getDataWatcher().addObject(14, (byte) 0);
    }

    public int getTotalArmorValue() {
        int i = super.getTotalArmorValue() + 2;

        if (i > 20) {
            i = 20;
        }

        return i;
    }

    public boolean isBreakDoorsTaskSet() {
        return isBreakDoorsTaskSet;
    }

    public void setBreakDoorsAItask(boolean par1) {
        if (isBreakDoorsTaskSet != par1) {
            isBreakDoorsTaskSet = par1;

            if (par1) {
                tasks.addTask(1, breakDoor);
            } else {
                tasks.removeTask(breakDoor);
            }
        }
    }

    public boolean isChild() {
        return getDataWatcher().getWatchableObjectByte(12) == 1;
    }

    public void setChild(boolean childZombie) {
        getDataWatcher().updateObject(12, (byte) (childZombie ? 1 : 0));

        if (worldObj != null && !worldObj.isRemote) {
            IAttributeInstance iattributeinstance = getEntityAttribute(SharedMonsterAttributes.movementSpeed);
            iattributeinstance.removeModifier(babySpeedBoostModifier);

            if (childZombie) {
                iattributeinstance.applyModifier(babySpeedBoostModifier);
            }
        }

        setChildSize(childZombie);
    }

    protected int getExperiencePoints(EntityPlayer player) {
        if (isChild()) {
            experienceValue = (int) ((float) experienceValue * 2.5F);
        }

        return super.getExperiencePoints(player);
    }

    public boolean isVillager() {
        return getDataWatcher().getWatchableObjectByte(13) == 1;
    }

    public void setVillager(boolean villager) {
        getDataWatcher().updateObject(13, (byte) (villager ? 1 : 0));
    }

    public void onLivingUpdate() {
        if (worldObj.isDaytime() && !worldObj.isRemote && !isChild()) {
            float f = getBrightness(1.0F);
            BlockPos blockpos = new BlockPos(posX, (double) Math.round(posY), posZ);

            if (f > 0.5F && rand.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && worldObj.canSeeSky(blockpos)) {
                boolean flag = true;
                ItemStack itemstack = getEquipmentInSlot(4);

                if (itemstack != null) {
                    if (itemstack.isItemStackDamageable()) {
                        itemstack.setItemDamage(itemstack.getItemDamage() + rand.nextInt(2));

                        if (itemstack.getItemDamage() >= itemstack.getMaxDamage()) {
                            renderBrokenItemStack(itemstack);
                            setCurrentItemOrArmor(4, null);
                        }
                    }

                    flag = false;
                }

                if (flag) {
                    setFire(8);
                }
            }
        }

        if (isRiding() && getAttackTarget() != null && ridingEntity instanceof EntityChicken) {
            ((EntityLiving) ridingEntity).getNavigator().setPath(getNavigator().getPath(), 1.5D);
        }

        super.onLivingUpdate();
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (super.attackEntityFrom(source, amount)) {
            EntityLivingBase entitylivingbase = getAttackTarget();

            if (entitylivingbase == null && source.getEntity() instanceof EntityLivingBase) {
                entitylivingbase = (EntityLivingBase) source.getEntity();
            }

            if (entitylivingbase != null && worldObj.getDifficulty() == EnumDifficulty.HARD && (double) rand.nextFloat() < getEntityAttribute(reinforcementChance).getAttributeValue()) {
                int i = MathHelper.floor_double(posX);
                int j = MathHelper.floor_double(posY);
                int k = MathHelper.floor_double(posZ);
                EntityZombie entityzombie = new EntityZombie(worldObj);

                for (int l = 0; l < 50; ++l) {
                    int i1 = i + MathHelper.getRandomIntegerInRange(rand, 7, 40) * MathHelper.getRandomIntegerInRange(rand, -1, 1);
                    int j1 = j + MathHelper.getRandomIntegerInRange(rand, 7, 40) * MathHelper.getRandomIntegerInRange(rand, -1, 1);
                    int k1 = k + MathHelper.getRandomIntegerInRange(rand, 7, 40) * MathHelper.getRandomIntegerInRange(rand, -1, 1);

                    if (World.doesBlockHaveSolidTopSurface(worldObj, new BlockPos(i1, j1 - 1, k1)) && worldObj.getLightFromNeighbors(new BlockPos(i1, j1, k1)) < 10) {
                        entityzombie.setPosition(i1, j1, k1);

                        if (!worldObj.isAnyPlayerWithinRangeAt(i1, j1, k1, 7.0D) && worldObj.checkNoEntityCollision(entityzombie.getEntityBoundingBox(), entityzombie) && worldObj.getCollidingBoundingBoxes(entityzombie, entityzombie.getEntityBoundingBox()).isEmpty() && !worldObj.isAnyLiquid(entityzombie.getEntityBoundingBox())) {
                            worldObj.spawnEntityInWorld(entityzombie);
                            entityzombie.setAttackTarget(entitylivingbase);
                            entityzombie.onInitialSpawn(worldObj.getDifficultyForLocation(new BlockPos(entityzombie)), null);
                            getEntityAttribute(reinforcementChance).applyModifier(new AttributeModifier("Zombie reinforcement caller charge", -0.05000000074505806D, 0));
                            entityzombie.getEntityAttribute(reinforcementChance).applyModifier(new AttributeModifier("Zombie reinforcement callee charge", -0.05000000074505806D, 0));
                            break;
                        }
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public void onUpdate() {
        if (!worldObj.isRemote && isConverting()) {
            int i = getConversionTimeBoost();
            conversionTime -= i;

            if (conversionTime <= 0) {
                convertToVillager();
            }
        }

        super.onUpdate();
    }

    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag = super.attackEntityAsMob(entityIn);

        if (flag) {
            int i = worldObj.getDifficulty().getDifficultyId();

            if (getHeldItem() == null && isBurning() && rand.nextFloat() < (float) i * 0.3F) {
                entityIn.setFire(2 * i);
            }
        }

        return flag;
    }

    protected String getLivingSound() {
        return "mob.zombie.say";
    }

    protected String getHurtSound() {
        return "mob.zombie.hurt";
    }

    protected String getDeathSound() {
        return "mob.zombie.death";
    }

    protected void playStepSound(BlockPos pos, Block blockIn) {
        playSound("mob.zombie.step", 0.15F, 1.0F);
    }

    protected Item getDropItem() {
        return Items.rotten_flesh;
    }

    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }

    protected void addRandomDrop() {
        switch (rand.nextInt(3)) {
            case 0:
                dropItem(Items.iron_ingot, 1);
                break;

            case 1:
                dropItem(Items.carrot, 1);
                break;

            case 2:
                dropItem(Items.potato, 1);
        }
    }

    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        super.setEquipmentBasedOnDifficulty(difficulty);

        if (rand.nextFloat() < (worldObj.getDifficulty() == EnumDifficulty.HARD ? 0.05F : 0.01F)) {
            int i = rand.nextInt(3);

            if (i == 0) {
                setCurrentItemOrArmor(0, new ItemStack(Items.iron_sword));
            } else {
                setCurrentItemOrArmor(0, new ItemStack(Items.iron_shovel));
            }
        }
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);

        if (isChild()) {
            tagCompound.setBoolean("IsBaby", true);
        }

        if (isVillager()) {
            tagCompound.setBoolean("IsVillager", true);
        }

        tagCompound.setInteger("ConversionTime", isConverting() ? conversionTime : -1);
        tagCompound.setBoolean("CanBreakDoors", isBreakDoorsTaskSet());
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);

        if (tagCompund.getBoolean("IsBaby")) {
            setChild(true);
        }

        if (tagCompund.getBoolean("IsVillager")) {
            setVillager(true);
        }

        if (tagCompund.hasKey("ConversionTime", 99) && tagCompund.getInteger("ConversionTime") > -1) {
            startConversion(tagCompund.getInteger("ConversionTime"));
        }

        setBreakDoorsAItask(tagCompund.getBoolean("CanBreakDoors"));
    }

    public void onKillEntity(EntityLivingBase entityLivingIn) {
        super.onKillEntity(entityLivingIn);

        if ((worldObj.getDifficulty() == EnumDifficulty.NORMAL || worldObj.getDifficulty() == EnumDifficulty.HARD) && entityLivingIn instanceof EntityVillager entityliving) {
            if (worldObj.getDifficulty() != EnumDifficulty.HARD && rand.nextBoolean()) {
                return;
            }

            EntityZombie entityzombie = new EntityZombie(worldObj);
            entityzombie.copyLocationAndAnglesFrom(entityLivingIn);
            worldObj.removeEntity(entityLivingIn);
            entityzombie.onInitialSpawn(worldObj.getDifficultyForLocation(new BlockPos(entityzombie)), null);
            entityzombie.setVillager(true);

            if (entityLivingIn.isChild()) {
                entityzombie.setChild(true);
            }

            entityzombie.setNoAI(entityliving.isAIDisabled());

            if (entityliving.hasCustomName()) {
                entityzombie.setCustomNameTag(entityliving.getCustomNameTag());
                entityzombie.setAlwaysRenderNameTag(entityliving.getAlwaysRenderNameTag());
            }

            worldObj.spawnEntityInWorld(entityzombie);
            worldObj.playAuxSFXAtEntity(null, 1016, new BlockPos((int) posX, (int) posY, (int) posZ), 0);
        }
    }

    public float getEyeHeight() {
        float f = 1.74F;

        if (isChild()) {
            f = (float) ((double) f - 0.81D);
        }

        return f;
    }

    protected boolean func_175448_a(ItemStack stack) {
        return (stack.getItem() != Items.egg || !isChild() || !isRiding()) && super.func_175448_a(stack);
    }

    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        float f = difficulty.getClampedAdditionalDifficulty();
        setCanPickUpLoot(rand.nextFloat() < 0.55F * f);

        if (livingdata == null) {
            livingdata = new EntityZombie.GroupData(worldObj.rand.nextFloat() < 0.05F, worldObj.rand.nextFloat() < 0.05F);
        }

        if (livingdata instanceof GroupData entityzombie$groupdata) {

            if (entityzombie$groupdata.isVillager) {
                setVillager(true);
            }

            if (entityzombie$groupdata.isChild) {
                setChild(true);

                if ((double) worldObj.rand.nextFloat() < 0.05D) {
                    List<EntityChicken> list = worldObj.getEntitiesWithinAABB(EntityChicken.class, getEntityBoundingBox().expand(5.0D, 3.0D, 5.0D), EntitySelectors.IS_STANDALONE);

                    if (!list.isEmpty()) {
                        EntityChicken entitychicken = list.get(0);
                        entitychicken.setChickenJockey(true);
                        mountEntity(entitychicken);
                    }
                } else if ((double) worldObj.rand.nextFloat() < 0.05D) {
                    EntityChicken entitychicken1 = new EntityChicken(worldObj);
                    entitychicken1.setLocationAndAngles(posX, posY, posZ, rotationYaw, 0.0F);
                    entitychicken1.onInitialSpawn(difficulty, null);
                    entitychicken1.setChickenJockey(true);
                    worldObj.spawnEntityInWorld(entitychicken1);
                    mountEntity(entitychicken1);
                }
            }
        }

        setBreakDoorsAItask(rand.nextFloat() < f * 0.1F);
        setEquipmentBasedOnDifficulty(difficulty);
        setEnchantmentBasedOnDifficulty(difficulty);

        if (getEquipmentInSlot(4) == null) {
            Calendar calendar = worldObj.getCurrentDate();

            if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31 && rand.nextFloat() < 0.25F) {
                setCurrentItemOrArmor(4, new ItemStack(rand.nextFloat() < 0.1F ? Blocks.lit_pumpkin : Blocks.pumpkin));
                equipmentDropChances[4] = 0.0F;
            }
        }

        getEntityAttribute(SharedMonsterAttributes.knockbackResistance).applyModifier(new AttributeModifier("Random spawn bonus", rand.nextDouble() * 0.05000000074505806D, 0));
        double d0 = rand.nextDouble() * 1.5D * (double) f;

        if (d0 > 1.0D) {
            getEntityAttribute(SharedMonsterAttributes.followRange).applyModifier(new AttributeModifier("Random zombie-spawn bonus", d0, 2));
        }

        if (rand.nextFloat() < f * 0.05F) {
            getEntityAttribute(reinforcementChance).applyModifier(new AttributeModifier("Leader zombie bonus", rand.nextDouble() * 0.25D + 0.5D, 0));
            getEntityAttribute(SharedMonsterAttributes.maxHealth).applyModifier(new AttributeModifier("Leader zombie bonus", rand.nextDouble() * 3.0D + 1.0D, 2));
            setBreakDoorsAItask(true);
        }

        return livingdata;
    }

    public boolean interact(EntityPlayer player) {
        ItemStack itemstack = player.getCurrentEquippedItem();

        if (itemstack != null && itemstack.getItem() == Items.golden_apple && itemstack.getMetadata() == 0 && isVillager() && isPotionActive(Potion.weakness)) {
            if (!player.capabilities.isCreativeMode) {
                --itemstack.stackSize;
            }

            if (itemstack.stackSize <= 0) {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            }

            if (!worldObj.isRemote) {
                startConversion(rand.nextInt(2401) + 3600);
            }

            return true;
        } else {
            return false;
        }
    }

    protected void startConversion(int ticks) {
        conversionTime = ticks;
        getDataWatcher().updateObject(14, (byte) 1);
        removePotionEffect(Potion.weakness.id);
        addPotionEffect(new PotionEffect(Potion.damageBoost.id, ticks, Math.min(worldObj.getDifficulty().getDifficultyId() - 1, 0)));
        worldObj.setEntityState(this, (byte) 16);
    }

    public void handleStatusUpdate(byte id) {
        if (id == 16) {
            if (!isSilent()) {
                worldObj.playSound(posX + 0.5D, posY + 0.5D, posZ + 0.5D, "mob.zombie.remedy", 1.0F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F, false);
            }
        } else {
            super.handleStatusUpdate(id);
        }
    }

    protected boolean canDespawn() {
        return !isConverting();
    }

    public boolean isConverting() {
        return getDataWatcher().getWatchableObjectByte(14) == 1;
    }

    protected void convertToVillager() {
        EntityVillager entityvillager = new EntityVillager(worldObj);
        entityvillager.copyLocationAndAnglesFrom(this);
        entityvillager.onInitialSpawn(worldObj.getDifficultyForLocation(new BlockPos(entityvillager)), null);
        entityvillager.setLookingForHome();

        if (isChild()) {
            entityvillager.setGrowingAge(-24000);
        }

        worldObj.removeEntity(this);
        entityvillager.setNoAI(isAIDisabled());

        if (hasCustomName()) {
            entityvillager.setCustomNameTag(getCustomNameTag());
            entityvillager.setAlwaysRenderNameTag(getAlwaysRenderNameTag());
        }

        worldObj.spawnEntityInWorld(entityvillager);
        entityvillager.addPotionEffect(new PotionEffect(Potion.confusion.id, 200, 0));
        worldObj.playAuxSFXAtEntity(null, 1017, new BlockPos((int) posX, (int) posY, (int) posZ), 0);
    }

    protected int getConversionTimeBoost() {
        int i = 1;

        if (rand.nextFloat() < 0.01F) {
            int j = 0;
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (int k = (int) posX - 4; k < (int) posX + 4 && j < 14; ++k) {
                for (int l = (int) posY - 4; l < (int) posY + 4 && j < 14; ++l) {
                    for (int i1 = (int) posZ - 4; i1 < (int) posZ + 4 && j < 14; ++i1) {
                        Block block = worldObj.getBlockState(blockpos$mutableblockpos.set(k, l, i1)).getBlock();

                        if (block == Blocks.iron_bars || block == Blocks.bed) {
                            if (rand.nextFloat() < 0.3F) {
                                ++i;
                            }

                            ++j;
                        }
                    }
                }
            }
        }

        return i;
    }

    public void setChildSize(boolean isChild) {
        multiplySize(isChild ? 0.5F : 1.0F);
    }

    protected final void setSize(float width, float height) {
        boolean flag = zombieWidth > 0.0F && zombieHeight > 0.0F;
        zombieWidth = width;
        zombieHeight = height;

        if (!flag) {
            multiplySize(1.0F);
        }
    }

    protected final void multiplySize(float size) {
        super.setSize(zombieWidth * size, zombieHeight * size);
    }

    public double getYOffset() {
        return isChild() ? 0.0D : -0.35D;
    }

    public void onDeath(DamageSource cause) {
        super.onDeath(cause);

        if (cause.getEntity() instanceof EntityCreeper && !(this instanceof EntityPigZombie) && ((EntityCreeper) cause.getEntity()).getPowered() && ((EntityCreeper) cause.getEntity()).isAIEnabled()) {
            ((EntityCreeper) cause.getEntity()).func_175493_co();
            entityDropItem(new ItemStack(Items.skull, 1, 2), 0.0F);
        }
    }

    class GroupData implements IEntityLivingData {
        public boolean isChild;
        public boolean isVillager;

        private GroupData(boolean isBaby, boolean isVillagerZombie) {
            isChild = false;
            isVillager = false;
            isChild = isBaby;
            isVillager = isVillagerZombie;
        }
    }
}
