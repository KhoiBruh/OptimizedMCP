package net.minecraft.network.login.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.INetHandlerLoginClient;
import net.minecraft.util.CryptManager;

import java.io.IOException;
import java.security.PublicKey;

public class S01PacketEncryptionRequest implements Packet<INetHandlerLoginClient> {
    private String hashedServerId;
    private PublicKey publicKey;
    private byte[] verifyToken;

    public S01PacketEncryptionRequest() {
    }

    public S01PacketEncryptionRequest(String serverId, PublicKey key, byte[] verifyToken) {
        hashedServerId = serverId;
        publicKey = key;
        this.verifyToken = verifyToken;
    }

    public void readPacketData(PacketBuffer buf) {
        hashedServerId = buf.readStringFromBuffer(20);
        publicKey = CryptManager.decodePublicKey(buf.readByteArray());
        verifyToken = buf.readByteArray();
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeString(hashedServerId);
        buf.writeByteArray(publicKey.getEncoded());
        buf.writeByteArray(verifyToken);
    }

    public void processPacket(INetHandlerLoginClient handler) {
        handler.handleEncryptionRequest(this);
    }

    public String getServerId() {
        return hashedServerId;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public byte[] getVerifyToken() {
        return verifyToken;
    }
}
