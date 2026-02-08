package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.Difficulty;

public class S41PacketServerDifficulty implements Packet<INetHandlerPlayClient> {
    private Difficulty difficulty;
    private boolean difficultyLocked;

    public S41PacketServerDifficulty() {
    }

    public S41PacketServerDifficulty(Difficulty difficultyIn, boolean lockedIn) {
        difficulty = difficultyIn;
        difficultyLocked = lockedIn;
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleServerDifficulty(this);
    }

    public void readPacketData(PacketBuffer buf) {
        difficulty = Difficulty.getDifficultyEnum(buf.readUnsignedByte());
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeByte(difficulty.getDifficultyId());
    }

    public boolean isDifficultyLocked() {
        return difficultyLocked;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }
}
