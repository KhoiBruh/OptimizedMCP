package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.BlockPos;
import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;

public class EntityAIRestrictOpenDoor extends EntityAIBase {
    private final EntityCreature entityObj;
    private VillageDoorInfo frontDoor;

    public EntityAIRestrictOpenDoor(EntityCreature creatureIn) {
        entityObj = creatureIn;

        if (!(creatureIn.getNavigator() instanceof PathNavigateGround)) {
            throw new IllegalArgumentException("Unsupported mob type for RestrictOpenDoorGoal");
        }
    }

    public boolean shouldExecute() {
        if (entityObj.worldObj.isDaytime()) {
            return false;
        } else {
            BlockPos blockpos = new BlockPos(entityObj);
            Village village = entityObj.worldObj.getVillageCollection().getNearestVillage(blockpos, 16);

            if (village == null) {
                return false;
            } else {
                frontDoor = village.getNearestDoor(blockpos);
                return frontDoor != null && (double) frontDoor.getDistanceToInsideBlockSq(blockpos) < 2.25D;
            }
        }
    }

    public boolean continueExecuting() {
        return !entityObj.worldObj.isDaytime() && !frontDoor.getIsDetachedFromVillageFlag() && frontDoor.func_179850_c(new BlockPos(entityObj));
    }

    public void startExecuting() {
        ((PathNavigateGround) entityObj.getNavigator()).setBreakDoors(false);
        ((PathNavigateGround) entityObj.getNavigator()).setEnterDoors(false);
    }

    public void resetTask() {
        ((PathNavigateGround) entityObj.getNavigator()).setBreakDoors(true);
        ((PathNavigateGround) entityObj.getNavigator()).setEnterDoors(true);
        frontDoor = null;
    }

    public void updateTask() {
        frontDoor.incrementDoorOpeningRestrictionCounter();
    }
}
