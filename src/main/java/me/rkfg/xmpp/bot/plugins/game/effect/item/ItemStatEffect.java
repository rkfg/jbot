package me.rkfg.xmpp.bot.plugins.game.effect.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;
import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.effect.AbstractEffect;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.StatsEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class ItemStatEffect extends AbstractEffect implements IUseEffect {

    public static final String TYPE = "stat";

    public ItemStatEffect() {
        super(TYPE, "");
    }

    private static final List<String> KEYS = Arrays.asList("hp", "atk", "def", "str", "prt", "lck", "stm");
    private static final List<TypedAttribute<Integer>> EFFECT_ATTRS = Arrays.asList(HP, ATK, DEF, STR, PRT, LCK, STM);
    private static final List<String> MESSAGES = Arrays.asList("Здоровье", "Способность атаковать", "Способность защищаться", "Сила",
            "Броня", "Удача", "Энергия");

    @Override
    public Collection<IEvent> applyEffect(IGameObject useTarget) {
        StatsEvent statsEvent = new StatsEvent();
        for (int i = 0; i < KEYS.size(); ++i) {
            int idx = i;
            getIntParameterByKey(KEYS.get(i)).ifPresent(stat -> {
                useTarget.log("%s %s на %d.", MESSAGES.get(idx), stat > 0 ? "увеличивается" : "уменьшается", stat);
                statsEvent.setAttribute(EFFECT_ATTRS.get(idx), stat);
            });
        }
        useTarget.enqueueEvent(statsEvent);
        return noEvent();
    }

    @Override
    public Optional<String> getDescription() {
        return super.getDescription().map(d -> d + describe());
    }

    private String describe() {
        return IntStream.range(0, KEYS.size()).mapToObj(idx -> getIntParameterByKey(KEYS.get(idx))
                .map(stat -> String.format("%s %s на %d", MESSAGES.get(idx).toLowerCase(), stat > 0 ? "увеличится" : "уменьшится", stat)))
                .filter(Optional::isPresent).map(Optional::get).reduce(commaReducer).orElse("");
    }
}
