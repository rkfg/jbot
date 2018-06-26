package me.rkfg.xmpp.bot.plugins.game.event;

import me.rkfg.xmpp.bot.plugins.game.Player;

public class CancelEvent extends AbstractEvent {

    public static final String CANCEL = "cancel";

    public CancelEvent() {
        super(CANCEL, Player.WORLD);
    }

    @Override
    public void apply() {
        // just a marker event, doesn't do anything
    }

}
