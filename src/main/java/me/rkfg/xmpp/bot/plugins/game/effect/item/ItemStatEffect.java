package me.rkfg.xmpp.bot.plugins.game.effect.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.effect.AbstractEffect;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.StatsEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class ItemStatEffect extends AbstractEffect implements IUseEffect {

    public static final String TYPE = "stat";

    public ItemStatEffect() {
        super(TYPE, "изменение стат");
    }

    private static final List<String> keys = Arrays.asList("hp", "atk", "def", "str", "prt", "lck", "stm");
    private static final List<TypedAttribute<Integer>> attrs = Arrays.asList(HP, ATK, DEF, STR, PRT, LCK, STM);
    private static final List<String> messages = Arrays.asList("Здоровье", "Способность атаковать", "Способность защищаться", "Сила",
            "Защита", "Удача", "Энергия");

    @Override
    public Collection<IEvent> applyEffect(IGameObject useTarget) {
        StatsEvent statsEvent = new StatsEvent();
        for (int i = 0; i < keys.size(); ++i) {
            int idx = i;
            getIntParameterByKey(keys.get(i)).ifPresent(stat -> {
                useTarget.log(messages.get(idx) + " %s на %d.", stat > 0 ? "увеличивается" : "уменьшается", stat);
                statsEvent.setAttribute(attrs.get(idx), stat);
            });
        }
        useTarget.enqueueEvent(statsEvent);
        return noEvent();
    }

}