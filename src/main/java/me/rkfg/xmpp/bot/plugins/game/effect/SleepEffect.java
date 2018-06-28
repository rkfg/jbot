package me.rkfg.xmpp.bot.plugins.game.effect;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.TickEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class SleepEffect extends AbstractEffect {

    public static final String TYPE = "sleep";
    public static final TypedAttribute<SleepType> SLEEP_TYPE_ATTR = TypedAttribute.of("sleeptype");

    public enum SleepType {
        DEEP, GUARD, AWAKE;

        public String getLocalized() {
            switch (this) {
            case DEEP:
                return "глубокий сон";
            case GUARD:
                return "сон вполглаза";
            case AWAKE:
                return "бодрствование";
            }
            return "не определено";
        }
    }

    public SleepEffect(SleepType sleepType, IGameObject source) {
        super(TYPE, "может спать по ночам", source);
        setAttribute(SLEEP_TYPE_ATTR, sleepType);
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        if (event.isOfType(TickEvent.TYPE)) {
            // add sleep logic
        }
        return super.processEvent(event);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + getAttribute(SLEEP_TYPE_ATTR).map(st -> ", режим " + st.getLocalized()).orElse("");
    }

}
