package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLiving;

public class EntityAIOpenDoor extends EntityAIDoorInteract {
    boolean closeDoor;
    int closeDoorTemporisation;

    public EntityAIOpenDoor(EntityLiving entitylivingIn, boolean shouldClose) {
        super(entitylivingIn);
        theEntity = entitylivingIn;
        closeDoor = shouldClose;
    }

    public boolean continueExecuting() {
        return closeDoor && closeDoorTemporisation > 0 && super.continueExecuting();
    }

    public void startExecuting() {
        closeDoorTemporisation = 20;
        doorBlock.toggleDoor(theEntity.worldObj, doorPosition, true);
    }

    public void resetTask() {
        if (closeDoor) {
            doorBlock.toggleDoor(theEntity.worldObj, doorPosition, false);
        }
    }

    public void updateTask() {
        --closeDoorTemporisation;
        super.updateTask();
    }
}
