package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.StatsEvent;

public interface IFatigueEffect extends IEffect {

    default void initFatigue(int stmCost) {
        setAttribute(FATIGUE, stmCost);
    }

    default Collection<IEvent> processFatigue(IEvent event, String reactToType, String fatigueMessage) {
        if (event.isOfType(reactToType) && getTarget() == event.getSource()) {
            return getAttribute(FATIGUE).map(stmCost -> getTarget().as(PLAYER_OBJ).filter(p -> p.getStat(STM) >= stmCost).map(player -> {
                player.enqueueEvent(new StatsEvent().setAttributeChain(STM, -stmCost));
                return noEvent();
            }).orElseGet(() -> {
                getTarget().log(fatigueMessage);
                return cancelEvent();
            })).orElseGet(this::noEvent);
        }
        return noEvent();
    }
}
