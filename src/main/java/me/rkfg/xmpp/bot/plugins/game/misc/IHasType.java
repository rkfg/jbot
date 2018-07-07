package me.rkfg.xmpp.bot.plugins.game.misc;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.OBJTYPE;

import me.rkfg.xmpp.bot.plugins.game.IGameBase;

public interface IHasType extends IGameBase, IHasAttributes {

    default String getType() {
        return getAttribute(OBJTYPE).orElse("");
    }
    
    default void setType(String type) {
        setAttribute(OBJTYPE, type);
    }

    default boolean isOfType(String type) {
        return getType().equals(type);
    }
}
