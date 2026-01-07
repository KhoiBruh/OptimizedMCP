package net.minecraft.entity.ai;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

import java.util.List;

public class EntitySenses {
    EntityLiving entityObj;
    List<Entity> seenEntities = Lists.newArrayList();
    List<Entity> unseenEntities = Lists.newArrayList();

    public EntitySenses(EntityLiving entityObjIn) {
        entityObj = entityObjIn;
    }

    public void clearSensingCache() {
        seenEntities.clear();
        unseenEntities.clear();
    }

    public boolean canSee(Entity entityIn) {
        if (seenEntities.contains(entityIn)) {
            return true;
        } else if (unseenEntities.contains(entityIn)) {
            return false;
        } else {
            entityObj.worldObj.theProfiler.startSection("canSee");
            boolean flag = entityObj.canEntityBeSeen(entityIn);
            entityObj.worldObj.theProfiler.endSection();

            if (flag) {
                seenEntities.add(entityIn);
            } else {
                unseenEntities.add(entityIn);
            }

            return flag;
        }
    }
}
