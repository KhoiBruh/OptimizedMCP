package net.minecraft.world;

import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;

public class NextTickListEntry implements Comparable<NextTickListEntry> {
    private static long nextTickEntryID;
    public final BlockPos position;
    private final Block block;
    public long scheduledTime;
    public int priority;
    private final long tickEntryID;

    public NextTickListEntry(BlockPos positionIn, Block blockIn) {
        tickEntryID = nextTickEntryID++;
        position = positionIn;
        block = blockIn;
    }

    public boolean equals(Object p_equals_1_) {
        if (!(p_equals_1_ instanceof NextTickListEntry nextticklistentry)) {
            return false;
        } else {
            return position.equals(nextticklistentry.position) && Block.isEqualTo(block, nextticklistentry.block);
        }
    }

    public int hashCode() {
        return position.hashCode();
    }

    public NextTickListEntry setScheduledTime(long scheduledTimeIn) {
        scheduledTime = scheduledTimeIn;
        return this;
    }

    public void setPriority(int priorityIn) {
        priority = priorityIn;
    }

    public int compareTo(NextTickListEntry p_compareTo_1_) {
        return scheduledTime < p_compareTo_1_.scheduledTime ? -1 : (scheduledTime > p_compareTo_1_.scheduledTime ? 1 : (priority != p_compareTo_1_.priority ? priority - p_compareTo_1_.priority : (tickEntryID < p_compareTo_1_.tickEntryID ? -1 : (tickEntryID > p_compareTo_1_.tickEntryID ? 1 : 0))));
    }

    public String toString() {
        return Block.getIdFromBlock(block) + ": " + position + ", " + scheduledTime + ", " + priority + ", " + tickEntryID;
    }

    public Block getBlock() {
        return block;
    }
}
