package me.rkfg.xmpp.bot.plugins.game;

import java.util.Collection;
import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.effect.IEffect;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public interface IGameObject extends IGameBase {

    default boolean hasEffect(String type) {
        return findEffect(type).isPresent();
    }

    void enqueueEvent(IEvent event);

    void processEvents();

    Optional<IEffect> findEffect(String type);

    default <T> boolean hasMatchingEffect(String type, TypedAttribute<T> attr, T value) {
        return findEffect(type).flatMap(e -> e.getAttribute(attr)).filter(v -> v.equals(value)).isPresent();
    }

    void enqueueEvents(Collection<? extends IEvent> attachEffectEvents);

    Collection<IEffect> listEffects();

    void log(String message);

    void enqueueEvents(IEvent... events);

}
