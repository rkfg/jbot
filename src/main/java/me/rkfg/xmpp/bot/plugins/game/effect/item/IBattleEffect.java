package me.rkfg.xmpp.bot.plugins.game.effect.item;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import me.rkfg.xmpp.bot.plugins.game.effect.IEffect;
import me.rkfg.xmpp.bot.plugins.game.event.BattleAttackEvent;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;

public interface IBattleEffect extends IEffect {

    @Override
    default Collection<IEvent> processEvent(IEvent event) {
        if (event.isOfType(BattleAttackEvent.TYPE)) {
            Set<IEvent> result = new HashSet<>();
            if (event.getAttribute(BattleAttackEvent.SUCCESSFUL).orElse(false)) {
                result.addAll(attackSuccess(event));
                result.addAll(defenceFailure(event));
            } else {
                result.addAll(attackFailure(event));
                result.addAll(defenceSuccess(event));
            }
        }
        return noEvent();
    }

    default Collection<IEvent> defenceSuccess(IEvent event) {
        return noEvent();
    }

    default Collection<IEvent> attackFailure(IEvent event) {
        return noEvent();
    }

    default Collection<IEvent> defenceFailure(IEvent event) {
        return noEvent();
    }

    default Collection<IEvent> attackSuccess(IEvent event) {
        return noEvent();
    }
}
