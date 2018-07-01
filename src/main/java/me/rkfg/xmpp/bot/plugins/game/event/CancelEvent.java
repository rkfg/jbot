package me.rkfg.xmpp.bot.plugins.game.event;

public class CancelEvent extends AbstractEvent {

    public static final String TYPE = "cancel";

    public CancelEvent() {
        super(TYPE);
    }

    @Override
    public void apply() {
        // just a marker event, doesn't do anything
    }

}
