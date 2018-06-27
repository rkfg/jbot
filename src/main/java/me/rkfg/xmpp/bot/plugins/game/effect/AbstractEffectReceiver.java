package me.rkfg.xmpp.bot.plugins.game.effect;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.event.CancelEvent;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;

public abstract class AbstractEffectReceiver implements IGameObject, IAttachDetachEffect {

    private Set<IEvent> incomingEvents = new HashSet<>();
    private Set<IEffect> effects = new HashSet<>();

    private boolean processingEvents = false;

    @Override
    public void enqueueEvent(IEvent event) {
        addEvent(event);
        processEvents();
    }

    private void addEvent(IEvent event) {
        event.setTarget(this);
        incomingEvents.add(event);
    }

    @Override
    public void enqueueEvents(Collection<? extends IEvent> events) {
        events.forEach(this::addEvent);
        processEvents();
    }

    @Override
    public void enqueueEvents(IEvent... events) {
        for (IEvent event : events) {
            addEvent(event);
        }
        processEvents();
    }

    @Override
    public void processEvents() {
        if (processingEvents) { // prevent endless recursion
            return;
        }
        processingEvents = true;
        while (!incomingEvents.isEmpty()) {
            Set<IEvent> processedEvents = new HashSet<>();
            Iterator<IEvent> eiter = incomingEvents.iterator();
            while (eiter.hasNext()) {
                IEvent event = eiter.next();
                for (IEffect effect : effects) {
                    Collection<IEvent> result = effect.processEvent(event);
                    if (result != null) {
                        if (result.stream().anyMatch(e -> CancelEvent.TYPE.equals(e.getType()))) {
                            eiter.remove();
                        }
                        // set ourselves as a target by default as most events are directed to us
                        result.stream().filter(e -> e.getTarget() == null).forEach(e -> e.setTarget(this));
                        processedEvents.addAll(result);
                    }
                }
            }
            processedEvents.addAll(incomingEvents);
            incomingEvents.clear(); // will accept more events from processed ones via attach/detach effects
            for (IEvent event : processedEvents) {
                event.apply();
            }
        }
        processingEvents = false;
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
            effects.remove(oe);
            oe.onDetach();
        });
        effect.setTarget(this);
        effect.onAttach();
        effects.add(effect);
    }

    @Override
    public void detachEffect(String type) {
        Optional<IEffect> effect = findEffect(type);
        effect.ifPresent(e -> {
            effects.remove(e);
            e.onDetach();
        });
    }

    @Override
    public Collection<IEffect> listEffects() {
        return Collections.unmodifiableSet(effects);
    }

}
