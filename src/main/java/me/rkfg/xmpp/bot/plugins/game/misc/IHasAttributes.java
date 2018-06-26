package me.rkfg.xmpp.bot.plugins.game.misc;

import java.util.Optional;

public interface IHasAttributes {

    public <T> void setAttribute(TypedAttribute<T> attr, T value);

    <T> boolean hasAttribute(TypedAttribute<T> attr);

    <T> Optional<T> getAttribute(TypedAttribute<T> attr);

}
