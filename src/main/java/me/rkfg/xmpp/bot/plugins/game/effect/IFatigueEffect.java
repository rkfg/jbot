package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;
import java.util.function.Predicate;

import me.rkfg.xmpp.bot.plugins.game.event.EffectEvent;
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
        return processFatigue(event, fatigueMessage,
                ev -> ev.getAttribute(EffectEvent.ATTACH_EFFECT).filter(eff -> eff.getType().equals(effectType)).isPresent());
    }

    default Collection<IEvent> processFatigue(IEvent event, String fatigueMessage, Predicate<IEvent> condition) {
        if (getTarget() == event.getSource() && condition.test(event)) {
            return getAttribute(FATIGUE).map(stmCost -> getTarget().as(PLAYER_OBJ).filter(p -> p.getStat(STM) >= stmCost).map(player -> {
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
