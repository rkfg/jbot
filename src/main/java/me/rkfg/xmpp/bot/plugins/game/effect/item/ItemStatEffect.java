package me.rkfg.xmpp.bot.plugins.game.effect.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.IntStream;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.effect.AttrStatsEffect;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;

/**
 * Changes stats on item usage, used for items
 *
 */
public class ItemStatEffect extends AttrStatsEffect implements IUseEffect {

    public static final String TYPE = "stat";

    public ItemStatEffect() {
        super(TYPE, "");
    }

    @Override
    public Collection<IEvent> applyEffect(IGameObject useTarget) {
        applyEffectToTarget(useTarget);
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
