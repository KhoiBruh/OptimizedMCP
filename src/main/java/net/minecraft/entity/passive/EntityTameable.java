package net.minecraft.entity.passive;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

import java.util.UUID;

public abstract class EntityTameable extends EntityAnimal implements IEntityOwnable {
    protected EntityAISit aiSit = new EntityAISit(this);

    public EntityTameable(World worldIn) {
        super(worldIn);
        setupTamedAI();
    }

    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(16, (byte) 0);
        dataWatcher.addObject(17, "");
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);

        if (getOwnerId() == null) {
            tagCompound.setString("OwnerUUID", "");
        } else {
            tagCompound.setString("OwnerUUID", getOwnerId());
        }

        tagCompound.setBoolean("Sitting", isSitting());
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        String s;

        if (tagCompund.hasKey("OwnerUUID", 8)) {
            s = tagCompund.getString("OwnerUUID");
        } else {
            String s1 = tagCompund.getString("Owner");
            s = PreYggdrasilConverter.getStringUUIDFromName(s1);
        }

        if (!s.isEmpty()) {
            setOwnerId(s);
            setTamed(true);
        }

        aiSit.setSitting(tagCompund.getBoolean("Sitting"));
        setSitting(tagCompund.getBoolean("Sitting"));
    }

    protected void playTameEffect(boolean play) {
        EnumParticleTypes enumparticletypes = EnumParticleTypes.HEART;

        if (!play) {
            enumparticletypes = EnumParticleTypes.SMOKE_NORMAL;
        }

        for (int i = 0; i < 7; ++i) {
            double d0 = rand.nextGaussian() * 0.02D;
            double d1 = rand.nextGaussian() * 0.02D;
            double d2 = rand.nextGaussian() * 0.02D;
            worldObj.spawnParticle(enumparticletypes, posX + (double) (rand.nextFloat() * width * 2.0F) - (double) width, posY + 0.5D + (double) (rand.nextFloat() * height), posZ + (double) (rand.nextFloat() * width * 2.0F) - (double) width, d0, d1, d2);
        }
    }

    public void handleStatusUpdate(byte id) {
        if (id == 7) {
            playTameEffect(true);
        } else if (id == 6) {
            playTameEffect(false);
        } else {
            super.handleStatusUpdate(id);
        }
    }

    public boolean isTamed() {
        return (dataWatcher.getWatchableObjectByte(16) & 4) != 0;
    }

    public void setTamed(boolean tamed) {
        byte b0 = dataWatcher.getWatchableObjectByte(16);

        if (tamed) {
            dataWatcher.updateObject(16, (byte) (b0 | 4));
        } else {
            dataWatcher.updateObject(16, (byte) (b0 & -5));
        }

        setupTamedAI();
    }

    protected void setupTamedAI() {
    }

    public boolean isSitting() {
        return (dataWatcher.getWatchableObjectByte(16) & 1) != 0;
    }

    public void setSitting(boolean sitting) {
        byte b0 = dataWatcher.getWatchableObjectByte(16);

        if (sitting) {
            dataWatcher.updateObject(16, (byte) (b0 | 1));
        } else {
            dataWatcher.updateObject(16, (byte) (b0 & -2));
        }
    }

    public String getOwnerId() {
        return dataWatcher.getWatchableObjectString(17);
    }

    public void setOwnerId(String ownerUuid) {
        dataWatcher.updateObject(17, ownerUuid);
    }

    public EntityLivingBase getOwner() {
        try {
            UUID uuid = UUID.fromString(getOwnerId());
            return worldObj.getPlayerEntityByUUID(uuid);
        } catch (IllegalArgumentException var2) {
            return null;
        }
    }

    public boolean isOwner(EntityLivingBase entityIn) {
        return entityIn == getOwner();
    }

    public EntityAISit getAISit() {
        return aiSit;
    }

    public boolean shouldAttackEntity(EntityLivingBase p_142018_1_, EntityLivingBase p_142018_2_) {
        return true;
    }

    public Team getTeam() {
        if (isTamed()) {
            EntityLivingBase entitylivingbase = getOwner();

            if (entitylivingbase != null) {
                return entitylivingbase.getTeam();
            }
        }

        return super.getTeam();
    }

    public boolean isOnSameTeam(EntityLivingBase otherEntity) {
        if (isTamed()) {
            EntityLivingBase entitylivingbase = getOwner();

            if (otherEntity == entitylivingbase) {
                return true;
            }

            if (entitylivingbase != null) {
                return entitylivingbase.isOnSameTeam(otherEntity);
            }
        }

        return super.isOnSameTeam(otherEntity);
    }

    public void onDeath(DamageSource cause) {
        if (!worldObj.isRemote && worldObj.getGameRules().getBoolean("showDeathMessages") && hasCustomName() && getOwner() instanceof EntityPlayerMP) {
            getOwner().addChatMessage(getCombatTracker().getDeathMessage());
        }

        super.onDeath(cause);
    }
}
