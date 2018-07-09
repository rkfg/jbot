package me.rkfg.xmpp.bot.plugins.game.event;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;

/**
 * This event is sent after the successful (i.e. not cancelled) battle. Effects may now remove the buffs/debuffs that have been set for the
 * battle duration. This event is not sent if the battle has been cancelled (i.e. any of {@link BattleInviteEvent} has been cancelled).
 * 
 */
public class BattleEndsEvent extends AbstractEvent {

    public static final String TYPE = "battleends";

    public BattleEndsEvent(IPlayer attacker, IPlayer defender) {
        super(TYPE);
        setSource(attacker);
        setTarget(defender);
    }

}
