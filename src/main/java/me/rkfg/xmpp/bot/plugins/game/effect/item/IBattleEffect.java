package me.rkfg.xmpp.bot.plugins.game.effect.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.effect.IEffect;
import me.rkfg.xmpp.bot.plugins.game.event.BattleAttackEvent;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;

public interface IBattleEffect extends IEffect {

    @Override
    default Collection<IEvent> processEvent(IEvent event) {
        Set<IEvent> result = new HashSet<>();
        if (event.isOfType(BattleAttackEvent.TYPE)) {
            if (event.getAttribute(BattleAttackEvent.SUCCESSFUL).orElse(false)) {
                result.addAll(attackSuccess(event));
                result.addAll(defenceFailure(event));
            } else {
                result.addAll(attackFailure(event));
                result.addAll(defenceSuccess(event));
            }
        }
        return result;
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

    default Collection<IEvent> withPlayers(IEvent event, BiFunction<IPlayer, IPlayer, Collection<IEvent>> f) {
        return event.getSource().as(PLAYER_OBJ)
                .flatMap(attacker -> event.getTarget().as(PLAYER_OBJ).map(defender -> f.apply(attacker, defender)))
                .orElseGet(this::noEvent);
    }
}
