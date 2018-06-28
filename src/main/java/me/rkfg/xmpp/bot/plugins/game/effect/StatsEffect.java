package me.rkfg.xmpp.bot.plugins.game.effect;

import java.util.HashSet;
import java.util.Set;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.event.EffectEvent;
import me.rkfg.xmpp.bot.plugins.game.event.StatsEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class StatsEffect extends AbstractEffect {

    private StatsEvent attachStatsEvent;
    private Set<EffectEvent> attachEffectEvents = new HashSet<>();
    private StatsEvent detachStatsEvent;
    private Set<EffectEvent> detachEffectEvents = new HashSet<>();

    public StatsEffect(String type, String description, IGameObject source) {
        super(type, description, source);
        attachStatsEvent = new StatsEvent(source);
        detachStatsEvent = new StatsEvent(source);
    }

    public void setStatChange(TypedAttribute<Integer> attr, Integer diff) {
        attachStatsEvent.setAttribute(attr, diff);
        detachStatsEvent.setAttribute(attr, -diff);
    }

    public void addEffect(IEffect effect) {
        attachEffectEvents.add(new EffectEvent(effect));
        detachEffectEvents.add(new EffectEvent(effect.getType(), source));
    }

    @Override
    public void onAttach() {
        target.enqueueEvent(attachStatsEvent);
        target.enqueueEvents(attachEffectEvents);
    }

    @Override
    public void onDetach() {
        target.enqueueEvent(detachStatsEvent);
        target.enqueueEvents(detachEffectEvents);
    }

}
