package net.minecraft.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public abstract class EntityAgeable extends EntityCreature {
    protected int growingAge;
    protected int field_175502_b;
    protected int field_175503_c;
    private float ageWidth = -1.0F;
    private float ageHeight;

    public EntityAgeable(World worldIn) {
        super(worldIn);
    }

    public abstract EntityAgeable createChild(EntityAgeable ageable);

    public boolean interact(EntityPlayer player) {
        ItemStack itemstack = player.inventory.getCurrentItem();

        if (itemstack != null && itemstack.getItem() == Items.spawn_egg) {
            if (!worldObj.isRemote) {
                Class<? extends Entity> oclass = EntityList.getClassFromID(itemstack.getMetadata());

                if (oclass != null && getClass() == oclass) {
                    EntityAgeable entityageable = createChild(this);

                    if (entityageable != null) {
                        entityageable.setGrowingAge(-24000);
                        entityageable.setLocationAndAngles(posX, posY, posZ, 0.0F, 0.0F);
                        worldObj.spawnEntityInWorld(entityageable);

                        if (itemstack.hasDisplayName()) {
                            entityageable.setCustomNameTag(itemstack.getDisplayName());
                        }

                        if (!player.capabilities.isCreativeMode) {
                            --itemstack.stackSize;

                            if (itemstack.stackSize <= 0) {
                                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                            }
                        }
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(12, (byte) 0);
    }

    public int getGrowingAge() {
        return worldObj.isRemote ? dataWatcher.getWatchableObjectByte(12) : growingAge;
    }

    public void setGrowingAge(int age) {
        dataWatcher.updateObject(12, (byte) MathHelper.clamp_int(age, -1, 1));
        growingAge = age;
        setScaleForAge(isChild());
    }

    public void func_175501_a(int p_175501_1_, boolean p_175501_2_) {
        int i = getGrowingAge();
        int j = i;
        i = i + p_175501_1_ * 20;

        if (i > 0) {
            i = 0;

            if (j < 0) {
                onGrowingAdult();
            }
        }

        int k = i - j;
        setGrowingAge(i);

        if (p_175501_2_) {
            field_175502_b += k;

            if (field_175503_c == 0) {
                field_175503_c = 40;
            }
        }

        if (getGrowingAge() == 0) {
            setGrowingAge(field_175502_b);
        }
    }

    public void addGrowth(int growth) {
        func_175501_a(growth, false);
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setInteger("Age", getGrowingAge());
        tagCompound.setInteger("ForcedAge", field_175502_b);
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        setGrowingAge(tagCompund.getInteger("Age"));
        field_175502_b = tagCompund.getInteger("ForcedAge");
    }

    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (worldObj.isRemote) {
            if (field_175503_c > 0) {
                if (field_175503_c % 4 == 0) {
                    worldObj.spawnParticle(ParticleTypes.VILLAGER_HAPPY, posX + (double) (rand.nextFloat() * width * 2.0F) - (double) width, posY + 0.5D + (double) (rand.nextFloat() * height), posZ + (double) (rand.nextFloat() * width * 2.0F) - (double) width, 0.0D, 0.0D, 0.0D);
                }

                --field_175503_c;
            }

            setScaleForAge(isChild());
        } else {
            int i = getGrowingAge();

            if (i < 0) {
                ++i;
                setGrowingAge(i);

                if (i == 0) {
                    onGrowingAdult();
                }
            } else if (i > 0) {
                --i;
                setGrowingAge(i);
            }
        }
    }

    protected void onGrowingAdult() {
    }

    public boolean isChild() {
        return getGrowingAge() < 0;
    }

    public void setScaleForAge(boolean p_98054_1_) {
        setScale(p_98054_1_ ? 0.5F : 1.0F);
    }

    protected final void setSize(float width, float height) {
        boolean flag = ageWidth > 0.0F;
        ageWidth = width;
        ageHeight = height;

        if (!flag) {
            setScale(1.0F);
        }
    }

    protected final void setScale(float scale) {
        super.setSize(ageWidth * scale, ageHeight * scale);
    }
}
