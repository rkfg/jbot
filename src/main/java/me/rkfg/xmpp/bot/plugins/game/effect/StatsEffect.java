package me.rkfg.xmpp.bot.plugins.game.effect;

import java.util.HashSet;
import java.util.Set;

import me.rkfg.xmpp.bot.plugins.game.event.EffectEvent;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.StatsEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class StatsEffect extends AbstractEffect {

    private StatsEvent attachStatsEvent;
    private Set<IEvent> attachEffectEvents = new HashSet<>();
    private StatsEvent detachStatsEvent;
    private Set<IEvent> detachEffectEvents = new HashSet<>();

    public StatsEffect(String type, String description) {
        super(type, description);
        attachStatsEvent = new StatsEvent();
        detachStatsEvent = new StatsEvent();
    }

    public StatsEffect() {
        this("statseffect", null);
    }

    public void setStatChange(TypedAttribute<Integer> attr, Integer diff) {
        attachStatsEvent.setAttribute(attr, diff);
        detachStatsEvent.setAttribute(attr, -diff);
    }

    public void addEffect(IEffect effect) {
        attachEffectEvents.add(new EffectEvent(effect));
        detachEffectEvents.add(new EffectEvent(effect.getType()));
    }

    @Override
    public void onBeforeAttach() {
        target.enqueueEvent(attachStatsEvent);
        target.enqueueEvents(attachEffectEvents);
    }

    @Override
    public void onAfterDetach() {
        target.enqueueEvent(detachStatsEvent);
        target.enqueueEvents(detachEffectEvents);
    }

}
