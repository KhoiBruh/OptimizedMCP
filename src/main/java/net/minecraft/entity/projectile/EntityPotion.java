package net.minecraft.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import java.util.List;

public class EntityPotion extends EntityThrowable {
    private ItemStack potionDamage;

    public EntityPotion(World worldIn) {
        super(worldIn);
    }

    public EntityPotion(World worldIn, EntityLivingBase throwerIn, int meta) {
        this(worldIn, throwerIn, new ItemStack(Items.potionitem, 1, meta));
    }

    public EntityPotion(World worldIn, EntityLivingBase throwerIn, ItemStack potionDamageIn) {
        super(worldIn, throwerIn);
        potionDamage = potionDamageIn;
    }

    public EntityPotion(World worldIn, double x, double y, double z, int p_i1791_8_) {
        this(worldIn, x, y, z, new ItemStack(Items.potionitem, 1, p_i1791_8_));
    }

    public EntityPotion(World worldIn, double x, double y, double z, ItemStack potionDamageIn) {
        super(worldIn, x, y, z);
        potionDamage = potionDamageIn;
    }

    protected float getGravityVelocity() {
        return 0.05F;
    }

    protected float getVelocity() {
        return 0.5F;
    }

    protected float getInaccuracy() {
        return -20.0F;
    }

    public int getPotionDamage() {
        if (potionDamage == null) {
            potionDamage = new ItemStack(Items.potionitem, 1, 0);
        }

        return potionDamage.getMetadata();
    }

    public void setPotionDamage(int potionId) {
        if (potionDamage == null) {
            potionDamage = new ItemStack(Items.potionitem, 1, 0);
        }

        potionDamage.setItemDamage(potionId);
    }

    protected void onImpact(MovingObjectPosition p_70184_1_) {
        if (!worldObj.isRemote) {
            List<PotionEffect> list = Items.potionitem.getEffects(potionDamage);

            if (list != null && !list.isEmpty()) {
                AxisAlignedBB axisalignedbb = getEntityBoundingBox().expand(4.0D, 2.0D, 4.0D);
                List<EntityLivingBase> list1 = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb);

                if (!list1.isEmpty()) {
                    for (EntityLivingBase entitylivingbase : list1) {
                        double d0 = getDistanceSqToEntity(entitylivingbase);

                        if (d0 < 16.0D) {
                            double d1 = 1.0D - Math.sqrt(d0) / 4.0D;

                            if (entitylivingbase == p_70184_1_.entityHit) {
                                d1 = 1.0D;
                            }

                            for (PotionEffect potioneffect : list) {
                                int i = potioneffect.getPotionID();

                                if (Potion.potionTypes[i].isInstant()) {
                                    Potion.potionTypes[i].affectEntity(this, getThrower(), entitylivingbase, potioneffect.getAmplifier(), d1);
                                } else {
                                    int j = (int) (d1 * (double) potioneffect.getDuration() + 0.5D);

                                    if (j > 20) {
                                        entitylivingbase.addPotionEffect(new PotionEffect(i, j, potioneffect.getAmplifier()));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            worldObj.playAuxSFX(2002, new BlockPos(this), getPotionDamage());
            setDead();
        }
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);

        if (tagCompund.hasKey("Potion", 10)) {
            potionDamage = ItemStack.loadItemStackFromNBT(tagCompund.getCompoundTag("Potion"));
        } else {
            setPotionDamage(tagCompund.getInteger("potionValue"));
        }

        if (potionDamage == null) {
            setDead();
        }
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);

        if (potionDamage != null) {
            tagCompound.setTag("Potion", potionDamage.writeToNBT(new NBTTagCompound()));
        }
    }
}
