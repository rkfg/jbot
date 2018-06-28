package me.rkfg.xmpp.bot.plugins.game.effect;

import java.util.Collection;
import java.util.Collections;

import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasAttributes;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasDirection;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasType;

public interface IEffect extends IHasAttributes, IHasType, IHasDirection {

    default Collection<IEvent> processEvent(IEvent event) {
        return Collections.emptySet();
    }

    default void onAttach() {

    }

    default void onDetach() {

    }

    String getDescription();

    default boolean isReplacementAllowed(IEffect replacement) {
        return true;
    }

}
