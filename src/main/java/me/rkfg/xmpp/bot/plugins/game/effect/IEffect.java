package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.event.CancelEvent;
import me.rkfg.xmpp.bot.plugins.game.event.EffectEvent;
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

    default Optional<String> getParameter(int idx) {
        return getAttribute(EFFECT_PARAMS).map(pl -> {
            if (idx < 0 || idx >= pl.size()) {
                return null;
            }
            return pl.get(idx);
        });
    }

    default String getParameter(int idx, String def) {
        return getParameter(idx).orElse(def);
    }

    default Optional<Integer> getIntParameter(int idx) {
        return getAttribute(EFFECT_PARAMS).map(p -> {
            if (p.size() <= idx) {
                return null;
            }
            try {
                return Integer.valueOf(p.get(idx));
            } catch (NumberFormatException e) {
                return null;
            }
        });
    }

    default Integer getIntParameter(int idx, int def) {
        return getIntParameter(idx).orElse(def);
    }

    default Optional<String> getParameterByKey(String key) {
        return getAttribute(EFFECT_PARAMS_KV).map(kv -> kv.get(key));
    }

    default String getParameterByKey(String key, String def) {
        return getParameterByKey(key).orElse(def);
    }

    default Optional<Integer> getIntParameterByKey(String key) {
        try {
            return getParameterByKey(key).map(Integer::valueOf);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default Integer getIntParameterByKey(String key, int def) {
        return getIntParameterByKey(key).orElse(def);
    }

}
