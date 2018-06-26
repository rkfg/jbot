package me.rkfg.xmpp.bot.plugins.game.event;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.effect.IAttachDetachEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.SleepEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.SleepEffect.SleepType;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class SetSleepEvent extends AbstractEvent {

    public static final String TYPE = "setsleep";

    public static final TypedAttribute<SleepType> SLEEP_ATTR = TypedAttribute.of("sleeptype");

    public SetSleepEvent(SleepType type, IGameObject source) {
        super(TYPE, source);
        setAttribute(SLEEP_ATTR, type);
    }

    @Override
    public void apply() {
        getAttribute(SLEEP_ATTR).ifPresent(st -> {
            target.log("Персонаж выбирает стратегию сна: " + st.getLocalized());
            ((IAttachDetachEffect) target).attachEffect(new SleepEffect(st, source));
        });
    }

}
