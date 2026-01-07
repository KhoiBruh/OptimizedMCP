package net.minecraft.util;

import com.google.gson.*;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;

import java.lang.reflect.Type;

public class ChatStyle {
    private static final ChatStyle rootStyle = new ChatStyle() {
        public EnumChatFormatting getColor() {
            return null;
        }

        public boolean getBold() {
            return false;
        }

        public boolean getItalic() {
            return false;
        }

        public boolean getStrikethrough() {
            return false;
        }

        public boolean getUnderlined() {
            return false;
        }

        public boolean getObfuscated() {
            return false;
        }

        public ClickEvent getChatClickEvent() {
            return null;
        }

        public HoverEvent getChatHoverEvent() {
            return null;
        }

        public String getInsertion() {
            return null;
        }

        public ChatStyle setColor(EnumChatFormatting color) {
            throw new UnsupportedOperationException();
        }

        public ChatStyle setBold(Boolean boldIn) {
            throw new UnsupportedOperationException();
        }

        public ChatStyle setItalic(Boolean italic) {
            throw new UnsupportedOperationException();
        }

        public ChatStyle setStrikethrough(Boolean strikethrough) {
            throw new UnsupportedOperationException();
        }

        public ChatStyle setUnderlined(Boolean underlined) {
            throw new UnsupportedOperationException();
        }

        public ChatStyle setObfuscated(Boolean obfuscated) {
            throw new UnsupportedOperationException();
        }

        public ChatStyle setChatClickEvent(ClickEvent event) {
            throw new UnsupportedOperationException();
        }

        public ChatStyle setChatHoverEvent(HoverEvent event) {
            throw new UnsupportedOperationException();
        }

        public ChatStyle setParentStyle(ChatStyle parent) {
            throw new UnsupportedOperationException();
        }

        public String toString() {
            return "Style.ROOT";
        }

        public ChatStyle createShallowCopy() {
            return this;
        }

        public ChatStyle createDeepCopy() {
            return this;
        }

        public String getFormattingCode() {
            return "";
        }
    };
    private ChatStyle parentStyle;
    private EnumChatFormatting color;
    private Boolean bold;
    private Boolean italic;
    private Boolean underlined;
    private Boolean strikethrough;
    private Boolean obfuscated;
    private ClickEvent chatClickEvent;
    private HoverEvent chatHoverEvent;
    private String insertion;

    public EnumChatFormatting getColor() {
        return color == null ? getParent().getColor() : color;
    }

    public ChatStyle setColor(EnumChatFormatting color) {
        this.color = color;
        return this;
    }

    public boolean getBold() {
        return bold == null ? getParent().getBold() : bold;
    }

    public ChatStyle setBold(Boolean boldIn) {
        bold = boldIn;
        return this;
    }

    public boolean getItalic() {
        return italic == null ? getParent().getItalic() : italic;
    }

    public ChatStyle setItalic(Boolean italic) {
        this.italic = italic;
        return this;
    }

    public boolean getStrikethrough() {
        return strikethrough == null ? getParent().getStrikethrough() : strikethrough;
    }

    public ChatStyle setStrikethrough(Boolean strikethrough) {
        this.strikethrough = strikethrough;
        return this;
    }

    public boolean getUnderlined() {
        return underlined == null ? getParent().getUnderlined() : underlined;
    }

    public ChatStyle setUnderlined(Boolean underlined) {
        this.underlined = underlined;
        return this;
    }

    public boolean getObfuscated() {
        return obfuscated == null ? getParent().getObfuscated() : obfuscated;
    }

    public ChatStyle setObfuscated(Boolean obfuscated) {
        this.obfuscated = obfuscated;
        return this;
    }

    public boolean isEmpty() {
        return bold == null && italic == null && strikethrough == null && underlined == null && obfuscated == null && color == null && chatClickEvent == null && chatHoverEvent == null;
    }

    public ClickEvent getChatClickEvent() {
        return chatClickEvent == null ? getParent().getChatClickEvent() : chatClickEvent;
    }

    public ChatStyle setChatClickEvent(ClickEvent event) {
        chatClickEvent = event;
        return this;
    }

    public HoverEvent getChatHoverEvent() {
        return chatHoverEvent == null ? getParent().getChatHoverEvent() : chatHoverEvent;
    }

    public ChatStyle setChatHoverEvent(HoverEvent event) {
        chatHoverEvent = event;
        return this;
    }

    public String getInsertion() {
        return insertion == null ? getParent().getInsertion() : insertion;
    }

    public ChatStyle setInsertion(String insertion) {
        this.insertion = insertion;
        return this;
    }

    public ChatStyle setParentStyle(ChatStyle parent) {
        parentStyle = parent;
        return this;
    }

    public String getFormattingCode() {
        if (isEmpty()) {
            return parentStyle != null ? parentStyle.getFormattingCode() : "";
        } else {
            StringBuilder stringbuilder = new StringBuilder();

            if (getColor() != null) {
                stringbuilder.append(getColor());
            }

            if (getBold()) {
                stringbuilder.append(EnumChatFormatting.BOLD);
            }

            if (getItalic()) {
                stringbuilder.append(EnumChatFormatting.ITALIC);
            }

            if (getUnderlined()) {
                stringbuilder.append(EnumChatFormatting.UNDERLINE);
            }

            if (getObfuscated()) {
                stringbuilder.append(EnumChatFormatting.OBFUSCATED);
            }

            if (getStrikethrough()) {
                stringbuilder.append(EnumChatFormatting.STRIKETHROUGH);
            }

            return stringbuilder.toString();
        }
    }

    private ChatStyle getParent() {
        return parentStyle == null ? rootStyle : parentStyle;
    }

    public String toString() {
        return "Style{hasParent=" + (parentStyle != null) + ", color=" + color + ", bold=" + bold + ", italic=" + italic + ", underlined=" + underlined + ", obfuscated=" + obfuscated + ", clickEvent=" + getChatClickEvent() + ", hoverEvent=" + getChatHoverEvent() + ", insertion=" + getInsertion() + '}';
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (!(p_equals_1_ instanceof ChatStyle chatstyle)) {
            return false;
        } else {
            boolean flag;
            label0:
            {

                if (getBold() == chatstyle.getBold() && getColor() == chatstyle.getColor() && getItalic() == chatstyle.getItalic() && getObfuscated() == chatstyle.getObfuscated() && getStrikethrough() == chatstyle.getStrikethrough() && getUnderlined() == chatstyle.getUnderlined()) {
                    label85:
                    {
                        if (getChatClickEvent() != null) {
                            if (!getChatClickEvent().equals(chatstyle.getChatClickEvent())) {
                                break label85;
                            }
                        } else if (chatstyle.getChatClickEvent() != null) {
                            break label85;
                        }

                        if (getChatHoverEvent() != null) {
                            if (!getChatHoverEvent().equals(chatstyle.getChatHoverEvent())) {
                                break label85;
                            }
                        } else if (chatstyle.getChatHoverEvent() != null) {
                            break label85;
                        }

                        if (getInsertion() != null) {
                            if (getInsertion().equals(chatstyle.getInsertion())) {
                                break label0;
                            }
                        } else if (chatstyle.getInsertion() == null) {
                            break label0;
                        }
                    }
                }

                flag = false;
                return flag;
            }
            flag = true;
            return flag;
        }
    }

    public int hashCode() {
        int i = color.hashCode();
        i = 31 * i + bold.hashCode();
        i = 31 * i + italic.hashCode();
        i = 31 * i + underlined.hashCode();
        i = 31 * i + strikethrough.hashCode();
        i = 31 * i + obfuscated.hashCode();
        i = 31 * i + chatClickEvent.hashCode();
        i = 31 * i + chatHoverEvent.hashCode();
        i = 31 * i + insertion.hashCode();
        return i;
    }

    public ChatStyle createShallowCopy() {
        ChatStyle chatstyle = new ChatStyle();
        chatstyle.bold = bold;
        chatstyle.italic = italic;
        chatstyle.strikethrough = strikethrough;
        chatstyle.underlined = underlined;
        chatstyle.obfuscated = obfuscated;
        chatstyle.color = color;
        chatstyle.chatClickEvent = chatClickEvent;
        chatstyle.chatHoverEvent = chatHoverEvent;
        chatstyle.parentStyle = parentStyle;
        chatstyle.insertion = insertion;
        return chatstyle;
    }

    public ChatStyle createDeepCopy() {
        ChatStyle chatstyle = new ChatStyle();
        chatstyle.setBold(getBold());
        chatstyle.setItalic(getItalic());
        chatstyle.setStrikethrough(getStrikethrough());
        chatstyle.setUnderlined(getUnderlined());
        chatstyle.setObfuscated(getObfuscated());
        chatstyle.setColor(getColor());
        chatstyle.setChatClickEvent(getChatClickEvent());
        chatstyle.setChatHoverEvent(getChatHoverEvent());
        chatstyle.setInsertion(getInsertion());
        return chatstyle;
    }

    public static class Serializer implements JsonDeserializer<ChatStyle>, JsonSerializer<ChatStyle> {
        public ChatStyle deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
            if (p_deserialize_1_.isJsonObject()) {
                ChatStyle chatstyle = new ChatStyle();
                JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();

                if (jsonobject == null) {
                    return null;
                } else {
                    if (jsonobject.has("bold")) {
                        chatstyle.bold = jsonobject.get("bold").getAsBoolean();
                    }

                    if (jsonobject.has("italic")) {
                        chatstyle.italic = jsonobject.get("italic").getAsBoolean();
                    }

                    if (jsonobject.has("underlined")) {
                        chatstyle.underlined = jsonobject.get("underlined").getAsBoolean();
                    }

                    if (jsonobject.has("strikethrough")) {
                        chatstyle.strikethrough = jsonobject.get("strikethrough").getAsBoolean();
                    }

                    if (jsonobject.has("obfuscated")) {
                        chatstyle.obfuscated = jsonobject.get("obfuscated").getAsBoolean();
                    }

                    if (jsonobject.has("color")) {
                        chatstyle.color = p_deserialize_3_.deserialize(jsonobject.get("color"), EnumChatFormatting.class);
                    }

                    if (jsonobject.has("insertion")) {
                        chatstyle.insertion = jsonobject.get("insertion").getAsString();
                    }

                    if (jsonobject.has("clickEvent")) {
                        JsonObject jsonobject1 = jsonobject.getAsJsonObject("clickEvent");

                        if (jsonobject1 != null) {
                            JsonPrimitive jsonprimitive = jsonobject1.getAsJsonPrimitive("action");
                            ClickEvent.Action clickevent$action = jsonprimitive == null ? null : ClickEvent.Action.getValueByCanonicalName(jsonprimitive.getAsString());
                            JsonPrimitive jsonprimitive1 = jsonobject1.getAsJsonPrimitive("value");
                            String s = jsonprimitive1 == null ? null : jsonprimitive1.getAsString();

                            if (clickevent$action != null && s != null && clickevent$action.shouldAllowInChat()) {
                                chatstyle.chatClickEvent = new ClickEvent(clickevent$action, s);
                            }
                        }
                    }

                    if (jsonobject.has("hoverEvent")) {
                        JsonObject jsonobject2 = jsonobject.getAsJsonObject("hoverEvent");

                        if (jsonobject2 != null) {
                            JsonPrimitive jsonprimitive2 = jsonobject2.getAsJsonPrimitive("action");
                            HoverEvent.Action hoverevent$action = jsonprimitive2 == null ? null : HoverEvent.Action.getValueByCanonicalName(jsonprimitive2.getAsString());
                            IChatComponent ichatcomponent = p_deserialize_3_.deserialize(jsonobject2.get("value"), IChatComponent.class);

                            if (hoverevent$action != null && ichatcomponent != null && hoverevent$action.shouldAllowInChat()) {
                                chatstyle.chatHoverEvent = new HoverEvent(hoverevent$action, ichatcomponent);
                            }
                        }
                    }

                    return chatstyle;
                }
            } else {
                return null;
            }
        }

        public JsonElement serialize(ChatStyle p_serialize_1_, Type p_serialize_2_, JsonSerializationContext p_serialize_3_) {
            if (p_serialize_1_.isEmpty()) {
                return null;
            } else {
                JsonObject jsonobject = new JsonObject();

                if (p_serialize_1_.bold != null) {
                    jsonobject.addProperty("bold", p_serialize_1_.bold);
                }

                if (p_serialize_1_.italic != null) {
                    jsonobject.addProperty("italic", p_serialize_1_.italic);
                }

                if (p_serialize_1_.underlined != null) {
                    jsonobject.addProperty("underlined", p_serialize_1_.underlined);
                }

                if (p_serialize_1_.strikethrough != null) {
                    jsonobject.addProperty("strikethrough", p_serialize_1_.strikethrough);
                }

                if (p_serialize_1_.obfuscated != null) {
                    jsonobject.addProperty("obfuscated", p_serialize_1_.obfuscated);
                }

                if (p_serialize_1_.color != null) {
                    jsonobject.add("color", p_serialize_3_.serialize(p_serialize_1_.color));
                }

                if (p_serialize_1_.insertion != null) {
                    jsonobject.add("insertion", p_serialize_3_.serialize(p_serialize_1_.insertion));
                }

                if (p_serialize_1_.chatClickEvent != null) {
                    JsonObject jsonobject1 = new JsonObject();
                    jsonobject1.addProperty("action", p_serialize_1_.chatClickEvent.action().getCanonicalName());
                    jsonobject1.addProperty("value", p_serialize_1_.chatClickEvent.value());
                    jsonobject.add("clickEvent", jsonobject1);
                }

                if (p_serialize_1_.chatHoverEvent != null) {
                    JsonObject jsonobject2 = new JsonObject();
                    jsonobject2.addProperty("action", p_serialize_1_.chatHoverEvent.action().getCanonicalName());
                    jsonobject2.add("value", p_serialize_3_.serialize(p_serialize_1_.chatHoverEvent.value()));
                    jsonobject.add("hoverEvent", jsonobject2);
                }

                return jsonobject;
            }
        }
    }
}
