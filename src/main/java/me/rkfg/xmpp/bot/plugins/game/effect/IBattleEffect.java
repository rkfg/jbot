package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.event.BattleAttackEvent;
import me.rkfg.xmpp.bot.plugins.game.event.BattleBeginsEvent;
import me.rkfg.xmpp.bot.plugins.game.event.BattleEndsEvent;
import me.rkfg.xmpp.bot.plugins.game.event.BattleInviteEvent;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;

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
        if (event.isOfType(BattleInviteEvent.TYPE)) {
            result.addAll(battleInvite(event));
        }
        if (event.isOfType(BattleBeginsEvent.TYPE)) {
            result.addAll(battleBegins(event));
        }
        if (event.isOfType(BattleEndsEvent.TYPE)) {
            result.addAll(battleEnds(event));
        }
        return result;
    }


    default Collection<IEvent> battleInvite(IEvent event) {
        return noEvent();
    }

    default Collection<IEvent> battleBegins(IEvent event) {
        return noEvent();
    }

    default Collection<IEvent> battleEnds(IEvent event) {
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

    default Collection<IEvent> withPlayers(IEvent event, BiFunction<IPlayer, IPlayer, Collection<IEvent>> f) {
        return event.getSource().as(PLAYER_OBJ)
                .flatMap(attacker -> event.getTarget().as(PLAYER_OBJ).map(defender -> f.apply(attacker, defender)))
                .orElseGet(this::noEvent);
    }

    default boolean imDefender(IEvent event) {
        return event.getTarget() == getTarget();
    }

    default boolean imAttacker(IEvent event) {
        return event.getSource() == getTarget();
    }

    default boolean myOwnerIsDefender(IEvent event) {
        return getOwner().filter(p -> p == event.getTarget()).isPresent();
    }

    default boolean myOwnerIsAttacker(IEvent event) {
        return getOwner().filter(p -> p == event.getSource()).isPresent();
    }

    default Optional<IGameObject> getOwner() {
        return getTarget().as(ITEM_OBJ).flatMap(IItem::getOwner);
    }
}
