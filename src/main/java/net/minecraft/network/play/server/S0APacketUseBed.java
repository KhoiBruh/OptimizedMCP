package net.minecraft.network.play.server;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.io.IOException;

public class S0APacketUseBed implements Packet<INetHandlerPlayClient> {
    private int playerID;
    private BlockPos bedPos;

    public S0APacketUseBed() {
    }

    public S0APacketUseBed(EntityPlayer player, BlockPos bedPosIn) {
        playerID = player.getEntityId();
        bedPos = bedPosIn;
    }

    public void readPacketData(PacketBuffer buf) {
        playerID = buf.readVarIntFromBuffer();
        bedPos = buf.readBlockPos();
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeVarIntToBuffer(playerID);
        buf.writeBlockPos(bedPos);
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleUseBed(this);
    }

    public EntityPlayer getPlayer(World worldIn) {
        return (EntityPlayer) worldIn.getEntityByID(playerID);
    }

    public BlockPos getBedPosition() {
        return bedPos;
    }
}
