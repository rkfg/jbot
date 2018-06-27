package me.rkfg.xmpp.bot.plugins.game.misc;

import java.util.Optional;

public interface IHasAttributes {

    TypedAttributeMap getAttrs();

    default <T> void setAttribute(TypedAttribute<T> attr, T value) {
        getAttrs().put(attr, value);
    }

    default <T> boolean hasAttribute(TypedAttribute<T> attr) {
        return getAttrs().containsAttr(attr);
    }

    default <T> Optional<T> getAttribute(TypedAttribute<T> attr) {
        return getAttrs().get(attr);
    }

    default <T> boolean matchAttributeValue(TypedAttribute<T> attr, T val) {
        return getAttribute(attr).filter(v -> v.equals(val)).isPresent();
    }
}
