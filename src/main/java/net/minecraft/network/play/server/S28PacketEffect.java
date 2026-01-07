package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.BlockPos;

public class S28PacketEffect implements Packet<INetHandlerPlayClient> {
    private int soundType;
    private BlockPos soundPos;
    private int soundData;
    private boolean serverWide;

    public S28PacketEffect() {
    }

    public S28PacketEffect(int soundTypeIn, BlockPos soundPosIn, int soundDataIn, boolean serverWideIn) {
        soundType = soundTypeIn;
        soundPos = soundPosIn;
        soundData = soundDataIn;
        serverWide = serverWideIn;
    }

    public void readPacketData(PacketBuffer buf) {
        soundType = buf.readInt();
        soundPos = buf.readBlockPos();
        soundData = buf.readInt();
        serverWide = buf.readBoolean();
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeInt(soundType);
        buf.writeBlockPos(soundPos);
        buf.writeInt(soundData);
        buf.writeBoolean(serverWide);
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleEffect(this);
    }

    public boolean isSoundServerwide() {
        return serverWide;
    }

    public int getSoundType() {
        return soundType;
    }

    public int getSoundData() {
        return soundData;
    }

    public BlockPos getSoundPos() {
        return soundPos;
    }
}
