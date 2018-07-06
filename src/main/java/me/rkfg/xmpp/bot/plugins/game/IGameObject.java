package me.rkfg.xmpp.bot.plugins.game;

import java.util.Collection;
import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.effect.IEffect;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasDescription;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public interface IGameObject extends IGameBase, IHasDescription {

    default boolean hasEffect(String type) {
        return findEffect(type).isPresent();
    }

    boolean enqueueEvent(IEvent event);

    Optional<IEffect> findEffect(String type);

    default <T> boolean hasMatchingEffect(String type, TypedAttribute<T> attr, T value) {
        return findEffect(type).flatMap(e -> e.getAttribute(attr)).filter(v -> v.equals(value)).isPresent();
    }

    void enqueueEvents(Collection<IEvent> events);

    Collection<IEffect> listEffects();

    void log(String message);

    default void log(String message, Object... args) {
        log(String.format(message, args));
    }

    void enqueueEvents(IEvent... events);

    <T extends IGameObject> Optional<T> as(TypedAttribute<T> type);

    void enqueueAttachEffect(IEffect effect);

    void enqueueDetachEffect(String effectType);

    default void enqueueToggleEffect(IEffect effect) {
        final String type = effect.getType();
        if (hasEffect(type)) {
            enqueueDetachEffect(type);
        } else {
            enqueueAttachEffect(effect);
        }
    }

}
