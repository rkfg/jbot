package me.rkfg.xmpp.bot.plugins.game;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

import me.rkfg.xmpp.bot.plugins.game.effect.IEffect;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasDescription;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public interface IGameObject extends IGameBase, IHasDescription {

    default boolean hasEffect(String type) {
        return getEffect(type).isPresent();
    }

    boolean enqueueEvent(IEvent event);

    Optional<IEffect> getEffect(String type);

    default <T> boolean hasMatchingEffect(String type, TypedAttribute<T> attr, T value) {
        return getEffect(type).flatMap(e -> e.getAttribute(attr)).filter(v -> v.equals(value)).isPresent();
    }

    void enqueueEvents(Collection<IEvent> events);

    Collection<IEffect> listEffects();

    void log(String message);

    default void log(String message, Object... args) {
        log(String.format(message, args));
    }

    default void log(String messageType, String[] keys, String[] values) {
        log(messageType, keys, values, null);
    }

    default void log(String messageType, String[] keys, String[] values, Function<String, String> postproc) {
        Optional<String> message = World.THIS.getMessageRepository().getRandomObject(DESC_IDX, messageType);
        if (!message.isPresent()) {
            log("Message of type [%s] not found", messageType);
        }
        message.ifPresent(m -> {
            if (keys.length != values.length) {
                log("Error, keys/values length doesn't match: %d/%d [%s]", keys.length, values.length, m);
                return;
            }
            for (int i = 0; i < keys.length; ++i) {
                m = m.replaceAll(Pattern.quote(keys[i]), values[i]);
            }
            if (postproc != null) {
                m = postproc.apply(m);
            }
            log(m);
        });
    }

    default void flushLogs() {
        // do nothing
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
