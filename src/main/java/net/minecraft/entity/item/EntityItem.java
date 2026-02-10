package net.minecraft.entity.item;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityItem extends Entity {
    private static final Logger logger = LogManager.getLogger();
    public float hoverStart;
    private int age;
    private int delayBeforeCanPickup;
    private int health;
    private String thrower;
    private String owner;

    public EntityItem(World worldIn, double x, double y, double z) {
        super(worldIn);
        health = 5;
        hoverStart = (float) (Math.random() * Math.PI * 2.0D);
        setSize(0.25F, 0.25F);
        setPosition(x, y, z);
        rotationYaw = (float) (Math.random() * 360.0D);
        motionX = (float) (Math.random() * 0.20000000298023224D - 0.10000000149011612D);
        motionY = 0.20000000298023224D;
        motionZ = (float) (Math.random() * 0.20000000298023224D - 0.10000000149011612D);
    }

    public EntityItem(World worldIn, double x, double y, double z, ItemStack stack) {
        this(worldIn, x, y, z);
        setEntityItemStack(stack);
    }

    public EntityItem(World worldIn) {
        super(worldIn);
        health = 5;
        hoverStart = (float) (Math.random() * Math.PI * 2.0D);
        setSize(0.25F, 0.25F);
        setEntityItemStack(new ItemStack(Blocks.air, 0));
    }

    protected boolean canTriggerWalking() {
        return false;
    }

    protected void entityInit() {
        getDataWatcher().addObjectByDataType(10, 5);
    }

    public void onUpdate() {
        if (getEntityItem() == null) {
            setDead();
        } else {
            super.onUpdate();

            if (delayBeforeCanPickup > 0 && delayBeforeCanPickup != 32767) {
                --delayBeforeCanPickup;
            }

            prevPosX = posX;
            prevPosY = posY;
            prevPosZ = posZ;
            motionY -= 0.03999999910593033D;
            noClip = pushOutOfBlocks(posX, (getEntityBoundingBox().minY + getEntityBoundingBox().maxY) / 2.0D, posZ);
            moveEntity(motionX, motionY, motionZ);
            boolean flag = (int) prevPosX != (int) posX || (int) prevPosY != (int) posY || (int) prevPosZ != (int) posZ;

            if (flag || ticksExisted % 25 == 0) {
                if (worldObj.getBlockState(new BlockPos(this)).getBlock().getMaterial() == Material.lava) {
                    motionY = 0.20000000298023224D;
                    motionX = (rand.nextFloat() - rand.nextFloat()) * 0.2F;
                    motionZ = (rand.nextFloat() - rand.nextFloat()) * 0.2F;
                    playSound("random.fizz", 0.4F, 2.0F + rand.nextFloat() * 0.4F);
                }

                if (!worldObj.isRemote) {
                    searchForOtherItemsNearby();
                }
            }

            float f = 0.98F;

            if (onGround) {
                f = worldObj.getBlockState(new BlockPos(MathHelper.floor(posX), MathHelper.floor(getEntityBoundingBox().minY) - 1, MathHelper.floor(posZ))).getBlock().slipperiness * 0.98F;
            }

            motionX *= f;
            motionY *= 0.9800000190734863D;
            motionZ *= f;

            if (onGround) {
                motionY *= -0.5D;
            }

            if (age != -32768) {
                ++age;
            }

            handleWaterMovement();

            if (!worldObj.isRemote && age >= 6000) {
                setDead();
            }
        }
    }

    private void searchForOtherItemsNearby() {
        for (EntityItem entityitem : worldObj.getEntitiesWithinAABB(EntityItem.class, getEntityBoundingBox().expand(0.5D, 0.0D, 0.5D))) {
            combineItems(entityitem);
        }
    }

    private boolean combineItems(EntityItem other) {
        if (other == this) {
            return false;
        } else if (other.isEntityAlive() && isEntityAlive()) {
            ItemStack itemstack = getEntityItem();
            ItemStack itemstack1 = other.getEntityItem();

            if (delayBeforeCanPickup != 32767 && other.delayBeforeCanPickup != 32767) {
                if (age != -32768 && other.age != -32768) {
                    if (itemstack1.getItem() != itemstack.getItem()) {
                        return false;
                    } else if (itemstack1.hasTagCompound() ^ itemstack.hasTagCompound()) {
                        return false;
                    } else if (itemstack1.hasTagCompound() && !itemstack1.getTagCompound().equals(itemstack.getTagCompound())) {
                        return false;
                    } else if (itemstack1.getItem() == null) {
                        return false;
                    } else if (itemstack1.getItem().getHasSubtypes() && itemstack1.getMetadata() != itemstack.getMetadata()) {
                        return false;
                    } else if (itemstack1.stackSize < itemstack.stackSize) {
                        return other.combineItems(this);
                    } else if (itemstack1.stackSize + itemstack.stackSize > itemstack1.getMaxStackSize()) {
                        return false;
                    } else {
                        itemstack1.stackSize += itemstack.stackSize;
                        other.delayBeforeCanPickup = Math.max(other.delayBeforeCanPickup, delayBeforeCanPickup);
                        other.age = Math.min(other.age, age);
                        other.setEntityItemStack(itemstack1);
                        setDead();
                        return true;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void setAgeToCreativeDespawnTime() {
        age = 4800;
    }

    public boolean handleWaterMovement() {
        if (worldObj.handleMaterialAcceleration(getEntityBoundingBox(), Material.water, this)) {
            if (!inWater && !firstUpdate) {
                resetHeight();
            }

            inWater = true;
        } else {
            inWater = false;
        }

        return inWater;
    }

    protected void dealFireDamage(int amount) {
        attackEntityFrom(DamageSource.inFire, (float) amount);
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isEntityInvulnerable(source)) {
            return false;
        } else if (getEntityItem() != null && getEntityItem().getItem() == Items.nether_star && source.isExplosion()) {
            return false;
        } else {
            setBeenAttacked();
            health = (int) ((float) health - amount);

            if (health <= 0) {
                setDead();
            }

            return false;
        }
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        tagCompound.setShort("Health", (byte) health);
        tagCompound.setShort("Age", (short) age);
        tagCompound.setShort("PickupDelay", (short) delayBeforeCanPickup);

        if (thrower != null) {
            tagCompound.setString("Thrower", thrower);
        }

        if (owner != null) {
            tagCompound.setString("Owner", owner);
        }

        if (getEntityItem() != null) {
            tagCompound.setTag("Item", getEntityItem().writeToNBT(new NBTTagCompound()));
        }
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        health = tagCompund.getShort("Health") & 255;
        age = tagCompund.getShort("Age");

        if (tagCompund.hasKey("PickupDelay")) {
            delayBeforeCanPickup = tagCompund.getShort("PickupDelay");
        }

        if (tagCompund.hasKey("Owner")) {
            owner = tagCompund.getString("Owner");
        }

        if (tagCompund.hasKey("Thrower")) {
            thrower = tagCompund.getString("Thrower");
        }

        NBTTagCompound nbttagcompound = tagCompund.getCompoundTag("Item");
        setEntityItemStack(ItemStack.loadItemStackFromNBT(nbttagcompound));

        if (getEntityItem() == null) {
            setDead();
        }
    }

    public void onCollideWithPlayer(EntityPlayer entityIn) {
        if (!worldObj.isRemote) {
            ItemStack itemstack = getEntityItem();
            int i = itemstack.stackSize;

            if (delayBeforeCanPickup == 0 && (owner == null || 6000 - age <= 200 || owner.equals(entityIn.getName())) && entityIn.inventory.addItemStackToInventory(itemstack)) {
                if (itemstack.getItem() == Item.getItemFromBlock(Blocks.log)) {
                    entityIn.triggerAchievement(AchievementList.mineWood);
                }

                if (itemstack.getItem() == Item.getItemFromBlock(Blocks.log2)) {
                    entityIn.triggerAchievement(AchievementList.mineWood);
                }

                if (itemstack.getItem() == Items.leather) {
                    entityIn.triggerAchievement(AchievementList.killCow);
                }

                if (itemstack.getItem() == Items.diamond) {
                    entityIn.triggerAchievement(AchievementList.diamonds);
                }

                if (itemstack.getItem() == Items.blaze_rod) {
                    entityIn.triggerAchievement(AchievementList.blazeRod);
                }

                if (itemstack.getItem() == Items.diamond && thrower != null) {
                    EntityPlayer entityplayer = worldObj.getPlayerEntityByName(thrower);

                    if (entityplayer != null && entityplayer != entityIn) {
                        entityplayer.triggerAchievement(AchievementList.diamondsToYou);
                    }
                }

                if (!isSilent()) {
                    worldObj.playSoundAtEntity(entityIn, "random.pop", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                }

                entityIn.onItemPickup(this, i);

                if (itemstack.stackSize <= 0) {
                    setDead();
                }
            }
        }
    }

    public String getName() {
        return hasCustomName() ? getCustomNameTag() : StatCollector.translateToLocal("item." + getEntityItem().getUnlocalizedName());
    }

    public boolean canAttackWithItem() {
        return false;
    }

    public void travelToDimension(int dimensionId) {
        super.travelToDimension(dimensionId);

        if (!worldObj.isRemote) {
            searchForOtherItemsNearby();
        }
    }

    public ItemStack getEntityItem() {
        ItemStack itemstack = getDataWatcher().getWatchableObjectItemStack(10);

        if (itemstack == null) {
            if (worldObj != null) {
                logger.error("Item entity {} has no item?!", getEntityId());
            }

            return new ItemStack(Blocks.stone);
        } else {
            return itemstack;
        }
    }

    public void setEntityItemStack(ItemStack stack) {
        getDataWatcher().updateObject(10, stack);
        getDataWatcher().setObjectWatched(10);
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getThrower() {
        return thrower;
    }

    public void setThrower(String thrower) {
        this.thrower = thrower;
    }

    public int getAge() {
        return age;
    }

    public void setDefaultPickupDelay() {
        delayBeforeCanPickup = 10;
    }

    public void setNoPickupDelay() {
        delayBeforeCanPickup = 0;
    }

    public void setInfinitePickupDelay() {
        delayBeforeCanPickup = 32767;
    }

    public void setPickupDelay(int ticks) {
        delayBeforeCanPickup = ticks;
    }

    public boolean cannotPickup() {
        return delayBeforeCanPickup > 0;
    }

    public void setNoDespawn() {
        age = -6000;
    }

    public void func_174870_v() {
        setInfinitePickupDelay();
        age = 5999;
    }
}
