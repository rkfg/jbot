package me.rkfg.xmpp.bot.plugins.game.misc;

import java.util.Optional;
import java.util.function.Supplier;

import me.rkfg.xmpp.bot.plugins.game.IGameBase;

public interface IHasAttributes extends IGameBase {

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

    default void changeAttribute(TypedAttribute<Integer> attr, int diff) {
        getAttribute(attr).ifPresent(a -> setAttribute(attr, a + diff));
    }

    default void incAttribute(TypedAttribute<Integer> attr) {
        changeAttribute(attr, 1);
    }

    default void decAttribute(TypedAttribute<Integer> attr) {
        changeAttribute(attr, -1);
    }

    default <T> Optional<T> decAttribute(TypedAttribute<Integer> attr, Supplier<T> zeroReached) {
        return getAttribute(attr).map(a -> {
            if (a == 1) {
                setAttribute(attr, 0);
                return zeroReached.get();
            }
            if (a > 1) {
                setAttribute(attr, a - 1);
            }
            return null;
        });
    }
}
