package me.rkfg.xmpp.bot.plugins.game.effect;

import java.util.Collection;
import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.event.BattleBeginsEvent;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.StatsEvent;

public class BattleFatigueEffect extends AbstractEffect {

    private static final String TYPE = "battlefatigue";
    private int stmCost;

    public BattleFatigueEffect(IGameObject source, int stmCost) {
        super(TYPE, "устаёт в бою", source);
        this.stmCost = stmCost;
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        if (event.isOfType(BattleBeginsEvent.TYPE) && target == event.getSource()) {
            Optional<IPlayer> player = target.asPlayer().filter(p -> p.getStat(IPlayer.STM) >= stmCost);
            if (player.isPresent()) {
                player.get().enqueueEvent(new StatsEvent(target).setAttributeChain(IPlayer.STM, -stmCost));
            } else {
                target.log("Вы слишком устали, чтобы сражаться.");
                return cancelEvent();
            }
        }
        return super.processEvent(event);
    }

}
