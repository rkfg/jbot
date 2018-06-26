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
                        if (result.stream().anyMatch(e -> CancelEvent.CANCEL.equals(e.getType()))) {
                            eiter.remove();
                        }
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
    public Optional<IEffect> findEffect(String name) {
        return effects.stream().filter(e -> e.getName().equals(name)).findFirst();
    }

    @Override
    public boolean hasEffect(String name) {
        return findEffect(name).isPresent();
    }

    @Override
    public void attachEffect(IEffect effect) {
        Optional<IEffect> oldEffect = findEffect(effect.getName());
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
    public void detachEffect(String name) {
        Optional<IEffect> effect = findEffect(name);
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
