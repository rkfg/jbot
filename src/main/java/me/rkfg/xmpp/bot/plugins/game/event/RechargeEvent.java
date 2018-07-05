package me.rkfg.xmpp.bot.plugins.game.event;

public class RechargeEvent extends AbstractEvent {

    public static final String TYPE = "rechargeevent";

    public RechargeEvent() {
        super(TYPE);
    }

}
