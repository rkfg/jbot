package me.rkfg.xmpp.bot.plugins.game.event;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;

/**
 * This event is sent after the battle invite has been received and confirmed. Cancelling this event does nothing, it serves as a signal
 * that the battle has been set and is about to begin. Effects may react to this event to setup battle buffs/debuffs and other things.
 *
 */
public class BattleBeginsEvent extends AbstractEvent {

    public static final String TYPE = "battlebegins";

    public BattleBeginsEvent(IPlayer attacker, IPlayer defender) {
        super(TYPE);
        setSource(attacker);
        setTarget(defender);
    }

}
