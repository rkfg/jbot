package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Arrays;
import java.util.List;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.event.StatsEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class AttrStatsEffect extends AbstractEffect {

    protected static final List<String> KEYS = Arrays.asList("hp", "atk", "def", "str", "prt", "lck", "stm");
    protected static final List<TypedAttribute<Integer>> EFFECT_ATTRS = Arrays.asList(HP, ATK, DEF, STR, PRT, LCK, STM);
    protected static final List<String> MESSAGES = Arrays.asList("Здоровье", "Способность атаковать", "Способность защищаться", "Сила",
            "Броня", "Удача", "Энергия");

    public AttrStatsEffect(String type, String description) {
        super(type, description);
    }

    protected void applyEffectToTarget(IGameObject useTarget) {
        StatsEvent statsEvent = new StatsEvent();
        for (int i = 0; i < KEYS.size(); ++i) {
            int idx = i;
            getIntParameterByKey(KEYS.get(i)).ifPresent(stat -> {
                useTarget.log("%s %s на %d.", MESSAGES.get(idx), stat > 0 ? "увеличивается" : "уменьшается", stat);
                statsEvent.setAttribute(EFFECT_ATTRS.get(idx), stat);
            });
        }
        useTarget.enqueueEvent(statsEvent);
    }

}
