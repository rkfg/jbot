package me.rkfg.xmpp.bot.plugins.game.effect;

import java.util.Collection;
import java.util.Collections;

import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasAttributes;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasDescription;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasDirection;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasType;

public interface IEffect extends IHasAttributes, IHasType, IHasDirection, IHasDescription {

    default Collection<IEvent> processEvent(IEvent event) {
        return Collections.emptySet();
    }

    default void onBeforeAttach() {

    }

    default void onAfterAttach() {

    }

    default void onAfterDetach() {

    }

    default void onBeforeDetach() {

    }

    default boolean isReplacementAllowed(IEffect replacement) {
        return true;
    }

}
