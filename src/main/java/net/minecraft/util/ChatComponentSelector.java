package net.minecraft.util;

public class ChatComponentSelector extends ChatComponentStyle {
    private final String selector;

    public ChatComponentSelector(String selectorIn) {
        selector = selectorIn;
    }

    public String getSelector() {
        return selector;
    }

    public String getUnformattedTextForChat() {
        return selector;
    }

    public ChatComponentSelector createCopy() {
        ChatComponentSelector chatcomponentselector = new ChatComponentSelector(selector);
        chatcomponentselector.setChatStyle(getChatStyle().createShallowCopy());

        for (IChatComponent ichatcomponent : getSiblings()) {
            chatcomponentselector.appendSibling(ichatcomponent.createCopy());
        }

        return chatcomponentselector;
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (!(p_equals_1_ instanceof ChatComponentSelector chatcomponentselector)) {
            return false;
        } else {
            return selector.equals(chatcomponentselector.selector) && super.equals(p_equals_1_);
        }
    }

    public String toString() {
        return "SelectorComponent{pattern='" + selector + '\'' + ", siblings=" + siblings + ", style=" + getChatStyle() + '}';
    }
}
