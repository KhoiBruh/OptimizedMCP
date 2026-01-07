package net.minecraft.util;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

public abstract class ChatComponentStyle implements IChatComponent {
    protected List<IChatComponent> siblings = Lists.newArrayList();
    private ChatStyle style;

    public static Iterator<IChatComponent> createDeepCopyIterator(Iterable<IChatComponent> components) {
        Iterator<IChatComponent> iterator = Iterators.concat(Iterators.transform(components.iterator(), p_apply_1_ -> p_apply_1_.iterator()));
        iterator = Iterators.transform(iterator, p_apply_1_ -> {
            IChatComponent ichatcomponent = p_apply_1_.createCopy();
            ichatcomponent.setChatStyle(ichatcomponent.getChatStyle().createDeepCopy());
            return ichatcomponent;
        });
        return iterator;
    }

    public IChatComponent appendSibling(IChatComponent component) {
        component.getChatStyle().setParentStyle(getChatStyle());
        siblings.add(component);
        return this;
    }

    public List<IChatComponent> getSiblings() {
        return siblings;
    }

    public IChatComponent appendText(String text) {
        return appendSibling(new ChatComponentText(text));
    }

    public IChatComponent setChatStyle(ChatStyle style) {
        this.style = style;

        for (IChatComponent ichatcomponent : siblings) {
            ichatcomponent.getChatStyle().setParentStyle(getChatStyle());
        }

        return this;
    }

    public ChatStyle getChatStyle() {
        if (style == null) {
            style = new ChatStyle();

            for (IChatComponent ichatcomponent : siblings) {
                ichatcomponent.getChatStyle().setParentStyle(style);
            }
        }

        return style;
    }

    public Iterator<IChatComponent> iterator() {
        return Iterators.concat(Iterators.<IChatComponent>forArray(this), createDeepCopyIterator(siblings));
    }

    public final String getUnformattedText() {
        StringBuilder stringbuilder = new StringBuilder();

        for (IChatComponent ichatcomponent : this) {
            stringbuilder.append(ichatcomponent.getUnformattedTextForChat());
        }

        return stringbuilder.toString();
    }

    public final String getFormattedText() {
        StringBuilder stringbuilder = new StringBuilder();

        for (IChatComponent ichatcomponent : this) {
            stringbuilder.append(ichatcomponent.getChatStyle().getFormattingCode());
            stringbuilder.append(ichatcomponent.getUnformattedTextForChat());
            stringbuilder.append(EnumChatFormatting.RESET);
        }

        return stringbuilder.toString();
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (!(p_equals_1_ instanceof ChatComponentStyle chatcomponentstyle)) {
            return false;
        } else {
            return siblings.equals(chatcomponentstyle.siblings) && getChatStyle().equals(chatcomponentstyle.getChatStyle());
        }
    }

    public int hashCode() {
        return 31 * style.hashCode() + siblings.hashCode();
    }

    public String toString() {
        return "BaseComponent{style=" + style + ", siblings=" + siblings + '}';
    }
}
