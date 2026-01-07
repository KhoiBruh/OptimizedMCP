package net.minecraft.event;

import com.google.common.collect.Maps;

import java.util.Map;

public record ClickEvent(Action action, String value) {

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (p_equals_1_ != null && getClass() == p_equals_1_.getClass()) {
            ClickEvent clickevent = (ClickEvent) p_equals_1_;

            if (action != clickevent.action) {
                return false;
            } else {
                if (value != null) {
                    return value.equals(clickevent.value);
                } else return clickevent.value == null;
            }
        } else {
            return false;
        }
    }

    public String toString() {
        return "ClickEvent{action=" + action + ", value='" + value + '\'' + '}';
    }

    public enum Action {
        OPEN_URL("open_url", true),
        OPEN_FILE("open_file", false),
        RUN_COMMAND("run_command", true),
        TWITCH_USER_INFO("twitch_user_info", false),
        SUGGEST_COMMAND("suggest_command", true),
        CHANGE_PAGE("change_page", true);

        private static final Map<String, Action> nameMapping = Maps.newHashMap();

        static {
            for (Action clickevent$action : values()) {
                nameMapping.put(clickevent$action.getCanonicalName(), clickevent$action);
            }
        }

        private final boolean allowedInChat;
        private final String canonicalName;

        Action(String canonicalNameIn, boolean allowedInChatIn) {
            canonicalName = canonicalNameIn;
            allowedInChat = allowedInChatIn;
        }

        public static Action getValueByCanonicalName(String canonicalNameIn) {
            return nameMapping.get(canonicalNameIn);
        }

        public boolean shouldAllowInChat() {
            return allowedInChat;
        }

        public String getCanonicalName() {
            return canonicalName;
        }
    }
}
