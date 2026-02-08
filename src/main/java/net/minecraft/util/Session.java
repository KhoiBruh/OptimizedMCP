package net.minecraft.util;

import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record Session(String username, String playerID, String token, String sessionType) {


    public String getSessionID() {
        return "token:" + token + ":" + playerID;
    }

    public GameProfile getProfile() {
        try {
            UUID uuid = UUIDTypeAdapter.fromString(playerID());
            return new GameProfile(uuid, username());
        } catch (IllegalArgumentException var2) {
            return new GameProfile(null, username());
        }
    }

    public enum Type {
        LEGACY("legacy"),
        MOJANG("mojang");

        private static final Map<String, Type> SESSION_TYPES = new HashMap<>();

        static {
            for (Type session$type : values()) {
                SESSION_TYPES.put(session$type.sessionType, session$type);
            }
        }

        private final String sessionType;

        Type(String sessionTypeIn) {
            sessionType = sessionTypeIn;
        }

        public static Type setSessionType(String sessionTypeIn) {
            return SESSION_TYPES.get(sessionTypeIn.toLowerCase());
        }
    }
}
