package net.minecraft.network.play.server;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.CombatTracker;

import java.io.IOException;

public class S42PacketCombatEvent implements Packet<INetHandlerPlayClient> {
    public S42PacketCombatEvent.Event eventType;
    public int field_179774_b;
    public int field_179775_c;
    public int field_179772_d;
    public String deathMessage;

    public S42PacketCombatEvent() {
    }

    @SuppressWarnings("incomplete-switch")
    public S42PacketCombatEvent(CombatTracker combatTrackerIn, S42PacketCombatEvent.Event combatEventType) {
        eventType = combatEventType;
        EntityLivingBase entitylivingbase = combatTrackerIn.func_94550_c();

        switch (combatEventType) {
            case END_COMBAT:
                field_179772_d = combatTrackerIn.func_180134_f();
                field_179775_c = entitylivingbase == null ? -1 : entitylivingbase.getEntityId();
                break;

            case ENTITY_DIED:
                field_179774_b = combatTrackerIn.getFighter().getEntityId();
                field_179775_c = entitylivingbase == null ? -1 : entitylivingbase.getEntityId();
                deathMessage = combatTrackerIn.getDeathMessage().getUnformattedText();
        }
    }

    public void readPacketData(PacketBuffer buf) {
        eventType = buf.readEnumValue(Event.class);

        if (eventType == S42PacketCombatEvent.Event.END_COMBAT) {
            field_179772_d = buf.readVarIntFromBuffer();
            field_179775_c = buf.readInt();
        } else if (eventType == S42PacketCombatEvent.Event.ENTITY_DIED) {
            field_179774_b = buf.readVarIntFromBuffer();
            field_179775_c = buf.readInt();
            deathMessage = buf.readStringFromBuffer(32767);
        }
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeEnumValue(eventType);

        if (eventType == S42PacketCombatEvent.Event.END_COMBAT) {
            buf.writeVarIntToBuffer(field_179772_d);
            buf.writeInt(field_179775_c);
        } else if (eventType == S42PacketCombatEvent.Event.ENTITY_DIED) {
            buf.writeVarIntToBuffer(field_179774_b);
            buf.writeInt(field_179775_c);
            buf.writeString(deathMessage);
        }
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleCombatEvent(this);
    }

    public enum Event {
        ENTER_COMBAT,
        END_COMBAT,
        ENTITY_DIED
    }
}
