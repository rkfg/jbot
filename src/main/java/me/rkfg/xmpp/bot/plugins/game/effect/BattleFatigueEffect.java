package me.rkfg.xmpp.bot.plugins.game.effect;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.event.BattleBeginsEvent;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;

public class BattleFatigueEffect extends AbstractEffect implements IFatigueEffect {

    public static final String TYPE = "battlefatigue";

    public BattleFatigueEffect(int stmCost) {
        super(TYPE, "устаёт в бою");
        initFatigue(stmCost);
    }

    public BattleFatigueEffect() {
        this(5);
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        return processFatigue(event, BattleBeginsEvent.TYPE, "Вы слишком устали, чтобы сражаться.");
    }

    @Override
    public boolean isVisible() {
        return false;
    }

}
