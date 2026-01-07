package net.minecraft.entity.player;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.event.ClickEvent;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.*;
import net.minecraft.world.*;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("incomplete-switch")
public abstract class EntityPlayer extends EntityLivingBase {
    private final GameProfile gameProfile;
    public InventoryPlayer inventory = new InventoryPlayer(this);
    public Container inventoryContainer;
    public Container openContainer;
    public float prevCameraYaw;
    public float cameraYaw;
    public int xpCooldown;
    public double prevChasingPosX;
    public double prevChasingPosY;
    public double prevChasingPosZ;
    public double chasingPosX;
    public double chasingPosY;
    public double chasingPosZ;
    public BlockPos playerLocation;
    public float renderOffsetX;
    public float renderOffsetY;
    public float renderOffsetZ;
    public PlayerCapabilities capabilities = new PlayerCapabilities();
    public int experienceLevel;
    public int experienceTotal;
    public float experience;
    public EntityFishHook fishEntity;
    protected FoodStats foodStats = new FoodStats();
    protected int flyToggleTimer;
    protected boolean sleeping;
    protected float speedOnGround = 0.1F;
    protected float speedInAir = 0.02F;
    private InventoryEnderChest theInventoryEnderChest = new InventoryEnderChest();
    private int sleepTimer;
    private BlockPos spawnChunk;
    private boolean spawnForced;
    private BlockPos startMinecartRidingCoordinate;
    private int xpSeed;
    private ItemStack itemInUse;
    private int itemInUseCount;
    private int lastXPSound;
    private boolean hasReducedDebug = false;

    public EntityPlayer(World worldIn, GameProfile gameProfileIn) {
        super(worldIn);
        entityUniqueID = getUUID(gameProfileIn);
        gameProfile = gameProfileIn;
        inventoryContainer = new ContainerPlayer(inventory, !worldIn.isRemote, this);
        openContainer = inventoryContainer;
        BlockPos blockpos = worldIn.getSpawnPoint();
        setLocationAndAngles((double) blockpos.getX() + 0.5D, blockpos.getY() + 1, (double) blockpos.getZ() + 0.5D, 0.0F, 0.0F);
        unused180 = 180.0F;
        fireResistance = 20;
    }

    public static BlockPos getBedSpawnLocation(World worldIn, BlockPos bedLocation, boolean forceSpawn) {
        Block block = worldIn.getBlockState(bedLocation).getBlock();

        if (block != Blocks.bed) {
            if (!forceSpawn) {
                return null;
            } else {
                boolean flag = block.canSpawnInBlock();
                boolean flag1 = worldIn.getBlockState(bedLocation.up()).getBlock().canSpawnInBlock();
                return flag && flag1 ? bedLocation : null;
            }
        } else {
            return BlockBed.getSafeExitLocation(worldIn, bedLocation, 0);
        }
    }

    public static UUID getUUID(GameProfile profile) {
        UUID uuid = profile.getId();

        if (uuid == null) {
            uuid = getOfflineUUID(profile.getName());
        }

        return uuid;
    }

    public static UUID getOfflineUUID(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(1.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.10000000149011612D);
    }

    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(16, (byte) 0);
        dataWatcher.addObject(17, 0.0F);
        dataWatcher.addObject(18, 0);
        dataWatcher.addObject(10, (byte) 0);
    }

    public ItemStack getItemInUse() {
        return itemInUse;
    }

    public int getItemInUseCount() {
        return itemInUseCount;
    }

    public boolean isUsingItem() {
        return itemInUse != null;
    }

    public int getItemInUseDuration() {
        return isUsingItem() ? itemInUse.getMaxItemUseDuration() - itemInUseCount : 0;
    }

    public void stopUsingItem() {
        if (itemInUse != null) {
            itemInUse.onPlayerStoppedUsing(worldObj, this, itemInUseCount);
        }

        clearItemInUse();
    }

    public void clearItemInUse() {
        itemInUse = null;
        itemInUseCount = 0;

        if (!worldObj.isRemote) {
            setEating(false);
        }
    }

    public boolean isBlocking() {
        return isUsingItem() && itemInUse.getItem().getItemUseAction(itemInUse) == EnumAction.BLOCK;
    }

    public void onUpdate() {
        noClip = isSpectator();

        if (isSpectator()) {
            onGround = false;
        }

        if (itemInUse != null) {
            ItemStack itemstack = inventory.getCurrentItem();

            if (itemstack == itemInUse) {
                if (itemInUseCount <= 25 && itemInUseCount % 4 == 0) {
                    updateItemUse(itemstack, 5);
                }

                if (--itemInUseCount == 0 && !worldObj.isRemote) {
                    onItemUseFinish();
                }
            } else {
                clearItemInUse();
            }
        }

        if (xpCooldown > 0) {
            --xpCooldown;
        }

        if (isPlayerSleeping()) {
            ++sleepTimer;

            if (sleepTimer > 100) {
                sleepTimer = 100;
            }

            if (!worldObj.isRemote) {
                if (!isInBed()) {
                    wakeUpPlayer(true, true, false);
                } else if (worldObj.isDaytime()) {
                    wakeUpPlayer(false, true, true);
                }
            }
        } else if (sleepTimer > 0) {
            ++sleepTimer;

            if (sleepTimer >= 110) {
                sleepTimer = 0;
            }
        }

        super.onUpdate();

        if (!worldObj.isRemote && openContainer != null && !openContainer.canInteractWith(this)) {
            closeScreen();
            openContainer = inventoryContainer;
        }

        if (isBurning() && capabilities.disableDamage) {
            extinguish();
        }

        prevChasingPosX = chasingPosX;
        prevChasingPosY = chasingPosY;
        prevChasingPosZ = chasingPosZ;
        double d5 = posX - chasingPosX;
        double d0 = posY - chasingPosY;
        double d1 = posZ - chasingPosZ;
        double d2 = 10.0D;

        if (d5 > d2) {
            prevChasingPosX = chasingPosX = posX;
        }

        if (d1 > d2) {
            prevChasingPosZ = chasingPosZ = posZ;
        }

        if (d0 > d2) {
            prevChasingPosY = chasingPosY = posY;
        }

        if (d5 < -d2) {
            prevChasingPosX = chasingPosX = posX;
        }

        if (d1 < -d2) {
            prevChasingPosZ = chasingPosZ = posZ;
        }

        if (d0 < -d2) {
            prevChasingPosY = chasingPosY = posY;
        }

        chasingPosX += d5 * 0.25D;
        chasingPosZ += d1 * 0.25D;
        chasingPosY += d0 * 0.25D;

        if (ridingEntity == null) {
            startMinecartRidingCoordinate = null;
        }

        if (!worldObj.isRemote) {
            foodStats.onUpdate(this);
            triggerAchievement(StatList.minutesPlayedStat);

            if (isEntityAlive()) {
                triggerAchievement(StatList.timeSinceDeathStat);
            }
        }

        int i = 29999999;
        double d3 = MathHelper.clamp_double(posX, -2.9999999E7D, 2.9999999E7D);
        double d4 = MathHelper.clamp_double(posZ, -2.9999999E7D, 2.9999999E7D);

        if (d3 != posX || d4 != posZ) {
            setPosition(d3, posY, d4);
        }
    }

    public int getMaxInPortalTime() {
        return capabilities.disableDamage ? 0 : 80;
    }

    protected String getSwimSound() {
        return "game.player.swim";
    }

    protected String getSplashSound() {
        return "game.player.swim.splash";
    }

    public int getPortalCooldown() {
        return 10;
    }

    public void playSound(String name, float volume, float pitch) {
        worldObj.playSoundToNearExcept(this, name, volume, pitch);
    }

    protected void updateItemUse(ItemStack itemStackIn, int p_71010_2_) {
        if (itemStackIn.getItemUseAction() == EnumAction.DRINK) {
            playSound("random.drink", 0.5F, worldObj.rand.nextFloat() * 0.1F + 0.9F);
        }

        if (itemStackIn.getItemUseAction() == EnumAction.EAT) {
            for (int i = 0; i < p_71010_2_; ++i) {
                Vec3 vec3 = new Vec3(((double) rand.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
                vec3 = vec3.rotatePitch(-rotationPitch * (float) Math.PI / 180.0F);
                vec3 = vec3.rotateYaw(-rotationYaw * (float) Math.PI / 180.0F);
                double d0 = (double) (-rand.nextFloat()) * 0.6D - 0.3D;
                Vec3 vec31 = new Vec3(((double) rand.nextFloat() - 0.5D) * 0.3D, d0, 0.6D);
                vec31 = vec31.rotatePitch(-rotationPitch * (float) Math.PI / 180.0F);
                vec31 = vec31.rotateYaw(-rotationYaw * (float) Math.PI / 180.0F);
                vec31 = vec31.addVector(posX, posY + (double) getEyeHeight(), posZ);

                if (itemStackIn.getHasSubtypes()) {
                    worldObj.spawnParticle(EnumParticleTypes.ITEM_CRACK, vec31.xCoord(), vec31.yCoord(), vec31.zCoord(), vec3.xCoord(), vec3.yCoord() + 0.05D, vec3.zCoord(), Item.getIdFromItem(itemStackIn.getItem()), itemStackIn.getMetadata());
                } else {
                    worldObj.spawnParticle(EnumParticleTypes.ITEM_CRACK, vec31.xCoord(), vec31.yCoord(), vec31.zCoord(), vec3.xCoord(), vec3.yCoord() + 0.05D, vec3.zCoord(), Item.getIdFromItem(itemStackIn.getItem()));
                }
            }

            playSound("random.eat", 0.5F + 0.5F * (float) rand.nextInt(2), (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
        }
    }

    protected void onItemUseFinish() {
        if (itemInUse != null) {
            updateItemUse(itemInUse, 16);
            int i = itemInUse.stackSize;
            ItemStack itemstack = itemInUse.onItemUseFinish(worldObj, this);

            if (itemstack != itemInUse || itemstack != null && itemstack.stackSize != i) {
                inventory.mainInventory[inventory.currentItem] = itemstack;

                if (itemstack.stackSize == 0) {
                    inventory.mainInventory[inventory.currentItem] = null;
                }
            }

            clearItemInUse();
        }
    }

    public void handleStatusUpdate(byte id) {
        if (id == 9) {
            onItemUseFinish();
        } else if (id == 23) {
            hasReducedDebug = false;
        } else if (id == 22) {
            hasReducedDebug = true;
        } else {
            super.handleStatusUpdate(id);
        }
    }

    protected boolean isMovementBlocked() {
        return getHealth() <= 0.0F || isPlayerSleeping();
    }

    protected void closeScreen() {
        openContainer = inventoryContainer;
    }

    public void updateRidden() {
        if (!worldObj.isRemote && isSneaking()) {
            mountEntity(null);
            setSneaking(false);
        } else {
            double d0 = posX;
            double d1 = posY;
            double d2 = posZ;
            float f = rotationYaw;
            float f1 = rotationPitch;
            super.updateRidden();
            prevCameraYaw = cameraYaw;
            cameraYaw = 0.0F;
            addMountedMovementStat(posX - d0, posY - d1, posZ - d2);

            if (ridingEntity instanceof EntityPig) {
                rotationPitch = f1;
                rotationYaw = f;
                renderYawOffset = ((EntityPig) ridingEntity).renderYawOffset;
            }
        }
    }

    public void preparePlayerToSpawn() {
        setSize(0.6F, 1.8F);
        super.preparePlayerToSpawn();
        setHealth(getMaxHealth());
        deathTime = 0;
    }

    protected void updateEntityActionState() {
        super.updateEntityActionState();
        updateArmSwingProgress();
        rotationYawHead = rotationYaw;
    }

    public void onLivingUpdate() {
        if (flyToggleTimer > 0) {
            --flyToggleTimer;
        }

        if (worldObj.getDifficulty() == EnumDifficulty.PEACEFUL && worldObj.getGameRules().getBoolean("naturalRegeneration")) {
            if (getHealth() < getMaxHealth() && ticksExisted % 20 == 0) {
                heal(1.0F);
            }

            if (foodStats.needFood() && ticksExisted % 10 == 0) {
                foodStats.setFoodLevel(foodStats.getFoodLevel() + 1);
            }
        }

        inventory.decrementAnimations();
        prevCameraYaw = cameraYaw;
        super.onLivingUpdate();
        IAttributeInstance iattributeinstance = getEntityAttribute(SharedMonsterAttributes.movementSpeed);

        if (!worldObj.isRemote) {
            iattributeinstance.setBaseValue(capabilities.getWalkSpeed());
        }

        jumpMovementFactor = speedInAir;

        if (isSprinting()) {
            jumpMovementFactor = (float) ((double) jumpMovementFactor + (double) speedInAir * 0.3D);
        }

        setAIMoveSpeed((float) iattributeinstance.getAttributeValue());
        float f = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
        float f1 = (float) (Math.atan(-motionY * 0.20000000298023224D) * 15.0D);

        if (f > 0.1F) {
            f = 0.1F;
        }

        if (!onGround || getHealth() <= 0.0F) {
            f = 0.0F;
        }

        if (onGround || getHealth() <= 0.0F) {
            f1 = 0.0F;
        }

        cameraYaw += (f - cameraYaw) * 0.4F;
        cameraPitch += (f1 - cameraPitch) * 0.8F;

        if (getHealth() > 0.0F && !isSpectator()) {
            AxisAlignedBB axisalignedbb = null;

            if (ridingEntity != null && !ridingEntity.isDead) {
                axisalignedbb = getEntityBoundingBox().union(ridingEntity.getEntityBoundingBox()).expand(1.0D, 0.0D, 1.0D);
            } else {
                axisalignedbb = getEntityBoundingBox().expand(1.0D, 0.5D, 1.0D);
            }

            List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, axisalignedbb);

            for (Entity entity : list) {
                if (!entity.isDead) {
                    collideWithPlayer(entity);
                }
            }
        }
    }

    private void collideWithPlayer(Entity p_71044_1_) {
        p_71044_1_.onCollideWithPlayer(this);
    }

    public int getScore() {
        return dataWatcher.getWatchableObjectInt(18);
    }

    public void setScore(int p_85040_1_) {
        dataWatcher.updateObject(18, p_85040_1_);
    }

    public void addScore(int p_85039_1_) {
        int i = getScore();
        dataWatcher.updateObject(18, i + p_85039_1_);
    }

    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        setSize(0.2F, 0.2F);
        setPosition(posX, posY, posZ);
        motionY = 0.10000000149011612D;

        if (getName().equals("Notch")) {
            dropItem(new ItemStack(Items.apple, 1), true, false);
        }

        if (!worldObj.getGameRules().getBoolean("keepInventory")) {
            inventory.dropAllItems();
        }

        if (cause != null) {
            motionX = -MathHelper.cos((attackedAtYaw + rotationYaw) * (float) Math.PI / 180.0F) * 0.1F;
            motionZ = -MathHelper.sin((attackedAtYaw + rotationYaw) * (float) Math.PI / 180.0F) * 0.1F;
        } else {
            motionX = motionZ = 0.0D;
        }

        triggerAchievement(StatList.deathsStat);
        func_175145_a(StatList.timeSinceDeathStat);
    }

    protected String getHurtSound() {
        return "game.player.hurt";
    }

    protected String getDeathSound() {
        return "game.player.die";
    }

    public void addToPlayerScore(Entity entityIn, int amount) {
        addScore(amount);
        Collection<ScoreObjective> collection = getWorldScoreboard().getObjectivesFromCriteria(IScoreObjectiveCriteria.totalKillCount);

        if (entityIn instanceof EntityPlayer) {
            triggerAchievement(StatList.playerKillsStat);
            collection.addAll(getWorldScoreboard().getObjectivesFromCriteria(IScoreObjectiveCriteria.playerKillCount));
            collection.addAll(func_175137_e(entityIn));
        } else {
            triggerAchievement(StatList.mobKillsStat);
        }

        for (ScoreObjective scoreobjective : collection) {
            Score score = getWorldScoreboard().getValueFromObjective(getName(), scoreobjective);
            score.func_96648_a();
        }
    }

    private Collection<ScoreObjective> func_175137_e(Entity p_175137_1_) {
        ScorePlayerTeam scoreplayerteam = getWorldScoreboard().getPlayersTeam(getName());

        if (scoreplayerteam != null) {
            int i = scoreplayerteam.getChatFormat().getColorIndex();

            if (i >= 0 && i < IScoreObjectiveCriteria.field_178793_i.length) {
                for (ScoreObjective scoreobjective : getWorldScoreboard().getObjectivesFromCriteria(IScoreObjectiveCriteria.field_178793_i[i])) {
                    Score score = getWorldScoreboard().getValueFromObjective(p_175137_1_.getName(), scoreobjective);
                    score.func_96648_a();
                }
            }
        }

        ScorePlayerTeam scoreplayerteam1 = getWorldScoreboard().getPlayersTeam(p_175137_1_.getName());

        if (scoreplayerteam1 != null) {
            int j = scoreplayerteam1.getChatFormat().getColorIndex();

            if (j >= 0 && j < IScoreObjectiveCriteria.field_178792_h.length) {
                return getWorldScoreboard().getObjectivesFromCriteria(IScoreObjectiveCriteria.field_178792_h[j]);
            }
        }

        return Lists.newArrayList();
    }

    public EntityItem dropOneItem(boolean dropAll) {
        return dropItem(inventory.decrStackSize(inventory.currentItem, dropAll && inventory.getCurrentItem() != null ? inventory.getCurrentItem().stackSize : 1), false, true);
    }

    public EntityItem dropPlayerItemWithRandomChoice(ItemStack itemStackIn, boolean unused) {
        return dropItem(itemStackIn, false, false);
    }

    public EntityItem dropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem) {
        if (droppedItem == null) {
            return null;
        } else if (droppedItem.stackSize == 0) {
            return null;
        } else {
            double d0 = posY - 0.30000001192092896D + (double) getEyeHeight();
            EntityItem entityitem = new EntityItem(worldObj, posX, d0, posZ, droppedItem);
            entityitem.setPickupDelay(40);

            if (traceItem) {
                entityitem.setThrower(getName());
            }

            if (dropAround) {
                float f = rand.nextFloat() * 0.5F;
                float f1 = rand.nextFloat() * (float) Math.PI * 2.0F;
                entityitem.motionX = -MathHelper.sin(f1) * f;
                entityitem.motionZ = MathHelper.cos(f1) * f;
                entityitem.motionY = 0.20000000298023224D;
            } else {
                float f2 = 0.3F;
                entityitem.motionX = -MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI) * f2;
                entityitem.motionZ = MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI) * f2;
                entityitem.motionY = -MathHelper.sin(rotationPitch / 180.0F * (float) Math.PI) * f2 + 0.1F;
                float f3 = rand.nextFloat() * (float) Math.PI * 2.0F;
                f2 = 0.02F * rand.nextFloat();
                entityitem.motionX += Math.cos(f3) * (double) f2;
                entityitem.motionY += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
                entityitem.motionZ += Math.sin(f3) * (double) f2;
            }

            joinEntityItemWithWorld(entityitem);

            if (traceItem) {
                triggerAchievement(StatList.dropStat);
            }

            return entityitem;
        }
    }

    protected void joinEntityItemWithWorld(EntityItem itemIn) {
        worldObj.spawnEntityInWorld(itemIn);
    }

    public float getToolDigEfficiency(Block p_180471_1_) {
        float f = inventory.getStrVsBlock(p_180471_1_);

        if (f > 1.0F) {
            int i = EnchantmentHelper.getEfficiencyModifier(this);
            ItemStack itemstack = inventory.getCurrentItem();

            if (i > 0 && itemstack != null) {
                f += (float) (i * i + 1);
            }
        }

        if (isPotionActive(Potion.digSpeed)) {
            f *= 1.0F + (float) (getActivePotionEffect(Potion.digSpeed).getAmplifier() + 1) * 0.2F;
        }

        if (isPotionActive(Potion.digSlowdown)) {
            float f1 = 1.0F;

            switch (getActivePotionEffect(Potion.digSlowdown).getAmplifier()) {
                case 0:
                    f1 = 0.3F;
                    break;

                case 1:
                    f1 = 0.09F;
                    break;

                case 2:
                    f1 = 0.0027F;
                    break;

                case 3:
                default:
                    f1 = 8.1E-4F;
            }

            f *= f1;
        }

        if (isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(this)) {
            f /= 5.0F;
        }

        if (!onGround) {
            f /= 5.0F;
        }

        return f;
    }

    public boolean canHarvestBlock(Block blockToHarvest) {
        return inventory.canHeldItemHarvest(blockToHarvest);
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        entityUniqueID = getUUID(gameProfile);
        NBTTagList nbttaglist = tagCompund.getTagList("Inventory", 10);
        inventory.readFromNBT(nbttaglist);
        inventory.currentItem = tagCompund.getInteger("SelectedItemSlot");
        sleeping = tagCompund.getBoolean("Sleeping");
        sleepTimer = tagCompund.getShort("SleepTimer");
        experience = tagCompund.getFloat("XpP");
        experienceLevel = tagCompund.getInteger("XpLevel");
        experienceTotal = tagCompund.getInteger("XpTotal");
        xpSeed = tagCompund.getInteger("XpSeed");

        if (xpSeed == 0) {
            xpSeed = rand.nextInt();
        }

        setScore(tagCompund.getInteger("Score"));

        if (sleeping) {
            playerLocation = new BlockPos(this);
            wakeUpPlayer(true, true, false);
        }

        if (tagCompund.hasKey("SpawnX", 99) && tagCompund.hasKey("SpawnY", 99) && tagCompund.hasKey("SpawnZ", 99)) {
            spawnChunk = new BlockPos(tagCompund.getInteger("SpawnX"), tagCompund.getInteger("SpawnY"), tagCompund.getInteger("SpawnZ"));
            spawnForced = tagCompund.getBoolean("SpawnForced");
        }

        foodStats.readNBT(tagCompund);
        capabilities.readCapabilitiesFromNBT(tagCompund);

        if (tagCompund.hasKey("EnderItems", 9)) {
            NBTTagList nbttaglist1 = tagCompund.getTagList("EnderItems", 10);
            theInventoryEnderChest.loadInventoryFromNBT(nbttaglist1);
        }
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setTag("Inventory", inventory.writeToNBT(new NBTTagList()));
        tagCompound.setInteger("SelectedItemSlot", inventory.currentItem);
        tagCompound.setBoolean("Sleeping", sleeping);
        tagCompound.setShort("SleepTimer", (short) sleepTimer);
        tagCompound.setFloat("XpP", experience);
        tagCompound.setInteger("XpLevel", experienceLevel);
        tagCompound.setInteger("XpTotal", experienceTotal);
        tagCompound.setInteger("XpSeed", xpSeed);
        tagCompound.setInteger("Score", getScore());

        if (spawnChunk != null) {
            tagCompound.setInteger("SpawnX", spawnChunk.getX());
            tagCompound.setInteger("SpawnY", spawnChunk.getY());
            tagCompound.setInteger("SpawnZ", spawnChunk.getZ());
            tagCompound.setBoolean("SpawnForced", spawnForced);
        }

        foodStats.writeNBT(tagCompound);
        capabilities.writeCapabilitiesToNBT(tagCompound);
        tagCompound.setTag("EnderItems", theInventoryEnderChest.saveInventoryToNBT());
        ItemStack itemstack = inventory.getCurrentItem();

        if (itemstack != null && itemstack.getItem() != null) {
            tagCompound.setTag("SelectedItem", itemstack.writeToNBT(new NBTTagCompound()));
        }
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isEntityInvulnerable(source)) {
            return false;
        } else if (capabilities.disableDamage && !source.canHarmInCreative()) {
            return false;
        } else {
            entityAge = 0;

            if (getHealth() <= 0.0F) {
                return false;
            } else {
                if (isPlayerSleeping() && !worldObj.isRemote) {
                    wakeUpPlayer(true, true, false);
                }

                if (source.isDifficultyScaled()) {
                    if (worldObj.getDifficulty() == EnumDifficulty.PEACEFUL) {
                        amount = 0.0F;
                    }

                    if (worldObj.getDifficulty() == EnumDifficulty.EASY) {
                        amount = amount / 2.0F + 1.0F;
                    }

                    if (worldObj.getDifficulty() == EnumDifficulty.HARD) {
                        amount = amount * 3.0F / 2.0F;
                    }
                }

                if (amount == 0.0F) {
                    return false;
                } else {
                    Entity entity = source.getEntity();

                    if (entity instanceof EntityArrow && ((EntityArrow) entity).shootingEntity != null) {
                        entity = ((EntityArrow) entity).shootingEntity;
                    }

                    return super.attackEntityFrom(source, amount);
                }
            }
        }
    }

    public boolean canAttackPlayer(EntityPlayer other) {
        Team team = getTeam();
        Team team1 = other.getTeam();
        return team == null || (!team.isSameTeam(team1) || team.getAllowFriendlyFire());
    }

    protected void damageArmor(float p_70675_1_) {
        inventory.damageArmor(p_70675_1_);
    }

    public int getTotalArmorValue() {
        return inventory.getTotalArmorValue();
    }

    public float getArmorVisibility() {
        int i = 0;

        for (ItemStack itemstack : inventory.armorInventory) {
            if (itemstack != null) {
                ++i;
            }
        }

        return (float) i / (float) inventory.armorInventory.length;
    }

    protected void damageEntity(DamageSource damageSrc, float damageAmount) {
        if (!isEntityInvulnerable(damageSrc)) {
            if (!damageSrc.isUnblockable() && isBlocking() && damageAmount > 0.0F) {
                damageAmount = (1.0F + damageAmount) * 0.5F;
            }

            damageAmount = applyArmorCalculations(damageSrc, damageAmount);
            damageAmount = applyPotionDamageCalculations(damageSrc, damageAmount);
            float f = damageAmount;
            damageAmount = Math.max(damageAmount - getAbsorptionAmount(), 0.0F);
            setAbsorptionAmount(getAbsorptionAmount() - (f - damageAmount));

            if (damageAmount != 0.0F) {
                addExhaustion(damageSrc.getHungerDamage());
                float f1 = getHealth();
                setHealth(getHealth() - damageAmount);
                getCombatTracker().trackDamage(damageSrc, f1, damageAmount);

                if (damageAmount < 3.4028235E37F) {
                    addStat(StatList.damageTakenStat, Math.round(damageAmount * 10.0F));
                }
            }
        }
    }

    public void openEditSign(TileEntitySign signTile) {
    }

    public void openEditCommandBlock(CommandBlockLogic cmdBlockLogic) {
    }

    public void displayVillagerTradeGui(IMerchant villager) {
    }

    public void displayGUIChest(IInventory chestInventory) {
    }

    public void displayGUIHorse(EntityHorse horse, IInventory horseInventory) {
    }

    public void displayGui(IInteractionObject guiOwner) {
    }

    public void displayGUIBook(ItemStack bookStack) {
    }

    public boolean interactWith(Entity targetEntity) {
        if (isSpectator()) {
            if (targetEntity instanceof IInventory) {
                displayGUIChest((IInventory) targetEntity);
            }

            return false;
        } else {
            ItemStack itemstack = getCurrentEquippedItem();
            ItemStack itemstack1 = itemstack != null ? itemstack.copy() : null;

            if (!targetEntity.interactFirst(this)) {
                if (itemstack != null && targetEntity instanceof EntityLivingBase) {
                    if (capabilities.isCreativeMode) {
                        itemstack = itemstack1;
                    }

                    if (itemstack.interactWithEntity(this, (EntityLivingBase) targetEntity)) {
                        if (itemstack.stackSize <= 0 && !capabilities.isCreativeMode) {
                            destroyCurrentEquippedItem();
                        }

                        return true;
                    }
                }

                return false;
            } else {
                if (itemstack != null && itemstack == getCurrentEquippedItem()) {
                    if (itemstack.stackSize <= 0 && !capabilities.isCreativeMode) {
                        destroyCurrentEquippedItem();
                    } else if (itemstack.stackSize < itemstack1.stackSize && capabilities.isCreativeMode) {
                        itemstack.stackSize = itemstack1.stackSize;
                    }
                }

                return true;
            }
        }
    }

    public ItemStack getCurrentEquippedItem() {
        return inventory.getCurrentItem();
    }

    public void destroyCurrentEquippedItem() {
        inventory.setInventorySlotContents(inventory.currentItem, null);
    }

    public double getYOffset() {
        return -0.35D;
    }

    public void attackTargetEntityWithCurrentItem(Entity targetEntity) {
        if (targetEntity.canAttackWithItem()) {
            if (!targetEntity.hitByEntity(this)) {
                float f = (float) getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
                int i = 0;
                float f1 = 0.0F;

                if (targetEntity instanceof EntityLivingBase) {
                    f1 = EnchantmentHelper.getModifierForCreature(getHeldItem(), ((EntityLivingBase) targetEntity).getCreatureAttribute());
                } else {
                    f1 = EnchantmentHelper.getModifierForCreature(getHeldItem(), EnumCreatureAttribute.UNDEFINED);
                }

                i = i + EnchantmentHelper.getKnockbackModifier(this);

                if (isSprinting()) {
                    ++i;
                }

                if (f > 0.0F || f1 > 0.0F) {
                    boolean flag = fallDistance > 0.0F && !onGround && !isOnLadder() && !isInWater() && !isPotionActive(Potion.blindness) && ridingEntity == null && targetEntity instanceof EntityLivingBase;

                    if (flag && f > 0.0F) {
                        f *= 1.5F;
                    }

                    f = f + f1;
                    boolean flag1 = false;
                    int j = EnchantmentHelper.getFireAspectModifier(this);

                    if (targetEntity instanceof EntityLivingBase && j > 0 && !targetEntity.isBurning()) {
                        flag1 = true;
                        targetEntity.setFire(1);
                    }

                    double d0 = targetEntity.motionX;
                    double d1 = targetEntity.motionY;
                    double d2 = targetEntity.motionZ;
                    boolean flag2 = targetEntity.attackEntityFrom(DamageSource.causePlayerDamage(this), f);

                    if (flag2) {
                        if (i > 0) {
                            targetEntity.addVelocity(-MathHelper.sin(rotationYaw * (float) Math.PI / 180.0F) * (float) i * 0.5F, 0.1D, MathHelper.cos(rotationYaw * (float) Math.PI / 180.0F) * (float) i * 0.5F);
                            motionX *= 0.6D;
                            motionZ *= 0.6D;
                            setSprinting(false);
                        }

                        if (targetEntity instanceof EntityPlayerMP && targetEntity.velocityChanged) {
                            ((EntityPlayerMP) targetEntity).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(targetEntity));
                            targetEntity.velocityChanged = false;
                            targetEntity.motionX = d0;
                            targetEntity.motionY = d1;
                            targetEntity.motionZ = d2;
                        }

                        if (flag) {
                            onCriticalHit(targetEntity);
                        }

                        if (f1 > 0.0F) {
                            onEnchantmentCritical(targetEntity);
                        }

                        if (f >= 18.0F) {
                            triggerAchievement(AchievementList.overkill);
                        }

                        setLastAttacker(targetEntity);

                        if (targetEntity instanceof EntityLivingBase) {
                            EnchantmentHelper.applyThornEnchantments((EntityLivingBase) targetEntity, this);
                        }

                        EnchantmentHelper.applyArthropodEnchantments(this, targetEntity);
                        ItemStack itemstack = getCurrentEquippedItem();
                        Entity entity = targetEntity;

                        if (targetEntity instanceof EntityDragonPart) {
                            IEntityMultiPart ientitymultipart = ((EntityDragonPart) targetEntity).entityDragonObj;

                            if (ientitymultipart instanceof EntityLivingBase) {
                                entity = (EntityLivingBase) ientitymultipart;
                            }
                        }

                        if (itemstack != null && entity instanceof EntityLivingBase) {
                            itemstack.hitEntity((EntityLivingBase) entity, this);

                            if (itemstack.stackSize <= 0) {
                                destroyCurrentEquippedItem();
                            }
                        }

                        if (targetEntity instanceof EntityLivingBase) {
                            addStat(StatList.damageDealtStat, Math.round(f * 10.0F));

                            if (j > 0) {
                                targetEntity.setFire(j * 4);
                            }
                        }

                        addExhaustion(0.3F);
                    } else if (flag1) {
                        targetEntity.extinguish();
                    }
                }
            }
        }
    }

    public void onCriticalHit(Entity entityHit) {
    }

    public void onEnchantmentCritical(Entity entityHit) {
    }

    public void respawnPlayer() {
    }

    public void setDead() {
        super.setDead();
        inventoryContainer.onContainerClosed(this);

        if (openContainer != null) {
            openContainer.onContainerClosed(this);
        }
    }

    public boolean isEntityInsideOpaqueBlock() {
        return !sleeping && super.isEntityInsideOpaqueBlock();
    }

    public boolean isUser() {
        return false;
    }

    public GameProfile getGameProfile() {
        return gameProfile;
    }

    public EntityPlayer.EnumStatus trySleep(BlockPos bedLocation) {
        if (!worldObj.isRemote) {
            if (isPlayerSleeping() || !isEntityAlive()) {
                return EntityPlayer.EnumStatus.OTHER_PROBLEM;
            }

            if (!worldObj.provider.isSurfaceWorld()) {
                return EntityPlayer.EnumStatus.NOT_POSSIBLE_HERE;
            }

            if (worldObj.isDaytime()) {
                return EntityPlayer.EnumStatus.NOT_POSSIBLE_NOW;
            }

            if (Math.abs(posX - (double) bedLocation.getX()) > 3.0D || Math.abs(posY - (double) bedLocation.getY()) > 2.0D || Math.abs(posZ - (double) bedLocation.getZ()) > 3.0D) {
                return EntityPlayer.EnumStatus.TOO_FAR_AWAY;
            }

            double d0 = 8.0D;
            double d1 = 5.0D;
            List<EntityMob> list = worldObj.getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB((double) bedLocation.getX() - d0, (double) bedLocation.getY() - d1, (double) bedLocation.getZ() - d0, (double) bedLocation.getX() + d0, (double) bedLocation.getY() + d1, (double) bedLocation.getZ() + d0));

            if (!list.isEmpty()) {
                return EntityPlayer.EnumStatus.NOT_SAFE;
            }
        }

        if (isRiding()) {
            mountEntity(null);
        }

        setSize(0.2F, 0.2F);

        if (worldObj.isBlockLoaded(bedLocation)) {
            EnumFacing enumfacing = worldObj.getBlockState(bedLocation).getValue(BlockDirectional.FACING);
            float f = 0.5F;
            float f1 = 0.5F;

            switch (enumfacing) {
                case SOUTH:
                    f1 = 0.9F;
                    break;

                case NORTH:
                    f1 = 0.1F;
                    break;

                case WEST:
                    f = 0.1F;
                    break;

                case EAST:
                    f = 0.9F;
            }

            func_175139_a(enumfacing);
            setPosition((float) bedLocation.getX() + f, (float) bedLocation.getY() + 0.6875F, (float) bedLocation.getZ() + f1);
        } else {
            setPosition((float) bedLocation.getX() + 0.5F, (float) bedLocation.getY() + 0.6875F, (float) bedLocation.getZ() + 0.5F);
        }

        sleeping = true;
        sleepTimer = 0;
        playerLocation = bedLocation;
        motionX = motionZ = motionY = 0.0D;

        if (!worldObj.isRemote) {
            worldObj.updateAllPlayersSleepingFlag();
        }

        return EntityPlayer.EnumStatus.OK;
    }

    private void func_175139_a(EnumFacing p_175139_1_) {
        renderOffsetX = 0.0F;
        renderOffsetZ = 0.0F;

        switch (p_175139_1_) {
            case SOUTH:
                renderOffsetZ = -1.8F;
                break;

            case NORTH:
                renderOffsetZ = 1.8F;
                break;

            case WEST:
                renderOffsetX = 1.8F;
                break;

            case EAST:
                renderOffsetX = -1.8F;
        }
    }

    public void wakeUpPlayer(boolean immediately, boolean updateWorldFlag, boolean setSpawn) {
        setSize(0.6F, 1.8F);
        IBlockState iblockstate = worldObj.getBlockState(playerLocation);

        if (playerLocation != null && iblockstate.getBlock() == Blocks.bed) {
            worldObj.setBlockState(playerLocation, iblockstate.withProperty(BlockBed.OCCUPIED, Boolean.FALSE), 4);
            BlockPos blockpos = BlockBed.getSafeExitLocation(worldObj, playerLocation, 0);

            if (blockpos == null) {
                blockpos = playerLocation.up();
            }

            setPosition((float) blockpos.getX() + 0.5F, (float) blockpos.getY() + 0.1F, (float) blockpos.getZ() + 0.5F);
        }

        sleeping = false;

        if (!worldObj.isRemote && updateWorldFlag) {
            worldObj.updateAllPlayersSleepingFlag();
        }

        sleepTimer = immediately ? 0 : 100;

        if (setSpawn) {
            setSpawnPoint(playerLocation, false);
        }
    }

    private boolean isInBed() {
        return worldObj.getBlockState(playerLocation).getBlock() == Blocks.bed;
    }

    public float getBedOrientationInDegrees() {
        if (playerLocation != null) {
            EnumFacing enumfacing = worldObj.getBlockState(playerLocation).getValue(BlockDirectional.FACING);

            switch (enumfacing) {
                case SOUTH:
                    return 90.0F;

                case NORTH:
                    return 270.0F;

                case WEST:
                    return 0.0F;

                case EAST:
                    return 180.0F;
            }
        }

        return 0.0F;
    }

    public boolean isPlayerSleeping() {
        return sleeping;
    }

    public boolean isPlayerFullyAsleep() {
        return sleeping && sleepTimer >= 100;
    }

    public int getSleepTimer() {
        return sleepTimer;
    }

    public void addChatComponentMessage(IChatComponent chatComponent) {
    }

    public BlockPos getBedLocation() {
        return spawnChunk;
    }

    public boolean isSpawnForced() {
        return spawnForced;
    }

    public void setSpawnPoint(BlockPos pos, boolean forced) {
        if (pos != null) {
            spawnChunk = pos;
            spawnForced = forced;
        } else {
            spawnChunk = null;
            spawnForced = false;
        }
    }

    public void triggerAchievement(StatBase achievementIn) {
        addStat(achievementIn, 1);
    }

    public void addStat(StatBase stat, int amount) {
    }

    public void func_175145_a(StatBase p_175145_1_) {
    }

    public void jump() {
        super.jump();
        triggerAchievement(StatList.jumpStat);

        if (isSprinting()) {
            addExhaustion(0.8F);
        } else {
            addExhaustion(0.2F);
        }
    }

    public void moveEntityWithHeading(float strafe, float forward) {
        double d0 = posX;
        double d1 = posY;
        double d2 = posZ;

        if (capabilities.isFlying && ridingEntity == null) {
            double d3 = motionY;
            float f = jumpMovementFactor;
            jumpMovementFactor = capabilities.getFlySpeed() * (float) (isSprinting() ? 2 : 1);
            super.moveEntityWithHeading(strafe, forward);
            motionY = d3 * 0.6D;
            jumpMovementFactor = f;
        } else {
            super.moveEntityWithHeading(strafe, forward);
        }

        addMovementStat(posX - d0, posY - d1, posZ - d2);
    }

    public float getAIMoveSpeed() {
        return (float) getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();
    }

    public void addMovementStat(double p_71000_1_, double p_71000_3_, double p_71000_5_) {
        if (ridingEntity == null) {
            if (isInsideOfMaterial(Material.water)) {
                int i = Math.round(MathHelper.sqrt_double(p_71000_1_ * p_71000_1_ + p_71000_3_ * p_71000_3_ + p_71000_5_ * p_71000_5_) * 100.0F);

                if (i > 0) {
                    addStat(StatList.distanceDoveStat, i);
                    addExhaustion(0.015F * (float) i * 0.01F);
                }
            } else if (isInWater()) {
                int j = Math.round(MathHelper.sqrt_double(p_71000_1_ * p_71000_1_ + p_71000_5_ * p_71000_5_) * 100.0F);

                if (j > 0) {
                    addStat(StatList.distanceSwumStat, j);
                    addExhaustion(0.015F * (float) j * 0.01F);
                }
            } else if (isOnLadder()) {
                if (p_71000_3_ > 0.0D) {
                    addStat(StatList.distanceClimbedStat, (int) Math.round(p_71000_3_ * 100.0D));
                }
            } else if (onGround) {
                int k = Math.round(MathHelper.sqrt_double(p_71000_1_ * p_71000_1_ + p_71000_5_ * p_71000_5_) * 100.0F);

                if (k > 0) {
                    addStat(StatList.distanceWalkedStat, k);

                    if (isSprinting()) {
                        addStat(StatList.distanceSprintedStat, k);
                        addExhaustion(0.099999994F * (float) k * 0.01F);
                    } else {
                        if (isSneaking()) {
                            addStat(StatList.distanceCrouchedStat, k);
                        }

                        addExhaustion(0.01F * (float) k * 0.01F);
                    }
                }
            } else {
                int l = Math.round(MathHelper.sqrt_double(p_71000_1_ * p_71000_1_ + p_71000_5_ * p_71000_5_) * 100.0F);

                if (l > 25) {
                    addStat(StatList.distanceFlownStat, l);
                }
            }
        }
    }

    private void addMountedMovementStat(double p_71015_1_, double p_71015_3_, double p_71015_5_) {
        if (ridingEntity != null) {
            int i = Math.round(MathHelper.sqrt_double(p_71015_1_ * p_71015_1_ + p_71015_3_ * p_71015_3_ + p_71015_5_ * p_71015_5_) * 100.0F);

            if (i > 0) {
                switch (ridingEntity) {
                    case EntityMinecart entityMinecart -> {
                        addStat(StatList.distanceByMinecartStat, i);

                        if (startMinecartRidingCoordinate == null) {
                            startMinecartRidingCoordinate = new BlockPos(this);
                        } else if (startMinecartRidingCoordinate.distanceSq(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ)) >= 1000000.0D) {
                            triggerAchievement(AchievementList.onARail);
                        }
                    }
                    case EntityBoat entityBoat -> addStat(StatList.distanceByBoatStat, i);
                    case EntityPig entityPig -> addStat(StatList.distanceByPigStat, i);
                    case EntityHorse entityHorse -> addStat(StatList.distanceByHorseStat, i);
                    default -> {
                    }
                }
            }
        }
    }

    public void fall(float distance, float damageMultiplier) {
        if (!capabilities.allowFlying) {
            if (distance >= 2.0F) {
                addStat(StatList.distanceFallenStat, (int) Math.round((double) distance * 100.0D));
            }

            super.fall(distance, damageMultiplier);
        }
    }

    protected void resetHeight() {
        if (!isSpectator()) {
            super.resetHeight();
        }
    }

    protected String getFallSoundString(int damageValue) {
        return damageValue > 4 ? "game.player.hurt.fall.big" : "game.player.hurt.fall.small";
    }

    public void onKillEntity(EntityLivingBase entityLivingIn) {
        if (entityLivingIn instanceof IMob) {
            triggerAchievement(AchievementList.killEnemy);
        }

        EntityList.EntityEggInfo entitylist$entityegginfo = EntityList.entityEggs.get(EntityList.getEntityID(entityLivingIn));

        if (entitylist$entityegginfo != null) {
            triggerAchievement(entitylist$entityegginfo.field_151512_d);
        }
    }

    public void setInWeb() {
        if (!capabilities.isFlying) {
            super.setInWeb();
        }
    }

    public ItemStack getCurrentArmor(int slotIn) {
        return inventory.armorItemInSlot(slotIn);
    }

    public void addExperience(int amount) {
        addScore(amount);
        int i = Integer.MAX_VALUE - experienceTotal;

        if (amount > i) {
            amount = i;
        }

        experience += (float) amount / (float) xpBarCap();

        for (experienceTotal += amount; experience >= 1.0F; experience /= (float) xpBarCap()) {
            experience = (experience - 1.0F) * (float) xpBarCap();
            addExperienceLevel(1);
        }
    }

    public int getXPSeed() {
        return xpSeed;
    }

    public void removeExperienceLevel(int levels) {
        experienceLevel -= levels;

        if (experienceLevel < 0) {
            experienceLevel = 0;
            experience = 0.0F;
            experienceTotal = 0;
        }

        xpSeed = rand.nextInt();
    }

    public void addExperienceLevel(int levels) {
        experienceLevel += levels;

        if (experienceLevel < 0) {
            experienceLevel = 0;
            experience = 0.0F;
            experienceTotal = 0;
        }

        if (levels > 0 && experienceLevel % 5 == 0 && (float) lastXPSound < (float) ticksExisted - 100.0F) {
            float f = experienceLevel > 30 ? 1.0F : (float) experienceLevel / 30.0F;
            worldObj.playSoundAtEntity(this, "random.levelup", f * 0.75F, 1.0F);
            lastXPSound = ticksExisted;
        }
    }

    public int xpBarCap() {
        return experienceLevel >= 30 ? 112 + (experienceLevel - 30) * 9 : (experienceLevel >= 15 ? 37 + (experienceLevel - 15) * 5 : 7 + experienceLevel * 2);
    }

    public void addExhaustion(float p_71020_1_) {
        if (!capabilities.disableDamage) {
            if (!worldObj.isRemote) {
                foodStats.addExhaustion(p_71020_1_);
            }
        }
    }

    public FoodStats getFoodStats() {
        return foodStats;
    }

    public boolean canEat(boolean ignoreHunger) {
        return (ignoreHunger || foodStats.needFood()) && !capabilities.disableDamage;
    }

    public boolean shouldHeal() {
        return getHealth() > 0.0F && getHealth() < getMaxHealth();
    }

    public void setItemInUse(ItemStack stack, int duration) {
        if (stack != itemInUse) {
            itemInUse = stack;
            itemInUseCount = duration;

            if (!worldObj.isRemote) {
                setEating(true);
            }
        }
    }

    public boolean isAllowEdit() {
        return capabilities.allowEdit;
    }

    public boolean canPlayerEdit(BlockPos p_175151_1_, EnumFacing p_175151_2_, ItemStack p_175151_3_) {
        if (capabilities.allowEdit) {
            return true;
        } else if (p_175151_3_ == null) {
            return false;
        } else {
            BlockPos blockpos = p_175151_1_.offset(p_175151_2_.getOpposite());
            Block block = worldObj.getBlockState(blockpos).getBlock();
            return p_175151_3_.canPlaceOn(block) || p_175151_3_.canEditBlocks();
        }
    }

    protected int getExperiencePoints(EntityPlayer player) {
        if (worldObj.getGameRules().getBoolean("keepInventory")) {
            return 0;
        } else {
            int i = experienceLevel * 7;
            return i > 100 ? 100 : i;
        }
    }

    protected boolean isPlayer() {
        return true;
    }

    public boolean getAlwaysRenderNameTagForRender() {
        return true;
    }

    public void clonePlayer(EntityPlayer oldPlayer, boolean respawnFromEnd) {
        if (respawnFromEnd) {
            inventory.copyInventory(oldPlayer.inventory);
            setHealth(oldPlayer.getHealth());
            foodStats = oldPlayer.foodStats;
            experienceLevel = oldPlayer.experienceLevel;
            experienceTotal = oldPlayer.experienceTotal;
            experience = oldPlayer.experience;
            setScore(oldPlayer.getScore());
            lastPortalPos = oldPlayer.lastPortalPos;
            lastPortalVec = oldPlayer.lastPortalVec;
            teleportDirection = oldPlayer.teleportDirection;
        } else if (worldObj.getGameRules().getBoolean("keepInventory")) {
            inventory.copyInventory(oldPlayer.inventory);
            experienceLevel = oldPlayer.experienceLevel;
            experienceTotal = oldPlayer.experienceTotal;
            experience = oldPlayer.experience;
            setScore(oldPlayer.getScore());
        }

        xpSeed = oldPlayer.xpSeed;
        theInventoryEnderChest = oldPlayer.theInventoryEnderChest;
        getDataWatcher().updateObject(10, oldPlayer.getDataWatcher().getWatchableObjectByte(10));
    }

    protected boolean canTriggerWalking() {
        return !capabilities.isFlying;
    }

    public void sendPlayerAbilities() {
    }

    public void setGameType(WorldSettings.GameType gameType) {
    }

    public String getName() {
        return gameProfile.getName();
    }

    public InventoryEnderChest getInventoryEnderChest() {
        return theInventoryEnderChest;
    }

    public ItemStack getEquipmentInSlot(int slotIn) {
        return slotIn == 0 ? inventory.getCurrentItem() : inventory.armorInventory[slotIn - 1];
    }

    public ItemStack getHeldItem() {
        return inventory.getCurrentItem();
    }

    public void setCurrentItemOrArmor(int slotIn, ItemStack stack) {
        inventory.armorInventory[slotIn] = stack;
    }

    public boolean isInvisibleToPlayer(EntityPlayer player) {
        if (!isInvisible()) {
            return false;
        } else if (player.isSpectator()) {
            return false;
        } else {
            Team team = getTeam();
            return team == null || player == null || player.getTeam() != team || !team.getSeeFriendlyInvisiblesEnabled();
        }
    }

    public abstract boolean isSpectator();

    public ItemStack[] getInventory() {
        return inventory.armorInventory;
    }

    public boolean isPushedByWater() {
        return !capabilities.isFlying;
    }

    public Scoreboard getWorldScoreboard() {
        return worldObj.getScoreboard();
    }

    public Team getTeam() {
        return getWorldScoreboard().getPlayersTeam(getName());
    }

    public IChatComponent getDisplayName() {
        IChatComponent ichatcomponent = new ChatComponentText(ScorePlayerTeam.formatPlayerName(getTeam(), getName()));
        ichatcomponent.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + getName() + " "));
        ichatcomponent.getChatStyle().setChatHoverEvent(getHoverEvent());
        ichatcomponent.getChatStyle().setInsertion(getName());
        return ichatcomponent;
    }

    public float getEyeHeight() {
        float f = 1.62F;

        if (isPlayerSleeping()) {
            f = 0.2F;
        }

        if (isSneaking()) {
            f -= 0.08F;
        }

        return f;
    }

    public float getAbsorptionAmount() {
        return getDataWatcher().getWatchableObjectFloat(17);
    }

    public void setAbsorptionAmount(float amount) {
        if (amount < 0.0F) {
            amount = 0.0F;
        }

        getDataWatcher().updateObject(17, amount);
    }

    public boolean canOpen(LockCode code) {
        if (code.isEmpty()) {
            return true;
        } else {
            ItemStack itemstack = getCurrentEquippedItem();
            return itemstack != null && itemstack.hasDisplayName() && itemstack.getDisplayName().equals(code.lock());
        }
    }

    public boolean isWearing(EnumPlayerModelParts p_175148_1_) {
        return (getDataWatcher().getWatchableObjectByte(10) & p_175148_1_.getPartMask()) == p_175148_1_.getPartMask();
    }

    public boolean sendCommandFeedback() {
        return MinecraftServer.getServer().worldServers[0].getGameRules().getBoolean("sendCommandFeedback");
    }

    public boolean replaceItemInInventory(int inventorySlot, ItemStack itemStackIn) {
        if (inventorySlot >= 0 && inventorySlot < inventory.mainInventory.length) {
            inventory.setInventorySlotContents(inventorySlot, itemStackIn);
            return true;
        } else {
            int i = inventorySlot - 100;

            if (i >= 0 && i < inventory.armorInventory.length) {
                int k = i + 1;

                if (itemStackIn != null && itemStackIn.getItem() != null) {
                    if (itemStackIn.getItem() instanceof ItemArmor) {
                        if (EntityLiving.getArmorPosition(itemStackIn) != k) {
                            return false;
                        }
                    } else if (k != 4 || itemStackIn.getItem() != Items.skull && !(itemStackIn.getItem() instanceof ItemBlock)) {
                        return false;
                    }
                }

                inventory.setInventorySlotContents(i + inventory.mainInventory.length, itemStackIn);
                return true;
            } else {
                int j = inventorySlot - 200;

                if (j >= 0 && j < theInventoryEnderChest.getSizeInventory()) {
                    theInventoryEnderChest.setInventorySlotContents(j, itemStackIn);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    public boolean hasReducedDebug() {
        return hasReducedDebug;
    }

    public void setReducedDebug(boolean reducedDebug) {
        hasReducedDebug = reducedDebug;
    }

    public enum EnumChatVisibility {
        FULL(0, "options.chat.visibility.full"),
        SYSTEM(1, "options.chat.visibility.system"),
        HIDDEN(2, "options.chat.visibility.hidden");

        private static final EntityPlayer.EnumChatVisibility[] ID_LOOKUP = new EntityPlayer.EnumChatVisibility[values().length];

        static {
            for (EntityPlayer.EnumChatVisibility entityplayer$enumchatvisibility : values()) {
                ID_LOOKUP[entityplayer$enumchatvisibility.chatVisibility] = entityplayer$enumchatvisibility;
            }
        }

        private final int chatVisibility;
        private final String resourceKey;

        EnumChatVisibility(int id, String resourceKey) {
            chatVisibility = id;
            this.resourceKey = resourceKey;
        }

        public static EntityPlayer.EnumChatVisibility getEnumChatVisibility(int id) {
            return ID_LOOKUP[id % ID_LOOKUP.length];
        }

        public int getChatVisibility() {
            return chatVisibility;
        }

        public String getResourceKey() {
            return resourceKey;
        }
    }

    public enum EnumStatus {
        OK,
        NOT_POSSIBLE_HERE,
        NOT_POSSIBLE_NOW,
        TOO_FAR_AWAY,
        OTHER_PROBLEM,
        NOT_SAFE
    }
}
