package net.minecraft.util;

public class ChatComponentText extends ChatComponentStyle {
    private final String text;

    public ChatComponentText(String msg) {
        text = msg;
    }

    public String getChatComponentText_TextValue() {
        return text;
    }

    public String getUnformattedTextForChat() {
        return text;
    }

    public ChatComponentText createCopy() {
        ChatComponentText chatcomponenttext = new ChatComponentText(text);
        chatcomponenttext.setChatStyle(getChatStyle().createShallowCopy());

        for (IChatComponent ichatcomponent : getSiblings()) {
            chatcomponenttext.appendSibling(ichatcomponent.createCopy());
        }

        return chatcomponenttext;
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (!(p_equals_1_ instanceof ChatComponentText chatcomponenttext)) {
            return false;
        } else {
            return text.equals(chatcomponenttext.text) && super.equals(p_equals_1_);
        }
    }

    public String toString() {
        return "TextComponent{text='" + text + '\'' + ", siblings=" + siblings + ", style=" + getChatStyle() + '}';
    }
}
