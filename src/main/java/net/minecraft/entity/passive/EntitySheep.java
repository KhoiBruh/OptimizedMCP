package net.minecraft.entity.passive;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Random;

public class EntitySheep extends EntityAnimal {
    private static final Map<DyeColor, float[]> DYE_TO_RGB = Maps.newEnumMap(DyeColor.class);

    static {
        DYE_TO_RGB.put(DyeColor.WHITE, new float[]{1.0F, 1.0F, 1.0F});
        DYE_TO_RGB.put(DyeColor.ORANGE, new float[]{0.85F, 0.5F, 0.2F});
        DYE_TO_RGB.put(DyeColor.MAGENTA, new float[]{0.7F, 0.3F, 0.85F});
        DYE_TO_RGB.put(DyeColor.LIGHT_BLUE, new float[]{0.4F, 0.6F, 0.85F});
        DYE_TO_RGB.put(DyeColor.YELLOW, new float[]{0.9F, 0.9F, 0.2F});
        DYE_TO_RGB.put(DyeColor.LIME, new float[]{0.5F, 0.8F, 0.1F});
        DYE_TO_RGB.put(DyeColor.PINK, new float[]{0.95F, 0.5F, 0.65F});
        DYE_TO_RGB.put(DyeColor.GRAY, new float[]{0.3F, 0.3F, 0.3F});
        DYE_TO_RGB.put(DyeColor.SILVER, new float[]{0.6F, 0.6F, 0.6F});
        DYE_TO_RGB.put(DyeColor.CYAN, new float[]{0.3F, 0.5F, 0.6F});
        DYE_TO_RGB.put(DyeColor.PURPLE, new float[]{0.5F, 0.25F, 0.7F});
        DYE_TO_RGB.put(DyeColor.BLUE, new float[]{0.2F, 0.3F, 0.7F});
        DYE_TO_RGB.put(DyeColor.BROWN, new float[]{0.4F, 0.3F, 0.2F});
        DYE_TO_RGB.put(DyeColor.GREEN, new float[]{0.4F, 0.5F, 0.2F});
        DYE_TO_RGB.put(DyeColor.RED, new float[]{0.6F, 0.2F, 0.2F});
        DYE_TO_RGB.put(DyeColor.BLACK, new float[]{0.1F, 0.1F, 0.1F});
    }

    private final InventoryCrafting inventoryCrafting = new InventoryCrafting(new Container() {
        public boolean canInteractWith(EntityPlayer playerIn) {
            return false;
        }
    }, 2, 1);
    private int sheepTimer;
    private final EntityAIEatGrass entityAIEatGrass = new EntityAIEatGrass(this);

    public EntitySheep(World worldIn) {
        super(worldIn);
        setSize(0.9F, 1.3F);
        ((PathNavigateGround) getNavigator()).setAvoidsWater(true);
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIPanic(this, 1.25D));
        tasks.addTask(2, new EntityAIMate(this, 1.0D));
        tasks.addTask(3, new EntityAITempt(this, 1.1D, Items.wheat, false));
        tasks.addTask(4, new EntityAIFollowParent(this, 1.1D));
        tasks.addTask(5, entityAIEatGrass);
        tasks.addTask(6, new EntityAIWander(this, 1.0D));
        tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        tasks.addTask(8, new EntityAILookIdle(this));
        inventoryCrafting.setInventorySlotContents(0, new ItemStack(Items.dye, 1, 0));
        inventoryCrafting.setInventorySlotContents(1, new ItemStack(Items.dye, 1, 0));
    }

    public static float[] getDyeRgb(DyeColor dyeColor) {
        return DYE_TO_RGB.get(dyeColor);
    }

    public static DyeColor getRandomSheepColor(Random random) {
        int i = random.nextInt(100);
        return i < 5 ? DyeColor.BLACK : (i < 10 ? DyeColor.GRAY : (i < 15 ? DyeColor.SILVER : (i < 18 ? DyeColor.BROWN : (random.nextInt(500) == 0 ? DyeColor.PINK : DyeColor.WHITE))));
    }

    protected void updateAITasks() {
        sheepTimer = entityAIEatGrass.getEatingGrassTimer();
        super.updateAITasks();
    }

    public void onLivingUpdate() {
        if (worldObj.isRemote) {
            sheepTimer = Math.max(0, sheepTimer - 1);
        }

        super.onLivingUpdate();
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(8.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.23000000417232513D);
    }

    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(16, (byte) 0);
    }

    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        if (!getSheared()) {
            entityDropItem(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, getFleeceColor().getMetadata()), 0.0F);
        }

        int i = rand.nextInt(2) + 1 + rand.nextInt(1 + lootingModifier);

        for (int j = 0; j < i; ++j) {
            if (isBurning()) {
                dropItem(Items.cooked_mutton, 1);
            } else {
                dropItem(Items.mutton, 1);
            }
        }
    }

    protected Item getDropItem() {
        return Item.getItemFromBlock(Blocks.wool);
    }

    public void handleStatusUpdate(byte id) {
        if (id == 10) {
            sheepTimer = 40;
        } else {
            super.handleStatusUpdate(id);
        }
    }

    public float getHeadRotationPointY(float p_70894_1_) {
        return sheepTimer <= 0 ? 0.0F : (sheepTimer >= 4 && sheepTimer <= 36 ? 1.0F : (sheepTimer < 4 ? ((float) sheepTimer - p_70894_1_) / 4.0F : -((float) (sheepTimer - 40) - p_70894_1_) / 4.0F));
    }

    public float getHeadRotationAngleX(float p_70890_1_) {
        if (sheepTimer > 4 && sheepTimer <= 36) {
            float f = ((float) (sheepTimer - 4) - p_70890_1_) / 32.0F;
            return ((float) Math.PI / 5F) + ((float) Math.PI * 7F / 100F) * MathHelper.sin(f * 28.7F);
        } else {
            return sheepTimer > 0 ? ((float) Math.PI / 5F) : rotationPitch / (180F / (float) Math.PI);
        }
    }

    public boolean interact(EntityPlayer player) {
        ItemStack itemstack = player.inventory.getCurrentItem();

        if (itemstack != null && itemstack.getItem() == Items.shears && !getSheared() && !isChild()) {
            if (!worldObj.isRemote) {
                setSheared(true);
                int i = 1 + rand.nextInt(3);

                for (int j = 0; j < i; ++j) {
                    EntityItem entityitem = entityDropItem(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, getFleeceColor().getMetadata()), 1.0F);
                    entityitem.motionY += rand.nextFloat() * 0.05F;
                    entityitem.motionX += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
                    entityitem.motionZ += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
                }
            }

            itemstack.damageItem(1, player);
            playSound("mob.sheep.shear", 1.0F, 1.0F);
        }

        return super.interact(player);
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setBoolean("Sheared", getSheared());
        tagCompound.setByte("Color", (byte) getFleeceColor().getMetadata());
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        setSheared(tagCompund.getBoolean("Sheared"));
        setFleeceColor(DyeColor.byMetadata(tagCompund.getByte("Color")));
    }

    protected String getLivingSound() {
        return "mob.sheep.say";
    }

    protected String getHurtSound() {
        return "mob.sheep.say";
    }

    protected String getDeathSound() {
        return "mob.sheep.say";
    }

    protected void playStepSound(BlockPos pos, Block blockIn) {
        playSound("mob.sheep.step", 0.15F, 1.0F);
    }

    public DyeColor getFleeceColor() {
        return DyeColor.byMetadata(dataWatcher.getWatchableObjectByte(16) & 15);
    }

    public void setFleeceColor(DyeColor color) {
        byte b0 = dataWatcher.getWatchableObjectByte(16);
        dataWatcher.updateObject(16, (byte) (b0 & 240 | color.getMetadata() & 15));
    }

    public boolean getSheared() {
        return (dataWatcher.getWatchableObjectByte(16) & 16) != 0;
    }

    public void setSheared(boolean sheared) {
        byte b0 = dataWatcher.getWatchableObjectByte(16);

        if (sheared) {
            dataWatcher.updateObject(16, (byte) (b0 | 16));
        } else {
            dataWatcher.updateObject(16, (byte) (b0 & -17));
        }
    }

    public EntitySheep createChild(EntityAgeable ageable) {
        EntitySheep entitysheep = (EntitySheep) ageable;
        EntitySheep entitysheep1 = new EntitySheep(worldObj);
        entitysheep1.setFleeceColor(getDyeColorMixFromParents(this, entitysheep));
        return entitysheep1;
    }

    public void eatGrassBonus() {
        setSheared(false);

        if (isChild()) {
            addGrowth(60);
        }
    }

    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        setFleeceColor(getRandomSheepColor(worldObj.rand));
        return livingdata;
    }

    private DyeColor getDyeColorMixFromParents(EntityAnimal father, EntityAnimal mother) {
        int i = ((EntitySheep) father).getFleeceColor().getDyeDamage();
        int j = ((EntitySheep) mother).getFleeceColor().getDyeDamage();
        inventoryCrafting.getStackInSlot(0).setItemDamage(i);
        inventoryCrafting.getStackInSlot(1).setItemDamage(j);
        ItemStack itemstack = CraftingManager.getInstance().findMatchingRecipe(inventoryCrafting, father.worldObj);
        int k;

        if (itemstack != null && itemstack.getItem() == Items.dye) {
            k = itemstack.getMetadata();
        } else {
            k = worldObj.rand.nextBoolean() ? i : j;
        }

        return DyeColor.byDyeDamage(k);
    }

    public float getEyeHeight() {
        return 0.95F * height;
    }
}
