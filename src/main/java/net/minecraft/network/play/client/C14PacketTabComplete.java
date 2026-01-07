package net.minecraft.network.play.client;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.BlockPos;
import org.apache.commons.lang3.StringUtils;

public class C14PacketTabComplete implements Packet<INetHandlerPlayServer> {
    private String message;
    private BlockPos targetBlock;

    public C14PacketTabComplete() {
    }

    public C14PacketTabComplete(String msg) {
        this(msg, null);
    }

    public C14PacketTabComplete(String msg, BlockPos target) {
        message = msg;
        targetBlock = target;
    }

    public void readPacketData(PacketBuffer buf) {
        message = buf.readStringFromBuffer(32767);
        boolean flag = buf.readBoolean();

        if (flag) {
            targetBlock = buf.readBlockPos();
        }
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeString(StringUtils.substring(message, 0, 32767));
        boolean flag = targetBlock != null;
        buf.writeBoolean(flag);

        if (flag) {
            buf.writeBlockPos(targetBlock);
        }
    }

    public void processPacket(INetHandlerPlayServer handler) {
        handler.processTabComplete(this);
    }

    public String getMessage() {
        return message;
    }

    public BlockPos getTargetBlock() {
        return targetBlock;
    }
}
