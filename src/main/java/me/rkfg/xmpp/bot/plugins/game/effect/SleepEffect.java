package me.rkfg.xmpp.bot.plugins.game.effect;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.TickEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class SleepEffect extends AbstractEffect {

    public static final String SLEEP_EFFECT = "sleep";
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

    public SleepEffect(SleepType type, IGameObject source) {
        super(SLEEP_EFFECT, "может спать по ночам", source);
        setAttribute(SLEEP_TYPE_ATTR, type);
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        if (event.getType().equals(TickEvent.TICK_EVENT_TYPE)) {
            target.log("Checking if it's time to sleep... Our type is "
                    + getAttribute(SLEEP_TYPE_ATTR).map(SleepType::getLocalized).orElse(""));
        }
        return super.processEvent(event);
    }

    @Override
    public String getLocalizedName() {
        return super.getLocalizedName() + getAttribute(SLEEP_TYPE_ATTR).map(st -> ", режим " + st.getLocalized()).orElse("");
    }

}
