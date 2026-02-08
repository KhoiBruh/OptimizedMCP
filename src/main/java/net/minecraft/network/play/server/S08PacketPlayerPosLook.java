package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

import java.util.EnumSet;
import java.util.Set;

public class S08PacketPlayerPosLook implements Packet<INetHandlerPlayClient> {
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private Set<Flags> field_179835_f;

    public S08PacketPlayerPosLook() {
    }

    public S08PacketPlayerPosLook(double xIn, double yIn, double zIn, float yawIn, float pitchIn, Set<Flags> p_i45993_9_) {
        x = xIn;
        y = yIn;
        z = zIn;
        yaw = yawIn;
        pitch = pitchIn;
        field_179835_f = p_i45993_9_;
    }

    public void readPacketData(PacketBuffer buf) {
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        yaw = buf.readFloat();
        pitch = buf.readFloat();
        field_179835_f = Flags.func_180053_a(buf.readUnsignedByte());
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        buf.writeByte(Flags.func_180056_a(field_179835_f));
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handlePlayerPosLook(this);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public Set<Flags> func_179834_f() {
        return field_179835_f;
    }

    public enum Flags {
        X(0),
        Y(1),
        Z(2),
        Y_ROT(3),
        X_ROT(4);

        private final int field_180058_f;

        Flags(int p_i45992_3_) {
            field_180058_f = p_i45992_3_;
        }

        public static Set<Flags> func_180053_a(int p_180053_0_) {
            Set<Flags> set = EnumSet.noneOf(Flags.class);

            for (Flags s08packetplayerposlook$enumflags : values()) {
                if (s08packetplayerposlook$enumflags.func_180054_b(p_180053_0_)) {
                    set.add(s08packetplayerposlook$enumflags);
                }
            }

            return set;
        }

        public static int func_180056_a(Set<Flags> p_180056_0_) {
            int i = 0;

            for (Flags s08packetplayerposlook$enumflags : p_180056_0_) {
                i |= s08packetplayerposlook$enumflags.func_180055_a();
            }

            return i;
        }

        private int func_180055_a() {
            return 1 << field_180058_f;
        }

        private boolean func_180054_b(int p_180054_1_) {
            return (p_180054_1_ & func_180055_a()) == func_180055_a();
        }
    }
}
