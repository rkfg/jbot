package me.rkfg.xmpp.bot.plugins.game.event;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;

public class BattleBeginsEvent extends AbstractEvent {

    public static final String TYPE = "battlebegins";

    public BattleBeginsEvent(IPlayer attacker, IPlayer defender) {
        super(TYPE);
        setSource(attacker);
        setTarget(defender);
    }
}
