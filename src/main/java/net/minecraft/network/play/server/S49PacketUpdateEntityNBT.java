package net.minecraft.network.play.server;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.World;

import java.io.IOException;

public class S49PacketUpdateEntityNBT implements Packet<INetHandlerPlayClient> {
    private int entityId;
    private NBTTagCompound tagCompound;

    public S49PacketUpdateEntityNBT() {
    }

    public S49PacketUpdateEntityNBT(int entityIdIn, NBTTagCompound tagCompoundIn) {
        entityId = entityIdIn;
        tagCompound = tagCompoundIn;
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        entityId = buf.readVarIntFromBuffer();
        tagCompound = buf.readNBTTagCompoundFromBuffer();
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(entityId);
        buf.writeNBTTagCompoundToBuffer(tagCompound);
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleEntityNBT(this);
    }

    public NBTTagCompound getTagCompound() {
        return tagCompound;
    }

    public Entity getEntity(World worldIn) {
        return worldIn.getEntityByID(entityId);
    }
}
