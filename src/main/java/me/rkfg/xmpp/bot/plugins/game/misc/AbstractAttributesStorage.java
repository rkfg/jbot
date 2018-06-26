package me.rkfg.xmpp.bot.plugins.game.misc;

import java.util.Optional;

public abstract class AbstractAttributesStorage implements IHasAttributes {
    protected TypedAttributeMap attrs = new TypedAttributeMap();

    @Override
    public <T> void setAttribute(TypedAttribute<T> attr, T value) {
        attrs.put(attr, value);
    }

    @Override
    public <T> boolean hasAttribute(TypedAttribute<T> attr) {
        return attrs.containsAttr(attr);
    }

    @Override
    public <T> Optional<T> getAttribute(TypedAttribute<T> attr) {
        return attrs.get(attr);
    }
}
