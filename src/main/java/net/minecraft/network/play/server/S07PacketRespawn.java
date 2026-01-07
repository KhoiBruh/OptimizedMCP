package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;

public class S07PacketRespawn implements Packet<INetHandlerPlayClient> {
    private int dimensionID;
    private EnumDifficulty difficulty;
    private WorldSettings.GameType gameType;
    private WorldType worldType;

    public S07PacketRespawn() {
    }

    public S07PacketRespawn(int dimensionIDIn, EnumDifficulty difficultyIn, WorldType worldTypeIn, WorldSettings.GameType gameTypeIn) {
        dimensionID = dimensionIDIn;
        difficulty = difficultyIn;
        gameType = gameTypeIn;
        worldType = worldTypeIn;
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleRespawn(this);
    }

    public void readPacketData(PacketBuffer buf) {
        dimensionID = buf.readInt();
        difficulty = EnumDifficulty.getDifficultyEnum(buf.readUnsignedByte());
        gameType = WorldSettings.GameType.getByID(buf.readUnsignedByte());
        worldType = WorldType.parseWorldType(buf.readStringFromBuffer(16));

        if (worldType == null) {
            worldType = WorldType.DEFAULT;
        }
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeInt(dimensionID);
        buf.writeByte(difficulty.getDifficultyId());
        buf.writeByte(gameType.getID());
        buf.writeString(worldType.getWorldTypeName());
    }

    public int getDimensionID() {
        return dimensionID;
    }

    public EnumDifficulty getDifficulty() {
        return difficulty;
    }

    public WorldSettings.GameType getGameType() {
        return gameType;
    }

    public WorldType getWorldType() {
        return worldType;
    }
}
