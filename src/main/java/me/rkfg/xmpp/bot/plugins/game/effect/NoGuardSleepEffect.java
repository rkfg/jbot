package me.rkfg.xmpp.bot.plugins.game.effect;

import java.util.Arrays;
import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.effect.SleepEffect.SleepType;
import me.rkfg.xmpp.bot.plugins.game.event.CancelEvent;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.SetSleepEvent;

public class NoGuardSleepEffect extends AbstractEffect {

    public NoGuardSleepEffect(IGameObject source) {
        super("noguardsleep", "не может спать вполглаза", source);
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        if (event.getType().equals(SetSleepEvent.SETSLEEP_EVENT_TYPE)
                && event.getAttribute(SetSleepEvent.SLEEP_ATTR).orElse(SleepType.DEEP) == SleepType.GUARD) {
            target.log("Этот персонаж не может спать вполглаза");
            return Arrays.asList(new CancelEvent());
        }
        return super.processEvent(event);
    }

}
