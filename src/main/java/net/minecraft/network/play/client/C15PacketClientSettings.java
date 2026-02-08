package net.minecraft.network.play.client;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class C15PacketClientSettings implements Packet<INetHandlerPlayServer> {
    private String lang;
    private int view;
    private EntityPlayer.ChatVisibility chatVisibility;
    private boolean enableColors;
    private int modelPartFlags;

    public C15PacketClientSettings() {
    }

    public C15PacketClientSettings(String langIn, int viewIn, EntityPlayer.ChatVisibility chatVisibilityIn, boolean enableColorsIn, int modelPartFlagsIn) {
        lang = langIn;
        view = viewIn;
        chatVisibility = chatVisibilityIn;
        enableColors = enableColorsIn;
        modelPartFlags = modelPartFlagsIn;
    }

    public void readPacketData(PacketBuffer buf) {
        lang = buf.readStringFromBuffer(7);
        view = buf.readByte();
        chatVisibility = EntityPlayer.ChatVisibility.getEnumChatVisibility(buf.readByte());
        enableColors = buf.readBoolean();
        modelPartFlags = buf.readUnsignedByte();
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeString(lang);
        buf.writeByte(view);
        buf.writeByte(chatVisibility.getChatVisibility());
        buf.writeBoolean(enableColors);
        buf.writeByte(modelPartFlags);
    }

    public void processPacket(INetHandlerPlayServer handler) {
        handler.processClientSettings(this);
    }

    public String getLang() {
        return lang;
    }

    public EntityPlayer.ChatVisibility getChatVisibility() {
        return chatVisibility;
    }

    public boolean isColorsEnabled() {
        return enableColors;
    }

    public int getModelPartFlags() {
        return modelPartFlags;
    }
}
