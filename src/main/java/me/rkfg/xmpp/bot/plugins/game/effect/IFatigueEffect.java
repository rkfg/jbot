package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.StatsEvent;

public interface IFatigueEffect extends IEffect {

    default void initFatigue(int stmCost) {
        setAttribute(FATIGUE, stmCost);
    }

    default Collection<IEvent> processFatigue(IEvent event, String reactToType, String fatigueMessage) {
        return processFatigue(event, fatigueMessage, e -> e.isOfType(reactToType));
    }

    default Collection<IEvent> processEffectAttachFatigue(IEvent event, String effectType, String fatigueMessage) {
        return processFatigue(event, fatigueMessage, ev -> ev.isAttachEffect(effectType));
    }

    default Collection<IEvent> processFatigue(IEvent event, String fatigueMessage, Predicate<IEvent> condition) {
        return processFatigue(event, fatigueMessage, condition, e -> getAttribute(FATIGUE));
    }

    default Collection<IEvent> processFatigue(IEvent event, String fatigueMessage, Predicate<IEvent> condition,
            Function<IEvent, Optional<Integer>> fatigueFunction) {
        if (getTarget() == event.getSource() && condition.test(event)) {
            return fatigueFunction.apply(event)
                    .map(stmCost -> getTarget().as(PLAYER_OBJ).filter(p -> p.getStat(STM) >= stmCost).map(player -> {
                        return singleEvent(new StatsEvent().setAttributeChain(STM, -stmCost));
                    }).orElseGet(() -> {
                        getTarget().log(fatigueMessage);
                        return cancelEvent();
                    })).orElseGet(this::noEvent);
        }
        return noEvent();
    }

    @Override
    default boolean isVisible() {
        return false;
    }
}
