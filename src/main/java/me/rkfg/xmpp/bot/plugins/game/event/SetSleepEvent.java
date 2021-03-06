package me.rkfg.xmpp.bot.plugins.game.event;

import me.rkfg.xmpp.bot.plugins.game.effect.SleepEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.SleepEffect.SleepType;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class SetSleepEvent extends AbstractEvent {

    public static final String TYPE = "setsleep";

    public static final TypedAttribute<SleepType> SLEEP_ATTR = TypedAttribute.of("sleeptype");

    public SetSleepEvent(SleepType type) {
        super(TYPE);
        setAttribute(SLEEP_ATTR, type);
        setDescription("Персонаж выбирает стратегию сна: " + type.getLocalized());
    }

    @Override
    public void apply() {
        getAttribute(SLEEP_ATTR).ifPresent(st -> {
            target.enqueueAttachEffect(new SleepEffect(st));
            super.apply();
        });
    }

}
