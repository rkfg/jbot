package me.rkfg.xmpp.bot.plugins.game.event;

import me.rkfg.xmpp.bot.plugins.game.misc.IHasAttributes;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasDirection;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasType;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public interface IEvent extends IHasAttributes, IHasType, IHasDirection {

    public static final TypedAttribute<String> COMMENT = TypedAttribute.of("comment");

    public void apply();

    default <T> boolean matchByTypeAttr(String type, TypedAttribute<T> attr, T value) {
        return isOfType(type) && matchAttributeValue(attr, value);
    }

    public void setCancelled();

    public boolean isCancelled();

    default void setComment(String comment) {
        setAttribute(COMMENT, comment);
    }

}
