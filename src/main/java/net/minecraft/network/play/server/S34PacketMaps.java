package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.Vec4b;
import net.minecraft.world.storage.MapData;

import java.io.IOException;
import java.util.Collection;

public class S34PacketMaps implements Packet<INetHandlerPlayClient> {
    private int mapId;
    private byte mapScale;
    private Vec4b[] mapVisiblePlayersVec4b;
    private int mapMinX;
    private int mapMinY;
    private int mapMaxX;
    private int mapMaxY;
    private byte[] mapDataBytes;

    public S34PacketMaps() {
    }

    public S34PacketMaps(int mapIdIn, byte scale, Collection<Vec4b> visiblePlayers, byte[] colors, int minX, int minY, int maxX, int maxY) {
        mapId = mapIdIn;
        mapScale = scale;
        mapVisiblePlayersVec4b = visiblePlayers.toArray(new Vec4b[0]);
        mapMinX = minX;
        mapMinY = minY;
        mapMaxX = maxX;
        mapMaxY = maxY;
        mapDataBytes = new byte[maxX * maxY];

        for (int i = 0; i < maxX; ++i) {
            for (int j = 0; j < maxY; ++j) {
                mapDataBytes[i + j * maxX] = colors[minX + i + (minY + j) * 128];
            }
        }
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        mapId = buf.readVarIntFromBuffer();
        mapScale = buf.readByte();
        mapVisiblePlayersVec4b = new Vec4b[buf.readVarIntFromBuffer()];

        for (int i = 0; i < mapVisiblePlayersVec4b.length; ++i) {
            short short1 = buf.readByte();
            mapVisiblePlayersVec4b[i] = new Vec4b((byte) (short1 >> 4 & 15), buf.readByte(), buf.readByte(), (byte) (short1 & 15));
        }

        mapMaxX = buf.readUnsignedByte();

        if (mapMaxX > 0) {
            mapMaxY = buf.readUnsignedByte();
            mapMinX = buf.readUnsignedByte();
            mapMinY = buf.readUnsignedByte();
            mapDataBytes = buf.readByteArray();
        }
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(mapId);
        buf.writeByte(mapScale);
        buf.writeVarIntToBuffer(mapVisiblePlayersVec4b.length);

        for (Vec4b vec4b : mapVisiblePlayersVec4b) {
            buf.writeByte((vec4b.func_176110_a() & 15) << 4 | vec4b.func_176111_d() & 15);
            buf.writeByte(vec4b.func_176112_b());
            buf.writeByte(vec4b.func_176113_c());
        }

        buf.writeByte(mapMaxX);

        if (mapMaxX > 0) {
            buf.writeByte(mapMaxY);
            buf.writeByte(mapMinX);
            buf.writeByte(mapMinY);
            buf.writeByteArray(mapDataBytes);
        }
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleMaps(this);
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapdataTo(MapData mapdataIn) {
        mapdataIn.scale = mapScale;
        mapdataIn.mapDecorations.clear();

        for (int i = 0; i < mapVisiblePlayersVec4b.length; ++i) {
            Vec4b vec4b = mapVisiblePlayersVec4b[i];
            mapdataIn.mapDecorations.put("icon-" + i, vec4b);
        }

        for (int j = 0; j < mapMaxX; ++j) {
            for (int k = 0; k < mapMaxY; ++k) {
                mapdataIn.colors[mapMinX + j + (mapMinY + k) * 128] = mapDataBytes[j + k * mapMaxX];
            }
        }
    }
}
