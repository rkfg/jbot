package me.rkfg.xmpp.bot.plugins.game.effect;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import me.rkfg.xmpp.bot.plugins.game.event.CancelEvent;
import me.rkfg.xmpp.bot.plugins.game.event.EffectEvent;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasAttributes;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasDirection;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasType;

public interface IEffect extends IHasAttributes, IHasType, IHasDirection {

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

    default boolean isVisible() {
        return true;
    }

    default Collection<IEvent> detachEffect(String effectType) {
        return singleEvent(new EffectEvent(effectType));
    }

    default Collection<IEvent> attachEffect(IEffect effect) {
        return singleEvent(new EffectEvent(effect));
    }

    default Collection<IEvent> singleEvent(IEvent event) {
        return Arrays.asList(event);
    }

    default Collection<IEvent> multipleEvents(IEvent... events) {
        return Arrays.asList(events);
    }

    default Collection<IEvent> noEvent() {
        return Collections.emptyList();
    }

    default Collection<IEvent> cancelEvent() {
        return singleEvent(new CancelEvent());
    }

}
