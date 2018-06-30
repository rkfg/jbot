package me.rkfg.xmpp.bot.plugins.game.effect;

import java.util.Arrays;
import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.effect.SleepEffect.SleepType;
import me.rkfg.xmpp.bot.plugins.game.event.CancelEvent;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.SetSleepEvent;

public class NoGuardSleepEffect extends AbstractEffect {

    public static final String TYPE = "noguardsleep";

    public NoGuardSleepEffect(IGameObject source) {
        super(TYPE, "не может спать вполглаза", source);
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        if (event.matchByTypeAttr(SetSleepEvent.TYPE, SetSleepEvent.SLEEP_ATTR, SleepType.GUARD)) {
            target.log("Этот персонаж не может спать вполглаза");
            return Arrays.asList(new CancelEvent());
        }
        return super.processEvent(event);
    }

    @Override
    public void onBeforeAttach() {
        if (target.hasMatchingEffect(SleepEffect.TYPE, SleepEffect.SLEEP_TYPE_ATTR, SleepType.GUARD)) {
            target.enqueueEvent(new SetSleepEvent(SleepType.AWAKE, source));
        }
    }

}
