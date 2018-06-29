package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.StatsEvent;
import me.rkfg.xmpp.bot.plugins.game.event.TickEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class BleedEffect extends AbstractEffect {

    public static final String TYPE = "bleed";
    public static final TypedAttribute<Integer> BLEED_RATE_ATTR = TypedAttribute.of("bleedrate");

    public BleedEffect(IGameObject source, int bleedRate) {
        super(TYPE, "истекает кровью", source);
        setAttribute(BLEED_RATE_ATTR, bleedRate);
    }

    public BleedEffect(IGameObject source) {
        this(source, 1);
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        if (event.getType().equals(TickEvent.TYPE)) {
            final StatsEvent bleedEvent = new StatsEvent(source);
            final Integer bleedRate = getAttribute(BLEED_RATE_ATTR).orElse(1);
            bleedEvent.setAttribute(HP, -bleedRate);
            bleedEvent.setDescription("Персонаж теряет " + bleedRate + " hp, истекая кровью");
            return singleEvent(bleedEvent);
        }
        return super.processEvent(event);
    }

}
