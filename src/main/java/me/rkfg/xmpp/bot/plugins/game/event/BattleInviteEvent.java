package me.rkfg.xmpp.bot.plugins.game.event;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;

/**
 * This event is sent to the attacker and defender before the battle begins. Each one may cancel it to prevent the battle from happening for
 * whatever reason. Effects should not change the stats or set other buffs/debuffs on receiving this event as the battle might be cancelled
 * by the other party. However, if the very attempt to start the battle should cause some stat change it's ok to do so (reducing stamina does
 * exactly that no matter if the battle has been cancelled or not)
 * 
 */
public class BattleInviteEvent extends AbstractEvent {

    public static final String TYPE = "battleinvite";

    public BattleInviteEvent(IPlayer attacker, IPlayer defender) {
        super(TYPE);
        setSource(attacker);
        setTarget(defender);
    }
}
