package net.minecraft.entity.ai;

public abstract class EntityAIBase {
    private int mutexBits;

    public abstract boolean shouldExecute();

    public boolean continueExecuting() {
        return shouldExecute();
    }

    public boolean isInterruptible() {
        return true;
    }

    public void startExecuting() {
    }

    public void resetTask() {
    }

    public void updateTask() {
    }

    public int getMutexBits() {
        return mutexBits;
    }

    public void setMutexBits(int mutexBitsIn) {
        mutexBits = mutexBitsIn;
    }
}
