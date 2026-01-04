package net.minecraft.event;

import com.google.common.collect.Maps;
import net.minecraft.util.IChatComponent;

import java.util.Map;

public record HoverEvent(Action action, IChatComponent value) {

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
            HoverEvent hoverevent = (HoverEvent) p_equals_1_;

            if (this.action != hoverevent.action) {
                return false;
            } else {
                if (this.value != null) {
                    return this.value.equals(hoverevent.value);
                } else return hoverevent.value == null;
            }
        } else {
            return false;
        }
    }

    public String toString() {
        return "HoverEvent{action=" + this.action + ", value='" + this.value + '\'' + '}';
    }

    public enum Action {
        SHOW_TEXT("show_text", true),
        SHOW_ACHIEVEMENT("show_achievement", true),
        SHOW_ITEM("show_item", true),
        SHOW_ENTITY("show_entity", true);

        private static final Map<String, Action> nameMapping = Maps.newHashMap();

        static {
            for (Action hoverevent$action : values()) {
                nameMapping.put(hoverevent$action.getCanonicalName(), hoverevent$action);
            }
        }

        private final boolean allowedInChat;
        private final String canonicalName;

        Action(String canonicalNameIn, boolean allowedInChatIn) {
            this.canonicalName = canonicalNameIn;
            this.allowedInChat = allowedInChatIn;
        }

        public static Action getValueByCanonicalName(String canonicalNameIn) {
            return nameMapping.get(canonicalNameIn);
        }

        public boolean shouldAllowInChat() {
            return this.allowedInChat;
        }

        public String getCanonicalName() {
            return this.canonicalName;
        }
    }
}
