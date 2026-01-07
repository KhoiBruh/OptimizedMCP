package net.minecraft.entity.ai;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class EntityAITradePlayer extends EntityAIBase {
    private final EntityVillager villager;

    public EntityAITradePlayer(EntityVillager villagerIn) {
        villager = villagerIn;
        setMutexBits(5);
    }

    public boolean shouldExecute() {
        if (!villager.isEntityAlive()) {
            return false;
        } else if (villager.isInWater()) {
            return false;
        } else if (!villager.onGround) {
            return false;
        } else if (villager.velocityChanged) {
            return false;
        } else {
            EntityPlayer entityplayer = villager.getCustomer();
            return entityplayer != null && (!(villager.getDistanceSqToEntity(entityplayer) > 16.0D) && entityplayer.openContainer instanceof Container);
        }
    }

    public void startExecuting() {
        villager.getNavigator().clearPathEntity();
    }

    public void resetTask() {
        villager.setCustomer(null);
    }
}
