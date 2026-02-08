package net.minecraft.network;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.login.server.S00PacketDisconnect;
import net.minecraft.network.login.server.S01PacketEncryptionRequest;
import net.minecraft.network.login.server.S02PacketLoginSuccess;
import net.minecraft.network.login.server.S03PacketEnableCompression;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.network.status.server.S00PacketServerInfo;
import net.minecraft.network.status.server.S01PacketPong;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public enum ConnectionState {
    HANDSHAKING(-1) {
        {
            registerPacket(PacketDirection.SERVERBOUND, C00Handshake.class);
        }
    },
    PLAY(0) {
        {
            registerPacket(PacketDirection.CLIENTBOUND, S00PacketKeepAlive.class);
            registerPacket(PacketDirection.CLIENTBOUND, S01PacketJoinGame.class);
            registerPacket(PacketDirection.CLIENTBOUND, S02PacketChat.class);
            registerPacket(PacketDirection.CLIENTBOUND, S03PacketTimeUpdate.class);
            registerPacket(PacketDirection.CLIENTBOUND, S04PacketEntityEquipment.class);
            registerPacket(PacketDirection.CLIENTBOUND, S05PacketSpawnPosition.class);
            registerPacket(PacketDirection.CLIENTBOUND, S06PacketUpdateHealth.class);
            registerPacket(PacketDirection.CLIENTBOUND, S07PacketRespawn.class);
            registerPacket(PacketDirection.CLIENTBOUND, S08PacketPlayerPosLook.class);
            registerPacket(PacketDirection.CLIENTBOUND, S09PacketHeldItemChange.class);
            registerPacket(PacketDirection.CLIENTBOUND, S0APacketUseBed.class);
            registerPacket(PacketDirection.CLIENTBOUND, S0BPacketAnimation.class);
            registerPacket(PacketDirection.CLIENTBOUND, S0CPacketSpawnPlayer.class);
            registerPacket(PacketDirection.CLIENTBOUND, S0DPacketCollectItem.class);
            registerPacket(PacketDirection.CLIENTBOUND, S0EPacketSpawnObject.class);
            registerPacket(PacketDirection.CLIENTBOUND, S0FPacketSpawnMob.class);
            registerPacket(PacketDirection.CLIENTBOUND, S10PacketSpawnPainting.class);
            registerPacket(PacketDirection.CLIENTBOUND, S11PacketSpawnExperienceOrb.class);
            registerPacket(PacketDirection.CLIENTBOUND, S12PacketEntityVelocity.class);
            registerPacket(PacketDirection.CLIENTBOUND, S13PacketDestroyEntities.class);
            registerPacket(PacketDirection.CLIENTBOUND, S14PacketEntity.class);
            registerPacket(PacketDirection.CLIENTBOUND, S14PacketEntity.S15PacketEntityRelMove.class);
            registerPacket(PacketDirection.CLIENTBOUND, S14PacketEntity.S16PacketEntityLook.class);
            registerPacket(PacketDirection.CLIENTBOUND, S14PacketEntity.S17PacketEntityLookMove.class);
            registerPacket(PacketDirection.CLIENTBOUND, S18PacketEntityTeleport.class);
            registerPacket(PacketDirection.CLIENTBOUND, S19PacketEntityHeadLook.class);
            registerPacket(PacketDirection.CLIENTBOUND, S19PacketEntityStatus.class);
            registerPacket(PacketDirection.CLIENTBOUND, S1BPacketEntityAttach.class);
            registerPacket(PacketDirection.CLIENTBOUND, S1CPacketEntityMetadata.class);
            registerPacket(PacketDirection.CLIENTBOUND, S1DPacketEntityEffect.class);
            registerPacket(PacketDirection.CLIENTBOUND, S1EPacketRemoveEntityEffect.class);
            registerPacket(PacketDirection.CLIENTBOUND, S1FPacketSetExperience.class);
            registerPacket(PacketDirection.CLIENTBOUND, S20PacketEntityProperties.class);
            registerPacket(PacketDirection.CLIENTBOUND, S21PacketChunkData.class);
            registerPacket(PacketDirection.CLIENTBOUND, S22PacketMultiBlockChange.class);
            registerPacket(PacketDirection.CLIENTBOUND, S23PacketBlockChange.class);
            registerPacket(PacketDirection.CLIENTBOUND, S24PacketBlockAction.class);
            registerPacket(PacketDirection.CLIENTBOUND, S25PacketBlockBreakAnim.class);
            registerPacket(PacketDirection.CLIENTBOUND, S26PacketMapChunkBulk.class);
            registerPacket(PacketDirection.CLIENTBOUND, S27PacketExplosion.class);
            registerPacket(PacketDirection.CLIENTBOUND, S28PacketEffect.class);
            registerPacket(PacketDirection.CLIENTBOUND, S29PacketSoundEffect.class);
            registerPacket(PacketDirection.CLIENTBOUND, S2APacketParticles.class);
            registerPacket(PacketDirection.CLIENTBOUND, S2BPacketChangeGameState.class);
            registerPacket(PacketDirection.CLIENTBOUND, S2CPacketSpawnGlobalEntity.class);
            registerPacket(PacketDirection.CLIENTBOUND, S2DPacketOpenWindow.class);
            registerPacket(PacketDirection.CLIENTBOUND, S2EPacketCloseWindow.class);
            registerPacket(PacketDirection.CLIENTBOUND, S2FPacketSetSlot.class);
            registerPacket(PacketDirection.CLIENTBOUND, S30PacketWindowItems.class);
            registerPacket(PacketDirection.CLIENTBOUND, S31PacketWindowProperty.class);
            registerPacket(PacketDirection.CLIENTBOUND, S32PacketConfirmTransaction.class);
            registerPacket(PacketDirection.CLIENTBOUND, S33PacketUpdateSign.class);
            registerPacket(PacketDirection.CLIENTBOUND, S34PacketMaps.class);
            registerPacket(PacketDirection.CLIENTBOUND, S35PacketUpdateTileEntity.class);
            registerPacket(PacketDirection.CLIENTBOUND, S36PacketSignEditorOpen.class);
            registerPacket(PacketDirection.CLIENTBOUND, S37PacketStatistics.class);
            registerPacket(PacketDirection.CLIENTBOUND, S38PacketPlayerListItem.class);
            registerPacket(PacketDirection.CLIENTBOUND, S39PacketPlayerAbilities.class);
            registerPacket(PacketDirection.CLIENTBOUND, S3APacketTabComplete.class);
            registerPacket(PacketDirection.CLIENTBOUND, S3BPacketScoreboardObjective.class);
            registerPacket(PacketDirection.CLIENTBOUND, S3CPacketUpdateScore.class);
            registerPacket(PacketDirection.CLIENTBOUND, S3DPacketDisplayScoreboard.class);
            registerPacket(PacketDirection.CLIENTBOUND, S3EPacketTeams.class);
            registerPacket(PacketDirection.CLIENTBOUND, S3FPacketCustomPayload.class);
            registerPacket(PacketDirection.CLIENTBOUND, S40PacketDisconnect.class);
            registerPacket(PacketDirection.CLIENTBOUND, S41PacketServerDifficulty.class);
            registerPacket(PacketDirection.CLIENTBOUND, S42PacketCombatEvent.class);
            registerPacket(PacketDirection.CLIENTBOUND, S43PacketCamera.class);
            registerPacket(PacketDirection.CLIENTBOUND, S44PacketWorldBorder.class);
            registerPacket(PacketDirection.CLIENTBOUND, S45PacketTitle.class);
            registerPacket(PacketDirection.CLIENTBOUND, S46PacketSetCompressionLevel.class);
            registerPacket(PacketDirection.CLIENTBOUND, S47PacketPlayerListHeaderFooter.class);
            registerPacket(PacketDirection.CLIENTBOUND, S48PacketResourcePackSend.class);
            registerPacket(PacketDirection.CLIENTBOUND, S49PacketUpdateEntityNBT.class);
            registerPacket(PacketDirection.SERVERBOUND, C00PacketKeepAlive.class);
            registerPacket(PacketDirection.SERVERBOUND, C01PacketChatMessage.class);
            registerPacket(PacketDirection.SERVERBOUND, C02PacketUseEntity.class);
            registerPacket(PacketDirection.SERVERBOUND, C03PacketPlayer.class);
            registerPacket(PacketDirection.SERVERBOUND, C03PacketPlayer.C04PacketPlayerPosition.class);
            registerPacket(PacketDirection.SERVERBOUND, C03PacketPlayer.C05PacketPlayerLook.class);
            registerPacket(PacketDirection.SERVERBOUND, C03PacketPlayer.C06PacketPlayerPosLook.class);
            registerPacket(PacketDirection.SERVERBOUND, C07PacketPlayerDigging.class);
            registerPacket(PacketDirection.SERVERBOUND, C08PacketPlayerBlockPlacement.class);
            registerPacket(PacketDirection.SERVERBOUND, C09PacketHeldItemChange.class);
            registerPacket(PacketDirection.SERVERBOUND, C0APacketAnimation.class);
            registerPacket(PacketDirection.SERVERBOUND, C0BPacketEntityAction.class);
            registerPacket(PacketDirection.SERVERBOUND, C0CPacketInput.class);
            registerPacket(PacketDirection.SERVERBOUND, C0DPacketCloseWindow.class);
            registerPacket(PacketDirection.SERVERBOUND, C0EPacketClickWindow.class);
            registerPacket(PacketDirection.SERVERBOUND, C0FPacketConfirmTransaction.class);
            registerPacket(PacketDirection.SERVERBOUND, C10PacketCreativeInventoryAction.class);
            registerPacket(PacketDirection.SERVERBOUND, C11PacketEnchantItem.class);
            registerPacket(PacketDirection.SERVERBOUND, C12PacketUpdateSign.class);
            registerPacket(PacketDirection.SERVERBOUND, C13PacketPlayerAbilities.class);
            registerPacket(PacketDirection.SERVERBOUND, C14PacketTabComplete.class);
            registerPacket(PacketDirection.SERVERBOUND, C15PacketClientSettings.class);
            registerPacket(PacketDirection.SERVERBOUND, C16PacketClientStatus.class);
            registerPacket(PacketDirection.SERVERBOUND, C17PacketCustomPayload.class);
            registerPacket(PacketDirection.SERVERBOUND, C18PacketSpectate.class);
            registerPacket(PacketDirection.SERVERBOUND, C19PacketResourcePackStatus.class);
        }
    },
    STATUS(1) {
        {
            registerPacket(PacketDirection.SERVERBOUND, C00PacketServerQuery.class);
            registerPacket(PacketDirection.CLIENTBOUND, S00PacketServerInfo.class);
            registerPacket(PacketDirection.SERVERBOUND, C01PacketPing.class);
            registerPacket(PacketDirection.CLIENTBOUND, S01PacketPong.class);
        }
    },
    LOGIN(2) {
        {
            registerPacket(PacketDirection.CLIENTBOUND, S00PacketDisconnect.class);
            registerPacket(PacketDirection.CLIENTBOUND, S01PacketEncryptionRequest.class);
            registerPacket(PacketDirection.CLIENTBOUND, S02PacketLoginSuccess.class);
            registerPacket(PacketDirection.CLIENTBOUND, S03PacketEnableCompression.class);
            registerPacket(PacketDirection.SERVERBOUND, C00PacketLoginStart.class);
            registerPacket(PacketDirection.SERVERBOUND, C01PacketEncryptionResponse.class);
        }
    };

    private static final Map<Class<? extends Packet>, ConnectionState> STATES_BY_CLASS = new HashMap<>();
    private static final int field_181136_e = -1;
    private static final int field_181137_f = 2;
    private static final ConnectionState[] STATES_BY_ID = new ConnectionState[field_181137_f - field_181136_e + 1];

    static {
        for (ConnectionState enumconnectionstate : values()) {
            int i = enumconnectionstate.id;

            if (i < field_181136_e || i > field_181137_f) {
                throw new Error("Invalid protocol ID " + i);
            }

            STATES_BY_ID[i - field_181136_e] = enumconnectionstate;

            for (BiMap<Integer, Class<? extends Packet>> integerClassBiMap : enumconnectionstate.directionMaps.values()) {
                for (Class<? extends Packet> oclass : (integerClassBiMap).values()) {
                    if (STATES_BY_CLASS.containsKey(oclass) && STATES_BY_CLASS.get(oclass) != enumconnectionstate) {
                        throw new Error("Packet " + oclass + " is already assigned to protocol " + STATES_BY_CLASS.get(oclass) + " - can't reassign to " + enumconnectionstate);
                    }

                    try {
                        oclass.getDeclaredConstructor().newInstance();
                    } catch (Throwable var10) {
                        throw new Error("Packet " + oclass + " fails instantiation checks! " + oclass);
                    }

                    STATES_BY_CLASS.put(oclass, enumconnectionstate);
                }
            }
        }
    }

    private final int id;
    private final Map<PacketDirection, BiMap<Integer, Class<? extends Packet>>> directionMaps;

    ConnectionState(int protocolId) {
        directionMaps = Maps.newEnumMap(PacketDirection.class);
        id = protocolId;
    }

    public static ConnectionState getById(int stateId) {
        return stateId >= field_181136_e && stateId <= field_181137_f ? STATES_BY_ID[stateId - field_181136_e] : null;
    }

    public static ConnectionState getFromPacket(Packet packetIn) {
        return STATES_BY_CLASS.get(packetIn.getClass());
    }

    protected ConnectionState registerPacket(PacketDirection direction, Class<? extends Packet> packetClass) {
        BiMap<Integer, Class<? extends Packet>> bimap = directionMaps.computeIfAbsent(direction, k -> HashBiMap.create());

        if (bimap.containsValue(packetClass)) {
            String s = direction + " packet " + packetClass + " is already known to ID " + bimap.inverse().get(packetClass);
            LogManager.getLogger().fatal(s);
            throw new IllegalArgumentException(s);
        } else {
            bimap.put(bimap.size(), packetClass);
            return this;
        }
    }

    public Integer getPacketId(PacketDirection direction, Packet packetIn) {
        return (Integer) ((BiMap) directionMaps.get(direction)).inverse().get(packetIn.getClass());
    }

    public Packet getPacket(PacketDirection direction, int packetId) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Class<? extends Packet> oclass = (Class) ((BiMap) directionMaps.get(direction)).get(packetId);
        return oclass == null ? null : oclass.getDeclaredConstructor().newInstance();
    }

    public int getId() {
        return id;
    }
}
