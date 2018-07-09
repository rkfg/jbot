package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.event.CancelEvent;
import me.rkfg.xmpp.bot.plugins.game.event.EffectEvent;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;

public abstract class AbstractEffectReceiver implements IGameObject, IAttachDetachEffect {

    private static final int RECURSION_LIMIT = 10;
    private Set<IEffect> effects = new HashSet<>();
    private int recursionCounter = 0;

    @Override
    public boolean enqueueEvent(IEvent event) {
        prepareEvent(event);
        List<IEvent> newEvents = new LinkedList<>();
        newEvents.add(event);
        processEvents(newEvents);
        return !event.isCancelled();
    }

    private void prepareEvent(IEvent event) {
        if (event.getTarget() == null) {
            event.setTarget(this);
        }
        if (event.getSource() == null) {
            event.setSource(this);
        }
    }

    @Override
    public void enqueueEvents(Collection<IEvent> events) {
        events.forEach(this::prepareEvent);
        processEvents(events);
    }

    @Override
    public void enqueueEvents(IEvent... events) {
        List<IEvent> newEvents = new LinkedList<>();
        for (IEvent event : events) {
            newEvents.add(event);
        }
        enqueueEvents(newEvents);
    }

    private void processEvents(Collection<IEvent> incomingEvents) {
        if (++recursionCounter > RECURSION_LIMIT) {
            log("Слишком большая рекурсия событий ({}), обработка прервана. Очередь событий: {}", recursionCounter,
                    incomingEvents.stream().map(IEvent::getType).reduce(commaReducer));
            return;
        }
        HashSet<IEvent> newEvents = new HashSet<>();
        Iterator<IEvent> eiter = incomingEvents.iterator();
        while (eiter.hasNext()) {
            processEffects(newEvents, eiter);
        }
        incomingEvents.addAll(newEvents);
        for (IEvent event : incomingEvents) {
            event.apply();
        }
        --recursionCounter;
    }

    private void processEffects(Set<IEvent> incomingEvents, Iterator<IEvent> eiter) {
        IEvent event = eiter.next();
        Set<IEffect> localEffects = new HashSet<>(effects); // make a copy to prevent concurrent modification on recursion
        for (IEffect effect : localEffects) {
            Collection<IEvent> resultEvents = effect.processEvent(event);
            if (resultEvents != null) {
                if (resultEvents.stream().anyMatch(e -> CancelEvent.TYPE.equals(e.getType())) && !event.isCancelled()) {
                    event.setCancelled();
                    eiter.remove();
                }
                // set ourselves as a target by default as most events are directed to us
                resultEvents.stream().filter(e -> e.getTarget() == null).forEach(e -> e.setTarget(this));
                incomingEvents.addAll(resultEvents);
            }
        }
    }

    @Override
    public Optional<IEffect> findEffect(String type) {
        return effects.stream().filter(e -> e.getType().equals(type)).findFirst();
    }

    @Override
    public void attachEffect(IEffect effect) {
        Optional<IEffect> oldEffect = findEffect(effect.getType());
        if (oldEffect.map(e -> !e.isReplacementAllowed(effect)).orElse(false)) {
            return;
        }
        oldEffect.ifPresent(oe -> {
            oe.onBeforeDetach();
            effects.remove(oe);
            oe.onAfterDetach();
        });
        effect.setTarget(this);
        if (effect.getSource() == null) {
            effect.setSource(this);
        }
        effect.onBeforeAttach();
        effects.add(effect);
        effect.onAfterAttach();
    }

    @Override
    public void detachEffect(String type) {
        Optional<IEffect> effect = findEffect(type);
        effect.ifPresent(e -> {
            e.onBeforeDetach();
            effects.remove(e);
            e.onAfterDetach();
        });
    }

    @Override
    public Collection<IEffect> listEffects() {
        return Collections.unmodifiableSet(effects);
    }

    @Override
    public void enqueueAttachEffect(IEffect effect) {
        enqueueEvent(new EffectEvent(effect));
    }

    @Override
    public void enqueueDetachEffect(String effectType) {
        enqueueEvent(new EffectEvent(effectType));
    }
}
