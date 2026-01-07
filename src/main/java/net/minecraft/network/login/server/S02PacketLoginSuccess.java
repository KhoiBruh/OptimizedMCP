package net.minecraft.network.login.server;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.INetHandlerLoginClient;

import java.util.UUID;

public class S02PacketLoginSuccess implements Packet<INetHandlerLoginClient> {
    private GameProfile profile;

    public S02PacketLoginSuccess() {
    }

    public S02PacketLoginSuccess(GameProfile profileIn) {
        profile = profileIn;
    }

    public void readPacketData(PacketBuffer buf) {
        String s = buf.readStringFromBuffer(36);
        String s1 = buf.readStringFromBuffer(16);
        UUID uuid = UUID.fromString(s);
        profile = new GameProfile(uuid, s1);
    }

    public void writePacketData(PacketBuffer buf) {
        UUID uuid = profile.getId();
        buf.writeString(uuid == null ? "" : uuid.toString());
        buf.writeString(profile.getName());
    }

    public void processPacket(INetHandlerLoginClient handler) {
        handler.handleLoginSuccess(this);
    }

    public GameProfile getProfile() {
        return profile;
    }
}
