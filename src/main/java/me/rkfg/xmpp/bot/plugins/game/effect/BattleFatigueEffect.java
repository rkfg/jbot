package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;
import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.event.BattleBeginsEvent;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.StatsEvent;

public class BattleFatigueEffect extends AbstractEffect {

    public static final String TYPE = "battlefatigue";
    private int stmCost;

    public BattleFatigueEffect(int stmCost) {
        super(TYPE, "устаёт в бою");
        this.stmCost = stmCost;
    }

    public BattleFatigueEffect() {
        this(5);
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        if (event.isOfType(BattleBeginsEvent.TYPE) && target == event.getSource()) {
            Optional<IPlayer> player = target.as(PLAYER_OBJ).filter(p -> p.getStat(STM) >= stmCost);
            if (player.isPresent()) {
                player.get().enqueueEvent(new StatsEvent(target).setAttributeChain(STM, -stmCost));
            } else {
                target.log("Вы слишком устали, чтобы сражаться.");
                return cancelEvent();
            }
        }
        return super.processEvent(event);
    }

}
