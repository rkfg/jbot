package me.rkfg.xmpp.bot.plugins.game.event;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;

public class BattleEndsEvent extends AbstractEvent {

    public static final String TYPE = "battleends";

    public BattleEndsEvent(IPlayer attacker, IPlayer defender) {
        super(TYPE);
        setSource(attacker);
        setTarget(defender);
    }

}
