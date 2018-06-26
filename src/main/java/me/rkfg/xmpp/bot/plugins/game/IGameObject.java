package me.rkfg.xmpp.bot.plugins.game;

import java.util.Collection;
import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.effect.IEffect;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;

public interface IGameObject extends IGameBase {

    boolean hasEffect(String type);

    void enqueueEvent(IEvent event);

    void processEvents();

    Optional<IEffect> findEffect(String type);

    void enqueueEvents(Collection<? extends IEvent> attachEffectEvents);

    Collection<IEffect> listEffects();

    void log(String message);

    void enqueueEvents(IEvent... events);

}
